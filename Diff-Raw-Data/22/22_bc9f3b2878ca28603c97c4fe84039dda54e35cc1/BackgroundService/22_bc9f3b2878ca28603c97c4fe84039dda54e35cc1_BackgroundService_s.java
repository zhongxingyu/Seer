 package com.example.raddeapp;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 
 /**
  * Created by Radovan on 2013-07-02.
  */
 public class BackgroundService extends Service {
     public IBinder onBind(Intent intent) {
         return null;
     }
 }
