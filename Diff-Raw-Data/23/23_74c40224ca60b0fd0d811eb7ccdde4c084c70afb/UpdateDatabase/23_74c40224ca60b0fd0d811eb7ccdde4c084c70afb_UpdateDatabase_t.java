 package com.net.rmopenmenu;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonStreamParser;
 
 public class UpdateDatabase extends AsyncTask<String, Integer, String> {
 	
 	private final int CONNECTION_TIMEOUT = 6000;
 	private final int SOCKET_TIMEOUT = 6000;
 	private Context context;
 	
 	public UpdateDatabase(Context context) {
 		this.context = context;
 	}
 
 	@Override
 	protected String doInBackground(String... params) {
 		return load(params[0]);
 	}
 	
 	protected void onProgressUpdate(Integer... progress) {
     }
 
     protected void onPostExecute(String result) {
 
 	}
     
     private HttpClient createHttpClient() {
 		HttpParams params = new BasicHttpParams();
 
 		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 		HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
 		HttpProtocolParams.setUseExpectContinue(params, true);
 
 		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
 		HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);
 
 		SchemeRegistry schReg = new SchemeRegistry();
 		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 		schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
 		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
 
 		return new DefaultHttpClient(conMgr, params);
 	}
 
 	private String Post(String url, List<BasicNameValuePair> nameValuePairs) {
 
 		// Initialize input stream and response variables
 		InputStream iStream = null;
 		String data = "";
 
 		try {
 			HttpPost httppost = new HttpPost("http://www.project-fin.org/openmenu/sync.php");
 
 			// Process the response from the server 
 			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpClient httpClient = createHttpClient();
 			HttpResponse httpResponse = httpClient.execute(httppost);
 
 			HttpEntity entity = httpResponse.getEntity();
 			iStream = entity.getContent();
 		} catch(Exception e) {
 			if (e != null) {
 				Log.e("log_tag", "Error in http connection " + e.toString());
 			}
 		}
 
 		// Convert server's response to a String
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(iStream,"iso-8859-1"),8);
 			StringBuilder sb = new StringBuilder();
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + "\n");
 			}
 			iStream.close();
 
 			data = sb.toString();
 		} catch(Exception e) {
 			if (e != null) {
 				Log.e("log_tag", "Error converting result " + e.toString());
 			}
 		}
 
 		return data.trim();
 	}
 	
 	public String load(String url) {
 		// Initialize the array of name value pairs
 		List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
     	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		String timestamp = prefs.getString("lastOpened", "1330910642");
 
 		nameValuePairs.add(new BasicNameValuePair("timestamp", timestamp));
 		String result = Post(url, nameValuePairs);
 		//used for parsing the JSON object
 		JsonStreamParser parser = new JsonStreamParser(result);
 		
 		SQLiteDatabase db = new Database(context).getWritableDatabase();
 
		if (parser.hasNext()) {
 			JsonArray arr = parser.next().getAsJsonArray();
 	
 			for (int i = 0; i < arr.size(); i++)
 			{
 				if (arr.get(i).isJsonObject())
 				{
 					//Since the JsonArray contains whole bunch json array, we can get each one out
 					JsonObject ob = arr.get(i).getAsJsonObject();
 	
 					// Grab the stuff
 					int rid = ob.get("rid").getAsInt();
 					String name = ob.get("name").getAsString();
 					String address = ob.get("address").getAsString();
 					String city = ob.get("city").getAsString();
 					String state = ob.get("state").getAsString();
 					String country = ob.get("country").getAsString();
 					int lat = ob.get("lat").getAsInt();
 					int lon = ob.get("lon").getAsInt();
 					
 					db.execSQL("INSERT OR REPLACE INTO restaurants (rid, name, address, city, state, country, lat, lon) VALUES (" + 
 													  rid + ", '" + name + "', '" + address + "', '" + city + "', '" + state + "', '" + country + "', " + lat + ", " + lon + ")");
 				}
 			}
 			
 			arr = parser.next().getAsJsonArray();
 			
 			for (int i = 0; i < arr.size(); i++)
 			{
 				if (arr.get(i).isJsonObject())
 				{
 					//Since the JsonArray contains whole bunch json array, we can get each one out
 					JsonObject ob = arr.get(i).getAsJsonObject();
 	
 					// Grab the stuff
 					int iid = ob.get("iid").getAsInt();
 					String name = ob.get("name").getAsString();
 					String description = ob.get("description").getAsString();
 					String price = ob.get("price").getAsString();
 					int veg = ob.get("veg").getAsInt();
 					
 					db.execSQL("INSERT OR REPLACE INTO items (iid, name, description, price, veg) VALUES (" + 
 													  iid + ", '" + name + "', '" + description + "', '" + price + "', " + veg + ")");
 				}
 			}
 			
 			arr = parser.next().getAsJsonArray();
 			
 			for (int i = 0; i < arr.size(); i++)
 			{
 				if (arr.get(i).isJsonObject())
 				{
 					//Since the JsonArray contains whole bunch json array, we can get each one out
 					JsonObject ob = arr.get(i).getAsJsonObject();
 	
 					// Grab the stuff
 					int rid = ob.get("rid").getAsInt();
 					int iid = ob.get("iid").getAsInt();
 					
 					db.execSQL("INSERT OR REPLACE INTO restaurants_items (rid, iid) VALUES (" + 
 													  rid + ", " + iid + ")");
 				}
 			}
 		}
 		
 	    SharedPreferences.Editor editor = prefs.edit();
 		editor.putString("lastOpened", System.currentTimeMillis() / 1000 + "");
 		editor.commit();
 				
 		db.close();
 		
 		return "";
 	}
 
 }
