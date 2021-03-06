 package ec.nem.BlueNet;
 
 import java.util.Set;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ExpandableListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import ec.nem.BlueNet.BluetoothNodeService.LocalBinder;
 
 public class BuildNetworkActivity extends Activity implements MessageListener, NodeListener {
 
 	private static final String TAG = "BuildNetworkActivity";
 	public static String EXTRA_DEVICE_ADDRESS = "device_address";
 	private static final int REQUEST_ENABLE_BT = 2039234;
 	private BluetoothNodeService connectionService;
 	private boolean boundToService = false;
 	private BluetoothAdapter btAdapter;
     private ArrayAdapter<String> pairedDevicesArrayAdapter;
     private ArrayAdapter<String> newDevicesArrayAdapter;
     private ExpandableListAdapter currentNetworkListAdapter;
     
     /*
 	* BuildNetworkActivity
 	* - takes minimum network size, name, uuid, next activity?
 	* - starts bluetooth, displays paired devices,
 	*   gives access to device discovery
 	*/
 	
 	@Override
 	public void onCreate(Bundle savedInstance){
 		super.onCreate(savedInstance);
 		setContentView(R.layout.buildnetwork);
 		btAdapter = BluetoothAdapter.getDefaultAdapter();
 		if (btAdapter != null && !btAdapter.isEnabled()) {
 		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
 		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
 		}
 		else{
 			onBluetoothEnabled(RESULT_OK, null);
 		}
 		// Set result CANCELED incase the user backs out
 		setResult(Activity.RESULT_CANCELED);
 	}
 	
 	@Override
     protected void onDestroy() {
         super.onDestroy();
 
         if(boundToService){
         	unbindService(connection);
        	boundToService = false;
         }
         // Make sure we're not doing discovery anymore
         if (btAdapter != null) {
             btAdapter.cancelDiscovery();
         }
         	
         // Unregister broadcast listeners
         /*if(mReceiver != null){
         	this.unregisterReceiver(mReceiver);
         }*/
     }
 	
 	@Override
 	protected void onStart(){
 		super.onStart();
 		Intent intent = new Intent(this, BluetoothNodeService.class);
     	bindService(intent, connection, Context.BIND_AUTO_CREATE);
 	}
 	
 	@Override
 	protected void onStop(){
 		super.onStop();
     	if(boundToService){
     		unbindService(connection);
        	boundToService = false;
     	}
 	}
 	
 	public void onBluetoothEnabled(int resultCode, Intent data){
 		if(resultCode == RESULT_OK){
 			Log.d(TAG, "Bluetooth has been enabled.");
 			
 			Button scanButton = (Button) findViewById(R.id.discover_users_button);
 	        scanButton.setOnClickListener(new OnClickListener() {
 	            public void onClick(View v) {
 	                doDiscovery();
 	                v.setVisibility(View.GONE);
 	            }
 	        });
 	        
 	        //currentNetworkListAdapter = new BluetoothExpandableListAdapter()
 	        
 	        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
 	        newDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
 	        
 	        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
 	        pairedListView.setAdapter(pairedDevicesArrayAdapter);
 	        pairedListView.setOnItemClickListener(mDeviceClickListener);
 	        
 	        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
 	        newDevicesListView.setAdapter(newDevicesArrayAdapter);
 	        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
 	        
 	        // Register for broadcasts when a device is discovered
 	        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 	        this.registerReceiver(mReceiver, filter);
 	        
 	        // Register for broadcasts when discovery has finished
 	        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
 	        this.registerReceiver(mReceiver, filter);
 	        
 	        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
 
 	        // If there are paired devices, add each one to the ArrayAdapter
 	        if (pairedDevices.size() > 0) {
 	            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
 	            for (BluetoothDevice device : pairedDevices) {
 	                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
 	            }
 	        } else {
 	            String noDevices = getResources().getText(R.string.none_paired).toString();
 	            pairedDevicesArrayAdapter.add(noDevices);
 	        }
 		}
 		else{
 			Log.d(TAG, "Bluetooth was not enabled. Exiting.");
 			finish();
 		}
 	}
 	
 	private void doDiscovery() {
         Log.d(TAG, "doDiscovery()");
 
         // Indicate scanning in the title
         setProgressBarIndeterminateVisibility(true);
         setTitle(R.string.scanning);
 
         // Turn on sub-title for new devices
         findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
 
         // If we're already discovering, stop it
         if (btAdapter.isDiscovering()) {
             btAdapter.cancelDiscovery();
         }
 
         // Request discover from BluetoothAdapter
         btAdapter.startDiscovery();
     }
 	
 	// The on-click listener for all devices in the ListViews
     private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
         public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
             // Cancel discovery because it's costly and we're about to connect
             btAdapter.cancelDiscovery();
 
             // Get the device MAC address, which is the last 17 chars in the View
             String info = ((TextView) v).getText().toString();
             String address = info.substring(info.length() - 17);
 
             // Create the result Intent and include the MAC address
             Intent intent = new Intent();
             intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
 
             // Set result and finish this Activity
             setResult(Activity.RESULT_OK, intent);
             finish();
         }
     };
     
  // The BroadcastReceiver that listens for discovered devices and
     // changes the title when discovery is finished
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
                     newDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                 }
             // When discovery is finished, change the Activity title
             } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                 setProgressBarIndeterminateVisibility(false);
                 setTitle(R.string.select_device);
                 if (newDevicesArrayAdapter.getCount() == 0) {
                     String noDevices = getResources().getText(R.string.none_found).toString();
                     newDevicesArrayAdapter.add(noDevices);
                 }
             }
         }
     };
     
     private ServiceConnection connection = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className,
                 IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             LocalBinder binder = (LocalBinder) service;
             connectionService = binder.getService();
             connectionService.addMessageListener(BuildNetworkActivity.this);
             connectionService.addNodeListener(BuildNetworkActivity.this);
             boundToService = true;
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             boundToService = false;
         }
     };
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		super.onActivityResult(requestCode, resultCode, data);
 		if(requestCode == REQUEST_ENABLE_BT){
 			onBluetoothEnabled(resultCode, data);
 		}
 	}
 
 	@Override
 	public void onNodeEnter() {
 		Log.d(TAG, "New node in network.");
 	}
 
 	@Override
 	public void onNodeExit() {
 		Log.d(TAG, "A node left the network.");
 	}
 
 	@Override
 	public void onMessageReceived() {
 		Log.d(TAG, "A new message has been received.");
 	}
 }
