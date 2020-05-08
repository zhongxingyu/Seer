 package com.ijg.darklight.web.sdk;
 
 import java.util.HashMap;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonObject;
 import com.ijg.darklight.web.api.APIRequest;
 import com.ijg.darklight.web.api.DarklightAPI;
 
 /**
  * Easy access to the Darklight API
  * @author Isaac Grant
  * @author Lucas Nicodemus
  * @version .1
  * @see com.ijg.darklight.web.api.DarklightAPI
  */
 
 public class DarklightSDK {
 	String APIProtocol;
 	String APIServer;
 	
	int APISessionId;
 	String APISessionKey;
 	
 	private DarklightAPI api;
 	
 	JsonObject lastRequestResponse;
 	
 	/**
 	 * Empty constructor
 	 * requires calling setAPIProtocol() and setAPIServer() at
 	 * a later time, followed by createAPI(), if the API is to be used.
 	 */
 	public DarklightSDK() {}
 	
 	/**
 	 * API constructor: creates API without having to explicitly call createAPI()
 	 * @param APIProtocol Protocol to be used by the DarklightAPI
 	 * @param APIServer Server to be used by the DarklightAPI
 	 * @param APISessionId The API session ID used by the DarklightAPI
 	 */
	public DarklightSDK(String APIProtocol, String APIServer, int APISessionId) {
 		this.APIProtocol = APIProtocol;
 		this.APIServer = APIServer;
 		this.APISessionId = APISessionId;
 		if(!createAPI()) {
 			System.out.println("[DarklightSDK] error creating API");
 		}
 	}
 	
 	/**
 	 * Check if the DarklightAPI is ready to be created 
 	 * @return True if the API protocol, server, and session id have been specified
 	 */
 	private boolean APIReady() {
 		if (APIProtocol != null && APIServer != null && APISessionId > 0) {
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Create DarklightAPI
 	 * @return True if an instance of DarklightAPI was successfully created
 	 */
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
 	
 	/**
 	 * 
 	 * @return The current instance of DarklightAPI, or if api==null, calls createAPI, and returns api
 	 */
 	public DarklightAPI api() {
 		if (api == null) {
 			createAPI();
 		}
 		return api;
 	}
 	
 	/**
 	 * 
 	 * @return The protocol used by DarklightAPI
 	 */
 	public String getAPIProtocol() {
 		return APIProtocol;
 	}
 	
 	/**
 	 * 
 	 * @param APIProtocol The protocol to be used by DarklightAPI
 	 */
 	public void setAPIProtocol(String APIProtocol) {
 		this.APIProtocol = APIProtocol;
 	}
 	
 	/**
 	 * 
 	 * @return The server used by DarklightAPI
 	 */
 	public String getAPIServer() {
 		return APIServer;
 	}
 	
 	/**
 	 * 
 	 * @param APIServer The server to be used by DarklightAPI
 	 */
 	public void setAPIServer(String APIServer) {
 		this.APIServer = APIServer;
 	}
 	
 	/**
 	 * 
 	 * @return The session ID used by DarklightAPI
 	 */
	public int getAPISessionID() {
 		return APISessionId;
 	}
 	
 	/**
 	 * 
 	 * @param id The session ID to be used by DarklightAPI
 	 */
	public void setAPISessionID(int id) {
 		APISessionId = id;
 	}
 	
 	/**
 	 * Public utility function to generate a JSON string from a HashMap
 	 * @param map A hash map to be turned into JSON
 	 * @return A JSON string generated from the passed hash map
 	 */
 	public String toJSONString(HashMap<?, ?> map) {
 		Gson gson = new GsonBuilder().setPrettyPrinting().create();
 		return gson.toJson(map);
 	}
 	
 	
 	/*
 	 * Easy API access
 	 * ===============================
 	 */
 	/**
 	 * Get the session key from the Darklight API
 	 * @param name The name to which the session key belongs
 	 * @return True if the request succeeded
 	 */
 	public boolean apiAuth(String name) {
 		HashMap<String, String> parameters = new HashMap<String, String>();
 		parameters.put("name", name);
 		
 		APIRequest authRequest = api().individualSessionRequest(APISessionId, DarklightAPI.AUTH_ENDPOINT, 
 				parameters);
 		authRequest.send();
 		
 		lastRequestResponse = authRequest.getResponse();
 		
 		long statusCode = (long) authRequest.get("status_code");
 		if (statusCode == 200L || statusCode == 201L) {
 			APISessionKey = (String) authRequest.get("sessionkey");
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
 	
 	/**
 	 * Send a score update to the Darklight API
 	 * @param score The number of fixed issues
 	 * @param desc A HashMap with the keys being issue names, values being issue descriptions
 	 * @return True if the request succeeded
 	 */
 	public boolean apiUpdate(int score, HashMap<String, String> desc) {
 		HashMap<String, String> parameters = new HashMap<String, String>();
 		parameters.put("sessionkey", APISessionKey);
 		parameters.put("score", "" + score);
 		parameters.put("desc", toJSONString(desc));
 		
 		APIRequest updateRequest = api().individualSessionRequest(APISessionId, DarklightAPI.UPDATE_ENDPOINT, 
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
