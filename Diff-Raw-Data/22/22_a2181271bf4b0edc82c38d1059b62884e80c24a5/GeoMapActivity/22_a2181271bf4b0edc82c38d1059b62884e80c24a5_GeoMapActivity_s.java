 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.activity.map;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.database.DatabaseDI;
 import com.google.code.geobeagle.database.GeocachesSql;
 import com.google.code.geobeagle.database.ISQLiteDatabase;
 import com.google.code.geobeagle.database.WhereFactoryNearestCaches;
 import com.google.code.geobeagle.database.DatabaseDI.GeoBeagleSqliteOpenHelper;
 import com.google.code.geobeagle.database.DatabaseDI.SearchFactory;
 import com.google.code.geobeagle.database.WhereFactoryNearestCaches.WhereStringFactory;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import java.util.List;
 
 public class GeoMapActivity extends MapActivity {
     GeoMapActivityDelegate mGeoMapActivityDelegate;
 
     @Override
     protected boolean isRouteDisplayed() {
         // This application doesn't use routes
         return false;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.map);
         final MapView mapView = (MapView)findViewById(R.id.mapview);
         final GeoBeagleSqliteOpenHelper open = new GeoBeagleSqliteOpenHelper(this);
         final SQLiteDatabase sqDb = open.getReadableDatabase();
         final ISQLiteDatabase database = new DatabaseDI.SQLiteWrapper(sqDb);
         final GeocachesSql geocachesSql = DatabaseDI.createGeocachesSql(database);
         final WhereStringFactory whereStringFactory = new WhereStringFactory();
         final SearchFactory searchFactory = new SearchFactory();
         final WhereFactoryNearestCaches whereFactory = new WhereFactoryNearestCaches(searchFactory,
                 whereStringFactory);
         final Drawable drawable = getResources().getDrawable(R.drawable.map_others);
         final MapItemizedOverlay mapItemizedOverlay = new MapItemizedOverlay(this, drawable);
         final MyLocationOverlay myLocationOverlay = new MyLocationOverlay(this, mapView);
         final MapController mapController = mapView.getController();
         final List<Overlay> mapOverlays = mapView.getOverlays();
 
        mGeoMapActivityDelegate = new GeoMapActivityDelegate(this, mapView,
                getApplicationContext(), myLocationOverlay);
        mGeoMapActivityDelegate.initialize(geocachesSql, whereFactory, mapItemizedOverlay,
                 mapController, mapOverlays);
         open.close();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         mGeoMapActivityDelegate.onResume();
     }
 
     @Override
     public void onPause() {
         mGeoMapActivityDelegate.onPause();
         super.onPause();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.map_menu, menu);
         // return mCacheListDelegate.onCreateOptionsMenu(menu);
 
         return true;
     }
 
     @Override
     public boolean onMenuOpened(int featureId, Menu menu) {
         return mGeoMapActivityDelegate.onMenuOpened(featureId, menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         return mGeoMapActivityDelegate.onOptionsItemSelected(item);
     }
 }
