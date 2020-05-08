 package com.vitaltech.bioink;
 
 import java.util.ArrayList;
 import java.util.Set;
 
 import android.annotation.SuppressLint;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 @SuppressLint("HandlerLeak")
 public class BluetoothManager {
 	//Logging information
 	private final boolean DEBUG = MainActivity.DEBUG;
 	private static final String TAG = MainActivity.class.getSimpleName();
 
 	//Zephyr Consts
 	private final int HEART_RATE = 0x100;
 	private final int RESPIRATION_RATE = 0x101;
 	private final int POSTURE = 0x103;
 	private final int PEAK_ACCLERATION = 0x104;
 
 	//Needed Globals
 	ArrayList<Bioharness> BHDevices;
 	DataProcess dataProcessing;
 	BluetoothAdapter adapter;
 
 	public BluetoothManager(BluetoothAdapter _adapter,
 			DataProcess _dataProcessing) {
 		if (DEBUG)
 			Log.d(TAG, "Entering BluetoothManager Constructor");
 
 		// Set our BTManager Globals
 		adapter = _adapter;
 		dataProcessing = _dataProcessing;
 
 		if (DEBUG)
 			Log.d(TAG, "Initialize ArrayList of BHDevices");
 		// Initialize Bioharness object array
 		BHDevices = new ArrayList<Bioharness>();
 
 		// Create our list of Bioharnesses
 		buildBHList();
 
 	}
 
 	//Called by UI incase of bluetooth being turned on
 	public void bt_enabled() {
 		if (DEBUG)
 			Log.d(TAG, "bt_enabled called");
 		buildBHList();
 		if (DEBUG) Log.d(TAG, "exit bt_enabled");
 	}
 
 	//Called by UI in case of bluetooth being turned off
 	public void bt_disabled() {
 		if (DEBUG)
 			Log.d(TAG, "bt_disabled called");
 		for (Bioharness device : BHDevices) {
 			device.disconnect();
 		}
 		Log.d(TAG, "Clear out BHDevices list");
 		BHDevices.clear();
 		Log.d(TAG, "bt_disabled exiting");
 	}
 
 	private void buildBHList() {
 		// Get our pairedDevices
 		if (DEBUG)
 			Log.d(TAG, "Enter buildBHList");
 		
 		//Check that the adapter actually exists
 		if(adapter == null){
 			Log.e(TAG, "adapter is null!");
 		}
 		if (DEBUG)
 			Log.d(TAG, "getting bonded devices");
 		Set<BluetoothDevice> AllDevices = adapter.getBondedDevices();
 		for (BluetoothDevice device : AllDevices) {
 			if (device.getName().startsWith("BH")) {
 				if (DEBUG)
 					Log.d(TAG, "Creating and adding a BH device");
 				BHDevices.add(new Bioharness(adapter, device, msgHandler));
 			}
 		}
 	}
 
 	private final Handler msgHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			String UID = msg.getData().getString("UID");
 			switch (msg.what) {
 			case HEART_RATE:
 				float HeartRate = msg.getData().getFloat("HeartRate");
 				dataProcessing.push(UID, BiometricType.HEARTRATE, HeartRate);
 				break;
 
 			case RESPIRATION_RATE:
 				float RespirationRate = msg.getData().getFloat(
 						"RespirationRate");
 				dataProcessing.push(UID, BiometricType.RESPIRATION,
 						RespirationRate);
 				break;
 
 			case POSTURE:
 				float Posture = msg.getData().getFloat("Posture");
 				dataProcessing.push(UID, BiometricType.POSTURE, Posture);
 				break;
 
 			case PEAK_ACCLERATION:
 				float PeakAcc = msg.getData().getFloat("PeakAcceleration");
 				dataProcessing.push(UID, BiometricType.PEAKACC, PeakAcc);
 				break;
 
 			}
 		}
 
 	};
 }
