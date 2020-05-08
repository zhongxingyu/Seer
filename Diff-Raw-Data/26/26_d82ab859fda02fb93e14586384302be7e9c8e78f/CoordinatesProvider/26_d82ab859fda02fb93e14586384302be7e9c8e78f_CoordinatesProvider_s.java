 package it.unibz.geospy.datamanager;
 
 import java.util.ArrayList;
 
 import it.unibz.geospy.GeoSpy;
 import it.unibz.geospy.database.DBAdapter;
 import it.unibz.geospy.sender.LocationSender;
 import it.unibz.geospy.sender.NetworkManager;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.widget.Toast;
 
 /**
  * Manages location services.
  * @author Dainius Jocas
  *
  */
 public class CoordinatesProvider {
 
 	private static Context mContext;
 	private static LocationManager mLocationManager;
 	private static LocationListener mLocationListener;
 
 	/**
 	 * DANGER: STATIC INITIALIZER!
 	 * Since most of the stuff this class is doing is static things, static
 	 * initializer can do the job. Advantages of this approach, IMHO, are that
 	 * I don't need to call a constructor. Disadvantages -- we'll see in the 
 	 * future, because now I can't think of any.
 	 */
 	static {
 		CoordinatesProvider.mContext = GeoSpy.getAppContext();
 		// Acquire a reference to the system Location Manager
 		CoordinatesProvider.mLocationManager = 
 				(LocationManager) CoordinatesProvider
 				.mContext.getSystemService(Context.LOCATION_SERVICE);
 		// Define a listener that responds to location updates
 		CoordinatesProvider.mLocationListener = new LocationListener() {
 			@Override
 			public void onLocationChanged(Location location) {
 				// Called when a new location is found by the network location
 				// provider.
 				// if network is available, send the location, if not store 
 				// the current location in the DB
 				
 				DBAdapter aDBAdapter = 
 						new DBAdapter(CoordinatesProvider.mContext);
 				long modifiedLatitude = 
 						(long) (location.getLatitude() * 1000000);
 				long modifiedLongitude = 
 						(long) (location.getLongitude() * 1000000);
 				if (NetworkManager.isNetworkAvailable(mContext)) {
 					// available network
 					// send current location
 					// sync all unsyced
 					// TODO: send a list or every location a piece.
 					LocationObject aLocationObject = 
 							new LocationObject(IMEIProvider.getIMEI(mContext),
 									String.valueOf(modifiedLatitude),
 									String.valueOf(modifiedLongitude),
 									String.valueOf(location.getTime()));
 					LocationSender.sendLocation(aLocationObject);
 					// sync unsynced location:
 					// retrieve list of location objects from DB
 					// if there are any, send them.
 					// TODO: potential place to lose some records
 					ArrayList<LocationObject> locations =
 							aDBAdapter.getListOfNotSyncedLocations();
 					for (LocationObject aLocation : locations) {
 						LocationSender.sendLocation(aLocation);
 					}
 					Toast.makeText(mContext, 
 							"Network is available", 
 							Toast.LENGTH_SHORT).show();
 				} else {
 					// no network connection. store location in the DB.
 					aDBAdapter.storeLocation(modifiedLatitude,
 							modifiedLongitude, 
 							location.getTime());
 					Toast.makeText(mContext, 
 							"No Network. Stored in DB.", 
 							Toast.LENGTH_SHORT).show();
 				}
 				
 			}
 
 			@Override
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {
				Toast.makeText(CoordinatesProvider.mContext,
						"onStatusChanged", Toast.LENGTH_SHORT).show();
 			}
 
 			@Override
 			public void onProviderEnabled(String provider) {
 				Toast.makeText(CoordinatesProvider.mContext,
						"onProviderEnabled", Toast.LENGTH_SHORT).show();
 			}
 
 			@Override
 			public void onProviderDisabled(String provider) {
 				Toast.makeText(CoordinatesProvider.mContext,
						"onProviderDisabled", Toast.LENGTH_SHORT).show();
 			}
 		};
 
 		CoordinatesProvider.enableLocationListener();
 				
 	}
 	
 	/**
 	 * Constructor initializes location provider services.
 	 * @param pContext
 	 */
 	private CoordinatesProvider(Context pContext) {
 		CoordinatesProvider.mContext = pContext;
 	}
 	/*
 	 *  Register the listener with the Location Manager to receive
 	 *  location updates. Currently using both.
 	 */
 	private static void enableLocationListener() {
 		CoordinatesProvider.mLocationManager.requestLocationUpdates(
 				LocationManager.GPS_PROVIDER,
 				60000, // minimum time interval between notifications	
 				0, // minimum change in distance between notifications
 				mLocationListener);
 		CoordinatesProvider.mLocationManager.requestLocationUpdates(
 				LocationManager.NETWORK_PROVIDER, 
 				60000,	
 				0, 
 				mLocationListener);
 	}
 	/**
 	 * Get last known GPS location. If there is a GPS location available, then
 	 * return GPS, if not - GSM and WIFI location.
 	 * TODO: should I be concerned about LocationManager.NETWORK_PROVIDER ??
 	 * @return Location object
 	 */
 	public static Location getLocation() {
 		Location aLocation = 
 				CoordinatesProvider.mLocationManager.getLastKnownLocation
 				(LocationManager.GPS_PROVIDER);
 		if (null == aLocation) {
 			aLocation = 
 					CoordinatesProvider.mLocationManager.getLastKnownLocation
 					(LocationManager.NETWORK_PROVIDER);
 		}
 		return aLocation;
 	}
 
 	/**
 	 * Turn of receiving updates about device's location.
 	 */
 	public static void stopUpdates() {
 		if (null != CoordinatesProvider.mLocationListener) {
 			mLocationManager
 			.removeUpdates(CoordinatesProvider.mLocationListener);
 		}
 	}
 	
 	/**
 	 * Start receiving location updates.
 	 */
 	public static void startUpdates() {
 //		CoordinatesProvider.mLocationManager.isProviderEnabled(provider)
 		CoordinatesProvider.enableLocationListener();
 	}
 
 }
