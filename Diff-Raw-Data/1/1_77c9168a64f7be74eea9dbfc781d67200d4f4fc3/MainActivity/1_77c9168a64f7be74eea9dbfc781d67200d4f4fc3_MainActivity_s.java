 package com.blackbourna.softeng;
 
 import org.apache.cordova.DroidGap;
 
 import android.os.Bundle;
 import android.view.Menu;
 public class MainActivity extends DroidGap {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //setContentView(R.layout.activity_main);
         super.loadUrl("file:///android_asset/www/soft_eng.html");
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
