 package csc498.slowery.lunchlist;
 
 import java.util.Calendar;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 public class OnBootReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		setAlarm(context);
 	}
 	
 	public static void setAlarm(Context context) {
 		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 		Calendar calendar = Calendar.getInstance();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		String time = prefs.getString("alarm_time","12:00");
 		calendar.set(Calendar.HOUR_OF_DAY, TimePreference.getHour(time));
 		calendar.set(Calendar.MINUTE, TimePreference.getMinute(time));
 		calendar.set(Calendar.SECOND,0);
 		calendar.set(Calendar.MILLISECOND,0);
 		manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, getPendingIntent(context));
 	}
 	
 	public static void cancelAlarm(Context context) {
 		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 		manager.cancel(getPendingIntent(context));
 	}
 	
 	private static PendingIntent getPendingIntent(Context context) {
 		Intent intent = new Intent(context, OnAlarmReceiver.class);
 		return PendingIntent.getBroadcast(context, 0, intent, 0);
 	}
 }
