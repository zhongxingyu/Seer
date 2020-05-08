 // Copyright 2011-2012, Art Hare
 // This file is part of WifiLapper.
 
 //WifiLapper is free software: you can redistribute it and/or modify
 //it under the terms of the GNU General Public License as published by
 //the Free Software Foundation, either version 3 of the License, or
 //(at your option) any later version.
 
 //WifiLapper is distributed in the hope that it will be useful,
 //but WITHOUT ANY WARRANTY; without even the implied warranty of
 //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //GNU General Public License for more details.
 
 //You should have received a copy of the GNU General Public License
 //along with WifiLapper.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.artsoft.wifilapper;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class LandingOptions extends LandingRaceBase implements OnCheckedChangeListener, OnClickListener
 {
 	public final static String SPEEDO_SPEEDDISTANCE = "Speed/Distance Graph";
 	public final static String SPEEDO_LIVEPLUSMINUS = "Live +/-";
 	public final static String SPEEDO_COMPARATIVE = "Speedometer - Comparative";
 	public final static String SPEEDO_SIMPLE = "Speedometer - Simple";
 	
 	public final static String[] rgstrSpeedos = {SPEEDO_SPEEDDISTANCE,SPEEDO_LIVEPLUSMINUS, SPEEDO_COMPARATIVE,SPEEDO_SIMPLE};
 	
 	private static final int ACTIVITYRESULT_BLUETOOTH_GPS = 51;
 	private static final int ACTIVITYRESULT_BLUETOOTH_OBD2 = 52;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.landingoptions);
 	}
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		
 		UpdateUI();
 	}
 	public static boolean isSdPresent() 
 	{		 
 		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
 	}
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		
 		// save settings
 		// load UI elements
 		CheckBox chkTestMode = (CheckBox)findViewById(R.id.chkTestMode);
 		Spinner spnSpeedo = (Spinner)findViewById(R.id.spnDisplayMode);
 		Spinner spnUnits = (Spinner)findViewById(R.id.spnUnits);
 		RadioButton chkInternal = (RadioButton)findViewById(R.id.chkDBInternal);
 		
 		// get data
 		String strSpeedoStyle = spnSpeedo.getSelectedItem().toString();
 		boolean fTestMode = chkTestMode.isChecked();
 		Prefs.UNIT_SYSTEM eUnits = Prefs.UNIT_SYSTEM.valueOf(spnUnits.getSelectedItem().toString());
 		boolean fInternal = chkInternal.isChecked();
 		
 		// save data
 		SharedPreferences settings = getSharedPreferences(Prefs.SHAREDPREF_NAME, 0);
 		SharedPreferences.Editor edit = settings.edit();
 		
 		edit.putBoolean(Prefs.PREF_TESTMODE_BOOL, fTestMode)
 		  .putString(Prefs.PREF_SPEEDOSTYLE_STRING, strSpeedoStyle)
 		  .putString(Prefs.PREF_UNITS_STRING, eUnits.toString())
 		  .putBoolean(Prefs.PREF_DBLOCATION_BOOL, fInternal)
 		  .commit();
 	}
 	
     protected void SetupSpeedoSpinner(Spinner spn, String strDefault)
     {
     	int ixDefault = -1;
 		List<String> lstStrings = new ArrayList<String>();
 		for(int x = 0; x < rgstrSpeedos.length; x++)
 		{
 			if(rgstrSpeedos[x].equalsIgnoreCase(strDefault)) ixDefault = x;
 			lstStrings.add(rgstrSpeedos[x]);
 		}
 		
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simplelistitem_blacktext, lstStrings);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spn.setAdapter(adapter);
         if(ixDefault >= 0) spn.setSelection(ixDefault);
         spn.invalidate();
     }
     protected void SetupUnitSpinner(Spinner spn, Prefs.UNIT_SYSTEM eDefault)
     {
     	int ixDefault = -1;
 		List<String> lstStrings = new ArrayList<String>();
 		for(int x = 0; x < Prefs.UNIT_SYSTEM.values().length; x++)
 		{
 			if(Prefs.UNIT_SYSTEM.values()[x].equals(eDefault)) ixDefault = x;
 			lstStrings.add(Prefs.UNIT_SYSTEM.values()[x].toString());
 		}
 		
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simplelistitem_blacktext, lstStrings);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spn.setAdapter(adapter);
         if(ixDefault >= 0) spn.setSelection(ixDefault);
         spn.invalidate();
     }
 
 	@Override
 	public void onCheckedChanged(CompoundButton arg0, boolean arg1) 
 	{
 		if(arg0.getId() == R.id.chkDBInternal && arg1)
 		{
 			RaceDatabase.CreateInternal(getApplicationContext(),getFilesDir().toString());
 			SharedPreferences settings = getSharedPreferences(Prefs.SHAREDPREF_NAME, 0);
 			settings.edit().clear().putBoolean(Prefs.PREF_DBLOCATION_BOOL, true).commit();
 		}
 		else if(arg0.getId() == R.id.chkDBExternal && arg1)
 		{
 			RaceDatabase.CreateExternal(getApplicationContext());
 			SharedPreferences settings = getSharedPreferences(Prefs.SHAREDPREF_NAME, 0);
 			settings.edit().clear().putBoolean(Prefs.PREF_DBLOCATION_BOOL, false).commit();
 		}
 	}
 	
 	
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		if(requestCode == ACTIVITYRESULT_BLUETOOTH_GPS)
 		{
 			// hooray, bluetooth has been dealt with
 			boolean fBTWorking = resultCode == Activity.RESULT_OK;
 			if(!fBTWorking)
 			{
 				Toast.makeText(this, "Failed to enable Bluetooth (are you in flight/airplane mode?)", Toast.LENGTH_SHORT).show();
 			}
 		}
 		else if(requestCode == ACTIVITYRESULT_BLUETOOTH_OBD2)
 		{
 			// hooray, bluetooth has been dealt with
 			boolean fBTWorking = resultCode == Activity.RESULT_OK;
 			if(!fBTWorking)
 			{
 				Toast.makeText(this, "Failed to enable Bluetooth (are you in flight/airplane mode?)", Toast.LENGTH_SHORT).show();
 			}
 		}
 		UpdateUI();
 	}
 	
 	private String MakeGPSText(boolean fBTWorking, String strUnit)
 	{
 		if(!fBTWorking)
 		{
 			return "GPS: Internal (bluetooth disabled)";
 		}
 		else
 		{
 			if(strUnit != null && strUnit.length() > 0)
 			{
 				return "GPS: Using '" + strUnit + "' over bluetooth";
 			}
 			else
 			{
 				return "GPS: Internal";
 			}
 		}
 	}
 	private String MakeOBD2Text(SharedPreferences settings, boolean fBTWorking, String strUnit)
 	{
 		if(!fBTWorking)
 		{
 			return "OBD2: Internal (bluetooth disabled)";
 		}
 		else
 		{
 			if(strUnit != null && strUnit.length() > 0)
 			{
 				List<Integer> lst = new ArrayList<Integer>();
 				
 				Prefs.LoadOBD2PIDs(settings, lst);
 				return "OBD2: Using '" + strUnit + "' over bluetooth (" + lst.size() + " PIDs selected)";
 			}
 			else
 			{
 				return "OBD2: Off";
 			}
 		}
 	}
 	private String MakeIOIOText(SharedPreferences settings, boolean fIOIO)
 	{
 		if(fIOIO)
 		{
 			String strSelected = "";
 			String strComma = "";
 			boolean fAny = false;
 			for(int x = 1; x < 48; x++)
 			{
 				String strAnalKey = "ioiopin" + (x);
 				String strPulseKey = "ioiopinpulse" + (x);
 				if(settings.getBoolean(strAnalKey, false) || settings.getBoolean(strPulseKey, false))
 				{
 					strSelected += strComma + (x);
 					strComma = ", ";
 					fAny = true;
 				}
 			}
 			if(fAny)
 			{
 				strSelected = " (pins " + strSelected + " selected)";
 			}
 			else
 			{
 				strSelected = " (no pins selected)";
 			}
 			return "IOIO: Enabled" + strSelected;
 		}
 		else
 		{
 			return "IOIO: Off";
 		}
 	}
 	private String MakeAccelText(SharedPreferences settings)
 	{
 		if(settings.getBoolean(Prefs.PREF_USEACCEL_BOOLEAN,Prefs.DEFAULT_USEACCEL))
 		{
 			return "Using Accelerometer";
 		}
 		else
 		{
 			return "Not using accelerometer";
 		}
 	}
 	private void UpdateUI()
 	{
 		// load UI elements
 		CheckBox chkTestMode = (CheckBox)findViewById(R.id.chkTestMode);
 		Spinner spnSpeedo = (Spinner)findViewById(R.id.spnDisplayMode);
 		Spinner spnUnits = (Spinner)findViewById(R.id.spnUnits);
 		RadioButton chkInternal = (RadioButton)findViewById(R.id.chkDBInternal);
 		RadioButton chkExternal = (RadioButton)findViewById(R.id.chkDBExternal);
 		
 		Button btnGPS = (Button)findViewById(R.id.btnConfigureGPS);
 		Button btnOBD2 = (Button)findViewById(R.id.btnConfigureOBD2);
 		Button btnIOIO = (Button)findViewById(R.id.btnConfigureIOIO);
 		Button btnAccel = (Button)findViewById(R.id.btnConfigureAccel);
 		Button btnComms = (Button)findViewById(R.id.btnConfigureComms);
 		TextView chkGPS = (TextView)findViewById(R.id.lblUseExternalGPS);
 		TextView chkOBD2 = (TextView)findViewById(R.id.lblUseExternalOBD2);
 		TextView chkIOIO = (TextView)findViewById(R.id.lblUseIOIO);
 		TextView chkAccel = (TextView)findViewById(R.id.lblUseAccel);
 		
 		
 		// load settings
 		SharedPreferences settings = getSharedPreferences(Prefs.SHAREDPREF_NAME, 0);
 		String strSpeedoStyle = settings.getString(Prefs.PREF_SPEEDOSTYLE_STRING, rgstrSpeedos[0]);
 		boolean fTestMode = settings.getBoolean(Prefs.PREF_TESTMODE_BOOL, Prefs.DEFAULT_TESTMODE_BOOL);
 		Prefs.UNIT_SYSTEM eUnits = Prefs.UNIT_SYSTEM.valueOf(settings.getString(Prefs.PREF_UNITS_STRING, Prefs.DEFAULT_UNITS_STRING.toString()));
 		boolean fInternalDB = settings.getBoolean(Prefs.PREF_DBLOCATION_BOOL, Prefs.DEFAULT_DBLOCATION_BOOL);
 		String strBTGPS = settings.getString(Prefs.PREF_BTGPSNAME_STRING,Prefs.DEFAULT_GPS_STRING);
 		String strBTOBD2 = settings.getString(Prefs.PREF_BTOBD2NAME_STRING, Prefs.DEFAULT_OBD2_STRING);
 		
 		BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
 		final boolean fBTWorking = ba != null && ba.isEnabled();
 		
 		final boolean fIOIO = settings.getBoolean(Prefs.PREF_USEIOIO_BOOLEAN, false);
 		
 		chkGPS.setText(MakeGPSText(fBTWorking, strBTGPS));
 		chkGPS.setClickable(false);
 		
 		chkOBD2.setText(MakeOBD2Text(settings, fBTWorking, strBTOBD2));
 		chkOBD2.setClickable(false);
 		
 		chkIOIO.setText(MakeIOIOText(settings,fIOIO));
 		chkIOIO.setClickable(false);
 		
 		chkAccel.setText(MakeAccelText(settings));
 		
 		if(!isSdPresent())
 		{
 			fInternalDB = true;
 			chkExternal.setEnabled(false);
 			chkInternal.setChecked(true);
 		}
 		else
 		{
 			chkExternal.setEnabled(true);
 		}
 		
 		SetupSpeedoSpinner(spnSpeedo, strSpeedoStyle);
 		SetupUnitSpinner(spnUnits, eUnits);
 		chkTestMode.setChecked(fTestMode);
 		
		spnSpeedo.setOnItemSelectedListener(this);
		spnUnits.setOnItemSelectedListener(this);
		
 		if(fInternalDB) chkInternal.setChecked(true);
 		else chkExternal.setChecked(true);
 		
 		chkInternal.setOnCheckedChangeListener(this);
 		chkExternal.setOnCheckedChangeListener(this);
 		btnGPS.setOnClickListener(this);
 		btnOBD2.setOnClickListener(this);
 		btnIOIO.setOnClickListener(this);
 		btnAccel.setOnClickListener(this);
 		btnComms.setOnClickListener(this);
 	}

 	
 	@Override
 	public void onClick(View v) 
 	{
 		if(v.getId() == R.id.btnConfigureGPS)
 		{
 			Intent i = new Intent(this.getApplicationContext(), ConfigureGPSActivity.class);
 			startActivity(i);
 		}
 		else if(v.getId() == R.id.btnConfigureOBD2)
 		{
 			Intent i = new Intent(this.getApplicationContext(), ConfigureOBD2Activity.class);
 			startActivity(i);
 		}
 		else if(v.getId() == R.id.btnConfigureIOIO)
 		{
 			Intent i = new Intent(this.getApplicationContext(), ConfigureIOIOActivity.class);
 			startActivity(i);
 		}
 		else if(v.getId() == R.id.btnConfigureAccel)
 		{
 			Intent i = new Intent(this.getApplicationContext(), ConfigureAccelerometerActivity.class);
 			startActivity(i);
 		}
 		else if(v.getId() == R.id.btnConfigureComms)
 		{
 			Intent i = new Intent(this.getApplicationContext(), ConfigureCommunications.class);
 			startActivity(i);
 		}
 		/*if(v.getId() == R.id.btnScanPIDs)
 		{
 			Spinner spnOBD2 = (Spinner)findViewById(R.id.spnBTOBD2);
 			if(spnOBD2.getChildCount() > 0 && spnOBD2.getSelectedItem() != null)
 			{
 				final String strBT = spnOBD2.getSelectedItem().toString();
 				OBDThread.ThdGetSupportedPIDs(strBT,this);
 
 				Button btnSearch = (Button)findViewById(R.id.btnScanPIDs);
 				btnSearch.setEnabled(false);
 				Toast.makeText(this, "Querying device '" + strBT + "'", Toast.LENGTH_LONG).show();
 			}
 			else
 			{
 				Toast.makeText(this, "You must select a bluetooth device first", Toast.LENGTH_LONG).show();
 			}
 		}*/
 		/*else if(v.getId() == R.id.btnEnableBT)
 		{
 			BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
 			Button btn = (Button)v;
 			if(ba != null)
 			{
 				if(ba.isEnabled())
 				{
 					ba.disable();
 					HandleBTGPS(false);
 					HandleBTOBD2(false);
 					CheckBox chkGPS = (CheckBox)findViewById(R.id.chkUseExternalGPS);
 					CheckBox chkOBD2 = (CheckBox)findViewById(R.id.chkUseExternalOBD2);
 					chkGPS.setEnabled(false);
 					chkOBD2.setEnabled(false);
 					btn.setText("Enable Bluetooth");
 				}
 				else
 				{
 					Intent enableBtIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
 					startActivityForResult(enableBtIntent, 2); // we will get an onActivityResult() call with the result of this launching
 				}
 			}
 			else
 			{
 				Toast.makeText(this, "Could not get bluetooth adapter.  Does your phone have Bluetooth?", Toast.LENGTH_SHORT).show();
 			}
 		}*/
 	}
 	@Override
 	protected void SetIPString(String strIP) 
 	{
 		// shouldn't even be possible!
 	}
 
 }
