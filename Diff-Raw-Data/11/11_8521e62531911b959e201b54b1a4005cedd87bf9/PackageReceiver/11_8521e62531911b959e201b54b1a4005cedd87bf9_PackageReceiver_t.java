 package com.kaist.crescendo.alarm;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class PackageReceiver extends BroadcastReceiver {
 	private final String TAG = "PackageReceiver";
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		if(intent.getAction().equals(BootReceiver.INSTALL_ACTION)) {
 			Log.v(TAG, "onReceive invoked AlarmService when app is installed");
 			context.startService(new Intent(BootReceiver.START_ACTION));
 		
 		} else if(intent.getAction().equals(BootReceiver.REPLACED_ACTION)) {
 			Log.v(TAG, "onReceive invoked AlarmService when app is replaced");
 			context.startService(new Intent(BootReceiver.START_ACTION));
 			
 		} else if(intent.getAction().equals(BootReceiver.REMOVED_ACTION)) {
 			Log.v(TAG, "onReceive invoked AlarmService when app is removed");			
 			context.stopService(new Intent(BootReceiver.STOP_ACTION));
 			
 		} else if(intent.getAction().equals(BootReceiver.ADDED_ACTION)) {
			Log.v(TAG, "onReceive invoked AlarmService when app is added");
			context.startService(new Intent(BootReceiver.START_ACTION));
 		}		
 	}
 }
