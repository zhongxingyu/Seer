 package com.MetroSub.activity;
 
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.BitmapDrawable;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.View;
 import android.widget.*;
 import com.MetroSub.R;
 import com.MetroSub.database.dataobjects.StopData;
 import com.MetroSub.ui.StationListAdapter;
 import com.MetroSub.ui.SubwayLinePlotter;
 import com.MetroSub.ui.SubwayTimesListAdapter;
 import com.MetroSub.utils.AlarmReceiver;
 import com.MetroSub.utils.UIUtils;
 import com.actionbarsherlock.app.SherlockMapFragment;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.model.*;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.loopj.android.http.*;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import java.net.URLEncoder;
 import java.util.*;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: kushpatel
  * Date: 7/14/13
  * Time: 5:23 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MapActivity extends BaseActivity implements LocationListener {
 
     private static final String TAG = "MapActivity";
 
     public static final LatLng MANHATTAN = new LatLng(40.7697, -73.9735);
     public static final float DEFAULT_ZOOM_LEVEL = 12;
     public static final float CLOSE_UP_ZOOM_LEVEL = 17;
 
     protected GoogleMap map;
     protected View mMapOptionsBar;
     protected View mSelectTripByLinesScreen;
     protected View mStationsListScreen;
     protected View mScheduleAlertsOptionsBar;
     protected View mScheduleAlertsScreen;
     protected View mViewLinesScreen;
     protected com.actionbarsherlock.app.ActionBar mActionBar;
     protected List<Integer> mNextTrainTimes;
     protected String mCurrentLine;
     protected String mCurrentStation;
     protected String mCurrentStopId;
     protected String mCurrentLineDirection;
     protected String mStartAlertsAfter;
 
     protected Marker mCurrentLocationMarker = null;
     private TextView mLatitudeTextView;
     private TextView mLongitudeTextView;
     private LocationManager mLocationManager;
     private String mProvider;
     private double mCurrentLat;
     private double mCurrentLng;
     private LatLng mCurrentCoordinates;
 
     private boolean havelocation;
     //protected ActionBarSherlock sherlock = ActionBarSherlock.wrap(this);
 
     private boolean[] mLinesCheckBoxes;
 
 
     /* Request updates at startup */
     @Override
     protected void onResume() {
         super.onResume();
         mLocationManager.requestLocationUpdates(mProvider, 400, 1, this);
     }
 
     /* Remove the locationlistener updates when Activity is paused */
     @Override
     protected void onPause() {
         super.onPause();
         mLocationManager.removeUpdates(this);
     }
 
     private void drawCurrentPositionMarker()
     {
         if (!havelocation)
             return;
         if (mCurrentLocationMarker != null)
             mCurrentLocationMarker.remove();
 
         mCurrentLocationMarker = map.addMarker(new MarkerOptions().position(mCurrentCoordinates)
                 .title("Your location")
 
         );
     }
     @Override
     public void onLocationChanged(Location location) {
         havelocation = true;
          mCurrentLat =  (location.getLatitude());
          mCurrentLng = (location.getLongitude());
         Toast.makeText(this, "LOC CHANGED", Toast.LENGTH_SHORT).show();
 
 
      //   Toast.makeText(this, "lat = " + String.valueOf(lng), Toast.LENGTH_LONG).show();
    //     latituteField.setText(String.valueOf(lat));
      //   longitudeField.setText(String.valueOf(lng));
 
         mCurrentCoordinates = new LatLng(mCurrentLat, mCurrentLng);
 
           drawCurrentPositionMarker();
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void onProviderEnabled(String provider) {
         Toast.makeText(this, "Enabled new provider " + provider,
                 Toast.LENGTH_SHORT).show();
 
     }
 
     @Override
     public void onProviderDisabled(String provider) {
         Toast.makeText(this, "Disabled provider " + provider,
                 Toast.LENGTH_SHORT).show();
     }
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.map);
 
 
         map = ((SherlockMapFragment)(getSupportFragmentManager().findFragmentById(R.id.map))).getMap();
         mMapOptionsBar = findViewById(R.id.map_options_bar);
         mSelectTripByLinesScreen = findViewById(R.id.select_trip_by_line_screen);
         mViewLinesScreen = findViewById(R.id.view_lines_screen);
 
         mStationsListScreen = findViewById(R.id.stations_list_screen);
         mScheduleAlertsOptionsBar = findViewById(R.id.schedule_alerts_option_bar);
         mScheduleAlertsScreen = findViewById(R.id.schedule_alerts_screen);
 
         mActionBar = getSupportActionBar();
 
         havelocation = false;
 
         mLinesCheckBoxes = new boolean[UIUtils.NUMBER_SUBWAY_LINES];
 
         LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
         boolean enabled = service
                 .isProviderEnabled(LocationManager.GPS_PROVIDER);
 
 
 // Check if enabled and if not send user to the GPS settings
 // Better solution would be to display a dialog and suggesting to
 // go to the settings
         if (!enabled) {
             Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
             startActivity(intent);
         }
         // Get the location manager
 
         mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         // Define the criteria how to select the locatioin provider -> use
         // default
         Criteria criteria = new Criteria();
         mProvider = mLocationManager.getBestProvider(criteria, false);
         Location location = mLocationManager.getLastKnownLocation(mProvider);
 
      //   locationManager.requestLocationUpdates(provider, 500, 0, this);
 
         // needed for geo fix to work with emulator (ie. emulating GPS locations)
         mLocationManager.addTestProvider("test",false,true,false,false,false,false,false,Criteria.POWER_LOW,Criteria.ACCURACY_FINE);
 
 
         /* Map setup
         ================================================================================================================*/
 
         // Hide default zoom buttons
         map.getUiSettings().setZoomControlsEnabled(false);
 
         // Center and zoom camera on New York City ... default onStart case
         map.animateCamera(CameraUpdateFactory.newLatLngZoom(MANHATTAN, DEFAULT_ZOOM_LEVEL));
 
         // Add subway polylines
         SubwayLinePlotter.plotLine("1", mQueryHelper, map);
         SubwayLinePlotter.plotLine("2", mQueryHelper, map);
         SubwayLinePlotter.plotLine("3", mQueryHelper, map);
         SubwayLinePlotter.plotLine("4", mQueryHelper, map);
         SubwayLinePlotter.plotLine("5", mQueryHelper, map);
         SubwayLinePlotter.plotLine("6", mQueryHelper, map);
         SubwayLinePlotter.plotLine("7", mQueryHelper, map);
 
         /* Map screen UI setup
         ================================================================================================================*/
 
         Button zoominButton = (Button) findViewById(R.id.zoomin);
         zoominButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 map.animateCamera(CameraUpdateFactory.zoomIn());
 
             }
         });
         Button zoomoutButton = (Button) findViewById(R.id.zoomout);
         zoomoutButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 map.animateCamera(CameraUpdateFactory.zoomOut());
 
             }
         });
 
         Button tripByLinesButton = (Button) findViewById(R.id.plan_trip_lines_button);
         tripByLinesButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Hide the options bar with trip selector buttons
                 mMapOptionsBar.setVisibility(View.GONE);
 
                 // Show the select trip by lines screen
 
                 mSelectTripByLinesScreen.setVisibility(View.VISIBLE);
                 mSelectTripByLinesScreen.requestLayout();
                 mSelectTripByLinesScreen.bringToFront();
 
                 //setContentView(mSelectTripByLinesScreen);
             }
         });
 
 
         Button backButtonLines = (Button) findViewById(R.id.go_back_button_select_line);
         backButtonLines.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Hide the select trip by lines screen
                 mSelectTripByLinesScreen.setVisibility(View.GONE);
 
                 // Show the options bar with trip selector buttons
                 mMapOptionsBar.setVisibility(View.VISIBLE);
 
             }
         });
 
         Button backButtonStations = (Button) findViewById(R.id.go_back_button_stations_list);
         backButtonStations.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Hide the select stations screen
                 mStationsListScreen.setVisibility(View.GONE);
                 setDefaultActionBar();
 
                 //Show the select trip by lines screen
                 mSelectTripByLinesScreen.setVisibility(View.VISIBLE);
 
             }
         });
 
         Button startOverButton = (Button) findViewById(R.id.start_over_button);
         startOverButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 // Hide the schedule alerts options bar
                 mScheduleAlertsOptionsBar.setVisibility(View.GONE);
 
                 // Show the options bar with trip selector buttons
                 mMapOptionsBar.setVisibility(View.VISIBLE);
 
                 // Center and zoom camera on New York City ... default onStart case
                 map.animateCamera(CameraUpdateFactory.newLatLngZoom(MANHATTAN, DEFAULT_ZOOM_LEVEL));
             }
         });
 
         Button scheduleAlertsButton = (Button) findViewById(R.id.schedule_alerts_button);
         scheduleAlertsButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 setupSubwayTimesList();
                 setupAlertTimesSelector();
             }
         });
 
         Button backButtonSchedule = (Button) findViewById(R.id.go_back_button_schedule_screen);
         backButtonSchedule.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Hide the schedule alerts screen
                 mScheduleAlertsScreen.setVisibility(View.GONE);
 
                 // Show the schedule alerts options bar
                 mScheduleAlertsOptionsBar.setVisibility(View.VISIBLE);
             }
         });
 
         Button setAlertsButton = (Button) findViewById(R.id.set_alerts_button);
         setAlertsButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Hide the schedule alerts screen
                 mScheduleAlertsScreen.setVisibility(View.GONE);
 
                 // Show the schedule alerts options bar
                 mScheduleAlertsOptionsBar.setVisibility(View.VISIBLE);
 
                 long notificationDelay = 5000;
                 //long notificationDelay = UIUtils.getNotificationTime(mStartAlertsAfter);
                 if (notificationDelay > 0) {
                     /*
                     Timer notificationTimer = new Timer();
                     notificationTimer.schedule(new NotificationTimerTask(), notificationDelay);
                     */
 
                     Calendar cal = Calendar.getInstance();
 
                     Intent alarmintent = new Intent(getApplicationContext(), AlarmReceiver.class);
                     mNextTrainTimes = mGtfsFeed.getNextTrainsArrival(mCurrentLine, mCurrentStopId + mCurrentLineDirection);
                     if (mNextTrainTimes.isEmpty()) return;
                     String minuteString = (mNextTrainTimes.get(0) == 1) ? " minute." : " minutes.";
                     alarmintent.putExtra("note","Next subway is arriving in " + mNextTrainTimes.get(0) + minuteString);
                     PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 1,
                             alarmintent,PendingIntent.FLAG_UPDATE_CURRENT|  Intent.FILL_IN_DATA);
 
                     AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                     am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + 5000, sender);
                     Toast.makeText(MapActivity.this , "Alarm set", Toast.LENGTH_SHORT).show();
 
                 }
             }
         });
 
 
         Button lineButton_1 = (Button) findViewById(R.id.line_1_button);
         lineButton_1.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 selectLine("1");
             }
         });
 
         Button lineButton_2 = (Button) findViewById(R.id.line_2_button);
         lineButton_2.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 selectLine("2");
             }
         });
 
         Button lineButton_3 = (Button) findViewById(R.id.line_3_button);
         lineButton_3.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 selectLine("3");
             }
         });
 
         Button lineButton_4 = (Button) findViewById(R.id.line_4_button);
         lineButton_4.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 selectLine("4");
             }
         });
 
         Button lineButton_5 = (Button) findViewById(R.id.line_5_button);
         lineButton_5.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 selectLine("5");
             }
         });
 
         Button lineButton_6 = (Button) findViewById(R.id.line_6_button);
         lineButton_6.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 selectLine("6");
             }
         });
 
         Button lineButton_7 = (Button) findViewById(R.id.line_7_button);
         lineButton_7.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 selectLine("7");
             }
         });
 
         Button lineButton_S = (Button) findViewById(R.id.line_S_button);
         lineButton_S.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 selectLine("S");
             }
         });
 
         CheckBox lineCheckbox_1 = (CheckBox) findViewById(R.id.checkbox_1);
         lineCheckbox_1.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Toggle the approriate value in the checkbox array
                 mLinesCheckBoxes[UIUtils.LINE_1_POS] = !mLinesCheckBoxes[UIUtils.LINE_1_POS];
             }
         });
 
         CheckBox lineCheckbox_2 = (CheckBox) findViewById(R.id.checkbox_2);
         lineCheckbox_2.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Toggle the approriate value in the checkbox array
                 mLinesCheckBoxes[UIUtils.LINE_2_POS] = !mLinesCheckBoxes[UIUtils.LINE_2_POS];
             }
         });
 
         CheckBox lineCheckbox_3 = (CheckBox) findViewById(R.id.checkbox_3);
         lineCheckbox_3.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Toggle the approriate value in the checkbox array
                 mLinesCheckBoxes[UIUtils.LINE_3_POS] = !mLinesCheckBoxes[UIUtils.LINE_3_POS];
             }
         });
 
         CheckBox lineCheckbox_4 = (CheckBox) findViewById(R.id.checkbox_4);
         lineCheckbox_4.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Toggle the approriate value in the checkbox array
                 mLinesCheckBoxes[UIUtils.LINE_4_POS] = !mLinesCheckBoxes[UIUtils.LINE_4_POS];
             }
         });
 
         CheckBox lineCheckbox_5 = (CheckBox) findViewById(R.id.checkbox_5);
         lineCheckbox_5.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Toggle the approriate value in the checkbox array
                 mLinesCheckBoxes[UIUtils.LINE_5_POS] = !mLinesCheckBoxes[UIUtils.LINE_5_POS];
             }
         });
 
         CheckBox lineCheckbox_6 = (CheckBox) findViewById(R.id.checkbox_6);
         lineCheckbox_6.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Toggle the approriate value in the checkbox array
                 mLinesCheckBoxes[UIUtils.LINE_6_POS] = !mLinesCheckBoxes[UIUtils.LINE_6_POS];
             }
         });
 
         CheckBox lineCheckbox_7 = (CheckBox) findViewById(R.id.checkbox_7);
         lineCheckbox_7.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Toggle the approriate value in the checkbox array
                 mLinesCheckBoxes[UIUtils.LINE_7_POS] = !mLinesCheckBoxes[UIUtils.LINE_7_POS];
             }
         });
 
         CheckBox lineCheckbox_S = (CheckBox) findViewById(R.id.checkbox_S);
         lineCheckbox_S.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Toggle the approriate value in the checkbox array
                 mLinesCheckBoxes[UIUtils.LINE_S_POS] = !mLinesCheckBoxes[UIUtils.LINE_S_POS];
             }
         });
 
         Button viewLinesButton = (Button) findViewById(R.id.view_lines_button);
         viewLinesButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Hide the options bar with trip selector buttons
                 mMapOptionsBar.setVisibility(View.GONE);
 
                 // Show the view lines selector screen
                 mViewLinesScreen.setVisibility(View.VISIBLE);
             }
         });
 
         Button displayButtonViewLines = (Button) findViewById(R.id.display_button_view_lines_screen);
         displayButtonViewLines.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Display the selected lines on map
                 map.clear();
                 UIUtils.plotSelectedLines(mLinesCheckBoxes, mQueryHelper, map);
 
                 // Hide the view lines selector screen
                 mViewLinesScreen.setVisibility(View.GONE);
 
                 // Show the options bar with trip selector buttons
                 mMapOptionsBar.setVisibility(View.VISIBLE);
             }
         });
 
         Button cancelButtonViewLines = (Button) findViewById(R.id.cancel_button_view_lines_screen);
         cancelButtonViewLines.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Hide the view lines selector screen
                 mViewLinesScreen.setVisibility(View.GONE);
 
                 // Show the options bar with trip selector buttons
                 mMapOptionsBar.setVisibility(View.VISIBLE);
             }
         });
 
 
         //mGtfsFeed.sampleAPILogger();
 
         //BackendUtils.getShaKey(this);     //code to troubleshoot if key for google maps api is incorrect
 
     }
 
 
     /* Map setup helper functions
     ====================================================================================================================*/
 
     public void selectLine(String line) {
 
         // Hide the select trip by lines screen
         mSelectTripByLinesScreen.setVisibility(View.GONE);
 
         // Set up stations list screen using list view adapter
         setupStationsListScreen(line);
 
         // Show the stations list screen
         mStationsListScreen.setVisibility(View.VISIBLE);
 
         mActionBar.setCustomView(R.layout.actionbar_custom_stationlist);
         mActionBar.setDisplayShowCustomEnabled(true);
         final RadioGroup selectDirection = (RadioGroup) findViewById(R.id.select_dir);
         final String theline = line;
         selectDirection.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 setupStationsListScreen(theline);
             }
         });
 
 
         //Toast.makeText(MapActivity.this, "Line " + line + " selected!", Toast.LENGTH_LONG).show();
 
     }
 
     private List<LatLng> decodePoly(String encoded) {
 
         List<LatLng> poly = new ArrayList<LatLng>();
         int index = 0, len = encoded.length();
         int lat = 0, lng = 0;
 
         while (index < len) {
             int b, shift = 0, result = 0;
             do {
                 b = encoded.charAt(index++) - 63;
                 result |= (b & 0x1f) << shift;
                 shift += 5;
             } while (b >= 0x20);
             int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
             lat += dlat;
 
             shift = 0;
             result = 0;
             do {
                 b = encoded.charAt(index++) - 63;
                 result |= (b & 0x1f) << shift;
                 shift += 5;
             } while (b >= 0x20);
             int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
             lng += dlng;
             LatLng p = new LatLng((((double) lat / 1E5)),(((double) lng / 1E5)));
 
 
             poly.add(p);
         }
 
         return poly;
     }
 
     public void setupStationsListScreen(final String line) {
 
         mCurrentLine = line;
 
         ArrayList<StopData> stopDataList = mQueryHelper.queryForLineStops(line);
 
         int iconResId = UIUtils.getIconForLine(line.charAt(0));
         StationListAdapter stationListAdapter = new StationListAdapter(MapActivity.this, R.layout.station_list_item,
                 stopDataList, iconResId);
 
         ListView stationsListView = (ListView) findViewById(R.id.stations_list);
         stationsListView.setAdapter(stationListAdapter);
         stationsListView.setClickable(true);
         stationsListView.setItemsCanFocus(true);
 
         stationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
 
                 // Pre-processing before marker can be set up on the map
                 StopData stopData = (StopData) adapterView.getAdapter().getItem(position);
                 int iconResId = UIUtils.getIconForLine(line.charAt(0));
                 String stationLat = stopData.getStopLat();
                 String stationLon = stopData.getStopLon();
 
                 mCurrentStation = stopData.getStopName();
 
                 String stopId = mQueryHelper.queryForStopId(stationLat, stationLon);
                 mCurrentStopId = stopId;
 
                 // Trips suffixed with "S" are northbound!!
                 String lineDirection = "S"; //default
                 RadioGroup selectDirection = (RadioGroup) findViewById(R.id.select_dir);
                 int selectedID = selectDirection.getCheckedRadioButtonId();
                 if (selectedID == R.id.dir_south) {
                     lineDirection = "N";
                 }
                 mCurrentLineDirection = lineDirection;
 
                 mNextTrainTimes = mGtfsFeed.getNextTrainsArrival(line, stopId + lineDirection);
                 String markerTitle = mNextTrainTimes.isEmpty() ? "Live data not available." : mCurrentStation;
                 String minuteString = (!mNextTrainTimes.isEmpty() && mNextTrainTimes.get(0) == 1) ? " minute." : " minutes.";
                 String markerSnippet = mNextTrainTimes.isEmpty() ? "" : "In " + mNextTrainTimes.get(0) + minuteString;
 
                 // Hide the stations list screen
                 mStationsListScreen.setVisibility(View.GONE);
                 setDefaultActionBar();
 
                 if (mNextTrainTimes.isEmpty()) {
                     /// Show the options bar with trip selector buttons
                     mMapOptionsBar.setVisibility(View.VISIBLE);
                 } else {
                     // Show the schedule alerts options bar
                     mScheduleAlertsOptionsBar.setVisibility(View.VISIBLE);
                 }
 
                 // Set up marker on google map
                 LatLng stationCoordinates = new LatLng(Double.parseDouble(stationLat), Double.parseDouble(stationLon));
                 map.clear();
                 // Add subway polylines
                 SubwayLinePlotter.plotLine(line, mQueryHelper, map);
                 map.animateCamera(CameraUpdateFactory.newLatLngZoom(stationCoordinates, CLOSE_UP_ZOOM_LEVEL));
                 Marker marker = map.addMarker(new MarkerOptions().position(stationCoordinates)
                         .title(markerTitle)
                         .snippet(markerSnippet)
                         .icon(BitmapDescriptorFactory.fromResource(iconResId)));
                 marker.showInfoWindow();
 
                 // show directions to selected station from current location
                 if (!havelocation)
                      return;
                 showPathFromCurrentLocation(stationLat, stationLon);
             }
         });
 
     }
 
     public void showPathFromCurrentLocation(String stationLat, String stationLon) {
         float[] distbetween = new float[1];
          double LAT_NYC= 40.714623;
         double LNG_NYC =  -74.006605;
        float METERS_PER_MILE = 1609;
        float DIST_CUTOFF = 50 * METERS_PER_MILE;  //in meters (50 miles)
         Location.distanceBetween(mCurrentLat, mCurrentLng, Double.parseDouble(stationLat), Double.parseDouble(stationLon), distbetween);
         if (distbetween[0] > DIST_CUTOFF)
         {
             return;
         }
         String url = "https://maps.googleapis.com/maps/api/directions/json";
         String charset = "UTF-8";
         String origin = Double.toString(mCurrentLat) + "," + Double.toString(mCurrentLng);
         String destination = stationLat + "," + stationLon;
 
         HttpClient httpClient = new DefaultHttpClient();
         try {
             String query = String.format("origin=%s&destination=%s&sensor=true&mode=walking",
                     URLEncoder.encode(origin, charset),
                     URLEncoder.encode(destination, charset));
 
 
             AsyncHttpClient client = new AsyncHttpClient();
             client.get(url + "?" + query, new AsyncHttpResponseHandler() {
                 @Override
                 public void onSuccess(String response) {
 
 
                     Document doc = null;
                     try {
                         JSONObject obj = new JSONObject(response);
                         JSONArray jarr = (JSONArray) obj.getJSONArray("routes");
                         //  Log.e("SHENIL", "LEN: " + Integer.toString(jarr.length()));
                         JSONObject theroute = jarr.getJSONObject(0);
                         //   Log.e("SHENIL", theroute.toString());
                         JSONObject thepath = theroute.getJSONObject("overview_polyline");
 
                         String thedirections = thepath.getString("points");
 
                         List<LatLng> thepoints = decodePoly(thedirections);
 
                         for (LatLng ll : thepoints)
                             Log.e("SHENIL", "curval:" + ll.toString());
                         Log.e("SHENIL", "SIZE: " + Integer.toString(thepoints.size()));
                         PolylineOptions lineopts = new PolylineOptions()
                                 .color(Color.CYAN)
                                 .width(10);
 
                         for (LatLng ll : thepoints)
                             lineopts.add(ll);
 
                         Polyline pl = map.addPolyline(lineopts);
                         drawCurrentPositionMarker();
 
                         //   pl.setVisible(true);
 
 
                     } catch (Exception e) {
                         Toast.makeText(MapActivity.this, "Error finding directions to station", Toast.LENGTH_SHORT).show();
                     }
 
                      /*
                             Toast.makeText(MapActivity.this, "Success", Toast.LENGTH_SHORT).show();
                             Toast.makeText(MapActivity.this, response, Toast.LENGTH_LONG).show();*/
                 }
             });
 
         } catch (Exception e) {
             //e.printStackTrace();
 
 
             for (StackTraceElement ee : e.getStackTrace()) {
                 Log.e("SHENIL", ee.toString());
             }
             //  Log.e("SHENIL",e.getStackTrace().toString());
             Toast.makeText(MapActivity.this, "Error directions: " + e.getMessage(), Toast.LENGTH_LONG).show();
         } finally {
             httpClient.getConnectionManager().shutdown();
         }
     }
 
     public void setupSubwayTimesList() {
 
         SubwayTimesListAdapter timesAdapter = new SubwayTimesListAdapter(this, R.layout.subway_times_list_item,
                 mNextTrainTimes);
         ListView subwayTimesListView = (ListView) findViewById(R.id.subway_times_list);
         subwayTimesListView.setAdapter(timesAdapter);
 
         TextView currentStationName = (TextView) findViewById(R.id.schedule_station_name);
         currentStationName.setText(mCurrentStation);
 
         ImageView currentStationIcon = (ImageView) findViewById(R.id.schedule_station_icon);
         currentStationIcon.setImageResource(UIUtils.getIconForLine(mCurrentLine.charAt(0)));
 
         // Hide the schedule alerts options bar
         mScheduleAlertsOptionsBar.setVisibility(View.GONE);
 
         // Show the schedule alerts screen
         mScheduleAlertsScreen.setVisibility(View.VISIBLE);
 
     }
 
     public void setupAlertTimesSelector() {
         Spinner alertTimesSelector = (Spinner) findViewById(R.id.alert_times_selector);
         alertTimesSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 //Toast.makeText(MapActivity.this, "Alerts in " + parent.getItemAtPosition(position).toString() +
                 //        " selected!", Toast.LENGTH_LONG).show();
                 mStartAlertsAfter = parent.getItemAtPosition(position).toString();
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> parent) {
             }
         });
 
 
     }
 
     public void setupNotificationForAlert() {
 
         mNextTrainTimes = mGtfsFeed.getNextTrainsArrival(mCurrentLine, mCurrentStopId + mCurrentLineDirection);
         if (mNextTrainTimes.isEmpty()) return;
         String minuteString = (mNextTrainTimes.get(0) == 1) ? " minute." : " minutes.";
         String contentText = "Next subway is arriving in " + mNextTrainTimes.get(0) + minuteString;
 
         // Prepare intent which is triggered if the notification is selected
         Intent intent = new Intent(this, MapActivity.class);
         PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
 
         // Build notification
         Notification notification = new Notification.Builder(this)
                 .setContentTitle("Subway Alert")
                 .setContentText(contentText)
                 .setLargeIcon(((BitmapDrawable) getResources().getDrawable(R.drawable.metro_icon_transparent)).getBitmap())
                 .setSmallIcon(R.drawable.metro_icon_transparent)
                 .setContentIntent(pIntent).build();
 
         NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
         // Hide the notification after its selected
         notification.flags |= Notification.FLAG_AUTO_CANCEL;
         // Vibrate/ring/light for notification
         notification.defaults |= Notification.DEFAULT_ALL;
         //notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
 
         notificationManager.notify(0, notification);
     }
 
     private class NotificationTimerTask extends TimerTask {
         public void run() {
             setupNotificationForAlert();
         }
     }
 
     public void setDefaultActionBar() {
         mActionBar.setDisplayShowCustomEnabled(false);
     }
 }
