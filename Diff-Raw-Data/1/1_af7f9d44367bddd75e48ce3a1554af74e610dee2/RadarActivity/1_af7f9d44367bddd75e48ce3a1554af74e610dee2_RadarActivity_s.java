 package com.tabbie.android.radar;
 
 import java.net.URL;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TabHost;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TabHost.TabSpec;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 
 public class RadarActivity extends ServerThreadActivity implements
     OnTabChangeListener, RemoteDrawableController.PreLoadFinishedListener {
 
   private static final String LIST_FEATURED_TAG = "Featured";
   private static final String EVENT_TAB_TAG = "Events";
   private static final String RADAR_TAB_TAG = "Radar";
   private static final int MAX_TITLE_LENGTH = 36;
 
   private TabHost tabHost;
   private ListView currentListView;
   private ListView featuredListView;
   private ListView allListView;
   private ListView radarListView;
   private TextView myNameView;
 
   private String token;
   private int currentViewPosition = 0;
   private boolean tabbieVirgin = true; // SharedPref variable to determine if the tutorial should run
   private boolean forceFeatureTab = false; // Used to make sure the user can't escape the tutorial
 
   private RadarCommonController commonController;
   private RemoteDrawableController remoteDrawableController;
 
   // FB junk
   private Facebook facebook = new Facebook("217386331697217");
   private SharedPreferences preferences;
 
   protected class EventListAdapter extends ArrayAdapter<Event> {
 
     public EventListAdapter(Context context, int resource,
         int textViewResourceId, List<Event> events) {
       super(context, resource, textViewResourceId, events);
     }
 
     @Override
     public View getView(final int position, View convertView, ViewGroup parent) {
       if (null == convertView) {
         LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         convertView = inflater.inflate(R.layout.event_list_element, null);
       }
       final Event e = getItem(position);
       TextView title = (TextView) convertView.findViewById(R.id.event_text);
       title.setText(e.getAbbreviatedName(MAX_TITLE_LENGTH));
 
       ((TextView) convertView.findViewById(R.id.event_list_time))
           .setText(e.time.makeYourTime());
       ((TextView) convertView.findViewById(R.id.event_location))
           .setText(e.venueName);
       Log.v("RadarActivity", "Setting RadarCount " + e.radarCount);
       ((TextView) convertView.findViewById(R.id.upvotes)).setText(Integer
           .toString(e.radarCount));
 
       /*
        * Check and see if there is an image that has been loaded If there is an
        * image that has been loaded and it has been drawn, then do nothing If
        * there is an image that has been loaded, but it hasn't been drawn, draw
        * it If there is no image that has been loaded, display the loader and
        * LOAD THAT SH*T
        */
 
       final ImageView loader = (ImageView) convertView
           .findViewById(R.id.element_loader);
       final ImageView img = (ImageView) convertView
           .findViewById(R.id.event_image);
 
       if (!remoteDrawableController.hasImage(e.image)) {
         Log.d("RadarActivity", "Image still being retrieved, displaying loader");
         loader.startAnimation(AnimationUtils.loadAnimation(RadarActivity.this,
             R.anim.rotate));
       } else if (img.getTag() == null
           || 0 != ((URL) img.getTag()).toString().compareTo(e.image.toString())) {
         Log.d("RadarActivity", "RDC has image");
         loader.setVisibility(View.GONE);
         img.setVisibility(View.VISIBLE);
         remoteDrawableController.drawImage(e.image, img);
       }
 
       convertView.findViewById(R.id.list_list_element_layout)
           .setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
               if (null != e) {
                 currentViewPosition = position;
                 ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE))
                     .vibrate(30);
 
                 Intent intent = new Intent(RadarActivity.this,
                     EventDetailsActivity.class);
                 intent.putExtra("eventId", e.id);
                 intent.putExtra("controller", commonController);
                 intent.putExtra("token", token);
                 if(tabbieVirgin) {
                 	intent.putExtra("virgin", true); // Make sure this activity knows it's in tutorial mode
                 	getPreferences(MODE_PRIVATE).edit().putBoolean("virgin", false)
                 	.commit();
                 }
                 startActivityForResult(intent,
                     RadarCommonController.RETRIEVE_INSTANCE);
               }
             }
           });
 
       convertView.findViewById(R.id.location_image_layout).setOnClickListener(
           new OnClickListener() {
             public void onClick(View v) {
               Intent intent = new Intent(RadarActivity.this,
                   RadarMapActivity.class);
               intent.putExtra("controller", commonController);
               intent.putExtra("event", e);
               startActivity(intent);
             }
           });
       return convertView;
     }
   }
 
   @Override
   public void onCreate(final Bundle savedInstanceState) {
     Log.d("RadarActivity", "OnCreate Method");
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
 
     Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
 
     currentListView = featuredListView = (ListView) findViewById(R.id.featured_event_list);
     allListView = (ListView) findViewById(R.id.all_event_list);
     radarListView = (ListView) findViewById(R.id.radar_list);
     myNameView = (TextView) findViewById(R.id.user_name);
 
     // Spinning loading dialog
 
     ((ImageView) findViewById(R.id.loading_spin)).startAnimation(AnimationUtils
         .loadAnimation(this, R.anim.rotate));
 
     preferences = getPreferences(MODE_PRIVATE);
     // Facebook Access Token
     String accessToken = preferences.getString("access_token", null);
     long expires = preferences.getLong("access_expires", 0);
     // Check and see if the facebook access is still valid
     if (accessToken != null) {
       facebook.setAccessToken(accessToken);
     }
     if (expires != 0) {
       facebook.setAccessExpires(expires);
     }
 
     if (!facebook.isSessionValid()) {
       facebook.authorize(this, new String[] { "email" }, new DialogListener() {
         public void onComplete(Bundle values) {
           SharedPreferences.Editor editor = preferences.edit();
           editor.putString("access_token", facebook.getAccessToken());
           editor.putLong("access_expires", facebook.getAccessExpires());
           editor.commit();
           sendServerRequest(new ServerGetRequest(
               "https://graph.facebook.com/me/?access_token="
                   + facebook.getAccessToken(), MessageType.FACEBOOK_LOGIN));
         }
 
         public void onFacebookError(FacebookError error) {
         }
 
         public void onError(DialogError e) {
         }
 
         public void onCancel() {
         }
       });
     } else {
       // Already have fb session
       sendServerRequest(new ServerGetRequest(
           "https://graph.facebook.com/me/?access_token="
               + facebook.getAccessToken(), MessageType.FACEBOOK_LOGIN));
     }
 
     commonController = new RadarCommonController();
     remoteDrawableController = new RemoteDrawableController(this);
 
     // Set up the Tab Host
     tabHost = (FlingableTabHost) findViewById(android.R.id.tabhost);
     tabHost.setup();
     tabHost.setOnTabChangedListener(this);
 
     featuredListView.setAdapter(new EventListAdapter(this,
         R.id.featured_event_list, R.layout.event_list_element,
         commonController.featuredList));
 
     allListView.setAdapter(new EventListAdapter(this, R.id.all_event_list,
         R.layout.event_list_element, commonController.eventsList));
 
     radarListView.setAdapter(new EventListAdapter(this, R.id.radar_list,
         R.layout.event_list_element, commonController.radarList));
 
     findViewById(R.id.map_button).setOnClickListener(new OnClickListener() {
       public void onClick(View v) {
     	  if(!forceFeatureTab) {
 	        Intent intent = new Intent(RadarActivity.this, RadarMapActivity.class);
 	        intent.putExtra("controller", commonController);
 	        startActivity(intent);
     	  } else {
     		  Toast.makeText(RadarActivity.this, "Please select an event to continue", Toast.LENGTH_SHORT).show();
     	  }
       }
     });
 
     setupTab(featuredListView, LIST_FEATURED_TAG);
     setupTab(allListView, EVENT_TAB_TAG);
     setupTab(radarListView, RADAR_TAB_TAG);
 
     tabHost.setCurrentTab(0);
 
     featuredListView.setFastScrollEnabled(true);
     allListView.setFastScrollEnabled(true);
     radarListView.setFastScrollEnabled(true);
   }
 
   public void onTabChanged(String tabName) {
 	  if(!tabbieVirgin) {
 	    findViewById(R.id.radar_list_empty_text).setVisibility(View.GONE); // Is
 	                                                                       // this
 	                                                                       // the
 	                                                                       // most
 	                                                                       // efficient
 	                                                                       // implementation?
 	    
 	    final View v;
 	    if (tabName.equals(EVENT_TAB_TAG)) {
 	    	v = findViewById(R.id.all_event_list);
 	    	
 	        currentListView = allListView;
 	        
 	
 	    } else if (tabName.equals(LIST_FEATURED_TAG)) {
 	    	v = findViewById(R.id.featured_event_list);
 	      currentListView = featuredListView;  
 	
 	    } else if (tabName.equals(RADAR_TAB_TAG)) {
 	      if (radarListView.getAdapter().getCount() == 0) {
 	    	  v = findViewById(R.id.radar_list_empty_text);
 	        v.setVisibility(View.VISIBLE);
 	      } else {
 	    	  v = findViewById(R.id.radar_list);
 	        commonController.order();
 	        ((EventListAdapter) radarListView.getAdapter()).notifyDataSetChanged();
 	      }
 	      currentListView = radarListView;
 	    } else throw new RuntimeException();
 	    
 		PlayAnim(v,	getBaseContext(),
 				android.R.anim.fade_in,
 				100);
 	  } else if(forceFeatureTab) {
 		  tabHost.setCurrentTab(0); // TODO This probably shouldn't be hardcoded
 		  Toast.makeText(this, "Please select an event to continue", Toast.LENGTH_SHORT).show();
 	  }
   }
   
   public Animation PlayAnim(View v, Context con, int animationId, int StartOffset)
   {
 	  if(v!=null)
 	  {
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
     Log.v("Request Code", "This: " + requestCode);
     switch (requestCode) {
     case RadarCommonController.RETRIEVE_INSTANCE:
       final Bundle controller = data.getExtras();
       commonController = controller.getParcelable("controller");
 
       featuredListView.setAdapter(new EventListAdapter(this,
           R.id.featured_event_list, R.layout.event_list_element,
           commonController.featuredList));
 
       allListView.setAdapter(new EventListAdapter(this, R.id.all_event_list,
           R.layout.event_list_element, commonController.eventsList));
 
       radarListView.setAdapter(new EventListAdapter(this, R.id.radar_list,
           R.layout.event_list_element, commonController.radarList));
 
       ((EventListAdapter) featuredListView.getAdapter()).notifyDataSetChanged();
       ((EventListAdapter) allListView.getAdapter()).notifyDataSetChanged();
       ((EventListAdapter) radarListView.getAdapter()).notifyDataSetChanged();
 
       currentListView.setSelection(currentViewPosition);
       if(forceFeatureTab)
     	  forceFeatureTab = false;
       break;
     default:
     	facebook.authorizeCallback(requestCode, resultCode, data);
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
     Log.d("RadarActivity", "Handling a server response");
     if (MessageType.FACEBOOK_LOGIN == resp.responseTo) {
       JSONObject json = resp.parseJsonContent();
       if (json == null || !json.has("id")) {
         return false;
       }
       final Long facebookId;
       final String facebookName;
       try {
         facebookId = json.getLong("id");
         facebookName = json.getString("first_name") + " "
             + json.getString("last_name").substring(0, 1) + ".";
       } catch (JSONException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         return false;
       }
       // Now that we have our Facebook user info, we can send this to Tabbie to
       // get our Tabbie id
       this.runOnUiThread(new Runnable() {
         public void run() {
           myNameView.setText(facebookName);
           ServerPostRequest req = new ServerPostRequest(
               ServerThread.TABBIE_SERVER + "/mobile/auth.json",
               MessageType.TABBIE_LOGIN);
           req.params.put("fb_token", facebook.getAccessToken());
 
           sendServerRequest(req);
         }
       });
     } else if (MessageType.TABBIE_LOGIN == resp.responseTo) {
       JSONObject json = resp.parseJsonContent();
       if (null == json || !json.has("token")) {
         return false;
       }
       try {
         token = json.getString("token");
         this.runOnUiThread(new Runnable() {
           public void run() {
             ServerGetRequest req = new ServerGetRequest(
                 ServerThread.TABBIE_SERVER + "/mobile/all.json?auth_token="
                     + token, MessageType.LOAD_EVENTS);
             sendServerRequest(req);
           }
         });
       } catch (JSONException e) {
         e.printStackTrace();
         return false;
       }
     } else if (MessageType.LOAD_EVENTS == resp.responseTo) {
       JSONArray list = resp.parseJsonArray();
       if (null == list) {
         return false;
       }
       Log.d("RadarActivity", "Loading Benchmark 1");
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
 
           final Event e = new Event(obj.getString("id"), obj.getString("name"),
               obj.getString("description"), obj.getString("location"),
               obj.getString("street_address"), new URL(
                   "http://tonight-life.com" + obj.getString("image_url")),
               obj.getDouble("latitude"), obj.getDouble("longitude"),
               radarCount, obj.getBoolean("featured"),
               obj.getString("start_time"), serverRadarIds.contains(obj
                   .getString("id")));
 
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
 
           findViewById(R.id.loading_screen).setVisibility(View.GONE);
           findViewById(R.id.loading_screen_image).setVisibility(View.GONE);
           findViewById(R.id.loading_spin).setVisibility(View.GONE);
           findViewById(R.id.tonightlife_layout).setVisibility(View.VISIBLE);
           
           tabbieVirgin = getPreferences(MODE_PRIVATE).getBoolean("virgin", true);
           
           if(tabbieVirgin) {
 	          new AlertDialog.Builder(RadarActivity.this)
 	          .setMessage("Is this your first time using TonightLife?")
 	          .setCancelable(false)
 	          .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 	      		
 	      		@Override
 	      		public void onClick(DialogInterface dialog, int which) {
 	      			new AlertDialog.Builder(RadarActivity.this)
 	      			.setMessage("Awesome! TonightLife lets you discover all the best events going on in your area tonight. " +
 	      					"It's quick and easy to use; how about a quick tour?")
 	      			.setCancelable(false)
 	      			.setPositiveButton("Sounds good", new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							tabHost.setCurrentTab(2); // TODO This probably shouldn't be hardcoded
 							new AlertDialog.Builder(RadarActivity.this)
 							.setMessage("This is your radar - events will show up here in chronological order when you add them. We'll do that in a second - for now, think of this as your own personal planner")
 							.setCancelable(false)
 							.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
 								
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									tabHost.setCurrentTab(1); // TODO This probably shouldn't be hardcoded
 									
 									// WE MUST GO DEEPER
 									
 									new AlertDialog.Builder(RadarActivity.this)
 									.setMessage("This is a list of everything going on after 5:00 PM today. We update it daily with new content.")
 									.setCancelable(false)
 									.setPositiveButton("Sweet!", new DialogInterface.OnClickListener() {
 										
 										@Override
 										public void onClick(DialogInterface dialog, int which) {
 											tabHost.setCurrentTab(0);
 											forceFeatureTab = true;
 											/*
 											 * EVEN DEEPER
 											 */
 											new AlertDialog.Builder(RadarActivity.this)
 											.setMessage("Featured events are curated by the TonightLife team every day for your enjoyment. Go ahead and select one now...")
 											.setCancelable(true)
 											.setPositiveButton("Alright", null)
 											.create().show();
 										}
 									})
 									.create().show();
 								}
 							})
 							.create().show();
 						}
 					})
 					.setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							// TODO Auto-generated method stub							
 						}
 					})
 					.create().show();
 	      		}
 		      	})
 		      	.setNegativeButton("No", new DialogInterface.OnClickListener() {
 		      		
 		      		@Override
 		      		public void onClick(DialogInterface dialog, int which) {
 		                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
 		                editor.putBoolean("virgin", false);
 		                editor.commit();
 		      		}
 		      	})
 		      	.create().show();
           }
         }
       });
 
       for (Event e : commonController.eventsList) {
         remoteDrawableController.preload(e.image);
       }
     }
     // Assume that ADD_TO_RADAR and REMOVE_FROM_RADAR always succeed
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
 
     // TODO This doesn't work right now, don't know why
     switch (item.getItemId()) {
     case R.id.refresh_me:
       this.runOnUiThread(new Runnable() {
         public void run() {
           ServerGetRequest req = new ServerGetRequest(
               ServerThread.TABBIE_SERVER + "/mobile/all.json?auth_token="
                   + token, MessageType.LOAD_EVENTS);
           sendServerRequest(req);
         }
       });
 
       return true;
     default:
       return super.onOptionsItemSelected(item);
     }
   }
 
   @Override
   public void onPreLoadFinished() {
     synchronized (this) {
       this.runOnUiThread(new Runnable() {
         public void run() {
           ((EventListAdapter) featuredListView.getAdapter())
               .notifyDataSetChanged();
           ((EventListAdapter) allListView.getAdapter()).notifyDataSetChanged();
           ((EventListAdapter) radarListView.getAdapter())
               .notifyDataSetChanged();
         }
       });
     }
   }
 }
