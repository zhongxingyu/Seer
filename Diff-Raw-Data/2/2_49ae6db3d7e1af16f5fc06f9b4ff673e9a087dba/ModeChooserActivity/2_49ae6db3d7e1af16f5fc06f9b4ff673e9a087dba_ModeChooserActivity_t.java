 package com.davidjennes.ElectroJam;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.view.View;
 
 public class ModeChooserActivity extends Activity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         // strict mode
         if (getResources().getBoolean(R.bool.developer_mode))
             StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
     }
     
     public void chooseInstrument(View view) {
     	// TODO: choose instruments
     	Intent intent = new Intent(this, InstrumentActivity.class);
     	startActivity(intent);
     }
     
     public void startServer(View view) {
     	Intent intent = new Intent(this, ServerMode.class);
     	startActivity(intent);
     }
     
     public void startLooper(View view) {
    	Intent intent = new Intent(this, TestActivity.class);
     	startActivity(intent);
     }
 }
