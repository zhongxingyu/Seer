 package com.rimproject.andsensor;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TimerTask;
 
import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.hardware.SensorManager;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 
 public class NearbyBluetoothLogger extends BasicTimedDurationLogger
{	
	BluetoothAdapter bluetoothAdapter;
 	BroadcastReceiver bluetoothReceiver;
 	List<String> bluetoothDevices;
 	
 	public NearbyBluetoothLogger() 
 	{
 		super();
 		setLoggingDuration(20 * 1000);
 		
		this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
 		this.bluetoothReceiver = new BluetoothReceiver(this);
 	}
 	
 	class BluetoothReceiver extends BroadcastReceiver {
 		
 		NearbyBluetoothLogger logger;
 		
 		public BluetoothReceiver(NearbyBluetoothLogger aLogger) {
 			this.logger = aLogger;
 		}
 		
 		public void onReceive(Context context, Intent intent) {
 	        String action = intent.getAction();
 	        // When discovery finds a device
 	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 	            // Get the BluetoothDevice object from the Intent
 	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 	            // Add the name and address to an array adapter to show in a ListView
	            this.logger.bluetoothDevices.add(device.getName() + " (" + device.getAddress() + ")");
 	        }
 	    }
 	}
 	
 	public void startLogging() {
 		super.startLogging();
 		
 		this.bluetoothDevices = new ArrayList<String>();
		if(this.bluetoothAdapter.isEnabled()) { 
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			AndSensor.getContext().registerReceiver(this.bluetoothReceiver, filter);
			this.bluetoothAdapter.startDiscovery();
		}
 	}
 	
 	protected void stopLogging() 
 	{
 		super.stopLogging();
 		
		try {
			AndSensor.getContext().unregisterReceiver(this.bluetoothReceiver);
		} catch (IllegalArgumentException e) {
			//receiver was not registered, no action required
		} finally {
			this.bluetoothAdapter.cancelDiscovery();
			if (this.bluetoothDevices != null) {
				super.writeToLogFile(this.bluetoothDevices.toString());
			}
 		}
 	}
 }
