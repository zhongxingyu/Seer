 package com.ahumellihuk.fuelprices;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.TableLayout;
 import android.widget.TextView;
 
 import com.ahumellihuk.fuelprices.tools.FetchData;
 
 public class PricesFragment extends Fragment {
 	
 	TableLayout table;
     SharedPreferences sharedPref;
     ProgressDialog progress;
     private Activity activity;
     private List<FuelPricesRow> rows = new ArrayList<FuelPricesRow>();
     private Double[][] prices;
     TextView litersAmount;
     SeekBar litersInput;
     private Integer currentLitersAmount;
 
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, 
         Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         return inflater.inflate(R.layout.table_layout, container, false);
     }
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
         activity = getActivity();
         table = (TableLayout)getView().findViewById(R.id.pricesTable);
         sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
 
         getView().findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 resetTable();
             }
         });
 
         getView().findViewById(R.id.calculate).setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 calculatePrices();
             }
         });
 
         litersAmount = (TextView) getView().findViewById(R.id.litersAmount);
 
         litersInput = ((SeekBar) getView().findViewById(R.id.litersInput));
         litersInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 currentLitersAmount = progress + 1;
                 litersAmount.setText(currentLitersAmount + " " + getString(R.string.liters));
             }
 
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {
 
             }
 
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {
 
             }
         });
 
 		if (sharedPref.getBoolean("useLocalData", false)) {
 			Date date = new Date();
 			boolean needToUpdate = false;
 			if (sharedPref.getInt("updateYear", 0) < date.getYear())
 				needToUpdate = true;
 			else if (sharedPref.getInt("updateMonth", 0) < date.getMonth())
 				needToUpdate = true;
 			else if (sharedPref.getInt("updateDay", 0) < date.getDate())
 				needToUpdate = true;
 			
 			if (needToUpdate) {
 				fetchData();
 			} else {
 				if (sharedPref.contains("prices")) {
                     prices = FetchData.decode(sharedPref.getString("prices", null));
 					buildTable(prices, false);
 				} else if (savedInstanceState != null && savedInstanceState.containsKey("prices")) {
 				    prices = FetchData.decode(savedInstanceState.getString("prices"));
                     buildTable(prices, false);
 				} else {
 					fetchData();
 				}
 			}
 		} else {
 			fetchData();
 		}
 	}
 
     private void buildTable(Double[][] prices, boolean updateData) {
         for (FuelPricesRow row : rows) {
             table.removeView(row);
         }
         rows.clear();
 
         //Lowest price finding
         Double petrol95LowestPrice = 0.0, petrol98LowestPrice = 0.0, dieselLowestPrice = 0.0;
         for (Double price : prices[0]) {
             if (price != null) {
                 petrol95LowestPrice = price;
                 break;
             }
         }
         for (Double price : prices[1]) {
             if (price != null) {
                 petrol98LowestPrice = price;
                 break;
             }
         }
         for (Double price : prices[2]) {
             if (price != null) {
                 dieselLowestPrice = price;
                 break;
             }
         }
         for (Double price : prices[0]) {
             if (price != null) {
                 petrol95LowestPrice = Math.min(petrol95LowestPrice, price);
             }
         }
         for (Double price : prices[1]) {
             if (price != null) {
                 petrol98LowestPrice = Math.min(petrol98LowestPrice, price);
             }
         }
         for (Double price : prices[2]) {
             if (price != null) {
                 dieselLowestPrice = Math.min(dieselLowestPrice, price);
             }
         }
 
 
         for (int i = 0; i<prices[0].length; i++) {
             Double petrol95 = prices[0][i];
             Double petrol98 = prices[1][i];
             Double diesel = prices[2][i];
             FuelPricesRow row = new FuelPricesRow(activity);
             row.setPetrol95Price(petrol95);
             row.setPetrol98Price(petrol98);
             row.setDieselPrice(diesel);
             //Set logo
             switch (i) {
                 case 0:
                     row.setLogo(R.drawable.krooning);
                     break;
                 case 1:
                     row.setLogo(R.drawable.eurooil);
                     break;
                 case 2:
                     row.setLogo(R.drawable.alexela);
                     break;
                 case 3:
                     row.setLogo(R.drawable.neste);
                     break;
                 case 4:
                     row.setLogo(R.drawable.olerex);
                     break;
                 case 5:
                     row.setLogo(R.drawable.statoil);
                     break;
                 case 6:
                     row.setLogo(R.drawable.fiveplus);
                     break;
             }
             //Set lowest price color
             if (petrol95 != null && petrol95.equals(petrol95LowestPrice)) {
                 row.setPetrol95LowestPrice();
             }
             if (petrol98 != null && petrol98.equals(petrol98LowestPrice)) {
                 row.setPetrol98LowestPrice();
             }
             if (diesel != null && diesel.equals(dieselLowestPrice)) {
                 row.setDieselLowestPrice();
             }
             table.addView(row);
             rows.add(row);
         }
         if (updateData) {
             SharedPreferences.Editor editor = sharedPref.edit();
             Date date = new Date();
             editor.putInt("updateDay", date.getDate());
             editor.putInt("updateMonth", date.getMonth());
             editor.putInt("updateYear", date.getYear());
             editor.putString("prices", FetchData.encode(prices));
             editor.commit();
         }
     }
 
     @Override
 	public void onSaveInstanceState(Bundle outState) {
         if (prices != null) {
             outState.putString("prices", FetchData.encode(prices));
         }
 		super.onSaveInstanceState(outState);
 	}
     protected boolean checkNetworkState() {
         ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
         if (networkInfo != null && networkInfo.isConnected())
             return true;
         else
             return false;
     }
 
     protected void fetchData() {
         if (checkNetworkState()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(FetchData.TASK_FINISHED);
            activity.registerReceiver(new BroadcastReceiver() {

                @Override
                public void onReceive(Context arg0, Intent intent) {
                    progress.dismiss();
                    prices = FetchData.decode(intent.getStringExtra("result"));
                    buildTable(prices, true);
                    activity.unregisterReceiver(this);
                }
            }, filter);
             FetchData task = new FetchData();
             task.execute(this);
             progress = new ProgressDialog(activity, ProgressDialog.STYLE_SPINNER);
             progress.setIndeterminate(true);
             progress.setTitle(getString(R.string.pleaseWait));
             progress.setMessage(getString(R.string.fetchingData));
             progress.show();
         } else {
             AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
             alertDialog.setTitle("Alert");
             alertDialog.setMessage("No Internet Access!");
             alertDialog.show();
         }
     }
 
     protected void calculatePrices() {
         for (FuelPricesRow row : rows) {
             row.calculatePriceForLiters(currentLitersAmount);
         }
     }
 
     protected void resetTable() {
         for (FuelPricesRow row : rows) {
             row.resetPrices();
         }
         litersInput.setProgress(0);
     }
  
 }
