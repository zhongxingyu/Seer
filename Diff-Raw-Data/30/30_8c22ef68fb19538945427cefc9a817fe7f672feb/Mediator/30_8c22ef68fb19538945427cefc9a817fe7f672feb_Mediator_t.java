 package co.shoutbreak;
 
 import android.content.Context;
 import co.shoutbreak.shared.C;
 import co.shoutbreak.shared.Flag;
 import co.shoutbreak.shared.SBLog;
 
 public class Mediator {
 	
 	private static final String TAG = "Mediator";
 	
 	// colleagues
 	private ShoutbreakService _service;
 	private Shoutbreak _ui;
 	private PreferenceManager _preferences;
 	private Notifier _notifier;
 	private LocationTracker _location;
	private DataListener _data;
 	
 	// state flags
 	private Flag _isUIAlive = new Flag();
 	private Flag _isPollingAlive = new Flag();
 	private Flag _isServiceConnected = new Flag();
 	private Flag _isServiceStarted = new Flag();
 	private Flag _isLocationAvailable = new Flag();
 	private Flag _isDataAvailable = new Flag();
 	
 	/* Mediator Lifecycle */
 	
 	public Mediator(ShoutbreakService service) {
     	SBLog.i(TAG, "new Mediator()");
 		// add colleagues
 		_service = service;
 		_service.setMediator(this);
 		_preferences = new PreferenceManager(_service.getSharedPreferences(C.PREFERENCE_FILE, Context.MODE_PRIVATE));
 		_preferences.setMediator(this);
 		_notifier = new Notifier(_service);
 		_notifier.setMediator(this);
 		_location = new LocationTracker(_service);
 		_location.setMediator(this);
		_data = new DataListener(_service);
		_data.setMediator(this);
		
		// initialize state
		_isLocationAvailable.set(_location.isLocationEnabled());
		_isDataAvailable.set(_data.isDataEnabled());
 	}
 	
 	public void registerUI(Shoutbreak ui) {
 		// ui is created before the mediator exists
 		// it must be added once the mediator is created
 		SBLog.i(TAG, "registerUI()");
 		_isUIAlive.set(true);
 		_ui = ui;
 		_ui.setMediator(this);
 	}
 	
 	public void unregisterUI(boolean forceKillUI) {
 		// called by ui's onDestroy() method
 		SBLog.i(TAG, "unregisterUI()");
 		if (_isUIAlive.get()) {
 			_isUIAlive.set(false);
 			_ui.unsetMediator();
 			if (forceKillUI) {
 				// forces UI to destroy itself if the mediator / service is killed off
 				SBLog.e(TAG, "force killed ui, service shutdown while ui running");
 				_ui.finish();
 			}
 			_ui = null;
 		} else {
 			SBLog.e(TAG, "ui is not alive, unable to unregister");
 		}
 	}
 	
 	public void kill() {
 		// removes all colleague references to the mediator
 		// called by service's onDestroy() method
     	SBLog.i(TAG, "kill()");
 		_service = null;
 		unregisterUI(true);
 		_preferences.unsetMediator();
 		_preferences = null;
 		_notifier.unsetMediator();
 		_notifier = null;
 		_location.unsetMediator();
 		_location = null;
		_data.unsetMediator();
		_data = null;
 	}
 	
 	/* Mediator Commands */
 	
 	public void onServiceConnected() {
 		// called when service handler binds ui and service
 		SBLog.i(TAG, "onServiceConnected()");
 		_isServiceConnected.set(true);
 	}
 	
 	public void onServiceDisconnected() {
 		// called when ui unbinds from the service
 		// shouldn't ever be called
 		SBLog.i(TAG, "onServiceDisconnected()");
 		_isServiceConnected.set(false);
 	}
 	
 	public void onServiceStart() {
 		SBLog.i(TAG, "onServiceStart()");
 		_isServiceStarted.set(true);
 		_isPollingAlive.set(false);
 	}
 	
 	public void appLaunchedFromUI() {
 		SBLog.i(TAG, "appLaunchedFromUI()");
 		_isServiceStarted.set(true);
 		if (_preferences.getBoolean(C.POWER_STATE_PREF, true)) {
 			_ui.setPowerState(true);
 		} else {
 			_ui.setPowerState(false);
 		}
 	}
 	
 	public void appLaunchedFromAlarm() {
 		SBLog.i(TAG, "appLaunchedFromAlarm()");
 		_isServiceStarted.set(true);
 		if (_preferences.getBoolean(C.POWER_STATE_PREF, true)) {
 			startPolling();
 		} else {
 			stopPolling();
 		}
 	}
 	
 	public void startPolling() {
 		SBLog.i(TAG, "startPolling()");
 		if (!_isPollingAlive.get() && _isLocationAvailable.get() && _isDataAvailable.get()) {
 			SBLog.i(TAG, "app fully functional");
 			_isPollingAlive.set(true);
 //			_service.startPolling();	
 		} else if (!_isLocationAvailable.get() || !_isDataAvailable.get()) {
 			if (!_isLocationAvailable.get()) {
 				SBLog.e(TAG, "unable to start service, location unavailable");
 			}
 			if (!_isDataAvailable.get()) {
 				SBLog.e(TAG, "unable to start service, data unavailable");
 			}
 			if (_isUIAlive.get()) {
 				_ui.setPowerState(false);
				_ui.unableToTurnOnApp();
 			} else {
 				onPowerDisabled();
 			}
 		} else if (_isPollingAlive.get()) {
 			SBLog.i(TAG, "service is already polling, unable to call startPolling()");
 		}
 	}
 	
 	public void stopPolling() {
 		SBLog.i(TAG, "stopPolling()");
 		if (_isPollingAlive.get()) {
 			_isPollingAlive.set(false);
 //			_service.stopPolling();
 		} else {
 			SBLog.i(TAG, "service is not polling, unable to call stopPolling()");
 		}
 	}
 	
 	public void onPowerEnabled() {
 		SBLog.i(TAG, "onPowerEnabled()");
 		_preferences.putBoolean(C.POWER_STATE_PREF, true);
 		_service.enableAlarmReceiver();
 		startPolling();
 	}
 	
 	public void onPowerDisabled() {
 		SBLog.i(TAG, "onPowerDisabled()");
 		_preferences.putBoolean(C.POWER_STATE_PREF, false);
 		_service.disableAlarmReceiver();
 		stopPolling();
 	}
 	
 	public void onLocationEnabled() {
 		SBLog.i(TAG, "onLocationEnabled()");
 		_isLocationAvailable.set(true);
 		if (_isUIAlive.get()) {

 		}
 	}
 	
 	public void onLocationDisabled() {
 		SBLog.i(TAG, "onLocationEnabled()");
 		_isLocationAvailable.set(false);
 		stopPolling();
 		if (_isUIAlive.get()) {
 			_ui.onLocationDisabled();
 		}
 	}
 	
 	public void onDataEnabled() {
 		SBLog.i(TAG, "onDataEnabled()");
 		_isDataAvailable.set(true);
 		if (_isUIAlive.get()) {
 			_ui.onDataDisabled();
 		}
 	}
 	
 	public void onDataDisabled() {
 		SBLog.i(TAG, "onDataEnabled()");
 		_isDataAvailable.set(false);
 		stopPolling();
 		if (_isUIAlive.get()) {
 			
 		}
 	}
 }
 	/*
 
 	public void initIsLocationAvailable() {
 		LocationManager manager = _location.getLocationManager();
 
 		// check if GPS is enabled
 		if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
 			if (_isUIAlive) {
 				new AlertDialog.Builder(_ui)
 					.setMessage("Your location seems to be disabled, do you want to enable it?")
 				 	.setCancelable(false)
 				 	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 				 		public void onClick(final DialogInterface dialog, final int id) {
 				 	        ComponentName toLaunch = new ComponentName("com.android.settings","com.android.settings.SecuritySettings");
 				 	        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				 	        intent.addCategory(Intent.CATEGORY_LAUNCHER);
 				 	        intent.setComponent(toLaunch);
 				 	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				 	        _ui.startActivityForResult(intent, 0);
 				 		}
 				 })
 				 .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				 	public void onClick(final DialogInterface dialog, final int id) {
 				 		dialog.cancel();
 				 	}
 				 })
 				 .create().show();
 		    }
 		}
 		
 		_isLocationAvailable  = _location.isLocationEnabled();
 	}
 	
 	public void initIsDataAvailable() {
 		ConnectivityManager connectivityManager = (ConnectivityManager) _service.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 		_isDataAvailable = activeNetworkInfo != null;
 	}
 */
