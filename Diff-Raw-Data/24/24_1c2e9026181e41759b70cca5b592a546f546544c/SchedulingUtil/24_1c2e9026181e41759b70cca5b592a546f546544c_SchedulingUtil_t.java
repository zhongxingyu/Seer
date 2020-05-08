 package net.anzix.android.celebrate;
 
 import java.util.Calendar;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 
 /**
  * Utility to schedule the update service.
  * 
  * @author elek
  */
 public class SchedulingUtil {
 
 	public static void schedule(Context context) {
 
 		PendingIntent newPending = makeControlPendingIntent(context);
 		AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.AM_PM, Calendar.AM);
 		cal.set(Calendar.HOUR, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		alarms.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, newPending);
 
 	}
 
 	protected static PendingIntent makeControlPendingIntent(Context context) {
 		Intent active = new Intent(context, NotifyService.class);
 		return (PendingIntent.getService(context, 0, active, PendingIntent.FLAG_UPDATE_CURRENT));
 	}
 
 }
