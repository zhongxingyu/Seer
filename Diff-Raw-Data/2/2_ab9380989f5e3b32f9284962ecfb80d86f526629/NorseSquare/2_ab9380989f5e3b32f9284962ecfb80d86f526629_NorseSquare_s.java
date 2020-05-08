 package com.sp.norsesquare.froyo;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentSender.SendIntentException;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 
 import com.google.android.gms.auth.GoogleAuthException;
 import com.google.android.gms.auth.GoogleAuthUtil;
 import com.google.android.gms.auth.UserRecoverableAuthException;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.plus.PlusClient;
 import com.google.android.gms.plus.model.people.Person;
 
 
 public class NorseSquare extends NSBaseActivity implements View.OnClickListener,
 ConnectionCallbacks, OnConnectionFailedListener, DialogInterface.OnClickListener
 {
    
 	
     private GoogleMap mMap;
     private CameraUpdate cUpdate;
     boolean releaseLocation;
     private LocationManager locationManager;
     private LatLng currentLocation;
     private String Googletoken;
     private final String TAG = "Main NorseSquare Activity";
     
     public LocationListener locationListener;
     private String lutherAccount = "";  
     
     
     private ArrayList<MapMarker> storedMarkerList;
     private ArrayList<EventMarker> storedEventList;
     private ArrayList<Friend> storedFriendList;
     
     //Authentication variables
     AccountManager mAccountManager;
     String[] accountList;
     String googleAuthToken;
     
     PlusClient mPlusClient;
     ConnectionResult result;
     ProgressDialog mConnectionProgressDialog;
     private ConnectionResult mConnectionResult;
     private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
     
     private static final LatLng PREUS = new LatLng(43.312306,-91.802734);
     private static final LatLng LIBRARY_LAWN = new LatLng(43.312345,-91.803957);
     private static final LatLng CFL = new LatLng(43.312954,-91.805288);
     private static final LatLng BRUNSDALE = new LatLng(43.315405,-91.80503);
     
     
     GoogleUser me;
     
     //Dialog Box variables
     CreateEventListView eDialog;
     AboutDialogBox aDialog;
     HelpDialogBox hDialog;
     HelpDialogBoxTwo h2Dialog;
     HelpDialogBoxThree h3Dialog;
     
     //Get context for use in inner classes
     Context context = this;
     public NorseSquare()
 	{
 		super(R.string.app_name);
 	}
 
     @Override
 	public void onCreate(Bundle savedInstanceState) 
     {
          	
     	
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.layout_relative_map);
         setSlidingActionBarEnabled(true);
         
         //Get accounts
         AccountManager accountManager = AccountManager.get(this);
         accountList = getAccountNames();
         super.setUpSlidingMenu();
        
         storedEventList = new ArrayList<EventMarker>();
 //        storedEventList.add(new EventMarker(PREUS,"Study Session for Paideia","Wisdom in Community."));
 //        storedEventList.add(new EventMarker(BRUNSDALE,"Birthday Party for Sheila","The cake is, sadly, a lie."));
 //        storedEventList.add(new EventMarker(LIBRARY_LAWN,"QUIDDITCH","FREAKING QUIDDITCH"));
 //        storedEventList.add(new EventMarker(CFL,"LCSO Concert","GET SOME."));
 //        
         
         //TODO Make a case that handles if there is no luther.edu account on the phone.
         for (int i=0;i<accountList.length;i++)
         {
         	Log.i("GOOGLEAUTH",accountList[i]);
         	
         	if (accountList[i].contains("@luther.edu"))
         	{
         		Log.i("GOOGLEAUTH","Luther Account = " + accountList[i]);
         		lutherAccount = accountList[i];
         		i = accountList.length;
         	}
         	
         }
         
        // SECTION TO LOG IN 
         //LOGGING INTO GOOGLE
        //Get authToken for luther.edu account 
        AsyncTask<String, Void, String> LoginTask = new LoginAsyncTask(lutherAccount,this).execute();
        Log.i("GOOGLEAUTH","AuthToken is: " + googleAuthToken);   
        
       
        
        
        //new UserInfoAsyncTask(googleAuthToken).execute();
        //Initialize PlusClient
        //TODO - Add in appropriate listeners to below call
        mPlusClient = new PlusClient.Builder(this, this, this)
        .setVisibleActivities("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
        .build();
 
        
        
        // Progress bar to be displayed if the connection failure is not resolved.
        mConnectionProgressDialog = new ProgressDialog(this);
        mConnectionProgressDialog.setMessage("Signing in...");
         
         //Set up relevant services and listeners for GoogleMap
         storedMarkerList = new ArrayList<MapMarker>();
         
         locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         
         setUpMap();
         Toast.makeText(this, "Map has been set up.", Toast.LENGTH_SHORT).show();
     
         
         Log.i(TAG, "OnCreate");
         
         
         
     }
 
     private void LoginToDatabase() {
 		// TODO Auto-generated method stub
     	Log.i("hello ppls", me.getGID());
     	//AsyncTask<String, Void, Integer> LoginDatabase = new LoginDatabaseTask(me.getFirstName(), me.getLastName(), me.getEmail(), me.getGID()).execute();
 	}
 
 	@Override
     protected void onResume() 
     {
         super.onResume();
         
         //Get location manager
         locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         setUpMap();
         Log.i(TAG, "OnResume");
     }
     
 
 	
     
     @Override
 	public void onStart()
 	{
 		  //Get location manager, check if wifi and gps are enabled.
 		  
 	  	super.onStart();
 
 	  	
 	  	
 	  	// obtain location manager at restart of activity
 	  	locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 	  	
 	  	//TODO - Determine why all providers are seen as true, all the time
 	  	final boolean wifiEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 	  	final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 	  	
 	  	mPlusClient.connect();
 		locationListener = new LocationListener() 
 		{
 		
 		
 			public void onLocationChanged(Location location) 
 			{
 				// Called when a new location is found by the network location provider.
 				//TODO - Find how often this is called, determine if it is too frequent.
 				//updateLocation(location);
 				//Toast.makeText(context, "Location is being updated", Toast.LENGTH_SHORT).show();
 				
 			}
 		
 		
 			@Override
 			public void onProviderDisabled(String arg0)
 			{
 				// TODO Auto-generated method stub
 			
 			}
 		
 			@Override
 			public void onProviderEnabled(String arg0)
 			{
 				// TODO Auto-generated method stub
 			
 			}
 		
 			@Override
 			public void onStatusChanged(String arg0, int arg1, Bundle arg2)
 			{
 				// TODO Auto-generated method stub
 			
 			}
 	  		
 		};
   	
 	  	//TODO - Add dialogfragment to force user to enable the given provider.
 	  	if (!wifiEnabled)
 	  	{
 	  		Toast.makeText(this, "Wifi is not enabled", Toast.LENGTH_LONG).show();
 	  		System.exit(0);
 	  	}
 	  	
 	  	if (!gpsEnabled)
 	  	{
 	  		//put alert box here, for now exit
 	  		//System.exit(0);
 	  	}
 	  	Log.i(TAG, "OnStart");
 	}
 	
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		Log.i(TAG, "OnPause");
 	
 	}
 	
 	@Override
 	public void onStop()
 	{
 		super.onStop();
 		mPlusClient.disconnect();
 		Log.i(TAG, "OnStop");
 	}
 	
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 		Log.i(TAG, "OnDestroy");
 	}
 	
 	public void showDialog()
 	{
 		DialogFragment eDialog = (DialogFragment) CreateEventAlertDialog.instantiate(this, "edialog");
 		eDialog.show(getSupportFragmentManager(), "eDialog");
 	}
 	
 //	public void showEventListDialog()
 //	{
 //		DialogFragment eDialog = (DialogFragment) CreateEventListView.instantiate(this, "edialog");
 //		eDialog.show(getSupportFragmentManager(), "eDialog");
 //		
 ////		eDialog = new CreateEventListView();
 //////    	View v = ((View) findViewById(R.id.RelativeMapLayout));
 //////    	v.requestFocus();
 ////    	
 //////    	Intent i = new Intent(this,NorseSquare.class);
 //////    	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 //////    	startActivity(i);
 ////    	
 ////    	eDialog.show(getSupportFragmentManager(), "event_list");
 //	}
 	
 	//Functions for Event Creation Dialog
     public void doPositiveClick(String en,String ed,String d)
     {
     	String eventName = en;
     	String eventDescription = ed;
     	String date = d;
     	Location location = this.returnCurrentWifiLocation();
     	
     	
     	
     	String snippetString = eventDescription; 
     	LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
     	
     	
         this.storeEventMarker(ll,eventName,snippetString,date);
         this.redrawMarkers();
     }
     
     public void doNegativeClick()
     {
     	//Do nothing, user canceled event creation
     }
     
     
     
     
     //Methods for Friends feature
     public void addFriend()
     {
        	
     }
 	
     
    /*Functions for options menus*/   
   
     /*
     @Override
     public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) 
     {
         inflater.inflate(R.menu.menu_main_settings, menu);
         super.onCreateOptionsMenu(menu, inflater);
     } */
   
   
    
     
 //    @Override
 //    public boolean onPrepareOptionsMenu(Menu menu)
 //    {
 //    	super.onPrepareOptionsMenu(menu);
 ////    	//TODO - Figure out why the ********** this won't work
 ////    	getMenuInflater().inflate(R.menu.menu_main_settings, menu);
 ////    	return true;
 //    }
 //    
 //    @Override
 //    public boolean onOptionsItemSelected(MenuItem item) {
 //        // Handle item selection
 //    	super.onOptionsItemSelected(item);
 //    }
 
     
 	@Override
     protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
         if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == RESULT_OK) {
             mConnectionResult = null;
             mPlusClient.connect();
         }
     }
 	
 	
 	@Override
 	public void onClick(View view)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onConnectionFailed(ConnectionResult result)
 	{
 		if (result.hasResolution()) {
             try {
                 result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
             } catch (SendIntentException e) {
                 mPlusClient.connect();
             }
         }
         // Save the result and resolve the connection failure upon a user click.
         mConnectionResult = result;
 		
 	}
 
     @Override
     public void onConnected() {
         String accountName = mPlusClient.getAccountName();
         Toast.makeText(this, accountName + " is connected.", Toast.LENGTH_LONG).show();
         
         //Retrieve logged in user
         Person p = mPlusClient.getCurrentPerson();
   
         me = new GoogleUser(p.getName().getGivenName(),p.getName().getFamilyName(),mPlusClient.getAccountName(),p.getId());
         
         
         //Toast.makeText(this,((p.getName().getFamilyName())) + " is now connected",Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public void onDisconnected() {
         Log.d(TAG, "disconnected");
         //Toast.makeText(this, "Account has been disconnected", Toast.LENGTH_SHORT).show();
     }
 	
     public void setReleaseLocation(boolean b)
     {
     	releaseLocation = b;
     }
 
     private String[] getAccountNames() 
     {
     	
     	//Enumerate all Google accounts on a device
         mAccountManager = AccountManager.get(this);
         
         Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
         String[] names = new String[accounts.length];
         for (int i = 0; i < names.length; i++) 
         {
             names[i] = accounts[i].name;
         }
         return names;
     }
     
     
 
     //Functions for GoogleMap
     
     void setUpMap()
     {
         // Do a null check to confirm that we have not already instantiated the map.
         if (mMap == null)
         {
             // Try to obtain the map from the SupportMapFragment.
             mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.main_map)).getMap();
             
             
             //Set onCameraChangeListener to allow for boundaries to be used after "layout"(?)
             mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener()
 			{
 				
 				@Override
 				public void onCameraChange(CameraPosition arg0)
 				{
 					 /*Code for limiting map to Decorah area */
 					//TODO - Don't recalculate every time, only calculate decorah bounds after layout
 					//TODO - Add more precise boundaries
 					//TODO - See if possible to limit zoom capability with these boundaries.
 					
 					  /*Points with which to limit view of map:
 					   * Greater Decorah Area
 				     Southwest: Lat - 43.282454  Long - -91.827679
 				     Northeast: Lat - 43.309191  Long - -91.766739
 				     
 				     Luther Campus:
 				     Southwest: lat 43.310503, long -91.808732
 				     Northeast: lat 43.314999, long -91.801114
 				    
 
 				     */
 					
 					
 					LatLng boundSW = new LatLng(43.310503,-91.808732);
 			        LatLng boundNE = new LatLng(43.314999,-91.801114);
 			        
 			        LatLngBounds.Builder builder = new LatLngBounds.Builder();
 			        builder.include(boundSW);
 			        builder.include(boundNE);
 			        
 			        LatLngBounds decorahBound = new LatLngBounds(boundSW,boundNE);			        
 			        
 
 					// TODO - Find out why a null pointer is thrown sometimes
 					//wifiLocate(findViewById(R.id.main_map));
 					
 					
 			   
 			        cUpdate = CameraUpdateFactory.newLatLngBounds(decorahBound, 5);
 					
 					mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 5));
 	                mMap.setOnCameraChangeListener(null);
 					
 				}
 			});
             
          }
         
     }
 
 
     //Methods called from ControlPanel classes and Sliding menu 
     
     public void wifiLocate(View v)
     {
     	//Called from Control Panel button Wifi Locate, gets wifi location
     	//TODO - Zoom in closer on current location
     	
     	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 25, locationListener);
     	Location coarseLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
     	
     	
     	//placeSingleMarker(v, new LatLng(coarseLocation.getLatitude(),coarseLocation.getLongitude()));
     	updateLocation(coarseLocation);
     	
     	//Check In after location has been loaded
     	//TODO - uncomment this
     	checkIn();
     	
     }
     
     public Location returnCurrentWifiLocation()
         {
           //Get and return the current location from Wifi. 
           
           locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 25, locationListener);
           Location coarseLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
           
           return coarseLocation;
         }
     
 //    public void addFriend(String n, String e)
 //    {
 //    	Friend f = new Friend(n,e);
 //    	storedFriendList.add(f);
 //    	
 //    }
     
     
     public void storeMarker(LatLng latlong,String title, String snippet)
     {
     	//Add marker to list of stored markers, making sure that it is not a duplicate
     	//Duplicate = marker with same title (person's name)
     	
         Log.i("Store Marker","Store Marker called");
     	Log.i("Store Marker","Size of storeMarkerList is " + storedMarkerList.size());
     	
         //If duplicate item found, delete and add newest version. If not, add current item (must be new).
     	
     	if (storedMarkerList.size()==0)
     	{
     		storedMarkerList.add(new MapMarker(latlong,title,snippet));
     	}
     	else
     	{
     		for (int i=0;i<storedMarkerList.size();i++)
         	{
         	   MapMarker m = storedMarkerList.get(i);
         	   
         	   if (m.getTitle() != title)
         	   {
         		   Log.i("StoreMarker","New marker added wth title " + title);
         		   storedMarkerList.add(new MapMarker(latlong,title,snippet));
         	   }
         	   else
         	   {
         		   storedMarkerList.remove(m);
         		   Log.i("StoreMarker","New marker added wth title " + title);
         		   storedMarkerList.add(new MapMarker(latlong,title,snippet));
         	   }
         	  
         	}
     	}
     	
     	
     	
     }
     
     public void createEvent(View v)
     {
     	//Instantiate a CreateEventAlertDialog, which will add an appropriate marker to the stored marker list 
     	
     	CreateEventAlertDialog eDialog = new CreateEventAlertDialog();
 //    	View v = ((View) findViewById(R.id.RelativeMapLayout));
 //    	v.requestFocus();
     	
 //    	Intent i = new Intent(this,NorseSquare.class);
 //    	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 //    	startActivity(i);
     	
     	eDialog.show(getSupportFragmentManager(), "event_creation");
     	
     }
     
     public void showEventList(View v)
     {
     	CreateEventListView eDialog = new CreateEventListView();
     	
     	eDialog.show(getSupportFragmentManager(), "event_list");
     }
     
     public void showAboutDialog(View v)
     {
     	aDialog = new AboutDialogBox();
     	aDialog.show(getSupportFragmentManager(), "about_dialog");
     }
     public void closeAboutDialog(View v)
     {
     	aDialog.dismiss();
     }
     
     public void showHelpDialog(View v)
     {
     	hDialog = new HelpDialogBox();
     	hDialog.show(getSupportFragmentManager(), "help_dialog_one");
     }
     
     
     public void showHelpDialogTwo(View v)
     {
     	hDialog.dismiss();
     	h2Dialog = new HelpDialogBoxTwo();
     	h2Dialog.show(getSupportFragmentManager(), "help_dialog_two");
     }
     public void showHelpDialogThree(View v)
     {
     	h2Dialog.dismiss();
     	h3Dialog = new HelpDialogBoxThree();
     	h3Dialog.show(getSupportFragmentManager(), "help_dialog_three");
     }
     
     public void backToDialogOne(View v)
     {
     	h2Dialog.dismiss();
     	hDialog = new HelpDialogBox();
     	hDialog.show(getSupportFragmentManager(), "help_dialog_one");
     }
     public void backToDialogTwo(View v)
     {
     	h3Dialog.dismiss();
     	h2Dialog = new HelpDialogBoxTwo();
     	h2Dialog.show(getSupportFragmentManager(), "help_dialog_two");
     }
     
     public void closeHelpDialogOne(View v)
     {
     	hDialog.dismiss();
     }
     public void closeHelpDialogTwo(View v)
     {
     	h2Dialog.dismiss();
     }
     public void closeHelpDialogThree(View v)
     {
     	h3Dialog.dismiss();
     }
     
     public void storeEventMarker(LatLng latlong,String title, String snippet,String date)
     {
     	//Add marker to list of stored markers, making sure that it is not a duplicate
     	//Duplicate = marker with same title (person's name)
     	
         //Log.i("Store Marker","Store Marker called");
     	//Log.i("Store Marker","Size of storeMarkerList is " + storedEventList.size());
     	
         //If duplicate item found, delete and add newest version. If not, add current item (must be new).
     	
     	if (storedEventList.size()==0)
     	{
     		storedEventList.add(new EventMarker(latlong,title,snippet,date));
     	}
     	else
     	{
     		for (int i=0;i<storedEventList.size();i++)
         	{
         	   MapMarker m = storedEventList.get(i);
         	   
         	   if (m.getTitle() != title)
         	   {
         		   Log.i("StoreMarker","New marker added wth title " + title);
         		   storedEventList.add(new EventMarker(latlong,title,snippet,date));
         	   }
         	   else
         	   {
         		   storedEventList.remove(m);
         		   Log.i("StoreMarker","New marker added wth title " + title);
         		   storedEventList.add(new EventMarker(latlong,title,snippet,date));
         	   }
         	  
         	}
     	}
     	
     	
     	
     }
 
     public void checkInClicked(View v)
     {
     	//This method allows checkIn() to be called by a button, and also 
     	//avoid dealing with issues of scope and inconvenient refactoring of existing calls to checkIn()
     	checkIn();
     }
     
     public void checkIn()
     {
 		// Get and Send current location and information to web server. Update current position for logged in user, and add marker to map for current location.
     	//TODO - Include name?
     	
     	Location locate = this.returnCurrentWifiLocation();
 //    	storeMarker(new LatLng(locate.getLatitude(),locate.getLongitude()),me.getFirstName(),"I have checked in.");
 		
 		new CheckinTask(Double.toString(locate.getLatitude()),Double.toString(locate.getLongitude()),lutherAccount).execute((String[])null);
 		
 		try{
         	LoginToDatabase();
         }
         catch(Exception e){
     	    e.printStackTrace();
         }
 		
 	
 		
 //		if(storedMarkerList.contains((m.getTitle()==me.getFullName()))){
 //			m.showInfoWindow();
 //		}
     }
     
     
 
 
 	public void placeSingleMarker(View v,LatLng latlong)
     {
     	//------------DEPRECATED------------- - All further uses should store markers in storedMarkersList and use placeStoreMarkers()
     	mMap.clear();
     	
     	
     	//TODO - Programmatically alter marker contents for a more in depth user experience
     	
     	Marker cl = mMap.addMarker(new MarkerOptions().position(currentLocation)
     			                                      .title("Current Location")			                                      
     			                                      .snippet(latlong.toString()));
     	
     
     }
     
     public void placeStoredMarkers()
     {
         //Toast.makeText(this, "Placing Stored Markers", Toast.LENGTH_SHORT).show();
     	
     	Iterator i = storedMarkerList.iterator();
     	
         if (i.hasNext()==false)
         {
         	//Toast.makeText(this,"No Stored Markers", Toast.LENGTH_SHORT).show();
         }
     	while (i.hasNext())
     	{
     	   Log.i("Map Marker", "Placing Map Marker");
     	   MapMarker m = (MapMarker) i.next();
    	   mMap.addMarker(m.getMarkerOptions()).showInfoWindow();
     	}
     	
         Iterator<EventMarker> h = storedEventList.iterator();
     	
         if (h.hasNext()==false)
         {
         	//Toast.makeText(this,"No Stored Markers", Toast.LENGTH_SHORT).show();
         	Log.i("EventMarkers", "no Stored event markers");
         }
     	while (h.hasNext())
     	{
     	   Log.i("Map Marker", "Placing Map Marker");
     	   EventMarker m = (EventMarker) h.next();
     	   mMap.addMarker(m.getMarkerOptions());
     	}
     }
     
     public void updateLocation(Location l)
     {
     	//Primary method to update location in the map. All other methods should call this one, regardless of provider.
     	
     	//Set current location. This is called from both listeners and buttons, and is done to avoid having to get the location anew every time.
     	//TODO - See if this is already cached and easily available, refer to location strategies
     	currentLocation = new LatLng(l.getLatitude(),l.getLongitude());
     	
     	LatLng ll = new LatLng(l.getLatitude(),l.getLongitude());
     
     	
     	//mMap.moveCamera(CameraUpdateFactory.zoomBy(7));
     	mMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
     	
     	mMap.moveCamera(CameraUpdateFactory.newLatLng(ll));	
     }
     
 
     
     public void redrawMarkers()
     {
     	//Toast.makeText(this, "Redrawing Markers", Toast.LENGTH_LONG).show();
     	
     	mMap.clear();
     	
     	placeStoredMarkers();
     
     }
     
     //Joel's classes/etc for not location related things.
 
 //	public void popUp(View v)
 //	{
 //		//pops up toast full of text from textbox.
 //		EditText text = (EditText)findViewById(R.id.text_box);
 //		String value = text.getText().toString();
 //		Toast toast = Toast.makeText(this, value, Toast.LENGTH_LONG);
 //		toast.show();
 //	}
     
     
     //Listener classes for location management
     @Override
     public void myToggle() {
     	toggle();
     }
     
     public void datCheckIn(View v) {
 		wifiLocate(findViewById(R.id.main_map));
 		findAll(findViewById(R.id.main_map));
 	//	ns.checkInClicked(ns.findViewById(R.id.main_map));
 
     }
 	
 	
     public void findAll(View w){
     	
     	storedMarkerList.clear();
     	
     	AsyncTask<String, Void, String> Task = new DatabaseTask().execute((String[])null);
     	try{
     		String xmlString = Task.get();
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = factory.newDocumentBuilder();
             InputSource inStream = new InputSource();
             inStream.setCharacterStream(new StringReader(xmlString));
             Document doc = db.parse(inStream);
 
             //String playcount = "empty";
             NodeList nlist = doc.getElementsByTagName("person");
             
             for(int i = 0; i < nlist.getLength();i++){
             	
             	
 	            NodeList UserInfo = nlist.item(i).getChildNodes();
 	        	String fname = UserInfo.item(0).getTextContent();
 	        	String lname = UserInfo.item(1).getTextContent();
 	        	String username = UserInfo.item(2).getTextContent();
 	        	String googleid = UserInfo.item(3).getTextContent();
 	        	String gtime = UserInfo.item(4).getTextContent();
 	        	Double longitude = Double.parseDouble(UserInfo.item(5).getTextContent());
 	        	Double latitude = Double.parseDouble(UserInfo.item(6).getTextContent());
 	        	
 	        	String timedif = parsetime(gtime);
 	        	
 	        	
 	        	//Create new marker with user's information. Add to storedMarkerList.
 	        	LatLng locP = new LatLng(latitude,longitude);
 	        	MapMarker newmark = new MapMarker(locP, fname+" "+lname, "checked in "+ gtime+" ago");
 	        	//Toast.makeText(this, "Adding found to Marker List", Toast.LENGTH_SHORT).show();
 	        	storedMarkerList.add(newmark);
 	        	
 	        	
 	        	
             }
           
         	redrawMarkers();
             
     	}
     	catch(Exception e) {
     		Log.i("ERROR", "error in response answer");
     		e.printStackTrace();
     	}
     	
     }
 
 
 
 
 private String parsetime(String gtime) {
 		// TODO Auto-generated method stub
 	
 	Calendar c = Calendar.getInstance();
 	Date curtime = c.getTime();
 	String currenttime = curtime.toString();
 	
 	
 	return currenttime;
 	
 		
 	}
 
 
 
 
 /*Class to allow for background database calls to be made in alternate threads */
 public class LoginAsyncTask extends AsyncTask<String, Void, String>
 {
 	String lutherEmail;
 	String mScope;
 	String authToken;
 	Context context;
 	Bundle bundle;
 	
 	public LoginAsyncTask(String le,Context c)
 	{
 
 		lutherEmail = le;
 		context = c;
 		bundle = new Bundle();
 	}
 	
 	protected void onPreExecute()
 	{
 		Log.i("BEGIN","Getting authtoken");
 	}
 	
 	protected String doInBackground(String... args)
 	{
 		try 
 		{
 			authToken = GoogleAuthUtil.getToken(context, lutherEmail, "oauth2:"+"https://www.googleapis.com/auth/userinfo.profile", bundle);
 			Log.i("MESSAGEGEGEGE","YOUR TOKEN = "+authToken);
 			
 		}
 		catch (UserRecoverableAuthException recoverableException) {
 			
 			Log.e("GOOGLEAUTH","UserRecoverableException Triggered");
 		    Intent recoveryIntent = recoverableException.getIntent();
 		    startActivityForResult(recoveryIntent,1701);  //Return 1701 if activity for ameliorating action is started
 		     // Use the intent to create a Notification.
 		 } catch (GoogleAuthException authEx) {
 		     // This is likely unrecoverable.
 		     Log.e("MESSAAGEGEG", "Unrecoverable authentication exception: " + authEx.getMessage(), authEx);
 		 } catch (IOException ioEx) {
 		     Log.i("MESSAGEGEGE", "transient error encountered: " + ioEx.getMessage());
 		     //doExponentialBackoff();
 		 }
 	       catch (Exception e) {
 	    	   e.printStackTrace();
 	       }
 		
 		return authToken;
 	}
 	
 	protected void onProgressUpdate(Integer... progress)
 	{
 		Log.i("PROGRESS","Getting somewhere");
     }
 	
 	protected void onPostExecute(String result) 
 	{
 		
         Log.i("GOOGLEAUTH", "Returning Received Google Token");
         googleAuthToken = result;
         
        
     }
 	
 }
 
 public class GoogleUser
 {
 	String firstName;
 	String lastName;
 	String accountEmail;
 	String gid;
 	URL pictureURL = null;
 	
 	public GoogleUser(String fn, String ln, String email,String g)
 	{
 	   firstName = fn;
 	   lastName = ln;
 	   accountEmail = email;
 	   gid = g;
 	   
 	}
 	
 	public String getFirstName()
 	{
 	   return firstName;
 	  
 	}
 	
 	public String getLastName()
 	{
 		return lastName;
 	}
 	
 	public String getFullName()
 	{
 		return firstName + " " + lastName;
 	}
 	
 	public String getEmail()
 	{
 		return accountEmail;
 	}
 	public void setPictureURL(URL u)
 	{
 		pictureURL = u;
 	}
 	
 	public String getGID()
 	{
 		return gid;
 	}
 
 }
 
 public class UserInfoAsyncTask extends AsyncTask<Void, Void, String>
 {
 
 	String userToken;
 	
 	public UserInfoAsyncTask(String ut)
 	{
        userToken = ut;
 		
 	}
 	
 	protected void onPreExecute()
 	{
 		
 	}
 	
 	
 	
 	@Override
 	protected String doInBackground(Void... arg0)
 	{
 		
 		return null;
 	}
 	
 	protected void onProgressUpdate(Integer... progress)
 	{
 		Log.i("PROGRESS","Getting somewhere");
     }
 	
 	protected void onPostExecute(String result) 
 	{
 		
        
        
     }
 
 
 	
 	
   }
 
 
 public class PlusPictureTask extends AsyncTask<Void, Void, Void>
 {
 		
 		
 		
 		protected void onPreExecute()
 		{
 			
 		}
 		
 		@Override
 		protected Void doInBackground(Void... args)
 		{
 			return null;
 			
 		}
 		
 		
 
 	
 	
 }
 
 
 @Override
 public void onClick(DialogInterface arg0, int arg1)
 {
 	// TODO Auto-generated method stub
 	
 }
 
 
 
 }
 
 
 
 
 
 
  
