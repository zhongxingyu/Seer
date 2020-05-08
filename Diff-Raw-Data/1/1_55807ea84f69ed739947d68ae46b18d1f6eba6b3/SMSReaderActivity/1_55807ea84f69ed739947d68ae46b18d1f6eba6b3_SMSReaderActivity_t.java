 package org.flying.lions;
 
 import android.os.Bundle;
 import org.apache.cordova.*;
import org.flying.lions.R;
 
 public class SMSReaderActivity extends DroidGap {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //Splash screen
         super.setIntegerProperty("splashscreen", R.drawable.splash);
         setContentView(R.layout.main);
         
         //@param 2nd parameter is time splash screen is shown
         super.loadUrl("file:///android_asset/www/index.html",2000);
     }
 }
