 package com.lsg.solarsteuerung;
 
 import java.util.ArrayList;
 import java.util.Set;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SelectBTDevice extends ListActivity {
 	private boolean known_id;
 	private long   id;
 	private String device_desc;
 	private String device_name;
 	BluetoothAdapter mBluetoothAdapter;
     private static final int REQUEST_ENABLE_BT = 3;
     public class BluetoothDeviceArr {
     	String name;
     	String mac;
     	BluetoothDeviceArr(String _name, String _mac) {
     		name = _name;
     		mac = _mac;
     	}
     }
     public class btadapter extends ArrayAdapter<BluetoothDeviceArr> {
     	Context context;
     	ArrayList<BluetoothDeviceArr> btdevices = new ArrayList<BluetoothDeviceArr>();
     	public btadapter(Context context, int textViewResourceId, BluetoothDeviceArr[] objects) {
     		super(context, textViewResourceId, objects);
     		this.context = context;
 		    for (BluetoothDeviceArr device : objects) {
 		    	if(device != null)
 		    		btdevices.add(device);
 		        }
     		}
     	public int getCount() {
             return btdevices.size();
         }
     	@Override
     	public View getView(int position, View convertView, ViewGroup parent) {
     		LayoutInflater inflater=getLayoutInflater();
     		View list_item = inflater.inflate(R.layout.list_item_devices, parent, false);
     		TextView name  = (TextView)list_item.findViewById(R.id.device_name);
     		name.setText(btdevices.get(position).name);
     		TextView mac   = (TextView)list_item.findViewById(R.id.device_description);
     		mac.setText(btdevices.get(position).mac);
     		return list_item;
     		}
     	public void add(BluetoothDeviceArr btdevice) {
     		btdevices.add(btdevice);
     		notifyDataSetChanged();
     		}
     	@Override
     	public void clear() {
     		btdevices = new ArrayList<BluetoothDeviceArr>();
     		}
     	}
     
     private btadapter btdevices;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); //indeterminate progress
 		super.onCreate(savedInstanceState);
 		btdevices  = new btadapter(this, R.layout.list_item_devices, new BluetoothDeviceArr[] {null});
 		HelperClass.setTheme(true, this);
 		Bundle info = getIntent().getExtras();
 		known_id = info.getBoolean(HelperClass.known_id, false);
 		if(known_id)
 			id = info.getLong(HelperClass.DB_ROWID); //keep id to return
 		else
 			device_desc = info.getString(HelperClass.DB_DEVICE_DESCRIPTION); //keep device description
 		device_name = info.getString(HelperClass.DB_DEVICE_NAME);
 		//setContentView(R.layout.select_device);
 		ListView mListView = getListView();
 		mListView.setAdapter(btdevices);
 		mListView.setOnItemClickListener(BTClickListener);
 		
 		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 		registerReceiver(mReceiver, filter);
 		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
 		registerReceiver(mReceiver, filter);
 	}
 	@Override
 	public void onResume() {
 		super.onResume();
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		if (mBluetoothAdapter == null) {
 		    Log.d("intcar", "device does not support bluetooth. exiting.");
 		    Toast.makeText(this, R.string.no_bt_support, Toast.LENGTH_LONG).show();
 		    finish();
 		}
 		else {
 			if(!mBluetoothAdapter.isEnabled()) {
 				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
 				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
 				}
 			else
 				discoverDevices();
 			}
 	}
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		unregisterReceiver(mReceiver);
 	}
 	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
 	        switch (requestCode) {
 	        case REQUEST_ENABLE_BT:
 	            if (resultCode == Activity.RESULT_OK) {
 	                discoverDevices();
 	            } else {
	                Log.d("intcar", "Bluetooth not enabled");
 	                Toast.makeText(this, R.string.no_bt, Toast.LENGTH_SHORT).show();
 	                finish();
 	            }
 	        }
 	    }
 	 public void discoverDevices() {
 		 btdevices.clear();
		 Log.d("asdf", "discoverDevices()");
 		 Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
 		// If there are paired devices
 		if (pairedDevices.size() > 0) {
 		    // Loop through paired devices
 		    for (BluetoothDevice device : pairedDevices) {
 		        // Add the name and address to an array adapter to show in a ListView
 		    	Log.d(device.getName(), device.getAddress());
 		    	BluetoothDeviceArr btdevice = new BluetoothDeviceArr(device.getName(), device.getAddress());
 		        btdevices.add(btdevice);
 		    }
 		}
         setProgressBarIndeterminateVisibility(true);
         mBluetoothAdapter.startDiscovery();
 		setTitle(getString(R.string.scanning_for_bt_devices));
 	 }
 	 
 	 private BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		 public void onReceive(Context context, Intent intent) {
 			 String action = intent.getAction();
 			 if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 				 BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 				 btdevices.add(new BluetoothDeviceArr(device.getName(), device.getAddress()));
 				 }
 			 if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
 				 setProgressBarIndeterminateVisibility(false);
 				    String titlename = SelectBTDevice.this.device_name;
				    if(titlename == null)
				    	titlename = getString(R.string.new_device);
 				 setTitle(getString(R.string.select_bluetooth_device) + " » " + titlename);
 				 if (btdevices.getCount() == 0) {
 					 btdevices.add(new BluetoothDeviceArr(getText(R.string.no_bt_devices).toString(), getText(R.string.no_bt_devices_desc).toString()));
 					 }
 				 }
 			 }
 		 };
 		 private OnItemClickListener BTClickListener = new OnItemClickListener() {
 			 public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
 				 Intent intent = toSettings();
 				 TextView btdevice_mac  = (TextView) v.findViewById(R.id.device_description);
 				 String   btmac         = btdevice_mac.getText().toString();
 				 //Toast.makeText(Select_device.this, btmac, Toast.LENGTH_LONG).show();
 				 if(btmac.length() == 17) {
 					 mBluetoothAdapter.cancelDiscovery();
 					 TextView btdevice_name = (TextView) v.findViewById(R.id.device_name);
 					 String   btname        = btdevice_name.getText().toString();
 					 Toast.makeText(SelectBTDevice.this, btname + "\n" + btmac, Toast.LENGTH_LONG).show();
 					 intent.putExtra(HelperClass.DB_BTDEVICE_NAME, btname);
 					 intent.putExtra(HelperClass.DB_DEVICE_MAC,    btmac);
 					 intent.putExtra(HelperClass.send_BT, true);
 					 startActivity(intent);
 					 finish();
 					 }
 				 else {
 					 startActivity(intent);
 					 finish();
 					 }
 				 }
 			 };
 			 @Override
 			 public boolean onKeyDown(int keyCode, KeyEvent event) {
 				 if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 					 Intent intent = toSettings();
 					 startActivity(intent);
 					 finish();
 					 }
 				 return super.onKeyDown(keyCode, event);
 				 }
 			 public Intent toSettings() {
 				 Intent intent = new Intent(SelectBTDevice.this, DeviceGeneralSettings.class);
 				 intent.putExtra(HelperClass.DB_DEVICE_NAME,   device_name);
 				 intent.putExtra(HelperClass.known_id, known_id);
 				 if(known_id)
 					 intent.putExtra(HelperClass.DB_ROWID, id);
 				 else {
 					 intent.putExtra(HelperClass.new_device, true);
 					 intent.putExtra(HelperClass.DB_DEVICE_DESCRIPTION, device_desc);
 				 }
 				 return intent;
 			 }
 			 @Override
 			 public boolean onCreateOptionsMenu(Menu menu) {
 				 MenuInflater inflater = getMenuInflater();
 				 inflater.inflate(R.menu.select_device, menu);
 				 return true;
 				 }
 			 @Override
 			 public boolean onOptionsItemSelected(MenuItem item) {
 				 // Handle item selection
 				 switch (item.getItemId()) {
 				 case R.id.reload_bluetooth:
 					 discoverDevices();
 					 return true;
 					 default:
 						 return super.onOptionsItemSelected(item);
 						 }
 				 }
 			 }
