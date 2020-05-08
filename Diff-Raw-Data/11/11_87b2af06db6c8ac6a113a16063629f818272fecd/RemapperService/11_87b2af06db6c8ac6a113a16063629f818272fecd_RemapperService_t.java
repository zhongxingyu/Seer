 package com.albertarmea.handsfreeactions;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Created by aarmea on 5/24/13.
  */
 public class RemapperService extends Service {
    public static final String TAG = "RemapperService";
 
     @Override
     public void onCreate() {
         super.onCreate();
         Toast.makeText(this, "RemapperService created", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Service created");
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "Service started");
         return START_STICKY;
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
        Log.i(TAG, "Service stopped");
         Toast.makeText(this, "RemapperService destroyed", Toast.LENGTH_LONG).show();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 }
