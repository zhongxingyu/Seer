 package com.turfgame.alarm;
 
 import java.util.Calendar;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.os.AsyncTask;
 import android.os.IBinder;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.View;
 import android.widget.RemoteViews;
 
 import com.turfgame.graphic.CustomText;
 import com.turfgame.widget.CharStats;
 import com.turfgame.widget.Prefs;
 import com.turfgame.widget.TurfWidget;
 import com.turfgame.widget.R;
 import com.turfgame.xml.ParseXML;
 
 public class AppService extends Service
 {
 	public static String textView = "";
 	public static final String UPDATE_WIDGET_SERVICE = "service.APPWIDGET_UPDATE";
 
 	public AppService getService()
 	{
 		return AppService.this;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId)
 	{
 		if (TurfWidget.DEBUG) {
 			Log.d(TurfWidget.DEBUG_STRING, "onStartCommand");
 		}
 
 		// Parse in parameters and stop service if desired.
 		Boolean vibrator;
 		try {
 			vibrator = intent.getExtras().containsKey("vibrate");
 			if (vibrator) {
 				vibrator = intent.getExtras().getBoolean("vibrate");
 			}
 
 			Boolean killKey = intent.getExtras().containsKey("kill");
 			if (killKey) {
 				Boolean kill = intent.getExtras().getBoolean("kill");
 				if (kill) {
 					stopSelf();
 					return 0;
 				}
 			}
 		}
 		catch (NullPointerException e) {
 			vibrator = false;
 		}
 
 		new updateTask().execute(vibrator);
 
 		return START_STICKY;
 	}
 
 	private class updateTask extends AsyncTask<Boolean, Void, Object[]>
 	{
 		@Override
 		protected void onPreExecute()
 		{
 			RemoteViews statsView = new RemoteViews(getApplicationContext().getPackageName(),
 			                                        R.layout.main);
 			statsView.setViewVisibility(R.id.refresh, View.GONE);
 			statsView.setViewVisibility(R.id.ProgressBarWrapper, View.VISIBLE);
 			updateWidgets(statsView);
 		}
 
 		@Override
 		protected Object[] doInBackground(Boolean... vibrate)
 		{
 			// Get stats from network.
 			CharStats currentChar = getCurrentChar();
 
 			return new Object[] { currentChar, vibrate[0] };
 		}
 
 		protected void onPostExecute(Object[] o)
 		{
 			// Create RemoteViews
 			RemoteViews statsView = getStatsView((CharStats) o[0],
 			                                     (Boolean) o[1]);
 			statsView.setViewVisibility(R.id.ProgressBarWrapper, View.GONE);
 			statsView.setViewVisibility(R.id.refresh, View.VISIBLE);
 			
 
 
 			// Update all widgets.
 			updateWidgets(statsView);
 
 			// Schedule next update
 			scheduleUpdate();
 		}
 	}
 
 	private CharStats getCurrentChar()
 	{
 		CharStats currentChar = new CharStats(); // FIXME: why new?
 		currentChar = ParseXML.parseXML(Prefs.getUserEmail(this));
 		return currentChar;
 	}
 
 	private RemoteViews getStatsView(CharStats currentChar, Boolean vibrator)
 	{
 		RemoteViews statsView;
 		if (TurfWidget.getError() == null) {
 			statsView = updateViews(this, currentChar, vibrator);
 		} else {
 			statsView = updateViewErrors(this);
 		}
 
 		// Bind event handlers to the views.
 
 		// Prefs
 		Intent prefsIntent = new Intent(this, Prefs.class);
 		PendingIntent prefsPendingIntent = PendingIntent.getActivity(this,
 		                                                             0,
 		                                                             prefsIntent,
 		                                                             0);
 		statsView.setOnClickPendingIntent(R.id.option, prefsPendingIntent);
 
 		// Launch Turf
 		Intent update = new Intent(this, TurfWidget.class);
 		update.setAction(TurfWidget.TURF_LAUNCH);
 		PendingIntent updatePendingIntent = PendingIntent.getBroadcast(this, 0,
 		                                                               update,
 		                                                               0);
 		statsView.setOnClickPendingIntent(R.id.TextWrapper, updatePendingIntent);
		statsView.setOnClickPendingIntent(R.id.error, updatePendingIntent);
 
 		// Refresh
 		Intent refresh = new Intent(this, TurfWidget.class);
 		refresh.setAction(TurfWidget.WIDGET_UPDATE);
 		PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(this,
 		                                                                0,
 		                                                                refresh,
 		                                                                0);
 		statsView.setOnClickPendingIntent(R.id.refresh, refreshPendingIntent);
 
 		return statsView;
 	}
 
 	private void updateWidgets(RemoteViews statsView)
 	{
 		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
 		ComponentName provider = new ComponentName(this, TurfWidget.class);
 		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(provider);
 		appWidgetManager.updateAppWidget(appWidgetIds, statsView);
 	}
 
 	private void scheduleUpdate()
 	{
 		int freq = Integer.parseInt(Prefs.getUpdateFreq(this));
 		if (freq != -1) {
 			if (TurfWidget.DEBUG) {
 				Log.d(TurfWidget.DEBUG_STRING, "Scheduling update");
 			}
 			Calendar cal = Calendar.getInstance();
 
 			OnAlarmReceiver.alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
 			Intent alarmIntent = new Intent(this, OnAlarmReceiver.class);
 			alarmIntent.setAction(UPDATE_WIDGET_SERVICE);
 			OnAlarmReceiver.alarmPedningIntent = PendingIntent.getBroadcast(this,
 			                                                                0,
 			                                                                alarmIntent,
 			                                                                0);
 			OnAlarmReceiver.alarmManager.set(AlarmManager.RTC,
 			                                 cal.getTimeInMillis() + freq,
 			                                 OnAlarmReceiver.alarmPedningIntent);
 		} else {
 			if (TurfWidget.DEBUG) {
 				Log.d(TurfWidget.DEBUG_STRING, "Update disabled");
 			}
 		}
 	}
 
 	private static RemoteViews updateViews(Context context,
 	                                       CharStats currentChar,
 	                                       Boolean vibrator)
 	{
 
 		if (TurfWidget.DEBUG) {
 			Log.v("Error", "Is: " + TurfWidget.getError());
 		}
 
 		RemoteViews statsView = new RemoteViews(context.getPackageName(),
 		                                        R.layout.main);
 
 		if (TurfWidget.DEBUG) {
 			int freq = Integer.parseInt(Prefs.getUpdateFreq(context));
 			Log.v("AppService", "updateViews: " + (freq / 60000) + " min");
 		}
 
 		CustomText customText = new CustomText(context);
 		if (CharStats.isAccount()) {
 			statsView.setTextViewText(R.id.error, "");
 			statsView.setImageViewResource(R.id.power, R.drawable.power);
 			statsView.setImageViewBitmap(R.id.points,
 			                             customText.createCustomPoints(CharStats.getPoints()));
 			statsView.setImageViewBitmap(R.id.hourzones,
 			                             customText.createCustomHourZones(CharStats.getHour(),
 			                                                              CharStats.getZones()));
 			statsView.setImageViewResource(R.id.placestar, R.drawable.star);
 			statsView.setImageViewBitmap(R.id.place,
 			                             customText.createCustomPlace(CharStats.getPlace()));
 
 			if (CharStats.getPrevZonesAlert()) {
 				AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
 				boolean silent = manager.getRingerMode() == AudioManager.RINGER_MODE_SILENT;
 
 				if (vibrator && Prefs.getVibrate(context) && !silent &&
 				    !CharStats.isAlert()) {
 					((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(600);
 
 					if (TurfWidget.DEBUG) {
 						Log.e("Vibrate", "The device is vibrating!");
 					}
 
 				}
 
 				statsView.setImageViewResource(R.id.alert, R.drawable.alert);
 				CharStats.setAlert(true);
 			} else {
 				statsView.setImageViewResource(R.id.alert, R.drawable.empty);
 				CharStats.setAlert(false);
 			}
 		} else {
 			statsView.setTextViewText(R.id.error, "No Account\nChange settings");
 			int emptyImage = R.drawable.empty;
 			statsView.setImageViewResource(R.id.power, emptyImage);
 			statsView.setImageViewResource(R.id.points, emptyImage);
 			statsView.setImageViewResource(R.id.hourzones, emptyImage);
 			statsView.setImageViewResource(R.id.placestar, emptyImage);
 			statsView.setImageViewResource(R.id.place, emptyImage);
 		}
 
 		return statsView;
 	}
 
 	private static RemoteViews updateViewErrors(Context context)
 	{
 		RemoteViews statsView = new RemoteViews(context.getPackageName(),
 		                                        R.layout.main);
 
 		if (TurfWidget.getError().equals("internet")) {
 			if (TurfWidget.DEBUG) {
 				Log.e("Error:", "Internet");
 			}
 			statsView.setTextViewText(R.id.error, "No Connection to internet");
 
 		} else if (TurfWidget.getError().equals("server")) {
 			if (TurfWidget.DEBUG) {
 				Log.e("Error:", "Server");
 			}
 			statsView.setTextViewText(R.id.error,
 			                          "Something wrong with server?");
 
 		} else {
 			if (TurfWidget.DEBUG) {
 				Log.e("Error:", "Unkown");
 			}
 			statsView.setTextViewText(R.id.error, TurfWidget.getError());
 		}
 
 		int emptyImage = R.drawable.empty;
 		statsView.setImageViewResource(R.id.power, emptyImage);
 		statsView.setImageViewResource(R.id.points, emptyImage);
 		statsView.setImageViewResource(R.id.hourzones, emptyImage);
 		statsView.setImageViewResource(R.id.placestar, emptyImage);
 		statsView.setImageViewResource(R.id.place, emptyImage);
 
 		return statsView;
 	}
 
 	public static void resetAlert(Context context)
 	{
 		CharStats.setAlert(false);
 		RemoteViews statsView = new RemoteViews(context.getPackageName(),
 		                                        R.layout.main);
 
 		statsView.setImageViewResource(R.id.alert, R.drawable.empty);
 		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
 		ComponentName provider = new ComponentName(context, TurfWidget.class);
 		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(provider);
 
 		// FIXME: Need to bind eventhandlers here?
 
 		appWidgetManager.updateAppWidget(appWidgetIds, statsView);
 	}
 
 	@Override
 	public IBinder onBind(Intent intent)
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
