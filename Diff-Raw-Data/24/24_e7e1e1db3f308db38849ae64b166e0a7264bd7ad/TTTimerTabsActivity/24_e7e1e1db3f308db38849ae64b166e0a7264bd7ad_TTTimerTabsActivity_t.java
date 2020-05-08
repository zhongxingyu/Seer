 package com.gvccracing.android.tttimer;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import android.content.BroadcastReceiver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.TabHost;
 
 import com.gvccracing.android.tttimer.Controls.Timer;
 import com.gvccracing.android.tttimer.DataAccess.AppSettingsCP.AppSettings;
 import com.gvccracing.android.tttimer.DataAccess.RaceCP.Race;
 import com.gvccracing.android.tttimer.DataAccess.RaceInfoViewCP.RaceInfoResultsView;
 import com.gvccracing.android.tttimer.DataAccess.RaceResultsCP.RaceResults;
 import com.gvccracing.android.tttimer.Dialogs.AddRaceView;
 import com.gvccracing.android.tttimer.Dialogs.ChooseViewingMode;
 import com.gvccracing.android.tttimer.Dialogs.OtherRaceResults;
 import com.gvccracing.android.tttimer.Tabs.CheckInTab;
 import com.gvccracing.android.tttimer.Tabs.FinishTab;
 import com.gvccracing.android.tttimer.Tabs.OtherTab;
 import com.gvccracing.android.tttimer.Tabs.RaceInfoTab;
 import com.gvccracing.android.tttimer.Tabs.ResultsTab;
 import com.gvccracing.android.tttimer.Tabs.StartTab;
 import com.gvccracing.android.tttimer.Utilities.TabManagerContainer;
 
 public class TTTimerTabsActivity extends FragmentActivity {
 	
 	/**
 	 * The string used for logging everywhere in this class
 	 */
 	public static final String LOG_TAG = TTTimerTabsActivity.class.getSimpleName();
     
 	/**
 	 * The race ID has changed
 	 */
 	public static final String RACE_ID_CHANGED_ACTION = "com.gvccracing.android.tttimer.RACE_ID_CHANGED";
 
 	/**
 	 * This action is fired when a new racer is on deck
 	 */
 	public static final String CHANGE_VISIBLE_TAB = "com.gvccracing.android.tttimer.CHANGE_VISIBLE_TAB";
 	
 	/**
 	 * This is the name of the tag that contains the tab name to make visible
 	 */
 	public static final String VISIBLE_TAB_TAG = "VisibleTabTag";
  
     /**
      * The intent filter is used to listen for events that this class can handle.  Subscribed events are set up in onCreate.
      */
 	public IntentFilter actionFilter = new IntentFilter();
 
     /**
      * The timer object that will be doing the timing calculations and display the results
      */
     public Timer timer = null;
     
     /**
      * The tab manager in charge of creating and showing tabs
      */
     private TabManagerContainer.TabManager tabManager;
     
     /**
      * The tabHost that contains all of the tabs
      */
     private TabHost tabHost;
 	
 	/**
 	 * Does some basic setup, including setting the layout to use, setting up action filters, creating the tabs, setting some member variables,
 	 * and setting up which tabs are visible, and what state they're in.
 	 * @param The saved instance state of this activity that could be used to restore the previous state that we were in before being killed off
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		try{
 			super.onCreate(savedInstanceState);
 			
 			setContentView(R.layout.tabs_main);
 			
 			CreateTabs();
 		    
  			// Set up the timer view
  			timer = ((Timer)findViewById(R.id.TimerBottom));
 		}catch(Exception ex){
     		Log.e(LOG_TAG, "onCreate failed: ", ex);
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		AppSettings.Update(this, AppSettings.AppSetting_AdminMode_Name, Boolean.toString(false), true);
 		
 		AddActionFilter(AddRaceView.RACE_ADDED_ACTION);
 		AddActionFilter(Timer.RACE_IS_FINISHED_ACTION);
 		AddActionFilter(TTTimerTabsActivity.CHANGE_VISIBLE_TAB);
 		AddActionFilter(TTTimerTabsActivity.RACE_ID_CHANGED_ACTION);
 
         // Register for broadcasts when a tab is changed
         this.registerReceiver(mActionReceiver, actionFilter);
 		timer.RegisterReceiver();
 		
 		UpdateRaceState();
 	}
 	
 	@Override 
 	protected void onPause() {
 		super.onPause();
 		timer.UnregisterReceiver();
 		if(mActionReceiver != null && actionFilter.countActions() > 0){
     		unregisterReceiver(mActionReceiver);
     	}
 	}
 	
 	public void UpdateRaceState(){
 	    // Figure out if a race is currently going on.  
         if(FindRaceInProgress()) {
         	// Since races are started and tracked only locally, if a race is still going, it was started on this device
 	        // If the race already has a raceStartTime, and not all racers are finished, set the startTime variable to raceStartTime and get the timer running
         	UpdateFromRaceInProgress();
         } else if (FindAvailableRace()){
         	// Find a race that's not started yet, but has been set up for this date
         	tabHost.setCurrentTabByTag(RaceInfoTab.RaceInfoTabSpecName);
         } else if (FindFinishedRace()) {
         	// Find a finished race on the current date
         	SetupFinishedRace();
         } else {
         	// If no races are configured for the current date, check if any raceID is in the DB, and display that underneath the upcoming dialog
         	// Figure out if there's a race_ID in the database (not -1)
         	// if so, set up the tabs for that race, since it's being displayed anyway
         	long race_ID = Long.parseLong(AppSettings.ReadValue(this, AppSettings.AppSetting_RaceID_Name, Long.toString(-1l)));
         	if(race_ID > 0){
             	String[] projection = new String[]{Race.getTableName() + "." + Race._ID + " as _id", Race.RaceDate, Race.RaceStartTime, RaceResults.EndTime, Race.StartInterval};
         		String selection = Race.getTableName() + "." + Race._ID + "=?";
         		String[] selectionArgs = new String[]{Long.toString(race_ID)}; 
         		String sortOrder = Race.getTableName() + "." + Race._ID;
         		Cursor theRace =  getContentResolver().query(RaceInfoResultsView.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
         		if(theRace != null){
         			theRace.moveToFirst();
         		}
         		if(IsRaceFinished(theRace)){
         			// Do the same thing as SetupFinishedRace, but don't show the results tab
         			// Hide the timer
         	    	timer.setVisibility(View.GONE);     	
         	    	// Remove "check in" tab - Don't allow any more racers to check in, the race is done!
         	    	tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.GONE);//.setEnabled(false);
         	    	// Remove "finish" tab - There are no unfinished racers
         	    	tabHost.getTabWidget().getChildTabViewAt(3).setVisibility(View.GONE);//.setEnabled(false);
         		} else {
         			SetupAvailableRace();
         		}
        		if(theRace != null){
        			theRace.close();
        			theRace = null;
        		}
         	}else{
         		AppSettings.Update(this, AppSettings.AppSetting_RaceID_Name, Long.toString(-1l), true);
         	}
         	tabHost.setCurrentTabByTag(RaceInfoTab.RaceInfoTabSpecName);
         	// Couldn't find a race in progress, or an unstarted race, or a finished race, so need to add a race, or select a previous one
         	// Let the user choose whether to add a race or view a previous one	 
         	if(FindAnyPreviousRaces()){
 				ChooseViewingMode chooseModeDialog = new ChooseViewingMode();
 				FragmentManager fm = getSupportFragmentManager();
 				chooseModeDialog.show(fm, ChooseViewingMode.LOG_TAG);
         	}else{
         		AddRaceView addRaceDialog = new AddRaceView();
 				FragmentManager fm = getSupportFragmentManager();
 				addRaceDialog.show(fm, AddRaceView.LOG_TAG);
         	}
         }
 	}
 	
 	/**
 	 * This is called when a previous race was selected from the list.  It still figures out what to display, and then displays it.
 	 * @param raceID
 	 */
 	public void UpdatePreviousRaceState(long raceID){
 		// Hide the timer
     	timer.setVisibility(View.VISIBLE);     	
     	// Remove "check in" tab - Don't allow any more racers to check in, the race is done!
     	tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.VISIBLE);//.setEnabled(false);
     	// Remove "finish" tab - There are no unfinished racers
     	tabHost.getTabWidget().getChildTabViewAt(3).setVisibility(View.VISIBLE);//.setEnabled(false);
     	// Show "results" panel - This is really what people care about when looking at a finished race
     	tabHost.setCurrentTabByTag(RaceInfoTab.RaceInfoTabSpecName);
     	
     	String[] projection = new String[]{Race.getTableName() + "." + Race._ID + " as _id", Race.RaceDate, Race.RaceStartTime, RaceResults.EndTime, Race.StartInterval};
 		String selection = Race.getTableName() + "." + Race._ID + "=?";
 		String[] selectionArgs = new String[]{Long.toString(raceID)}; 
 		String sortOrder = Race.getTableName() + "." + Race._ID;
 		
 		Cursor theRace = getContentResolver().query(RaceInfoResultsView.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 		
 		if(theRace != null && theRace.getCount() > 0){
 			theRace.moveToFirst();
 			
 			Long startInterval = theRace.getLong(theRace.getColumnIndex(Race.StartInterval));
 			AppSettings.Update(this, AppSettings.AppSetting_StartInterval_Name, Long.toString(startInterval), true);
 			
 		    // Figure out if a race is currently going on.  
 	        if(IsRaceInProgress(theRace)) {
 	        	// Show the timer
 	        	timer.setVisibility(View.VISIBLE);  
 	        	// Since races are started and tracked only locally, if a race is still going, it was started on this device
 		        // If the race already has a raceStartTime, and not all racers are finished, set the startTime variable to raceStartTime and get the timer running
 	        	UpdateFromRaceInProgress(theRace);
 	        } else if (IsRaceFinished(theRace)) {	        
 	        	timer.setVisibility(View.GONE); 
 	        	// Find a finished race on the current date
 	        	SetupFinishedRace();
 	        } 
 		} else {
 			if(theRace != null){
 		        theRace.close();
 		        theRace = null;
 			}
 			projection = new String[]{Race.getTableName() + "." + Race._ID + " as _id", Race.RaceDate, Race.RaceStartTime, Race.StartInterval};
 			theRace = getContentResolver().query(Race.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 
 			if(theRace != null && theRace.getCount() > 0){
 				theRace.moveToFirst();
 
 				Long startInterval = theRace.getLong(theRace.getColumnIndex(Race.StartInterval));
 				AppSettings.Update(this, AppSettings.AppSetting_StartInterval_Name, Long.toString(startInterval), true);
 				
 				if (IsRaceAvailable(theRace)){ 
 		        	SetupAvailableRace();
 		        }
 			}
 		}
 		if(theRace != null){
 	        theRace.close();
 	        theRace = null;
 		}
 	}
 	
 	private boolean IsRaceInProgress(Cursor theRace){
 		boolean foundRaceInProgress = false;
 		try{			
 			if(theRace.getCount() > 0 && theRace.isNull(theRace.getColumnIndex(RaceResults.EndTime)) && 
 			   !theRace.isNull(theRace.getColumnIndex(Race.RaceStartTime)) && theRace.getLong(theRace.getColumnIndex(Race.RaceStartTime)) > 0){	
 				theRace.moveToFirst();
 				Long raceID = theRace.getLong(theRace.getColumnIndex(Race._ID));
 				if(System.currentTimeMillis() < theRace.getLong(theRace.getColumnIndex(Race.RaceStartTime)) + 86400000)
 				{				
 					foundRaceInProgress = true;
 				}else {
 					// The race was started over 24 hours ago.  This probably isn't correct.  Just stop the race now.
 					do{
 						ContentValues content = new ContentValues();
 						content.put(RaceResults.EndTime, theRace.getLong(theRace.getColumnIndex(Race.RaceStartTime)) + 86400000);
 						content.put(RaceResults.ElapsedTime, System.currentTimeMillis() - theRace.getLong(theRace.getColumnIndex(Race.RaceStartTime)));
 				
 						// Update the race results
 						RaceResults.Update(this, content, RaceResults.Race_ID + "= ? and " + RaceResults.EndTime + " IS NULL", new String[]{Long.toString(raceID)});
 					}while(theRace.moveToNext());
 				}
 			}
      	}catch(Exception ex){Log.e(LOG_TAG, "RaceIsInProgress failed", ex);}
 		
 		return foundRaceInProgress;
 	}
 	
 	private boolean IsRaceAvailable(Cursor theRace){
 		boolean foundAvailableRace = false;
 		try{						
 			if(theRace.getCount() > 0 && !theRace.isNull(theRace.getColumnIndex(Race.RaceDate)) && theRace.isNull(theRace.getColumnIndex(Race.RaceStartTime))){	
 				foundAvailableRace = true;
 			}
 			
      	}catch(Exception ex){Log.e(LOG_TAG, "RaceIsAvailable failed", ex);}
 		
 		return foundAvailableRace;
 	}
 	
 	private boolean IsRaceFinished(Cursor theRace){
 		boolean foundFinishedRace = false;
 		try{			
 			if(theRace.getCount() > 0 && !theRace.isNull(theRace.getColumnIndex(Race.RaceDate)) &&  
 			   ((!theRace.isNull(theRace.getColumnIndex(Race.RaceStartTime)) && theRace.getLong(theRace.getColumnIndex(Race.RaceStartTime)) > 0) ||
 			   (!theRace.isNull(theRace.getColumnIndex(RaceResults.EndTime)) && theRace.getLong(theRace.getColumnIndex(RaceResults.EndTime)) > 0))){	
 				foundFinishedRace = true;
 			}
      	}catch(Exception ex){
      		Log.e(LOG_TAG, "RaceIsFinished failed", ex);
  		}
 		
 		return foundFinishedRace;
 	}
 
 	/**
 	 * Create all of the tabs and add them to the tabManager/tabHost
 	 */
 	private void CreateTabs() {
         // Set up the tabs
 	    tabHost = (TabHost)findViewById(android.R.id.tabhost);  // The activity TabHost
 	    tabHost.setup();
 	    tabManager = new TabManagerContainer.TabManager(this, tabHost, R.id.realtabcontent);
 	    
 	    // Race Info Tab
 	    tabManager.addTab(tabHost.newTabSpec(RaceInfoTab.RaceInfoTabSpecName).setIndicator("Race Info"), RaceInfoTab.class, null);
 	    // Check In Tab
 	    tabManager.addTab(tabHost.newTabSpec(CheckInTab.CheckInTabSpecName).setIndicator("Check In"), CheckInTab.class, null);
 	    // Start Tab
 	    tabManager.addTab(tabHost.newTabSpec(StartTab.StartTabSpecName).setIndicator("Start"), StartTab.class, null);
 	    // Finish Tab
 	    tabManager.addTab(tabHost.newTabSpec(FinishTab.FinishTabSpecName).setIndicator("Finish"), FinishTab.class, null);
 	    // Results Tab
 	    tabManager.addTab(tabHost.newTabSpec(ResultsTab.ResultsTabSpecName).setIndicator("Results"), ResultsTab.class, null);
 	    // Other Tab
 	    tabManager.addTab(tabHost.newTabSpec(OtherTab.OtherTabSpecName).setIndicator("Other"), OtherTab.class, null);
 	}
 
 	/**
 	 * Set up the tabs control display for a finished race
 	 */
 	private void SetupFinishedRace(){
     	// Hide the timer
     	timer.setVisibility(View.GONE);     	
     	// Remove "check in" tab - Don't allow any more racers to check in, the race is done!
     	tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.GONE);//.setEnabled(false);
     	// Remove "finish" tab - There are no unfinished racers
     	tabHost.getTabWidget().getChildTabViewAt(3).setVisibility(View.GONE);//.setEnabled(false);
     	// Show "results" panel - This is really what people care about when looking at a finished race
     	tabHost.setCurrentTabByTag(ResultsTab.ResultsTabSpecName);
     	
 
 //		long race_ID = Long.parseLong(AppSettings.ReadValue(this, AppSettings.AppSetting_RaceID_Name, "-1"));
 //    	// Calculate Category Placing, Overall Placing, Points
 //    	Calculations.CalculateCategoryPlacings(this, race_ID);
 //    	Calculations.CalculateOverallPlacings(this, race_ID); 
 	}
 	
 	/**
 	 * Setup a race that could be running
 	 */
 	private void SetupAvailableRace(){
     	// Show the timer
     	timer.setVisibility(View.VISIBLE);
     	// Reset the timer
     	timer.resetTimer();
     	// Enable "check in" tab - Allow racers to check in
 		tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.VISIBLE);//.setEnabled(true);
     	// Enable "finish" tab - Nothing has even started yet!
     	tabHost.getTabWidget().getChildTabViewAt(3).setVisibility(View.VISIBLE);//.setEnabled(true);
     	// Show "race info" panel - Let the user know which race they are looking at
     	tabHost.setCurrentTabByTag(RaceInfoTab.RaceInfoTabSpecName);
 	}
 
 	/**
      * Add an action that the broadcast receiver should be listening for
      * @param action - The action to receive events for
      */
     protected void AddActionFilter(String action){
     	// Add the action to the intent filter
 		actionFilter.addAction(action);
     }
 	
 	/**
 	 * Sends an intent to the tabs to indicate which one needs to be shown
 	 * @param tabName - The name of the tab to show
 	 */
 	public void ShowTab(String tabName) {
 
     	tabHost.setCurrentTabByTag(tabName);  
 	}
 	
 	/**
      * Create and set up the action receiver for any intents that we're listening for
      */
 	private final BroadcastReceiver mActionReceiver = new BroadcastReceiver() {
 		/**
 		 * Receives and handles any intents that come in.
 		 * @param context - The context that the actions came from
 		 * @param intent - The intent that's being received
 		 */
         @Override
         public void onReceive(Context context, Intent intent) {
         	Log.v(LOG_TAG, "onReceive");
         	if(intent.getAction().equals(AddRaceView.RACE_ADDED_ACTION)){	// Set the raceID to the currently added race
         		AppSettings.Update(context, AppSettings.AppSetting_RaceID_Name, Long.toString(intent.getLongExtra(AppSettings.AppSetting_RaceID_Name, -1l)), true);
         		AppSettings.Update(context, AppSettings.AppSetting_StartInterval_Name, Long.toString(intent.getLongExtra(AppSettings.AppSetting_StartInterval_Name, 60l)), true);
         		SetupAvailableRace();
         	} else if(intent.getAction().equals(TTTimerTabsActivity.CHANGE_VISIBLE_TAB)){
         		String visibleTabTag = intent.getStringExtra(TTTimerTabsActivity.VISIBLE_TAB_TAG);
         		if(visibleTabTag == FinishTab.FinishTabSpecName && tabHost.getTabWidget().getChildTabViewAt(3).getVisibility() == View.GONE){
         			tabHost.getTabWidget().getChildTabViewAt(3).setVisibility(View.VISIBLE);
         		} else if(visibleTabTag == CheckInTab.CheckInTabSpecName && tabHost.getTabWidget().getChildTabViewAt(1).getVisibility() == View.GONE){
         			tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.VISIBLE);
         		}
         		tabHost.setCurrentTabByTag(visibleTabTag);
         	} else if(intent.getAction().equals(Timer.RACE_IS_FINISHED_ACTION)){
 				timer.CleanUpExtraUnassignedTimes();
 				SetupFinishedRace();
         	} else if(intent.getAction().equals(TTTimerTabsActivity.RACE_ID_CHANGED_ACTION)){
         		long raceID = Long.parseLong(intent.getStringExtra(RaceResults.Race_ID));
         		UpdatePreviousRaceState(raceID);
         	}
         }
     };
     
     @Override
     protected void onStop()
     {
     	try{
 	    	super.onStop();
     	}catch(Exception ex){
     		Log.e(LOG_TAG, "Unexpected error in onStop", ex);
     	}
         
     }	 
 	
 	/**
 	 * Figure out if there are any previous races in the DB
 	 * @return True if there are other races in the DB
 	 */
 	private boolean FindAnyPreviousRaces() {
 		boolean foundRace = false;
 		try{
 			String[] projection = new String[]{Race.getTableName() + "." + Race._ID + " as _id"};
 			String selection = RaceResults.EndTime + " IS NOT NULL";
 			String[] selectionArgs = null; 
 			String sortOrder = Race.getTableName() + "." + Race._ID;
 			
 			Cursor currentRace = getContentResolver().query(RaceInfoResultsView.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 			
 			if(currentRace.getCount() > 0){	
 				foundRace = true;
 			}
 			
 			currentRace.close();
 			currentRace = null;
      	}catch(Exception ex){Log.e(LOG_TAG, "FindAnyPreviousRaces failed", ex);}
 		
 		return foundRace;
 	}
 	
 	/**
 	 * Figure out if a race is in progress
 	 * @return True is a race is currently in progress, and false if no race is in progress
 	 */
 	private boolean FindRaceInProgress() {
 		boolean foundRaceInProgress = false;
 		try{
 			String[] projection = new String[]{Race.getTableName() + "." + Race._ID + " as _id", Race.RaceStartTime, Race.StartInterval};
 			String selection = Race.RaceStartTime + " > 0 and " + RaceResults.EndTime + " IS NULL";
 			String[] selectionArgs = null; 
 			String sortOrder = Race.getTableName() + "." + Race._ID;
 			
 			Cursor currentRace = getContentResolver().query(RaceInfoResultsView.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 			
 			if(currentRace.getCount() > 0){	
 				currentRace.moveToFirst();
 				int idIndex = currentRace.getColumnIndex(Race._ID);
 				Long raceID = currentRace.getLong(idIndex);
 				if(System.currentTimeMillis() < currentRace.getLong(currentRace.getColumnIndex(Race.RaceStartTime)) + 86400000)
 				{				
 					foundRaceInProgress = true;
 					
 					Long startInterval = currentRace.getLong(currentRace.getColumnIndex(Race.StartInterval));
 					AppSettings.Update(this, AppSettings.AppSetting_RaceID_Name, Long.toString(raceID), true);
 					AppSettings.Update(this, AppSettings.AppSetting_StartInterval_Name, Long.toString(startInterval), true);
 				}else {
 					// The race was started over 24 hours ago.  This probably isn't correct.  Just stop the race now.
 					do{
 						ContentValues content = new ContentValues();
 						content.put(RaceResults.EndTime, currentRace.getLong(currentRace.getColumnIndex(Race.RaceStartTime)) + 86400000);
 						content.put(RaceResults.ElapsedTime, System.currentTimeMillis() - currentRace.getLong(currentRace.getColumnIndex(Race.RaceStartTime)));
 				
 						// Update the race results
 						RaceResults.Update(this, content, RaceResults.Race_ID + "= ? and " + RaceResults.EndTime + " IS NULL", new String[]{Long.toString(raceID)});
 					}while(currentRace.moveToNext());
 				}
 			}
 			
 			currentRace.close();
 			currentRace = null;
      	}catch(Exception ex){Log.e(LOG_TAG, "FindRaceInProgress failed", ex);}
 		
 		return foundRaceInProgress;
 	}
 	
 	/**
 	 * Figure out if a race is available for check-in
 	 * @return True if a race is available for check in (but not started), False if no "available" race is found
 	 */
 	private boolean FindAvailableRace() {
 		boolean foundAvailableRace = false;
 		try{			
 			Calendar cal = Calendar.getInstance();
 			cal.setTimeInMillis(System.currentTimeMillis());
 			Long startOfDay = new Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0).getTime();
 			Long endOfDay = startOfDay + 86400000;
 			
 			String[] projection = new String[]{Race._ID, Race.StartInterval};
 			String selection = Race.RaceDate + ">=? AND " + Race.RaceDate + "<? AND " + Race.RaceStartTime + " IS NULL";
 			String[] selectionArgs = new String[]{Long.toString(startOfDay), Long.toString(endOfDay)}; 
 			String sortOrder = Race._ID + " DESC";
 			
 			Cursor currentRace = getContentResolver().query(Race.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 			
 			if(currentRace.getCount() > 0){	
 				foundAvailableRace = true;
 				currentRace.moveToFirst();
 				int raceCount = currentRace.getCount();
 				if(raceCount == 1){
 					int raceIDCol = currentRace.getColumnIndex(Race._ID);
 					long raceID = currentRace.getLong(raceIDCol);
 					AppSettings.Update(this, AppSettings.AppSetting_RaceID_Name, Long.toString(raceID), true);
 
 					Long startInterval = currentRace.getLong(currentRace.getColumnIndex(Race.StartInterval));
 					AppSettings.Update(this, AppSettings.AppSetting_StartInterval_Name, Long.toString(startInterval), true);
 				}else{
 					OtherRaceResults previousRaces = new OtherRaceResults();
 					FragmentManager fm = getSupportFragmentManager();
 					previousRaces.show(fm, OtherRaceResults.LOG_TAG);
 				}
 			}
 			
 			currentRace.close();
 			currentRace = null;
      	}catch(Exception ex){Log.e(LOG_TAG, "FindAvailableRace failed", ex);}
 		
 		return foundAvailableRace;
 	}
 
 	/**
 	 * Find a race that's finished on the current date
 	 * @return True if a race was finished, False if no finished races were found
 	 */
 	private boolean FindFinishedRace() {
 		boolean foundFinishedRace = false;
 		try{			
 			Calendar cal = Calendar.getInstance();
 			cal.setTimeInMillis(System.currentTimeMillis());
 			Long startOfDay = new Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0).getTime();
 			Long endOfDay = startOfDay + 86400000;
 			
 			String[] projection = new String[]{Race.getTableName() + "." + Race._ID};
 			String selection = Race.RaceDate + ">=? AND " + Race.RaceDate + "<? AND " + Race.RaceStartTime + ">0 AND " + RaceResults.EndTime + ">0";
 			String[] selectionArgs = new String[]{Long.toString(startOfDay), Long.toString(endOfDay)}; 
 			String sortOrder = Race.getTableName() + "." + Race._ID;
 			
 			Cursor finishedRaceResults = getContentResolver().query(RaceInfoResultsView.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 			
 			if(finishedRaceResults.getCount() > 0){	
 				foundFinishedRace = true;
 			}
 			
 			finishedRaceResults.close();
 			finishedRaceResults = null;
      	}catch(Exception ex){Log.e(LOG_TAG, "FindFinishedRace failed", ex);}
 		
 		return foundFinishedRace;
 	}
 	
 	/**
 	 * Set the raceID in AppSettings to the race that's currently in progress
 	 */
 	private void UpdateFromRaceInProgress() {
 		try{
 			String[] projection = new String[]{Race.getTableName() + "." + Race._ID + " as " + Race._ID, Race.RaceStartTime};
 			String selection = Race.RaceStartTime + " > 0 and " + RaceResults.EndTime + " IS NULL";
 			String[] selectionArgs = null; 
 			String sortOrder = Race.getTableName() + "." + Race._ID;
 			
 			Cursor currentRace = getContentResolver().query(RaceInfoResultsView.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 			
 			currentRace.moveToFirst();
 			
 			int race_IDCol = currentRace.getColumnIndex(Race._ID);
 			long race_ID = currentRace.getLong(race_IDCol);
 			AppSettings.Update(this, AppSettings.AppSetting_RaceID_Name, Long.toString(race_ID), true);
 			
 			UpdateFromRaceInProgress(currentRace);
 			
 		 	currentRace.close();
 		 	currentRace = null;
      	}catch(Exception ex){Log.e(LOG_TAG, "UpdateFromRaceInProgress failed", ex);}
 	}
 	
 	private void UpdateFromRaceInProgress(Cursor theRace) {
 		try{
 	    	// Show all tabs
 	    	tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.VISIBLE);//.setEnabled(false);
 	    	tabHost.getTabWidget().getChildTabViewAt(3).setVisibility(View.VISIBLE);//.setEnabled(false);
 			
 			theRace.moveToFirst();
 			
 			int race_IDCol = theRace.getColumnIndex(Race._ID);
 			long race_ID = theRace.getLong(race_IDCol);
 			AppSettings.Update(this, AppSettings.AppSetting_RaceID_Name, Long.toString(race_ID), true);
 				
 			long startTime = theRace.getLong(theRace.getColumnIndex(Race.RaceStartTime));
 			
 			// Start the timer (using the race's start time)
 			Intent startTimer = new Intent();
 		 	startTimer.setAction(Timer.START_TIMER_ACTION);
 		 	startTimer.putExtra(Timer.START_TIME, startTime);
 		 	sendBroadcast(startTimer);
 		 	
 		 	// Figure out how many racers are in this race
 		 	String[] projection = new String[]{RaceResults._ID};
 		 	String selection = RaceResults.Race_ID + " = " + race_ID;
 			String[] selectionArgs = null; 
 			String sortOrder = RaceResults._ID;
 			Cursor totalRacers = getContentResolver().query(RaceResults.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 			
 			selection = RaceResults.Race_ID + " = " + AppSettings.getParameterSql(AppSettings.AppSetting_RaceID_Name)+ " and " + RaceResults.StartTime + " IS NOT NULL";
 			Cursor startedRacers = getContentResolver().query(RaceResults.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 			
 			if(startedRacers.getCount() < totalRacers.getCount()) {
 				// Navigate to the start tab if not all racers have been started
 				ShowTab(StartTab.StartTabSpecName);
 			}else{
 				// Else, navigate to the finish tab
 				ShowTab(FinishTab.FinishTabSpecName);
 			}
 			
 			totalRacers.close();
 			totalRacers = null;
 			
 			startedRacers.close();
 			startedRacers = null;
      	}catch(Exception ex){Log.e(LOG_TAG, "UpdateFromRaceInProgress failed", ex);}
 	}
 
 //	private void SetupInProgressRace(long race_ID, Long raceStartTime) {
 //		try{
 //			if( raceStartTime == null){
 //				String[] projection = new String[]{Race.RaceStartTime};
 //				String selection = Race._ID + " =?";
 //				String[] selectionArgs = new String[]{Long.toString(race_ID)}; 
 //				String sortOrder = Race.RaceStartTime;
 //				
 //				Cursor currentRace = getContentResolver().query(Race.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 //				
 //				currentRace.moveToFirst();
 //				
 //				int raceStartTimeCol = currentRace.getColumnIndex(Race.RaceStartTime);
 //				raceStartTime = currentRace.getLong(raceStartTimeCol);
 //				
 //			 	currentRace.close();
 //			 	currentRace = null;
 //			}
 //			// set the startTime variable to raceStartTime and get the timer running
 //			Intent startTimer = new Intent();
 //		 	startTimer.setAction(Timer.START_TIMER_ACTION);
 //		 	startTimer.putExtra(Timer.START_TIME, raceStartTime);
 //		 	sendBroadcast(startTimer);	 	
 //		 	
 //			// if not all racers have been started yet (current time > startTime + greatest startTimeOffset), go to the start page
 //		 	String[] projection = new String[]{"MAX(" + RaceResults.StartTimeOffset + ") as " + RaceResults.StartTimeOffset};
 //			String selection = RaceResults.Race_ID + "=?";
 //			String[] selectionArgs = new String[]{Long.toString(race_ID)};
 //			String sortOrder = RaceResults.StartTimeOffset + " DESC";
 //			
 //		 	Cursor lastRaceResult = getContentResolver().query(RaceResults.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 //		 	lastRaceResult.moveToFirst();
 //		 	long lastStartTimeOffset = lastRaceResult.getLong(lastRaceResult.getColumnIndex(RaceResults.StartTimeOffset));
 //		 	if(System.currentTimeMillis() < raceStartTime + lastStartTimeOffset){
 //			    tabHost.setCurrentTabByTag(StartTab.StartTabSpecName);
 //		 	} else {
 //		 		// if all racers have already started, go to the finish screen
 //		 		tabHost.setCurrentTabByTag(FinishTab.FinishTabSpecName);
 //		 	}
 //		 	lastRaceResult.close();
 //		 	lastRaceResult = null;
 //
 //     	}catch(Exception ex){Log.e(LOG_TAG, "UpdateFromRaceInProgress failed", ex);}
 //	}
 
 //	private void SetupView(long race_ID) {
 //		// Figure out if a race is currently going on.  
 //        if(RaceInProgress(race_ID)) {
 //        	// Since races are started and tracked only locally, if a race is still going, it was started on this device
 //	        // If the race already has a raceStartTime, and not all racers are finished, set the startTime variable to raceStartTime and get the timer running
 //        	SetupInProgressRace(race_ID, null);
 //        } else if (RaceNotStarted(race_ID)){
 //        	// Show the check in tab
 //        	tabHost.setCurrentTabByTag(CheckInTab.CheckInTabSpecName);
 //        } else if (RaceFinished(race_ID)) {
 //        	SetupFinishedRace();
 //        }
 //	}
 
 //	private boolean RaceFinished(long race_ID) {
 //		boolean foundFinishedRace = false;
 //		try{			
 //			if(race_ID > 0){
 //				Calendar cal = Calendar.getInstance();
 //				cal.setTimeInMillis(System.currentTimeMillis());
 //				Long startOfDay = new Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0).getTime();
 //				Long endOfDay = startOfDay + 86400000;
 //				
 //				String[] projection = new String[]{Race.getTableName() + "." + Race._ID};
 //				String selection = Race.RaceDate + ">=? AND " + Race.RaceDate + "<? AND " + Race.RaceStartTime + " >0 AND " + RaceResults.EndTime + " IS NULL AND " + Race.getTableName() + "." + Race._ID + "=?";
 //				String[] selectionArgs = new String[]{Long.toString(startOfDay), Long.toString(endOfDay), Long.toString(race_ID)}; 
 //				String sortOrder = Race.getTableName() + "." + Race._ID;
 //				
 //				Cursor finishedRaceResults = getContentResolver().query(RaceInfoResultsView.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 //				
 //				if(finishedRaceResults.getCount() <= 0){	
 //					foundFinishedRace = true;
 //				}
 //				
 //				finishedRaceResults.close();
 //				finishedRaceResults = null;
 //			}
 //     	}catch(Exception ex){Log.e(LOG_TAG, "RaceFinished failed:", ex);}
 //		
 //		return foundFinishedRace;
 //	}
 //
 //	private boolean RaceNotStarted(long race_ID) {
 //		boolean foundAvailableRace = false;
 //		try{			
 //			Calendar cal = Calendar.getInstance();
 //			cal.setTimeInMillis(System.currentTimeMillis());
 //			Long startOfDay = new Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0).getTime();
 //			Long endOfDay = startOfDay + 86400000;
 //			
 //			String[] projection = new String[]{Race.getTableName() + "." + Race._ID};
 //			String selection = Race.RaceDate + ">=? AND " + Race.RaceDate + "<? AND " + Race.RaceStartTime + " IS NULL AND " + Race.getTableName() + "." + Race._ID + "=?";
 //			String[] selectionArgs = new String[]{Long.toString(startOfDay), Long.toString(endOfDay), Long.toString(race_ID)}; 
 //			String sortOrder = Race.getTableName() + "." + Race._ID;
 //			
 //			Cursor currentRace = getContentResolver().query(Race.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
 //			
 //			if(currentRace.getCount() > 0){	
 //				foundAvailableRace = true;
 //			}
 //			
 //			currentRace.close();
 //			currentRace = null;
 //     	}catch(Exception ex){Log.e(LOG_TAG, "RaceNotStarted failed:", ex);}
 //		
 //		return foundAvailableRace;
 //	}
 }
