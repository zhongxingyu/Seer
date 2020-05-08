 package com.tealeaf.plugin.plugins;
 
 import com.tealeaf.logger;
 import com.tealeaf.plugin.IPlugin;
 import java.io.*;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import java.util.Iterator;
 
 import com.google.analytics.tracking.android.GoogleAnalytics;
 import com.google.analytics.tracking.android.Tracker;
 import com.google.analytics.tracking.android.Fields;
 import com.google.analytics.tracking.android.GAServiceManager;
 import com.google.analytics.tracking.android.MapBuilder;
 import com.google.analytics.tracking.android.Logger.LogLevel;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.Context;
 import android.util.Log;
 import android.os.Bundle;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 
 public class GoogleAnalyticsPlugin implements IPlugin {
 
 	private Tracker mGaTracker;
 	private GoogleAnalytics mGaInstance;
 
 	public GoogleAnalyticsPlugin() {
 	}
 
 	public void onCreateApplication(Context applicationContext) {
 	}
 
 	public void onCreate(Activity activity, Bundle savedInstanceState) {
         PackageManager manager = activity.getPackageManager();
         String trackingID = "";
         try {
             Bundle meta = manager.getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA).metaData;
             if (meta != null) {
                 trackingID = meta.getString("GOOGLE_TRACKING_ID");
             }
         } catch (Exception e) {
             android.util.Log.d("EXCEPTION", "" + e.getMessage());
         }
 
 		logger.log("{googleAnalytics} Initializing from manifest with googleTrackingID=", trackingID);
 
 		mGaInstance = GoogleAnalytics.getInstance(activity);
 		//mGaInstance.getLogger().setLogLevel(LogLevel.VERBOSE);
 
 		mGaTracker = mGaInstance.getTracker(trackingID);
 
 		mGaTracker.send(MapBuilder
 				.createEvent("UX", "appstart", null, null)
 				.set(Fields.SESSION_CONTROL, "start")
 				.build()
 				);
 	}
 
     public void track(final String json) {
		final Tracker tracker = mGATracker;
 
 		new Thread(new Runnable() {
 			public void run() {
 
 				String eventName = "noName";
 				try {
 					JSONObject obj = new JSONObject(json);
 					eventName = obj.getString("eventName");
 					JSONObject paramsObj = obj.getJSONObject("params");
 					Iterator<String> iter = paramsObj.keys();
 
 					if (paramsObj.length() == 1) {
 						String key = iter.next();
 						String value = null;
 						try {
 							value = paramsObj.getString(key);
 						} catch (JSONException e) {
 							logger.log("{googleAnalytics} track - failure: " + eventName + " - " + e.getMessage());
 						}
 
 						tracker.send(MapBuilder
 							.createEvent(eventName, key, value, null)
 							.build()
 							);
 						logger.log("{googleAnalytics} track - success: category=", eventName, "action=", key, "label=", value);
 					} else {
 						String value = paramsObj.toString();
 						tracker.send(MapBuilder
 								.createEvent(eventName, "JSON", value, null)
 								.build()
 								);
 						logger.log("{googleAnalytics} track - success: category=", eventName, "action='JSON' label=", value);
 					}
 
 				} catch (JSONException e) {
 					logger.log("{googleAnalytics} track - failure: " + eventName + " - " + e.getMessage());
 				}
 
 			}
 		}).start();
     }
 
 	public void trackScreen(String screenName) {
 		mGaTracker.set(Fields.SCREEN_NAME, screenName);
 
 		mGaTracker.send(MapBuilder.createAppView().build());
 	}
 
 	public void onResume() {
 	}
 
 	public void onStart() {
 	}
 
 	public void onPause() {
 	}
 
 	public void onStop() {
 	}
 
 	public void onDestroy() {
 	}
 
 	public void onNewIntent(Intent intent) {
 	}
 
 	public void setInstallReferrer(String referrer) {
 	}
 
 	public void onActivityResult(Integer request, Integer result, Intent data) {
 	}
 
 	public boolean consumeOnBackPressed() {
 		return true;
 	}
 
 	public void onBackPressed() {
 	}
 }
