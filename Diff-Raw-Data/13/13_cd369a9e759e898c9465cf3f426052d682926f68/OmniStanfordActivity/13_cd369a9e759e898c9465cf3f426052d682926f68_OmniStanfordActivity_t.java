 package mobisocial.omnistanford;
 
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.AsyncTask;
import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Base64;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.LinearLayout;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import mobisocial.omnistanford.db.CheckinManager;
 import mobisocial.omnistanford.db.LocationManager;
 import mobisocial.omnistanford.db.MAccount;
 import mobisocial.omnistanford.db.MCheckinData;
 import mobisocial.omnistanford.db.MLocation;
 import mobisocial.omnistanford.server.ui.CheckinListFragment;
 import mobisocial.omnistanford.service.LocationService;
 import mobisocial.omnistanford.ui.PullToRefreshListView;
 import mobisocial.omnistanford.ui.PullToRefreshListView.OnRefreshListener;
 import mobisocial.omnistanford.util.Util;
 import mobisocial.socialkit.musubi.DbFeed;
 import mobisocial.socialkit.musubi.Musubi;
 
 public class OmniStanfordActivity extends OmniStanfordBaseActivity {
     public static final String TAG = "OmniStanfordActivity";
     
     public static final long MONTH = 1000L * 60L * 60L * 24L * 30L;
     
     private Musubi mMusubi;
     private LinearLayout mButtonView;
     private List<HashMap<String, String>> mHms;
     private HashMap<Long, Long> mIdMap;
     private PullToRefreshListView mListView;
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         
         if (requestCode == REQUEST_CREATE_FEED) {
             if (resultCode == RESULT_OK) {
                 Uri feedUri = data.getData();
                 if (feedUri != null) {
                     // Single feed
                     Log.d(TAG, "Feed URI: " + feedUri);
                     
                     if (mMusubi != null) {
                         DbFeed feed = mMusubi.getFeed(feedUri);
                         mMusubi.setFeed(feed);
                     }
                 }
             }
         }
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setTheme(R.style.Theme_Sherlock_ForceOverflow);
         
         if(setServerMode()) {
         	FragmentManager fragmentManager = getSupportFragmentManager();
         	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 
         	CheckinListFragment fragment = new CheckinListFragment();
         	fragmentTransaction.add(R.id.contentArea, fragment);
         	fragmentTransaction.commit();
         } else {
             mButtonView = new LinearLayout(this);
             mButtonView.setOrientation(LinearLayout.VERTICAL);
             
 //            Button checkinButton = new Button(this);
 //            checkinButton.setText("Checkin");
 //            checkinButton.setOnClickListener(mCheckinClickListener);
 //            mButtonView.addView(checkinButton);
 //            
 //            Button checkoutButton = new Button(this);
 //            checkoutButton.setText("Checkout");
 //            checkoutButton.setOnClickListener(mCheckoutClickListener);
 //            mButtonView.addView(checkoutButton);
             
             ((LinearLayout)findViewById(R.id.contentArea)).addView(mButtonView);
             
             if (Musubi.isMusubiInstalled(this)) {
                 mMusubi = Musubi.getInstance(this);
             }
 
             TextView tv = new TextView(this);
             tv.setTextSize(20.0f);
             tv.setTextColor(Color.WHITE);
             tv.setText("Recent Locations");
             CheckinManager cm = new CheckinManager(App.getDatabaseSource(this));
             List<MCheckinData> checkins = cm.getRecentCheckins();
             for (MCheckinData data : checkins) {
                 if (data.exitTime == null) {
                     LocationManager lm = new LocationManager(App.getDatabaseSource(this));
                     MLocation loc = lm.getLocation(data.locationId);
                     if (loc != null) {
                         //tv.setText("Checked in at " + loc.name);
                     }
                     break;
                 }
             }
             mButtonView.addView(tv);
             
             showRecent(mButtonView);
             
             // Do some location updates
             if (Musubi.isMusubiInstalled(this)) {
                 new CreateFeedsTask().execute(App.getDatabaseSource(this));
             }
             
             bindServices(null);
         }
     }
     
     @Override
     public void onResume() {
         super.onResume();
         
         if (Musubi.isMusubiInstalled(this)) {
             new CreateFeedsTask().execute(App.getDatabaseSource(this));
         }
         
         if (mHms == null) {
             mHms = new ArrayList<HashMap<String, String>>();
         }
         constructMap(null);
     }
     
     private void constructMap(List<MCheckinData> checkins) {
         synchronized(this) {
             mHms.clear();
             LocationManager lm = new LocationManager(App.getDatabaseSource(this));
             if (checkins == null) {
                 CheckinManager cm = new CheckinManager(App.getDatabaseSource(this));
                 checkins = cm.getRecentCheckins(MONTH);
             }
             mIdMap = new HashMap<Long, Long>();
             for (MCheckinData checkin : checkins) {
                 HashMap<String, String> hm = new HashMap<String, String>();
                 MLocation loc = lm.getLocation(checkin.locationId);
                 Format fullFormatter = new SimpleDateFormat("M/d/yyyy h:mm a");
                 hm.put("title", loc.name);
                 if (checkin.exitTime == null || checkin.exitTime == 0L) {
                     hm.put("subtitle2", "Entered at " + fullFormatter.format(new Date(checkin.entryTime)));
                     hm.put("subtitle", "You are currently here.");
                 } else {
                     hm.put("subtitle", "Entered at " + fullFormatter.format(new Date(checkin.entryTime)));
                     hm.put("subtitle2", "Left at " + fullFormatter.format(new Date(checkin.exitTime)));
                 } 
                 mHms.add(hm);
                 mIdMap.put(new Long(mHms.size()), checkin.id);
             }
         }
     }
     
     
     private void showRecent(LinearLayout wrapper) {
         String[] from = new String[] { "title", "subtitle", "subtitle2" };
         int[] to = new int[] { R.id.plainTitle, R.id.plainSubtitle, R.id.plainSubtitle2 };
         synchronized(this) {
             if (mHms == null) {
                 mHms = new ArrayList<HashMap<String, String>>();
             }
         }
         constructMap(null);
         
         mListView = new PullToRefreshListView(this);
         mListView.setAdapter(new SimpleAdapter(this, mHms, R.layout.plain_list_item, from, to));
         mListView.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position,
                     long id) {
                 //do something
                 if (!Musubi.isMusubiInstalled(OmniStanfordActivity.this)) {
                     Toast.makeText(
                             OmniStanfordActivity.this, "Musubi not installed!", Toast.LENGTH_SHORT)
                             .show();
                     return;
                 }
                 Log.d(TAG, "Selected " + position);
                 Intent intent = new Intent(OmniStanfordActivity.this, SelectContactsActivity.class);
                 if (mIdMap.containsKey(new Long(position))) {
                     intent.putExtra("checkin", mIdMap.get(new Long(position)).longValue());
                 }
                 startActivity(intent);
             }
         });
         mListView.setOnRefreshListener(new OnRefreshListener() {
             @Override
             public void onRefresh() {
                 new RefreshListTask().execute(1L);
             }
         });
         
         wrapper.addView(mListView);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSherlock().getMenuInflater();
         inflater.inflate(R.menu.action_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_settings:
             	Intent create = new Intent(this, SettingsActivity.class);
             	startActivity(create);
             	return true;
             case R.id.menu_add_account:
                 if (!Musubi.isMusubiInstalled(this)) {
                    Toast.makeText(this, "Musubi not installed!", Toast.LENGTH_SHORT).show();
                     return true;
                 }
             	Intent picker = new Intent(ACTION_OWNED_ID_PICKER);
                 startActivityForResult(picker, REQUEST_PICK_ID);
                 return true;
             case R.id.menu_schedule:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                	Intent intent = new Intent(OmniStanfordActivity.this, ScheduleActivity.class);
    				startActivity(intent);
                } else {
                    Toast.makeText(this, "Android 3.0+ required!", Toast.LENGTH_SHORT).show();
                }
 				return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
 //    private OnClickListener mCheckinClickListener = new OnClickListener() {
 //		@Override
 //		public void onClick(View v) {
 //			Request req = new Request("gates.stanford@gmail.com", "checkin", null);
 //			req.addParam("loc_id", "1")
 //				.addParam("dorm", "McFarland")
 //				.addParam("department", "CS");
 //			req.send(v.getContext());
 //			bindServices("locationUpdate");
 //		}
 //    };
     
 //    private OnClickListener mCheckoutClickListener = new OnClickListener() {
 //		@Override
 //		public void onClick(View v) {
 //		    Request req = new Request("gates.stanford@gmail.com", "checkout", null);
 //            req.send(v.getContext());
 //            
 //		    Log.d(TAG, "Checkout");
 //            // Exit open checkins
 //		    CheckinManager cm = new CheckinManager(
 //		            App.getDatabaseSource(OmniStanfordActivity.this));
 //		    LocationManager lm = new LocationManager(
 //		            App.getDatabaseSource(OmniStanfordActivity.this));
 //            List<MCheckinData> checkins = cm.getRecentOpenCheckins(MONTH);
 //            for (MCheckinData data : checkins) {
 //                if (data.exitTime == null || data.exitTime == 0) {
 //                    data.exitTime = System.currentTimeMillis();
 //                    cm.updateCheckin(data);
 //                    MLocation loc = lm.getLocation(data.locationId);
 //                    Log.d(TAG, "Checking out from " + loc.name + " for: " + data.id);
 //                    Request request = new Request(loc.principal, "checkout", null);
 //                    request.send(v.getContext());
 //                } else {
 //                    Log.d(TAG, "exit time: " + data.exitTime + " for: " + data.id);
 //                }
 //            }
 //		}
 //    };
     
     private void bindServices(String extra) {
     	Intent locationService = new Intent(this, LocationService.class);
     	if (extra != null) {
     	    locationService.putExtra(extra, true);
     	}
     	startService(locationService);
     }
     
     private boolean setServerMode() {
         LocationManager lm = new LocationManager(App.getDatabaseSource(this));
         List<MLocation> locations = lm.getLocations();
         MAccount account = Util.loadAccount(this);
         if(account != null) {
         	for(MLocation loc : locations) {
         		String locHash = Base64.encodeToString(
         				digestPrincipal(loc.principal), Base64.NO_WRAP);
         		if(locHash.equals(account.identifier)) {
         			((App)getApplicationContext()).setServerMode(true);
         			return true;
         		}
         	}
         }
         
         return false;
     }
     
     class RefreshListTask extends AsyncTask<Long, Void, List<MCheckinData>> {
         @Override
         protected List<MCheckinData> doInBackground(Long... params) {
             CheckinManager cm = new CheckinManager(
                     App.getDatabaseSource(OmniStanfordActivity.this));
             return cm.getRecentCheckins(MONTH);
         }
         
         @Override
         protected void onPostExecute(List<MCheckinData> data) {
             constructMap(data);
             mListView.onRefreshComplete();
         }
     }
 }
