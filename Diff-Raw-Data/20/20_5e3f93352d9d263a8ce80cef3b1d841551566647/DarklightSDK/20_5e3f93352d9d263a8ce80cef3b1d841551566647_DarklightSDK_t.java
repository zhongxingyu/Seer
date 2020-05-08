 package com.ijg.darklight.web.sdk;
 
 import java.util.HashMap;
 
 import org.json.simple.JSONObject;
 
 import com.ijg.darklight.web.api.APIRequest;
 import com.ijg.darklight.web.api.DarklightAPI;
 
 public class DarklightSDK {
 	String APIProtocol;
 	String APIServer;
 	
 	long API_SESSION_ID;
 	String API_SESSION_KEY;
 	
 	private DarklightAPI api;
 	
 	JSONObject lastRequestResponse;
 	
 	/*
 	 * Empty constructor
 	 * requires calling setAPIProtocol() and setAPIServer() at
 	 * a later time, followed by createAPI(), if the API is to be used.
 	 */
 	public DarklightSDK() {}
 	
 	// API constructor: creates API without having to explicitly call createAPI()
 	public DarklightSDK(String APIProtocol, String APIServer, long APISessionID) {
 		this.APIProtocol = APIProtocol;
 		this.APIServer = APIServer;
 		API_SESSION_ID = APISessionID;
		if(!createAPI()) {
			System.out.println("[DarklightSDK] error creating API");
		}
 	}
 	
 	
 	private boolean APIReady() {
 		if (APIProtocol != null && APIServer != null && API_SESSION_ID > 0) {
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean createAPI() {
 		/*
 		 * Instead of explicitly checking APIReady(),
 		 * if createAPI() returns false then you know
 		 * either the APIProtocol or APIServer are not
 		 * defined (!APIReady())
 		 */
 		if (APIReady()) {
 			api = new DarklightAPI(APIProtocol, APIServer);
 			return true;
 		}
 		return false;
 	}
 	
 	public DarklightAPI api() {
 		if (api == null) {
 			createAPI();
 		}
 		return api;
 	}
 	
 	public String getAPIProtocol() {
 		return APIProtocol;
 	}
 	public void setAPIProtocol(String APIProtocol) {
 		this.APIProtocol = APIProtocol;
 	}
 	public String getAPIServer() {
 		return APIServer;
 	}
 	public void setAPIServer(String APIServer) {
 		this.APIServer = APIServer;
 	}
 	public long getAPISessionID() {
 		return API_SESSION_ID;
 	}
 	public void setAPISessionID(long id) {
 		API_SESSION_ID = id;
 	}
 	
 	public String toJSONString(HashMap<?, ?> map) {
 		// Easier than importing JSONObject elsewhere
 		return JSONObject.toJSONString(map);
 	}
 	
 	
 	/*
 	 * Easy API access
 	 * ===============================
 	 */
 	public boolean apiAuth(String name) {
 		HashMap<String, String> parameters = new HashMap<String, String>();
 		parameters.put("name", name);
 		
 		APIRequest authRequest = api().individualSessionRequest(API_SESSION_ID, DarklightAPI.AUTH_ENDPOINT, 
 				parameters);
 		authRequest.send();
 		
 		lastRequestResponse = authRequest.getResponse();
 		
 		long statusCode = (long) authRequest.get("status_code");
 		if (statusCode == 200L || statusCode == 201L) {
 			API_SESSION_KEY = (String) authRequest.get("sessionkey");
 			return true;
 		}
		System.out
				.println("[DarklightSDK] api auth error, returned with status code: "
						+ statusCode);
		try {
			System.out.println("[DarklightSDK] error description: "
				+ authRequest.get("desc"));
		} catch (Exception e) {}
 		
 		return false;
 	}
 	
 	public boolean apiUpdate(int score, HashMap<String, String> desc) {
 		HashMap<String, String> parameters = new HashMap<String, String>();
 		parameters.put("sessionkey", API_SESSION_KEY);
 		parameters.put("score", "" + score);
 		parameters.put("desc", toJSONString(desc));
 		
 		APIRequest updateRequest = api().individualSessionRequest(API_SESSION_ID, DarklightAPI.UPDATE_ENDPOINT, 
 				parameters);
 		updateRequest.send();
 		
 		lastRequestResponse = updateRequest.getResponse();
 		
 		long statusCode = (long) updateRequest.get("status_code");
 		if (statusCode == 200L || statusCode == 201L) {
 			return true;
 		}
		System.out
		.println("[DarklightSDK] api update error, returned with status code: "
				+ statusCode);
		try {
			System.out.println("[DarklightSDK] error description: "
					+ updateRequest.get("desc"));
		} catch (Exception e) {}
 		
 		return false;
 	}
 }
