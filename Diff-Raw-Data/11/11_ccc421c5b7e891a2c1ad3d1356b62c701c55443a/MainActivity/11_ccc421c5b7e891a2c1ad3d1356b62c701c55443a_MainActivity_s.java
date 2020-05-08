 package com.example.PrayerTimes;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity  {
 
 	public static final String PREFS_NAME = "MyPrefsFile"; //Preferences tag
 	private ListView prayersListView;  //Main prayers list
 	public Calendar now = Calendar.getInstance(); //Main calendar
 
 	// Define amd setup location manager parameters
 	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 2000; // in Meters  
 	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 10000; // in Milliseconds
 	protected LocationManager locationManager;
 	public MyLocationListener locationListener;
 
 	// Get today's date
 	int cyear = now.get(Calendar.YEAR);
 	int cmonth = now.get(Calendar.MONTH);
 	int cday = now.get(Calendar.DAY_OF_MONTH);
 
 	settingsBlob mySettings = new settingsBlob(0, 0, 0, 0, 0, 0); //City settings
 	Calculator myTimeCalculator = new Calculator(mySettings); //Prayers calculator
 	final ArrayList<Prayer> prayersList = new ArrayList<Prayer>(); //Prayers objects list
 	Profile mainProfile; //Main settings profile
 
 	boolean myLocShown = false;
 	Dialog mainDialog; 
 	Dialog newNameDialog;
 	Dialog dialog;
 	DatabaseHandler db = new DatabaseHandler(this);
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		//Load Settings into main Profile
 		mainProfile = loadSettings();
 
 		if(mainProfile.cityName.equals("Unnammed City"))
 			askForNewName();
 
 		//Add prayer objects to the prayersList
 		prayersList.add(new Prayer(-18.0, "exact", "Fajr", "up"));
 		prayersList.add(new Prayer(-0.83, "exact", "Sunrise", "up"));
 		prayersList.add(new Prayer(0, "max", "Dhuhr" ,"x"));
 		prayersList.add(new Prayer(-0.83, "exact", "Sunset", "down"));
 		prayersList.add(new Prayer(-4.0, "exact", "Maghrib", "down"));
 		prayersList.add(new Prayer(0, "noCalc", "Midnight", "down"));
 
 		//Set today's date into the blob.
 		myTimeCalculator.mySettings.year = cyear;
 		myTimeCalculator.mySettings.month = cmonth+1;
 		myTimeCalculator.mySettings.day = cday;
 
 		Button locOnMapButton = (Button)findViewById(R.id.button1);
 		locOnMapButton.setOnClickListener(locOnMapButtonListener);
 
 		Button newCityButton = (Button)findViewById(R.id.button5);
 		newCityButton.setOnClickListener(newCityButtonListener);
 
 		Button loadCityButton = (Button)findViewById(R.id.button4);
 		loadCityButton.setOnClickListener(loadCityButtonListener);
 
 		Button changeDateButton = (Button)findViewById(R.id.button3);
 		changeDateButton.setOnClickListener(changeDateButtonListener);
 
 		//Define the GPS checkBox
 		CheckBox gpsCheckBox = (CheckBox)findViewById(R.id.checkBox1);
 		gpsCheckBox.setOnClickListener(gpsCheckBoxListener);
 
 
 		//Define the TimeZone checkbox
 		CheckBox timezoneCheckBox = (CheckBox)findViewById(R.id.checkBox2);
 		timezoneCheckBox.setOnClickListener(timezoneCheckBoxListener);
 
 
 		//Setup the EditTexts
 		EditText latit = (EditText)findViewById(R.id.editText1);
 		latit.setOnFocusChangeListener(EditBoxesListener);
 
 		EditText longit = (EditText)findViewById(R.id.editText2);
 		longit.setOnFocusChangeListener(EditBoxesListener);
 
 		EditText timezoneEditText = (EditText)findViewById(R.id.editText3);
 		timezoneEditText.setOnFocusChangeListener(EditBoxesListener);
 
 
 		//Calculate the prayer times for the PrayersList and display them on the PrayerList
 		applyProfile(mainProfile);
 		calculateAndDisplay(prayersList);
 
 	} //END OF OnCreate
 
 	public void calculateAndDisplay(ArrayList<Prayer> prayersList){
 		if(myTimeCalculator.mySettings.latitude != 0.0 || myTimeCalculator.mySettings.latitude != 0.0){ //Don't calculate on 0,0
 			// Calculate prayer times and store them inside the objects
 			myTimeCalculator.getTimes(prayersList);
 
 			//Calculate time for midnight  { Midnight = Sunset + (Fajr+24-Sunset)/2 }
 			prayersList.get(5).prayerTime = prayersList.get(3).prayerTime + (prayersList.get(0).prayerTime+(24*60*60)-prayersList.get(3).prayerTime)/2;
 
 			// Loop over the objects showing their times
 			/*for(int index=0;index<5;index++)
 			Toast.makeText(getApplicationContext(), ""+ prayersList.get(index).name + " prayer time is : " + myTimeCalculator.pretty(prayersList.get(index).prayerTime),Toast.LENGTH_LONG).show();
 			 */
 
 			final PrayerItem prayer_data[] = new PrayerItem[]
 					{
 					new PrayerItem(R.drawable.fajr, "Fajr",		myTimeCalculator.pretty(prayersList.get(0).prayerTime)),
 					new PrayerItem(R.drawable.sunrise, "Sunrise",	myTimeCalculator.pretty(prayersList.get(1).prayerTime)),
 					new PrayerItem(R.drawable.duhr, "Duhr",		myTimeCalculator.pretty(prayersList.get(2).prayerTime)),
 					new PrayerItem(R.drawable.sunset, "Sunset",	myTimeCalculator.pretty(prayersList.get(3).prayerTime)),
 					new PrayerItem(R.drawable.maghrib, "Maghrib",	myTimeCalculator.pretty(prayersList.get(4).prayerTime)),
 					new PrayerItem(R.drawable.midnight, "Midnight",myTimeCalculator.pretty(prayersList.get(5).prayerTime))
 					};
 			PrayersListAdapter adapter = new PrayersListAdapter(com.example.PrayerTimes.MainActivity.this, 
 					R.layout.listview_item_row, prayer_data);
 
 			prayersListView = (ListView)findViewById(R.id.listView1);
 			prayersListView.setAdapter(adapter);
 		}
 	}
 
 	// Buttons and other elements' functions
 
 	View.OnClickListener locOnMapButtonListener = new View.OnClickListener() {
 		public void onClick(View v) {
 			Intent mapIntent = new Intent(getBaseContext(), MapActivity.class);
 			mapIntent.putExtra("latitude",myTimeCalculator.mySettings.latitude);
 			mapIntent.putExtra("longitude",myTimeCalculator.mySettings.longitude);
 			startActivityForResult(mapIntent,0);
 		}
 	};
 
 	View.OnClickListener newCityButtonListener = new View.OnClickListener() {
 		public void onClick(View v) {
 			newCity();
 		}
 	};
 
 	View.OnClickListener loadCityButtonListener = new View.OnClickListener() {
 		public void onClick(View v) {
 			Intent mapIntent = new Intent(getBaseContext(), cityManager.class);
 			mapIntent.putParcelableArrayListExtra("profile", new ArrayList<Profile>(Collections.singletonList(mainProfile)));
 			startActivityForResult(mapIntent,1);
 		}
 	};
 	View.OnClickListener changeDateButtonListener = new View.OnClickListener() {
 		public void onClick(View v) {
 
 			//Make a datePicker dialog and initialize its listener and its onDateSet function.
 			Dialog calender = new DatePickerDialog(MainActivity.this,  new DatePickerDialog.OnDateSetListener() {
 				// onDateSet method
 				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 					cyear=year;
 					cmonth=monthOfYear;
 					cday=dayOfMonth;
 
 					//Reconfigure our settings blob.
 					myTimeCalculator.mySettings.year = cyear;
 					myTimeCalculator.mySettings.month = cmonth+1;
 					myTimeCalculator.mySettings.day = cday;
 
 					//Calculate the new prayer times for the updated PrayersList and display them on the PrayersList
 					applyProfile(mainProfile);
 					calculateAndDisplay(prayersList);                    
 
 				}
 			},  cyear, cmonth, cday);
 			calender.setTitle("Show prayer times for:");
 			calender.show(); 
 		}
 	};
 	View.OnClickListener onOkNewNameListener = new OnClickListener(){
 		public void onClick(View v) {
 			EditText edit = (EditText) newNameDialog.findViewById(R.id.editText1);
 			String inputString = edit.getText().toString();
 			//If the user typed something
 			if(!inputString.equals("")){
 				//Get the new name and send it to save settings
 				mainProfile.cityName = inputString;
 				applyProfile(mainProfile);
 				saveSettings(mainProfile);
 				//Add it to the database too
 				db.addProfile(mainProfile);
 				// Dismiss dialog
 				newNameDialog.dismiss();
 			}
 		}
 	};
 	View.OnClickListener gpsCheckBoxListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 
 			mainProfile.useGPS = ((CheckBox)v).isChecked();
 			applyProfile(mainProfile);
 			saveSettings(mainProfile);
 
 			calculateAndDisplay(prayersList);
 		}
 	};
 	View.OnClickListener timezoneCheckBoxListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			mainProfile.useTimezone = ((CheckBox)v).isChecked();
 
 			applyProfile(mainProfile);
 			saveSettings(mainProfile);
 
 			//Recalculate
 			calculateAndDisplay(prayersList);
 		}
 	};
 	View.OnFocusChangeListener EditBoxesListener = new View.OnFocusChangeListener()  {
 
 		@Override
 		public void onFocusChange(View v, boolean hasFocus) {
 			if(!hasFocus){
 				EditText selected = (EditText)v;
 
 				switch(selected.getId()){
 				case R.id.editText1: 
 					mainProfile.savedLatitude = Double.parseDouble(selected.getText().toString());
 					break;
 				case R.id.editText2:
 					mainProfile.savedLongitude = Double.parseDouble(selected.getText().toString());
 					break;
 				case R.id.editText3:
 					mainProfile.savedTimezone = Integer.parseInt(selected.getText().toString());
 					break;
 				}
 				applyProfile(mainProfile); //Apply changes to mySettings too
 				saveSettings(mainProfile); //Save profile to registry
 				calculateAndDisplay(prayersList);
 			}
 
 		}
 	};
 
 	public void setupGPS(){
 		if(locationListener == null){
 			locationListener = new MyLocationListener();
 			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MINIMUM_TIME_BETWEEN_UPDATES,MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,locationListener);
 		}
 	}
 	private class MyLocationListener implements LocationListener{
 		public void onLocationChanged(Location location) {  
 			if(		mainProfile.useGPS == true && 
 					!(location.getLongitude() == mainProfile.savedLongitude && location.getLatitude() == mainProfile.savedLatitude)
 					){
 				mainProfile.savedLongitude = location.getLongitude();
 				mainProfile.savedLatitude = location.getLatitude();
 
 				applyProfile(mainProfile); //Save changes to mySettings too
 				saveSettings(mainProfile);
 
 				String message = String.format(  
 						"New Location : \n Longitude: %1$s \n Latitude: %2$s\n Recalculating...",  
 						myTimeCalculator.mySettings.longitude, myTimeCalculator.mySettings.latitude);
 
 				Toast.makeText(MainActivity.this, message ,Toast.LENGTH_LONG).show();
 				calculateAndDisplay(prayersList);
 			}
 		}  
 		public void onProviderDisabled(String s) {  
 			if(mainProfile.useGPS)
 				Toast.makeText(MainActivity.this,"GPS is turned off, please either turn GPS on or use manual coordinates.",Toast.LENGTH_LONG).show();
 		}  
 		public void onProviderEnabled(String s) {
 			if(mainProfile.useGPS)
 				Toast.makeText(MainActivity.this,"GPS Enabled, getting coordinates.",Toast.LENGTH_LONG).show();
 		}
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 		}  
 	} 
 	public Profile loadSettings(){
 
 		SharedPreferences settings  = getSharedPreferences(PREFS_NAME, 0);
 		Profile profile = new Profile();
 
 		String lastCity = settings.getString("lastCity", "Unnammed City");
 
 		if(lastCity.equals("Unnammed City"))
 			return profile;
 		else
 			return db.getProfile(lastCity);
 	}
 	public void saveSettings(Profile profile){
 		//Get shared preferences
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 
 		// if the city is not Unnammed City, update it on db and save its name
 		if(!profile.cityName.equals("Unnammed City")){
 			editor.putString("lastCity", profile.cityName);
 			editor.commit();
 			db.addProfile(profile);
 		}
 	}
 	public int getTimezone(){
 		Calendar c = Calendar.getInstance();
 		long tz = c.getTimeZone().getRawOffset();
 		boolean timezone=c.getTimeZone().inDaylightTime(c.getTime());
 		tz/=1000*60*60;
 		tz+=timezone?1:0;
 		return (int)tz;
 	}
 	public void applyProfile(Profile profile){
		//Set the calculator values from this profile
		myTimeCalculator.mySettings.latitude = profile.savedLatitude;
		myTimeCalculator.mySettings.longitude = profile.savedLongitude;
		myTimeCalculator.mySettings.timeZone = profile.savedTimezone;

 		//Set up the GUI from this profile
 		Button changeDateButton = (Button)findViewById(R.id.button3);
 		changeDateButton.setText("Prayer times table for "+profile.cityName+" in "+ String.valueOf(cmonth+1)+"/"+String.valueOf(cday)+"/"+String.valueOf(cyear));
 
 		CheckBox gpsCheckBox = (CheckBox)findViewById(R.id.checkBox1);
 		gpsCheckBox.setChecked(profile.useGPS);
 
 		CheckBox timezoneCheckBox = (CheckBox)findViewById(R.id.checkBox2);
 		timezoneCheckBox.setChecked(profile.useTimezone);
 
 		EditText latit = (EditText)findViewById(R.id.editText1);
 		latit.setEnabled(!profile.useGPS);
 		latit.setText(""+profile.savedLatitude);
 
 		EditText longit = (EditText)findViewById(R.id.editText2);
 		longit.setEnabled(!profile.useGPS);
 		longit.setText(""+profile.savedLongitude);
 
 		EditText timezoneEditText = (EditText)findViewById(R.id.editText3);
 
 		timezoneEditText.setEnabled(!profile.useTimezone);
 
 		if(profile.useTimezone == false)
 			timezoneEditText.setText(""+profile.savedTimezone);
 		else
 		{ 
 			timezoneEditText.setText(""+getTimezone());
 			profile.savedTimezone = getTimezone();
 			saveSettings(profile);
 		}
 
 		//GPS listener enable/disable depending on profile.useGPS 
 		if(profile.useGPS == true)
 			setupGPS();
 		else
 		{
 			if(locationManager!=null && locationListener!=null)
 				locationManager.removeUpdates(locationListener);
 			locationListener = null;
 			System.gc();
 		}
 
 	}
 
 	// After map activity is finished
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(resultCode != RESULT_CANCELED){
 			// Get its data parameters
 			Bundle params = data.getExtras();
 
 			//If results came from MapActivity
 			if(requestCode == 0){
 				// Check the 'result' entry for 'ok' or 'cancelled'
 				String result = params.getString("result");
 				//Apply the new coordinates to the current profile and the GUI
 				if(result.equals("ok")){
 					mainProfile.useGPS = false;
 					mainProfile.savedLatitude = params.getDouble("latitude");
 					mainProfile.savedLongitude = params.getDouble("longitude");
 
 					applyProfile(mainProfile);
 					saveSettings(mainProfile);
 					calculateAndDisplay(prayersList);
 				}
 			}
 
 			//If results came from cityManager load activity (with statusOK status)
 			if(requestCode == 1 && params.getString("status").equals("statusOK")){
 				// Receive the loaded profile
 				mainProfile = (Profile)(data.getParcelableArrayListExtra("profile").get(0));
 
 				// Save the profile, apply gui, and recalculate
 				saveSettings(mainProfile);
 				applyProfile(mainProfile);
 				calculateAndDisplay(prayersList);
 			}
 		}
 	}
 
 	void askForNewName(){
 		newNameDialog = new Dialog(MainActivity.this);
 		newNameDialog.setContentView(R.layout.city_new_name);
 		newNameDialog.setTitle("What is this city's name?");
 		newNameDialog.setCancelable(false);
 
 		//set up button (OK)
 		Button button2 = (Button) newNameDialog.findViewById(R.id.Button02);
 		button2.setOnClickListener(onOkNewNameListener);
 
 		//now that the dialog is set up, it's time to show it
 		newNameDialog.show();
 	}
 	void newCity(){
 		dialog = new Dialog(MainActivity.this);
 		dialog.setContentView(R.layout.city_name);
 		dialog.setTitle("City name:");
 		dialog.setCancelable(true);
 
 		//set up button (Cancel)
 		Button button = (Button) dialog.findViewById(R.id.Button01);
 		button.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 
 		//set up button (OK)
 		Button button2 = (Button) dialog.findViewById(R.id.Button02);
 		button2.setOnClickListener(okListener);
 		//now that the dialog is set up, it's time to show it
 		dialog.show();
 	}
 
 	View.OnClickListener okListener = new OnClickListener(){
 		public void onClick(View v) {
 			EditText edit = (EditText) dialog.findViewById(R.id.editText1);
 			String inputString = edit.getText().toString();
 			Profile newProfile = mainProfile;
 			//If the user typed something
 			if(!inputString.equals("")){
 				newProfile.cityName = inputString;
 				//Delete if an existing one is there
 				if(db.profileExists(newProfile.cityName))
 					db.deleteProfile(newProfile.cityName);
 
 				//Add the new profile
 				db.addProfile(newProfile);
 
 				//A little tip
 				Toast.makeText(getApplicationContext(),"Now you can set the new coordinates and the timezone.",Toast.LENGTH_LONG).show();
 
 				//Update current profile and settings
 				mainProfile = newProfile;
 				applyProfile(mainProfile);
 				saveSettings(mainProfile);
 				calculateAndDisplay(prayersList);
 
 				// Dismiss dialog and cityManager
 				dialog.dismiss();
 			}
 		}
 	};
 }
