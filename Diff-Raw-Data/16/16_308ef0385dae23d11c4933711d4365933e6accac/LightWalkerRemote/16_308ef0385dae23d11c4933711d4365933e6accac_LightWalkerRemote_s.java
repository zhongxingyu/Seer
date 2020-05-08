 package com.inappropirates.remotecontrol;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.inappropirates.remotecontrol.R;
 import com.inappropirates.util.BluetoothChatService;
 import com.inappropirates.util.DeviceListActivity;
 
 @SuppressLint("HandlerLeak")
 public class LightWalkerRemote extends Activity {
 
 	private ListView modeListView;
 
 	// Intent request codes
 	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
 	private static final int REQUEST_ENABLE_BT = 3;
 
 	static final String EXTRA_MODE_NAME = "android.intent.extra.MODE_NAME";
 
 	private Button mSendButton;
 
 	private SharedPreferences mPrefs;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (AppUtil.DEBUG)
 			Log.e(AppUtil.TAG, "--Create--");
 
 		setContentView(R.layout.activity_light_walker_remote);
 
 		AppUtil.mContext = getApplicationContext();
 		AppUtil.mTitle = (TextView) findViewById(R.id.Status);
 
 		AppUtil.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		AppUtil.mLightWalker = this;
 
 		modeListView = (ListView) findViewById(R.id.modeList);
 		ArrayAdapter<String> mModeArrayAdapter = new ArrayAdapter<String>(
 				modeListView.getContext(), R.layout.list_modes, getResources()
 						.getStringArray(R.array.modeList));
 		modeListView.setAdapter(mModeArrayAdapter);
 
 		modeListView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				String itemName = (String) (modeListView
 						.getItemAtPosition(position));
 				Intent intent = new Intent(LightWalkerRemote.this,
 						SettingsActivity.class);
 				intent.putExtra(EXTRA_MODE_NAME, itemName);
 				startActivity(intent);
 			}
 		});
 
 		// If the adapter is null, then Bluetooth is not supported
 		if (AppUtil.mBluetoothAdapter == null) {
 			Toast.makeText(this, "Bluetooth is not available",
 					Toast.LENGTH_LONG).show();
 			finish();
 			return;
 		}
 
		mPrefs = getSharedPreferences("main_preferences.xml", MODE_PRIVATE);
		AppUtil.mSelectedMode = LightWalkerModes.valueOf(mPrefs.getString(
				"mainPrefDefaultMode", LightWalkerModes.pulse.toString()));
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		if (AppUtil.DEBUG)
 			Log.e(AppUtil.TAG, "--START--");
 
 		if (!AppUtil.mBluetoothAdapter.isEnabled()) {
 			Intent enableIntent = new Intent(
 					BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
 			// Otherwise, setup the chat session
 		} else {
 			if ((AppUtil.mChatService != null)
 					&& (AppUtil.mChatService.getState() != BluetoothChatService.STATE_CONNECTED)) {
 				AppUtil.mChatService.stop();
 				AppUtil.mChatService = null;
 				setupBluetooth();
 			} else if (AppUtil.mChatService == null)
 				setupBluetooth();
 
 			this.modeListView.requestFocus();
 		}
 	}
 
 	@Override
 	public synchronized void onResume() {
 		super.onResume();
 		if (AppUtil.DEBUG)
 			Log.e(AppUtil.TAG, "--RESUME--");
 
 		// Performing this check in onResume() covers the case in which BT was
 		// not enabled during onStart(), so we were paused to enable it...
 		// onResume() will be called when ACTION_REQUEST_ENABLE activity
 		// returns.
 		if ((AppUtil.mChatService != null)
 				&& (AppUtil.mChatService.getState() == BluetoothChatService.STATE_NONE)) {
 			// Only if the state is STATE_NONE, do we know that we haven't
 			// started already
 			AppUtil.mChatService.start();
 		}
 
 		AppUtil.mApplicationContext = getApplicationContext();
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 
 		if (AppUtil.DEBUG)
 			Log.e(AppUtil.TAG, "--Stop--");
 	}
 
 	@Override
 	public void onDestroy() {
		super.onDestroy();
 
 		// if (AppUtil.mBluetoothAdapter.isEnabled() && (AppUtil.mChatService !=
 		// null)) {
 		// AppUtil.mChatService.stop();
 		// }
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Intent intent = null;
 		switch (item.getItemId()) {
 		case R.id.menu_bluetooth:
 			// intent = new Intent(this, DeviceListActivity.class);
 			// startActivityForResult(intent, REQUEST_CONNECT_DEVICE_SECURE);
 			String address = "00:06:66:48:59:FB";
 
 			BluetoothDevice device = AppUtil.mBluetoothAdapter
 					.getRemoteDevice(address);
 			// Attempt to connect to the device
 			AppUtil.mChatService.connect(device);
 
 			// Initialize the send button with a listener that for click events
 			mSendButton = (Button) findViewById(R.id.buttonSend);
 			mSendButton.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					// Send a message using content of the edit text widget
 					TextView view = (TextView) findViewById(R.id.commandText);
 					String message = view.getText().toString();
 					AppUtil.sendMessage(message);
 				}
 			});
 
 			return true;
 		case R.id.menu_bluetooth_disconnect:
 			AppUtil.mChatService.stop();
 			return true;
 			/*
 			 * case R.id.menu_settings: intent = new Intent(this,
 			 * SettingsActivity.class); intent.putExtra(EXTRA_MODE_NAME,
 			 * "Main"); startActivity(intent);
 			 */
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void setupBluetooth() {
 		// Initialize the compose field with a listener for the return key
 		AppUtil.mCommandText = (EditText) findViewById(R.id.commandText);
 		AppUtil.mCommandText.setOnEditorActionListener(mCommandListener);
 
 		// Initialize the send button with a listener that for click events
 		mSendButton = (Button) findViewById(R.id.buttonSend);
 		mSendButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				// Send a message using content of the edit text widget
 				TextView view = (TextView) findViewById(R.id.commandText);
 				String message = view.getText().toString();
 				AppUtil.sendMessage(message);
 			}
 		});
 
 		AppUtil.mBluetoothMessageTextView = (TextView) findViewById(R.id.bluetoothMessageView);
 		AppUtil.mBluetoothMessageLabel = getString(R.string.bluetooth_response);
 
 		// Initialize the BluetoothChatService to perform bluetooth connections
 		AppUtil.mChatService = new BluetoothChatService(this,
 				AppUtil.mBluetoothHandler);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_light_walker_remote, menu);
 		return true;
 	}
 
 	/*
 	 * public void onConfigButtonClick(View v) { // get selected radio button
 	 * from radioGroup AppUtil.mSelectedModeId =
 	 * mRadioModeGroup.getCheckedRadioButtonId();
 	 * 
 	 * Intent intent; switch (AppUtil.mSelectedModeId) { case
 	 * R.id.radioEqualizer: //Toast.makeText(LightWalkerRemote.this, "eq",
 	 * Toast.LENGTH_SHORT).show(); AppUtil.mSelectedMode = "equalizer"; intent =
 	 * new Intent(this, ModeEqualizerConfigActivity.class);
 	 * startActivity(intent); break; case R.id.radioGravity:
 	 * AppUtil.mSelectedMode = "gravity";
 	 * //Toast.makeText(LightWalkerRemote.this, "gravity",
 	 * Toast.LENGTH_SHORT).show(); intent = new Intent(this,
 	 * ModeGravityConfigActivity.class); startActivity(intent); break; case
 	 * R.id.radioSparkle: AppUtil.mSelectedMode = "sparkle"; intent = new
 	 * Intent(this, SettingsActivity.class);
 	 * intent.putExtra(EXTRA_PREFERENCES_NAME, "sparkle_preferences");
 	 * startActivity(intent); break; case R.id.radioPulse: AppUtil.mSelectedMode
 	 * = "pulse"; intent = new Intent(this, SettingsActivity.class);
 	 * intent.putExtra(EXTRA_PREFERENCES_NAME, "pulse_preferences");
 	 * startActivity(intent); break; } }
 	 */
 
 	private TextView.OnEditorActionListener mCommandListener = new TextView.OnEditorActionListener() {
 		public boolean onEditorAction(TextView view, int actionId,
 				KeyEvent event) {
 			// If the action is a key-up event on the return key, send the
 			// message
 			if (actionId == EditorInfo.IME_NULL
 					&& event.getAction() == KeyEvent.ACTION_UP) {
 				String message = view.getText().toString();
 				AppUtil.sendMessage(message);
 				view.setText("");
 			}
 			if (AppUtil.DEBUG)
 				Log.i(AppUtil.TAG, "END onEditorAction");
 			return true;
 		}
 	};
 
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (AppUtil.DEBUG)
 			Log.d(AppUtil.TAG, "onActivityResult " + resultCode);
 		switch (requestCode) {
 		case REQUEST_CONNECT_DEVICE_SECURE:
 			// When DeviceListActivity returns with a device to connect
 			if (resultCode == Activity.RESULT_OK) {
 				// Get the device MAC address
 				String address = data.getExtras().getString(
 						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
 				// Get the BLuetoothDevice object
 				BluetoothDevice device = AppUtil.mBluetoothAdapter
 						.getRemoteDevice(address);
 				// Attempt to connect to the device
 				AppUtil.mChatService.connect(device);
 
 				// Initialize the send button with a listener that for click
 				// events
 				mSendButton = (Button) findViewById(R.id.buttonSend);
 				mSendButton.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						// Send a message using content of the edit text widget
 						TextView view = (TextView) findViewById(R.id.commandText);
 						String message = view.getText().toString();
 						AppUtil.sendMessage(message);
 					}
 				});
 			}
 			break;
 		case REQUEST_ENABLE_BT:
 			// When the request to enable Bluetooth returns
 			if (resultCode == Activity.RESULT_OK) {
 				// Bluetooth is now enabled, so set up a chat session
 				setupBluetooth();
 			} else {
 				// User did not enable Bluetooth or an error occured
 				Log.d(AppUtil.TAG, "BT not enabled");
 				Toast.makeText(this, R.string.bt_not_enabled_leaving,
 						Toast.LENGTH_SHORT).show();
 				finish();
 			}
 		}
 	}
 }
