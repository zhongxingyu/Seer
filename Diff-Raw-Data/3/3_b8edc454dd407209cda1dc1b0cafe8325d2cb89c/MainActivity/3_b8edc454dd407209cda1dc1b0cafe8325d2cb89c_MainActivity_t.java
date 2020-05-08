 package com.application.wakeapp;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.concurrent.TimeUnit;
 
 public class MainActivity extends Activity {
 
     private Location finalDestination;
     private Location myLocation=null;
     private SearchView mSearchView;
     private ListView mListView;
     private ArrayAdapter<String> mAdapter;
     private Button mButton;
     private TextView mTextView;
     private ArrayList<String> stationList;
     private ArrayList<String> stationListNameOnly;
     private LocationManager locationManager;
     private Boolean isServiceStarted = Boolean.FALSE;
     private String stationName;
     private Float distance;
     private DataBaseHandler mDataBaseHandler;
     private Boolean isThereAnDatabase = Boolean.FALSE;
     private SharedPreferences prefs;
     private int searchRadius;
     private int outsidethreshold;
     private Boolean usedatabase;
     private final String PATH_TO_DATABASE =
             "data/data/com.application.wakeapp/databases/stationNames";
     private static final String LOG_TAG = "WakeApp";
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         Log.d(LOG_TAG," onCreate " + isServiceStarted);
 
         prefs =  PreferenceManager.getDefaultSharedPreferences(this);
         outsidethreshold = Integer.parseInt(prefs.getString("outsidethreshold","500"));
         searchRadius = Integer.parseInt(prefs.getString("searchradius","5000"));
 
         usedatabase = prefs.getBoolean("usedatabase",Boolean.TRUE);
         prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
             @Override
             public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                 Log.d(LOG_TAG,"onSharedPreferenceChanged: " + s);
                 // If the user have changed searchradius we need to discard the database
                 // and re-fetch the station list from server.
                 if ( s.equals("searchradius") ){
                     getApplicationContext().deleteDatabase(mDataBaseHandler.getDatabaseName());
                 }
 
             }
         });
 
         findGPSPosition();
 
         if ( checkDataBase()){
             Log.d(LOG_TAG,"Database exists");
             isThereAnDatabase = Boolean.TRUE;
         }
 
         finalDestination = new Location("Destination");
         stationList = new ArrayList<String>();
         stationListNameOnly = new ArrayList<String>();
         mDataBaseHandler = new DataBaseHandler(MainActivity.this);
 
         new Background().execute();
 
         mSearchView = (SearchView) findViewById(R.id.searchView);
         mListView   = (ListView) findViewById(R.id.listView);
         mButton     = (Button) findViewById(R.id.button);
         mTextView   = (TextView) findViewById(R.id.textView);
 
         mListView.setAdapter(mAdapter = new ArrayAdapter<String>(
                             this,android.R.layout.test_list_item,
                     stationListNameOnly));
 
         mButton.setOnClickListener(new View.OnClickListener(){
 
             @Override
             public void onClick(View view) {
                 Intent newIntent = new Intent(MainActivity.this,BackgroundService.class);
 
                 newIntent.putExtra("lng",finalDestination.getLongitude());
                 newIntent.putExtra("lat",finalDestination.getLatitude());
                 startService(newIntent);
 
                 Intent startMain = new Intent(Intent.ACTION_MAIN);
                 startMain.addCategory(Intent.CATEGORY_HOME);
                 startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 startActivity(startMain);
 
                 isServiceStarted = Boolean.TRUE;
                 Log.d(LOG_TAG, finalDestination.getLongitude() + " " + isServiceStarted);
             }
         });
         mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 stationName = ((TextView) view).getText().toString();
                 Double lat = 0.0, lng = 0.0;
 
                 for (String item : stationList) {
                     if (item.startsWith(stationName)) {
                         lat = getLatitude(item);
                         lng = getLongitude(item);
                         break;
                     }
                 }
                 finalDestination.setLatitude(lat);
                 finalDestination.setLongitude(lng);
 
                 distance = myLocation.distanceTo(finalDestination);
 
                 mListView.setVisibility(View.INVISIBLE);
                 mButton.setVisibility(View.VISIBLE);
                 mTextView.setVisibility(View.VISIBLE);
                 mTextView.setText(getTravelInfo());
 
                 hideSoftKeyboard();
             }
 
             protected void hideSoftKeyboard() {
                 InputMethodManager imm = (InputMethodManager)
                         getSystemService(Activity.INPUT_METHOD_SERVICE);
                 imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
             }
         });
         mListView.setTextFilterEnabled(true);
         mSearchView.setSubmitButtonEnabled(false);
         mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
 
             @Override
             public boolean onQueryTextSubmit(String s) {
                 return false;
             }
 
             @Override
             public boolean onQueryTextChange(String newText) {
                 if (TextUtils.isEmpty(newText)) {
                     mListView.clearTextFilter();
                     mListView.setVisibility(View.INVISIBLE);
                     mButton.setVisibility(View.INVISIBLE);
                     mTextView.setVisibility(View.INVISIBLE);
                 } else {
                     mListView.setFilterText(newText.toString());
                     mListView.setVisibility(View.VISIBLE);
                     mTextView.setVisibility(View.INVISIBLE);
                 }
                 return true;
             }
         });
 
     }
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     private Double getLatitude(String s){
         String[] tmp = s.split(" ");
         return Double.parseDouble(tmp[tmp.length-2]);
     }
     private Double getLongitude(String s){
         String[] tmp = s.split(" ");
         return Double.parseDouble(tmp[tmp.length-1]);
     }
     private Boolean tryGetQuickGPSFix(){
         Boolean ret = Boolean.FALSE;
 
         Criteria criteria = new Criteria();
         criteria.setSpeedAccuracy(Criteria.ACCURACY_LOW);
 
         String name = locationManager.getBestProvider(criteria,false);
 
         Location tmp = locationManager.getLastKnownLocation(name);
 
         if (tmp == null)
             return Boolean.FALSE;
 
         long delta = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - tmp.getTime());
 
         // If the location is older then 3minutes
         // then we should consider it out-dated
         if ( (int)delta < (60*3) ){
             Log.d(LOG_TAG,"We got Quick GPS fix: " + delta);
             myLocation = tmp;
             ret = Boolean.TRUE;
         }
 
         return ret;
     }
     private void findGPSPosition(){
 
         locationManager = (LocationManager)
                 getSystemService(Context.LOCATION_SERVICE);
 
         // If there is an position that is not out-dated
         // we should use it to get fast GPS coordinates.
         // Otherwise we need to use the GPS which takes
         // more time and consumes more power.
         if (tryGetQuickGPSFix())
             return;
 
         LocationListener locationListener = new LocationListener() {
             @Override
             public void onLocationChanged(Location location) {
                 myLocation = location;
             }
 
             @Override
             public void onStatusChanged(String s, int i, Bundle bundle) {
 
             }
 
             @Override
             public void onProviderEnabled(String s) {
 
             }
 
             @Override
             public void onProviderDisabled(String s) {
 
             }
         };
 
         locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,
                 locationListener,null);
 
         locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
                 locationListener,null);
 
         locationManager.requestSingleUpdate(LocationManager.PASSIVE_PROVIDER,
                 locationListener,null);
 
     }
     // Check if database exits
     private boolean checkDataBase() {
         SQLiteDatabase checkDB = null;
         try {
             checkDB = SQLiteDatabase.openDatabase(
                     PATH_TO_DATABASE, null,
                     SQLiteDatabase.OPEN_READONLY);
 
             checkDB.close();
         } catch (SQLiteException e) {
             // database doesn't exist yet.
         }
         return checkDB != null ? true : false;
     }
 
     class Background extends AsyncTask<String, Integer, String> {
 
         private void addPreviousLocation() {
             Log.d(LOG_TAG,"addPreviousLocation");
             Location l = new Location("previousSearch");
             l.setLatitude(myLocation.getLatitude());
             l.setLongitude(myLocation.getLongitude());
 
             mDataBaseHandler.addLocation(l);
         }
         private void populateDatabase(){
             Log.d(LOG_TAG,"populateDatabase");
             Stations stations =
                     new Stations(myLocation.getLongitude(),
                             myLocation.getLatitude(),
                             searchRadius);
 
             stationList = stations.getAllStations(Boolean.FALSE);
 
             stationListNameOnly = removeCoordinates(stationList);
 
             // Very ugly way to parse the location string that
             // looks like this
             // "Lund cental station 53.213 15.235"
             // "Aroboga 56.542 17.34456"
             // when we have extracted name and coordinates we
             // add it to the database
             for (String item : stationList){
                 StringBuilder sb = new StringBuilder();
                 String[] tmp = item.split(" ");
                 int items = tmp.length;
                 Double lat = Double.parseDouble(tmp[(items-2)]);
                 Double lng = Double.parseDouble(tmp[(items-1)]);
                 for ( int i=0;i<items-2;i++){
                     sb.append(tmp[i] + " ");
                 }
                 String name = sb.toString();
 
                 Location l = new Location(name);
                 l.setLatitude(lat);
                 l.setLongitude(lng);
                 
                 mDataBaseHandler.addLocation(l);
             }
         }
         private Boolean haveWeBeenHereBefore(){
             Boolean ret = Boolean.FALSE;
             Float distanceTo=0f;
             ArrayList<Location> locations = mDataBaseHandler.getOnlyPreviousSearchesLocation();
 
             for (Location l : locations){
                     if ( myLocation.distanceTo(l) < outsidethreshold){
                         distanceTo = myLocation.distanceTo(l);
                         ret = Boolean.TRUE;
                         break;
                     }
 
             }
             Log.d(LOG_TAG,"haveWeBeenHereBefore TRUE distance: "
                     + distanceTo + "meters");
             return ret;
         }
         @SuppressWarnings("unused")
 		private void fetchFromServer(){
             Log.d(LOG_TAG,"fetchFromServer");
             Stations stations =
                     new Stations(myLocation.getLongitude(),
                             myLocation.getLatitude(),
                             searchRadius);
 
             stationList = stations.getAllStations(Boolean.FALSE);
 
             stationListNameOnly = removeCoordinates(stationList);
         }
         private void fetchFromCache(){
             Log.d(LOG_TAG,"fetchFromCache");
             stationList = mDataBaseHandler.getAllButPreviousString();
             stationListNameOnly = removeCoordinates(stationList);
         }
 
         private ArrayList<String> removeCoordinates(ArrayList<String> l){
             ArrayList<String> newList = new ArrayList<String>();
 
             for ( int i=0;i<l.size();i++){
                 StringBuilder sb = new StringBuilder();
                 String tmp = l.get(i);
                 String[] temp = tmp.split(" ");
                 for ( int j=0;j<temp.length-2;j++){
                     sb.append(temp[j] + " ");
                 }
                 newList.add(sb.toString());
             }
 
             return  newList;
         }
         protected String doInBackground(String... urls) {
             long startTime = System.currentTimeMillis();
             do{//We need to get an position
                 try {
                     Thread.sleep(1000);
                     Log.d(LOG_TAG,"looking for GPS");
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }while(myLocation == null);
             Log.d(LOG_TAG,"Time to get GPS; " +
                     (System.currentTimeMillis() - startTime)/1000 + " sec");
 
             // First start-up we don't have an database.
             // Download station list from server and
             // populate database.
             // else we have the data locally so no need to
             // fetch from server.
             if ( !isThereAnDatabase || !haveWeBeenHereBefore() || !usedatabase)
                 populateDatabase();
             else
                 fetchFromCache();
 
             if (!haveWeBeenHereBefore())
             	addPreviousLocation();
 
             Log.d(LOG_TAG,"Pos found: lat: " +
                     myLocation.getLatitude() + " lng: " +
                     myLocation.getLongitude());
 
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     mAdapter.addAll(stationListNameOnly);
                     mAdapter.notifyDataSetChanged();
                     Log.d(LOG_TAG,"notifyDataSetChanged len: " + stationListNameOnly.size());
                     Toast t = Toast.makeText(getApplicationContext(),
                             "Station list updated",
                             Toast.LENGTH_LONG);
                     t.setGravity(Gravity.CENTER ,0, 0);
                     t.show();
                 }
             });
             return null;
         }
     }
     @Override
     protected void onResume() {
         super.onResume();
 
        if (isServiceStarted){            
             mTextView.setVisibility(View.VISIBLE);
             mButton.setVisibility(View.VISIBLE);
             mTextView.setText(getTravelInfo());
 
             // Stop the background service when we
             // resume the UI.
             stopService(new Intent(MainActivity.this,
                     BackgroundService.class));
 
             // Find our new position if we have moved
             findGPSPosition();
             new Background().execute();
         }
         isServiceStarted = Boolean.FALSE;
     }
     @Override
     protected void onNewIntent(Intent intent){
     	super.onNewIntent(intent);
     	setIntent(intent);
     	String msg;
     	    	
     	msg = intent.getStringExtra("AlarmActivity");
     	
     	Log.d(LOG_TAG,"MainActivity onNewIntent " + msg);
     	
     	// If we get an intent from the AlarmActivity that means
     	// we should exit application and set it to on-first-start-mode
         if ( msg != null && msg.equals("PingByAlarm")){
         	mSearchView.setIconified(true);
             mListView.setVisibility(View.INVISIBLE);
             mButton.setVisibility(View.INVISIBLE);
             mTextView.setVisibility(View.INVISIBLE);
             finish();
         }
     }
     @Override
     protected void onDestroy(){
         super.onDestroy();
 
         stopService(new Intent(MainActivity.this,
                 BackgroundService.class));
         isServiceStarted = Boolean.FALSE;
 
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         if (item.getTitle().equals("Settings")){
             Intent i = new Intent(MainActivity.this,WakeAppPreferences.class);
             startActivity(i);
         }
 
         return true;
     }
     private String getTravelInfo(){
         String dist;
 
         if (distance > 1000)
             dist = String.format("%.3g km\n",distance/1000);
         else
             dist = String.format("%.3g meter\n",distance);
 
         String info = "Final destination: " + stationName + "\n" +
                       "Distance to destination: " + dist +
                       "Current speed: " + myLocation.getSpeed()*(3.6) + " km/h\n";
 
         return info;
     }
 
 }
