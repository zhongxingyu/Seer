 package de.c3t;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 
 public class StartupReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent indent) {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("de.c3t.OnlineService");
		context.startService(serviceIntent);
 	}
 
 }
