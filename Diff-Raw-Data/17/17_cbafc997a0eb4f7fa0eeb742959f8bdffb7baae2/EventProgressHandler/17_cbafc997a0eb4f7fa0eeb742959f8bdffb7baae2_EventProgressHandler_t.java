 package clock.sched;
 
 import java.util.concurrent.TimeUnit;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.util.Log;
 import clock.db.DbAdapter;
 import clock.db.Event;
 import clock.outsources.GoogleTrafficHandler.TrafficData;
 import clock.outsources.dependencies.WeatherModel;
 
 public class EventProgressHandler{
 	
 	private static final long GO_OUT_REMINDER_TIME = 1000 * 60 * 15;	// '15 minutes to go' reminder
 	private static boolean userHasBeenNotified;
 	private static long userHasBeenWakedUp;
 	private static final int NOTIFICATION_ID = 1;
 	private static final int WAKEUP_ID = 2;
 	private static final int CRITICAL_ID = 3;
 	
 		
 	/**
 	 * This method should be called from clock handler when setting alarm.
 	 * It calculate what should be notified to user and returns the times left to next notification in milliseconds 
 	 */
 	synchronized public static long handleEventProgress(Context context, Event event, long timesLeftToGoOut, long arrangeTime)
 	{
 		Log.d("PROGRESS", "Handling event progress from clock handler");
 		loadDetailsFromEvent(event);
 		long timeLeftToWakeUp = -1;
 		long timeLeftToNotify = -1;
 
 		if (arrangeTime > 0 && userHasBeenWakedUp == 0) //Wake up is needed
 		{
 			timeLeftToWakeUp = getTimesLeftToWakeUp(timesLeftToGoOut, arrangeTime);
 			if (timeLeftToWakeUp <= 0)
 			{
 				wakeupUser(context, event, arrangeTime);
 			}
 		}
 		
 		if (timeLeftToWakeUp > 0)	//Still not waked up, return with time left to wake up
 		{
 			saveDetailsToEvent(event, context);
			Log.d("PROGRESS", "Times left to wakeup is " + TimeUnit.MILLISECONDS.toMinutes(timeLeftToWakeUp) + " Min");
 			return timeLeftToWakeUp;
 		}
 			
 		
 		// Else we calculate time left to notification
 		timeLeftToNotify = timesLeftToGoOut - GO_OUT_REMINDER_TIME;
 		
 		// Remind user to go out if needed
 		if (timeLeftToNotify <= 0)
 		{
 			String msg = "Time to go out in " + TimeUnit.MILLISECONDS.toMinutes(timesLeftToGoOut) +  " Minutes";			
 			notifyUser(context, msg);
 		}
 		
 		if (timeLeftToNotify > 0)	//Still not notify to user, return with time left to notify
 		{
 			saveDetailsToEvent(event, context);
			Log.d("PROGRESS", "Times left to notify is " + TimeUnit.MILLISECONDS.toMinutes(timeLeftToNotify) + " Min");
 			return timeLeftToNotify;
 		}
 		
 		
 		//If we got here, no duture notification to user with current event
 		saveDetailsToEvent(event, context);
 		Log.d("PROGRESS", "User got all messages from progress handler");
 		return -1;
 	}
 
 
 	/**
 	 * Should be called from location handler, after location changed update received.
 	 */
 	synchronized public static void handleEventProgress(Context context, Event event, Location location)
 	{
 		Log.d("PROGRESS", "Handling event progress from location handler");
 		loadDetailsFromEvent(event);
 		
 		if (userHasBeenWakedUp != 0)
 		{
 			try
 			{
 				String origin = GoogleAdapter.getOrigin(context, location);
 				WeatherModel weatherData = GoogleAdapter.getWeatherModel(origin);
 				AlarmsManager alarmsManager = new AlarmsManager(context, new DbAdapter(context));
 				int arrangementTime = getArrangementTime();
 				alarmsManager.UserGotOut(event, arrangementTime, weatherData);
 			}
 			catch (Exception e) {
 				Log.e("PROGRESS", "Can't update User Got Out, error: " + e.getMessage());
 			}
 		}
 		else
 		{
 			try
 			{
 				TrafficData trafficData = GoogleAdapter.getTrafficData(context, event, location);
 				long diffTime = TimeUnit.SECONDS.toMillis(trafficData.getDuration()) - event.getTimesLeftToEvent();
 				
 				if (diffTime > 0)
 				{
 					String msg = "You're going to be late for event - " + event.toString();
 					criticalMsg(context, msg);
 				}
 				else if (diffTime <= GO_OUT_REMINDER_TIME)
 				{
 					String msg = "Time to go out in " + TimeUnit.MILLISECONDS.toMinutes(diffTime) +  " Minutes";
 					notifyUser(context, msg);
 				}
 				
 			}
 			catch (Exception e) {
 				Log.e("PROGRESS", "Error while trying to check if user is late: " + e.getMessage());
 			}
 		}
 		
 		saveDetailsToEvent(event, context);
 		
 	}
 	
 	/**
 	 * 
 	 * @return Time in mili.
 	 */
 	private static int getArrangementTime() {
 		long currentTime = System.currentTimeMillis();
 		return (int)(currentTime - userHasBeenWakedUp);
 	}
 
 
 	synchronized private static long getTimesLeftToWakeUp(long timesLeftToGoOut, long arrangeTime) 
 	{
 		long timeToWakeUp = timesLeftToGoOut - arrangeTime;
 		
		return timeToWakeUp - System.currentTimeMillis();
 	}
 	
 	synchronized private static void wakeupUser(Context context, Event event, long arrangeTimeInMillis)
 	{
 		Log.d("PROGRESS", "Waking up the user");
 		
 		String msg = "Times to wake up, Arrange time is " 
 				+ TimeUnit.MILLISECONDS.toMinutes(arrangeTimeInMillis)
 				+ " Minutes";
 		
 		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		Notification notification = getBasicNotification(context, msg);
 		notification.flags |= Notification.FLAG_INSISTENT;		//This will cause annoying notification
 		
 		notificationManager.notify(WAKEUP_ID, notification);
 		
 		userHasBeenWakedUp = System.currentTimeMillis();
 		
 		// Reset location handler to notify after 100 meters movement
 		LocationHandler.cancelLocationListener(context, event);
 		LocationHandler.setLocationListener(context, event, 1000);	//1000 since update is set to 10% of distance
 		
 		Log.d("PROGRESS", "User has been waked up with message: " + msg);
 	}
 	
 	synchronized private static void notifyUser(Context context, String msg)
 	{
 		if (userHasBeenNotified)
 			return;
 		
 		Log.d("PROGRESS", "Notifying user with: " + msg);
 		
 		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		Notification notification = getBasicNotification(context, msg);
 		notification.defaults |= Notification.DEFAULT_SOUND;
 
 		notificationManager.notify(NOTIFICATION_ID, notification);
 		Log.d("PROGRESS", "User has been notified");
 		
 		userHasBeenNotified = true;
 	}
 
 
 	synchronized private static void criticalMsg(Context context, String msg) 
 	{
 		if (userHasBeenNotified)
 			return;
 		
 		Log.d("PROGRESS", "Critical message to user with: " + msg);
 		
 		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		Notification notification = getBasicNotification(context, msg);
 		notification.defaults |= Notification.DEFAULT_SOUND;
 				
 		notificationManager.notify(CRITICAL_ID, notification);
 		Log.d("PROGRESS", "User has been critical messaged");
 		
 		userHasBeenNotified = true;		
 	}
 	
 	synchronized private static Notification getBasicNotification(Context context, String msg) 
 	{
 		int icon = R.drawable.icon;
 		long when = System.currentTimeMillis();		//For showing the notification now
 		Notification notification = new Notification(icon, msg, when);
 		String title = "AppDate";
 		Intent intent = new Intent(context, EventProgressHandler.class);
 		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
 		
 		notification.setLatestEventInfo(context, title, msg, pendingIntent);
 		
 		return notification;
 	}
 
 	synchronized private static void loadDetailsFromEvent(Event event) {
 		userHasBeenNotified = event.isUserHasBeenNotified();
 		userHasBeenWakedUp = event.getUserHasBeenWakedUp();
 		
 	}
 	
 	/**
 	 * This will save the event's userHasBeenNotified and userHasBeenWakedUp.
 	 * The event then will be updated in DB.
 	 * @param event
 	 * @param context
 	 */
 	synchronized private static void saveDetailsToEvent(Event event, Context context) {
 		event.setUserHasBeenNotified(userHasBeenNotified);
 		event.setUserHasBeenWakedUp(userHasBeenWakedUp);
 		DbAdapter dbAdapter = new DbAdapter(context);
 		dbAdapter.updateEvent(event);
 	}
 
 
 	synchronized public static void goOutNotification(Context context, String msg) 
 	{
 		Log.d("PROGRESS", "Go out notification: " + msg);
 		
 		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		Notification notification = getBasicNotification(context, msg);
 		notification.defaults |= Notification.DEFAULT_SOUND;
 
 		notificationManager.notify(NOTIFICATION_ID, notification);
 		Log.d("PROGRESS", "User has been notified to go out");
 	}
 }
