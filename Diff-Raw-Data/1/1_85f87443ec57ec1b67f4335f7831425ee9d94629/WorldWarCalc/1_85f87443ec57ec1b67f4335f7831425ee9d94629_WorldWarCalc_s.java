 package com.zorro666.worldwarcalculator;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.widget.TabHost;
 import android.widget.TabHost.OnTabChangeListener;
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
 
 public class WorldWarCalc extends Activity implements OnKeyListener, OnTouchListener, OnClickListener, OnFocusChangeListener, OnItemSelectedListener, OnTabChangeListener
 {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		Log.i(TAG,"onCreate");
 		super.onCreate(savedInstanceState);
 		
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
 		m_activeProfileName = m_activeProfile.GetName();
 		m_bestBuildingToBuy = "";
 		
 		m_profileNames = new ArrayList<String>();
 		m_profiles = new HashMap<String, WWProfile>(DEFAULT_NUM_PROFILES);
 
 		m_profilesAdapter =  new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_profileNames);
 		
 		setContentView(R.layout.main);
 
 		Button saveProfile = (Button) findViewById(R.id.profileSaveButton);
 		saveProfile.setOnClickListener(this);
 		
 		Button newProfile = (Button) findViewById(R.id.profileNewButton);
 		newProfile.setOnClickListener(this);
 
 		Button deleteProfile = (Button) findViewById(R.id.profileDeleteButton);
 		deleteProfile.setOnClickListener(this);
 
 		//EditText profileName = (EditText)findViewById(R.id.profileName);
 		//profileName.setOnFocusChangeListener(this);
 
 		Spinner profileNameView = (Spinner)findViewById(R.id.profileSpinner);
 		profileNameView.setOnItemSelectedListener(this);
 		
 		Button renameProfile = (Button) findViewById(R.id.profileRenameButton);
 		renameProfile.setOnClickListener(this);
 		
 		// An option would be to subclass it and implement getView function to make it work with WWProfile
 		m_profilesAdapter.setDropDownViewResource(android.R.layout. simple_spinner_dropdown_item);
 		profileNameView.setAdapter(m_profilesAdapter);
 		profileNameView.setSelection(0);
 		
 		TableLayout defenceView = (TableLayout) findViewById(R.id.DefenceView);
 		boolean evenRow;
 		evenRow = true;
 		for (int i = 0; i < m_numDefenceBuildings; i++) 
 		{
 			WWBuilding building = m_defenceBuildings[i];
 			addRow(defenceView, building);
 			
 			TableRow row = building.GetViewRow();
 			SetDefaultRowColours(row,evenRow);
 			evenRow ^= true;
 		}
 
 		TableLayout incomeView = (TableLayout) findViewById(R.id.IncomeViewData);
 		evenRow = true;
 		for (int i = 0; i < m_numIncomeBuildings; i++) 
 		{
 			WWBuilding building = m_incomeBuildings[i];
 			addRow(incomeView, building);
 			
 			TableRow row = building.GetViewRow();
 			SetDefaultRowColours(row,evenRow);
 			evenRow ^= true;
 		}
 
 		m_incomeViewHeader = (HorizontalScrollView)findViewById(R.id.IncomeViewHeader);
 		//m_incomeViewHeader.setOnTouchListener(this);
 
 		m_incomeViewScroll = (HorizontalScrollView)findViewById(R.id.IncomeViewScroll);
 		m_incomeViewScroll.setOnTouchListener(this);
 		m_incomeViewScroll.setSmoothScrollingEnabled(true);
 
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
 		
 		// Create a new default profile to use - do this before the tab listener is set so the tab listener code has a valid profile to work with!
 		ProfileNew();
 		
 		tabs.setOnTabChangedListener(this);
 		tabs.setCurrentTab(1);
 	}
 	
 	@Override
 	public void onStart()
 	{
 		Log.i(TAG,"onStart");
 		super.onStart();
 		
 		LoadAllProfiles();
 	}
 
 	@Override
 	// The activity comes to the foreground from being stopped (from onStop state)
 	public void onRestart()
 	{
 		Log.i(TAG,"onRestart");
 		super.onRestart();
 	}
 
 	// The activity comes to the foreground from being paused (from onPause state)
 	@Override
 	public void onResume()
 	{
 		Log.i(TAG,"onResume");
 		super.onResume();
 		
 		Log.i(TAG,"onResume LoadAppState");
 		LoadAppState();
 
 		// Set selected item to match the active profile
 		if (m_profilesAdapter.getCount() == 0)
 		{
 			ProfileNew();
 		}
 
 		ProfileSetSelectedProfile(m_activeProfileName);
 	}
 	
 	// Another application comes in front of this view
 	@Override
 	public void onPause()
 	{
 		Log.i(TAG,"onPause");
 		super.onPause();
 		
 		// Save state in the onSaveInstanceState() override
 		// Restore state in the onRestoreInstanceState() override
 		// BAD BAD BAD BAD BAD
 		SaveAllProfiles();
 		SaveAppState();
 	}
 
 	// Application is no longer in view
 	@Override
 	public void onStop()
 	{
 		Log.i(TAG,"onStop");
 		super.onStop();
 	}
 	
 	// Application is being destroyed
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 		
 		Log.i(TAG,"onDestroy");
 		SaveAllProfiles();
 		SaveAppState();
 	}
 	
 	public boolean onTouch(View v, MotionEvent event) 
 	{
 		if (v == m_incomeViewScroll) 
 		{
 			m_incomeViewHeader.onTouchEvent(event);
 		}
 		if (v == m_incomeViewHeader) 
 		{
 			//m_incomeViewScroll.onTouchEvent(event);
 		}
 		return v.onTouchEvent(event);
 	}
 
 	public void onFocusChange(View v, boolean hasFocus) 
 	{ 
 		if (v.getId()==R.id.profileName) 
 		{ 
 			if (hasFocus == false) 
 			{ 
 //				EditText profileNameView = (EditText)v;
 //				String newName = profileNameView.getText().toString();
 //				ProfileRename(newName);
 			} 
 		}
 	}
 	public void onTabChanged(String tabId)
 	{
 		if (m_activeProfile != null)
 		{
 			UpdateDisplay();
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
 		else if (v.getId() == R.id.profileRenameButton)
 		{
 			EditText profileNameView = (EditText)findViewById(R.id.profileName);
 			String newName = profileNameView.getText().toString();
 			ProfileRename(newName);
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
 						UpdateDisplay();
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
 				UpdateDisplay();
 
 				Log.i(TAG, "onKey Building = " + profileEntry.GetBuilding().GetName() + " numOwned = " + numOwned+ " key="+key);
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
 				Log.i(TAG,"onItemSelected["+position+"] = "+profileName);
 				ProfileSelect();
 			}
 		}
 	}
 	private void ProfileSetSelectedProfile(String profileName)
 	{
 		Spinner profileSpinner = (Spinner)findViewById(R.id.profileSpinner);
 		int itemIndex = m_profilesAdapter.getPosition(profileName);
 		int numItems = profileSpinner.getCount();
 		if ((itemIndex < 0) || (itemIndex >= numItems))
 		{
 			Log.i(TAG,"ProfileSetSelectedProfile:"+profileName+" NOT FOUND");
 			return;
 		}
 		Log.i(TAG,"ProfileSetSelectedProfile:"+profileName+" "+itemIndex);
 		profileSpinner.setSelection(itemIndex);
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
 		ProfileSelectByName(profileName);
 	}
 	
 	void ProfileSelectByName(String profileName)
 	{
 		Log.i(TAG,"ProfileSelectByName = "+profileName);
 		WWProfile profile = m_profiles.get(profileName);
 		// Need to error check this in case it returns NULL
 		if (profile == null)
 		{
 			Log.i(TAG,"ProfileSelectByName: NULL = "+profileName);
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
 		m_activeProfileName = m_activeProfile.GetName();
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
 		UpdateDisplay();
 	}
 	private void HighlightBestBuildingToBuy(String bestBuildingToBuy)
 	{
 		final TabHost tabs = (TabHost)findViewById(R.id.tabhost);
 		String tabName = tabs.getCurrentTabTag();
 
 		if (tabName.equals("Income"))
 		{
 			boolean evenRow;
 			evenRow = true;
 			for (int i = 0; i < m_activeProfile.GetNumIncomeBuildings(); i++) 
 			{
 				WWProfileEntry profileEntry = m_activeProfile.GetIncomeBuilding(i);
 				WWBuilding building = profileEntry.GetBuilding();
 				
 				if (building != null)
 				{
 					TableRow row = building.GetViewRow();
 					//Reset colours to normal or highlight if this is the best building to buy
 					boolean highlight = (building.GetName().equals(bestBuildingToBuy));
 					SetRowColours(row,evenRow,highlight);
 				}
 				evenRow ^= true;
 			}
 		}
 		else if (tabName.equals("Defence"))
 		{
 			boolean evenRow;
 			evenRow = true;
 			for (int i = 0; i < m_activeProfile.GetNumDefenceBuildings(); i++) 
 			{
 				WWProfileEntry profileEntry = m_activeProfile.GetDefenceBuilding(i);
 				WWBuilding building = profileEntry.GetBuilding();
 				
 				if (building != null)
 				{
 					TableRow row = building.GetViewRow();
 					//Highlight if this is the best building to buy
 					//Reset colours to normal or highlight if this is the best building to buy
 					boolean highlight = (building.GetName().equals(bestBuildingToBuy));
 					SetRowColours(row,evenRow,highlight);
 				}
 				evenRow ^= true;
 			}
 		}
 	}
 	private void UpdateDisplay()
 	{
 		UpdateHintText();
 		// Now highlight the best buy row in bold & red
 		HighlightBestBuildingToBuy(m_bestBuildingToBuy);
 	}
 	private void UpdateHintText()
 	{
 		final TabHost tabs = (TabHost)findViewById(R.id.tabhost);
 		String tabName = tabs.getCurrentTabTag();
 		Log.i(TAG,"tabName="+tabName);
 
 		TextView hintView = (TextView)findViewById(R.id.hintText);
 		String hintText = "";
 		String bestBuildingToBuy = "";
 		if (tabName.equals("Income"))
 		{
 			m_activeProfile.SortIncomeCheapness();
 			WWProfileEntry cheapestIncome = m_activeProfile.GetSortedIncomeEntry(0);
 			WWBuilding cheapestIncomeBuilding = cheapestIncome.GetBuilding();
 			if (cheapestIncomeBuilding != null)
 			{
 				bestBuildingToBuy = cheapestIncomeBuilding.GetName();
 				int numToBuy = m_activeProfile.GetIncomeNumToBuy(0);
 				hintText = "Buy " + numToBuy + " " + bestBuildingToBuy;
 			}
 			cheapestIncome = m_activeProfile.GetSortedIncomeEntry(1);
 			cheapestIncomeBuilding = cheapestIncome.GetBuilding();
 			if (cheapestIncomeBuilding != null)
 			{
 				int numToBuy = m_activeProfile.GetIncomeNumToBuy(1);
 				hintText += "\n";
 				hintText += "Buy " + numToBuy + " " + cheapestIncomeBuilding.GetName();
 			}
 		}
 		else if (tabName.equals("Defence"))
 		{
 			m_activeProfile.SortDefenceCheapness();
 			WWProfileEntry cheapestDefence = m_activeProfile.GetSortedDefenceEntry(0);
 			WWBuilding cheapestDefenceBuilding = cheapestDefence.GetBuilding();
 			if (cheapestDefenceBuilding != null)
 			{
 				bestBuildingToBuy = cheapestDefenceBuilding.GetName();
 				int numToBuy = m_activeProfile.GetDefenceNumToBuy(0);
 				hintText += "Buy " + numToBuy + " " + bestBuildingToBuy;
 			}
 			cheapestDefence = m_activeProfile.GetSortedDefenceEntry(1);
 			cheapestDefenceBuilding = cheapestDefence.GetBuilding();
 			if (cheapestDefenceBuilding != null)
 			{
 				int numToBuy = m_activeProfile.GetDefenceNumToBuy(1);
 				hintText += "\n";
 				hintText += "Buy " + numToBuy + " " + cheapestDefenceBuilding.GetName();
 			}
 		}
 		else if (tabName.equals("Profile"))
 		{
 			hintText ="";
 			hintText += "A:" + m_activeProfile.GetName();
 			hintText += " N:" + m_activeProfileName;
 			Spinner profileSpinner = (Spinner)findViewById(R.id.profileSpinner);
 			String itemName = (String)profileSpinner.getSelectedItem();
 			if (itemName != null)
 			{
 				hintText += "\nS:" + itemName;
 			}
 		}
 		else
 		{
 			hintText = "Unknown";
 		}
 		Log.i(TAG,"UpdateHintText:"+hintText);
 		m_bestBuildingToBuy = bestBuildingToBuy;
 		hintView.setText(hintText);
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
 		float cheapness = building.GetCheapness(numOwned);
 		String cheapnessString = Float.toString(cheapness);
 		TextView cheapnessView = building.GetViewCheapness();
 		cheapnessView.setText(cheapnessString);
 
 		long currentCost = building.GetCurrentCost(numOwned);
 		String currentCostString = Long.toString(currentCost);
 		TextView currentCostText = building.GetViewCurrentCost();
 		currentCostText.setText(currentCostString);
 		
 		Log.i(TAG, "UpdateBuildingRow: "+building.GetName()+" cheapness:"+cheapnessString+" currentCost:"+currentCostString);
 	}
 
 	private void SetDefaultRowColours(TableRow row,boolean evenRow)
 	{
 		SetRowColours(row,evenRow,false);
 	}
 	private void SetRowColours(TableRow row,boolean evenRow,boolean highlight)
 	{
 		int colour = evenRow ? Color.BLUE : Color.DKGRAY;
 		colour = highlight ? Color.RED : colour;
 		int numChildren = row.getChildCount();
 		for (int i=0; i<numChildren; i++)
 		{
 			View element = row.getChildAt(i);
 			int id = element.getId();
 			if ((id == 123456) || (id == 654321))
 			{
 				// Special case for the minus & plus buttons
 				element.setBackgroundColor(0xFFA0A0A0);
 			}
 			else if (id == 2468)
 			{
 				if (element.getClass() == EditText.class)
 				{
 					EditText number = (EditText)element;
 					if (highlight == false)
 					{
 						// Special case for the number text default non-highlight colour
 						number.setBackgroundColor(colour);
 						number.setTextColor(Color.WHITE);
 						number.setHighlightColor(Color.RED);
 					}
 					else
 					{
 						number.setBackgroundColor(colour);
 						number.setTextColor(Color.WHITE);
 						number.setHighlightColor(Color.BLACK);
 					}
 				}
 			}
 			else
 			{
 				element.setBackgroundColor(colour);
 			}
 		}
 	}
 	private void addRow(TableLayout parent, WWBuilding building) 
 	{
 		final float textSize = 14.0f;
 		final int rowHeight = 40;
 		
 		final int padTop = 0;
 		final int padBottom = 0;
 		final int padLeft = 1;
 		final int padRight = 1;
 		
 		final int numOwned = 0;
 		
 		int colour = Color.BLUE;
 		
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
 		building.SetViewRow(row);
 
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
 		number.setMinWidth((int)(textSize*3.5f));
 		number.setWidth((int)(textSize*3.5f));
 		number.setMaxWidth((int)(textSize*3.5f));
 		number.setMinLines(1);
 		number.setMaxLines(1);
 		number.setFilters(filters4);
 		number.setLines(1);
 		number.setGravity(Gravity.TOP|Gravity.RIGHT);
 		number.setText(Integer.toString(numOwned));
 		number.setPadding(10,padTop,10,padBottom);
 		number.setOnKeyListener(this);
 		number.setSelectAllOnFocus(true);
 		number.setMinHeight(rowHeight);
 		number.setHeight(rowHeight);
 		number.setMaxHeight(rowHeight);
 		number.setTextSize(textSize);
 		number.setTextColor(0xFFFFFFFF);
 		number.setBackgroundDrawable(null);
 		number.setBackgroundColor(0xFF444444);
 		number.setId(2468);
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
 		
 		TextView cheapness = new TextView(row.getContext());
 		cheapness.setKeyListener(new DigitsKeyListener());
 		cheapness.setInputType(InputType.TYPE_CLASS_NUMBER);
 		cheapness.setMinWidth((int)(textSize*5));
 		cheapness.setWidth((int)(textSize*5));
 		cheapness.setMaxWidth((int)(textSize*5));
 		cheapness.setLines(1);
 		cheapness.setMinLines(1);
 		cheapness.setMaxLines(1);
 		cheapness.setFilters(filters10);
 		cheapness.setGravity(Gravity.TOP|Gravity.RIGHT);
 		cheapness.setPadding(padLeft,padTop,padRight,padBottom);
 		cheapness.setText(Float.toString(building.GetCheapness(numOwned)));
 		cheapness.setTextSize(textSize);
 		cheapness.setMinHeight(rowHeight);
 		cheapness.setHeight(rowHeight);
 		cheapness.setMaxHeight(rowHeight);
 		cheapness.setBackgroundColor(colour);
 		building.SetViewCheapness(cheapness);
 		row.addView(cheapness);
 
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
 	
 	private boolean SaveAppState() 
 	{
 		String fileName = "AppState";
 		try 
 		{
 			TextFileOutput outFile = new TextFileOutput(openFileOutput( fileName, MODE_PRIVATE));
 			try 
 			{
 				// Active profile
 				String activeProfile = m_activeProfileName;
 				outFile.WriteString(activeProfile);
 				Log.i(TAG,"SaveAppState activeProfile:"+activeProfile);
 				
 				outFile.Close();
 				Log.i(TAG,"SaveAppState DONE file:"+fileName);
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
 		catch (IOException e) 
 		{
 			return false;
 		}
 	}
 		
 	private boolean LoadAppState() 
 	{
 		String fileName = "AppState";
 		try 
 		{
 			TextFileInput inFile = new TextFileInput(openFileInput(fileName));
 			try 
 			{
 				// Active profile
 				String activeProfile = inFile.ReadString();
 				Log.i(TAG,"LoadAppState activeProfile:"+activeProfile);
 				
 				inFile.Close();
 				Log.i(TAG,"LoadAppState DONE file:"+fileName);
 				
 				m_activeProfileName = activeProfile;
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
 			Log.i(TAG,"LoadAppState FileNotFound:"+fileName);
 			return false;
 		}
 		catch (IOException e) 
 		{
 			return false;
 		}
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
 		Log.i(TAG,"LoadAllProfiles");
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
 					if (LoadProfile(profileFileName) == true) 
 					{
 						numProfiles++;
 					}
 				}
 			}
 		}
 		return numProfiles;
 	}
 
 	private boolean LoadProfile(String profileFileName)
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
 		catch (IOException e) 
 		{
 			return false;
 		}
 	}
 
 	private boolean SaveProfile(String profileFileName,WWProfile profile) 
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
 		catch (IOException e) 
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
 				m_activeProfileName = m_activeProfile.GetName();
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
 		String profileName = profile.GetName();
 		String profileFileName = MakeProfileFileName(profileName);
 		SaveProfile(profileFileName,profile);
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
 	private String m_activeProfileName;
 	private String m_bestBuildingToBuy;
 
 	HorizontalScrollView m_incomeViewHeader;
 	HorizontalScrollView m_incomeViewScroll;
 }
