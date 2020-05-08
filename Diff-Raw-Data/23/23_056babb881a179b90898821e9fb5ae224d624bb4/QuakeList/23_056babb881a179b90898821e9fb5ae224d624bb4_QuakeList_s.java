 package com.captech.capquake;
 
 import android.app.ListActivity;
 import android.net.Uri;
 import android.os.Bundle;
 
 import com.captech.capquake.Quake.Quakes;
 
 public class QuakeList extends ListActivity {
 	private static final String FEED_URI = "http://earthquake.usgs.gov/earthquakes/shakemap/rss.xml";
 	
    /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.quake_list);
         setListAdapter(Adapters.loadCursorAdapter(this, R.xml.quake, Quakes.CONTENT_URI + "?url="
         		+ Uri.encode(FEED_URI) + "&reset=" + (savedInstanceState == null)));
         
         getListView().setOnItemClickListener(new UrlIntentListener());
     }
 }
