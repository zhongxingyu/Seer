 package edu.ua.moundville;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 
 import android.app.Activity; import android.util.Log; 
 public class DBHandler {
 	static final String TAG = "DBHandler";
 	static final String URL = "http://betatesting.as.ua.edu/mapexperience/DB-interface.php";
 	static final String CONNECTION = "blah";
 
 	public DBHandler() {}
 
 	/*SendQuery for List - Acceptable formata for Params - 
 	("case", 1) ("lat", num) ("lon", num) - List Artifacts, Sites close to lat, lon
 	("case", 2) ("style", style) - List Artifacts with style tag (string)
 	("case", 3) ("site", Site_ID) - List Artifacts associated with Site ID (int)
 	("case", 4) ("cat", category) - List Artifacts with category tag (string)
 	("case", 5) ("timepd", time_period) - List Artifacts with time_period tag (string)
 	("case", 6) ("art", Artifact_ID) - Record of Artifact with Artifact_ID (int)
 	("case", 7) ("site", Site_ID) - Record of Site with Site_ID (int)
 	("case", 8) - List all Sites */
 	public void sendQuery(Activity activity, ArrayList<NameValuePair> params) {
 		NetworkHelper nHelper = new NetworkHelper(activity, params);
 		new Thread(nHelper).start();
 	}
 
 	public class NetworkHelper implements Runnable {
 		private ArrayList<NameValuePair> params;
 		Activity activity;
 
 		public NetworkHelper(Activity activity, ArrayList<NameValuePair> params) {
 			this.params = params;
 			this.activity = activity;
 		}
 
 		public void run() {
 			String result = "";
 			InputStream is = null;
 			JSONArray jArray = null;
 			
 			try{
 				HttpClient httpclient = new DefaultHttpClient();
 				HttpPost httppost = new HttpPost(URL);
 				httppost.setEntity(new UrlEncodedFormEntity(params));
 				HttpResponse response = httpclient.execute(httppost);
 				HttpEntity entity = response.getEntity();
 				is = entity.getContent();
 			}catch(UnsupportedEncodingException e) {
 				Log.e(TAG, "UnsupportedEncodingException "+e.toString());
 			}catch(ClientProtocolException e) {
 				Log.e(TAG, "ClientProtocolException "+e.toString());
 			}catch(IOException e) {
 				Log.e(TAG, "IO error "+e.toString());
 			}catch(Exception e){
 				Log.e(TAG, "Error in http connection "+e.toString());
 			}
 
 			//convert response to string
 			try{
 				BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
 				StringBuilder sb = new StringBuilder();
 				String line = null;
 				while ((line = reader.readLine()) != null) {
 					sb.append(line + "\n");
 				}
 				is.close();
 
 				result=sb.toString();
 				result = result.split("<query>")[1].split("</query>")[0];
 				
 				jArray = new JSONArray(result);
 
 			}catch(Exception e){
 				Log.e(TAG, "Error converting result "+e.toString());
 			}
 			
			((DBResult) activity).recieveResult(jArray);
 		}
 	}
 	
 	public interface DBResult {
		void recieveResult(JSONArray jArray);
 	}
 }
