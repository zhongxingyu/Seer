 package com.onedatapoint;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 
 public class SplashActivity extends Activity {
     // Set the display time, in milliseconds (or extract it out as a configurable parameter)
    private final int SPLASH_DISPLAY_LENGTH = 1000;
     private boolean isSplashEnabled = true;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.splash);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         if (isSplashEnabled) {
             new Handler().postDelayed(new Runnable() {
                 public void run() {
                     //Finish the splash activity so it can't be returned to.
                     SplashActivity.this.finish();
                     // Create an Intent that will start the main activity.
                     Intent mainIntent = new Intent(SplashActivity.this, CuringDepressionActivity.class);
                     SplashActivity.this.startActivity(mainIntent);
                 }
             }, SPLASH_DISPLAY_LENGTH);
         } else {
             // if the splash is not enabled, then finish the activity immediately and go to main.
             finish();
             Intent mainIntent = new Intent(SplashActivity.this, CuringDepressionActivity.class);
             SplashActivity.this.startActivity(mainIntent);
         }
     }
 }
