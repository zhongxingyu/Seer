 package com.example.bluetoothfound;
 
import java.util.List;

 import android.media.AudioManager;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 
 /**
  * bluetooth found activity
  * 
  * 修改日期:2013年9月5日
  * 
  * @author gus
  * @version 1.0.0
  */
 public class BluetoothFoundActivity extends Activity {
 	public static final String ACTION_UI_UPDATE = "uiUpdate";
 	//extras value
 	public static final String UPDATE_STRING = "updateString";
 	// enable bluetooth request code
 	private static final int REQUEST_ENABLE_BT = 2;
 	
 	public static final int RINGTONE_ENABLE = 1;
 	public static final int VIBRATE_ENABLE = 2;
 	public static final int VIBRATE_RINGTONE_ENABLE = 3;
 	//pick ringtone request code
 	private static final int REQUEST_CODE_PICK_RINGTONE = 1;
 	public static final String SETTING = "setting";
 	public static final String SETTING_DURATION = "duration";
 	public static final String SETTING_RINGTONE_URI = "ringtoneUri";
 	private String mRingtoneUri = null;
 	private int[] duration = { 10, 20, 30, 60, 120, 300};
 	
	private BluetoothHeadset mBluetoothHeadset;
 	private Button connectBtn;
 	private TextView foundLogTextView;
 	private SharedPreferences prefs;
 	private Editor mEditor;
 	private Settings settings;
 	private BluetoothAdapter mBluetoothAdapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		initSettings();
 		setContentView(R.layout.activity_bluetooth_found);
 		
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		// If the adapter is null, then Bluetooth is not supported
 		if (mBluetoothAdapter == null) {
 			Toast.makeText(this, "Bluetooth is not available",
 					Toast.LENGTH_LONG).show();
 			finish();
 			return;
		} ;
 		
 		connectBtn = (Button) findViewById(R.id.connectBtn);
 		foundLogTextView = (TextView) findViewById(R.id.foundLog);
 		Intent intent = getIntent();
 		if (intent != null) {
 			boolean isDiscovery = intent.getBooleanExtra("isDiscovery", false);
 			int serviceStatus = prefs.getInt("serviceStatus", 0);
			if (serviceStatus == 1) {
 				String deviceDisConnectText = getResources().getString(
 						R.string.DeviceDisConnect);
 				connectBtn.setText(deviceDisConnectText);
				if (isDiscovery) {
					String message = intent.getStringExtra("message");
					foundLogTextView.setText(message);
				} else {
					
				}
 			} else {
 				foundLogTextView.setText("");
 			}
 		}
 		/**
 		 * connect to device listener
 		 */
 		connectBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				String text = connectBtn.getText().toString();
 				String deviceConnectText = getResources().getString(
 						R.string.DeviceConnect);
 				String deviceDisConnectText = getResources().getString(
 						R.string.DeviceDisConnect);
 				
 				if (text.equalsIgnoreCase(deviceConnectText)) {
 					mEditor.putInt("serviceStatus", 1);
 					mEditor.commit();
 					connectBtn.setText(deviceDisConnectText);
 					foundLogTextView.setText("TGK设备搜索中.");
 					startService();
 				} else {
 					mEditor.putInt("serviceStatus", 0);
 					mEditor.commit();
 					connectBtn.setText(deviceConnectText);
 					foundLogTextView.setText("TGK设备搜索服务已停止!");
 					Intent intent = new Intent();
 					intent.setAction(BluetoothService.ACTION_STOP_PLAY_RINGTONE);
 					sendBroadcast(intent);
 				}
 			}
 		});
 		
 		// register ui update broadcast receiver
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(ACTION_UI_UPDATE);
 		registerReceiver(uiUpdateReceiver, filter);
 				
 		Spinner sp = (Spinner)findViewById(R.id.duration);
 		int durationValue = prefs.getInt(SETTING_DURATION, 0);
 		//init cache duration value
 		for (int index = 0; index < duration.length; index++) {
 			if (durationValue == duration[index]) {
 				sp.setSelection(index);
 			}
 		}
 		sp.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int position, long arg3) {
 					mEditor.putInt("duration", duration[position]);
 					mEditor.commit();
 			}
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 			}
 				
 		});
 		
 		Button pickRingtoneBtn = (Button)findViewById(R.id.pickringtone);
 		pickRingtoneBtn.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				doPickRingtone();
 			}
 		});
 		
 		//init cache ringer mode
 		int ringerMode = prefs.getInt("ringerMode", RINGTONE_ENABLE);
 		if (ringerMode == RINGTONE_ENABLE) {
 			((RadioButton)findViewById(R.id.ringer_mode)).setChecked(true);
 		} else if (ringerMode == VIBRATE_ENABLE) {
 			((RadioButton)findViewById(R.id.vibrate_mode)).setChecked(true);
 		} else if (ringerMode == VIBRATE_RINGTONE_ENABLE) {
 			((RadioButton)findViewById(R.id.ringer_vibrate_mode)).setChecked(true);
 		}
 		RadioGroup rg = (RadioGroup)findViewById(R.id.alram_mode);
 		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				int ringerMode = AudioManager.RINGER_MODE_NORMAL;
 				//set vibrate mode
 				if(checkedId == R.id.vibrate_mode) {
 					ringerMode = VIBRATE_ENABLE;
 				} else if(checkedId == R.id.ringer_mode) {
 					ringerMode = RINGTONE_ENABLE;
 				} else if(checkedId == R.id.ringer_vibrate_mode) {
 					ringerMode = VIBRATE_RINGTONE_ENABLE;
 				}
 				mEditor.putInt("ringerMode", ringerMode);
 				mEditor.commit();
 			}
 		});
 
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		// If BT is not on, request that it be enabled.
 		// setupChat() will then be called during onActivityResult
 		if (!mBluetoothAdapter.isEnabled()) {
 			Intent enableIntent = new Intent(
 					BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
 			// Otherwise, setup the chat session
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		unregisterReceiver(uiUpdateReceiver);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.bluetooth_found, menu);
 		return true;
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case REQUEST_ENABLE_BT:
 			if (resultCode == Activity.RESULT_OK) {
 				// Bluetooth is now enabled, so set up a chat session
 				Toast.makeText(this, "蓝牙启动成功!", Toast.LENGTH_LONG).show();
 			} else {
 				Toast.makeText(this, "蓝牙无法启动!", Toast.LENGTH_LONG).show();
 				finish();
 			}
 			break;
 		case REQUEST_CODE_PICK_RINGTONE: 
 			if (resultCode == Activity.RESULT_OK) {
 				Uri pickedUri = data
 						.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
 				handleRingtonePicked(pickedUri);
 			} else {
 				//Toast.makeText(this, "铃声设置失败", Toast.LENGTH_LONG).show();
 			}
 			break;
 		
 		}
 	}
 
 	private void initSettings() {
 		prefs = getSharedPreferences(SETTING, Context.MODE_PRIVATE);
 		mEditor = prefs.edit();
 		settings = new Settings();
 		// default ringtone duration time 20ms
 		settings.setDuration(prefs.getInt(SETTING_DURATION, 20 * 1000));
 		String ringtoneUri = prefs.getString(SETTING_RINGTONE_URI, "");
 		if (ringtoneUri == "") {
 			Toast.makeText(this, "请设置提醒铃声", Toast.LENGTH_LONG).show();
 		}
 		settings.setRingtone(ringtoneUri);
 
 	}
 		
 	/**
 	 * start discovery bluetooth device service.
 	 */
 	private void startService() {
 		Intent service = new Intent("com.example.bluetoothfound.BluetoothService");
 		startService(service);
 	}
 	/**
 	 * stop discovery bluetooth device service.
 	 */
 	private void stopService() {
 		Intent service = new Intent("com.example.bluetoothfound.BluetoothService");
 		stopService(service);
 	}
 	
 	
 	private void doPickRingtone() {
 		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
 		// Allow user to pick 'Default'
 		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
 		// Show only ringtones
 		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
 				RingtoneManager.TYPE_RINGTONE);
 		// Don't show 'Silent'
 		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
 
 		Uri ringtoneUri;
 		if (mRingtoneUri != null) {
 			ringtoneUri = Uri.parse(mRingtoneUri);
 		} else {
 			// Otherwise pick default ringtone Uri so that something is
 			// selected.
 			ringtoneUri = RingtoneManager
 					.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
 		}
 
 		// Put checkmark next to the current ringtone for this contact
 		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
 				ringtoneUri);
 
 		// Launch!
 		// startActivityForResult(intent, REQUEST_CODE_PICK_RINGTONE);
 		startActivityForResult(intent, REQUEST_CODE_PICK_RINGTONE);
 	}
 	
 	
 	private void handleRingtonePicked(Uri pickedUri) {
 		if (pickedUri == null || RingtoneManager.isDefault(pickedUri)) {
 			mRingtoneUri = null;
 		} else {
 			mRingtoneUri = pickedUri.toString();
 		}
 		// get ringtone name and you can save mRingtoneUri for database.
 		if (mRingtoneUri != null) {
 			SharedPreferences sharedPreferences = getSharedPreferences(SETTING, Context.MODE_PRIVATE);
 			Editor editor = sharedPreferences.edit();
 			editor.putString(SETTING_RINGTONE_URI, mRingtoneUri);
 			editor.commit();
 		} else {
 			Toast.makeText(this, "铃声设置失败", Toast.LENGTH_LONG).show();
 		}
 	}
 	
	
 	/**
 	 * update ui
 	 */
 	private final BroadcastReceiver uiUpdateReceiver = new BroadcastReceiver() {
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Bundle b = intent.getExtras();
 			String updateText = b.getString(UPDATE_STRING);
 			foundLogTextView.setText(updateText);
 		}
 	};
 	
 
 }
