 package com.barestodo.android.app;
 
 import android.app.Application;
 import android.content.Context;
 
/**
 * use for access application context statically
 */
 public class MyApplication extends Application {
 
     private static Context context;
 
     public void onCreate(){
         super.onCreate();
         MyApplication.context = getApplicationContext();
     }
 
     public static Context getAppContext() {
         return MyApplication.context;
     }
 }
