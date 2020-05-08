 package com.nikog.metropolia.schedule;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlarmManager;
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 public class UpdateService extends IntentService {
 	
 	private int appWidgetId;
 	
 	public UpdateService() {
 		super("MetroSchedIntentService");
 		Log.d(WidgetProvider.TAG, "UpdateService launched");
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		Log.d(WidgetProvider.TAG, "Handling intent");
 		
 		// Get widgetId from extras
 		Bundle extras = intent.getExtras();
 		this.appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
 		
 		List<Event> resultList = getEvents(3);
 		
 		// Default/minimum update time is 30 minutes from now
 		long updateTimeMillis = System.currentTimeMillis() + 1800000L;
 		
 		if(resultList != null) {
 			updateTimeMillis = resultList.get(0).getEnd();
 		}
 
 		// If next event is tomorrow, fetch new data
 		if((resultList == null || resultList.get(0).getStart() > DateUtils.tomorrow())) {
 			if(isConnected()) {
 				// Get URI from shared preferences
 				URL url = getURL(appWidgetId);
 				
 				// Fetch and parse data
 				String jsonString = getJSONSchedule(url);
 				parseJSONToDatabase(jsonString);
 				
 				resultList = getEvents(3);

				updateTimeMillis = resultList.get(0).getEnd();
 			}
 		}
 		
 		// Trim events that are not on the same day
 		resultList = trimEvents(resultList);
 		
 		buildUpdate(appWidgetId, resultList);
 		
 		// Create timer for next update
 		createAlarmTimer(this, appWidgetId, updateTimeMillis);
 	}
 	
 
 	/**
 	 * Build and deploy updated view from the supplied event list.
 	 * 
 	 * @param widgetId Widget id.
 	 * @param eventList	List of Event-objects to deploy to the widget view.
 	 */
 	public void buildUpdate(int widgetId, List<Event> eventList) {
 		Log.d(WidgetProvider.TAG, "Building");
 		RemoteViews view = new RemoteViews(getPackageName(), R.layout.widget);
 		
 		// Placeholder click action
 		Intent intent = new Intent(this, WidgetProvider.class);
 		intent.setAction(WidgetProvider.METRO_ACTION_CLICK);
 		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
 
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 
 		view.setOnClickPendingIntent(R.id.widgetlayout, pendingIntent);
 		// ----------------------
 		
 		String start, end, roomId, subject, viewText;		
 		int[] textViews = WidgetProvider.TEXTVIEWS;		
 		int listSize = eventList.size(); // 2
 		Event event;
 		
 		for(int i=0; i<textViews.length; i++) { // 3
 			viewText = "";
 			
 			if(i < listSize) { // 0, 1,
 				event = eventList.get(i);
 				
 				start = DateUtils.timeMillisToLocalReadable(event.getStart());
 				end = DateUtils.timeMillisToLocalReadable(event.getEnd());
 				roomId = event.getRoomId();
 				subject = event.getSubject();
 				
 				viewText = start + " - " + end + " " + roomId + " " + subject;
 			}
 			
 			view.setTextViewText(textViews[i], viewText);
 		}
 		
		view.setTextViewText(R.id.date, DateUtils.getFullDay(eventList.get(0).getStart()));
 		
 		AppWidgetManager manager = AppWidgetManager.getInstance(this);
 		
 		manager.updateAppWidget(widgetId, view);
 	}
 	
 	/**
 	 * Generates valid URL from the hard coded URL plus group id from preferences
 	 * 
 	 * @param widgetId Widget id.
 	 * @return URL
 	 */
 	public URL getURL(int widgetId) {
 		SharedPreferences prefs = getSharedPreferences(ConfigurationActivity.PREFS_NAME, 0);	
 		String group = prefs.getString("group#" + widgetId, "");
 		
 		URL url = null;	
 		
 		try {
 			url = new URL(WidgetProvider.URIString + group);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		
 		Log.d(WidgetProvider.TAG, "Got URI " + url.toString());
 		return url;
 	}
 	
 	/**
 	 * Check for network connectivity.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isConnected() {
 		ConnectivityManager cm =
 		        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		 
 		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
 		
 		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
 	}
 	
 	/**
 	 * Fetches supplied URL and returns the JSON response as String.
 	 * 
 	 * @param url URL with the url to request data from.
 	 * @return JSON-object in String format.
 	 */
 	public String getJSONSchedule(URL url) {
 		Log.d(WidgetProvider.TAG, "Fetching & parsing");
 		HttpURLConnection urlConnection = null;
 
 		String result = null;
 		
 		if(isConnected()) {
 			
 		}
 		
 		try {
 			urlConnection = (HttpURLConnection) url.openConnection();
 			
 			if (urlConnection.getResponseCode() != 200) {
 				throw new IOException("Service not available: " + urlConnection.getResponseMessage());
 			}
 			
 			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
 			
 			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 			StringBuilder sb = new StringBuilder();
 			String line = null;
 			
 			while((line = reader.readLine()) != null) {
 				sb.append(line);
 			}
 			result = sb.toString();
 			//Log.d(MetrolukkariWidget.TAG, result);
 		} catch(ClientProtocolException e) {
 			e.printStackTrace();
 		} catch(IOException e) {
 			Log.d(WidgetProvider.TAG, "IOException: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			urlConnection.disconnect();
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Parse JSON response String and cache three events of tree days to the database.
 	 * 
 	 * @param jsonString JSON-object in String.
 	 */
 	public void parseJSONToDatabase(String jsonString) {
 		// Parse JSON here
 		try {
 			JSONObject jObject = new JSONObject(jsonString);
 			JSONArray jArray = jObject.getJSONArray("events");
 			
 			DBAdapter dataSource = new DBAdapter(getApplicationContext(), appWidgetId);
 			dataSource.open();
 			dataSource.deleteOld();
 			
 			long start, end;
 			String subject, roomId;
 			
 			int day = 0;
 			int prevDay = 0;
 			int dayCount = 0;
 			
 			for(int i=0; i<jArray.length(); i++) {
 				JSONObject item = jArray.getJSONObject(i);
 				
 				start = DateUtils.unixTimeToTimeMillis(item.getLong("start"));
 				end = DateUtils.unixTimeToTimeMillis(item.getLong("end"));
 				subject = item.getString("subject");
 				roomId = item.getString("roomid");
 				
 				day = DateUtils.getDay(start);
 				
 				if(day != prevDay) {
 					prevDay = day;
 					dayCount++;
 				}
 				
 				if(dayCount > 3) {
 					break;
 				} else {
 					Log.d(WidgetProvider.TAG, "Caching: " + subject + " from " + start + " to " + end + " at " + roomId);
 					dataSource.push(subject, start, end, roomId);
 				}
 			}
 			
 			dataSource.close();
 			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Get cached events from local storage.
 	 * 
 	 * @param limit Amount of events to get.
 	 * @return List containing Event objects.
 	 */
 	public List<Event> getEvents(int limit) {
 		DBAdapter dataSource = new DBAdapter(getApplicationContext(), appWidgetId);
 		
 		dataSource.open();	
 		List<Event> events = dataSource.getUpcoming(limit);		
 		dataSource.close();
 		
 		return events;
 	}
 	
 	/**
 	 * Remove events not happening on the same day as the first event.
 	 * 
 	 * @param events List containing the Event objects.
 	 * @return Trimmed list.
 	 */
 	public List<Event> trimEvents(List<Event> events) {
 		if(events.size() < 1) {
 			return null;
 		}
 		int day = DateUtils.getDay(events.get(0).getStart());
 		int tDay;
 		
 		Iterator<Event> i = events.iterator();
 		
 		while(i.hasNext()) {
 			Event event = i.next();
 			tDay = DateUtils.getDay(event.getStart());
 			
 			if(tDay != day) {
 				i.remove();
 			}
 		}
 		return events;
 	}
 	
 	/**
 	 * Schedule next widget update.
 	 * 
 	 * @param ctx Application context.
 	 * @param widgetId Widget id.
 	 * @param time Time in milliseconds that alarm should go off.
 	 */
 	public void createAlarmTimer(Context ctx, int widgetId, long time) {
 		// Check for already running alarm timer
 		Intent intent = new Intent(ctx, UpdateService.class);
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, widgetId, intent, PendingIntent.FLAG_NO_CREATE);
 
 		if (pendingIntent == null) {
 			pendingIntent = getSyncPendingIntent(ctx, widgetId);
 			
 			Log.d(WidgetProvider.TAG, "Next update at " + DateUtils.timeMillisToLocalReadable(time));
 
 			AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ctx.ALARM_SERVICE);
 			alarmManager.set(AlarmManager.RTC, time, pendingIntent);
 		} else {
 			Log.d(WidgetProvider.TAG, "AlarmManager already running for widget instance #" + widgetId);
 		}
 	}
 	
 	/**
 	 * Returns synchronized pendingIntents for launching the UpdateService.
 	 * 
 	 * @param ctx Application context.
 	 * @param widgetId Widget id.
 	 * @return PendingIntent for launching UpdateService.
 	 */
 	public static PendingIntent getSyncPendingIntent(Context ctx, int widgetId) {
 		Intent intentUpdate = new Intent(ctx, UpdateService.class);
 		intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
 		PendingIntent pendingIntentUpdate = PendingIntent.getService(ctx, widgetId, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT);
 		
 		return pendingIntentUpdate;
 	}
 	
 	/**
 	 * Starts UpdateService and immediately calls onHandleIntent.
 	 * 
 	 * @param ctx Application context.
 	 * @param widgetId Widget id.
 	 */
 	public static void startIntentService(Context ctx, int widgetId) {
 		Log.d(WidgetProvider.TAG, "Attempting to start service from provider");
 		Intent serviceIntent = new Intent(ctx, UpdateService.class);
 		serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
 		ctx.startService(serviceIntent);	
 	}
 
 }
