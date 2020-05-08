 package com.porknbunny.friendcompass;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.MenuItem;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 public class NavigateActivity extends FragmentActivity implements LocationListener, SensorEventListener {
     private static final String TAG = "friendCompass.NavigateActivity";
     private LocationManager locationManager;
     private SensorManager sensorManager;
     private Sensor mSensor;
     private Criteria criteria;
     //private TextView distBiz, distFriend, bearBiz, bearFriend, bizName,bizAddress,bizSuburb, time,bearing;
     private Location myLocation;
     private float[] mValues;
     private float compassBearing;
     private Business navBusiness;
     private Friend friend;
     private String userName;
     private ImageView bizHud, friendHud;
     private TextView bizDist,bizName,bizAddr, bizSub,friendDist, friendName,friendToBiz;
     private String bizID;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.navigation);
 
 
         //---home
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                 
         bizID = getIntent().getExtras().getString("business");
         friend = (Friend) getIntent().getExtras().getSerializable("friend");
 
 
         new SAPIBusinessQuery().execute(bizID);
         userName = getUsername();
 
         //lots of textviews
         /*
         distBiz = (TextView) findViewById(R.id.distance_to_business);
         distFriend = (TextView) findViewById(R.id.distance_to_friend);
         bearBiz = (TextView) findViewById(R.id.bearing_to_business);
         bearFriend = (TextView) findViewById(R.id.bearing_to_friend);
         bizName = (TextView) findViewById(R.id.business_name);
         bizAddress = (TextView) findViewById(R.id.business_address);
         bizSuburb = (TextView) findViewById(R.id.business_suburb);
         time = (TextView) findViewById(R.id.time);
         bearing = (TextView) findViewById(R.id.c_bearing);
         */
 
         bizHud  = (ImageView) findViewById(R.id.na_biz_hud);
         friendHud  = (ImageView) findViewById(R.id.na_friend_hud);
         bizDist = (TextView) findViewById(R.id.na_biz_distance);
         bizName = (TextView) findViewById(R.id.na_biz_name);
         bizAddr = (TextView) findViewById(R.id.na_biz_addr);
         bizSub = (TextView) findViewById(R.id.na_biz_suburb);
         friendDist = (TextView) findViewById(R.id.na_friend_distance);
         friendToBiz = (TextView) findViewById(R.id.na_friend_to_biz);
         friendName = (TextView) findViewById(R.id.na_friend_name);
         friendName.setText(friend.getUserid());
         
         //location stuff
         locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
         criteria = new Criteria();
         criteria.setAccuracy(Criteria.ACCURACY_FINE);
         criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
 
         List<String> providers = locationManager.getAllProviders();
         double accuracy = -1;
         if (providers.size() == 0) {
             Log.w(TAG, "No providers available");
         }
         for (String provider : providers) {
             Location tempLoc = locationManager.getLastKnownLocation(provider);
             if (tempLoc != null && (accuracy < 0 || tempLoc.getAccuracy() < accuracy)) {
                 myLocation = tempLoc;
             }
         }
 
         //sensor stuff
         sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
         mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
 
         locationUpdate();
     }
 
 
     public void gmapsBusiness(View view){
         //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+navBusiness.getLatitude()+","+navBusiness.getLongitude()+"?q="+navBusiness.getLatitude()+","+navBusiness.getLongitude()+"("+navBusiness.getName()+"+"+navBusiness.getName()+")"));
         Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+navBusiness.getLatitude()+","+navBusiness.getLongitude()+"?q="+navBusiness.getLatitude()+","+navBusiness.getLongitude()+"("+URLEncoder.encode(navBusiness.getName())+")"));
         startActivity(intent);
 
     }
 
     public void gmapsFriend(View view){
         //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+navBusiness.getLatitude()+","+navBusiness.getLongitude()+"?q="+navBusiness.getLatitude()+","+navBusiness.getLongitude()+"("+navBusiness.getName()+"+"+navBusiness.getName()+")"));
         Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+friend.getLat()+","+friend.getLongi()+"?q="+friend.getLat()+","+friend.getLongi()+"("+friend.getUserid()+")"));
         startActivity(intent);
 
     }
 
     public String getUsername(){
         AccountManager manager = AccountManager.get(this);
         Account[] accounts = manager.getAccountsByType("com.google");
         List<String> possibleEmails = new LinkedList<String>();
 
         for (Account account : accounts) {
             // TODO: Check possibleEmail against an email regex or treat
             // account.name as an email address only for certain account.type values.
             possibleEmails.add(account.name);
         }
 
         if(!possibleEmails.isEmpty() && possibleEmails.get(0) != null){
             String email = possibleEmails.get(0);
             String[] parts = email.split("@");
             if(parts.length > 0 && parts[0] != null)
                 return parts[0];
             else
                 return null;
         }else
             return null;
     }
 
     private int count = 0;
     private void locationUpdate(){
         if(navBusiness != null){
             //TODO NICE NUMBERS
         bizDist.setText(""+NumberFormat.getInstance().format( (int) myLocation.distanceTo(navBusiness.getLocation()))+"m");
         friendDist.setText("" + NumberFormat.getInstance().format((int) myLocation.distanceTo(friend.getLocation())) + "m");
         friendToBiz.setText("" + NumberFormat.getInstance().format((int) navBusiness.getLocation().distanceTo(friend.getLocation())) + "m");
 
 
         float bearingBiz = ((myLocation.bearingTo(navBusiness.getLocation())+compassBearing)%360);
         float bearingFriend = ((myLocation.bearingTo(friend.getLocation())+compassBearing)%360);
         updateHUD(bizHud, bearingBiz);
         updateHUD(friendHud, bearingFriend);
 
         }
        if(count++ % 1000 == 0){
             new FriendQuery().execute("");
         }
     }
 
     private void updateHUD(ImageView view, float bearing){
         if(bearing <0){
             bearing = 360 - bearing; 
         }
         bearing = 360 - bearing;
         
         int imageResource = R.drawable.greenfill1;
         if(bearing > 20 && bearing <= 60){
             imageResource = R.drawable.greenfill1;
         }
         else if(bearing > 60 && bearing <= 100){
             imageResource = R.drawable.greenfill2;
         }
         else if(bearing > 100 && bearing <= 140){
             imageResource = R.drawable.greenfill3;
         }
         else if(bearing > 140 && bearing <= 180){
             imageResource = R.drawable.greenfill4;
             
         }
         else if(bearing > 180 && bearing <= 220){
             imageResource = R.drawable.greenfill5;
             
         }
         else if(bearing > 220 && bearing <= 260){
             imageResource = R.drawable.greenfill6;
             
         }
         else if(bearing > 260 && bearing <= 300){
             imageResource = R.drawable.greenfill7;
             
         }
         else if(bearing > 300 && bearing <= 340){
             imageResource = R.drawable.greenfill8;
         
         }
         else{
             imageResource = R.drawable.greenfill1;
         }
 
         view.setImageResource(imageResource);
     }
     
     
     @Override
     public void onStart() {
         super.onStart();
         locationManager.requestLocationUpdates(0, 0, criteria, this, null);
         sensorManager.registerListener(this, mSensor,
                 SensorManager.SENSOR_DELAY_GAME);
 
     }
 
     @Override
     public void onStop() {
         super.onStop();
         locationManager.removeUpdates(this);
         sensorManager.unregisterListener(this);
     }
 
     @Override
     public void onLocationChanged(Location location) {
         myLocation = location;
         locationUpdate();
     }
 
     @Override
     public void onStatusChanged(String s, int i, Bundle bundle) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void onProviderEnabled(String s) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void onProviderDisabled(String s) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
 
     //--- bearings ---
     @Override
     public void onSensorChanged(SensorEvent sensorEvent) {
         mValues = sensorEvent.values;
 
         compassBearing = mValues[0];
         locationUpdate();
 
 
     }
 
     @Override
     public void onAccuracyChanged(Sensor sensor, int i) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 finish();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
 
         }
     }
 
 
     private String getMetaData(String key) {
         try {
             ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
             Bundle bundle = ai.metaData;
             String value = bundle.getString(key);
             //Log.d(TAG, "Meta-data: " + key + " - " +value);
             return value;
         } catch (PackageManager.NameNotFoundException e) {
             Log.e(TAG, "Failed to load meta-data Key: " + key + ", NameNotFound: " + e.getMessage());
         } catch (NullPointerException e) {
             Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
         }
         return "";
     }
 
     private BufferedInputStream getUrl(String url) {
         int TEMP_BUFF_SIZE = 16384;
         try {
             HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
             connection.setReadTimeout(5000);
             InputStream inputStream = (InputStream) connection.getContent();
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, TEMP_BUFF_SIZE);
             int responseCode = connection.getResponseCode();
             if (responseCode == HttpURLConnection.HTTP_OK) {
 
                 return bufferedInputStream;
             }
             Log.w(TAG, "Incorrect response for " + url + " : " + connection.getResponseCode());
         } catch (Exception e) {
             e.printStackTrace();
             //Log.w(TAG, e.getMessage());
             Log.w(TAG, "Could not get " + url);
         }
         return null;
     }
 
     //--- AsyncDoSearch ---
     private class FriendQuery extends AsyncTask<String, Void, String> {
         private String url;
         private final int BUFF_SIZE = 16384;
 
         @Override
         protected String doInBackground(String... searchTerms) {
             for (String searchTerm : searchTerms) {
 
                 url = "http://friendcompass.porknbunny.com/?user="
                         + userName +"&lat="+myLocation.getLatitude()+"&long="+ myLocation.getLongitude()+"&biz="+ bizID +"&friend=" + friend.getUserid();
 
 
                 BufferedInputStream inputStream = getUrl(url);
 
                 if (inputStream != null) {
 
                     ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(BUFF_SIZE);
                     try {
                         if (inputStream != null) {
                             byte[] tempBuff = new byte[BUFF_SIZE];
                             int readCount;
                             while ((readCount = inputStream.read(tempBuff)) != -1) {
                                 byteArrayOutputStream.write(tempBuff, 0, readCount);
                             }
                             byte[] newBuff = byteArrayOutputStream.toByteArray();
                             inputStream.close();
                             byteArrayOutputStream.close();
                             String temp = new String(newBuff, "US_ASCII");
                             return temp;
                         }
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
 
                 }
 
 
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(String result) {
             if (result != null) {
                 //time to parse some JSON!
                 try {
                     JSONArray list = new JSONArray(result);
                     for (int i = 0; i < list.length(); i++) {
                         JSONObject jsonFriend = list.getJSONObject(i);
                         Friend tempFriend = new Friend(jsonFriend.getString("userid"),
                                 jsonFriend.getString("friend"),
                                 jsonFriend.getString("businessid"),
                                 jsonFriend.getDouble("lat"),
                                 jsonFriend.getDouble("long"),
                                 jsonFriend.getInt("time"));
                         //if(tempFriend.getUserid().compareTo(userName) != 0){
                         if(friend.getUserid().compareTo(tempFriend.getUserid()) == 0){
                             friend.setLat(tempFriend.getLat());
                             friend.setLongi(tempFriend.getLongi());
                             friend.setTime(tempFriend.getTime());
                         }
                         //}
                     }
 
                 }
                 catch (Exception e){
 
                 }
             }
         }
     }
 
     //--- AsyncDoSearch ---
     private class SAPIBusinessQuery extends AsyncTask<String, Void, String> {
         private String url;
         private final int BUFF_SIZE = 16384;
         private final String sapi_key;
 
         private SAPIBusinessQuery() {
             sapi_key = getMetaData("SAPI_KEY");
         }
 
         @Override
         protected String doInBackground(String... businessIDs) {
             ArrayList<String> newList = new ArrayList<String>();
             for (String businessID : businessIDs) {
                 try {
                     url = "http://api.sensis.com.au/ob-20110511/test/getByListingId?key="
                             + sapi_key
                             + "&query="
                             + URLEncoder.encode(businessID, "UTF-8");
 
                 } catch (UnsupportedEncodingException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
 
                 BufferedInputStream inputStream = getUrl(url);
 
                 if (inputStream != null) {
 
                     ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(BUFF_SIZE);
                     try {
                         if (inputStream != null) {
                             byte[] tempBuff = new byte[BUFF_SIZE];
                             int readCount;
                             while ((readCount = inputStream.read(tempBuff)) != -1) {
                                 byteArrayOutputStream.write(tempBuff, 0, readCount);
                             }
                             byte[] newBuff = byteArrayOutputStream.toByteArray();
                             inputStream.close();
                             byteArrayOutputStream.close();
                             String temp = new String(newBuff, "US_ASCII");
                             return temp;
                         }
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
 
                 }
 
 
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(String result) {
             if (result != null) {
                 //time to parse some JSON!
                 try {
                     JSONObject jsonObject = new JSONObject(result);
                     JSONArray jsonArray = jsonObject.getJSONArray("results");
                     for (int i = 0; i < jsonArray.length(); i++) {
                         try {
                             JSONObject jsonBusiness = jsonArray.getJSONObject(i);
 
                             String name = jsonBusiness.getString("name");
                             String id = jsonBusiness.getString("id");
 
                             //address and gps
                             JSONObject address = jsonBusiness.getJSONObject("primaryAddress");
                             String latitudeStr = address.getString("latitude");
                             String longitudeStr = address.getString("longitude");
                             Double latitude = new Double(latitudeStr);
                             Double longitude = new Double(longitudeStr);
                             String addressLine = address.getString("addressLine");
                             String suburb = address.getString("suburb");
 
                             //category
                             String category = "";
                             try {
                                 JSONArray categories = jsonBusiness.getJSONArray("categories");
                                 for (int j = 0; j < categories.length(); j++) {
                                     JSONObject cat = categories.getJSONObject(j);
                                     category = cat.getString(name);
                                 }
                             } catch (Exception e) {
 
                             }
 
                             //phone
                             JSONArray contacts = jsonBusiness.getJSONArray("primaryContacts");
                             String phone = "";
                             try {
                                 for (int j = 0; j < contacts.length(); j++) {
                                     JSONObject contact = contacts.getJSONObject(j);
                                     if (contact.getString("type").compareTo("Phone") == 0) {
                                         phone = contact.getString("value");
                                     }
                                 }
                             } catch (Exception e) {
 
                             }
 
                             Business business = new Business(name,
                                     latitude,
                                     longitude,
                                     addressLine,
                                     suburb,
                                     bizID,
                                     category,
                                     phone);
                             navBusiness = business;
                             bizAddr.setText(navBusiness.getAddressLine());
                             bizName.setText(navBusiness.getName());
                             bizSub.setText(navBusiness.getSuburb());
                         } catch (Exception e) {
 
                         }
 
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
 
 }
