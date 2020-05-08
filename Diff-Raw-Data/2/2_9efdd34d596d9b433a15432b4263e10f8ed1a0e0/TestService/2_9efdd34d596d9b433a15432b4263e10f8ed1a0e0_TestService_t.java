 package com.schoentoon.parallel.intentservice;
 
 import android.content.Intent;
 import android.util.Log;
 
 public class TestService extends ParallelIntentService {
   private static final String TAG = TestService.class.getSimpleName();
 
   public TestService() {
     super(TAG);
   }
 
   @Override
   protected void onHandleIntent(Intent intent) {
     Log.d(TAG,"onHandleIntent(Intent); Started, thread id: " + Thread.currentThread().getId());
    Log.d(TAG,"onHandleIntent(Intent); Sleeping for 60 seconds, thread id: " + Thread.currentThread().getId());
     try {
       Thread.sleep(60 * 1000);
     } catch (InterruptedException e) {
       e.printStackTrace();
     }
     Log.d(TAG,"onHandleIntent(Intent); Done, thread id: " + Thread.currentThread().getId());
   }
 
 }
