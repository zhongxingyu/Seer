 package com.turf.widget.turf;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.turf.alarm.AppService;
 import com.turf.alarm.OnAlarmReceiver;
 
 public class TurfWidget extends AppWidgetProvider
 {
 
 	public static final boolean DEBUG = false;
 	public static final String DEBUG_STRING = "TurfWidget DEBUG";
 	public static final String PREFS_UPDATE = "com.turf.widget.turf.PREFS_UPDATE";
 	public static final String WIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
 	public static final String TURF_LAUNCH = "com.turf.widget.turf.TURF_LAUNCH";;
 
 	public static String ERROR = null;
 
 	@Override
 	public void onEnabled(Context context)
 	{
 		if (DEBUG) {
 			Log.v(DEBUG_STRING, "onEnabled");
 		}
 
 		super.onEnabled(context);
 	}
 
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 	                     int[] appWidgetIds)
 	{
 		if (DEBUG) {
 			Log.v(DEBUG_STRING, "onUpdate");
 		}
 		super.onUpdate(context, appWidgetManager, appWidgetIds);
 	}
 
 	public void onReceive(Context context, Intent intent)
 	{
 		if (DEBUG) {
 			Log.v(DEBUG_STRING, "onReceive");
 		}
 
 		if (DEBUG) {
 			Log.v("onReceive", intent.getAction());
 		}
 
 		if (PREFS_UPDATE.equals(intent.getAction()) ||
 		    WIDGET_UPDATE.equals(intent.getAction())) {
 
 			Intent serviceIntent = new Intent(context, AppService.class);
 			serviceIntent.putExtra("vibrate", false);
 			serviceIntent.putExtra("kill", false);
 			context.startService(serviceIntent);
 
 			return;
 		} else if (TURF_LAUNCH.equals(intent.getAction())) {
 
 			AppService.resetAlert(context);
 			Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("se.turfwars.android");
 
 			if (launchIntent == null) {
 				Toast.makeText(context,
 				               "To use this function.\nPlease install the game Turf",
 				               Toast.LENGTH_LONG).show();
 			} else {
 				context.startActivity(launchIntent);
 			}
 			return;
 		}
 		super.onReceive(context, intent);
 	}
 
 	@Override
 	public void onDeleted(Context context, int[] appWidgetIds)
 	{
 
 		if (DEBUG) {
 			Log.v(DEBUG_STRING, "onDeleted");
 		}
 
 		// Brodcasta till onAlarm s att service avslutas!
 		Intent closeService = new Intent(context, OnAlarmReceiver.class);
 		closeService.setAction(OnAlarmReceiver.DELETE_WIDGET_SERVICE);
 		context.sendBroadcast(closeService);
 
 		super.onDeleted(context, appWidgetIds);
 	}
 
 	public void onDisable(Context context)
 	{
 		if (DEBUG) {
 			Log.v(DEBUG_STRING, "onDisable");
 		}
 		super.onDisabled(context);
 	}
 
 	public static synchronized void setError(String error)
 	{
 		ERROR = error;
 	}
 
 	public static synchronized String getError()
 	{
 		return ERROR;
 	}
 }
