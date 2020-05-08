 package com.mobile.pesome;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import org.apache.cordova.DroidGap;
 
 public class Pesome extends DroidGap {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //setContentView(R.layout.activity_pesome);
        super.loadUrl("file:///android_asset/www/index.html");
        
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_pesome, menu);
         return true;
     }
 }
