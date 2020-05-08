 package com.playphone.sdk.example;
 
 import android.app.Application;
 import org.acra.*;
 import org.acra.annotation.*;
 
 @ReportsCrashes(formKey = "dHJFM241VDZfR3ZQSmZqa2MwRU9CblE6MQ")
 public class MyApplication extends Application {
 	
 	@Override
     public void onCreate() {
         // The following line triggers the initialization of ACRA
         ACRA.init(this);
         super.onCreate();
     }
 }
