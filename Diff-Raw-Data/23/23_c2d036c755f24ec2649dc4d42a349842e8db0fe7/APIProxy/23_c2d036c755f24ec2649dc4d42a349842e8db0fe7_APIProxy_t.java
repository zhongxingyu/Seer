 package com.appirio.mobile.aau.nativemap;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import org.apache.cordova.DroidGap;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 
 import com.salesforce.androidsdk.app.ForceApp;
 import com.salesforce.androidsdk.rest.ClientManager;
 import com.salesforce.androidsdk.rest.ClientManager.AccountInfoNotFoundException;
 import com.salesforce.androidsdk.rest.RestClient;
 
 public class APIProxy {
 	
 	private DroidGap ctx;
 	private static String authToken = null;
 	private static String instanceUrl = null;
 	private static String clientId = null;
 	private static String refreshToken = null;
 	private static String teletracQueryString = null;
 	private static JSONObject settings;
 
 	private static final String SETTINGS_ENDPOINT = "/services/apexrest/teletracInfo/";
 	private static JSONObject cacheObject;
 	
 	private static JSONObject loadCacheObject(Context ctx, boolean reload) {
 		if(cacheObject != null && !reload) {
 			return cacheObject;
 		}
 		
 		File cacheFile = new File(ctx.getFilesDir(), "cacheFile.txt");
 		
 		try {
 			if(cacheFile.exists()) {
 				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile)));
 				
 				String line = null;
 				StringBuilder cacheData = new StringBuilder();
 				
 				while((line = reader.readLine()) != null) {
 					cacheData.append(line);
 				}
 				
 				cacheObject = new JSONObject(cacheData.toString());
 			}
 		} catch (Exception e) {
 			// Cache issue, can be safely ignored
 			
 			e.printStackTrace();
 		}
 		
 		if(cacheObject == null) {
 			cacheObject = new JSONObject();
 		}
 		
 		return cacheObject;
 	}
 	
 	private static void saveCacheObject(Context ctx) {
 		try {
 			if(cacheObject != null) {
 				File cacheFile = new File(ctx.getFilesDir(), "cacheFile.txt");
 
 				if(cacheFile.exists()) {
 					cacheFile.delete();
 				}
 				
 				OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(cacheFile));
 				
 				out.write(cacheObject.toString());
 				
 				out.close();
 			}
 		} catch (Exception e) {
 			// Cache issue, can be safely ignored
 			
 			e.printStackTrace();
 		}
 	}
 	
 	private static void cachePut(String key, String value, Context ctx) {
 		try {
 			loadCacheObject(ctx, false);
 			
 			JSONObject cacheItem = new JSONObject();
 			
 			cacheItem.put("storeDate", System.currentTimeMillis());
 			cacheItem.put("data", value);
 			
 			cacheObject.put(key, cacheItem.toString());
 			
 			saveCacheObject(ctx);
 		} catch (JSONException e) {
 			// Cache problem, can be safely ignored
 			
 			e.printStackTrace();
 		}
 	}
 	
 	private static String cacheGet(String key, int expirationInDays, Context ctx) {
 		try {
 			loadCacheObject(ctx, false);
 			
 			if(cacheObject.has(key)) {
 				JSONObject cacheItem = new JSONObject(cacheObject.get(key).toString());
 				
 				if(cacheItem.getLong("storeDate") + (expirationInDays * 24 * 60 * 60 * 10000) > System.currentTimeMillis()) {
 					return cacheItem.getString("data"); 
 				}
 			}
 		} catch (JSONException e) {
 			// Cache issue, can be safely ignored
 			
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 	
 	private String getTeletracQueryString() throws AMException {
 		try {
 			ensureToken();
 
 			if(teletracQueryString == null) {
 				teletracQueryString = String.format(
 						"?strAccountId=%s&strUserName=%s&strPwd=%s",
 						settings.getString("Account"),
 						settings.getString("Username"),
 						settings.getString("Password"));
 			}
 			
 			return teletracQueryString;
 		} catch (Exception e) {
 			e.printStackTrace();
 			
 			throw new AMException(e);
 		}
 	}
 	
 	public APIProxy(DroidGap ctx) {
 		this.ctx = ctx;
 	}
 	
 	private void ensureToken() throws AccountInfoNotFoundException, AMException, JSONException {
		ClientManager mgr = new ClientManager(this.ctx.getContext(),
				ForceApp.APP.getAccountType(), null);
		RestClient cli = mgr.peekRestClient();

		if (authToken == null || refreshToken == null || !refreshToken.equals(cli.getRefreshToken())) {
 
 			authToken = cli.getAuthToken();
 			refreshToken = cli.getRefreshToken();
 			instanceUrl = cli.getClientInfo().instanceUrl.toString();
 			clientId = cli.getClientInfo().clientId;
 
 			String settingsResponse = cacheGet(instanceUrl + SETTINGS_ENDPOINT, 30, ctx);
 			if(settingsResponse == null) { 
 				settingsResponse = makeSFRequest(instanceUrl + SETTINGS_ENDPOINT, true, true);
 			} else { 
 				settings = new JSONObject(settingsResponse);
 				
 				if(settings.getInt("cacheTimeout") < 30) {
 					settingsResponse = makeSFRequest(instanceUrl + SETTINGS_ENDPOINT, true, true);
 				}
 			}
 			
 			settings = new JSONObject(settingsResponse);
 
 		}
 	}
 	
 	public String makeSFRequest(String uri, boolean retry, boolean useCache) throws AMException {
 		
 		try {
 			ensureToken();
 			
 			if(settings != null && useCache) {
 				String cacheData = cacheGet(uri, settings.getInt("cacheTimeout"), ctx);
 				
 				if(cacheData != null) {
 					return cacheData;
 				}
 			}
 			
 			boolean error = false;
 
 			String line = null;
 
 			URL url = new URL(uri);
 
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.addRequestProperty("Authorization", "OAuth " + authToken); 
 
 			StringBuilder responseBody = new StringBuilder();
 
 			BufferedReader reader = null;
 
 			try {
 				reader = new BufferedReader(new InputStreamReader(
 						conn.getInputStream()));
 			} catch (IOException ex) {
 				error = true;
 			}
 			
 			if(conn.getResponseCode() != 200) {
 				error = true;
 			}
 			
 			if(!error) {
 				while ((line = reader.readLine()) != null) {
 					responseBody.append(line);
 				}
 			}
 			
 			try {
 				JSONObject response = new JSONObject(responseBody.toString());
 				
 				if(response.has("errorCode")) {
 					error = true;
 				}
 			} catch (Exception e) {
 				// not a json return, can ignore
 				e.printStackTrace();
 			}
 
 			if (error) {
 				if (retry) {
 					url = new URL(instanceUrl + "/services/oauth2/token");
 
 					conn = (HttpURLConnection) url.openConnection();
 
 					conn.setRequestMethod("POST");
 					conn.setRequestProperty("ContentType",
 							"application/x-www-form-urlencoded");
 					conn.setRequestProperty("Accept", "application/json");
 
 					conn.setDoOutput(true);
 
 					conn.getOutputStream().write(
 							("grant_type=refresh_token&client_id=" + clientId
 									+ "&refresh_token=" + refreshToken).getBytes());
 
 					reader = new BufferedReader(new InputStreamReader(
 							conn.getInputStream()));
 
 					while ((line = reader.readLine()) != null) {
 						responseBody.append(line);
 					}
 
 					authToken = new JSONObject(responseBody.toString()).getString("access_token"); 
 					
 					return makeSFRequest(uri, false, true);
 
 				} else {
 					throw new RuntimeException(conn.getResponseMessage());
 				}
 			} else {
 				cachePut(uri, responseBody.toString(), ctx); 
 				
 				return responseBody.toString();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new AMException(e);
 		}
 
 	}
 	
 	public String makeTeletracRequest(String uri) throws AMException {
 		try {
 			uri += getTeletracQueryString();
 			
 			URL url = new URL(uri);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			StringBuilder responseBody = new StringBuilder();
 			BufferedReader reader = null;
 
 			reader = new BufferedReader(new InputStreamReader(
 						conn.getInputStream()));
 			
 			String line = null;
 			
 			while((line = reader.readLine()) != null) {
 				responseBody.append(line);
 			}
 			
 			return responseBody.toString();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			
 			throw new AMException(uri);
 		}
 	}
 	
 	public JSONObject getSchedule(String stopName) throws AMException {
 		try {
 			ensureToken();
 			
 			return new JSONObject(makeSFRequest(instanceUrl + "/services/apexrest/BusStopSchedule?busStopName=" + URLEncoder.encode(stopName, "UTF-8"), true, true));
 		} catch (Exception e) {
 			e.printStackTrace();
 			
 			throw new AMException(e);
 		}
 	}
 	
 	public JSONArray getBusStops() throws AMException {
 		try {
 			ensureToken();
 			
 			return new JSONArray(makeSFRequest(instanceUrl + "/services/apexrest/BusStops/", true, true));
 		} catch (Exception e) {
 			e.printStackTrace();
 			
 			throw new AMException(e);
 		}
 	}
 	
 	public String getVehicles() throws AMException {
 		String buses = makeTeletracRequest("https://xmlgateway.teletrac.net/AsciiService.asmx/GetVehicles");
 		
 		return buses;		
 	}
 }
