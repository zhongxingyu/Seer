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
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import com.artsoft.wifilapper.LapAccumulator.LapAccumulatorParams;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 import android.widget.Spinner;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class LandingNewRace extends LandingRaceBase implements OnClickListener, DialogInterface.OnClickListener
 {
 	private Button m_btnApply;
 	private DialogInterface m_dlgAlert;
 	private Intent m_startIntent = null;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		View vNewRace = (View)View.inflate(this, R.layout.landingnewrace, null);
 		setContentView(vNewRace);
 	}
 	@Override
 	public void onResume()
 	{
 		super.onResume();
     	SetupSettingsView();
 	}
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		EditText txtIP = (EditText)findViewById(R.id.txtIP);
 		Spinner spnSSID = (Spinner)findViewById(R.id.spnSSID);
 		EditText txtRaceName = (EditText)findViewById(R.id.txtRaceName);
 		
     	SharedPreferences settings = getSharedPreferences(Prefs.SHAREDPREF_NAME, 0);
     	final boolean fSSIDGood = (spnSSID.isEnabled() && spnSSID.getSelectedItem() != null);
 		ApiDemos.SaveSharedPrefs(settings, 
 				txtIP.getText().toString(), 
 				fSSIDGood ? spnSSID.getSelectedItem().toString() : null, 
 				null, // no GPS changes here
 				txtRaceName.getText().toString());
 	}
     private void SetupSettingsView()
     {
     	m_btnApply = (Button)findViewById(R.id.btnApply);
     	m_btnApply.setOnClickListener(this);
 
 		Button btnIP = (Button)findViewById(R.id.btnAutoIP);
 		btnIP.setOnClickListener(this);
 		
 		
 		EditText txtIP = (EditText)findViewById(R.id.txtIP);
 		Spinner spnSSID = (Spinner)findViewById(R.id.spnSSID);
 		EditText txtRaceName = (EditText)findViewById(R.id.txtRaceName);
 		
 		SharedPreferences settings = getSharedPreferences(Prefs.SHAREDPREF_NAME, 0);
 		String strIP = settings.getString(Prefs.PREF_IP_STRING,Prefs.DEFAULT_IP_STRING);
 		String strSSID = settings.getString(Prefs.PREF_SSID_STRING, Prefs.DEFAULT_SSID_STRING);
 		String strRaceName = settings.getString(Prefs.PREF_RACENAME_STRING, Prefs.DEFAULT_RACENAME_STRING);
 		
 		txtIP.setText(strIP);
 		SetupSSIDSpinner(spnSSID, strSSID);
 		txtRaceName.setText(strRaceName);
     }
 
 	@Override
 	public void onClick(View v) 
 	{
 		if(v.getId() == R.id.btnApply)
     	{
     		EditText txtIP = (EditText)findViewById(R.id.txtIP);
     		Spinner spnSSID = (Spinner)findViewById(R.id.spnSSID);
     		EditText txtRaceName = (EditText)findViewById(R.id.txtRaceName);
     		RadioGroup rgMode = (RadioGroup)findViewById(R.id.rgRaceMode);
     		
     		String strIP = txtIP.getText().toString();
     		String strSSID = (spnSSID.isEnabled() && spnSSID.getSelectedItem() != null) ? spnSSID.getSelectedItem().toString() : "";
     		String strRaceName = txtRaceName.getText().toString();
     		Prefs.UNIT_SYSTEM eUnitSystem;
     		
     		SharedPreferences settings = getSharedPreferences(Prefs.SHAREDPREF_NAME, 0);
     		String strSpeedoStyle = settings.getString(Prefs.PREF_SPEEDOSTYLE_STRING, Prefs.DEFAULT_SPEEDOSTYLE_STRING);
     		boolean fTestMode = settings.getBoolean(Prefs.PREF_TESTMODE_BOOL, Prefs.DEFAULT_TESTMODE_BOOL);
     		String strUnitSystem = settings.getString(Prefs.PREF_UNITS_STRING, Prefs.DEFAULT_UNITS_STRING.toString());
 			String strBTGPS = settings.getString(Prefs.PREF_BTGPSNAME_STRING, Prefs.DEFAULT_GPS_STRING);
 			String strBTOBD2 = settings.getString(Prefs.PREF_BTOBD2NAME_STRING, Prefs.DEFAULT_OBD2_STRING);
     		boolean fUseAccel = settings.getBoolean(Prefs.PREF_USEACCEL_BOOLEAN, Prefs.DEFAULT_USEACCEL);
     		boolean fAckSMS = settings.getBoolean(Prefs.PREF_ACKSMS_BOOLEAN, Prefs.DEFAULT_ACKSMS);
     		String strPrivacy = settings.getString(Prefs.PREF_PRIVACYPREFIX_STRING, Prefs.DEFAULT_PRIVACYPREFIX);
     		int iButtonPin = settings.getInt(Prefs.PREF_IOIOBUTTONPIN, Prefs.DEFAULT_IOIOBUTTONPIN);
     		
     		boolean fUseP2P = rgMode.getCheckedRadioButtonId() == R.id.rbPointToPoint;
     		final int iStartMode = settings.getInt(Prefs.PREF_P2P_STARTMODE, Prefs.DEFAULT_P2P_STARTMODE);
     		final float flStartParam = settings.getFloat(Prefs.PREF_P2P_STARTPARAM, Prefs.DEFAULT_P2P_STARTPARAM);
     		final int iStopMode = settings.getInt(Prefs.PREF_P2P_STOPMODE, Prefs.DEFAULT_P2P_STOPMODE);
     		final float flStopMode = settings.getFloat(Prefs.PREF_P2P_STOPPARAM, Prefs.DEFAULT_P2P_STOPPARAM);
     		
     		eUnitSystem = Prefs.UNIT_SYSTEM.valueOf(strUnitSystem);
     		
     		ApiDemos.SaveSharedPrefs(settings, strIP, strSSID, strBTGPS, strRaceName);
 
     		List<Integer> lstSelectedPIDs = new ArrayList<Integer>();
     		Prefs.LoadOBD2PIDs(settings, lstSelectedPIDs);
     		
     		IOIOManager.PinParams rgAnalPins[] = Prefs.LoadIOIOAnalPins(settings);
     		IOIOManager.PinParams rgPulsePins[] = Prefs.LoadIOIOPulsePins(settings);
     		
     		LapAccumulator.LapAccumulatorParams lapParams = new LapAccumulator.LapAccumulatorParams();
     		lapParams.iCarNumber = settings.getInt(Prefs.PREF_CARNUMBER, Prefs.DEFAULT_CARNUMBER);
    		lapParams.iSecondaryCarNumber = (int)(Math.random() * 100000.0); 
     		
     		Intent i = ApiDemos.BuildStartIntent(rgAnalPins,rgPulsePins, iButtonPin, fUseP2P, iStartMode, flStartParam, iStopMode, flStopMode, lstSelectedPIDs, getApplicationContext(), strIP,strSSID, lapParams, strRaceName, strPrivacy, fAckSMS, fUseAccel, fTestMode, -1, -1, strBTGPS, strBTOBD2, strSpeedoStyle, eUnitSystem.toString());
     		if(fTestMode)
     		{
     			// they're about to start a run in test mode.  Test mode sucks for real users, so warn them
     			AlertDialog ad = new AlertDialog.Builder(this).create();
     			ad.setMessage("Test mode is currently selected.  GPS reception will be disabled.  Are you sure?");
     			ad.setButton(AlertDialog.BUTTON_POSITIVE,"Yes", this);
     			ad.setButton(AlertDialog.BUTTON_NEGATIVE,"No/Cancel", this);
     			m_startIntent = i;
     			m_dlgAlert = ad;
     			ad.show();
     		}
     		else
     		{
     			startActivity(i);
     		}
     	}
 		else if(v.getId() == R.id.btnAutoIP)
 		{
 			ShowAutoIPActivity();
 		}
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) 
 	{
 	}
 	@Override
 	protected void SetIPString(String strIP) 
 	{
 		// re-save our settings.
 		// this actually occurs before onResume, so we can't just update the textbox
 		Spinner spnSSID = (Spinner)findViewById(R.id.spnSSID);
 		EditText txtRaceName = (EditText)findViewById(R.id.txtRaceName);
 		
 		SharedPreferences settings = getSharedPreferences(Prefs.SHAREDPREF_NAME, 0);
     	final boolean fSSIDGood = (spnSSID.isEnabled() && spnSSID.getSelectedItem() != null);
 		ApiDemos.SaveSharedPrefs(settings, 
 				strIP, 
 				fSSIDGood ? spnSSID.getSelectedItem().toString() : null, 
 				null, // no GPS changes here
 				txtRaceName.getText().toString());
 	}
 	@Override
 	public void onClick(DialogInterface dlg, int choice) 
 	{
 		if(dlg == m_dlgAlert && m_startIntent != null)
 		{
 			// they clicked our alert dialog
 			if(choice == Dialog.BUTTON_POSITIVE)
 			{
 				// they're fine with test mode, so go ahead
     			startActivity(m_startIntent);
 			}
 			else if(choice == Dialog.BUTTON_NEGATIVE)
 			{
 				// they have wisely chosen to avoid test mode
 			}
 			dlg = null;
 			m_startIntent = null;
 		}
 	}
 	
 }
