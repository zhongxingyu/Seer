 package cs309.a1.shared.activities;
 
 import java.util.Set;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import cs309.a1.shared.R;
 import cs309.a1.shared.Util;
 
 /**
  * This Activity appears as a dialog. It lists any paired devices and devices
  * detected in the area after discovery. When a device is chosen by the user,
  * the MAC address of the device is sent back to the parent Activity in the
  * result Intent.
  */
 public class DeviceListActivity extends Activity {
 	private static final String TAG = DeviceListActivity.class.getName();
 
 	// Return Intent extra
 	public static String EXTRA_DEVICE_ADDRESS = "deviceAddress";
 	private static final int REQUEST_ENABLE_BT = 3;
 
 	// Member fields
 	private boolean displayCurrentlyConnected = false;
 	private TextView noDevicesFound;
 	private ProgressBar deviceListProgress;
 	private ImageButton refreshDeviceListButton;
 	private BluetoothAdapter mBtAdapter;
 	private DeviceListAdapter mDevicesArrayAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.device_list);
 
 		noDevicesFound = (TextView) findViewById(R.id.noDevicesFoundText);
 		noDevicesFound.setText(R.string.scanning);
 
 		deviceListProgress = (ProgressBar) findViewById(R.id.deviceListProgress);
 		refreshDeviceListButton = (ImageButton) findViewById(R.id.refreshDeviceList);
 
 		refreshDeviceListButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				refreshDeviceListButton.setVisibility(View.GONE);
 				deviceListProgress.setVisibility(View.VISIBLE);
 
 				noDevicesFound.setVisibility(View.VISIBLE);
 				noDevicesFound.setText(R.string.scanning);
 
 				setupDeviceList();
 				doDiscovery();
 			}
 		});
 
 		// Initialize array adapters. One for already paired devices and
 		// one for newly discovered devices
 		mDevicesArrayAdapter = new DeviceListAdapter(this, R.layout.device_name);
 
 		// Find and set up the ListView for paired devices
 		ListView devicesListView = (ListView) findViewById(R.id.devices);
 		devicesListView.setAdapter(mDevicesArrayAdapter);
 		devicesListView.setOnItemClickListener(mDeviceClickListener);
 
 		// Register for broadcasts when a device is discovered
 		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 		this.registerReceiver(mReceiver, filter);
 
 		// Register for broadcasts when discovery has finished
 		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
 		this.registerReceiver(mReceiver, filter);
 
 		// Get the local Bluetooth adapter
 		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
 
 		if (!mBtAdapter.isEnabled()) {
 			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
 		} else {
 			setupDeviceList();
 			doDiscovery();
 		}
 	}
 
 	/**
 	 * If we want to display the currently connected devices, add those to the listview
 	 * and then add a separator with the text "New Devices"
 	 */
 	private void setupDeviceList() {
 		mDevicesArrayAdapter.clear();
 
 		if (displayCurrentlyConnected) {
 			// Get a set of currently paired devices
 			Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
 
 			// If there are paired devices, add each one to the ArrayAdapter
 			if (pairedDevices.size() > 0) {
 				mDevicesArrayAdapter.add(new ListSeparator(getResources().getString(R.string.connected_devices_separator)));
 
 				for (BluetoothDevice device : pairedDevices) {
 					mDevicesArrayAdapter.add(new DeviceListItem(device.getName(), device.getAddress()));
 				}
 			}
 
 			mDevicesArrayAdapter.add(new ListSeparator(getResources().getString(R.string.new_devices_separator)));
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		// Make sure we're not doing discovery anymore
 		if (mBtAdapter != null) {
 			mBtAdapter.cancelDiscovery();
 		}
 
 		// Unregister broadcast listeners
 		this.unregisterReceiver(mReceiver);
 	}
 
 	/**
 	 * Start device discover with the BluetoothAdapter
 	 */
 	private void doDiscovery() {
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "doDiscovery()");
 		}
 
 		// Indicate scanning in the title
 		setTitle(R.string.scanning);
 
 		// If we're already discovering, stop it
 		if (mBtAdapter.isDiscovering()) {
 			mBtAdapter.cancelDiscovery();
 		}
 
 		// Request discover from BluetoothAdapter
 		mBtAdapter.startDiscovery();
 	}
 
 	@Override
 	public void onBackPressed() {
 		// Cancel discovery because it's costly and we're about to connect
 		mBtAdapter.cancelDiscovery();
 
 		setResult(RESULT_CANCELED);
 		finish();
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "onActivityResult " + resultCode);
 		}
 
 		switch (requestCode) {
 		case REQUEST_ENABLE_BT:
 			// When the request to enable Bluetooth returns
 			if (resultCode == Activity.RESULT_OK) {
 				// Bluetooth is now enabled, so set up a chat session
 				setupDeviceList();
 				doDiscovery();
 			} else {
 				// User did not enable Bluetooth or an error occurred
 				Log.d(TAG, "BT not enabled");
 				finish();
 			}
 		}
 	}
 
 	/**
 	 * The on-click listener for all devices in the ListViews
 	 */
 	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
 		@Override
 		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
 			// Cancel discovery because it's costly and we're about to connect
 			mBtAdapter.cancelDiscovery();
 
 			DeviceListItem item = (DeviceListItem) mDevicesArrayAdapter.getItem(arg2);
 			String address = item.getDeviceMacAddress();
 
 			// Create the result Intent and include the MAC address
 			Intent intent = new Intent();
 			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
 
 			// Set result and finish this Activity
 			setResult(Activity.RESULT_OK, intent);
 			finish();
 		}
 	};
 
 	/**
 	 * The BroadcastReceiver that listens for discovered devices and changes the
 	 * title when discovery is finished
 	 */
 	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			// When discovery finds a device
 			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 				// Get the BluetoothDevice object from the Intent
 				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 
 				// If it's already paired, skip it, because it's been listed already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					// TODO: maybe check to see if there is a device that is currently in the list
					//		 with the same MAC address
					noDevicesFound.setVisibility(View.GONE);
					mDevicesArrayAdapter.add(new DeviceListItem(device.getName(), device.getAddress()));
				}
 			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
 				// When discovery is finished, change the Activity title
 				setTitle(R.string.title_device_list);
 
 				deviceListProgress.setVisibility(View.GONE);
 				refreshDeviceListButton.setVisibility(View.VISIBLE);
 
 				if (mDevicesArrayAdapter.getCount() == 0) {
 					noDevicesFound.setText(R.string.no_devices_found);
 				}
 			}
 		}
 	};
 }
