 package com.mpl.altimeter;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.trevorpage.tpsvg.SVGParserRenderer;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 @SuppressWarnings("unused")
 public class MainActivity extends Activity {
 	private static final String TAG = "MainActivity";
	private static final double METRIC_TO_IMP = 0.9144;
 	TextView		_altitude;
 	TextView		_latitude;
 	TextView		_longitude;
 	BackgroundView	_background;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		_altitude = (TextView) findViewById(R.id.textview_altitude);
 		_latitude = (TextView) findViewById(R.id.textview_latitude);
 		_longitude = (TextView) findViewById(R.id.textview_longitude);
 		_background = (BackgroundView) findViewById(R.id.background);
 		SVGParserRenderer image = new SVGParserRenderer(this, R.raw.background);
 		_background.setSVGRenderer(image, null);
 		_background.setBackgroundColor(0xffffffff);
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		requestLocation();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			startSettings();
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 	private void startSettings() {
 		Intent i = new Intent();
 		i.setClass(MainActivity.this, SettingsActivity.class);
 		startActivity(i);
 	}
 
 	private class AltitudeRequester extends AsyncTask<Location, Void, Double> {
 		@Override
 		protected Double doInBackground(Location... arg) {
 			return getAltitude(arg[0].getLongitude(), arg[0].getLatitude());
 		}
 
 		@Override
 		protected void onPostExecute(Double result) {
 			if (!result.equals(Double.NaN)) {
 				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 				int stringId = R.plurals.altitude_meters;
 				if (!prefs.getBoolean("metric", true)) {
 					result = result * METRIC_TO_IMP;
 					stringId = R.plurals.altitude_yards;
 				}
 				_altitude.setText(getResources().getQuantityString(stringId, result.intValue(), result));
 				_background.setAltitude(result * 9000 / (double)Integer.valueOf("0" + prefs.getString("max_alt", "9000")));
 			} else {
 				_altitude.setText(R.string.no_internet_connection);
 			}
 		}
 	}
 
 	private void requestLocation() {
 		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 			_altitude.setText(R.string.gps_disabled);
 		}
 		LocationListener locationListener = new LocationListener() {
 			public void onLocationChanged(Location location) {
 				AltitudeRequester task = new AltitudeRequester();
 				task.execute(location);
 				Resources res = getResources();
 				_latitude.setText(String.format(res.getString(R.string.latitude), location.getLatitude()));
 				_longitude.setText(String.format(res.getString(R.string.longitude), location.getLongitude()));
 			}
 
 			public void onStatusChanged(String provider, int status, Bundle extras) {}
 
 			public void onProviderEnabled(String provider) {}
 
 			public void onProviderDisabled(String provider) {}
 		};
 
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 60 * 1, 0, locationListener);
 	}
 
 	private Double getAltitude(Double longitude, Double latitude) {
 		Double result = Double.NaN;
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpContext localContext = new BasicHttpContext();
 		String url = "https://maps.googleapis.com/maps/api/elevation/json?locations="
 				+ latitude.toString()
 				+ ","
 				+ longitude.toString()
 				+ "&sensor=true";
 		HttpGet httpGet = new HttpGet(url);
 		try {
 			HttpResponse response = httpClient.execute(httpGet, localContext);
 			HttpEntity entity = response.getEntity();
 			if (entity != null) {
 				InputStream instream = entity.getContent();
 				int r = -1;
 				StringBuffer respStr = new StringBuffer();
 				while ((r = instream.read()) != -1)
 					respStr.append((char) r);
 				instream.close();
 
 				try {
 					JSONObject json = new JSONObject(respStr.toString());
 					result = json.getJSONArray("results").getJSONObject(0).getDouble("elevation");
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 
 			}
 		} catch (ClientProtocolException e) {
 			//e.printStackTrace();
 		}
 		catch (IOException e) {
 			//e.printStackTrace();
 		}
 		return result;
 	}
 
 }
