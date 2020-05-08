 package mmt.uit.placehelper.activities;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Locale;
 
 import com.google.android.maps.GeoPoint;
 
 import mmt.uit.placehelper.R;
 import mmt.uit.placehelper.models.MainGroup;
 import mmt.uit.placehelper.utilities.CatParser;
 import mmt.uit.placehelper.utilities.CheckConnection;
 import mmt.uit.placehelper.utilities.ConstantsAndKey;
 import mmt.uit.placehelper.utilities.ExpListAdapter;
 import mmt.uit.placehelper.utilities.LocationHelper;
 import mmt.uit.placehelper.utilities.LocationHelper.LocationResult;
 import mmt.uit.placehelper.utilities.PointAddressUtil;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Looper;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ExpandableListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.ExpandableListView.OnGroupClickListener;
 import android.widget.ExpandableListView.OnGroupExpandListener;
 
 public class CategoriesListActivity extends Activity {	
 	private List<MainGroup> lsGroup = new ArrayList<MainGroup>();    
 	private ExpListAdapter adapter;
 	private ExpandableListView listView ;
 	private TextView cur_add;
 	private ProgressBar loc_progress;
 	private Context mContext;
 	boolean gps_enabled = false;
 	boolean network_enabled = false;	
 	private Location currentLoc=null;
 	private String mProviderName;	
 	private SharedPreferences mSharePref;
 	private boolean hasLocation=false;	
 	private LocationHelper locHelper;
 	private GetLocation getLoc;
 	private String lang = "en";	
 	private static final int FAVORITE_GROUP = 900;
 	private static final int SETTING_GROUP = 800;
 	private static final int CHILD_ADD = 701;
 	private static final int MAX_AGE = 2*60*1000;//Max age of location cache
     /** Called when the activity is first created. */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         mContext = this;
     	super.onCreate(savedInstanceState);
     	Configuration mConfig = new Configuration(getResources().getConfiguration());
     	mSharePref = PreferenceManager.getDefaultSharedPreferences(mContext);
     	if(mSharePref!=null){
     	lang = mSharePref.getString(getResources().getString(R.string.prefkey_lang), getResources().getStringArray(R.array.arr_lang_value)[0]);    	
     	mConfig.locale = new Locale(lang);
     	getResources().updateConfiguration(mConfig, getResources().getDisplayMetrics());
     	}
     	
         setContentView(R.layout.ph_catlist);
         
         // Retrive the ExpandableListView from the layout
         listView = (ExpandableListView) findViewById(R.id.explist);
         listView.setDividerHeight(0);
         cur_add = (TextView)findViewById(R.id.cur_address);
         loc_progress = (ProgressBar)findViewById(R.id.loc_progress);
         //Call task to create category list.
         CreateCat cc = new CreateCat();
         cc.execute();
         // Collapse all groups that are not clicked        
         listView.setOnGroupExpandListener(new OnGroupExpandListener() {
         	public void onGroupExpand(int groupPosition) {
         		int len = adapter.getGroupCount();
         	    for (int i = 0; i < len; i++) {
         	    	if (i!=groupPosition || lsGroup.get(i).getChild().size()==0) {
         	    		listView.collapseGroup(i);        	    		
         	        }
         	    }
         	    
         	}
         });
 
         listView.setOnChildClickListener(new OnChildClickListener()
         {
             
             @Override
             public boolean onChildClick(ExpandableListView expLv, View v, int groupPos, int childPos, long id)
             {
                 if(lsGroup.get(groupPos).getChild().get(childPos).getId()!=CHILD_ADD){
             	startSearch(getResources().getString(lsGroup.get(groupPos).getChild().get(childPos).getName()),lsGroup.get(groupPos).getChild().get(childPos).getImgID(),
             			lsGroup.get(groupPos).getChild().get(childPos).getTypes());            	
                 return true;}
                 return false;
             }
         });
         
         listView.setOnGroupClickListener(new OnGroupClickListener()
         {
             
             @Override
             public boolean onGroupClick(ExpandableListView expLv, View v, int groupPos, long id)
             {
             	            	
             	if(lsGroup.get(groupPos).getId()==FAVORITE_GROUP){
             		Intent intent = new Intent(getApplicationContext(), ListFavoriteActivity.class);        			
         			startActivity(intent);
         			return true;
             	}
             	if(lsGroup.get(groupPos).getId()==SETTING_GROUP){
             		Intent intSetting = new Intent(getApplicationContext(),SettingsActivity.class);
         			startActivity(intSetting);
             		return true;
             	}
             	if(lsGroup.get(groupPos).getChild().size()==0){
                 	startSearch(getResources().getString(lsGroup.get(groupPos).getName()),lsGroup.get(groupPos).getImgID(),lsGroup.get(groupPos).getTypes());
                 	return true;
                 }
                 
                 return false;
             }
         });
 
         locHelper = new LocationHelper();
         locHelper.getLocation(mContext, locResult);
         getLoc = new GetLocation();
         getLoc.execute(mContext);
 
         
     }
     
     @Override
     protected void onStop() {
     	
     	super.onStop();
     	//When activity stop also stop update location and stop task too.
     	locHelper.stopLocationUpdates();
     	getLoc.cancel(true);
     }
     
     public LocationResult locResult = new LocationResult() {
 		
 		@Override
 		public void gotLocation(Location location) {
			if (location!=null){
 			currentLoc = new Location(location);
 			hasLocation = true;
			}
 		}
 	};
     
     private void startSearch(String place, int img, String types){
 		Log.v("ph_info", "Start Search");
 		if (CheckConnection.checkInternet(mContext)){
 		if (currentLoc!=null){
 			
 		Bundle b = new Bundle();					
 		b.putString("search", place);		
 			b.putDouble("curlat", currentLoc.getLatitude());			
 			b.putDouble("curlng", currentLoc.getLongitude());
 			b.putInt("img", img);
 			b.putString("types", types);
 			
 		Intent mIntent = new Intent(getApplicationContext(),
 				SearchResultActivity.class);
 		mIntent.putExtras(b);
 		startActivity(mIntent);
 		}		
 		}
 		else 
 			Toast.makeText(mContext, R.string.error_network, Toast.LENGTH_LONG);
 		}	
     //Get location 
     private Location getLocation() {
 		LocationManager locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
 		Criteria criteria = new Criteria();
 		Location lastestLoc;
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);		
 		MyLocationListener listener = new MyLocationListener();
 		mProviderName = locMgr.getBestProvider(criteria, true);		
 		if (mProviderName == null) {
 			showDialog(ConstantsAndKey.CHECK_SETTING);			
 			return null;
 		} 
 		
 			lastestLoc = locMgr.getLastKnownLocation(mProviderName);
 			if (lastestLoc !=null && (System.currentTimeMillis() - lastestLoc.getTime() < MAX_AGE) ){
 				return lastestLoc;	
 				}
 				else {
 				locMgr.requestLocationUpdates(mProviderName, 0, 0, listener);
 				
 			}
 			
 			return lastestLoc;
 		
 	}
     
     //Create menu
     
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mn_mainscreen, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId())
 		{
 		case R.id.mn_lstfavorite:
 			Intent intent = new Intent(getApplicationContext(), ListFavoriteActivity.class);
 			Bundle b = new Bundle();
 			if (currentLoc!=null){
 				b.putDouble("curlat", currentLoc.getLatitude());			
 				b.putDouble("curlon", currentLoc.getLongitude());
 			}
 			intent.putExtras(b);
 			startActivity(intent);
 			return true;
 		case R.id.mn_changle_location:
 			Intent intent2 = new Intent(getApplicationContext(), ChangeLocationActivity.class);
 			startActivity(intent2);
 			return true;
 		case R.id.mn_about:			
 			showDialog(ConstantsAndKey.ABOUT);
 			return true;
 		case R.id.setting:
 			Intent intSetting = new Intent(getApplicationContext(),SettingsActivity.class);
 			startActivity(intSetting);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
     
     private void getCurrentLocation(){
 		//mSharePref = getSharedPreferences(Constants.PREF_NAME,0);
 		
 		try {
 			/*if (mSharePref!=null)
 				showDialog(Constants.RESTORE_LOC);
 			else*/
 			getLocation();
 		}
 		catch (Exception e) {
 				Log.v(ConstantsAndKey.TAG_EXCEPTION, e.toString());
 			
 			}
 	}
     
     protected Dialog onCreateDialog(int id) {
 
 		switch (id) {
 		case ConstantsAndKey.CHECK_SETTING:
 			return new AlertDialog.Builder(this).setTitle(R.string.alert).setMessage(
 					R.string.gps_off)
 					.setPositiveButton(R.string.setting,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									Intent callGPSSettingIntent = new Intent(
 											android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 									startActivity(callGPSSettingIntent);
 								}
 							}).setNegativeButton(R.string.cancel,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									
 								}
 							}).create();
 		case ConstantsAndKey.WAIT_MSG:
 			return new ProgressDialog(this).show(this,getResources().getText(R.string.wait_plz),getResources().getText(R.string.get_location), true,true);
 		case ConstantsAndKey.LOC_NOTFOUND:
 			return new AlertDialog.Builder(this)
 					.setMessage(
 							R.string.get_loca_error)
 					.setNegativeButton(R.string.ok, null).show();
 		case ConstantsAndKey.RESTORE_LOC: 
 			return new AlertDialog.Builder(this)
             .setIcon(R.drawable.app_icon)
             .setTitle(R.string.ask_save)
             .setPositiveButton("CĂ³", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                 	currentLoc = new Location("network");
                     currentLoc.setLatitude(mSharePref.getFloat(ConstantsAndKey.KEY_LAT, 0));
                     currentLoc.setLongitude(mSharePref.getFloat(ConstantsAndKey.KEY_LNG, 0));
                 }
             })
             .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
 
                     try{
                     	getLocation();
                     }
                     catch (Exception e){
                     	Log.v(ConstantsAndKey.TAG_EXCEPTION, e.toString());
                     }
                 }
             })
             .create();
 			
 		case ConstantsAndKey.ABOUT:
             return new AlertDialog.Builder(this)
                 .setIcon(R.drawable.about)
                 .setTitle(R.string.about_title)
                 .setMessage(R.string.about_msg)
                 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
     
                         dismissDialog(ConstantsAndKey.ABOUT);
                     }
                 })
                 
                 .create();
 		}
 		return null;
 	}
     
     class MyLocationListener implements LocationListener {
 
 		@Override
 		public void onLocationChanged(Location location) {
 
 			if (location != null) {
 				currentLoc = location;
 								
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
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// TODO Auto-generated method stub
 
 		}
 	}
     
     //Task to parse xml and create main screen
     private class CreateCat extends AsyncTask<Void, Void, List<MainGroup>>{
 
 		@Override
 		protected List<MainGroup> doInBackground(Void... params) {			
 			CatParser dp = new CatParser(mContext);
 	    	List<MainGroup> results = dp.parse();
 	    	return results;
 		}
 		
 		@Override
 		protected void onPostExecute(List<MainGroup> result) {
 			
 			if (result !=null){
 				lsGroup = result;				
 				adapter = new ExpListAdapter(mContext, lsGroup);
 		        listView.setAdapter(adapter);
 			}
 		}
     	
     }
     
     private class GetLocation extends AsyncTask<Context, Void, Void>{
     	
 
          protected void onPreExecute()
          {
              
              loc_progress.setVisibility(View.VISIBLE);
          }
 
          @Override 
          protected Void doInBackground(Context... params)
          {
              //Wait 30 seconds to see if we can get a location from either network or GPS, otherwise stop
              Long t = Calendar.getInstance().getTimeInMillis();
              while (!hasLocation && Calendar.getInstance().getTimeInMillis() - t < 15000) {
                  try {
                      Thread.sleep(30000);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              };
              return null;
          }
 
          protected void onPostExecute(final Void unused)
          {
              
              if (currentLoc != null)
              {
             	 loc_progress.setVisibility(View.GONE);
             	 GeoPoint point = new GeoPoint(
    			          (int) (currentLoc.getLatitude() * 1E6), 
    			          (int) (currentLoc.getLongitude() * 1E6));
    			String currentAdd = mContext.getResources().getString(R.string.near);		
    			currentAdd += PointAddressUtil.ConvertPointToAddress(point, mContext);
 	   		if (currentAdd!=null){
 	   			cur_add.setText(currentAdd);
 	   		}
              }
              else
              {
                  cur_add.setText(R.string.err_address);
                 loc_progress.setVisibility(View.GONE);
              }
          }
 
     	
     }
 }
