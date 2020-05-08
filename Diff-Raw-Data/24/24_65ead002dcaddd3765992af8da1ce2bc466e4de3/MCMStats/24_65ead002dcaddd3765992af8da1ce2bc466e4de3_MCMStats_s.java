 package com.malcom.library.android.module.stats;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.CountDownTimer;
 import android.util.Log;
 import com.malcom.library.android.module.core.MCMCoreAdapter;
 import com.malcom.library.android.module.stats.Subbeacon.SubbeaconType;
 import com.malcom.library.android.module.stats.services.PendingBeaconsDeliveryService;
 import com.malcom.library.android.utils.LocationUtils;
 import com.malcom.library.android.utils.ToolBox;
 import com.malcom.library.android.utils.encoding.DigestUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.*;
 
 /**
  * Stats module.
  * 
  * @author Malcom Ventures, S.L.
  * @since  2012
  *
  */
 public class MCMStats {
 
 	private static MCMStats mBeacon;
 	
 	private static boolean uncaughtExceptionHandlerInitialized = false;
 	private static final UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
 	
 	
 	public static final String TAG = "MCMStats";
 	public static final String CACHED_BEACON_FILE_PREFIX = "beacon_";	
 
 	private static Context mContext;
 	
 	private static Properties properties;	
 	private static double mStartTime;
 	private static double mEndTime;
 	
 	private static Hashtable<String, Subbeacon> mSubbeaconsDictionary;
 	private static ArrayList<Subbeacon> mSubbeaconsArray;
 	
 	private static List<String> tags;
 	
 	private static boolean mUseLocation;
 	
 	private static boolean appCrashed;
 	
 	private static CountDownTimer waitTimer;	
 	
 	private MCMStats(Context context, Properties properties, boolean useLocation, List<String> tags) {
 		MCMStats.mContext = context;
 		MCMStats.properties = properties;
 		MCMStats.mSubbeaconsArray = new ArrayList<Subbeacon>();
 		MCMStats.mSubbeaconsDictionary = new Hashtable<String, Subbeacon>();
 		MCMStats.tags = tags;
 		
 		setUseLocation(useLocation);
 	}
	
 	
 	// GETTERS & SETTERS
 	
 	private void setUseLocation(boolean useLocation) {
 		MCMStats.mUseLocation = useLocation;
 	}
 	
 	
 	// BEACONS ---------------------------------------------------------------------------------
 	
 	/**
 	 * Initialize and starts the stats service.
 	 * 
 	 * @param context
 	 * @param properties
 	 * @param uselocation
 	 */
 	public synchronized static void initAndStartBeacon(final Context context, Properties properties, 
 														boolean uselocation) {
 		if(mBeacon!=null){
 			Log.i(TAG, "Beacon was already started.");
 		}else{
 			Log.i(TAG, "Starting beacon...");
 			mBeacon = new MCMStats(context, properties, uselocation, tags);
 			mBeacon.startBeacon();
 			
 		}
 		
 		
 		if(!uncaughtExceptionHandlerInitialized){
 			Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 				public void uncaughtException(Thread t, Throwable e) {
 					onCrash();					
 						//This way much better than System.exit().					
 		        		//android.os.Process.killProcess(android.os.Process.myPid());
 					
 					//call original handler  
 					defaultUncaughtExceptionHandler.uncaughtException(t, e);
 				}
 	        });
 		}
 	}
 	
 	/**
 	 * Stops the beacon, collecting data and sending the information to 
 	 * Malcom server. 
 	 */
 	public synchronized static void stopBeacon() {
 		Log.i(TAG, "Stopping beacon...");
 		mEndTime = BeaconUtils.timeIntervalSince1970(new Date());
 		String beaconData = getJSON();
 		try {
 			if(beaconData!=null){
 				
 				cacheBeacon(beaconData);
 				
 			}
 			
 		} catch (Exception e) {
 			Log.i(TAG, "Exception sending message to the Malcom beacon queue = " + e.getMessage());
 			Log.e(TAG, "Detailed error trace: ",e);
 			cacheBeacon(beaconData);
 		} finally {
 			MCMStats.mBeacon = null;
 			
 			Intent svcPendingBeacons = new Intent(mContext,PendingBeaconsDeliveryService.class);
 			mContext.startService(svcPendingBeacons);
 		}
 	}
 
 	
 	/**
 	 * Get the beacon service instance.
 	 * 
 	 * @return
 	 * @throws MCMStats.BeaconException
 	 */
 	public static synchronized MCMStats getSharedInstance() throws MCMStats.BeaconException {
 		
 		System.out.println("mBeacon: " + mBeacon);
 		
 		if (mBeacon != null) {
 			return mBeacon;
 		} else {
 			throw new BeaconException("Did you call initAndStartBeacon() before?");
 		}
 	}
 
 	
 	
 	
 	// BEACONS - AUXILIAR FUNCTIONS
 	
 	
 	private synchronized void startBeacon() {
 		Date date = new Date();
 		mStartTime = BeaconUtils.timeIntervalSince1970(date);
 	}	
 	
 	/* 
 	 * When a crash occurs, we set the "crash" flag before beacons is sent.
 	 *
 	 */
 	private static void onCrash(){
 		Log.i(TAG, "Stopping beacon after application crash...");
 		appCrashed = true;
 		stopBeacon();
 		Log.i(TAG, "Stopping beacon after application crash done.");
 	}
 	
 	/*
 	 * Saves the beacon in the internal memory of the device in the private 
 	 * application folder. 
 	 */
 	private static synchronized void cacheBeacon(String beaconData){
 		//Save the beacon for later send
 		try {
 			String name = CACHED_BEACON_FILE_PREFIX +DigestUtils.md5Hex(beaconData.getBytes());
 			ToolBox.storage_storeDataInInternalStorage(mContext, name, beaconData.getBytes());
 		} catch (Exception e) {
 			Log.e(TAG,"Error saving the beacon for later delivery ("+e.getMessage()+")",e);
 		}
 	}
 
 	private static String getJSON() {
 		String res = null;
 		
 		JSONObject beaconJson = new JSONObject();
 				
 		try {
 			
 			JSONObject beaconContentJson = new JSONObject();
 			
 			beaconContentJson.put("application_code", MCMCoreAdapter.getInstance().coreGetProperty(MCMCoreAdapter.PROPERTIES_MALCOM_APPID));
 			beaconContentJson.put("lib_version", MCMCoreAdapter.getInstance().SDKVersion());
 			beaconContentJson.put("udid", ToolBox.device_getId(mContext));
 			beaconContentJson.put("device_model", BeaconUtils.getDeviceModel());
 			beaconContentJson.put("device_os", BeaconUtils.getDeviceOs());
 			beaconContentJson.put("app_version", BeaconUtils.getApplicationVersion(mContext));
 			beaconContentJson.put("language", BeaconUtils.getDeviceIsoLanguage());			
 			beaconContentJson.put("device_platform", BeaconUtils.getDevicePlatform());
 			beaconContentJson.put("time_zone", BeaconUtils.getDeviceTimeZone());						
 			beaconContentJson.put("country", BeaconUtils.getDeviceIsoCountry());
 			beaconContentJson.put("tags", (getTagsAsJsonArray()));			
 			beaconContentJson.put("city", mUseLocation? LocationUtils.getDeviceCityLocation(mContext):"");
 			beaconContentJson.put("started_on", mStartTime);
 			beaconContentJson.put("stopped_on", mEndTime);
 			beaconContentJson.put("location", LocationUtils.getLocationJson(mContext));
 			beaconContentJson.put("subbeacons", getSubbeaconsJsonArray());
 			beaconContentJson.put("user_metadata", getUserMetadata());
 			
 			if(appCrashed){
 				beaconContentJson.put("crash", true);
 			}
 			
 			beaconJson.put("beacon", beaconContentJson);
 			Log.i(TAG, "JSON = \n" + beaconJson.toString());
 			
 			res = beaconJson.toString();
 			
 		} catch (Exception e) {
 			Log.i(TAG, "Exception generating JSON beacon = " + e.getMessage());			
 		}
 		
 		return res;
 	}
 
 	private static JSONArray getTagsAsJsonArray() {
 		List<String> listTags = getTags();
 		Log.d(TAG, "Tags: "+ listTags.toString());
 		return new JSONArray(listTags);
 	}
 	
 	
 	// SUB-BEACONS --------------------------------------------------------------------------------	
 	
 	/**
 	 * Starts the sub-beacon (an event) with the specified name. If track-session
 	 * is enabled, time will be saved with the event.
 	 * 
 	 * @param beaconName	SubBeacon name.
 	 * @param trackSession	Tells if track the session
 	 */
 	public void startSubBeaconWithName(String beaconName, boolean trackSession) {
 		Subbeacon subbeacon;
 		try {
 			Log.i(TAG, "Starting subbeacon...");
 			subbeacon = new Subbeacon(beaconName);
 			if (trackSession) {
 				subbeacon.setStartedOn(new Date().getTime());
 				mSubbeaconsDictionary.put(beaconName, subbeacon);
 			}
 
 			mSubbeaconsArray.add(subbeacon);
 		} catch (JSONException e) {
 			Log.e(TAG, "Error starting sub-beacon: "+e.getMessage(),e);
 		}
 	}
 
 	/**
 	 * Stops the specified sub-beacon.
 	 * 
 	 * @param beaconName SubBeacon name.
 	 */
 	public void endSubBeaconWithName(String beaconName) {
 		Subbeacon subbeacon = mSubbeaconsDictionary.get(beaconName);
 		if (subbeacon != null) {
 			subbeacon.setStoppedOn(new Date().getTime());
 		}
 		
 	}
 	
 	/**
 	 * Starts the sub-beacon (an event) with the specified name. If track-session
 	 * is enabled, time will be saved with the event.
 	 * 
 	 * @param beaconName	SubBeacon name.
 	 * @param type	SubBeacon type, it could be: CUSTOM, SPECIAL, ERROR...
 	 * @param trackSession	Tells if track the session
 	 */
 	public void startSubBeaconWithName(String beaconName, SubbeaconType type, Hashtable<String, Object> params, boolean trackSession) {
 		Subbeacon subbeacon;
 		try {
 			Log.i(TAG, "Starting subbeacon...");
 			subbeacon = new Subbeacon(beaconName, type, params);
 			
 			subbeacon.setStartedOn(new Date().getTime());
 
 			if (trackSession) {
 		        //We add the subbeacon to dictionary in order to search it later and stablish the end time
 				mSubbeaconsDictionary.put(beaconName, subbeacon);
 			}else{
 		        //If not track session means that is a unique event without time
 				subbeacon.setStoppedOn(new Date().getTime());
 			}
 
 			mSubbeaconsArray.add(subbeacon);
 		} catch (JSONException e) {
 			Log.e(TAG, "Error starting sub-beacon: "+e.getMessage(),e);
 		}
 	}
 
 	/**
 	 * Stops the specified sub-beacon.
 	 * 
 	 * @param beaconName SubBeacon name.
 	 */
 	public void endSubBeaconWithName(String beaconName, Hashtable<String, Object> params) {
 		Subbeacon subbeacon = mSubbeaconsDictionary.get(beaconName);
 		
 		//Check if the new params are in the start one
 		Enumeration<String> enumeration = params.keys();
 
 		//Update the param array
 		while (enumeration.hasMoreElements()) {
 			String key = enumeration.nextElement();
 			subbeacon.getParams().put(key, params.get(enumeration.nextElement()));
 		}
 
 		if (subbeacon != null) {
 			subbeacon.setStoppedOn(new Date().getTime());
 		}
 		
 	}
 
 	
 	
 	
 	// SUB-BEACONS Auxiliar functions
 	
 	private static JSONArray getSubbeaconsJsonArray() {
 		ArrayList<Subbeacon> subbeaconsJSON = new ArrayList<Subbeacon>();
 		for (Subbeacon sub : mSubbeaconsArray) {
 			//We automatically close any unclosed sub-beacons.
 			if(sub.getStoppedOn()==0){
 				Subbeacon subbeacon = mSubbeaconsDictionary.get(sub.getName());
 				if (subbeacon != null) {
 					Log.w(TAG, "You should close the subbeacon with name: "+subbeacon.getName());
 //					subbeacon.setStoppedOn(new Date().getTime());
 					subbeacon.setStoppedOn(subbeacon.getStartedOn());
 				}				
 			}
 			subbeaconsJSON.add(sub);
 		}
 
 		return new JSONArray(subbeaconsJSON);
 	}
 	
 	//	TAGS
 
     public static List<String> getTags() {
 
         SharedPreferences preferences = mContext.getSharedPreferences("tags", Context.MODE_PRIVATE);
         List<String> tags = new ArrayList<String>((Collection<? extends String>) preferences.getAll().values());
 
         return tags;
     }
 	
 	public static void addTag(String tag) {
 		
 		SharedPreferences preferences = mContext.getSharedPreferences("tags", Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = preferences.edit();
 		editor.putString(tag,tag);
 		
 		editor.commit();
 		
 	}
 	
 	public static void removeTag(String tag) {
 		
 		SharedPreferences preferences = mContext.getSharedPreferences("tags", Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = preferences.edit();
 		editor.remove(tag);
 		
 		editor.commit();
 		
 	}
 	
 	public void setUserMetadata(String userMetadata) {
 		
 		SharedPreferences preferences = mContext.getSharedPreferences("mcm_user_metadata", Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = preferences.edit();
 		editor.putString("user_metadata", userMetadata);
 		
 		editor.commit();
 		
 	}
 	
 	public static String getUserMetadata() {
 		
 		SharedPreferences preferences = mContext.getSharedPreferences("mcm_user_metadata", Context.MODE_PRIVATE); 
 		return (String) preferences.getAll().get("user_metadata");
 		
 	}
 	
 	
 	// UTILITY CLASSES *********************************************************
 	
 	// BeaconException
 	public static class BeaconException extends Exception {
 		
 		private static final long serialVersionUID = 2115918307306918270L;
 
 		public BeaconException(String detailMessage) {
 			super(detailMessage);
 		}
 	}
 	
 	/*
      * This class is used to send beacon to malcom
      * 
      * We run this process in a separate thread to android > 4.0
      *  
      * @author Malcom Ventures S.L
      * @since  2012
      *
      */
 	private static class SendBeaconToMalcom extends Thread implements Runnable{
     	
     	private String beaconToSend;
     	
     	
     	public SendBeaconToMalcom(String beacon) {
     		
     		this.beaconToSend = beacon;
     		
     	}
     	
 		@Override
 		public void run() {
 			
 			try {
 				StatsUtils.sendBeaconToMalcom(this.beaconToSend);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 		
     }
 
 }
