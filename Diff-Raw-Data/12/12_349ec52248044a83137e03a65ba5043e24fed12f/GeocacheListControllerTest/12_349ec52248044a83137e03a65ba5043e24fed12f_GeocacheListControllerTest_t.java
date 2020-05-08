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
 
 package com.google.code.geobeagle.activity.cachelist;
 
 import static org.junit.Assert.assertTrue;
 
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.actions.CacheAction;
 import com.google.code.geobeagle.actions.CacheFilterUpdater;
 import com.google.code.geobeagle.actions.MenuActions;
 import com.google.code.geobeagle.activity.cachelist.actions.MenuActionSyncGpx;
 import com.google.code.geobeagle.activity.cachelist.presenter.CacheListAdapter;
 import com.google.code.geobeagle.database.DatabaseDI;
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.app.ListActivity;
 import android.view.Menu;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
         GeocacheListController.class, ListActivity.class,
         DatabaseDI.class
 })
 public class GeocacheListControllerTest {
 
     @Test
     public void testOnCreateOptionsMenu() {
         MenuActions menuActions = PowerMock.createMock(MenuActions.class);
         Menu menu = PowerMock.createMock(Menu.class);
         CacheAction defaultCacheAction = PowerMock
                 .createMock(CacheAction.class);
 
         EasyMock.expect(menuActions.onCreateOptionsMenu(menu)).andReturn(true);
 
         PowerMock.replayAll();
        assertTrue(new GeocacheListController(null, null, menuActions, defaultCacheAction, null).onCreateOptionsMenu(menu));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnListItemClick() {
         Geocache geocache = PowerMock.createMock(Geocache.class);
         CacheListAdapter cacheListAdapter = 
             PowerMock.createMock(CacheListAdapter.class);
         CacheAction defaultCacheAction = PowerMock.createMock(CacheAction.class);
         EasyMock.expect(cacheListAdapter.getGeocacheAt(45)).andReturn(geocache);
         defaultCacheAction.act(geocache);
 
         PowerMock.replayAll();
         new GeocacheListController(cacheListAdapter, null, null,
                 defaultCacheAction, null).onListItemClick(null, null, 46, 0);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnPause() {
         MenuActionSyncGpx menuActionSync = PowerMock
                 .createMock(MenuActionSyncGpx.class);
 
         menuActionSync.abort();
 
         PowerMock.replayAll();
        new GeocacheListController(null, menuActionSync, null, null, null)
                 .onPause();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnResume() {
         CacheFilterUpdater cacheFilterUpdater = PowerMock.createMock(CacheFilterUpdater.class);
         CacheListAdapter cacheListAdapter = PowerMock.createMock(CacheListAdapter.class);
 
         cacheFilterUpdater.loadActiveFilter();
         cacheListAdapter.forceRefresh();
 
         PowerMock.replayAll();
        new GeocacheListController(cacheListAdapter, null, null, null, cacheFilterUpdater)
                 .onResume(false);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnResumeAndImport() {
         CacheFilterUpdater cacheFilterUpdater = PowerMock.createMock(CacheFilterUpdater.class);
         CacheListAdapter cacheList = PowerMock
                 .createMock(CacheListAdapter.class);
         MenuActionSyncGpx menuActionSyncGpx = PowerMock
                 .createMock(MenuActionSyncGpx.class);
 
         cacheFilterUpdater.loadActiveFilter();
         cacheList.forceRefresh();
         menuActionSyncGpx.act();
 
         PowerMock.replayAll();
        new GeocacheListController(cacheList, menuActionSyncGpx, null, null, cacheFilterUpdater).onResume(true);
         PowerMock.verifyAll();
     }
 }
