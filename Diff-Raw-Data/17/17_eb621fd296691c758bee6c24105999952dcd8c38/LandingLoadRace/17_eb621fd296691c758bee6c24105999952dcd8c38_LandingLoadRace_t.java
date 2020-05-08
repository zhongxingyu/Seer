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
 import java.util.Vector;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class LandingLoadRace extends LandingRaceBase implements OnDismissListener, OnClickListener, Handler.Callback
 {
 	private RaceImageFactory m_imgFactory;
 	private Handler m_handler;
 	private static final int MSG_NEW_IMAGE = 151;
 	private static final int MSG_RESENT_RACE_SUCCESS = 152;
 	private static final int MSG_RESENT_RACE_ERROR = 153;
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		m_handler = new Handler(this);
 		
 		setContentView(R.layout.landingloadrace);
 	}
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		DoUIInit();
 	}
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		EditText txtIP = (EditText)findViewById(R.id.txtIP);
 		Spinner spnSSID = (Spinner)findViewById(R.id.spnSSID);
 
     	SharedPreferences settings = getSharedPreferences(Prefs.SHAREDPREF_NAME, 0);
     	final boolean fSSIDGood = (spnSSID.isEnabled() && spnSSID.getSelectedItem() != null);
 		ApiDemos.SaveSharedPrefs(settings, 
 				txtIP.getText().toString(), 
 				fSSIDGood ? spnSSID.getSelectedItem().toString() : null, 
 				null, 
 				null);
 		
 		m_imgFactory.Shutdown();
 	}
 	private void DoUIInit()
 	{
 		m_imgFactory = new RaceImageFactory(m_handler, MSG_NEW_IMAGE);
 		
 		ListView list = (ListView)findViewById(android.R.id.list);
 		
 		FillRaceData(list);
 		SetupSettingsView();
 	}
 	private void SetupSettingsView()
     {
 		Button btnIP = (Button)findViewById(R.id.btnAutoIP);
 		btnIP.setOnClickListener(this);
 		
 		EditText txtIP = (EditText)findViewById(R.id.txtIP);
 		Spinner spnSSID = (Spinner)findViewById(R.id.spnSSID);
     	
 		SharedPreferences settings = getSharedPreferences(Prefs.SHAREDPREF_NAME, 0);
 		String strIP = settings.getString(Prefs.PREF_IP_STRING,Prefs.DEFAULT_IP_STRING);
 		String strSSID = settings.getString(Prefs.PREF_SSID_STRING, Prefs.DEFAULT_SSID_STRING);
 		
     	SetupSSIDSpinner(spnSSID, strSSID);
 		txtIP.setText(strIP);
     }
 	
 	private static class ListRaceData
 	{
 		private String strRaceName;
 		private int cLaps;
 		private int id;
 		private float laptime;
 		private long unixStartTimeSeconds;
 		private boolean fUsePointToPoint;
 		public ListRaceData(String strRaceName, int cLaps, int id, float laptime, int iStartTime, boolean fUsePointToPoint)
 		{
 			this.strRaceName = strRaceName;
 			this.cLaps = cLaps;
 			this.id = id;
 			this.laptime = laptime;
 			this.unixStartTimeSeconds = iStartTime;
 			this.fUsePointToPoint = fUsePointToPoint;
 		}
 		public String toString()
 		{
 			return "";
 		}
 		public int GetId()
 		{
 			return id;
 		}
 	}
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) 
     {
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.loadracemenu, menu);
     	return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem menu)
     {
     	if(menu.getItemId() == R.id.mnuDeleteTestData)
     	{
     		RaceDatabase.DeleteTestData(RaceDatabase.Get());
     		DoUIInit();
     		return true;
     	}
     	return super.onOptionsItemSelected(menu);
     }
     private class LoadRaceAdapter extends ArrayAdapter<ListRaceData>
 	{
 	    private List<ListRaceData> items;
 
 	    public LoadRaceAdapter(Context context, List<ListRaceData> objects) 
 	    {
 	        super(context, R.id.txtRaceName, objects);
 
 	        this.items = objects;
 	    }
 
 	    @Override
 	    public View getView(int position, View convertView, ViewGroup parent) 
 	    {
 	        View v = convertView;
 	        if (v == null) 
 	        {
 	            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	            v = vi.inflate(R.layout.landingloadrace_item, null);
 	        }
 	        ListRaceData myobject = items.get(position);
 
 	        if (myobject!=null)
 	        {
 	        	TextView txtName = (TextView)v.findViewById(R.id.txtRaceName);
 	        	TextView txtLapTime = (TextView)v.findViewById(R.id.txtLapTime);
 	        	TextView txtLapCount = (TextView)v.findViewById(R.id.txtLapCount);
 	        	TextView txtDate = (TextView)v.findViewById(R.id.txtDate);
 	        	ImageView img = (ImageView)v.findViewById(R.id.imgRace);
 	        	
 	        	txtName.setText(myobject.strRaceName);
 	        	txtLapTime.setText("Best Lap: " + Utility.FormatSeconds(myobject.laptime));
 	        	txtLapCount.setText("Laps: " + myobject.cLaps);
 	        	txtDate.setText(Utility.GetDateStringFromUnixTime(myobject.unixStartTimeSeconds*1000));
 	        	img.setImageBitmap(m_imgFactory.GetImage(myobject.id, false));
 	        }
 
 	        return v;
 	    }
 	}
     
 	private void FillRaceData(ListView list) 
 	{	
 		Cursor cursor = RaceDatabase.GetRaceList(RaceDatabase.Get());
 
 		List<ListRaceData> lstRaceData = new ArrayList<ListRaceData>();
 		while(cursor.moveToNext())
 		{
 			int ixName = cursor.getColumnIndex(RaceDatabase.KEY_RACENAME);
 			int ixCount = cursor.getColumnIndex(RaceDatabase.KEY_LAPCOUNT);
 			int ixId = cursor.getColumnIndex(RaceDatabase.KEY_RACEID);
 			int ixLapTime = cursor.getColumnIndex(RaceDatabase.KEY_LAPTIME);
 			int ixStartTime = cursor.getColumnIndex(RaceDatabase.KEY_STARTTIME);
 			int ixP2P = cursor.getColumnIndex(RaceDatabase.KEY_P2P);
 			
 			String strRaceName = cursor.getString(ixName);
 			int cLaps = cursor.getInt(ixCount);
 			int id = cursor.getInt(ixId);
 			float flLapTime = cursor.getFloat(ixLapTime);
 			int iStartTime = cursor.getInt(ixStartTime);
 			boolean fUsePointToPoint = cursor.getInt(ixP2P) != 0;
 			
 			lstRaceData.add(new ListRaceData(strRaceName, cLaps, id, flLapTime, iStartTime, fUsePointToPoint));
 		}
 
 		cursor.close();
 		
 		ArrayAdapter<ListRaceData> adapter = new LoadRaceAdapter(this, lstRaceData);
 		
 		list.setAdapter(adapter);
 		registerForContextMenu(list);
 	}
 	public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
 	{
 		if(v == findViewById(android.R.id.list))
 		{
 			// ok, they've contextmenu'd on the race selection list.  We want to show the "delete/rename" menu
 	    	MenuInflater inflater = getMenuInflater();
 	    	inflater.inflate(R.menu.raceoptions, menu);
 		}
 	}
 	
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item)
 	{
 		if(item.getItemId() == R.id.mnuDelete)
 		{
 			// they have requested that we delete the selected race
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
 			ListView list = (ListView)info.targetView.getParent();
 			ListRaceData lrd = (ListRaceData)list.getItemAtPosition(info.position);
 			
 			RaceDatabase.DeleteRace(RaceDatabase.Get(), lrd.id);
 			
 			FillRaceData(list);
 			return true;
 		}
 		else if(item.getItemId() == R.id.mnuRetransmit)
 		{
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
 			ListView list = (ListView)info.targetView.getParent();
 			ListRaceData lrd = (ListRaceData)list.getItemAtPosition(info.position);
 			
 			EditText txtIP = (EditText)findViewById(R.id.txtIP);
 			
 			String strIP = txtIP.getText().toString();
 
 			LapSender.SendDBToPitside(lrd.id, strIP, m_handler, MSG_RESENT_RACE_SUCCESS, MSG_RESENT_RACE_ERROR);
 		}
 		else if(item.getItemId() == R.id.mnuRename)
 		{
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
 			ListView list = (ListView)info.targetView.getParent();
 			ListRaceData lrd = (ListRaceData)list.getItemAtPosition(info.position);
 			
 			// they have requested that we rename the selected race
 			Dialog d = new RenameDialog(this, "Set the new race name", lrd, lrd.strRaceName, R.id.edtRename);
 			d.setOnDismissListener(this);
 			d.show();
 			
 			return true;
 		}
 		else if(item.getItemId() == R.id.mnuShareSummary)
 		{
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
 			ListView list = (ListView)info.targetView.getParent();
 			ListRaceData lrd = (ListRaceData)list.getItemAtPosition(info.position);
 			
 			Intent myIntent = new Intent(getApplicationContext(), SummaryActivity.class);
 			myIntent.putExtra(Prefs.IT_RACEID_LONG, (long)lrd.id);
 			myIntent.putExtra(Prefs.IT_RACENAME_STRING, lrd.strRaceName);
 			
 			SharedPreferences settings = getSharedPreferences(Prefs.SHAREDPREF_NAME, 0);
     		Prefs.UNIT_SYSTEM eUnitSystem = Prefs.UNIT_SYSTEM.valueOf(settings.getString(Prefs.PREF_UNITS_STRING, Prefs.DEFAULT_UNITS_STRING.toString()));
     		
 			myIntent.putExtra(Prefs.IT_UNITS_STRING, eUnitSystem.toString());
 			startActivity(myIntent);
 			
 		}
 		else if(item.getItemId() == R.id.mnuResumeRace)
 		{
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
 			ListView list = (ListView)info.targetView.getParent();
 			ListRaceData lrd = (ListRaceData)list.getItemAtPosition(info.position);
 			
 			ShowNextActivity(lrd, lrd.strRaceName, ApiDemos.RESUME_MODE.RESUME_RACE.ordinal());
 		}
 		else if(item.getItemId() == R.id.mnuReuseSplits)
 		{
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
 			ListView list = (ListView)info.targetView.getParent();
 			ListRaceData lrd = (ListRaceData)list.getItemAtPosition(info.position);
 			
 			RenameDialog<ListRaceData> rd = new RenameDialog<ListRaceData>(this, "Enter new race name", lrd, lrd.strRaceName, ApiDemos.RESUME_MODE.REUSE_SPLITS.ordinal());
 			rd.setOnDismissListener(this);
 			rd.show();
 		}
 		return false;
 	}
 	
 	private void ShowNextActivity(ListRaceData listData, String strRaceName, int idModeSelected)
 	{
 		SharedPreferences settings = getSharedPreferences(Prefs.SHAREDPREF_NAME, 0);
 		
 		int iCarNumber = settings.getInt(Prefs.PREF_CARNUMBER, Prefs.DEFAULT_CARNUMBER);
 		
 		RaceDatabase.RaceData r = RaceDatabase.GetRaceData(RaceDatabase.Get(),listData.GetId(), iCarNumber);
 		r.lapParams.iCarNumber = iCarNumber;
		r.lapParams.iSecondaryCarNumber = (int)(Math.random() * 100000.0);
 		if(r != null)
 		{
 			EditText txtIP = (EditText)findViewById(R.id.txtIP);
 			Spinner spnSSID = (Spinner)findViewById(R.id.spnSSID);
 			
 			String strIP = txtIP.getText().toString();
 			String strSSID = (spnSSID.isEnabled() && spnSSID.getSelectedItem() != null) ? spnSSID.getSelectedItem().toString() : "";
 			
     		String strSpeedoStyle = settings.getString(Prefs.PREF_SPEEDOSTYLE_STRING, Prefs.DEFAULT_SPEEDOSTYLE_STRING);
     		Prefs.UNIT_SYSTEM eUnitSystem = Prefs.UNIT_SYSTEM.valueOf(settings.getString(Prefs.PREF_UNITS_STRING, Prefs.DEFAULT_UNITS_STRING.toString()));
 			String strBTGPS = settings.getString(Prefs.PREF_BTGPSNAME_STRING, Prefs.DEFAULT_GPS_STRING);
     		String strOBD2 = settings.getString(Prefs.PREF_BTOBD2NAME_STRING, Prefs.DEFAULT_OBD2_STRING);
     		boolean fUseAccel = settings.getBoolean(Prefs.PREF_USEACCEL_BOOLEAN, Prefs.DEFAULT_USEACCEL);
     		boolean fAckSMS = settings.getBoolean(Prefs.PREF_ACKSMS_BOOLEAN, Prefs.DEFAULT_ACKSMS);
     		String strPrivacy = settings.getString(Prefs.PREF_PRIVACYPREFIX_STRING, Prefs.DEFAULT_PRIVACYPREFIX);
     		int iButtonPin = settings.getInt(Prefs.PREF_IOIOBUTTONPIN, Prefs.DEFAULT_IOIOBUTTONPIN);
     		
     		boolean fUseP2P = listData.fUsePointToPoint;
     		final int iStartMode = settings.getInt(Prefs.PREF_P2P_STARTMODE, Prefs.DEFAULT_P2P_STARTMODE);
     		final float flStartParam = settings.getFloat(Prefs.PREF_P2P_STARTPARAM, Prefs.DEFAULT_P2P_STARTPARAM);
     		final int iStopMode = settings.getInt(Prefs.PREF_P2P_STOPMODE, Prefs.DEFAULT_P2P_STOPMODE);
     		final float flStopMode = settings.getFloat(Prefs.PREF_P2P_STOPPARAM, Prefs.DEFAULT_P2P_STOPPARAM);
     		
     		List<Integer> lstSelectedPIDs = new ArrayList<Integer>();
     		Prefs.LoadOBD2PIDs(settings, lstSelectedPIDs);
 
     		IOIOManager.PinParams rgAnalPins[] = Prefs.LoadIOIOAnalPins(settings);
     		IOIOManager.PinParams rgPulsePins[] = Prefs.LoadIOIOPulsePins(settings);
     		
 			ApiDemos.SaveSharedPrefs(settings, strIP, strSSID, null, null);
 			Intent i = ApiDemos.BuildStartIntent(rgAnalPins, rgPulsePins, iButtonPin, fUseP2P, iStartMode, flStartParam, iStopMode, flStopMode, lstSelectedPIDs, getApplicationContext(), strIP,strSSID, r.lapParams, strRaceName, strPrivacy, fAckSMS, fUseAccel, r.fTestMode, listData.id, idModeSelected, strBTGPS, strOBD2, strSpeedoStyle, eUnitSystem.toString());
 			startActivity(i);
 		}
 		else
 		{
 			Toast.makeText(this, "Could not load race...", Toast.LENGTH_LONG).show();
 		}
 	}
 	@Override
 	public void onDismiss(DialogInterface arg0) 
 	{
 		if(arg0.getClass().equals(RenameDialog.class))
 		{
 			RenameDialog<ListRaceData> rd = (RenameDialog<ListRaceData>)arg0;
 			if(!rd.WasCancelled())
 			{
 				if(rd.GetParam() == R.id.edtRename)
 				{
 					// it was shown for the rename purpose
 					ListRaceData lrd = rd.GetData();
 					String strNewRacename = rd.GetResultText();
 					
 					RaceDatabase.RenameRace(RaceDatabase.Get(), lrd.id, strNewRacename);
 					
 					ListView list = (ListView)findViewById(android.R.id.list);
 					FillRaceData(list);
 				}
 				else if(rd.GetParam() == ApiDemos.RESUME_MODE.REUSE_SPLITS.ordinal())
 				{
 					// it was shown because they have the waypoints-only copy selected
 					ListRaceData lrd = rd.GetData();
 					
 					ShowNextActivity(lrd, rd.GetResultText(), ApiDemos.RESUME_MODE.REUSE_SPLITS.ordinal());
 				}
 			}
 		}
 	}
 	@Override
 	protected void SetIPString(String strIP) 
 	{
 		// re-save our settings.
 		// this actually occurs before onResume, so we can't just update the textbox
 		Spinner spnSSID = (Spinner)findViewById(R.id.spnSSID);
 		
 		SharedPreferences settings = getSharedPreferences(Prefs.SHAREDPREF_NAME, 0);
     	final boolean fSSIDGood = (spnSSID.isEnabled() && spnSSID.getSelectedItem() != null);
 		ApiDemos.SaveSharedPrefs(settings, 
 				strIP, 
 				fSSIDGood ? spnSSID.getSelectedItem().toString() : null, 
 				null, // no GPS changes here
 				null);
 	}
 	@Override
 	public void onClick(View arg0) 
 	{
 		if(arg0.getId() == R.id.btnAutoIP)
 		{
 			ShowAutoIPActivity();
 		}
 	}
 	@Override
 	public boolean handleMessage(Message arg0) 
 	{
 		if(arg0.what == MSG_NEW_IMAGE)
 		{
 			ListView list = (ListView)findViewById(android.R.id.list);
 			list.invalidateViews();
 			return true;
 		}
 		else if(arg0.what == MSG_RESENT_RACE_ERROR)
 		{
 			Toast.makeText(this, "Failed to resend race: " + arg0.obj.toString(), Toast.LENGTH_LONG).show();
 		}
 		else if(arg0.what == MSG_RESENT_RACE_SUCCESS)
 		{
 			Toast.makeText(this, "Successfully transmitted database to pitside", Toast.LENGTH_LONG).show();
 		}
 		return false;
 	}
 }
