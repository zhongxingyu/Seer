 package com.overlakehome.locals;
 
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.widget.TabHost;
 
 public class MainActivity extends TabActivity implements LocationListener {
     private static final Location DENNY_PARK_SEATTLE_WA = new Location(LocationManager.GPS_PROVIDER) {{
         setLatitude(47.6192649);
         setLongitude(-122.3404047);
     }};
 
     private LocationManager locationManager = null;
 
     public void onCreate(Bundle savedInstanceState) {
         locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 5, this); // 60 seconds & 5 meters
 
         Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         onLocationChanged(location != null ? location : DENNY_PARK_SEATTLE_WA);
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         // Reference: http://developer.android.com/resources/tutorials/views/hello-tabwidget.html
         Resources res = getResources();
         TabHost tabHost = getTabHost();
 
         tabHost.addTab(tabHost.newTabSpec("places")
                 .setIndicator("Places", res.getDrawable(R.drawable.ic_tab_places))
                 .setContent(new Intent().setClass(this, PlacesActivity.class)));
 
         tabHost.addTab(tabHost.newTabSpec("deals")
                 .setIndicator("Deals", res.getDrawable(R.drawable.ic_tab_deals))
                 .setContent(new Intent().setClass(this, DealsActivity.class)));
 
         tabHost.addTab(tabHost.newTabSpec("trends")
                 .setIndicator("Trends", res.getDrawable(R.drawable.ic_tab_places))
                 .setContent(new Intent().setClass(this, TrendsActivity.class)));
 
         tabHost.addTab(tabHost.newTabSpec("bookmarks")
                 .setIndicator("Bookmarks", res.getDrawable(R.drawable.ic_tab_bookmarks))
                 .setContent(new Intent().setClass(this, BookmarksActivity.class)));
 
        tabHost.setCurrentTab(0);
     }
 
     @Override
     public void onLocationChanged(Location location) {
         if (null != location) {
             NearBys.getInstance().setLocation(location);
             NearBys.getInstance().findNearBys();
         }
     }
 
     @Override
     public void onProviderDisabled(String provider) {
     }
 
     @Override
     public void onProviderEnabled(String provider) {
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {
     }
 }
