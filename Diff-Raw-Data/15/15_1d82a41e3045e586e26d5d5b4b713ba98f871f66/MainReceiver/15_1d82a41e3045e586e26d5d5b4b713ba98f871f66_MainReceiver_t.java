 package jp.shuri.android.pedometer;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class MainReceiver extends BroadcastReceiver {
 	private final String TAG = "MainReceiver";
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Log.d(TAG, "onReceive");
 /*
         if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
         */
             Log.d(TAG, "BOOT_COMPLETED");
            Log.d(TAG, "intent.getAction() : " + intent.getAction());
             Intent i = new Intent(context, MainService.class);
             context.startService(i);
             /*
         } else if (intent.getAction().equals("android.intent.action.REBOOT")) {
             Log.d(TAG, "REBOOT");
 
         } else if (intent.getAction().equals("android.intent.action.SHUTDOWN")) {
             Log.d(TAG, "SHUTDOWN");
 
         } else if (intent.getAction().equals("android.intent.action.DATE_CHANGED")) {
         	Log.d(TAG, "DATE_CHANGED");
         }
         */
 	}
 
 }
