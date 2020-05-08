 package edu.vub.at.nfcpoker.smartwatch;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class ExtensionReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
        Log.d("wePoker-sw ExtensionReceiver", "onReceive: " + intent.getAction());
         intent.setClass(context, WePokerSWService.class);
         context.startService(intent);
 	}
 
 }
