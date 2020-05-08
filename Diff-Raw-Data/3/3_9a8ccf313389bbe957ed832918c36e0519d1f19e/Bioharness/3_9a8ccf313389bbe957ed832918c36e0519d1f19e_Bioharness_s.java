 package com.vitaltech.bioink;
 
 import zephyr.android.BioHarnessBT.BTClient;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.os.Handler;
 import android.util.Log;
 
 public class Bioharness {
 	//Logging information
 	private final boolean DEBUG = MainActivity.DEBUG;
 	private static final String TAG = MainActivity.class.getSimpleName();
 	
 	//Private globals needed to maintain BT connection
 	private BluetoothDevice BtDevice;
 	private BTClient bt;
 	private NewConnectedListener NConnListener;
 	private Handler msgHandler;
 	
 	public Bioharness(BluetoothAdapter _adapter, BluetoothDevice _BtDevice, Handler _msgHandler){
 		if(DEBUG) Log.d(TAG, "Entering Bioharness constructor");
 		BtDevice = _BtDevice;
 		msgHandler = _msgHandler;
 		
 		if(DEBUG) Log.d(TAG, "Creating needed Zephyr objects");
 		if(DEBUG) Log.d(TAG, BtDevice.getAddress() + " " + BtDevice.getName());
 		bt = new BTClient(_adapter, BtDevice.getAddress());
 		if(DEBUG) Log.d(TAG,"Connected? " + bt.IsConnected());
 		NConnListener = new NewConnectedListener(msgHandler,msgHandler);
 		bt.addConnectedEventListener(NConnListener);
 		if(DEBUG) Log.d(TAG,"Connected? " + bt.IsConnected());
 		if(DEBUG) Log.d(TAG, "Exiting Bioharness constructor");
 		bt.start();
 	}
 	
 	public void disconnect(){
 		if(DEBUG) Log.d(TAG, "disconnect called");
 		//Called to close "the _comms thread and the communication link with the remote"
 		bt.removeConnectedEventListener(NConnListener);
		bt.Close();
 	}
 }
