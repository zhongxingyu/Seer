 package mobisocial.omnistanford.service;
 
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import mobisocial.omnistanford.App;
 import mobisocial.omnistanford.SettingsActivity;
 import mobisocial.omnistanford.db.CheckinManager;
 import mobisocial.omnistanford.db.DiscoveredPersonManager;
 import mobisocial.omnistanford.db.DiscoveryManager;
 import mobisocial.omnistanford.db.MAccount;
 import mobisocial.omnistanford.db.MDiscoveredPerson;
 import mobisocial.omnistanford.db.MDiscovery;
 import mobisocial.omnistanford.db.MLocation;
 import mobisocial.omnistanford.db.MCheckinData;
 import mobisocial.omnistanford.db.MUserProperty;
 import mobisocial.omnistanford.db.PropertiesManager;
 import mobisocial.omnistanford.receiver.PassiveLocationChangedReceiver;
 import mobisocial.omnistanford.util.Request;
 import mobisocial.omnistanford.util.ResponseHandler;
 import mobisocial.omnistanford.util.Util;
 import mobisocial.socialkit.musubi.DbObj;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Binder;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 
 public class LocationService extends Service {
 	public static final String TAG = "LocationService";
 	
 
 	public static final long MINUTE = 1000L * 60L;
 	public static final long DAY = MINUTE * 60L * 24L; 
 	public static final long MONTH = DAY * 30L;
 	
 	// The minimum distance the user should travel between location updates. 
 	public static int MIN_DISTANCE = 100;
 	// The minimum time that should pass before the user gets a location update.
 	public static long MIN_TIME_SHORT = MINUTE * 5L;
 	public static long MIN_TIME_LONG = MINUTE * 15L;
 	
     private Location mCurrent = null;
 	private Integer mCheckoutCount = 0;
 	private long mLastRequest = 0;
 	private static final int MAX_OUTSIDE_COUNT = 0;
 	
 	private LocationManager mLocationManager;
 	private mobisocial.omnistanford.db.LocationManager mLm;
 	
 	private final IBinder mBinder = new LocationBinder();
 	
 	public class LocationBinder extends Binder {
 		LocationBinder getService() {
 			return LocationBinder.this;
 		}
 	}
     
 	@Override
 	public void onCreate() {
         Log.d(TAG, "onCreate called");
         mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         mLm = new mobisocial.omnistanford.db.LocationManager(App.getDatabaseSource(this));
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.i(TAG, "received start id " + startId + ": " + intent);
		if(intent.hasExtra("location")) {
 			Location loc = intent.getParcelableExtra("location");
 			notifyLocationUpdate(loc);
 		} else {
 	        Location loc = requestInitialLocation();
 	        if(loc != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
 	            notifyLocationUpdate(loc);
 	            requestPeriodicalLocation(loc);
 	        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO) {
 	            if (loc != null) {
 	                notifyLocationUpdate(loc);
 	            }
 	            requestPeriodicalLocationLegacy(loc);
 	        }
 		}
 		return START_STICKY;
 	}
 	
 	@Override
 	public void onDestroy() {
 		Log.i(TAG, "service stopped");
 	}
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 	
 	private Location requestInitialLocation() {
 		float bestAccuracy = Float.MAX_VALUE;
 		Location bestResult = null;
 		long bestTime = 0L;
 		long maxTime = System.currentTimeMillis() - DAY;
 		long minTime = System.currentTimeMillis() - MIN_TIME_SHORT;
 		float maxDistance = MIN_DISTANCE;
 		
 		List<String> matchingProviders = mLocationManager.getAllProviders();
 		for (String provider: matchingProviders) {
 			Location location = mLocationManager.getLastKnownLocation(provider);
 			if (location != null) {
 				float accuracy = location.getAccuracy();
 				long time = location.getTime();
 
 				// find location that is most recently updated and most accurate
 				if ((time > minTime && accuracy < bestAccuracy)) {
 					bestResult = location;
 					bestAccuracy = accuracy;
 					bestTime = time;
 				} else if (time < minTime && bestAccuracy == Float.MAX_VALUE && time > bestTime){
 					// use the most recent one if we cannot find one accurate enough
 					bestResult = location;
 					bestTime = time;
 				}
 			}
 		}
 		
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
     		if (bestTime < maxTime || bestAccuracy > maxDistance) { 
     			// return most recent first, and try to find a more accurate one
     			Criteria criteria = new Criteria();
     			criteria.setAccuracy(Criteria.ACCURACY_COARSE);
     			criteria.setPowerRequirement(Criteria.POWER_LOW);
     			mLocationManager.requestSingleUpdate(criteria, mSingleLocationUpdateListener, getMainLooper());
     		}
 		}
 		Log.i(TAG, "first best result:" + bestResult.toString());
 		return bestResult;
 	}
 	
 	private void requestPeriodicalLocation(Location location) {
 		if(isOnCampus(location)) {
 			Log.i(TAG, "at Stanford");
 			Criteria criteria = new Criteria();
 			criteria.setAccuracy(Criteria.ACCURACY_FINE);
 			mLocationManager.requestLocationUpdates(MIN_TIME_SHORT, MIN_DISTANCE, criteria, mLocationListener, getMainLooper());
 			
 			Intent activeIntent = new Intent(this, PassiveLocationChangedReceiver.class);
 			PendingIntent locationListenerPendingIntent = 
 			  PendingIntent.getBroadcast(this, 0, activeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 			mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MIN_TIME_SHORT, MIN_DISTANCE, locationListenerPendingIntent);
 		} else {
 			Criteria criteria = new Criteria();
 			criteria.setAccuracy(Criteria.ACCURACY_COARSE);
 			criteria.setPowerRequirement(Criteria.POWER_LOW);
 			mLocationManager.requestLocationUpdates(MIN_TIME_LONG, MIN_DISTANCE, criteria, mLocationListener, getMainLooper());
 			mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MIN_TIME_LONG, MIN_DISTANCE * 5, mLocationListener);
 		}
 	}
 	
 	private void requestPeriodicalLocationLegacy(Location location) {
 	    if (location != null && isOnCampus(location)) {
 	        Log.i(TAG, "at Stanford");
 	        mLocationManager.removeUpdates(mLocationListener);
             mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                     MIN_TIME_SHORT, MIN_DISTANCE, mLocationListener, getMainLooper());
             mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                     MIN_TIME_SHORT, MIN_DISTANCE, mLocationListener, getMainLooper());
 
             Intent activeIntent = new Intent(this, PassiveLocationChangedReceiver.class);
             PendingIntent locationListenerPendingIntent = 
                     PendingIntent.getBroadcast(this, 0, activeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
             mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
                     MIN_TIME_SHORT, MIN_DISTANCE, locationListenerPendingIntent);
 	    } else {
             mLocationManager.removeUpdates(mLocationListener);
             mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                     MIN_TIME_SHORT, MIN_DISTANCE, mLocationListener, getMainLooper());
             mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
                     MIN_TIME_LONG, MIN_DISTANCE, mLocationListener);
 	    }
 	}
 	
 	private boolean isOnCampus(Location loc) {
 		double latitude = loc.getLatitude();
 		double longitude = loc.getLongitude();
 		
 		return latitude < 37.446 && latitude > 37.415 && longitude < -122.148 && longitude > -122.1926;
 	}
 	
 	private LocationListener mSingleLocationUpdateListener = new LocationListener() {
 
 		@Override
 		public void onLocationChanged(Location location) {
 			notifyLocationUpdate(location);
 			requestPeriodicalLocation(location);
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	};
 	
 	private LocationListener mLocationListener = new LocationListener() {
 		@Override
 		public void onLocationChanged(Location location) {
 			Log.i(TAG, "received location + " + location.toString());
 			notifyLocationUpdate(location);
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 			Log.i(TAG, "provider disabled: " + provider);
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 			Log.i(TAG, "provider enabled");
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			Log.i(TAG, "status changed");
 		}
 	};
 	
 	private void notifyLocationUpdate(Location location) {
 		if(isBetterLocation(location)) {
 			Log.i(TAG, "location is better");
 			mCurrent = location;
 			CheckinManager cm = new CheckinManager(App.getDatabaseSource(LocationService.this));
 			MLocation match = mLm.getLocation(mCurrent.getLatitude(), mCurrent.getLongitude());
 			
 			if (match != null && match.feedUri == null) {
 				Log.d(TAG, "match found, no uri set");
 			} else if (match != null && match.feedUri != null) {
 				Log.d(TAG, "Found " + match.name);
 				MCheckinData data = null;
 				List<MCheckinData> possible = cm.getRecentOpenCheckins(MONTH);
 				if (possible.size() > 0) {
 				    data = possible.get(0);
 				}
 				// Only update if no recent checkins, or already checked out
 				if (data == null) {
 					data = new MCheckinData();
 					data.entryTime = System.currentTimeMillis();
 					data.locationId = match.id;
 					MAccount acct = Util.loadAccount(LocationService.this);
 					if (acct != null) {
 						data.accountId = acct.id;
 
 						// Check in locally
 						cm.insertCheckin(data);
 						Log.d(TAG, "inserted with id " + data.id + " at " + data.entryTime);
 
 						// Check in remotely
 						PropertiesManager pm = new PropertiesManager(App.getDatabaseSource(LocationService.this));
 						Request request = new Request(match.principal, "checkin", mResponseHandler);
 						request.addParam("id", new Long(data.id).toString());
 						MUserProperty dorm = pm.getProperty(SettingsActivity.RESIDENCE);
 						if (dorm != null) {
 							request.addParam(SettingsActivity.RESIDENCE, dorm.value);
 						}
 						MUserProperty department = pm.getProperty(SettingsActivity.DEPARTMENT);
 						if (department != null) {
 							request.addParam(SettingsActivity.DEPARTMENT, department.value);
 						}
 						MUserProperty enabled = pm.getProperty(SettingsActivity.ENABLED);
 						if (enabled != null) {
 							boolean shouldSend = "true".equals(enabled.value) ? true : false;
 							if (shouldSend) {
 								long now = System.currentTimeMillis();
 								if (now - MINUTE > mLastRequest) {
 									mLastRequest = now;
 									request.send(LocationService.this);
 								}
 							}
 						}
 					}
 				} else {
 					// Only check in remotely
 					PropertiesManager pm = new PropertiesManager(App.getDatabaseSource(LocationService.this));
 					Request request = new Request(match.principal, "checkin", mResponseHandler);
 					request.addParam("id", new Long(data.id).toString());
 					MUserProperty dorm = pm.getProperty(SettingsActivity.RESIDENCE);
 					if (dorm != null) {
 						request.addParam(SettingsActivity.RESIDENCE, dorm.value);
 					}
 					MUserProperty department = pm.getProperty(SettingsActivity.DEPARTMENT);
 					if (department != null) {
 						request.addParam(SettingsActivity.DEPARTMENT, department.value);
 					}
 					MUserProperty enabled = pm.getProperty(SettingsActivity.ENABLED);
 					if (enabled != null) {
 						boolean shouldSend = "true".equals(enabled.value) ? true : false;
 						if (shouldSend) {
 							long now = System.currentTimeMillis();
 							if (now - MINUTE > mLastRequest) {
 								mLastRequest = now;
 								request.send(LocationService.this);
 							}
 						}
 					}
 				}
 			}
 			
 			boolean isAtDifferentLocation = false;
 			if(match != null) {
 				List<MCheckinData> recentCheckins = cm.getRecentOpenCheckins(MONTH);
 				for(MCheckinData checkin : recentCheckins) {
 					if(checkin.locationId != match.id) {
 						isAtDifferentLocation = true;
 						break;
 					}
 				}
 			}
 			if(match == null || match.feedUri == null || isAtDifferentLocation){
     			// Exit open checkins (if we get enough updates outside a valid location)
     			Log.d(TAG, "exiting open");
     			synchronized(mCheckoutCount) {
     				mCheckoutCount++;
     				if (mCheckoutCount > MAX_OUTSIDE_COUNT) {
     					mCheckoutCount = 0;
     					List<MCheckinData> checkins = cm.getRecentOpenCheckins(MONTH);
     					for (MCheckinData data : checkins) {
     						if (data.exitTime == null || data.exitTime == 0) {
     							MLocation loc = mLm.getLocation(data.locationId);
     							if(isAtDifferentLocation && loc.id == match.id) {
     								continue;
     							}
                                 data.exitTime = System.currentTimeMillis();
                                 Log.d(TAG, "exiting id " + data.id + " " + loc.name);
     							cm.updateCheckin(data);
     							Request request = new Request(loc.principal, "checkout", null);
     							request.send(LocationService.this);
     						}
 						}
 					}
 				}
 			}	
 		}
 	}
 	
 	/** Determines whether one Location reading is better than the current Location fix
      * @param location  The new Location that you want to evaluate
      * @param currentBestLocation  The current Location fix, to which you want to compare the new one
      * Thanks, Android documentation.
      */
    protected boolean isBetterLocation(Location location) {
        if (mCurrent == null) {
            // A new location is always better than no location
            return true;
        }
 
        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - mCurrent.getTime();
        boolean isSignificantlyOlder = timeDelta < -MIN_TIME_SHORT;
        boolean isNewer = timeDelta > 0;
 
        // If the new location is more than two minutes older, it must be worse
        if (isSignificantlyOlder) {
            return false;
        }
 
        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - mCurrent.getAccuracy());
        Log.d(TAG, location.getProvider() + " accuracy: " + location.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > MIN_DISTANCE;
 
        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
     		   mCurrent.getProvider());
 
        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }
 
    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }
 	
 	// For a given checkin, get some discovery information set up
 	private ResponseHandler mResponseHandler = new ResponseHandler() {
 	    @Override
         public void OnResponse(DbObj obj) {
             Log.d(TAG, "got a response");
             JSONObject json = obj.getJson();
             if (!json.optString("id").equals("")) {
                 long checkinId = Long.parseLong(json.optString("id"));
                 JSONArray arr = json.optJSONArray("res");
                 if (arr != null) {
                     DiscoveredPersonManager dpm = new DiscoveredPersonManager(
                             App.getDatabaseSource(LocationService.this));
                     DiscoveryManager dm = new DiscoveryManager(
                             App.getDatabaseSource(LocationService.this));
                     PropertiesManager pm = new PropertiesManager(
                             App.getDatabaseSource(LocationService.this));
                     MUserProperty myDorm = pm.getProperty(SettingsActivity.RESIDENCE);
                     MUserProperty myDept = pm.getProperty(SettingsActivity.DEPARTMENT);
                     for (int i = 0; i < arr.length(); i++) {
                         JSONObject match = arr.optJSONObject(i);
                         if (match != null) {
                             MDiscoveredPerson person = new MDiscoveredPerson();
                             person.name = match.optString("name");
                             person.identifier = match.optString("principal");
                             person.accountType = match.optString("type");
                             dpm.ensurePerson(person);
                             MDiscovery discovery = new MDiscovery();
                             discovery.checkinId = checkinId;
                             discovery.personId = person.id;
                             if (myDorm != null && myDorm.value.equals(match.optString("dorm"))) {
                                 discovery.connectionType = SettingsActivity.RESIDENCE;
                                 dm.ensureDiscovery(discovery);
                             }
                             if (myDept != null && myDept.value.equals(match.optString("department"))) {
                                 discovery.connectionType = SettingsActivity.DEPARTMENT;
                                 dm.ensureDiscovery(discovery);
                             }
                         }
                     }
                 }
             }
         }
 	};
 }
