 package com.example.raddeapp;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
import android.util.Log;
 
 /**
  * Created by Radovan on 2013-07-02.
  */
 public class BackgroundService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Radde123 Service: onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("Radde123 Service: onStartCommand");
        Log.i("Radde123","Radde123");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Radde123 Service: onDestroy");
    }

     public IBinder onBind(Intent intent) {
         return null;
     }
 }
