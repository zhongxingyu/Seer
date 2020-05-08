 package com.vitaltech.bioink;
 
 import java.util.ArrayList;
 import java.util.Set;
 
 import zephyr.android.BioHarnessBT.BTClient;
 import zephyr.android.BioHarnessBT.ZephyrProtocol;
 import android.annotation.SuppressLint;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 
 @SuppressLint("HandlerLeak")
 public class BluetoothManager {
     /** Called when the activity is first created. */
 	
 	private final boolean DEBUG  = MainActivity.DEBUG;
 	private static final String TAG=MainActivity.class.getSimpleName();
 	
 	private final int HEART_RATE = 0x100;
 	private final int RESPIRATION_RATE = 0x101;
 	private final int POSTURE = 0x103;
 	private final int PEAK_ACCLERATION = 0x104;
 	
 	
 	//Move into Bioharness Class
 	BluetoothAdapter adapter = null;
 	ArrayList<BluetoothDevice> BHDevices = null;
 	DataProcess dataProcessing;
 	BTClient _bt;
 	ZephyrProtocol _protocol;
 	NewConnectedListener _NConnListener;
 	
 		
 	public BluetoothManager(BluetoothAdapter _adapter , DataProcess _dataProcessing){
 		if(DEBUG) Log.d(TAG, "BluetoothManager Created");
 		//Set our BT adapter object
 		adapter = _adapter;
 		dataProcessing = _dataProcessing;
 		
 		//
 		BHDevices = new ArrayList<BluetoothDevice>();
 		
 		//Get our pairedDevices
 		Set<BluetoothDevice> AllDevices = adapter.getBondedDevices();
 		for(BluetoothDevice device : AllDevices){
 			if(device.getName().startsWith("BH")){
 				//BHDevices.add(device);
 				//Quick hack for single device
/*				BHdevice = device;
compile error
ramos commented this code out
*/				
 			}
 		}
 		
 		if(!BHDevices.isEmpty()){
 			if(_bt != null){
 				_bt.Close();
 				_bt = null;
 			}
/*			_bt = new BTClient(adapter, BHDevices[0].getAddress());
compile error
ramos commented this code out
*/
 			_NConnListener = new NewConnectedListener(msgHandler,msgHandler);
 			_bt.addConnectedEventListener(_NConnListener);
 			if(_bt.IsConnected()){
 				_bt.start();
 			}
 		}
 		
 	}
 	
 	public void bt_enabled(){
     	//Do something
     }
     public void bt_disabled(){
     	//Do something
     }
 	
 	private final Handler msgHandler = new Handler(){
     	public void handleMessage(Message msg)
     	{
     		String UID = msg.getData().getString("UID");
     		switch (msg.what)
     		{
     		case HEART_RATE:
     			float HeartRate = msg.getData().getFloat("HeartRate");
     			dataProcessing.push(UID, BiometricType.HEARTRATE, HeartRate);
     		break;
     		
     		case RESPIRATION_RATE:
     			float RespirationRate = msg.getData().getFloat("RespirationRate");
     			dataProcessing.push(UID, BiometricType.RESPIRATION, RespirationRate);
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
