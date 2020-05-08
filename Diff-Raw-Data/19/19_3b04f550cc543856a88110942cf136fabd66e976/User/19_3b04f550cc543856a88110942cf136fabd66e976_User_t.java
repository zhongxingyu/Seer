 package com.envsocial.android.api;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONStringer;
 
 import android.content.Context;
 import android.util.JsonWriter;
 
 public class User {
 	public static final String TAG = "user";
 	
 	private Context mContext;
 	private Location mLocation;
 	private UserProfileData mUserData;
 	private String mUri;
 	
 	public User(Context context, Location location, UserProfileData userdata) {
 		mContext = context;
 		mLocation = location;
 		mUserData = userdata;
 	}
 	
 	public User(Context context, Location location, UserProfileData userdata, String uri) {
 		this(context, location, userdata);
 		mUri = uri;
 	}
 	
 	
 	public static List<User> getUsers(Context context, Location location, String jsonString) throws Exception {
 		if (jsonString == null) { 
 			String type = (location.isEnvironment()) ? Location.ENVIRONMENT : Location.AREA;
 			Url url = new Url(Url.RESOURCE, TAG);
 			url.setParameters(new String[] { type }, 
 					new String[] { location.getId() }
 			);
 			
 			//System.err.println("[DEBUG]>> Url for getUSERS: " + url);
 			return getUsersList(context, url.toString(), location);
 		}
 		else {
 			return parse(context, jsonString, location);
 		}
 	}
 	
 	
 	public static List<User> getUsers(Context context, Location location, int offset, int limit) throws Exception {
 		String type = (location.isEnvironment()) ? Location.ENVIRONMENT : Location.AREA;
 		Url url = new Url(Url.RESOURCE, TAG);
 		url.setParameters(new String[] { type, "offset", "limit" }, 
 				new String[] { location.getId(), offset + "", limit + "" }
 		);
 		
 		return getUsersList(context, url.toString(), location);
 	}
 	
 	
 	private static List<User> getUsersList(Context context, String url, Location location) throws Exception {
 		AppClient client = new AppClient(context);
 		HttpResponse response = client.makeGetRequest(url);
 		
 		// Check the status code
 		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
 			throw new Exception(Integer.toString(HttpStatus.SC_SERVICE_UNAVAILABLE));
 		}
 		
 		// If SC_OK, parse response
 		String responseData = EntityUtils.toString(response.getEntity());
 		
 		
 		// For now we will do several requests until we consume the entire user list
 		List<User> users = parse(context, responseData, location);
 		JSONObject meta = new JSONObject(responseData).getJSONObject("meta");
 		String next = meta.getString("next");
 		
 		if (next != null && !next.equalsIgnoreCase("null")) {
 			//System.err.println("[DEBUG]>> Next url for list: " + next);
 			users.addAll(getUsersList(context, next, location));
 		}
 		
 		return users;
 	}
 	
 	
 	// Parse response to get a list of User objects
 	private static List<User> parse(Context context, String jsonString, 
 			Location location) throws JSONException {
 		
 		JSONArray array = new JSONObject(jsonString).getJSONArray("objects");
 		int len = array.length();
 
 		List<User> users = new ArrayList<User>();
 		
 		for (int i = 0; i < len; ++i) {
 			JSONObject user = array.getJSONObject(i);
 			
 			String uri = user.getString("resource_uri");
 			UserProfileData userdata = UserProfileData.parseProfileData(user);
 			
 			users.add(new User(context, location, userdata, uri));
 		}
 		
 		return users;
 	}
 	
 	
 	public static String toJSON(List<User> users) {
 		try {
 			if (users == null) {
 				return null;
 			}
 			
 			JSONArray userListJSON = new JSONArray();
 			int len = users.size();
 			
 			for (int i = 0; i < len; i++) {
 				User userdata = users.get(i);
 				
 				// build research_interests list
 				JSONArray research_interests = new JSONArray();
 				for (int k = 0; k < userdata.getUserData().getResearchInterests().length; k++) {
 					research_interests.put(userdata.getUserData().getResearchInterests()[k]);
 				}
 				
 				//System.err.println("[DEBUG]>> checked in people research_interests: " + research_interests);
 				
 				// build reseatrch_profile hash
 				JSONObject research_profile = new JSONObject();
 				research_profile.put("affiliation", userdata.getUserData().getAffiliation());
 				research_profile.put("research_interests", research_interests);
 				
 				//System.err.println("[DEBUG]>> checked in people research_profile: " + research_profile);
 				
 				// build user object hash
 				JSONObject userJSON = new JSONObject();
 				userJSON.put("resource_uri", userdata.getUri());
 				userJSON.put("first_name", userdata.getUserData().getFirstName());
 				userJSON.put("last_name", userdata.getUserData().getLastName());
 				userJSON.put("research_profile", research_profile);
 				
 				userListJSON.put(userJSON);
 			}
 			
 			JSONObject list = new JSONObject();
 			list.put("objects", userListJSON);
 			String jsonString = list.toString();
 			
 			//System.err.println("[DEBUG]>> created checked in people JSON string: " + jsonString);
 			return jsonString;
 			
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	
 	public Location getLocation() {
 		return mLocation;
 	}
 	
 	public UserProfileData getUserData() {
 		return mUserData;
 	}
 	
 	public String getUri() {
 		return mUri;
 	}
 	
 	public static class UserProfileData {
 		
 		UserProfileData(String firstName, String lastName, String affiliation, String[] researchInterests) {
 			this.firstName = firstName;
 			this.lastName = lastName;
 			this.affiliation = affiliation;
 			this.researchInterests = researchInterests;
 		}
 
 		private String firstName;
 		private String lastName;
 		private String affiliation;
 		private String[] researchInterests;
 		
 		public String getFirstName() {
 			return firstName;
 		}
 		
 		public String getLastName() {
 			return lastName;
 		}
 		
 		public String getAffiliation() {
 			return affiliation;
 		}
 		
 		public String[] getResearchInterests() {
 			return researchInterests;
 		}
 		
 		public static UserProfileData parseProfileData(JSONObject user) throws JSONException {
 			String firstName = user.getString("first_name");
 			String lastName = user.getString("last_name");
 			String affiliation = "n.a.";
 			String[] researchInterests = {"n.a."};
 			
 			//System.err.println("[DEBUG]>> user profile JSONObject: " + user.toString());
 			
 			JSONObject research_profile = (JSONObject)user.opt("research_profile");
 			
 			if (research_profile != null) {
				affiliation = research_profile.optString("affiliation", "n.a.");
 				
				JSONArray research_interests = research_profile.optJSONArray("research_interests");
				if (research_interests != null) {
					int len = research_interests.length();
					researchInterests = new String[len];
					 
					for (int i = 0; i < len; i++) {
						researchInterests[i] = research_interests.optString(i, "n.a.");
					}
 				}
 			}
 			
 			return new UserProfileData(firstName, lastName, affiliation, researchInterests);
 		}
 	}
 }
