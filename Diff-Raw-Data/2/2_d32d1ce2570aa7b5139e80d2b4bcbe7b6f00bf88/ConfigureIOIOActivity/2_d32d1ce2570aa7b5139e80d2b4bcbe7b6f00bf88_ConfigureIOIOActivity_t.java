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
 
 import com.artsoft.wifilapper.IOIOManager.PinParams;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.RadioGroup;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 
 public class ConfigureIOIOActivity extends Activity implements OnCheckedChangeListener, OnClickListener, OnSeekBarChangeListener
 {
 	List<IOIOManager.PinParams> lstAnalPins;
 	List<IOIOManager.PinParams> lstPulsePins;
 	
 	private int m_iLastFilterType = IOIOManager.PinParams.FILTERTYPE_NONE;
 	private double m_dLastParam1;
 	private double m_dLastParam2;
 	private int m_iLastCustomType;
 	
 	@Override
 	public void onCreate(Bundle extras)
 	{
 		super.onCreate(extras);
 		setContentView(R.layout.configureioio);
 	}
 	
 	@Override
 	public void onActivityResult(int reqCode, int resCode, Intent data)
 	{
 		if(reqCode == ConfigureIOIOFilter.REQUESTCODE_CUSTOMFILTER && resCode == Activity.RESULT_OK)
 		{
 			// they successfully changed things
 			m_iLastFilterType = data.getIntExtra("filtertype", IOIOManager.PinParams.FILTERTYPE_NONE);
 			m_dLastParam1 = data.getDoubleExtra("param1", 0);
 			m_dLastParam2 = data.getDoubleExtra("param2", 0);
 			m_iLastCustomType = data.getIntExtra("customtype", 0);
 			
 			// for convenience sake, if they have select a filter type that only applies to a certain type of pin, then select that type of pin
 			RadioGroup rg = (RadioGroup)findViewById(R.id.rgPinType);
 			switch(m_iLastFilterType)
 			{
 			case PinParams.FILTERTYPE_WHEELSPEED:
 				rg.check(R.id.rbPulse);
 				break;
 			default:
 				// don't change anything
 			}
 	    	TextView txtCurrentFilter = (TextView)findViewById(R.id.lblCurrentFilter);
 			txtCurrentFilter.setText("Filter: " + PinParams.BuildDesc(m_iLastFilterType, this.m_dLastParam1, this.m_dLastParam2, true));
 		}
 	}
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		
 		SharedPreferences settings = this.getSharedPreferences(Prefs.SHAREDPREF_NAME, 0);
 		
 		{ // set up spinner
 			long iSelectedPin = settings.getLong("selpin", 31);
 			
 			Spinner spnPin = (Spinner)findViewById(R.id.spnPin);
 			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 		            this, R.array.ioiopins, android.R.layout.simple_spinner_item);
 		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		    spnPin.setAdapter(adapter);
 		    for(int x = 0; x < spnPin.getCount(); x++)
 		    {
 		    	CharSequence c = (CharSequence)spnPin.getItemAtPosition(x);
 		    	if(("" + iSelectedPin).equals(c))
 		    	{
 		    		spnPin.setSelection(x);
 		    		break;
 		    	}
 		    }
 		}
 		
 	    SeekBar seek = (SeekBar)findViewById(R.id.seekSampleRate);
 		CheckBox chk = (CheckBox)findViewById(R.id.chkIOIO);
 		CheckBox chkClicker = (CheckBox)findViewById(R.id.chkClicker);
 		Button btnAdd = (Button)findViewById(R.id.btnAdd);
 		Button btnCustom = (Button)findViewById(R.id.btnCustom);
 		
 		boolean fIOIO = settings.getBoolean(Prefs.PREF_USEIOIO_BOOLEAN, false);
 		boolean fClicker = settings.getInt(Prefs.PREF_IOIOBUTTONPIN, Prefs.DEFAULT_IOIOBUTTONPIN) >= 1;
 		
 		chk.setChecked(fIOIO);
 		chkClicker.setChecked(fClicker);
 		onCheckedChanged(chk,fIOIO);
 
 		lstAnalPins = new ArrayList<IOIOManager.PinParams>();
 		lstPulsePins = new ArrayList<IOIOManager.PinParams>();
 		IOIOManager.PinParams rgAnalPins[] = Prefs.LoadIOIOAnalPins(settings);
 		IOIOManager.PinParams rgPulsePins[] = Prefs.LoadIOIOPulsePins(settings);
 		for(int x = 0;x < rgAnalPins.length; x++)
 		{
 			lstAnalPins.add(rgAnalPins[x]);
 		}
 		for(int x = 0;x < rgPulsePins.length; x++)
 		{
 			lstPulsePins.add(rgPulsePins[x]);
 		}
 		
 		chk.setOnCheckedChangeListener(this);
 		btnAdd.setOnClickListener(this);
 		btnCustom.setOnClickListener(this);
 		seek.setOnSeekBarChangeListener(this);
 		
 		UpdateList();
 	}
 	
 	// sets up the main controls to match the given pin
 	// note that iPinType is the id of the radiobutton control that needs to be checked to represent pin type (analog, pulse, digital, etc)
 	private void SetPinControls(IOIOManager.PinParams pin, int iPinType)
 	{
 	    {
 		    SeekBar seek = (SeekBar)findViewById(R.id.seekSampleRate);
 	    	TextView txtRate = (TextView)findViewById(R.id.txtSampleRate);
 	    	final double dPeriod = pin.iPeriod; // sample spread in milliseconds
 			final double dSeek = (Math.log(dPeriod) - Math.log(10000))/-4.605;
 			final double dRate = 1000 / dPeriod;
 			txtRate.setText("Sample Rate (" + Utility.FormatFloat((float)dRate, 1) + "hz):");
 			
 	    	seek.setProgress((int)(dSeek*seek.getMax()));
 	    	seek.invalidate();
 	    }
 
 	    { // radio buttons
 	    	RadioGroup rg = (RadioGroup)findViewById(R.id.rgPinType);
 	    	rg.check(iPinType);
 	    	rg.invalidate();
 	    }
 	    
 	    { // pin #s
 	    	Spinner spnPin = (Spinner)findViewById(R.id.spnPin);
 	    	for(int x = 0;x < spnPin.getCount(); x++)
 	    	{
 	    		Object objSelected = spnPin.getItemAtPosition(x);
 	    		if(objSelected != null)
 	    		{
 	    			long lPinNumber = Long.parseLong(objSelected.toString());
 	    			if(lPinNumber == pin.iPin)
 	    			{
 	    				spnPin.setSelection(x);
 	    				break;
 	    			}
 	    		}
 	    	}
 	    	spnPin.invalidate();
 	    }
 	    
 	    { // filter display
 			this.m_iLastCustomType = pin.iCustomType;
 			this.m_iLastFilterType = pin.iFilterType;
 			this.m_dLastParam1 = pin.dParam1;
 			this.m_dLastParam2 = pin.dParam2;
 			
 	    	TextView txtCurrentFilter = (TextView)findViewById(R.id.lblCurrentFilter);
 			txtCurrentFilter.setText("Filter: " + PinParams.BuildDesc(pin.iFilterType, pin.dParam1, pin.dParam2, true));
 			
 			txtCurrentFilter.invalidate();
 	    }
 	}
 	// builds a view to put in the list of pins
 	private View BuildPinView(String strName, IOIOManager.PinParams pin)
 	{
 		TableRow.LayoutParams layout = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f);
 		
 		
 		TextView txtName = new TextView(this);
 		txtName.setText(strName);
 		txtName.setLayoutParams(layout);
 		
 		TextView txtPin = new TextView(this);
 		txtPin.setText("Pin " + pin.iPin);
 		txtPin.setLayoutParams(layout);
 		
 		TextView txtRate = new TextView(this);
 		final float dHz = 1000.0f / (float)pin.iPeriod;
 		txtRate.setText("Rate: " + Utility.FormatFloat(dHz, 1) + "hz");
 		txtRate.setLayoutParams(layout);
 		
 		TextView txtFilter = new TextView(this);
 		txtFilter.setText(PinParams.BuildDesc(pin.iFilterType,pin.dParam1,pin.dParam2, true));
 		
 		Button btn = new Button(this);
 		btn.setText("Delete");
 		btn.setId(pin.iPin);
 		btn.setOnClickListener(this);
 		btn.setLayoutParams(layout);
 
 		TableRow tr = new TableRow(this);
 		tr.setLayoutParams(new TableRow.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
 		tr.addView(txtName);
 		tr.addView(txtPin);
 		tr.addView(txtRate);
 		tr.addView(txtFilter);
 		tr.addView(btn);
 		return tr;
 	}
 	private void UpdateList()
 	{
 		// takes rgAnalPins and rgPulsePins, and puts them into list form
 		TableLayout pinList = (TableLayout)findViewById(R.id.pintable);
 		pinList.removeAllViews();
 		for(int x = 0; x < lstAnalPins.size(); x++)
 		{
 			View v = BuildPinView("Analog Pin",lstAnalPins.get(x));
 			pinList.addView(v);
 		}
 		for(int x = 0; x < lstPulsePins.size(); x++)
 		{
 			View v = BuildPinView("Pulse Pin",lstPulsePins.get(x));
 			pinList.addView(v);
 		}
 		pinList.invalidate();
 		
     	TextView txtCurrentFilter = (TextView)findViewById(R.id.lblCurrentFilter);
 		txtCurrentFilter.setText("Filter: " + PinParams.BuildDesc(m_iLastFilterType, this.m_dLastParam1, this.m_dLastParam2, true));
 	}
 	
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 
 		Spinner spnPin = (Spinner)findViewById(R.id.spnPin);
 		
 		SharedPreferences settings = this.getSharedPreferences(Prefs.SHAREDPREF_NAME, 0);
 		CheckBox chk = (CheckBox)findViewById(R.id.chkIOIO);
 		CheckBox chkClicker = (CheckBox)findViewById(R.id.chkClicker);
 		
 		SharedPreferences.Editor edit = settings.edit();
 		edit = SaveIOIOPins(edit);
 		edit = edit.putBoolean(Prefs.PREF_USEIOIO_BOOLEAN, chk.isChecked());
 		edit = edit.putInt(Prefs.PREF_IOIOBUTTONPIN, chkClicker.isChecked() ? 17 : Prefs.DEFAULT_IOIOBUTTONPIN);
 		
 		Object objSelected = spnPin.getItemAtPosition(spnPin.getSelectedItemPosition());
 		if(objSelected != null)
 		{
 			edit = edit.putLong("selpin", Long.parseLong(objSelected.toString()));
 		}
 		edit.commit();
 	}
 
 	private SharedPreferences.Editor SaveIOIOPins(SharedPreferences.Editor edit)
 	{
 		CheckBox chkIOIO = (CheckBox)findViewById(R.id.chkIOIO);
 		
 		boolean fIOIOEnabled = chkIOIO.isChecked();
 		
 		edit = edit.putBoolean(Prefs.PREF_USEACCEL_BOOLEAN, fIOIOEnabled);
 
 		edit = Prefs.SavePins(edit,lstAnalPins,lstPulsePins);
 		
 		return edit;
 	}
 	
 	@Override
 	public void onCheckedChanged(CompoundButton arg0, boolean arg1) 
 	{
 	}
 
 	@Override
 	public void onClick(View arg0) 
 	{
 		if(arg0.getId() == R.id.btnAdd)
 		{
 			// they want to add the currently selected pin data.  check if it's valid (no dupes, analogs only 31-39, etc), then add it
 			RadioGroup rg = (RadioGroup)findViewById(R.id.rgPinType);
 			SeekBar seek = (SeekBar)findViewById(R.id.seekSampleRate);
 			Spinner spnPin = (Spinner)findViewById(R.id.spnPin);
			CheckBox chkIOIO = (CheckBox)findViewById(R.id.chkIOIO);
			chkIOIO.setChecked(true);
 			
 			double dSeek = (double)seek.getProgress() / (double)seek.getMax();
 			
 			
 			// we need seek(0) = 100, seek(0.5) = 1000, and seek(1.0) = 10000.  This does the trick
 			double dPeriod = 10000*Math.pow(Math.E, -4.605*dSeek);
 			int iPin = Integer.parseInt(spnPin.getSelectedItem().toString());
 			
 			boolean fAlreadyPresent = false;
 			// make sure that this pin is not already in the list
 			for(int x = 0;x < lstAnalPins.size(); x++) fAlreadyPresent |= lstAnalPins.get(x).iPin == iPin;
 			for(int x = 0;x < lstPulsePins.size(); x++) fAlreadyPresent |= lstPulsePins.get(x).iPin == iPin;
 			
 			boolean fPermitted = true;
 			if(iPin < 31 && rg.getCheckedRadioButtonId() == R.id.rbAnalog) fPermitted &= false; // not allowed having analog pins below 31
 			
 			if(!fAlreadyPresent)
 			{
 				if(fPermitted)
 				{
 					IOIOManager.PinParams pin = new IOIOManager.PinParams(iPin,(int)dPeriod, m_iLastFilterType, m_dLastParam1, m_dLastParam2, m_iLastCustomType);
 					if(rg.getCheckedRadioButtonId() == R.id.rbAnalog)
 					{
 						lstAnalPins.add(pin);
 					}
 					else if(rg.getCheckedRadioButtonId() == R.id.rbPulse)
 					{
 						lstPulsePins.add(pin);
 					}
 				}
 				else
 				{
 					Toast.makeText(this, "You cannot have an analog pin below pin 31.", Toast.LENGTH_SHORT).show();
 				}
 			}
 			else
 			{
 				Toast.makeText(this,"That pin is already being queried.",Toast.LENGTH_SHORT).show();
 			}
 		}
 		else if(arg0.getId() == R.id.btnCustom)
 		{
 			// they want to go to the customize view
 			Intent i = new Intent(this, ConfigureIOIOFilter.class);
 			this.startActivityForResult(i, ConfigureIOIOFilter.REQUESTCODE_CUSTOMFILTER);
 		}
 		else
 		{
 			IOIOManager.PinParams pinRemoved = null;
 			int iTypeId = -1;
 			// they must have clicked a 'remove' button.  Find all pins that match the id (the id being the pin # they want to remove)
 			for(int x = 0; x < lstAnalPins.size(); x++)
 			{
 				if(lstAnalPins.get(x).iPin == arg0.getId())
 				{
 					pinRemoved = lstAnalPins.get(x);
 					iTypeId = R.id.rbAnalog;
 					lstAnalPins.remove(x);
 				}
 			}
 			for(int x = 0; x < lstPulsePins.size(); x++)
 			{
 				if(lstPulsePins.get(x).iPin == arg0.getId())
 				{
 					pinRemoved = lstPulsePins.get(x);
 					iTypeId = R.id.rbPulse;
 					lstPulsePins.remove(x);
 				}
 			}
 			if(pinRemoved != null)
 			{
 				SetPinControls(pinRemoved, iTypeId);
 			}
 		}
 		UpdateList();
 	}
 
 
 	@Override
 	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) 
 	{
 		TextView txtRate = (TextView)findViewById(R.id.txtSampleRate);
 		double dSeek = (double)arg0.getProgress() / (double)arg0.getMax();
 		
 		// we need seek(0) = 100, seek(0.5) = 1000, and seek(1.0) = 10000.  This does the trick
 		double dPeriod = 10000*Math.pow(Math.E, -4.605*dSeek); // how many milliseconds between samples
 		double dRate = 1000 / dPeriod;
 		txtRate.setText("Sample Rate (" + Utility.FormatFloat((float)dRate, 1) + "hz):");
 	}
 
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar) {}
 }
