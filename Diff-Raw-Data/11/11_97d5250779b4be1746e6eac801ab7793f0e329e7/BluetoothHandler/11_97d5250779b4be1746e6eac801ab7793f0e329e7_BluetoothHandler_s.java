 package it.chalmers.tendu.network.bluetooth;
 
 import it.chalmers.tendu.network.INetworkHandler;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 
 import com.badlogic.gdx.backends.android.AndroidApplication;
 
 public class BluetoothHandler implements INetworkHandler {
 	
 	public static final int REQUEST_ENABLE_BT = 666;
 	private static final String APP_NAME = "Tendu";
 	
 	BluetoothGameService bgs;
 	Context context;
 	private BluetoothAdapter mBluetoothAdapter;
 	private List<BluetoothDevice> devicesList;
 	
 
 	public BluetoothHandler(Context context){
 		this.context=context;
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		if (!mBluetoothAdapter.isEnabled()) {
 			enableBluetooth(); 
 		}
 		
 		bgs=new BluetoothGameService(context);
 		devicesList = new ArrayList();
 		registerBroadcastReceiver();
 		
 		//addTenduToName();
 	}
 
 	@Override
 	public void hostSession() {
 		bgs.start();
 		
 	}
 	
 	@Override
 	public void joinGame() {
 		
		BluetoothDevice bd = searchTeam().get(0);
		bgs.connect(bd, true);
 	}
 
 
 
 	public List<BluetoothDevice> searchTeam() {
 		List<BluetoothDevice> list=new ArrayList<BluetoothDevice>();
 		for( BluetoothDevice d: devicesList) { // bgs.getDevicesList()){
 			if(isDeviceValid(d)) {
 				list.add(d);		
 			}
 		}
 		return list;
 	}
 
 	//----------------------- HELP METHODS ------------------------
 	
 	private void enableBluetooth() {
 		Intent enableBtIntent; 
 		if (!mBluetoothAdapter.isEnabled()) {
 			enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 			((AndroidApplication) context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); // context is wrong
 		} 
 	}
 	
 	private void addTenduToName() {
 		String name = mBluetoothAdapter.getName();
 		if (!name.contains(APP_NAME)) {
 			mBluetoothAdapter.setName(name + " - " + APP_NAME);
 		}
 	}
 	
 	private boolean isDeviceValid(BluetoothDevice device) {
 		return device.getName().contains(APP_NAME);
 	}
 	
 	// Create a BroadcastReceiver for ACTION_FOUND
 		private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
 			public void onReceive(Context context, Intent intent) {
 				String action = intent.getAction();
 				// When discovery finds a device
 				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 					// Get the BluetoothDevice object from the Intent
 					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 					// Add the name and address to an array adapter to show in a ListView
 					Log.v("Bluetooth", "Device: " + device.getName() + "Adress: " + device.getAddress());
 					devicesList.add(device);			
 				}
 			}
 		};
 		
 		
 		
 		private List<BluetoothDevice> getDevicesList() {
 			return devicesList;
 		}
 
 
 		private void registerBroadcastReceiver() {
 			// Register the BroadcastReceiver
 			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 			context.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
 		}
 		
 		// Temporary test method
 		private BluetoothDevice findFirstAvailableDevice() {
 			List<BluetoothDevice> devices = searchTeam();
 			if (devices.isEmpty()) {
 				return null;
 			} else return devices.get(0);
 		}
 }
