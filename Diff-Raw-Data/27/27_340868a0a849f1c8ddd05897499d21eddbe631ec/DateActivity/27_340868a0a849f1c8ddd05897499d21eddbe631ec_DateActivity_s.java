 package com.alvarosantisteban.berlincurator;
 
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 import com.j256.ormlite.stmt.DeleteBuilder;
 import com.j256.ormlite.stmt.SelectArg;
 
 import android.app.ActionBar;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.ExpandableListView.OnGroupClickListener;
 import android.widget.ExpandableListView.OnGroupCollapseListener;
 import android.widget.ExpandableListView.OnGroupExpandListener;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Displays a list with the events for a concrete day organized by the origin of the website and the time.
  * 
  * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
  *
  */
 public class DateActivity extends OrmLiteBaseActivity<DatabaseHelper>{
 
 	// Event's thema tags
 	public final static String ART_THEMA_TAG = "Art";
 	public final static String POLITICAL_THEMA_TAG = "Political";
 	public final static String GOING_OUT_THEMA_TAG = "Going out";
 	
 	// Event's type tags
 	public final static String CONCERT_TYPE_TAG = "Concert";
 	public final static String PARTY_TYPE_TAG = "Party";
 	public final static String EXHIBITION_TYPE_TAG = "Exhibition";
 	public final static String TALK_TYPE_TAG = "Talk";
 	public final static String SCREENING_TYPE_TAG = "Screening";
 	public final static String OTHER_TYPE_TAG = "Other";
 	
 	public final static String ART_EVENTS = "Art events (Exhibitions, Talks, etc)";
 	public final static String CONCERTS = "Concerts (Rock, Metal and Punk)";
 	public final static String PARTIES = "Parties";
 	public final static String POLITICAL_EVENTS = "Political events";
 	public final static String OTHER_EVENTS = "Other (Screenings, Exhibitions, Talks, etc)";
 	
 	public static final String EXTRA_EVENT = "com.alvarosantisteban.berlincurator.event";
 	// Settings
 	private static final int RESULT_SETTINGS = 1;
 	
 	// Code return constants used to get info from Event Activity 
 	private static final int INTENT_RETURN_CODE = 1;
 	public static final int RESULT_UPDATE = 1;
 	public static final String EVENTS_RESULT_DATA = "result data";
 	
 	public String[] lastSelection;
 	
 	private static Toast toast;
 			
 	/**
 	 * Used for logging purposes
 	 */
 	String tag = "DateActivity";
 	
 	/**
 	 * Format of the date
 	 */
 	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
 	
 	/**
 	 * The calendar used to get the actual date
 	 */
 	Calendar currentDay = Calendar.getInstance();
 	
 	/**
 	 * The String with the date of today in the format DD/MM/YYYY
 	 */
 	private String today = dateFormat.format(currentDay.getTime());
 	
 	/**
 	 * The TextView displaying the selected date. It may also display the words "Today" or "Tomorrow"
 	 */
 	private TextView displayedDate;
 	
 	/**
 	 * The String with the choosenDate by the user in the format DD/MM/YYYY
 	 */
 	public String choosenDate;
 	
 	/**
 	 * The total number of events for a choosenDate
 	 */
 	int numEvents = 0;
 	
 	/**
 	 * The expandable list with the groups and events
 	 */
 	ExpandableListView expandableSitesList;
 	
 	/**
 	 * A LinkedHashMap with the name of the website as key and its corresponding HeaderInfo as value. 
 	 * Used to make the search easier
 	 */
 	private LinkedHashMap<String, HeaderInfo> websitesMap = new LinkedHashMap<String, HeaderInfo>();
 	/**
 	 * An ArrayList with the HeaderInfo of each website
 	 */
 	private ArrayList<HeaderInfo> websitesList = new ArrayList<HeaderInfo>();
 	 
 	/**
 	 * The adapter for the ExpandableListView
 	 */
 	private ListAdapter listAdapter;
 	
 	/**
 	 * The Database Helper that helps dealing with the db easily
 	 */
 	//private DatabaseHelper databaseHelper = null;
 	
 	final Context context = this;
 	
 	/**
    	 * The progress bar for downloading and extracting the events
    	 */
 	ProgressBar loadProgressBar;
 	
 	/**
 	 *  User preferences
 	 */
 	SharedPreferences sharedPref;
 		
 	/**
 	 * The position of the last touched group
 	 */
 	int lastTouchedGroupPosition;
 	/**
 	 * The position of the last child (event) touched inside  the lastTouchedGroupPosition
 	 */
 	int lastTouchedChildPosition;
 	
 	/**
 	 *  Loads the elements from the resources, gets the data from the mainActitivy and calls the parsers to extract the information that
 	 *  will be shown.
 	 *  
 	 * @param savedInstanceState
 	 */
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		System.out.println(tag +" onCreate-----------------");
 		
 		toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
 		
 		// Get the default shared preferences
 		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		// --------------------------------------------------
 		// Know if its the first time the user uses the app
 		// --------------------------------------------------
 		boolean isFirstTimeApp = sharedPref.getBoolean("isFirstTimeApp", true);
 		lastSelection = sharedPref.getStringSet("lastSelection", new HashSet<String>(Arrays.asList(FirstTimeActivity.websNames))).toArray(new String[0]);
 		
 		Intent intent;
 		//isFirstTimeApp = true;
 		if (isFirstTimeApp) {
 			// Go directly to First Time Activity
 			intent = new Intent(context, FirstTimeActivity.class);
 			startActivity(intent);
 		}
 		
 		// --------------------------------------------------
 		// Set basic UI
 		// --------------------------------------------------
 		
 		setContentView(R.layout.activity_date);
 		expandableSitesList = (ExpandableListView) findViewById(R.id.expandableSitesList);
 		loadProgressBar = (ProgressBar)findViewById(R.id.progressLoadHtml);	
 		
 		// Get the sites are meant to be shown
 		Set<String> set = sharedPref.getStringSet(SettingsFragment.KEY_PREF_MULTILIST_SITES, new HashSet<String>(Arrays.asList(FirstTimeActivity.websNames)));
 		FirstTimeActivity.websNames = set.toArray(new String[0]);
 		createHeaderGroups(FirstTimeActivity.websNames);	
 	    
 	    // --------------------------------------------------
 	 	// Display the right date
 	    // --------------------------------------------------
 		
 		intent = getIntent();
 		//eventsList = intent.getExtra(MainActivity.EXTRA_HTML);
 		// Get the choosen date from the calendar or from the event that the user was consulting
 		choosenDate = intent.getStringExtra(CalendarActivity.EXTRA_DATE);
 		choosenDate = intent.getStringExtra(EventActivity.EXTRA_DATE);
 		displayedDate = (TextView) findViewById(R.id.date);
 		if (choosenDate == null){	
 			// The user did not select anything, the default date is today
 			displayedDate.setText(R.string.events_for_today);
 			choosenDate = today;
 		}else{
 			if (choosenDate.equals(today)){
 				displayedDate.setText(R.string.events_for_today);
 			}else if (choosenDate.equals(getTomorrow())){
 				displayedDate.setText(R.string.events_for_tomorrow);
 			}else{
 				displayedDate.setText(R.string.events_for_a);
 				displayedDate.append(" " +choosenDate);
 			}
 		}	
 		
 		
 		//databaseHelper = getHelper();
 		
 		// Enable the app's icon to act as home
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		
 		// --------------------------------------------------
 		// Adapter and loading of events
 		// --------------------------------------------------
 		
 		// Create the adapter by passing the ArrayList data
 		listAdapter = new ListAdapter(DateActivity.this, websitesList);
 		// Attach the adapter to the expandableList
 		expandableSitesList.setAdapter(listAdapter);
 		
 		// Load the events for the selected websites
 		loadEvents();
 		
 		// Expand the groups with events
 		expandGroupsWithEvents();
 		
 		// Listener for the events
 		expandableSitesList.setOnChildClickListener(myEventClicked);
 		// Listener for the sites
 		expandableSitesList.setOnGroupClickListener(myListGroupClicked);
 		// Listener for the collapsed group
 		//expandableSitesList.setOnGroupCollapseListener(myCollapsedGroup);
 		// Listener for the expanded group
 		//expandableSitesList.setOnGroupExpandListener(myExpandedGroup);
 		
 		if(numEvents == 0){
 			displayToast("There are no events for this day. Refresh or go to another day.");
 		}
 		/*
 		System.out.println("----- NEW SELECTION ----- ");
 		for (int i=0; i<FirstTimeActivity.websNames.length; i++){
 			System.out.println(FirstTimeActivity.websNames[i]);
 		}
 		System.out.println("----- LAST SELECTION ----- ");
 		for (int i=0; i<lastSelection.length; i++){
 			System.out.println(lastSelection[i]);
 		}
 		*/
 		if(!Arrays.equals(FirstTimeActivity.websNames, lastSelection)){
 			checkDifferencesBetweenSelection(FirstTimeActivity.websNames, lastSelection);
 			lastSelection = FirstTimeActivity.websNames;
 			Editor editor = sharedPref.edit();
             editor.putStringSet("lastSelection", new HashSet<String>(Arrays.asList(FirstTimeActivity.websNames)));
             editor.commit();
 		}
 	}
 
 	/**
 	 * Checks the differences between two string arrays to determinate which groups have elements that have to be deleted and which groups
 	 * have events that have to be downloaded.
 	 * 
 	 * @param newSelection the new selection of the user
 	 * @param oldSelection the old selection of the user
 	 */
 	private void checkDifferencesBetweenSelection(String[] newSelection, String[] oldSelection) {
 		System.out.println("checkDifferencesBetweenSelection");
 		// Check the added groups
 		List<String> added = new ArrayList<String>();
 		for(int i=0; i<newSelection.length;i++){
 			for(int j=0; j<oldSelection.length;j++){
 				if(newSelection[i].equals(oldSelection[j])){
 					j = oldSelection.length;
 				}else{
 					if(j == oldSelection.length-1){
 						added.add(newSelection[i]);
 					}
 				}
 			}
 		}
 		
 		// Download the events of the new added thema/type, if applies
 		System.out.println("New added groups:"+added.size());
 		if(added.size() > 0){
 			// Create a connection
 			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 			// Check if is possible to establish a connection
 			if (networkInfo != null && networkInfo.isConnected()) {
 				DownloadWebpageTask2 download = new DownloadWebpageTask2();
 				// Execute the asyncronous task of downloading the websites
 				download.execute(added.toArray(new String[added.size()]));
 			}
 		}
 		
 		// Check the deleted groups
 		List<String> deleted = new ArrayList<String>();
 		for(int i=0; i<oldSelection.length;i++){
 			for(int j=0; j<newSelection.length;j++){
 				if(oldSelection[i].equals(newSelection[j])){
 					j = newSelection.length;
 				}else{
 					if(j == newSelection.length-1){
 						deleted.add(oldSelection[i]);
 					}
 				}
 			}
 		}
 		
 		// Remove from the database the old ones, if applies
 		System.out.println("Deleted groups:"+deleted.size());
 		if (deleted.size() > 0){
 			RuntimeExceptionDao<Event, Integer> eventDao = getHelper().getEventDataDao();
 			DeleteBuilder<Event, Integer> deleteBuilder = eventDao.deleteBuilder();	
 			for (int i=0; i<deleted.size(); i++){
 				try {
 					System.out.println("eventsOrigin:"+deleted.get(i));
 					// create our argument which uses a SQL ? to avoid having problems with apostrophes
 					SelectArg deletedArg = new SelectArg(deleted.get(i));
 					deleteBuilder.where().eq("eventsOrigin", deletedArg);
 					deleteBuilder.delete();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Returns the date of tomorrow formated like DD/MM/YYYY
 	 * 
 	 * @return a String with tomorrow in the format DD/MM/YYYY
 	 */
 	private String getTomorrow() {
 		Calendar tomorrow = currentDay;
 		// add one day to the date/calendar
 	    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
 		return dateFormat.format(tomorrow.getTime());
 	}
 
 	/**
 	 * Expand all groups
 	 */
 	private void expandAll() {
 		int count = listAdapter.getGroupCount();
 		for (int i = 0; i < count; i++){
 			expandableSitesList.expandGroup(i);
 		}
 	}
 	
 	/**
 	 * Collapse all groups
 	 */
 	private void collapseAll() {
 		int count = listAdapter.getGroupCount();
 		for (int i = 0; i < count; i++){
 			expandableSitesList.collapseGroup(i);
 		}
 	}
 	
 	/**
 	 * Expand the groups with events on it
 	 */
 	private void expandGroupsWithEvents(){
 		int count = listAdapter.getGroupCount();
 		for (int i = 0; i < count; i++){
 			if(listAdapter.getChildrenCount(i) > 0){
 				expandableSitesList.expandGroup(i);
 			}
 			/*else{
 				expandableSitesList.setGroupIndicator(null);
 			}*/
 		}
 	}
 	
 	/**
 	 * Loads the events from the selected websites into the list if the day is the right one
 	 */
 	private void loadEvents(){ 
 		System.out.println("LOAD EVENTS");
 		System.out.println("-----------------------------");
 		// Get our dao
 		RuntimeExceptionDao<Event, Integer> eventDao = getHelper().getEventDataDao();
 		for (int i=0; i<FirstTimeActivity.websNames.length; i++){
 			System.out.println(FirstTimeActivity.websNames[i]);
 			List<Event> eventsFromWebsite = null;
 			try {
 				Map<String, Object> fieldValues = new HashMap<String,Object>();
 				fieldValues.put("eventsOrigin", FirstTimeActivity.websNames[i]);
 				fieldValues.put("day", choosenDate);
 				eventsFromWebsite = eventDao.queryForFieldValuesArgs(fieldValues);
 			//} catch (SQLException e) {
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.out.println("DB exception while retrieving the events from the website " +FirstTimeActivity.websNames[i]);
 			}
 			for (int j = 0; j < eventsFromWebsite.size(); j++) {
 				addEvent(FirstTimeActivity.websNames[i],eventsFromWebsite.get(j));
 			}
 		}
 	}
 	
 	/**
 	 * Add an event to its corresponding group (site where it comes from)
 	 * 
 	 * @param websiteName String with the name of the website from where the event comes from
 	 * @param newEvent The event to be attached
 	 * @return the position of group where the event was added
 	 */
 	private int addEvent(String websiteName, Event newEvent){
 		int groupPosition = 0;
 	   
 		// Check in the hash map if the group already exists
 		HeaderInfo headerInfo = websitesMap.get(websiteName);
 		// Add the group if doesn't exists
 		if(headerInfo == null){
 			headerInfo = new HeaderInfo();
 			headerInfo.setName(websiteName);
 			websitesMap.put(websiteName, headerInfo);
 			websitesList.add(headerInfo);
 		}
 	 
 		// Get the children (events) for the group
 		ArrayList<Event> eventsList = headerInfo.getEventsList();
 		// Get the size of the children list
 		int listSize = eventsList.size();
 		// Add to the counters
 		listSize++;
 		numEvents++;
 	 
 		// Set the sequence for the event
 		newEvent.setSequence(String.valueOf(listSize));
 		// Add it to its list of events
 		eventsList.add(newEvent);
 		// Update the site with the "new" eventsList
 		headerInfo.setEventsList(eventsList);
 		headerInfo.setEventsNumber(listSize);
 		
 		 
 		//find the group position inside the list
 		groupPosition = websitesList.indexOf(headerInfo);
 		return groupPosition;
 	}
 	
 	/**
 	 * Populates the {@link websites} and the {@link websitesList} by creating {@link HeaderInfo} using the array of names of sites passed
 	 * in the parameter.
 	 * 
 	 * @param sitesNames the names of the sites which contain the events
 	 */
 	private void createHeaderGroups(String[] sitesNames) {
 		//System.out.println("createHeaderGroups");
 		for (int i=0; i<sitesNames.length; i++){
 			HeaderInfo headerInfo = new HeaderInfo();
 			headerInfo.setName(sitesNames[i]);
 			websitesMap.put(sitesNames[i], headerInfo);
 			websitesList.add(headerInfo);
 		}
 	}
 	
 	/**
 	 * The child listener for the events
 	 */
 	private OnChildClickListener myEventClicked =  new OnChildClickListener() {
 		 
 		  public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
 	    
 			  // Get the group header 
 			  HeaderInfo headerInfo = websitesList.get(groupPosition);
 			  // Get the child info
 			  Event clickedEvent =  headerInfo.getEventsList().get(childPosition);
 			  // Update the last touched group and child position
 			  lastTouchedGroupPosition = groupPosition;
 			  lastTouchedChildPosition = childPosition;
 			  // Go the Event Activity
 			  Intent intent = new Intent(context, EventActivity.class);
 			  intent.putExtra(EXTRA_EVENT, clickedEvent);
 			  startActivityForResult(intent, INTENT_RETURN_CODE);
 			  return false;
 		  }
 	};
 	
 	/**
 	 * The group listener for the sites
 	 */
 	private OnGroupClickListener myListGroupClicked =  new OnGroupClickListener() {
 		 
 		  public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
 			  System.out.println("onGroupClick");
 			  // Get the group header
 			  HeaderInfo headerInfo = websitesList.get(groupPosition);
 			  // If the group does not contain events, tell the user
 			  if(headerInfo.getEventsNumber() == 0){
 				  displayToast("There are no events to show for " + headerInfo.getName());
 				  //Toast toast = Toast.makeText(getBaseContext(), "There are no events to show for " + headerInfo.getName(), Toast.LENGTH_SHORT);
 				  //toast.setGravity(Gravity.TOP, 0, FirstTimeActivity.actionBarHeight);
 				  //toast.show();
 				  // Avoid propagation = the group is not expanded/collapsed
 				  return true;
 			  }
 			  return false;
 		  }
 	   
 	};
 	
 	/**
 	 * The group collapse listener 
 	 */
 	private OnGroupCollapseListener myCollapsedGroup = new OnGroupCollapseListener(){
 		
 		public void onGroupCollapse(int groupPosition){
 			System.out.println("onGroupCollapse");
 		}
 	};
 	
 	/**
 	 * The group expand listener
 	 */
 	private OnGroupExpandListener myExpandedGroup = new OnGroupExpandListener(){
 		
 		public void onGroupExpand(int groupPosition){
 			System.out.println("onGroupExpand");
 		}
 	};
 	
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.date, menu);
 		return true;
 	}
 	
 	/**
 	 * Checks which item from the menu has been clicked
 	 */
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.menu_calendar) {
 			Intent i2 = new Intent(this, CalendarActivity.class);
 			startActivity(i2);
 		}else if (item.getItemId() == R.id.menu_refresh_events) {
 			// Create a connection
 			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		    // Check if is possible to establish a connection
 		    if (networkInfo != null && networkInfo.isConnected()) {
 				DownloadWebpageTask2 download = new DownloadWebpageTask2();
 				// Execute the asyncronous task of downloading the websites
 				download.execute(FirstTimeActivity.websNames);
 		    } else {
 		    	// Inform the user that there is no network connection available
 		    	displayToast(getString(R.string.no_network));
 		        System.out.println(R.string.no_network);
 		    }
 		}else if (item.getItemId() == R.id.menu_settings) {
 			Intent i = new Intent(this, SettingsActivity.class);
 			startActivityForResult(i, RESULT_SETTINGS);
 		}
 		/*else if (item.getItemId() == android.R.id.home) {
 			// app icon in action bar clicked; go home
 			Intent intent = new Intent(this, MainActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 		}*/
         return true;
     }
 	
 	public void onStart() {
 		super.onStart();
 		System.out.println(tag +"In the onStart() event");
 	}		   
 
 	public void onRestart() {
 		super.onRestart();
 	    System.out.println(tag + "In the onRestart() event");
 	}
 	    
 	public void onResume() {
 		super.onResume();
 	    System.out.println(tag +"In the onResume() event");
 	}
 	    
 	public void onPause() {
 	    super.onPause();
 	    System.out.println(tag + "In the onPause() event");
 	}
 	    
 	public void onStop() {
 	    super.onStop();
 	    System.out.println(tag + "In the onStop() event");
 	}
 	    
 	public void onDestroy() {
 	    super.onDestroy();
 	    System.out.println(tag + "In the onDestroy() event");
 	    /*
 	    if (databaseHelper != null) {
 	        OpenHelperManager.releaseHelper();
 	        databaseHelper = null;
 	    }*/
 	}
 	
 	/**
 	 * Called when an activity you launched exits, giving you the requestCode you started it with, 
 	 * the resultCode it returned, and any additional data from it. 
 	 * The resultCode will be RESULT_CANCELED if the activity explicitly returned that, didn't return any result, 
 	 * or crashed during its operation.
 	 * 
 	 * You will receive this call immediately before onResume() when your activity is re-starting.
 	 * 
 	 * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
 	 * @param resultCode The integer result code returned by the child activity through its setResult().
 	 * @param data 	An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
 	 */
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 	    System.out.println(tag + "In the onActivityResult() event");
 		if (requestCode == INTENT_RETURN_CODE) {
 			if(resultCode == RESULT_UPDATE){      
 		        // The event was updated
 				Event changedEvent = (Event)data.getSerializableExtra(EVENTS_RESULT_DATA);
 				replaceAnEvent(changedEvent);
 				listAdapter.notifyDataSetChanged();
 		    }
 		}
 	}
 	
 	/**
 	 * Replaces the event pointed by lastTouchedGroupPosition and lastTouchedChildPosition for the one received as parameter
 	 * 
 	 * @param changedEvent the event that will be set in the websitesList
 	 */
 	private void replaceAnEvent(Event changedEvent) {		
 		// Get the group header of the touched event
 		HeaderInfo headerInfo = websitesList.get(lastTouchedGroupPosition);
 		// Substitute the old event for the new one
 		headerInfo.getEventsList().set(lastTouchedChildPosition, changedEvent);
 		websitesList.set(lastTouchedGroupPosition, headerInfo);
 	}
 
 	/*
 	private DatabaseHelper getHelper() {
 	    if (databaseHelper == null) {
 	        databaseHelper =
 	            OpenHelperManager.getHelper(this, DatabaseHelper.class);
 	    }
 	    return databaseHelper;
 	}*/
 	
 	private void displayToast(final String message) {
 	    //toast.cancel();
 	    toast.setText(message); 
 	    toast.setGravity(Gravity.TOP, 0, FirstTimeActivity.actionBarHeight);
 	    toast.show();
 	}
 	
 	/** 
      * Uses AsyncTask to create a task away from the main UI thread. 
      * This task takes the creates several HttpUrlConnection to download the html from different websites. 
      * Afterwards, the several lists of Events are created and the execution goes to the Date Activity.
     */
 	public class DownloadWebpageTask2 extends AsyncTask<String, String, Integer> {
 		
 		/**
 		 * Makes the progressBar visible
 		 */
 		protected void onPreExecute(){
 	    	System.out.println("onPreExecute");
 	    	loadProgressBar.setVisibility(View.VISIBLE);
 	    	// Inform the user
 	    	publishProgress("Start", "");
 		}
 		
 		/**
 		 * Downloads the htmls and creates the lists of Events. 
 		 * Detects any possible problem during the download of the website or the extraction of events.
 		 * @return 
 		 * 
 		 */
 		protected Integer doInBackground(String... urls) { 
 			int numOfNewEvents = 0;
 			// Load the events from the selected websites
 			for (int i=0; i<urls.length; i++){
 				List<Event> eventsFromAWebsite = null;
 				if (urls[i].equals("I Heart Berlin")){
 					System.out.println("Ihearberlin dentro");
 					eventsFromAWebsite = EventLoaderFactory.newIHeartBerlinEventLoader().load(context);
 				}else if(urls[i].equals("Berlin Art Parasites")){
 					System.out.println("artParasites dentro");
 					eventsFromAWebsite = EventLoaderFactory.newArtParasitesEventLoader().load(context);
 				}else if(urls[i].equals("Metal Concerts")){
 					System.out.println("metalConcerts dentro");
 					eventsFromAWebsite = EventLoaderFactory.newMetalConcertsEventLoader().load(context);
 				}else if(urls[i].equals("White Trash")){
 					System.out.println("whitetrash dentro");
 					eventsFromAWebsite = EventLoaderFactory.newWhiteTrashEventLoader().load(context);
 				}else if(urls[i].equals("Kpi's events")){
 					System.out.println("koepi dentro");
 					eventsFromAWebsite = EventLoaderFactory.newKoepiEventLoader().load(context);
 				}else if(urls[i].equals("Goth Datum")){
 					System.out.println("goth dentro");
 					eventsFromAWebsite = EventLoaderFactory.newGothDatumEventLoader().load(context);
 				}else if(urls[i].equals("Stress Faktor")){
 					System.out.println("Stresssssss faktor dentro");
 					eventsFromAWebsite = EventLoaderFactory.newStressFaktorEventLoader().load(context);
 				}else if(urls[i].equals("Index")){
 					System.out.println("Index dentro");
 					eventsFromAWebsite = EventLoaderFactory.newIndexEventLoader().load(context);
 				}else{
 					return null;
 				}
 				// If there was a problem loading the events we tell the user
 				if (eventsFromAWebsite == null){
 					// Distinguish the situation where the event is null because the Berlin Art Parasites website is not checked
 					if(!(urls[i].equals("Berlin Art Parasites") && !ArtParasitesEventLoader.isBerlinWeekend())){
 						System.out.println("Event is null");
 						publishProgress("Exception", urls[i]);	
 					}
 				}else{
 					// Add the events from this website to the DB
 					for (int j = 0; j < eventsFromAWebsite.size(); j++) {
 						if(addEventToDB(eventsFromAWebsite.get(j))){
 							numOfNewEvents++;
 						}
 					}
 				}
 			}
 			// Inform the user
 			publishProgress("Finish",Integer.valueOf(numOfNewEvents).toString());
 			return numOfNewEvents;
 		} 
 		
 		/**
 		 * Adds an event to the DB if it does not already exist.
 		 * 
 		 * @param event The event to be added
 		 * @return true if the event was added, false otherwise.
 		 */
 		private boolean addEventToDB(Event event) {
 			// Get our dao
 			RuntimeExceptionDao<Event, Integer> eventDao = getHelper().getEventDataDao();
 			// Check if the event already exists in the DB
 			if(!isEventInDB(eventDao, event)){
 				// Store the event in the database
 				eventDao.create(event);
 				return true;
 			}
 			return false;
 		}
 
 		/**
 		 * Checks if an event is already in the DB. To determinate it, checks the name, day and description of an event.
 		 * 
 		 * @param eventDao Our DAO for the Events table
 		 * @param event The event to be checked its presence in the DB
 		 * @return true if the event is already in the DB
 		 */
 		private boolean isEventInDB(RuntimeExceptionDao<Event, Integer> eventDao, Event event) {
 			Map<String, Object> fieldValues = new HashMap<String,Object>();
 			fieldValues.put("name", event.getName());
 			fieldValues.put("day", event.getDay());
 			fieldValues.put("description", event.getDescription());
 			List<Event> foundEvents = eventDao.queryForFieldValuesArgs(fieldValues);
 			/*
 			 * for (int i=0; i<foundEvents.size();i++){
 				System.out.println(foundEvents.get(i).getName());
 			}
 			*/
 			if(foundEvents.size() >= 1){
 				System.out.println("El evento ya existe, no se aade.");
 				
 				return true;
 			}
 			return false;
 		}
 
 		/**
 		 * Informs the user of the state of the process of events download.
 		 */
 		protected void onProgressUpdate(String... progress) {
     		System.out.println("Estoy en onProgressUpdate:"+progress[0]);
     		if (progress[0].equals("Start")){
     			displayToast(getString(R.string.searching));
     		}else if (progress[0].equals("Exception")){
     			displayToast("There were problems downloading the content from: " +progress[1] +" It's events won't be displayed.");
     		}else if (progress[0].equals("Finish")){
     			if (Integer.parseInt(progress[1]) > 0){
     				displayToast("Added " +progress[1] +" new events");
     			}else{
     				displayToast(getString(R.string.no_new_events));
     			}
     		}
     		//loadProgressBar.setProgress(progress[0].intValue());
 		}
 		
 		/**
 		* Goes to the Date Activity and hides the progressBar.
         */
 		protected void onPostExecute(Integer result) {
 			System.out.println("onPostExecute------------>");
 			loadProgressBar.setVisibility(View.GONE);
 			// Reload the Date Activity if there are new events
 			if (result > 0){
 				Intent intent = new Intent(context, DateActivity.class);
 				startActivity(intent);
			}			
 		}
 	}
 }
