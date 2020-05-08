 package com.example.zonedhobbitsportfolio;
 
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.ViewGroup;
 import android.app.Activity;
 import android.graphics.Color;
 
 public class SplashScreenActivity extends Activity {
 
   private long splashDelay = 6000; //6 segundos
   private static Activity activity;
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     
     GifWebView view = new GifWebView(this, "file:///android_asset/ring.gif"); 
    setContentView(R.layout.activity_splash_screen);
     
     view.setBackgroundColor(Color.BLACK);
     
     activity = this;
   }
 
   public static void finishSplash() {
 	  activity.finish();
   }
   
 }
