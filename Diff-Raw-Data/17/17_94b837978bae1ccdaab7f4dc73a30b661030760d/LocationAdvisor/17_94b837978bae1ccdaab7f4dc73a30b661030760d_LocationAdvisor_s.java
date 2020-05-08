 package edu.ucsb.geog.blake_regalia.wap_tracer;
 
 import java.util.Date;
 import java.util.List;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.location.GpsStatus;
 import android.location.GpsStatus.Listener;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Looper;
 import android.util.Log;
 
 public class LocationAdvisor {
 	
 	private static final String TAG = "LocationAdvisor::";
 	
 	public static final int GPS = (1 << 0);
 	public static final int WIFI = (1 << 1);
 
 	// minimum 100m accuracy
 	protected static final int BOUNDARY_CHECK_MIN_ACCURACY_M = 100;
 	// good enough for boundary check: 60m accuracy
 	protected static final int BOUNDARY_CHECK_GOOD_ACCURACY_M = 60;
 	// maximum 90s old fix
 	protected static final long BOUNDARY_CHECK_MAX_AGE_MS = 90 * 1000;
 	// max number of location updates
 	protected static final int BOUNDARY_CHECK_MAX_NUM_EVENTS = 7;
 	// timeout duration: how long to wait for a better location (12s)
 	protected static final long BOUNDARY_CHECK_TIMEOUT_MS = 12 * 1000;
 
 	// minimum 60m accuracy
 	protected static final int TRACE_LOCATION_MIN_ACCURACY_M = 60;
 	// maximum 20s old fix
 	protected static final long TRACE_LOCATION_MAX_AGE_MS = 20 * 1000;
 	// good enough to use wifi location
 	protected static final int TRACE_LOCATION_GOOD_ACCURACY_M = 6;
 	// decent enough to use wifi location when gps chip is lagging
 	protected static final long TRACE_LOCATION_DECENT_ACCURACY_M = 15;
 	// only use wifi location if gps is 10s old
 	protected static final long TRACE_LOCATION_GPS_OLD_MS = 10 * 1000;
 	
 	
 	protected static final int LOCATION_TOO_INACCURATE = 250; 
 
 	public static final int TRACE_LOCATION_LISTENER = 0;
 	public static final int BOUNDARY_CHECK_LISTENER = 1;
 	
 	private boolean use_gps = false;
 	private boolean use_wifi = false;
 	
 	private Context context;
 	
 	private LocationManager location_manager;
 	
 	private Location last_location = null;
 	private boolean has_gps_fix = false;
 	
 	private LocationListener active_listener = null;
 	private int active_location_events = 0;
 
 	private Runnable callback_pending_location_fix = null;
 	private Runnable callback_incaseof_hardware_fail = null;
 	private Runnable callback_incaseof_position_lost = null;
 	private Looper service_thread_looper = null;
 	
 	private boolean isOldFix = false;
 	private boolean waitingOnFirstFix = false;
 
 	public LocationAdvisor(Context _context) {
 		context = _context;
 
 		// Acquire a reference to the system Location Manager
 		location_manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
 	}
 	
 	public boolean useProvider(int provider) {
 		use_gps = ((GPS & provider) != 0);
 		use_wifi = ((WIFI & provider) != 0);
 		
 		return false;
 	}
 	
 	
 	
 	public Location getLocation() {
 		return last_location;
 	}
 
 	public void obtainLocation(int listener, final Runnable code, final Runnable disabled, Looper looper) {
 		obtainLocation(listener, code, null, disabled, looper);
 	}
 
 	public void obtainLocation(int listener, final Runnable code, final Runnable lost, final Runnable disabled, Looper looper) {
 		// prepare the callback methods for when a location is fixed or if hardware fails
 		callback_pending_location_fix = code;
 		callback_incaseof_hardware_fail = disabled;
 		callback_incaseof_position_lost = lost;
 		service_thread_looper  = looper;
 		
 		// make sure there are no listeners requestion location updates
 		stopListener();
 
 		Log.d(TAG, "requesting location updates");
 		
 		boolean requires_only_one_location = false;
 		
 		// reset all listener variables, including last_location
 		switch(listener) {
 		case BOUNDARY_CHECK_LISTENER:
 			setActiveListener(new boundary_check_listener());
 			requires_only_one_location = true;
 
 			// start a timeout thread to return the best location after a period of inactivity
 			startTimeout(boundary_check_timeout, BOUNDARY_CHECK_TIMEOUT_MS);
 
 			break;
 			
 		case TRACE_LOCATION_LISTENER:
 			setActiveListener(new trace_location_listener());
 			
 			waitingOnFirstFix = true;
 			
 			// start a timeout thread to notify the service of position lost after period of inactivity
 			startTimeout(trace_location_timeout, TRACE_LOCATION_MAX_AGE_MS);
 			break;
 		}
 		
 		if(active_listener == null) {
 			Log.d(TAG, "active listener is null");
 		}
 		else if(requires_only_one_location) {
 			isOldFix = true;
 			
 			Log.d(TAG, "BEGIN: old fix");
 			
 			// start by passing the method the last known locations
 			List<String> matchingProviders = location_manager.getAllProviders();
 			for (String provider: matchingProviders) {
 				Location location = location_manager.getLastKnownLocation(provider);
 				if (location != null) {
 					location.setProvider(provider);
 					active_listener.onLocationChanged(location);
 					// if the method accepted one of the last known locations and that's all we need
 					if(active_listener == null) {
 						if(!requires_only_one_location) {
 							System.err.println("ERROR: LocationListener should expect more than one location");
 						}
 						return;
 					}
 				}
 			}
 
 			Log.d(TAG, "END: old fix");
 			isOldFix = false;
 		}
 		
 		// begin listening for updates
 		active_location_events = 0;
 		if(use_gps) {
 			location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, active_listener, looper);
 		}
 		if(use_wifi) {
 			location_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, active_listener, looper);
 		}
 	}
 	
 	private void setActiveListener(LocationListener listener) {
 		if(listener == null) {
 			System.err.println("ERROR: listener is null");
 		}
 		active_listener = listener;
 		active_location_events = 0;
 		last_location = null;
 		has_gps_fix = false;
 	}
 	
 	private void stopListener() {
 		Log.d(TAG, "removing updates");
 		if(active_listener != null) {
 			location_manager.removeUpdates(active_listener);
 			active_listener = null;
 		}
 	}
 
 	private int timeout = -1; 
 	
 	private void callback(Runnable callback) {
 		Thread thread = new Thread(callback);
 		//thread.setPriority(Thread.MIN_PRIORITY);
 		thread.start();
 	}
 	
	private void startTimeout(Runnable task, long timeout) {
 		stopTimeout();
		timeout = Timeout.setTimeout(task, timeout);
 	}
 
 	private void stopTimeout() {
 		if(timeout != -1) {
 			Timeout.clearTimeout(timeout);
 			timeout = -1;
 		}
 	}
 	
 	private void stop() {
 		stopTimeout();
 		stopListener();
 	}
 	
 	private void locationFixed() {
 		// if this location hasn't been fixed yet
 		if(active_listener != null) {
 			stop();
 			callback(callback_pending_location_fix);
 			callback_pending_location_fix = null;
 		}
 	}
 
 	private void hardwareFail() {
 		stop();
 		callback(callback_incaseof_hardware_fail);
 		callback_incaseof_hardware_fail = null;
 	}
 	
 	private void positionLost() {
 		Log.d(TAG, "## position lost");
 		stop();
 		Log.d(TAG, "executing callback...");
 		callback(callback_incaseof_position_lost);
 		callback_incaseof_position_lost = null;
 	}
 	
 	
 	
 	/** basic location listener class
 	 * 
 	 *  **/
 	public class basic_location_listener implements LocationListener {
 		public synchronized void onLocationChanged(Location location) {
 		}
 		
 		public synchronized void onProviderDisabled(String provider) {
 			Log.d(TAG, "provider failed: "+provider);
 			WifiManager wifi_manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 			
 			boolean fail = true;
 			
 			if(provider.equals(LocationManager.NETWORK_PROVIDER)) {
 				switch(wifi_manager.getWifiState()) {
 				case WifiManager.WIFI_STATE_DISABLED:
 					System.out.println("WIFI: disabled");
 					break;
 				case WifiManager.WIFI_STATE_ENABLED:
 					System.out.println("WIFI: enabled");
 					fail = false;
 					break;
 				case WifiManager.WIFI_STATE_DISABLING:
 					System.out.println("WIFI: disabling");
 					break;
 				case WifiManager.WIFI_STATE_ENABLING:
 					System.out.println("WIFI: enabling");
 					break;
 				default:
 					System.out.println("WIFI: unknown");
 					break;
 				}
 			}
 			if(fail) {
 				hardwareFail();
 			}
 			else {
 //				restart_listener();
 			}
 		}
 		public void onProviderEnabled(String arg0) {
 		}
 		public synchronized void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 			String str = "unknown";
 			switch(arg1) {
 			case LocationProvider.OUT_OF_SERVICE:
 				str = arg0+" out of service";
 				break;
 			case LocationProvider.AVAILABLE:
 				str = arg0+" available";
 				break;
 			case LocationProvider.TEMPORARILY_UNAVAILABLE:
 				str = arg0+" temporarily unavailable";
 				break;
 			}
 
 			Log.d(TAG, "status change event: "+str);
 			
 		}
 	}
 	
 
 	
 	public class boundary_check_listener extends basic_location_listener {
 		public synchronized void onLocationChanged(Location location) {
 
 		Log.d(TAG, "boundary check: location change event: "+active_location_events+" => "+location.getProvider());
 		
 		//Toast.makeText(context, "location change event: "+active_location_events, Toast.LENGTH_SHORT).show();
 			if(active_listener == null) {
 				Log.e(TAG, "boundary check:: location changed when should have unregistered");
 				location_manager.removeUpdates(this);
 				return;
 			}
 			
 			if(!location.hasAccuracy()) {
 				System.err.println("### location has no accuracy");
 				// no accuracy!?
 				return;
 			}
 			
 			if(location.getAccuracy() > LOCATION_TOO_INACCURATE) {
 				return;
 			}
 
 			long expiration = ((new Date()).getTime() - BOUNDARY_CHECK_MAX_AGE_MS);
 			
 			// if this location is accurate enough
 			if(location.getAccuracy() <= BOUNDARY_CHECK_MIN_ACCURACY_M
 				// and new enough
 					&& (location.getTime() > expiration)) {
 				
 		Log.d(TAG, "location is accurate & new enough to use");
 				
 				// save it if we don't have one saved
 				if(last_location == null
 					// or if the one we have is too old
 						|| (last_location.getTime() > expiration)
 					// or if this location is more accurate
 						|| (last_location.getAccuracy() <= location.getAccuracy()) ) {
 
 		Log.d(TAG, "location was saved");
 					last_location = location;
 				}
 			}
 
 			// as soon as there is a location that has good enough accurcy
 			if(last_location != null && last_location.getAccuracy() <= BOUNDARY_CHECK_GOOD_ACCURACY_M) {
 				
 		Log.d(TAG, "location is good enough for boundary check");
 		
 				locationFixed();
 				return;
 			}
 			// if location listener has been called several times..
 			else if(active_location_events >= BOUNDARY_CHECK_MAX_NUM_EVENTS) {
 				
 		Log.d(TAG, "location changed enough times, using best location");
 				
 				locationFixed();
 				return;
 			}
 			
 			// start a timeout thread to return the best location after a period of inactivity
 			startTimeout(boundary_check_timeout, BOUNDARY_CHECK_TIMEOUT_MS);
 
 			active_location_events += 1;
 		}
 	}
 
 	
 
 	public class trace_location_listener extends basic_location_listener {
 		public synchronized void onLocationChanged(Location location) {
 
 			if(active_listener == null) {
 				Log.e(TAG, "trace location:: location changed when should have unregistered");
 				location_manager.removeUpdates(this);
 				return;
 			}
 			
 			boolean usingNewLocation = false;
 			
 			Log.d(TAG, "trace location: location change event: "+active_location_events+" => "+location.getProvider());
 
 			if(!location.hasAccuracy()) {
 				System.err.println("### location has no accuracy");
 				// no accuracy!?
 				return;
 			}
 
 			double accuracy = location.getAccuracy();
 			String provider = location.getProvider();
 
 			// GPS location
 			if(provider.equals(LocationManager.GPS_PROVIDER)) {
 				
 				// report position lost when poor accuracy breaks threshold
 				if(accuracy > TRACE_LOCATION_MIN_ACCURACY_M) {
 					Log.d(TAG, "### is old fix? ==> "+isOldFix);
 					if(!isOldFix) {
 						Log.d(TAG, "### location accuracy too poor: "+location.getAccuracy());
 						positionLost();
 						return;
 					}
 				}
 				else {
 					// store this location
 					last_location = location;
 					usingNewLocation = true;
 				}
 
 			}
 			
 			// Network-based location
 			else if(provider.equals(LocationManager.NETWORK_PROVIDER)) {
 				
 				// if this location is VERY good (from wifi), then use it
 				if(accuracy <= TRACE_LOCATION_GOOD_ACCURACY_M) {
 					last_location = location;
 					usingNewLocation = true;
 				}
 
 				// if we don't have a location yet, accept anything
 				else if(last_location == null) {
 					last_location = location;
 					usingNewLocation = true;
 				}
 				
 				// if this position is accurate enough, and the GPS location is too old, then reluctantly accept it
 				else if(last_location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
 					long gps_old = ((new Date()).getTime() - TRACE_LOCATION_GPS_OLD_MS);
 					if((accuracy <= TRACE_LOCATION_DECENT_ACCURACY_M) && (last_location.getTime() > gps_old)) {
 						last_location = location;
 						usingNewLocation = true;
 					}
 				}
 			}
 			
 			if(usingNewLocation) {
 				if(waitingOnFirstFix) {
 					callback(callback_pending_location_fix);
 					waitingOnFirstFix = false;
 				}
 				// start a timeout thread to notify the service of position lost after period of inactivity
 				startTimeout(trace_location_timeout, TRACE_LOCATION_MAX_AGE_MS);
 			}
 		}
 	}
 
 	private Runnable boundary_check_timeout = new Runnable() {
 		public void run() {
 			if(timeout == -1) {
 				Log.e(TAG, "boundary check timeout:: should it have been called?");
 			}
 			if(last_location != null) {
 				Log.d(TAG, "location updating timed out, using best estimate");
 				locationFixed();
 			}
 			else {
 				Log.d(TAG, "No good locations...");
 				locationFixed();
 			}
 		}
 	};
 	
 	private Runnable trace_location_timeout = new Runnable() {
 		public void run() {
 			if(timeout == -1) {
 				Log.e(TAG, "trace location timeout:: should it have been called?");
 			}
 			positionLost();
 		}
 	};
 	
 	/** 
 	 * Stops all active listeners and timeouts and releases any resources
 	 * that may be associated with them
 	 * 
 	 */
 	public void shutDown() {
 		Log.w(TAG, "shutting down");
 		stop();
 	}
 
 	
 	
 	
 	
 	
 	
 	
 	
 
 
 
 
 
 	/**
 	 * Listens to GPS status changes
 	 */
 	private Listener mStatusListener = new GpsStatus.Listener()
 	{
 		public synchronized void onGpsStatusChanged(int event)
 		{
 			System.out.println("GPS Status changed");
 			switch (event)
 			{
 			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
 				/*
 				int mSatellites = 0;
 				Iterable<GpsSatellite> list = .getSatellites();
 				for (GpsSatellite satellite : list)
 				{
 					if (satellite.usedInFix())
 					{
 						mSatellites++;
 					}
 				}*/
 				break;
 			case GpsStatus.GPS_EVENT_STOPPED:
 				break;
 			case GpsStatus.GPS_EVENT_STARTED:
 				break;
 			default:
 				break;
 			}
 		}
 	};
 	
 }
