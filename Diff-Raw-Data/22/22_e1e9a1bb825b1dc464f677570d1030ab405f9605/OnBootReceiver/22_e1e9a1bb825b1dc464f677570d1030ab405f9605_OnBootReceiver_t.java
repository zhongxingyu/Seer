 package csci498.bybeet.lunchlist;
 
 import java.util.Calendar;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
import android.util.Log;
 
 public class OnBootReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		setAlarm(context);
 	}
 	
 	public static void setAlarm(Context context) {
 		AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 		Calendar cal = Calendar.getInstance();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		String time = prefs.getString("alarm_time", "12:00");
 		
 		cal.set(Calendar.HOUR_OF_DAY, TimePreference.getHour(time));
 		cal.set(Calendar.MINUTE, TimePreference.getMinute(time));
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		
 		if(cal.getTimeInMillis() < System.currentTimeMillis()) {
 			cal.add(Calendar.DAY_OF_YEAR, 1);
 		}
 		
		Log.e("OnBootReceive", "Alarm set and sent to AlarmManager.");
		
 		manager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, getPendingIntent(context));
 		
 	}
 	
 	public static void cancelAlarm(Context context) {
 		AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 		manager.cancel(getPendingIntent(context));
 	}
 	
 	private static PendingIntent getPendingIntent(Context context) {
 		Intent i = new Intent(context, OnAlarmReceiver.class);
 		return PendingIntent.getBroadcast(context, 0, i, 0);
 	}
 
 }
