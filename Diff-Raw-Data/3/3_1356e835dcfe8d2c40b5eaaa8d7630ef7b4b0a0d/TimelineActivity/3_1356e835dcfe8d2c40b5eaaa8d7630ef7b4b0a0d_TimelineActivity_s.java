 package de.fabianonline.geotweeter.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.scribe.model.Token;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.google.android.gcm.GCMRegistrar;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import de.fabianonline.geotweeter.Account;
 import de.fabianonline.geotweeter.BackgroundImageLoader;
 import de.fabianonline.geotweeter.Constants;
 import de.fabianonline.geotweeter.MapOverlay;
 import de.fabianonline.geotweeter.R;
 import de.fabianonline.geotweeter.TimelineElementAdapter;
 import de.fabianonline.geotweeter.User;
 import de.fabianonline.geotweeter.timelineelements.DirectMessage;
 import de.fabianonline.geotweeter.timelineelements.TimelineElement;
 import de.fabianonline.geotweeter.timelineelements.Tweet;
 
 public class TimelineActivity extends MapActivity {
 	private final String LOG = "TimelineActivity";
 	private ArrayList<Account> accounts = new ArrayList<Account>();
 	private int acc;
 	public static Account current_account = null;
 	public static BackgroundImageLoader background_image_loader = null;
 	public static String reg_id = "";
 	private MapView map;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		map = new MapView(this, Constants.MAPS_API_KEY);
 		setContentView(R.layout.activity_timeline);
 		background_image_loader = new BackgroundImageLoader(getApplicationContext());
 		TimelineElement.tweetTimeStyle = getSharedPreferences(Constants.PREFS_APP, 0).getString("pref_tweet_time_style", "dd.MM.yy HH:mm");
 		
 		acc=0;
 		ArrayList<User> auth_users = getAuthUsers();
 		if (auth_users != null) {
 			for (User u : auth_users) {
 				TimelineElementAdapter ta = new TimelineElementAdapter(this, R.layout.timeline_element, new ArrayList<TimelineElement>());
 				addAccount(new Account(ta, getUserToken(u), u));
 			}
 		}
 		
 		ListView l = (ListView) findViewById(R.id.timeline);
 		l.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				//view.setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0xFFFFFFFF, 0xFFCCCCCC }));
 				showMapIfApplicable(parent, view, position, id);
 			}
 		});
 		
 		l.setOnItemLongClickListener(new OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 				if (current_account.getElements().getItem(position).isReplyable()) {
 					Intent replyIntent = new Intent(TimelineActivity.this, NewTweetActivity.class);
 					replyIntent.putExtra("de.fabianonline.geotweeter.reply_to_tweet", current_account.getElements().getItem(position));
 					startActivity(replyIntent);
 					return true;
 				} else {
 					return false;
 				}
 			}
 		});
 		
 		if (current_account != null) {
 			l.setAdapter(current_account.getElements());
 			GCMRegistrar.checkDevice(this);
 			GCMRegistrar.checkManifest(this);
 			reg_id = GCMRegistrar.getRegistrationId(this);
 			if (reg_id.equals("")) {
 				GCMRegistrar.register(this, Constants.GCM_SENDER_ID);
 			}
 		} else {
 			Intent addAccountIntent = new Intent(TimelineActivity.this, SettingsAccounts.class);
 			startActivity(addAccountIntent);
 		}
 	}
 
 	protected void showMapIfApplicable(AdapterView<?> parent, View view,
 			int position, long id) {
 		TimelineElement te = current_account.getElements().getItem(position);
 		if (map.getParent() != null) {
 			FrameLayout mapContainer = (FrameLayout) map.getParent();
 			mapContainer.removeAllViews();
 			LinearLayout mapAndControls = (LinearLayout) mapContainer.getParent();
 			mapAndControls.setVisibility(View.GONE);
 		}
 		if (te instanceof Tweet) {
 			Tweet tweet = (Tweet) te;
 			if (tweet.coordinates != null) {
 				float lon = tweet.coordinates.coordinates.get(0);
 				float lat = tweet.coordinates.coordinates.get(1);
 				GeoPoint coords = new GeoPoint((int) (lat*1e6), (int) (lon*1e6));
 				
 				LinearLayout mapAndControls = (LinearLayout) view.findViewById(R.id.map_and_controls);
 				FrameLayout mapContainer = (FrameLayout) view.findViewById(R.id.map_fragment_container);
 				
 				TextView zoomIn = (TextView) view.findViewById(R.id.zoom_in);
 				TextView zoomOut = (TextView) view.findViewById(R.id.zoom_out);
 				
 				mapContainer.addView(map);
 				
 				List<Overlay> overlays = map.getOverlays();
 				Drawable marker = MapOverlay.getLocationMarker(background_image_loader.loadBitmap(tweet.user.profile_image_url_https));
 				MapOverlay overlay = new MapOverlay(marker);
 				OverlayItem overlayItem = new OverlayItem(coords, null, null);
 				overlay.addOverlay(overlayItem);
 				overlays.add(overlay);
 				
 				map.setBuiltInZoomControls(false);
 				map.getController().setCenter(coords);
 				map.getController().setZoom(15);
 				map.setVisibility(View.VISIBLE);
 				
 				zoomIn.setOnClickListener(new OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						map.getController().zoomIn();
 					}
 				});
 				zoomOut.setOnClickListener(new OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						map.getController().zoomOut();
 					}
 				});
 				
 				mapAndControls.setVisibility(View.VISIBLE);
 			}
 		}
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		for (Account acct : Account.all_accounts) {
 			try {
 				acct.stopStream();
 			} catch (Exception ex) {}
 		}
 	}
 	
 	public void nextAccountHandler(View v) {
 		acc = (acc + 1) % accounts.size();
 		current_account = accounts.get(acc);
 //		elements = current_account.getElements().getItems();
 		ListView l = (ListView) findViewById(R.id.timeline);
 		l.setAdapter(current_account.getElements());
 		Log.d(LOG, "Changed Account: " + current_account.getUser().screen_name);
 	}
 
 	private Token getUserToken(User u) {
 		SharedPreferences sp = getSharedPreferences(Constants.PREFS_APP, 0);
 		return new Token(sp.getString("access_token."+String.valueOf(u.id), null), 
 						  sp.getString("access_secret."+String.valueOf(u.id), null));
 	}
 
 	private ArrayList<User> getAuthUsers() {
 		ArrayList<User> result = null;
 		
 		SharedPreferences sp = getSharedPreferences(Constants.PREFS_APP, 0);
 		String accountString = sp.getString("accounts", null);
 		
 		if (accountString != null) {
 			String[] accounts = accountString.split(" ");
 			result = User.getPersistentData(getApplicationContext(), accounts);
 		}
 		
 		return result;
 	}
 	
 	public void addAccount(Account acc) {
 		accounts.add(acc);
 		if (current_account == null) {
 			current_account = acc;
 //			elements = acc.getElements().getItems();
 		}
 		if (!reg_id.equals("")) {
 			acc.registerForGCMMessages();
 		}
 	}
 
 	public void newTweetClickHandler(View v) {
 		startActivity(new Intent(this, NewTweetActivity.class));
 	}
 	
 	public void markReadClickHandler(View v) {
 		ListView list = (ListView)findViewById(R.id.timeline);
 		TimelineElementAdapter elements = (TimelineElementAdapter)list.getAdapter();
 		int pos = list.getFirstVisiblePosition()+1;
 		TimelineElement current;
 		long new_max_read_tweet_id = 0;
 		long new_max_read_dm_id = 0;
 		long new_max_read_mention_id = 0;
 		while (pos < elements.getCount()) {
 			current = elements.getItem(pos);
 			if (current instanceof DirectMessage) {
 				if (new_max_read_dm_id == 0) {
 					new_max_read_dm_id = current.getID();
 				}
 			} else if (current instanceof Tweet) {
 				if (new_max_read_mention_id == 0 && ((Tweet)current).mentionsUser(current_account.getUser())) {
 					new_max_read_mention_id = current.getID();
 				}
 				if (new_max_read_tweet_id == 0) {
 					new_max_read_tweet_id = current.getID();
 				}
 			}
 			if (new_max_read_mention_id>0 && new_max_read_dm_id>0 && new_max_read_tweet_id>0) {
 				break;
 			}
 			pos++;
 		}
 		current_account.setMaxReadIDs(new_max_read_tweet_id, new_max_read_mention_id, new_max_read_dm_id);
 	}
 	
 	public void scrollDownHandler(View v) {
 		ListView lvList = (ListView)findViewById(R.id.timeline);
 		TimelineElementAdapter elements = (TimelineElementAdapter) lvList.getAdapter();
 		int pos = 0;
 		while (pos < elements.getCount()) {
 			TimelineElement element = elements.getItem(pos);
 			if (element instanceof Tweet && !(element instanceof DirectMessage)) {
 				if (element.getID() < current_account.getMaxReadTweetID()) {
 					break;
 				}
 			}
 			pos++;
 		}
 		lvList.smoothScrollToPosition(pos);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_timeline, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			Intent settingsActivity = new Intent(this,
 					GeneralPrefsActivity.class);
 			startActivity(settingsActivity);
 			return true;
 		}
 		return true;
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		/* Die Methode muss hier hin wegen MapActivity */
 		return false;
 	}
 }
