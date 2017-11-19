package com.wallet.crypto.trustapp.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wallet.crypto.trustapp.R;
import com.wallet.crypto.trustapp.controller.Controller;
import com.wallet.crypto.trustapp.controller.EthplorerService;
import com.wallet.crypto.trustapp.model.EPAddressInfo;
import com.wallet.crypto.trustapp.model.EPToken;
import com.wallet.crypto.trustapp.model.EPTokenInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TokenListActivity extends AppCompatActivity {

    private static String TAG = "TOKENS";
    private String mAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mAddress = getIntent().getStringExtra(Controller.KEY_ADDRESS);

        RecyclerView recyclerView = findViewById(R.id.token_list);

        setupRecyclerView(recyclerView);
    }

    private void setupRecyclerView(final @NonNull RecyclerView recyclerView) {
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.ethplorer.io")
                    .build();

            EthplorerService service = retrofit.create(EthplorerService.class);

            Call<EPAddressInfo> call = service.getAddressInfo(mAddress, "freekey");

            call.enqueue(new Callback<EPAddressInfo>() {

                @Override
                public void onResponse(Call<EPAddressInfo> call, Response<EPAddressInfo> response) {
                    try {
                        Log.d(TAG, Integer.toString(response.body().getTokens().size()));
                        EPAddressInfo addressInfo = response.body();
                        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(addressInfo.getTokens()));
                    } catch (Exception e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }

                @Override
                public void onFailure(Call<EPAddressInfo> call, Throwable t) {
                    Log.e("ERROR", t.toString());
                    Toast.makeText(TokenListActivity.this.getApplicationContext(), "Error contacting token service. Check internet connection.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<EPToken> mValues;

        public SimpleItemRecyclerViewAdapter(List<EPToken> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.token_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);

            EPToken token = holder.mItem;
            EPTokenInfo info = token.getTokenInfo();

            holder.mNameView.setText(info.getName());
            holder.mSymbolView.setText(info.getSymbol());
            BigDecimal balance = new BigDecimal(token.getBalance());
            balance = balance.setScale(2, RoundingMode.HALF_UP);
            balance = balance.divide(new BigDecimal(Math.pow(10, info.getDecimals())));
            holder.mBalanceView.setText(balance.toString());
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mNameView;
            public final TextView mSymbolView;
            public final TextView mBalanceView;

            public EPToken mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mNameView = view.findViewById(R.id.name);
                mSymbolView = view.findViewById(R.id.symbol);
                mBalanceView = view.findViewById(R.id.balance);
            }

            @Override
            public String toString() {
                return super.toString();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
