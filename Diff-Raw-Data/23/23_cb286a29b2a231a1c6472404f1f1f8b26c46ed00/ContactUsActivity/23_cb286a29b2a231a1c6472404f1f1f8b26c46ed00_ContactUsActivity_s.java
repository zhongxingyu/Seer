 package com.example.careapp;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class ContactUsActivity extends Activity {
 	
 	ContactUsAdapter adapter;
 	ArrayList<HashMap<String, String>> contactUsMap;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_contact_us);
 		
 		contactUsMap = populateList();
 		ListView listView = (ListView) findViewById(R.id.contactUsListView);				
 		adapter = new ContactUsAdapter(this, contactUsMap);	
 
 		listView.setAdapter(adapter);
 		this.registerForContextMenu(listView);
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			
 			
 			/**
 			 * Pop up a context menu when an item in the contact us person list is clicked.
 			 * The context menu will allow the user to call or email the clicked contact.
 			 */
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View view, int pos, long id) {
 				ContactUsActivity.this.openContextMenu(view);
 			}
 
 		});
 		
 		
 		
 		// Show the Up button in the action bar.
 		setupActionBar();
 	}
 	
 	
 	/**
 	 * Method is called when an item in the contact us person list is clicked.
 	 * It generates a context menu with two options: Call or email.
 	 */
 	@Override
 	public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenuInfo contextMenuInfo) {
 		super.onCreateContextMenu(contextMenu, view, contextMenuInfo);
 		AdapterContextMenuInfo adapterContextMenuInfo = (AdapterContextMenuInfo) contextMenuInfo;
 
 		if (adapter != null) {
 			contextMenu.setHeaderTitle("Options");
 			contextMenu.add(adapterContextMenuInfo.position, 1, 1, "Call");
 			contextMenu.add(adapterContextMenuInfo.position, 2, 2, "Email");
 		}
 
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem menuItem) {
 	    int listId = menuItem.getGroupId();
 	    if (menuItem.getItemId() == 1) { //needs some work
 	    	try {
 	    		//need to validate telephone, Uri doesn't throw an exception
 	    		Uri tele = Uri.parse("tel:" + contactUsMap.get(listId).get("phone"));
 	    		if (tele == null || (tele == Uri.EMPTY)) {
 	    			throw new Exception("Error: Invalid phone number.");
 	    		}
 	    		startActivity(new Intent(Intent.ACTION_CALL, tele)); 	             
 	    	} catch (Exception e) {
 	    		Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
 	    	}
 	    } else if (menuItem.getItemId() == 2) {
 
 	    	try {
 	    		//need to validate webpage, Uri doesn't throw an exception
 	    		Uri url = Uri.parse(contactUsMap.get(listId).get("email"));
 	    		if (url == null || (url == Uri.EMPTY)) {
 	    			throw new Exception("Error: Invalid url.");
 	    		}
 	    		
 	    		//Intent i = new Intent(Intent.ACTION_SEND);
 	    		//i.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ "dascenzd@go.stockton.edu" });
 	    		//i.putExtra(android.content.Intent.EXTRA_SUBJECT, "testing");
 	    		//i.putExtra(android.content.Intent.EXTRA_TEXT, "This is a test");
 	    		//startActivity(Intent.createChooser(i, "Send email"));
 	    		
 	    		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
 	    		emailIntent.setType("plain/text");
	    		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ "dascenzd@go.stockton.edu" });
 	    		startActivity(emailIntent);
 	    		
 	    	} catch (Exception e) {
 	    		Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
 	    	}
 	    }
 	    return true;
 	}
 	
 	/**
 	 * Only 3 people in list, so it's easier just to manually create each
 	 * @return
 	 */
 	private ArrayList<HashMap<String, String>> populateList() {
 		
 		ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String,String>>();
 		
 		HashMap<String, String> entry1 = new HashMap<String, String>();
 		entry1.put("name", "Tomas Itaas");
 		entry1.put("phone", "609-652-4407");
 		entry1.put("email", "Tomas.Itaas@stockton.edu");
 		entry1.put("room", "F  110");
 		data.add(entry1);
 		
 		HashMap<String, String> entry2 = new HashMap<String, String>();
 		entry2.put("name", "Jason Babin");
 		entry2.put("phone", "609-652-3419");
 		entry2.put("email", "Jason.Babin@stockton.edu");
 		entry2.put("room", "F  107");
 		data.add(entry2);
 		
 		HashMap<String, String> entry3 = new HashMap<String, String>();
 		entry3.put("name", "Mikhil Pandit");
 		entry3.put("phone", "609-626-6061");
 		entry3.put("email", "Mikhil.Pandit@stockton.edu");
 		entry3.put("room", "F  107");
 		data.add(entry3);
 		
 		return data;
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.contact_us, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
