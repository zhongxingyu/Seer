 package org.is.mynamspace;
 
 import android.app.Activity;
 import org.apache.cordova.*;
 import android.os.Bundle;
 
 public class HelloCordova2Activity extends DroidGap {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         super.setBooleanProperty("keepRunning", false);
        super.loadUrl("file:///android_asset/www/landing_page.html");
     }
 }
