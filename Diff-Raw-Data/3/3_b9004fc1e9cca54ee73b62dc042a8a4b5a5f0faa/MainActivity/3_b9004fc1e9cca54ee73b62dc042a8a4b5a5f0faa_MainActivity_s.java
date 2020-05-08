 package com.suddenlybunt.toprider.sms;
 
 import android.os.Bundle;
 
 import com.phonegap.DroidGap;
 
 public class MainActivity extends DroidGap {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //setContentView(R.layout.main);
         super.loadUrl("file:///android_asset/www/before/index.html");
        
     }
 }
