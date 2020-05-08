 package com.captech.capquake;
 
 import com.captech.capquake.Quake.Quakes;
 import com.captech.capquake.Quake.RssQuakes;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.net.Uri;
 import android.os.Bundle;
 import android.widget.CursorAdapter;
 
 public class QuakeList extends ListActivity {
 	private static final String FEED_URI = "http://earthquake.usgs.gov/earthquakes/shakemap/rss.xml";
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.quake_list);
        setListAdapter(Adapters.loadCursorAdapter(this, 0, Quakes.CONTENT_URI + "?url="
         		+ Uri.encode(FEED_URI) + "&reset=" + (savedInstanceState == null)));
     }
 }
