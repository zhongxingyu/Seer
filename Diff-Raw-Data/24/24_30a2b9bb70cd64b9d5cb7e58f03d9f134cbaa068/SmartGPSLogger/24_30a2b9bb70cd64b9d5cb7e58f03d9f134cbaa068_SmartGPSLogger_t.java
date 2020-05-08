 /*
  * Copyright (C) 2012 Michel Sébastien & Jérémy Compostella
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.oux.SmartGPSLogger;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 import android.app.SearchManager;
 import android.content.ServiceConnection;
 import java.util.LinkedList;
 import android.content.ComponentName;
 import android.os.IBinder;
 import android.location.Location;
 import com.google.android.maps.MyLocationOverlay;
 import android.graphics.Color;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import android.text.format.DateFormat;
 
 public class SmartGPSLogger extends MapActivity implements LocationUpdate
 {
     private static final String TAG = "SmartGPSLogger";
 
     // Identifiers for option menu items
     private static final int MENU_RECLOG = Menu.FIRST;
     private static final int MENU_STOPLOG = MENU_RECLOG + 1;
     private static final int MENU_FORCE = MENU_STOPLOG + 1;
     private static final int MENU_SETTINGS = MENU_FORCE + 1;
 
     private Intent mService;
     private MyLocationOverlay me;
     private MapView map;
     private TextView text;
     private GPSService.MyBinder binder;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         Settings.getInstance(this);
         setContentView(R.layout.main);
 
         map = (MapView)findViewById(R.id.map);
         me = new MyLocationOverlay(this, map);
         map.getOverlays().add(me);
         map.setBuiltInZoomControls(true);
 
         text = (TextView)findViewById(R.id.text_area);
 
         mService = new Intent(this, GPSService.class);
         bindService(mService, connection, Context.BIND_AUTO_CREATE);
     }
 
     @Override
     protected boolean isRouteDisplayed()
     {
         return false;
     }
 
     @Override
     protected void onDestroy()
     {
         super.onDestroy();
         unbindService(connection);
     }
 
     @Override
     protected void onPause()
     {
         super.onPause();
         me.disableCompass();
     }
 
     @Override
     protected void onResume()
     {
         super.onResume();
         me.enableCompass();
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu)
     {
         menu.clear();
        if (binder != null) {
            if (!binder.isRunning())
                menu.add(0, MENU_RECLOG, 0, R.string.menu_reclog)
                    .setIcon(R.drawable.ic_reclog)
                    .setAlphabeticShortcut(SearchManager.MENU_KEY);
            else {
                menu.add(0, MENU_STOPLOG, 0, R.string.menu_stoplog)
                    .setIcon(R.drawable.ic_stoplog)
                    .setAlphabeticShortcut(SearchManager.MENU_KEY);
                menu.add(0, MENU_FORCE, 0, R.string.menu_force)
                    .setIcon(R.drawable.ic_reclog)
                    .setAlphabeticShortcut(SearchManager.MENU_KEY);
            }
         }
         menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings)
             .setIcon(android.R.drawable.ic_menu_preferences)
             .setIntent(new Intent(this,Preferences.class))
             .setAlphabeticShortcut(SearchManager.MENU_KEY);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case MENU_RECLOG:
         case MENU_FORCE:
             this.startService(mService);
             return true;
         case MENU_STOPLOG:
             this.binder.stopUpdates();
             Log.d(TAG, "stopped");
             return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     public void newLocation(Location loc)
     {
         updateText(System.currentTimeMillis());
     }
 
     private void updateText(long lastGPSFixTime)
     {
         if (lastGPSFixTime == 0)
             text.setText("Last GPS fix information is not available");
         else
             text.setText("Last GPS fix at + " + DateFormat.format("yyyy:MM:dd kk:mm:ss", lastGPSFixTime));
 
         if (lastGPSFixTime >= System.currentTimeMillis() - Settings.getInstance().maxPeriod() * 60 * 1000)
             text.setTextColor(Color.GREEN);
         else
             text.setTextColor(Color.RED);
     }
 
     private ServiceConnection connection = new ServiceConnection()
     {
         private PathOverlay path;
 
         public void onServiceConnected(ComponentName className, IBinder binder)
         {
             SmartGPSLogger.this.binder = (GPSService.MyBinder)binder;
             LinkedList<Location> locations = SmartGPSLogger.this.binder.getLocations();
             path = new PathOverlay(locations);
             map.getOverlays().add(path);
             SmartGPSLogger.this.updateText(SmartGPSLogger.this.binder.getLastGPSFixTime());
             SmartGPSLogger.this.binder.registerLocationUpdate(SmartGPSLogger.this);
             SmartGPSLogger.this.binder.registerLocationUpdate(path);
         }
 
         public void onServiceDisconnected(ComponentName className)
         {
             map.getOverlays().remove(path);
             SmartGPSLogger.this.binder.unregisterLocationUpdate(SmartGPSLogger.this);
             SmartGPSLogger.this.binder.unregisterLocationUpdate(path);
         }
     };
 }
 // vi:et
