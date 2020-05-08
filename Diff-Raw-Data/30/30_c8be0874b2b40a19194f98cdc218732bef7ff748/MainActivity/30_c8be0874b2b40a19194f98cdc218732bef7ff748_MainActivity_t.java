 package com.example.soapbox;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Stack;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.example.soapbox.DisplayShoutListTask.ShoutListCallbackInterface;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements ShoutListCallbackInterface
 {
 	public static final String HOSTNAME = "http://acx0.dyndns.org:3000/";
 	public static final String SHOUTS = "shouts";
 	public static final String SLASH = "/";
 	
 	SharedPreferences prefs;
 	JSONArray shoutArray = null;
 	String username = null;
 	String location = null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
		//retrieveUserInfo();
 		
 		//Refresh shout list on MainActivity creation
 		View v = (View)findViewById(R.layout.activity_main);
 		refreshShouts(v);
 	}
 
 	@Override
	public void onResume()
	{
		super.onResume();
		retrieveUserInfo();
	}
	
	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	//gets the user's info from sharedprefs
 	public void retrieveUserInfo()
 	{
 		//Get the user's location from shared prefs if it is stored there
 		prefs = this.getSharedPreferences("com.example.soapbox", Context.MODE_PRIVATE);
 		
 		location = prefs.getString(LoginTask.TAG, LoginTask.DEFAULT_TAG_VALUE);
 		username = prefs.getString(LoginTask.NAME, null);
 		
 		TextView usernameLabel = (TextView) findViewById(R.id.username_label_register);
		Button button = (Button) findViewById(R.id.post_button);
 		if(username == null)
 		{
			usernameLabel.setText("Please sign in");	
			button.setVisibility(Button.GONE);
			button = (Button)findViewById(R.id.login_button);
			button.setVisibility(Button.VISIBLE);
 		}
 		else
 		{
 			usernameLabel.setText("Welcome, " + username);
			button.setVisibility(Button.VISIBLE);
			button = (Button)findViewById(R.id.login_button);
			button.setVisibility(Button.GONE);
 		}
 //		location = LoginTask.DEFAULT_TAG_VALUE;
 		System.out.println("Location: " + location);
 	}
 	
 	//called when Post button is clicked
 	public void makePost(View view) {
 		Intent intent = new Intent(this, PostShoutActivity.class);
 		startActivity(intent);		
 	}
 	
 	//called when Login button is clicked
 	public void openLogin(View view) {
 		Intent intent = new Intent(this, LoginActivity.class);
 		startActivity(intent);
 	}
 	
 	public void refreshShouts(View view)
 	{
 		String url = HOSTNAME + SHOUTS;
 		String method = DisplayShoutListTask.GET;
 		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 		
 		retrieveUserInfo(); //Get the latest values
 		
 		//If the location tag is not global
 		if(location != LoginTask.DEFAULT_TAG_VALUE)
 		{
 			BasicNameValuePair tag = new BasicNameValuePair(LoginTask.TAG, location);
 			params.add(tag);
 		}
 		
 		System.out.println("Pre Execute");
 		
 		DisplayShoutListTask t = new DisplayShoutListTask(url,method,params,this,this);
 		t.execute();
 		
 		System.out.println("Post Execute");
 	}
 
 	@Override
 	public void onRequestComplete(JSONArray result) 
 	{
 		System.out.println("Complete");
 		System.out.println(result);
 		shoutArray = result;
 		
 		LinkedList<HashMap<String, String>> list = new LinkedList<HashMap<String,String>>();
 		try 
 		{
 			for(int i=0; i<shoutArray.length(); i++)
 			{
 				JSONObject o = shoutArray.getJSONObject(i);
 				HashMap<String, String> map = new HashMap<String, String>();
 				
 				map.put(DisplayShoutListTask.ID, o.getString(DisplayShoutListTask.ID));
 				map.put(DisplayShoutListTask.NAME, o.getString(DisplayShoutListTask.NAME));
 				map.put(DisplayShoutListTask.TAG, o.getString(DisplayShoutListTask.TAG));
 				map.put(DisplayShoutListTask.MESSAGE, o.getString(DisplayShoutListTask.MESSAGE));
 				list.addFirst(map);
 			}
 		} 
 		catch (JSONException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		final ListView listView = (ListView) findViewById(R.id.list);
 
 		// get data from the table by the ListAdapter
 		ListAdapter adapter = new com.example.soapbox.ListAdapter
 				(this, list , R.layout.shout_list_component,
 				new String[] {DisplayShoutListTask.MESSAGE},
 				new int[] { R.id.message_component });
 
 		listView.setAdapter(adapter);
 		
 		listView.setOnItemClickListener(new OnItemClickListener() 
 		{
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
 			{
 //				@SuppressWarnings("unchecked")
 				HashMap<String, String> o = (HashMap<String, String>) listView.getItemAtPosition(position);	        		
 				Toast.makeText(MainActivity.this, "ID '" + o.get("id") + "' was clicked.", Toast.LENGTH_SHORT).show(); 
 
 			}
 		});
 	}
 }
