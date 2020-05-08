 package com.tim.stullich.drawerapp;
 
 import java.io.IOException;
import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
 
 import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthenticationException;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
 
 import android.app.Activity;
 import android.util.Log;
 
 import com.google.gson.Gson;
 
 public class APIRequest {
 
 	//TODO Clean up this mess
 	public static final int SERVER_STATUS = 0;
 	public static final int TARGET_REQUEST = 1;
 	public static final int TARGET_ATTRIBUTE_REQUEST = 2;
 	public static final int DEBUG_MODE = 3;
 
 	private static String FINAL_ADDRESS;
 	private static String SERVER_ADDRESS;
 	private static int SERVER_PORT;
 	
 	private HttpClient client;
 	private HttpGet request;
 	//private static final String USER_PREFS_FILE = "UserPrefs";
 	
 	/**
 	 * Builds a request to be executed at a later time
 	 * @param act
 	 * @param apiType
 	 */
 	public APIRequest(Activity act, final int apiType) {
 		//SharedPreferences settings = act.getSharedPreferences(USER_PREFS_FILE, 0);
 		SERVER_ADDRESS = "iam.vm.oracle.com";
 		SERVER_PORT = 18102;
 				
 		request = new HttpGet();
 		try {
 			request.setURI(new URI(SERVER_ADDRESS));
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 		}
 		
 		UsernamePasswordCredentials credentials =
                 new UsernamePasswordCredentials("olaf", "welcome1");
 		BasicScheme scheme = new BasicScheme();
         Header authorizationHeader = null;
 		try {
 			authorizationHeader = scheme.authenticate(credentials, request);
 		} catch (AuthenticationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         request.addHeader(authorizationHeader);
 		
 		//TODO Build more cases... maybe even a better way to implement this.
 		/*switch (apiType) {
 			case SERVER_STATUS : 
 				StringBuilder sb = new StringBuilder();
 				sb.append("https://");
 				sb.append(SERVER_ADDRESS + ":");
 				sb.append(SERVER_PORT);
 				sb.append("/opam/");
 				FINAL_ADDRESS = sb.toString();
 				break;
 		}*/
 	}
 	
 	public boolean login() {
 		try {
 			Log.i("OPAM", "Let's break");
 			client.execute(request);
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return true;
 	}
 	
 	//TODO Handle Threading issue.
 	public Gson execute() throws IOException {
 		Log.i("OPAM", "Let's break");
 		client.execute(request);
 		return null;
 	}
 }
