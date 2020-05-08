 package com.pilot51.lclock;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
import android.util.Log;
 
 public class AlertBuilder {
 	private Common common;
 	private String TAG;
 	private int n, alertTime;
 	private SharedPreferences extras;
 	private SharedPreferences.Editor editor;
 	private ArrayList<HashMap<String, Object>>
 		listNasa = new ArrayList<HashMap<String, Object>>(),
 		listSfn = new ArrayList<HashMap<String, Object>>();
 	private Activity activity;
 	private Intent intent;
 	private AlarmManager am;
 
 	protected Common newCommon() {
 		return new Common(activity);
 	}
 
 	protected AlertBuilder(final Activity activity) {
 		new Thread(new Runnable() {
 			public void run() {
 				AlertBuilder.this.activity = activity;
 				common = newCommon();
 				TAG = common.TAG;
 				extras = activity.getSharedPreferences("extraPref", 0);
 				am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
 				intent = common.intentAlarmReceiver();
 				cancelAlerts();
 				listNasa = common.readCache(1);
 				listSfn = common.readCache(2);
 				editor = extras.edit();
 				readAlertPref();
 			}
 		}).start();
 	}
 
 	protected void readAlertPref() {
 		try {
 			alertTime = Integer.parseInt(common.prefs.getString("alertTime", null));
 			alertTime = alertTime * 60000;
 		} catch (NumberFormatException e) {
 			alertTime = -1;
 		}
 		if (alertTime != -1) {
 			buildAlerts(1);
 			buildAlerts(2);
 		}
 		editor.putInt("nAlerts", n);
 		editor.commit();
 	}
 
 	private void cancelAlerts() {
 		//Log.d(TAG, "Alerts canceled");
 		int n = extras.getInt("nAlerts", 0);
 		//Log.d(TAG, "cancelAlerts: " + Integer.toString(n));
 		if (n > 0) {
 			//Log.d(TAG, "nAlerts > 0");
 			do {
 				n--;
 				//Log.d(TAG, "Canceled alert id: " + n);
 				PendingIntent pi = PendingIntent.getBroadcast(activity, n, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 				pi.cancel();
 				//am.cancel(pi);
 			} while (n > 0);
 		}
 	}
 
 	private void buildAlerts(int src) {
 		ArrayList<HashMap<String, Object>> list = src == 1 ? listNasa : listSfn;
 		String nameKey = src == 1 ? "mission" : "vehicle";
 		if (!list.isEmpty()) {
 			int i = 0;
 			HashMap<String, Object> event;
 			do {
 				event = list.get(i);
				try {
					if ((Integer)event.get("calAccuracy") >= List.ACC_MINUTE) {
						createAlarm(n, ((Calendar) event.get("cal")).getTimeInMillis(), (String)event.get(nameKey), src);
						n++;
					}
				} catch (NullPointerException e) {
					Log.w(TAG, "Time accuracy not available (likely because loaded cache from v0.6.0), skipping alert creation.");
					break;
				}
 				i++;
 			} while (i < list.size());
 		}
 	}
 
 	private void createAlarm(int id, long time, String name, int src) {
 		//Log.d(TAG, "Creating alert id: " + id);
 		intent.putExtra("time", alertTime);
 		intent.putExtra("name", name);
 		intent.putExtra("src", src);
 		PendingIntent pi = PendingIntent.getBroadcast(activity, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 		//Log.d(TAG, "Current time: " + new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss zzz").format(Calendar.getInstance().getTime()));
 		//Log.d(TAG, "Current time in milliseconds: " + System.currentTimeMillis());
 		//Calendar c = Calendar.getInstance();
 		//c.setTimeInMillis(time - alertTime);
 		//Log.d(TAG, "Assigned alert time: " + new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss zzz").format(c.getTime()));
 		//Log.d(TAG, "Assigned alert time in milliseconds: " + (time - alertTime));
 		if (System.currentTimeMillis() < time - alertTime)
 			am.set(AlarmManager.RTC_WAKEUP, time - alertTime, pi);
 	}
 }
