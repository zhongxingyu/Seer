 package com.example.tmr;
 
 import android.app.IntentService;
 import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
 import android.util.Log;
 
 public class MyService3 extends IntentService {
 
  final static String TAG = "MyService3";
 
   public MyService3() {
     super(TAG);
   }
 
   @Override
   protected void onHandleIntent(Intent intent) {
     Log.d(TAG, "onHandleIntent");
    TimerActivity.mRingtone.play();
   }
 }
