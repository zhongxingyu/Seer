 package net.shortround.rose;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.BatteryManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class RoseActivity extends Activity {
 	// Debug
 	private static final String TAG = "RoseActivity";
 		
 	// Intent request codes
 	private static final int REQUEST_ENABLE_BT = 1;
 		
 	// Messages sent from the Web Service
 	public static final int MESSAGE_DECAY = 0;
 	public static final int MESSAGE_REVERT = 1;
 	public static final int MESSAGE_TOGGLE_DISPLAY = 2;
 	
 	// Messages sent from the Bluetooth Service
 	public static final int MESSAGE_FAILURE = 3;
 	public static final int MESSAGE_GET_DATA = 4;
 	public static final int MESSAGE_READ = 5;
 	public static final int MESSAGE_STATE_CHANGE = 6;
 	public static final int MESSAGE_WRITE = 7;
 	
 	private BroadcastReceiver batteryReceiver;
 	private RoseView roseView;
 	
 	private StringBuffer outputStringBuffer;
 	private BluetoothAdapter bluetoothAdapter = null;
 	private BluetoothService bluetoothService = null;
 	
     /*** Lifecycle Callbacks ***/
 	
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
     	switch (requestCode) {
     	case REQUEST_ENABLE_BT:
     		if (resultCode == Activity.RESULT_OK) {
     			setupBluetoothService();
     		} else {
     			Log.e(TAG, "Bluetooth not enabled");
     			finish();
     		}
     		break;
     	}
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         // Hide the title
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         // Get the local Bluetooth Adapter
         bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         
         // If the adapter is null, then Bluetooth is not supported
         if (bluetoothAdapter == null) {
         	Log.e(TAG, "No bluetooth adapter");
         	finish();
         	return;
         }
         
         // Build the view
         roseView = new RoseView(this);
         roseView.setFocusable(true);
         
         // Build the battery receiver
         batteryReceiver = new BroadcastReceiver() {
         	@Override
         	public void onReceive(Context context, Intent intent) {
        		int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
         		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
         		
         		int level = -1;
         		if (rawLevel  >= 0 && scale > 0) {
         			level = (rawLevel * 100) / scale;
         		}
         		roseView.setBattery(level);
         	}
         };
         
         // Spool up the web server
         WebServer server = WebServer.getInstance();
         server.setAssetManager(this.getAssets());
         server.setHandler(webHandler);
         server.setView(roseView);
         
         // Show the view
         setContentView(roseView);
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	
     	// Unregister the battery receiver
     	unregisterReceiver(batteryReceiver);
     }
     
     @Override 
     protected void onResume() {
     	super.onResume();
     	
     	// Register the battery receiver
     	registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
     	
         // Go full screen
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         
         // Keep the screen on
         roseView.setKeepScreenOn(true);
         
         // Hide the soft buttons
         // roseView.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
         roseView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
         
         // Restart Bluetooth if we need to
         if (bluetoothService != null) {
     		if (bluetoothService.getState() == BluetoothService.STATE_NONE) {
     			bluetoothService.start();
     		}
     	}
     }
     
     @Override
     public void onStart() {
     	super.onStart();
     	
     	// Request bluetooth 
     	if (bluetoothAdapter.isEnabled()) {
     		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
     		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
     	} else {
     		if (bluetoothService == null) setupBluetoothService();
     	}
     }
     
     /*** Bluetooth Methods ***/
     
     private final Handler bluetoothHandler = new Handler() {
     	@Override
     	public void handleMessage(Message message) {
     		switch (message.what) {
     		case MESSAGE_FAILURE:
     			break;
     		case MESSAGE_GET_DATA:
     			break;
     		case MESSAGE_READ:
     			byte[] readBuf = (byte[]) message.obj;
     			String readMessage = new String(readBuf, 0, message.arg1);
     			parseMessage(readMessage);
     			break;
     		case MESSAGE_STATE_CHANGE:
     			switch(message.arg1) {
     			case BluetoothService.STATE_NONE:
     			case BluetoothService.STATE_LISTEN:
     				break;
     			case BluetoothService.STATE_CONNECTING:
     				break;
     			case BluetoothService.STATE_CONNECTED:
     				break;
     			}
     			break;
     		case MESSAGE_WRITE:
     			break;
     		}
     	}
     };
     
     public void ensureDiscoverable() {
         if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
             Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
             discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
             startActivity(discoverableIntent);
         }
     }
     
     private void parseMessage(String message) {
     	if (message.equals("decay")) {
     		roseView.decay();
     	} else if (message.equals("revert")) {
     		roseView.revert();
     	} else if (message.equals("display")) {
     		roseView.toggleDisplay();
     	}
     	
     	// A sent message always returns the current data of the view
     	sendMessage(roseView.getSerializedData().toString());
     }
     
     private void sendMessage(String message) {
     	// Check that we have a connection
     	if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
     		Log.e(TAG, "Send message with no connection");
     		return;
     	}
     	
     	// Check that there's something to send
     	if (message.length() > 0) {
     		// Get the message as bytes
     		byte[] send = message.getBytes();
     		bluetoothService.write(send);
     		
     		// Clean up
     		outputStringBuffer.setLength(0);
     	}
     }
     
     private void setupBluetoothService() {
     	// Initialize the bluetooth service
     	bluetoothService = new BluetoothService(this, bluetoothHandler);
     	
     	// Build the outgoing buffer
     	outputStringBuffer = new StringBuffer("");
     }
     
     /*** Web Methods ***/
     
     private final Handler webHandler = new Handler() {
     	@Override
     	public void handleMessage(Message msg) {
     		switch (msg.what) {
     			case MESSAGE_DECAY:
     				roseView.decay();
     				break;
     			case MESSAGE_REVERT:
     				roseView.revert();
     				break;
     			case MESSAGE_TOGGLE_DISPLAY:
     				roseView.toggleDisplay();
     				break;
     		}
     	}
     };
 }
