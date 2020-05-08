 package cs585_hw3.team33;
 
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.WindowManager;
 import android.widget.TabHost;

import com.google.android.maps.GeoPoint;

 import cs585_hw3.team33.browse.BrowseActivity;
 import cs585_hw3.team33.lib.DatabaseHelper;
 import cs585_hw3.team33.lib.ObservableLocationListener;
 import cs585_hw3.team33.lib.ObservableLocationListener.LocationObserver;
 import cs585_hw3.team33.manage.ManageActivity;
 import cs585_hw3.team33.post.PostActivity;
 
 public class MainActivity extends TabActivity implements LocationObserver {
 	public DatabaseHelper dh;
 	public GeoPoint current_loc = null;
 	
 	private LocationManager lm;
 	public ObservableLocationListener locationListener;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         
         dh = new DatabaseHelper(this);
 
         // Make the Tabs        
         Resources res = getResources(); // Resource object to get Drawables
         TabHost tabHost = getTabHost();  // The activity TabHost
         TabHost.TabSpec spec;
         Intent intent;  
 
         // Initialize a TabSpec for each tab and add it to the TabHost
         intent = new Intent().setClass(this, ManageActivity.class);
         spec = tabHost.newTabSpec("manage")
         .setIndicator(res.getText(R.string.manage_tab_lbl),null)
         .setContent(intent);
         tabHost.addTab(spec);
 
         intent = new Intent().setClass(this, PostActivity.class);
         spec = tabHost.newTabSpec("post")
         .setIndicator(res.getText(R.string.post_tab_lbl),null)
         .setContent(intent);
         tabHost.addTab(spec);
 
         intent = new Intent().setClass(this, BrowseActivity.class);
         spec = tabHost.newTabSpec("browse")
         .setIndicator(res.getText(R.string.browse_tab_lbl),null)
         .setContent(intent);
         tabHost.addTab(spec);
 
         tabHost.setCurrentTab(0);
         
         lm = (LocationManager) 
         getSystemService(Context.LOCATION_SERVICE);    
     
         locationListener = new ObservableLocationListener();
         locationListener.list.add(this);
     
     }
     @Override
 	public void locationChanged(GeoPoint p) {
     	current_loc = p;
 	}
     @Override
     public void onResume() {
     	 super.onResume();
     	 lm.requestLocationUpdates(
     	            LocationManager.GPS_PROVIDER, 
     	            0, 
     	            0, 
     	            locationListener);    	        
     }
     @Override
     public void onPause() {
     	super.onPause();
     	lm.removeUpdates(locationListener);
     	
     }
 
 	
 }
