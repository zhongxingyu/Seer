 package com.application.wakeapp;
 
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.text.TextUtils;
 import android.view.Menu;
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
 
 public class MainActivity extends Activity {
 
     private Location finalDestination;
     private Location myLocation=null;
     private Integer radius = 2000;
     private SearchView mSearchView;
     private ListView mListView;
     private ArrayAdapter<String> mAdapter;
     private Button mButton;
     private TextView mTextView;
     private ArrayList<String> stationList;
     private ArrayList<String> stationListNameOnly;
     private LocationManager locationManager;
     private Boolean isServiceStarted = Boolean.FALSE;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         System.out.println("Radde123 onCreate " + isServiceStarted);
 
         findGPSPosition();
 
         finalDestination = new Location("Destination");
         stationList = new ArrayList<String>();
         stationListNameOnly = new ArrayList<String>();
 
         new Background().execute();
 
         mSearchView = (SearchView) findViewById(R.id.searchView);
         mListView   = (ListView) findViewById(R.id.listView);
         mButton     = (Button) findViewById(R.id.button);
         mTextView   = (TextView) findViewById(R.id.textView);
 
         mListView.setAdapter(mAdapter = new ArrayAdapter<String>(
                             this,android.R.layout.simple_list_item_1,
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
                 System.out.println("Radde123 " + finalDestination.getLongitude() + " " + isServiceStarted);
             }
         });
         mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String stationName = ((TextView) view).getText().toString();
                 Double lat = 0.0, lng = 0.0;
                Float distance;
 
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
                 mTextView.setText("Final destination: " + stationName + "\n" +
                         "Distance to destination: " + distance / 1000 + " km\n" +
                         "Current speed: " + myLocation.getSpeed());
 
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
     private void findGPSPosition(){
 
         locationManager = (LocationManager)
                 getSystemService(Context.LOCATION_SERVICE);
 
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
 
     }
 
     class Background extends AsyncTask<String, Integer, String> {
         private ArrayList<String> removeCoordinates(ArrayList<String> l){
             ArrayList<String> newList = new ArrayList<String>();
 
             for ( int i=0;i<l.size();i++){
                 StringBuilder sb = new StringBuilder();
                 String tmp = l.get(i);
                 String[] temp = tmp.split(" ");
                 for ( int j=0;j<temp.length-2;j++){
                     //System.out.println("Radde123" + temp[j]);
                     sb.append(temp[j] + " ");
                 }
                 newList.add(sb.toString());
             }
 
             return  newList;
         }
         protected String doInBackground(String... urls) {
             do{//We need to get an position
                 try {
                     Thread.sleep(1000);
                     System.out.println("Radde123 looking for GPS");
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }while(myLocation == null);
 
             System.out.println("Radde123 Pos found: lat: " +
                     myLocation.getLatitude() + " lng: " +
                     myLocation.getLongitude());
 
             Stations stations =
                     new Stations(myLocation.getLongitude(),
                             myLocation.getLatitude(),
                             radius);
 
             stationList = stations.getAllStations(Boolean.FALSE);
 
             stationListNameOnly = removeCoordinates(stationList);
 
             //for (int i=0;i<stationList.size();i++){
             //    System.out.println("Radde123 " + stationList.get(i));
             //}
 
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     mAdapter.addAll(stationListNameOnly);
                     mAdapter.notifyDataSetChanged();
                     System.out.println("Radde123 notifyDataSetChanged len: " + stationListNameOnly.size());
                     Toast.makeText(getApplicationContext(),
                             "Station list updated",
                             Toast.LENGTH_SHORT).show();
                 }
             });
             return null;
         }
     }
     @Override
     protected void onResume() {
         super.onResume();
         System.out.println("Radde123 onResume " + isServiceStarted);
         stopService(new Intent(MainActivity.this,
                 BackgroundService.class));
         isServiceStarted = Boolean.FALSE;
 
     }
     @Override
     protected void onDestroy(){
         super.onDestroy();
         System.out.println("Radde123 onDestroy " + isServiceStarted);
         stopService(new Intent(MainActivity.this,
                 BackgroundService.class));
         isServiceStarted = Boolean.FALSE;
 
     }
 
 }
