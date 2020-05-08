 package com.marakana.android.yamba.svc;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 
 
 public class BootReceiver extends BroadcastReceiver {
 
     @Override
     public void onReceive(Context ctxt, Intent i) {
         YambaService.startPolling(ctxt);
     }
 }
