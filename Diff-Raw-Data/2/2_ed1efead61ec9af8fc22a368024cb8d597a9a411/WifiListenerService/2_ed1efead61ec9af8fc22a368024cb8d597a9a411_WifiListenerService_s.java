 package com.tikalk.wifilistener;
 
 import java.util.Iterator;
 import java.util.List;
 
 import com.tikalk.tools.DBTool;
 
 import android.app.ActivityManager;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.location.LocationManager;
 import android.net.wifi.WifiManager;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 
 public class WifiListenerService extends Service {
 	/**
 	 * CONSTANTS
 	 */
 	public final static String TAG = WifiListenerService.class.getName();
 
 	/**
 	 * MEMBERS
 	 */
 	//broadcast receiver for wifi changes
 	BroadcastReceiver mWifiChanges;
 	// This is the object that receives interactions from clients.  See
     // RemoteService for a more complete example.
     private final IBinder mBinder = new LocalBinder();
 
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		//int retCode =  super.onStartCommand(intent, flags, startId);
 		//add broadcast receievers\
 		Log.d("registerReciever", "registering");
 		//for wifi we want when scan results are available or wifi state changed
 		Log.d(TAG, "adding wifi broadcast receiver");
 		mWifiChanges = new WifiChangeBroadcastReciever();
 		
 		IntentFilter scansAvailable = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
 		registerReceiver(mWifiChanges, scansAvailable);
 		IntentFilter wifiChange = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
 		registerReceiver(mWifiChanges, wifiChange);
 		//toggle the component on
 		setBootReceiver(true);
 		
 		return START_STICKY;
 	}
 
 	
 	
 	public static boolean isServiceRunning(Context context){
 		boolean serviceRunning = false;
 		ActivityManager actManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
 		List<ActivityManager.RunningServiceInfo> runningServicesList = actManager.getRunningServices(200);
 		Iterator<ActivityManager.RunningServiceInfo> listIterator = runningServicesList.iterator();
 		while (listIterator.hasNext()) {
 			ActivityManager.RunningServiceInfo runningServiceInfo =
 				(ActivityManager.RunningServiceInfo) listIterator.next();
 			//Log.d(TAG, runningServiceInfo.service.getClassName() + "is running");
 			if(runningServiceInfo.service.getClassName().equals(WifiListenerService.class.getName())){
 				serviceRunning = true;
 			}
 		}
 		return serviceRunning;
 	}
 	//when destroyed make sure to remove all of the intent listners
 	public void unregisterRecievers(){
 		Log.d("registerReciever", "unregistering");
 		setBootReceiver(false);
 		unregisterReceiver(mWifiChanges);
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return mBinder;
 	}
 	
 	/**
      * Class for clients to access.  Because we know this service always
      * runs in the same process as its clients, we don't need to deal with
      * IPC.
      */
     public class LocalBinder extends Binder {
     	public WifiListenerService getService() {
             return WifiListenerService.this;
         }
     }
     
     private void setBootReceiver(boolean enabled){
     	Log.d(TAG, "toggling boot broadcast reciever " + enabled);
     	int flag=(enabled ?
     	            PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
     	            PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
    	ComponentName component=new ComponentName("com.tikalk.wifinotify", BootBroadcastReceiver.class.getName());
     	int setting = getPackageManager().getComponentEnabledSetting(component);
     	try {
 			ActivityInfo recInfo =  getPackageManager().getReceiverInfo(component, 0);
 			Log.d(TAG, "reciever info is " + recInfo.packageName + ", " + recInfo.toString());
 		} catch (NameNotFoundException e) {
 			Log.d(TAG, "failed to get receiver info");
 		}
     	Log.d(TAG, "setting is " + setting + " before set");
     	getPackageManager()
     	    .setComponentEnabledSetting(component, flag,
     	                                PackageManager.DONT_KILL_APP);
     	setting = getPackageManager().getComponentEnabledSetting(component);
     	Log.d(TAG, "setting is " + setting + " after set");
     }
 }
