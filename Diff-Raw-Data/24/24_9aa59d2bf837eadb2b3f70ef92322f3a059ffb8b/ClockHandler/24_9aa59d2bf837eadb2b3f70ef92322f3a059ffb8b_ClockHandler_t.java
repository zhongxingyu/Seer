 package clock.sched;
 
 import java.util.Calendar;
 import java.util.concurrent.TimeUnit;
 
 import clock.db.DbAdapter;
 import clock.db.Event;
 import clock.outsources.GoogleTrafficHandler.TrafficData;
 import clock.outsources.GoogleWeatherHandler;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioRecord.OnRecordPositionUpdateListener;
 import android.text.format.Time;
 import android.util.Log;
 
 public class ClockHandler extends BroadcastReceiver 
 {
 	// Stop when event is 1 minutes ahead
 	private static final long TIMES_UP = (60 * 1000);
 
 	public static void setAlarm(Context context, Event event, long durationTime, long arrangeTime)
 	{
 		setAlarm(context, event, durationTime, arrangeTime, false);
 	}	
 	
 	/**
 	 *
 	 * @param context
 	 * @param event The event to schedule
 	 * @param extraTime Extra milliseconds to subtract from the event actual time. 
 	 * @param setAfterEvent - if true, then the next alarm will be set to 1 min after the event -
 	 * should be used after the time to the event is after TIMES_UP.
 	 */
 	private static void setAlarm(Context context, Event event,  long durationTime, long arrangeTime, boolean setAfterEvent) 
 	{
 		// First notify event progress handler
 		long timeLeftToGoOut = event.getTimesLeftToEvent() - durationTime;
 		long progressHandlerTiming = EventProgressHandler.handleEventProgress(context, event, timeLeftToGoOut, arrangeTime);
 		
 		long extraTime = durationTime + arrangeTime;
 		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 		PendingIntent pendingIntent = getPendingIntent(context, event);
 		long alarmMiliSecond = setAfterEvent == true ? event.toCalendar().getTimeInMillis() + TIMES_UP : 
 														calNextAlarm(event, durationTime, progressHandlerTiming);
 		// for debug
 		Calendar c = Calendar.getInstance();
 		c.setTimeInMillis(alarmMiliSecond);
 		Log.d("ALARM", "setAlarm: " + c.getTime());
 		alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmMiliSecond, pendingIntent);	
 	}
 		
 	private static void setAfterEventAlarm(Context context, Event event) 
 	{
 		Log.d("ALARM", "Inside setAfterAlarm");
 		setAlarm(context, event, 0, 0, true);
 	}
 	
 	private static long calNextAlarm(Event event, long durationTime, long progressHandlerTiming) 
 	{
 		Calendar calander = Calendar.getInstance();
 		Time t = new Time();
 		t.setToNow();
 		long currentTime = t.toMillis(false);
 		calander.setTimeInMillis(currentTime);
 		Log.d("ALARM", "Current Time: " + calander.getTime());
 		
 		calander = event.toCalendar();
 		calander.add(Calendar.MILLISECOND, (int)-durationTime);
 		Log.d("ALARM", "Time To go out: " + calander.getTime());
 		long miliToGetOut = calander.getTimeInMillis();
 		long miliToNextAlarm = ((miliToGetOut - currentTime) / 2) + currentTime;
 
 		Calendar debugCal = Calendar.getInstance();
 		debugCal.setTimeInMillis(miliToNextAlarm);
		Log.d("ALARM", "BEFORE CHECK -- Clock Handler Next Alarm: " + debugCal.getTime());
 		
 		//In case user assigned event that time to go out has already passed
 		if (miliToNextAlarm < 0)
 		{
 			miliToNextAlarm = 30 * 1000; //This will cause immediately response in 30 seconds 
 		}
 		
 //		Avoid passing progress handler timing if its greater then 0
 		if (progressHandlerTiming > 0 && miliToNextAlarm > progressHandlerTiming + currentTime)
 		{
			Log.d("ALARM", "Progress Handler Wants Alarm in: " + TimeUnit.MILLISECONDS.toMinutes(progressHandlerTiming) + " Min");
 			miliToNextAlarm = progressHandlerTiming + currentTime;
 		}
		
		debugCal.setTimeInMillis(miliToNextAlarm);
		Log.d("ALARM", "AFTER CHECK -- Clock Handler Next Alarm: " + debugCal.getTime());
 
 		return miliToNextAlarm;
 	}
 	
 	private static PendingIntent getPendingIntent(Context context, Event event)
 	{
 		Intent intent = new Intent(context, ClockHandler.class);
 		intent.putExtra("eventStr", event.encodeToString());
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
 		return pendingIntent;
 	}
 
 	// moved into Event member funcion.
 	//	public static long getTimesLeftToEvent(Event event)	
 	@Override
 	public void onReceive(Context context, Intent i) 
 	{
 		Log.d("ALARM", "Inside OnReceive:");
 		DbAdapter db = new DbAdapter(context);
 //		db.open();
 		Event nextEvent = db.getNextEvent();
 //		db.close();
 		if (nextEvent != null)
 		{
 			long timesLeftToEvent = nextEvent.getTimesLeftToEvent();
 			try
 			{
 				AlarmsManager am = new AlarmsManager(context, db);
 				TrafficData traficHandler = GoogleAdapter.getTrafficData(context, nextEvent, null);
 				long travelTime = traficHandler.getDuration();
 				if(travelTime == -1){
 					Log.d("ALARM", "travel time is -1 !!!"); 
 				}
 
 				long arrangeTime = am.getArrangmentTime(nextEvent);				
 				long timesLeftToGoOut = timesLeftToEvent - travelTime;				
 				
 				Log.d("ALARM", String.valueOf(timesLeftToGoOut));
 				// If the event time to go out has not passed yet
 				if (timesLeftToGoOut - arrangeTime > TIMES_UP){
 					setAlarm(context, nextEvent, travelTime, arrangeTime);
 				}
 				else // ClockHandler move to the next event.
 				{
 					EventProgressHandler.goOutNotification(context, "It is time to go out, Drive safely");
 					Log.d("ALARM", "TIMES IS UP!");
 					setAfterEventAlarm(context, nextEvent);
 				}	
 			}
 			catch (Exception ex)
 			{
 				Log.e("ALARM", "Failed to set next alarm for event: " + nextEvent.toString()
 						 + "... With Error: " + ex.getStackTrace().toString());
 			}
 		
 		}
 		
 	}
 
 	public static void cancelEventAlarm(Context context, Event event) 
 	{
 		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 		PendingIntent pendingIntent = getPendingIntent(context, event);
 		alarmMgr.cancel(pendingIntent);	
 		Log.d("ALARM", "ALARM has been canceled for event: " + event.toString());
 	}
 }
