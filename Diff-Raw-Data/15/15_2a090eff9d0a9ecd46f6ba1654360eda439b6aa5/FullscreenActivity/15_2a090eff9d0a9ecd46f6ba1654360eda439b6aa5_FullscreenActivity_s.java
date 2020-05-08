 package com.ewaterhouse.getit;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 
 
 public class FullscreenActivity extends Activity implements LocationListener {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_fullscreen);
        getAddress();
     }
 
     @Override
     protected void onStop() {
         LocationManager locationManager =  (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
         locationManager.removeUpdates(this);
         super.onStop();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu items for use in the action bar
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.fullscreen_activity_actions, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle presses on the action bar items
         switch (item.getItemId()) {
             case R.id.action_clear:
                 clearPreferences();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void clearPreferences() {
         SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = prefs.edit();
         editor.clear();
         editor.commit();
     }
 
     private void getAddress() {
         Location loc = getCurrentLocation();
         setAddressFromLocation(loc);
     }
 
     private void setAddressFromLocation(Location loc) {
         double lat; double lon;
         if (loc == null) {
             updateContentView(this.getString(R.string.searching));
             return;
         } else {
             lat = loc.getLatitude();
             lon = loc.getLongitude();
         }
 
         String url = String.format("http://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false", lat, lon);
         SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
         String res = prefs.getString(url, null);
         if (res != null) {
             updateContentView(res);
             return;
         }
         new HttpTask().execute(url);
     }
 
     private void updateContentView(String data) {
         final TextView contentView = (TextView)findViewById(R.id.fullscreen_content);
         contentView.setText(data);
     }
 
     Location getCurrentLocation() {
         LocationManager locationManager =  (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
         Location newestLocation = null;
         List<String> providers = locationManager.getProviders(true);
         if (providers != null) {
             for (String provider : providers) {
                 Location location = locationManager.getLastKnownLocation(provider);
                 if (location != null) {
                     if (newestLocation == null) {
                         newestLocation = location;
                     } else {
                         if (location.getTime() > newestLocation.getTime()) {
                             newestLocation = location;
                         }
                     }
                     locationManager.requestLocationUpdates(provider, 0, 0, this);
                 }
             }
         } else {
             updateContentView(this.getString(R.string.nolocationprovider));
         }
         return newestLocation;
 
     }
 
     // LocationListener methods
 
     @Override
     public void onLocationChanged(Location location) {
         setAddressFromLocation(location);
     }
 
     @Override
     public void onStatusChanged(String s, int i, Bundle bundle) {
         return;
     }
 
     @Override
     public void onProviderEnabled(String s) {
         return;
     }
 
     @Override
     public void onProviderDisabled(String s) {
         return;
     }
 
 
     private class HttpTask extends AsyncTask<String, Void, String> {
         private Exception exception;
 
         protected String doInBackground(String... urls) {
             try {
                 String url= urls[0];
 
                 String result = "";
                 StringBuilder stringBuilder = new StringBuilder();
                 HttpClient httpClient = new DefaultHttpClient();
                 HttpGet httpGet = new HttpGet(url);
                 try {
                     HttpResponse response = httpClient.execute(httpGet);
                     StatusLine statusLine = response.getStatusLine();
                     int statusCode = statusLine.getStatusCode();
                     if (statusCode == 200) {
                         HttpEntity entity = response.getEntity();
                         InputStream inputStream = entity.getContent();
                         BufferedReader reader = new BufferedReader(
                                 new InputStreamReader(inputStream));
                         String line;
                         while ((line = reader.readLine()) != null) {
                             stringBuilder.append(line);
                         }
                         inputStream.close();
                     }
                     JSONTokener tokener = new JSONTokener(stringBuilder.toString());
                     JSONObject json = new JSONObject(tokener);
                    result = json.getJSONArray("results").getJSONObject(0).getString("formatted_address");
                 } catch (Exception e) {
                     result = e.getLocalizedMessage();
                 }
 
                 SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
                 SharedPreferences.Editor editor = prefs.edit();
                 if (prefs.getAll().size() > 10) {
                     editor.remove(prefs.getAll().keySet().toArray()[0].toString());
                 }
                 editor.putString(url, result);
                 editor.commit();
 
                 return result;
 
             } catch (Exception e) {
                 this.exception = e;
                 return null;
             }
         }
 
         protected void onPostExecute(String result) {
             try {
                 updateContentView(result);
             } catch (Exception e) {
                 updateContentView(e.getLocalizedMessage());
             }
         }
 
     }
 }
