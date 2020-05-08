 package com.yhsif.purge;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.text.format.Time;
 import android.util.Log;
 
 import java.util.Map;
 
 public class AutoPurge extends BroadcastReceiver {
 
 	// alarm at 00:05:00 everyday
 	public static int HOUR = 0;
 	public static int MINUTE = 5;
 	public static int SECOND = 0;
 
 	@Override public void onReceive(Context context, Intent intent) {
		if (intent.getAction() == null ||
				!intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
 			SharedPreferences setting = context.getSharedPreferences(PurgeActivity.PREF, 0);
 			if (!setting.getBoolean(PurgeActivity.KEY_AUTO_ENABLED, false)) {
 				Log.e(PurgeActivity.TAG, "onReceive: auto purge disabled!");
 				return;
 			}
 			Map<String, ?> values = setting.getAll();
 			if (values.containsKey(PurgeActivity.KEY_AUTO_DAYS) &&
 					values.containsKey(PurgeActivity.KEY_AUTO_CALL_LOG) &&
 					values.containsKey(PurgeActivity.KEY_AUTO_SMS) &&
 					values.containsKey(PurgeActivity.KEY_AUTO_LOCKED_SMS)) {
 				int days = setting.getInt(PurgeActivity.KEY_AUTO_DAYS, -1);
 				boolean call = setting.getBoolean(PurgeActivity.KEY_AUTO_CALL_LOG, true);
 				boolean sms = setting.getBoolean(PurgeActivity.KEY_AUTO_SMS, true);
 				boolean locked = setting.getBoolean(PurgeActivity.KEY_AUTO_LOCKED_SMS, false);
 				PurgeActivity.purge(context, days, call, sms, locked);
 			} else {
 				Log.e(PurgeActivity.TAG, "onReceive: settings missing.");
 			}
 		}
 
 		registerAlarm(context);
 	}
 
 	public static void registerAlarm(Context context) {
 		Time fire = new Time();
 		fire.setToNow();
 		fire.hour = HOUR;
 		fire.minute = MINUTE;
 		fire.second = SECOND;
 
 		if (fire.toMillis(true) <= System.currentTimeMillis())
 			fire.monthDay++;
 
 		long time = fire.toMillis(true);
 
 		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 		// One day is not always 24 hours (consider DST), so we use set instead of setRepeating,
 		// and set the next alarm in onReceive.
 		alarm.set(AlarmManager.RTC_WAKEUP, time, getSender(context));
 
 		Log.d(PurgeActivity.TAG, String.format("registered alarm with time %d", time));
 	}
 
 	public static void unregisterAlarm(Context context) {
 		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 		alarm.cancel(getSender(context));
 	}
 
 	protected static PendingIntent getSender(Context context) {
 		Intent intent = new Intent(context, AutoPurge.class);
 		return PendingIntent.getBroadcast(context, 0, intent, 0);
 	}
 
 }
