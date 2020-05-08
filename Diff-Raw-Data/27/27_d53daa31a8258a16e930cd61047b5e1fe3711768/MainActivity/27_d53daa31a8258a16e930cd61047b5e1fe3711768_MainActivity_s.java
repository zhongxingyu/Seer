 package com.vitaltech.bioink;
 
 import com.vitaltech.bioink.RangeSeekBar.OnRangeSeekBarChangeListener;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Movie;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.MediaController;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.VideoView;
 
 @SuppressWarnings("unused")
 public class MainActivity extends Activity {
 	private static final String TAG = MainActivity.class.getSimpleName();
 	public static final Boolean DEBUG=true;
 
 	private Button vizButton;
 	private Button menuButton;
 	private Discovery discovery;
 	private BroadcastReceiver broadcastReceiver;
 	private IntentFilter intentFilter;
 	private BluetoothAdapter btAdapter;
 	private LinearLayout linearControl;
 	private LinearLayout linearAdvanced;
 	private LinearLayout linearStub;
 	private Button acceptButton;
 	private String startText = "Start Visualization";
 	private String enableBlue = "Enable Bluetooth";
 	
 	// Settings
 	private float minHR = DataProcess.MIN_HR;
 	private float maxHR = DataProcess.MAX_HR;
 	private float minResp = DataProcess.MIN_RESP;
 	private float maxResp = DataProcess.MAX_RESP;
 	private BiometricType colorType = BiometricType.RESPIRATION;
 	private BiometricType energyType = BiometricType.HEARTRATE;
 	private LinearLayout settingsLayout;
 	private LinearLayout.LayoutParams settingsParams;
 
 	// **** Start Lifecycle ****
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		if(DEBUG) Log.d(TAG, "__onCreate()__");
 
 		btAdapter=BluetoothAdapter.getDefaultAdapter();
 		if(btAdapter == null){
 			Toast.makeText(getApplicationContext(),"Bluetooth not available on this device",Toast.LENGTH_LONG).show();
 			Log.e(TAG, "Bluetooth not available on this device");
 			finish();
 		}
 
 		discovery = new Discovery(this, btAdapter);
 
 		// Catch Bluetooth radio events
 		intentFilter = new IntentFilter();
 		intentFilter.addAction(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED);
 		intentFilter.addAction(android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED);
 		intentFilter.addAction(android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
 		broadcastReceiver = new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context contxt, Intent intent) {
 				String action = intent.getAction();
 				if(DEBUG) Log.v(TAG, "intent received: " + intent.toString() + ", " + action);
 				if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
 					if(DEBUG) Log.v(TAG, "discovery finished");
 					discovery.stopListener();
 					discovery.showDevices();
 				}else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
 					if(DEBUG) Log.v(TAG, "discovery started");
					discovery.showProgress(true);
 				}else{
 					int code = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -99);
 					switch(code){
 					case BluetoothAdapter.STATE_ON:
 						if(DEBUG) Log.v(TAG, "bluetooth broadcast receiver => on");
 						vizButton.setText(startText);
 						doDiscovery();
 						break;
 					case BluetoothAdapter.STATE_OFF:
 						if(DEBUG) Log.v(TAG, "bluetooth broadcast receiver => off");
 						vizButton.setText(enableBlue);
 						discovery.stopListener();
 						discovery.showDevices();
 						break;
 					case BluetoothAdapter.STATE_TURNING_OFF:
 					case BluetoothAdapter.STATE_TURNING_ON:
 						if(DEBUG) Log.v(TAG, "bluetooth broadcast receiver => changing");
 						break;
 					default:
 						Log.w(TAG, "bluetooth broadcast receiver => undefined: " + action + ", "+ code);
 						break;
 					}
 				}
 			}
 		};
 		registerReceiver(broadcastReceiver, intentFilter);
 		connectButton();
 	}
 
 	@Override
 	protected void onResume() { // Activity was partially visible
 		if(DEBUG) Log.d(TAG, "__onResume()__");
 		registerReceiver(broadcastReceiver, intentFilter);
 		super.onResume();
 		if(DEBUG) Log.d(TAG, "return to menu");
 		setContentView(R.layout.activity_main);
 		linearStub = null;
 		
 		connectButton();
 	}
 
 	// **** Activity is running at this point ****
 
 	@Override
 	public void onPause(){ // Activity was visible but now is now partially visible
 		if(DEBUG) Log.d(TAG, "__onPause()__");
 		unregisterReceiver(broadcastReceiver);
 		btAdapter.cancelDiscovery();
 		discovery.stopListener();
 		super.onPause();
 	}
 	// **** End Lifecycle ****    
 
 
 	private void connectButton(){		
 		// Configure single Button system
 		vizButton = (Button) findViewById(R.id.vizButton);
 		if(btAdapter.isEnabled()){
 			Log.v(TAG, "radio is on");
 			vizButton.setText(startText);
 			doDiscovery();
 		}else{
 			Log.v(TAG, "radio is off");
 			vizButton.setText(enableBlue);
 			discovery.showDevices();
 		}
 		
 		vizButton.setOnClickListener(
 			new OnClickListener() {
 				public void onClick(View v) {
 					if(DEBUG) Log.d(TAG, "Viz Button pressed");
 					if(vizButton.getText() == enableBlue){
 						if (DEBUG) Log.d(TAG,"enabling bluetooth");
 						startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
 					}else if(vizButton.getText() == startText){
 						if (DEBUG) Log.d(TAG,"starting viz");
 						Intent myIntent = new Intent(v.getContext(), RajActivity.class);
 						
 						// transfer settings to RajActivity
 						myIntent.putExtra("minHR", minHR);
 						myIntent.putExtra("maxHR", maxHR);
 						myIntent.putExtra("minResp", minResp);
 						myIntent.putExtra("maxResp", maxResp);
 						myIntent.putExtra("colorType", colorType);
 						myIntent.putExtra("energyType", energyType);
 						
 						startActivityForResult(myIntent, 0);
 					}else{
 						Log.w(TAG, "button labeled as " + vizButton.getText());
 					}
 				}
 			}
 		);
 	// swap static logo for animated gif
 //		ImageView img = (ImageView) findViewById(R.id.logoImageView);
 //		img.setVisibility(ImageView.GONE);
 
 //		VideoView gifView = (VideoView) findViewById( R.id.logoVideoView);
 //		gifView.setMediaController( new MediaController( getApplicationContext()));
 //		gifView.setVideoURI( Uri.parse( "android.resource://" + getPackageName() + "/" + R.raw.logo));
 //		gifView.requestFocus();
 //		gifView.start();
 //		gifView.setVisibility( VideoView.VISIBLE);
 
 //		WebView gifView = (WebView) findViewById(R.id.logoWebView);
 //		gifView.loadUrl("file:///android_res/raw/logo.gif");
 //		gifView.setInitialScale(100);
 //		gifView.setVisibility(WebView.VISIBLE);
 
 //		Movie gifMovie = (Movie) findViewById(R.id.logoMovieView);
 		
 		
 		
 		
 //		LinearLayout linearLogo = (LinearLayout) findViewById(R.id.logoSide);
 //		linearLogo.addView(gifView);
 
 		linearControl = (LinearLayout) findViewById(R.id.linearControl);
 		linearAdvanced = (LinearLayout) findViewById(R.id.linearAdvanced);
 		acceptButton = (Button) findViewById(R.id.accept_button);
 		
 		menuButton = (Button) findViewById(R.id.menuButton);
 		menuButton.setOnClickListener(
 			new OnClickListener() {
 				public void onClick(View v) {
 					if(DEBUG) Log.d(TAG, "add right side advanced controls");
 					showAdvanced();
 				}
 			}
 		);
 	}
 	
 	private void showAdvanced(){
 		linearControl.setVisibility(LinearLayout.GONE);
 		linearAdvanced.setVisibility(LinearLayout.VISIBLE);
 
 		if(linearStub == null){
 			Log.w(TAG, "linearStub == null");
 			linearStub = (LinearLayout) findViewById(R.id.linearStub);
 			setupSettingsMenu(linearStub);
 		}
 
 		acceptButton.setOnClickListener(
 			new OnClickListener() {
 				public void onClick(View v) {
 					if(DEBUG) Log.d(TAG, "advanced settings have been chosen");
 
 					linearControl.setVisibility(LinearLayout.VISIBLE);
 					linearAdvanced.setVisibility(LinearLayout.GONE);
 				}
 			}
 		);
 	}
 
 	private void setupSettingsMenu(LinearLayout ll_stub){
 		// Grabbing the Application context
 		final Context context = getApplication();
 
 		// Setting the orientation to vertical
 		settingsLayout = ll_stub;
 		settingsLayout.setOrientation(LinearLayout.VERTICAL);
 		 
 		// Defining the LinearLayout layout parameters to fill the parent.
 		settingsParams = new LinearLayout.LayoutParams(
 		//	            LinearLayout.LayoutParams.FILL_PARENT,
 		    LinearLayout.LayoutParams.WRAP_CONTENT,
 		    LinearLayout.LayoutParams.WRAP_CONTENT);
 		settingsParams.weight = 1;
 		
 		LinearLayout HRLayout = new LinearLayout(this);
 		HRLayout.setOrientation(LinearLayout.HORIZONTAL);
 		LinearLayout RespLayout = new LinearLayout(this);
 		RespLayout.setOrientation(LinearLayout.HORIZONTAL);
 		LinearLayout ColorLayout = new LinearLayout(this);
 		ColorLayout.setOrientation(LinearLayout.HORIZONTAL);
 		LinearLayout EnergyLayout = new LinearLayout(this);
 		EnergyLayout.setOrientation(LinearLayout.HORIZONTAL);
 		
 		TextView HRText = new TextView(this);
 		final TextView minHRText = new TextView(this);
 		final TextView maxHRText = new TextView(this);
 		TextView RespText = new TextView(this);
 		final TextView minRespText = new TextView(this);
 		final TextView maxRespText = new TextView(this);
 		TextView colorText = new TextView(this);
 		TextView energyText = new TextView(this);
 			        
 		HRText.setWidth(150);
 		RespText.setWidth(150);
 		colorText.setWidth(150);
 		energyText.setWidth(150);
 		
 		minHRText.setWidth(50);
 		maxHRText.setWidth(50);
 		minRespText.setWidth(50);
 		maxRespText.setWidth(50);
 		
 		minHRText.setText(String.format("%d", (int)minHR));
 		maxHRText.setText(String.format("%d", (int)maxHR));
 		minRespText.setText(String.format("%d", (int)minResp));
 		maxRespText.setText(String.format("%d", (int)maxResp));
 		
 		HRText.setText("Heartrate:");
 		RespText.setText("Respiration:");
 		colorText.setText("Color:");
 		energyText.setText("Energy:");
 		
 		// create RangeSeekBar as Float for Heartrate
 		RangeSeekBar<Float> seekBarHR = new RangeSeekBar<Float>(DataProcess.MIN_HR, DataProcess.MAX_HR, context);
 		seekBarHR.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Float>() {
 		    public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Float minValue, Float maxValue) {
 				if(minValue.equals(maxValue)){
 					Log.w(TAG, "fixing: min == max");
 					minValue -= 0.00001f;
 					maxValue += 0.00001f;
 				}
 				
 		        minHR = minValue;
 		        maxHR = maxValue;
 		        
 		        minHRText.setText(String.format("%d", (int)minHR));
 		        maxHRText.setText(String.format("%d", (int)maxHR));
 		        if(DEBUG) Log.d(TAG + "menu", "minHR: "+minHR+", maxHR: "+maxHR);
 		    }
 		});
 		
 		// create RangeSeekBar as Float for Respiration
 		RangeSeekBar<Float> seekBarResp = new RangeSeekBar<Float>(DataProcess.MIN_RESP, DataProcess.MAX_RESP, context);
 		seekBarResp.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Float>() {
 		    public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Float minValue, Float maxValue) {
 		    	if(minValue.equals(maxValue)){
 					minValue -= 0.00001f;
 					maxValue += 0.00001f;
 				}
 		    	
 		        minResp = minValue;
 		        maxResp = maxValue;
 		        
 		        minRespText.setText(String.format("%d", (int)minResp));
 		        maxRespText.setText(String.format("%d", (int)maxResp));
 		        if(DEBUG) Log.d(TAG + "menu", "minResp: "+minResp+", maxResp: "+maxResp);
 		    }
 		});
 		
 		// set up the sliders to be at the current correct position
 		seekBarHR.setSelectedMinValue(minHR);
 		seekBarHR.setSelectedMaxValue(maxHR);
 		seekBarResp.setSelectedMinValue(minResp);
 		seekBarResp.setSelectedMaxValue(maxResp);
 		
 		final String[] biometricTypes = { "Heartrate", "Respiration" };
 		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, biometricTypes);
 		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);	        
 		
 		Spinner colorSpinner = new Spinner(this);
 		colorSpinner.setAdapter(aa);
 		colorSpinner.setSelection(1); // start this one with Respiration selected instead of heartrate
 		colorSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
 			public void onItemSelected(AdapterView<?> parent, View v, int position,
 					long id) {
 				if(biometricTypes[position].equals("Heartrate")){
 					colorType = BiometricType.HEARTRATE;
 				}
 				if(biometricTypes[position].equals("Respiration")){
 					colorType = BiometricType.RESPIRATION;
 				}
 				if(DEBUG) Log.d(TAG + "menu", "colorType: "+colorType);
 			}
 		
 			public void onNothingSelected(AdapterView<?> parent) {}
 		});
 		
 		Spinner energySpinner = new Spinner(this);
 		energySpinner.setAdapter(aa);
 		energySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
 			public void onItemSelected(AdapterView<?> parent, View v, int position,
 					long id) {
 				if(biometricTypes[position].equals("Heartrate")){
 					energyType = BiometricType.HEARTRATE;
 				}
 				if(biometricTypes[position].equals("Respiration")){
 					energyType = BiometricType.RESPIRATION;
 				}
 				if(DEBUG) Log.d(TAG + "menu", "energyType: " + energyType);
 			}
 		
 			public void onNothingSelected(AdapterView<?> parent) {}
 		});
 		
 		HRLayout.addView(HRText);
 		HRLayout.addView(minHRText);
 		HRLayout.addView(seekBarHR,settingsParams);
 		HRLayout.addView(maxHRText);
 		
 		RespLayout.addView(RespText);
 		RespLayout.addView(minRespText);
 		RespLayout.addView(seekBarResp,settingsParams);
 		RespLayout.addView(maxRespText);
 		
 		ColorLayout.addView(colorText);
 		ColorLayout.addView(colorSpinner,settingsParams);
 		
 		EnergyLayout.addView(energyText);
 		EnergyLayout.addView(energySpinner,settingsParams);
 		
 		settingsLayout.addView(HRLayout);
 		settingsLayout.addView(RespLayout);
 		settingsLayout.addView(ColorLayout);
 		settingsLayout.addView(EnergyLayout);
 	}
 
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if(DEBUG) Log.d(TAG, "keycode: " + keyCode + ", keyevent: " + event.toString());
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if(DEBUG) Log.d(TAG, "ending app");
 			finish();
 		}else if (keyCode == KeyEvent.KEYCODE_MENU){
 			if(DEBUG) Log.d(TAG, "onCreateOptionsMenu()");
 			doDiscovery();
 			// TODO toggle advanced settings
 			return true;
 		}
 		return false;
 	}
 
 	private void doDiscovery(){
 		if(DEBUG) Log.v(TAG, "doDiscovery(): start");
 		new Thread(
 			new Runnable() {
 				public void run(){
 					if(DEBUG) Log.v(TAG,"doDiscovery(): start finding devices");
 					discovery.findDevices();
 					if(DEBUG) Log.d(TAG,"doDiscovery(): finding devices finished");
 					runOnUiThread(
 						new Runnable() {
 							public void run() {
 								if(DEBUG) Log.v(TAG,"doDiscovery(): show devices start");
 								discovery.showDevices();
 								if(DEBUG) Log.d(TAG,"doDiscovery(): finish showing devices");
 							}
 						}
 					);
 					if(DEBUG) Log.v(TAG, "doDiscovery() finished");
 				}
 			}
 		).start();
 	}
 }
 
