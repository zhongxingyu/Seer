 package mobisocial.omnistanford.service;
 
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import mobisocial.omnistanford.App;
 import mobisocial.omnistanford.SettingsActivity;
 import mobisocial.omnistanford.db.CheckinManager;
 import mobisocial.omnistanford.db.DatabaseHelper;
 import mobisocial.omnistanford.db.DiscoveredPersonManager;
 import mobisocial.omnistanford.db.DiscoveryManager;
 import mobisocial.omnistanford.db.MAccount;
 import mobisocial.omnistanford.db.MDiscoveredPerson;
 import mobisocial.omnistanford.db.MDiscovery;
 import mobisocial.omnistanford.db.MLocation;
 import mobisocial.omnistanford.db.MCheckinData;
 import mobisocial.omnistanford.db.MUserProperty;
 import mobisocial.omnistanford.db.PropertiesManager;
 import mobisocial.omnistanford.util.LocationUpdater;
 import mobisocial.omnistanford.util.Request;
 import mobisocial.omnistanford.util.ResponseHandler;
 import mobisocial.omnistanford.util.Util;
 import mobisocial.socialkit.musubi.DbObj;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.SystemClock;
 import android.util.Log;
 
 public class LocationService extends Service {
 	public static final String TAG = "LocationService";
 	
 	private static final long INTERVAL = 1000 * 60 * 15;
 	private static final long SHORT_INTERVAL = 1000 * 60 * 6;
 	private static final long MINUTE = 1000 * 60;
 	private static final long MONTH = 1000L * 60L * 60L * 24L * 30L;
 	
 	private Integer mUpdateCount = 0;
 	private Integer mCheckoutCount = 0;
 	private long mLastRequest = 0;
 	private static final int MAX_OUTSIDE_COUNT = 9;
 	private static final int MAX_UPDATE_COUNT = 4;
 	
 	private LocationManager mLocationManager;
 	private mobisocial.omnistanford.db.LocationManager mLm =
 	        new mobisocial.omnistanford.db.LocationManager(new DatabaseHelper(this));
 	
 	private final IBinder mBinder = new LocationBinder();
 	
 	private Handler mFastHandler;
 	private Handler mSlowHandler;
 	
 	public class LocationBinder extends Binder {
 		LocationBinder getService() {
 			return LocationBinder.this;
 		}
 	}
     
     private void setInnerLocationState() {
         Location last = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         if (last != null) {
             double latitude = last.getLatitude();
             double longitude = last.getLongitude();
             
             Log.d(TAG, "Location: (" + latitude + ", " + longitude + ")");
             
             // Use fine locations if at Stanford
             if (latitude < 37.446 && latitude > 37.415 && longitude < -122.148 && longitude > -122.1926) {
                 Log.d(TAG, "At Stanford");
                 mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                 mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
             }
         }
     }
     
     private void setOuterLocationState() {
         Location last = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         if (last != null) {
             double latitude = last.getLatitude();
             double longitude = last.getLongitude();
             
             Log.d(TAG, "Location: (" + latitude + ", " + longitude + ")");
             
             // Use fine locations if at Stanford
             if (latitude < 37.446 && latitude > 37.415 && longitude < -122.148 && longitude > -122.1926) {
                 Log.d(TAG, "At Stanford");
                 mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                 mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
             } else {
                 Log.d(TAG, "Not at Stanford");
                 mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
             }
         }
     }
 	
 	@Override
 	public void onCreate() {
         Log.d(TAG, "onCreate called");
 	    
 	    mFastHandler = new Handler();
 	    mSlowHandler = new Handler();
 
         mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
         mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
 	    
         mLm = new mobisocial.omnistanford.db.LocationManager(new DatabaseHelper(this));
 	    AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
         long currentElapsedTime = SystemClock.elapsedRealtime();
         
         mFastUpdater.run();
         mSlowUpdater.run();
 	    
 	    Intent locationFetchIntent = new Intent(this, LocationService.class);
 	    locationFetchIntent.putExtra("locationFetch", true);
 	    PendingIntent fetchSender = PendingIntent.getService(this, 0, locationFetchIntent, 0);
 	    am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, currentElapsedTime,
 	            AlarmManager.INTERVAL_DAY, fetchSender);
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.i(TAG, "received start id " + startId + ": " + intent);
 		// Need to update list of known locations periodically
 		if (intent != null) {
 		    if (intent.getExtras() != null) {
 		        Log.d(TAG, "Extras: " + intent.getExtras().toString());
 		    }
     		if (intent.hasExtra("locationFetch")) {
     		    new LocationUpdater(mLm).update();
     		} else if (intent.hasExtra("locationUpdate")) {
     		    Log.d(TAG, "locationUpdate");
                 setInnerLocationState();
             } else if (intent.hasExtra("periodicLocationUpdate")) {
                 Log.d(TAG, "periodicLocationUpdate");
                 setOuterLocationState();
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
 	
 	private Runnable mFastUpdater = new Runnable() {
 	    @Override
 	    public void run() {
 	        Intent intent = new Intent(LocationService.this, LocationService.class);
 	        intent.putExtra("locationUpdate", true);
 	        startService(intent);
 	        mFastHandler.postDelayed(this, SHORT_INTERVAL);
 	    }
 	};
 	
 	private Runnable mSlowUpdater = new Runnable() {
 	    @Override
 	    public void run() {
             Intent intent = new Intent(LocationService.this, LocationService.class);
             intent.putExtra("periodicLocationUpdate", true);
             startService(intent);
 	        mSlowHandler.postDelayed(this, INTERVAL);
 	    }
 	};
 	
 	private LocationListener mLocationListener = new LocationListener() {
 	    private static final int TWO_MINUTES = 1000 * 60 * 2;
 	    Location mCurrent = null;
 
 	    /** Determines whether one Location reading is better than the current Location fix
 	      * @param location  The new Location that you want to evaluate
 	      * @param currentBestLocation  The current Location fix, to which you want to compare the new one
 	      * Thanks, Android documentation.
 	      */
 	    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
 	        if (currentBestLocation == null) {
 	            // A new location is always better than no location
 	            return true;
 	        }
 
 	        // Check whether the new location fix is newer or older
 	        long timeDelta = location.getTime() - currentBestLocation.getTime();
 	        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
 	        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
 	        boolean isNewer = timeDelta > 0;
 
 	        // If it's been more than two minutes since the current location, use the new location
 	        // because the user has likely moved
 	        if (isSignificantlyNewer) {
 	            return true;
 	        // If the new location is more than two minutes older, it must be worse
 	        } else if (isSignificantlyOlder) {
 	            return false;
 	        }
 
 	        // Check whether the new location fix is more or less accurate
 	        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
 	        boolean isLessAccurate = accuracyDelta > 0;
 	        boolean isMoreAccurate = accuracyDelta < 0;
 	        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
 	        // Check if the old and new location are from the same provider
 	        boolean isFromSameProvider = isSameProvider(location.getProvider(),
 	                currentBestLocation.getProvider());
 
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
 	    
 		@Override
 		public void onLocationChanged(Location location) {
 			Log.i(TAG, "received location + " + location.toString());
 			if (isBetterLocation(location, mCurrent)) {
 			    Log.i(TAG, "location is better");
 			    mCurrent = location;
                 CheckinManager cm = new CheckinManager(App.getDatabaseSource(LocationService.this));
 			    MLocation match = mLm.getLocation(mCurrent.getLatitude(), mCurrent.getLongitude());
 			    if (match != null && match.feedUri == null) {
 			        Log.d(TAG, "match found, no uri set");
 			    } else if (match != null && match.feedUri != null) {
 			        Log.d(TAG, "Found " + match.name);
			        MCheckinData data = cm.getRecentCheckin(match.id);
			        if (data == null) {
			            List<MCheckinData> possible = cm.getRecentOpenCheckins(MONTH);
			            if (possible.size() > 0) {
			                data = possible.get(0);
			            }
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
 			    } else {
 			        // Exit open checkins (if we get enough updates outside a valid location)
 			        Log.d(TAG, "exiting open");
 			        synchronized(mCheckoutCount) {
 			            mCheckoutCount++;
 			            if (mCheckoutCount > MAX_OUTSIDE_COUNT) {
 			                mCheckoutCount = 0;
         			        List<MCheckinData> checkins = cm.getRecentOpenCheckins(MONTH);
         			        for (MCheckinData data : checkins) {
         			            if (data.exitTime == null || data.exitTime == 0) {
         			                data.exitTime = System.currentTimeMillis();
         			                cm.updateCheckin(data);
         			                MLocation loc = mLm.getLocation(data.locationId);
         			                Request request = new Request(loc.principal, "checkout", null);
         			                request.send(LocationService.this);
         			            }
         			        }
 			            }
 			        }
 			    }
 			}
 			// Turn off location updates periodically
 			synchronized(mUpdateCount) {
     			mUpdateCount++;
     			if (mUpdateCount > MAX_UPDATE_COUNT) {
     			    mUpdateCount = 0;
     			    mLocationManager.removeUpdates(this);
     			}
 			}
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 			Log.i(TAG, "provider disabled: " + provider);
 			if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
 //			    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
 			} else if (provider.equals(LocationManager.GPS_PROVIDER)) {
                 mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
             }
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 			Log.i(TAG, "provider enabled");
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			Log.i(TAG, "status changed");
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
 	};
 }
