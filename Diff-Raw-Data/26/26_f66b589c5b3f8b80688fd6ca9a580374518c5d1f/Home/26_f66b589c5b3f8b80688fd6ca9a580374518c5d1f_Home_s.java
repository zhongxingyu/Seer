 package com.dotcom.nextup.activities;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
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
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.drawable.Drawable;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.dotcom.nextup.R;
import com.dotcom.nextup.categorymodels.CategoryHistogram;
 import com.dotcom.nextup.categorymodels.CheckIn;
 import com.dotcom.nextup.categorymodels.CheckInManager;
 import com.dotcom.nextup.classes.Venue;
 import com.dotcom.nextup.datastoring.Update;
 import com.dotcom.nextup.oauth.AndroidOAuth;
 import com.dotcom.nextup.yelp.Yelp;
 import com.google.android.maps.GeoPoint;
 
 public class Home extends ListActivity {
 	/** Called when the activity is first created. */
 	private Boolean codeStored;
 	private Boolean receivedLocationUpdate;
 	private String token;
 	private String code;
 	private ArrayList<CheckIn> checkIns;

	private CategoryHistogram ch;

	@SuppressWarnings("unused")
 	private SharedPreferences pref;
 	private AndroidOAuth oa;
 	private CheckInManager checkInManager;
 	private LocationManager locationManager;
 	private LocationListener locationListener;
 	public GeoPoint currentLocation;
 	public GeoPoint lastLocation;
 	public String currentLocationName;
 	public String lastLocationName;
 	public ArrayList<Venue> nearbyLocations;
 
 	private ArrayList<Venue> my_venues = null;
 	private VenueAdapter m_adapter;
 	ProgressDialog dialog = null;
 	private Runnable viewVenues;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_checkedin);
 		receivedLocationUpdate = false;
 		nearbyLocations = new ArrayList<Venue>();
 		pref = PreferenceManager.getDefaultSharedPreferences(this);
 		oa = new AndroidOAuth(this);
 		checkInManager = new CheckInManager();
 		codeStored = getCode(getIntent());
 
 		dialog = ProgressDialog.show(this, "Loading",
 				"Creating Personal Recommendations...");
 
 		dealWithCode(codeStored);
 		if (this.checkIns != null)
 			getLastLocation();
 			getLastLocationName();
			

		if (this.checkIns != null)
			getLastLocation();
		getCurrentLocation();
 		
 		
 		ArrayList<Category> suggestions = BackendManager.getSuggestionsFromCloud(new Category(this.lastLocationName));
 		System.out.println(suggestions.get(0).getName());
 
 		my_venues = new ArrayList<Venue>();
 		this.m_adapter = new VenueAdapter(this, R.layout.row, my_venues);
 		setListAdapter(this.m_adapter);
 		
 		/** @TODO: should only do this once location has been found */
 		viewVenues = new Runnable() {
 			@Override
 			public void run() {
 				getVenues();
 			}
 		};
 
 		Thread thread = new Thread(null, viewVenues, "GettingVenuesThread");
 		thread.start();
 		dialog = ProgressDialog.show(Home.this, "Please wait...", "Pulling up cool places...", true);
 		
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
 				if (!receivedLocationUpdate)
 					getCurrentLocationNameFromFoursquare(currentLocation);
 				receivedLocationUpdate = true;
 				dealWithLocation();
 			}
 		};
 
 		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 		Location temp = null;
 		if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
 			temp = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 			currentLocation = new GeoPoint((int)(temp.getLatitude() * 1E6), (int)(temp.getLongitude() * 1E6));
 			if (currentLocationName == null)
 				getCurrentLocationNameFromFoursquare(currentLocation);
 			dealWithLocation();
 		}
 		if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
 			temp = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 			currentLocation = new GeoPoint((int)(temp.getLatitude() * 1E6), (int)(temp.getLongitude() * 1E6));
 			if (currentLocationName == null)
 				getCurrentLocationNameFromFoursquare(currentLocation);
 			dealWithLocation();		
 		}
 
 	}
 
 	public void onResume() {
 		 super.onResume();
 	     locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 	     locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 	     if (currentLocation != null) {
 	       	if (currentLocationName == null) 
 	     		getCurrentLocationNameFromFoursquare(currentLocation);
 	        dealWithLocation();
 	     }
 
 		 if (m_adapter != null && m_adapter.getItems().size() > 0) {
 			 m_adapter.notifyDataSetChanged();
 		 }
 	}
 	
 	 //pauses listener while app is inactive
     @Override
     public void onPause() {
         super.onPause();
         locationManager.removeUpdates(locationListener);
     }
     
 	/*----------------------- ACCESS TOKEN CODE BELOW --------------------*/
 
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
 						Update.update(pref, getString(R.string.updateTimePreferenceName), this.checkIns, Home.this);
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
 			Toast.makeText(Home.this,
 					"There was an error connecting to Foursquare",
 					Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 			//TODO: Deal with this error
 		} catch (IOException e) {
 			Toast.makeText(Home.this,
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
 			Intent i = new Intent(Home.this, FourSquareLoginPrompt.class);
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
 
 	/*---------------- UI CODE BELOW *----------------------*/
 
 	private void getNearbyLocationsFromFoursquare(JSONArray close) throws JSONException {
 		for (int i = 0; i < close.length(); i++) {
 			JSONObject nearbyPlace = close.getJSONObject(i);
 			JSONObject location = nearbyPlace.getJSONObject("location");
 			int lat = (int) (Double.parseDouble(location.getString("lat")) * 1E6);
 			int lon = (int) (Double.parseDouble(location.getString("lng")) * 1E6);
 			GeoPoint locationGeoPoint = new GeoPoint(lat, lon);
 			Venue newVenue;
 			try {
 				newVenue = new Venue(nearbyPlace.getString("name"), "url", "image url", locationGeoPoint, Integer.parseInt(location.getString("distance")));
 				this.nearbyLocations.add(newVenue);
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
 
 	private void dealWithLocation() {
 		if (sameLocation(lastLocation, currentLocation) && lastLocation != null) {
 			// they checked in at a place and are still there
 			setContentView(R.layout.main_checkedin);
 		}
 		if (lastLocation != null && currentLocation == null) {
 			// they checked in at a place, we don't know where they are now
 			// but we'll assume they're still there
 			setContentView(R.layout.main_checkedin);
 		}
 		if (lastLocation == null && currentLocation != null) {
 			// we don't know their last check in, but we know where they are now
 			// prompt them to check in
 			setContentView(R.layout.main_notcheckedin);
 		}
 		if (sameLocation(lastLocation, currentLocation) && lastLocation == null) {
 			// we dont know their last check in or where they are now
 			// this.getLocation();
 			setContentView(R.layout.main_notcheckedin); // need to change this
 			// to main_wedontknow
 			// later
 		}
 	}
 
 	public void toFriends(View view) {
 		Intent toFriends = new Intent(this, Friends.class);
 		startActivity(toFriends);
 	}
 
 	public void toMap(View view) {
 		Intent toMap = new Intent(this, Map.class);
 		startActivity(toMap);
 	}
 
 	public void toPreferences(View view) {
 		Intent toPreferences = new Intent(this, Preferences.class);
 		startActivity(toPreferences);
 	}
 
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 	    Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( my_venues.get(position).getURL() ) );
 	    startActivity( browse );
 	}
 	
 	
 	private Runnable returnRes = new Runnable() {
 		@Override
 		public void run() {
 			if (my_venues != null && my_venues.size() > 0) {
 				m_adapter.notifyDataSetChanged();
 				for (int i = 0; i < my_venues.size(); i++)
 					m_adapter.add(my_venues.get(i));
 			}
 			dialog.dismiss();
 			m_adapter.notifyDataSetChanged();
 		}
 	};
 
 	private void getVenues() {
 		try {
 			Log.v("Home", "entering getVenues()");
 			/* uses up limited actual Yelp queries 
 			Yelp yelp = getYelp();
 			ArrayList<Category> cats = new ArrayList<Category>();
 			cats.add(new Category("cafe"));
 			cats.add(new Category("dessert"));
 			cats.add(new Category("coffee"));
 			// throws NullPointerException due to myLocation
 			//RecommendationInput input = new RecommendationInput(cats, myLocation.getLatitude(), myLocation.getLongitude());
 			RecommendationInput input = new RecommendationInput(cats, 42.283, -71.23, 5000);
 			ArrayList<YelpVenue> venues = yelp.getRecommendation(input);
 			my_venues = new ArrayList<Venue>();			
 			for (int i = 0; i < venues.size(); i++) {
 				YelpVenue yven = venues.get(i);
 				int lat = (int)(yven.getLatitude() * 1E6);
 				int lon = (int)(yven.getLongitude() * 1E6);
 				GeoPoint gp = new GeoPoint(lat, lon);
 				Venue ven = new Venue(yven.getName(), yven.getURL(), yven.getImageURL(), gp, yven.getDistance());
 				ven.setRating(yven.getRating());
 				my_venues.add(ven);
 			} */
 			
 			
 			/* for debugging, just use made up venues */
 			my_venues = new ArrayList<Venue>();
 			String name = "Lotus Spa";
 			String url = "http://www.lotusspaeauclaire.com/";
 			String img_url = "http://www.teachenglishinasia.net/files/u2/purple_lotus_flower.jpg";
 			int lat = 42283000;
 			int lon = -71230000;
 			GeoPoint gp = new GeoPoint(lat, lon);
 			int distance = 2000;
 			int rating = 4;
 			for (int i = 0; i < 3; i++) {
 				Venue ven = new Venue(name, url, img_url, gp, distance);
 				ven.setRating(rating);
 				my_venues.add(ven);
 			}
 			Thread.sleep(2000);
 			
 
 		} catch (Exception e) {
 			Log.e("getVenues()", e.toString());
 		}
 		runOnUiThread(returnRes);
 	}
 	
 	/* like everything in Java, you need to make a Yelp object in order to actually do anything
 	 * (actually there's a reason for this:  it authorizes you with the Yelp API)
 	 */
     public Yelp getYelp() {
     	Log.v("Yelp", "entering getYelp()");
         String consumerKey = getString( R.string.oauth_consumer_key );
         String consumerSecret = getString( R.string.oauth_consumer_secret);
         String token = getString(R.string.oauth_token);
         String tokenSecret = getString(R.string.oauth_token_secret);
         
         Yelp yelp = new Yelp(consumerKey, consumerSecret, token, tokenSecret);
         
         return yelp;
     }
     
     public class VenueAdapter extends ArrayAdapter<Venue> {
 
     	private Context context;
     	private ArrayList<Venue> items;
 
     	public VenueAdapter(Context context, int textViewResourceId, ArrayList<Venue> items) {
     		super(context, textViewResourceId, items);
     		this.items = items;
     		this.context = context;
     	}
 
     	@Override
     	public View getView(int position, View convertView, ViewGroup parent) {
     		View v = convertView;
     		if (v == null) {
     			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     			v = vi.inflate(R.layout.row, null);
     		}
     		Venue o = items.get(position);
     		if (o != null) {
     			TextView tt = (TextView) v.findViewById(R.id.toptext);
     			TextView bt = (TextView) v.findViewById(R.id.bottomtext);
     			ImageView iv = (ImageView) v.findViewById(R.id.icon);
     			if (tt != null) {
     				tt.setText(o.getName());
     			}
     			if (bt != null) {
     				bt.setText(Double.toString(o.getRating()));
     			}
     			if (iv != null) {
     	    		Drawable image = ImageOperations(context, items.get(position).getImageURL(), "item" + Integer.toString(position) + ".jpg");
     	    		if (image == null) {
     	    			/* supposed to display this when the image can't be gotten from the url
     	    			 * but instead, no image displays, which is ok but doesn't look so good
     	    			 * probably returning null because it's an incorrect path name
     	    			 */
     	    			image = Drawable.createFromPath("../../../../../res/drawable/default_venue_image.png");
     	    		}
     				iv.setImageDrawable(image);
     			}
     		}
     		return v;
     	}
     	
     	public ArrayList<Venue> getItems() { return items; }
         
         /* http://asantoso.wordpress.com/2008/03/07/download-and-view-image-from-the-web/ 
          * returns a Drawable from a URL for an image
          * an ImageView can set itself to display this Drawable as its image
          * example usage:
          * ArrayList<YelpVenue> venues; //already got them using yelp.getRecommendation(), see getVenues() method in Home
          * Drawable image = ImageOperations(this,venues.get(0).rating_img_url_small,"image.jpg");
            ImageView imgView = (ImageView)findViewById(R.id.image1);
            imgView.setImageDrawable(image);
          */
     	private Drawable ImageOperations(Context ctx, String url, String saveFilename) {
     		try {
     			InputStream is = (InputStream) this.fetch(url);
     			Drawable d = Drawable.createFromStream(is, saveFilename);
     			return d;
     		} catch (MalformedURLException e) {
     			e.printStackTrace();
     			return null;
     		} catch (IOException e) {
     			e.printStackTrace();
     			return null;
     		}
     	}
 
         /* http://asantoso.wordpress.com/2008/03/07/download-and-view-image-from-the-web/
          * used by ImageOperations to get an image from a URL
          */
     	public Object fetch(String address) throws MalformedURLException,IOException {
     		URL url = new URL(address);
     		Object content = url.getContent();
     		return content;
     	}
     }
 }
 
