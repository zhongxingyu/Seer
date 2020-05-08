 package com.drexel.duca;
 
 import java.io.Console;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.regex.Pattern;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
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
 	private STUser user;
 	private TembooSession session;
 	public Scanner input;
 	public Gson gson;
 
 	public Application() throws TembooException {
 		session = new TembooSession("ryseff724", "SafeTalk",
 				"d9049685-fa16-4275-b");
 		input = new Scanner(System.in);
 		gson = new Gson();
 
 	}
	
	public String reformatFromCloudMine(String original) {
		int firstIndex = original.indexOf("success")+9;
		int indexToRemove = original.indexOf("errors") - 3;
		return original.substring(firstIndex, indexToRemove);
	}
 
 	public boolean firstTime() {
 		System.out.print("Is this your first time on SafeTalk? (y/n): ");
 		String response = input.nextLine();
 		if (response.equalsIgnoreCase("y"))
 			return true;
 		else
 			return false;
 	}
 
 	public void createSTUserManually() throws TembooException, JSONException {
 		System.out.print("Enter a SafeTalk username: ");
 		String username = input.nextLine();
 		// CHECK FOR USERNAME IN USE.
 
 		Console console = System.console();
 		char[] password = console.readPassword("Enter a password: ");
 		String passwordString = new String(password);
 
 		int stID = genSTid();
 
 		this.user = new STUser(stID, new ArrayList<Long>(), username,
 				passwordString, new ArrayList<Integer>());
 	}
 
 	public void createSTUserFromFB() throws TembooException, JSONException {
 		String accessCode = fb.OAuth();
 
 		User userChoreo = new User(session);
 
 		// Get an InputSet object for the choreo
 		UserInputSet userInputs = userChoreo.newInputSet();
 
 		// Set inputs
 		userInputs.set_AccessToken(fb.getOAuthResults().get_AccessToken());
 
 		// Execute Choreo
 		UserResultSet userResults = userChoreo.execute(userInputs);
 
 		FacebookUser fbUser = userResults.getUser();
 		
 		System.out.print("Enter a SafeTalk username: ");
 		String username = input.nextLine();
 		
 		System.out.print("Enter a SafeTalk password: ");
 		String passwordString = input.nextLine();
 		
 		/*
 		Console console = System.console();
 		char[] password = console.readPassword("Enter a password: ");
 		String passwordString = new String(password);
 		*/
 		
 		System.out.println(accessCode);
 		STUser user = new STUser(genSTid(), new ArrayList<Long>(),
 				username,
 				passwordString, new ArrayList<Integer>());
 		user.setFbID(fbUser.getId());
 		user.setAccessToken(accessCode);
 		this.user = user;
 	}
 
 	public void pushUser() throws TembooException {
 		String jsonString = gson.toJson(user);
		jsonString = "{\"" + user.getUsername() + "\":" + jsonString + "}";
 
 		ObjectSet objectSetChoreo = new ObjectSet(session);
 
 		// Get an InputSet object for the choreo
 		ObjectSetInputSet objectSetInputs = objectSetChoreo.newInputSet();
 
 		// Set inputs
 		System.out.println(jsonString);
 		objectSetInputs.set_Data(jsonString);
 		objectSetInputs.set_APIKey("48e28d9c141e463899c9787620ac16dc");
 		objectSetInputs
 				.set_ApplicationIdentifier("b09b78c7522d40a2b0c798bb41b405b4");
 
 		// Execute Choreo
 		ObjectSetResultSet objectSetResults = objectSetChoreo.execute(objectSetInputs);
 	}
 	
 
 	public void logIn(String username, String password) throws TembooException {
 		ObjectGet objectGetChoreo = new ObjectGet(session);
 
 		// Get an InputSet object for the choreo
 		ObjectGetInputSet objectGetInputs = objectGetChoreo.newInputSet();
 
 		// Set inputs
 		objectGetInputs.set_APIKey("48e28d9c141e463899c9787620ac16dc");
 		objectGetInputs
 				.set_ApplicationIdentifier("b09b78c7522d40a2b0c798bb41b405b4");
 
 		// ERROR CHECK FOR IF USERNAME DOESN'T EXIST.
 		objectGetInputs.set_Keys(username);
 
 		// Execute Choreo
		ObjectGetResultSet objectGetResults = objectGetChoreo.execute(objectGetInputs);
		String JSONuser = this.reformatFromCloudMine(objectGetResults.get_Response());
		System.out.println(JSONuser);
 
 		STUser pulled = gson.fromJson(JSONuser, STUser.class);
 
 		if (pulled.getPassword().equals(password)) {
 			pulled.setOnline(true);
 			this.user = pulled;
 			pushUser();
 		} else {
 			System.out.println("Wrong password.");
 			System.exit(1);
 		}
 
 	}
 
 	public void getFriendsUsingFB() throws TembooException, JSONException {
 
 		FQL fQLChoreo = new FQL(session);
 
 		// Get an InputSet object for the choreo
 		FQLInputSet fQLInputs = fQLChoreo.newInputSet();
 
 		// Set inputs.... uid1's # is the user's facebook ID
 		fQLInputs.set_Conditions("uid IN (SELECT uid2 FROM friend WHERE uid1="
 				+ user.getFbID() + ") AND is_app_user=1");
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
 
 		for (long fbIDnum : user.getContactstIDlist()) {
 			ObjectSearch objectSearchChoreo = new ObjectSearch(session);
 			ObjectSearchInputSet objectSearchInputs = objectSearchChoreo
 					.newInputSet();
 
 			// Set inputs
 			objectSearchInputs.set_APIKey("48e28d9c141e463899c9787620ac16dc");
 			objectSearchInputs.set_Query("[fbID = \"" + fbIDnum + "\"]");
 			objectSearchInputs
 					.set_ApplicationIdentifier("b09b78c7522d40a2b0c798bb41b405b4");
 
 			// Execute Choreo
 			ObjectSearchResultSet objectSearchResults = objectSearchChoreo
 					.execute(objectSearchInputs);
 			
			String JSONresults = this.reformatFromCloudMine(objectSearchResults.get_Response());
 			STUser pulled = gson.fromJson(JSONresults, STUser.class);
 			STidList.add(pulled.getStID());
 		}
 		
 		user.setSTcontactsList(STidList);
 	}
 
 	public int genSTid() throws TembooException, JSONException {
 		// Instantiate the Choreo, using a previously instantiated TembooSession
 		// object, eg:
 		// TembooSession session = new TembooSession("ryseff724",
 		// "APP_KEY_NAME", "APP_KEY_VALUE");
 		ObjectGet objectGetChoreo = new ObjectGet(session);
 
 		// Get an InputSet object for the choreo
 		ObjectGetInputSet objectGetInputs = objectGetChoreo.newInputSet();
 
 		// Set inputs
 		objectGetInputs.set_APIKey("48e28d9c141e463899c9787620ac16dc");
 		objectGetInputs
 				.set_ApplicationIdentifier("b09b78c7522d40a2b0c798bb41b405b4");
 		objectGetInputs.set_Keys("STidData");
 
 		// Execute Choreo
 		ObjectGetResultSet objectGetResults = objectGetChoreo
 				.execute(objectGetInputs);
		String STIDjson = this.reformatFromCloudMine(objectGetResults.get_Response());
 		int firstIndex = STIDjson.indexOf("Data")+6;
		STIDjson = STIDjson.substring(firstIndex);
 		System.out.println(STIDjson);
 		JSONObject STIDjsonObject = new JSONObject(STIDjson);
 		System.out.println(STIDjsonObject.toString());
 		int toReturn = STIDjsonObject.getInt("nextAvailableSTID");
 
 		ObjectSet objectSetChoreo = new ObjectSet(session);
 
 		// Get an InputSet object for the choreo
 		ObjectSetInputSet objectSetInputs = objectSetChoreo.newInputSet();
 
 		// Set inputs
 		objectSetInputs.set_Data("{\"STidData\":{\"nextAvailableSTID\":"
 				+ (toReturn + 1) + "}}");
 		objectSetInputs.set_APIKey("48e28d9c141e463899c9787620ac16dc");
 		objectSetInputs
 				.set_ApplicationIdentifier("b09b78c7522d40a2b0c798bb41b405b4");
 
 		// Execute Choreo
 		ObjectSetResultSet objectSetResults = objectSetChoreo
 				.execute(objectSetInputs);
 
 		return toReturn;
 	}
 
 	public void setOffline() throws TembooException {
 		this.user.setOnline(false);
 		pushUser();
 	}
 
 
 	public static void main(String[] args) throws TembooException,
 			JSONException {
 		Application app = new Application();
 		boolean firstTime = app.firstTime();
 		if (firstTime) app.createSTUserFromFB();
		else app.logIn("ryseff724", "1234");
 			
 		app.getFriendsUsingFB();
 		System.out.println(app.user.getContactstIDlist());
 		app.pushUser();
 
 	}
 }
