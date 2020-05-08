 /**
  * Defines a service which performs all data collection and sends it off to the
  * server. This class's main job is to establish 2 recurring tasks (one for data
  * collection and one for sending the data) which will run at defined intervals.
  */
 
 package edu.wpi.cs.peds.hmn.appcollector;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 import edu.wpi.cs.peds.hmn.appstatviewer.IObservable;
 import edu.wpi.cs.peds.hmn.appstatviewer.IObserver;
 import edu.wpi.cs.peds.hmn.log.HmnLog;
 import edu.wpi.cs.peds.hmn.stats.GlobalDataCollector;
 import edu.wpi.cs.peds.hmn.stats.JSONSender;
 import edu.wpi.cs.peds.hmn.stats.apps.Application;
 import edu.wpi.cs.peds.hmn.stats.apps.GlobalAppList;
 
 /**
  * @author Austin Noto-Moniz, austinnoto@wpi.edu
  * @author Richard Brown, rpb111@wpi.edu
  * 
  */
 public class AppCollectorService extends Service implements IObservable {
 
 	public class MyBinder extends Binder {
 		public AppCollectorService getService() {
 			return AppCollectorService.this;
 		}
 	}
 	
 	// Allows easier debugging by restricting the service from starting before
 	// you're ready
 	public static boolean startOnBoot = false;
 	
 	private final List<IObserver> observers = new ArrayList<IObserver>();
 	private final Handler handler = new Handler();
 	private ConnectivityManager connectivityManager = null;
 	private final IBinder binder = new MyBinder();
 	BroadcastReceiver receiver;
 	
 	/**
 	 * The data gathering task, which is set to run every 2 seconds.
 	 */
 	private final long dataGatheringPeriod = 2000;
 	private Runnable dataGatheringTask = new Runnable() {
 		public void run() {
 			Log.i(HmnLog.HMN_LOG_TAG, "Collecting data.");
 			
 			GlobalDataCollector.getInstance().gatherStats(getApplicationContext());
 			notifyObservers();
 
 			handler.postDelayed(this, dataGatheringPeriod);
 		}
 	};
 	
 	/**
 	 * The data sending task, which is set to run every 30 seconds.
 	 */
	private final long dataSendingPeriod = 5000;
 	private Runnable dataSendingTask = new Runnable() {
 		public void run() {
 			Log.i(HmnLog.HMN_LOG_TAG, "Transmitting data.");
 			
 			JSONSender.post();
 
 			handler.postDelayed(this, dataSendingPeriod);
 		}
 	};
 
 	
 	@Override
 	public IBinder onBind(Intent intent)
 	{
 		return binder;
 	}
 
 	@Override
 	public boolean onUnbind(Intent intent)
 	{
 		super.onUnbind(intent);
 		return true;
 	}
 
 	/**
 	 * Initializes everything the service needs to run properly upon its
 	 * creation. The app list is initialized, network activity is recorded,
 	 * receivers are registered, and the data collection/transmission tasks are
 	 * started.
 	 */
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		JSONSender.loadAndSendBuffer(this);
 		
 		ActivityManager activityManager = (ActivityManager)getSystemService(Activity.ACTIVITY_SERVICE);
 		GlobalAppList.init(getPackageManager(), activityManager);
 		
 		Log.i(HmnLog.HMN_LOG_TAG,"!!!!!!!!!!SERVICE GET STARTED!!!!!");
 		if (connectivityManager == null) {
 			connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
 			NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
 			NetDevice lastConnectionType = NetDevice.determineNetworkConnection(activeNetwork);
 			for (Application app : GlobalAppList.getInstance().getAllApps())
 				app.lastConnectionType = lastConnectionType;
 		}
 		if (activityManager == null) {
 			activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
 		}
 
 		Log.d(HmnLog.HMN_LOG_TAG, "App Collector Service created.");
 
 		receiver = new StatCollectorBroadcastReceiver();
 
 		// Register receiver to modify logic on events
 		registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
 		registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
 		registerReceiver(receiver, new IntentFilter(Intent.ACTION_SHUTDOWN));
 		registerReceiver(receiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
 		// TODO register receiver on package install, package change in
 		// data/cache, package replaced...there are many
 
 		// Begin periodic sampling, next sample @ networkSamplingRateMilli
 		handler.postDelayed(dataGatheringTask, dataGatheringPeriod);
 		handler.postDelayed(dataSendingTask, dataSendingPeriod);
 	}
 	
 	/**
 	 * Ensures the app completely stops by stopping its recurring tasks and no
 	 * longer receiving broadcast events.
 	 */
 	@Override
 	public void onDestroy() {
 		JSONSender.stashBuffer(this);
 		
 		handler.removeCallbacks(dataGatheringTask);
 		handler.removeCallbacks(dataSendingTask);
 		
 		unregisterReceiver(receiver);
 	}
 
 	/**
 	 * Tells the app how to handle certain actions posted to it. If the screen
 	 * turns off, data collection is paused. When the screen is turned back on,
 	 * data collection resumes.
 	 */
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		if (intent != null) {
 			String action = intent.getStringExtra("action");
 			if (action != null) {
 				if (action.equals(Intent.ACTION_SCREEN_ON)) {
 					// Begin periodic sampling, next sample @
 					// networkSamplingRateMilli
 					handler.postDelayed(dataGatheringTask,dataGatheringPeriod);
 					handler.postDelayed(dataSendingTask, dataSendingPeriod);
 				} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
 					// Unregister the handler events
 					handler.removeCallbacks(dataGatheringTask);
 					handler.removeCallbacks(dataSendingTask);
 				}
 			}
 		}
 		Log.d(HmnLog.HMN_LOG_TAG, "Service startcommand was called.");
 
 		return START_STICKY;
 	}
 	
 	
 	public void addObserver(IObserver O) {
 		if (!observers.contains(O)) {
 			observers.add(O);
 		}
 	}
 	
 	public int countObservers() {
 		return observers.size();
 	}
 
 	public void deleteObserver(IObserver O) {
 		if (observers.contains(O)) {
 			observers.remove(O);
 		}
 	}
 
 	public void deleteObservers() {
 		observers.clear();
 	}
 	
 	public void notifyObservers() {
 		for (IObserver observer : observers) {
 			observer.update(this, null);
 		}
 	}
 
 	public void notifyObservers(Object arg) {
 		for (IObserver observer : observers) {
 			observer.update(this, arg);
 		}
 	}
 }
