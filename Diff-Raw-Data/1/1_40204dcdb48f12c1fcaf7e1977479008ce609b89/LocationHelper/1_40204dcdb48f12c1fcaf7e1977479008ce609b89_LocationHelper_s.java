 package edu.aau.utzon.location;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.android.maps.GeoPoint;
 
 import edu.aau.utzon.SettingsActivity;
 import edu.aau.utzon.webservice.PointModel;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.util.Log;
 
 public class LocationHelper {
 
 	private static final String TAG = "LocationHelper";
 	private Location mCurrentLoc = null;
 	private PointModel mCurrentClosePoi = null;
 	private PointModel mPreviusClosePoi = null;
 	private List<PointModel> mPois = null;
 
 	public double distToPoi(PointModel poi)
 	{
 		//poi = poi == null ? mCurrentClosePoi : poi;
 		double userLat = mCurrentLoc.getLatitude();
 		double userLong = mCurrentLoc.getLongitude();
 		return distFrom(userLat, userLong, poi.getLat(), poi.getLong());
 	}
 
 	public boolean isNearPoi() {
 		Log.d(TAG, "isNearPoi()");
 		SharedPreferences prefs = mContext.getSharedPreferences(SettingsActivity.PREFS_PROXIMITY, Context.MODE_PRIVATE);
 		int prox = prefs.getInt("proximity", 20);
 		if(getCurrentClosePoi() != null) {
 			double dist = distToPoi(getCurrentClosePoi());
 			return dist > prox ? false : true;
 		}
 		else return false;
 	}
 	
 	private void updateUserLocation(Location l)
 	{
 		if(isBetterLocation(l, mCurrentLoc) || mCurrentLoc == null) {
 			mCurrentLoc = l;
 		}
 	}
 	
 	private void updateClosePoi() {
 		mPreviusClosePoi = mCurrentClosePoi;
 		mCurrentClosePoi = nearestPOI(mPois, mCurrentLoc);
 	}
 
 	Context mContext = null;
 	public LocationHelper(Context c){
 		mContext = c;
 		mPois = PointModel.dbGetAll(c);
 	}
 
 	public Location getCurrentLocation(){
 		return mCurrentLoc;
 	}
 
 	public PointModel getCurrentClosePoi(){
 		return mCurrentClosePoi;
 	}
 
 	public void makeUseOfNewLocation(Location location) {
 		updateUserLocation(location);
 		updateClosePoi();
 
 	}
 
 	public List<PointModel> getPois() {
 		return mPois;
 	}
 
 	static public double distFrom(double lat1, double lng1, double lat2, double lng2) {
 		double earthRadius = 3958.75;
 		double dLat = Math.toRadians(lat2-lat1);
 		double dLng = Math.toRadians(lng2-lng1);
 		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
 				Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
 				Math.sin(dLng/2) * Math.sin(dLng/2);
 		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
 		double dist = earthRadius * c;
 
 		int meterConversion = 1609;
 
 		return new Double(dist * meterConversion).doubleValue();
 	}
 
 	public List<PointModel> knearestPOI(int k)
 	{
 		List<PointModel> result = new ArrayList<PointModel>();
 
 		List<PointModel> qtemp = mPois;
 		for(int i=0;i<=k;i++)
 		{
 			PointModel nearest = nearestPOI(qtemp, mCurrentLoc);
 			if(nearest != null)
 			{
 				result.add(nearest);
 				qtemp.remove(nearest);
 			}
 		}
 		return result;
 	}
 
 	private PointModel nearestPOI(List<PointModel> pois, Location loc) {
 		PointModel result = null;
 		double closestDist = Math.pow(2, 64);
 
 		for(PointModel p : pois)
 		{
 			// In meters
 			double locDist = distFrom(p.getLat(), p.getLong(), loc.getLatitude(), loc.getLongitude());
 
 			// New closest found
 			if(locDist < closestDist ) { 
 				closestDist = locDist; 
 				result = p; 
 			}
 		}
 
 		if(result == null) Log.e(TAG, "nearestPOI is NULL !");
 
 		return result;
 	}
 
 	static public GeoPoint locToGeo(Location loc)
 	{
 		// Location -> GeoPoint conversion
 		return new GeoPoint((int)(loc.getLatitude()*1e6),(int)(loc.getLongitude()*1e6));
 	}
 
 
 
 
 
 
 
 
 
 
 
 	/** Determines whether one Location reading is better than the current Location fix
 	 * @param location  The new Location that you want to evaluate
 	 * @param currentBestLocation  The current Location fix, to which you want to compare the new one
 	 */
 	private static final int TWO_MINUTES = 1000 * 60 * 2;
 	private boolean isBetterLocation(Location location, Location currentBestLocation) {
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
 
 
 }
