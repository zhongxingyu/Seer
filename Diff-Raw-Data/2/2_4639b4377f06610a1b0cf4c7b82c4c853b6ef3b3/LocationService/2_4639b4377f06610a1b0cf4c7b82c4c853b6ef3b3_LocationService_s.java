 package com.itsmap.kn10731.themeproject.simpledmiapp;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.IBinder;
 import android.util.Log;
 
 public class LocationService extends Service {
 
 	private static final String TAG = "LocationService";
 	private LocationManager locationManager;
 	private String provider;
 	private String lat;
 	private String lng;
 	private String by;
 	private String postnr;
 	private String region;
 
 	private Runnable locationTask = new Runnable() {
 		public void run() {
 			// Get the location manager
 			locationManager = (LocationManager) getBaseContext()
 					.getSystemService(Context.LOCATION_SERVICE);
 			// Define the criteria how to select the location provider -> use
 			// default
 			Criteria criteria = new Criteria();
 
 			provider = locationManager.getBestProvider(criteria, false);
 			if (provider != null) {
 				Location location = locationManager
 						.getLastKnownLocation(provider);
 
 				if (location != null) {
 					System.out.println("Provider " + provider
 							+ " has been selected.");
 					onLocationChanged(location);
 					Log.d(TAG, "Lat: " + lat + " Lng: " + lng);
 					parsePostnumre(getGeoData(lat, lng, "postnumre"));
 					parseRegion(getGeoData(lat, lng, "politikredse"));
 				} else {
 					Log.d(TAG, "Failure");
 
 				}
 			} else {
 				Log.d(TAG, "Provider is null");
 			}
 		}
 
 		public void onLocationChanged(Location location) {
 			String lat = String.valueOf(location.getLatitude());
 			String lng = String.valueOf(location.getLongitude());
 			Log.d(TAG, "Lat: " + lat + ". Lng: " + lng);
 		}
 
 		public JSONObject getGeoData(String lat, String lng, String type) {
 			// Takes types "postnumre" or "politikredse"
 			DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
			HttpPost httppost = new HttpPost("http://geo.oiorest.dk/" + type + "/" + lat + "," + lng);
 			// Depends on your web service
 			httppost.setHeader("Content-type", "application/json");
 
 			InputStream inputStream = null;
 			String result = null;
 			HttpResponse response = null;
 			try {
 				response = httpclient.execute(httppost);
 			} catch (ClientProtocolException e) {
 			} catch (IOException e) {
 			}
 			HttpEntity entity = response.getEntity();
 
 			try {
 				inputStream = entity.getContent();
 			} catch (IllegalStateException e) {
 			} catch (IOException e) {
 			}
 			// json is UTF-8 by default i beleive
 			BufferedReader reader = null;
 			try {
 				reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
 			} catch (UnsupportedEncodingException e) {
 			}
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 			try {
 				while ((line = reader.readLine()) != null) {
 					sb.append(line + "\n");
 				}
 			} catch (IOException e) {
 			}
 			result = sb.toString();
 
 			JSONObject jObject = null;
 			try {
 				jObject = new JSONObject(result);
 			} catch (JSONException e) {
 			}
 			
 			return jObject;
 		}
 		
 		public void parsePostnumre(JSONObject jObject) {
 			try {
 				by = jObject.getString("navn");
 				postnr = jObject.getString("fra");
 			} catch (JSONException e) {
 			}
 			
 		}
 		
 		public void parseRegion(JSONObject jObject) {
 			int index = 0;
 			try {
 				index = Integer.parseInt(jObject.getString("nr"));
 			} catch (JSONException e) {
 			}
 			
 			switch(index) {
 			case 1:
 				region = getString(R.string.nordj);
 				break;
 			case 10:
 				region = getString(R.string.kbh);
 				break;
 			case 11:
 				region = getString(R.string.kbh);
 				break;
 			case 12:
 				region = getString(R.string.born);
 				break;
 			case 2:
 				region = getString(R.string.ostj);
 				break;
 			case 3:
 				region = getString(R.string.midtj);
 				break;
 			case 4:
 				region = getString(R.string.sydj);
 				break;
 			case 5:
 				region = getString(R.string.sydj);
 				break;
 			case 6:
 				region = getString(R.string.fyn);
 				break;
 			case 7:
 				region = getString(R.string.vestsj);
 				break;
 			case 8:
 				region = getString(R.string.midtj);
 				break;
 			case 9:
 				region = getString(R.string.kbh);
 				break;
 			default:
 				Log.d(TAG, "Errornous region number");
 			}
 			
 			
 		}
 	};
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		getLocation();
 		Log.d(TAG, "onCreate");
 	}
 
 	private void getLocation() {
 		Thread backgroundThread = new Thread(locationTask) {
 			@Override
 			public void run() {
 				try {
 					locationTask.run();
 				} finally {
 				}
 			}
 		};
 		backgroundThread.start();
 	}
 
 }
