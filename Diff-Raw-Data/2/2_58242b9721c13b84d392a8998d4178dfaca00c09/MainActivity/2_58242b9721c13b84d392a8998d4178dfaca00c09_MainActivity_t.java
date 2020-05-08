 package com.wap.battle.client;
 
 import android.app.Activity;
 import android.content.*;
 import android.os.*;
 import android.provider.Settings.Secure;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 import java.io.*;
 import java.util.*;
 import org.apache.http.*;
 import org.apache.http.client.*;
 import org.apache.http.client.methods.*;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.*;
 
 public class MainActivity extends Activity implements AdapterView.OnItemClickListener {	
 	private static final String LOG_TAG = MainActivity.class.getSimpleName(); // For Log.i() LogCat output.
 	
 	private ListView listMonster; // UI elements.
 	
 	protected void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		Log.i(LOG_TAG, "onCreate()");
 		setContentView(R.layout.activity_main);
 
 		listMonster = (ListView) findViewById(R.id.listMonster);
 		listMonster.setOnItemClickListener(this);
 		new DeviceFindByIdTask().execute(Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
 	}
 	
 	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
 		Log.i(LOG_TAG, "onItemClick()");
 		Monster monster = (Monster) listMonster.getItemAtPosition(pos);
 		Intent intent = new Intent(view.getContext(), MonsterActivity.class);
 		intent.putExtra(Monster.NAME, monster);
 		startActivity(intent);
 	}
 	
 	/**
 	 * 
 	 */
 	public String getJSON(String uri) {
 		StringBuilder builder = new StringBuilder();
 		HttpClient client = new DefaultHttpClient();
 		HttpGet req = new HttpGet(uri);
 		try {
 	    	HttpResponse res = client.execute(req);
 	    	StatusLine line = res.getStatusLine();
 	    	int code = line.getStatusCode();
 	    	if (code == HttpStatus.SC_OK) {
 	    		HttpEntity entity = res.getEntity();
 	    		InputStream content = entity.getContent();
 	    		BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 	    		String tmp;
 	    		while ((tmp = reader.readLine()) != null) {
 	    			builder.append(tmp);
 	    		}
 	    	} else {
 	    		Log.e(LOG_TAG, "Error donwloading " + uri);
 	    	}
 	    } catch (Exception e) {
 	    	e.printStackTrace();
 	    }
 		return builder.toString();
 	}
 	
	public class DeviceFindByIdTask extends AsyncTask<String, Integer, String> {	
 		protected String doInBackground(String... params) {
 			Log.i(LOG_TAG, "doInBackground()");
 			String remoteUri = Cake.BASE + Cake.DS
 				+ "devices" + Cake.DS // Controller, model, action, Device.id, JSON suffix.
 				+ "findById" + Cake.DS
 				+ params[0] + ".json";
 			return getJSON(remoteUri);
 		}
 		
 		protected void onPostExecute(String result) {
 			Log.i(LOG_TAG, "onPostExecute()");
 			try {
 				JSONObject top = new JSONObject(result); // Topmost level container.
 				Log.i(LOG_TAG, top.toString());
 				Iterator<?> foo = top.keys();
 				while (foo.hasNext()) {
 					String i = (String) foo.next();
 					if (i.equals("items")) { 
 						JSONObject items = top.getJSONObject(i);
 						Iterator<?> bar = items.keys();
 						while (bar.hasNext()) {
 							String j = (String) bar.next();
 							if (j.equals("Device")) { // Skip Device{} parsing.
 								continue;
 							}
 							
 							JSONArray monsters = items.getJSONArray(j);
 							if (monsters.length() <= 0) {
 								Toast.makeText(getApplicationContext(), "No monster(s) read.", Toast.LENGTH_SHORT).show();
 							}
 							List<Monster> list = new ArrayList<Monster>();
 							Log.i(LOG_TAG, "Read " + monsters.length() + " monster(s).");
 							for (int k=0; k<monsters.length(); k++) {
 								list.add(new Monster(monsters.getJSONObject(k)));
 							}
 							ArrayAdapter<Monster> adapter = new ArrayAdapter<Monster>(
 								getApplicationContext(),
 								android.R.layout.simple_list_item_single_choice,
 								list
 							);
 							listMonster.setAdapter(adapter);
 						}
 					}
 					if (i.equals("message")) { // Display as a Toast.
 						String s = top.getString(i);
 						Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
