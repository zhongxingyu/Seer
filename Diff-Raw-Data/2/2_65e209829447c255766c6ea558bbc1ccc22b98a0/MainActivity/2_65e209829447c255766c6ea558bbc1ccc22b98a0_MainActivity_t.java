 package com.example.first;
 
 import org.apache.cordova.DroidGap;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.view.Menu;
 
 public class MainActivity extends DroidGap {
 
     @Override
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        super.loadUrl("file:///android_asset/www/index2.html");
         super.setIntegerProperty("loadUrlTimeoutValue", 10000);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig) 
     { super.onConfigurationChanged(newConfig); }
     
 }
