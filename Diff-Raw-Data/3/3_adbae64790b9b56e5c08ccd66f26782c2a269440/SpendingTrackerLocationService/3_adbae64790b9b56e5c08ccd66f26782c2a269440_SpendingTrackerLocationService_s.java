 package com.tzachsolomon.spendingtracker;
 
 import java.util.Calendar;
 import java.util.Currency;
 import java.util.Locale;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public class SpendingTrackerLocationService extends Service implements
 		LocationListener {
 
 	private static final String TAG = SpendingTrackerLocationService.class
 			.getSimpleName();
 
 	public static final String ACTION_FILTER = "com.tzachsolomon.spendingtracker.updatelocation";
 
 	private ClassDbEngine m_SpendingTrackerDbEngine;
 
 	private NotificationManager m_NotificationManager;
 	private Notification m_Notification;
 	private LocationManager m_LocationManager;
 	private SharedPreferences m_SharedPreferences;
 	private int m_ValidDistance;
 	private boolean mUseGpsProvider;
 	private boolean m_DebugMode;
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		//
 		return null;
 	}
 
 	@Override
 	public void onDestroy() {
 		//
 		Log.i(TAG, "Location Service Destroyed");
 
 		m_LocationManager.removeUpdates(this);
 
 		super.onDestroy();
 	}
 
 	@Override
 	public void onCreate() {
 		//
 		super.onCreate();
 
 		Log.i(TAG, "Location Service Created");
 
 		initPreferences();
 		initializeVariables();
 		initLocalationManager();
 
 		// checkReminders(m_LocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
 
 	}
 
 	private void initPreferences() {
 		try {
 			m_SharedPreferences = PreferenceManager
 					.getDefaultSharedPreferences(getBaseContext());
 
 			m_DebugMode = m_SharedPreferences.getBoolean(
 					"checkBoxPreferenceDebug", false);
 
 			String validDistance = m_SharedPreferences.getString(
 					"editTextPreferenceValidDistance", "bla");
 
 			m_ValidDistance = Integer.parseInt(validDistance);
 		} catch (Exception e) {
 
 			String message = e.getMessage().toString();
 
 			if (m_DebugMode) {
 				Toast.makeText(this, message, Toast.LENGTH_LONG).show();
 			}
 			Log.e(TAG, message);
 		}
 
 	}
 
 	private void initLocalationManager() {
 
 		Intent intent = new Intent(SpendingTrackerLocationService.ACTION_FILTER);
 		
 		m_LocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		mUseGpsProvider = m_LocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 		
 		if (mUseGpsProvider) {
 			m_LocationManager.requestLocationUpdates(
 					LocationManager.GPS_PROVIDER, 0, 0, this);
 			intent.putExtra("location", m_LocationManager
 					.getLastKnownLocation(LocationManager.GPS_PROVIDER));
 		} else {
 			m_LocationManager.requestLocationUpdates(
 					LocationManager.NETWORK_PROVIDER, 0, 0, this);
 			intent.putExtra("location", m_LocationManager
 					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
 		}
 
 		sendBroadcast(intent);
 
 	}
 
 	private void initializeVariables() {
 		//
 		m_SpendingTrackerDbEngine = new ClassDbEngine(this);
 		m_NotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 
 	}
 
 	private void checkReminders(Location i_CurrentLocation) {
 		//
 		ContentValues[] data = m_SpendingTrackerDbEngine.getLocationReminders();
 		int i, end;
 		float distance;
 
 		end = data.length;
 		Log.i(TAG, "Looping over " + end + " Location reminders");
 
 		for (i = 0; i < end; i++) {
 
 			distance = getDistance(i_CurrentLocation, data[i]);
 
 			if (distance < m_ValidDistance) {
 				sendNotification(data[i]);
 			}
 
 		}
 
 	}
 
 	private void sendNotification(ContentValues i_Data) {
 		//
 		Calendar calendar = Calendar.getInstance();
 
 		Bundle extras = new Bundle();
 		Intent intent = new Intent(getBaseContext(),
 				ActivityMain1.class);
 		String amount = i_Data.getAsString(ClassDbEngine.KEY_AMOUNT);
 		String category = i_Data.getAsString(ClassDbEngine.KEY_CATEGORY);
 		String rowId = i_Data.getAsString(ClassDbEngine.KEY_ROWID);
 		String locationName = i_Data
 				.getAsString(ClassDbEngine.KEY_LOCATION_NAME);
 		int notificationId = 0;
 
 		if (m_SpendingTrackerDbEngine.isLocationChanged(rowId) == false) {
 			Log.d(TAG, "Already sent a notification once about this location");
 		} else {
 
 			calendar.setTimeInMillis(System.currentTimeMillis());
 
 			notificationId += calendar.get(Calendar.SECOND);
 			notificationId += calendar.get(Calendar.MINUTE);
 			notificationId += calendar.get(Calendar.HOUR_OF_DAY);
 			notificationId += calendar.get(Calendar.DAY_OF_MONTH);
 			notificationId += calendar.get(Calendar.MONTH);
 			notificationId += calendar.get(Calendar.YEAR);
 
 			extras.putBoolean("fromNotifaction", true);
 			extras.putString(ClassDbEngine.KEY_AMOUNT, amount);
 			extras.putString(ClassDbEngine.KEY_CATEGORY, category);
 			extras.putString(ClassDbEngine.KEY_REMINDER_TYPE,
 					ClassDbEngine.KEY_REMINDER_TYPE_LOCATION);
 			extras.putString(ClassDbEngine.KEY_REMINDER_ID, rowId);
 			extras.putInt("notificationId", notificationId);
 
 			intent.putExtras(extras);
 
 			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 
 			// Instead of starting the application start notification
 
 			boolean enableVibrate = PreferenceManager
 					.getDefaultSharedPreferences(this).getBoolean(
 							"cbNotifictionVibrate", true);
 			boolean enableSound = PreferenceManager
 					.getDefaultSharedPreferences(this).getBoolean(
 							"cbNotifictionSound", true);
 			boolean enableLights = PreferenceManager
 					.getDefaultSharedPreferences(this).getBoolean(
 							"cbNotifictionLights", true);
 
 			StringBuilder sb = new StringBuilder();
 
 			sb.append(getString(R.string.stringDidYouSpend));
 			sb.append(' ');
 			sb.append(amount);
 			sb.append(' ');
 			try {
 				sb.append(Currency.getInstance(Locale.getDefault()).getSymbol());
 			} catch (Exception e) {
 				//
 				if (m_DebugMode) {
 					Toast.makeText(this, e.getMessage().toString(),
 							Toast.LENGTH_SHORT).show();
 				}
 
 				Log.d(TAG, e.getMessage().toString());
 
 			}
 
 			sb.append(getString(R.string.stringDidYouSpend));
 			sb.append(' ');
 			sb.append(category);
 			sb.append(' ');
 			sb.append(" @ ");
 			sb.append(locationName);
 			sb.append("?");
 
 			m_Notification = new Notification(R.drawable.notify,
 					"Spending notification", System.currentTimeMillis());
 
 			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 					intent, PendingIntent.FLAG_UPDATE_CURRENT);
 
 			m_Notification.setLatestEventInfo(getApplicationContext(),
 					getString(R.string.app_name), sb.toString(), contentIntent);
 			if (enableLights) {
 				m_Notification.defaults |= Notification.DEFAULT_LIGHTS;
 			}
 			if (enableSound) {
 				m_Notification.defaults |= Notification.DEFAULT_SOUND;
 			}
 			if (enableVibrate) {
 				m_Notification.defaults |= Notification.DEFAULT_VIBRATE;
 			}
 
 			m_NotificationManager.notify(notificationId, m_Notification);
 		}
 
 	}
 
 	private float getDistance(Location i_CurrentLocation,
 			ContentValues contentValues) {
 		//
 		float ret = -1;
 		try {
 			Location location = new Location(i_CurrentLocation);
 
 			location.setAccuracy(contentValues
 					.getAsFloat(ClassDbEngine.KEY_ACCURACY));
 			location.setAltitude(contentValues
 					.getAsDouble(ClassDbEngine.KEY_ALTITUDE));
 			location.setBearing(contentValues
 					.getAsFloat(ClassDbEngine.KEY_BEARING));
 			location.setLatitude(contentValues
 					.getAsDouble(ClassDbEngine.KEY_LATITUDE));
 			location.setLongitude(contentValues
 					.getAsDouble(ClassDbEngine.KEY_LONGITUDE));
 			location.setProvider(contentValues
 					.getAsString(ClassDbEngine.KEY_PROVIDER));
 			location.setSpeed(contentValues.getAsFloat(ClassDbEngine.KEY_SPEED));
 			location.setTime(contentValues.getAsLong(ClassDbEngine.KEY_TIME));
 
 			ret = i_CurrentLocation.distanceTo(location);
 
 			Log.v(TAG,
 					"Distance from "
 							+ contentValues
 									.getAsString(ClassDbEngine.KEY_LOCATION_NAME)
 							+ " is " + ret);
 		} catch (Exception e) {
 			Log.e(TAG, e.getMessage().toString());
 
 		}
 
 		return ret;
 	}
 
 	public void onLocationChanged(Location location) {
 		// update the activity UI
 		Intent intent = new Intent(SpendingTrackerLocationService.ACTION_FILTER);
 		intent.putExtra("location", location);
 
 		sendBroadcast(intent);
 
 		// check if to send notification
 		checkReminders(location);
 
 	}
 
 	public void onProviderDisabled(String provider) {
 		//
 
 	}
 
 	public void onProviderEnabled(String provider) {
 		//
 
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		//
 
 	}
 
 }
