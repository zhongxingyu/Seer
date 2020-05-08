 package com.tabbie.android.radar;
 
 import java.net.URL;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ListView;
 import android.widget.TabHost;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TabHost.TabSpec;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.facebook.android.Facebook;
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 import com.tabbie.android.radar.MultiSpinner.MultiSpinnerListener;
 import com.tabbie.android.radar.http.ServerGetRequest;
 import com.tabbie.android.radar.http.ServerResponse;
 
 public class RadarActivity extends ServerThreadActivity implements
     OnTabChangeListener, OnItemClickListener, OnItemLongClickListener {
   
   // Intent constants
   private static final String[] FOUNDERS_EMAIL = {"founders@tonight-life.com"};
   private static final String APP_FEEDBACK_SUBJECT = "TonightLife Application Feedback";
   private static final int REQUEST_LOGIN = 42;
 
   // Often-used views
   private TabHost tabHost;
   private ListView currentListView;
   private ListView featuredListView;
   private ListView allListView;
   private ListView radarListView;
   private TextView myNameView;
 
   // Internal state for views
   private String tabbieAccessToken = null;
   private int currentViewPosition = 0;
 
   // Controllers
   private RadarCommonController commonController;
 
   // FB junk
   private final Facebook facebook = new Facebook("217386331697217");
   private SharedPreferences preferences;
   
   // Google analytics
   private GoogleAnalyticsTracker googleAnalyticsTracker;
   
   
   @Override
   public void onCreate(final Bundle savedInstanceState) {
 	  
 	// Set initial conditions
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
     Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
     commonController = new RadarCommonController();
 
     // Start Google Analytics
     googleAnalyticsTracker = GoogleAnalyticsTracker.getInstance();
     googleAnalyticsTracker.startNewSession("UA-34193317-1", this);
     googleAnalyticsTracker.trackPageView(this.getClass().getName());
     
     // Create view references
     currentListView = featuredListView = (ListView) findViewById(R.id.featured_event_list);
     allListView = (ListView) findViewById(R.id.all_event_list);
     radarListView = (ListView) findViewById(R.id.radar_list);
     myNameView = (TextView) findViewById(R.id.user_name);
     tabHost = (FlingableTabHost) findViewById(android.R.id.tabhost);
     
     findViewById(R.id.map_button).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
             Intent intent = new Intent(RadarActivity.this, RadarMapActivity.class);
             intent.putExtra("controller", commonController);
             intent.putExtra("token", tabbieAccessToken);
             startActivity(intent);
         }
       });
 
     // Set up the Tab Host
     tabHost.setup();
     tabHost.setOnTabChangedListener(this);
     setupTab(featuredListView, getString(R.string.tab_top10));
     setupTab(allListView, getString(R.string.tab_all_events));
     setupTab(radarListView, getString(R.string.tab_short_list));
     tabHost.setCurrentTab(0);
 
     // Set ListView Properties
     featuredListView.setAdapter(new EventListAdapter(this, commonController.featuredList));
     featuredListView.setFastScrollEnabled(true);
     featuredListView.setOnItemClickListener(this);
     featuredListView.setOnItemLongClickListener(this);
     
     allListView.setAdapter(new EventListAdapter(this, commonController.eventsList));
     allListView.setFastScrollEnabled(true);
     allListView.setOnItemClickListener(this);
     allListView.setOnItemLongClickListener(this);
     
     radarListView.setAdapter(new EventListAdapter(this, commonController.radarList));
     radarListView.setFastScrollEnabled(true);
     radarListView.setOnItemClickListener(this);
     radarListView.setOnItemLongClickListener(this);
     
     // Launch Authentication Activity
     preferences = getPreferences(MODE_PRIVATE);
     final Intent authenticate = new Intent(this, AuthenticateActivity.class);
     authenticate.putExtra("token", preferences.getString("access_token", null));
     authenticate.putExtra("expires", preferences.getLong("access_expires", 0));
     startActivityForResult(authenticate, REQUEST_LOGIN);
   }
 
   public void onTabChanged(String tabName) {
       findViewById(R.id.radar_list_empty_text).setVisibility(View.GONE);
       
 
       final View v;
       
       if(0 == commonController.eventsList.size()) {
     	v = findViewById(R.id.radar_list_empty_text);
       	v.setVisibility(View.VISIBLE);
       } else if (tabName.equals(getString(R.string.tab_all_events))) {
         v = findViewById(R.id.all_event_list);
 
         currentListView = allListView;
 
       } else if (tabName.equals(getString(R.string.tab_top10))) {
         v = findViewById(R.id.featured_event_list);
         currentListView = featuredListView;
 
       } else if (tabName.equals(getString(R.string.tab_short_list))) {
         if (0 == commonController.radarList.size()) {
           v = findViewById(R.id.radar_list_empty_text);
           v.setVisibility(View.VISIBLE);
         } else {
           v = findViewById(R.id.radar_list);
           commonController.order();
           ((EventListAdapter) radarListView.getAdapter())
               .notifyDataSetChanged();
         }
         currentListView = radarListView;
       } else
         throw new RuntimeException();
 
       PlayAnim(v, getBaseContext(), android.R.anim.fade_in, 100);
   }
 
   public Animation PlayAnim(View v, Context con, int animationId,
       int StartOffset) {
     if (null != v) {
       Animation animation = AnimationUtils.loadAnimation(con, animationId);
       animation.setStartOffset(StartOffset);
       v.startAnimation(animation);
 
       return animation;
     }
     return null;
   }
 
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     
     switch (requestCode) {
     case REQUEST_LOGIN:
     	
         final SharedPreferences.Editor editor = preferences.edit();
         editor.putString("access_token", data.getStringExtra("fbAccessToken"));
         editor.putLong("access_expires", data.getLongExtra("expires", 0));
         editor.commit();
         
     	tabbieAccessToken = data.getStringExtra("tabbieAccessToken");
     	myNameView.setText(data.getStringExtra("facebookName"));
     	
     	final ServerGetRequest req = new ServerGetRequest(
     			ServerThread.TABBIE_SERVER + "/mobile/all.json?auth_token="
     			+ tabbieAccessToken, MessageType.LOAD_EVENTS);
     	sendServerRequest(req);
     	break;
     	
     case RadarCommonController.RETRIEVE_INSTANCE:
       final Bundle controller = data.getExtras();
       commonController = controller.getParcelable("controller");
 
       // New adapters are necessary (for now) for the lists
       // to update if the user clicked anything in Details
       
       featuredListView.setAdapter(new EventListAdapter(this, commonController.featuredList));
 
       allListView.setAdapter(new EventListAdapter(this, commonController.eventsList));
 
       radarListView.setAdapter(new EventListAdapter(this, commonController.radarList));
 
       ((EventListAdapter) featuredListView.getAdapter()).notifyDataSetChanged();
       ((EventListAdapter) allListView.getAdapter()).notifyDataSetChanged();
       ((EventListAdapter) radarListView.getAdapter()).notifyDataSetChanged();
 
       currentListView.setSelection(currentViewPosition);
       break;
       
     case RadarCommonController.FIRE_EVENT:
     	final Event e = commonController.getEvent(data.getExtras().getString("event"));
         Intent intent = new Intent(RadarActivity.this,
                 EventDetailsActivity.class);
             intent.putExtra("eventId", e.id);
             intent.putExtra("controller", commonController);
             intent.putExtra("image",
             		((EventListAdapter) allListView.getAdapter()).
             		imageLoader.getBitmap(e.image.toString())); // TODO Make this code suck less
             intent.putExtra("token", tabbieAccessToken);
         startActivity(intent);
         break;
     }
   }
 
   @Override
   public void onResume() {
     super.onResume();
     facebook.extendAccessTokenIfNeeded(this, null);
   }
 
   private void setupTab(final View view, final String tag) {
     View tabview = createTabView(tabHost.getContext(), tag);
 
     TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview)
         .setContent(new TabHost.TabContentFactory() {
           public View createTabContent(String tag) {
             return view;
           }
         });
     tabHost.addTab(setContent);
 
   }
 
   private static View createTabView(final Context context, final String text) {
     View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
     TextView tv = (TextView) view.findViewById(R.id.tabsText);
     tv.setText(text);
     return view;
   }
 
   @SuppressLint({ "ParserError", "ParserError" })
   @Override
   protected synchronized boolean handleServerResponse(ServerResponse resp) {
       JSONArray list = resp.parseJsonArray();
       
 
       Set<String> serverRadarIds = new LinkedHashSet<String>();
       try {
         JSONObject radarObj = list.getJSONObject(list.length() - 1);
         JSONArray tmpRadarList = radarObj.getJSONArray("radar");
         for (int i = 0; i < tmpRadarList.length(); ++i) {
           serverRadarIds.add(tmpRadarList.getString(i));
           Log.d("Here is id", tmpRadarList.getString(i));
         }
       } catch (JSONException e1) {
         e1.printStackTrace();
       }
 
       commonController.clear();
       for (int i = 0; i < list.length() - 1; ++i) {
         try {
           JSONObject obj = list.getJSONObject(i);
           String radarCountStr = obj.getString("user_count");
           int radarCount = 0;
 
           if (null != radarCountStr && 0 != radarCountStr.compareTo("null"))
             radarCount = Integer.parseInt(radarCountStr);
 
           final Event e = new Event(  obj.getString("id"),
                                       obj.getString("name"),
                                       obj.getString("description"),
                                       obj.getString("location"),
                                       obj.getString("street_address"),
                                       new URL("http://tonight-life.com" + obj.getString("image_url")),
                                       obj.getDouble("latitude"),
                                       obj.getDouble("longitude"),
                                       radarCount,
                                       obj.getBoolean("featured"),
                                       obj.getString("start_time"),
                                       serverRadarIds.contains(obj.getString("id")));
 
           commonController.addEvent(e);
 
         } catch (JSONException e) {
           Toast.makeText(this, "Fatal Error: Failed to Parse JSON",
               Toast.LENGTH_SHORT).show();
           e.printStackTrace();
           return false;
         } catch (final Exception e) {
           Log.e("RadarActivity",
               "Fatal Error: Non JSON-Exception during event creation");
           throw new RuntimeException();
         }
       }
       Log.d("RadarActivity", "Loading Benchmark 3, all events instantiated");
       commonController.order();
       this.runOnUiThread(new Runnable() {
         public void run() {
           ((EventListAdapter) featuredListView.getAdapter())
               .notifyDataSetChanged();
           ((EventListAdapter) allListView.getAdapter()).notifyDataSetChanged();
           ((EventListAdapter) radarListView.getAdapter())
               .notifyDataSetChanged();
           
           findViewById(R.id.radar_list_empty_text).setVisibility(View.GONE);
           if(0 == commonController.eventsList.size() || 0 == commonController.featuredList.size())
           	findViewById(R.id.radar_list_empty_text).setVisibility(View.VISIBLE);
         }
       });
     return false;
   }
 
   @Override
   public boolean onCreateOptionsMenu(final Menu menu) {
     final MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.main_menu, menu);
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(final MenuItem item) {
     // Handle item selection
 
     int itemId = item.getItemId();
 	if (itemId == R.id.refresh_me) {
 		this.runOnUiThread(new Runnable() {
 		    public void run() {
 		      ServerGetRequest req = new ServerGetRequest(
 		          ServerThread.TABBIE_SERVER + "/mobile/all.json?auth_token="
 		              + tabbieAccessToken, MessageType.LOAD_EVENTS);
 		      sendServerRequest(req);
 		    }
 		  });
 		return true;
 	} else if (itemId == R.id.report_me) {
 		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
 		emailIntent.setType("plain/text");
 		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, FOUNDERS_EMAIL);
 		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, APP_FEEDBACK_SUBJECT);
 		startActivity(Intent.createChooser(emailIntent, "Send feedback..."));
 		return true;
 	} else if (itemId == R.id.preference_me) {
 		final PreferencesDialog preferencesDialog = new PreferencesDialog(this, R.layout.preferences);
 		preferencesDialog.setTitle("Preferences");
 		preferencesDialog.setOnAgeItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View v,
 					int position, long id) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> container) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		preferencesDialog.setOnCostItemsSelectedListener(new MultiSpinnerListener() {
 			
 			@Override
 			public void onItemsSelected(boolean[] selected) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		preferencesDialog.setOnEnergyItemsSelectedListener(new MultiSpinnerListener() {
 			
 			@Override
 			public void onItemsSelected(boolean[] selected) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		preferencesDialog.show();
 		return true;
 	} else {
 		return super.onOptionsItemSelected(item);
 	}
   }
   
   @Override
   public void onDestroy() {
     super.onDestroy();
     googleAnalyticsTracker.stopSession();
   }
   
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
 		
 		final Event e = (Event) parent.getItemAtPosition(position);
 		
 		Log.d("OnItemClick", "Event is " + e.name);
 		
 	    if (null != e) {
 	        currentViewPosition = position;
 	        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE))
 	            .vibrate(30);
 
 	        new AsyncTask<Void, Void, Intent>() {
 
 	          private ProgressDialog dialog;
 
 	          @Override
 	          protected void onPreExecute() {
 	            dialog = ProgressDialog.show(RadarActivity.this, "",
 	                "Loading, please wait...");
 	            super.onPreExecute();
 	          }
 
 	          @Override
 	          protected Intent doInBackground(Void... params) {
 	            Intent intent = new Intent(RadarActivity.this,
 	                EventDetailsActivity.class);
 	            intent.putExtra("eventId", e.id);
 	            intent.putExtra("controller", commonController);
 	            intent.putExtra("token", tabbieAccessToken);
 	            return intent;
 	          }
 
 	          @Override
 	          protected void onPostExecute(Intent result) {
 	            startActivityForResult(result,
 	                RadarCommonController.RETRIEVE_INSTANCE);
 	            dialog.dismiss();
 
 	          };
 	        }.execute();
 	      }
 	}
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> parent, View v, int position,
 			long rowId) {
 		// TODO Pop up a dialog here
 		Toast.makeText(this, "Long click!", Toast.LENGTH_SHORT).show();
 		return true;
 	}
 }
