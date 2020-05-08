 package me.lhom.timeout;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.wifi.WifiManager;
 import android.os.BatteryManager;
 import android.os.PowerManager;
 import android.text.format.DateUtils;
 import android.text.format.Time;
 import android.util.Log;
 
 public class EventReceiver extends BroadcastReceiver {
 
 	private static final String ACTION_TIMEOUT = "me.lhom.timeout.action.TIMEOUT";
 	private static boolean isPowerPlugged = false;
 	private static boolean userDisabledWifi;
	private static boolean wifiChanging = false;
 
 	public EventReceiver(Context context) {
 		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 		userDisabledWifi = !wifiManager.isWifiEnabled();
 	}
 
 	public EventReceiver() {
 	}
	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Log.v("Timeout", "EventReceiver received "+intent);
 		String action = intent.getAction();
 		if (action!=null) {
 			if (Intent.ACTION_SCREEN_OFF.equals(action)) {
 				// start a timer to disable Wifi
 				setPendingAlarm(context);
 			}
 			else if (Intent.ACTION_SCREEN_ON.equals(action)) {
 				// cancel the timer to disable Wifi
				// TODO: enable back wifi only it is was enabled before we disabled it
 				setPendingAlarm(context);
 			}
 			else if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
 				isPowerPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)!=0;
 				setPendingAlarm(context);
 			}
 			else if (ACTION_TIMEOUT.equals(action)) {
 				setWifiState(context, false);
 			}
 			else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
 				WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				Log.i("Timeout", "wifi changed "+intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)+" / "+wifiManager.isWifiEnabled()+" by us:"+wifiChanging);
				if (wifiChanging)
					wifiChanging = false;
				else {
					userDisabledWifi = !wifiManager.isWifiEnabled();
 					//TODO: if disabled, cancel the possible alarm
 				}
 			}
 		}
 	}
 
 	private static void setPendingAlarm(Context context) {
 		// get the screen state
 		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
 		boolean screenIsOn = pm.isScreenOn();
 
 		Log.d("Timeout", "screenIsOn:"+screenIsOn+" isPlugged:"+isPowerPlugged+" userDisabled:"+userDisabledWifi);
 
 		Intent intentService = new Intent(context, EventReceiver.class);
 		intentService.setAction(ACTION_TIMEOUT);
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentService, 0);
 		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 
 		// only disable if power not plugged and screen is off
 		if (screenIsOn || isPowerPlugged) {
 			alarmManager.cancel(pendingIntent);
 			if (!userDisabledWifi)
 				setWifiState(context, true);
 		} else {
 			Time time = new Time();
 			time.set(System.currentTimeMillis() + 10 * DateUtils.SECOND_IN_MILLIS); //TODO: make the 10s a parameter
 			long nextStart = time.toMillis(false);
 			alarmManager.set(0, nextStart, pendingIntent);
 		}
 	}
 
 	private static void setWifiState(Context context, boolean enabled) {
 		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 		if (wifiManager.isWifiEnabled() != enabled) {
			wifiChanging = true;
			Log.d("Timeout", "We should enable WIFI "+enabled);
 			wifiManager.setWifiEnabled(enabled);
 		}
 	}
 }
