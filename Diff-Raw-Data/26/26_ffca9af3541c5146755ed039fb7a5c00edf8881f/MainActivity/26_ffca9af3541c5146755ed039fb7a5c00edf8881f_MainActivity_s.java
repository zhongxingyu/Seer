 package com.ryuchihoon.bluetraincon;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.RadioGroup;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
     // Debugging
     private static final String TAG = "BlueTrainCon";
     private static final boolean D = true;
 
     // Message types sent from the BluetoothChatService Handler
     public static final int MESSAGE_STATE_CHANGE = 1;
     public static final int MESSAGE_READ = 2;
     public static final int MESSAGE_WRITE = 3;
     public static final int MESSAGE_DEVICE_NAME = 4;
     public static final int MESSAGE_TOAST = 5;
 
     // Key names received from the BluetoothChatService Handler
     public static final String DEVICE_NAME = "device_name";
     public static final String TOAST = "toast";
 
     // Intent request codes
     private static final int REQUEST_CONNECT_DEVICE = 1;
     private static final int REQUEST_ENABLE_BT = 2;
     
     private String mLastSentData = "";
     
     private String mConnectedDeviceName = null;
     // Local Bluetooth adapter
     private BluetoothAdapter mBluetoothAdapter = null;
     // Member object for the chat services
     private BluetoothChatService mChatService = null;
     
     private TextView mTextViewBluetoothStatus;
     private SeekBar mSeekBarSpeed;
     private RadioGroup mRadioGroupDir;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if(D) Log.e(TAG, "+ ON CREATE +");
 		setContentView(R.layout.activity_main);
 		
 		mTextViewBluetoothStatus = (TextView)findViewById(R.id.textViewStatusBluetooth);
 		
         // Get local Bluetooth adapter
         mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 
         // If the adapter is null, then Bluetooth is not supported
         if (mBluetoothAdapter == null) {
             Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
             finish();
             return;
         }
         
         View scanBtn = findViewById(R.id.buttonScan);
         scanBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				scan();
 			}
 		});
         
         mSeekBarSpeed = (SeekBar)findViewById(R.id.seekBarSpeed);
         mRadioGroupDir = (RadioGroup)findViewById(R.id.radioGroupDirection);
 
         mSeekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				sendDataFromControls();
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				sendDataFromControls();
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				sendDataFromControls();
 			}
         });
         mRadioGroupDir.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				sendDataFromControls();
 			}
 		});
 	}
 	
     @Override
     public void onStart() {
         super.onStart();
         if(D) Log.e(TAG, "++ ON START ++");
 
         // If BT is not on, request that it be enabled.
         // setupChat() will then be called during onActivityResult
         if (!mBluetoothAdapter.isEnabled()) {
             Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
         // Otherwise, setup the chat session
         } else {
             if (mChatService == null) setupChat();
         }
     }
     
     @Override
     public synchronized void onResume() {
         super.onResume();
         if(D) Log.e(TAG, "+ ON RESUME +");
 
         // Performing this check in onResume() covers the case in which BT was
         // not enabled during onStart(), so we were paused to enable it...
         // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
         if (mChatService != null) {
             // Only if the state is STATE_NONE, do we know that we haven't started already
             if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
               // Start the Bluetooth chat services
               mChatService.start();
             }
         }
     }
     
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 	
     @Override
     public void onDestroy() {
         super.onDestroy();
         // Stop the Bluetooth chat services
         if (mChatService != null) mChatService.stop();
         if(D) Log.e(TAG, "--- ON DESTROY ---");
     }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
 	}
 	
     private void setupChat() {
         Log.d(TAG, "setupChat()");
 

         // Initialize the BluetoothChatService to perform bluetooth connections
         mChatService = new BluetoothChatService(this, mHandler);
     }
     
    private final void setStatus(int resId) {
    	setStatus(getString(resId));
     }
    private final void setStatus(CharSequence text) {
     	mTextViewBluetoothStatus.setText(text);
     }
     
     /**
      * Sends a message.
      * @param message  A string of text to send.
      */
     private void sendMessage(String message) {
         // Check that we're actually connected before trying anything
         if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
             Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
             return;
         }
         
         // Check that there's actually something to send
         if (message.length() > 0 && !message.equals(mLastSentData)) {
             mLastSentData = message;
             // Get the message bytes and tell the BluetoothChatService to write
             byte[] send = message.getBytes();
             mChatService.write(send);
         }
     }
     
     // The Handler that gets information back from the BluetoothChatService
     private final Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
             case MESSAGE_STATE_CHANGE:
                 if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                 switch (msg.arg1) {
                 case BluetoothChatService.STATE_CONNECTED:
                    setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                     resetControllers();
                     mLastSentData = "";
                     break;
                 case BluetoothChatService.STATE_CONNECTING:
                    setStatus(R.string.title_connecting);
                     break;
                 case BluetoothChatService.STATE_LISTEN:
                 case BluetoothChatService.STATE_NONE:
                    setStatus(R.string.title_not_connected);
                     break;
                 }
                 break;
             case MESSAGE_WRITE:
                 byte[] writeBuf = (byte[]) msg.obj;
                 // construct a string from the buffer
                 String writeMessage = new String(writeBuf);
                 if(D) Log.d(TAG,"write:"+writeMessage);
                 break;
             case MESSAGE_READ:
                 byte[] readBuf = (byte[]) msg.obj;
                 // construct a string from the valid bytes in the buffer
                 String readMessage = new String(readBuf, 0, msg.arg1);
                 if(D) Log.d(TAG,"read:"+readMessage);
                 break;
             case MESSAGE_DEVICE_NAME:
                 // save the connected device's name
                 mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                 Toast.makeText(getApplicationContext(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                 break;
             case MESSAGE_TOAST:
                 Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                                Toast.LENGTH_SHORT).show();
                 break;
             }
         }
     };
 
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if(D) Log.d(TAG, "onActivityResult " + resultCode);
         switch (requestCode) {
         case REQUEST_CONNECT_DEVICE:
             // When DeviceListActivity returns with a device to connect
             if (resultCode == Activity.RESULT_OK) {
                 connectDevice(data);
             }
             break;
         case REQUEST_ENABLE_BT:
             // When the request to enable Bluetooth returns
             if (resultCode == Activity.RESULT_OK) {
                 // Bluetooth is now enabled, so set up a chat session
                 setupChat();
             } else {
                 // User did not enable Bluetooth or an error occurred
                 Log.d(TAG, "BT not enabled");
                 Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                 finish();
             }
         }
     }
 
     private void connectDevice(Intent data) {
         // Get the device MAC address
         String address = data.getExtras()
             .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
         // Get the BluetoothDevice object
         BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
         // Attempt to connect to the device
         mChatService.connect(device);
     }
     
     private void scan() {
     	Intent serverIntent = new Intent(this, DeviceListActivity.class);
     	startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
     }
     
     private void sendDataFromControls() {
     	boolean backward = mRadioGroupDir.getCheckedRadioButtonId() == R.id.radioDirBackward;
     	int speed = mSeekBarSpeed.getProgress();
     	sendData(backward, speed);
     }
     
     private void sendData(boolean backward, int speed) {
     	if(D) Log.d(TAG, "sendData("+backward+", "+speed+")");
     	if(speed > 240) { speed = 255; }
     	if(speed < 15) { speed = 0; }
 
     	String strMsg = "(s,"+(speed*(backward?-1:1))+")";
     	sendMessage(strMsg);
     }
     
     private void resetControllers() {
     	mRadioGroupDir.check(R.id.radioDirForward);
     	mSeekBarSpeed.setProgress(0);
     }
 }
