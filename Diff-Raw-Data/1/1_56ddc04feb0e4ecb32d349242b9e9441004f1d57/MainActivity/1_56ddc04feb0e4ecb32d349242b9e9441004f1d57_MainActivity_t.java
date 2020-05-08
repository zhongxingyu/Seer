 package com.ruby_in_tab;
 import org.apache.cordova.DroidGap;
 import android.os.Bundle;
 
 public class MainActivity extends DroidGap {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     	super.setIntegerProperty("splashscreen", R.drawable.splash);
         super.loadUrl("file:///android_asset/www/index.html",10000);

     }
     
 }
