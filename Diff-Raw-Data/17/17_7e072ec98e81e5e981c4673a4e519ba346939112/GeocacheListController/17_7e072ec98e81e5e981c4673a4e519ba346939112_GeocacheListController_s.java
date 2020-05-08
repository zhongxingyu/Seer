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
 
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.data.GeocacheVectors;
 import com.google.code.geobeagle.io.Database;
 import com.google.code.geobeagle.io.GpxImporter;
 import com.google.code.geobeagle.io.DatabaseDI.SQLiteWrapper;
 import com.google.code.geobeagle.ui.ErrorDisplayer;
 
 import android.app.ListActivity;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnCreateContextMenuListener;
 import android.widget.ListView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class GeocacheListController {
 
     public static class CacheListOnCreateContextMenuListener implements OnCreateContextMenuListener {
         private final GeocacheVectors mGeocacheVectors;
 
         CacheListOnCreateContextMenuListener(GeocacheVectors geocacheVectors) {
             mGeocacheVectors = geocacheVectors;
         }
 
         public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
             AdapterContextMenuInfo acmi = (AdapterContextMenuInfo)menuInfo;
             if (acmi.position > 0) {
                 menu.setHeaderTitle(mGeocacheVectors.get(acmi.position - 1).getId());
                 menu.add(0, MENU_VIEW, 0, "View");
                 menu.add(0, MENU_DELETE, 1, "Delete");
             }
         }
     }
 
     static final int MENU_DELETE = 0;
     static final int MENU_VIEW = 1;
     public static final String SELECT_CACHE = "SELECT_CACHE";
     private final ContextAction mContextActions[];
     private final Database mDatabase;
     private final ErrorDisplayer mErrorDisplayer;
     private final FilterNearestCaches mFilterNearestCaches;
     private final GpxImporter mGpxImporter;
     private final ListActivity mListActivity;
     private final MenuActionRefresh mMenuActionRefresh;
     private final MenuActions mMenuActions;
     private final SQLiteWrapper mSqliteWrapper;
 
     GeocacheListController(ListActivity listActivity, MenuActions menuActions,
             ContextAction[] contextActions, SQLiteWrapper sqliteWrapper, Database database,
             GpxImporter gpxImporter, MenuActionRefresh menuActionRefresh,
             FilterNearestCaches filterNearestCaches, ErrorDisplayer errorDisplayer) {
         mListActivity = listActivity;
         mErrorDisplayer = errorDisplayer;
         mContextActions = contextActions;
         mMenuActions = menuActions;
         mGpxImporter = gpxImporter;
         mMenuActionRefresh = menuActionRefresh;
         mSqliteWrapper = sqliteWrapper;
         mFilterNearestCaches = filterNearestCaches;
         mDatabase = database;
     }
 
     public boolean onContextItemSelected(MenuItem menuItem) {
         try {
             AdapterContextMenuInfo adapterContextMenuInfo = (AdapterContextMenuInfo)menuItem
                     .getMenuInfo();
             mContextActions[menuItem.getItemId()].act(adapterContextMenuInfo.position - 1);
         } catch (final Exception e) {
             mErrorDisplayer.displayErrorAndStack(e);
         }
         return true;
     }
 
     public void onCreate() {
         try {
             mSqliteWrapper.openReadableDatabase(mDatabase);
             mMenuActionRefresh.sort();
         } catch (final Exception e) {
             mErrorDisplayer.displayErrorAndStack(e);
         }
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         mListActivity.getMenuInflater().inflate(R.menu.cache_list_menu, menu);
         return true;
     }
 
     public void onListItemClick(ListView l, View v, int position, long id) {
         try {
             if (position > 0)
                 mContextActions[MENU_VIEW].act(position - 1);
             else
                 mMenuActions.act(R.id.menu_refresh);
         } catch (final Exception e) {
             mErrorDisplayer.displayErrorAndStack(e);
         }
     }
 
     public boolean onMenuOpened(int featureId, Menu menu) {
         menu.findItem(R.id.menu_toggle_filter).setTitle(mFilterNearestCaches.getMenuString());
         return true;
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         try {
             mMenuActions.act(item.getItemId());
         } catch (Exception e) {
             mErrorDisplayer.displayErrorAndStack(e);
         }
         return true;
     }
 
     public void onPause() {
         try {
             mGpxImporter.abort();
         } catch (InterruptedException e) {
             // Nothing we can do here! There is no chance to communicate to
             // the user.
         }
     }
 }
