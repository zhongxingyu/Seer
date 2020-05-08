 package se.chalmers.h_sektionen;
 
import android.os.Bundle;
 import java.util.ArrayList;
 import java.util.List;
 
 
 import se.chalmers.h_sektionen.utils.*;
 
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
>>>>>>> events
 
 public class EventsActivity extends BaseActivity {
 
 	@Override
 	protected void onResume() {
 
 		setCurrentView(MenuItems.EVENTS);
 		createEventsView();
 		
 		super.onResume();
 	}
     
 	private void createEventsView(){
 
 		getFrameLayout().removeAllViews();
 		getFrameLayout().addView(getLayoutInflater().inflate(R.layout.view_events, null));
 		
 		
 		//setContentView(R.layout.view_events);
 
 		
 		//TextView txt = (TextView) findViewById(R.id.title);  
 		//Typeface font = Typeface.createFromAsset(getAssets(), "Chantelli_Antiqua.ttf");  
 		//txt.setTypeface(font);  
 		
     	try {
     		
     		ListView eventsFeed = (ListView) findViewById(R.id.events_feed);
     		//ArrayAdapter<String> feedAdapter = new ArrayAdapter<String>(this, R.layout.events_feed_item, new LoadEvents().execute().get());
 			List<Events> events = new ArrayList<Events>();
 			
 			events = new LoadEvents().execute().get();
 			
 			
     		ArrayAdapter<Events> feedAdapter = new EventsArrayAdapter(this, R.layout.events_feed_item, events);
     		eventsFeed.setAdapter(feedAdapter);
     		
     		 // Creating an item click listener, to open/close our toolbar for each item
             eventsFeed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                 public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
 
                     View toolbar = view.findViewById(R.id.toolbarEvents);
 
                     // Creating the expand animation for the item
                     ExpandAnimation expandAni = new ExpandAnimation(toolbar, 500);
 
                     // Start the animation on the toolbar
                     toolbar.startAnimation(expandAni);
                 }
             });
     	}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
     	
     	 
 	}
 		
 
 }
