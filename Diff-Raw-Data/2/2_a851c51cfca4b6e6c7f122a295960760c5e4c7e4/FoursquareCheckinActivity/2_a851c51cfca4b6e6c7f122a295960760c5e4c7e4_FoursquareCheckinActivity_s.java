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
 import android.location.Criteria;
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
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class FoursquareCheckinActivity extends ListActivity {
 
	private static final int LOOKUP_LIFETIME = 1000 * 30;
 	private static final int MIN_ACCURACY = 200;
 
 	private static boolean initialLookup = false;
 	private static ArrayList<String> venues = new ArrayList<String>();
 	private static ArrayList<String> venueIDs = new ArrayList<String>();
 
 	private static int locationUpdateStatus = -200;
 	private static LocationManager locationManager = null;
 	private static Location location = null;
 	// Define a listener that responds to location updates
 	LocationListener locationListener = new LocationListener() {
 		public void onLocationChanged(Location newLocation) {
 			// Called when a new location is found
 
 			ProgressBar spn_Locating = (ProgressBar) findViewById(R.id.spn_Locating);
 			spn_Locating.setVisibility(View.VISIBLE);
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
 
 		locationUpdateStatus = 0;
 
 		if (currentBestLocation == null) {
 			// A new location is always better than no location
 			return true;
 		}
 
 		long timeDelta = location.getTime() - currentBestLocation.getTime();
 		boolean isSignificantlyNewer = timeDelta > LOOKUP_LIFETIME;
 		boolean isSignificantlyOlder = timeDelta < -LOOKUP_LIFETIME;
 
 		if (isSignificantlyNewer) {
 			locationUpdateStatus = 1;
 			return true;
 		} else if (isSignificantlyOlder) {
 			locationUpdateStatus = -1;
 			return false;
 		}
 
 		int accuracyDelta = (int) (currentBestLocation.getAccuracy() - location
 				.getAccuracy());
 		System.out.println("New: " + location.getAccuracy() + " | Current: "
 				+ currentBestLocation.getAccuracy());
 		System.out.println(accuracyDelta < -MIN_ACCURACY);
 		boolean isLessAccurate = accuracyDelta > 0;
 		boolean isMoreAccurate = accuracyDelta < 0;
 		boolean isSignificantlyLessAccurate = accuracyDelta < -MIN_ACCURACY;
 
 		boolean isFromSameProvider = isSameProvider(location.getProvider(),
 				currentBestLocation.getProvider());
 
 		if (isMoreAccurate) {
 			locationUpdateStatus = 2;
 			return true;
 		} else if (isSignificantlyNewer && !isLessAccurate) {
 			locationUpdateStatus = 3;
 			return true;
 		} else if (isSignificantlyNewer && !isSignificantlyLessAccurate
 				&& isFromSameProvider) {
 			locationUpdateStatus = 4;
 			return true;
 		}
 
 		locationUpdateStatus = -2;
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
 
 		setContentView(R.layout.foursquare_checkin_activity);
 		setListAdapter(new ArrayAdapter<String>(this,
 				R.layout.foursquare_checkin_venue, venues));
 
 		initialLookup = true;
 		locationManager = (LocationManager) this
 				.getSystemService(Context.LOCATION_SERVICE);
 
 		// Get user's location:
 		String provider = null;
 		Criteria net_criteria = new Criteria();
 		net_criteria.setAccuracy(Criteria.ACCURACY_COARSE);
 		provider = locationManager.getBestProvider(net_criteria, true);
 		Toast.makeText(getApplicationContext(),
 				"Creating location based on " + provider, Toast.LENGTH_SHORT)
 				.show();
 		locationManager.requestLocationUpdates(
 				LocationManager.NETWORK_PROVIDER, LOOKUP_LIFETIME, 0,
 				locationListener);
 
 		Criteria gps_criteria = new Criteria();
 		gps_criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		provider = locationManager.getBestProvider(gps_criteria, true);
 		Toast.makeText(getApplicationContext(),
 				"Creating location based on " + provider, Toast.LENGTH_SHORT)
 				.show();
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 				LOOKUP_LIFETIME, 0, locationListener);
 
 		ListView lv = getListView();
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				String visibility = "private";
 				ToggleButton tgl_Visibility = (ToggleButton) findViewById(R.id.tgl_Visibility);
 				if (tgl_Visibility.isChecked()) {
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
 		locationManager.removeUpdates(locationListener);
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		locationManager.removeUpdates(locationListener);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		locationManager.removeUpdates(locationListener);
 	}
 
 	public void removeFoursquareAuthentication() {
 		Helpers.removeAccessToken();
 	}
 
 	private class SearchVenuesTask extends AsyncTask<Location, Void, Boolean> {
 		/**
 		 * The system calls this to perform work in a worker thread and delivers
 		 * it the parameters given to AsyncTask.execute()
 		 */
 		@Override
 		protected Boolean doInBackground(Location... locations) {
 			Location newLocation = locations[0];
 			Boolean venuesUpdated = false;
 
 			if (initialLookup || isBetterLocation(newLocation, location)) {
 				initialLookup = false;
 
 				System.out.println("Accepting new Location from "
 						+ newLocation.getProvider() + " at "
 						+ newLocation.getTime());
 
 				String message = "...";
 				switch (locationUpdateStatus) {
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
 
 				System.out.println(newLocation.getProvider() + " > " + message);
 			}
 
 			location = newLocation;
 
 			try {
 
 				System.out.println(location.getProvider() + " > "
 						+ "Searching for venues...");
 
 				venuesUpdated = false;
 
 				// initialize
 				InputStream is = null;
 				String result = "";
 				String url = "https://api.foursquare.com/v2/venues/search?v=20120728&oauth_token="
 						+ Helpers.readAccessToken()
 						+ "&ll="
 						+ location.getLatitude()
 						+ ","
 						+ location.getLongitude();
 
 				// http get
 				System.out.println(location.getProvider() + " > "
 						+ "Contacting Foursquare...");
 				HttpClient httpclient = new DefaultHttpClient();
 				HttpGet httpGet = new HttpGet(url);
 				HttpResponse httpResponse = httpclient.execute(httpGet);
 				HttpEntity entity = httpResponse.getEntity();
 				is = entity.getContent();
 
 				// convert response to string
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
 				returnedVenues = response.getJSONArray("venues");
 
 				venues.clear();
 				venueIDs.clear();
 
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
 
 					// System.out.println("Adding venue: " + venueName);
 					venues.add(venueCategory + ": " + venueName);
 					venueIDs.add(e.getString("id"));
 
 					venuesUpdated = true;
 				}
 
 			} catch (ClientProtocolException e) {
 				System.err.println("ClientProtocolException");
 				locationUpdateStatus = -3;
 			} catch (IOException e) {
 				System.err.println("IOException");
 				locationUpdateStatus = -3;
 			} catch (JSONException e) {
 				System.err.println("JSONException");
 				locationUpdateStatus = -4;
 			}
 
 			return venuesUpdated;
 		}
 
 		/**
 		 * The system calls this to perform work in the UI thread and delivers
 		 * the result from doInBackground()
 		 */
 		protected void onPostExecute(Boolean venuesUpdated) {
 			System.out.println(location.getProvider() + " > "
 					+ "Post-Execute! Status:" + locationUpdateStatus);
 
 			ProgressBar spn_Locating = (ProgressBar) findViewById(R.id.spn_Locating);
 			spn_Locating.setVisibility(View.INVISIBLE);
 			spn_Locating.invalidate();
 			spn_Locating.requestLayout();
 
 			if (venuesUpdated) {
 
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
 		/**
 		 * The system calls this to perform work in a worker thread and delivers
 		 * it the parameters given to AsyncTask.execute()
 		 */
 		@Override
 		protected Boolean doInBackground(JSONObject... checkin) {
 
 			boolean success = false;
 
 			try {
 
 				String venueID = checkin[0].getString("venueID");
 				int position = checkin[0].getInt("position");
 				String visibility = checkin[0].getString("visibility");
 
 				String venueName = venues.get(position);
 
 				// initialize
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
 
 				// http post
 				System.out.println("Contacting Foursquare...");
 				HttpClient httpclient = new DefaultHttpClient();
 				HttpPost httpPost = new HttpPost(url);
 				HttpResponse response = httpclient.execute(httpPost);
 				HttpEntity entity = response.getEntity();
 				is = entity.getContent();
 
 				// convert response to string
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
 				System.err.println("ClientProtocolException");
 			} catch (IOException e) {
 				System.err.println("IOException");
 			} catch (JSONException e) {
 				System.err.println("JSONException");
 			}
 
 			return success;
 		}
 
 		/**
 		 * The system calls this to perform work in the UI thread and delivers
 		 * the result from doInBackground()
 		 */
 		protected void onPostExecute(Boolean checkinSuccessful) {
 			System.out
 					.println(location.getProvider() + " > " + "Post-Execute!");
 
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
