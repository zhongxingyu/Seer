 package com.example.PrayerTimes;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	public static final String PREFS_NAME = "MyPrefsFile"; //Preferences tag
 	private ListView prayersListView;  //Main prayers list
 	public Calendar now = Calendar.getInstance(); //Main calendar
 
 	// Define amd setup location manager parameters
 	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 2000; // in Meters  
 	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 10000; // in Milliseconds
 	protected LocationManager locationManager;
 	public MyLocationListener locationListener = new MyLocationListener();
 
 	// Get today's date
 	int cyear = now.get(Calendar.YEAR);
 	int cmonth = now.get(Calendar.MONTH);
 	int cday = now.get(Calendar.DAY_OF_MONTH);
 
 	settingsBlob mySettings = new settingsBlob(0, 0, 0, 0, 0, 0); //City settings
 	Calculator myTimeCalculator = new Calculator(mySettings); //Prayers calculator
 	final ArrayList<Prayer> prayersList = new ArrayList<Prayer>(); //Prayers objects list
 	Profile mainProfile; //Main settings profile
 
 	String city_string="Arlington, VA";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		//Load Settings into main Profile
 		mainProfile = loadSettings();
 
 
 		//Setup GPS
 		if(mainProfile.useGPS)
 			setupGPS();
 
 		//Add prayer objects to the prayersList
 		prayersList.add(new Prayer(-18.0, "exact", "Fajr"));
 		prayersList.add(new Prayer(-0.83, "exact", "Sunrise"));
 		prayersList.add(new Prayer(0, "max", "Dhuhr"));
 		prayersList.add(new Prayer(-0.83, "exact", "Sunset"));
 		prayersList.add(new Prayer(-4.0, "exact", "Maghrib"));
 		prayersList.add(new Prayer(0, "noCalc", "Midnight"));
 
 		Button locOnMapButton = (Button)findViewById(R.id.button1);
 		locOnMapButton.setOnClickListener(locOnMapButtonListener);
 
 		Button changeDateButton = (Button)findViewById(R.id.button3);
 		changeDateButton.setText("Prayer times table for "+city_string+" in "+ String.valueOf(now.get(Calendar.MONTH)+1)+"/"+String.valueOf(now.get(Calendar.DAY_OF_MONTH))+"/"+String.valueOf(now.get(Calendar.YEAR)));
 		changeDateButton.setOnClickListener(changeDateButtonListener);
 
 		//Define the GPS checkBox
 		CheckBox gpsCheckBox = (CheckBox)findViewById(R.id.checkBox1);
 		gpsCheckBox.setOnClickListener(gpsCheckBoxListener);
 		gpsCheckBox.setChecked(mainProfile.useGPS);
 
 		//Define the TimeZone checkbox
 		CheckBox timezoneCheckBox = (CheckBox)findViewById(R.id.checkBox2);
 		timezoneCheckBox.setOnClickListener(timezoneCheckBoxListener);
 		timezoneCheckBox.setChecked(mainProfile.useTimezone);
 
 		//Setup the EditTexts
 		EditText latit = (EditText)findViewById(R.id.editText1);
 		latit.setOnFocusChangeListener(EditBoxesListener);
 		latit.setEnabled(!mainProfile.useGPS);
 		if(mainProfile.useGPS == false)
 			latit.setText(""+mainProfile.savedLatitude);
 
 		EditText longit = (EditText)findViewById(R.id.editText2);
 		longit.setOnFocusChangeListener(EditBoxesListener);
 		longit.setEnabled(!mainProfile.useGPS);
 		if(mainProfile.useGPS == false)
 			longit.setText(""+mainProfile.savedLatitude);
 
 		EditText timezoneEditText = (EditText)findViewById(R.id.editText3);
 		timezoneEditText.setOnFocusChangeListener(EditBoxesListener);
 		timezoneEditText.setEnabled(!mainProfile.useTimezone);
 		if(mainProfile.useTimezone == false)
 			timezoneEditText.setText(""+mainProfile.savedTimezone);
 		else
 		{ 
 			timezoneEditText.setText(""+getTimezone());
 			mainProfile.savedTimezone = getTimezone();
 			saveSettings(mainProfile);
 		}
 
 
 		//Calculate the prayer times for the PrayersList and display them on the WeatherList
 		calculateAndDisplay(prayersList);
 
 	} //END OF OnCreate
 
 	public void calculateAndDisplay(ArrayList<Prayer> prayersList){
 		// Calculate prayer times and store them inside the objects
 		myTimeCalculator.getTimes(prayersList);
 
 		//Calculate time for midnight  { Midnight = Sunset + (Sunset-Fajr)/2 }
 		prayersList.get(5).prayerTime = prayersList.get(3).prayerTime + (prayersList.get(0).prayerTime-prayersList.get(3).prayerTime)/2;  
 
 		// Loop over the objects showing their times
 		/*for(int index=0;index<5;index++)
 			Toast.makeText(getApplicationContext(), ""+ prayersList.get(index).name + " prayer time is : " + myTimeCalculator.pretty(prayersList.get(index).prayerTime),Toast.LENGTH_LONG).show();
 		 */
 
 		final Weather weather_data[] = new Weather[]
 				{
 				new Weather(R.drawable.fajr, "Fajr",		myTimeCalculator.pretty(prayersList.get(0).prayerTime)),
 				new Weather(R.drawable.sunrise, "Sunrise",	myTimeCalculator.pretty(prayersList.get(1).prayerTime)),
 				new Weather(R.drawable.duhr, "Duhr",		myTimeCalculator.pretty(prayersList.get(2).prayerTime)),
 				new Weather(R.drawable.sunset, "Sunset",	myTimeCalculator.pretty(prayersList.get(3).prayerTime)),
 				new Weather(R.drawable.maghrib, "Maghrib",	myTimeCalculator.pretty(prayersList.get(4).prayerTime)),
 				new Weather(R.drawable.midnight, "Midnight",myTimeCalculator.pretty(prayersList.get(5).prayerTime))
 				};
 		WeatherAdapter adapter = new WeatherAdapter(com.example.PrayerTimes.MainActivity.this, 
 				R.layout.listview_item_row, weather_data);
 
 		prayersListView = (ListView)findViewById(R.id.listView1);
 		prayersListView.setAdapter(adapter);
 	}
 
 	// Buttons and other elements' functions
 	View.OnClickListener locOnMapButtonListener = new View.OnClickListener() {
 		public void onClick(View v) {
 
 			final Dialog dialog = new Dialog(MainActivity.this);
 
 			dialog.setContentView(R.layout.maindialog);
 			dialog.setTitle("Choose Timezone:");
 			dialog.setCancelable(true);
 
 			//set up button
 			Button button = (Button) dialog.findViewById(R.id.Button01);
 			View.OnClickListener buttonListener = new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					dialog.dismiss();
 				}
 			}; 
 			button.setOnClickListener(buttonListener);
 
 			//now that the dialog is set up, it's time to show it    
 			dialog.show();
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
 
 					//Do whatever you want with the variables you get here
 					Button myButton2 = (Button)findViewById(R.id.button3);
 					myButton2.setText("Prayer times table for "+city_string+" in "+ String.valueOf(monthOfYear+1)+"/"+String.valueOf(dayOfMonth)+"/"+String.valueOf(year));
 
 					//Reconfigure our settings blob.
 					myTimeCalculator.mySettings.year = cyear;
 					myTimeCalculator.mySettings.month = cmonth+1;
 					myTimeCalculator.mySettings.day = cday;
 
 					//Calculate the new prayer times for the updated PrayersList and display them on the WeatherList
 					calculateAndDisplay(prayersList);                    
 
 				}
 			},  cyear, cmonth, cday);
 			calender.setTitle("Show prayer times for:");
 			calender.show(); 
 		}
 	};
 	View.OnClickListener gpsCheckBoxListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 
 			mainProfile.useGPS = ((CheckBox)v).isChecked();
 			saveSettings(mainProfile);
 
 			if(mainProfile.useGPS == true)
 				setupGPS();
 			else
 				locationManager.removeUpdates(locationListener);
 
 			EditText latit = (EditText)findViewById(R.id.editText1);
 			latit.setEnabled(!((CheckBox)v).isChecked());
 
 
 			EditText longit = (EditText)findViewById(R.id.editText2);
 			longit.setEnabled(!((CheckBox)v).isChecked());
 
 		}
 	};
 	View.OnClickListener timezoneCheckBoxListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			mainProfile.useTimezone = ((CheckBox)v).isChecked();
 			saveSettings(mainProfile);
 
 			EditText timezone_field = (EditText)findViewById(R.id.editText3);
 
 			timezone_field.setEnabled(!mainProfile.useTimezone);
 
 			if( mainProfile.useTimezone ){
 				timezone_field.setText(""+getTimezone());
 			}
 
 			//Set timeZone to the value in the timezone editBox
 			mainProfile.savedTimezone = Integer.parseInt(timezone_field.getText().toString());
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
 
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MINIMUM_TIME_BETWEEN_UPDATES,MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,locationListener);
 
 		/*List<String> providers =locationManager.getProviders(true);
 
 		 if (myTimeCalculator.mySettings.latitude==0.0 && myTimeCalculator.mySettings.latitude==0.0){
 
 			Location l = null;
 			for (int i=providers.size()-1; i>=0; i--) {
 				l = locationManager.getLastKnownLocation(providers.get(i));
 				if (l != null) break;
 			}
 			if (l != null) {
 				mainProfile.savedLatitude = l.getLatitude();
 				EditText latit = (EditText)findViewById(R.id.editText1);
 				latit.setText(""+mainProfile.savedLatitude);
 
 				mainProfile.savedLongitude = l.getLongitude();
 				EditText longit = (EditText)findViewById(R.id.editText2);
 				longit.setText(""+mainProfile.savedLongitude);
 
 				saveSettings(mainProfile);
 				applyProfile(mainProfile); //Apply changes from mainProfile into mySettings
 			}
 		} */
 	}
 	private class MyLocationListener implements LocationListener{
 		public void onLocationChanged(Location location) {  
 			if(mainProfile.useGPS){
 				mainProfile.savedLongitude = location.getLongitude();
 				mainProfile.savedLatitude = location.getLatitude();
 
 				applyProfile(mainProfile); //Save changes to mySettings too
 				saveSettings(mainProfile);
 
 				EditText latit = (EditText)findViewById(R.id.editText1);
 				EditText longit = (EditText)findViewById(R.id.editText2);
 
 				latit.setText(""+myTimeCalculator.mySettings.latitude);
 				longit.setText(""+myTimeCalculator.mySettings.longitude);
 
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
 				Toast.makeText(MainActivity.this,"GPS is turned off, please either turn GPS on or use manual coordinates.",Toast.LENGTH_LONG).show();
 		}
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 		}  
 	} 
 	public Profile loadSettings(){
 		Profile profile = new Profile();
 		SharedPreferences settings  = getSharedPreferences(PREFS_NAME, 0);
 
 		profile.savedLatitude = (double)settings.getFloat("savedLatitude", 0);
 		profile.savedLongitude = (double) settings.getFloat("savedLongitude", 0);
 		profile.savedTimezone = settings.getInt("savedTimezone", 0);
 		profile.cityName = settings.getString("cityName", "Unnammed City");
 		profile.useGPS = settings.getBoolean("useGPS", false);
 		profile.useTimezone = settings.getBoolean("useTimezone", true);
 
 		return profile;
 	}
 	public void saveSettings(Profile profile){
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putFloat("savedLatitude", (float)(profile.savedLatitude));
 		editor.putFloat("savedLongitude", (float)(profile.savedLongitude));
 		editor.putInt("savedTimezone", profile.savedTimezone);
 		editor.putString("cityName", profile.cityName);
 		editor.putBoolean("useGPS", profile.useGPS);
 		editor.putBoolean("useTimezone", profile.useTimezone);
 		editor.commit();
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
 		myTimeCalculator.mySettings.latitude = profile.savedLatitude;
 		myTimeCalculator.mySettings.longitude = profile.savedLongitude;
 		myTimeCalculator.mySettings.timeZone = profile.savedTimezone;
 	}
 }
