 package com.linkedin.localin.ININ;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 
 import com.google.code.linkedinapi.client.LinkedInApiClient;
 import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
 import com.google.code.linkedinapi.client.enumeration.ProfileField;
 import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
 import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
 import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
 import com.google.code.linkedinapi.schema.Person;
 
 
 
 
 import com.linkedin.localin.ININ.R;
 
 import eu.erikw.PullToRefreshListView;
 import eu.erikw.PullToRefreshListView.OnRefreshListener;
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
 	private static final int TWO_MINUTES = 1000 * 60 * 2;
 	private static final int LOCATION_WAIT_TIME = 1000 * 10;
 	
 	public static final String CONSUMER_KEY = "rkqpdhrfmlbn";
     public static final String CONSUMER_SECRET = "U5ceoqmJfqrTTH93";
     public static final String OAUTH_CALLBACK_SCHEME = "x-oauthflow-linkedin";
     public static final String OAUTH_CALLBACK_HOST = "callback";
     public static final String OAUTH_CALLBACK_URL = OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
     
     Context mContext;
     
     public static final LinkedInOAuthService oAuthService = LinkedInOAuthServiceFactory.getInstance().createLinkedInOAuthService(CONSUMER_KEY, CONSUMER_SECRET);
     public static final LinkedInApiClientFactory factory = LinkedInApiClientFactory.newInstance(CONSUMER_KEY, CONSUMER_SECRET);
     LinkedInApiClient client;
     LinkedInAccessToken accessToken;
     
     private static final int CODE_LOGIN = 1;
 	private static final int CODE_PROFILE = 2;
     
     private Person currentUser;
     private HttpClient httpclient = new DefaultHttpClient();
     
     private Location bestLocation;
 	private String locationProvider = LocationManager.NETWORK_PROVIDER;
 	private LocationManager locationManager;
 	private long obtainLocationStartTime;
 	
 	private boolean locationUpdated = false;
 	private PullToRefreshListView listView;
 	
     /**
      * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
      * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
      * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
      * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
      */
     SectionsPagerAdapter mSectionsPagerAdapter;
 
     /**
      * The {@link ViewPager} that will host the section contents.
      */
     ViewPager mViewPager;
     private Contact currentUserContact;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	if (android.os.Build.VERSION.SDK_INT > 9) 
         {
             StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
             StrictMode.setThreadPolicy(policy);
         }
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         // Create the adapter that will return a fragment for each of the three primary sections
         // of the app.
         mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
         // Set up the action bar.
         final ActionBar actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // Set up the ViewPager with the sections adapter.
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mSectionsPagerAdapter);
 
         mContext = this;
         
         // When swiping between different sections, select the corresponding tab.
         // We can also use ActionBar.Tab#select() to do this if we have a reference to the
         // Tab.
         mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 actionBar.setSelectedNavigationItem(position);
             }
         });
 
         // For each of the sections in the app, add a tab to the action bar.
         for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
             // Create a tab with text corresponding to the page title defined by the adapter.
             // Also specify this Activity object, which implements the TabListener interface, as the
             // listener for when this tab is selected.
             actionBar.addTab(
                     actionBar.newTab()
                             .setText(mSectionsPagerAdapter.getPageTitle(i))
                             .setTabListener(this));
         }
         
         locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
         if(bestLocation == null)
         	bestLocation = locationManager.getLastKnownLocation(locationProvider);
         Intent intent = new Intent(this, LoginActivity.class);
         startActivityForResult(intent, CODE_LOGIN);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         // When the given tab is selected, switch to the corresponding page in the ViewPager.
         mViewPager.setCurrentItem(tab.getPosition());
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     /**
      * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
      * sections of the app.
      */
     public class SectionsPagerAdapter extends FragmentPagerAdapter {
     	public Fragment list = new ListFragment();
     	
     	
         public SectionsPagerAdapter(FragmentManager fm) {
             super(fm);
             
             
             
         }
 
         @Override
         public Fragment getItem(int i) {
         	Fragment fragment;
         	if(i==0){
         		return list;
         	}
         	else{
         		fragment = new DummySectionFragment();
         	}
         	
         	Bundle args = new Bundle();
     		args.putInt(ListFragment.ARG_SECTION_NUMBER, i + 1);
     		fragment.setArguments(args);
             return fragment;
         }
 
         @Override
         public int getCount() {
             return 3;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             switch (position) {
                 case 0: return getString(R.string.title_section1).toUpperCase();
                 case 1: return getString(R.string.title_section2).toUpperCase();
                 case 2: return getString(R.string.title_section3).toUpperCase();
             }
             return null;
         }
     }
 
     
     
     public class DummySectionFragment extends Fragment {
         public DummySectionFragment() {
         }
 
         public static final String ARG_SECTION_NUMBER = "section_number";
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
         	TextView tv = new TextView(inflater.getContext());
         	tv.setText("fuck you");
             return tv;
         }
     }
     
     /**
      * A dummy fragment representing a section of the app, but that simply displays dummy text.
      */
     public class ListFragment extends Fragment {
         public ListFragment() {
         }
 
         public static final String ARG_SECTION_NUMBER = "section_number";
 
         public void updateList(){
         	
         }
         
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
         	
         	View v = inflater.inflate(R.layout.nearby, null);
         	listView = (PullToRefreshListView) v.findViewById(R.id.listView1);
             listView.setOnRefreshListener(new OnRefreshListener() {
     			
     			@Override
     			public void onRefresh() {
     				queryNearbyUsers();
     				listView.onRefreshComplete();
     				//Toast.makeText(mContext, "fuck you ", Toast.LENGTH_LONG).show();
     				// Your code to refresh the list contents goes here
 
     				// Make sure you call listView.onRefreshComplete()
     				// when the loading is done. This can be done from here or any
     				// other place, like on a broadcast receive from your loading
     				// service or the onPostExecute of your AsyncTask.
 
     				// For the sake of this sample, the code will pause here to
     				// force a delay when invoking the refresh
 //    				listView.postDelayed(new Runnable() {
 //    					@Override
 //    					public void run() {
 //    						listView.onRefreshComplete();
 //    					}
 //    				}, 2000);
 //    				Toast.makeText(mContext, "fuck you ", Toast.LENGTH_LONG).show();
     			}
     		});
             
             listView.setOnItemClickListener(new OnItemClickListener() {
 
     			@Override
     			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
     			{
     				Contact contact = (Contact)parent.getItemAtPosition(position);
     				if(contact.getId().equals(currentUser.getId()))
     					return;
     				
     				Intent intent = new Intent(mContext, ProfileActivity.class);
     				intent.putExtra("user", contact);
     				intent.putExtra("token", accessToken);
     				intent.putExtra("current", currentUserContact);
     				//startActivity(intent);
     				startActivityForResult(intent, CODE_PROFILE);
     				
     			}
     		});
             
             return v;
         }
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
     	super.onActivityResult(requestCode, resultCode, data);
     	Log.d("info", "on activity result!");
     	if(requestCode == CODE_LOGIN)
     	{
     		if(resultCode == RESULT_OK)
     		{
     			accessToken = (LinkedInAccessToken)data.getSerializableExtra("token");
     			client = factory.createLinkedInApiClient(accessToken);
     			currentUser = client.getProfileForCurrentUser(
     					EnumSet.of(
     							ProfileField.FIRST_NAME, 
     							ProfileField.LAST_NAME,
     							ProfileField.ID,
     							ProfileField.HEADLINE,
     							ProfileField.LOCATION,
     							ProfileField.INDUSTRY,
     							ProfileField.PICTURE_URL,
     							ProfileField.PUBLIC_PROFILE_URL,
     							ProfileField.SITE_STANDARD_PROFILE_REQUEST_URL));
     			
     			String url = currentUser.getSiteStandardProfileRequest().getUrl();
         		Long mid = Long.parseLong(Uri.parse(url).getQueryParameter("key"));
     			
     			currentUserContact = new Contact(
     					mid, 
     					currentUser.getId(), 
     					currentUser.getFirstName(), 
     					currentUser.getLastName(),
     					currentUser.getHeadline(), 
     					currentUser.getLocation().getName(),
     					currentUser.getIndustry(),
     					currentUser.getPictureUrl(),
     					0.0, 0.0, "");
 //    			TextView textView = (TextView) findViewById(R.id.textView2);
 //    			textView.setText("Hello " + currentUser.getFirstName() + "!");
     			Log.d("info", currentUser.getPublicProfileUrl());
     		    Log.d("info", currentUser.getSiteStandardProfileRequest().getUrl());
     			
     			Log.d("info", currentUser.toString());
     			
     			obtainLocation();
     		}
     	}
     	else if (requestCode == CODE_PROFILE)
     	{
     		if(resultCode == RESULT_OK)
     		{
     			Log.d("info", "return to chat!");
     			//tabHost.setCurrentTab(1);
     			
     			Contact contact = (Contact)data.getSerializableExtra("contact");
     			Intent intent = new Intent(this, ChatActivity.class);
     			intent.putExtra("contact", contact);
     			intent.putExtra("current", currentUserContact);
     			startActivity(intent);
     		}
     	}
     }
     
     public void obtainLocation()
     {
 //    	TextView textView = (TextView)findViewById(R.id.textView3);
 //    	textView.setText("locating....");
 //    	
     	obtainLocationStartTime = Calendar.getInstance().getTimeInMillis();
     	Log.d("location", "starting getting location...");
     	
     	
     	
     	LocationListener locationListener = new LocationListener()
     	{
     		protected boolean isBetterLocation(Location location, Location currentBestLocation)
     		{
     			if (currentBestLocation == null) {
     		        // A new location is always better than no location
     		        return true;
     		    }
 
     		    // Check whether the new location fix is newer or older
     		    long timeDelta = location.getTime() - currentBestLocation.getTime();
     		    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
     		    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
     		    boolean isNewer = timeDelta > 0;
 
     		    // If it's been more than two minutes since the current location, use the new location
     		    // because the user has likely moved
     		    if (isSignificantlyNewer) {
     		        return true;
     		    // If the new location is more than two minutes older, it must be worse
     		    } else if (isSignificantlyOlder) {
     		        return false;
     		    }
 
     		    // Check whether the new location fix is more or less accurate
     		    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
     		    boolean isLessAccurate = accuracyDelta > 0;
     		    boolean isMoreAccurate = accuracyDelta < 0;
     		    boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
     		    // Check if the old and new location are from the same provider
     		    boolean isFromSameProvider = isSameProvider(location.getProvider(),
     		            currentBestLocation.getProvider());
 
     		    // Determine location quality using a combination of timeliness and accuracy
     		    if (isMoreAccurate) {
     		        return true;
     		    } else if (isNewer && !isLessAccurate) {
     		        return true;
     		    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
     		        return true;
     		    }
     		    return false;
     		}
     		
     		private boolean isSameProvider(String provider1, String provider2) {
     		    if (provider1 == null) {
     		      return provider2 == null;
     		    }
     		    return provider1.equals(provider2);
     		}
 
 			@Override
 			public void onLocationChanged(Location location) 
 			{
 			//	TextView textView = (TextView)findViewById(R.id.textView3);
 //				textView.setText("lat: " + location.getLatitude() 
 //						+ ", lng: " + location.getLongitude()
 //						+ ", acc: " + location.getAccuracy());
 				Log.d("location", "location changed! " + location.getLatitude() + "," + location.getLongitude() + 
 						", acc:" + location.getAccuracy());
 				
 				if(isBetterLocation(location, bestLocation))
 				{
 					bestLocation = location;
 //					textView.setText("lat: " + location.getLatitude() 
 //					+ ", lng: " + location.getLongitude()
 //					+ ", acc: " + location.getAccuracy());
 					
 					if(!locationUpdated)
 					{
 						logCurrentLocation();
 						locationUpdated = true;
 						
 						queryNearbyUsers();
 					}
 					
 				}
 				
 				if(Calendar.getInstance().getTimeInMillis() - obtainLocationStartTime > LOCATION_WAIT_TIME)
 				{
 					if(bestLocation == null)
 						obtainLocationStartTime = Calendar.getInstance().getTimeInMillis();
 					else
 					{
 						Log.d("location", "stop getting location updates");
 						locationManager.removeUpdates(this);
 					}
 				}
 			}
 
 			@Override
 			public void onProviderDisabled(String provider) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onProviderEnabled(String provider) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {
 				// TODO Auto-generated method stub
 				
 			}
     	};
     	
     	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, 
     			locationListener);
     	
     }
     
     private void logCurrentLocation()
     {
     	HttpPost logpost = new HttpPost("http://aaronplex.net/project/localin/log.php");
     	try
     	{
     		List<NameValuePair> params = new ArrayList<NameValuePair>();
     		String url = currentUser.getSiteStandardProfileRequest().getUrl();
     		Long mid = Long.parseLong(Uri.parse(url).getQueryParameter("key"));
    		params.add(new BasicNameValuePair("mid", "" + mid));
    		params.add(new BasicNameValuePair("id",""+currentUser.getId()));
     		params.add(new BasicNameValuePair("first", currentUser.getFirstName()));
     		params.add(new BasicNameValuePair("last", currentUser.getLastName()));
     		params.add(new BasicNameValuePair("headline", currentUser.getHeadline()));
     		params.add(new BasicNameValuePair("location", currentUser.getLocation().getName()));
     		params.add(new BasicNameValuePair("industry", currentUser.getIndustry()));
     		params.add(new BasicNameValuePair("pic", currentUser.getPictureUrl()));
     		params.add(new BasicNameValuePair("lat", "" + bestLocation.getLatitude()));
     		params.add(new BasicNameValuePair("lng", "" + bestLocation.getLongitude()));
     		logpost.setEntity(new UrlEncodedFormEntity(params));
     		
     		HttpResponse response = httpclient.execute(logpost);
     		if(response.getStatusLine().getStatusCode() == 200)
     		{
     			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
     			StringBuilder sb = new StringBuilder();
     			String line;
     			while((line = br.readLine()) != null)
     			{
     				sb.append(line + "\n");
     				//Log.d("info", line);
     			}
     			br.close();
     			Log.d("info", sb.toString());
     		}
     	}
     	catch(Exception e)
     	{
     		e.printStackTrace();
     		Log.d("info", "http post error!");
     	}
     }
     
     private void queryNearbyUsers()
     {
     	AsyncTask<Void, Void, JSONArray> mTask = new AsyncTask<Void, Void, JSONArray>() {
     		
             @Override
             protected void onPreExecute() {
                 // show the intro video
                 
             }
             
             @Override
             protected JSONArray doInBackground(Void... params) {
                 // Start authenticating...
                 // not using attemptAuth() since that's on a
                 // separate thread thereby causing synchronization
                 // issues
                 // Note: since "handler" parameter is null in authenticate(), the callback to AuthenticatorActivity
                 // will not occur
             	HttpGet queryget = new HttpGet("http://aaronplex.net/project/localin/q.php?lat=" + bestLocation.getLatitude() + "&lng=" + bestLocation.getLongitude());
             	JSONArray array = null;
             	try
             	{
             		HttpResponse response = httpclient.execute(queryget);
             		if(response.getStatusLine().getStatusCode() == 200)
             		{
             			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
             			StringBuilder sb = new StringBuilder();
             			String line;
             			while((line = br.readLine()) != null)
             			{
             				sb.append(line + "\n");
             				//Log.d("info", line);
             			}
             			br.close();
             			Log.d("info", sb.toString());
             			
             			array = (JSONArray)JSONValue.parse(sb.toString());
             			
             			
             		}
             	}
             	catch(Exception e)
             	{
             		e.printStackTrace();
             		Log.d("info", "http get error");
             	}
             	return array;
             }
 
             @Override
             protected void onPostExecute(JSONArray result) {
             	loadNearbyUsers(result);
             }
         };
         mTask.execute((Void[]) null);    	
     }
     
     private void loadNearbyUsers(JSONArray array)
     {
     	ArrayList<Contact> contacts = new ArrayList<Contact>();
     	for(int i = 0; i < array.size(); i++)
     	{
     		JSONObject user = (JSONObject)array.get(i);
     		Contact contact = new Contact(
 		              Long.parseLong((String)user.get("memberId")),
 		              (String)user.get("id"),
 		              (String)user.get("firstName"),
 		              (String)user.get("lastName"),
 		              (String)user.get("headline"),
 		              (String)user.get("region"),
 		              (String)user.get("industry"),
 		              (String)user.get("pictureUrl"),
 		              Double.parseDouble((String)user.get("lat")),
 		              Double.parseDouble((String)user.get("lng")),
 		              (String)user.get("timestamp"));
     		contact.setDistance(distanceByLatLng(bestLocation.getLatitude(), bestLocation.getLongitude(),
     							contact.getLatitude(), contact.getLongitude()));
     		contacts.add(contact);		            		  
     	}
     	Collections.sort(contacts);
     	ContactListAdapter adapter = new ContactListAdapter(this, contacts);
     	
     	listView.setAdapter(adapter);
     }
     
     
 public int updateDB(int userid, Timestamp timestamp, String lastSentence){
     	
     	//step one, store the sensor information and get a row id, uri should just be the normal uri
     	ContentValues values = new ContentValues();
     	
     	values.put(ConversationProvider.OTHERID, userid);
     	values.put(ConversationProvider.LASTSENTENCE, lastSentence);
     	values.put(ConversationProvider.TIMESTAMP, timestamp.getTime());
     	ContentResolver resolver = getContentResolver();
     	
     	
     	String base = ConversationProvider.RECORD_URI.toString();
     	Uri uri = Uri.parse(base+"/"+userid);
     	int count = resolver.update(uri, values,null,null);
     	return count;
     }
     
     public ArrayList<ContentValues> queryDB() throws Throwable{
         ContentResolver contentResolver = this.getContentResolver();
         //Uri uri = Uri.parse("content://com.ljq.provider.personprovider/person");
         Uri uri = ConversationProvider.RECORD_URI;
         Cursor cursor = contentResolver.query(uri, null, null, null, null);
         ArrayList<ContentValues> values = new ArrayList<ContentValues>();
         while(cursor.moveToNext()){
             ContentValues bundle = new ContentValues();
             int userid = cursor.getInt(0);
             long timestamp = cursor.getLong(1);
             String lastMsg = cursor.getString(2);
             bundle.put(ConversationProvider.OTHERID, userid);
             bundle.put(ConversationProvider.TIMESTAMP, timestamp);
             bundle.put(ConversationProvider.LASTSENTENCE, lastMsg);
             values.add(bundle);
         }
         cursor.close();
         return values;
     }
     
     public int deleteDB(int userid){
     	String base = ConversationProvider.RECORD_URI.toString();
     	Uri uri = Uri.parse(base+"/"+userid);
     	ContentResolver contentResolver = this.getContentResolver();
         return contentResolver.delete(uri, null, null);
     }
     
     public int storeDB(int userid, Timestamp timestamp, String lastSentence){
     	Uri mRecordUri;
     	
     	//step one, store the sensor information and get a row id, uri should just be the normal uri
     	ContentValues values = new ContentValues();
     	
     	values.put(ConversationProvider.OTHERID, userid);
     	values.put(ConversationProvider.LASTSENTENCE, lastSentence);
     	values.put(ConversationProvider.TIMESTAMP, timestamp.getTime());
     	ContentResolver resolver = getContentResolver();
     	mRecordUri = resolver.insert(ConversationProvider.RECORD_URI, values);
     	
     	//should be the id
     	final String row_id = mRecordUri.getLastPathSegment();
     	return Integer.parseInt(row_id);       
     }
     
     public static double distanceByLatLng(double lat1, double lng1, double lat2, double lng2)
     {
     	double R = 6371;
     	double toRad = Math.PI / 180.0;
     	double dLat = (lat2 - lat1) * toRad;
     	double dLng = (lng2 - lng1) * toRad;
     	double rLat1 = lat1 * toRad;
     	double rLat2 = lat2 * toRad;
     	
     	double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2) + Math.sin(dLng / 2.0) * Math.sin(dLng / 2.0) * Math.cos(rLat1) * Math.cos(rLat2);
     	double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
     	double d = R * c;
     	
     	return d * 0.621371; // km to miles
     	
 //    	double convert = Math.PI / 180.0;
 //    	double t1 = Math.sin(lat1 * convert);
 //    	double t2 = Math.cos(lat1 * convert);
 //    	double t3 = Math.cos(lng1 * convert - lng1 * convert);
 //    	double t4 = t2 * t3;
 //    	double t5 = t1 + t4;
 //    	double rad_dist = Math.atan(-t5 / Math.sqrt(-t5 * t5 + 1)) + 2 * Math.atan(1);
 //    	return rad_dist * 3437.74677 * 1.1508;
     }
 }
