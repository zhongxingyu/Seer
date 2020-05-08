 package de.electricdynamite.pasty;
 
 /*
  *  Copyright 2012-2013 Philipp Geschke
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.DefaultedHttpParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Base64;
 import android.util.Log;
 
 public class PastyClient {
 	private static final String		TAG = PastyClient.class.toString();
 	private static final String		REST_SERVER_DEFAULT_BASE_HOST = "api.pastyapp.org";
 	private static final int		REST_SERVER_DEFAULT_PORT_HTTP = 80;
 	private static final int 		REST_SERVER_DEFAULT_PORT_HTTPS = 443;
 	private static final boolean	REST_SERVER_DEFAULT_TLS_ENABLED = true;
 	private static final boolean LOCAL_LOG = false;
 	
 	private String 						REST_SERVER_BASE_URL;
 	private Boolean						REST_SERVER_TLS_ENABLE;
 
     private static final String		REST_URI_ITEM			= "/v2/clipboard/item";
     private static final String 	REST_URI_CLIPBOARD		= "/v2/clipboard/list.json";
     private static final String		REST_URI_DEVICE			= "/v2.1/devices/android";
 	
 	public static final int			API_VERSION				= 2;
 	public static final String		VERSION = "0.3.0";
 	
 	private String username;
 	private String password;
 	private String basicAuthInfo;
 	private final String httpUserAgent = "PastyClient for Android/"+PastyClient.VERSION;
 	private DefaultedHttpParams defaultHttpParams;
 	
 
 	public PastyClient(String restServerBaseURL, Boolean tls) {
 		this.REST_SERVER_BASE_URL = restServerBaseURL;
 		this.REST_SERVER_TLS_ENABLE = tls;
 		initializeEnvironment();
 	}
 	
 	public PastyClient() {
 		String url = "";
 		if(PastyClient.REST_SERVER_DEFAULT_TLS_ENABLED) {
 			url = "https://";
 		} else {
 			url = "http://";
 		}
 		url = url+PastyClient.REST_SERVER_DEFAULT_BASE_HOST;
 		this.REST_SERVER_BASE_URL = url;
 		this.REST_SERVER_TLS_ENABLE = PastyClient.REST_SERVER_DEFAULT_TLS_ENABLED;
 		initializeEnvironment();
 	}
 	
 	private void initializeEnvironment() {
 		//this.defaultHttpParams.setParameter(CoreProtocolPNames.USER_AGENT, this.httpUserAgent);
 	}
 	
 	public void setUsername(String username) {
 		this.username = username;
 		this.basicAuthInfo = null;
 	}
 	
 	public void setPassword(String password) {
 		this.password = password;
 		this.basicAuthInfo = null;
 	}
 	
 	public JSONArray getClipboard() throws PastyException {
 		String url 				= REST_SERVER_BASE_URL+REST_URI_CLIPBOARD;
 		if(LOCAL_LOG) Log.v(TAG,"Starting REST call to API endpoint  "+url);
 		StringBuilder builder	= new StringBuilder();
 		HttpClient client 		= new DefaultHttpClient();
 		HttpGet httpGet			= new HttpGet(url);
 		
 		try {
 			httpGet.setHeader("Authorization", getHTTPBasicAuth());
 		    httpGet.setHeader("Content-type", "application/json"); 
 		    httpGet.setHeader("User-Agent", "PastyClient for Android/"+PastyClient.VERSION);
 		    System.setProperty("http.keepAlive", "false");
 		    HttpResponse response = client.execute(httpGet);
 		    StatusLine statusLine = response.getStatusLine();
 		    int statusCode = statusLine.getStatusCode();
 		    if(LOCAL_LOG) Log.v(TAG, "REST CALL finished with status "+statusCode);
 		    if (statusCode == 200) {
 		    	HttpEntity entity = response.getEntity();
 		    	InputStream content = entity.getContent();
 		    	BufferedReader reader = new BufferedReader(
 				new InputStreamReader(content));
 				String line;
 				while ((line = reader.readLine()) != null) {
 					builder.append(line);
 				}
 				entity		= null;
 				content		= null;
 				reader		= null;
 				JSONObject jsonResponse = new JSONObject(builder.toString());
 				JSONObject jsonPayload = jsonResponse.getJSONObject("payload");
 				JSONArray jsonClipboard = jsonPayload.getJSONArray("items");
 				builder 	= null;
 				client 		= null;
 				httpGet		= null;
 				response	= null;
 				statusLine	= null;
 				return jsonClipboard;
 			} else if(statusCode == 401) {
 				throw new PastyException(PastyException.ERROR_AUTHORIZATION_FAILED);
 			} else {
 				throw new PastyException(PastyException.ERROR_ILLEGAL_RESPONSE);
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			throw new PastyException(PastyException.ERROR_IO_EXCEPTION);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new PastyException(PastyException.ERROR_IO_EXCEPTION);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new PastyException(PastyException.ERROR_ILLEGAL_RESPONSE);
 		}
 	}
 	
 	public String addItem(final String Item) throws PastyException {
 		String url 				= REST_SERVER_BASE_URL+REST_URI_ITEM;
 		if(LOCAL_LOG) Log.v(TAG,"Starting REST call to API endpoint "+url);
 		StringBuilder builder	= new StringBuilder();
 		HttpClient client 		= new DefaultHttpClient();
 		HttpPost httpPost		= new HttpPost(url);
 		JSONObject params		= new JSONObject();
 		
 		try {
 			httpPost.setHeader("Authorization", getHTTPBasicAuth());
 		    params.put("item", Item);
 		    httpPost.setEntity(new ByteArrayEntity(
 		        params.toString().getBytes("UTF8")));
 		    httpPost.setHeader("Content-type", "application/json");
 		    httpPost.setHeader("User-Agent", "PastyClient for Android/"+PastyClient.VERSION);
 		    System.setProperty("http.keepAlive", "false");
 		    HttpResponse response = client.execute(httpPost);
 		    StatusLine statusLine = response.getStatusLine();
 		    int statusCode = statusLine.getStatusCode();
 		    if(LOCAL_LOG) Log.v(TAG,"REST call finished with status "+statusCode);
 		    if (statusCode == 201) {
 		    	HttpEntity entity = response.getEntity();
 		    	InputStream content = entity.getContent();
 		    	BufferedReader reader = new BufferedReader(
 				new InputStreamReader(content));
 				String line;
 				while ((line = reader.readLine()) != null) {
 					builder.append(line);
 				}
 				entity		= null;
 				content		= null;
 				reader		= null;
 				JSONObject jsonResponse = new JSONObject(builder.toString());
 				JSONObject jsonPayload = jsonResponse.getJSONObject("payload");
 				String ItemId = jsonPayload.getString("_id");
 				builder 	= null;
 				client 		= null;
 				httpPost	= null;
 				params		= null;
 				response	= null;
 				statusLine	= null;
 				return ItemId;
 			} else if(statusCode == 401) {
 				throw new PastyException(PastyException.ERROR_AUTHORIZATION_FAILED);
 			} else {
 				throw new PastyException(PastyException.ERROR_ILLEGAL_RESPONSE);
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			throw new PastyException(PastyException.ERROR_IO_EXCEPTION);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new PastyException(PastyException.ERROR_IO_EXCEPTION);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new PastyException(PastyException.ERROR_ILLEGAL_RESPONSE);
 		}
 	}	
 	
 	public void deleteItem(ClipboardItem Item) throws PastyException {
 		String url 				= REST_SERVER_BASE_URL+REST_URI_ITEM+Item.getId();
 		if(LOCAL_LOG) Log.v(TAG,"Starting REST call to API endpoint "+url);
 		HttpClient client 		= new DefaultHttpClient();
 		HttpDelete httpDelete	= new HttpDelete(url);
 		
 		try {
 			httpDelete.setHeader("Authorization", getHTTPBasicAuth());
 		    httpDelete.setHeader("Content-type", "application/json");
 		    httpDelete.setHeader("User-Agent", "PastyClient for Android/"+PastyClient.VERSION);
 		    System.setProperty("http.keepAlive", "false");
 		    HttpResponse response = client.execute(httpDelete);
 		    StatusLine statusLine = response.getStatusLine();
 		    int statusCode = statusLine.getStatusCode();
 		    if(LOCAL_LOG) Log.v(TAG,"REST call finished with status "+statusCode);
 		    if(statusCode == 200) {
 				client 		= null;
 				httpDelete	= null;
 				response	= null;
 				statusLine	= null;
 			} else if(statusCode == 401) {
 				throw new PastyException(PastyException.ERROR_AUTHORIZATION_FAILED);
 			} else {
 				throw new PastyException(PastyException.ERROR_ILLEGAL_RESPONSE);
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			throw new PastyException(PastyException.ERROR_IO_EXCEPTION);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new PastyException(PastyException.ERROR_IO_EXCEPTION);
 		}
 	}	
 	
 	public boolean registerDevice(String regid) throws PastyException {
 		String url 				= REST_SERVER_BASE_URL+REST_URI_DEVICE;
 		if(LOCAL_LOG) Log.v(TAG,"Starting REST call to API endpoint "+url);
 		StringBuilder builder	= new StringBuilder();
 		HttpClient client 		= new DefaultHttpClient();
 		HttpPost httpPost		= new HttpPost(url);
 		JSONObject params		= new JSONObject();
 		
 		try {
 			httpPost.setHeader("Authorization", getHTTPBasicAuth());
		    params.put("regId", regid);
 		    httpPost.setEntity(new ByteArrayEntity(
 		        params.toString().getBytes("UTF8")));
 		    httpPost.setHeader("Content-type", "application/json");
 		    httpPost.setHeader("User-Agent", "PastyClient for Android/"+PastyClient.VERSION);
 		    System.setProperty("http.keepAlive", "false");
 		    HttpResponse response = client.execute(httpPost);
 		    StatusLine statusLine = response.getStatusLine();
 		    int statusCode = statusLine.getStatusCode();
 		    if(LOCAL_LOG) Log.v(TAG,"REST call finished with status "+statusCode);
 		    if (statusCode == 201) {
 		    	HttpEntity entity = response.getEntity();
 		    	InputStream content = entity.getContent();
 		    	BufferedReader reader = new BufferedReader(
 				new InputStreamReader(content));
 				String line;
 				while ((line = reader.readLine()) != null) {
 					builder.append(line);
 				}
 				entity		= null;
 				content		= null;
 				reader		= null;
 				JSONObject jsonResponse = new JSONObject(builder.toString());
 				JSONObject jsonPayload = jsonResponse.getJSONObject("payload");
 				Boolean success = jsonPayload.getBoolean("success");
 				builder 	= null;
 				client 		= null;
 				httpPost	= null;
 				params		= null;
 				response	= null;
 				statusLine	= null;
 				return success;
 			} else if(statusCode == 401) {
 				throw new PastyException(PastyException.ERROR_AUTHORIZATION_FAILED);
 			} else {
 				throw new PastyException(PastyException.ERROR_ILLEGAL_RESPONSE);
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			throw new PastyException(PastyException.ERROR_IO_EXCEPTION);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new PastyException(PastyException.ERROR_IO_EXCEPTION);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new PastyException(PastyException.ERROR_ILLEGAL_RESPONSE);
 		}
 	}
 	
 	public boolean unregisterDevice(String regId) throws PastyException {
 		String url 				= REST_SERVER_BASE_URL+REST_URI_DEVICE+"/"+regId;
 		if(LOCAL_LOG) Log.v(TAG,"Starting REST call to API endpoint "+url);
 		HttpClient client 		= new DefaultHttpClient();
 		HttpDelete httpDelete	= new HttpDelete(url);
 		
 		try {
 			httpDelete.setHeader("Authorization", getHTTPBasicAuth());
 		    httpDelete.setHeader("Content-type", "application/json");
 		    httpDelete.setHeader("User-Agent", "PastyClient for Android/"+PastyClient.VERSION);
 		    System.setProperty("http.keepAlive", "false");
 		    HttpResponse response = client.execute(httpDelete);
 		    StatusLine statusLine = response.getStatusLine();
 		    int statusCode = statusLine.getStatusCode();
 		    if(LOCAL_LOG) Log.v(TAG,"REST call finished with status "+statusCode);
 		    if(statusCode == 200) {
 				client 		= null;
 				httpDelete	= null;
 				response	= null;
 				statusLine	= null;
 				return true;
 			} else if(statusCode == 401) {
 				throw new PastyException(PastyException.ERROR_AUTHORIZATION_FAILED);
 			} else {
 				throw new PastyException(PastyException.ERROR_ILLEGAL_RESPONSE);
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			throw new PastyException(PastyException.ERROR_IO_EXCEPTION);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new PastyException(PastyException.ERROR_IO_EXCEPTION);
 		}
 	}
 	
 	private String getHTTPBasicAuth() {
 		if(basicAuthInfo == null) {
 			String auth = username+":"+password;
 			this.basicAuthInfo = "Basic " + Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP);
 			auth = null;
 		}
 		return this.basicAuthInfo;
 	}
 }
