 package com.smartpark.activities;
 
 import java.util.Locale;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.smartpark.R;
 import com.smartpark.background.BackOperationService;
 import com.smartpark.background.Ref;
 import com.smartpark.bluetooth.BlueController;
 import com.smartpark.fragments.ControllerListFragment;
 import com.smartpark.fragments.ControllerMapFragment;
 import com.smartpark.fragments.DatePickerFragment;
 import com.smartpark.fragments.UserDemoFragment;
 import com.smartpark.fragments.UserHistoryFragment;
 import com.smartpark.fragments.UserSmartParkFragment;
 import com.smartpark.gps.GPSService;
 
 public class MainActivity extends FragmentActivity implements
 		ActionBar.TabListener {
 	/**
 	 * The {@link android.support.v4.view.PagerAdapter} that will provide
 	 * fragments for each of the sections. We use a
 	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
 	 * will keep every loaded fragment in memory. If this becomes too memory
 	 * intensive, it may be best to switch to a
 	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
 	 */
 	private SectionsPagerAdapter mSectionsPagerAdapter;
 
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	private ViewPager mViewPager;
 
 	/**
 	 * Fragments, different views in application that you swipe, instead of
 	 * activities.
 	 */
 	private Fragment fragment;
 
 	private ActionBar actionBar;
 
 	private TextView gps_text;
 
 
 	// This is used for vibration
 	private Vibrator myVib;
 
 	// Boolean ControllerApplication
 	private boolean isController;
 
 	// Debugging and stuff
 	private static final String TAG = "MainActivity";
 	private static final boolean D = Ref.D;
 
 	// ======== START OF LIFECYCLE METHODS =======================
 	// onCreate and onDestroy must go hand in hand
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.i(TAG, "++ onCreate ++");
 
 		setContentView(R.layout.activity_main);
 
 		
 
 
 		setContentView(R.layout.activity_main);
 
 		myVib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
 		myVib.vibrate(50);
 
 		Ref.activeActivity = this;
 		Log.e(TAG, "---------- in onCreate");
 		startService(new Intent(getBaseContext(), BackOperationService.class));
 
 		if (D)
 			Log.d(TAG,
 					"--> Getting the actionBar and setting its navigation mode");
 
 		// Set up the action bar
 		actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		if (D)
 			Log.d(TAG, "--> instantiating SectionsPagerAdapter");
 		// Create the adapter that will return a fragment for each sections of
 		// the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 
 		if (D)
 			Log.d(TAG, "--> passing SectionsPagerAdapter to ViewPager");
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		/*
 		 * When swiping between different sections, select the corresponding
 		 * tab. We can also use ActionBar.Tab#select() to do this if we have a
 		 * reference to the Tab.
 		 */
 		mViewPager
 				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 					@Override
 					public void onPageSelected(int position) {
 						if (D)
 							Log.d(TAG, "position " + position);
 						actionBar.setSelectedNavigationItem(position);
 					}
 				});
 		// For each of the sections in the app, add a tab to the action bar.
 		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
 			// Create a tab with text corresponding to the page title defined by
 			// the adapter. Also specify this Activity object, which implements
 			// the TabListener interface, as the callback (listener) for when
 			// this tab is selected.
 			actionBar.addTab(actionBar.newTab()
 					.setText(mSectionsPagerAdapter.getPageTitle(i))
 					.setTabListener(this));
 		}
 
 		if (D)
 			Log.d(TAG, "--> loading from savedInstanceState");
 
 		// Restore additional variables and objects from last session
 		if (savedInstanceState != null) {
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.i(TAG, "++ onDestroy ++");
 		/*
 		 * Most resources are being handles by the bgTherad and will be released
 		 * by it and not here. This method is only responsible for resources
 		 * taken by this activity.
 		 */
 
 		Ref.flagMainActivityInFront = false;
 
 	}// ------------------------------------------------------------------------
 		// ============================================================
 		// onStart and onStop must go hand in hand
 		// (onRestart can also be used before onStart is invoked)
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		Log.i(TAG, "++ onStart ++");
 
 	}// -------------------------------------------------------------------------------------
 
 	@Override
 	public void onRestart() {
 		super.onRestart();
 		Log.i(TAG, "++ onRestart ++");
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		Log.i(TAG, "++ onStop ++");
 
 	}
 
 	// ============================================================
 	// onResume and onPause must go hand in hand
 	@Override
 	public void onResume() {
 		super.onResume();
 		Log.i(TAG, "++ onResume ++");
 
 		// Ref.activeActivity = this;
 		// Ref.bgThread.activityMAIN = true;
 
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		Log.i(TAG, "++ onPause ++");
 
 		// We have to save everything in this method for later use
 		// Ref.bgThread.activityMAIN = false;
 
 	}// ------------------------------------------------------------
 
 	/**
 	 * This method will be invoked right before onPause() is invoked and is used
 	 * to save certain data that we wish to hold for the next session instead of
 	 * recreating them.
 	 */
 	@Override
 	public void onSaveInstanceState(final Bundle outState) {
 		super.onSaveInstanceState(outState);
 		Log.i(TAG, "++ onSaveInstanceState ++");
 
 	
 
 	}// ------------------------------------------------------------
 		// ======= END OF LIFECYCLE METHODS ===========================
 
 	// =======================
 	// onCLICK-METHODS SECTION
 	// =======================
 
 	@Override
 	public void onBackPressed() {
 		Log.e(TAG, "++ onBackPressed ++");
 		myVib.vibrate(20);
 		stopService(new Intent(getBaseContext(), BackOperationService.class));
 		finish();
 
 	}
 
 	/*
 	 * Fragment SmartPark
 	 */
 	public void onClickBtnPark(View view) {
 		if (((Button) findViewById(R.id.btnTogglePark)).getText()
 				.equals("Park")) {
 			Toast.makeText(this, "Parking...", Toast.LENGTH_SHORT).show();
 			((Button) findViewById(R.id.btnTogglePark)).setText("Stop Parking");
 			return;
 		} else
 			((Button) findViewById(R.id.btnTogglePark)).setText("Park");
 
 		Toast.makeText(this, "Stopped Parking...", Toast.LENGTH_SHORT).show();
 	}
 
 	/*
 	 * Fragment Demo
 	 */
 
 	public void onClickBtnParkRolf(View view) {
 		myVib.vibrate(20);
 		Toast.makeText(this, "Parked Rolf", Toast.LENGTH_SHORT).show();
 	}
 
 	public void onClickBtnParkKristina(View view) {
 		myVib.vibrate(20);
 		Toast.makeText(this, "Parked Kristina", Toast.LENGTH_SHORT).show();
 	}
 
 	public void onClickBtnParkTommy(View view) {
 		myVib.vibrate(20);
 		Toast.makeText(this, "Parked Tommy", Toast.LENGTH_SHORT).show();
 	}
 
 	public void onClickBtnStopParkRolf(View view) {
 		myVib.vibrate(20);
 		Toast.makeText(this, "Stopped Parking Rolf", Toast.LENGTH_SHORT).show();
 	}
 
 	public void onClickBtnStopParkKristina(View view) {
 		myVib.vibrate(20);
 		Toast.makeText(this, "Stopped Parking Kristina", Toast.LENGTH_SHORT)
 				.show();
 	}
 
 	public void onClickBtnStopParkTommy(View view) {
 		myVib.vibrate(20);
 		Toast.makeText(this, "Stopped Parking Tommy", Toast.LENGTH_SHORT)
 				.show();
 	}
 
 	public void onClickBtnLogin(View view) {
 		myVib.vibrate(20);
 		Toast.makeText(this, "Logging into Server", Toast.LENGTH_SHORT).show();
 
 		Ref.bgThread.sendByTCP("Login;910611:artur");
 	}
 
 	public void onClickBtnSendLocation(View view) {
 		myVib.vibrate(20);
 		Toast.makeText(this, "Sending GPS Location", Toast.LENGTH_SHORT).show();
 	}
 
 	public void onClickBtnPairedDevicesCount(View view) {
 		myVib.vibrate(20);
 		Log.i(TAG, "++ pairedDevicesCount ++");
 
 		/* @formatter:off */
 		String str = Ref.activeActivity 			!= null ? "\nactiveActivity OK "		: "\nactiveActivity null ";
 		str += Ref.bgThread 						!= null ? "\nbgThread OK " 			: "\nbgThread null ";
 		Log.e(TAG, str);
 		/* @formatter:on */
 	}
 
 	public void onClickBtnIsBTavailable(View view) {
 		myVib.vibrate(20);
 		Ref.bgThread.sendByBT("1");
 		Log.d(TAG, "wrote 1");
 	}
 
 	public void onClickBtnIsBTEnable(View view) {
 		myVib.vibrate(20);
 		Ref.bgThread.sendByBT("999950");
 		Log.d(TAG, "wrote 10");
 	}
 
 	public void onClickBtnStartGPS(View view) {
 		myVib.vibrate(20);
 		Log.i(TAG, "++ startGPS ++");
 
 	}
 
 	public void onClickBtnEndGPS(View view) {
 		stopService(new Intent(getBaseContext(), GPSService.class));
 	}
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 		// When the given tab is selected, switch to the corresponding page in
 		// the ViewPager.
 		mViewPager.setCurrentItem(tab.getPosition());
 	}
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	/*
 	 * debugFragment Button Events
 	 */
 
 	/**
 	 * Connect to server onClick-method.
 	 * 
 	 * @param view
 	 */
 	public void connect(View view) {
 		if (D)
 			Log.i(TAG, "++ connect ++");
 		Toast.makeText(this, "connecting to server", Toast.LENGTH_LONG).show();
 		// new ConnectTask().execute("");
 		// Ref.tcpClient = new TCPController();
 	}
 
 	/**
 	 * Disconnect from server onClick-method.
 	 * 
 	 * @param view
 	 */
 	public void disconnect(View view) {
 		if (D)
 			Log.i(TAG, "++ disconnect ++");
 		// if (Ref.tcpClient != null) {
 		Toast.makeText(this, "this button no longer works", Toast.LENGTH_LONG)
 				.show();
 		// Ref.tcpClient.disconnect();
 		// }
 	}
 
 	// =================================
 	// FINISHED WORKING ON THESE METHODS
 	// =================================
 
 	/*
 	 * This will be invoked by the startActivityForResult of the
 	 * blueController-class
 	 */
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.i(TAG, "++ onActivityResult ++");
 
 		switch (requestCode) {
 		case BlueController.REQUEST_ENABLE_BT:
 			if (resultCode == Activity.RESULT_OK) {
 				Toast.makeText(this, "Enabling Bluetooth", Toast.LENGTH_SHORT)
 						.show();
 			} else {
 				Toast.makeText(this, "Cancel enabling Bluetooth",
 						Toast.LENGTH_SHORT).show();
 			}
 			break;
 		case BlueController.REQUEST_DISCOVERABLE_BT:
 			if (resultCode == Activity.RESULT_OK) {
 				Toast.makeText(this, "Bluetooth Discoverable",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(this, "Bluetooth not Discoverable",
 						Toast.LENGTH_SHORT).show();
 			}
 			break;
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * Create ActionMenu with settings and add our own menu items.
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		if (D)
 			Log.i(TAG, "++ onCreateOptionsMenu ++");
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		CreateMenu(menu);
 		return true;
 	}
 
 	/**
 	 * Create Menu Create Action Menu that the application will have to start
 	 * other activities.
 	 * 
 	 * @param menu
 	 */
 	private void CreateMenu(Menu menu) {
 		// Debug stuff
 		if (D) {
 			Log.i(TAG, "++ CreateMenu ++");
 		}
 		menu.setQwertyMode(true);
 		MenuItem aMenu1 = menu.add(0, 0, 0, "Login");
 		aMenu1.setAlphabeticShortcut('a');
 
 		MenuItem aMenu2 = menu.add(0, 1, 1, "Item 2");
 		aMenu2.setAlphabeticShortcut('b');
 
 		MenuItem aMenu3 = menu.add(0, 2, 2, "Item 3");
 		aMenu3.setAlphabeticShortcut('c');
 
 		MenuItem aMenu4 = menu.add(0, 3, 3, "Item 4");
 		aMenu4.setAlphabeticShortcut('d');
 
 	}
 
 	/**
 	 * On ActionMenu Select Do something when that get selected in the
 	 * ActionMenu
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (D)
 			Log.i(TAG, "++ onOptionsItemSelected ++");
 
 		if (D) {
 			Log.d(TAG, "Item: " + item.toString() + "\nID: " + item.getItemId()
 					+ "\nIntent: " + item.getIntent());
 		}
 
 		// TODO Cleanup!
 
 		// On select
 		switch (item.getItemId()) {
 		case 0:
 			Toast.makeText(this, "You clicked on Login", Toast.LENGTH_SHORT)
 					.show();
 			startActivity(new Intent(this, LoginActivity.class));
 			return true;
 
 		case 1:
 			Toast.makeText(this, "You clicked on Item 2", Toast.LENGTH_SHORT)
 					.show();
 			return true;
 
 		case 2:
 			Toast.makeText(this, "You clicked on Item 3", Toast.LENGTH_SHORT)
 					.show();
 			return true;
 
 		case 3:
 			Toast.makeText(this, "You clicked on Item 4", Toast.LENGTH_SHORT)
 					.show();
 			return true;
 
 		default:
 			Toast.makeText(this, "You clicked on Settings", Toast.LENGTH_SHORT)
 					.show();
 			startActivity(new Intent(this, SettingsActivity.class));
 		}
 		return false;
 	}
 
 	// ================
 	// INTERNAL CLASSES
 	// ================
 
 	/**
 	 * This is a sleep-class. This will be used whenever we want to wait for
 	 * other parts of the program to get ready. Methods can start a thread that
 	 * is taking a long time to finish, then they will need to wait processing
 	 * more code before the started threads return.
 	 * 
 	 * @author Saeed
 	 * 
 	 */
 	public class TestSleep {
 		public void main(String[] args) {
 			System.out.println("Do this stuff");
 			try {
 				Thread.currentThread();
 				Thread.sleep(3000);
 			} catch (Exception e) {
 				Log.e("Therad sleep", "--> Sleep didn't work");
 			}
 		}
 	}
 
 	/**
 	 * A FragmentPagerAdapter that returns a fragment corresponding to one of
 	 * the sections/tabs/pages.
 	 */
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		/**
 		 * {@link Fragment} This sets the different fragment views.
 		 */
 		@Override
 		public Fragment getItem(int position) {
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a DummySectionFragment (defined as a static inner class
 			// below) with the page number as its lone argument.
 			Bundle args = new Bundle();
 
 			switch (position) {
 
 			case 0:
 				fragment = new UserSmartParkFragment();
 				args.putInt(ControllerListFragment.ARG_SECTION_NUMBER,
 						position + 1);
 				fragment.setArguments(args);
 				break;
 
 			case 1:
 				fragment = new UserHistoryFragment();
 				args.putInt(ControllerListFragment.ARG_SECTION_NUMBER,
 						position + 1);
 				fragment.setArguments(args);
 				break;
 
 			case 2:
 				fragment = new UserDemoFragment();
 				args.putInt(ControllerListFragment.ARG_SECTION_NUMBER,
 						position + 1);
 				fragment.setArguments(args);
 				break;
 
 			case 3:
 				fragment = new ControllerListFragment();
 				args.putInt(ControllerListFragment.ARG_SECTION_NUMBER,
 						position + 1);
 				fragment.setArguments(args);
 				break;
 
 			case 4:
 				fragment = new ControllerMapFragment();
 				args.putInt(ControllerMapFragment.ARG_SECTION_NUMBER,
 						position + 1);
 				fragment.setArguments(args);
 				break;
 
 			}
 			return fragment;
 		}
 
 		/**
 		 * Getcount This sets how many swipe pages you want to have in the
 		 * application.
 		 */
 		@Override
 		public int getCount() {
 			// Show 5 total pages.
 
 			if (isController)
 				return 4;
 
 			return 5;
 		}
 
 		/**
 		 * {@link Character} This sets the name on the different sections of the
 		 * fragments.
 		 */
 		@Override
 		public CharSequence getPageTitle(int position) {
 			Locale l = Locale.getDefault();
 			switch (position) {
 			case 0:
 				return getString(R.string.title_section1).toUpperCase(l);
 			case 1:
 				return getString(R.string.title_section2).toUpperCase(l);
 			case 2:
 				return getString(R.string.title_section3).toUpperCase(l);
 			case 3:
 				return getString(R.string.title_section4).toUpperCase(l);
 			case 4:
 				return getString(R.string.title_section5).toUpperCase(l);
 			}
 			return null;
 		}
 	}
 }
