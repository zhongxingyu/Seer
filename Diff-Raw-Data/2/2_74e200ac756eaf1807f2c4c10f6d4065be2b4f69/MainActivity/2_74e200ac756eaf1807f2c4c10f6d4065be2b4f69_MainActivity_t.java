 package com.example.soapbox;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.soapbox.DisplayShoutListTask.ShoutListCallbackInterface;
 
 
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
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		//Refresh shout list on MainActivity creation
 		View v = (View)findViewById(R.layout.activity_main);
 		refreshShouts(v);
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		retrieveUserInfo();
 		this.invalidateOptionsMenu();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) 
 	{
 		prefs = this.getSharedPreferences("com.example.soapbox", Context.MODE_PRIVATE);
 
 		Boolean loggedIn = prefs.getBoolean(LoginTask.LOGINSTATUSKEY, false);
 
 		MenuItem usernameItem =  menu.findItem(R.id.main_menu_change_username);
 		MenuItem locationItem =  menu.findItem(R.id.main_menu_change_location);
 		MenuItem signOutItem = menu.findItem(R.id.main_menu_sign_out);
 		MenuItem loginActionItem =  menu.findItem(R.id.main_menu_action_login);
 		MenuItem postShoutActionItem =  menu.findItem(R.id.main_menu_action_post);
 		if(!loggedIn)
 		{
 			usernameItem.setVisible(false);
 			locationItem.setVisible(false);
 			signOutItem.setVisible(false);
 			postShoutActionItem.setVisible(false);
 			loginActionItem.setVisible(true);
 		}
 		else
 		{
 			usernameItem.setVisible(true);
 			locationItem.setVisible(true);
 			signOutItem.setVisible(true);
 			postShoutActionItem.setVisible(true);
 			loginActionItem.setVisible(false);
 		}
 		return true;
 	}
 
 	//gets the user's info from sharedprefs
 	public void retrieveUserInfo()
 	{
 		//Get the user's location from shared prefs if it is stored there
 		prefs = this.getSharedPreferences("com.example.soapbox", Context.MODE_PRIVATE);
 
 		location = prefs.getString(LoginTask.TAG, LoginTask.DEFAULT_TAG_VALUE);
 		username = prefs.getString(LoginTask.NAME, null);
 		Boolean loggedIn = prefs.getBoolean(LoginTask.LOGINSTATUSKEY, false);
 
 		TextView usernameLabel = (TextView) findViewById(R.id.username_label_register);
 		if(!loggedIn)
 		{
 			usernameLabel.setText("Please sign in");	
 			prefs.edit().putBoolean(LoginTask.LOGINSTATUSKEY, false).commit();
 		}
 		else
 		{
 			usernameLabel.setText("Welcome, " + username);
 		}
 		System.out.println("Location: " + location);
 	}
 
 	//called when Post button is clicked
 	public void makePost(View view) 
 	{
 		Intent intent = new Intent(this, PostShoutActivity.class);
 		startActivity(intent);		
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) 
 	{
 		View v = (View)findViewById(R.layout.activity_main);
 		switch (item.getItemId()) 
 		{
 		case R.id.main_menu_change_username:
 			changeUsername(v);
 			break;
 		case R.id.main_menu_change_location:
 			changeLocation(v);
 			break;
 		case R.id.main_menu_sign_out:
 			openLogin(v);
 			break;
 		case R.id.main_menu_action_refresh:
 			refreshShouts(v);
 			break;
 		case R.id.main_menu_action_post:
 			makePost(v);
 			break;
 		case R.id.main_menu_action_login:
 			openLogin(v);
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 
 	public void changeUsername(View view)
 	{
 		// custom dialog
 		final Dialog dialog = new Dialog(this);
 		dialog.setContentView(R.layout.change_username);
 		dialog.setTitle("Change Username");
 
 		// set the custom dialog components - text, image and button
 		final TextView text = (TextView) dialog.findViewById(R.id.change_username_textbox);
 
 		Button dialogButton = (Button) dialog.findViewById(R.id.change_username_ok);
 		// if button is clicked, close the custom dialog
 		dialogButton.setOnClickListener(new OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				username = text.getText().toString();
 				System.out.println(username);
 				dialog.dismiss();
 			}
 		});
 
 		Button cancelButton = (Button) dialog.findViewById(R.id.change_username_cancel);
 		cancelButton.setOnClickListener(new OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) {dialog.dismiss();}
 		});
 
 		dialog.show();
 	}
 
 	public void changeLocation(View view)
 	{
 		// custom dialog
 		final Dialog dialog = new Dialog(this);
 		dialog.setContentView(R.layout.change_location);
 		dialog.setTitle("Change Location");
 
 		final HashMap<String,String> m = Locations.constructCityMap();
 
 		final Spinner spinner = (Spinner)dialog.findViewById(R.id.change_location_spinner);
 		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, Locations.cityNames);
 		spinner.setAdapter(spinnerArrayAdapter);
 
 		Button okButton = (Button) dialog.findViewById(R.id.change_location_ok);
 		okButton.setOnClickListener(new OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				String spinnerText = spinner.getSelectedItem().toString();
 				location = m.get(spinnerText);
 				System.out.println(location);
 				dialog.dismiss();
 			}
 		});
 
 		Button cancelButton = (Button) dialog.findViewById(R.id.change_location_cancel);
 		cancelButton.setOnClickListener(new OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) {dialog.dismiss();}
 		});
 
 		dialog.show();
 	}
 
 	//called when Login button is clicked
 	public void openLogin(View view) 
 	{
 		prefs = this.getSharedPreferences("com.example.soapbox", Context.MODE_PRIVATE);
 		Boolean loggedIn = prefs.getBoolean(LoginTask.LOGINSTATUSKEY, false);
 
 		//If no user is logged in
 		if(!loggedIn)
 		{
 			Intent intent = new Intent(this, LoginActivity.class);
 			startActivity(intent);
 		}
 		//Else user is logged in
 		else
 		{
 			prefs = this.getSharedPreferences("com.example.soapbox", Context.MODE_PRIVATE);
 			prefs.edit().clear().commit(); //Delete all sharedprefs
 
 			prefs.edit().putBoolean(LoginTask.LOGINSTATUSKEY, false).commit();
 
 			retrieveUserInfo();
 			this.invalidateOptionsMenu();	//Reset Action bar
 			View v = (View)findViewById(R.layout.activity_main);
 			refreshShouts(v);
 		}
 	}
 
 	public void refreshShouts(View view)
 	{
 		String url = HOSTNAME + SHOUTS;
 		String method = DisplayShoutListTask.GET;
 		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		retrieveUserInfo(); //Get the latest values
 
 		//If the location tag is not global
		if(!location.equals(LoginTask.DEFAULT_TAG_VALUE))
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
 				@SuppressWarnings("unchecked")
 				HashMap<String, String> o = (HashMap<String, String>) listView.getItemAtPosition(position);	        		
 				Toast.makeText(MainActivity.this, "ID '" + o.get("id") + "' was clicked.", Toast.LENGTH_SHORT).show(); 
 
 			}
 		});
 	}
 }
