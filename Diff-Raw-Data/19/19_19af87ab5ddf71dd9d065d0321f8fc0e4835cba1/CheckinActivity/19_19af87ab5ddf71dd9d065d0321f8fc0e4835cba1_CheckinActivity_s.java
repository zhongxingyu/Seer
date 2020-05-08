 package com.seawolfsanctuary.keepingtracks.foursquare;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 import com.seawolfsanctuary.keepingtracks.Helpers;
 import com.seawolfsanctuary.keepingtracks.R;
 
 public class CheckinActivity extends ListActivity {
 
 	private Bundle checkin_details = new Bundle();
 
 	private static final long LONG_LOOKUP_LIFETIME = 1000 * 60;
 	private static final long SHORT_LOOKUP_LIFETIME = 1000 * 15;
 
 	private static long lastLookup = 0;
 
 	private static long getLastLookup() {
 		return lastLookup;
 	}
 
 	private static void setLastLookup(long lastLookup) {
 		CheckinActivity.lastLookup = lastLookup;
 	}
 
 	private static ArrayList<String> venues = new ArrayList<String>();
 
 	private static ArrayList<String> getVenues() {
 		return venues;
 	}
 
 	private static void setVenues(ArrayList<String> venues) {
 		CheckinActivity.venues = venues;
 	}
 
 	private static ArrayList<String> venueIDs = new ArrayList<String>();
 
 	private static ArrayList<String> getVenueIDs() {
 		return venueIDs;
 	}
 
 	private static void setVenueIDs(ArrayList<String> venueIDs) {
 		CheckinActivity.venueIDs = venueIDs;
 	}
 
 	private static boolean updatingLocation = false;
 
 	public static boolean isUpdatingLocation() {
 		return updatingLocation;
 	}
 
 	public static void amUpdatingLocation(boolean updatingLocation) {
 		CheckinActivity.updatingLocation = updatingLocation;
 	}
 
 	private static LocationManager locationManager = null;
 
 	private static LocationManager getLocationManager() {
 		return locationManager;
 	}
 
 	private static void setLocationManager(LocationManager locationManager) {
 		CheckinActivity.locationManager = locationManager;
 	}
 
 	private static Location location = null;
 
 	private static Location getLocation() {
 		return location;
 	}
 
 	private static void setLocation(Location location) {
 		CheckinActivity.location = location;
 	}
 
 	// Define a listener that responds to location updates
 	LocationListener locationListener = new LocationListener() {
 		public void onLocationChanged(Location newLocation) {
 			// Called when a new location is found
 			ProgressBar spn_Locating = (ProgressBar) findViewById(R.id.spn_Locating);
 			System.out.println("New Location from: "
 					+ newLocation.getProvider());
 
 			if (isBetterLocation(newLocation, getLocation())) {
 				spn_Locating.setVisibility(View.VISIBLE);
 				setLastLookup(newLocation.getTime());
 				new SearchVenuesTask().execute(newLocation);
 			} else {
 				spn_Locating.setVisibility(View.INVISIBLE);
 			}
 
 			spn_Locating.postInvalidate();
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 		}
 
 		public void onProviderEnabled(String provider) {
 		}
 
 		public void onProviderDisabled(String provider) {
 		}
 	};
 
 	protected boolean isBetterLocation(Location location,
 			Location currentBestLocation) {
 
 		if (isUpdatingLocation()) {
 			System.out
 					.println("FYI: Currently updating from another provider/status.");
 		}
 
 		// Initial Network update
 		if (currentBestLocation == null) {
 			System.out.println("No current best location");
 			return true;
 		}
 
 		int accuracyDelta = (int) ((currentBestLocation.getAccuracy() - location
 				.getAccuracy()));
 		boolean isMoreAccurate = accuracyDelta > -1;
 
 		boolean isFromSameProvider = isSameProvider(location.getProvider(),
 				currentBestLocation.getProvider());
 
 		boolean isVeryOld = ((location.getTime() - currentBestLocation
 				.getTime()) > LONG_LOOKUP_LIFETIME);
 		boolean isSlightlyOld = ((location.getTime() - currentBestLocation
 				.getTime()) > SHORT_LOOKUP_LIFETIME);
 
 		System.out.println("Provider: " + currentBestLocation.getProvider()
 				+ " -> " + location.getProvider());
 		System.out.println("Accuracy: " + currentBestLocation.getAccuracy()
 				+ " (" + accuracyDelta + " should be positive) "
 				+ location.getAccuracy() + " (more accurate? " + isMoreAccurate
 				+ ")");
 		System.out.println("Very Old: " + currentBestLocation.getTime() + " ("
 				+ (location.getTime() - currentBestLocation.getTime())
 				+ " should be more than " + LONG_LOOKUP_LIFETIME + ") "
 				+ location.getTime() + " (old enough? " + isVeryOld + ")");
 		System.out.println("Slightly Old: " + currentBestLocation.getTime()
 				+ " (" + (location.getTime() - currentBestLocation.getTime())
 				+ " should be more than " + SHORT_LOOKUP_LIFETIME + ") "
 				+ location.getTime() + " (nearly old enough? " + isSlightlyOld
 				+ ")");
 
 		if (isFromSameProvider && isMoreAccurate && isVeryOld) {
 			// Same provider giving better update
 			System.out.println("More accurate, same provider, old enough.");
 			return true;
 		} else if (!isFromSameProvider && isMoreAccurate && isSlightlyOld) {
 			// Different provider giving better update
 			System.out
 					.println("More accurate, another provider, nearly old enough.");
 			return true;
 		} else {
 			System.out.println("No criteria met:");
 			System.out.println("!isVeryOld[" + isVeryOld
 					+ "] && isMoreAccurate[" + isMoreAccurate + "]");
 			System.out.println("!isFromSameProvider[" + !isFromSameProvider
 					+ "] && isMoreAccurate[" + isMoreAccurate
 					+ "] && isSlightlyOld[" + isSlightlyOld + "]");
 
 			return false;
 		}
 
 	}
 
 	private boolean isSameProvider(String provider1, String provider2) {
 		if (provider1 == null) {
 			return provider2 == null;
 		}
 		return provider1.equals(provider2);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.foursquare_context_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.foursquare_logout:
 			if (Helpers.removeAccessToken() == true) {
 				CheckinActivity.this.finish();
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.foursquare_logout_success),
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.foursquare_logout_failure),
 
 						Toast.LENGTH_LONG).show();
 			}
 
 			return true;
 		default:
 			System.out.println("Unknown action: " + item.getItemId());
 			return true;
 		}
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.foursquare_checkin_activity);
 
 		checkin_details = getIntent().getExtras();
 
 		setListAdapter(new ArrayAdapter<String>(this,
 				R.layout.foursquare_checkin_venue, getVenues()));
 
 		setLocationManager((LocationManager) this
 				.getSystemService(Context.LOCATION_SERVICE));
 
 		removeLocationListener();
 		attachNetworkLocation();
 		attachGPSLocation();
 
 		ListView lv = getListView();
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				String visibility = "private";
 				CheckBox chk_Visibility = (CheckBox) findViewById(R.id.chk_Visibility);
 				if (chk_Visibility.isChecked() == true) {
 					visibility = "public";
 				}
 
 				try {
 					JSONObject params = new JSONObject();
 					params.put("venueID", getVenueIDs().get(position));
 					params.put("venueName", getVenues().get(position));
 					params.put("position", position);
 					params.put("visibility", visibility);
 
 					// Delete this to check-in with no shout
 					String checkinMessage = getString(
 							R.string.foursquare_checkin_mesage_blank,
 							params.getString("venueName"));
 
 					// When Checking-In from AddActivity
 					if (checkin_details != null) {
 						boolean fromStn = checkin_details.getString("from_stn")
 								.length() > 0;
 						boolean toStn = checkin_details.getString("to_stn")
 								.length() > 0;
 						boolean unit = checkin_details
 								.getString("detail_class").length() > 0;
 						boolean headcode = checkin_details.getString(
 								"detail_headcode").length() > 0;
 
 						if (fromStn && toStn && !unit && !headcode) {
 							checkinMessage = getString(
 									R.string.foursquare_checkin_mesage_stations,
 									checkin_details.getString("from_stn"),
 									checkin_details.getString("to_stn"));
 						}
 						if (fromStn && toStn && unit && !headcode) {
 							checkinMessage = getString(
 									R.string.foursquare_checkin_mesage_advanced_half,
 									checkin_details.getString("from_stn"),
 									checkin_details.getString("to_stn"),
 									checkin_details.getString("detail_class"));
 						}
 						if (fromStn && toStn && !unit && headcode) {
 							checkinMessage = getString(
 									R.string.foursquare_checkin_mesage_advanced_half,
 									checkin_details.getString("from_stn"),
 									checkin_details.getString("to_stn"),
 									checkin_details
 											.getString("detail_headcode"));
 						}
						if (fromStn && toStn && unit && !headcode) {
 							checkinMessage = getString(
 									R.string.foursquare_checkin_mesage_advanced_full,
 									checkin_details.getString("from_stn"),
 									checkin_details.getString("to_stn"),
 									checkin_details
 											.getString("detail_headcode"),
 									checkin_details.getString("detail_class"));
 						}
 					}
 
 					CheckBox chk_Shout = (CheckBox) findViewById(R.id.chk_Shout);
 					if (chk_Shout.isChecked()) {
 						params.put("message", checkinMessage);
 						Toast.makeText(getBaseContext(),
 								params.getString("message"), Toast.LENGTH_LONG)
 								.show();
 					} else {
 						params.put("message", "");
 					}
 
 					new CheckinTask().execute(params);
 				} catch (JSONException e) {
 					System.err.println("JSONException");
 				}
 
 			}
 		});
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		removeLocationListener();
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		removeLocationListener();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		removeLocationListener();
 	}
 
 	private void attachNetworkLocation() {
 		getLocationManager().requestLocationUpdates(
 				LocationManager.NETWORK_PROVIDER, SHORT_LOOKUP_LIFETIME, 0,
 				locationListener);
 	}
 
 	private void attachGPSLocation() {
 		getLocationManager().requestLocationUpdates(
 				LocationManager.GPS_PROVIDER, SHORT_LOOKUP_LIFETIME, 0,
 				locationListener);
 	}
 
 	private void removeLocationListener() {
 		getLocationManager().removeUpdates(locationListener);
 	}
 
 	public void removeFoursquareAuthentication() {
 		Helpers.removeAccessToken();
 	}
 
 	private class SearchVenuesTask extends AsyncTask<Location, Void, Boolean> {
 		@Override
 		protected Boolean doInBackground(Location... locations) {
 			Location newLocation = locations[0];
 			Boolean venuesUpdated = false;
 
 			if (!isUpdatingLocation()) {
 				amUpdatingLocation(false);
 			}
 
 			amUpdatingLocation(true);
 			setLastLookup(newLocation.getTime());
 
 			System.out.println("Accepting new Location from "
 					+ newLocation.getProvider() + " at "
 					+ newLocation.getTime());
 
 			setLocation(newLocation);
 
 			try {
 				Location location = getLocation();
 
 				InputStream is = null;
 				String result = "";
 				String url = "https://api.foursquare.com/v2/venues/search?v=20120728&oauth_token="
 						+ Helpers.readAccessToken()
 						+ "&ll="
 						+ location.getLatitude()
 						+ ","
 						+ location.getLongitude();
 
 				System.out.println(location.getProvider() + " > "
 						+ "Contacting Foursquare...");
 				HttpClient httpclient = new DefaultHttpClient();
 				HttpGet httpGet = new HttpGet(url);
 				HttpResponse httpResponse = httpclient.execute(httpGet);
 				HttpEntity entity = httpResponse.getEntity();
 				is = entity.getContent();
 
 				System.out.println(location.getProvider() + " > "
 						+ "Recieving response...");
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(is, "utf-8"), 8);
 				StringBuilder sb = new StringBuilder();
 				String line = null;
 				while ((line = reader.readLine()) != null) {
 					sb.append(line + "\n");
 				}
 				is.close();
 				result = sb.toString();
 
 				JSONArray returnedVenues = null;
 
 				System.out.println(location.getProvider() + " > "
 						+ "Parsing response...");
 				JSONObject jArray = new JSONObject(result);
 
 				JSONObject response = jArray.getJSONObject("response");
 				try {
 					returnedVenues = response.getJSONArray("venues");
 				} catch (JSONException e) {
 					System.err.println(response.toString());
 				}
 
 				ArrayList<String> venues = new ArrayList<String>();
 				ArrayList<String> venueIDs = new ArrayList<String>();
 
 				System.out.println(location.getProvider() + " > "
 						+ "Parsing venues...");
 				for (int i = 0; i < returnedVenues.length(); i++) {
 					JSONObject e = returnedVenues.getJSONObject(i);
 					String venueName = e.getString("name");
 
 					String venueCategory = "Uncategorised";
 					JSONArray venueCategories = e.getJSONArray("categories");
 					for (int cc = 0; cc < venueCategories.length(); cc++) {
 						JSONObject c = venueCategories.getJSONObject(cc);
 						String categoryPrimary = c.getString("primary");
 						if (categoryPrimary == "true") {
 							venueCategory = c.getString("name");
 						}
 					}
 
 					venues.add(venueCategory + ": " + venueName);
 					venueIDs.add(e.getString("id"));
 
 				}
 
 				setVenues(venues);
 				setVenueIDs(venueIDs);
 
 				System.out.println(venues);
 
 				amUpdatingLocation(false);
 				venuesUpdated = true;
 
 			} catch (ClientProtocolException e) {
 				System.err.println("ClientProtocolException =>"
 						+ e.getMessage());
 			} catch (IOException e) {
 				System.err.println("IOException =>" + e.getMessage());
 			} catch (JSONException e) {
 				System.err.println("JSONException =>" + e.getMessage());
 			}
 
 			return venuesUpdated;
 		}
 
 		protected void onPostExecute(Boolean venuesUpdated) {
 			amUpdatingLocation(false);
 			Location location = getLocation();
 
 			System.out.println("Post-Execute!");
 
 			ProgressBar spn_Locating = (ProgressBar) findViewById(R.id.spn_Locating);
 			spn_Locating.setVisibility(View.INVISIBLE);
 			spn_Locating.postInvalidate();
 
 			if (venuesUpdated) {
 				System.out.println(location.getProvider() + " > "
 						+ "Venues Updated!");
 
 				setListAdapter(new ArrayAdapter<String>(CheckinActivity.this,
 						R.layout.foursquare_checkin_venue, getVenues()));
 
 				Toast.makeText(
 						getApplicationContext(),
 						getString(R.string.foursquare_updates_from,
 								location.getProvider()), Toast.LENGTH_SHORT)
 						.show();
 			} else {
 				System.out.println(location.getProvider()
 						+ " > Venues not updated.");
 			}
 
 		}
 	}
 
 	private class CheckinTask extends AsyncTask<JSONObject, Void, Boolean> {
 		@Override
 		protected Boolean doInBackground(JSONObject... checkin) {
 
 			boolean success = false;
 
 			try {
 
 				Location location = getLocation();
 
 				String venueID = checkin[0].getString("venueID");
 				int position = checkin[0].getInt("position");
 				String visibility = checkin[0].getString("visibility");
 				String venueName = getVenues().get(position);
 				String message = checkin[0].getString("message");
 
 				InputStream is = null;
 				String result = "";
 				String encodedShout = URLEncoder.encode(message, "utf-8");
 				String url = "https://api.foursquare.com/v2/checkins/add?v=20120728&oauth_token="
 						+ Helpers.readAccessToken()
 						+ "&venueId="
 						+ venueID
 						+ "&ll="
 						+ location.getLatitude()
 						+ ","
 						+ location.getLongitude()
 						+ "&broadcast="
 						+ visibility
 						+ "&shout=" + encodedShout;
 
 				System.out.println("URL: " + url);
 
 				System.out.println("Contacting Foursquare...");
 				HttpClient httpclient = new DefaultHttpClient();
 				HttpPost httpPost = new HttpPost(url);
 				HttpResponse response = httpclient.execute(httpPost);
 				HttpEntity entity = response.getEntity();
 				is = entity.getContent();
 
 				System.out.println("Recieving response...");
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(is, "utf-8"), 8);
 				StringBuilder sb = new StringBuilder();
 				String line = null;
 				while ((line = reader.readLine()) != null) {
 					sb.append(line + "\n");
 				}
 				is.close();
 				result = sb.toString();
 
 				System.out.println(result);
 
 				int returnedStatus = -1;
 
 				System.out.println("Parsing response...");
 				JSONObject postResponse = new JSONObject(result);
 
 				System.out.println("Selecting response...");
 				JSONObject meta = postResponse.getJSONObject("meta");
 
 				returnedStatus = meta.getInt("code");
 
 				System.out.println("Parsing checkin response: "
 						+ Integer.toString(returnedStatus));
 
 				success = (returnedStatus == 200);
 
 			} catch (ClientProtocolException e) {
 				System.err.println("ClientProtocolException =>"
 						+ e.getMessage());
 			} catch (IOException e) {
 				System.err.println("IOException =>" + e.getMessage());
 			} catch (JSONException e) {
 				System.err.println("JSONException =>" + e.getMessage());
 			}
 
 			return success;
 		}
 
 		protected void onPostExecute(Boolean checkinSuccessful) {
 			System.out.println(getLocation().getProvider() + " > "
 					+ "Post-Execute!");
 
 			if (checkinSuccessful) {
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.foursquare_checkin_success),
 						Toast.LENGTH_SHORT).show();
 				CheckinActivity.this.finish();
 			} else {
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.foursquare_checkin_failure),
 						Toast.LENGTH_SHORT).show();
 			}
 
 		}
 	}
 }
