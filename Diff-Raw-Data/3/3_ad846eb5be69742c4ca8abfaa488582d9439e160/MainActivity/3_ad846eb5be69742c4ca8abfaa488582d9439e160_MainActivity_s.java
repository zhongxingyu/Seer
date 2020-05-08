 package com.smartpark;
 
 import java.util.Locale;
 import java.util.Set;
 import android.util.Log;
 
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.FragmentTransaction;
 
 import android.bluetooth.BluetoothDevice;
 
 import android.content.DialogInterface;
 import android.content.Intent;
 
 import android.os.Bundle;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 
 import com.smartpark.bluetooth.*;
 import com.smartpark.fragments.*;
 import com.smartpark.interfaces.*;
 import com.smartpark.tcp.*;
 
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
 	SectionsPagerAdapter mSectionsPagerAdapter;
 
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	ViewPager mViewPager;
 
 	/**
 	 * Fragments, different views in application that you swype, instead of
 	 * activities.
 	 */
 	Fragment fragment;
 	/**
 	 * Bluetooth, this instance will control every aspect of the bluetooth
 	 * adapter
 	 */
 	BlueController bluetooth;
 	
 	ActionBar actionBar;
 
 	// Debugging and stuff
 	private static final String TAG = "MainActivityDebug";
 	private static final boolean D = Ref.d;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Debug stuff
 		if (D) {
 			Log.d(TAG, "Starting onCreate, loading savedInstanceState");
 		}
 		// Restore additional variables and objects from last session
 		if (savedInstanceState != null) {
 
 		}
 		// Debug stuff
 		if (D) {
 			Log.d(TAG, "repopulate References.backgroundThread and run it");
 		}
 		// Creates and starts the background-operation-thread
 		if (Ref.backgroundThread == null) {
 			Ref.backgroundThread = new BackgoundOperationThread();
 			Ref.backgroundThread.start();
 		} else if (Ref.backgroundThread.isAlive() == false) {
 			Ref.backgroundThread.start();
 		}
 		// Debug stuff
 		if (D) {
 			Log.d(TAG, "working on actionbar");
 		}
 		// Set up the action bar
 		actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		// Create the adapter that will return a fragment for each sections
 		// of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 		
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		// When swiping between different sections, select the corresponding
 		// tab. We can also use ActionBar.Tab#select() to do this if we have
 		// a reference to the Tab.
 		mViewPager
 				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 					@Override
 					public void onPageSelected(int position) {
 						Log.d(TAG, "position " + position);
 						actionBar.setSelectedNavigationItem(position);
 					}
 				});
 		// Restoring the position of the actionBar
 		if(savedInstanceState != null){
 			Log.d(TAG, "actionbar setting");
 			Log.d(TAG, "actionbar setting " + savedInstanceState.getInt("ActionBarPosition"));
 			
 //			actionBar.setSelectedNavigationItem(savedInstanceState.getInt("ActionBarPosition"));
 			Log.d(TAG, "actionbar set");
 		}
 		// Debug stuff
 		if (D) {
 			Log.d(TAG, "4");
 		}
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
 		// Debug stuff
 		if (D) {
 			Log.d(TAG, "before bt");
 		}
 		// intantiate the bluetooth-control-class
 		bluetooth = new BlueController();
 		// Debug stuff
 		if (D) {
 			Log.d(TAG, "after bt");
 		}
 	}
 
 
 	public void pairedDevicesCount(View view) {
 		Log.d(TAG, "pairedDevicesCount invoked");
 		Toast.makeText(this, "pairedDevicesCount() invoked", Toast.LENGTH_SHORT)
 				.show();
 
 		if (!BlueController.btAdapter.isEnabled()) {
 			Toast.makeText(this, "adapter is not enabled", Toast.LENGTH_SHORT)
 					.show();
 		}
 
 		Toast.makeText(this, "getting pairedlist", Toast.LENGTH_SHORT).show();
 
 		Set<BluetoothDevice> pairedDevices = bluetooth.getPairedDevicesList();
 		if (pairedDevices != null) {
 			Toast.makeText(this, "" + pairedDevices.size(), Toast.LENGTH_SHORT)
 					.show();
 		}
 		Toast.makeText(this, "end of method", Toast.LENGTH_SHORT).show();
 		Log.d(TAG, "pairedDevicesCount ends");
 
 		AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
 		builder1.setTitle("Title");
 		builder1.setMessage("my message");
 		builder1.setCancelable(true);
 		builder1.setNegativeButton(android.R.string.no,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						// TODO Add your code for the button here.
 					}
 				});
 		builder1.setNeutralButton("neutral",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						// TODO Add your code for the button here.
 					}
 				});
 		builder1.setPositiveButton(android.R.string.ok,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						// TODO Add your code for the button here.
 					}
 				});
 
 		AlertDialog alert = builder1.create();
 		alert.show();
 	}
 
 	public void isBTavailable(View view) {
 		Log.d(TAG, "invoking isBTavailable");
 		if (BlueController.btAdapter != null) {
 			Toast.makeText(this, "availabe", Toast.LENGTH_SHORT).show();
 		} else {
 			Toast.makeText(this, "not availabe", Toast.LENGTH_SHORT).show();
 		}
 		Log.d(TAG, "done invoking isBTavailable");
 	}
 
 	public void isBTEnable(View view) {
 		Log.d(TAG, "enable bluetooth if disabled");
 		if (BlueController.btAdapter.isEnabled()) {
 			Toast.makeText(this, "Enabled", Toast.LENGTH_SHORT).show();
 		} else {
 			Toast.makeText(this, "Disabled", Toast.LENGTH_SHORT).show();
 			bluetooth.enableAdapter(this);
 		}
 		Log.d(TAG, "Enabling done");
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO
 		Log.d(TAG, "onActivityResult");
 	}
 
 	/**
 	 * This method will be invoked right before onPause() or onDestroy() is
 	 * invoked and is used to save certain classes that we wish to hold for the
 	 * next session instead of recreating it.
 	 */
 	@Override
 	public void onSaveInstanceState(final Bundle outState) {
 		super.onSaveInstanceState(outState);
 		Log.d(TAG, "onSaveInstanceState");
 //		outState.putInt("ActionBarPosition", actionBar.getSelectedNavigationIndex());
 		
 		Log.d(TAG, "" + actionBar.getSelectedNavigationIndex());
 
 	}
 
 	/**
 	 * Create ActionMenu with settings and add our own menu items.
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Debug stuff
 		if (D) {
 			Log.d(TAG, "onCreateOptionsMenu");
 		}
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
 			Log.d(TAG, "CreateMenu");
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
 		// Debug stuff
 		if (D) {
 			Log.d(TAG, "onOptionsItemSelected");
 		}
 		// Debug stuff
 		if (D) {
 			Log.d(TAG, "Item: " + item.toString() + "\nID: " + item.getItemId()
 					+ "\nIntent: " + item.getIntent());
 		}
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
 	public void connect(View view) {
 		// Debug stuff
 		if (D) {
 			Log.d(TAG, "connect");
 		}
 		Toast.makeText(this, "connecting...", Toast.LENGTH_LONG).show();
 
 		// new ConnectTask().execute("");
 
 		Ref.client = new TCPClient(new OnMessageReceived() {
 			@Override
 			// here the messageReceived method is implemented
 			public void messageReceived(String message) {
 				Log.e(TAG, message);
 				// this method calls the onProgressUpdate
 				// publishProgress(message);
 
 			}
 		});
 
 		Ref.clientThread = new Thread(Ref.client);
 		Ref.clientThread.start();
 
 	}
 
 	/**
 	 * Disconnect from server action.
 	 * @param view
 	 */
 	public void disconnect(View view) {
 		// Debug stuff
 		if (D) {
 			Log.d(TAG, "disconnect");
 		}
 		if (Ref.client != null) {
 			Toast.makeText(this, "dissconnecting...", Toast.LENGTH_LONG).show();
 			Ref.client.stopClient();
 		}
 	}
 
 	
 	/**
 	 * Only for inspection, removed later.
 	 */
 	// public class ConnectTask extends AsyncTask<String, String, TCPClient> {
 	//
 	// @Override
 	// protected TCPClient doInBackground(String... message) {
 	// // Debug stuff
 	// if (D) {
 	// Log.d(TAG, "class ConnectTask doInBackground");
 	// }
 	//
 	// // we create a TCPClient object and
 	// References.client = new TCPClient(new OnMessageReceived() {
 	// @Override
 	// // here the messageReceived method is implemented
 	// public void messageReceived(String message) {
 	// Log.e(TAG, message);
 	// // this method calls the onProgressUpdate
 	// publishProgress(message);
 	// }
 	// });
 	// References.client.run();
 	//
 	// return null;
 	// }
 	//
 	// @Override
 	// protected void onProgressUpdate(String... values) {
 	// // Debug stuff
 	// if (D) {
 	// Log.d(TAG, "onProgressUpdate");
 	// }
 	// super.onProgressUpdate(values);
 	//
 	// // in the arrayList we add the messaged received from server
 	// // arrayList.add(values[0]);
 	// // notify the adapter that the data set has changed. This means that
 	// // new message received
 	// // from server was added to the list
 	// // mAdapter.notifyDataSetChanged();
 	// }
 	// }
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
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
 				fragment = new SmartParkFragment();
 				args.putInt(DebugFragment.ARG_SECTION_NUMBER, position + 1);
 				fragment.setArguments(args);
 				break;
 
 			case 1:
 				fragment = new GPSFragment();
 				args.putInt(DebugFragment.ARG_SECTION_NUMBER, position + 1);
 				fragment.setArguments(args);
 				break;
 
 			case 2:
 				fragment = new BluetoothFragment();
 				args.putInt(DebugFragment.ARG_SECTION_NUMBER, position + 1);
 				fragment.setArguments(args);
 				break;
 
 			case 3:
 				fragment = new DebugFragment();
 				args.putInt(DebugFragment.ARG_SECTION_NUMBER, position + 1);
 				fragment.setArguments(args);
 				break;
 
 			case 4:
 				fragment = new DummySectionFragment();
 				args.putInt(DummySectionFragment.ARG_SECTION_NUMBER,
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
