 package com.hillbomber;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.view.ViewPager.LayoutParams;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.Toast;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.AsyncFacebookRunner.RequestListener;
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class MainActivity extends MapActivity {
 
 	private SharedPreferences userPreferences;
 
 	private LocationManager locationManager;
 	private LocationListener locationListener;
 
 	private String facebookId, facebookName;
 	private String currentRoute, currentDifficulty;
 	private Location startLocation, endLocation;
 
 	private MapView mapView;
 	private View routeView;
 	private Button refreshButton, newButton, startButton, endButton;
 	private EditText titleText;
 	private RadioGroup difficultyRadios;
 
 	private MapController mapController;
 	private GoogleParser googleParser;
 
 	private Facebook facebook = new Facebook("468347859863144");
 	private AsyncFacebookRunner asyncFacebookRunner = new AsyncFacebookRunner(
 			facebook);
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		refreshButton = (Button) findViewById(R.id.refresh);
 		newButton = (Button) findViewById(R.id.newRoute);
 		endButton = (Button) findViewById(R.id.endRoute);
 
 		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		routeView = inflater.inflate(R.layout.route, null);
 		routeView.setVisibility(View.GONE);
 		startButton = (Button) routeView.findViewById(R.id.start);
 		startButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				onStartRouteClicked(v);
 			}
 		});
 		titleText = (EditText) routeView.findViewById(R.id.title);
 		difficultyRadios = (RadioGroup) routeView.findViewById(R.id.difficulty);
 		addContentView(routeView, new LayoutParams());
 
 		mapView = (MapView) findViewById(R.id.mapview);
 		userPreferences = getPreferences(MODE_PRIVATE);
 		String accessToken = userPreferences.getString("access_token", null);
 		long expires = userPreferences.getLong("access_expires", 0);
 		facebookId = userPreferences.getString("facebook_id", null);
 		facebookName = userPreferences.getString("facebook_name", null);
 		if (accessToken != null && expires != 0) {
 			facebook.setAccessToken(accessToken);
 			facebook.setAccessExpires(expires);
 		}
 
 		if (!facebook.isSessionValid()) {
 			facebook.authorize(this, new DialogListener() {
 				public void onComplete(Bundle values) {
 					SharedPreferences.Editor editor = userPreferences.edit();
 					editor.putString("access_token", facebook.getAccessToken());
 					editor.putLong("access_expires",
 							facebook.getAccessExpires());
 					editor.commit();
 
 					asyncFacebookRunner.request("me", new RequestListener() {
 
 						@Override
 						public void onMalformedURLException(
 								MalformedURLException e, Object state) {
 							// Log.e("Facebook Error", e.toString());
 						}
 
 						@Override
 						public void onIOException(IOException e, Object state) {
 							// Log.e("Facebook Error", e.toString());
 						}
 
 						@Override
 						public void onFileNotFoundException(
 								FileNotFoundException e, Object state) {
 							// Log.e("Facebook Error", e.toString());
 						}
 
 						@Override
 						public void onFacebookError(FacebookError e,
 								Object state) {
 							// Log.e("Facebook Error", e.toString());
 						}
 
 						@Override
 						public void onComplete(String response, Object state) {
 							Log.i("Facebook Me", response);
 							try {
 								JSONObject me = (JSONObject) new JSONTokener(
 										response).nextValue();
 								facebookId = me.getString("id");
 								facebookName = me.getString("name");
 
 								List<NameValuePair> params = new ArrayList<NameValuePair>();
 								params.add(new BasicNameValuePair("uid",
 										facebookId));
 								params.add(new BasicNameValuePair("name",
 										facebookName));
 								postConnection(
 										"http://hillbomber.herokuapp.com/mobile_user.json",
 										params);
 
 								SharedPreferences.Editor editor = userPreferences
 										.edit();
 								editor.putString("facebook_id", facebookId);
 								editor.putString("facebook_name", facebookName);
 								editor.commit();
 							} catch (JSONException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 						}
 					});
 				}
 
 				public void onFacebookError(FacebookError e) {
 					// Log.e("Facebook Error", e.toString());
 				}
 
 				public void onError(DialogError e) {
 					// Log.e("Facebook Error", e.toString());
 				}
 
 				public void onCancel() {
 				}
 			});
 		}
 
 		googleParser = new GoogleParser();
 
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapController = mapView.getController();
 		mapController.setZoom(18); // Fixed Zoom Level
 
 		onRefreshClicked(null);
 
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		locationListener = new LongboardLocationListener();
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
 				0, locationListener);
 
 		// Get the current location in start-up
 		centerLocation(new GeoPoint((int) (locationManager
 				.getLastKnownLocation(LocationManager.GPS_PROVIDER)
 				.getLatitude() * 1E6), (int) (locationManager
 				.getLastKnownLocation(LocationManager.GPS_PROVIDER)
 				.getLongitude() * 1E6)));
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		facebook.extendAccessTokenIfNeeded(this, null);
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		facebook.authorizeCallback(requestCode, resultCode, data);
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (routeView.getVisibility() == View.VISIBLE) {
 			routeView.setVisibility(View.GONE);
 		} else {
 			super.onBackPressed();
 		}
 	}
 
 	public void onNewRouteClicked(View v) {
 		routeView.setVisibility(View.VISIBLE);
 		titleText.setText("");
 		difficultyRadios.clearCheck();
 	}
 
 	public void onStartRouteClicked(View v) {
 		if (!titleText.getText().toString().equals("")) {
 			currentRoute = titleText.getText().toString();
 			currentDifficulty = ((RadioButton) difficultyRadios
 					.findViewById(difficultyRadios.getCheckedRadioButtonId()))
 					.getText().toString();
 			startLocation = locationManager
 					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 
 			refreshButton.setVisibility(View.GONE);
 			newButton.setVisibility(View.GONE);
 			endButton.setVisibility(View.VISIBLE);
 			routeView.setVisibility(View.GONE);
 		} else {
 			Toast toast = Toast.makeText(getApplicationContext(),
 					"Please title your new trail", Toast.LENGTH_SHORT);
 			toast.show();
 		}
 	}
 
 	public void onEndRouteClicked(View v) {
 		endLocation = locationManager
 				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		newButton.setVisibility(View.VISIBLE);
 		endButton.setVisibility(View.GONE);
 		refreshButton.setVisibility(View.VISIBLE);
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("uid", facebookId));
 		params.add(new BasicNameValuePair("name", facebookName));
 		params.add(new BasicNameValuePair("title", currentRoute));
 		params.add(new BasicNameValuePair("s_lat", Double
 				.toString(startLocation.getLatitude())));
 		params.add(new BasicNameValuePair("s_long", Double
 				.toString(startLocation.getLongitude())));
 		params.add(new BasicNameValuePair("e_lat", Double.toString(endLocation
 				.getLatitude())));
 		params.add(new BasicNameValuePair("e_long", Double.toString(endLocation
 				.getLongitude())));
 		params.add(new BasicNameValuePair("difficulty", Integer
 				.toString(difficultyToInteger(currentDifficulty))));
 
 		postConnection("http://hillbomber.herokuapp.com/trails.json", params);
 
 		Toast toast = Toast.makeText(getApplicationContext(), "Trail added!",
 				Toast.LENGTH_SHORT);
 		toast.show();
 	}
 
 	public void onRefreshClicked(View v) {
 		BufferedReader in = null;
 		String line = null;
 		try {
 			in = new BufferedReader(
 					new InputStreamReader(
 							getConnection("http://hillbomber.herokuapp.com/trails.json")));
 			List<Overlay> overlays = mapView.getOverlays();
 			overlays.clear();
 			while ((line = in.readLine()) != null) {
 				JSONArray routes = (JSONArray) new JSONTokener(line)
 						.nextValue();
 				for (int i = 0; i < routes.length(); i++) {
 					JSONObject route = routes.getJSONObject(i);
 					GeoPoint startPoint = new GeoPoint(
 							(int) (route.getDouble("s_lat") * 1E6),
 							(int) (route.getDouble("s_long") * 1E6));
 					GeoPoint endPoint = new GeoPoint(
 							(int) (route.getDouble("e_lat") * 1E6),
 							(int) (route.getDouble("e_long") * 1E6));
 					String url = googleParser.directions(startPoint, endPoint);
					PinItemizedOverlay pinItemizedOverlay = new PinItemizedOverlay(route.getInt("difficulty"), getApplicationContext());
 					pinItemizedOverlay.addOverlay(new OverlayItem(startPoint, route.getString("title"), "by " + route.getString("creator")));
 					overlays.add(pinItemizedOverlay);
 					RouteOverlay routeOverlay = new RouteOverlay(
 							googleParser.parse(getConnection(url)), Color.BLUE);
 					overlays.add(routeOverlay);
 				}
 			}
 			mapView.invalidate();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private InputStream getConnection(String url) {
 		InputStream is = null;
 		try {
 			URLConnection conn = new URL(url).openConnection();
 			is = conn.getInputStream();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return is;
 	}
 
 	public void postConnection(String url, List<NameValuePair> params) {
 		// Create a new HttpClient and Post Header
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpPost httppost = new HttpPost(url);
 
 		try {
 			// Add your data
 			httppost.setEntity(new UrlEncodedFormEntity(params));
 
 			// Execute HTTP Post Request
 			// HttpResponse response = httpclient.execute(httppost);
 			httpclient.execute(httppost);
 
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 		}
 	}
 
 	private class LongboardLocationListener implements LocationListener {
 
 		public void onLocationChanged(Location argLocation) {
 			// TODO Auto-generated method stub
 			GeoPoint geoPoint = new GeoPoint(
 					(int) (argLocation.getLatitude() * 1E6),
 					(int) (argLocation.getLongitude() * 1E6));
 
 			centerLocation(geoPoint);
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
 	}
 
 	private void centerLocation(GeoPoint centerGeoPoint) {
 		mapController.animateTo(centerGeoPoint);
 	}
 	
 	private static int difficultyToInteger(String id) {
 		id = id.toLowerCase();
 		if (id.contains("green")) {
 			return 0;
 		} else if (id.contains("blue")) {
 			return 1;
 		} else if (id.contains("double")) {
 			return 2;
 		} else if (id.contains("black")) {
 			return 3;
 		} else {
 			return -1;
 		}
 
 	}
 
 }
