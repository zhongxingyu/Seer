 package com.zorro666.worldwarcalculator;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.widget.TabHost;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.HorizontalScrollView;
 import android.widget.ArrayAdapter;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.view.View.OnTouchListener;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.text.InputFilter;
 import android.text.InputType;
 import android.text.method.DigitsKeyListener;
 import android.util.Log;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 public class WorldWarCalc extends Activity implements OnKeyListener, OnTouchListener, OnClickListener, OnFocusChangeListener, OnItemSelectedListener
 {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		m_defenceBuildings = new WWBuilding[NUM_DEFENCE_BUILDINGS];
 		m_incomeBuildings = new WWBuilding[NUM_INCOME_BUILDINGS];
 		m_numDefenceBuildings = 0;
 		m_numIncomeBuildings = 0;
 
 		m_defenceBuildings[m_numDefenceBuildings++] = new WWDefenceBuilding( "Bunker", 30000, 3);
 		m_defenceBuildings[m_numDefenceBuildings++] = new WWDefenceBuilding( "Guard Tower", 200000, 10);
 		m_defenceBuildings[m_numDefenceBuildings++] = new WWDefenceBuilding( "Anti-Aircraft Launcher", 560000, 15);
 		m_defenceBuildings[m_numDefenceBuildings++] = new WWDefenceBuilding( "Turret", 2800000, 32);
 		m_defenceBuildings[m_numDefenceBuildings++] = new WWDefenceBuilding( "Landmine Field", 10000000, 50);
 		m_defenceBuildings[m_numDefenceBuildings++] = new WWDefenceBuilding( "Automated Sentry Gun", 24000000, 75);
 
 		m_incomeBuildings[m_numIncomeBuildings++] = new WWIncomeBuilding( "Supply Depot", 18000, 1000);
 		m_incomeBuildings[m_numIncomeBuildings++] = new WWIncomeBuilding( "Refinery", 150000, 6500);
 		m_incomeBuildings[m_numIncomeBuildings++] = new WWIncomeBuilding( "Weapons Factory", 540000, 16500);
 		m_incomeBuildings[m_numIncomeBuildings++] = new WWIncomeBuilding( "Power Plant", 2700000, 56000);
 		m_incomeBuildings[m_numIncomeBuildings++] = new WWIncomeBuilding( "Oil Rig", 20000000, 270000);
 		m_incomeBuildings[m_numIncomeBuildings++] = new WWIncomeBuilding( "Military Research Lab", 60000000, 500000);
 		m_incomeBuildings[m_numIncomeBuildings++] = new WWIncomeBuilding( "Nuclear Testing Facility", 100000000, 700000);
 		m_incomeBuildings[m_numIncomeBuildings++] = new WWIncomeBuilding( "Solar Satellite Network", 340000000, 1200000);
 
 		m_activeProfile = new WWProfile("default");
 		
 		m_profileNames = new ArrayList<String>();
 		m_profiles = new HashMap<String, WWProfile>(DEFAULT_NUM_PROFILES);
 
 		m_profilesAdapter =  new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_profileNames);
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		Button saveProfile = (Button) findViewById(R.id.profileSaveButton);
 		saveProfile.setOnClickListener(this);
 		
 		Button newProfile = (Button) findViewById(R.id.profileNewButton);
 		newProfile.setOnClickListener(this);
 
 		Button deleteProfile = (Button) findViewById(R.id.profileDeleteButton);
 		deleteProfile.setOnClickListener(this);
 
 		EditText profileName = (EditText)findViewById(R.id.profileName);
 		profileName.setOnFocusChangeListener(this);
 
 		Spinner profileNameView = (Spinner)findViewById(R.id.profileSpinner);
 		profileNameView.setOnItemSelectedListener(this);
 		
 		// An option would be to subclass it and implement getView function to make it work with WWProfile
 		m_profilesAdapter.setDropDownViewResource(android.R.layout. simple_spinner_dropdown_item);
 		profileNameView.setAdapter(m_profilesAdapter);
 		profileNameView.setSelection(0);
 		
 		TableLayout defenceView = (TableLayout) findViewById(R.id.DefenceView);
 		boolean row;
 		row = true;
 		for (int i = 0; i < m_numDefenceBuildings; i++) 
 		{
 			WWBuilding building = m_defenceBuildings[i];
 			addRow(defenceView, building, row);
 			row ^= true;
 		}
 
 		TableLayout incomeView = (TableLayout) findViewById(R.id.IncomeViewData);
 		row = true;
 		for (int i = 0; i < m_numIncomeBuildings; i++) 
 		{
 			WWBuilding building = m_incomeBuildings[i];
 			addRow(incomeView, building, row);
 			row ^= true;
 		}
 
 		// m_incomeViewHeader =
 		// (HorizontalScrollView)findViewById(R.id.IncomeViewHeader);
 		// m_incomeViewHeader.setOnTouchListener(this);
 
 		// HorizontalScrollView m_incomeViewScroll =
 		// (HorizontalScrollView)findViewById(R.id.IncomeViewScroll);
 		// m_incomeViewScroll.setOnTouchListener(this);
 		// m_incomeViewScroll.setSmoothScrollingEnabled(true);
 
 		final TabHost tabs = (TabHost) findViewById(R.id.tabhost);
 		tabs.setup();
 
 		TabHost.TabSpec spec;
 
 		spec = tabs.newTabSpec("Profile");
 		spec.setContent(R.id.ProfileViewTab);
 		spec.setIndicator("Profile");
 		tabs.addTab(spec);
 
 		spec = tabs.newTabSpec("Income");
 		spec.setContent(R.id.IncomeViewTab);
 		spec.setIndicator("Income");
 		tabs.addTab(spec);
 
 		spec = tabs.newTabSpec("Defence");
 		spec.setContent(R.id.DefenceView);
 		spec.setIndicator("Defence");
 		tabs.addTab(spec);
 		tabs.setCurrentTab(1);
 	}
 	
 	@Override
 	public void onStart()
 	{
 		// Load the profile files in
 		LoadAllProfiles();
 
 		if (m_profilesAdapter.getCount() == 0)
 		{
 			ProfileNew();
 		}
 
 		ProfileSelect();
 		
 		super.onStart();
 	}
 
 	@Override
 	public void onRestart()
 	{
 		super.onRestart();
 	}
 
 	// Another application comes in front of this view
 	@Override
 	public void onPause()
 	{
 		// Save all the profiles
 		SaveAllProfiles();
 		
 		super.onPause();
 	}
 
 	// Application is no longer in view
 	@Override
 	public void onStop()
 	{
 		super.onStop();
 	}
 	
 	// Application is being destroyed
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 	}
 	
 	public boolean onTouch(View v, MotionEvent event) 
 	{
 		if (v == m_incomeViewScroll) 
 		{
 			m_incomeViewHeader.onTouchEvent(event);
 		}
 		if (v == m_incomeViewHeader) 
 		{
 			m_incomeViewScroll.onTouchEvent(event);
 		}
 		return v.onTouchEvent(event);
 	}
 
 	public void onFocusChange(View v, boolean hasFocus) 
 	{ 
 		if (v.getId()==R.id.profileName) 
 		{ 
 			if (hasFocus == false) 
 			{ 
 				EditText profileNameView = (EditText)v;
 				String newName = profileNameView.getText().toString();
 				ProfileRename(newName);
 			} 
 		}
 	}
 	public void onClick(View v) 
 	{
 		if (v.getId() == R.id.profileSaveButton) 
 		{
 			ProfileSave(m_activeProfile);
 		}
 		else if (v.getId() == R.id.profileNewButton) 
 		{
 			ProfileNew();
 		}
 		else if (v.getId() == R.id.profileDeleteButton)
 		{
 			ProfileDelete();
 		}
 		else
 		{
 			Object tag = v.getTag();
 			if (tag.getClass() == WWProfileEntry.class)
 			{
 				WWProfileEntry profileEntry = (WWProfileEntry) tag;
 				WWBuilding building = profileEntry.GetBuilding();
 				int delta = 0;
 				if (v.getId() == 123456)
 				{
 					delta = -1;
 					Log.i(TAG,"Minus Button:"+building.GetName());
 				}
 				if (v.getId() == 654321)
 				{
 					delta = +1;
 				}
 				if (delta != 0)
 				{
 					int numOwned = profileEntry.GetNumOwned();
 					numOwned += delta;
 					if ((numOwned >= 0) && (numOwned <= 9999))
 					{
 						profileEntry.SetNumOwned(numOwned);
 						UpdateBuildingRow(profileEntry);
 						UpdateBuildingNumOwned(profileEntry);
 						Log.i(TAG,"-/+ Button:"+building.GetName()+" Delta:"+delta);
 					}
 				}
 			}
 		}
 	}
 	public boolean onKey(View v, int key, KeyEvent event) 
 	{
 		Object tag = v.getTag();
 		if (tag.getClass() == WWProfileEntry.class) 
 		{
 			WWProfileEntry profileEntry = (WWProfileEntry) tag;
 			WWBuilding building = profileEntry.GetBuilding();
 			EditText numberText = building.GetViewNumOwned();
 			if (v == numberText) 
 			{
 				int numOwned = 0;
 				String text = numberText.getText().toString();
 
 				if (text.length() > 0) 
 				{
 					numOwned = Integer.parseInt(text);
 				}
 				profileEntry.SetNumOwned(numOwned);
 				
 				UpdateBuildingRow(profileEntry);
 
 				Log.i(TAG, "onKey Building = " + profileEntry.GetBuilding().GetName() + " numOwned = " + numOwned);
 			}
 		}
 		return false;
 	}
 
 	public void onNothingSelected(AdapterView<?> parent)
 	{
 	}
 
 	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) 
 	{
 		if (parent.getId() == R.id.profileSpinner)
 		{
 			TextView item = (TextView)view;
 			if (item != null)
 			{
 				String profileName = item.getText().toString();
 				Log.i(TAG,"ProfileSelect["+position+"] = "+profileName);
 				ProfileSelect();
 			}
 		}
 	}
 	private void ProfileSelect()
 	{
 		Spinner profileSpinner = (Spinner)findViewById(R.id.profileSpinner);
		int itemIndex = (int)profileSpinner.getSelectedItemId();
		int numItems = profileSpinner.getCount();
		if (itemIndex >= numItems)
		{
			itemIndex = 0;
		}
		if (numItems == 0)
		{
			return;
		}
		String profileName = m_profilesAdapter.getItem(itemIndex);
 		Log.i(TAG,"ProfileSelect = "+profileName);
 		
 		WWProfile profile = m_profiles.get(profileName);
 		// Need to error check this in case it returns NULL
 		if (profile == null)
 		{
 			Log.i(TAG,"ProfileSelect: NULL = "+profileName);
 			// Make sure we have a profile to use
 			if (m_profilesAdapter.getCount() == 0)		
 			{
 				ProfileNew();
 			}
 		}
 		
 		EditText profileNameView = (EditText)findViewById(R.id.profileName);
 		profileNameView.setText(profileName);
 		
 		// Now update the displayed details based on the active profile
 		m_activeProfile = profile;
 		UpdateBuildingsView();
 	}
 	
 	private void UpdateBuildingsView()
 	{
 		for (int i = 0; i < m_activeProfile.GetNumDefenceBuildings(); i++) 
 		{
 			WWProfileEntry profileEntry = m_activeProfile.GetDefenceBuilding(i);
 			UpdateBuildingRow(profileEntry);
 			UpdateBuildingNumOwned(profileEntry);
 		}
 		for (int i = 0; i < m_activeProfile.GetNumIncomeBuildings(); i++) 
 		{
 			WWProfileEntry profileEntry = m_activeProfile.GetIncomeBuilding(i);
 			UpdateBuildingRow(profileEntry);
 			UpdateBuildingNumOwned(profileEntry);
 		}
 	}
 	
 	private void UpdateBuildingNumOwned( WWProfileEntry profileEntry )
 	{
 		WWBuilding building = profileEntry.GetBuilding();
 		
 		int numOwned =  profileEntry.GetNumOwned();
 		EditText numOwnedView = building.GetViewNumOwned();
 		String numOwnedString = Integer.toString(numOwned);
 		numOwnedView.setText(numOwnedString);
 		numOwnedView.setTag(profileEntry);
 		
 		Button minusButtonView = building.GetViewMinusButton();
 		if (minusButtonView != null)
 		{
 			minusButtonView.setTag(profileEntry);
 		}
 		
 		Button plusButtonView = building.GetViewPlusButton();
 		if (plusButtonView != null)
 		{
 			plusButtonView.setTag(profileEntry);
 		}
 		
 		Log.i(TAG, "UpdateBuildingNumOwned: "+building.GetName()+" numOwned:"+numOwnedString);
 	}
 	
 	private void UpdateBuildingRow( WWProfileEntry profileEntry )
 	{
 		WWBuilding building = profileEntry.GetBuilding();
 		
 		int numOwned =  profileEntry.GetNumOwned();
 		float value = building.GetValue(numOwned);
 		String valueString = Float.toString(value);
 		TextView valueView = building.GetViewValue();
 		valueView.setText(valueString);
 
 		long currentCost = building.GetCurrentCost(numOwned);
 		String currentCostString = Long.toString(currentCost);
 		TextView currentCostText = building.GetViewCurrentCost();
 		currentCostText.setText(currentCostString);
 		
 		Log.i(TAG, "UpdateBuildingRow: "+building.GetName()+" value:"+valueString+" currentCost:"+currentCostString);
 	}
 
 	private void addRow(TableLayout parent, WWBuilding building, boolean oddRow) 
 	{
 		final float textSize = 14.0f;
 		final int rowHeight = 40;
 		
 		final int padTop = 0;
 		final int padBottom = 0;
 		final int padLeft = 1;
 		final int padRight = 1;
 		
 		final int numOwned = 0;
 		
 		int colour = oddRow ? Color.BLACK : Color.DKGRAY;
 		
 		InputFilter[] filters4 = new InputFilter[1];
 		filters4[0] = new InputFilter.LengthFilter(4);
 
 		InputFilter[] filters8 = new InputFilter[1];
 		filters8[0] = new InputFilter.LengthFilter(8);
 		
 		InputFilter[] filters10 = new InputFilter[1];
 		filters10[0] = new InputFilter.LengthFilter(10);
 
 		InputFilter[] filters13 = new InputFilter[1];
 		filters13[0] = new InputFilter.LengthFilter(13);
 		
 		TableRow row = new TableRow(parent.getContext());
 		row.setPadding(0,0,0,0);
 
 		TextView name = new TextView(row.getContext());
 		name.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
 		name.setTextSize(textSize);
 		name.setMinWidth(116);
 		name.setWidth(116);
 		name.setMaxWidth(116);
 		name.setLines(2);
 		name.setMinLines(2);
 		name.setMaxLines(2);
 		name.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
 		name.setPadding(padLeft,padTop,padRight,padBottom);
 		name.setShadowLayer(1.0f, 2.0f, 2.0f, Color.BLACK);
 		name.setMinHeight(rowHeight);
 		name.setHeight(rowHeight);
 		name.setMaxHeight(rowHeight);
 		name.setBackgroundColor(colour);
 		name.setText(building.GetName());
 		row.addView(name);
 
 		Button minus = new Button(row.getContext());
 		minus.setTextSize(textSize);
 		minus.setMinWidth((int)(textSize*2.0f));
 		minus.setWidth((int)(textSize*2.0f));
 		minus.setMaxWidth((int)(textSize*2.0f));
 		minus.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
 		minus.setPadding(padLeft,padTop,padRight,padBottom);
 		minus.setMinHeight(rowHeight);
 		minus.setHeight(rowHeight);
 		minus.setMaxHeight(rowHeight);
 		minus.setBackgroundDrawable(null);
 		minus.setBackgroundColor(0xFFA0A0A0);
 		minus.setText("-");
 		minus.setOnClickListener(this);
 		minus.setId(123456);
 		building.SetViewMinusButton(minus);
 		row.addView(minus);
 		
 		EditText number = new EditText(row.getContext());
 		number.setInputType(InputType.TYPE_CLASS_NUMBER);
		number.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
 		number.setKeyListener(new DigitsKeyListener());
 		number.setMinWidth((int)(textSize*2.5f));
 		number.setWidth((int)(textSize*2.5f));
 		number.setMaxWidth((int)(textSize*2.5f));
 		number.setMinLines(1);
 		number.setMaxLines(1);
 		number.setFilters(filters4);
 		number.setLines(1);
 		number.setGravity(Gravity.TOP|Gravity.RIGHT);
 		number.setText(Integer.toString(numOwned));
 		number.setPadding(padLeft,padTop,padRight,padBottom);
 		number.setOnKeyListener(this);
 		number.setSelectAllOnFocus(true);
 		number.setMinHeight(rowHeight);
 		number.setHeight(rowHeight);
 		number.setMaxHeight(rowHeight);
 		number.setTextSize(textSize);
		number.setTextColor(0xFFFFFFFF);
 		number.setBackgroundDrawable(null);
		number.setBackgroundColor(0xFF444444);
 		building.SetViewNumOwned(number);
 		row.addView(number);
 
 		Button plus = new Button(row.getContext());
 		plus.setTextSize(textSize);
 		plus.setMinWidth((int)(textSize*2.0f));
 		plus.setWidth((int)(textSize*2.0f));
 		plus.setMaxWidth((int)(textSize*2.0f));
 		plus.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
 		plus.setPadding(padLeft,padTop,padRight,padBottom);
 		plus.setMinHeight(rowHeight);
 		plus.setHeight(rowHeight);
 		plus.setMaxHeight(rowHeight);
 		plus.setBackgroundDrawable(null);
 		plus.setBackgroundColor(0xFFA0A0A0);
 		plus.setText("+");
 		plus.setOnClickListener(this);
 		plus.setId(654321);
 		building.SetViewPlusButton(plus);
 		row.addView(plus);
 		
 		TextView value = new TextView(row.getContext());
 		value.setKeyListener(new DigitsKeyListener());
 		value.setInputType(InputType.TYPE_CLASS_NUMBER);
 		value.setMinWidth((int)(textSize*6));
 		value.setWidth((int)(textSize*6));
 		value.setMaxWidth((int)(textSize*6));
 		value.setLines(1);
 		value.setMinLines(1);
 		value.setMaxLines(1);
 		value.setFilters(filters10);
 		value.setGravity(Gravity.TOP|Gravity.RIGHT);
 		value.setPadding(padLeft,padTop,padRight,padBottom);
 		value.setText(Float.toString(building.GetValue(numOwned)));
 		value.setTextSize(textSize);
 		value.setMinHeight(rowHeight);
 		value.setHeight(rowHeight);
 		value.setMaxHeight(rowHeight);
 		value.setBackgroundColor(colour);
 		building.SetViewValue(value);
 		row.addView(value);
 
 		TextView reward = new TextView(row.getContext());
 		reward.setKeyListener(new DigitsKeyListener());
 		reward.setInputType(InputType.TYPE_CLASS_NUMBER);
 		reward.setMinWidth((int)(textSize*4.5f));
 		reward.setWidth((int)(textSize*4.5f));
 		reward.setMaxWidth((int)(textSize*4.5f));
 		reward.setMinLines(1);
 		reward.setMaxLines(1);
 		reward.setLines(1);
 		reward.setFilters(filters8);
 		reward.setPadding(padLeft,padTop,padRight,padBottom);
 		reward.setGravity(Gravity.TOP|Gravity.RIGHT);
 		reward.setText(Integer.toString(building.GetReward()));
 		reward.setTextSize(textSize);
 		reward.setMinHeight(rowHeight);
 		reward.setHeight(rowHeight);
 		reward.setMaxHeight(rowHeight);
 		reward.setBackgroundColor(colour);
 		row.addView(reward);
 
 		TextView currentCost = new TextView(row.getContext());
 		currentCost.setKeyListener(new DigitsKeyListener());
 		currentCost.setInputType(InputType.TYPE_CLASS_NUMBER);
 		currentCost.setMinWidth((int)(textSize*8.0f));
 		currentCost.setWidth((int)(textSize*8.0f));
 		currentCost.setMaxWidth((int)(textSize*8.0f));
 		currentCost.setMinLines(1);
 		currentCost.setMaxLines(1);
 		currentCost.setLines(1);
 		currentCost.setFilters(filters13);
 		currentCost.setPadding(padLeft,padTop,padRight,padBottom);
 		currentCost.setGravity(Gravity.TOP|Gravity.RIGHT);
 		currentCost.setText(Long.toString(building.GetCurrentCost(numOwned)));
 		currentCost.setTextSize(textSize);
 		currentCost.setMinHeight(rowHeight);
 		currentCost.setHeight(rowHeight);
 		currentCost.setMaxHeight(rowHeight);
 		currentCost.setBackgroundColor(colour);
 		building.SetViewCurrentCost(currentCost);
 		row.addView(currentCost);
 
 		TextView baseCost = new TextView(row.getContext());
 		baseCost.setKeyListener(new DigitsKeyListener());
 		baseCost.setInputType(InputType.TYPE_CLASS_NUMBER);
 		baseCost.setMinWidth((int)(textSize*5.5f));
 		baseCost.setWidth((int)(textSize*5.5f));
 		baseCost.setMaxWidth((int)(textSize*5.5f));
 		baseCost.setMinLines(1);
 		baseCost.setMaxLines(1);
 		baseCost.setLines(1);
 		baseCost.setFilters(filters8);
 		baseCost.setPadding(padLeft,padTop,padRight,padBottom);
 		baseCost.setGravity(Gravity.TOP|Gravity.RIGHT);
 		baseCost.setText(Long.toString(building.GetBaseCost()));
 		baseCost.setTextSize(textSize);
 		baseCost.setMinHeight(rowHeight);
 		baseCost.setHeight(rowHeight);
 		baseCost.setMaxHeight(rowHeight);
 		baseCost.setBackgroundColor(colour);
 		row.addView(baseCost);
 		
 		parent.addView(row);
 		
 		TableRow sep = new TableRow(parent.getContext());
 		sep.setBackgroundColor(Color.WHITE);
 		sep.setPadding(0,1,0,1);
 
 		parent.addView(sep);
 	}
 
 	private String MakeProfileFileName(String profileName)
 	{
 		String profileFileName = PROFILE_HEADER + "_" + PROFILE_VERSION + "_" + profileName;
 		return profileFileName;
 	}
 	
 	private void SaveAllProfiles() 
 	{
 		for (Map.Entry<String,WWProfile> entry: m_profiles.entrySet())
 		{
 			WWProfile profile = entry.getValue();
 			ProfileSave(profile);
 		}
 	}
 	private int LoadAllProfiles() 
 	{
 		int numProfiles = 0;
 		String[] fileNames = fileList();
 		int numFiles = fileNames.length;
 		if (numFiles > 0) 
 		{
 			for (int file = 0; file < numFiles; file++) 
 			{
 				String profileFileName = fileNames[file];
 				String baseProfileFileName = MakeProfileFileName("");
 				if (profileFileName.startsWith(baseProfileFileName))
 				{
 					try 
 					{
 						if (LoadProfile(profileFileName) == true) 
 						{
 							numProfiles++;
 						}
 					} 
 					catch (IOException e) 
 					{
 					}
 				}
 			}
 		}
 		return numProfiles;
 	}
 
 	private boolean LoadProfile(String profileFileName) throws IOException 
 	{
 		try 
 		{
 			TextFileInput inFile = new TextFileInput( openFileInput(profileFileName));
 			try 
 			{
 				// Profile name
 				String name = inFile.ReadString();
 				WWProfile tempProfile = CreateNewProfile(name);
 
 				// Number of each defence building
 				int numDefenceBuildings = inFile.ReadInt();
 				for (int i = 0; i < numDefenceBuildings; i++) 
 				{
 					int number = inFile.ReadInt();
 					tempProfile.SetNumDefenceBuilding(i, number);
 				}
 				// Number of each income building
 				int numIncomeBuildings = inFile.ReadInt();
 				for (int i = 0; i < numIncomeBuildings; i++) 
 				{
 					int number = inFile.ReadInt();
 					tempProfile.SetNumIncomeBuilding(i, number);
 
 				}
 				inFile.Close();
 				ProfileAdd(tempProfile);
 				Log.i(TAG,"LoadProfile DONE:"+name+" file:"+profileFileName);
 			} 
 			catch (IOException e) 
 			{
 				inFile.Close();
 				return false;
 			}
 			return true;
 		} 
 		catch (FileNotFoundException e) 
 		{
 			return false;
 		}
 	}
 
 	private boolean SaveProfile(String profileFileName,WWProfile profile) throws IOException 
 	{
 		try 
 		{
 			TextFileOutput outFile = new TextFileOutput(openFileOutput( profileFileName, MODE_PRIVATE));
 			String name = profile.GetName();
 			try 
 			{
 				// Profile name
 				outFile.WriteString(name);
 
 				// Number of each defence building
 				int numDefenceBuildings = profile.GetNumDefenceBuildings();
 				outFile.WriteInt(numDefenceBuildings);
 				for (int i = 0; i < numDefenceBuildings; i++) 
 				{
 					int number = profile.GetNumDefenceBuilding(i);
 					outFile.WriteInt(number);
 				}
 				// Number of each income building
 				int numIncomeBuildings = profile .GetNumIncomeBuildings();
 				outFile.WriteInt(numIncomeBuildings);
 				for (int i = 0; i < numIncomeBuildings; i++) 
 				{
 					int number = profile.GetNumIncomeBuilding(i);
 					outFile.WriteInt(number);
 				}
 				outFile.Close();
 				Log.i(TAG,"SaveProfile DONE:"+name+" file:"+profileFileName);
 			} 
 			catch (IOException e) 
 			{
 				outFile.Close();
 				return false;
 			}
 			return true;
 		} 
 		catch (FileNotFoundException e) 
 		{
 			return false;
 		}
 	}
 
 	private void ProfileNew() 
 	{
 		String name = "default";
 		WWProfile tempProfile = CreateNewProfile(name);
 		ProfileAdd(tempProfile);
 		
 		Log.i(TAG,"ProfileNew");
 	}
 	private void ProfileDelete() 
 	{
 		if (m_profilesAdapter.getCount() == 0)
 		{
 			return;
 		}
 		String name = m_activeProfile.GetName();
 		m_profiles.remove(name);
 		m_profilesAdapter.remove(name);
 		
 		String profileFileName = MakeProfileFileName(name);
 		if (deleteFile(profileFileName) == false)
 		{
 			Log.i(TAG, "ProfileDelete FAILED:"+profileFileName);
 		}
 		else
 		{
 			Log.i(TAG, "ProfileDelete SUCCESS:"+profileFileName);
 		}
 		
 		// If deleting the last profile then create a default one
 		if (m_profilesAdapter.getCount() == 0)
 		{
 			ProfileNew();
 		}
 		ProfileSelect();
 	}
 
 	private WWProfile CreateNewProfile(String name) 
 	{
 		WWProfile profile = m_profiles.get(name);
 		if (profile == null) 
 		{
 			profile = new WWProfile(name);
 			Log.i(TAG, "CreateNewProfile:" + name);
 		}
 		for (int i = 0; i < m_numDefenceBuildings; i++) 
 		{
 			WWProfileEntry profileEntry = profile.GetDefenceBuilding(i);
 			WWBuilding building = m_defenceBuildings[i];
 			profileEntry.SetBuilding(building);
 		}
 		for (int i = 0; i < m_numIncomeBuildings; i++) 
 		{
 			WWProfileEntry profileEntry = profile.GetIncomeBuilding(i);
 			WWBuilding building = m_incomeBuildings[i];
 			profileEntry.SetBuilding(building);
 		}
 		return profile;
 	}
 
 	private void ProfileRename(String newName)
 	{
 		// Get the new profile name and compare against the active profile name 
 		// Update active profile name if it has changed 
 		// Update m_profileNames, m_profiles, profile drop down list 
 		String oldName = m_activeProfile.GetName();
 		if (m_profiles.containsKey(oldName) == true) 
 		{
 			if (oldName.equals(newName) == false)
 			{
 				Log.i(TAG, "ProfileRename: "+oldName+"->"+newName);
 				//m_profileNames.remove(oldName);
 				m_profilesAdapter.remove(oldName);
 				m_profiles.remove(oldName);
 				m_activeProfile.SetName(newName);
 				ProfileAdd(m_activeProfile);
 			}
 		}
 	}
 	
 	private void ProfileAdd(WWProfile profile) 
 	{
 		String name = profile.GetName();
 		if (m_profiles.containsKey(name) == false) 
 		{
 			//m_profileNames.add(name);
 			m_profiles.put(name, profile);
 			Log.i(TAG,"ProfileAdd:"+name);
 			m_profilesAdapter.add(name);
 		}
 		
 		// Update the selected profile in the drop down list
 		int profileIndex = m_profilesAdapter.getPosition(name);
 		Spinner profileSpinner = (Spinner)findViewById(R.id.profileSpinner);
 		profileSpinner.setSelection(profileIndex);
 		
 		ProfileSelect();
 	}
 
 	private void ProfileSave(WWProfile profile) 
 	{
 		try 
 		{
 			String profileName = profile.GetName();
 			String profileFileName = MakeProfileFileName(profileName);
 			SaveProfile(profileFileName,profile);
 		} 
 		catch (IOException e) 
 		{
 		}
 	}
 
 	private static final String TAG = "WWCALC";
 	private static final String PROFILE_HEADER = "WWProfile";
 	private static final String PROFILE_VERSION = "1";
 
 	public static final int NUM_DEFENCE_BUILDINGS = 6;
 	public static final int NUM_INCOME_BUILDINGS = 8;
 	public static final int DEFAULT_NUM_PROFILES = 8;
 
 	private int m_numDefenceBuildings;
 	private int m_numIncomeBuildings;
 	private WWBuilding[] m_defenceBuildings;
 	private WWBuilding[] m_incomeBuildings;
 	private ArrayAdapter<String> m_profilesAdapter;
 
 	private Map<String, WWProfile> m_profiles;
 	private List<String> m_profileNames;
 	private WWProfile m_activeProfile;
 
 	HorizontalScrollView m_incomeViewHeader;
 	HorizontalScrollView m_incomeViewScroll;
 }
