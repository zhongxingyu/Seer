 package pt.up.fe.cmov.rest;
 
 import org.apache.http.conn.ConnectTimeoutException;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 public class RailsRestClient {
 	
	public final static String SERVER_URL = "http://10.0.0.144:3000/";
 
 
 	public static JSONObject Get(String controller) throws ConnectTimeoutException {
 		return HttpClient.SendHttpGet(SERVER_URL + controller + ".json");
 	}
 	
 	public static JSONArray GetArray(String controller) throws ConnectTimeoutException {
 		return HttpClient.SendHttpGetArray(SERVER_URL + controller + ".json");
 	}
 	
 	public static JSONObject Get(String controller, String parameters) throws ConnectTimeoutException {
 		return HttpClient.SendHttpGet(SERVER_URL + controller + ".json?"+parameters);
 	}
 	
 	public static JSONArray GetArray(String controller, String parameters) throws ConnectTimeoutException {
 		return HttpClient.SendHttpGetArray(SERVER_URL + controller + ".json?"+parameters);
 	}
 	
 	public static JSONObject Post(String controller, JSONObject obj) throws ConnectTimeoutException {
 		return HttpClient.SendHttpPost(SERVER_URL + controller + ".json", obj);
 	}
 	
 	public static JSONObject Delete(String controller, String id) throws ConnectTimeoutException {
 		return HttpClient.SendHttpDelete(SERVER_URL + controller + "/" + id + ".json");
 	}
 	
 	public static JSONObject Put(String controller, String id, JSONObject obj) throws ConnectTimeoutException {
 		return HttpClient.SendHttpPut(SERVER_URL + controller + "/" + id + ".json", obj);
 	}
 }
