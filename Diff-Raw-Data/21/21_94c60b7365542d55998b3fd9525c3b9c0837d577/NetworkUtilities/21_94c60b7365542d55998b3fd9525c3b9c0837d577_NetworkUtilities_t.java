 package com.challengeearth.cedroid.helpers;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONObject;
 
 import android.accounts.NetworkErrorException;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.util.Log;
 
 public class NetworkUtilities {
 
	private final static String BASE_URI = "http://160.85.232.31:8080/com.challengeEarth_ChallengeEarth_war_1.0-SNAPSHOT/rest";
	//private final static String BASE_URI ="http://ec2-46-137-18-40.eu-west-1.compute.amazonaws.com/ChallengeEarth-1.0-SNAPSHOT/rest";
 	//private final static String BASE_URI = "http://www.challenge-earth.com/ChallengeEarth/rest";
 	private final static String TAG = "NetworkUtilities";
 	
 	
 	/**
 	 * This method returns true, when network connectivity is available, either wifi or mobile
 	 * 
 	 * @param context
 	 * @return
 	 */
 	static public boolean checkStatus(Context context) {
 		final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		final android.net.NetworkInfo wifi = connMgr
 				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		final android.net.NetworkInfo mobile = connMgr
 				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 		
 		boolean networkAvailable = (wifi.isConnected() || mobile.isConnected());
 		return networkAvailable;
 	}
 
 	
 	/**
 	 * <p>Retrieves the GET Reader to based on the Base URI and the given path</p>
 	 * 
 	 * @param path
 	 * 
 	 * @return
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 * @throws NetworkErrorException
 	 */
 	public static BufferedReader getGETReader(String path) throws ClientProtocolException, IOException, NetworkErrorException {
 		HttpClient client = new DefaultHttpClient();
 		if(!path.equals("")) {
 			path = "/"+path;
 		}
 		HttpGet get = new HttpGet(BASE_URI + path);	
 		HttpResponse response;
 		
 		Log.i(TAG, "HttpGet object created for URL: " + BASE_URI + path);
 		
 		response = client.execute(get);
 		StatusLine statusLine = response.getStatusLine();
 	
 		int statusCode = statusLine.getStatusCode();
 		if (statusCode == 200) {
 			HttpEntity entity = response.getEntity();
 			InputStream content = entity.getContent();
 			BufferedReader reader = new BufferedReader(
 					new InputStreamReader(content));
 			Log.i(TAG, "Reader created for URL: " + BASE_URI + path);
 			return reader;
 		}
 		else {
 			throw new NetworkErrorException();
 		}
 	}
 	
 	public static HttpResponse doPost(JSONObject object, String path) {
 		HttpClient client = new DefaultHttpClient();
 		HttpPost post = new HttpPost(BASE_URI + path);
 		try {
 			post.setEntity(new StringEntity(object.toString()));
 			post.setHeader("Content-Type", "application/json");
 			Log.i(TAG, "Do post: " + object.toString());
 			HttpResponse response = client.execute(post);
 			return response;
 		} catch (Exception e) {
 			Log.e(TAG, "could not perform post", e);
 		}
 		return null;
 	}
 }
