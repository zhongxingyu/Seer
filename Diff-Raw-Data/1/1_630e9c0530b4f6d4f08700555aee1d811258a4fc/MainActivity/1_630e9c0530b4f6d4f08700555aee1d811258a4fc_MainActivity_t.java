 /*
  * Copyright 2011 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.net.rmopenmenu;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 import android.widget.TabHost;
 import android.widget.Toast;
 
 import com.net.rmopenmenu.SearchActivity.TabsAdapter;
 
 public class MainActivity extends ActionBarActivity {
 	
 	TabHost mTabHost;
     ViewPager  mViewPager;
     TabsAdapter mTabsAdapter;
     LocationManager locationManager;
     LocationListener locationListener;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         
         setContentView(R.layout.fragment_tabs_pager);
         
         mTabHost = (TabHost)findViewById(android.R.id.tabhost);
         mTabHost.setup();
 
         mViewPager = (ViewPager)findViewById(R.id.pager);
 
         mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
         
     	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	        
         Bundle b1 = new Bundle();
         b1.putBoolean("menu", true);
         
         Bundle b2 = new Bundle();
         b2.putBoolean("menu", false);
 
         mTabsAdapter.addTab(mTabHost.newTabSpec("menu").setIndicator("Menu"),
                 MenuFragment.class, b1);
         mTabsAdapter.addTab(mTabHost.newTabSpec("restaurant").setIndicator("Restaurant"),
                 MenuFragment.class, b2);
         if (savedInstanceState != null) {
             mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
         }
 		
 		// Acquire a reference to the system Location Manager
 		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
 		// Define a listener that responds to location updates
 	    locationListener = new LocationListener() {
 		    public void onStatusChanged(String provider, int status, Bundle extras) {}
 
 		    public void onProviderEnabled(String provider) {}
 
 		    public void onProviderDisabled(String provider) {}
 
 			@Override
 			public void onLocationChanged(Location location) {
 				SharedPreferences.Editor editor = prefs.edit();
 								
 				editor.putInt("lat", (int)(location.getLatitude()*1000000));
 				editor.putInt("lon", (int)(location.getLongitude()*1000000));
 				
 				editor.commit();
 			}
 		  };
 
 		// Register the listener with the Location Manager to receive location updates
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 	}
     
     @Override
     public void onPause() {
     	super.onPause();
     	locationManager.removeUpdates(locationListener);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater menuInflater = getMenuInflater();
         menuInflater.inflate(R.menu.main, menu);
                 
         // Calling super after populating the menu is necessary here to ensure that the
         // action bar helpers have a chance to handle this event.
         return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onSearchRequested() {
          Bundle appData = new Bundle();
          String tag = mTabHost.getCurrentTabTag();
          boolean menu = tag.equals("menu")? true : false;
          
          appData.putBoolean("menu", menu);
          startSearch(null, false, appData, false);
          return true;
      }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 break;
 
             case R.id.menu_refresh:
                 getActionBarHelper().setRefreshActionItemState(true);
                 getWindow().getDecorView().postDelayed(
                         new Runnable() {
                             @Override
                             public void run() {
                                 getActionBarHelper().setRefreshActionItemState(false);
                             }
                         }, 3000);
                 break;
 
             case R.id.menu_search:
             	onSearchRequested();
                 break;
 
             case R.id.menu_share:
                 Toast.makeText(this, "Tapped share", Toast.LENGTH_SHORT).show();
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 }
