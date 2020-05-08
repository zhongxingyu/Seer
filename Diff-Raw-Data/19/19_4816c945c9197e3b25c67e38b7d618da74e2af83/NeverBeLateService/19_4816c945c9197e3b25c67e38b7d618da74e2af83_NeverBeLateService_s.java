 /**
  * 
  */
 package com.madhackerdesigns.neverbelate.service;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlarmManager;
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.text.format.DateUtils;
 
 import com.madhackerdesigns.neverbelate.R;
 import com.madhackerdesigns.neverbelate.provider.AlertsContract;
 import com.madhackerdesigns.neverbelate.provider.AlertsHelper;
 import com.madhackerdesigns.neverbelate.settings.PreferenceHelper;
 import com.madhackerdesigns.neverbelate.ui.WarningDialog;
 import com.madhackerdesigns.neverbelate.util.Logger;
 import com.madhackerdesigns.neverbelate.util.NetUtils;
 
 /**
  * @author flintinatux
  *
  */
 public class NeverBeLateService extends IntentService implements ServiceCommander {
 	
 	// static numbers and such
 	private static final long DELTA = 5*60*1000;				// age delta = 5 minutes
 	private static final float MARGIN = (float) 1.25;			// accuracy margin = 25%
 	private static final int NOTIFICATION_ID = 1;
 	private static final long[] VIBRATE_PATTERN = new long[] {1250, 250, 250, 250};
 	private static final long WAKELOCK_TIMEOUT = 10000;			// (ms) timeout = 10s
 	private static final String WAKELOCK_NAME = "com.madhackerdesigns.neverbelate.service.NeverBeLateService";
 	
 	private static final boolean OUT_LOUD = true;
 	private static final boolean SILENTLY = false;
 
 	// url and possible return statuses for Directions API
 	private static final String DIRECTIONS_API_URL = "http://maps.googleapis.com/maps/api/directions/json";
 	private static final String LOG_TAG = "NeverBeLateService";
 	private enum DirectionsApiStatus { OK, NOT_FOUND, ZERO_RESULTS, MAX_WAYPOINTS_EXCEEDED, INVALID_REQUEST, 
 		OVER_QUERY_LIMIT, REQUEST_DENIED, UNKNOWN_ERROR }
 	
 	// field to hold shared preferences
 	
 	// private member fields
 	private ContentResolver mContentResolver;
 	private LocationManager mLocationManager;
 	private NotificationManager mNotificationManager;
 	private long mNow;
 	private PreferenceHelper mPrefs;
 	private static volatile PowerManager.WakeLock lockStatic = null;
 	
 	public NeverBeLateService() {
 		super("NeverBeLateService");
 	}
 	
 	public static void sendWakefulWork(Context ctxt, Intent i) {
 		if (lockStatic == null) {
 			PowerManager pm = (PowerManager) ctxt.getApplicationContext().getSystemService(POWER_SERVICE);
 			lockStatic = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_NAME);
 		}
 		lockStatic.acquire(WAKELOCK_TIMEOUT);
 		ctxt.startService(i);
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
 	 */
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		try {
 			String time = FullDateTime(new Date().getTime());
 			Logger.d("Started handling intent at: " + time);
 			doWakefulWork(intent);
 		} finally {
 			String time = FullDateTime(new Date().getTime());
 			Logger.d("Finished handling intent at: " + time);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Service#onStart(android.content.Intent, int)
 	 */
 	public void doWakefulWork(Intent intent) {
 		
 		// Load (or reload) prefs and services every time, in case the service was previous stopped
 		loadPrefsAndServices();
 		
 		// pull instructions from the intent
 		Bundle extras = (Bundle) intent.getExtras();
 		int task = extras.getInt(EXTRA_SERVICE_COMMAND);
 		Logger.d(LOG_TAG, "NeverBeLateService received an intent with task '" + task + "'.");
 		
 		// delegate task based on ServiceTask chosen, if any
 		switch (task) {
 		case CLEAR_ALL:
 			// Clear all alerts
 			// TODO: Report with Flurry
 		case DISMISS:
 			// Dismiss all alerts
 			// TODO: Report with Flurry
 			dismissAllAlerts();
 		case SNOOZE:
 			// Snooze the notification
 			// TODO: Report with Flurry
 			mNotificationManager.cancel(NOTIFICATION_ID);
 			break;
 		case CHECK_TRAVEL_TIMES:	
 			// Check for travel times for upcoming event instances
 			// TODO: Report with Flurry
 			checkTravelTimes();
 			break;
 		case NOTIFY:
 			// Use extras from intent to notify user
 			notifyUserNow();
 			break;
 		case STARTUP:
 			// TODO: Do we need to do anything here anymore?
 			break;
 		case SILENCE:
 			// Send new notification without ringtone, vibration, or insistence
 			notifyUser(SILENTLY);
 			break;
 		default:
 			break;
 		}
 		releasePrefsAndServices();
 	}
 
 	private void dismissAllAlerts() {
 		ContentValues values = new ContentValues();
 		values.put(AlertsContract.Alerts.DISMISSED, 1);
 		String selection = 
 			AlertsContract.Alerts.FIRED + "=? AND " + 
 			AlertsContract.Alerts.DISMISSED + "=?";
 		String[] selectionArgs = new String[] { "1", "0" };
 		int rows = mContentResolver.update(AlertsContract.Alerts.CONTENT_URI, values, 
 				selection, selectionArgs);
 		Logger.d(LOG_TAG, "Dismissed " + rows + " alerts.");
 	}
 	
 	private void checkTravelTimes() {
 		// Optimization: shuffle PreferenceHelper into a local variable
 		PreferenceHelper prefs = mPrefs;
 		
 		// Start off by clearing old alerts that are past their expiration date.
 		// Don't want the database to get too big and eat up the user's precious storage space.
 		long expiration = new Date().getTime() - prefs.getLookaheadWindow();
 		int rowsDeleted = mContentResolver.delete(
 				AlertsContract.Alerts.CONTENT_URI, 
 				AlertsContract.Alerts.BEGIN + "<?", 
 				new String[] { String.valueOf(expiration) }
 		);
 		if (rowsDeleted > 0) {
 			Logger.d(LOG_TAG, "Deleted " + rowsDeleted + " alerts from the AlertProvider database.");
 		}
 		
 		// Instantiate new CalendarHelper and query for event instances within the lookahead window
 		CalendarHelper ch = new CalendarHelper();
 		mNow = new Date().getTime();
 		String currentTime = "The current time is " + FullDateTime(mNow);
 		Logger.d(LOG_TAG, currentTime);
 		Uri.Builder instanceBuilder = ch.mInstancesUri.buildUpon();
 		ContentUris.appendId(instanceBuilder, mNow - DateUtils.DAY_IN_MILLIS);
 		ContentUris.appendId(instanceBuilder, mNow + DateUtils.DAY_IN_MILLIS);
 		Logger.d(LOG_TAG, "Querying calendar...");
 		Cursor instance = mContentResolver.query(	
 			instanceBuilder.build(), 
 			CalendarHelper.INSTANCES_PROJECTION, 
 			null, 
 			null, 
 			null
 		);
 		Logger.d(LOG_TAG, "Found " + instance.getCount() + " events!");
 		
 		// Get current best location
 		Location currentBestLocation = getCurrentLocation();
 		
 		// Iterate through the event instances
		if (instance.moveToFirst()) {
 			// ContentValues and Cursor objects to re-use
 			ContentValues values = new ContentValues();
 			Cursor alertCursor;
 			
 			do {
 				// Ignore all-day events by default
 				Boolean instanceAllDay = !instance.getString(CalendarHelper.PROJ_ALL_DAY).equals("0");
 				if (instanceAllDay) { continue; }
 				
 				// Pull the event location and time
 				String eventID = instance.getString(CalendarHelper.PROJ_EVENT_ID);
 				String eventTitle = instance.getString(CalendarHelper.PROJ_TITLE);
 				long instanceBegin = instance.getLong(CalendarHelper.PROJ_BEGIN);
 				long instanceEnd = instance.getLong(CalendarHelper.PROJ_END);
 				String eventLocation = instance.getString(CalendarHelper.PROJ_EVENT_LOCATION);
 				String eventDescription = instance.getString(CalendarHelper.PROJ_DESCRIPTION);
 				int calendarColor = instance.getInt(CalendarHelper.PROJ_COLOR);
 				
 				// ignore event if not in window
 				if (instanceBegin < mNow || instanceBegin > (mNow + prefs.getLookaheadWindow())) {
 					Logger.d(LOG_TAG, "Event " + eventID + " not in window.");
 					continue;
 				}
 				
 				// TODO:  in the future, ask user if they want to add a location
 				if (eventLocation == null || eventLocation == "") { 
 					Logger.d(LOG_TAG, "Event " + eventID + " does not have a location specified.");
 					continue; 
 				}
 				
 				// If the users wants to mark searchable locations with a (*), then filter out
 				// those that aren't marked
 				if (prefs.isOnlyMarkedLocations() && !eventLocation.matches("^\\*.*")) { 
 					Logger.d(LOG_TAG, "Event " + eventID + " not marked with a star.");
 					continue; 
 				}
 				
 				// Bite the * off the front if it's there
 				eventLocation = eventLocation.replaceFirst("^\\*", "");
 				
 				// Query for existing alert entry
 				String selection = AlertsContract.Alerts.EVENT_ID + "=? AND " + 
 					AlertsContract.Alerts.BEGIN + "=?";
 				String[] selectionArgs = new String[] { eventID, String.valueOf(instanceBegin) };
 				alertCursor = mContentResolver.query(
 						AlertsContract.Alerts.CONTENT_URI, 
 						AlertsHelper.ALERT_PROJECTION,
 						selection,
 						selectionArgs, 
 						null
 				);
 				
 				// If the alert exists, and has already been fired, ignore it
 				String alertID = null;
 				if (alertCursor.moveToFirst()) {
 					if (alertCursor.getInt(AlertsHelper.PROJ_FIRED) == 1) {
 						Logger.d(LOG_TAG, "Alert for event " + eventID + " has already been FIRED.");
 						continue;
 					}
 					
 					// Otherwise, grab the id for update
 					alertID = alertCursor.getString(AlertsHelper.PROJ_ID);
 				}
 				
 				// Check for empty event title, and give it a default if needed
 				if (eventTitle == null || eventTitle.length() <= 0) {
 					eventTitle = getResources().getString(R.string.untitled_event);
 				}
 				
 				// Talk to me, baby!
 				Logger.d(LOG_TAG, "Event title: " + eventTitle);
 				Logger.d(LOG_TAG, "Event location: " + eventLocation);
 				Logger.d(LOG_TAG, "Begin time: " + FullDateTime(instanceBegin));
 				
 				// Build url to pull down directions from Google Directions API
 				Uri.Builder b = Uri.parse(DIRECTIONS_API_URL).buildUpon();
 				b.appendQueryParameter("origin", String.valueOf(currentBestLocation.getLatitude()) + "," 
 						+ String.valueOf(currentBestLocation.getLongitude()));
 				b.appendQueryParameter("destination", eventLocation);
 				b.appendQueryParameter("mode", prefs.getTravelMode());
 				if (prefs.isAvoidHighways()) { b.appendQueryParameter("avoid", "highways"); }
 				if (prefs.isAvoidTolls()) { b.appendQueryParameter("avoid", "tolls"); }
 				b.appendQueryParameter("sensor", "true");
 				String url = b.build().toString();
 				
 				// Download json data using the new url
 				// Note: If internet not available, use archived json from alertCursor
 				String json = null;
 				ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 				NetworkInfo netInfo = cm.getActiveNetworkInfo();
 				if (netInfo != null && netInfo.isConnected()) {
 					Logger.d(LOG_TAG, "Network available, attempting to download new directions.");
 					json = NetUtils.DownloadText(url);
 				} else if (alertCursor.moveToFirst()) {
 					Logger.d(LOG_TAG, "Network not available, falling back to previous archived directions.");
 					json = alertCursor.getString(AlertsHelper.PROJ_JSON);
 				}
 				if (json == null || json.length() == 0) { 
 					// TODO: Not sure what to do if I still pull down an empty json result
 					// (which has happened before!)
 					Logger.d(LOG_TAG, "No JSON returned.  Not good.");
 					continue;
 				}
 				
 				// Close alertCursor before parsing the json data
 				alertCursor.close();
 				
 				JSONObject directions;
 				try {
 					// Parse the json data
 					directions = new JSONObject(json);
 					
 					// Check the status code of the json object and handle appropriately
 					String statusString = directions.getString("status");
 					Logger.d(LOG_TAG, "JSON status: " + statusString);
 					DirectionsApiStatus status = Enum.valueOf(DirectionsApiStatus.class, statusString);
 					switch (status) {
 					case OK:
 						// Set the early arrival parameter
 						Long earlyArrival = prefs.getEarlyArrival();
 						if (earlyArrival == null) { earlyArrival = (long) 0; }
 						
 						// Process the directions returned
 						JSONObject route = directions.getJSONArray("routes").getJSONObject(0);
 						JSONObject leg = route.getJSONArray("legs").getJSONObject(0);
 						long duration = leg.getJSONObject("duration").getLong("value") * 1000;   // in ms
 						Logger.d(LOG_TAG, "Duration: " + String.valueOf(duration/60000) + " min");
 						long warnTime = instanceBegin - earlyArrival - duration - prefs.getAdvanceWarning();
 						Logger.d(LOG_TAG, "Warn time: " + FullDateTime(warnTime));
 						String copyrights = route.getString("copyrights");
 						
 						// Put together a ContentValues object of the alert data for this event instance
 						values.clear();
 						values.put(AlertsContract.Alerts.EVENT_ID, eventID);
 						values.put(AlertsContract.Alerts.CALENDAR_COLOR, calendarColor);
 						values.put(AlertsContract.Alerts.TITLE, eventTitle);
 						values.put(AlertsContract.Alerts.BEGIN, instanceBegin);
 						values.put(AlertsContract.Alerts.END, instanceEnd);
 						values.put(AlertsContract.Alerts.LOCATION, eventLocation);
 						values.put(AlertsContract.Alerts.DESCRIPTION, eventDescription);
 						values.put(AlertsContract.Alerts.DURATION, duration);
 						values.put(AlertsContract.Alerts.COPYRIGHTS, copyrights);
 						values.put(AlertsContract.Alerts.JSON, json);
 						
 						// Insert or update the alert data for this event instance
 						Uri alertUri = null;
 						if (alertID != null) {
 							// If alert currently exists, update the entry
 							alertUri = Uri.withAppendedPath(AlertsContract.Alerts.CONTENT_URI, alertID);
 							mContentResolver.update(alertUri, values, null, null);
 						} else {
 							// Otherwise, insert the alert as a new entry
 							alertUri = mContentResolver.insert(AlertsContract.Alerts.CONTENT_URI, values);
 						}
 						
 						// Issue a notification if warning is required before the next check
 						if (warnTime <= (mNow + prefs.getTraveltimeFreq())) {
 							// Whether we alert user now or later, consider alert as FIRED.
 							Logger.d(LOG_TAG, "Marking event " + eventID + " as FIRED.");
 							ContentValues firedValues = new ContentValues();
 							firedValues.put(AlertsContract.Alerts.FIRED, 1);
 							mContentResolver.update(alertUri, firedValues, null, null); 
 							
 							if (warnTime <= mNow) {
 								// Warn user immediately if warnTime is before now
 								Logger.d(LOG_TAG, "Notify the user now about event " + eventID);
 								notifyUserNow();
 							} else {
 								// Otherwise, set alarm to warn user at future warnTime
 								Logger.d(LOG_TAG, "Notify the user later about event " + eventID);
 								notifyUserLater(warnTime);
 							}
 						}
 						continue;
 					case NOT_FOUND:
 						// TODO:  this definitely represents a bad address.
 						continue;
 					case ZERO_RESULTS:
 						// TODO:  this may represent a bad address.  may ask user for new address.
 						continue;
 					case MAX_WAYPOINTS_EXCEEDED:
 						// TODO:  this should never happen in this application
 						continue;
 					case INVALID_REQUEST:
 						// TODO:  whoops! we screwed something up in the request
 						continue;
 					case OVER_QUERY_LIMIT:
 						// TODO:  busted! we asked for directions too many times.
 						continue;
 					case REQUEST_DENIED:
 						// TODO:  most likely that we forgot to include the "sensor=true" parameter
 						continue;
 					case UNKNOWN_ERROR:
 						// TODO:  most likely a server error, and the best solution is to try again
 						continue;
 					default:
 						// TODO:  this may be reached if status is null, but do something safe
 						continue;
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 					throw new RuntimeException(e);
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 					throw new RuntimeException(e);
 				}
 			} while (instance.moveToNext());
 		} else {
			// no calendar entries in the next lookaheadWindow, so wait til next time
 		}
 		instance.close();
 	}
 	
 	private void notifyUserLater(long warnTime) {
 		// Get alarm manager and setup new alarm to pass notification extras to WakefulServiceReceiver
 		Context context = getApplicationContext();
 		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		Intent intent = new Intent(context, WakefulServiceReceiver.class);
 		intent.putExtra(EXTRA_SERVICE_COMMAND, NOTIFY);
 		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		alarm.set(AlarmManager.RTC_WAKEUP, warnTime, alarmIntent);
 	}
 	
 	private void notifyUserNow() {
 		// Notify user by their preferred method
 		switch(mPrefs.getNotificationMethod()) {
 		case ALERT:
 			// Notify user with alert dialog. Note that this will interrupt the user's current task.
 			startActivity(getWarningIntent());
 		case STATUS_BAR_ONLY:
 			// Notify user with status bar notification.
 			notifyUser(OUT_LOUD);
 		}
 	}
 	
 	private Intent getWarningIntent() {
 		Intent alertIntent = new Intent(getApplicationContext(), WarningDialog.class);
 		int flags = 0;
 		flags |= Intent.FLAG_ACTIVITY_NEW_TASK;
 		return alertIntent.setFlags(flags);
 	}
 	
 	private void notifyUser(boolean outLoud) {
 		// Cancel any existing notification first
 		mNotificationManager.cancel(NOTIFICATION_ID);
 		
 		// Get the application context first
 		Context context = getApplicationContext();
 		Logger.d(LOG_TAG, "Notifying user now of upcoming events!");
 		
 		// Query for the total fired alerts not already dismissed
 		Uri contentUri = AlertsContract.Alerts.CONTENT_URI;
 		String[] projection = AlertsHelper.ALERT_PROJECTION;
 		String selection = 
 			AlertsContract.Alerts.FIRED + "=? AND " + 
 			AlertsContract.Alerts.DISMISSED + "=?";
 		String[] selectionArgs = new String[] { "1", "0" };
 		Cursor cursor = mContentResolver.query(contentUri, projection, selection, selectionArgs, null);
 		int total = cursor.getCount();
 		String eventTitle = "";
 		if (cursor.moveToFirst()) {
 			// Set event title in the notification to the first upcoming event.
 			eventTitle = cursor.getString(AlertsHelper.PROJ_TITLE);
 		} else {
 			// TODO: The alert has gone missing. Not sure what to do.
 		}
 		cursor.close();
 		
 		// Build status bar notification to notify user immediately
 		Resources res = getResources();
 		int icon = R.drawable.ic_stat_notify_rabbit3;
 		CharSequence tickerText = res.getString(R.string.ticker_text);
 		long when = System.currentTimeMillis();
 		Notification notification = new Notification(icon, tickerText, when);
 		
 		// Pull application resources to fill notification fields
 		String contentTitle = res.getString(R.string.content_title);
 		contentTitle += " " + eventTitle;
 		String contentText = "";
 		if (total > 1) { 
 //			contentText += "(+" + (total-1) + " " + res.getString(R.string.more_text) + ")  ";
 			contentText += "(+" + (total-1) + ")  ";
 		}
 		contentText += res.getString(R.string.content_text);
 		
 		// Build pending intent for notification click action
 		Intent alertIntent = getWarningIntent();
 		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, alertIntent, 0);
 		
 		// Build pending intent for "Clear All" action
 		Intent i = new Intent(getApplicationContext(), WakefulServiceReceiver.class);
 		i.putExtra(EXTRA_SERVICE_COMMAND, CLEAR_ALL);
 		PendingIntent deleteIntent = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
 		
 		// Push notification to NotificationManager
 		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
 		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
 		notification.defaults |= Notification.DEFAULT_LIGHTS;
 		notification.deleteIntent = deleteIntent;
 		if (outLoud) { 
 			notification.sound = mPrefs.getRingtone();
 			if (mPrefs.isVibrate()) { notification.vibrate = VIBRATE_PATTERN; }
 			if (mPrefs.isInsistent()) { notification.flags |= Notification.FLAG_INSISTENT; }
 		}
 		mNotificationManager.notify(NOTIFICATION_ID, notification);
 	}
 
 	public Location getCurrentLocation() {
 
 		// Get the location manager locally and create new listener
 		LocationManager lm = mLocationManager;
 		
 		// getLastKnownLocation of, and start listening for new locations from, all available enabled providers
 		List<String> allProviders = lm.getProviders(true);
 		Location bestLocation = null;
 		if (allProviders != null) {
 			for(String provider : allProviders) {
 				// Cheap solution for now, get all last known locations and decide on a best one
 				Location nextLocation = lm.getLastKnownLocation(provider);
 				
 				// Rule 0: if last known location is null, don't use it
				if (nextLocation == null) { continue; }
 				
 				// Rule 1: if the current best location is null, set it to the next location
 				if (bestLocation == null) { 
 					bestLocation = nextLocation; 
 					continue; 
 				}
 				
 				// Rule 2: check accuracy
 				int accuracyComparison = compareLocationAccuracy(nextLocation, bestLocation);
 				
 				// Rule 3: check age
 				int ageComparison = 0;
 				switch (accuracyComparison) {
 				case 1:		// nextLocation accuracy is higher
 					ageComparison = compareLocationAge(nextLocation, bestLocation, -DELTA);
 					break;
 				case 0:		// nextLocation accuracy is the same
 					ageComparison = compareLocationAge(nextLocation, bestLocation, 0);
 					break;
 				case -1:	// nextLocation accuracy is lower
 					ageComparison = compareLocationAge(nextLocation, bestLocation, DELTA);
 					break;
 				}
 				switch (ageComparison) {
 				case 1:		// nextLocation is newer = accept
 				case 0:		// nextLocation has same age = accept
 					// Rule 4: if the nextLocation isn't far enough away, reject
 					// TODO: revise this to accommodate accuracy ranges from API level 9
 					if (nextLocation.hasAccuracy()) {
 						if (bestLocation.distanceTo(nextLocation) <= MARGIN*(nextLocation.getAccuracy())) {
 							// do nothing
 							break;
 						}
 					}
 					// If it is far enough away, or we can't tell, go ahead and accept
 					bestLocation = nextLocation;
 					break;
 				case -1:	// nextLocation is older = reject
 					// do nothing
 					break;
 				}
 				
 				// TODO: use the following syntax on bootup to request location updates periodically
 //				lm.requestLocationUpdates(provider, 0, 0, myLocationListener);
 			}
 		} else {
//			publishProgress("No location providers enabled!");
 			return null;
 		}
 		
 		if (bestLocation == null) {
 			Logger.d(LOG_TAG, "Location is null!");
 			return null;
 		}
 		
 		return bestLocation;
 	}
 	
 	private int compareLocationAccuracy(Location locationA, Location locationB) {
 		if (locationA.hasAccuracy() == true && locationB.hasAccuracy() == true) {
 			// If both locations have accuracy values in meters, make comparison based on those values
 			float accuracyA = locationA.getAccuracy();
 			float accuracyB = locationB.getAccuracy();
 			// Compare accuracy and return int
 			if (accuracyA < accuracyB) { return 1; }			// Note: less meters = more accurate (typ)
 			else if (accuracyA > accuracyB) { return -1; }
 			else { return 0; }
 		} else {
 			// Otherwise, make the comparison based on the general accuracy description of the provider.
 			int accuracyA = mLocationManager.getProvider(locationA.getProvider()).getAccuracy();
 			int accuracyB = mLocationManager.getProvider(locationB.getProvider()).getAccuracy();
 			// If SDK version code >= 9, flip the numbers around to make a lower number more accurate,
 			// so as to match the behavior of previous versions
 			if (Build.VERSION.SDK_INT >= 9) {
 				accuracyA = 4 - accuracyA;
 				accuracyB = 4 - accuracyB;
 			}
 			// Compare accuracy and return int
 			if (accuracyA < accuracyB) { return 1; }
 			else if (accuracyA > accuracyB) { return -1; }
 			else { return 0; }
 		}
 	}
 	
 	private int compareLocationAge(Location locationA, Location locationB, long delta) {
 		long timeA = locationA.getTime();
 		long timeB = locationB.getTime();
 		
 		if (timeA > timeB + delta) { return 1; }		// Greater timestamp = newer (typ)
 		else if (timeA < timeB + delta) { return -1; }
 		else { return 0; }
 	}
 	
 	public static String FullDateTime(Long time) {
 		return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(new Date(time));
 	}
 	
 	// Load (or reload) shared preferences and system services
 	private void loadPrefsAndServices() {
 		if (mContentResolver == null) {
 			mContentResolver = getContentResolver();
 		}
 		if (mLocationManager == null) {
 			mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		}
 		if (mNotificationManager == null) {
 			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		}
 		if (mPrefs == null) {
 			mPrefs = new PreferenceHelper(getApplicationContext());
 		}
 	}
 	
 	// Release the shared preferences and system services to avoid battery drain
 	public void releasePrefsAndServices() {
 		mContentResolver = null;
 		mLocationManager = null;
 		mNotificationManager = null;
 		mPrefs = null;
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.Service#onBind(android.content.Intent)
 	 */
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	
 }
