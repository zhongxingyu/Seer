 package com.madhackerdesigns.neverlate;
 
 import java.util.Calendar;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 
 public class StartupReceiver extends BroadcastReceiver {
 	
 	private static final int REQUEST_CODE = 19327;
 	private PreferenceHelper mPrefs;
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		// Un-comment the following to startup a background service at boot.
 //		Intent serviceIntent = new Intent(context, NeverLateService.class);
 //		serviceIntent.putExtra(NeverLateService.EXTRA_TASK, "STARTUP");
 //		WakefulIntentService.sendWakefulWork(context, serviceIntent);
 		
 		// first pull shared prefs and get settings
 		mPrefs = new PreferenceHelper(context);
 //		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 //		boolean enabled = prefs.getBoolean("neverlate_enabled", true);
 //		long travelTimeFreq = Long.parseLong(prefs.getString("traveltime_freq", DEFAULT_TRAVELTIME_FREQ));
 //		long locationFreq = Long.parseLong(prefs.getString("location_freq", DEFAULT_LOCATION_FREQ));
 		
 		// next destroy any old alarms, just to be sure we have a clean slate and don't run anything twice
 		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 		Intent travelTimeIntent = new Intent(context, TravelTimeReceiver.class);
 		PendingIntent travelTimePending = PendingIntent.getBroadcast(context, REQUEST_CODE, travelTimeIntent, 
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		alarm.cancel(travelTimePending);
 //		Intent locationIntent = new Intent(context, LocationReceiver.class);
 //		PendingIntent locationPending = PendingIntent.getBroadcast(context, REQUEST_CODE, locationIntent, 
 //		PendingIntent.FLAG_UPDATE_CURRENT);
 //		alarm.cancel(locationPending);
 		
 		// if NeverLate is enabled, set new alarms to run at the appointed times
 		Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.MINUTE, 1);
 		if (mPrefs.isNeverLateEnabled()) {
//			Toast.makeText(context, "NeverLate is enabled", Toast.LENGTH_SHORT).show();
 			alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
 					mPrefs.getTraveltimeFreq(), travelTimePending);
 //			alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
 //					locationFreq, locationPending);
 		} else {
//			Toast.makeText(context, "NeverLate is disabled", Toast.LENGTH_SHORT).show();
 		}
 		
 	}
 
 }
