 package com.jamessantangelo.batteryline;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 import android.view.View;
 import android.widget.RemoteViews;
 import android.widget.ProgressBar;
 
 public class BatteryLineWidgetProvider extends AppWidgetProvider
 {
 	private static final String TAG = "BatteryLine";
 	private static final String ACTION_LINE_CLICKED = "com.jamessantangelo.batteryline.LINE_CLICKED";
	private static BroadcastReceiver registeredReceiver = null;
	private static boolean textShowing = true;
 
 	public void onUpdate(Context context, AppWidgetManager widgetManager,
 		int[] widgetIds)
 	{
 		Log.d(TAG, "onUpdate...");
 
 		int numberOfWidgets = widgetIds.length;
 
 		for (int widgetIndex = 0; widgetIndex < numberOfWidgets; ++widgetIndex)
 		{
 			int widgetId = widgetIds[widgetIndex];
 
 			Intent intent = new Intent(context, BatteryLineWidgetProvider.class);
 			intent.setAction(ACTION_LINE_CLICKED);
 			PendingIntent pendingIntent = PendingIntent.getBroadcast(
 				context, 0, intent, 0);
 
 			RemoteViews views = new RemoteViews(context.getPackageName(), 
 				R.layout.linelayout);
 			views.setOnClickPendingIntent(R.id.main_layout, pendingIntent);
 
 			widgetManager.updateAppWidget(widgetId, views);
 		}
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent)
 	{
 		super.onReceive(context, intent);
 
 		if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED))
 		{
 			RemoteViews views = new RemoteViews(context.getPackageName(), 
 				R.layout.linelayout);
 			int level = intent.getIntExtra("level", 0);
 			views.setTextViewText(R.id.debugtext, "" + level);
 			views.setProgressBar(R.id.line, 100, level, false);
 
 			ComponentName componentName = new ComponentName(context, BatteryLineWidgetProvider.class);
 			AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
 			widgetManager.updateAppWidget(componentName, views);
 		}
 		else if (intent.getAction().equals(ACTION_LINE_CLICKED))
 		{
 			RemoteViews views = new RemoteViews(context.getPackageName(), 
 				R.layout.linelayout);
 			if (textShowing)
 			{
				Log.d(TAG, "Hiding text...");
 				views.setViewVisibility(R.id.debugtext, View.INVISIBLE);
 				textShowing = false;
 			}
 			else
 			{
				Log.d(TAG, "Un-hiding text...");
 				views.setViewVisibility(R.id.debugtext, View.VISIBLE);
 				textShowing = true;
 			}
 
 			ComponentName componentName = new ComponentName(context, BatteryLineWidgetProvider.class);
 			AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
 			widgetManager.updateAppWidget(componentName, views);
 		}
 	}
 
 	@Override
 	public void onEnabled(Context context)
 	{
 		textShowing = true;
 
 		Log.d(TAG, "Registering BatteryLine broadcast receiver...");
 		registeredReceiver = this;
 		context.getApplicationContext().registerReceiver(registeredReceiver,
 			new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
 	}
 
 	@Override
 	public void onDisabled(Context context)
 	{
 		Log.d(TAG, "Cleaning up Battery Line...");
 		if (registeredReceiver != null)
 		{
 			try
 			{
 				context.getApplicationContext().unregisterReceiver(registeredReceiver);
 			}
 			catch (Exception e)
 			{
 				Log.d(TAG, "Unable to deregister broadcast receiver: " + e);
 			}
 			finally
 			{
 				registeredReceiver = null;
 			}
 		}
 	}
 }
