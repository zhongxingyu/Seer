 package com.dotcom.nextup.activities;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.dotcom.nextup.R;
import com.dotcom.nextup.categorymodels.Categories;
 import com.dotcom.nextup.categorymodels.CheckIn;
 import com.dotcom.nextup.categorymodels.CheckInManager;
 import com.dotcom.nextup.classes.Venue;
 import com.dotcom.nextup.datastoring.Update;
 import com.dotcom.nextup.oauth.AndroidOAuth;
 import com.google.android.maps.GeoPoint;
 
 public class Intermediate extends Activity {
 	ArrayList<Venue> nearby_locations = null;
 	Venue nearest_location = null;
 	ArrayAdapter<CharSequence> adapter;
 	ArrayList<CharSequence> spinner_locations = null;
 	private AndroidOAuth oa;
 	private LocationManager locationManager;
 	private LocationListener locationListener;
 	public GeoPoint currentLocation;
 	public GeoPoint lastLocation;
 	public String currentLocationName;
 	public String lastLocationName;
 	private Boolean receivedLocationUpdate;
 	private Boolean codeStored;
 	private String token;
 	private String code;
 	private CheckInManager checkInManager;
 	private ArrayList<CheckIn> checkIns;
 	private SharedPreferences pref;
 	private Boolean checkinsUpdated = false;
 	Categories categories = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.intermediate2);
 		nearby_locations = new ArrayList<Venue>();
 		oa = new AndroidOAuth(this);
 		checkInManager = new CheckInManager();
 		pref = PreferenceManager.getDefaultSharedPreferences(this);
 		codeStored = getCode(getIntent());
 		dealWithCode(codeStored);
 		if (this.checkIns != null)
 			getLastLocation();
 			getLastLocationName();
 		
 		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
 		locationListener = new LocationListener() {
 			@Override
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 			}
 
 			@Override
 			public void onProviderEnabled(String provider) {
 			}
 
 			@Override
 			public void onProviderDisabled(String provider) {
 			}
 
 			@Override
 			public void onLocationChanged(Location location) {
 				currentLocation = new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6));
 				if (!receivedLocationUpdate) {
 					getCurrentLocationNameFromFoursquare(currentLocation);
 					updateSpinner();
 					receivedLocationUpdate = true;
 				}
 			}
 		};
 
 		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 		Location temp = null;
 		if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
 			temp = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 			currentLocation = new GeoPoint((int)(temp.getLatitude() * 1E6), (int)(temp.getLongitude() * 1E6));
 			if (currentLocationName == null) {
 				getCurrentLocationNameFromFoursquare(currentLocation);
 				updateSpinner();
 			}
 		}
 		if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
 			temp = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 			currentLocation = new GeoPoint((int)(temp.getLatitude() * 1E6), (int)(temp.getLongitude() * 1E6));
 			if (currentLocationName == null)
 				getCurrentLocationNameFromFoursquare(currentLocation);
 				updateSpinner();
 		}
 		
 		
 	}
 
 	public void onResume() {
 		 super.onResume();
 	     locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 	     locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 	     if (currentLocation != null && currentLocationName == null)
 	     		getCurrentLocationNameFromFoursquare(currentLocation);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		locationManager.removeUpdates(locationListener);
 	}
 	
 	/* ---------------- UI CODE BELOW -------------------*/
 	
 	@SuppressWarnings("unchecked")
 	public void updateSpinner() {
 		Spinner spinner = (Spinner)findViewById(R.id.Intermediate2Spinner);
 		spinner_locations = new ArrayList<CharSequence>();
 		adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, spinner_locations);
 		spinner.setAdapter(adapter);
 		
 		if (nearby_locations != null && nearby_locations.size() > 0) {
 			adapter.notifyDataSetChanged();
 			for (int i = 0; i < nearby_locations.size(); i++)
 				adapter.add(nearby_locations.get(i).getName());
 		}
 		adapter.notifyDataSetChanged();
 	}
 	
 	/* -------------TOKEN/CODE STUFF BELOW------------*/
 	private void dealWithCode(Boolean codeStored) {
 		if (codeStored) {
 			try {
 				if (!getTokenFromPreferences()) {
 					retrieveToken();
 				}
 				if (token != null) {
 					if ((this.checkIns =CheckInManager.getCheckins(this.token, this.checkIns)) != null) {
 						/*
 						 * This function does so much work! It's awesome!
 						 */
 						if (!checkinsUpdated) {
 							Update.update(pref, getString(R.string.updateTimePreferenceName), this.checkIns, Intermediate.this);
 							checkinsUpdated = true;
 						}
 					}
 				}
 			} catch (MalformedURLException e) {
 				//TODO: Deal with this error
 			}
 		}
 	}
 
 	private boolean getTokenFromPreferences() {
 		if (this.pref.contains(getString(R.string.accessTokenPreferenceName))) {
 			this.token = this.pref.getString(
 					getString(R.string.accessTokenPreferenceName), "Unknown");
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private void retrieveToken() {
 		HttpGet request = new HttpGet(oa.getAccessTokenUrl(this.code));
 
 		DefaultHttpClient client = new DefaultHttpClient();
 		Editor edit = this.pref.edit();
 		try {
 
 			HttpResponse resp = client.execute(request);
 			int responseCode = resp.getStatusLine().getStatusCode();
 
 			if (responseCode >= 200 && responseCode < 300) {
 
 				String response = responseToString(resp);
 				JSONObject jsonObj = new JSONObject(response);
 				this.token = jsonObj.getString("access_token");
 				edit.putString(getString(R.string.accessTokenPreferenceName),
 						this.token);
 				edit.putString(getString(R.string.accessCodePreferenceName),
 						this.code);
 				edit.commit();
 			}
 		} catch (ClientProtocolException e) {
 			Toast.makeText(Intermediate.this,
 					"There was an error connecting to Foursquare",
 					Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 			//TODO: Deal with this error
 		} catch (IOException e) {
 			Toast.makeText(Intermediate.this,
 					"There was an error connecting to Foursquare",
 					Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 			//TODO: Deal with this error
 		} catch (JSONException e) {
 			// This means that they probably revoked the token
 			// We are going to clear the preferences and go back to
 			// The login prompt
 			e.printStackTrace();
 			edit.remove(getString(R.string.accessCodePreferenceName));
 			edit.remove(getString(R.string.accessTokenPreferenceName));
 			edit.commit();
 			Intent i = new Intent(Intermediate.this, FourSquareLoginPrompt.class);
 			startActivity(i);
 		}
 	}
 
 	private Boolean getCode(Intent i) {
 		if (i.getData() != null) {
 			this.code = i.getData().getQueryParameter("code");
 			return true;
 		} else {
 			if (i.getStringExtra(getString(R.string.accessCodePreferenceName)) != "None") {
 				this.code = i
 						.getStringExtra(getString(R.string.accessCodePreferenceName));
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static String responseToString(HttpResponse resp)
 			throws IllegalStateException, IOException {
 		BufferedReader in = new BufferedReader(new InputStreamReader(resp
 				.getEntity().getContent()));
 
 		StringBuffer sb = new StringBuffer();
 
 		String line = "";
 
 		while ((line = in.readLine()) != null) {
 			sb.append(line);
 		}
 		in.close();
 
 		return sb.toString();
 	}
 
 	/* ----------------LOCATION CODE BELOW --------------------- */
     public void getLastLocation() {
     	if (this.checkIns != null) {
     		CheckIn lastCheckIn = this.checkInManager.getLastCheckIn(this.checkIns);
     		this.lastLocation = lastCheckIn.getLocation();
     	}
     }
     
     private void getLastLocationName() {
     	if (this.checkIns != null) {
     		CheckIn lastCheckIn = this.checkInManager.getLastCheckIn(this.checkIns);
     		this.lastLocationName = lastCheckIn.getName();
     	}
     }
 	private void getCurrentLocationNameFromFoursquare(GeoPoint location) {
 		if (this.token == null) {
 			return;
 		}
 		String url = "https://api.foursquare.com/v2/venues/search?ll=" + String.valueOf((location.getLatitudeE6()/1E6)) + "," + String.valueOf((location.getLongitudeE6()/1E6));
 		String authUrl = url + "&oauth_token=" + this.token;
 		HttpClient hc = new DefaultHttpClient();
 	
 		try {
 			HttpGet request = new HttpGet(authUrl);
 			HttpResponse resp = hc.execute(request);
 			HttpEntity entity = resp.getEntity();
 			String contentString = CheckInManager.convertStreamToString(entity.getContent());
 			JSONObject responseObj = new JSONObject(contentString);
 			int responseCode = resp.getStatusLine().getStatusCode();
 
 			if (responseCode >= 200 && responseCode < 300) {
 				JSONObject res = responseObj.getJSONObject("response");
 				JSONArray groups = res.getJSONArray("groups");
 				JSONObject nearby = groups.getJSONObject(1);
 				JSONArray close = nearby.getJSONArray("items");
 				JSONObject closest = close.getJSONObject(0);
 				this.currentLocationName = closest.getString("name");
 				getNearbyLocationsFromFoursquare(close);
 			} 
 		}catch (IllegalStateException e) {
 			//TODO: Deal with this error
 			e.printStackTrace();
 		} catch (IOException e) {
 			//TODO: Deal with this error
 			e.printStackTrace();
 		} catch (JSONException e) {
 			//TODO: Deal with this error
 			e.printStackTrace();
 		}
 	}
 	
 	private void getNearbyLocationsFromFoursquare(JSONArray close) throws JSONException {
 		this.nearby_locations.clear();
 		for (int i = 0; i < close.length(); i++) {
 			JSONObject nearbyPlace = close.getJSONObject(i);
 			JSONObject location = nearbyPlace.getJSONObject("location");
 			int lat = (int) (Double.parseDouble(location.getString("lat")) * 1E6);
 			int lon = (int) (Double.parseDouble(location.getString("lng")) * 1E6);
 			GeoPoint locationGeoPoint = new GeoPoint(lat, lon);
 			Venue newVenue;
 			try {
 				newVenue = new Venue(nearbyPlace.getString("name"), "url", "image url", locationGeoPoint, Integer.parseInt(location.getString("distance")));
 				this.nearby_locations.add(newVenue);
 			} catch (NumberFormatException e) {
 				//TODO: Deal with this error
 				e.printStackTrace();
 			} catch (JSONException e) {
 				//TODO: Deal with this error
 				e.printStackTrace();
 			}
 		}	
 	}
 	
 	public boolean sameLocation(GeoPoint l1, GeoPoint l2) {
 		if ((l1.getLongitudeE6() == l2.getLongitudeE6()) && (l1.getLatitudeE6() == l2.getLatitudeE6()))
 			return true;
 		return false;
 	}
 	
 	public void toHome(View view) {
 		Intent gotoHome = new Intent(this, Home.class);
 		
 		// pass it the categories to search for
 		ArrayList<String> keys = categories.getKeys();
 		ArrayList<String> values = categories.getValues();
 		for (int i = 0; i < keys.size(); i++) {
 			gotoHome.putExtra(keys.get(i), values.get(i));
 		}
 		startActivity(gotoHome);
 	}
 
 }
