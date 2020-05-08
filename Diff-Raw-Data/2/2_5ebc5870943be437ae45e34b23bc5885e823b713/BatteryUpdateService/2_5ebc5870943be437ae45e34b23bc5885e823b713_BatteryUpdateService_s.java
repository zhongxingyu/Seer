 package org.tjsimmons.GameBatteryMeter;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.BatteryManager;
 import android.os.Handler;
 import android.os.IBinder;
 import android.widget.RemoteViews;
 import android.appwidget.AppWidgetManager;
 import android.util.Log;
 
 public class BatteryUpdateService extends Service {
 	Context context;
 	AppWidgetManager appWidgetManager;
 	RemoteViews views;
 	ComponentName thisWidget;
 	Handler serviceHandler;
 	long statusUpdateMillis = 5000;//600000;		// 10 minutes
 	
 	private Runnable updateBatteryLevelTask = new Runnable() {
 		public void run() {
 			updateBatteryLevel();
 			serviceHandler.postDelayed(this, statusUpdateMillis);
 		}
 	};
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		//Log.v("BatteryUpdateService::onCreate", "onCreate called");
 		
 		serviceHandler = new Handler();
 		
 		context = this;
 		appWidgetManager = AppWidgetManager.getInstance(context);
 		views = new RemoteViews(context.getPackageName(), R.layout.main);
 		thisWidget = new ComponentName(context, GameBatteryMeterWidgetProvider.class);
 		
 		//serviceHandler.post(updateBatteryChargeTask);
 		serviceHandler.post(updateBatteryLevelTask);
 	}
 	
 	@Override
 	public void onDestroy() {
 		serviceHandler.removeCallbacks(updateBatteryLevelTask);
 		//serviceHandler.removeCallbacks(updateBatteryChargeTask);
 		//Log.v("BatteryUpdateService::onDestroy", "onDestroy called");
 		super.onDestroy();
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		//Log.v("BatteryUpdateService::onStartCommand", "onStartCommand called");
 		return START_STICKY;
 	}
 	
 	private void updateBatteryLevel() {
 		//Log.v("BatteryUpdateService::updateBatteryLevel", "updateBatteryLevel called");
 	    BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
 	        public void onReceive(Context context, Intent intent) {	        	
 	            context.unregisterReceiver(this);
 	            
 	            updateChargeStatus(intent);
 	            updateCapacityStatus(intent);
 	            
 	            appWidgetManager.updateAppWidget(thisWidget, views);
 	        }
 	    };
 	    
 	    IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
 	    registerReceiver(batteryReceiver, batteryFilter);
 	}
 	
 	private void updateChargeStatus(Intent intent) {
 		//Log.v("BatteryUpdateService::updateChargeStatus", "updateChargeStatus called");
 	
 		String mDrawableName = "chargeoff";
 		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
         boolean isCharging = 	status == BatteryManager.BATTERY_STATUS_CHARGING ||
                 				status == BatteryManager.BATTERY_STATUS_FULL;
         int statusID;
         
         if (isCharging) {
         	mDrawableName = "chargeon";
         }
         
         //Log.v("BatteryUpdateService::updateChargeStatus", "Charge Status: " + isCharging + ", Image: " + mDrawableName);
         
         statusID = getResources().getIdentifier(mDrawableName, "drawable", getPackageName());
         views.setImageViewResource(R.id.charge_image, statusID);
 	}
 	
 	private void updateCapacityStatus(Intent intent) {
 		int rawlevel = intent.getIntExtra("level", -1);
         int scale = intent.getIntExtra("scale", -1);
         int level = -1;
         int levelID;
         String mDrawableName;
         
         if (rawlevel >= 0 && scale > 0) {
             level = (rawlevel * 100) / scale;
         }
         
         mDrawableName = numToWord(((Integer) (level / 10)).toString() + "0");
         
         levelID = getResources().getIdentifier(mDrawableName, "drawable", getPackageName());
         
         views.setImageViewResource(R.id.status_image, levelID);
 	}
 	
 	private String numToWord(String num) {
 		String word;
 		
 		switch (Integer.parseInt(num)) {
 		case 0:
 			word = "zero";
 			break;
 		case 10:
 			word = "ten";
 			break;
 		case 20:
 			word = "twenty";
 			break;
 		case 30:
 			word = "thirty";
 			break;
 		case 40:
 			word = "forty";
 			break;
 		case 50:
 			word = "fifty";
 			break;
 		case 60:
 			word = "sixty";
 			break;
 		case 70:
 			word = "seventy";
 			break;
 		case 80:
 			word = "eighty";
 			break;
 		case 90:
 			word = "ninety";
 			break;
 		case 100:
 			word = "hundred";
 			break;
 		default:
 			word = "hundred";
 			break;
 		}
 		
 		return word;
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 }
 
