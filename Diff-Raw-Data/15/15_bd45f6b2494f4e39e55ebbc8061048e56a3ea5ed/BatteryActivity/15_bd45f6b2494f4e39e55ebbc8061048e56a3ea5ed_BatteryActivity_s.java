 package org.tjsimmons.GameBatteryMeter;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.widget.RemoteViews;
 import android.appwidget.AppWidgetManager;
 
 public class BatteryActivity extends Activity {
	Context context = this;
	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
	ComponentName thisWidget = new ComponentName(context, GameBatteryMeterWidgetProvider.class);
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getBatteryLevel();
 	}
 	
 	private void getBatteryLevel() {
 	    BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
 	        public void onReceive(Context context, Intent intent) {
 	            context.unregisterReceiver(this);
 	            int rawlevel = intent.getIntExtra("level", -1);
 	            int scale = intent.getIntExtra("scale", -1);
 	            int level = -1;
 	            
 	            if (rawlevel >= 0 && scale > 0) {
 	                level = (rawlevel * 100) / scale;
 	            }
 	            
 	            views.setTextViewText(R.id.status_text, ((Integer) level).toString() + "%");
 	            appWidgetManager.updateAppWidget(thisWidget, views);
 	        }
 	    };
 	    
 	    IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
 	    registerReceiver(batteryLevelReceiver, batteryLevelFilter);
 	}
 }
