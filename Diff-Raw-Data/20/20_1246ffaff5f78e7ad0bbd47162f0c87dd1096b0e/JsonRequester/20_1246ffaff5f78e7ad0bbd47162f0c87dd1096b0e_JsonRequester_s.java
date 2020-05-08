 package com.h5n1.eventsys;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.h5n1.eventsys.events.Event;
 
 @SuppressWarnings("rawtypes")
 public class JsonRequester {
 	private static String json;
 	private static JSONObject jsonObj;
 	private static InputStream is;
 //	private static HashMap<Long, JSONObject> generatedEvents = new HashMap<Long, JSONObject>();
 	private static JSONArray events;
 	private static String token;
 	
 	private static String deviceID = "42";
 	public static String getDeviceID() {
 		return deviceID;
 	}
 	public static void setDeviceID(String deviceID) {
 		JsonRequester.deviceID = deviceID;
 	}
 	
	public static final String TAG_EVENTS = "events";
 	public static final String TAG_MESSAGE = "message";
 	public static final String TAG_CONTENT = "content";
 	public static final String TAG_TYPE = "type";
 	public static final String TAG_TIME = "time";
 	public static final String TAG_ID = "id";
 	public static final String TAG_DEVICEID = "deviceid";
 	public static final String TAG_EVENTID = "eventid";
 	public static final String TAG_RECEIVERID = "receiverid";
 
 	
 	private static final String REGISTER_DEVICE_URL = "http://192.168.1.92/ubicomp/register_device.php";
 	private static final String CREATE_DEVICE_URL = "http://192.168.1.92/ubicomp/create_device.php";
 	private static final String CREATE_URL = "http://192.168.1.92/ubicomp/create_event.php";
 	private static final String GET_ALL_URL = "http://192.168.1.92/ubicomp/get_all_events.php";
 	private static final String UPDATE_URL = "http://192.168.1.92/ubicomp/update_event.php";
 	private static final String DELETE_URL = "http://192.168.1.92/ubicomp/delete_event.php";
 	private static final String DELETE_ALL_URL = "http://192.168.1.92/ubicomp/delete_events.php";
 	private static final String GET_URL = "http://192.168.1.92/ubicomp/get_event.php";
 	// private static final String CREATE_URL = "http://54.235.186.77/ubicomp/api/post/event";
 	// private static final String DELETE_URL = "http://54.235.186.77/ubicomp/api/delete/event";
 	// private static final String GET_URL = "http://54.235.186.77/ubicomp/api/get/event/";
 	// private static final String TOKEN_URL = "http://54.235.186.77/ubicomp/api/get/token";
 
 //	public static void getToken() {
 //		List<NameValuePair> params = new ArrayList<NameValuePair>();
 //		JSONObject getJsonObj = makeHttpRequest(TOKEN_URL, "POST", params);	
 //		token = (String) getJsonObj.get("token");
 //		deviceID = (Integer) getJsonObj.get("device_id");
 //	}
 	
 	public static JSONObject newEvent(Event event) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair(TAG_DEVICEID, getDeviceID()));
 		params.add(new BasicNameValuePair(TAG_EVENTID, ""+event.getEventId()));
 		params.add(new BasicNameValuePair(TAG_RECEIVERID, ""+event.getReceiverId()));
 		params.add(new BasicNameValuePair(TAG_TYPE, event.getClass().getSimpleName() + "-" + event.getType().toString()));
 		params.add(new BasicNameValuePair(TAG_CONTENT, event.toJsonString()));
 		return makeHttpRequest(CREATE_URL, "POST", params);
 	}
 	
	public static JSONObject registerDevice(String deviceid) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair(TAG_DEVICEID, getDeviceID()));
 		return makeHttpRequest(REGISTER_DEVICE_URL, "POST", params);
 	}
 
 	public static JSONObject updateEvent(Event event) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair(TAG_DEVICEID, getDeviceID()));
 		params.add(new BasicNameValuePair(TAG_EVENTID, ""+event.getEventId()));
 		params.add(new BasicNameValuePair(TAG_RECEIVERID, event.getReceiverId()));
 		params.add(new BasicNameValuePair(TAG_CONTENT, event.toJsonString()));
 		return makeHttpRequest(UPDATE_URL, "POST", params);
 	}
 
 	public static JSONObject getEvent(String deviceid, String receiverid, Long eventid) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair(TAG_EVENTID, eventid+""));
 		params.add(new BasicNameValuePair(TAG_DEVICEID, deviceid));
 		params.add(new BasicNameValuePair(TAG_RECEIVERID, receiverid));
 		return makeHttpRequest(GET_URL, "GET", params);
 	}
 
 	public static JSONObject deleteEvent(String deviceid, Long eventid) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair(TAG_EVENTID, eventid+""));
 		params.add(new BasicNameValuePair(TAG_DEVICEID, deviceid));
 		return makeHttpRequest(DELETE_URL, "POST", params);
 	}
 	
 	public static JSONObject deleteEvents(String deviceid) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair(TAG_DEVICEID, deviceid));
 		return makeHttpRequest(DELETE_ALL_URL, "POST", params);
 	}
 	
 	public static JSONObject createDeviceTable(String deviceid) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair(TAG_DEVICEID, deviceid));
 		return makeHttpRequest(CREATE_DEVICE_URL, "POST", params);
 	}
 
 	public static JSONObject getAllEvents(String deviceid, String receiverid) {
 //		JSONArray array = new JSONArray();
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair(TAG_DEVICEID, deviceid));
 		params.add(new BasicNameValuePair(TAG_RECEIVERID, receiverid));
 		JSONObject getJsonObj = makeHttpRequest(GET_ALL_URL, "GET", params);
 		return getJsonObj;
 //		try {
 //			array.put(new JSONObject(getJsonObj.get(TAG_MESSAGE)) );
 //			events = getJsonObj.getJSONArray(TAG_EVENTS);
 //			for (int i = 0; i < events.length(); i++) {
 //				JSONObject c = events.getJSONObject(i);
 //				String id = c.getString(TAG_ID);
 //				String type = c.getString(TAG_TYPE);
 //				String time = c.getString(TAG_TIME);
 //				String content = c.getString(TAG_CONTENT);
 //				JSONObject event = new JSONObject();
 //				event.put(TAG_ID, id);
 //				event.put(TAG_TYPE, type);
 //				event.put(TAG_TIME, time);
 //				event.put(TAG_CONTENT, content);
 //				array.put(event);
 //			}
 //			return array;
 //		} catch (JSONException e) {
 //			e.printStackTrace();
 //		}
 //		return null;
 	}
 
 	public static JSONObject makeHttpRequest(String url, String method,
 			List<NameValuePair> params) {
 		try {
 			jsonObj = null;
 			if (method == "POST") {
 				DefaultHttpClient httpClient = new DefaultHttpClient();
 				HttpPost httpPost = new HttpPost(url);
 				httpPost.setEntity(new UrlEncodedFormEntity(params));
 				HttpResponse httpResponse = httpClient.execute(httpPost);
 				HttpEntity httpEntity = httpResponse.getEntity();
 				is = httpEntity.getContent();
 			} else if (method == "GET") {
 				DefaultHttpClient httpClient = new DefaultHttpClient();
 				String paramString = URLEncodedUtils.format(params, "utf-8");
 				url += "?" + paramString;
 				HttpGet httpGet = new HttpGet(url);
 				HttpResponse httpResponse = httpClient.execute(httpGet);
 				HttpEntity httpEntity = httpResponse.getEntity();
 				is = httpEntity.getContent();
 			}
 			try {
 				BufferedReader reader = new BufferedReader(new InputStreamReader(
 						is, "utf-8"), 8);
 				StringBuilder sb = new StringBuilder();
 				String line = null;
 				while ((line = reader.readLine()) != null) {
 					sb.append(line + "\n");
 				}
 				is.close();
 				json = sb.toString();
 				try {
 					jsonObj = new JSONObject(json.trim());
 				} catch (JSONException e) {
 					System.out.println("Error parsing data " + e.toString());
 				}
 			} catch (Exception e) {
 				System.out.println("Error converting result " + e.toString());
 			}
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return jsonObj;
 	}
 }
