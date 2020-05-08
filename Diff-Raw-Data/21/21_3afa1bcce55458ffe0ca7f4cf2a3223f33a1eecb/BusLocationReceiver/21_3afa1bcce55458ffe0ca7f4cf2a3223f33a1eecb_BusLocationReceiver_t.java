 package ru.ratadubna.dubnabus;
 
 import java.util.ArrayList;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import com.commonsware.cwac.wakeful.WakefulIntentService;
 
 public class BusLocationReceiver extends BroadcastReceiver {
 	static boolean loadingPermission = false;

 	@Override
 	public void onReceive(Context ctxt, Intent i) {
 		if (i.getAction().equals(BusLocationService.ACTION_BUS_LOCATION)) {
 			Intent locServiceIntent = new Intent(ctxt, BusLocationService.class);
 			locServiceIntent.putExtra("ids", i.getIntegerArrayListExtra("ids"));
 			loadingPermission = true;
 			WakefulIntentService.sendWakefulWork(ctxt, locServiceIntent);
 		} else if (i.getAction().equals(BusLocationService.ACTION_BUS_LOADED)) {
 			if (loadingPermission)
				((DubnaBusActivity) ctxt).addBuses();
 		}
 	}
 
 	static void scheduleAlarm(Context ctxt, ArrayList<Integer> ids) {
 		AlarmManager mgr = (AlarmManager) ctxt
 				.getSystemService(Context.ALARM_SERVICE);
 		Intent i = new Intent(BusLocationService.ACTION_BUS_LOCATION);
 		i.putExtra("ids", ids);
 		PendingIntent pi = PendingIntent.getBroadcast(ctxt, 1337, i,
 				PendingIntent.FLAG_UPDATE_CURRENT);
		mgr.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 10000,
				pi);
 	}
 }
