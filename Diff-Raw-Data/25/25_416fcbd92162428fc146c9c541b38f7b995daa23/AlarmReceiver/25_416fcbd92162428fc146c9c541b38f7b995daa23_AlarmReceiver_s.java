 package com.coffeebean.waterreminder;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class AlarmReceiver extends BroadcastReceiver {
 	public void onReceive(Context context, Intent i) {
		Log.d(AlarmReceiver.class.getSimpleName(), "received alarm = ");
 		context.startService(new Intent(context, WaterReminderService.class));
 	}
 }
