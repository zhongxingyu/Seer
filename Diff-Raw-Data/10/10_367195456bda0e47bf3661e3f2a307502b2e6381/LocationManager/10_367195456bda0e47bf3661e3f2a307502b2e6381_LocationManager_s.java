 package uk.co.mentalspace.android.bustimes;
 
 import android.content.Context;
 import java.util.List;
 import android.database.sqlite.SQLiteDatabaseLockedException;
 import android.util.Log;
 import android.widget.Toast;
 import uk.co.mentalspace.android.bustimes.db.LocationsDBAdapter;
 
 public class LocationManager {
 	private static final String LOGNAME = "LocationManager";
 	
 	public static boolean isLocationSelected(Context ctx, int stopId) {
 		Log.d(LOGNAME, "Testing location selection status. loc id: "+stopId);
         LocationsDBAdapter ldba = new LocationsDBAdapter(ctx);
         try {
 	        ldba.openReadable();
 	        Location loc = ldba.getLocationByID(stopId);
 	        if (null == loc) return false;
 	        return (loc.getChosen() == 1);
         } catch (SQLiteDatabaseLockedException sdle) {
         	Log.e(LOGNAME, "Database Locked");
         	Toast.makeText(ctx, "Failed to load selected locations", Toast.LENGTH_SHORT).show();
         	return false;
         } finally {
         	if (null != ldba) try {ldba.close(); } catch (Exception e) {Log.e(LOGNAME, "Unknown exception", e); }
         }
 	}
 	
 	public static void selectLocation(Context ctx, long stopId) {
 		Log.d(LOGNAME, "Selecting location id: "+stopId);
         LocationsDBAdapter ldba = new LocationsDBAdapter(ctx);
         try {
 	        ldba.open();
 	        Location loc = ldba.getLocationByID(stopId);
 	        if (null == loc) return;
 	        loc.setChosen(1);
 	        ldba.updateLocation(loc);
         } catch (SQLiteDatabaseLockedException sdle) {
         	Log.e(LOGNAME, "Database Locked", sdle);
         	Toast.makeText(ctx, "Failed to select location", Toast.LENGTH_SHORT).show();
         } finally {
         	if (null != ldba) try {ldba.close(); } catch (Exception e) {Log.e(LOGNAME, "Unknown exception", e); }
         }
 	}
 	
 	public static void deselectLocation(Context ctx, long stopId) {
 		Log.d(LOGNAME, "Deselecting stop: "+stopId);
         LocationsDBAdapter ldba = new LocationsDBAdapter(ctx);
         try {
 	        ldba.open();
 	        Location loc = ldba.getLocationByID(stopId);
 	        if (null == loc) return;
 	        loc.setChosen(0);
 	        ldba.updateLocation(loc);
         } catch (SQLiteDatabaseLockedException sdle) {
         	Log.e(LOGNAME, "Database Locked", sdle);
         	Toast.makeText(ctx, "Failed to deselect location", Toast.LENGTH_SHORT).show();
         } finally {
         	if (null != ldba) try {ldba.close(); } catch (Exception e) {Log.e(LOGNAME, "Unknown exception", e); }
         }
 	}
 	
 	public static void updateNickName(Context ctx, long stopId, String nickName) {
 		Log.d(LOGNAME, "Updating Nick Name");
         LocationsDBAdapter ldba = new LocationsDBAdapter(ctx);
         try {
 	        ldba.open();
 	        Location loc = ldba.getLocationByID(stopId);
 	        if (null == loc) return;
 	        loc.setNickName(nickName);
 	        ldba.updateLocation(loc);
         } catch (SQLiteDatabaseLockedException sdle) {
         	Log.e(LOGNAME, "Database Locked", sdle);
         	Toast.makeText(ctx, "Failed to update nick name ["+nickName+"]", Toast.LENGTH_SHORT).show();
         	return;
         } finally {
         	if (null != ldba) try {ldba.close(); } catch (Exception e) {Log.e(LOGNAME, "Unknown exception", e); }
         }
 	}
 	
 	public static List<Location> getSelectedLocations(Context ctx) {
 		Log.d(LOGNAME, "Getting selected locations");
         LocationsDBAdapter ldba = new LocationsDBAdapter(ctx);
         try {
 	        ldba.openReadable();
 	        List<Location> locs = ldba.getSelectedLocations();
 	        return locs;
         } catch (SQLiteDatabaseLockedException sdle) {
         	Log.e(LOGNAME, "Failed to open Database: ", sdle);
         	Toast.makeText(ctx, "Failed to retrieve selected locations", Toast.LENGTH_SHORT).show();
        	return null;
         } finally {
         	if (null != ldba) try {ldba.close(); } catch (Exception e) {Log.e(LOGNAME, "Unknown exception", e); }
         }
 	}
 	
 	public static Location getNextLocation(Context ctx, Location loc) {
 		Log.d(LOGNAME, "Getting next location after loc: "+loc);
 		List<Location> locs = getSelectedLocations(ctx);
 		if (null != loc) {
 			if (locs.contains(loc)) {
 				int nextPosition = locs.indexOf(loc)+1;
 				if (nextPosition == locs.size()) nextPosition = 0;
 				return locs.get(nextPosition);
 			} else {
 				return locs.get(0);
 			}
 		} else {
 			return null;
 		}
 	}
 	
 	public static List<Location> getLocationsInArea(Context ctx, int top, int right, int bottom, int left) {
 		Log.d(LOGNAME, "Getting locations in area t ["+top+"], r ["+right+"], b ["+bottom+"], l ["+left+"]");
         LocationsDBAdapter ldba = new LocationsDBAdapter(ctx);
         try {
 	        ldba.openReadable();
 	        List<Location> locations = ldba.getLocationsInArea(top, right, bottom, left);
 	        return locations;
         } catch (SQLiteDatabaseLockedException sdle) {
         	Log.e(LOGNAME, "Database locked", sdle);
         	Toast.makeText(ctx, "Failed to load locations in area", Toast.LENGTH_SHORT).show();
        	return null;
         } finally {
         	if (null != ldba) try {ldba.close(); } catch (Exception e) {Log.e(LOGNAME, "Unknown exception", e); }
         }
 	}
 	
 	public static Location getNearestSelectedLocation(Context ctx, int lat, int lon) {
 		Log.d(LOGNAME, "Getting nearest selected location to lat ["+lat+"], lon ["+lon+"]");
         LocationsDBAdapter ldba = new LocationsDBAdapter(ctx);
         try {
 	        ldba.openReadable();
 	        Location loc = ldba.getClosestSelectedLocation(lat, lon);
 	        return loc;
         } catch (SQLiteDatabaseLockedException sdle) {
         	Log.e(LOGNAME, "Database Locked", sdle);
         	Toast.makeText(ctx, "Failed to locate nearest location", Toast.LENGTH_SHORT).show();
         	return null;
         } finally {
         	if (null != ldba) try {ldba.close(); } catch (Exception e) {Log.e(LOGNAME, "Unknown exception", e); }
         }
 	}
 	
 	public static Location getLocationById(Context ctx, long stopId) {
 		Log.d(LOGNAME, "Getting location by Id: "+stopId);
         LocationsDBAdapter ldba = new LocationsDBAdapter(ctx);
         try {
 	        ldba.openReadable();
 	        Location loc = ldba.getLocationByID(stopId);
 	        return loc;
         } catch (SQLiteDatabaseLockedException sdle) {
         	Log.e(LOGNAME, "Database Locked", sdle);
         	Toast.makeText(ctx, "Failed to load specified location", Toast.LENGTH_SHORT).show();
         	return null;
         } finally {
         	if (null != ldba) try {ldba.close(); } catch (Exception e) {Log.e(LOGNAME, "Unknown exception", e); }
         }
 	}
 	
 	public static Location getLocationByStopCode(Context ctx, String stopCode) {
 		Log.d(LOGNAME, "Getting location by stop code: "+stopCode);
         LocationsDBAdapter ldba = new LocationsDBAdapter(ctx);
         try {
 	        ldba.openReadable();
 	        Location loc = ldba.getLocationByStopCode(stopCode);
 	        return loc;
         } catch (SQLiteDatabaseLockedException sdle) {
         	Log.e(LOGNAME, "Database Locked", sdle);
         	Toast.makeText(ctx, "Failed to load Location ["+stopCode+"]", Toast.LENGTH_SHORT).show();
         	return null;
         } finally {
         	if (null != ldba) try {ldba.close(); } catch (Exception e) {Log.e(LOGNAME, "Unknown exception", e); }
         }
 	}
 	
 }
