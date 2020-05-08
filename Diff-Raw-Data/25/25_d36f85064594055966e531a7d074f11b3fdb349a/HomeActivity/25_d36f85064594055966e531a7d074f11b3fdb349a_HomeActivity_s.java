 package com.androidmontreal.arduino.bluetooth;
 
 import com.androidmontreal.opencv.R;
 
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 //import android.app.ActionBar;
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.webkit.ValueCallback;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class HomeActivity extends Activity {
 	protected static final String TAG = "RoogleTank";
 	public static final boolean D = true;
 	// Used to control App on market vs App sold with bluetooth
 	private static final boolean mAppWithBlueToothProbe = true;
 
 	// Bluetooth debug Views
 	private TextView mTitle;
 	private ListView mConversationView;
 	private EditText mOutEditText;
 	private Button mSendButton;
 
 	// OpenCV views
 	private TextView textView1;
 
 	private ValueCallback<Uri> mUploadMessage;
 
 	private static final int SWITCH_LANGUAGE = 24;
 	private static final int FILECHOOSER_RESULTCODE = 21;
 	private static final int REQUEST_PROBE_IDENTIFIER = 22;
 
 	// Used to turn on bluetooth only if in a menu which requires it.
 	private boolean mShouldBeAbleToUseBlueToothForDataEntry = false;
 
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
 	// private static final int REQUEST_CONNECT_DEVICE_SECURE = 10;
 	// private static final int REQUEST_CONNECT_DEVICE_INSECURE = 11;
 	private static final int REQUEST_CONNECT_DEVICE_SERIAL = 12;
 	private static final int REQUEST_ENABLE_BT = 13;
 
 	// Name of the connected device
 	private String mConnectedDeviceName = null;
 	// MAC address of current/most recent probe
 	private String mProbeMacAddress = null;
 	// Array adapter for the conversation thread
 	private ArrayList<String> mProbeConversationArrayAdapter;
 	
 	 // Array adapter for the conversation thread
     private ArrayAdapter<String> mConversationArrayAdapter;
  
 	// String buffer for outgoing messages
 	private StringBuffer mOutStringBuffer;
 	// Local Bluetooth adapter
 	private BluetoothAdapter mBluetoothAdapter = null;
 	// Member object for the chat services
 	private BluetoothProbeService mProbeService = null;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (D)
 			Log.e(TAG, "+++ ON CREATE +++");
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 
 		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
 
 		/*
 		 * Create a TextView and set its content. the text is retrieved by
 		 * calling a native function.
 		 */
 		textView1 = (TextView) findViewById(R.id.textview1);
 		// textView1.setText( stringFromJNI() );
 
		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		// new CopyAssetsTask().execute();
		// ActionBar actionBar = getActionBar();
		// actionBar.setDisplayHomeAsUpEnabled(true);
		
 		// Initialize the compose field with a listener for the return key
         mOutEditText = (EditText) findViewById(R.id.edit_text_out);
         mOutEditText.setOnEditorActionListener(mWriteListener);
 
 		
 		getPneuLogicProbe();
 		
 		
 //		setupChat();
 
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		if (D)
 			Log.e(TAG, "++ ON START ++");
 		enableBTandSetup("");
 
 	}
 
 	private void enableBTandSetup(String address) {
 		if (mShouldBeAbleToUseBlueToothForDataEntry) {
 
 			// Get local Bluetooth adapter
 			if (mBluetoothAdapter == null)
 				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 			// If the adapter is still null, then Bluetooth is not supported
 			if (mBluetoothAdapter == null) {
 				mShouldBeAbleToUseBlueToothForDataEntry = false;
 				Toast.makeText(this, "Bluetooth is not available",
 						Toast.LENGTH_LONG).show();
 				return;
 			}
 
 			// If BT is not on, request that it be enabled.
 			// setupChat() will then be called during onActivityResult
 			if (!mBluetoothAdapter.isEnabled()) {
 				Intent enableIntent = new Intent(
 						BluetoothAdapter.ACTION_REQUEST_ENABLE);
 				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
 				// Otherwise, setup the chat session
 			} else {
 				if (mProbeService == null)
 					setupChat(address);
 			}
 		}
 	}
 
 	@Override
 	public synchronized void onResume() {
 		super.onResume();
 		if (D)
 			Log.e(TAG, "+ ON RESUME +");
 		startProbeServiceIfNotStarted("");
 
 	}
 
 	private void startProbeServiceIfNotStarted(String address) {
 		if (mShouldBeAbleToUseBlueToothForDataEntry) {
 			// Performing this check in onResume() covers the case in which BT
 			// was
 			// not enabled during onStart(), so we were paused to enable it...
 			// onResume() will be called when ACTION_REQUEST_ENABLE activity
 			// returns.
 			if (mProbeService != null) {
 				// Only if the state is STATE_NONE, do we know that we haven't
 				// started
 				// already
 				if (mProbeService.getState() == BluetoothProbeService.STATE_NONE) {
 					// Start the Bluetooth chat services
 					mProbeService.start();
 					if (address.length() > 2)
 						connectDevice(address, false, true);
 				}
 			}
 		}
 	}
 
 	@Override
 	public synchronized void onPause() {
 		super.onPause();
 		if (D)
 			Log.e(TAG, "- ON PAUSE -");
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		if (D)
 			Log.e(TAG, "-- ON STOP --");
 	}
 
 	@Override
 	public void onDestroy() {
 
 		super.onDestroy();
 		// Stop the Bluetooth chat services
 		if (mProbeService != null)
 			mProbeService.stop();
 		if (D)
 			Log.e(TAG, "--- ON DESTROY ---");
 	}
 
 	private void setupChat(String address) {
 		Log.d(TAG, "setupChat()");
 
 		// Initialize the array adapter for the conversation thread
 		mProbeConversationArrayAdapter = new ArrayList<String>();
 
 		// Initialize the BluetoothChatService to perform bluetooth connections
 		mProbeService = new BluetoothProbeService(this, mHandler);
 		if (address.length() > 2)
 			startProbeServiceIfNotStarted(address);
 
 		// Initialize the buffer for outgoing messages
 		mOutStringBuffer = new StringBuffer("");
 
 	}
 	private void setupChat() {
         Log.d(TAG, "setupChat()");
 
         // Initialize the array adapter for the conversation thread
         mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
         mConversationView = (ListView) findViewById(R.id.in);
         mConversationView.setAdapter(mConversationArrayAdapter);
 
         // Initialize the compose field with a listener for the return key
         mOutEditText = (EditText) findViewById(R.id.edit_text_out);
         mOutEditText.setOnEditorActionListener(mWriteListener);
 
         // Initialize the send button with a listener that for click events
         mSendButton = (Button) findViewById(R.id.button_send);
         mSendButton.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 // Send a message using content of the edit text widget
                 TextView view = (TextView) findViewById(R.id.edit_text_out);
                 String message = view.getText().toString();
                 sendMessage(message);
             }
         });
 
         // Initialize the buffer for outgoing messages
         mOutStringBuffer = new StringBuffer("");
     }
 	  // The action listener for the EditText widget, to listen for the return key
     private TextView.OnEditorActionListener mWriteListener =
         new TextView.OnEditorActionListener() {
         public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
             // If the action is a key-up event on the return key, send the message
             if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                 String message = view.getText().toString();
                 sendMessage(message);
             }
             if(D) Log.i(TAG, "END onEditorAction");
             return true;
         }
     };
 
 
 	// The Handler that gets information back from the BluetoothChatService
 	private final Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 
 			switch (msg.what) {
 			case MESSAGE_STATE_CHANGE:
 				if (D)
 					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
 				switch (msg.arg1) {
 				case BluetoothProbeService.STATE_CONNECTED:
 					setStatus("connected_to " + mConnectedDeviceName);
 					mProbeConversationArrayAdapter.clear();
 					break;
 				case BluetoothProbeService.STATE_CONNECTING:
 					setStatus("connecting");
 					break;
 				case BluetoothProbeService.STATE_LISTEN:
 					setStatus("connected_to " + mConnectedDeviceName);
 				case BluetoothProbeService.STATE_NONE:
 					setStatus("not_connected");
 					break;
 				}
 				break;
 			case MESSAGE_WRITE:
 				byte[] writeBuf = (byte[]) msg.obj;
 				// construct a string from the buffer
 				String writeMessage = new String(writeBuf);
 				mProbeConversationArrayAdapter.add(getString(R.string.app_name)
 						+ ":  " + writeMessage);
 				break;
 			case MESSAGE_READ:
 				byte[] readBuf = (byte[]) msg.obj;
 				// construct a string from the valid bytes in the buffer
 				setStatus("connected_to " + mConnectedDeviceName);
 				String readMessage = new String(readBuf, 0, msg.arg1);
 				mProbeConversationArrayAdapter.add(mConnectedDeviceName + ":  "
 						+ readMessage);
 				// mWebView.loadUrl("javascript:hub.publish('probeResponse','"
 				// + readMessage.trim() + "')");
 				Toast.makeText(getApplicationContext(),
 						"Arduino replied: " + readMessage.trim(),
 						Toast.LENGTH_SHORT).show();
 
 				Log.d(TAG, "Probe replied:" + readMessage.trim() + ":");
 				break;
 			case MESSAGE_DEVICE_NAME:
 				// save the connected device's name
 				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
 				Toast.makeText(getApplicationContext(),
 						"Connected to " + mConnectedDeviceName,
 						Toast.LENGTH_SHORT).show();
 				break;
 			case MESSAGE_TOAST:
 				Toast.makeText(getApplicationContext(),
 						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
 						.show();
 
 				break;
 			}
 		}
 	};
 
 	/**
 	 * Sends a message.
 	 * 
 	 * @param message
 	 *            A string of text to send.
 	 */
 	private void sendMessage(String message) {
 		// Check that we're actually connected before trying anything
 		if (mProbeService.getState() != BluetoothProbeService.STATE_CONNECTED) {
 			Toast.makeText(this, "not_connected", Toast.LENGTH_SHORT).show();
 			// cant send webview from here, exception called from wrong thread
 			// mWebView.loadUrl("javascript:hub.publish('probeResponse','Disconnected. "
 			// + "')");
 			return;
 		}
 
 		// Check that there's actually something to send
 		if (message.length() > 0) {
 			// Get the message bytes and tell the BluetoothChatService to write
 			message = message + "\n";
 			byte[] send = message.getBytes();
 			mProbeService.write(send);
 			Log.d(TAG, "Sent to probe: " + message);
 
 			// Reset out string buffer to zero and clear the edit text field
 			mOutStringBuffer.setLength(0);
 		}
 	}
 
 	
 
 	private final void setStatus(CharSequence subTitle) {
		mTitle.setText(R.string.title_connected_to);
		mTitle.append(mConnectedDeviceName);
		// final ActionBar actionBar = getActionBar();
		// actionBar.setSubtitle(subTitle);
 	}
 
 	private void connectDevice(String address, boolean secure, boolean serial) {
 		// Get the BluetoothDevice object
 		if (mBluetoothAdapter == null)
 			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
 		// Attempt to connect to the device
 		if (mProbeService != null) {
 			mProbeService.connect(device, secure, serial);
 		} else {
 			enableBTandSetup(address);
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent intent) {
 		if (resultCode != Activity.RESULT_OK) {
 			return;
 		}
 
 		if (D)
 			Log.d(TAG, "Returning to activty with result: " + resultCode);
 		switch (requestCode) {
 		case REQUEST_CONNECT_DEVICE_SERIAL:
 			// When DeviceListActivity returns with a device to connect
 			if (resultCode == Activity.RESULT_OK) {
 				// Get the device MAC address
 				String address = intent.getExtras().getString(
 						BluetoothLister.EXTRA_PROBE_MAC);
 				connectDevice(address, false, true);
 
 				
 					mProbeMacAddress = address;
 				
 			}
 			break;
 		case REQUEST_ENABLE_BT: {
 			if (D)
 				Log.d(TAG,
 						"Returning from request to enable Bluetooth result: "
 								+ resultCode + " vs ok:" + Activity.RESULT_OK);
 			if (resultCode == Activity.RESULT_OK) {
 				getPneuLogicProbe();
 			} else {
 				Toast.makeText(
 						this,
 						"The app can't connect to the probe without bluetooth. \nYou will have to enter the data manually.",
 						Toast.LENGTH_LONG).show();
 			}
 			break;
 		}
 		}
 
 	}
 
 	private void getPneuLogicProbe() {
 
 		if (mProbeMacAddress != null && mProbeMacAddress.length() > 2) {
 			enableBTandSetup(mProbeMacAddress);
 			Log.e(TAG, "Known MAC addresses, not asking user to choose."
 					+ mProbeMacAddress);
 		} else {
 			Log.e(TAG, "No known MAC addresses,  asking user.");
 			Intent i = new Intent(this, BluetoothLister.class);
 			startActivityForResult(i, REQUEST_CONNECT_DEVICE_SERIAL);
 		}
 
 	}
 
 	public int sendProbeCommandJIS(String command) {
 		sendMessage(command);
 		return 1;
 	}
 
 }
