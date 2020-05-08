 package com.tealeaf.plugin.plugins;
 import java.util.Map;
 import org.json.JSONObject;
 import org.json.JSONArray;
 import org.json.JSONException;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import com.tealeaf.EventQueue;
 import com.tealeaf.GLSurfaceView;
 import com.tealeaf.TeaLeaf;
 import com.tealeaf.logger;
 import com.tealeaf.event.PluginEvent;
 import android.content.pm.PackageManager;
 import android.content.pm.ApplicationInfo;
 import android.os.Bundle;
 import java.util.HashMap;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 
 import com.tealeaf.plugin.IPlugin;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Intent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.util.Log;
 
 import com.tealeaf.EventQueue;
 import com.tealeaf.event.*;
 
 public class GeolocPlugin implements IPlugin {
 	public class GeolocEvent extends com.tealeaf.event.Event {
 		boolean failed;
 		double longitude, latitude;
 
 		public GeolocEvent() {
 			super("geoloc");
 			this.failed = true;
 		}
 
 		public GeolocEvent(double longitude, double latitude) {
 			super("geoloc");
 			this.failed = false;
 			this.longitude = longitude;
 			this.latitude = latitude;
 		}
 	}
 
 	public class MyLocationListener implements LocationListener {
 		public boolean enabled;
 		public int callback;
 		LocationManager mgr;
 
 		@Override
 			public void onLocationChanged(Location loc) {
 				logger.log("{geoloc} Received location changed event");
 
 				// If position is enabled,
 				if (enabled) {
 					EventQueue.pushEvent(new GeolocEvent(loc.getLongitude(), loc.getLatitude()));
 					mgr.removeUpdates(this);
 				} else {
 					EventQueue.pushEvent(new GeolocEvent());
 				}
 			}
 
 		@Override
 			public void onProviderDisabled(String provider) {
 				enabled = false;
 				logger.log("{geoloc} Location provider disabled: ", provider);
 
 				EventQueue.pushEvent(new GeolocEvent());
 			}
 
 		@Override
 			public void onProviderEnabled(String provider) {
 				enabled = true;
 				logger.log("{geoloc} Location provider enabled: ", provider);
 			}
 
 		@Override
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 				logger.log("{geoloc} Location provider status changed: ", provider, status);
 			}
 	}
 
 	boolean _gps_ask;
 	MyLocationListener _listener;
 	Context _ctx;
 	LocationManager _mgr;
 
 	public GeolocPlugin() {
 		_listener = new MyLocationListener();
 	}
 
 	public void onCreateApplication(Context applicationContext) {
 		_ctx = applicationContext;
 	}
 
 	public void onCreate(Activity activity, Bundle savedInstanceState) {
 		_mgr = (LocationManager)_ctx.getSystemService(Context.LOCATION_SERVICE);
 		_listener.enabled = _mgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
 		_listener.mgr = _mgr;
 
 		if (_listener.enabled) {
 			logger.log("{geoloc} GPS provider is initially enabled.");
 		} else {
 			logger.log("{geoloc} GPS provider is initially DISABLED.");
 		}
 
 		_gps_ask = false;
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
 
 	private void showGPSDisabledAlertToUser() {
 		TeaLeaf.get().runOnUiThread(new Runnable() {
 			public void run() {
 				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TeaLeaf.get());
 				alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
 								  .setCancelable(false)
 								  .setPositiveButton("Goto Settings Page To Enable GPS",
 									new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id){
 						Intent callGPSSettingIntent = new Intent(
 							android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 						TeaLeaf.get().startActivity(callGPSSettingIntent);
 					}
 				});
 
 				alertDialogBuilder.setNegativeButton("Cancel",
 									new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 
 				AlertDialog alert = alertDialogBuilder.create();
 				alert.show();
 			}
 		});
 	}
 
 	public void onRequest(String jsonData) {
 		try {
 			if (_listener.enabled) {
 				logger.log("{geoloc} Requesting single GPS update");
 			} else {
 				if (_gps_ask) {
 					logger.log("{geoloc} GPS is disabled but requesting anyway in case it changed");
 				} else {
 					logger.log("{geoloc} Presenting GPS disabled alert to user");
 
 					showGPSDisabledAlertToUser();
 
 					_gps_ask = true;
 				}
 			}
 
 			String provider = null;
 
 			if (_mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 				provider = LocationManager.GPS_PROVIDER;
 			} else if (_mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
 				provider = LocationManager.NETWORK_PROVIDER;
 			}
 
 			if (provider != null) {
 				_mgr.requestLocationUpdates(provider, 0, 0, _listener);
 			} else {
 				EventQueue.pushEvent(new GeolocEvent());
 			}
 		} catch (Exception e) {
 			logger.log(e);
 			e.printStackTrace();
 			EventQueue.pushEvent(new GeolocEvent());
 		}
 	}
 
 	public void logError(String error) {
 	}
 
 	public boolean consumeOnBackPressed() {
		return true;
 	}
 
 	public void onBackPressed() {
 	}
 }
 
