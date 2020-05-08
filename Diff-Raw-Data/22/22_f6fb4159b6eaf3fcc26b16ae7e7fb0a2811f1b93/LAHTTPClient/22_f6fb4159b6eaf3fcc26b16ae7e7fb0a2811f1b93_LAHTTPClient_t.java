 package de.wehub.lanetworking;
 
import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
 
 public class LAHTTPClient extends DefaultHttpClient {
	
	public LAHTTPClient() {
		super();
	}

	public LAHTTPClient(ClientConnectionManager conman, HttpParams params) {
		super(conman, params);
	}

	public LAHTTPClient(HttpParams params) {
		super(params);
	}
 
 	private static LAHTTPClient _instance = null;
 	
 	public static LAHTTPClient getInstance() {
 		if(_instance == null) {
 			_instance = new LAHTTPClient();
 		}
 		return _instance;
 	}
 }
