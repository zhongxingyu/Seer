 package org.minyanmate.minyanmate.services;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import org.minyanmate.minyanmate.contentprovider.MinyanMateContentProvider;
 import org.minyanmate.minyanmate.database.MinyanSchedulesTable;
 import org.minyanmate.minyanmate.models.MinyanSchedule;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.SystemClock;
 import android.util.Log;
 
 /**
  * A class which is automatically queries the {@link MinyanMateContentProvider}
  * for all Minyans marked as being active in {@link MinyanSchedulesTable#COLUMN_IS_ACTIVE}.
  */
 public class OnBootReceiver extends BroadcastReceiver{
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		
 		Log.d("OnBootReceiver", "Inside OnBootReceiver");
 		
 		Cursor minyanSchedulesCursor = context.getContentResolver().query(
 				MinyanMateContentProvider.CONTENT_URI_TIMES, 
 				null, 
 				MinyanSchedulesTable.COLUMN_IS_ACTIVE + "=?", new String[] { "1" }, 
				MinyanSchedulesTable.COLUMN_ID + " ASC");
 		
 		// Get alarm manager to set recurring alarms for minyans
 //		AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 		
 		MinyanRegistrar.registerMinyanEvents(context, minyanSchedulesCursor);
 		
 //		while(minyanSchedulesCursor.moveToNext()) {
 //			
 //			// Translate to a schedule, calculate the next occurance, and then schedule it with the alarm manager
 //			MinyanSchedule sched = MinyanSchedule.schedFromCursor(minyanSchedulesCursor);
 //			
 //			MinyanRegistrar.registerMinyanEvent(context, sched);
 //			
 //    		Intent i =new Intent(context, OnMinyanAlarmReceiver.class);
 //    		i.putExtra("requestCode", sched.getId());
 //    		PendingIntent pi = PendingIntent.getBroadcast(context, sched.getId(), i, 0);
 //
 //    		Calendar date = new GregorianCalendar();
 //    		date.set(Calendar.HOUR_OF_DAY, sched.getHour());
 //    		date.set(Calendar.MINUTE, sched.getMinute());
 //    		date.set(Calendar.DAY_OF_WEEK, sched.getDayNum());
 //    		if (date.getTimeInMillis() < System.currentTimeMillis()) 
 //    			date.add(Calendar.WEEK_OF_YEAR, 1);
 //    		date.add(Calendar.SECOND, (int) (-1*sched.getSchedulingWindowLength()));
 //    		
 //    		mgr.setRepeating(AlarmManager.RTC_WAKEUP,
 //                      date.getTimeInMillis(),
 //                      AlarmManager.INTERVAL_DAY*7,
 //                      pi);
 //			 
 //		}
 	}
 
 }
