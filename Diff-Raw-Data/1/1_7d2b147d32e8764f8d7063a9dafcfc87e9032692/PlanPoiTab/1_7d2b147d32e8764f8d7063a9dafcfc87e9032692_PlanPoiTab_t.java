 /**
  * @contributor(s): Kristian Greve Hagen (NTNU), Jacqueline Floch (SINTEF), Rune Sætre (NTNU)
  *
  * Copyright (C) 2011-2012 UbiCompForAll Consortium (SINTEF, NTNU)
  * for the UbiCompForAll project
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  * 
  */
 
 package org.ubicompforall.cityexplorer.gui;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.TreeMap;
 
 
 import org.ubicompforall.cityexplorer.CityExplorer;
 import org.ubicompforall.cityexplorer.R;
 import org.ubicompforall.cityexplorer.data.DBFactory;
 import org.ubicompforall.cityexplorer.data.DatabaseInterface;
 import org.ubicompforall.cityexplorer.data.DatabaseUpdater;
 import org.ubicompforall.cityexplorer.data.IntentPassable;
 import org.ubicompforall.cityexplorer.data.Poi;
 import org.ubicompforall.cityexplorer.data.PoiAdapter;
 import org.ubicompforall.cityexplorer.data.SeparatedListAdapter;
 import org.ubicompforall.cityexplorer.data.Sharing;
 import org.ubicompforall.cityexplorer.data.Trip;
 import org.ubicompforall.cityexplorer.map.MapsActivity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Adapter;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 /**
  * @description:
  * This class handles all the action going on in the locations tab.
  */
 
 public class PlanPoiTab extends PlanActivityTab implements OnMultiChoiceClickListener, DialogInterface.OnClickListener, OnItemSelectedListener{
 
 	/** Field containing the String of the category settings, used in shared preferences. */
 	private static String CATEGORY_SETTINGS = "catset";
 
 	/*** Field containing the list of all favorite pois. */
 	private ArrayList<Poi> favouriteList = new ArrayList<Poi>();
 
 	/*** Field containing all pois.*/
 	private ArrayList<Poi> allPois = new ArrayList<Poi>();
 
 	/*** Field containing the adapter for favorite pois.*/
 	private PoiAdapter favouriteAdapter;
 
 	/*** Field containing this activity's resources.*/
 	private Resources res;
 
 	/*** Field containing the {@link SeparatedListAdapter} that holds all the other adapters.*/
 	private SeparatedListAdapter adapter;
 
 	/*** Field containing this activity's {@link ListView}.*/
 	private ListView lv;
 
 	/*** Field containing the users current location.*/
 	private Location userLocation;
 
 	/*** Field containing an {@link LinkedList} of the categories.*/
 	private LinkedList<String> categories;
 
 	/*** Field containing this activity's context.*/
 	private Activity context;
 
 	/*** Field containing a {@link HashMap} for the checked categories in the filter.*/
 	private TreeMap<String, Boolean> CheckedCategories = new TreeMap<String, Boolean>();
 
 	/*** Field containing the request code from other activities.*/
 	private int requestCode;
 
 	/*** Field containing a single poi.*/
 	private Poi poi;
 
 	/*** Field containing pois you want to share.*/
 	private ArrayList<Poi> sharePois;
 
 	/*** Field containing pois you want to download.*/
 	private ArrayList<Poi> downloadedPois;
 
 	/*** Field giving access to databaseUpdater methods.*/
 	private DatabaseUpdater du;
 
 	/*** Field containing pois you have selected for adding to trip.*/
 	private ArrayList<Poi> selectedPois;
 
 	/*** Field containing a single trip.*/
 	private Trip trip;
 
 	/*** Remember whether the data is saved or not. */
 	private boolean saved;
 
 	//private boolean menu_shown;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);		
 		debug(2, "");
 		saved = true;
 		//menu_shown = false;
 		init();
		userLocation = StartActivity.verifyUserLocation( userLocation, this );
 	} //onCreate
 
 	@Override
 	public void onStart(){
 		super.onStart();
 		debug(2, "PlanTabPoi Start");
 	}//onStart
 	
 	@Override
 	protected void onResume() {
 		debug(2,"");
 		super.onResume();
 		if(requestCode != CityExplorer.REQUEST_DOWNLOAD_POI){			
 			updateSections();
 			adapter.notifyDataSetChanged();
 		}//if not download-mode
 	}//onResume
 
 //	if ( requestCode == 0 && ! menu_shown ){ //Main menu // Default OFF now
 //	//openOptionsMenu(); // Crashes.... Postpone 1000 ms until ready
 //	debug(1, "requestCode is "+requestCode );
 //	new Handler().postDelayed(new Runnable() {
 //		public void run() {
 //			openOptionsMenu();
 //		}
 //	}, 1000);
 //}
 
 	
 	private void debug( int level, String message ) {
 		CityExplorer.debug( level, message );		
 	} //debug
 
 
 	/**
 	 * Initializes the activity.
 	 */
 	private void init() {
 		context = this;
 		requestCode = getIntent().getIntExtra("requestCode",0);
 		DatabaseInterface dbInstance = DBFactory.getInstance(this);
 		lv = getListView();
 		if (requestCode == CityExplorer.CHOOSE_POI || 
 				requestCode == CityExplorer.REQUEST_SHOW_POI_NAME ||
 				requestCode == PlanTripTab.ADD_TO_TRIP || 
 				requestCode == TripListActivity.ADD_TO_TRIP ||
 // JF: Support for sharing removed (do not work properly)
 //				requestCode == CityExplorer.REQUEST_SHARE_POI ||
 				requestCode == CityExplorer.REQUEST_DOWNLOAD_POI){
 			lv.setOnItemLongClickListener(null);
 			debug(0, "requestCode is "+requestCode );
 		}else {			
 			debug(0, "requestCode is "+requestCode );
 			lv.setOnItemLongClickListener(new DrawPopup());
 		}
 		if( requestCode == CityExplorer.REQUEST_DOWNLOAD_POI ){
 			du = new DatabaseUpdater(this);
 			allPois = du.getInternetPois();
 			adapter = new SeparatedListAdapter(this, SeparatedListAdapter.INTERNET_POIS);
 		}else if( requestCode == CityExplorer.REQUEST_SHOW_POI_NAME ){
 			allPois = dbInstance.getAllPois();
 			debug(0, "Found new pois, filter for name from: "+allPois.size() );
 			//TODO: start activity PoiDetailsActivity
 			allPois = filterAllPoisName( getIntent().getStringExtra("name") ); //allPois = 
 			debug(0, "Found new pois, filtered for name is "+allPois.size() );
 			adapter = new SeparatedListAdapter(this, SeparatedListAdapter.INTERNET_POIS);
 		}else{
 			allPois = dbInstance.getAllPois();
 			adapter = new SeparatedListAdapter(this, SeparatedListAdapter.POI_LIST);
 		}
 		if(requestCode == PlanTripTab.ADD_TO_TRIP || requestCode == TripListActivity.ADD_TO_TRIP){		
 			trip = (Trip) getIntent().getParcelableExtra(IntentPassable.TRIP);		
 		}
 		res = getResources();
 		categories = dbInstance.getUniqueCategoryNames();
 		//Collections.sort(categories);
 		buildFilter();
 		makeSections();
 		lv.setAdapter(adapter);
 	}//init
 
 
 	private ArrayList<Poi> filterAllPoisName( String name ) {
 		debug(-1, "Name was "+name );
 		if (name != null){
 			ArrayList<Poi> filtered = new ArrayList<Poi>();
 			for (Poi poi : allPois){
 				if (poi.getLabel() != null && poi.getLabel().contains( name )){
 					filtered.add(poi);
 				}else if (poi.toString() != null && poi.toString().contains( name )){
 					debug(0, "Found: "+poi );
 					filtered.add(poi);
 				}else {
 					debug(0, "Looking for "+name+", Missed: "+poi.getLabel() );
 				}
 			}
 			if (filtered.size() >0){
 				return filtered;
 			}else{
 				Toast.makeText(this, "No poi found with name "+name, Toast.LENGTH_LONG).show();
 			}
 		}
 		return allPois;
 	}// filteredAllPois
 
 	/**
 	 * Makes the category sections that is shown in the POI list. 
 	 */
 	private void makeSections(){
 		debug(2, "make sections" );
 		favouriteAdapter = new PoiAdapter(this, R.layout.plan_listitem, favouriteList);
 		if(requestCode != CityExplorer.REQUEST_DOWNLOAD_POI){			
 			adapter.addSection(CityExplorer.FAVORITES, favouriteAdapter);
 		}
 		for (Poi poi : allPois){
 			if(poi.isFavorite()){ //add to favorite section
 				favouriteList.add(poi);
 				favouriteAdapter.notifyDataSetChanged();
 			}
 			if( !adapter.getSectionNames().contains(poi.getCategory() ) ){ //category does not exist, create it.
 				ArrayList<Poi> list = new ArrayList<Poi>();
 
 				PoiAdapter testAdapter;
 				testAdapter = new PoiAdapter(this, R.layout.plan_listitem, list);
 				adapter.addSection(poi.getCategory(), testAdapter);
 			}
 			((PoiAdapter)adapter.getAdapter(poi.getCategory())).add(poi);//add to the correct section
 			((PoiAdapter)adapter.getAdapter(poi.getCategory())).notifyDataSetChanged();
 		}
 	}//makeSections
 
 	/**
 	 * Updates the category sections in the list, e.g. after choosing filtering.
 	 */
 	private void updateSections(){
 		debug(2, "UPDATE Sections" );
 		if (allPois == null){
 			allPois = DBFactory.getInstance(this).getAllPois();
 		}
 		LinkedList<String> sectionsInUse = new LinkedList<String>(); 
 		for (Poi poi : allPois)		{
 			//ignore sections that are turned off:
 			if( CheckedCategories.keySet().contains( poi.getCategory() ) ){
 				if( !CheckedCategories.get( poi.getCategory() ) ){ //this section is turned off:
 					//debug(0, "Skipping");
 					continue;
 				}
 			}
 			sectionsInUse.add(poi.getCategory());
 			if ( poi.isFavorite() && CheckedCategories.get( CityExplorer.FAVORITES ) ){ // filter.fav.isSelected and in use){
 				sectionsInUse.add( CityExplorer.FAVORITES );
 			}
 			if(!adapter.getSectionNames().contains(poi.getCategory() ) ){
 				ArrayList<Poi> list = new ArrayList<Poi>();
 				list.add(poi);
 				PoiAdapter testAdapter = new PoiAdapter( this, R.layout.plan_listitem, list);
 				adapter.addSection(poi.getCategory(), testAdapter);
 			}//if contains category
 		}//for POIs
 
 		@SuppressWarnings("unchecked")
 		LinkedList<String> listSections = (LinkedList<String>) adapter.getSectionNames().clone();
 		//LinkedList<String> ListSections;// = (LinkedList<String>) adapter.getSectionNames().clone();
 		for( String sec : listSections ){
 			if( !sectionsInUse.contains(sec)
 				//&& !sec.equalsIgnoreCase(CityExplorer.FAVORITES)
 				//&& !sec.equalsIgnoreCase(CityExplorer.ALL) 
 			){
 				adapter.removeSection(sec);
 			}
 		}//for sections
 		lv.setAdapter(adapter);
 	}//updateSections
 
 
 	/**
 	 * Builds the filter list.
 	 */
 	private void buildFilter(){
 		//Set checked or not for "CheckedCategories"
 		SharedPreferences settings = getSharedPreferences(CATEGORY_SETTINGS, 0);
 		for ( String cat : categories ){
 			boolean checked = settings.getBoolean(cat, true);
 			CheckedCategories.put( cat, checked );
 		}
 	}//buildFilter
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 		//menu_shown = true;
 		debug(2, "REQUEST CODE is "+requestCode );
 		if (requestCode == CityExplorer.CHOOSE_POI){	
 			menu.removeItem(R.id.planMenuNewPoi);
 // JF: support for sharing removed (do not work properly)
 //			menu.removeItem(R.id.planMenuSharePois);
 			//menu.removeItem(R.id.planMenuUpdatePois);
 			menu.removeItem(R.id.planMenuAddPois);
 // JF: Support for sharing removed (do not work properly)
 //		}else if (requestCode == CityExplorer.REQUEST_SHARE_POI){	
 //			menu.removeItem(R.id.planMenuAddPois);
 //			menu.removeItem(R.id.planMenuNewPoi);
 //			//menu.removeItem(R.id.planMenuUpdatePois);
 		}else if(requestCode == CityExplorer.REQUEST_DOWNLOAD_POI){
 			menu.removeItem(R.id.planMenuAddPois);
 			menu.removeItem(R.id.planMenuNewPoi);
 // JF: Support for sharing removed (do not work properly)
 //			menu.removeItem(R.id.planMenuSharePois);
 			menu.removeItem(R.id.planMenuFilter);
 		}else if(requestCode == PlanTripTab.ADD_TO_TRIP  || requestCode == TripListActivity.ADD_TO_TRIP){
 			menu.removeItem(R.id.planMenuNewPoi);
 // JF: Support for sharing removed (do not work properly)
 //			menu.removeItem(R.id.planMenuSharePois);
 			//menu.removeItem(R.id.planMenuUpdatePois);
 		}else{
 			menu.removeItem(R.id.planMenuAddPois);
 		}//if - else - type of menu
 
 		return true;
 	}//onPrepareOptionsMenu
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu){
 		super.onCreateOptionsMenu(menu);
 		menu.setGroupVisible(R.id.planMenuGroupTrip, false);
 		return true;
 	}//onCreateOptionsMenu
 
 	/***
 	 * Selection in the options menu
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		debug(1, "item is "+item );
 		
 		if(item.getItemId() == R.id.planMenuMap){
 			setProgressBarVisibility(true);
 			StartActivity.exploreCity (this);
 		}
 
 		if(item.getItemId() == R.id.planMenuNewPoi){
 			Intent newPoi = new Intent(PlanPoiTab.this, NewPoiActivity.class);
 			CityExplorer.showProgressDialog( this, "Making new POI");
 			startActivity(newPoi);
 		}
 
 		if(item.getItemId() == R.id.planMenuFilter){
 			categories = DBFactory.getInstance(this).getUniqueCategoryNames();
 			debug(0, "Categories are "+categories );
 
 			AlertDialog.Builder alert = new AlertDialog.Builder(this);
 			alert.setTitle("Filter");
 			boolean[] CheckedCat = new boolean[categories.size()];
 			for (String c : categories){
 				if(CheckedCategories.get(c) == null){
 					CheckedCategories.put(c, true);
 				}
 				CheckedCat[categories.indexOf(c)] = CheckedCategories.get(c);
 			}
 
 			String[] array = new String[categories.size()];
 			array = categories.toArray(array);
 			
 			alert.setOnItemSelectedListener( this );
 			alert.setMultiChoiceItems( array, CheckedCat, this);
 			alert.setPositiveButton( R.string.label_all, this);
 			alert.setNeutralButton(R.string.label_select, this);
 			alert.setNegativeButton( R.string.label_none, this );
 			alert.create();
 			alert.show();
 		}//onOptionsItemSelected
 
 // JF: Support for sharing removed (do not work properly)
 //		if(item.getItemId() == R.id.planMenuSharePois){
 //			if(requestCode == CityExplorer.REQUEST_SHARE_POI){
 //				if (sharePois==null){
 //					Toast.makeText(this, "No locations selected", Toast.LENGTH_LONG).show();
 //					return false;
 //				}else {
 //					Sharing.send(this, sharePois);
 //					sharePois = null;
 //				}
 //				finish();
 //			}else {
 //				Intent sharePoi= new Intent(PlanPoiTab.this, PlanPoiTab.class);
 //				sharePoi.putExtra("requestCode", CityExplorer.REQUEST_SHARE_POI);
 //				startActivityForResult(sharePoi, CityExplorer.REQUEST_SHARE_POI);
 //			}
 //		}//if sharePOIs selected in menu
 
 		if(item.getItemId() == R.id.planMenuAddPois){
 			if ( ! saveAndFinish() ){
 				return false;	//No valid save was performed
 			}
 		}//if AddPois selected in menu
 		return true;
 	}//onOptionItemSelected
 
 	private boolean saveAndFinish(){
 		if(requestCode == PlanTripTab.ADD_TO_TRIP || requestCode == TripListActivity.ADD_TO_TRIP){
 			if (selectedPois==null){
 				Toast.makeText(this, "No locations selected", Toast.LENGTH_LONG).show();
 				//return false; // Moved to bottom
 			}else {
 				for (Poi p : selectedPois) {
 					trip.addPoi(p);
 					DBFactory.getInstance(this).addPoiToTrip(trip, p);						
 				}
 				Toast.makeText(this, selectedPois.size() + " locations added to " + trip.getLabel() + ".", Toast.LENGTH_LONG).show();
 				selectedPois = null;
 			}
 			Intent resultIntent = new Intent();
 			resultIntent.putExtra(IntentPassable.TRIP, trip);
 			setResult( Activity.RESULT_OK, resultIntent );
 			finish();
 		}//if "Add Pois to trip activity" requested results from us
 		return false; //Default is "failed to save"
 	}//saveAndFinish
 
 	@Override
 	public void onListItemClick(ListView l, View v, int pos, long id) {
 		debug(0, "RequestCode is "+ requestCode +". Choose_POI="+CityExplorer.CHOOSE_POI+"..." );
 		if(l.getAdapter().getItemViewType(pos) == SeparatedListAdapter.TYPE_SECTION_HEADER){
 			//Pressing a header			
 			return;
 		}
 		Poi p = (Poi) l.getAdapter().getItem(pos);
 
 		if (requestCode == CityExplorer.CHOOSE_POI){
 			Intent resultIntent = new Intent();
 			resultIntent.putExtra(IntentPassable.POI, p);
 			setResult( Activity.RESULT_OK, resultIntent );
 			finish();
 			return;
 		}
 
 		if (requestCode == PlanTripTab.ADD_TO_TRIP || requestCode == TripListActivity.ADD_TO_TRIP){
 			if(selectedPois == null){				
 				selectedPois = new ArrayList<Poi>();
 			}
 			if(!selectedPois.contains(p)){
 				v.setBackgroundColor(0xff9ba7d5);
 				selectedPois.add(p);
 				saved = false;
 			}else {
 				v.setBackgroundColor(Color.TRANSPARENT);
 				selectedPois.remove(p);
 				if ( selectedPois.size()==0 ){
 					saved=true;
 				}//if nothing to add
 			}//if select, else unselect
 			return;
 		}//if adding pois to save to some other activity
 
 // JF: Support for sharing removed (do not work properly)
 //		if (requestCode == CityExplorer.REQUEST_SHARE_POI){
 //			if(sharePois == null){				
 //				sharePois = new ArrayList<Poi>();
 //			}
 //			if(!sharePois.contains(p)){
 //				v.setBackgroundColor(0xff9ba7d5);
 //				sharePois.add(p);
 //			}else {
 //				v.setBackgroundColor(Color.TRANSPARENT);
 //				sharePois.remove(p);
 //			}
 //			return;
 //		}
 
 		if (requestCode == CityExplorer.REQUEST_DOWNLOAD_POI){
 
 			if(downloadedPois == null){				
 				downloadedPois = new ArrayList<Poi>();
 			}
 			if(!downloadedPois.contains(p)){
 				v.setBackgroundColor(0xff9ba7d5);
 				downloadedPois.add(p);
 			}else {
 				v.setBackgroundColor(Color.TRANSPARENT);
 				downloadedPois.remove(p);
 			}
 			return;
 		}
 
 		Intent details = new Intent(PlanPoiTab.this, PoiDetailsActivity.class);
 		details.putExtra(IntentPassable.POI, p);
 
 		startActivity(details);
 	}//onListItemClick
 
 	
 /////////////////////////////////////////////////////////////////////////
 // HELPER CLASSES
 	
 	/**
 	 * Shows quick actions when the user long-presses an item.
 	 */
 	final private class DrawPopup implements AdapterView.OnItemLongClickListener {
 
 		private void drawCategoryMenu( final AdapterView<?> parent, View view, final int pos ){
 			debug(0, "go" );
 			final int[] xy 			= new int[2];
 			view.getLocationInWindow(xy);
 			final Rect rect 		= new Rect(	xy[0], xy[1], xy[0]+view.getWidth(), xy[1]+view.getHeight());
 
 			final QuickActionPopup qa = new QuickActionPopup( PlanPoiTab.this, view, rect );
 
 			Drawable mapviewIcon	= res.getDrawable(android.R.drawable.ic_menu_mapmode);
 			//Drawable deleteIcon		= res.getDrawable(android.R.drawable.ic_menu_delete);
 
 			// Declare the quick actions menu	// 1: Show on Map
 			qa.addItem(mapviewIcon,	R.string.activity_menu_showOnMap,	 new OnClickListener(){
 				public void onClick(View view){
 					qa.dismiss();
 					CityExplorer.showProgressDialog(context, "Launching Map" );
 
 					Intent showInMap = new Intent(PlanPoiTab.this, MapsActivity.class);
 					Adapter sectionAd = adapter.getAdapter(parent.getAdapter().getItem(pos).toString());
 					ArrayList<Poi> selectedPois = new ArrayList<Poi>();
 					for (int i = 0; i < sectionAd.getCount(); i++){
 						selectedPois.add((Poi) sectionAd.getItem(i));
 					}
 					showInMap.putParcelableArrayListExtra(IntentPassable.POILIST, selectedPois);
 					startActivity(showInMap);
 				}
 			});//quick-action 1
 		}//drawCategoryMenu
 		
 		public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id) {
 
 			//RS-120214: Don't implement different behavior for headings and single items. Suddenly seeing the map is confusing!
 			if(parent.getAdapter().getItemViewType(pos) == SeparatedListAdapter.TYPE_SECTION_HEADER){
 				drawCategoryMenu( parent, v, pos );
 				debug(0, "draw category menu" );
 				return true;
 			}//If long-pressed category
 
 			final Poi	p 			= (Poi) parent.getAdapter().getItem(pos);
 			final AdapterView<?> par = parent;
 
 			final int[] xy 			= new int[2]; v.getLocationInWindow(xy);
 
 			final Rect rect 		= new Rect(	xy[0], 
 					xy[1], 
 					xy[0]+v.getWidth(), 
 					xy[1]+v.getHeight());
 
 			final QuickActionPopup qa = new QuickActionPopup( PlanPoiTab.this, v, rect );
 
 			Drawable addToTripIcon	= res.getDrawable(android.R.drawable.ic_menu_add);
 			Drawable mapviewIcon		= res.getDrawable(android.R.drawable.ic_menu_mapmode);
 			Drawable directIcon		= res.getDrawable(android.R.drawable.ic_menu_directions);
 			Drawable editIcon		= res.getDrawable(android.R.drawable.ic_menu_edit);
 // JF: Support for sharing removed (do not work properly)
 //			Drawable shareIcon		= res.getDrawable(android.R.drawable.ic_menu_share);
 			Drawable deleteIcon		= res.getDrawable(android.R.drawable.ic_menu_delete);
 
 			// Declare the quick actions menu
 
 			// 1: Show on Map
 			qa.addItem(mapviewIcon,	R.string.activity_menu_showOnMap,	 new OnClickListener(){
 				public void onClick(View view){
 					qa.dismiss();
 					CityExplorer.showProgressDialog(context, "Launching Map" );
 					Intent showInMap = new Intent(PlanPoiTab.this, MapsActivity.class);
 					ArrayList<Poi> selectedPois = new ArrayList<Poi>();
 					selectedPois.add(p);
 					showInMap.putParcelableArrayListExtra(IntentPassable.POILIST, selectedPois);
 					startActivity(showInMap);
 				}
 			});
 
 			// 2: Get Directions
 			qa.addItem(directIcon, "Get directions", new OnClickListener(){
 
 				public void onClick(View view){
 
 					//Latitude and longitude for current position
 					double slon = userLocation.getLongitude();
 					double slat = userLocation.getLatitude();
 					//Latitude and longitude for selected poi
 					double dlon = p.getGeoPoint().getLongitudeE6()/1E6;
 					double dlat = p.getGeoPoint().getLatitudeE6()/1E6;
 
 					Intent navigate = new Intent(PlanPoiTab.this, NavigateFrom.class);
 					navigate.putExtra("slon", slon);
 					navigate.putExtra("slat", slat);
 					navigate.putExtra("dlon", dlon);
 					navigate.putExtra("dlat", dlat);
 					startActivity(navigate);
 
 					qa.dismiss();
 				}
 			});
 
 			// 3: Favourite
 			if(p.isFavorite()){ // this POI is a favourite, add an option to not make it a favourite
 				Drawable	favIcon	= res.getDrawable(R.drawable.favstar_on);
 				qa.addItem(favIcon,	"",	new OnClickListener(){
 
 					public void onClick(View view){
 						//set favourite off
 						Poi poi = p;
 						poi = poi.modify().favourite(false).build();
 						DBFactory.getInstance(PlanPoiTab.this).editPoi(poi);//update poi;
 
 						adapter.notifyDataSetChanged();//update list
 						Toast.makeText(PlanPoiTab.this, poi.getLabel() + " removed from Favorites.", Toast.LENGTH_LONG).show();
 						qa.dismiss();
 					}
 				});
 			}else{ // this POI is not a favourite, add an option to make it a favourite
 				Drawable	favIcon	= res.getDrawable(R.drawable.favstar_off);
 				qa.addItem(favIcon,	"",	new OnClickListener(){
 
 					public void onClick(View view){
 						//set as favourite
 						Poi poi = p;
 						poi = poi.modify().favourite(true).build();
 						DBFactory.getInstance(PlanPoiTab.this).editPoi(poi);//update poi;
 
 						allPois.remove(p);
 						allPois.add(poi);
 						Toast.makeText(PlanPoiTab.this, poi.getLabel() + " added to Favorites.", Toast.LENGTH_LONG).show();
 						adapter.notifyDataSetChanged();//update list
 						qa.dismiss();
 					}
 				});
 			}//onItemLongClick
 
 			// 4: Edit
 			qa.addItem(editIcon, R.string.editLabel, new OnClickListener(){
 				public void onClick(View view){
 					Intent editIntent = new Intent( PlanPoiTab.this, NewPoiActivity.class );
 					debug( 0, "id is "+ p.getIdPrivate() +", globId is "+p.getIdGlobal() );
 					editIntent.putExtra(IntentPassable.POI, p);	//setResult( Activity.RESULT_OK, resultIntent );
 					startActivity( editIntent );
 					qa.dismiss();
 				}
 			});
 
 			
 			// 5: 
 // JF: Support for sharing removed (do not work properly)
 //			qa.addItem(shareIcon, "Share", new OnClickListener(){
 //				public void onClick(View view){
 //					ArrayList<Poi> sharePoi = new ArrayList<Poi>();
 //					sharePoi.add(p);
 //					Sharing.send(PlanPoiTab.this, sharePoi);
 //					qa.dismiss();
 //				}
 //			});
 
 			// 6 => 5: AddPoi (5 as sharing removed)
 			qa.addItem(addToTripIcon, R.string.activity_plan_menu_addpois, new OnClickListener(){
 				public void onClick(View view){
 					poi = p;
 					Intent selectTrip = new Intent(PlanPoiTab.this, PlanTripTab.class);
 					selectTrip.putExtra("requestCode", CityExplorer.REQUEST_ADD_TO_TRIP);
 					startActivityForResult(selectTrip, CityExplorer.REQUEST_ADD_TO_TRIP);
 					qa.dismiss();
 				}
 			});
 
 			// 7 => 6: Delete (6 as sharing removed)
 			qa.addItem(deleteIcon, "Delete", new OnClickListener(){
 				public void onClick(View view){
 					DBFactory.getInstance(context).deletePoi(p);											
 					updateSections();
 					((SeparatedListAdapter)par.getAdapter()).notifyDataSetChanged();
 					qa.dismiss();
 				}
 			});
 
 			qa.show();
 			return true;
 		}//onItemLongClick
 	}//class DrawPopup
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		if(resultCode==Activity.RESULT_CANCELED){
 			return;
 		}
 		switch (requestCode){
 		case CityExplorer.REQUEST_ADD_TO_TRIP:
 			Trip trip = (Trip) data.getParcelableExtra(IntentPassable.TRIP);
 			if (trip==null){ 
 				break;
 			}
 
 			trip.addPoi(poi);
 			DBFactory.getInstance(this).addPoiToTrip(trip, poi);
 			Toast.makeText(this, poi.getLabel() + " added to " + trip.getLabel() + ".", Toast.LENGTH_LONG).show();
 			break;
 		default:
 			break;
 		}
 	}//onActivityResult
 
 	@Override
 	public void onBackPressed() {
 		debug(1, "back pressed!" ); // do something on back.
 		if ( saved  ){
 			super.onBackPressed();
 		}else{
 			//Toast.makeText( this, "Save your times first! Press back button again to discard", Toast.LENGTH_LONG).show();
 			saveDialog( this, "Save!", "", null );
 		}
 		return;
 	} //onBackPressed
 	
 	/**
 	 * Handles click events in the filter dialog. Check/unCheck
 	 */
 	@Override
 	public void onClick( DialogInterface dialog, int which, boolean isChecked ){
 		debug(1, "FILTER "+which );
 		@SuppressWarnings("unchecked")
 		LinkedList<String> cat = (LinkedList<String>) categories.clone(); // clone to avoid concurrent read/write
 		//cat.add(0, CityExplorer.FAVORITES); //Moved to getCategories() somewhere...
 		CheckedCategories.remove(cat.get(which));
 		CheckedCategories.put(cat.get(which), isChecked);
 		debug(0, "Categories are "+CheckedCategories.values() );
 	}// onClick
 
 	/**
 	 * Handles the buttons in the filter dialog. Positive/Neutral/Negative Buttons: "ALL", "SELECT", "NONE"
 	 */
 	@Override
 	public void onClick( DialogInterface dialog, int which ){
 		debug(1, "CLICKED BUTTON "+which );
 
 		//add selection to settings:
 		SharedPreferences settings = getSharedPreferences(CATEGORY_SETTINGS, 0);
 		SharedPreferences.Editor editor = settings.edit();
 
 		@SuppressWarnings("unchecked")
 		LinkedList<String> cat = (LinkedList<String>) categories.clone();
 		for (String title : cat){
 			boolean isChecked = CheckedCategories.get(title);
 			//If ALL or NONE was checked: Set all to the same!
 			if ( which == Dialog.BUTTON_POSITIVE ){ //ALL
 				isChecked = true;
 			}else if ( which == Dialog.BUTTON_NEGATIVE ){ //NONE
 				isChecked = false;
 			}
 			//Update CheckedCategories
 			CheckedCategories.remove( title );
 			CheckedCategories.put( title, isChecked );
 
 			//Update preferences:
 			editor.putBoolean(title, isChecked);
 			if( !isChecked){
 				if(adapter.getSectionNames().contains(title)){
 					adapter.removeSection(title);
 				}
 			} else {
 				ArrayList<Poi> list = new ArrayList<Poi>();
 
 				for (Poi poi : allPois){
 					if( title.equals(CityExplorer.FAVORITES) && poi.isFavorite() ){ //add to favourite section
 						list.add(poi);
 					}else if(poi.getCategory().equals(title)){
 						list.add(poi);
 					}
 				}
 				PoiAdapter testAdapter = new PoiAdapter(this, R.layout.plan_listitem, list);
 				adapter.addSection(title, testAdapter);
 
 				if(title.equals(CityExplorer.FAVORITES))
 					favouriteList = list;
 			} // if checked, include
 		} // for each category
 
 		// Commit the edits!
 		editor.commit();
 		lv.setAdapter(adapter);
 	} // onClick ( "OK" button, in filter dialog)
 
 	/***
 	 * setOnItemSelectedListener. To Listen for clicks in the category-filter list
 	 */
 	@Override
 	public void onItemSelected(AdapterView<?> categoryDialog, View clickedCheckbox, int pos, long id) {
 		debug(0, "HEAR I AM!!!");
 		DialogInterface filter = (DialogInterface) categoryDialog;
 		if ( pos==0 ){ // ALL categories selected: Dismiss so the list is updated
 			filter.dismiss();
 		}
 	}//onItemSelected
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		debug(0, "HERE I AM!!!");
 	}//onNothringSelected
 	
 	/**
      * Display a dialog that user should save first!
 	 * @param requestCode ID for the calling Activity
      * Code from: http://osdir.com/ml/Android-Developers/2009-11/msg05044.html
      */
 	private void saveDialog( final Activity context, final String msg, final String cancelButtonStr, final Intent cancelIntent ) {
     	AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setTitle( "Add selected POIs?" );
 	    builder.setMessage( msg );
 		builder.setCancelable(true);
 		String cancelText = cancelButtonStr;
 		if ( cancelText == ""){
 			cancelText = context.getResources().getString( R.string.cancel );
 		}
 		builder.setPositiveButton( R.string.yes, new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog, int which) {
 		    	saveAndFinish();
 		    }
 		} );
 		builder.setNeutralButton( R.string.no, new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog, int which) {
 		    	finish();
 		    }
 		} );
 		builder.setNegativeButton( cancelText, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				if ( cancelIntent != null ){
 					context.startActivityForResult( cancelIntent, CityExplorer.REQUEST_LOCATION );
 					dialog.dismiss();
 		    	}
 				return;
 		    }
 		} );
 		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
 		    public void onCancel(DialogInterface dialog) {
 		    	if ( context == null ){
 		    		CityExplorer.debug(0, "OOOPS!");
 		    	}else{
 		    		Toast.makeText( context, "CANCELLED!", Toast.LENGTH_LONG).show();
 					if (cancelIntent != null){
 						context.startActivity( cancelIntent );
 					}
 		    	}
 		        return;
 		    }
 		} );
 		builder.show();
 	} // saveDialog
 
 } // class PlanPoiTab
