 package com.tabbie.android.radar;
 
 import java.io.IOException;
 import java.util.List;
 
import android.app.TabActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TabHost;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TextView;
 
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 
public class RadarActivity extends TabActivity implements OnTabChangeListener {
 
   private static final String LIST_FEATURED_TAG = "Featured";
   private static final String EVENT_TAB_TAG = "Events";
   private static final String RADAR_TAB_TAG = "Radar";
 
   private TabHost tabHost;
   private ListView featuredListView;
   private ListView allListView;
   private ListView radarListView;
   private RadarCommonController commonController;
 
   private ServerThread serverThread;
   
   // FB junk
   private Facebook facebook = new Facebook("217386331697217");
   private SharedPreferences preferences;
   
   private class EventListAdapter extends ArrayAdapter<Event> {
 
     public EventListAdapter(Context context, int resource,
         int textViewResourceId, List<Event> events) {
       super(context, resource, textViewResourceId, events);
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
       if (null == convertView) {
         LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         convertView = inflater.inflate(R.layout.event_list_element, null);
       }
       final Event e = getItem(position);
       TextView title = (TextView) convertView.findViewById(R.id.event_text);
       title.setText(e.name);
       
       ((TextView) convertView.findViewById(R.id.event_list_time)).setText(e.time);
       ((TextView) convertView.findViewById(R.id.event_location)).setText(e.venueName);
       final TextView upVotes = ((TextView) convertView.findViewById(R.id.upvotes));
       upVotes.setText(Integer.toString(e.radarCount));
       
       ImageView img = (ImageView) convertView.findViewById(R.id.event_image);
       try {
         img.setImageDrawable(Drawable.createFromStream(e.image.openStream(),
             "src"));
       } catch (IOException exception) {
         exception.printStackTrace();
       }
       final Button radarButton = (Button) convertView.findViewById(R.id.add_to_radar_image);
       if (e.isOnRadar()) {
         radarButton.setBackgroundResource(R.drawable.radar_button_on);
       } else {
         radarButton.setBackgroundResource(R.drawable.radar_button);
       }
       
       convertView.findViewById(R.id.list_list_element_layout).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
 	        if (null != e) {
 	          Intent intent = new Intent(RadarActivity.this, EventDetailsActivity.class);
 	          intent.putExtra("event", e);
 	          startActivity(intent);
 	        }
         }
       });
       
       convertView.findViewById(R.id.add_to_radar_image).setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
           if (e.isOnRadar() && commonController.removeFromRadar(e)) {
             radarButton.setBackgroundResource(R.drawable.radar_button);
           } else if (!e.isOnRadar() && commonController.addToRadar(e)){
             radarButton.setBackgroundResource(R.drawable.radar_button_on);
           }
           upVotes.setText(Integer.toString(e.radarCount));
          ((EventListAdapter) radarListView.getAdapter()).notifyDataSetChanged();
          ((EventListAdapter) featuredListView.getAdapter()).notifyDataSetChanged();
          ((EventListAdapter) allListView.getAdapter()).notifyDataSetChanged();
         }
       });
       
       convertView.findViewById(R.id.location_image).setOnClickListener(new OnClickListener() {        
         @Override
         public void onClick(View v) {
           Intent intent = new Intent(RadarActivity.this, RadarMapActivity.class);
           intent.putExtra("controller", commonController);
           intent.putExtra("event", e);
           startActivity(intent);
         }
       });
       return convertView;
     }
 
   }
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
 
     featuredListView = (ListView) findViewById(R.id.featured_event_list);
     allListView = (ListView) findViewById(R.id.all_event_list);
     radarListView = (ListView) findViewById(R.id.radar_list);
 
     preferences = getPreferences(MODE_PRIVATE);
     String accessToken = preferences.getString("access_token", null);
     long expires = preferences.getLong("access_expires", 0);
     if (accessToken != null) {
         facebook.setAccessToken(accessToken);
     }
     if (expires != 0) {
         facebook.setAccessExpires(expires);
     }
     
     if(!facebook.isSessionValid()) {
       facebook.authorize(this, new String[] { "email" }, new DialogListener() {
         @Override
         public void onComplete(Bundle values) {
           
           
         }
     
         @Override
         public void onFacebookError(FacebookError error) {}
     
         @Override
         public void onError(DialogError e) {}
     
         @Override
         public void onCancel() {}
       });
     }
     
     commonController = new RadarCommonController();
 
    tabHost = getTabHost();
     tabHost.setup();
     tabHost.setOnTabChangedListener(this);
 
     Filter<Event> featuredOnly = new Filter<Event>() {
       @Override
       public boolean apply(Event o) {
         return o.featured;
       }
     };
 
     featuredListView.setAdapter(new EventListAdapter(this,
         R.id.featured_event_list, R.layout.event_list_element, featuredOnly
             .applyToList(commonController.eventsList)));
     allListView.setAdapter(new EventListAdapter(this, R.id.all_event_list,
         R.layout.event_list_element, commonController.eventsList));
     radarListView.setAdapter(new EventListAdapter(this, R.id.radar_list,
         R.layout.event_list_element, commonController.radar));
     
     findViewById(R.id.map_button).setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View v) {
         Intent intent = new Intent(RadarActivity.this, RadarMapActivity.class);
         intent.putExtra("controller", commonController);
         startActivity(intent);
       }
     });
     
     tabHost.addTab(tabHost.newTabSpec(LIST_FEATURED_TAG)
         .setIndicator(LIST_FEATURED_TAG)
         .setContent(new TabHost.TabContentFactory() {
           public View createTabContent(String arg0) {
             return featuredListView;
           }
         }));
     tabHost.addTab(tabHost.newTabSpec(EVENT_TAB_TAG)
         .setIndicator(EVENT_TAB_TAG)
         .setContent(new TabHost.TabContentFactory() {
           public View createTabContent(String arg0) {
             return allListView;
           }
         }));
     tabHost.addTab(tabHost.newTabSpec(RADAR_TAB_TAG).setIndicator("Radar")
         .setContent(new TabHost.TabContentFactory() {
           public View createTabContent(String arg0) {
             return radarListView;
           }
         }));
 
     // IMPERATIVE to keep this hack here, otherwise app fubars
     tabHost.setCurrentTab(2);
     tabHost.setCurrentTab(1);
     tabHost.setCurrentTab(0);
   }
 
   public void onTabChanged(String tabName) {
     if (tabName.equals(EVENT_TAB_TAG)) {
 
     } else if (tabName.equals(LIST_FEATURED_TAG)) {
 
     } else if (tabName.equals(RADAR_TAB_TAG)) {
 
     }
   }
 
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     facebook.authorizeCallback(requestCode, resultCode, data);
   }
   
   @Override
   public void onResume() {    
     super.onResume();
     facebook.extendAccessTokenIfNeeded(this, null);
   }
 }
