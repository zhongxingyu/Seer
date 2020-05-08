 package il.ac.huji.shoppit;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 import com.parse.Parse;
 import com.parse.ParseFacebookUtils;
 import com.parse.ParseObject;
 import com.parse.ParseUser;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.SearchManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentSender;
 import android.content.res.Configuration;
 import android.location.Location;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.ActionBarActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.Toast;
 
 public class MainActivity extends ActionBarActivity
 implements ConnectionCallbacks, OnConnectionFailedListener, 
 LocationListener {
 
 	private static final String TAG = "MAIN_ACT";
 
 	private static final String GENERAL_SEPARATOR = "General";
 	private static final String CATEGORY_SEPARATOR = "Categories";
 
 	public static final String LATITUDE_EXTRA = "LATITUDE_EXTRA";
 	public static final String LONGITUDE_EXTRA = "LONGITUDE_EXTRA";
 
 	private static final int ADD_ITEM_REQUEST_CODE = 5000;
 	private static final int ADD_SHOP_REQUEST_CODE = 5001;
 
 	private DrawerLayout mDrawerLayout;
 	private ListView mDrawerList;
 	private ActionBarDrawerToggle mDrawerToggle;
 
 	private CharSequence mDrawerTitle;
 	private CharSequence mTitle;
 	private String[] mCategoryTitles;
 	private String[] mGeneralNavBarTitles;
 
 	private NavDrawerAdapter mNavDrawerAdapter;
 
 	final Activity mainActivity = this;
 
 	int selectedCategory = 1;
 
 	// -------------
 	// for getting location
 	// -------------
 	private LocationClient mLocationClient;
 
 	// used when Google Play services is unavailable.
 	private LocationRetriever2 mFallbackLocationRetriever;
 
 	private static final LocationRequest REQUEST = LocationRequest.create()
 			.setInterval(60 * 1000)		// 60 seconds
 			.setFastestInterval(1 * 1000)	// 1 second
 			.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
 
 	// if mLocationClient has found a location.
 	private boolean foundLocationLC = false;
 
 	// if mFallbackLocationRetriever has found a location.
 	private boolean foundLocationFLR = false;
 
 	/*
 	 * Define a request code to send to Google Play services
 	 * This code is returned in Activity.onActivityResult
 	 */
 	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
 
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		ParseObject.registerSubclass(Item.class);
 		ParseObject.registerSubclass(Shop.class);
 		ParseObject.registerSubclass(Comment.class);
 		Parse.initialize(this, "jAcoqyTFZ83HhbvfAaGQUe9hcu8lf0IOhyyYVKj5", "6gYN5nmVPMPpwyL0qNLOJbqShosYV0JR7Owp2Oli");
 		ParseFacebookUtils.initialize(getString(R.string.app_id));
 
 		mTitle = mDrawerTitle = getTitle();
 		mCategoryTitles = getResources().getStringArray(R.array.categories_array);
 		mGeneralNavBarTitles = getResources().getStringArray(R.array.general_navbar_array);
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 		mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
 		// set a custom shadow that overlays the main content when the drawer opens
 		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
 		// set up the drawer's list view with items and click listener
 		//		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mCategoryTitles));
 		mNavDrawerAdapter = new NavDrawerAdapter(getBaseContext());
 		mNavDrawerAdapter.addSeparatorItem(CATEGORY_SEPARATOR);
 		mNavDrawerAdapter.addItems(mCategoryTitles);
 		mNavDrawerAdapter.addSeparatorItem(GENERAL_SEPARATOR);
 		mNavDrawerAdapter.addItems(mGeneralNavBarTitles);
 
 		mDrawerList.setAdapter(mNavDrawerAdapter);
 		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
 		// enable ActionBar app icon to behave as action to toggle nav drawer
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 			getActionBar().setHomeButtonEnabled(true);// REQUIRES API LEVEL 14.
 		}
 
 		// ActionBarDrawerToggle ties together the the proper interactions
 		// between the sliding drawer and the action bar app icon
 		mDrawerToggle = new ActionBarDrawerToggle(
 				this,                  /* host Activity */
 				mDrawerLayout,         /* DrawerLayout object */
 				R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
 				R.string.drawer_open,  /* "open drawer" description for accessibility */
 				R.string.drawer_close  /* "close drawer" description for accessibility */
 				) {
 			public void onDrawerClosed(View view) {
 				getActionBar().setTitle(mTitle);
 				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
 			}
 
 			public void onDrawerOpened(View drawerView) {
 				getActionBar().setTitle(mDrawerTitle);
 				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
 			}
 		};
 		mDrawerLayout.setDrawerListener(mDrawerToggle);
 
 		// TODO moved to onResume
 		//		if (savedInstanceState == null) {
 		//			selectItem(mNavDrawerAdapter.getPosition(CATEGORY_SEPARATOR)+1);
 		//		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_activity_actions, menu);
 
 		// Get the SearchView and set the searchable configuration
 		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
 		searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchableActivity.class)));
 		//		searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/* Called whenever we call invalidateOptionsMenu() */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
 		menu.findItem(R.id.action_search).setVisible(!drawerOpen);
 		menu.findItem(R.id.action_add).setVisible(!drawerOpen);
 		menu.findItem(R.id.action_map).setVisible(!drawerOpen);
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// The action bar home/up action should open or close the drawer.
 		// ActionBarDrawerToggle will take care of this.
 		if (mDrawerToggle.onOptionsItemSelected(item)) {
 			return true;
 		}
 		// Handle action buttons
 
 		// DEMO CODE
 		//		switch(item.getItemId()) {
 		//		case R.id.action_websearch:
 		//			// create intent to perform web search for this planet
 		//			Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
 		//			intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
 		//			// catch event that there's no activity to handle intent
 		//			if (intent.resolveActivity(getPackageManager()) != null) {
 		//				startActivity(intent);
 		//			} else {
 		//				Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
 		//			}
 		//			return true;
 		//		default:
 		//			return super.onOptionsItemSelected(item);
 		//		}
 
 		switch (item.getItemId()) {
 		case R.id.action_map:
 			Intent intent = new Intent(getBaseContext(), MapActivity.class);
 			startActivity(intent);
 			return true;
 		case R.id.action_add:
 			if (ParseUser.getCurrentUser() != null) {
 				Location lastLocation = getLastLocation();
 
 				if (lastLocation != null) {
 					Intent newItemIntent = new Intent(getBaseContext(), NewItemActivity.class);
 					newItemIntent.putExtra(LATITUDE_EXTRA, lastLocation.getLatitude());
 					newItemIntent.putExtra(LONGITUDE_EXTRA, lastLocation.getLongitude());
 					startActivity(newItemIntent);
 				} else {
 					Toast.makeText(mainActivity, "Error getting device location",
 							Toast.LENGTH_LONG).show();
 				}
 			} else {
 				Intent loginIntent = new Intent(getBaseContext(), LoginActivity.class);
 				startActivityForResult(loginIntent, ADD_ITEM_REQUEST_CODE);
 			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/* The click listener for ListView in the navigation drawer */
 	private class DrawerItemClickListener implements ListView.OnItemClickListener {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			selectItem(position);
 		}
 	}
 
 	private void selectItem(int position) {
 		if (mNavDrawerAdapter.isSeparator(position)) {
 			return;
 		}
 
 		String sectionName = mNavDrawerAdapter.getSectionName(position);
 		String selectedName = mNavDrawerAdapter.getItemName(position);
 		//Log.i("MAIN ACT", selectedName);
 		int positionInSection = mNavDrawerAdapter.getPositionInSection(position);
 
 		if (sectionName == CATEGORY_SEPARATOR) {
 
 			Location lastLocation = getLastLocation();
 
 			if (lastLocation != null) {
 				// update the main content by replacing fragments
 				Fragment fragment = new CategoryFragment();
 				Bundle args = new Bundle();
 
 				args.putInt(CategoryFragment.ARG_CATEGORY_NUMBER, positionInSection);
 				args.putDouble(MainActivity.LATITUDE_EXTRA, lastLocation.getLatitude());
 				args.putDouble(MainActivity.LONGITUDE_EXTRA, lastLocation.getLongitude());
 				fragment.setArguments(args);
 
 				FragmentManager fragmentManager = getFragmentManager();
 				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
 
 				// update selected item and title, then close the drawer
 				mDrawerList.setItemChecked(position, true);
 				setTitle(mCategoryTitles[positionInSection]);
 				mDrawerLayout.closeDrawer(mDrawerList);
 
 				selectedCategory = position;
 			} else {
 				Toast.makeText(mainActivity, "Error getting device location",
 						Toast.LENGTH_LONG).show();
 			}
 
 			return;
 
 		} else if (selectedName.equals("Add Shop")) { // a bit ugly...
 			// start new activity
 			if (ParseUser.getCurrentUser() != null) {
 				Location lastLocation = getLastLocation();
 				if (lastLocation != null) {
 					Intent intent = new Intent(getBaseContext(), NewShopActivity.class);
 					intent.putExtra(LATITUDE_EXTRA, lastLocation.getLatitude());
 					intent.putExtra(LONGITUDE_EXTRA, lastLocation.getLongitude());
 					startActivity(intent);
 				} else {
 					Toast.makeText(mainActivity, "Error getting device location",
 							Toast.LENGTH_LONG).show();
 					return;
 				}
 
 			} else {
 				Intent loginIntent = new Intent(getBaseContext(), LoginActivity.class);
 				startActivityForResult(loginIntent, ADD_SHOP_REQUEST_CODE);
 			}
 
 			mDrawerLayout.closeDrawer(mDrawerList);
 			return;
 
 		} else if (selectedName.equals("Log out")) { // ugly again...
 
 			ParseUser.logOut();
 
 			// TODO remove log out option from menu (replace with login option?)
 			mDrawerLayout.closeDrawer(mDrawerList);
 			return;
 
 		} else if (selectedName.equals("Shops")) { // ugly again...
 			Location lastLocation = getLastLocation();
 
 			if (lastLocation != null) {
 				// update the main content by replacing fragments
 
 				Fragment fragment = new ShopListFragment();
 				Bundle args = new Bundle();
 
 				args.putDouble(MainActivity.LATITUDE_EXTRA, lastLocation.getLatitude());
 				args.putDouble(MainActivity.LONGITUDE_EXTRA, lastLocation.getLongitude());
 				fragment.setArguments(args);
 
 				FragmentManager fragmentManager = getFragmentManager();
 				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
 
 				// update selected item and title, then close the drawer
 				mDrawerList.setItemChecked(position, true);
 				mDrawerLayout.closeDrawer(mDrawerList);
 
 				selectedCategory = position;
 
 				return;
 			} else {
 				Toast.makeText(mainActivity, "Error getting device location",
 						Toast.LENGTH_LONG).show();
 				return;
 			}
 		}
 	}
 
 	@Override
 	public void setTitle(CharSequence title) {
 		mTitle = title;
 		getActionBar().setTitle(mTitle);
 	}
 
 	/**
 	 * When using the ActionBarDrawerToggle, you must call it during
 	 * onPostCreate() and onConfigurationChanged()...
 	 */
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		// Sync the toggle state after onRestoreInstanceState has occurred.
 		mDrawerToggle.syncState();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		// Pass any configuration change to the drawer toggles
 		mDrawerToggle.onConfigurationChanged(newConfig);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == ADD_ITEM_REQUEST_CODE && resultCode == RESULT_OK){
 			startActivity(new Intent(getBaseContext(), NewItemActivity.class));
 		} else if (requestCode == ADD_SHOP_REQUEST_CODE && resultCode == RESULT_OK){
 			startActivity(new Intent(getBaseContext(), NewShopActivity.class));
 		} else if (requestCode == CONNECTION_FAILURE_RESOLUTION_REQUEST) {
 			// TODO this isn't doing anything ATM
 			/*
 			 * If the result code is Activity.RESULT_OK, try
 			 * to connect again
 			 */
 			switch (resultCode) {
 			// If Google Play services resolved the problem
 			case Activity.RESULT_OK:
 				break;
 				// If any other result was returned by Google Play services
 			default:
 				break;
 			}
 
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// Check that Google Play services is available
 		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

 		// If Google Play services is available
 		if (ConnectionResult.SUCCESS == resultCode) {
 			
			Toast.makeText(mainActivity, "Google Play services is available.",
					Toast.LENGTH_LONG).show();

 			Log.d("Location Updates", "Google Play services is available.");
 			setUpLocationClientIfNeeded();
 			mLocationClient.connect();

 		} else {
			Toast.makeText(mainActivity, "Google Play services is unavailable. Using LR2.",
					Toast.LENGTH_LONG).show();
			
 			mFallbackLocationRetriever = new LocationRetriever2(new LocationRetriever2.TimerFunc() {
 
 				@Override
 				void timerFunc() {
 
 					//This function will fill the list of nearby items
 					//when the timer for getting the device position has elapsed
 					//or as soon as the position is located.
 
 					runOnUiThread(new Runnable() {
 						public void run() {
 							Log.i(TAG, "LR2 - runOnUiThread");
 							//					Log.d(TAG, (GeneralInfo.location == null)+"");
 							//					Log.d(TAG, (GeneralInfo.location)+"");
 							if (GeneralInfo.location == null) { //In case of error
								Toast.makeText(mainActivity, "LR2 - Error getting device location.",
 										Toast.LENGTH_LONG).show();
 							} else {
 								foundLocationFLR = true;
 								selectItem(mNavDrawerAdapter.getPosition(CATEGORY_SEPARATOR)+1);
 							}
 						}
 					});
 
 				}
 
 			});
 
 			// Get the device location
 			mFallbackLocationRetriever.startGettingUpdates(this, 10000);
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		if (mLocationClient != null) {
 			mLocationClient.disconnect();
 		}
 		if (mFallbackLocationRetriever != null) {
 			mFallbackLocationRetriever.stopGettingUpdates();
 		}
 	}
 
 	private void setUpLocationClientIfNeeded() {
 		if (mLocationClient == null) {
 			mLocationClient = new LocationClient(
 					getApplicationContext(),
 					this,  // ConnectionCallbacks
 					this); // OnConnectionFailedListener
 		}
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		// we only want to do this if no location has been found before. i.e. on app startup
		if (!foundLocationLC && location != null) {
 			foundLocationLC = true;
 			selectItem(mNavDrawerAdapter.getPosition(CATEGORY_SEPARATOR)+1);
 		}
 	}
 
 	@Override
 	public void onConnectionFailed(ConnectionResult connectionResult) {
 		/*
 		 * Google Play services can resolve some errors it detects.
 		 * If the error has a resolution, try sending an Intent to
 		 * start a Google Play services activity that can resolve
 		 * error.
 		 */
 		if (connectionResult.hasResolution()) {
 			try {
 				// Start an Activity that tries to resolve the error
 				connectionResult.startResolutionForResult(
 						this,
 						CONNECTION_FAILURE_RESOLUTION_REQUEST);
 				/*
 				 * Thrown if Google Play services canceled the original
 				 * PendingIntent
 				 */
 			} catch (IntentSender.SendIntentException e) {
 				// Log the error
 				e.printStackTrace();
 			}
 		} else {
 			/*
 			 * If no resolution is available, display a dialog to the
 			 * user with the error.
 			 */
 
 			showErrorDialog(connectionResult.getErrorCode());
 		}
 
 	}
 
 	@Override
 	public void onConnected(Bundle bundle) {
 		mLocationClient.requestLocationUpdates(
 				REQUEST,
 				this);  // LocationListener
 	}
 
 	@Override
 	public void onDisconnected() {
 		// do nothing
 	}
 
 	/**
 	 * Verify that Google Play services is available before making a request.
 	 *
 	 * @return true if Google Play services is available, otherwise false
 	 */
 	private boolean servicesConnected() {
 		// Check that Google Play services is available
 		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
 		// If Google Play services is available
 		if (ConnectionResult.SUCCESS == resultCode) {
 			// In debug mode, log the status
 			Log.d("Location Updates", "Google Play services is available.");
 			// Continue
 			return true;
 			// Google Play services was not available for some reason
 		} else {			
 			// Display an error dialog
 			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
 			if (dialog != null) {
 				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
 				errorFragment.setDialog(dialog);
 				errorFragment.show(getFragmentManager(), "Location Updates");
 			}
 			return false;
 		}
 	}
 
 	/**
 	 * Show a dialog returned by Google Play services for the
 	 * connection error code
 	 *
 	 * @param errorCode An error code returned from onConnectionFailed
 	 */
 	private void showErrorDialog(int errorCode) {
 
 		// Get the error dialog from Google Play services
 		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
 				errorCode,
 				this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
 
 		// If Google Play services can provide an error dialog
 		if (errorDialog != null) {
 
 			// Create a new DialogFragment in which to show the error dialog
 			ErrorDialogFragment errorFragment = new ErrorDialogFragment();
 
 			// Set the dialog in the DialogFragment
 			errorFragment.setDialog(errorDialog);
 
 			// Show the error dialog in the DialogFragment
 			errorFragment.show(getFragmentManager(), "Location Updates");
 		}
 	}
 
 	// Define a DialogFragment that displays the error dialog
 	public static class ErrorDialogFragment extends DialogFragment {
 		// Global field to contain the error dialog
 		private Dialog mDialog;
 		// Default constructor. Sets the dialog field to null
 		public ErrorDialogFragment() {
 			super();
 			mDialog = null;
 		}
 		// Set the dialog to display
 		public void setDialog(Dialog dialog) {
 			mDialog = dialog;
 		}
 		// Return a Dialog to the DialogFragment.
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			return mDialog;
 		}
 	}
 
 
 	/**
 	 * This will return the last location using either the GooglePlayServives location
 	 * finder or the fallback location retriever (LocationRetriever2).
 	 * @return the last location or null.
 	 */
 	private Location getLastLocation() {
 		if (foundLocationLC) {
 			return mLocationClient.getLastLocation();
 		} else if (foundLocationFLR) {
 			return GeneralInfo.location;
 		} else {
 			return null;
 		}
 	}
 
 
 }
