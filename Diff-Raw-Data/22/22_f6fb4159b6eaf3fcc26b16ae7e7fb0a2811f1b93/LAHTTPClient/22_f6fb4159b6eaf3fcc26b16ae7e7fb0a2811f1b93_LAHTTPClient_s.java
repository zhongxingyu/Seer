 package de.wehub.lanetworking;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 
 public class LAHTTPClient extends DefaultHttpClient {
 
 	private static LAHTTPClient _instance = null;
 	
 	public static LAHTTPClient getInstance() {
 		if(_instance == null) {
 			_instance = new LAHTTPClient();
 		}
 		return _instance;
 	}
 }
