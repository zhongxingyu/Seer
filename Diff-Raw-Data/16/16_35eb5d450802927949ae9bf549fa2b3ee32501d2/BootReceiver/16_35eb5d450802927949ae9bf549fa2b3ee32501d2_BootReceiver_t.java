 package com.marakana.android.yamba.svc;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
import android.util.Log;
 
 
 public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BOOT";
 
     @Override
     public void onReceive(Context ctxt, Intent i) {
        Log.d(TAG, "boot received");
         YambaService.startPolling(ctxt);
     }
 }
