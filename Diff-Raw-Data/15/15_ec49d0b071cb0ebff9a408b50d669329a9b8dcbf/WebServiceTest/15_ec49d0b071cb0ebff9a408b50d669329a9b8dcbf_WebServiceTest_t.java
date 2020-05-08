 package com.online.test;
 
import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.test.AndroidTestCase;
 import android.util.Log;
 
 import com.online.WebService;
 
 public class WebServiceTest extends AndroidTestCase {
 	private final static String LOG_TAG = "WebServiceTest";
 	WebService ws;
 
 	private void setSession() {
 		try {
 			WebService wss = new WebService();
 			wss.execute("auth.getVoidSession");
 			String sessionKey = wss.get().getString("session_key");
 			
 			ws.setQueryParam("session_key", sessionKey);
 		} catch (Exception e) {
 		}
 	}
 
 	private void assertStatusOK() {
 		try {
 			JSONObject jsonObject = ws.get();
 			assertEquals("ok", jsonObject.getJSONObject("@attributes").get("status"));
 		} catch (Exception e) {
 			assertTrue(e.getMessage(), false);
 		}
 	}
 
 	protected void setUp() {
 		ws = new WebService();
 	}
 
 	public void test_auth_getVoidSession() {
 		ws.execute("auth.getVoidSession");
 		assertStatusOK();
 	}
 
 	public void test_auth_getLatestCallId() {
 		setSession();
 		ws.execute("auth.getLatestCallId");
 		assertStatusOK();
 	}
 
 	public void test_server_getDayAndTime() {
 		setSession();
 		ws.setQueryParam("country_code", "SE");
 		ws.execute("server.getDayAndTime");
 		assertStatusOK();
 	}
 
 	public void test_library_getZipcodeByCoordinates() {
 		setSession();
 		ws.setQueryParam("latitude", "59.339343");
 		ws.setQueryParam("longitude", "18.009206");
 		ws.execute("library.getZipcodeByCoordinates");
 		assertStatusOK();
 		try {
 			JSONObject jsonObject = ws.get();
 			assertEquals("11251", jsonObject.getString("zipcode"));
 			assertEquals("SE", jsonObject.getString("country_code"));
 			
 		} catch (Exception e) {
 			assertTrue(e.getMessage(), false);
 		}
 	}
 
 	public void test_library_getStreets() {
 		setSession();
 		ws.setQueryParam("country_code", "SE");
 		ws.setQueryParam("search_string", "Sad");
 		ws.setQueryParam("limit", "1");
 		ws.execute("library.getStreets");
 		assertStatusOK();
 		try {
 			JSONObject jsonObject = ws.get();
 			assertEquals("Sadkowska", jsonObject.getJSONArray("streets").getJSONObject(0).getString("name"));
 			
 		} catch (Exception e) {
 			assertTrue(e.getMessage(), false);
 		}
 	}
 	
 	public void test_library_getRestaurantsByCountryCodeAndZipcode() {
 		setSession();
 		ws.setQueryParam("country_code", "SE");
 		ws.setQueryParam("zipcode", "12345");
 		ws.execute("library.getRestaurantsByCountryCodeAndZipcode");
 		assertStatusOK();
 		try {
 			JSONObject jsonObject = ws.get();
			//Log.i(LOG_TAG, jsonObject.toString(2));
			Object resObj = jsonObject.getJSONObject("restaurants").get("restaurant") ;
			JSONObject res = (resObj instanceof JSONObject ? (JSONObject)resObj : ((JSONArray)resObj).getJSONObject(0));
			
			String name = res.getJSONObject("@attributes").getString("name");
 			Log.i(LOG_TAG, name);
 			assertEquals("Demorestaurang", name);
 			
 		} catch (Exception e) {
 			assertTrue(e.getMessage(), false);
 		}
 		
 	}
 	
 	public void test_library_getRestaurantsByCountryCodeAndCity() {
 		setSession();
 		ws.setQueryParam("country_code", "SE");
 		ws.setQueryParam("city", "Teststad");
 		ws.execute("library.getRestaurantsByCountryCodeAndCity");
 		assertStatusOK();
 		try {
 			JSONObject jsonObject = ws.get();
 			//Log.i(LOG_TAG, jsonObject.toString(2));
			Object resObj = jsonObject.getJSONObject("restaurants").get("restaurant") ;
			JSONObject res = (resObj instanceof JSONObject ? (JSONObject)resObj : ((JSONArray)resObj).getJSONObject(0));
			
			String name = res.getJSONObject("@attributes").getString("name");
 			Log.i(LOG_TAG, name);
 			assertEquals("Demorestaurang", name);
 			
 		} catch (Exception e) {
 			assertTrue(e.getMessage(), false);
 		}
 		
 	}
 }
