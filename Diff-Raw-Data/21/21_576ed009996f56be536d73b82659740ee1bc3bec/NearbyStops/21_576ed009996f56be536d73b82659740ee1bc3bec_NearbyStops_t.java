 package transponders.translinkmobile;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.annotation.SuppressLint;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationRequest;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 //import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 
 /**
  * The Android activity that shows the map with the current location of the
  * device and shows the nearby stops as markers on the map. Currently, this is
  * also the home screen of the application.
  * 
  * @author Transponders
  * @version 1.0
  */
 public class NearbyStops extends FragmentActivity implements
 GooglePlayServicesClient.ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
 
 	/*nearbystops  -> stopdataloader
 				<- (stops with each stop having list of route)
 	(single  stop) -> routedataloader   set up list of stoptrips where StopTrip:{Stop, Trip:{id, route}, Time}
 						combine the stoptrips with common routes to find time to display
 						the closest time to now is also the Trip to assign
 						assign position in ListView to the soonest Trip by finding the Route, 
 						user clicks position, get that Trip
 			<-(Trip)
 		-> (Trip) routeStopsLoader	use the trip to find the stops and line as opposed to the route */
 	
 	
 	
 	/**
 	 * Set the default location in case the application cannot detect the
 	 * current location of the device.
 	 */
 	private static final LatLng DEFAULT_LOCATION = new LatLng(-27.498037,153.017823);
 	private final String TITLE = "Nearby Stops & Service ETA";
 	public static final int NUM_PAGES = 4;
 	public static final String PREFS_NAME = "MyPrefsFile";
 	public static final String TUTORIAL_SETTING = "SHOW_TUTORIAL";
 	public static final String SELECTED_STOP_NAME = "SELECTED_STOP_NAME";
 
 	public enum StackState {
 		NearbyStops, ShowRoute
 	}
 
 	// Map and markers
 	private GoogleMap mMap;
 	private Marker userPos;
 	private Marker clickPos;
 	private StopDataLoader stopLoader;
 	private RouteStopsLoader routeStopsLoader;
 	private SupportMapFragment mapFrag;
 	private boolean updatedOnce;
 	private ArrayList<Stop> selectedStops;
 	//private Route selectedRoute;
 	private Trip selectedTrip;
 	private ArrayList<StopTrip> selectedTripsStopTrips;
 	//private String selectedTripId;
 	private LatLng userLatLng;
 	private ArrayList<Marker> stopMarkers;
 	private HashMap<Marker, Stop> stopMarkersMap;
 	
 	private Polyline polyline;
 
 	// private ShowRouteFragment map2Fragment;
 	//private ArrayList<StackState> stackStates;
 	private HashMap<Integer, StackState> stackStatesMap;
 	private int previousBackStackPosition;
 	private boolean viewingMap;
 
 	// Navigation drawer
 	private DrawerLayout mDrawerLayout;
 	private ListView mDrawerList;
 	private ActionBarDrawerToggle mDrawerToggle;
 	private CharSequence mDrawerTitle;
 	private CharSequence mTitle;
 	String[] menuList;
 
 	private ViewPager mPager;
 	private PagerAdapter mPagerAdapter;
 	
 	private LocationClient mLocationClient;
 	private LocationRequest mLocationRequest;
 	// Handle to SharedPreferences for this app
     SharedPreferences mPrefs;
     // Handle to a SharedPreferences editor
     SharedPreferences.Editor mEditor;
  // Milliseconds per second
     private static final int MILLISECONDS_PER_SECOND = 1000;
     // Update frequency in seconds
     public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
     // Update frequency in milliseconds
     private static final long UPDATE_INTERVAL =
             MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
     // The fastest update frequency, in seconds
     private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
     // A fast frequency ceiling in milliseconds
     private static final long FASTEST_INTERVAL =
             MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
     boolean mUpdatesRequested = true;
 
 	
 	// For testing purposes
 	private JourneyPlanner jpFragment = null;
 	private MaintenanceNewsFragment mnFragment = null;
 
 	@SuppressLint("NewApi")
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		stopMarkers = new ArrayList<Marker>();
 		stopMarkersMap = new HashMap<Marker, Stop>();
 		
 		
 		
 
 		// Set the fragment manager so it will pop elements from the stackStates
 		FragmentManager manager = getSupportFragmentManager();
 		manager.addOnBackStackChangedListener(getBackListener());
 		//stackStates = new ArrayList<StackState>();
 		//stackStates.add(StackState.NearbyStops);
 		stackStatesMap = new HashMap<Integer,StackState>();
 		stackStatesMap.put(0, StackState.NearbyStops);
 		previousBackStackPosition = 0;
 		viewingMap = true;
 
 		initializeNavigationDrawer();
 
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		boolean showTut = settings.getBoolean(TUTORIAL_SETTING, true);
 
 		// For debugging, uncomment this line below so that the tutorial doesn't show at all.
 		// showTut = false;
 
         // Create the LocationRequest object
         mLocationRequest = LocationRequest.create();
         // Use high accuracy
         mLocationRequest.setPriority(
                 LocationRequest.PRIORITY_HIGH_ACCURACY);
         // Set the update interval to 5 seconds
         mLocationRequest.setInterval(UPDATE_INTERVAL);
         // Set the fastest update interval to 1 second
         mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
         // Open the shared preferences
         mPrefs = getSharedPreferences("SharedPreferences",
                 Context.MODE_PRIVATE);
         // Get a SharedPreferences editor
         mEditor = mPrefs.edit();
         /*
          * Create a new location client, using the enclosing class to
          * handle callbacks.
          */
         mLocationClient = new LocationClient(this, this, this);
         // Start with updates turned off
         mUpdatesRequested = true;
         mLocationClient.connect();
 
 		
 		if (showTut)
 			showFirstTimeTutorial();
 		
 		mapInit();
 		showNearbyStops();
 	}
 	
 	public void initializeNavigationDrawer()
 	{
 		// Set the title and the content of the navigation drawer.
 		mTitle = TITLE;
 		mDrawerTitle = getTitle();
 		menuList = getResources().getStringArray(R.array.menu_array);
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_ns);
 		mDrawerList = (ListView) findViewById(R.id.left_drawer_ns);
 
 		// set a custom shadow that overlays the main content when the drawer
 		// opens
 		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
 				GravityCompat.START);
 
 		// set up the drawer's list view with items and click listener
 		ArrayAdapter<String> adapter = new MenuAdapter(this, menuList);
 		mDrawerList.setAdapter(adapter);
 		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
 		int position = 0;
 		mDrawerList.setItemChecked(position, true);
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		getActionBar().setTitle(TITLE);
 
 		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
 		mDrawerLayout, /* DrawerLayout object */
 		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
 		R.string.drawer_open, /* "open drawer" description for accessibility */
 		R.string.drawer_close /* "close drawer" description for accessibility */
 		) {
 			public void onDrawerClosed(View view) {
 				getActionBar().setTitle(mTitle);
 			}
 
 			public void onDrawerOpened(View drawerView) {
 				getActionBar().setTitle(mDrawerTitle);
 			}
 		};
 
 		mDrawerLayout.setDrawerListener(mDrawerToggle);
 		
 		
 
 	}
 
 	/**
 	 * A method to move the camera when the user touch the map to set a new
 	 * location that will have the nearby stops generated around.
 	 * 
 	 * @param location
 	 *            the latitude and longitude of the new location.
 	 */
 	public void locationChanged(LatLng location) {
 		mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
 		mMap.animateCamera(CameraUpdateFactory.zoomTo((float) 14.8), 2000, null);
 		stopLoader
 				.requestStopsNear(location.latitude, location.longitude, 1000);
 	}
 
 	/**
 	 * A method to set the action bar. The action bar home/up action should open
 	 * or close the drawer. ActionBarDrawerToggle will take care of this.
 	 * 
 	 * @param item
 	 *            the MenuItem that is selected.
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (mDrawerToggle.onOptionsItemSelected(item)) {
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * A listener class that handles what will happen when the item inside the
 	 * navigation drawer is clicked.
 	 * 
 	 * @author Transponders
 	 * @version 1.0
 	 */
 	private class DrawerItemClickListener implements
 			ListView.OnItemClickListener {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 
 			selectItem(position);
 
 		}
 	}
 
 	/**
 	 * A method that defines what will happen when the user clicks a specific
 	 * menu item in the navigation drawer. The method will start a new activity
 	 * according to the selected menu.
 	 * 
 	 * @param pos
 	 *            the position of the menu item that is clicked.
 	 */
 	private void selectItem(int pos) {
 		Fragment fragment = new Fragment();
 		FragmentManager manager = getSupportFragmentManager();
 		// manager.addOnBackStackChangedListener(getBackListener());
 
 		switch (pos) {
 		case 0:
 			manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
 			mDrawerList.setItemChecked(pos, true);
 			setTitle(TITLE);
 			mDrawerLayout.closeDrawer(mDrawerList);
 			updatedOnce = false;
 			showNearbyStops();
 			if (userLatLng.latitude != 0 && userLatLng.longitude != 0) {
 				locationChanged(userLatLng);
 			}
 			return;
 		case 1:
 			jpFragment = new JourneyPlanner();
 			fragment = jpFragment;
 			Bundle args = new Bundle();
 			double[] userLoc = { userLatLng.latitude, userLatLng.longitude };
 			args.putDoubleArray(JourneyPlanner.ARGS_USER_LOC, userLoc);
 			fragment.setArguments(args);
 			break;
 		case 2:
 			mnFragment = new MaintenanceNewsFragment();
 			fragment = mnFragment;
 			break;
 		default:
 			break;
 		}
 
 		FragmentTransaction transaction = manager.beginTransaction();
 		transaction.replace(R.id.content_frame, fragment);
 		transaction.addToBackStack(null);
 		transaction.commitAllowingStateLoss();
 
 		// update selected item and title, then close the drawer
 		mDrawerList.setItemChecked(pos, true);
 		setTitle(menuList[pos]);
 		mDrawerLayout.closeDrawer(mDrawerList);
 
 	}
 	
 	public void mapInit() 
 	{
 		LatLng center = DEFAULT_LOCATION;
 
 		mapFrag = (SupportMapFragment) getSupportFragmentManager()
 				.findFragmentById(R.id.map);
 
 		mMap = mapFrag.getMap();
 		// mMap = ((MapFragment)
 		// getFragmentManager().findFragmentById(R.id.map)).getMap();
 
 		while (mMap == null) {
 			// The application is still unable to load the map.
 		}
 		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));
 		mMap.setMyLocationEnabled(false);
 		stopLoader = new StopDataLoader(mMap, stopMarkers, stopMarkersMap);
 		routeStopsLoader = new RouteStopsLoader(mMap, stopMarkers,
 				stopMarkersMap, polyline);
 		userLatLng = new LatLng(0,0);
 		updatedOnce = false;
 		
 		final Button button = (Button) findViewById(R.id.bLocateMe);
 		button.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				/*updatedOnce = false;
 				showNearbyStops();
 				if (userLatLng.latitude != 0 && userLatLng.longitude != 0) {
 					locationChanged(userLatLng);
 				}*/
 				startUpdates();
 				
 			}
 		});
 	}
 
 
 	public void showNearbyStops() {
 		if (userPos == null) {
 			userPos = mMap.addMarker(new MarkerOptions()
 					.position(DEFAULT_LOCATION)
 					.title("Your Position")
 					.visible(false)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.location_geo_border)));
 		}
 		if (clickPos == null) {
 			clickPos = mMap.addMarker(new MarkerOptions()
 					.position(DEFAULT_LOCATION)
 					.title("Your Selected Position")
 					.visible(false)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.chosen_geo_border)));
 		}
 
 		//Reload the stops if any from previous nearbystops
 		//test
 		stopLoader.addSavedStopMarkersToMap(true);
 		routeStopsLoader.removeLineFromMap();
 		getActionBar().setTitle(TITLE);
 
 		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
 
 			@Override
 			public void onInfoWindowClick(Marker marker) {
 				Stop stop = stopLoader.getIdOfMarker(marker);
 				if (stop != null) {
 					ArrayList<Stop> stops;
 					if (stop.hasParent()) {
 						stops = stopLoader.getStopsFromParent(stop);
 					} else {
 						stops = new ArrayList<Stop>();
 						stops.add(stop);
 					}
 					setSelectedStops(stops);
 
 					openTimetableFragment(marker.getTitle());
 				}
 			}
 		});
 
 		mMap.setOnMapClickListener(new OnMapClickListener() {
 
 			@Override
 			public void onMapClick(LatLng arg0) {
 				clickPos.setVisible(true);
 				clickPos.setPosition(arg0);
 				locationChanged(arg0);
 			}
 		});
 		/*
 		mMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
 
 			@Override
 			public boolean onMyLocationButtonClick() 
 			{
 				updatedOnce = false;
 				showNearbyStops();
 				if (userLatLng.latitude != 0 && userLatLng.longitude != 0) {
 					locationChanged(userLatLng);
 				}
 				
 				return true;
 			}
 			
 		});*/
 		
 	
 		
 		// Acquire a reference to the system Location Manager
 		LocationManager locationManager = (LocationManager) this
 				.getSystemService(Context.LOCATION_SERVICE);
 
 		// Define a listener that responds to location updates
 		LocationListener locationListener = new LocationListener() {
 			public void onLocationChanged(Location location) {
 				// Called when a new location is found by the network location
 				// provider.
 				userLatLng = new LatLng(location.getLatitude(),
 						location.getLongitude());
 				userPos.setVisible(true);
 				userPos.setPosition(userLatLng);
 				//if (!updatedOnce) {
 					locationChanged(userLatLng);
 				//}
 				updatedOnce = true;
 				Log.d("Location", "Location changed");
 			}
 
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {
 			}
 
 			public void onProviderEnabled(String provider) {
 			}
 
 			public void onProviderDisabled(String provider) {
 			}
 		};
 
 		// Register the listener with the Location Manager to receive location updates
 		// Check every 1 minute and only if location has changed by 50 meters.
 		/*ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		State wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
 		State mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
 		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) &&
 				(wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING)) {
 			Log.d("Location", "using network");
 			locationManager.requestLocationUpdates(
 					LocationManager.NETWORK_PROVIDER, 60000, 50,
 					locationListener);
 			
 		} else if (locationManager
 				.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 			Log.d("Location", "using gps");
 			locationManager.requestLocationUpdates(
 					LocationManager.GPS_PROVIDER, 100, 0, locationListener);
 			Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 			if (location != null) {
 				userLatLng = new LatLng(location.getLatitude(),
 						location.getLongitude());
 				userPos.setVisible(true);
 				userPos.setPosition(userLatLng);
 				//if (!updatedOnce) {
 					locationChanged(userLatLng);
 				//}
 				updatedOnce = true;
 			}
 		} else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) &&
 					(mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING)) {
 				Log.d("Location", "using network");
 				locationManager.requestLocationUpdates(
 						LocationManager.NETWORK_PROVIDER, 60000, 50,
 						locationListener);
 				
 				
 		} else {
 			// No location provider enabled. Use the default location for now
 			Log.d("Location", "cannot find user location");
 			locationChanged(new LatLng(-27.498037, 153.017823));
 		}
 	*/
 		// map2Fragment = new ShowRouteFragment();
 
 
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		boolean showTut = settings.getBoolean(TUTORIAL_SETTING, true);
 
 		// For debugging, uncomment this line below so that the tutorial doesn't
 		// show at all.
 		// showTut = false;
 
 		if (showTut)
 			showFirstTimeTutorial();
 		
 		TextView routeInfo = (TextView) findViewById(R.id.routeInfoBox); 
 		routeInfo.setVisibility(View.GONE);
		mMap.setMyLocationEnabled(false);
		final Button button = (Button) findViewById(R.id.bLocateMe);
		button.setVisibility(View.VISIBLE);
 	}
 
 	public void showFirstTimeTutorial() 
 	{
 		mPager = (ViewPager) findViewById(R.id.pager);
 		mPagerAdapter = new TutorialPagerAdapter(getSupportFragmentManager(), mPager);
 		mPager.setAdapter(mPagerAdapter);
 		
 		mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				invalidateOptionsMenu();
 			}
 		});
 	}
 
 	public void showRoute() 
 	{
 		//routeStopsLoader.requestRouteStops(selectedRoute);
 		routeStopsLoader.requestTripStops(selectedTrip);
 		TextView routeInfo = (TextView) findViewById(R.id.routeInfoBox); 
 		routeInfo.setText(selectedTrip.getRoute().getCode() + ": "+selectedTrip.getRoute().getDescription());
 		routeInfo.setVisibility(View.VISIBLE);
		mMap.setMyLocationEnabled(true);
		final Button button = (Button) findViewById(R.id.bLocateMe);
		button.setVisibility(View.GONE);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		// Sync the toggle state after onRestoreInstanceState has occurred.
 		mDrawerToggle.syncState();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		mDrawerToggle.onConfigurationChanged(newConfig);
 	}
 
 	@SuppressLint("NewApi")
 	@Override
 	public void setTitle(CharSequence title) {
 		mTitle = title;
 		getActionBar().setTitle(mTitle);
 	}
 
 	public void openTimetableFragment(String stopName) {
 		Fragment fragment = new Fragment();
 		FragmentManager manager = getSupportFragmentManager();
 		// manager.addOnBackStackChangedListener(getBackListener());
 		fragment = new DisplayRoutesFragment();
 		
 		Bundle args = new Bundle();
 		args.putString(SELECTED_STOP_NAME, stopName);
 		fragment.setArguments(args);
 
 		FragmentTransaction transaction = manager.beginTransaction();
 		transaction.replace(R.id.content_frame, fragment);
 		transaction.setCustomAnimations(android.R.anim.slide_in_left,
 				android.R.anim.slide_out_right);
 		transaction.addToBackStack(null);
 		transaction.commitAllowingStateLoss();
 
 		// update selected item and title, then close the drawer
 		setTitle("Timetable");
 	}
 
 	public OnBackStackChangedListener getBackListener() {
 		OnBackStackChangedListener result = new OnBackStackChangedListener() {
 			public void onBackStackChanged() {
 				FragmentManager manager = getSupportFragmentManager();
 
 				if (manager != null) {
 					//for (int i=0; i<stackStates.size(); i++) {
 					//	Log.d("Drawer", "stackStates("+i+")="+stackStates.get(i));
 					//}
 					int stackCount = manager.getBackStackEntryCount();
 					Log.d("Drawer", "previousStackCount=" + previousBackStackPosition + " stackCount=" + stackCount);
 					//if (stackCount == 0) {
 						// finish();
 					for (int i =0; i <manager.getFragments().size(); i++) {
 						Fragment frag = (Fragment) manager.getFragments().get(i);
 						Log.d("Drawer", "***i="+i);
 						if (frag == null) {
 							Log.d("Drawer", "******Fragment null");
 						} else {
 							Log.d("Drawer", "***Fragment: " + frag.getClass());
 						}
 					}
 					if (previousBackStackPosition < stackCount){
 						//do nothing because back button was not actually pushed
 					//	if (manager.getFragments().get(stackCount-1).getClass() == SupportMapFragment.class) {
 					//		viewingMap = true;
 					//	}
 					} else {
 						
 						
 						Fragment currFrag = (Fragment) manager.getFragments()
 								.get(stackCount);
 						if (currFrag == null) {
 							Log.d("Drawer", "currFrag is null");
 						} else {
 							Log.d("Drawer", "currFrag class is "+currFrag.getClass());
 							currFrag.onResume();
 							
 							// Log.d("Drawer", "First backstatefragment is called "
 							// + (Fragment)manager.getFragments().get(0));
 							if (currFrag.getClass() == SupportMapFragment.class) {
 								// assume seeing NearbyStops
 	
 								Log.d("Drawer", "found the supportmapfragment");
 								StackState state;
 								
 								
 								//state = stackStates.get(stackStates
 								//		.size() - 1);
 								
 								state = stackStatesMap.get(stackCount);
 								
 	
 								if (state == StackState.NearbyStops) {
 									// Should check if need to refresh or not
 									Log.d("Drawer", "starting nearbyStops");
 									showNearbyStops();
 								} else if (state == StackState.ShowRoute) {
 									// Should check if need to refresh or not
 									Log.d("Drawer", "starting showroute");
 									showRoute();
 								}
 								//stackStates.remove(stackStates.size()-1);
 							} else {
 								if (viewingMap) {
 									//remove the map state from fragment just viewing
 									//stackStates.remove(stackStates.size()-1);
 								}
 								viewingMap = false;
 							}
 						}
 					}
 					previousBackStackPosition = stackCount;
 					//ensure NearbyStops is still on bottom of stateStack
 					//if (stackStates.size() == 0) {
 					//	stackStates.add(StackState.NearbyStops);
 					//}
 				}
 			}
 		};
 
 		return result;
 	}
 
 	/**
 	 * Getter method of the selected stops object.
 	 * 
 	 * @return ArrayList<Stop> The list of selected stops.
 	 */
 	public ArrayList<Stop> getSelectedStops() {
 		return selectedStops;
 	}
 
 	/**
 	 * Setter method of the selected stops object.
 	 * 
 	 * @param stops
 	 *            the ArrayList of selected stops.
 	 */
 	public void setSelectedStops(ArrayList<Stop> stops) {
 		this.selectedStops = stops;
 	}
 
 	/*public Route getSelectedRoute() {
 		return selectedRoute;
 	}*/
 
 	/*
 	public void setSelectedRoute(Route route) {
 		selectedRoute = route;
 	}*/
 	
 	public void setSelectedTrip(Trip trip) {
 		selectedTrip = trip;
 	}
 	
 	/*public void setSelectedTripsStopTrips(ArrayList<StopTrip> stopTrips) {
 		this.selectedTripsStopTrips = stopTrips;
 	}*/
 	
 	
 
 	public void addStateToStack(StackState state) {
 		FragmentManager manager = getSupportFragmentManager();
 		int stackCount = manager.getBackStackEntryCount();
 		stackStatesMap.put(stackCount+1, state);
 		Log.d("Drawer", "Put " + state+ " into " + (stackCount-1));
 	}
 
 	public void removeAllMarkers() {
 		for (Marker m : stopMarkers) {
 			m.setVisible(false);
 			m.remove();
 		}
 	}
 
 	/*
 	 * public ShowRouteFragment getMap2Fragment() { return map2Fragment; }
 	 * public void setMap2Fragment(ShowRouteFragment map2Fragment) {
 	 * this.map2Fragment= map2Fragment; }
 	 */
 
 	private class TutorialPagerAdapter extends FragmentStatePagerAdapter 
 	{
 		private View pagerView;
 
 		public TutorialPagerAdapter(FragmentManager fm, View parent) 
 		{
 			super(fm);
 			pagerView = parent;
 		}
 
 		@Override
 		public Fragment getItem(int position) 
 		{
 			return TutorialFragment.create(position, pagerView);
 		}
 
 		@Override
 		public int getCount()
 		{
 			return NUM_PAGES;
 		}
 	}
 	
 	/*Testing functions */
 	public StopDataLoader getStopDataLoader() {
 		return stopLoader;
 	}
 
 	public RouteStopsLoader getRouteStopsLoader() {
 		return routeStopsLoader;
 	}
 
 
 	public ArrayList<Marker> getStopMarkers() {
 		return stopMarkers;
 	}
 	public Fragment getContentFragment() {
 		FragmentManager manager = getSupportFragmentManager();
 		return manager.findFragmentById(R.id.content_frame);
 	}
 	
 	public JourneyPlanner getJourneyPlannerFragment()
 	{
 		return jpFragment;
 	}
 	
 	public MaintenanceNewsFragment getMaintenanceNewsFragment()
 	{
 		return mnFragment;
 	}
 	
 	
 	/*End of Testing functions */
 
 	@Override
 	public void onConnected(Bundle arg0) {
 		
         // Display the connection status
         // If already requested, start periodic updates
         if (mUpdatesRequested) {
             mLocationClient.requestLocationUpdates(mLocationRequest, this);
             
         }
 		
 	}
 
 	@Override
 	public void onDisconnected() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	 @Override
 	    public void onLocationChanged(Location location) {
 	        // Report to the UI that the location was updated
 	        /*String msg = "Updated Location: " +
 	                Double.toString(location.getLatitude()) + "," +
 	                Double.toString(location.getLongitude());
 	        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();*/
 		 
 				// Called when a new location is found by the network location
 				// provider.
 				userLatLng = new LatLng(location.getLatitude(),
 						location.getLongitude());
 				userPos.setVisible(true);
 				userPos.setPosition(userLatLng);
 				//if (!updatedOnce) {
 					locationChanged(userLatLng);
 				//}
 				updatedOnce = true;
 				Log.d("Location", "Location changed");
 				mLocationClient.removeLocationUpdates(this);
 			
 	    }
 
 	public void startUpdates() {
 		mLocationClient.requestLocationUpdates(mLocationRequest, this);
 	}
 
 	@Override
 	public void onConnectionFailed(ConnectionResult arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 
 	
 	/*public void setSelectedTripId(String tripId) {
 		selectedTripId = tripId;
 	}*/
 
 }
