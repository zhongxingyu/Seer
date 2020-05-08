 package eu.neq.mais.connector.impl;
 
 //The Client sessions package
 import com.thetransactioncompany.jsonrpc2.client.*;
 
 //The Base package for representing JSON-RPC 2.0 messages
 import com.thetransactioncompany.jsonrpc2.*;
 
 import eu.neq.mais.connector.Connector;
 
 
 //For creating URLs
 import java.net.*;
 import java.util.HashMap;
 import java.util.Map;
 
 
 
public class GNUHealthConnectorImpl extends Connector {
 
 	private static Connector instance = null;
 
 	public static Connector getInstance(){
 		
 		if(instance == null){
 			instance = new GNUHealthConnectorImpl();
 		}
 		return instance;
 	}
 	
 	public static void main(String[] args) {
 		
 		// TESTING
 		String username = "admin";
 		String password = "iswi223<<";
 
 		username = "demo_de";
 		password = "demo";
 		
 		GNUHealthConnectorImpl.getInstance().login(username, password);
 
 		// Creating a new session to a JSON-RPC 2.0 web service at a specified URL
 
 		// The JSON-RPC 2.0 server URL
 		
 	
 	}
 
 
 
 	public void logout() {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	public String login(String username, String password) {
 		
 		
 		URL serverURL = null;
 
 		try {
 			serverURL = new URL("http://" + username + ":" + password + "@demo2.2.tryton.org:8000/demo2.2");
 
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 
 		// Create new JSON-RPC 2.0 client session
 		JSONRPC2Session mySession = new JSONRPC2Session(serverURL);
 		// Once the client session object is created, you can use to send a series
 		// of JSON-RPC 2.0 requests and notifications to it.
 
 		// Sending an example "getServerTime" request:
 
 		
 		
 		// Construct new request
 		String method = "common.db.login";
 		
 		Map params = new HashMap();
 		params.put("user", "demo_de");
 		params.put("pw", "demo");
 		
 		JSONRPC2Request request = new JSONRPC2Request(method, params, "0001");
 
 		
 		
 		
 		// Send request
 		JSONRPC2Response response = null;
 
 		try {
 			response = mySession.send(request);
 
 		} catch (JSONRPC2SessionException e) {
 
 		e.printStackTrace();
 	
 		}
 		if(response == null){
 			System.out.println("fuck you");
 		}
 		System.out.println("balökj: "+response.toJSON().toJSONString());
 
 		// Print response result / error
 		if (response.indicatesSuccess())
 			System.out.println(response.getResult());
 		else
 			System.out.println(response.getError().getMessage());
 		
 		return "Change me when done";
 	}
 
 	public Object exec(Object o) {
 		return o;
 		// TODO Auto-generated method stub
 		
 	}
 }
