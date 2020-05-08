 /*
  * VoMinerActivity.java
  * Copyright (C) 2011 Steve "Uru" West <uruwolf@gmail.com>
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
  */
 package com.uruwolf.vominer;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.uruwolf.vominer.data.*;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.*;
 import android.view.View.*;
 import android.widget.*;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 /**
  * The main activity for the app
  * @author Steve "Uru" West <uruwolf@gmail.com>
  */
 public class VoMinerActivity extends Activity implements OnItemSelectedListener, OnClickListener, OnItemClickListener{
 	
 	//Tag to use for debugging
 	public static final String TAG = "Vo-Miner";
 	//Name of the preferences we are to use
 	public static final String PREFS_NAME = "vominer-prefs";
 	
 	//Some preference key values for later
 	//The last selected system name
 	private static final String PREF_LAST_SYSTEM = "last_selected_system";
 	//Last selected letter coord
 	private static final String PREF_LAST_SECTOR_APLHA = "last_selected_alpha";
 	//Last selected numerical coord
 	private static final String PREF_LAST_SECTOR_NUM = "last_selected_num";
 	
 	private SectorDataSource data;
 	//Contains the currently selected sector
 	private Sector currentSector;
 	
 	private List<String> mineralList;
 	private ArrayAdapter<String> mineralAdapter;
 	
 	//This is used when the mineral list is empty. There has to be a element so we make it a blank one.
 	private String emptyMineralString = "";
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         data = new SectorDataSource(this);
         data.open();
         
         //Get the spinners to use later
         Spinner systemList = (Spinner)findViewById(R.id.systemList);
         Spinner gridAplhaList = (Spinner)findViewById(R.id.gridAlphaList);
         Spinner gridNumList = (Spinner)findViewById(R.id.gridNumList);
         
         //Make sure we can update the information when something is selected
         systemList.setOnItemSelectedListener(this);
         gridAplhaList.setOnItemSelectedListener(this);
         gridNumList.setOnItemSelectedListener(this);
         
         //Load up the last selected sector
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         
         //GOGO super uber messy nested method calls and casts!
         systemList.setSelection(((ArrayAdapter<String>) systemList.getAdapter()).getPosition(
         		settings.getString(PREF_LAST_SYSTEM, "")));
         gridAplhaList.setSelection(((ArrayAdapter<String>) gridAplhaList.getAdapter()).getPosition(
         		settings.getString(PREF_LAST_SECTOR_APLHA, "")));
         gridNumList.setSelection(((ArrayAdapter<String>) gridNumList.getAdapter()).getPosition(
         		settings.getString(PREF_LAST_SECTOR_NUM, "")));
         
         //Add a listener to the add button
         ((Button)findViewById(R.id.button_add_mineral)).setOnClickListener(this);
         
         //Add the listener to the mineral list
         ((ListView)findViewById(R.id.oreList)).setOnItemClickListener(this);
         
         //Add the assigned minerals to the list
        	Spinner oreSpinner = (Spinner) findViewById(R.id.mineralList);
        	//Set up the list
        	mineralList = new ArrayList<String>(Static.mineralList);
        	//Add it all in
       	mineralAdapter = new ArrayAdapter<String>(this,
       			android.R.layout.simple_list_item_1,
               	mineralList);
         mineralAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         oreSpinner.setAdapter(mineralAdapter);
     }
     
     /**
      * Show a fancy menu at the top right. Not sure if this will stay or not
      */
 //    @Override
 //    public boolean onCreateOptionsMenu(Menu menu) {
 //        MenuInflater inflater = getMenuInflater();
 //        inflater.inflate(R.menu.main_activity, menu);
 //        return true;
 //    }
     
     @Override
     public void onPause(){
     	super.onPause();
     	//Save the needed preferences
     	SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0).edit();
     	//Load the selected items
     	Sector selected = getSelectedSector();
     	//Dump them all into the right places and save
     	editor.putString(PREF_LAST_SYSTEM, selected.getSystem());
     	editor.putString(PREF_LAST_SECTOR_APLHA, selected.getAplhaCoord());
     	editor.putString(PREF_LAST_SECTOR_NUM, selected.getNumCoord());
     	editor.commit();
     	
     	data.close();
     }
     
     @Override
     public void onResume(){
     	super.onResume();
     	data.open();
     }
     
     public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
     	// Ping the SectorInfoFragment to get it to update
     	//Make sure we have an up-to-date sector
     	refreshCurrentSector();
 
     	setMineralLists(currentSector);
     }
     
     private void refreshCurrentSector(){
     	currentSector = data.populate(getSelectedSector());
     }
     
     /**
      * Gets the currently selected sector
      * @return A Sector containing the information
      */
     public Sector getSelectedSector(){
     	Sector info = new Sector((String) ((Spinner)findViewById(R.id.systemList)).getSelectedItem(),
 				  (String) ((Spinner)findViewById(R.id.gridAlphaList)).getSelectedItem(),
 				  (String) ((Spinner)findViewById(R.id.gridNumList)).getSelectedItem(),
 			      -1, "");
     	
     	return data.populate(info); 
     }
     
     public void onNothingSelected(AdapterView<?> parentView) {
     	// Do nothing
     }
 	
     /**
      * Handles what to do when the add button is clicked.
      * Namely adding the selected mineral to the sector and updating the lists after.
      */
 	public void onClick(View v){
 		Mineral mineral  = new Mineral();
 		String mineralName = (String)((Spinner)findViewById(R.id.mineralList)).getSelectedItem();
 		
 		if(!mineralName.equals(emptyMineralString)){
 			mineral.setMineral(mineralName);
 		
 			data.addMineralToSector(currentSector, mineral);
 			setMineralLists(currentSector);
 		}
 	}
 	
 	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
 		//Get the name of the selected mineral
 		String mineralName = (String) ((ListView)findViewById(R.id.oreList)).getItemAtPosition(pos);
 		data.removeMineralFromSector(currentSector, new Mineral(null, mineralName));
 		
 		refreshCurrentSector();
 		setMineralLists(currentSector);
 	}
 	
 	private void setMineralLists(Sector sector){
 		//Populate the list with the initial data
 		//Remove the ones we don't want
 		mineralList.clear();
 		mineralList.addAll(Static.mineralList);
 		
         //Grab the ore list for later
         ArrayAdapter<String> oreListAdapter = new ArrayAdapter<String>(this,
         		android.R.layout.simple_list_item_1,
         		new ArrayList<String>());
         ((ListView)findViewById(R.id.oreList)).setAdapter(oreListAdapter);
         
         //Add and remove the needed things from the lists 
         for(Mineral mineral : sector.getMinerals()){
         	mineralList.remove(mineral.getMineral());
         	
         	oreListAdapter.add(mineral.getMineral());
         }
         
        //Check to see if the mineral list is empty. If so add the emtpy mineral
         if(mineralList.size() == 0)
         	mineralList.add(emptyMineralString);
         
         mineralAdapter.notifyDataSetChanged();
         oreListAdapter.notifyDataSetChanged();
 	}
 }
