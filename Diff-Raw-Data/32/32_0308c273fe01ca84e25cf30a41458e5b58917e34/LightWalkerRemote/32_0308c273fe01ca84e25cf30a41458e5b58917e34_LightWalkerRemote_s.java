 package com.inappropirates.remotecontrol;
 
import java.util.Locale;

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
 
 		AppUtil.context = getApplicationContext();
 		AppUtil.title = (TextView) findViewById(R.id.Status);
 
 		AppUtil.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
 		if (AppUtil.bluetoothAdapter == null) {
 			Toast.makeText(this, "Bluetooth is not available",
 					Toast.LENGTH_LONG).show();
 			finish();
 			return;
 		}
 
 		mPrefs = getSharedPreferences("main_preferences", MODE_PRIVATE);
 		AppUtil.selectedMode = LightWalkerModes.values()[Integer.parseInt(mPrefs.getString("mainDefaultMode", String.valueOf(LightWalkerModes.sparkle.ordinal())))];
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		if (AppUtil.DEBUG)
 			Log.e(AppUtil.TAG, "--START--");
 
 		if (!AppUtil.bluetoothAdapter.isEnabled()) {
 			Intent enableIntent = new Intent(
 					BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
 			// Otherwise, setup the chat session
 		} else {
 			if ((AppUtil.chatService != null)
 					&& (AppUtil.chatService.getState() != BluetoothChatService.STATE_CONNECTED)) {
 				AppUtil.chatService.stop();
 				AppUtil.chatService = null;
 				setupBluetooth();
 			} else if (AppUtil.chatService == null)
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
 		if ((AppUtil.chatService != null)
 				&& (AppUtil.chatService.getState() == BluetoothChatService.STATE_NONE)) {
 			// Only if the state is STATE_NONE, do we know that we haven't
 			// started already
 			AppUtil.chatService.start();
 		} else if (AppUtil.chatService != null)
 		{
             switch (AppUtil.chatService.getState()) {
             case BluetoothChatService.STATE_CONNECTED:
                 AppUtil.title.setText(R.string.title_connected_to);
                 AppUtil.title.append(AppUtil.connectedDeviceName);
                 break;
             case BluetoothChatService.STATE_CONNECTING:
             	AppUtil.title.setText(R.string.title_connecting);
                 break;
             case BluetoothChatService.STATE_LISTEN:
             case BluetoothChatService.STATE_NONE:
             	AppUtil.title.setText(R.string.title_not_connected);
                 break;
             }
 		}
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
 			// <cgerstle> To auto select, comment out the activity starting and uncomment out the applicable address line
 			Intent intent = null;
 			intent = new Intent(this, DeviceListActivity.class);
 			startActivityForResult(intent, REQUEST_CONNECT_DEVICE_SECURE);
 			//String address = "00:06:66:48:59:FB"; // Original sparkfun bluetooth
 			//String address = "00:18:96:B0:01:8F"; // New bluefruit
 
 			//BluetoothDevice device = AppUtil.bluetoothAdapter.getRemoteDevice(address);
 			// Attempt to connect to the device
 			//AppUtil.chatService.connect(device);
 
 			return true;
 		case R.id.menu_bluetooth_disconnect:
 			AppUtil.chatService.stop();
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
 		AppUtil.logger = new ListLogger(AppUtil.context, (ListView) findViewById(R.id.logList));
 
 		// Initialize the BluetoothChatService to perform bluetooth connections
 		AppUtil.chatService = new BluetoothChatService(this, AppUtil.mBluetoothHandler);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_light_walker_remote, menu);
 		return true;
 	}
 
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
 				BluetoothDevice device = AppUtil.bluetoothAdapter
 						.getRemoteDevice(address);
 				// Attempt to connect to the device
 				AppUtil.chatService.connect(device);
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
