 package com.silverwraith.tornadowatch;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.provider.Settings;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.graphics.drawable.Drawable;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 import android.support.v4.app.FragmentActivity;
 
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.provider.Settings.Secure;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 import com.bugsense.trace.BugSenseHandler;
 
 public class TornadoWatchActivity extends FragmentActivity implements LocationListener {
 	
 	public static final String AUTH = "authentication";
     static String TAG = "TW";
     String registrationId = null; 
     static String CGI_BASE = "http://tw.silverwraith.com/cgi-bin";
     private AdView adView;
     Context context = null;
 
     private GoogleMap map;
     Drawable drawableLow;
     Drawable drawableHigh;
     public LocationManager locationManager;
    public Location location = null;
    public Integer appVer = 8;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         context = getApplicationContext();
     	BugSenseHandler.initAndStartSession(context, "2b4db4ea");
     	try {
 			if(isDebugBuild()) {
 				setContentView(R.layout.main_debug);
 			} else {
 				setContentView(R.layout.main);
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
     	
     	// Before anything else, make sure we have a network connection
     	final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
     	final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
     	if (activeNetwork != null && activeNetwork.isConnected()) {
     	    //notify user you are online
     	} else {
     		Toast.makeText(context, "No internet connection found!", Toast.LENGTH_SHORT).show();
     		this.finish();
     	}
 
         // Some initial setup that must be done in onCreate - cannot be done before.
         locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         if (location == null) {
             location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         }
 
         // Do our registration dance
     	class RegisterApp extends AsyncTask<Void, Void, Boolean> {
     		protected Boolean doInBackground(Void... arg0) {
     			Log.w("C2DM", "start registration process");
     			Intent intent = new Intent("com.google.android.c2dm.intent.REGISTER");
     			intent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));
     			intent.putExtra("sender", "avleen@gmail.com");
     			startService(intent);
     			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
     			String registrationId = prefs.getString(AUTH, "nokey");
     			Log.d("Tornado Debug", registrationId);
 				return true;
     		}
     		
 			protected void onPostExecute(Boolean success) {
 				super.onPostExecute(null);
 				if (success == true) {
 					// Fire off the initial location submit
 					AsyncSubmitInitialLocation do_submit_initial_location = new AsyncSubmitInitialLocation();
                 	do_submit_initial_location.execute();
     				Toast.makeText(context, "Successfully registered with Tornado Alert", Toast.LENGTH_SHORT).show();
 				}
     		}
     	}
     	Toast.makeText(context, "Registering with Tornado Alert service...", Toast.LENGTH_SHORT).show();
     	RegisterApp do_registration = new RegisterApp();
     	do_registration.execute();
     	
     	// Start the LocationListener service
     	Log.i(TAG, "Starting location tracker service");
     	startService(new Intent(context, TornadoWatchLocationLoggerService.class));
 
         // Let the user turn the GPS on, if it's off.
         final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
         if (!gpsEnabled) {
             offerGPSTurnOnDiaglog();
         }
 
         // GPS should be on now. Register for regular updates, get them as often as possible, or when the device moves 5 meters.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 5, this);
 
         // Create the map view
         map = ((SupportMapFragment)(getSupportFragmentManager().findFragmentById(R.id.map))).getMap();
 
         // Draw a dot for the user's location
        map.setMyLocationEnabled(true);
         
         // If we have a location for the user now, hop to it
         if (location != null) {
         	zoomToMyLocation();
         }
         
         // Let the user know that we're updating the tornado markers
         Toast.makeText(context, "Updating tornado reports...", Toast.LENGTH_SHORT).show();
         GetAndDrawMarkers do_get_draw_markers = new GetAndDrawMarkers();
         do_get_draw_markers.execute("onCreate");
         do_get_draw_markers = null;
         
     	// Ads!
     	// Look up the AdView as a resource and load a request.
     	adView = new AdView(this, AdSize.BANNER, "a15126d73db75bf");
         // Lookup your LinearLayout assuming it's been given
         // the attribute android:id="@+id/mainLayout"
         LinearLayout layout = (LinearLayout)findViewById(R.id.zoom);
         layout.addView(adView);
         // Initiate a generic request to load it with an ad
         adView.loadAd(new AdRequest());
     }
 
     private void offerGPSTurnOnDiaglog() {
         // Ask the user to turn the GPS on
         new AlertDialog.Builder(this)
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setTitle("GPS required to find your location")
                 .setMessage("Your GPS is off! You can still use the app, but we won't be able to get the most accurate tornado reports for you. Would you like to turn it on?")
                 .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                 {
                     public void onClick(DialogInterface dialog, int which) {
                         enableLocationSettings();
                     }
 
                 })
                 .setNegativeButton("No", null)
                 .show();
     }
 
     private void enableLocationSettings() {
         Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
         startActivity(settingsIntent);
     }
 
     @Override
     public void onDestroy() {
       if (adView != null) {
         adView.destroy();
       }
       BugSenseHandler.closeSession(context);
       super.onDestroy();
     }
     
     public boolean isDebugBuild() throws Exception {
        PackageManager pm = this.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);
        return ((pi.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
     }
 
 	class GetAndDrawMarkers extends AsyncTask<String, Void, JSONArray> {
 		String msg;
         JSONArray json = null;
 
 		@Override
 		protected JSONArray doInBackground(String... params) {
 			// Download markers in the background		       
 			try {
 		       	Log.i(TAG, "About to download markers");
 				json = downloadMarkers();
 				drawableLow = TornadoWatchActivity.this.getResources().getDrawable(R.drawable.locationplace);
 				drawableHigh = TornadoWatchActivity.this.getResources().getDrawable(R.drawable.locationplacered);
 				// Save the old overlays so we can compare them later
 				if (json == null) {
                     // We weren't able to parse the download, possibly an issue on the server.
                     msg = "Failed to download latest tornado locations.";
                     Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                     return json;
                 }
                 Log.i(TAG, "Number of markers found: " + json.length());
 			} catch (MalformedURLException e) {
 				// Toast.makeText(TornadoWatchActivity.this, "Marker URL is malformed", Toast.LENGTH_SHORT).show();
 				e.printStackTrace();
 			} catch (JSONException e) {
 				// Toast.makeText(TornadoWatchActivity.this, "Unable to decode markers", Toast.LENGTH_SHORT).show();
 				e.printStackTrace();
 			} catch (IOException e) {
 				// Toast.makeText(TornadoWatchActivity.this, "Marker download failed", Toast.LENGTH_SHORT).show();
 				e.printStackTrace();
 			}
 
 			/* Add an overlay for the current location marker.
 		     * Once we have it, refresh immediately and zoom to our location.
 		     */
 	        return json;
 		}
 
         public JSONArray downloadMarkers() throws MalformedURLException, JSONException, IOException {
             JSONArray json = null;
             try {
                 json = new JSONArray(downloadJSON());
             } catch (IOException e) {
                 // Nothing to do
             }
             return json;
         }
 
         public String downloadJSON() throws MalformedURLException, IOException {
             Log.i(TAG, "Downloading markers");
             URI url = null;
             StringBuilder total = new StringBuilder();
             String lng = (location != null) ? String.valueOf(location.getLongitude()) : "0";
             String lat = (location != null) ? String.valueOf(location.getLatitude()) : "0";
             String deviceId = Secure.getString(TornadoWatchActivity.this.getContentResolver(), Secure.ANDROID_ID);
             try {
                 url = new URI(CGI_BASE + "/get_markers.py");
             } catch (URISyntaxException e) {
                 e.printStackTrace();
             }
 
             try {
                 // Set some timeouts for the HTTP connection
                 HttpParams httpParameters = new BasicHttpParams();
                 int timeoutConnection = 3000;
                 HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                 int timeoutSocket = 5000;
                 HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
                 HttpClient client = new DefaultHttpClient(httpParameters);
                 HttpPost post = new HttpPost(url);
                 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                 nameValuePairs.add(new BasicNameValuePair("lng", lng));
                 nameValuePairs.add(new BasicNameValuePair("lat", lat));
                 nameValuePairs.add(new BasicNameValuePair("deviceId", deviceId));
                 post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                 HttpResponse response = client.execute(post);
                 BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                 String jsonline;
                 while ((jsonline = rd.readLine()) != null) {
                     total.append(jsonline);
                 }
                 httpParameters = null;
                 post = null;
                 client = null;
             } catch (IOException e) {
                 msg = "Unable to get tornado locations - try again!";
             }
             return total.toString();
         }
 		
 		protected void onPostExecute(JSONArray json) {
             if (json != null && json.length() > 0) {
                 // Clear the map before adding new markers.
                 map.clear();
                 for (int i=0; i < json.length(); i++) {
                     try {
                         JSONObject json_data = json.getJSONObject(i);
                         double markerLat = Float.valueOf(json_data.getString("lat"));
                         double markerLng = Float.valueOf(json_data.getString("lng"));
                         map.addMarker(
                                 new MarkerOptions()
                                         .position(
                                                 new LatLng(markerLat, markerLng))
                                         .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                         Log.i(TAG, "Marker: " + markerLat + "," + markerLng);
                     } catch (JSONException e) {
                         e.printStackTrace();
                     }
                 }
                 msg = "Tornado report markers updated";
             } else {
                 msg = "No change in tornado reports.";
             }
 		}
 	}
     
     protected boolean isRouteDisplayed() {
     	return false;
     }
 
     protected void onResume() {
     	super.onResume();
         // On start or resume, register for location updates!
         // Get them frequently to start with, the onLocationChange code will
         // slow this down.
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
     }
 
 	class AsyncSubmitInitialLocation extends AsyncTask<String, Void, Void> {
 		String screenmsg = null;
 		URI url;
 		protected Void doInBackground(String... param) {
 			try {
 				url = new URI(CGI_BASE + "/updatelocation.py");
 			} catch (URISyntaxException e) {
 				e.printStackTrace();
 			}
 			try {
 				// Set some timeouts for the HTTP connection
 				String deviceId = Secure.getString(TornadoWatchActivity.this.getContentResolver(), Secure.ANDROID_ID);
 				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
     			String registrationId = prefs.getString(AUTH, "nokey");
    			String latitude = String.valueOf(location.getLatitude());
    			String longitude = String.valueOf(location.getLongitude());
 				HttpParams httpParameters = new BasicHttpParams();
 				int timeoutConnection = 3000;
 				HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
 				int timeoutSocket = 5000;
 				HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
 				HttpClient client = new DefaultHttpClient(httpParameters);				
 				HttpPost post = new HttpPost(url);
 				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
 				nameValuePairs.add(new BasicNameValuePair("lat", latitude));
 				nameValuePairs.add(new BasicNameValuePair("lng", longitude));
 				nameValuePairs.add(new BasicNameValuePair("registrationId", registrationId));
 				nameValuePairs.add(new BasicNameValuePair("deviceId", deviceId));
 				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 				HttpResponse response = client.execute(post);
 				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 				String line = "";
 				while ((line = rd.readLine()) != null) {
 					Log.e("HttpResponse", line);
 				}
 				httpParameters = null;
 				post = null;
 				client = null;
 			} catch (IOException e) {
 				screenmsg = "Unable to submit marker - try again!";
 			}
 			return null;
 		}
 	}
 
 	protected void onPause() {
     	super.onPause();
     	/* Be polite - when we're closed, don't get network location updates.
     	 * TODO(avleen): This will change once we can run in the background,
     	 * to warn of nearby weather changes.
     	 */
         locationManager.removeUpdates(this);
         this.finish();
     }
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.optionsmenu, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	        case R.id.my_location:
 	            zoomToMyLocation();
 	            return true;
 	        case R.id.place_marker:
 	        	placeMarker();
 	        	return true;
 	        case R.id.preferences:
 	        	startActivity(new Intent("com.silverwraith.tornadowatch.TornadoPreferenceActivity"));
 	        case R.id.get_markers:
 	        	// getAndDrawMarkers("UserRefresh");
 	        	GetAndDrawMarkers do_get_draw_markers = new GetAndDrawMarkers();
 	            do_get_draw_markers.execute("onRequest");
 	            do_get_draw_markers = null;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 
     private void zoomToMyLocation() {
         if (location != null) {
             LatLng thisDeviceLocation = new LatLng(location.getLatitude(), location.getLongitude());
             map.animateCamera(CameraUpdateFactory.newLatLngZoom(thisDeviceLocation, 6));
         } else {
             Toast.makeText(this, "Cannot determine location", Toast.LENGTH_SHORT).show();
         }
     }
 	
 	private void placeMarker() {
 		// First make sure the user really wants to report a tornado
 		new AlertDialog.Builder(this)
         	.setIcon(android.R.drawable.ic_dialog_alert)
         	.setTitle("Really??")
         	.setMessage("Are you sure you want to report a tornado?")
         	.setPositiveButton("Yes", new DialogInterface.OnClickListener()
         {
         	public void onClick(DialogInterface dialog, int which) {
         		reallyPlaceMarker();
         	}
 
         })
         	.setNegativeButton("No", null)
         	.show();
 	}
 	
 	private void reallyPlaceMarker() {
         drawableLow = this.getResources().getDrawable(R.drawable.locationplace);
         // TODO(avleen): Beyond just "null", make sure this is the most accurate
         // location we can get.
         if (location != null) {
             AsyncSubmitCoords submit_coords = new AsyncSubmitCoords();
             // Toast.makeText(MainActivity.this, "Lat: " + location.getLatitude() + "Lng: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
             submit_coords.execute(location.getLongitude(), location.getLatitude());
 
             // Show a popup, letting the user know the marker has been submitted.
            new AlertDialog.Builder(context)
                     .setIcon(android.R.drawable.ic_dialog_alert)
                     .setTitle("Tornado submitted")
                     .setMessage("Thank you. Your tornado will be sent out a soon as we confirm it!")
                     .setPositiveButton("OK", new DialogInterface.OnClickListener()
                     {
                         public void onClick(DialogInterface dialog, int which) {
                         }
                     }).show();
         } else {
             Toast.makeText(this, "Waiting for GPS location...", Toast.LENGTH_SHORT).show();
         }
 	}
 	
 	class AsyncSubmitCoords extends AsyncTask<Double, Void, Void> {
 		String screenmsg = null;
 		URL url;
 		protected Void doInBackground(Double... param) {
 			try {
 				String queryString = "?lng=" + param[0] + "&lat=" + param[1] + "&registrationId=" + showRegistrationId();
 				url = new URL(CGI_BASE + "/user_submit.py" + queryString);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			}
 			try {
 				URLConnection urlConnection = url.openConnection();
 				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
 				InputStreamReader is = new InputStreamReader(in);
 				BufferedReader br = new BufferedReader(is);
 				String read = br.readLine();
 				if (read != null) {
 					Log.d(TAG, read);
 				}
 				screenmsg = read;
 			} catch (IOException e) {
 				screenmsg = "Unable to submit marker - try again!";
 			}
 			return null;
 		}
 		
 		protected void onPostExecute(String msg) {
 			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public String showRegistrationId() {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		String registrationId = prefs.getString(AUTH, "None");
 		Log.d("C2DM RegId requested", registrationId);
 		return registrationId;
 	}
 
 	public void onProviderDisabled(String provider) {
         // TODO Auto-generated method stub
 
     }
 
     public void onProviderEnabled(String provider) {
         // TODO Auto-generated method stub
 
     }
 
     public void onStatusChanged(String provider, int status, Bundle extras) {
         // TODO Auto-generated method stub
 
     }
 
     public void onLocationChanged(Location location) {
         if (location != null) {
             // Return - the location isn't set, and trying to use it will cause a NullPointerException
             return;
         }
         // On the first location fix, jump to the user's location and download markers!
         zoomToMyLocation();
         GetAndDrawMarkers do_get_draw_markers = new GetAndDrawMarkers();
         do_get_draw_markers.execute("onCreate");
         do_get_draw_markers = null;
     }
 }
