 package com.example.syncme;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class ServerUtils {
 
 	public static String SERVER_URL = "http://10.0.0.2/SyncMe";
 	public static String POST = "post";
 
 	public static String register(String name, String email, String regId, String server){
 		Map<String, String> params = new HashMap<String, String>();
 
 		params.put("name", name);
 		params.put("email", email);
 		params.put("regId", regId);
 
 		String res = "";
 		try{
 			res = executeHTTP(POST, server + "/register.php", params);
 		}
 		catch (Exception e) {
 		}
 
		return res != "succeeded" ? "Registration succeeded!" : res; 
 	}
 
 
 	private static String executeHTTP(final String method, final  String endpoint, final Map<String, String> params) throws InterruptedException, ExecutionException{ 
 		return  new AsyncTask<Void, Void, String>() {
 			@Override
 			protected String doInBackground(Void... prms) {
 
 				if(method.contentEquals(POST))
 					return post(endpoint, params);
 				return null;
 
 			}
 
 			@Override
 			protected void onPostExecute(String msg) {
 			}
 		}.execute(null, null, null).get();
 	}
 
 	// see http://androidsnippets.com/executing-a-http-post-request-with-httpclient
 	private static String post(String endpoint, Map<String, String> params) {
 		// Create a new HttpClient and Post Header
 		String res = "succeed";
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpPost httppost = new HttpPost(endpoint);
 		Log.v("REQUEST", httppost.getRequestLine().toString());
 		Log.v("ENDPOINT", endpoint);
 		try {
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 			// Add your data
 			for (Map.Entry<String, String> entry : params.entrySet()){
 				nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				Log.v("PAIR KEY VALUE", entry.getKey() +" : " + entry.getValue());
 			}
 
 			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 			// Execute HTTP Post Request
 			HttpResponse response = httpclient.execute(httppost);
 
			if(response.getStatusLine().toString() != "200")
 				Log.i("NOAM","Post request has failed to : " + endpoint);
 
 		} catch (ClientProtocolException e) {
 			Log.i("NOAM", "Client Protocol exception: " + e.toString());
 			res = "Client Protocol exception: " + e.toString();
 		} catch (IOException e) {
 			Log.i("NOAM", "IOException: " + e.toString());
 			res = "IOException: " + e.toString();
 		}catch (Exception e) {
 			res = "Exception: " + e.toString();
 		}
 		return res;
 	} 
 }
