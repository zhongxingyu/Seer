 package com.appathon.letsgo;
 
 import java.io.*;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.*;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.*;
 
 import android.util.Log;
 
 public class HTTPHelper {
 
 	/*
 	 * LIES, ALL LIES... but necessary lies for the demo
 	 */
 	static User[] us = new User[]
 			{ new User("Duke Nukem", "666", "dnukem"),
 			  new User("Captain Hammer", "13", "cphamm"),
 			  new User("Jack Sparrow", "2011", "captain"),
 			  new User("Fanx Emerson", "5", "doood")
 			};
 	static Event[] es = new Event[]
 			{ new Event(1, new Date(2013,07,12), "RM7116#203", "$10", "stokes", us),
 			  new Event(2, new Date(2013,07,15), "OPS1C", "($5000)", "DoD ;)", us),
 			  new Event(3, new Date(2075,12,25), "Ragnarok", "$5T", "Loki", us)
 			};
 
 	
 	//Need values
 	private static final String
             URL_GET_USER = "http://mphox.scripts.mit.edu/LetsGoScripts/addUser.php",
             URL_DEL_USER = "",
 			URL_CRT_USER = "http://mphox.scripts.mit.edu/LetsGoScripts/addUser.php",
             URL_GET_EVNT = "",
             URL_DEL_EVNT = "",
 			URL_CRT_EVNT = "",
             URL_ATN_EVNT = "",
             URL_GET_EVNT_ATNS = "http://mphox.scripts.mit.edu/LetsGoScripts/getAttendeesFromEvent.php",
             URL_GET_ALL_EVNT = "http://mphox.scripts.mit.edu/LetsGoScripts/getAllEvents.php";
 	
 	public static final int
 		ST_ABSENT = 0,
 		ST_GOING  = 1,
 		ST_DRIVING = 2;
 
 	static InputStream is = null;
 	static JSONObject jObj = null;
 	static String json = "";
 
 	private static JSONObject getJSONFromUrl(String url, List<NameValuePair> pairs)
 			throws JSONException {
 
 		// Making HTTP request
 		try {
 			// defaultHttpClient
 			DefaultHttpClient httpClient = new DefaultHttpClient();
 			HttpPost httpPost = new HttpPost(url);
 
 			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
 
 			HttpResponse httpResponse = httpClient.execute(httpPost);
 			HttpEntity httpEntity = httpResponse.getEntity();
 			is = httpEntity.getContent();
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "iso-8859-1"), 8);
 			StringBuilder sb = new StringBuilder();
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + "\n");
 			}
 			is.close();
 			json = sb.toString();
 		} catch (Exception e) {
 			Log.e("Buffer Error", "Error converting result " + e.toString());
 		}
 
 		// try parse the string to a JSON object
 		jObj = new JSONObject(json);
 
 		// return JSON String
 		return jObj;
 
 	}
 
 	/*
 	 * Sends {SID: String}
 	 * Expects {NickName: String} {Number: String} {SID: String}
 	 * Returns {Query0 {NickName: String, Number: String, SID: String} }
 	 */
 	// returns the User object corresponding to the sid
 	public static User GetUser(String sid) {
 		//Fake code
 		for(User u : us)
 		{
 			if(u.getSID().equals(sid))
 				return u;
 		}
 		return User.NO_ONE;
 		
 		//Real code
 		/*
 		JSONObject reader;
 		try {
 			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 			params.add(new BasicNameValuePair("SID", sid));
 			reader = getJSONFromUrl(URL_GET_USER, params);
 
 			return new User(reader.getString("NickName"),
 					reader.getString("Number"), reader.getString("SID"));
 		} catch (JSONException e) {
 		}
 
 		return User.NO_ONE;
 		*/
 	}
 
 	// deletes the User object corresponding to the sid
 	// returns true if User was deleted successfully, false otherwise
 	/*
 	 * Sends {SID: String}
 	 * Expects { anything if successful, nothing if failed }
 	 */
 	public static boolean DeleteUser(String sid) {
 		//Fake code
 
 		return true;
 		
 		//Real code
 		/*
 		JSONObject reader;
 
 		try {
 			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 			params.add(new BasicNameValuePair("SID", sid));
 			reader = getJSONFromUrl(URL_DEL_USER, params);
 			if(reader.length() > 0)
 				return true;
 		} catch (JSONException e) {
 		}
 		return false;
 		*/
 	}
 
 	/*
 	 * Sends {SID: String} {NickName: String} {Number: String}
 	 * Expects {anything if successful, nothing if failed}
 	 */
 	public static boolean CreateUser(User user) {
 		return true;
 		/*
 		JSONObject reader;
 
 		try {
 			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 			params.add(new BasicNameValuePair("SID", user.getSID()));
 			params.add(new BasicNameValuePair("NickName", user.getNickName()));
 			params.add(new BasicNameValuePair("Number", user.getPhoneNumber()));
 			reader = getJSONFromUrl(URL_CRT_USER, params);
 			if(reader.length() > 0)
 				return true;
 		} catch (JSONException e) {
 		}
 		return false;
 		*/
 	}
 
 	/*
 	 * Sends {ID: int}
 	 * Expects {ID: int} {Start: String (date)} {Location: String} {Cost: String} {POC: String}
 	 */
 	public static com.appathon.letsgo.Event GetEvent(int eventID) {
 		//Fake code
 		for(Event e : es)
 		{
 			if(e.getID() == eventID)
 				return e;
 		}
 		return Event.NO_EVENT;
 		
 		//Real code
 		/*
 		JSONObject reader;
 		try {
 			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 			params.add(new BasicNameValuePair("ID", Integer.valueOf(eventID).toString()));
 			reader = getJSONFromUrl(URL_GET_EVNT, params);
 
 			return new Event(reader.getInt("ID"), Date.valueOf(reader.getString("Start")),
 					reader.getString("Location"), reader.getString("Cost"), reader.getString("POC"));
 		} catch (JSONException e) {
 		}
 
 		return Event.NO_EVENT;
 		*/
 	}
 
 	/*
 	 * Sends {ID: int}
 	 * Expects {anything if successful, nothing if failed}
 	 */
 	public static boolean DeleteEvent(int eventID) {
 		return true;
 		/*
 		JSONObject reader;
 
 		try {
 			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 			params.add(new BasicNameValuePair("ID", Integer.valueOf(eventID).toString()));
 			reader = getJSONFromUrl(URL_DEL_EVNT, params);
 			if(reader.length() > 0)
 				return true;
 		} catch (JSONException e) {
 		}
 		return false;
 		*/
 	}
 
 	/*
 	 * Sends {ID: int} {Start: String} {Location: String} {Cost: String} {POC: String}
 	 * Expects {anything if successful, nothing if failed}
 	 */
 	public static boolean CreateEvent(Event event) {
		return true;
		/*
 		JSONObject reader;
 
 		try {
 			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 			params.add(new BasicNameValuePair("ID", Integer.valueOf(event.getID()).toString()));
 			params.add(new BasicNameValuePair("Start", event.getStartTime().toString()));
 			params.add(new BasicNameValuePair("Location", event.getLocation()));
 			params.add(new BasicNameValuePair("Cost", event.getCost()));
 			params.add(new BasicNameValuePair("POC", event.getPOC()));
 			reader = getJSONFromUrl(URL_CRT_EVNT, params);
 			if(reader.length() > 0)
 				return true;
 		} catch (JSONException e) {
 		}
 		return false;
		*/
 	}
 
 	/*
 	 * Sends {ID: int} {SID: String} {Driving: int}
 	 * Expects {anything if successful, nothing if failed}
 	 */
 	public static boolean AttendEvent(Event event, User user, int State) {
		return true;
		/*
 		JSONObject reader;
 
 		try {
 			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 			params.add(new BasicNameValuePair("ID", Integer.valueOf(event.getID()).toString()));
 			params.add(new BasicNameValuePair("SID", user.getSID()));
 			params.add(new BasicNameValuePair("Driving", Integer.valueOf(State).toString()));
 			reader = getJSONFromUrl(URL_ATN_EVNT, params);
 			if(reader.length() > 0)
 				return true;
 		} catch (JSONException e) {
 		}
 		return false;
		*/
 	}
 	
 	/*
 	 * Sends {ID: int}
 	 * Expects {Attendees: [ {SID: String} ] }
 	 */
 	public static User[] GetAttendees(int eventID) {
		Event e = GetEvent(eventID);
		return (User[]) e.getAttendees().toArray();
		/*
 		JSONObject reader;
 		try {
 			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 			params.add(new BasicNameValuePair("ID", Integer.valueOf(eventID).toString()));
 			reader = getJSONFromUrl(URL_GET_EVNT_ATNS, params);
 			JSONArray attns = reader.getJSONArray("Attendees");
 			
 			//Loop through the JSON array, and query for each user who is attending
 			User[] attns_info = new User[attns.length()];
 			for(int i = 0; i < attns.length(); i++) {
 				attns_info[i] = GetUser(attns.getJSONObject(i).getString("SID"));
 			}
 			return attns_info;
 		} catch (JSONException e) {
 		}
 
 		return new User[0];
		*/
 	}
 }
