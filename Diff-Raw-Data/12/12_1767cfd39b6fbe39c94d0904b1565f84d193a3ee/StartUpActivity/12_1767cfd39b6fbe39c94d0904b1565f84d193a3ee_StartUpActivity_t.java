 package com.r2mobile.mytravelasia;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 /**
 * An activity which serves as the startup entry point of the app. It shows an overview about Philippines, and some ads at
 * the bottom.
 *
  * @author avendael
  */
 public class StartUpActivity extends Activity {
     private static final String TAG = StartUpActivity.class.getSimpleName();
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.startup_activity);
     }
 }
