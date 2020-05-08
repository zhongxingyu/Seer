 package com.ottochiu.mse.heartbeat_simulator;
 
 import java.io.IOException;
 import java.util.UUID;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 public class SendMethodActivity extends Activity {
 	private static final String TAG = "SendMethodActivity";
 	private static final int REQUEST_BT_ENABLE = 1;
 	
 	private TextView mBtStatus = null;
 	private RadioGroup mConnectionGroup; 
 	private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
 	
 	// Create a BroadcastReceiver for ACTION_FOUND
 	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
 	    public void onReceive(Context context, Intent intent) {
 
         	String action = intent.getAction();
 
 	        // When discovery finds a device
 	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 	            // Get the BluetoothDevice object from the Intent
 	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 	            // Add the name and address to an array adapter to show in a ListView
 	            updateStatus("Discovered " + device.getName() + " @ " + device.getAddress());
 	            
 	    		if (device.getName().equals(getString(R.string.server_name))) {
 	    			updateStatus("Server found.");
 	    			unregisterReceiver(mReceiver);
	    			startConnection(device);	
 	    		}
 	        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
 	        	// Discovery finished.
 	        	updateStatus("Bluetooth discovery finished.");
 	        	
 	        	// Only make the options visible again when the BT device is null.
 	        	// Otherwise, the activity may be opening a connection.
 	        	if (SimulatorApplication.getApplication(SendMethodActivity.this).getDevice() == null) {
 	        		unregisterReceiver(mReceiver);
 	        		mConnectionGroup.setVisibility(View.VISIBLE);
 	        	}
             }
 	    }
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.options);
 
 		mConnectionGroup = (RadioGroup) findViewById(R.id.connectionOption);
 		mConnectionGroup.setVisibility(View.VISIBLE);
 		
 		// Bluetooth supported
 		if (BluetoothAdapter.getDefaultAdapter() != null) {
 			findViewById(R.id.bluetoothOption).setEnabled(true);
 			SimulatorApplication.getApplication(this).setDevice(null);
 		}
 		
     	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
 	}
 	
 	@Override
     protected void onStop() {
         super.onStop();
 
         // Make sure we are not doing discovery anymore
         if (mBtAdapter != null) {
             mBtAdapter.cancelDiscovery();
         }
 
         try {
         	unregisterReceiver(mReceiver);
         } catch (IllegalArgumentException e) {
         	// ignored. means receiver not registered.
         }
     }
 	
 	public void useHttp(View v) {
 		simulatorActivity(false);
 	}
 	
 	public void useBluetooth(View v) {
 		// Determine whether Bluetooth capability is enabled
     	try {
     		// Disable orientation change at this point. Otherwise BT process may get interrupted.
             setRequestedOrientation(
             		getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
             				ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT :
             					ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                                     
                                     
     		mBtStatus = (TextView) findViewById(R.id.bluetoothStatus);
     		mConnectionGroup.setVisibility(View.INVISIBLE); // hide the button while processing Bluetooth connection.
 
     		// If BT not enabled (BT adapter should not be null. Taken care of in onCreate().)
     		if (!mBtAdapter.isEnabled()) {
 
     			updateStatus("Enabling Bluetooth...");
     			
     			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
     			startActivityForResult(enableIntent, REQUEST_BT_ENABLE);
 
     		} else {
     			updateStatus("Bluetooth enabled.");
     			choosePairing();
     		}
 
     	} catch (NullPointerException e) {
     		Log.wtf(TAG, "NullPointerException in useBluetooth()");
     		v.setEnabled(false); // should not happen. disable view again
     		mConnectionGroup.setVisibility(View.VISIBLE); // allows user to try again
     	}
 	}
 	
 
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
     	// Check for the correct intent
     	if (requestCode == REQUEST_BT_ENABLE) {
     		if (resultCode == Activity.RESULT_OK) {
     			updateStatus("Bluetooth enabled.");
     			choosePairing();
     		} else {
     			updateStatus("Bluetooth NOT enabled.");
     			mConnectionGroup.setVisibility(View.VISIBLE);
     		}
     	}
     }
     
     
     private void choosePairing() {
     	// Set a descriptive name for this device
     	mBtAdapter.setName("Heartbeat Simulator");
     	
     	// First check if the server is connected already.
     	for (BluetoothDevice device : mBtAdapter.getBondedDevices()) {
     		updateStatus("Bonded device: " + device.getName());
     		
     		if (device.getName().equals(getString(R.string.server_name))) {
     			updateStatus("Server found @ " + device.getAddress());
     			startConnection(mBtAdapter.getRemoteDevice(device.getAddress()));
     			return;
     		}
     	}
 
 		// server has not been paired before. attempt to discover it.
     	updateStatus("Discovering server.");
     	
     	// Restart discovery.
     	if (mBtAdapter.isDiscovering()) {
     		mBtAdapter.cancelDiscovery();
     	}
     	
 		// Register the BroadcastReceiver.
 		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 		registerReceiver(mReceiver, filter); // Unregistered in callback
 		
         // Register for broadcasts when discovery has finished
         filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
         registerReceiver(mReceiver, filter);
 		
 		mBtAdapter.startDiscovery();
 	}
     
     private void startConnection(BluetoothDevice device) {
     	updateStatus("Connecting to server.");
 		
 		mBtAdapter.cancelDiscovery();
 
 		try {
 			new ConnectTask(SimulatorApplication.getApplication(this), device).execute();
 		} catch (IOException e) {
 			updateStatus("Bluetooth connection failed: " + e.getMessage());
 			mConnectionGroup.setVisibility(View.VISIBLE);
 		}
     }
     
     private void simulatorActivity(boolean isBluetoothEnabled) {
     	SimulatorApplication.getApplication(this).setSender(isBluetoothEnabled);
     	Intent intent = new Intent(getApplicationContext(), HeartbeatSimulatorActivity.class);
         startActivity(intent);
         mConnectionGroup.setVisibility(View.VISIBLE); // show the options again in case user wants to redo it.
     }
     
     
     private void updateStatus(String msg) {
     	mBtStatus.append(msg + "\n");
     }
     
     
     
     private class ConnectTask extends AsyncTask<Void, String, Boolean> {
 
     	ConnectTask(SimulatorApplication app, BluetoothDevice device) throws IOException {
     		mApp = app;
     		
     		// This is the device to use for the rest of the application.
     		mApp.setDevice(device);
     		
     		mApp.setSocket(device.createRfcommSocketToServiceRecord(
     				UUID.fromString(getString(R.string.uuid))));
     	}
     	
     	
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			
 			try {
 				publishProgress("Connecting to " + mApp.getDevice().getName());
 				mApp.getSocket().connect();
 				return Boolean.TRUE;
 				
 			} catch (IOException e) {
 				try {
 					mApp.getSocket().close();
 				} catch (IOException e1) { }
 				
 				publishProgress(e.getMessage());
 				return Boolean.FALSE;
 			}
 		}
 		
 		protected void onProgressUpdate(String... s) {
 			updateStatus(s[0]);
 		}
 		
 		protected void onPostExecute(Boolean status) {
 			// connection succeeded
 			if (status.booleanValue()) {
 				// move on to the next activity.
 				updateStatus("Bluetooth connection succeeded.");
 				simulatorActivity(true);
 			} else {
 				updateStatus("Bluetooth connection failed.");
 				mConnectionGroup.setVisibility(View.VISIBLE);
 			}
 		}
 		
 		
 		private final SimulatorApplication mApp;    	
     }
 }
