 package com.chips;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
 public class InventoryActivity extends HomeBarActivity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.top_bar);
        
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
         (Context.LAYOUT_INFLATER_SERVICE);

        View subview = inflater.inflate(R.layout.inventory, null);
        
         LinearLayout mainView = (LinearLayout)findViewById(R.id.subview);
        mainView.addView(subview);
         
         setupIntents();
     }
     
     private void setupIntents() {
         addFoodToInventoryIntent = new Intent(this, AddFoodToInventoryActivity.class);
         searchFoodIntent = new Intent(this, SearchFoodActivity.class);
     }
     
     // super calls for basic activity-changing functions.
     @Override
     protected void onStart() {
         super.onStart();
         // The activity is about to become visible.
     }
    
     @Override
     protected void onResume() {
         super.onResume();
         // The activity has become visible (it is now "resumed").
     }
     
     @Override
     protected void onPause() {
         super.onPause();
         // Another activity is taking focus (this activity is about to be "paused").
     }
     
     @Override
     protected void onStop() {
         super.onStop();
         // The activity is no longer visible (it is now "stopped")
     }
     
     @Override
     protected void onDestroy() {
         super.onDestroy();
         // The activity is about to be destroyed.
     }
     
     /*
      * Callback function for when 'Scan Barcode' is clicked
      */
     public void scanBarcodeClicked(View view) {
         IntentIntegrator integrator = new IntentIntegrator(this);
         integrator.initiateScan();
     }
     
     // Callback for barcode scanner activity
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
         Toast toast;
         IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
         if (scanResult != null) {
             toast = Toast.makeText(getApplicationContext(), scanResult.toString(), Toast.LENGTH_LONG);
             
         } else {
             toast = Toast.makeText(getApplicationContext(), "no result", Toast.LENGTH_LONG);
         }
         // else continue with any other code you need in the method
         toast.show();
     }
     
     public void addFoodToInventoryClicked(View view) {
         startActivity(addFoodToInventoryIntent);
     }
     
     public void searchFoodClicked(View view) {
         startActivity(searchFoodIntent);
     }
     
     private Intent addFoodToInventoryIntent;
     private Intent searchFoodIntent;
 }
