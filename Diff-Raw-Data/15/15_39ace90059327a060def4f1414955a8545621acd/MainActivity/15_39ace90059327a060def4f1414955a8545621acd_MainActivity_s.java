 package com.example.datamanager;
 
 import java.io.IOException;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.opengl.Visibility;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnClickListener, OnCheckedChangeListener {
 
 	// ui components
 	CheckBox cbData = null;
 	CheckBox cbDataMgr = null;
 	CheckBox cbWifi = null;
 	CheckBox cbWifiMgr = null;
 	EditText edTimeOn = null;
 	EditText edTimeOff = null;
 	EditText edInterval = null;
 	Button buttonSave = null;
 
 	// SharedPreferences
 	SharedPreferences prefs = null;
 	SharedPrefsEditor sharedPrefsEditor = null;
 	
 	private DataActivation dataActivation;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// shared prefs init
 		prefs = getSharedPreferences(SharedPrefsEditor.PREFERENCE_NAME,
 				Activity.MODE_PRIVATE);
 
 		dataActivation = new DataActivation(getBaseContext());
 		sharedPrefsEditor = new SharedPrefsEditor(prefs, dataActivation);
 		
 		/*try {
 			sharedPrefsEditor.resetPreferences();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}*/
 
 		try {
 			sharedPrefsEditor.initializePreferences();
 
 			setContentView(R.layout.activity_main);
 
 			// init ui
 			loadUiComponents();
 			initializeUiComponentsData();
 
 			// instanciate save button
 			buttonSave = (Button) findViewById(R.id.buttonSave);
 			buttonSave.setOnClickListener(this);
 			
 			cbData.setOnCheckedChangeListener(this);
 			cbWifi.setOnCheckedChangeListener(this);
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	/**
 	 * 
 	 * Load of components of UI
 	 */
 	private void loadUiComponents() {
 		cbData = (CheckBox) findViewById(R.id.checkBoxData);
 		cbDataMgr = (CheckBox) findViewById(R.id.checkBoxDataMgr);
 		
 		cbWifi = (CheckBox) findViewById(R.id.checkBoxWifi);
 		cbWifiMgr = (CheckBox) findViewById(R.id.checkBoxWifiMgr);
 
 		edTimeOn = (EditText) findViewById(R.id.editTextTimeOn);
 		edTimeOff = (EditText) findViewById(R.id.editTextTimeOff);
 		edInterval = (EditText) findViewById(R.id.editTextInterval);
 
 	}
 
 	/**
 	 * Init ui components from value extracted from shared preferences
 	 */
 	private void initializeUiComponentsData() {
 		int timeOn = sharedPrefsEditor.getTimeOn();
 		edTimeOn.setText(String.valueOf(timeOn));
 
 		int timeOff = sharedPrefsEditor.getTimeOff();
 		edTimeOff.setText(String.valueOf(timeOff));
 
 		int checkTime = sharedPrefsEditor.getIntervalCheck();
 		edInterval.setText(String.valueOf(checkTime));
 
 		boolean dataIsActivated = sharedPrefsEditor.isDataActivated();
 		cbData.setChecked(dataIsActivated);
 
 		boolean dataMgrIsActivated = sharedPrefsEditor.isDataMgrActivated();
 		cbDataMgr.setChecked(dataMgrIsActivated);
 		
 		boolean wifiIsActivated = sharedPrefsEditor.isWifiActivated();
 		cbWifi.setChecked(wifiIsActivated);
 
 		boolean wifiMgrIsActivated = sharedPrefsEditor.isWifiManagerActivated();
 		cbWifiMgr.setChecked(wifiMgrIsActivated);
 
 
 	}
 
 	@TargetApi(11)
 	public void onClick(View v) {
 
 		// if saved button is click
 		if (v == buttonSave) {
 			// load ui compenents
 			loadUiComponents();
 
 			// get settings values
 			int timeOn = Integer.parseInt(edTimeOn.getText().toString());
 			int timeOff = Integer.parseInt(edTimeOff.getText().toString());
 			int intervalCheck = Integer.parseInt(edInterval.getText()
 					.toString());
 
 			boolean dataIsActivated = cbData.isChecked();
 			boolean dataMgrIsActivated = cbDataMgr.isChecked();
 			
 			boolean wifiIsActivated = cbWifi.isChecked();
 			boolean wifiMgrIsActivated = cbWifiMgr.isChecked();
 
 			// save all these preferences
 			sharedPrefsEditor.setAllValues(timeOn, timeOff, intervalCheck,
 					dataIsActivated, dataMgrIsActivated,wifiIsActivated, wifiMgrIsActivated);
 
 			try {
 				// if data is disabled; data connection is stopped
 				if (!dataIsActivated) {
 
 					//keep auto sync (in case of wifi connection)
 					dataActivation.setMobileDataEnabled(false);
 					//dataActivation.setAutoSync(true);
 				} else {
 					// activate data
 					dataActivation.setMobileDataEnabled(true);
 					//dataActivation.setAutoSync(true);
 				}
 				
 				
 				//if wifi is disabled, wifi connection is stopped
 				if(!wifiIsActivated)
 				{
 					dataActivation.setWifiConnectionEnabled(false);
 					//dataActivation.setAutoSync(true);
 				}
 				else
 				{
 					dataActivation.setWifiConnectionEnabled(true);
 					//dataActivation.setAutoSync(true);
 				}
 				
 				// if data manager and wifi manager are disabled, service is stopped
				if (!dataMgrIsActivated && !wifiMgrIsActivated) {
 					stopDataManagerService();
 				}
 				
 				
 				//if data and data manager enable or if wifi and wifi manager enable, service is started
 				if ( (dataIsActivated && dataMgrIsActivated) ||  (wifiIsActivated && wifiMgrIsActivated) ){
 					//Toast.makeText(this, "service is started : "+sharedPrefsEditor.isDataMgrActivated(), Toast.LENGTH_SHORT).show();
 					StartDataManagerService();
 				}
 				
 				
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}
 
 	}
 
 	/**
 	 * Stop data manager service
 	 */
 	public void stopDataManagerService() {
 		// if service is started
 		//if (sharedPrefsEditor.isServiceActivated()) {
 			stopService(new Intent(this, MainService.class));
 		//}
 	}
 
 
 	public void StartDataManagerService() {
 		// if service is not started
 		//if (!sharedPrefsEditor.isServiceActivated()) {
 			Intent serviceIntent = new Intent(this, MainService.class);
 			startService(serviceIntent);
 		//}
 	}
 
 	/**
 	 * whenever a checkbox is checked/unchecked
 	 */
 
 	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 		
 
 		if(buttonView == cbData)
 		{
 			if(isChecked)
 			{
 				//enable dataManager checkBox
 				cbDataMgr.setVisibility(View.VISIBLE);
 			
 			}
 			else
 			{
 				//disable dataManager checkBoc
 				cbDataMgr.setVisibility(View.INVISIBLE);
 			}
 			
 		}
 		else if(buttonView == cbWifi)
 		{
 			if(isChecked)
 			{
 				//enable dataManager checkBox
 				cbWifiMgr.setVisibility(View.VISIBLE);
 			
 			}
 			else
 			{
 				//disable dataManager checkBoc
 				cbWifiMgr.setVisibility(View.INVISIBLE);
 			}
 		}
 		
 	}
 	
 
 }
