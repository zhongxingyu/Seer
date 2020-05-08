 package com.dinosaurwithakatana.jobhackcareerbuilder;
 
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.provider.Settings;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.util.Log;
 
 /**
  * Service that polls the phone's current location and queries CareerBuilder jobs.
  * @author anjan
  *
  */
 public class LocationService extends Service {
 	private LocationManager mLocationManager;
 	private LocalConfiguration mConfiguration;
 	private Location mLocation;
 	
 	private final IBinder mBinder = new LocalBinder();
 	
 	private static final String TAG = LocationService.class.getSimpleName();
 	private static final int TWO_MINUTES = 1000*60*2;
 	private static final String DEVELOPER_KEY = "WDHF0HJ60FHRP1N7XQ2K";
 	private static final String CAREER_BUILDER_URL = "http://api.careerbuilder.com/";
 	private static final String CAREER_BUILDER_API = "v1/jobsearch/";
 	private boolean is_gpsEnabled = false;
 	
 	public String getLocation() {
 		return mLocation.toString();
 	}
 	
 	/**
 	 * Get relevant jobs.
 	 * @return
 	 */
 	public List<Job> getJobs() {
 		try {
 			List<Job> jobsList = queryJobs(mLocation);
 			int jobCount = jobsList.size();
 			
 			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
 			        .setSmallIcon(R.drawable.ic_launcher)
 			        .setContentTitle("My notification")
 			        .setContentText("Hello World!");
 			// Creates an explicit intent for an Activity in your app
 			Intent resultIntent = new Intent(this, MainActivity.class);
 			int mId=0;
 
 			// The stack builder object will contain an artificial back stack for the
 			// started Activity.
 			// This ensures that navigating backward from the Activity leads out of
 			// your application to the Home screen.
 			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 			// Adds the back stack for the Intent (but not the Intent itself)
 			stackBuilder.addParentStack(LoginActivity.class);
 			// Adds the Intent that starts the Activity to the top of the stack
 			stackBuilder.addNextIntent(resultIntent);
 			PendingIntent resultPendingIntent =
 			        stackBuilder.getPendingIntent(
 			            0,
 			            PendingIntent.FLAG_UPDATE_CURRENT
 			        );
 			mBuilder.setContentIntent(resultPendingIntent);
 			NotificationManager mNotificationManager =
 			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 			// mId allows you to update the notification later on.
 			mNotificationManager.notify(mId, mBuilder.build());
			return jobsList;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Queries CareerBuilder by the given location
 	 * @param location
 	 * @throws ExecutionException 
 	 * @throws InterruptedException 
 	 */
 	public List<Job> queryJobs(Location location) throws InterruptedException, ExecutionException {
 		Log.d(TAG, "queryJobs called");
 		String url = CAREER_BUILDER_URL + CAREER_BUILDER_API + "?" + 
 							"DeveloperKey=" + DEVELOPER_KEY + "&" +
 							"Location=" + location.getLatitude() + "::" + location.getLongitude() + "&" +
 							"Radius=" + mConfiguration.getSearchRadius() + "&" + 
 							"SOCCode=" + CurrentUser.sSOCCode + "&" + 
 							"SpecificEduction=true&EducationCode=" + CurrentUser.sEducation + "&" +
 							"ExcludeNational=true&OrderBy=Distance&OrderDirection=ASC";
 		String response = new QueryJobsTask().execute(url).get();
 		
 		// Debugging purposes
 		Log.d(TAG, "Response length : "+response.length());
 		
 		XMLParser parser = new XMLParser();
 		
 		try {
 			return parser.parse(response);
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 		
 		return null;
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		mConfiguration = intent.getParcelableExtra("LocalConfiguration");
 		return mBinder;
 	}
 	
 	@Override
 	public void onCreate() {
 		Log.d(TAG,"CREATED");
 		
 		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 		final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 		
 		if (!gpsEnabled) {
 			Log.d(TAG, "GPS Not Enabled");
 			is_gpsEnabled = false;
 			// TODO: Build alert dialog
 //			enableLocationSettings();
 		} else {
 			is_gpsEnabled = true;
 			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
 					10000,   // 10000 seconds
 					10,      // 10 meters
 					listener);
 		}
 	}
 
 	public boolean isIs_gpsEnabled() {
 		return is_gpsEnabled;
 	}
 
 	public void setIs_gpsEnabled(boolean is_gpsEnabled) {
 		this.is_gpsEnabled = is_gpsEnabled;
 	}
 
 	private void enableLocationSettings() {
 		
 	}	
 	
 	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
 		if (currentBestLocation == null)
 			return true;
 		
 		long timeDelta = location.getTime() - currentBestLocation.getTime();
 		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
 		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
 		boolean isNewer = timeDelta > 0;
 		
 		if (isSignificantlyNewer)
 			return true;
 		else if (isSignificantlyOlder)
 			return false;
 		
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
 	
 	private boolean isSameProvider(String provider1, String provider2) {
 		if (provider1 == null)
 			return provider2 == null;
 		return provider1.equals(provider2);
 	}
 	
 	public class LocalBinder extends Binder {
 		LocationService getService() {
 			return LocationService.this;
 		}
 	}
 	
     private final LocationListener listener = new LocationListener() {
 		@Override
 		public void onLocationChanged(Location location) {
 			if (isBetterLocation(location, mLocation)) {
 				mLocation = location;
 			}
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
 }
