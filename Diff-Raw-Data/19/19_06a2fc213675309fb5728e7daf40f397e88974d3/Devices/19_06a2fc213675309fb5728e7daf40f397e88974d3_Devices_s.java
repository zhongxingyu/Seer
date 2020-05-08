 package no.group09.utils;
 
 /*
  * Licensed to UbiCollab.org under one or more contributor
  * license agreements.  See the NOTICE file distributed 
  * with this work for additional information regarding
  * copyright ownership. UbiCollab.org licenses this file
  * to you under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import no.group09.ucsoftwarestore.MainActivity;
 import no.group09.ucsoftwarestore.R;
 import java.util.ArrayList;
 import java.util.HashMap;
 import no.group09.connection.BluetoothConnection;
 import no.group09.connection.BluetoothConnection.ConnectionState;
 import no.group09.fragments.BluetoothDeviceAdapter;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 import android.view.View.OnClickListener;
 
 /**
  * This class searches for the BT devices in range and put them in a list
  */
 public class Devices extends Activity  {
 
 	/**Should be true if only arduinos is to be showed. False otherwise */
 	private boolean ONLY_SHOW_ARDUINOS = false;
 	private SharedPreferences sharedPref;
 	private ProgressDialog progressDialog;
 	private static final String TAG = "DEVICES";
 	private static final int REQUEST_ENABLE_BT = 1;
 	private ListView deviceList;
 	private BluetoothDeviceAdapter listAdapter;
 	private BluetoothAdapter btAdapter; 
 	private ArrayList<HashMap<String, String>> category_list;
 	private IntentFilter filter;
 	private Button refresh;
 	private Button addDeviceButton, browseShowButton;
 	private ProgressBar progressBar;
 	private ArrayList<HashMap<String, String>> device_list;
 	private boolean alreadyChecked = false;
 	private ArrayList<BluetoothDevice> btDeviceList = new ArrayList<BluetoothDevice>();
 	private MyBroadcastReceiver actionFoundReceiver;
 	public static final String MAC_ADDRESS = "MAC_ADDRESS";
 	static Context context;
 	private static ConnectionState oldState;
 
 	/** 
 	 * Message text that is to be printed to the user when the attempt to 
 	 * connect was unsuccessful.
 	 */
 	private final static String NEGATIVE_MESSAGE = "The connection was not successfull.\nPlease try again.";
 	private int savedPosition;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//Set the xml layout
 		setContentView(R.layout.devices);
 
 		//Fetching the shared preferences object used to write the preference file
 		sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 
 		//Progressdialog used to indicate that the program is connecting to a BT device
 		progressDialog = new ProgressDialog(this);
 
 		//progressBar = (ProgressBar) findViewById(R.id.progressBar1);
 		progressBar = (ProgressBar) findViewById(R.id.progbar);
 
 		registerBroadcastReceiver();
 
 		// Getting the Bluetooth adapter
 		btAdapter = BluetoothAdapter.getDefaultAdapter();
 
 		category_list = new ArrayList<HashMap<String, String>>();
 
 		// Getting adapter by passing xml data ArrayList
 		listAdapter = new BluetoothDeviceAdapter(getBaseContext(), category_list); 
 
 		//List of devices
 		device_list = new ArrayList<HashMap<String, String>>();
 
 		//Get the xml to how the list is structured
 		deviceList = (ListView) findViewById(R.id.bluetooth_devices_list);
 
 		//Get the adapter for the device list
 		listAdapter = new BluetoothDeviceAdapter(getBaseContext(), device_list);        
 
 		//Set the adapter
 		deviceList.setAdapter(listAdapter);
 
 		//Initialize the device list
 		initializeDeviceList();
 
 		//Add refresh button to UI
 		refresh = (Button) findViewById(R.id.refresh);
 
 		refresh.setVisibility(View.GONE);
 
 		//If we are connected to a device that is not in the list
 		if(isConnected()){
 			addToDeviceListAndSelectIt();
 		}
 
 		checkBTState();
 
 		//Add the button that opens the 'Add device' screen
 		addDeviceButton = (Button) findViewById(R.id.add_device_button);
 
 		//Add the button that takes you directly to the shop.
 		browseShowButton = (Button) findViewById(R.id.browse_shop_button);
 
 		addButtonFunctionality();
 	}
 
 	/**
 	 * Method that initializes the device list and creates a service if an
 	 * item is clicked.
 	 */
 	public void initializeDeviceList() {
 		//Click event for single list row
 		deviceList.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
 				//Cancel discovery when an item in the list is clicked
 				btAdapter.cancelDiscovery();
 
 				String macAddress = listAdapter.getMacAddress(position);
 
 				Intent serviceIntent = new Intent(getApplicationContext(), no.group09.utils.BtArduinoService.class);
 				serviceIntent.putExtra(MAC_ADDRESS, macAddress);
 
 				if(isMyServiceRunning()){
 					BtArduinoService.getBtService().getBluetoothConnection().setConnectionState(ConnectionState.STATE_DISCONNECTED);
 					BtArduinoService.getBtService().getBluetoothConnection().disconnect();
 					stopService(serviceIntent);
 				}
 				startService(serviceIntent);
 
 				ProgressDialogTask task = new ProgressDialogTask();
 				task.execute();
 				savedPosition = position;
 
 			}
 		});	
 
 		deviceList.setOnItemLongClickListener(new OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
 
 				disconnectButton(position);
 
 				return false;
 			}
 		});
 	}
 
 	/**
 	 * Checks whether a service is running or not
 	 * 
 	 * @return true if the service is running, false if not
 	 */
 	public boolean isMyServiceRunning() {
 		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 			if (BtArduinoService.class.getName().equals(service.service.getClassName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Check if there is an active connection
 	 * 
 	 * @return true if an active connection is existing, false if not
 	 */
 	public static boolean isConnected(){
 		if(BtArduinoService.getBtService() != null){
 			if(BtArduinoService.getBtService().getBluetoothConnection() != null){
 				return BtArduinoService.getBtService().getBluetoothConnection().isConnected();
 			}
 		}
 		return false;
 	}
 
 	public static boolean connectionHasFailed() {
 
 		if(BtArduinoService.getBtService() != null){
 			if(BtArduinoService.getBtService().getBluetoothConnection() != null){
 				BluetoothConnection connection = BtArduinoService.getBtService()
 						.getBluetoothConnection();
 				if (oldState == null) {
 					oldState = connection.getConnectionState();
 					Log.d(TAG, "State changed. New state: " + oldState);
 				}
 				else {
 					if (oldState == ConnectionState.STATE_CONNECTING && 
 							connection.getConnectionState() == ConnectionState.STATE_DISCONNECTED) {
 						Log.d(TAG, "Was connecting. Now disconnected");
 						return true;
 					}
 					else if (oldState != connection.getConnectionState()) {
 						oldState = connection.getConnectionState();
 						Log.d(TAG, "State changed. New state: " + oldState);
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Adds the functionality to all the buttons in Devices screen
 	 */
 	public void addButtonFunctionality() {
 
 		refresh.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				//Clear the list of BT devices
 				device_list.clear();
 
 				//Clear the list of BT device objects
 				btDeviceList.clear();
 
 				//Notify the adapter that the list is now empty
 				listAdapter.notifyDataSetChanged();
 
 				//Check if we are connected to a device
 				if(BtArduinoService.getBtService() != null){
 					if(BtArduinoService.getBtService().getBluetoothConnection() != null){
 						if(!BtArduinoService.getBtService().getBluetoothConnection().isConnected()){
 
 							//We are not connected to anything, update the title
 							setTitle("Devices");
 						}
 						else{
 
 							//We are connected to a device, update the title
 							HashMap<String, String> map = new HashMap<String, String>();
 							map.put("name", sharedPref.getString("connected_device_name", "null"));
 							map.put("mac", sharedPref.getString("connected_device_mac", "null"));
 							map.put("pager", "708");
 
 							//Check if there is a device in the list with the same mac address
 							if(!isDeviceWithMacInList(device_list, sharedPref.getString("connected_device_mac", "null"))){
 								device_list.add(map);
 								deviceList.setItemChecked(0, true);
 							}
 						}
 					}
 				}
 
 				//Scan for new BT devices
 				checkBTState();
 			}
 		});
 
 		addDeviceButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				startActivity(new Intent(getBaseContext(), AddDeviceScreen.class));
 			}
 		});
 
 		browseShowButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				//				finish();	//finishes the activity
 				startActivity(new Intent(getBaseContext(), MainActivity.class));
 			}
 		});
 	}
 
 	public static Context getContext() {
 		return context;
 	}
 
 	/**
 	 * Method used to register the broadcast receiver for communicating with
 	 * bluetooth API
 	 */
 	private void registerBroadcastReceiver() {
 		//Register the BroadcastReceiver
 		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
 		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
 		//Connected devices
 		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
 
 		actionFoundReceiver = new MyBroadcastReceiver();
 		//Registers the BT receiver with the requested filters
 		//Don't forget to unregister during onDestroy
 		registerReceiver(actionFoundReceiver, filter);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == REQUEST_ENABLE_BT) {
 			checkBTState();
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		unregisterReceiver(actionFoundReceiver);
 		if (btAdapter != null) {
 			btAdapter.cancelDiscovery();
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		//Clear the list of BT devices
 		device_list.clear();
 
 		//Clear the list of BT device objects
 		btDeviceList.clear();
 
 		if(isConnected()){
 			addToDeviceListAndSelectIt();
 		}
 
 		//Notify the adapter that the list is now empty
 		listAdapter.notifyDataSetChanged();
 
 		setActivityTitle();
 
 		checkBTState();
 	}
 
 	/**
 	 * If a valid connection is existing, this method fetches the name and MAC
 	 * address of the connected device from preferences and adds it and selects
 	 * it in the device list.
 	 */
 	private void addToDeviceListAndSelectIt(){
 		if(isConnected()){
 			HashMap<String, String> map = new HashMap<String, String>();
 			String deviceName = sharedPref.getString("connected_device_name", "null");
 			String deviceMac = sharedPref.getString("connected_device_mac", "null");
 
 			map.put("name", deviceName);
 			map.put("mac", deviceMac);
 			map.put("pager", "708");
 
 			//If we are connected but it does'nt appear to be in the device list: add it
 			if(!isDeviceWithMacInList(device_list, deviceMac) && !deviceName.equals("null") && !deviceMac.equals("null")){
 				device_list.add(map);
 				deviceList.setItemChecked(0, true);
 			}
 		}
 	}
 
 	/**
 	 * Method checking if the given MAC address in the device list from before
 	 * to avoid duplicates.
 	 * 
 	 * @param list The current device list
 	 * @param deviceMac The MAC address of the device that is to be added to the
 	 * device list
 	 * @return true if the device is in the device list from before, false if not.
 	 */
 	private boolean isDeviceWithMacInList(ArrayList<HashMap<String, String>> list, String deviceMac){
 
 		for(HashMap<String, String> map : list){
 			if(map.get("mac").equals(deviceMac)){
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/** Add the connected device name to the title if connected */
 	private void setActivityTitle(){
 		String appName = sharedPref.getString("connected_device_name", "null");
 
 		if(isConnected() && !appName.equals("null")){
 			setTitle("Devices - " + appName);
 		}
 		else{
 			setTitle("Devices");
 		}
 	}
 
 	/**
 	 * Method used to check the state of the bluetooth connection. First checks
 	 * if the Android device supports bluetooth connections. Starts a bluetooth
 	 * discovery and sets the progress bar visible if bluetooth is enabled on the
 	 * Android device. If bluetooth is disabled, it asks the user if he wants to 
 	 * enable it.
 	 */
 	private void checkBTState() {
 
 		//Check if the Android support bluetooth
 		if(btAdapter==null) return;
 		else {
 
 			//Check if the BT is turned on
 			if (btAdapter.isEnabled()) {
 
 				//Show the progress bar
 				progressBar.setVisibility(View.VISIBLE);
 				refresh.setVisibility(View.GONE);
 
 				// Starting the device discovery
 				btAdapter.startDiscovery();
 			}
 
 			//If the BT is not turned on, request the user to turn it on
 			else if (!btAdapter.isEnabled() && !alreadyChecked){
 				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
 				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
 
 				//The user have already turned it on or off: don't request again
 				alreadyChecked = true;
 			}
 		}
 	}
 
 	/**
 	 * Method used to only show valid arduinos in the device list. Should be called
 	 * each time a new device is to be added to the device list.
 	 * 
 	 * The code used for a valid arduino device is 708. This is originally the
 	 * code used a pager.
 	 * 
 	 * @param device The device to be checked if it is valid
 	 * @return true if the device is valid, false if not
 	 */
 	private boolean onlyShowArduinos(BluetoothDevice device){
 
 		//This is true if you want to hide other devices than arduinos
 		if(sharedPref.getBoolean("bluetooth_type", false)){
 
 			//Check if the device is an arduino
 			if((device.getBluetoothClass().toString()).equals("708")){
 				return true;
 			}
 
 			//The device was not a pager
 			else return false;
 		}
 
 		//Show the device
 		return true;
 	}
 
 	/**
 	 * Broadcast receiver class. Used to receive Android Bluetooth API communication
 	 */
 	private class MyBroadcastReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			//If discovery started
 			if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
 				Log.d(TAG, "\nDiscovery Started...");
 			}
 
 			//If a device is found
 			if (BluetoothDevice.ACTION_FOUND.equals(action)){
 
 				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 
 				//If the bluetooth class is named 708 that we made as a 'standard' for recognizing arduinos
 				if(onlyShowArduinos(device) && !btDeviceList.contains(device)){
 
 					//Adding found device
 					HashMap<String, String> map = new HashMap<String, String>();
 					map.put("name", device.getName());
 					map.put("mac", device.getAddress());
 					map.put("pager", device.getBluetoothClass().toString());
 
 					if(!isDeviceWithMacInList(device_list, device.getAddress())){
 						device_list.add(map);
 
 						//List of device objects
 						btDeviceList.add(device);
 					}
 					listAdapter.notifyDataSetChanged();
 				}
 			}
 
 			//If discovery finished
 			if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
 
 				//Hide the progress bar
 				progressBar.setVisibility(View.GONE);
 				refresh.setVisibility(View.VISIBLE);
 				Log.d(TAG, "\nDiscovery Finished");
 			}
 
 			if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
 				Toast.makeText(getBaseContext(), "State_Disconnected", Toast.LENGTH_SHORT).show();
 				Log.d(TAG, "ACTION_ACL_DISCONNECTED");
 			}
 
 			if(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)){
 				Toast.makeText(getBaseContext(), "STATE_DISCONNECT_REQUEST", Toast.LENGTH_SHORT).show();
 				Log.d(TAG, "ACTION_ACL_DISCONNECTED");
 			}
 
 			if(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)){
 				Toast.makeText(getBaseContext(), "State_changed", Toast.LENGTH_SHORT).show();
 				Log.d(TAG, "ACTION_CONNECTION_STATE_CHANGED");
 			}
 		}
 	}
 
 	/**
 	 * Creates a dialog to the user
 	 * 
 	 * @param message String containing the message to be presented to the user
 	 * @return The created dialog
 	 */
 	public Dialog createDialog(String message) {
 		AlertDialog.Builder responseDialog = new AlertDialog.Builder(this);
 
 		responseDialog.setMessage(message)
 		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				//Nothing needs to be done, this closes the box
 			}
 		}).setCancelable(false);
 
 		return responseDialog.show();
 	}
 
 	/**
 	 * AsyncTask handling the progress bar shown to the user upon connecting
 	 * to a device. 
 	 */
 	private class ProgressDialogTask extends AsyncTask<Void, Void, Boolean> {
 
 		private long timeout;
 
 		@Override
 		protected void onPreExecute() {
 			timeout = System.currentTimeMillis() + 30000;
 			progressDialog.setMessage("Connecting, please wait... This might take " +
 					"up to 30 seconds.");
 			progressDialog.setCancelable(false);
 			progressDialog.show();
 			oldState = null;
 		}
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			while(true) {
 
 				if (Devices.isConnected()) return true;
 
 				else if (Devices.connectionHasFailed()) return false;
 
 				if (System.currentTimeMillis() > timeout) {
 					return false;
 				}
 				try {
 					Thread.sleep(1);
 				} catch (InterruptedException e) {}
 			}
 		}
 
 		@Override 
 		protected void onPostExecute(Boolean success) {
 			BluetoothConnection connection = BtArduinoService.getBtService().getBluetoothConnection();
 			progressDialog.dismiss();
 			String message, title;
 
 			if (success && connection.isConnected()) {
 
 				message = "Successfully connected to device: " + listAdapter.getName(savedPosition);
 
 				String lastConnectedDevice = "Device name: " + listAdapter.getName(savedPosition)
 						+ "\nMAC Address: " + listAdapter.getMacAddress(savedPosition);
 
 				//Saves the full information about the last connected device to sharedPreferences
 				Editor edit = sharedPref.edit();
 				edit.putString("connected_device_dialog", lastConnectedDevice);
 				edit.putString("connected_device_name", listAdapter.getName(savedPosition));
 				edit.putString("connected_device_mac", listAdapter.getMacAddress(savedPosition));
 				edit.commit();
 
 				//Set the name of connected device in title bar
 				title = "Devices - " + listAdapter.getName(savedPosition);
 			}
 			else {
 				message = NEGATIVE_MESSAGE;
 				title = "Devices";
 
 				//Deselect the view in the list
 				deviceList.setItemChecked(savedPosition, false);
 
 				//Notify the adapter about the changes
 				listAdapter.notifyDataSetInvalidated();
 			}
 
 			createDialog(message);
 			Log.d(TAG, message);
 			setTitle(title);
 			Log.d(TAG, "ConnectionState was: " + connection.getConnectionState());
 		}
 	}
 
 	/** Button for disconnecting from a connected BT device */
 	public void disconnectButton(final int position){
 
 		//Check if the selected element is a connected device
 		if(BtArduinoService.getBtService() != null){
 			if(BtArduinoService.getBtService().getBluetoothConnection() != null){
 
 				//Get the BT connection
 				final BluetoothConnection connection = BtArduinoService.getBtService().getBluetoothConnection();
 
 				if(connection.isConnected()){
 
 					AlertDialog.Builder responseDialog = new AlertDialog.Builder(this);
 
 					responseDialog.setMessage("Click disconnect to disconnect from the device")
 					.setPositiveButton("Disconnect", new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							//Disconnect the connection
 							connection.disconnect();
 							connection.setConnectionState(ConnectionState.STATE_DISCONNECTED);
 
 							//Stop the service
 							BtArduinoService.getBtService().stopSelf();
 							BtArduinoService.getBtService().onDestroy();
 
 							//Clear the preferences
 							Editor edit = sharedPref.edit();
 							edit.remove("connected_device_name");
 							edit.remove("connected_device_mac");
 							edit.remove("connected_device_dialog");
 							edit.commit();
 
 							//Update the Activity's title
 							setActivityTitle();
 
 							//Deselect the view in the list
 							deviceList.setItemChecked(position, false);
 
 							//Notify the adapter about the changes
 							listAdapter.notifyDataSetInvalidated();
 
 							//Close the dialog box
 							dialog.cancel();
 						}
 					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.cancel();
 						}
 					});
 					responseDialog.show();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Creates options menu
 	 */
 	@Override
 	@SuppressLint("NewApi")
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.device_menu, menu);
 
 		return true;
 	}
 
 	/**
 	 * Returns true as long as item corresponds with a proper options action.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 
 		//Start the preferences class
 		case R.id.settings:
 			//Create an intent to start the preferences activity
 			Intent myIntent = new Intent(getApplicationContext(), Preferences.class);
 			this.startActivity(myIntent);
 			return true;
 
 		case R.id.hide_non_arduino:
 			//Prepare to edit the setting
 			Editor edit = sharedPref.edit();
 
 			//Fetches the current value of the 'hide incompatible' option in the preference file
			boolean hideIncompatible = sharedPref.getBoolean("bluetooth_type", false);
 
			if(hideIncompatible == true){
 
 				//Changes the value and commits the changes
 				edit.putBoolean("bluetooth_type", false);
 				edit.commit();
 
 				item.setChecked(false);
 			}
 
 			else{
 				//Changes the value and commits the changes
 				edit.putBoolean("bluetooth_type", true);
 				edit.commit();
 
 				item.setChecked(true);
 			}
 			return true;
 
 		default: 
 			return false;
 		}
 	}
 }
