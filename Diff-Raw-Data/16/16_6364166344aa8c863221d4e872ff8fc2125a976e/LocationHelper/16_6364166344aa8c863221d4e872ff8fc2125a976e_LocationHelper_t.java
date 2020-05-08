 package at.madexperts.logmynight;
 
 import static java.lang.Math.abs;
 import static java.lang.Math.pow;
 import static java.lang.Math.sqrt;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 
 public class LocationHelper {
 
 	private static final String TAG = LocationHelper.class.getName();
 	
 	private SQLiteDatabase db;
 	private Context ctx;
 	public LocationHelper(Context ctx, SQLiteDatabase db) {
 		this.ctx = ctx;
 		this.db = db;
 		
 		
 	}
 	
     private SortedMap<Double, List<String>> getSortedLocations(Location location) {
     	double latNow = 0.0;
     	double lonNow = 0.0;
     	if (location != null) {
     		latNow = location.getLatitude() + 90.0;
     		lonNow = location.getLongitude() + 180.0;
     	}
 	
     	Cursor c = db.rawQuery("SELECT _id, name, longitude, latitude FROM location", null);
     	TreeMap<Double, List<String>> map = new TreeMap<Double, List<String>>();
     	while (c.moveToNext()) {
 			double latRow = c.getDouble(c.getColumnIndex("longitude")) + 90.0;
 			double lonRow = c.getDouble(c.getColumnIndex("latitude")) + 180.0;
 			String name = c.getString(c.getColumnIndex("name"));
 			double distance = euklidDistance(latNow, lonNow, latRow, lonRow);
 			List<String> list = map.get(distance);
 			if (list == null) {
 				list = new ArrayList<String>();
 				map.put(distance, list);
 			}
 			list.add(name);
 			Log.d(TAG, name + " has distance of " + distance);
 		}
     	return map;
     }
     
     public String getNearestLocation() {
     	Location now = getLocation();
     	return getNearestLocation(now);
     }
     /**
      * Returns name of nearest location
      * @param now location for calculation distance to 
      * @return
      */
     public String getNearestLocation(Location now) {
     	SortedMap<Double, List<String>> map = getSortedLocations(now);
    	if (map.isEmpty()) {
    		return null;
    	}
     	List<String> list = map.get(map.firstKey());
     	if (list == null || list.isEmpty()) {
     		return null;
     	}
     	String name = list.get(0);
     	return name;
     	/*
     	Cursor c = db.rawQuery("SELECT _id, name, longitude, latitude FROM location WHERE name = ?", new String[] {name});
     	if (!c.moveToFirst()) {
     		return null;
     	}
     	double latitude = c.getDouble(c.getColumnIndex("latitude"));
     	double longitude = c.getDouble(c.getColumnIndex("longitude"));
     	Location result = new Location("LOGMYNIGHT");
     	result.setTime(new Date().getTime());
     	result.setLatitude(latitude);
     	result.setLongitude(longitude);
     	return result;
     	*/
     }
     
     public double euklidDistance(double x1, double y1, double x2, double y2) {
     	return sqrt( pow(abs(x1 - x2),2) * pow(abs(y1 - y2),2) );
     }
 
     
     public String getBestProvider(LocationManager lm) {
     	List<String> provs = lm.getAllProviders();
     	for (String prov : provs) {
     		Log.d(TAG, "Provider: " + prov);
     	}
     	Criteria c = new Criteria();
     	c.setAccuracy(Criteria.ACCURACY_COARSE);
     	c.setAltitudeRequired(false);
     	c.setBearingRequired(false);
     	c.setCostAllowed(false);
     	c.setPowerRequirement(Criteria.NO_REQUIREMENT);
     	c.setSpeedRequired(false);
     	String provider = lm.getBestProvider(c, true);
     	return provider;
     }
 	
 
     /**
      * Returns last know location with best provider 
      * @return
      */
 	public Location getLocation() {
 		LocationManager lm = (LocationManager) ctx.getSystemService(ctx.LOCATION_SERVICE);
 		String provider = getBestProvider(lm);
 		Log.d(TAG, "Used provider: " + provider);
 		return lm.getLastKnownLocation(provider);
     }
     
 
 	/**
 	 * Returns adapter with sorted list of location names, sorted by closed distance 
 	 * @return
 	 */
 	public ArrayAdapter<String> getSortedNameAdapter() {
 		Location location = getLocation();
 		return getSortedNameAdapter(location);
 	}
 	
     public ArrayAdapter<String> getSortedNameAdapter(Location location) {
     	SortedMap<Double, List<String>> map = getSortedLocations(location);
 		ArrayList<String> values = new ArrayList<String>();
 		for (List<String> list: map.values()) {
 			for(String value:list) {
 				values.add(value);
 				Log.d(TAG, "Sorted:" + value);
 			}
 		}
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_dropdown_item_1line, values);
     	return adapter;
     	
     }
 	
 }
