 package org.jbs.happysad;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.OverlayItem;
 
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.TimePicker;
 
 public abstract class AbstractMap extends MapActivity  {
 	protected MapView map; 
 	int checkHappy;
 	int checkSad;
 	boolean goToMyLocation;
 	int streetView;
 	MyLocationOverlay userLocationOverlay;
 	protected MapController controller;
 	ItemizedEmotionOverlay happyOverlay; 
 	ItemizedEmotionOverlay sadOverlay; 
 	ItemizedEmotionOverlay recentOverlay;
 	boolean enableChart;
 	private final String TAG = "AbstractMap";
 
 	int zoomLevel;
 	GeoPoint center;
 	Runnable latestThread;
 	ArrayList<HappyBottle> newBottles;
 	protected Handler handler = new Handler();
 	HashSet<String> filter = new HashSet<String>();
 	HappyData datahelper = new HappyData(this);
 	protected long timeReference = 0; //used to move forward in time
 	HappyBottle justUpdated;
 
 
 
 	protected synchronized void emotionOverlayAdder(int emotion, ArrayList<HappyBottle> toshow, ItemizedEmotionOverlay overlay){ 
 		if (toshow == null) {return; }///THIS IS A PROBLEM AND SHOULD NEVER HAPPEN
 		//overlay.emptyOverlay();
 		for(HappyBottle bottle : toshow) {
 			//filter works for objects. However, bottles that have the exact same contents count as different bottles. 
 			//This is a problem. Therefore we have a workaround:
 			String bottleObject = bottle.getMsg()+bottle.getTime()+bottle.getUID(); 
 			if( !filter.contains(bottleObject) && bottle.getEmo() == emotion){
 				filter.add(bottleObject);
 				Log.d(TAG, "filter size: " + filter.size());
 				int latitude = bottle.getLat();
 				int longitude = bottle.getLong();
 				GeoPoint point = new GeoPoint(latitude,longitude);
 				String S = (String) new Timestamp(bottle.getTime()).toLocaleString();
 				overlay.addToOverlay(new OverlayItem(point, S+emotion, bottle.getMsg()));
 			}
 		}
 	}
 
 
 
 
 	//Starts tracking the users position on the map. 
 	protected void initMyLocation() {
 		userLocationOverlay = new MyLocationOverlay(this, map);
 		userLocationOverlay.enableMyLocation();
 		map.getOverlays().add(userLocationOverlay);  //adds the users location overlay to the overlays being displayed
 	}
 
 	//helper method for showHappy and showSad onClick cases
 	protected void invalidateOverlay() {
 		map.getOverlays().add(userLocationOverlay);
 	}
 
 
 
 
 
 
 
 	//Required method to make the map work
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	public void chartEnable(ArrayList<HappyBottle> plottables) {
 		Iterator<HappyBottle> itr = plottables.iterator(); 
 
 		while(itr.hasNext()) {     
 			HappyBottle element = itr.next();
 
 			int x = new Timestamp (element.getTime()).getMonth() + 1;
 			int y = new Timestamp (element.getTime()).getYear() + 1900;
 			int z = new Timestamp (element.getTime()).getDate();
 
 			int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
 			int year = Calendar.getInstance().get(Calendar.YEAR);
 			int date = Calendar.getInstance().get(Calendar.DATE);
 
 			if (x == month && y == year && z == date){
 				this.enableChart = true;
 				break;
 			}	
 			this.enableChart = false;
 		}
 	}
 
 	//Finds and initializes the map view.
 	protected void initMapView() {
 		map = (MapView) findViewById(R.id.themap);
 		controller = map.getController();	
 		//checks streetView
 		if (streetView == 1) {
 			map.setStreetView(true);
 			map.setSatellite(false);
 		} else {
 			map.setStreetView(false);
 			map.setSatellite(true);	
 		}
 		map.invalidate();	
 
 		//adds the sad and happy overlays to the map
 		if (checkSad == 1)
 			map.getOverlays().add(sadOverlay);
 		if (checkHappy == 1) 
 			map.getOverlays().add(happyOverlay);
 		map.setBuiltInZoomControls(false); //hides the default map zoom buttons so they don't interfere with the app buttons
 
 	}
 
 	//-----------DATE AND TIME STUFF---------------------------------------------------------
 
 	protected Time timeForView = new Time();
 	protected long epochChecker; // used to check if time changed
 	protected int year;
 	protected int month;
 	protected int day;
 	protected int hour;
 	protected int minute;
 	protected long epochTime;
 
 	static final int DATE_DIALOG_ID = 0;
 	static final int TIME_DIALOG_ID = 1;
 
 	View setDate;// = findViewById(R.id.date_button);
 	View setTime;// = findViewById(R.id.time_button);
 
 	// the callback received when the user "sets" the date in the dialog
 	private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
 
 		public void onDateSet(DatePicker view, int new_year, int new_month,
 				int new_day) {
 			year = new_year;
 			month = new_month;
 			day = new_day;
 			dateTimeUpdate();
 		}
 	};
 
 	// the callback received when the user "sets" the time in the dialog
 	private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
 		public void onTimeSet(TimePicker view, int new_hour, int new_minute) {
 			hour = new_hour;
 			minute = new_minute;
 			dateTimeUpdate();
 		}
 	};
 
 	protected boolean isMoved() {
 		GeoPoint trueCenter =map.getMapCenter();
 		int trueZoom = map.getZoomLevel();
 		if(!((trueCenter.equals(center)) && (trueZoom == zoomLevel))){	
 			Log.d(TAG, "You moved!:" + center.toString() + " zoom: " + zoomLevel);
 			return true;
 		}else{
 			return false;
 		}}
 
 	protected void mapClear(){
 		happyOverlay.emptyOverlay();
 		sadOverlay.emptyOverlay();
 		filter.clear();
 	}
 
 	protected void goToMyLocation() {
 		if (goToMyLocation == true) {
 			userLocationOverlay.runOnFirstFix(new Runnable() {
 				public void run() {
 					// Zoom in to current location
 					controller.animateTo(userLocationOverlay.getMyLocation());
 					controller.setZoom(15); //sets the map zoom level to 15
 				}
 			});
 		}
 		map.getOverlays().add(userLocationOverlay); //adds the users location overlay to the overlays being displayed
 	}
 
 
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case TIME_DIALOG_ID:
 			return new TimePickerDialog(this,
 					timeSetListener, hour, minute, false);
 		case DATE_DIALOG_ID:
 			return new DatePickerDialog(this,
 					dateSetListener,
 					year, month, day);
 		}
 		return null;
 	}
 	
 
 
 	// updates the date in the TextView
 	protected void dateTimeUpdate() {
 		timeForView.set(0,minute,hour,day,month,year);
 		epochTime = timeForView.normalize(true);
 
 		((Button) setDate).setText(new StringBuilder().append(month + 1).append(" - ").append(day).append(" - ").append(year).append(" "));
 		((Button) setTime).setText(new StringBuilder().append(pad(convertAMPM(hour))).append(":").append(pad(minute)).append(" "+checkAMPM(hour)));
 		//Toast.makeText(getBaseContext(), "Time reference: "+epochTime, Toast.LENGTH_LONG).show();
 	}
 
 	protected void setTimeObjectValues(){
 		month = timeForView.month;
 		year = timeForView.year;
 		day = timeForView.monthDay;
 		hour = timeForView.hour;
 		minute = timeForView.minute;
 		((Button) setDate).setText(new StringBuilder().append(month + 1).append(" - ").append(day).append(" - ").append(year).append(" "));
 		((Button) setTime).setText(new StringBuilder().append(pad(convertAMPM(hour))).append(":").append(pad(minute)).append(" "+checkAMPM(hour)));
 	}
 
 	private static int convertAMPM (int convertedhour){
 		if(convertedhour>12){
 			convertedhour = convertedhour-12;
 		}
 		return (convertedhour);
 	}
 
 	private static String checkAMPM (int hour){
 		if(hour<12){
 			return ("AM");
 		}
 		else{
 			return ("PM");
 		}
 	}
 
 	private static String pad(int c) {
 		if (c >= 10)
 			return String.valueOf(c);
 		else
 			return "0" + String.valueOf(c);
 	}
 
 
 
 	protected boolean isTimeChanged(){
 		if(!(epochChecker == epochTime)){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 
 	//-----------DONE DATE AND TIME STUFF----------------------------------------------------	
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.current_location:
 			goToMyLocation = true;
 			goToMyLocation();
 			return true;
 		case R.id.new_update:
 			startActivity(new Intent(this, Prompt.class));
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	protected void readIntents(){
 		Intent i = getIntent();
 		Bundle b = i.getExtras();
 		checkHappy = b.getInt("Happy");
 		checkSad = b.getInt("Sad");
 		goToMyLocation = b.getBoolean("GoToMyLocation");
 		streetView = b.getInt("Street");
 
 		try{
 			//we might have an update in a bundle from the more screen. We might not! If not, that's fine, we'll just continue;
 			Long id = b.getLong("id");
 			int lat = b.getInt("BottleLat");
 			int longi = b.getInt("BottleLong");
 			String msg = b.getString("BottleMsg");
 			short emo = b.getShort("BottleEmo");
 			long time = b.getLong("BottleTime");
 
 			justUpdated = new HappyBottle(id, lat, longi, emo, msg, time);
 			 
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		};
 
 	}
 
 
 	protected void initLatestUpdateOverlay(HappyBottle bottle){
 		if(bottle == null){return;}
 		Drawable superhappyface = this.getResources().getDrawable(R.drawable.recenthappy);
 		Drawable supersadface = this.getResources().getDrawable(R.drawable.recentsad);
 		Drawable superface;
 		short emo = bottle.getEmo();
 		if(emo == 0){
 			//if the most recent update is sad;
 			superface = supersadface;
 		}
 		else{//else, show a recent happy face on the new "just updated" overlay
 			superface = superhappyface;
 		}
 		recentOverlay = new ItemizedEmotionOverlay(superface, this);
 		
 		int latitude = bottle.getLat();
 		int longitude = bottle.getLong();
 		GeoPoint point = new GeoPoint(latitude,longitude);
 		String S = (String) new Timestamp(bottle.getTime()).toLocaleString();
 		recentOverlay.addToOverlay(new OverlayItem(point, S+emo, bottle.getMsg()));
 		
 		Log.d(TAG, Boolean.toString(map.isEnabled()));
 		Log.d(TAG, Integer.toString(recentOverlay.size()));
 		
 		map.getOverlays().add(recentOverlay);
 	}
 
 	protected void initDateStuff(){
 		// get the current date
 		final Calendar c = Calendar.getInstance();
 		year = c.get(Calendar.YEAR);
 		month = c.get(Calendar.MONTH);
 		day = c.get(Calendar.DAY_OF_MONTH);
 		hour = c.get(Calendar.HOUR_OF_DAY);
 		minute = c.get(Calendar.MINUTE);
 	}
 
 	protected void initOverlayStuff(){
 		//Defines the drawable items for the happy and sad overlays
 		Drawable happyface = this.getResources().getDrawable(R.drawable.pinhappy);
 		Drawable sadface = this.getResources().getDrawable(R.drawable.pinsad);
 
 
 		//initializes the happy and sad overlays
 		happyOverlay = new ItemizedEmotionOverlay(happyface, this);	
 		sadOverlay = new ItemizedEmotionOverlay(sadface, this);
 
 
 
 
 	}
 
 }
