 package com.finlay.geomonsters;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 
 import org.apache.http.*;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.location.Location;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class WeatherManager extends AsyncTask<Object, Void, Weather>{
 
 	// www.survivingwithandroid.com/2013/05/build-weather-app-json-http-android.html
 
 	private static final String TAG = "WeatherManager";
 
 	// current example: http://api.openweathermap.org/data/2.5/weather?lat=53.5&long=-113.50
 	private static final String currentURL = "http://api.openweathermap.org/data/2.5/weather?";
 	// past example: http://api.openweathermap.org/data/2.5/history/city?id=5946768&type=hour&start=1369728000&cnt=1
 	private static final String pastURL = "http://api.openweathermap.org/data/2.5/history/city?";
 
 	private MainActivity _parent;
 	public WeatherManager(MainActivity parent) {
 		_parent = parent;
 	}
 
 	// Cannot be called from main thread.
 	private String getWeatherData(String address) {		
 		Log.v(TAG, "getWeatherData: " + address);
 		HttpURLConnection con = null;
 		InputStream is = null;
 
 		try {
 			con = (HttpURLConnection) ( new java.net.URL(address)).openConnection();
 			con.setRequestMethod("GET");
 			con.setDoInput(true);
 			con.setDoOutput(true);
 			con.connect();
 
 			Log.v(TAG, "Open Weather connected.");
 			_parent.appendMessage("Open Weather connected.");
 
 			// Let's read the response
 			StringBuffer buffer = new StringBuffer();
 			is = con.getInputStream();
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			String line = null;
 			while ( (line = br.readLine()) != null )
 				buffer.append(line + "\r\n");
 
 			is.close();
 			con.disconnect();
 			return buffer.toString();
 
 		} catch (Throwable t) {
 			Log.e(TAG, t.getMessage());
 		} finally {
 			try { is.close(); } catch (Throwable t) {}
 			try { con.disconnect(); } catch (Throwable t) {}
 		}
 
 		return null;
 	}
 
 	@Override
 	protected Weather doInBackground(Object... params) {
 		// getWeatherData cannot be called from main thread, so we have this function running 
 		// on a separate thread.
 		// Parameters are expected to be either: 
 		// Location, (boolean:true)			OR			Location, (boolean:false), Double (fromTime)
 		// For more info on using old data, check out : bugs.openweathermap.org/projects/api/wiki/Api_2_5_history
 
 		String longitude = (String) params[0];
 		String latitude = (String) params[1];
 		Weather weather = null;
 
 		// query weather data from openweather page
 		// TODO: Replace 'StationID' with proper method. Need to run query to find closest location first.
 		String address;
 		//if ((Boolean) params[2]) 	
 		address = currentURL + "lat=" + latitude + "&lon=" + longitude;
 		//else 						address = pastURL + "id=" + "<stationid>" + "&type=hour&start=" + (Double)params[3] + "&cnt=1";		
 
 		String data = getWeatherData(address);
 		Log.v(TAG, "Data returned from " + address);
 		Log.v(TAG, "Weather data: " + data);
 
 		try {
 
 			// Create JSON object from the returned data
 			JSONObject jObj = new JSONObject(data);
 
 			// Extract the info
 			JSONObject coordObj = jObj.getJSONObject("coord");
 
 			// JSON objects -- TODO: Might want more than just first item in weather array
 			JSONObject sysObj = jObj.getJSONObject("sys");
 			JSONArray wArray = jObj.getJSONArray("weather");
 			JSONObject main = jObj.getJSONObject("main");
 			JSONObject JSONWeather = wArray.getJSONObject(0);
 			JSONObject wObj = jObj.getJSONObject("wind");
 			JSONObject cObj = jObj.getJSONObject("clouds");
 
 			// Weather object
 			weather = new Weather();
 			weather.weatherID = JSONWeather.getInt("id");
 			weather.temp = main.getDouble("temp");
 			weather.sunrise = sysObj.getDouble("sunrise");
 			weather.sunset = sysObj.getDouble("sunset");
 			weather.wind_speed = wObj.getDouble("speed");
 			weather.cloud_cover = cObj.getInt("all");			
 
 		} catch (JSONException e) {
 			Log.e(TAG, e.getMessage());
 		}
 
 		Log.v(TAG, "Send weather data.");
 		_parent.setWeatherData(weather);
 		return weather;
 	}
 
 }
