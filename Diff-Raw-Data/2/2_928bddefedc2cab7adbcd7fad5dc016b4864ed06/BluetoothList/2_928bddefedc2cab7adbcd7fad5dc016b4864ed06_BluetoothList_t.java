 package org.darksoft.android.multimedia.remotecontrol;
 
 import org.darksoft.android.lib.widget.ExpandListAdapter;
 import org.darksoft.android.lib.widget.ExpandListChild;
 import org.darksoft.android.lib.widget.ExpandListGroup;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Set;
 import android.os.Bundle;
 import android.app.Activity;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ExpandableListView;
 
 /**
  * A {@link android.app.Activity Activity} used for get a list of paired and discovered
  * Bluetooth Devices. Only call it by a Intent.
  * 
  * @author Joel Pelaez Jorge
  *
  */
 
 public class BluetoothList extends Activity implements ExpandableListView.OnChildClickListener{
 
 	@SuppressWarnings("unused")
 	private final int BOUNDED_DEVICE_GROUP = 0;
 	private final int DISCOVER_DEVICE_GROUP = 1;
 	private ExpandListAdapter mAdapter;
 	private ExpandableListView mDeviceList;
 	private ArrayList<ExpandListGroup> mGroup;
 	private String mNameDevice;
 	private String mAddrDevice;
 	
 	// Create a BroadcastReceiver for ACTION_FOUND
 	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			// When discovery finds a device
 			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 				// Get the BluetoothDevice object from the Intent
 				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 				// Add the name and address to an array adapter to show in a ExpandableListView
 				ExpandListGroup group = mGroup.get(DISCOVER_DEVICE_GROUP);
 				BluetoothExpandListChild child = new BluetoothExpandListChild();
 				child.setName(device.getName());
 				child.setAddress(device.getAddress());
 				group.getItems().add(child);
				mAdapter.notifyDataSetChanged();
 	        }
 	    }
 	};
 	
 	// Get Bluetooth Device information and Send it to BluetoothClient
 	@Override
 	public boolean onChildClick(ExpandableListView parent, View v,
 			int groupPosition, int childPosition, long id) {
 		// Cancel the current discovery for avoid connection errors.
 		BluetoothClient.cancelDiscoveredDevices();
 		// Get the selected element from the list and get its name and address.
 		BluetoothExpandListChild dev = (BluetoothExpandListChild) mGroup.get(groupPosition).getItems().get(childPosition);
 		mNameDevice = dev.getName();
 		mAddrDevice = dev.getAddress();
 		// Put the data in a Intent and send to RemoteControl class.
 		Intent data = new Intent();
 		data.putExtra("name", mNameDevice);
 		data.putExtra("address", mAddrDevice);
 		setResult(RESULT_OK, data);
 		finish();
 		return false;
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_bluetooth_list);
 		
 		// Register the BroadcastReceiver
 		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
 		
 		mDeviceList = (ExpandableListView) findViewById(R.id.list_devices);
 		
 		mGroup = setDevicesList();
 		mAdapter = new ExpandListAdapter(this, mGroup);
 		mDeviceList.setAdapter(mAdapter);
 		mDeviceList.setOnChildClickListener(this);
 	}
 
 	@Override
 	protected void onDestroy() {
 		// Cancel Discover and Unregister Broadcast Receiver
 		BluetoothClient.cancelDiscoveredDevices();
 		unregisterReceiver(mReceiver);
 		super.onDestroy();
 	}
 	
 	private ArrayList<ExpandListGroup> setDevicesList() {
 		ArrayList<ExpandListGroup> mRootList = new ArrayList<ExpandListGroup>();
 		ExpandListGroup bounded = new ExpandListGroup();
 		ExpandListGroup discovered = new ExpandListGroup();
 		ArrayList<ExpandListChild> list_bounded = new ArrayList<ExpandListChild>();
 		ArrayList<ExpandListChild> list_discovered = new ArrayList<ExpandListChild>();
 		
 		bounded.setName(getResources().getString(R.string.list_devices_bounded));
 		discovered.setName(getResources().getString(R.string.list_devices_discovered));		
 		
 		Set<BluetoothDevice> devices = BluetoothClient.getDeviceList();
 		Iterator<BluetoothDevice> it = devices.iterator();
 		
 		while (it.hasNext()) {
 			BluetoothExpandListChild child = new BluetoothExpandListChild();
 			BluetoothDevice device = it.next();
 			child.setName(device.getName());
 			child.setAddress(device.getAddress());
 			list_bounded.add(child);
 		}
 		bounded.setItems(list_bounded);
 		discovered.setItems(list_discovered);
 		mRootList.add(bounded);
 		mRootList.add(discovered);
 				
 		return mRootList;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_bluetooth_list, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_discover_devices:
 			BluetoothClient.getDiscoveredDevices();
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 
 }
