 package com.objects;
 
 import java.util.Date;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 public class SavedInfo{
 	/* Constantly making the same check and editor and changing info clutters code, dififcult to see logic.  */
 	/* parkState
 	 * String email
 	 * String endTime
 	 * String code
 	 * Boolean remember
 	 * */
 	//TODO:  make sure all needed info is set via reset and stuff.
 	public static final String SAVED_INFO = "ParqMeInfo";
 	public static boolean isFirstTime(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO,0);
 		return check.getBoolean("firstTimeFlag", true);
 	}
 	public static boolean ringEnable(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO,0);
 		return check.getBoolean("ringEnable", true);
 	}
 	public static boolean warningEnable(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO,0);
 		return check.getBoolean("warningEnable", true);
 	}
 	public static boolean vibrateEnable(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO,0);
 		return check.getBoolean("vibrateEnable", true);
 	}
 	public static boolean autoRefill(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO,0);
 		return check.getBoolean("autoRefill", true);
 	}
 	public static void eraseTimer(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO,0);
 		SharedPreferences.Editor editor = check.edit();
 		editor.putLong("endTime", 0);
 		editor.commit();
 	}
 	//if parked return true, else false
 	public static boolean isParked(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		return check.getLong("endTime", 0) > System.currentTimeMillis();
 	}
 	//if logged in returns true, else false
 	public static boolean isLoggedIn(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO,0);
 		return check.getLong("uid", -1) != -1;
 		//SharedPreferences.Editor editor = check.edit();
 	}
 	//set's email to a string
 	public static void setEmail(Context activity, String email){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		SharedPreferences.Editor editor = check.edit();
 		editor.putString("email", email);
 		editor.commit();
 		
 	}
 	public static void setLatLon(Context activity, double lat, double lon){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		SharedPreferences.Editor editor = check.edit();
 		editor.putString("lat", ""+lat);
 		editor.putString("lon", ""+lon);
 		editor.commit();
 		
 	}
 	public static void setGeneric(Context activity, String title, boolean input){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		SharedPreferences.Editor editor = check.edit();
 		editor.putBoolean(title, input);
 		editor.commit();
 	}
 	public static long getEndTime(SharedPreferences prefs){
 		return prefs.getLong("endTime", 0);
 	}
 	public static Date getEndTimeDate(SharedPreferences prefs) {
 		return new Date(getEndTime(prefs));
 	}
 	public static String getEmail(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		return check.getString("email", null);
 	}
 	public static String getCardStub(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		return check.getString("ccStub", "****");
 	}
 	public static String getLat(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		return check.getString("lat", "0");
 	}
 	public static String getLon(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		return check.getString("lon", "0");
 	}
 	//set's code to a string
 	public static void setCode(Context activity, String code){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		SharedPreferences.Editor editor = check.edit();
 		editor.putString("code", code);
 		editor.commit();
 		
 	}
 	public static void setParkingReferenceNumber(Context activity, String parkReferenceNumber){
 
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		SharedPreferences.Editor editor = check.edit();
 		editor.putString("PARKID", parkReferenceNumber);
 		editor.commit();
 	}
 	public static String getParkId(SharedPreferences prefs) {
 	    return prefs.getString("PARKID", "");
 	}
     public static void unpark(Context activity) {
     	SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		SharedPreferences.Editor edit = check.edit();
         edit.remove("PARKID");
         edit.remove("endTime");
         edit.remove("lat");
         edit.remove("lon");
         edit.remove("spot");
         edit.remove("minTime");
         edit.remove("maxTime");
         edit.remove("defaultRate");
         edit.remove("minIncrement");
         edit.remove("description");
         edit.commit();
     }
     public static void park(Context activity, ParkInstanceObject park, RateObject rate) {
         SharedPreferences.Editor edit = activity.getSharedPreferences(SAVED_INFO, 0).edit();
         edit.putString("PARKID", park.getParkingReferenceNumber());
         edit.putLong("endTime", park.getEndTime());
         edit.putString("lat", String.valueOf(rate.getLat()));
         edit.putString("lon", String.valueOf(rate.getLon()));
         edit.putLong("spot", rate.getSpot());
         edit.putInt("minTime", rate.getMinTime());
         edit.putInt("maxTime", rate.getMaxTime());
         edit.putInt("defaultRate", rate.getDefaultRate());
         edit.putInt("minIncrement", rate.getMinIncrement());
         edit.putString("description", rate.getDescription());
         edit.commit();
     }
     public static RateObject getRate(SharedPreferences prefs) {
         final double lat = Double.valueOf(prefs.getString("lat", "0"));
         final double lon = Double.valueOf(prefs.getString("lon", "0"));
         final long spot = prefs.getLong("spot", 0);
         final int minTime = prefs.getInt("minTime", 0);
         final int maxTime = prefs.getInt("maxTime", 0);
         final int defaultRate = prefs.getInt("defaultRate", 0);
         final int minIncrement = prefs.getInt("minIncrement", 0);
         final String description = prefs.getString("description", "");
         return new RateObject(lat, lon, spot, minTime, maxTime, defaultRate, minIncrement, description);
     }
 	public static void logIn(Context activity, boolean parkState, String email, long uid, String password, String ccStub){
 		SharedPreferences.Editor editor = activity.getSharedPreferences(SAVED_INFO, 0).edit();
 		editor.putString("email", email);
 		editor.putLong("uid", uid);
 		editor.putString("password", password);
 		editor.putString("ccStub", ccStub);
 		editor.commit();
 	}
     public static void logOut(Context activity){
         SharedPreferences.Editor editor = activity.getSharedPreferences(SAVED_INFO, 0).edit();
         editor.clear();
         editor.commit();
     }
 	
 	public static void syncParkingSession(Context activity, ParkSync sync){
 		SharedPreferences.Editor editor = activity.getSharedPreferences(SAVED_INFO, 0).edit();
 		editor.putInt("defaultRate", sync.getDefaultRate());
 		editor.putString("description",sync.getDescription());
 		editor.putLong("endTime", sync.getEndTime());
 		editor.putString("lat", ""+sync.getLat());
 		editor.putString("lon", ""+sync.getLon());
 		editor.putInt("maxTime", sync.getMaxTime());
 		editor.putInt("minIncrement", sync.getMinIncrement());
 		editor.putInt("minTime", sync.getMinTime());
 		editor.putString("PARKID", sync.getParkingReferenceNumber());
 		editor.putLong("spot", sync.getSpotId());
		editor.putString("spotNumber", sync.getSpotNumber());
 		editor.commit();
 	}
	public static String getSpotNumber(SharedPreferences pref){
		return pref.getString("spotNumber", "xxxx");
		
	}
 	public static void toggleVibrate(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		SharedPreferences.Editor editor = check.edit();
 		if(check.getBoolean("vibrateEnable", false)){
 			//if parked, set as not parked
 			editor.putBoolean("vibrateEnable", false);
 		}else{
 			//if not parked, set as parked.
 			editor.putBoolean("vibrateEnable", true);
 		}
 		editor.commit();
 	}
 	public static void toggleWarn(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		SharedPreferences.Editor editor = check.edit();
 		if(check.getBoolean("warningEnable", false)){
 			//if parked, set as not parked
 			editor.putBoolean("warningEnable", false);
 		}else{
 			//if not parked, set as parked.
 			editor.putBoolean("warningEnable", true);
 		}
 		editor.commit();
 	}
 	public static void toggleRing(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		SharedPreferences.Editor editor = check.edit();
 		if(check.getBoolean("ringEnable", false)){
 			//if parked, set as not parked
 			editor.putBoolean("ringEnable", false);
 		}else{
 			//if not parked, set as parked.
 			editor.putBoolean("ringEnable", true);
 		}
 		editor.commit();
 	}
 	public static void toggleRefill(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		SharedPreferences.Editor editor = check.edit();
 		if(check.getBoolean("autoRefill", false)){
 			//if parked, set as not parked
 			editor.putBoolean("autoRefill", false);
 		}else{
 			//if not parked, set as parked.
 			editor.putBoolean("autoRefill", true);
 		}
 		editor.commit();
 	}
 	public static void toggleRemember(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO, 0);
 		SharedPreferences.Editor editor = check.edit();
 		if(check.getBoolean("remember", false)){
 			//if parked, set as not parked
 			editor.putBoolean("remember", false);
 		}else{
 			//if not parked, set as parked.
 			editor.putBoolean("remember", true);
 		}
 		editor.commit();
 	}
 }
