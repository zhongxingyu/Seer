 package com.captech.capquake;
 
 import android.app.ListActivity;
 import android.net.Uri;
 import android.os.Bundle;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
 
 import com.captech.capquake.Quake.Quakes;
 
 public class QuakeList extends ListActivity {
 	private static final String FEED_URI = "http://earthquake.usgs.gov/earthquakes/shakemap/rss.xml";
 	
  	@Override
	protected void onStop() {
		super.onStop();
		
		CursorAdapter adapter = (CursorAdapter)this.getListAdapter();
		if (adapter != null)
		{
			adapter.getCursor().close();
			adapter = null;
		}
	}

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
