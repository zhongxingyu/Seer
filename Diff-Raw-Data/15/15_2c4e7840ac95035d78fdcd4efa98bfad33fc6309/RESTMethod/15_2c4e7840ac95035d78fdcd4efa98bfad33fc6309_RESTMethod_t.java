 package ch.good2go.restful;
 
 import java.io.IOException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import android.util.Log;
 
 public class RESTMethod {
 	
 	private static String TAG = "RestMethod";
 	
 	public static String get(String url){
 		HttpGet g = new HttpGet(url);
 		HttpEntity entity;
 		try {
 			entity = new DefaultHttpClient().execute(g).getEntity();
 			String response = EntityUtils.toString(entity);
 			Log.i(TAG, response);
 			return response;
 		} catch (ClientProtocolException e) {
			Log.i(TAG, "HTTP-Request GET to:" + url + " failed.");
 		} catch (IOException e) {
			Log.i(TAG, "HTTP-Request GET to:" + url + " failed.");
 		}	
 		return "";
 	}
 
 	public static void post(String url, String json) {
 		HttpPost post = new HttpPost(url); 
 		post.setHeader("Accept", "application/json");
 		post.setHeader("Content-type", "application/json");
 		DefaultHttpClient client = new DefaultHttpClient();
 		try {
 			StringEntity se = new StringEntity(json);
 			post.setEntity(se);
 			client.execute(post);
 		} catch (ClientProtocolException e) {
			Log.i(TAG, "HTTP-Request POST to:" + url + " failed.");
 		} catch (IOException e) {
			Log.i(TAG, "HTTP-Request POST to:" + url + " failed.");
 		}
 	}
 }
