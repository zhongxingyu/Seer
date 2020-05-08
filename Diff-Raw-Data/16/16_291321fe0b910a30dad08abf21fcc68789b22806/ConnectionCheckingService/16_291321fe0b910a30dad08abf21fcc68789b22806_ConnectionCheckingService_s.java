 package com.vendsy.bartsy.venue.service;
 
 import org.json.JSONObject;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.net.wifi.WifiManager;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.vendsy.bartsy.venue.BartsyApplication;
 import com.vendsy.bartsy.venue.model.AppObservable;
 import com.vendsy.bartsy.venue.utils.Constants;
 import com.vendsy.bartsy.venue.utils.WebServices;
 import com.vendsy.bartsy.venue.view.AppObserver;
 
 public class ConnectionCheckingService extends Service implements AppObserver {
 
 	private static final String TAG = "ConnectionCheckingService";
 	
 	public static final String SERVICE_ORDER_TIMEOUT_EVENT = "SERVICE_ORDER_TIMEOUT_EVENT";
 
	private long wait = 12000; // 2 mins
 	// check for thread is running or not
 	private boolean isRunning = false;
 	private Thread thread;
 	private Handler handler = new Handler();
 
 	private BartsyApplication mApp;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		mApp = (BartsyApplication)getApplication();
         mApp.addObserver(this);
 
 		Log.i(TAG, "ConnectionCheckingService Service created...");
 		isRunning = true;
 		// Intiate NetworkThread
 		thread = new Thread(new NetworkThread());
 		thread.start();
 	}
 
 	/**
 	 * Our onDestroy() is called by the Android appliation framework when it
 	 * decides that our Service is no longer needed. Here we just kill the
 	 * background thread
 	 */
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		isRunning = false;
 		Toast.makeText(this, "Service destroyed...", Toast.LENGTH_LONG).show();
 	}
 
 	/**
 	 * We are not use the binder service so we return null.
 	 * 
 	 */
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	private class NetworkThread implements Runnable {
 		public void finalize() {
 
 			if (isRunning) {
 				// create the NetworkThread object in thread
 				thread = new Thread(new NetworkThread());
 				// start thread
 				thread.start();
 			}
 		}
 
 		public void run() {
 			while (isRunning) {
 				try {
 					
 					
 					// To update orders time out
 					mApp.notifyObservers(SERVICE_ORDER_TIMEOUT_EVENT);
 						
 					
 					if (!WebServices .isNetworkAvailable(ConnectionCheckingService.this)) {
 						
 						Log.w(TAG, "Network unavailable");
 						
						heartBeatSysCall();
						
 						// Attempt recovery by turning wifi off / on
 						
 						WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
 
 						if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
 							 Log.v(TAG, "WiFi enabled - disabling it");
 							 wifiManager.setWifiEnabled(false);
 						} else if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
 							 Log.v(TAG, "WiFi disabled - enabling it");
 							 wifiManager.setWifiEnabled(true);
 						} else {
 							 Log.v(TAG, "WiFi status: " + wifiManager.getWifiState());							
 						}
 						
 						
 						// Handler to show UI-related events
 						handler.post(new Runnable() {
 
 							@Override
 							public void run() {
 								
 								Toast.makeText(ConnectionCheckingService.this, "Network not available. Trying to fix this issue...", Toast.LENGTH_LONG).show();
 							}
 						});
 					}
 
 				} catch (Exception e) {
 					Log.w("connection checking service",
 							" ******************************** Exception ***********************************"
 									+ e.getMessage());
 					e.printStackTrace();
 				}
 				try {
 					// Thread in sleep
 					Thread.sleep(wait);
 				} catch (InterruptedException e) {
 
 					e.printStackTrace();
 				}
 
 			}
 		}
 
 	}
 
 	@Override
 	public void update(AppObservable o, Object arg) {
 				
 	}
 	
 	// HeartBeat web service calling 
 	private void heartBeatSysCall() {
 
 			Log.v(TAG, "handlingHeartBeat");
 			
 			new Thread() {
 				public void run() {
 					Log.v(TAG, "In thread");
 					// Checking venue id is null or not
 						if (mApp.venueProfileID!=null) {
 							try {
 								// Created jsonobject
 								JSONObject postData = new JSONObject();
 								postData.put("venueId", mApp.venueProfileID);
 								// Heart beat Webservice calling
 								WebServices.postRequest(Constants.URL_HEART_BEAT_VENUE,	postData, getApplicationContext());
 	
 							} catch (Exception e) {
 								e.printStackTrace();
 							}
 						}
 				}
 			}.start();
 	}
 
 }
