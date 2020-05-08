 package com.cromiumapps.uwhub;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.cromiumapps.uwhub.OMGUW.SectionFragment;
 import com.cromiumapps.uwhub.OMGUW.SectionsPagerAdapter;
 import com.cromiumapps.uwhub.UWAPIWrapper.UWAPIWrapperListener;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.Toast;
 
 
 public class Events extends FragmentActivity implements UWAPIWrapperListener {
 	
 	//copy pasted from OMGUW-----------------------------------
 	
 	// API Key
     // Adrian's API. But this is to be used to avoid conflicts.
     private String API_KEY;
     
     // UWAPIWrapper
     private static UWAPIWrapper apiWrapper;
     
     ListView lvEventsContent;
     
     // Data and List Adapter
     EventsListAdapter EventsListadapter;
     ArrayList<EventsData> Events = new ArrayList<EventsData>();
     // Others
     private final String LOG_TAG = "Events";
     
     //--------------------------------------------------
     
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_events);
 		
 		// Initialize an API wrapper object using the API key and this activity
 		// as the listener.
 		API_KEY = ((UWHUB) this.getApplication()).getAPI_KEY();
 		apiWrapper = new UWAPIWrapper(API_KEY, this, this);
 		
 		ListView lvEventsContent = (ListView) findViewById(R.id.lvEventsContent);
 		
 		EventsListadapter = new EventsListAdapter(this, Events);
 		lvEventsContent.setAdapter(EventsListadapter);
 		
 		if(savedInstanceState != null){
			API_KEY = ((UWHUB) this.getApplication()).getAPI_KEY();
			apiWrapper = new UWAPIWrapper(API_KEY, this, this);
		
			lvEventsContent = (ListView) findViewById(R.id.lvEventsContent);
		
			EventsListadapter = new EventsListAdapter(this, Events);
 			lvEventsContent.setAdapter(EventsListadapter);
 		} else {
 		    // Call Service
 		    apiWrapper.callService("Events", this);	    
 		}
 		
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onSaveInstanceState(savedInstanceState);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_events, menu);
 		return true;
 	}
 	
 	@Override
     public void onUWAPIRequestComplete(JSONObject jsonObject, boolean success) {
 	
 	try {
 	    JSONObject jsonResponse = jsonObject.getJSONObject("response");
 	    JSONObject jsonData = jsonResponse.getJSONObject("data");
 	    JSONArray jsonEvents = jsonData.getJSONArray("result");
 		
 		for (int j = 0; j < jsonEvents.length(); j++) {
 		    JSONObject EventsObject = jsonEvents.getJSONObject(j);
 		    Events.add(j, new EventsData(EventsObject.getString("ID"), jsonData.getString("Date"), EventsObject.getString("Name"), EventsObject.getString("Links"), EventsObject.getString("Description")));
 		}
 		
 		EventsListadapter.notifyDataSetChanged();
 	   
 		} catch (JSONException e) {
 			Log.v(LOG_TAG, e.getMessage());
 			// For testing
 			Toast.makeText(this, "Throws exception", Toast.LENGTH_SHORT).show();
 		}
 	}
 }
