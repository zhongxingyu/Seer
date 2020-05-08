 package com.jeremyhaberman.playgrounds;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
import android.sax.StartElementListener;
 import android.util.Log;
 
 public class WebPlaygroundDAO extends Activity implements PlaygroundDAO {
 
 	private static final String TAG = "WebPlaygroundDAO";
 	private Collection<Playground> playgrounds;
 	private Playgrounds swingset;
 	private Context context;
 
 	WebPlaygroundDAO(Playgrounds swingset) {
 		this.swingset = swingset;
 	}
 
 	WebPlaygroundDAO(Context context) {
 		this.context = context;
 	}
 
 	@Override
 	public int createPlayground(String name, String description, int latitude,
 			int longitude) {
 
 		int result = 0;
 
 		// Create a new HttpClient and Post Header
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpPost httppost = new HttpPost(
 				"http://swingsetweb.appspot.com/playground");
 
 		try {
 			// Add your data
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 			nameValuePairs.add(new BasicNameValuePair("name", name));
 			nameValuePairs.add(new BasicNameValuePair("description",
 					description));
 			nameValuePairs.add(new BasicNameValuePair("latitude", Integer
 					.toString(latitude)));
 			nameValuePairs.add(new BasicNameValuePair("longitude", Integer
 					.toString(longitude)));
 			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 			// Execute HTTP Post Request
 			HttpResponse response = httpclient.execute(httppost);
 
 			StatusLine status = response.getStatusLine();
 			int statusCode = status.getStatusCode();
 
 			if (statusCode != 200) {
 				result = statusCode;
 			}
 
 		} catch (ClientProtocolException e) {
 			result = -1;
 		} catch (IOException e) {
 			result = -1;
 		}
 
 		return result;
 	}
 
 	@Override
 	public boolean deletePlayground(Context context, int id) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public Collection<Playground> getAll(Context context) {
 //		synchronized (Swingset.initPlaygroundLock) {
 			playgrounds = new ArrayList<Playground>();
 			String result = swingset.getResources().getString(R.string.error);
 			HttpURLConnection httpConnection = null;
 			Log.d(TAG, "getPlaygrounds()");
 
 			try {
 				// Check if task has been interrupted
 				if (Thread.interrupted()) {
 					throw new InterruptedException();
 				}
 
 				// Build query
 				URL url = new URL("http://swingsetweb.appspot.com/playground");
 				httpConnection = (HttpURLConnection) url.openConnection();
 				httpConnection.setConnectTimeout(15000);
 				httpConnection.setReadTimeout(15000);
 				StringBuilder response = new StringBuilder();
 
 				if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
 					// Read results from the query
 					BufferedReader input = new BufferedReader(
 							new InputStreamReader(
 									httpConnection.getInputStream(), "UTF-8"));
 					String strLine = null;
 					while ((strLine = input.readLine()) != null) {
 						response.append(strLine);
 					}
 					input.close();
 
 				}
 
 				// Parse to get translated text
 				JSONArray jsonPlaygrounds = new JSONArray(response.toString());
 				int numOfPlaygrounds = jsonPlaygrounds.length();
 
 				JSONObject jsonPlayground = null;
 
 				for (int i = 0; i < numOfPlaygrounds; i++) {
 					jsonPlayground = jsonPlaygrounds.getJSONObject(i);
 					playgrounds.add(toPlayground(jsonPlayground));
 				}
 
 			} catch (Exception e) {
 				Log.e(TAG, "Exception", e);
 				Intent errorIntent = new Intent(context, Playgrounds.class);
 				errorIntent.putExtra("Exception", e.getLocalizedMessage());
 				errorIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				context.startActivity(errorIntent);
 			} finally {
 				if (httpConnection != null) {
 					httpConnection.disconnect();
 				}
 			}
 
 			// all done
 			Log.d(TAG, "   -> returned " + result);
 			return playgrounds;
 //		}
 	}
 
 	protected void setPlaygrounds(Collection<Playground> playgrounds) {
 		this.playgrounds = playgrounds;
 	}
 
 	private Playground toPlayground(JSONObject jsonPlayground)
 			throws JSONException {
 		String name = jsonPlayground.getString("name");
 		String description = jsonPlayground.getString("description");
 		int latitude = jsonPlayground.getInt("latitude");
 		int longitude = jsonPlayground.getInt("longitude");
 
 		return new Playground(name, description, latitude, longitude);
 	}
 }
