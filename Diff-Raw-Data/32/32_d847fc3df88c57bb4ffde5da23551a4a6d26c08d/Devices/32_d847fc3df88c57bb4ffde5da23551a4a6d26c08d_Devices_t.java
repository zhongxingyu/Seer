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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.TimeoutException;
 import no.group09.connection.BluetoothConnection;
 import no.group09.connection.ConnectionMetadata;
 import no.group09.connection.ConnectionMetadata.DefaultServices;
 import no.group09.fragments.BluetoothDeviceAdapter;
 import no.group09.ucsoftwarestore.R;
 import android.app.Activity;
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
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.AdapterView.OnItemClickListener;
 import android.view.View.OnClickListener;
 
 /**
  * This class searches for the BT devices in range and put them in a list
  */
 public class Devices extends Activity  {
 
 	/**Should be true if only arduinos is to be showed. False otherwise */
 	private boolean ONLY_SHOW_ARDUINOS = true;
 	
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
 
 	private BtArduinoService bluetoothService;
 	private BluetoothConnection connection;
 
 	private boolean secondClick = false;
 	
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
 
 		//Check the BT state
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
 
 				//FIXME: If the service allready is running, it does not start over.
 				//This might be a problem if it needs to change the connected device.
 				startService(serviceIntent);
 
 				ProgressDialogTask task = new ProgressDialogTask();
 				task.execute();
 				savedPosition = position;
 			}
 		});	
 
 		deviceList.setOnItemLongClickListener(new OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
 
 				dialogBoxForTestingPurposes();
 
 				return false;
 			}
 		});
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
 
 				//Notify the adapter that the list is now empty
 				listAdapter.notifyDataSetChanged();
 
 				if(!BtArduinoService.getBtService().getBluetoothConnection().isConnected()){
 					setTitle("Devices");
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
 				//Finishes this activity and goes back to the parent
 				//				finish();
 
 				//TODO: use finish() and delete this call when you want to browse shop instead for debug
 				dialogBoxForTestingPurposes();
 			}
 		});
 	}
 	
 	//Disse to er en del av en stygg hack, men det funker. Fix senere.
 	private void setContext(Context context) {
 		Devices.context = context;
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
 		filter.addAction(BluetoothDevice.ACTION_UUID);
 		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
 		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
 
 		actionFoundReceiver = new MyBroadcastReceiver();
 		//Registers the BT receiver with the requested filters
 		//Don't forget to unregister during onDestroy
 		registerReceiver(actionFoundReceiver, filter);
 	}
 
 
 	/* This routine is called when an activity completes.*/
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
 	/**
 	 * Called when this activity is destroyed. If a discovery is ongoing, it is
 	 * cancelled. The broadcastreceiver is also unregistered.
 	 */
 	protected void onDestroy() {
 		super.onDestroy();
 		unregisterReceiver(actionFoundReceiver);
 		if (btAdapter != null) {
 			btAdapter.cancelDiscovery();
 		}
 	}
 
 	/**
 	 * Called when Devices screen is resumed. Clears the list of visible 
 	 * bluetooth devices and starts a new search.
 	 */
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		//Clear the list of BT devices
 		device_list.clear();
 
 		//Notify the adapter that the list is now empty
 		listAdapter.notifyDataSetChanged();
 
 		setActivityTitle();
 	}
 
 	/** Add the connected device name to the title if connected */
 	private void setActivityTitle(){
 		String appName = sharedPref.getString("connected_device_name", "null");
 
 		if(BtArduinoService.getBtService() != null){
 			if(BtArduinoService.getBtService().getBluetoothConnection() != null){
 				setTitle("Devices - " + appName);
 			}
 			else{
 				setTitle("Devices");
 			}
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
 		if(ONLY_SHOW_ARDUINOS){
 			if((device.getBluetoothClass().toString()).equals("708")){
 				return true;
 			}
 
 			//Only return false if we only should show pagers, and that the device is not a pager
 			else return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Broadcast receiver class. Used to receive Android Bluetooth API communication
 	 * 
 	 * @author JeppeE
 	 *
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
 			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 
 				//If the bluetooth class is named 708 that we made as a 'standard' for recognizing arduinos
 				if(onlyShowArduinos(device)){
 
 					//Adding found device
 					HashMap<String, String> map = new HashMap<String, String>();
 					map = new HashMap<String, String>();
 					map.put("name", device.getName());
 					map.put("mac", device.getAddress());
 					map.put("pager", device.getBluetoothClass().toString());
 					device_list.add(map);
 
 					btDeviceList.add(device);	//FIXME: debugging
 
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
 		}
 	}
 
 	public Dialog createDialog(String message) {
 		AlertDialog.Builder responseDialog = new AlertDialog.Builder(this);
 
 		responseDialog.setMessage(message)
 		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 
 			}
 		});
 
 		return responseDialog.show();
 	}
 	private class ProgressDialogTask extends AsyncTask<Void, Void, Boolean> {
 
 		private long timeout;
 
 
 		@Override
 		protected void onPreExecute() {
 			timeout = System.currentTimeMillis() + 8000;
 			progressDialog.setMessage("Connecting, please wait...");
 			progressDialog.setCancelable(true);
 			progressDialog.show();
 		}
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			while(true) {
 				if (bluetoothService == null) {
 					bluetoothService = BtArduinoService.getBtService();
 				}
 				if (bluetoothService != null) {
 					connection = bluetoothService.getBluetoothConnection();
 				}
 				if (connection != null) {
 					if (connection.isConnected()) {
 						return true;
 					}
 				}
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
 
 			String appName = sharedPref.getString("connected_device_name", "could not find connected device name");
 
 			if (success && connection.isConnected()) {
 				message = "The connection was successful.";
 				title = "Devices - " + appName;
 				
 				String lastConnectedDevice = "Device name: " + listAdapter.getName(savedPosition)
 						+ "\nMAC Address: " + listAdapter.getMacAddress(savedPosition);
 
 				Editor edit = sharedPref.edit();
 
 				//Saves the full information about the last connected device to sharedPreferences
 				edit.putString("connected_device_dialog", lastConnectedDevice);
 
 				//Save the name of the BT device as a separate setting. Used
 				//to show the name of last connected device in title bar.
 				edit.putString("connected_device_name", listAdapter.getName(savedPosition));
 
 				edit.commit();
 				Log.d(TAG, "The information about the last connected device was written to shared preferences");
 				
 			}
 			else {
 				message = "The connection was not successfull." + "\nPlease try again.";
 				title = "Devices";
 			}
 
 			createDialog(message);
 			Log.d(TAG, message);
 			setTitle(title);
 			Log.d(TAG, "ConnectionState was: " + connection.getConnectionState());
 		}
 	}
 
 	/** 
 	 * This only shows a dialog box with functions to the arduino 
 	 * TODO: delete me when you dont need me anymore
 	 */
 	private void dialogBoxForTestingPurposes(){
 
 		final BluetoothConnection connection = BtArduinoService.getBtService().getBluetoothConnection();
 
 		if(connection.isConnected()){
 
 
 			// custom dialog
 			final Dialog dialog = new Dialog(Devices.this);
 			dialog.setContentView(R.layout.dialog_box_for_test_purposes);
 
 			Button LED = (Button) dialog.findViewById(R.id.LED);
 			Button VIBRATE = (Button) dialog.findViewById(R.id.VIBRATE);
 			Button SPEAKER = (Button) dialog.findViewById(R.id.SPEAKER);
 			Button LCD = (Button) dialog.findViewById(R.id.LCD);
 
 			ConnectionMetadata meta = connection.getConnectionData();
 			for(String service : meta.getServicesSupported()) {
 				Integer pins[] = meta.getServicePins(service);
 
 				if(pins.length > 0) {
 					if(service.equals(DefaultServices.SERVICE_LED_LAMP.name())){
 						LED.setOnClickListener(new OnClickListener() {
 
 							boolean ledIsToggled;
 							int pinID;
 
 							@Override
 							public void onClick(View v) {
 								ledIsToggled = !ledIsToggled;
 								try {
 									connection.write(pinID, ledIsToggled, false);
 								} catch (TimeoutException e) {}
 							}
 						});
 					}
 					if(service.equals(DefaultServices.SERVICE_VIBRATION.name())){
 						VIBRATE.setOnClickListener(new OnClickListener() {
 
 							Timer timer = new Timer();
 							int pin;
 
 							@Override
 							public void onClick(View v) {
 								timer.schedule(new TimerTask(){
									@Override
 									public void run(){
 										try {
 
 											//Vibrate mobile
 											Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 											vib.vibrate(2000);
 
 											//Vibrate remote module
 											connection.write(pin, true, false);
 											Thread.sleep(2000);
 											connection.write(pin, false, false);
 
 										} 
 										catch (Exception e) {
 										}
 									}
 								}, 0);
 							}
 						});
 					}
 					if(service.equals(DefaultServices.SERVICE_SPEAKER.name())){
 						SPEAKER.setOnClickListener(new OnClickListener() {
 
 							@Override
 							public void onClick(View v) {
 								try {
 									connection.data(new byte[]{100, 75, 52, 15}, false);
 								} catch (TimeoutException e) {}
 							}
 						});
 					}
 				}
 
 				else if(service.equals(DefaultServices.SERVICE_LCD_SCREEN.name())){
 					LCD.setOnClickListener(new OnClickListener() {
 
 						@Override
 						public void onClick(View v) {
 							try {
 								if(secondClick){
 									connection.print("hallo robin", false);
 									secondClick = false;
 								}
 								else{
 									connection.print(connection.getConnectionState().toString(), false);
 									secondClick = true;
 								}
 							} catch (TimeoutException e) {}
 						}
 					});
 				}
 			}
 
 			dialog.show();
 		}
 	}
 }
