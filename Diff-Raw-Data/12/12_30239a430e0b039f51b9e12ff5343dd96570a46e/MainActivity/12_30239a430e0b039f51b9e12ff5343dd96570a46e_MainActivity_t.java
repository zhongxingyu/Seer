 package com.jangonera.oscilloscope;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.ActionBarActivity;
 import android.view.Gravity;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 
 import com.example.oscilloscope.R;
 import com.jangonera.oscilloscope.ExternalDataService.ExternalDataServiceBinder;
 
 public class MainActivity extends ActionBarActivity {
 	public static int OPEN = 101;
 	public static int CLOSED = 102;
 	private FragmentTransaction ft;
 	private SetupFragment setupFRAG;
 	private GraphsFragment graphsFRAG;
 	//private boolean smallScreen;
 	private BluetoothManager bluetoothManager;
 	private ExternalDataService myService;
 	private ExternalDataContainer externalDataContainer;
 	// use mBound to check if the service is available
 	private boolean mBound;
 
     private DrawerLayout mDrawerLayout;
     private ActionBarDrawerToggle mDrawerToggle;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// Remember whether we were scanning or not
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		externalDataContainer = ExternalDataContainer.getContainer();
 		if (savedInstanceState != null) {
 
 		}
         bluetoothManager = BluetoothManager.getBluetoothManager();
         loadGraphs();
         loadSetup();
         loadDrawer();
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		registerReceiver(bluetoothManager);
 		connectToService();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		checkIfLockDrawerOpen();
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		unbindService(mConnection);
 		unregisterReceiver(bluetoothManager);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 
 	// Scan for probes button in setup
 	public void scanForDevices(View view) {
 		if (isBound()) {
 			bluetoothManager.scanForDevices();
 			invalidateScannedDeviceList();
 			markAsDiscovering();
 		}
 	}
 
 	public ExternalDataService getService() {
 		return myService;
 	}
 
 	public boolean isBound() {
 		return mBound;
 	}
 	
 	public void checkIfLockDrawerOpen(){
		if(graphsFRAG.hasNothingToDisplay()){
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
	        getSupportActionBar().setHomeButtonEnabled(false);
		}
		else{
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
	        getSupportActionBar().setHomeButtonEnabled(true);
		}
 	}
 
 	public void invalidateScannedDeviceList() {
 		if (setupFRAG != null)
 			setupFRAG.invalidateList();
 	}
 	
 	public void invalidateGraphsList() {
 		if (graphsFRAG != null){
 			graphsFRAG.invalidateList();
 		}
 	}
 
 	public void loadSetup() {
 		if(setupFRAG == null) setupFRAG = new SetupFragment();
 		ft = getSupportFragmentManager().beginTransaction();
 		ft.replace(R.id.area_setup, setupFRAG);	
 		ft.commit();
 	}
 
 	public void addGraph(int index) {
 		if(externalDataContainer.getScanProbe(index).addToReadyProbes()){
 			display(Const.NEW_GRAPH_ADDED);
 			//loadGraphs();
 			invalidateGraphsList();
 			//refresh to display which probe is "displaying"
 			invalidateScannedDeviceList();
 		}
 		//else loadGraphs();
 		checkIfLockDrawerOpen();
 	}
 	
 	public void removeGraph(int index){
 		if(externalDataContainer.removeReadyProbe(index)){
 			display(Const.GRAPH_REMOVED);
 			//loadGraphs();
 			invalidateGraphsList();
 			//refresh to display which probe is "displaying"
 			invalidateScannedDeviceList();
 			checkIfLockDrawerOpen();
 		}
 	}
 
 	private void loadGraphs() {
 		if(graphsFRAG == null) graphsFRAG = new GraphsFragment();
 		ft = getSupportFragmentManager().beginTransaction();
 		ft.replace(R.id.area_graphs, graphsFRAG);
 		ft.commit();
 	}
 	
 	public void openDrawer() {
 		if(mDrawerLayout != null) mDrawerLayout.openDrawer(Gravity.LEFT);
     }
     
 	private void registerReceiver(BluetoothManager bluetoothManager) {
 		bluetoothManager.registerContext(this);
 		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
 		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
 		registerReceiver(bluetoothManager, filter);
 	}
 	
 	private void loadDrawer() {
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         if(mDrawerLayout == null) return;
         mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                 R.drawable.ic_drawer, OPEN, CLOSED){
         	@Override
         	public void onDrawerOpened(View drawerView) {
         		super.onDrawerOpened(drawerView);
         		getSupportActionBar().setTitle(getString(R.string.title_activity_setup_fragment));
         	}
         	@Override
 			public void onDrawerClosed(View drawerView) {
 				super.onDrawerClosed(drawerView);
         		getSupportActionBar().setTitle(getString(R.string.app_name));
 			}
         };
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 	}
 
 	// DATA EXCHANGE///////////////////////////////
 	private void connectToService() {
 		Intent intent = new Intent(this, ExternalDataService.class);
 		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
 	}
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// We've bound to LocalService, cast the IBinder and get
 			// LocalService instance
 			ExternalDataServiceBinder binder = (ExternalDataServiceBinder) service;
 			myService = binder.getService();
 			// bluetoothManager.registerService(myService);
 			mBound = true;
 			invalidateScannedDeviceList();
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
 			mBound = false;
 		}
 	};
 
 	public void display(int message) {
 		switch (message) {
 		case Const.BLUETOOTH_UNAVAILABLE:
 			Toast.makeText(this, R.string.blueetooth_unavailable,
 					Toast.LENGTH_SHORT).show();
 			break;
 		case Const.BLUETOOTH_NEW_DEVICE:
 			Toast.makeText(this, R.string.new_device_found, Toast.LENGTH_SHORT)
 					.show();
 			break;
 		case Const.NEW_GRAPH_ADDED:
 			Toast.makeText(this, R.string.new_graph_added, Toast.LENGTH_SHORT)
 					.show();
 			break;
 		case Const.GRAPH_REMOVED:
 			Toast.makeText(this, R.string.graph_removed, Toast.LENGTH_SHORT)
 					.show();
 			break;
 		}
 	}
 
 	public void markAsDiscovering() {
 		if (setupFRAG != null)
 			setupFRAG.markAsDiscovering();
 	}
 
 	public void markAsFinishedDiscovery() {
 		if (setupFRAG != null)
 			setupFRAG.markAsFinishedDiscovery();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if ((requestCode == Const.REQUEST_BLUETOOTH_ON)
 				&& (resultCode == Activity.RESULT_OK)) {
 			scanForDevices(null);
 		}
 
 	}
 
 	public BluetoothManager getBluetoothManager() {
 		return bluetoothManager;
 	}
 	
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         mDrawerToggle.syncState();
     }
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (mDrawerToggle.onOptionsItemSelected(item)) {
           return true;
         }
         return super.onOptionsItemSelected(item);
     }
 }
