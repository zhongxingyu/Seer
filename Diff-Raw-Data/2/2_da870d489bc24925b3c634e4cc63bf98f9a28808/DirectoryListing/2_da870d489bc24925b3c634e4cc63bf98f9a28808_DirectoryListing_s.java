 package com.phonegap.directorylisting;
 
 import android.os.Bundle;
 
 import com.phonegap.DroidGap;
 
 public class DirectoryListing extends DroidGap {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         super.loadUrl("file:///android_asset/www/index.html",1000);
        super.setIntegerProperty("splashscreen",R.drawable.splash);
     }
 }
