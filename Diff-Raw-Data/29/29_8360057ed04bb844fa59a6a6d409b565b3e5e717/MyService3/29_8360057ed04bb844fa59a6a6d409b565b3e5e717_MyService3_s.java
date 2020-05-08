 package com.example.tmr;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.util.Log;
 
 public class MyService3 extends IntentService {
 
  final static String TAG = "DEBUG";
 
   public MyService3() {
     super(TAG);
   }
 
   @Override
   protected void onHandleIntent(Intent intent) {
     Log.d(TAG, "onHandleIntent");
    // A[炷
   }
 }
