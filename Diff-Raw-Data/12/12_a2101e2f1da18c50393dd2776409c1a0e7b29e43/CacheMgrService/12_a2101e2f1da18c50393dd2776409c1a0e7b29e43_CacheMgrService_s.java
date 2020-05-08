 package com.coffeeandpower.cache;
 
 import java.util.List;
 import java.util.Observer;
 
 import com.coffeeandpower.AppCAP;
 import com.coffeeandpower.Constants;
 import com.coffeeandpower.cont.DataHolder;
 import com.coffeeandpower.cont.UserSmart;
 import com.coffeeandpower.cont.VenueSmart;
 import com.coffeeandpower.cont.VenueSmart.CheckinData;
 import com.google.android.maps.GeoPoint;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 
 public class CacheMgrService extends Service {
 
 	protected static String TAG = "CacheMgrService";
 	
 	
 	
 	
 	
 	private static final double DATA_DISTANCE_REFRESH_THRESHOLD = 1000;			// meters
 	
 	private static Integer tick = 0;
 	
 	private static final double defaultLat = 37.7717121657157;
 	private static final double defaultLon = -122.4239288438208;
 	
 	private static CachedNetworkData venuesWithCheckinsCache = new CachedNetworkData("venuesWithCheckins");
 	private static CachedNetworkData nearbyVenuesCache = new CachedNetworkData("nearbyVenues");
 	private static CachedNetworkData contactsListCache = new CachedNetworkData("contactsList");
 	
 	private static boolean isRunning = false;
 	
 	private static boolean allowCachedDataThisRun = false;
 	private static boolean refreshAllDataThisRun = false;
 	
 	
 	
 	private static int numberOfCalls = 0;
 	
 	private static int apisCalledThisUpdate = 0;
 	private static boolean cachedDataSentThisUpdate = false;
 	
 	// Scheduler
 	protected static Handler taskHandler = new Handler();
 	
 	
 	//=====================================================
 	// Service Lifecycle
 	//=====================================================
 	@Override
 	public void onCreate() {
 		Log.d(TAG,"onCreate()");
 		
 		
 		
 		tick = 10;
 		
 	}
 	
 	@Override
 	public void onDestroy() {
 		Log.d(TAG,"onDestroy()");
 		//locationManager.removeUpdates(passiveLocationReceiver);
 		
 	}
 	
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// Not intended to be bound so return null
 		return null;
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		
 		Log.d(TAG,"onStartCommand()");
 		
 		
 		
 		
 		// Start the periodic timer
 		taskHandler.post(runTimer);
 		
 		return START_STICKY;
 		
 	}
 	
 	
 	//=====================================================
 	// Cached data consumer registration
 	//=====================================================
 
 	public static void startObservingAPICall(String apicall, Observer context) {
 		
 		if (apicall.equals("venuesWithCheckins")) {
 			if (Constants.debugLog)
 				Log.d(TAG,"Enabling venuesWithCheckins API for " + context.toString());
 			venuesWithCheckinsCache.activate();
 			venuesWithCheckinsCache.addObserver(context);
 		}
 		else if (apicall.equals("nearbyVenues")) {
 			if (Constants.debugLog)
 				Log.d(TAG,"Enabling nearbyVenues API for " + context.toString());
 			nearbyVenuesCache.activate();
 			nearbyVenuesCache.addObserver(context);
 		}
 		else if (apicall.equals("contactsList")) {
 			if (Constants.debugLog)
 				Log.d(TAG,"Enabling contactsList API for " + context.toString());
 			contactsListCache.activate();
 			contactsListCache.addObserver(context);
 		}
 		else
 		{
 			if (Constants.debugLog)
 				Log.d(TAG,"INVALID OPTION FOR OBSERVER REGISTRATION");
 		}
 		
 		//The user is moving around the activities lets keep the data fresh
 		numberOfCalls = 0;
 		//Restart the timer
 		allowCachedDataThisRun = true;
 		if (Constants.debugLog)
 			Log.d(TAG,"getCachedDataForAPICall is calling stop/start");
 		stopPeriodicTimer();
 		startPeriodicTimer();
 		
 	}
 	
 	
 	
 	
 	public static void startObservingAPICalls(String apicall1, String apicall2, Observer context) {
 		
 		if (apicall1.equals("venuesWithCheckins") || apicall2.equals("venuesWithCheckins")) {
 			if (Constants.debugLog)
 				Log.d(TAG,"Enabling venuesWithCheckins API for " + context.toString());
 			venuesWithCheckinsCache.activate();
 			venuesWithCheckinsCache.addObserver(context);
 		}
 		if (apicall1.equals("nearbyVenues") || apicall2.equals("nearbyVenues")) {
 			if (Constants.debugLog)
 				Log.d(TAG,"Enabling nearbyVenues API for " + context.toString());
 			nearbyVenuesCache.activate();
 			nearbyVenuesCache.addObserver(context);
 		}
 		if (apicall1.equals("contactsList") || apicall2.equals("contactsList")) {
 			if (Constants.debugLog)
 				Log.d(TAG,"Enabling contactsList API for " + context.toString());
 			contactsListCache.activate();
 			nearbyVenuesCache.addObserver(context);
 		}
 		
 		//The user is moving around the activities lets keep the data fresh
 		numberOfCalls = 0;
 		//Restart the timer
 		allowCachedDataThisRun = true;
 		if (Constants.debugLog)
 			Log.d(TAG,"getCachedDataForAPICall is calling stop/start");
 		stopPeriodicTimer();
 		startPeriodicTimer();
 		
 	}
 	
 	
 	
 	
 	public static void stopObservingAPICall(String apicall, Observer context) {
 		if (apicall.equals("venuesWithCheckins")) {
 			if (Constants.debugLog)
 				Log.d(TAG,"Removing venuesWithCheckins observer for " + context.toString() + ".");
 			venuesWithCheckinsCache.deleteObserver(context);
 			if (venuesWithCheckinsCache.countObservers() == 0) {
 				venuesWithCheckinsCache.deactivate();
 				if (Constants.debugLog)
 					Log.d(TAG,"Removed last observer from venuesWithCheckins, deactivating.");
 			}
 			
 		}
 		else if (apicall.equals("nearbyVenues")) {
 			if (Constants.debugLog)
 				Log.d(TAG,"Removing nearbyVenues observer for " + context.toString() + ".");
 			nearbyVenuesCache.deleteObserver(context);
 			if (nearbyVenuesCache.countObservers() == 0) {
 				nearbyVenuesCache.deactivate();
 				if (Constants.debugLog)
 					Log.d(TAG,"Removed last observer from nearbyVenues, deactivating.");
 			}
 		}
 		else if (apicall.equals("contactsList")) {
 			if (Constants.debugLog)
 				Log.d(TAG,"Removing contactsList observer for " + context.toString() + ".");
 			contactsListCache.deleteObserver(context);
 			if (contactsListCache.countObservers() == 0) {
 				contactsListCache.deactivate();
 				if (Constants.debugLog)
 					Log.d(TAG,"Removed last observer from contactsList, deactivating.");
 			}
 		}
 	}
 	
 	
 	
 	//=====================================================
 	// Periodic timer management
 	//=====================================================	
 
 	public static void stopPeriodicTimer() {
 		
 		if (isRunning == true) {
 			if (Constants.debugLog)
 				Log.d(TAG,"CacheMgrService.stop()");
 			isRunning = false;
 			taskHandler.removeCallbacks(runTimer);
 		}
 		else
 		{
 			if (Constants.debugLog)
 				Log.d(TAG,"Warning: Tried to stop CacheMgrService but it was not started...");
 		}
 	}
 
 	public static void startPeriodicTimer() {
 		
 		if (isRunning == false) {
 			if (Constants.debugLog)
 				Log.d(TAG,"CacheMgrService.start()");
 			isRunning = true;
 			taskHandler.removeCallbacks(runTimer);
 			taskHandler.post(runTimer);
 		}
 		else
 		{
 			if (Constants.debugLog)
 				Log.d(TAG,"Warning: Tried to start CacheMgrService when it was already running...");
 		}
 		
 	}
 	
 	public static void manualTrigger() {
 		if (Constants.debugLog)
 			Log.d(TAG,"manualTrigger()");
 		stopPeriodicTimer();
 		startPeriodicTimer();
 	}
 	
 	public static void refreshAllData() {
 		if (Constants.debugLog)
 			Log.d(TAG,"refreshAllData()");
 		refreshAllDataThisRun = true;
 		stopPeriodicTimer();
 		startPeriodicTimer();
 	}
 
 	private static Runnable runTimer = new Runnable()
         {
 		private double latForAPI;
 		private double lonForAPI;
 		private double[] llArray = new double[2];
 		
 		@Override
 		public void run()
 		{
         	    // We are now on the main thread, so kick off the API call in a worker thread
         	    new Thread(new Runnable() {
 			public void run() {
         			    
         			    //isFirstRun = true;
         			    
         			    if (AppCAP.getUserLatLon()[0] == 0 && AppCAP.getUserLatLon()[1] == 0) {
         				    if (Constants.debugLog)
         						Log.d(TAG,"User position is currently 0-0, using default position for API calls.");
 
         				    latForAPI = defaultLat;
         				    lonForAPI = defaultLon;
         			    } else {
         				    latForAPI = AppCAP.getUserLatLon()[0];
         				    lonForAPI = AppCAP.getUserLatLon()[1];
         			    }
         			    
         			    llArray[0] = latForAPI;
 				    llArray[1] = lonForAPI;
                                 	   
 				    // Debug instrumentation
 				    if (Constants.debugLog)
 						Log.d(TAG,"API calls: Using coordinates: " + latForAPI + ", " + lonForAPI);
 				    if (Constants.debugLog)
 						Log.d(TAG,"Cache Status: venuesWithCheckins: " + venuesWithCheckinsCache.hasData() +
                 				    " nearbyVenues: " + nearbyVenuesCache.hasData() + 
                 				    " contactsList: " + contactsListCache.hasData());
 				    if (Constants.debugLog)
 						Log.d(TAG," APIs Active: venuesWithCheckins: " + venuesWithCheckinsCache.isActive() +
                 				    " nearbyVenues: " + nearbyVenuesCache.isActive() + 
                 				    " contactsList: " + contactsListCache.isActive());
                 		    
 				    // Do API calls for active caches
 				    //
 				    // the data for each cache is sent to observers if:
 				    // - the cache is set as active ||
 				    // - the cache does not have data ||
 				    // - the flag refreshAllDataThisRun was set
 				    //
 				    // the criteria for sending cached data instead of refreshing is:
 				    // - allowCachedDataThisRun hasn't been set to false &&
 				    // - the cache has data &&
 				    // - refreshAllDataThisRun hasn't been set &&
 				    // - the current distance from the cached data is within the threshold
 				    
                 		    apisCalledThisUpdate = 0;
                 		    cachedDataSentThisUpdate = false;
                 		    
                 		    // Determine if venuesWithCheckins should run
                 		    if (venuesWithCheckinsCache.isActive() || 
                 				    !venuesWithCheckinsCache.hasData() ||
                 				    refreshAllDataThisRun) {
                 			    
                 			    // Determine if cached data should be sent or data needs to be refreshed
                 			    if (allowCachedDataThisRun && 
                 					    venuesWithCheckinsCache.hasData() && 
                 					    !refreshAllDataThisRun &&
                 					    venuesWithCheckinsCache.dataDistanceFrom(llArray) < DATA_DISTANCE_REFRESH_THRESHOLD) {
                 				    if (Constants.debugLog)
                 						Log.d(TAG,"Sending cached data for venuesWithCheckins");
                 				    venuesWithCheckinsCache.sendCachedData();
                 				    cachedDataSentThisUpdate = true;
                 			    } else {
                 				    if (Constants.debugLog)
                 						Log.d(TAG,"Refreshing venuesWithCheckinsCache...");
                         			    venuesWithCheckinsCache.setNewData(AppCAP.getConnection().getNearestVenuesWithCheckinsToCoordinate(llArray),llArray);
                         			    if (Constants.debugLog)
                         					Log.d(TAG,"Called VenuesWithCheckins, Received: " + venuesWithCheckinsCache.getData().getResponseMessage());
                         			    apisCalledThisUpdate += 1;
                 			    } 
                 		    }
                 		    
                 		    // Determine if venuesWithCheckins should run
                 		    if (nearbyVenuesCache.isActive() || 
                 				    !nearbyVenuesCache.hasData() ||
                 				    refreshAllDataThisRun) {
                 			    
                 			    if (allowCachedDataThisRun && 
                 					    nearbyVenuesCache.hasData() && 
                 					    !refreshAllDataThisRun &&
                         				    nearbyVenuesCache.dataDistanceFrom(llArray) < DATA_DISTANCE_REFRESH_THRESHOLD) {
                 				    if (Constants.debugLog)
                 						Log.d(TAG,"Sending cached data for nearbyVenues");
                 				    nearbyVenuesCache.sendCachedData();
                 				    cachedDataSentThisUpdate = true;
                 			    } else {
                 				    if (Constants.debugLog)
                 						Log.d(TAG,"Refreshing nearbyVenuesCache...");
                                 		    final GeoPoint gp = new GeoPoint((int)(latForAPI*1E6), (int)(lonForAPI*1E6));
                                 		    nearbyVenuesCache.setNewData(AppCAP.getConnection().getVenuesCloseToLocation(gp,20),new double[]{latForAPI,lonForAPI});
                                 		    if (Constants.debugLog)
                                 				Log.d(TAG,"Called VenuesWithCheckins, Received: " + nearbyVenuesCache.getData().getResponseMessage());
                                 		    apisCalledThisUpdate += 1;
                 			    }
                 		    }
                 		    
                 		 // Determine if contactsList should run
                 		    if (contactsListCache.isActive() || 
                 				    !contactsListCache.hasData() ||
                 				    refreshAllDataThisRun) {
                 			    
                 			    if (allowCachedDataThisRun && 
                 					    contactsListCache.hasData() && 
                 					    !refreshAllDataThisRun &&
                         				    contactsListCache.dataDistanceFrom(llArray) > DATA_DISTANCE_REFRESH_THRESHOLD) {
                 				    if (Constants.debugLog)
                 						Log.d(TAG,"Sending cached data for contactsList");
                 				    contactsListCache.sendCachedData();
                 				    cachedDataSentThisUpdate = true;
                 			    } else {
                 				    if (Constants.debugLog)
                 						Log.d(TAG,"Refreshing contactsListCache...");
                         			    contactsListCache.setNewData(AppCAP.getConnection().getContactsList(),new double[]{latForAPI,lonForAPI});
                         			    if (Constants.debugLog)
                         					Log.d(TAG,"Called getContactsList, Received: " + contactsListCache.getData().getResponseMessage());
                                 		    apisCalledThisUpdate += 1;
                 			    }
                 		    }
                 		    
                 		    if (cachedDataSentThisUpdate && Constants.debugLog) 
                 			    Log.d(TAG,"Cached Data sent this update.");
                 		    if (apisCalledThisUpdate > 0 && Constants.debugLog)
                 			    Log.d(TAG,"API calls this made this update: " + apisCalledThisUpdate);
                 		    if (!cachedDataSentThisUpdate && apisCalledThisUpdate == 0 && Constants.debugLog)
                 			    Log.d(TAG,"No data consumers active.");
                         	    
                 		    // Clear any flags that control behavior for a single update
         			    allowCachedDataThisRun = false;
         			    refreshAllDataThisRun = false;
                         	    
         			    //We are going stop the timer if the user hasn't moved views in a while
         			    numberOfCalls++;
         			    
         			    //Currently 10 second interval with 20 calls so 3.3bar minutes
         			    if(numberOfCalls < 20)
         			    {
         				    if (Constants.debugLog)
         						Log.d(TAG,"Posting runnable delayed for 10 seconds...");
         				    taskHandler.postDelayed(runTimer, tick * 1000);
         			    }
         			    else
         			    {
         				    if (Constants.debugLog)
         						Log.d(TAG,"Turning off periodic timer until user activity");
                                 	    //TODO We should also turn off the GPS at this point as well
         			    }
         			    
         			    
         		    }
         		    
         	    }).start();
         	    
         	    
             }
         };
         
         
         
         
         
         //=====================================================
         // Asynchronous Events
         //=====================================================	
         
 	public static void checkInTrigger(VenueSmart checkedInVenue) {
 
 		stopPeriodicTimer();
 		//Stow the venue Id for the checkout later
 		//FIXME
 		//Test uninitialized case first
 		//AppCAP.setUserLastCheckinVenueId(checkedInVenue.getVenueId());
 		
 		//Venue Related
 		//We need to look at the list of venues with checkins and see if they checked into one of those venues
 		//If they checked into a venue without checkins, we need to add it to the venuesWithCheckinsCache
 		DataHolder venuesWithCheckins = venuesWithCheckinsCache.getData();
 		Object[] obj = (Object[]) venuesWithCheckins.getObject();
 		@SuppressWarnings("unchecked")
		List<VenueSmart> arrayVenues = (List<VenueSmart>) obj[0];
 		boolean venueFound = false;
 		VenueSmart tmpVenue = null;
 		for(VenueSmart currVenue : arrayVenues)
 		{
 			if(currVenue.getVenueId() == checkedInVenue.getVenueId())
 			{
 				venueFound = true;
 				tmpVenue = currVenue;
 				break;
 			}
 		}
 		if(venueFound==false)
 		{
 			arrayVenues.add(checkedInVenue);
 			tmpVenue = checkedInVenue;
 		}
 		//Once we have the correct venue we need to add our user to the list of checkins and increment the total venue checkins
 		CheckinData newCheckinData = new CheckinData(AppCAP.getLoggedInUserId(), 0, 1);
 		//Check to see if we are in the checkins array first erroneously and then add us and increment
                 //TODO
                 //Implement check
 		tmpVenue.getArrayCheckins().add(newCheckinData);
 		tmpVenue.setCheckins(tmpVenue.getCheckins()+1);
 		
 		//People list Related
 		//Find the current logged in user in the people list and update their status to checkedin
 		@SuppressWarnings("unchecked")
 		List<UserSmart> arrayUsers = (List<UserSmart>) obj[1];
 		boolean userFound = false;
 		for(UserSmart currUser : arrayUsers)
 		{
 			if(currUser.getUserId() == AppCAP.getLoggedInUserId())
 			{
 				currUser.setCheckedIn(1);
 				userFound =  true;
 				break;
 			}
 		}
 		if(userFound == false && Constants.debugLog)
 		{
 			Log.d(TAG,"Logged In User not found in People list!!!!!");
 		}
 		//After local cache is updated kickoff a refresh of all data via http
 		refreshAllData();
 	}
 	
 	public static void checkOutTrigger(){
 		
 		stopPeriodicTimer();
 		boolean waitForServerData = false;
 		//Stow the venue Id for the checkout later
 		int lastVenueId = AppCAP.getUserLastCheckinVenueId();
 		//Check here for case when LastCheckin is not valid, multiple devices will defy this and create brief data discounts
 		//if(yada-yada)
 		//Venue Related
                 //We need to look at the list of venues with checkins and see if they checked into one of those venues
                 DataHolder venuesWithCheckins = venuesWithCheckinsCache.getData();
                 Object[] obj = (Object[]) venuesWithCheckins.getObject();
                 @SuppressWarnings("unchecked")
                List<VenueSmart> arrayVenues = (List<VenueSmart>) obj[0];
                 boolean venueFound = false;
                 VenueSmart tmpVenue = null;
                for(VenueSmart currVenue : arrayVenues)
                 {
                 	if(currVenue.getVenueId() == lastVenueId)
                 	{
                 		venueFound = true;
                 		tmpVenue = currVenue;
                 		int numCheckins = tmpVenue.getCheckins();
                 		//Decrement number of users checkedin, should never go below 0
                 		if(numCheckins>0)
                 		{
                 			tmpVenue.setCheckins(numCheckins - 1);
                 		}
                 		break;
                 	}
                 }
                 if(venueFound==false)
                 {
                 	//This shouldn't really happen, but if it does we just need to wait for server data
                 	//The list of venues with checkins should include the venue the user is checking out of
                 	waitForServerData = true;
                 }
                 else
                 {
                         //Once we have the correct venue we need to remove our user to the list of checkins
                         boolean usersCheckinFound = false;
                         for(CheckinData currCheckIn : tmpVenue.getArrayCheckins() )
                         {
                         	if(currCheckIn.getUserId() == AppCAP.getLoggedInUserId())
                         	{
                         		//Remove the current user from the checkin list
                         		tmpVenue.getArrayCheckins().remove(currCheckIn);
                         		usersCheckinFound =  true;
                         		break;
                         	}
                         }
                         if(usersCheckinFound == false)
                         {
                         	//We can't find our user in the cached venue, we will need to wait for the server data
                         	waitForServerData = true;
                         }
                 }
                 
                 //People list Related
                 //Find the current logged in user in the people list and update their status to checkedOut
                 @SuppressWarnings("unchecked")
                 List<UserSmart> arrayUsers = (List<UserSmart>) obj[1];
                 boolean userFound = false;
                 for(UserSmart currUser : arrayUsers)
                 {
                 	if(currUser.getUserId() == AppCAP.getLoggedInUserId())
                 	{
                 		currUser.setCheckedIn(0);
                 		userFound =  true;
                 		break;
                 	}
                 }
                 if(userFound == false && Constants.debugLog)
                 {
 			if (Constants.debugLog)
 				Log.d(TAG,"Logged In User not found in People list!!!!!");
                 }
                 if(waitForServerData)
                 {
                 	if (Constants.debugLog)
 				Log.d(TAG,"NEED TO WAIT FOR SERVER DATA CHECKOUT LOGIC FAILED!!!!!");
                 }
                 //After local cache is updated kickoff a refresh of all data via http
                 refreshAllData();
 		
 	}
 
 }
