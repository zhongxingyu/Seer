 package com.example.airportstatus;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 public class FlightStatsClient {
 	//curl -v  -X GET "https://api.flightstats.com/flex/weather/rest/v1/json/metar/ABQ?appId=41a9abe4&appKey=de9124c085c33b9b167b1b02e7c42ad2&codeType=IATA"
 	private static final String appId = "41a9abe4";
 	private static final String appKey = "de9124c085c33b9b167b1b02e7c42ad2";
 	private static final String BASE_URL = "https://api.flightstats.com/flex/";
 	private static final String DELAYS_URL = "delayindex/rest/v1/json/airports/";
 	private static final String WEATHER_URL = "weather/rest/v1/json/metar/"; // Use METAR type of weather request
 	private static AsyncHttpClient client = new AsyncHttpClient();
 	
 	
 	public static void getWeatherConditions(String code, AsyncHttpResponseHandler handler) {
 		client.get(getApiUrl(WEATHER_URL, code), makeParams(), handler);
 	}
 	
 	public static void getDelayDegree(String code, AsyncHttpResponseHandler handler) {
 		RequestParams delayRequestParams = makeParams();
 		delayRequestParams.put("classification", String.valueOf(5)); // Include airports of any size (1-5, 1=largest only)
 		delayRequestParams.put("score", String.valueOf(0)); // Include delays of any severity
 		
 		// FlightStats API phrases delays in severity, not time.
 		// The API offers a raw score that is a percentage of flights that are delayed, and
 		// a weighted score that can be interpreted.
 		client.get(getApiUrl(DELAYS_URL, code), delayRequestParams, handler);
 	}
 	
 	public static String getWeatherString(JSONObject response) throws Exception {
 		try {
 			JSONArray tags = response.getJSONObject("metar").getJSONArray("tags");
 			String conditionsTag = "Prevailing Conditions";
 			for (int i = 0; i < tags.length(); i++) {
 				JSONObject t = tags.getJSONObject(i);
 				if (t.has("key") && t.getString("key").equals(conditionsTag)) {
 					return t.getString("value");
 				}
 			}
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			Log.e("WEATHER", "Key not found");
 			Log.e("WEATHER", e.getMessage());
 		}
 		
 		// No result was found, so raise an exception
 		throw new Exception("No weather value found");
 	}
 	
 	public static int getDelayIndex(JSONObject response) throws Exception {
 		try {
 			JSONObject delayData = response.getJSONArray("delayIndexes").getJSONObject(0);
 			return (int) Math.round(delayData.getDouble("normalizedScore"));
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			Log.e("DELAYS", "Key not found");
 			Log.e("DELAYS", e.getMessage());
 		}
 		
 		// No result was found, so raise an exception
 		throw new Exception("No delay data received");
 	}
 	
 	private static RequestParams makeParams() {
 		RequestParams params = new RequestParams();
 		params.put("appId", appId);
 		params.put("appKey", appKey);
 		params.put("codeType", "IATA");
 		return params;
 	}
 	
 	protected static String getApiUrl(String url, String code) {
 		return BASE_URL + url + code;
 	}
 
 }
