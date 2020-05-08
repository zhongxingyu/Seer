 package org.zmonkey.beacon;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TabHost;
 import android.widget.TextView;
 import org.zmonkey.beacon.data.DataManager;
 
 public class MainActivity extends TabActivity implements LocationListener
 {
     //public static final String PREFS_NAME = "org.zmonkey.beacon";
     public static MainActivity main;
     private static final int LOCATION_UPDATE_TIME = 60000;
     private static final int LOCATION_UPDATE_DISTANCE = 50;
     
     private static final int OPTIONS_SETTINGS = 0;
     private static final int OPTIONS_REFRESH = 1;
     private static final int OPTIONS_ABOUT = 2;
 
     private Handler h;
     private LocationManager locationManager;
     public static ConnectivityManager connectivity;
     public ProgressDialog progressDialog;
 
     public void onLocationChanged(Location location) {
         if (LocationActivity.location != null){
             LocationActivity.location.updateLocation(location);
         }
         DataManager.data.currentLocation = location;
         //Toast.makeText(getApplicationContext(), "MainActivity.onLocationChanged", Toast.LENGTH_SHORT).show();
     }
 
     public void onStatusChanged(String provider, int status, Bundle extras) {}
 
     public void onProviderEnabled(String provider) {
         //Toast.makeText(getApplicationContext(), "MainActivity.onProviderEnabled", Toast.LENGTH_SHORT).show();
     }
 
     public void onProviderDisabled(String provider) {
         if (LocationActivity.location != null){
             LocationActivity.location.updateLocation(null);
         }
         //Toast.makeText(getApplicationContext(), "MainActivity.onProviderDisabled", Toast.LENGTH_SHORT).show();
     }
 
 
     protected void onStart(){
         super.onStart();
         //Toast.makeText(getApplicationContext(), "MainActivity.onStart", Toast.LENGTH_SHORT).show();
     }
 
     protected void onRestart(){
         super.onRestart();
         //Toast.makeText(getApplicationContext(), "MainActivity.onRestart", Toast.LENGTH_SHORT).show();
     }
 
     protected void onResume(){
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         if (prefs == null || prefs.getBoolean("usegps", true)){
             if (locationManager != null){
                 int updateTime = (prefs == null) ? prefs.getInt("gpsUpdateInterval", LOCATION_UPDATE_TIME) : LOCATION_UPDATE_TIME;
                 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, LOCATION_UPDATE_DISTANCE, this);
                 locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, updateTime, LOCATION_UPDATE_DISTANCE, this);
                 DataManager.data.currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
             }
         }
         super.onResume();
         //Toast.makeText(getApplicationContext(), "MainActivity.onResume", Toast.LENGTH_SHORT).show();
     }
 
     protected void onPause(){
         if (locationManager != null){
             locationManager.removeUpdates(this);
         }
         super.onPause();
         //Toast.makeText(getApplicationContext(), "MainActivity.onPause", Toast.LENGTH_SHORT).show();
     }
 
     protected void onStop(){
         super.onStop();
         //Toast.makeText(getApplicationContext(), "MainActivity.onStop", Toast.LENGTH_SHORT).show();
     }
 
     protected void onDestroy(){
         super.onDestroy();
         //Toast.makeText(getApplicationContext(), "MainActivity.onDestroy", Toast.LENGTH_SHORT).show();
     }
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         main = this;
 
         locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
         connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 
         Resources res = getResources();
         TabHost tabHost = getTabHost();
         TabHost.TabSpec spec;
         Intent intent;
 
         intent = new Intent().setClass(this, InfoActivity.class);
         spec = tabHost.newTabSpec("info").setIndicator("Info", res.getDrawable(R.drawable.ic_tab_info)).setContent(intent);
         tabHost.addTab(spec);
 
         intent = new Intent().setClass(this, MapActivity.class);
         spec = tabHost.newTabSpec("map").setIndicator("Map", res.getDrawable(R.drawable.ic_tab_map)).setContent(intent);
         tabHost.addTab(spec);
 
         intent = new Intent().setClass(this, ClueActivity.class);
         spec = tabHost.newTabSpec("clue").setIndicator("Clue", res.getDrawable(R.drawable.ic_tab_clue)).setContent(intent);
         tabHost.addTab(spec);
 
         intent = new Intent().setClass(this, SubjectsActivity.class);
         spec = tabHost.newTabSpec("subjects").setIndicator("Subjects", res.getDrawable(R.drawable.ic_tab_subjects)).setContent(intent);
         tabHost.addTab(spec);
 
         intent = new Intent().setClass(this, LocationActivity.class);
         spec = tabHost.newTabSpec("location").setIndicator("Location", res.getDrawable(R.drawable.ic_tab_location)).setContent(intent);
         tabHost.addTab(spec);
 
         setupCallbackHandler();
 
         setupMission();
         setupButtons();
         refreshDisplay();
     }
 
     private void setupCallbackHandler(){
         h = new Handler() {
             @Override
             public void handleMessage(Message msg) {
                 //Toast.makeText(getApplicationContext(), API_REQUESTS[msg.what] + "-/-" + (String)msg.obj, Toast.LENGTH_SHORT).show();
                 if (progressDialog != null){
                     progressDialog.dismiss();
                     progressDialog = null;
                 }
                 switch (msg.what) {
                     case RadishworksConnector.REQUEST_LIST_MISSIONS:
                         makeSelectMissionDialog((String) msg.obj);
                         break;
                 }
                 super.handleMessage(msg);
             }
         };
     }
 
     private void setupMission(){
         TextView t = (TextView) findViewById(R.id.mission);
         t.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 listActiveMissions();
             }
         });
     }
 
     private void setupButtons(){
 
     }
 
     private void listActiveMissions(){
         progressDialog = ProgressDialog.show(this, "", getString(R.string.loading));
         Thread thread = new Thread(new Runnable() {
             @Override
             public void run() {
                 RadishworksConnector.apiCall(RadishworksConnector.REQUEST_LIST_MISSIONS, MainActivity.this, h);
             }
         });
         thread.start();
     }
 
     public boolean hasApiKey(){
         return !PreferenceManager.getDefaultSharedPreferences(this).getString("apikey", "").equals("");
     }
 
     public boolean hasMissionNumber(){
         return (DataManager.data.activeMission.number > -1);
     }
 
     public void refreshDisplay(){
         refreshMission();
         refreshButtons();
     }
 
     private void refreshMission(){
         boolean enabled = false;
 
         if (hasApiKey()){
             enabled = true;
         }
         TextView t = (TextView)findViewById(R.id.mission);
         t.setEnabled(enabled);
         if (DataManager.data != null && DataManager.data.activeMission.name != null){
             t.setText(DataManager.data.activeMission.name);
         }
     }
     
     private void refreshButtons(){
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu){
         menu.add(Menu.NONE, OPTIONS_SETTINGS, 0, "Settings");
         menu.add(Menu.NONE, OPTIONS_REFRESH, 0, "Refresh");
         menu.add(Menu.NONE, OPTIONS_ABOUT, 0, "About");
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
         switch (item.getItemId()){
             case OPTIONS_SETTINGS:
                 startActivity(new Intent(this, SettingsActivity.class));
                 return true;
             case OPTIONS_ABOUT:
                 makeAboutDialog();
                 return true;
             case OPTIONS_REFRESH:
                 DataManager.data.loadMissionDetails(this);
                 DataManager.data.loadSubjects(this);
                 return true;
         }
         return false;
     }
 
     private void makeSelectMissionDialog(String missions){
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
         alert.setTitle("Mission Select");
 
         String failure = RadishworksConnector.apiFailure(missions);
         if (failure != null){
             alert.setMessage(failure);
             alert.setPositiveButton("Ok", null);
             alert.show();
             return;
         }
         //Toast.makeText(getApplicationContext(), missions, Toast.LENGTH_SHORT).show();
 
         final String[] missionList = missions.split(RadishworksConnector.FIELD_DELIMITER);
         final String[] missionNames = new String[missionList.length];
         final int[] missionNumbers = new int[missionList.length];
         for (int i=0; i<missionNames.length; i++){
             String s = missionList[i];
             String[] f = s.split(",", 3);
             missionNames[i] = f[1] + " " + f[2];
             missionNumbers[i] = Integer.parseInt(f[0]);
         }
         alert.setItems(missionNames, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 DataManager.data.activeMission.number = missionNumbers[item];
                 TextView t = (TextView)findViewById(R.id.mission);
                 t.setText(missionNames[item]);
                 refreshButtons();
                 DataManager.data.loadMissionDetails(MainActivity.this);
                 if (ClueActivity.clue != null){
                     ClueActivity.clue.enableFields(true);
                 }
                 DataManager.data.loadSubjects(MainActivity.this);
             }
         });
         alert.show();
     }
 
     public void makeAboutDialog(){
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
         alert.setTitle(getString(R.string.app_name));
         alert.setMessage(getString(R.string.about));
 
         alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
             }
         });
 
         alert.show();
     }
 }
