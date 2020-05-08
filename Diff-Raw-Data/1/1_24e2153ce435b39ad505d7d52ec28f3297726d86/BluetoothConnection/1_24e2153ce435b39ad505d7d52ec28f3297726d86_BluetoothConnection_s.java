 package org.obd2.bluetooth;
 
 import java.lang.reflect.Method;
 import org.apache.cordova.CallbackContext;
 import org.apache.cordova.CordovaPlugin;
 import org.apache.cordova.PluginResult;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.widget.Toast;
 
 public class BluetoothConnection extends CordovaPlugin {
 
 	//Android specific tag-messages
 	private static final String TAG ="BluetoothConnection";
 	private static final boolean D = true;
 	
 	
 	// Member-Variables
 	public BluetoothAdapter mBluetoothAdapter;
 	public JSONArray mListOfDiscoveredDevices;
 	public String mConnectedDeviceName;
 	public ConnectionHandler mConnectionHandler;
 	
 	
 	
 	// Phonegap-specific actions, which call the function
 	public String ACTION_ENABLEBLUETOOTH = "enableBluetooth";
 	public String ACTION_DISABLEBLUETOOTH = "disableBluetooth";
 	public String ACTION_DISCOVERDECIVES = "discoverDevices";
 	public String ACTION_STOPDISCOVERDEVICES = "stopDiscoverDevices";
 	public String ACTION_CREATEBOND = "createBond";
 	public String ACTION_WRITEMESSAGE = "writeMessage";
 
 	// not usable, this moment 
 	public String ACTION_DISCONNECT = "disconnect";
 
 	//Message types sent from the ConnectionHandler
 	public static final int MESSAGE_STATE_CHANGE = 1;
     public static final int MESSAGE_READ = 2;
     public static final int MESSAGE_WRITE = 3;
     public static final int MESSAGE_DEVICE_NAME = 4;
     public static final int MESSAGE_TOAST = 5;
     
     public static final String DEVICE_NAME = "device_name";
     public static final String TOAST = "toast";
 	
 	
 	
 	@Override
 	public boolean execute(String action, JSONArray args,
 			CallbackContext callbackContext) throws JSONException {
 
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 
 		if (mBluetoothAdapter.equals(null)) {
 			Log.i("obd2.bluetooth", "no adapter was found");
 		}
 		
 		mConnectionHandler = new ConnectionHandler(mHandler);
 		
 		
 		if (action.equals(ACTION_ENABLEBLUETOOTH)) {
 			enableBluetooth();
 		}
 		else if (action.equals(ACTION_DISABLEBLUETOOTH)) {
 			disableBluetooth();
 		}
 		else if (action.equals(ACTION_DISCOVERDECIVES)) {
 			discoverDevices();
 		}
 		else if (action.equals(ACTION_STOPDISCOVERDEVICES)) {
 			stopDiscovering(callbackContext);
 		}
 		else if (action.equals(ACTION_CREATEBOND)) {
 			try {
 				BluetoothDevice remoteBtDevice = createBond(args, callbackContext);
 				connect(remoteBtDevice, callbackContext);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		else if(action.equals(ACTION_WRITEMESSAGE)){
 			writeMessage(args.getString(0));
 		}
 		
 		else if (action.equals(ACTION_DISCONNECT)) {
 			disconnect();
 		}
 		
 		return false;
 
 	}
 
 	
 	
 	
 	
 	public void enableBluetooth() {
 		if (!mBluetoothAdapter.equals(null)) {
 			mBluetoothAdapter.enable();
 			Log.i("obd2.bluetooth", "bluetooth on");
 		}
 
 	}
 
 	public void disableBluetooth() {
 		if (mBluetoothAdapter.isEnabled()) {
 			mBluetoothAdapter.disable();
 			Log.i("obd2.bluetooth", "bluetooth off");
 		}
 	}
 
 	public void discoverDevices() {
 		mListOfDiscoveredDevices = new JSONArray();
 		Log.i("Log", "in the start searching method");
 		IntentFilter intentFilter = new IntentFilter(
 				BluetoothDevice.ACTION_FOUND);
 		cordova.getActivity().registerReceiver(mFoundDevices, intentFilter);
 		mBluetoothAdapter.startDiscovery();
 	}
 	
 	private void stopDiscovering(CallbackContext callbackContext) {
 		if (mBluetoothAdapter.isDiscovering()) {
 			mBluetoothAdapter.cancelDiscovery();
 		}
 
 		PluginResult res = new PluginResult(PluginResult.Status.OK,
 				mListOfDiscoveredDevices);
 		res.setKeepCallback(true);
 		callbackContext.sendPluginResult(res);
 
 		Log.i("Info", "Stopped discovering Devices !");
 
 	}
 	
 	
 	public BluetoothDevice createBond(JSONArray args, CallbackContext callbackContext) throws Exception {
 		String macAddress = args.getString(0);
 		Log.i("obd2.bluetooth", "Connect to MacAddress "+macAddress);
 		BluetoothDevice btDevice = mBluetoothAdapter.getRemoteDevice(macAddress);
 		Log.i("Device","Device "+btDevice);
 		
 		Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
         Method createBondMethod = class1.getMethod("createBond");  
         Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);  
         
         if(btDevice.equals(null))
         	throw new NullPointerException("Remote BluetoothDevice could not be paired !");
         
         return btDevice;
     }       
 	
 	public void removeBond(BluetoothDevice btDevice) throws Exception {
 		  Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
 	      Method removeBondMethod = btClass.getMethod("removeBond");  
 	      Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);  
 	}
 	
 
 
 	public void connect(BluetoothDevice btDevice, CallbackContext callbackContext) {
 		if(!btDevice.equals(null)){
 			mConnectionHandler.connect(btDevice, false);
 			PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
 			result.setKeepCallback(true);
 			callbackContext.sendPluginResult(result);
 			
 			Log.i(TAG, "Status after connecting "+mConnectionHandler.getState());
 		}
 		else {
 			callbackContext.error("Could not connect to "+btDevice.getAddress());
 		}
 	}
 	
 	
 	public void disconnect(){
 		
 	}
 	
 	public void writeMessage(String message){
 		if(mConnectionHandler.getState() != ConnectionHandler.STATE_CONNECTED){
 			Log.i(TAG, "Could not write to device");
 			Log.i(TAG, "State "+mConnectionHandler.getState());
 		}
 		
 		if(message.length() > 0) {
 			byte[] send = message.getBytes();
 			mConnectionHandler.write(send);
 			
 			Log.i(TAG, "sending "+message);
 			
 		}
 		else {
 			Log.i(TAG, "There is nothing to send.");
 		}
 	}
 
 
 
 	private BroadcastReceiver mFoundDevices = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Message msg = Message.obtain();
 			String action = intent.getAction();
 			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 				Toast.makeText(context, "found Device !", Toast.LENGTH_SHORT).show();
 
 				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 				
 				
 				Log.i("FOUND", "Name " + device.getName() + "-" + device.getAddress());
 				JSONObject discoveredDevice = new JSONObject();
 				try {
 					discoveredDevice.put("name", device.getName());
 					discoveredDevice.put("adress", device.getAddress());
 					if (!isJSONInArray(discoveredDevice)) {
 						mListOfDiscoveredDevices.put(discoveredDevice);
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 
 			}
 
 		}
 	};
 	
 	
 	
 	
 	private final Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
             
             case MESSAGE_STATE_CHANGE:
                 if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                 
                 switch (msg.arg1) {
                 	case ConnectionHandler.STATE_CONNECTED:
                 		Log.i(TAG, "ConnectionHandler.STATE_CONNECTED !");
                 		break;
                 	case ConnectionHandler.STATE_CONNECTING:
                 		Log.i(TAG, "ConnectionHandler.STATE_CONNECTING !");
                 		break;
                 	case ConnectionHandler.STATE_LISTEN:
                 		Log.i(TAG, "ConnectionHandler.STATE_LISTEN !");
                 		break;
                 	case ConnectionHandler.STATE_NONE:
                 		Log.i(TAG, "ConnectionHandler.STATE_NONE !");
                 		break;
                 	}
                 	break;
             	
             	case MESSAGE_WRITE:
             		byte[] writeBuf = (byte[]) msg.obj;
             		// construct a string from the buffer
             		String writeMessage = new String(writeBuf);
             		Log.i(TAG, "Write "+writeMessage);
             		break;
             	case MESSAGE_READ:
             		byte[] readBuf = (byte[]) msg.obj;
             		// construct a string from the valid bytes in the buffer
             		String readMessage = new String(readBuf, 0, msg.arg1);
             		Log.i(TAG, "Read "+readMessage);
             		break;
             	case MESSAGE_DEVICE_NAME:
             		// save the connected device's name
             		mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
             		Log.i(TAG, mConnectedDeviceName);
             		break;
             	case MESSAGE_TOAST:
             		String message = msg.getData().getString(TOAST);
             		Log.i(TAG, "Connection lost : " +message);
             		break;
             	}
         	}
     	};
     	
     	
 	
    /**
 	 * This function checks, whether a discovered Bluetooth-Device is already in the 
 	 * result-list. If a device is already in list, the function returns true, otherwise
 	 * false.
 	 * 
 	 * @param discoveredDevice - device which should be checked
 	 * @return
 	 */
 	public boolean isJSONInArray(JSONObject discoveredDevice) {
 		boolean result = false;
 
 		try {
 			for (int cnt = 0; cnt < mListOfDiscoveredDevices.length(); cnt++) {
 				String listElement;
 
 				listElement = mListOfDiscoveredDevices.get(cnt).toString();
 
 				String checkElement = discoveredDevice.toString();
 
 				if (listElement.equals(checkElement)) {
 					result = true;
 					System.out.println(listElement + "-" + checkElement);
 				}
 			}
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 	
 	
 	
 
 }
