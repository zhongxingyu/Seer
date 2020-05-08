 package com.carolineleung.clickcontrols;
 
 import java.util.Random;
 
 import android.app.PendingIntent;
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 public class ClickControlsWidgetService extends Service {
 
	private static final String LOG = "com.carolineleung.clickcontrols";
 
 	@Override
 	public void onStart(Intent intent, int startId) {
 		Log.i(LOG, "ClickControlsWidgetProvider called");
 
 		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
 
 		int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
 
 		ComponentName thisWidget = new ComponentName(getApplicationContext(), ClickControlsWidgetProvider.class);
 		int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
 		Log.w(LOG, "From Intent" + String.valueOf(allWidgetIds.length));
 		Log.w(LOG, "Direct" + String.valueOf(allWidgetIds2.length));
 
 		for (int widgetId : allWidgetIds) {
 			// Create some random data
 			int number = (new Random().nextInt(100));
 
 			RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_layout);
 			Log.w("WidgetExample", String.valueOf(number));
 			// Set the text
 			remoteViews.setTextViewText(R.id.indicatorText, "up: " + String.valueOf(number));
 			// remoteViews.setViewVisibility(R.id.imageButton, );
 
 			// Register an onClickListener
 			Intent clickIntent = new Intent(this.getApplicationContext(), ClickControlsWidgetProvider.class);
 
 			clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
 			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
 
 			PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 			remoteViews.setOnClickPendingIntent(R.id.control, pendingIntent);
 
 			appWidgetManager.updateAppWidget(widgetId, remoteViews);
 		}
 
 		stopSelf();
 
 		super.onStart(intent, startId);
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 }
