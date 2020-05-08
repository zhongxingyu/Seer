 package com.drexel.duca.backend;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Scanner;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonIOException;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.JsonSyntaxException;
 import com.temboo.Library.CloudMine.ObjectStorage.ObjectGet;
 import com.temboo.Library.CloudMine.ObjectStorage.ObjectGet.ObjectGetInputSet;
 import com.temboo.Library.CloudMine.ObjectStorage.ObjectGet.ObjectGetResultSet;
 import com.temboo.Library.CloudMine.ObjectStorage.ObjectSearch;
 import com.temboo.Library.CloudMine.ObjectStorage.ObjectSearch.ObjectSearchInputSet;
 import com.temboo.Library.CloudMine.ObjectStorage.ObjectSearch.ObjectSearchResultSet;
 import com.temboo.Library.CloudMine.ObjectStorage.ObjectSet;
 import com.temboo.Library.CloudMine.ObjectStorage.ObjectSet.ObjectSetInputSet;
 import com.temboo.Library.CloudMine.ObjectStorage.ObjectSet.ObjectSetResultSet;
 import com.temboo.Library.Facebook.Reading.User;
 import com.temboo.Library.Facebook.Reading.User.UserInputSet;
 import com.temboo.Library.Facebook.Reading.User.UserResultSet;
 import com.temboo.Library.Facebook.Searching.FQL;
 import com.temboo.Library.Facebook.Searching.FQL.FQLInputSet;
 import com.temboo.Library.Facebook.Searching.FQL.FQLResultSet;
 import com.temboo.core.TembooException;
 import com.temboo.core.TembooSession;
 import com.temboo.outputs.Facebook.FacebookUser;
 
 public class Application {
 
 	private int idCounter = 1;
 	private Facebook fb = new Facebook();
 	public STUser user;
 	private TembooSession session;
 	public Scanner input;
 	public Gson gson;
 	public ArrayList<STUser> usersOnline;
 	public ArrayList<STUser> friends;
 	public boolean usingFB = false;
 
 	public Application() throws TembooException {
 		session = new TembooSession("Razgriz", "SafeTalk", "wEdaH1w5LdKnQHXCIMv6");
 		input = new Scanner(System.in);
 		gson = new Gson();
 		friends = new ArrayList<STUser>();
 
 	}
 
 	public String getIP() throws JsonIOException, JsonSyntaxException, IOException {
 		URL url = new URL("http://ip-api.com/json");
 		HttpURLConnection huc = (HttpURLConnection) url.openConnection();
 		JsonParser jp = new JsonParser();
 		
 		JsonElement root = jp.parse(new InputStreamReader((InputStream) huc.getContent()));
 		
 		return root.getAsJsonObject().get("query").getAsString();
 	}
 
 	public String reformatUserFromCloudMine(String original) {
 		int firstIndex = original.indexOf("{\"stID");
 		int indexToRemove = original.indexOf("errors") - 3;
 		return original.substring(firstIndex, indexToRemove);
 	}
 
 	public String reformatIDFromCloudMine(String original) {
 		int firstIndex = original.indexOf("success") + 9;
 		int indexToRemove = original.indexOf("errors") - 3;
 		return original.substring(firstIndex, indexToRemove);
 	}
 
	public boolean firstTime() {

		// may not be needed in gui at all

		System.out.print("Is this your first time on SafeTalk? (y/n): ");
		String response = input.nextLine();
		if (response.equalsIgnoreCase("y"))
			return true;
		else
			return false;
	}

 	public boolean isUsernameAvailable(String username) throws TembooException {
 		ObjectGet objectGetChoreo = new ObjectGet(session);
 
 		// Get an InputSet object for the choreo
 		ObjectGetInputSet objectGetInputs = objectGetChoreo.newInputSet();
 
 		// Set inputs
 		objectGetInputs.set_APIKey("48e28d9c141e463899c9787620ac16dc");
 		objectGetInputs.set_ApplicationIdentifier("b09b78c7522d40a2b0c798bb41b405b4");
 		objectGetInputs.set_Keys(username);
 
 		// Execute Choreo
 		ObjectGetResultSet objectGetResults = objectGetChoreo.execute(objectGetInputs);
 		
 		if (objectGetResults.get_Response()
 				.equals("{\"success\":{},\"errors\":{\"" + username
 						+ "\":{\"code\":404,\"message\":\"Not found\"}}}")) {
 			return true;
 		} else
 			return false;
 	}
 
 	public void createSTUserManually(String username, String passwordString)
 			throws TembooException, JSONException, JsonIOException, JsonSyntaxException, IOException {
 		
 		while (!this.isUsernameAvailable(username)) {
 			throw new RuntimeException("Username taken.");
 		}
 
 		int stID = genSTid();
 
 		String ip = getIP();
 		this.user = new STUser(stID, new ArrayList<Long>(), username, passwordString,
 				new ArrayList<Integer>(), ip, false);
 
 	}
 
 	public void createSTUserFromFB(String username, String passwordString, String accessToken)
 			throws TembooException, JSONException, JsonIOException, JsonSyntaxException, IOException {
 
 		usingFB = true;
 		User userChoreo = new User(session);
 
 		// Get an InputSet object for the choreo
 		UserInputSet userInputs = userChoreo.newInputSet();
 
 		// Set inputs
 		userInputs.set_AccessToken(accessToken);
 
 		// Execute Choreo
 		UserResultSet userResults = userChoreo.execute(userInputs);
 
 		FacebookUser fbUser = userResults.getUser();
 
 		while (!this.isUsernameAvailable(username)) {
 			throw new RuntimeException("Username taken.");
 		}
 
 		STUser user = new STUser(genSTid(), new ArrayList<Long>(), username, passwordString,
 				new ArrayList<Integer>(), getIP(), false);
 		System.out.println(fbUser.getId());
 		user.setFbID(fbUser.getId());
 		user.setAccessToken(accessToken);
 		this.user = user;
 		getFriendsUsingFB();
 
 	}
 
 	public void pushUser() throws TembooException {
 		String jsonString = gson.toJson(user);
 		jsonString = "{\"" + user.getUsername() + "\":" + jsonString + "}";
 
 		ObjectSet objectSetChoreo = new ObjectSet(session);
 
 		// Get an InputSet object for the choreo
 		ObjectSetInputSet objectSetInputs = objectSetChoreo.newInputSet();
 
 		// Set inputs
 		objectSetInputs.set_Data(jsonString);
 		objectSetInputs.set_APIKey("48e28d9c141e463899c9787620ac16dc");
 		objectSetInputs.set_ApplicationIdentifier("b09b78c7522d40a2b0c798bb41b405b4");
 
 		// Execute Choreo
 		ObjectSetResultSet objectSetResults = objectSetChoreo.execute(objectSetInputs);
 	}
 
 	public void logIn(String username, String password) throws TembooException, JSONException {
 		ObjectGet objectGetChoreo = new ObjectGet(session);
 
 		// Get an InputSet object for the choreo
 		ObjectGetInputSet objectGetInputs = objectGetChoreo.newInputSet();
 
 		// Set inputs
 		objectGetInputs.set_APIKey("48e28d9c141e463899c9787620ac16dc");
 		objectGetInputs.set_ApplicationIdentifier("b09b78c7522d40a2b0c798bb41b405b4");
 		objectGetInputs.set_Keys(username);
 
 		// Execute Choreo
 		ObjectGetResultSet objectGetResults = objectGetChoreo.execute(objectGetInputs);
 		if (objectGetResults.get_Response()
 				.equals("{\"success\":{},\"errors\":{\"" + username
 						+ "\":{\"code\":404,\"message\":\"Not found\"}}}")) {
 			throw new RuntimeException("Username not found.");
 		}
 		String JSONuser = this.reformatUserFromCloudMine(objectGetResults.get_Response());
 
 		STUser pulled = gson.fromJson(JSONuser, STUser.class);
 
 		if (pulled.getPassword().equals(password)) {
 			pulled.setOnline(true);
 			this.user = pulled;
 			pushUser();
 		} else {
 			throw new RuntimeException("Password incorrect.");
 		}
 		
		if (usingFB) {
			getFriendsUsingFB();
		}
 
 	}
 
 	public void getFriendsUsingFB() throws TembooException, JSONException {
 
 		FQL fQLChoreo = new FQL(session);
 
 		// Get an InputSet object for the choreo
 		FQLInputSet fQLInputs = fQLChoreo.newInputSet();
 
 		// Set inputs.... uid1's # is the user's facebook ID
 		fQLInputs.set_Conditions("uid IN (SELECT uid2 FROM friend WHERE uid1=" + user.getFbID()
 				+ ") AND is_app_user=1");
 		fQLInputs.set_AccessToken(user.getAccessToken());
 		fQLInputs.set_Fields("uid,name");
 		fQLInputs.set_Table("user");
 
 		// Execute Choreo
 		FQLResultSet fQLResults = fQLChoreo.execute(fQLInputs);
 		String fQL_json = fQLResults.get_Response();
 		JSONArray jsonResults = new JSONObject(fQL_json).getJSONArray("data");
 		ArrayList<Long> fbFriendsIDs = user.getContactstIDlist();
 		for (int i = 0; i < jsonResults.length(); i++) {
 			JSONObject friendJSON = jsonResults.getJSONObject(i);
 			if (!fbFriendsIDs.contains(friendJSON.getLong("uid"))) {
 				fbFriendsIDs.add(friendJSON.getLong("uid"));
 			}
 		}
 		user.setContactstIDlist(fbFriendsIDs);
 		ArrayList<Integer> STidList = user.getSTcontactsList();
 
 		// If needed, the entire Application class can be written to get all users, and almost all filtering can be done locally instead of making more specific calls
 		for (long fbIDnum : user.getContactstIDlist()) {
 			ObjectSearch objectSearchChoreo = new ObjectSearch(session);
 			ObjectSearchInputSet objectSearchInputs = objectSearchChoreo.newInputSet();
 
 			// Set inputs
 			objectSearchInputs.set_APIKey("48e28d9c141e463899c9787620ac16dc");
 			objectSearchInputs.set_Query("[fbID = \"" + fbIDnum + "\"]");
 			objectSearchInputs.set_ApplicationIdentifier("b09b78c7522d40a2b0c798bb41b405b4");
 
 			// Execute Choreo
 			ObjectSearchResultSet objectSearchResults = objectSearchChoreo.execute(objectSearchInputs);
 
 			String JSONresults = this.reformatUserFromCloudMine(objectSearchResults.get_Response());
 			STUser pulled = gson.fromJson(JSONresults, STUser.class);
 			if (!STidList.contains(pulled.getStID())) {
 				STidList.add(pulled.getStID());
 			}
 		}
 
 		user.setSTcontactsList(STidList);
 	}
 
 	public int genSTid() throws TembooException, JSONException {
 
 		ObjectGet objectGetChoreo = new ObjectGet(session);
 
 		// Get an InputSet object for the choreo
 		ObjectGetInputSet objectGetInputs = objectGetChoreo.newInputSet();
 
 		// Set inputs
 		objectGetInputs.set_APIKey("48e28d9c141e463899c9787620ac16dc");
 		objectGetInputs.set_ApplicationIdentifier("b09b78c7522d40a2b0c798bb41b405b4");
 		objectGetInputs.set_Keys("STidData");
 
 		// Execute Choreo
 		ObjectGetResultSet objectGetResults = objectGetChoreo.execute(objectGetInputs);
 		String STIDjson = this.reformatIDFromCloudMine(objectGetResults.get_Response());
 		int firstIndex = STIDjson.indexOf("Data") + 6;
 		STIDjson = STIDjson.substring(firstIndex);
 		JSONObject STIDjsonObject = new JSONObject(STIDjson);
 		int toReturn = STIDjsonObject.getInt("nextAvailableSTID");
 
 		ObjectSet objectSetChoreo = new ObjectSet(session);
 
 		// Get an InputSet object for the choreo
 		ObjectSetInputSet objectSetInputs = objectSetChoreo.newInputSet();
 
 		// Set inputs
 		objectSetInputs.set_Data("{\"STidData\":{\"nextAvailableSTID\":" + (toReturn + 1) + "}}");
 		objectSetInputs.set_APIKey("48e28d9c141e463899c9787620ac16dc");
 		objectSetInputs.set_ApplicationIdentifier("b09b78c7522d40a2b0c798bb41b405b4");
 
 		// Execute Choreo
 		ObjectSetResultSet objectSetResults = objectSetChoreo.execute(objectSetInputs);
 
 		return toReturn;
 	}
 
 	public void setOffline() throws TembooException {
 		this.user.setOnline(false);
 		pushUser();
 	}
 
 	public void addFriendManually(String username) throws TembooException {
 		
 		ObjectSearch objectSearchChoreo = new ObjectSearch(session);
 
 		// Get an InputSet object for the choreo
 		ObjectSearchInputSet objectSearchInputs = objectSearchChoreo.newInputSet();
 
 		// Set credential to use for execution
 		objectSearchInputs.setCredential("SafeTalk");
 
 		// Set inputs
 		objectSearchInputs.set_Query("[username = \"" + username + "\"]");
 
 		// Execute Choreo
 		ObjectSearchResultSet objectSearchResults = objectSearchChoreo.execute(objectSearchInputs);
 
 		if (objectSearchResults.get_Response().equals("{\"success\":{},\"errors\":{}}")) {
 			throw new RuntimeException("No user found with that username.");
 		} else {
 			String JSONresults = this.reformatUserFromCloudMine(objectSearchResults.get_Response());
 			STUser pulled = gson.fromJson(JSONresults, STUser.class);
 			int friendSTid = pulled.getStID();
 			ArrayList<Integer> STidList = user.getSTcontactsList();
 			if (!STidList.contains(friendSTid)) {
 				STidList.add(friendSTid);
 			}
 			user.setSTcontactsList(STidList);
 			//pushUser();
 			this.friends.clear();
 			try {
 				getFriendObjects();
 			} catch (JsonIOException | JsonSyntaxException | JSONException
 					| IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void getFriendObjects() throws TembooException, JSONException, JsonIOException, JsonSyntaxException, IOException {
 		if (usingFB) getFriendsUsingFB();
 		ObjectSearch objectSearchChoreo = new ObjectSearch(session);
 
 		// Get an InputSet object for the choreo
 		ObjectSearchInputSet objectSearchInputs = objectSearchChoreo.newInputSet();
 
 		// Set credential to use for execution
 		objectSearchInputs.setCredential("SafeTalk");
 
 		// Set inputs
 		objectSearchInputs.set_Query("[derp=null]");
 
 		// Execute Choreo
 		ObjectSearchResultSet objectSearchResults = objectSearchChoreo.execute(objectSearchInputs);
 		String JSONresponse = objectSearchResults.get_Response();
 
 		JSONObject children = new JSONObject(JSONresponse).getJSONObject("success");
 		Iterator<?> keys = children.keys();
 
 		friends.clear();
 		while (keys.hasNext()) {
 			String key = (String) keys.next();
 			if (children.get(key) instanceof JSONObject) {
 				STUser user = new Gson().fromJson(children.get(key).toString(), STUser.class);
 					STUser potentialFriend = (new Gson().fromJson(children.get(key).toString(), STUser.class));
 					if (this.user.getSTcontactsList().contains(potentialFriend.getStID())) {
 						friends.add(potentialFriend);
 					}
 				
 			}
 		}
 		this.user.setIp(getIP());
 		pushUser();
 	}
 
 }
