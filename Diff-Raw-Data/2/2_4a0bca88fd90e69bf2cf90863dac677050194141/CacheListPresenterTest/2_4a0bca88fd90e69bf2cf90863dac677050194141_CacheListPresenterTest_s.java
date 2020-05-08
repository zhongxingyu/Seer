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
 
 package com.google.code.geobeagle.ui.cachelist;
 
 import static org.easymock.EasyMock.expect;
 
 import com.google.code.geobeagle.CombinedLocationManager;
 import com.google.code.geobeagle.LocationControlBuffered;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.Refresher;
 import com.google.code.geobeagle.data.GeocacheVectors;
 import com.google.code.geobeagle.io.Database;
 import com.google.code.geobeagle.io.DatabaseDI.SQLiteWrapper;
 import com.google.code.geobeagle.ui.ErrorDisplayer;
 import com.google.code.geobeagle.ui.GpsStatusWidget.UpdateGpsWidgetRunnable;
 import com.google.code.geobeagle.ui.cachelist.GeocacheListController.CacheListOnCreateContextMenuListener;
 import com.google.code.geobeagle.ui.cachelist.GeocacheListPresenter.CacheListRefreshLocationListener;
 import com.google.code.geobeagle.ui.cachelist.GeocacheListPresenter.CompassListener;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.app.ListActivity;
 import android.hardware.SensorManager;
 import android.location.LocationListener;
 import android.util.Log;
 import android.view.View;
 import android.widget.ListView;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
         GeocacheListPresenter.class, Log.class
 })
 public class CacheListPresenterTest {
     @Test
     public void testBaseAdapterLocationListener() {
         PowerMock.mockStatic(Log.class);
         CacheListRefresh cacheListRefresh = PowerMock.createMock(CacheListRefresh.class);
 
         EasyMock.expect(Log.v((String)EasyMock.anyObject(), (String)EasyMock.anyObject()))
                 .andReturn(0);
         cacheListRefresh.refresh();
 
         PowerMock.replayAll();
         CacheListRefreshLocationListener cacheListRefreshLocationListener = new CacheListRefreshLocationListener(
                 cacheListRefresh);
         cacheListRefreshLocationListener.onLocationChanged(null);
         cacheListRefreshLocationListener.onProviderDisabled(null);
         cacheListRefreshLocationListener.onProviderEnabled(null);
         cacheListRefreshLocationListener.onStatusChanged(null, 0, null);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnCreate() throws Exception {
         ListActivity listActivity = PowerMock.createMock(ListActivity.class);
         LocationControlBuffered locationControlBuffered = PowerMock
                 .createMock(LocationControlBuffered.class);
         CacheListOnCreateContextMenuListener listener = PowerMock
                 .createMock(CacheListOnCreateContextMenuListener.class);
         ListView listView = PowerMock.createMock(ListView.class);
         GeocacheVectors geocacheVectors = PowerMock.createMock(GeocacheVectors.class);
         UpdateGpsWidgetRunnable updateGpsWidgetRunnable = PowerMock
                 .createMock(UpdateGpsWidgetRunnable.class);
         View gpsWidgetView = PowerMock.createMock(View.class);
         GeocacheListAdapter geocacheListAdapter = PowerMock.createMock(GeocacheListAdapter.class);
 
         listActivity.setContentView(R.layout.cache_list);
         PowerMock.expectNew(CacheListOnCreateContextMenuListener.class, geocacheVectors).andReturn(
                 listener);
         expect(listActivity.getListView()).andReturn(listView);
         listView.addHeaderView(gpsWidgetView);
         listView.setOnCreateContextMenuListener(listener);
         updateGpsWidgetRunnable.run();
         listActivity.setListAdapter(geocacheListAdapter);
 
         PowerMock.replayAll();
         new GeocacheListPresenter(null, locationControlBuffered, locationControlBuffered,
                 gpsWidgetView, updateGpsWidgetRunnable, geocacheVectors, null, listActivity,
                 geocacheListAdapter, null, null, null, null, null).onCreate();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnPause() throws InterruptedException {
         CombinedLocationManager combinedLocationManager = PowerMock
                 .createMock(CombinedLocationManager.class);
         LocationListener gpsStatusWidgetLocationListener = PowerMock
                 .createMock(LocationListener.class);
         CacheListRefreshLocationListener cacheListRefreshLocationListener = PowerMock
                 .createMock(CacheListRefreshLocationListener.class);
         SQLiteWrapper sqliteWrapper = PowerMock.createMock(SQLiteWrapper.class);
         LocationControlBuffered locationControlBuffered = PowerMock
                 .createMock(LocationControlBuffered.class);
         SensorManager sensorManager = PowerMock.createMock(SensorManager.class);
         CompassListener compassListener = PowerMock.createMock(CompassListener.class);
 
         combinedLocationManager.removeUpdates(cacheListRefreshLocationListener);
         combinedLocationManager.removeUpdates(gpsStatusWidgetLocationListener);
         combinedLocationManager.removeUpdates(locationControlBuffered);
         sensorManager.unregisterListener(compassListener);
 
         PowerMock.replayAll();
         new GeocacheListPresenter(combinedLocationManager, locationControlBuffered,
                 gpsStatusWidgetLocationListener, null, null, null,
                 cacheListRefreshLocationListener, null, null, null, sqliteWrapper, null,
                 sensorManager, compassListener).onPause();
         PowerMock.verifyAll();
     }
 
     // @Test
     // public void testCompassListener() {
     // new CompassListener(null, null, 0).onAccuracyChanged(null, 0);
     // }
 
     @Test
     public void testCompassOnSensorUnchanged() {
         float values[] = new float[] {
             4f
         };
         final CompassListener compassListener = new CompassListener(null, null, 0);
         compassListener.onSensorChanged(0, values);
     }
 
     @Test
     public void testCompassOnAccuracyChanged() {
         new CompassListener(null, null, 0).onAccuracyChanged(0, 0);
     }
 
     @Test
     public void testCompassOnSensorChanged() {
         LocationControlBuffered locationControlBuffered = PowerMock
                 .createMock(LocationControlBuffered.class);
         Refresher refresher = PowerMock.createMock(Refresher.class);
 
         float values[] = new float[] {
             6f
         };
         locationControlBuffered.setAzimuth(5);
         refresher.refresh();
 
         PowerMock.replayAll();
         final CompassListener compassListener = new CompassListener(refresher,
                 locationControlBuffered, 0);
         compassListener.onSensorChanged(0, values);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnResume() {
         CombinedLocationManager combinedLocationManager = PowerMock
                 .createMock(CombinedLocationManager.class);
         LocationListener gpsStatusWidgetLocationListener = PowerMock
                 .createMock(LocationListener.class);
         CacheListRefreshLocationListener cacheListRefreshLocationListener = PowerMock
                 .createMock(CacheListRefreshLocationListener.class);
         LocationControlBuffered locationControlBuffered = PowerMock
                 .createMock(LocationControlBuffered.class);
         SQLiteWrapper sqliteWrapper = PowerMock.createMock(SQLiteWrapper.class);
         Database database = PowerMock.createMock(Database.class);
         SensorManager sensorManager = PowerMock.createMock(SensorManager.class);
         CompassListener compassListener = PowerMock.createMock(CompassListener.class);
 
         combinedLocationManager.requestLocationUpdates(GeocacheListPresenter.UPDATE_DELAY, 0,
                 gpsStatusWidgetLocationListener);
         combinedLocationManager.requestLocationUpdates(GeocacheListPresenter.UPDATE_DELAY, 0,
                 locationControlBuffered);
         combinedLocationManager.requestLocationUpdates(GeocacheListPresenter.UPDATE_DELAY, 0,
                 cacheListRefreshLocationListener);
         EasyMock.expect(
                 sensorManager.registerListener(compassListener, SensorManager.SENSOR_ORIENTATION,
                         SensorManager.SENSOR_DELAY_UI)).andReturn(true);
 
         PowerMock.replayAll();
         new GeocacheListPresenter(combinedLocationManager, locationControlBuffered,
                 gpsStatusWidgetLocationListener, null, null, null,
                 cacheListRefreshLocationListener, null, null, null, sqliteWrapper, database,
                 sensorManager, compassListener).onResume();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnResumeError() {
         CombinedLocationManager combinedLocationManager = PowerMock
                 .createMock(CombinedLocationManager.class);
         LocationControlBuffered locationControlBuffered = PowerMock
                 .createMock(LocationControlBuffered.class);
         ErrorDisplayer errorDisplayer = PowerMock.createMock(ErrorDisplayer.class);
 
         Exception e = new RuntimeException();
        combinedLocationManager.requestLocationUpdates(GeocacheListPresenter.UPDATE_DELAY, 1,
                 locationControlBuffered);
         EasyMock.expectLastCall().andThrow(e);
         errorDisplayer.displayErrorAndStack(e);
 
         PowerMock.replayAll();
         new GeocacheListPresenter(combinedLocationManager, locationControlBuffered,
                 locationControlBuffered, null, null, null, null, null, null, errorDisplayer, null,
                 null, null, null).onResume();
         PowerMock.verifyAll();
     }
 
 }
