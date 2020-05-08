 package clock.sched;
 
 import java.io.UnsupportedEncodingException;
 
 import android.content.Context;
 import android.util.Log;
 import clock.db.DbAdapter;
 import clock.db.Event;
 import clock.db.Event.eComparison;
 import clock.exceptions.CantGetLocationException;
 import clock.exceptions.IllegalAddressException;
 import clock.exceptions.InternetDisconnectedException;
 import clock.exceptions.OutOfTimeException;
 import clock.outsources.GoogleTrafficHandler.TrafficData;
 import clock.outsources.GoogleWeatherHandler;
 import clock.outsources.dependencies.WeatherModel;
 
 /**
  * Manager All alarms service in DataBase 
  * And the Android alarm services.
  * Manage all the internal alarm scheduling logic.
  *
  */
 public class AlarmsManager 
 {
 	/**
 	 * Handles arrangment times updating and recieving.
 	 * @author Idan
 	 *
 	 */
 	private class ArrangeTimeManager 
 	{				
 		private Context context;
 		private DbAdapter db;
 			
 		public ArrangeTimeManager(Context context, DbAdapter db) 
 		{
 			super();
 			this.context = context;
 			this.db = db;
 		}
 
 		/**
 		 * Should be called after user has left to the event.
 		 */
 		public void UserGotOut(Event event, WeatherModel weatherData)
 		{
 //			eCondition condition = conditionToEnum(weatherData.getCondition());
 //			db.addRecord(event, condition);			
 		}
 		
 		/**
 		 * 
 		 * @return The arrangment time in mintues.
 		 */
 		public int GetArrangmentTime(WeatherModel weatherData, String dayOfWeek)
 		{
 			return 0;
 		}
 	}
 	
 	/**
 	 * Weather conditions enum
 	 * @author Idan
 	 *
 	 */
 	public enum eCondition{
 		WET,
 		DRY
 	}
 	
 	private DbAdapter dbAdapter;
 	private Context context;
 	private Event latestEvent;
 	private ArrangeTimeManager arrangeTimeManager;
 	
 	public AlarmsManager(Context context, DbAdapter dbAdapter) 
 	{
 		super();
 		this.dbAdapter = dbAdapter;
 		this.context = context;
 		this.arrangeTimeManager = new ArrangeTimeManager(context, dbAdapter);
 	}
 	
 	/**
 	 * Informs the alarm manager about a new event.
 	 * saves the alarm in db, manage the alarm set/cancel in case needed.
 	 * @param newEvent 
 	 * @throws Exception 
 	 **/
 	public void newEvent(Event newEvent) throws Exception
 	{		
 		if (!GoogleAdapter.isInternetConnected(context))
 			throw new InternetDisconnectedException();
 		
 		if (!GoogleAdapter.isLegalAddress(newEvent.getLocation()))
 			throw new IllegalAddressException();
 
 		try
 		{
 			TrafficData trafficData = GoogleAdapter.getTrafficData(context, newEvent, null);
			if(newEvent.isAfterNow() && newEvent.timeFromNow(trafficData.getDuration()) < 0) // its not possible to get there ! 
			{
				throw new OutOfTimeException();
			}
 			
 			dbAdapter.open();
 			refreshLastEvent();
 			dbAdapter.insertEvent(newEvent);
 			if(newEvent.isAfterNow() &&
 							(latestEvent == null || Event.compareBetweenEvents(newEvent, latestEvent) == eComparison.BEFORE))
 			{
 				int arrageTime = getArrangmentTime(newEvent);
 				if (latestEvent != null)
 				{
 					ClockHandler.cancelEventAlarm(context, latestEvent);
 					LocationHandler.cancelLocationListener(context, latestEvent);
 				}
 				
 				this.latestEvent = newEvent;
 				long durationTime = trafficData.getDuration();
 				if (durationTime < 0)
 				{
 					throw new CantGetLocationException();
 				}
 				ClockHandler.setAlarm(context, latestEvent, ((int)durationTime + arrageTime));
 				LocationHandler.setLocationListener(context, latestEvent, trafficData.getDistance());
 			}
 		}
 		catch (Exception ex)
 		{
 			Log.e("Alarm manager", "Create new event has failed");
 			throw ex;	//Roll exception forward
 		}
 		finally
 		{
 			dbAdapter.close();
 		}
 	}
 
 	public int getArrangmentTime(Event newEvent) throws UnsupportedEncodingException 
 	{
 		int arrangeTime = 0;
 		if(newEvent.daysFromNow() > 3 && newEvent.getWithAlarmStatus() == true) // only if event time < 3 days from now we have weather data.
 		{
 			GoogleWeatherHandler gw = new GoogleWeatherHandler();
 			WeatherModel weather = gw.processWeatherRequest(newEvent.getLocation());
 			String eventDayName = newEvent.getDayName();
 			arrangeTime = arrangeTimeManager.GetArrangmentTime(weather, eventDayName);					
 		}
 		
 		return arrangeTime;
 	}
 
 	/**
 	 * Request the alarm manager to delete an event.
 	 * Means the event will be deleted from the database,
 	 * If alarm was schedule for the event it will be cancelled,
 	 * And a new alarm for the next event will be schedule.
 	 * @param event - the event for deletion
 	 * @throws Exception
 	 */
 	public void deleteEvent(Event event) throws Exception
 	{
 		//TODO: how many refresh and latest and new and what the hell?!?
 		dbAdapter.open();
 		refreshLastEvent();
 		dbAdapter.deleteEvent(event);
 		if (latestEvent != null && latestEvent.equals(event))
 		{
 			// Pull latest event from DB after the current one has been deleted
 			latestEvent = dbAdapter.getNextEvent();
 			if (latestEvent != null)
 			{
 				try
 				{
 					TrafficData trafficData = GoogleAdapter.getTrafficData(context, latestEvent, null);
 					int timeToArrange = getArrangmentTime(latestEvent);
 					ClockHandler.setAlarm(context, latestEvent, (int)(timeToArrange + trafficData.getDuration()));
 					LocationHandler.setLocationListener(context, latestEvent, trafficData.getDistance());
 				}
 				catch (Exception ex)
 				{
 					Log.e("Alarm manager", "Delete event has failed");
 					dbAdapter.close();
 				}
 			}
 			
 			ClockHandler.cancelEventAlarm(context, event);
 			LocationHandler.cancelLocationListener(context, event);
 		}
 		dbAdapter.close();
 	}
 	
 	/**
 	 * Updating data member (if needed) 'lastestEvent' from the database.
 	 * Should be called with open db connection.
 	 */
 	private void refreshLastEvent()
 	{
 		latestEvent = dbAdapter.getNextEvent();		
 	}
 
 }
