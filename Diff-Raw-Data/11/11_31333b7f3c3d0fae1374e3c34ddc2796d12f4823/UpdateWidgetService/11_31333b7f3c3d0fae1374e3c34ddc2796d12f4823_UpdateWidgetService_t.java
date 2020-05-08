 package com.example.helloandroid.service;
 
 import android.app.PendingIntent;
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.RemoteViews;
 
import com.example.helloandroid.R;
import com.example.helloandroid.prefs.NextBusObserverConfig;

 public class UpdateWidgetService extends Service {
 	private static final String TAG = UpdateWidgetService.class.getName();
 	
 	public static final String EXTRA_NEXT_TIME = "EXTRA_NEXT_TIME";
 	public static final String EXTRA_SECOND_TIME = "EXTRA_SECOND_TIME";
 
 	@Override
 	public void onStart(Intent intent, int startId) {
 		Log.i(TAG, "Called");
 
 		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
 
 		int[] widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
 
 		for (int widgetId : widgetIds) {
 
 			String route_info;
 			String next_time;
 			
 			NextBusObserverConfig prefs = new NextBusObserverConfig(getApplicationContext(), widgetId);
 			if (prefs.getRoute() == null) {
 				// Not a known ID, display a error message.
 				next_time = "--";
 				route_info = "No data.\n";
 				
 			} else {
 			
 				long now = System.currentTimeMillis();
 	
 				String route = prefs.getRoute().getLongLabel();
 				String stop = prefs.getStop().getLongLabel();
 				String direction = prefs.getDirection().getShortLabel();
 				
 				long nextTimeMs = intent.getLongExtra(EXTRA_NEXT_TIME, -1);
 				if (nextTimeMs <= 0) {
 					// No prediction time
 					next_time = "--";
 					
 				} else {
 					long seconds = (nextTimeMs - now) / 1000;
 					long minutes = seconds / 60;
 					seconds -= minutes * 60;
 					next_time = minutes + "m " + seconds + "s";
 				}
 	
 				if (direction == null) direction = "";
 				if (stop == null) stop = "";
 				
 				route += " -> " + direction;
 				
 				route_info = route + "\n" + stop;
 			}
 			
 			RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_layout);
 			// Set the text
 			remoteViews.setTextViewText(R.id.update, route_info);
 			remoteViews.setTextViewText(R.id.next_time, next_time);
 
 			// Register an onClickListener to refresh the widget
 			Intent clickIntent = new Intent(this.getApplicationContext(), MBTABackgroundService.class);
 
 			clickIntent.setAction(MBTABackgroundService.ACTION_WAKEUP_IMMEDIATE + "-" + widgetId);
 			clickIntent.putExtra(MBTABackgroundService.EXTRA_IMMEDIATE, true);
 			clickIntent.putExtra(MBTABackgroundService.EXTRA_WIDGET_ID, widgetId);
 
 			PendingIntent pendingIntent = PendingIntent.getService(
 					getApplicationContext(), 0, clickIntent,
 					PendingIntent.FLAG_UPDATE_CURRENT);
 			remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
 			
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
