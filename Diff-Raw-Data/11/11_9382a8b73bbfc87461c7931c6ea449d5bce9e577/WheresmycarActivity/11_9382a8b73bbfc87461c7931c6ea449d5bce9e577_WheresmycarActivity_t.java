 package com.ssasha.parking;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 public class WheresmycarActivity extends MapActivity {
 	public static String TAG = "com.ssasha.parking.WheresmycarActivity";
 	private LocationManager locationManager;
 	public Location myLocation;
 	protected MapView mapView;
 	protected MapController mc;
 	protected GeoPoint myPoint = null;
 	protected Spinner daySpinner, hourSpinner, halfSpinner, minuteSpinner;
 	protected Button saveButton;
 	protected int cleaningDay = -1;
 	protected int cleaningHour = -1;
 	protected int cleaningMinute = -1;
 	protected int cleaningHalf = 1;
 	protected int intLng, intLat;
 	private SharedPreferences mPrefs;
 	private GeocodeTask geotask;
 	private String address;
 	protected GregorianCalendar cal;
 	protected PrefsEditor prefs;
 
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.start);
 		initializeLocationProvision();
 
         //initialize mapview
         mapView = (MapView)findViewById(R.id.mapview);
         //show zoom in/out buttons
         mapView.setBuiltInZoomControls(true);
         //Standard view of the map(map/sat)
         mapView.setSatellite(false);
         //get controller of the map for zooming in/out
         mc = mapView.getController();
         // Zoom Level
         mc.setZoom(18); 
         //convert lat and long into units maps can use
         intLat =  (int)(locationManager.getLastKnownLocation(
                 LocationManager.GPS_PROVIDER)
                 .getLatitude()*1000000);
         intLng = (int)(locationManager.getLastKnownLocation(
                 LocationManager.GPS_PROVIDER)
                 .getLongitude()*1000000);
       //Get the current location in start-up
         myPoint = new GeoPoint(intLat, intLng);
         
         LocOverlay myLocationOverlay = new LocOverlay(myPoint);
         List<Overlay> list = mapView.getOverlays();
         list.add(myLocationOverlay);
         
         //show location
         mc.animateTo(myPoint);
          
         //initialize day chooser
         daySpinner = (Spinner)findViewById(R.id.dayspin);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.days, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         daySpinner.setAdapter(adapter);
         daySpinner.setOnItemSelectedListener(new DaySpinnerSelectedListener());
          
         //hour chooser
         hourSpinner = (Spinner)findViewById(R.id.hourspinner);
         Integer[] hours = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}; 
         ArrayAdapter<Integer> timeAdapter = new ArrayAdapter<Integer>(getApplicationContext(), android.R.layout.simple_spinner_item, hours);
         timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         hourSpinner.setAdapter(timeAdapter);
         hourSpinner.setOnItemSelectedListener( new OnItemSelectedListener() {
 			public void onItemSelected(AdapterView<?> arg0, View v, int pos, long id) {
 				cleaningHour = pos + 1;
 				setCleaningTime();
 			}
 
 			public void onNothingSelected(AdapterView<?> arg0) {
 					//nothing			
 			}
          });
               
          //minute chooser
          minuteSpinner = (Spinner)findViewById(R.id.minspin);
          adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.minutes, android.R.layout.simple_spinner_item);
          adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          minuteSpinner.setAdapter(adapter);
          minuteSpinner.setOnItemSelectedListener( new OnItemSelectedListener() {
 			public void onItemSelected(AdapterView<?> arg0, View v, int pos, long id) {
 				cleaningMinute = pos * 15;
 				setCleaningTime();
 			}
 
 			public void onNothingSelected(AdapterView<?> arg0) {
 					//nothing			
 			}
          });
           
          //am/pm chooser
          halfSpinner = (Spinner)findViewById(R.id.halfspinner);
          adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.halfs, android.R.layout.simple_spinner_item);
          adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          halfSpinner.setAdapter(adapter);
          halfSpinner.setOnItemSelectedListener( new OnItemSelectedListener() {
 			public void onItemSelected(AdapterView<?> arg0, View v, int pos, long id) {
 				cleaningHalf = pos;
 				setCleaningTime();
 			}
 
 			public void onNothingSelected(AdapterView<?> arg0) {
 					//nothing			
 			}
          });
          
 //         //timepicker
 //         TimePicker timepicker = (TimePicker)findViewById(R.id.timePicker1);
 //         timepicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
 //			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
 //				setCleaningTime(hourOfDay, minute);
 //			}
 //         });
          
          //savebutton
          saveButton = (Button)findViewById(R.id.savebutton);
     }
     
     //button click handler
     public void onButtonClick(View view) {
     	switch (view.getId()) {
     	case R.id.savebutton :
     		getNextCleaningTime();
     		break;
     	default : 
     		Toast.makeText(getApplicationContext(), "default: " + view.getId(), Toast.LENGTH_SHORT). show();
     	}
     }
     
 	private void initializeLocationProvision() {
 		// Get the location manager
 		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 		Criteria criteria = new Criteria();
 		String bestProvider = locationManager.getBestProvider(criteria, true);
 		if (bestProvider != null) {
 			Log.d(TAG,
 					"is " + bestProvider + " enabled? "
 							+ locationManager.isProviderEnabled(bestProvider));
 		} else {
 			Log.d(TAG, "no provider enabled");
 		}
 		locationManager.requestLocationUpdates(bestProvider, 0, 50,
 				new MyLocationListener(locationManager));
 	}
 	
 	//save day part of streetcleaning time
 	public void setCleaningDay(int dayofweek) {
 		cleaningDay = dayofweek;
 		checkSaveEnable();
 	}
 	
 	//save time part of streetcleaning time
 	public void setCleaningTime() {
 		
 		checkSaveEnable();
 	}
 	
 	private Boolean checkSaveEnable() {
 		if (cleaningDay > -1 && cleaningHour > -1 && cleaningMinute > -1 ) {
 			saveButton.setEnabled(true);
 			return true;
 		}
 		return false;
 	}
 
 	protected void getNextCleaningTime() {
 		if (saveButton.isEnabled()) {
 			//figure out time and day for next cleaning.
 			cal = new GregorianCalendar();
 			int today = cal.get(Calendar.DAY_OF_WEEK);
 			int delta = cleaningDay - today;
 			//adjust for AM/PM
 			cleaningHour += 12 * (cleaningHalf);
 			if (delta < 0) {
 				cal.add(Calendar.DAY_OF_YEAR, 7 + delta);
 			} else if (delta > 0) {
 				cal.add(Calendar.DAY_OF_YEAR, delta);
 			} else {//delta = 0
 				if (cleaningHour < cal.get(Calendar.HOUR_OF_DAY))
 					cal.add(Calendar.DAY_OF_YEAR, 7);
 			}
 			cal.set(Calendar.HOUR_OF_DAY, cleaningHour);
 			cal.set(Calendar.MINUTE, cleaningMinute);
 			System.out.println("new day " + cal.getTime() + "dif ms " + Long.toString(cal.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()));
 			AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
 			
 			//create intent for responding
 			Intent intent = new Intent(this, ReminderReceiver.class);
 			intent.putExtra(IParkingConstants.LAT, myLocation.getLatitude());
 			intent.putExtra(IParkingConstants.LNG, myLocation.getLongitude());
 			intent.putExtra(IParkingConstants.ADDRESS, address);
 	
 			//create PendingIntent for alarm manager
 			PendingIntent pi = PendingIntent.getBroadcast(this, 1, intent, 0);
 			am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
 			//write info to prefs (note that we're only dealing with one car at a time here. TODO
 			writePrefs();
 			String confirmation = "move your car at " + address + " by " + cal.getTime();
 			Toast.makeText(getApplicationContext(), confirmation, Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	//handle result from geocoding
 	protected String getStreetAddress(String xmlString) {
 		String address = "";
 		try {
 			DocumentBuilderFactory builderFactory =
 			        DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder = null;
 			
 		    builder = builderFactory.newDocumentBuilder();
 		    Document doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes()));
 		    Element rootElement = doc.getDocumentElement();
 		    NodeList nodes = rootElement.getChildNodes();
 		    for (int i = 0; i < nodes.getLength(); i++) {
 		    	Boolean isStreetNode = false;
 		    	String formattedAddress = "";
 		    	Node node = nodes.item(i);
 		    	if (node instanceof Element && node.getNodeName().equals("result")) {
 		    		NodeList chillun = node.getChildNodes();
 		    		for (int j = 0; j < chillun.getLength(); j++) {
 		    			Node subNode = chillun.item(j);
 		    			if (subNode.getNodeName().equals("type"))
 		    				if (subNode.getFirstChild().getNodeValue().equals("street_address"))
 		    					isStreetNode = true;
 		    				else
 		    					continue;
 		    			else if (subNode.getNodeName().equals("formatted_address"))
 		    				formattedAddress = subNode.getFirstChild().getNodeValue();
 		    		}
 		    		if (isStreetNode) {
 		    			address = formattedAddress;
 		    			return address;
 		    		}
 		    	}
 		    }
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 		return address;
 	}
 	
 	private void writePrefs() {
		//need to make sure cal has been initialized
		if (cal != null) {
			if (prefs == null)
				prefs = new PrefsEditor();
			prefs.write(this, intLat, intLng, address, cal.getTimeInMillis());
			Log.d(TAG, "writing prefs: " + intLat +", "+intLng+", " + address);
		}
 	}
 	
 	//handle results from GPS
 	private class MyLocationListener implements LocationListener {
 		private LocationManager locMgr;
 
 		public MyLocationListener(LocationManager lm) {
 			locMgr = lm;
 		}
 
 		public void onLocationChanged(Location locFromGps) {
 			Log.d(TAG, "new location: " + locFromGps.getLatitude() + " "
 					+ locFromGps.getLongitude());
 			myLocation = locFromGps;
 			locMgr.removeUpdates(this);
 			geotask = new GeocodeTask();
 			geotask.execute(myLocation.getLatitude(), myLocation.getLongitude());
 			
 		}
 
 		public void onProviderDisabled(String provider) {
 			// called when the GPS provider is turned off (user turning off the
 			// GPS on the phone)
 		}
 
 		public void onProviderEnabled(String provider) {
 			// called when the GPS provider is turned on (user turning on the
 			// GPS on the phone)
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// called when the status of the GPS provider changes
 			Log.d(TAG, "provider: " + provider + " now has status: " + status);
 		}
 	}
 
 	//boilerplate for MapActivity
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	public void displayLocation(Location loc) {
 		myPoint = new GeoPoint((int)(loc.getLatitude()*1000000), (int)(loc.getLongitude()*1000000));
 		mc.animateTo(myPoint);
 	}
 	
 	//does the (reverse) geocoding of lat/lng to get street address.
 	private class GeocodeTask extends AsyncTask<Double, Integer, String> {
 		@Override
 		protected String doInBackground(Double... params) {
 			StringBuffer input = new StringBuffer();
 			String locAddress = new String();
 			String urlString = "http://maps.googleapis.com/maps/api/geocode/xml?latlng=" + Double.toString(params[0])+"," 
 					+ Double.toString(params[1])+"&sensor=true";
 			try {
 				URL geocode = new URL(urlString);
 		        URLConnection geocon = geocode.openConnection();
 		        BufferedReader in = new BufferedReader(
 		                                new InputStreamReader(
 		                                geocon.getInputStream()));
 		        String line;
 		        while ((line = in.readLine()) != null) 
 		            input.append(line);
 		        in.close();
 			} catch (Exception e) {
 				//fail somewhat gracefully
 				e.printStackTrace();
 				locAddress = "Unknown address";
 			}
 			String xmlString = input.toString();
 			locAddress = getStreetAddress(xmlString);
 			address = locAddress;
 			Log.d(TAG, "Task address: " + address);
 			writePrefs();
 			return locAddress;
 		}
 	}
 	
 	//handles selection on day spinner
 	protected class DaySpinnerSelectedListener implements OnItemSelectedListener {
 		public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
 			setCleaningDay(pos +1);		
 		}
 		public void onNothingSelected(AdapterView<?> parent){
 			Log.i(TAG, "nothing");
 		}
 	}
 }
