 package com.seawolfsanctuary.tmt;
 
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
 import android.widget.BaseAdapter;
 import android.widget.CheckBox;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 public class FoursquareCheckinActivity extends ListActivity {
 
	private static final int LOOKUP_LIFETIME = 1000 * 60 * 1;
 	private static final int MIN_ACCURACY = 200;
 
 	private static long lastLookup = 0;
 
 	private static long getLastLookup() {
 		return lastLookup;
 	}
 
 	private static void setLastLookup(long lastLookup) {
 		FoursquareCheckinActivity.lastLookup = lastLookup;
 	}
 
 	private static ArrayList<String> venues = new ArrayList<String>();
 
 	private static ArrayList<String> getVenues() {
 		return venues;
 	}
 
 	private static void setVenues(ArrayList<String> venues) {
 		FoursquareCheckinActivity.venues = venues;
 	}
 
 	private static ArrayList<String> venueIDs = new ArrayList<String>();
 
 	private static ArrayList<String> getVenueIDs() {
 		return venueIDs;
 	}
 
 	private static void setVenueIDs(ArrayList<String> venueIDs) {
 		FoursquareCheckinActivity.venueIDs = venueIDs;
 	}
 
 	private static boolean updatingLocation = false;
 
 	public static boolean isUpdatingLocation() {
 		return updatingLocation;
 	}
 
 	public static void amUpdatingLocation(boolean updatingLocation) {
 		FoursquareCheckinActivity.updatingLocation = updatingLocation;
 	}
 
 	private static int locationUpdateStatus = -200;
 
 	private static int getLocationUpdateStatus() {
 		return locationUpdateStatus;
 	}
 
 	private static void setLocationUpdateStatus(int locationUpdateStatus) {
 		FoursquareCheckinActivity.locationUpdateStatus = locationUpdateStatus;
 	}
 
 	private static LocationManager locationManager = null;
 
 	private static LocationManager getLocationManager() {
 		return locationManager;
 	}
 
 	private static void setLocationManager(LocationManager locationManager) {
 		FoursquareCheckinActivity.locationManager = locationManager;
 	}
 
 	private static Location location = null;
 
 	private static Location getLocation() {
 		return location;
 	}
 
 	private static void setLocation(Location location) {
 		FoursquareCheckinActivity.location = location;
 	}
 
 	// Define a listener that responds to location updates
 	LocationListener locationListener = new LocationListener() {
 		public void onLocationChanged(Location newLocation) {
 			// Called when a new location is found
 			long lastUpdateTime = getLastLookup() + LOOKUP_LIFETIME;
 
 			ProgressBar spn_Locating = (ProgressBar) findViewById(R.id.spn_Locating);
 
 			if (lastUpdateTime < newLocation.getTime()) {
 				spn_Locating.setVisibility(View.VISIBLE);
 				setLastLookup(newLocation.getTime());
 				new SearchVenuesTask().execute(newLocation);
 			} else {
 				spn_Locating.setVisibility(View.INVISIBLE);
 			}
 
 			spn_Locating.invalidate();
 			spn_Locating.requestLayout();
 
 			new SearchVenuesTask().execute(newLocation);
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
 
 		setLocationUpdateStatus(0);
 
 		if (currentBestLocation == null) {
 			setLocationUpdateStatus(200);
 			return true;
 		}
 
 		int accuracyDelta = (int) (currentBestLocation.getAccuracy() - location
 				.getAccuracy());
 
 		boolean isLessAccurate = accuracyDelta > 0;
 		boolean isMoreAccurate = accuracyDelta < 0;
 		boolean isSignificantlyLessAccurate = accuracyDelta < -MIN_ACCURACY;
 
 		boolean isFromSameProvider = isSameProvider(location.getProvider(),
 				currentBestLocation.getProvider());
 
 		if (isMoreAccurate) {
 			setLocationUpdateStatus(2);
 			return true;
 		} else if (!isLessAccurate) {
 			setLocationUpdateStatus(3);
 			return true;
 		} else if (!isSignificantlyLessAccurate && isFromSameProvider) {
 			setLocationUpdateStatus(4);
 			return false;
 		}
 
 		setLocationUpdateStatus(-2);
 		return false;
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
 		case R.id.foursquare_deauthenticate:
 			if (Helpers.removeAccessToken() == true) {
 				FoursquareCheckinActivity.this.finish();
 				Toast.makeText(
 						getApplicationContext(),
 						"You must now re-authenticate with Foursquare to check-in.",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(
 						getApplicationContext(),
 						"Could not revoke access. Remove this application from your account by visiting the Foursquare website.",
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
 
 		venues = getVenues();
 		venueIDs = getVenueIDs();
 
 		setContentView(R.layout.foursquare_checkin_activity);
 		setListAdapter(new ArrayAdapter<String>(this,
 				R.layout.foursquare_checkin_venue, venues));
 
 		setLocationManager((LocationManager) this
 				.getSystemService(Context.LOCATION_SERVICE));
 
 		// Get user's location:
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
 					params.put("venueID", venueIDs.get(position));
 					params.put("venueName", venues.get(position));
 					params.put("position", position);
 					params.put("visibility", visibility);
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
 				LocationManager.NETWORK_PROVIDER, LOOKUP_LIFETIME, 0,
 				locationListener);
 	}
 
 	private void attachGPSLocation() {
 		getLocationManager().requestLocationUpdates(
 				LocationManager.GPS_PROVIDER, LOOKUP_LIFETIME, 0,
 				locationListener);
 	}
 
 	private void removeLocationListener() {
 		if (getLocationManager().getAllProviders().size() > 0) {
 			getLocationManager().removeUpdates(locationListener);
 		}
 	}
 
 	public void removeFoursquareAuthentication() {
 		Helpers.removeAccessToken();
 	}
 
 	private class SearchVenuesTask extends AsyncTask<Location, Void, Boolean> {
 		@Override
 		protected Boolean doInBackground(Location... locations) {
 			Location newLocation = locations[0];
 
 			if (isUpdatingLocation() == false) {
 				amUpdatingLocation(false);
 
 				if (getLocation() == null
 						|| isBetterLocation(newLocation, getLocation()) == true) {
 
 					setLastLookup(newLocation.getTime());
 					amUpdatingLocation(true);
 
 					System.out.println("Accepting new Location from "
 							+ newLocation.getProvider() + " at "
 							+ newLocation.getTime());
 
 					String message = "...";
 					switch (getLocationUpdateStatus()) {
 					case -4:
 						message = "Foursquare sent me something I couldn't understand.";
 						break;
 					case -3:
 						message = "Could not connect to the Internet.";
 						break;
 					case -2:
 						message = "Not worthy of an update.";
 						break;
 					case -1:
 						message = "Significantly older.";
 						break;
 					case 0:
 						message = "Current location unknown. Please switch on GPS.";
 						break;
 					case 1:
 						message = "Significantly newer.";
 						break;
 					case 2:
 						message = "More accurate.";
 						break;
 					case 3:
 						message = "Significantly newer and not less accurate.";
 						break;
 					case 4:
 						message = "Signifcantly newer, not significantly less accurate, from same provider.";
 						break;
 					}
 
 					System.out.println(newLocation.getProvider() + " > "
 							+ message);
 				}
 
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
 						JSONArray venueCategories = e
 								.getJSONArray("categories");
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
 
 					amUpdatingLocation(false);
 					return true;
 
 				} catch (ClientProtocolException e) {
 					System.err.println("ClientProtocolException =>"
 							+ e.getMessage());
 					setLocationUpdateStatus(-3);
 					return false;
 				} catch (IOException e) {
 					System.err.println("IOException =>" + e.getMessage());
 					setLocationUpdateStatus(-3);
 					return false;
 				} catch (JSONException e) {
 					System.err.println("JSONException =>" + e.getMessage());
 					setLocationUpdateStatus(-4);
 					return false;
 				}
 			}
 
 			return false;
 		}
 
 		protected void onPostExecute(Boolean venuesUpdated) {
 			amUpdatingLocation(false);
 			Location location = getLocation();
 
 			System.out.println(location.getProvider() + " > "
 					+ "Post-Execute! Venues "
 					+ (venuesUpdated == true ? "" : "not ")
 					+ " updated. Status: " + getLocationUpdateStatus());
 
 			ProgressBar spn_Locating = (ProgressBar) findViewById(R.id.spn_Locating);
 			spn_Locating.setVisibility(View.INVISIBLE);
 			spn_Locating.invalidate();
 			spn_Locating.requestLayout();
 
 			if (venuesUpdated == true) {
 
 				System.out.println(location.getProvider() + " > "
 						+ "Venues Updated!");
 
 				Toast.makeText(getApplicationContext(),
 						"Updated venues from " + location.getProvider() + ".",
 						Toast.LENGTH_SHORT).show();
 
 				ListView lst_Venues = getListView();
 				ListAdapter lst_Venues_Adaptor = lst_Venues.getAdapter();
 				((BaseAdapter) lst_Venues_Adaptor).notifyDataSetChanged();
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
 
 				InputStream is = null;
 				String result = "";
 				String encodedShout = URLEncoder.encode("I'm checking into "
 						+ venueName + " using TMT!", "utf-8");
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
 				Toast.makeText(getApplicationContext(), "Checked in!",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(getApplicationContext(), "Could not check in.",
 						Toast.LENGTH_SHORT).show();
 			}
 
 		}
 	}
 }
