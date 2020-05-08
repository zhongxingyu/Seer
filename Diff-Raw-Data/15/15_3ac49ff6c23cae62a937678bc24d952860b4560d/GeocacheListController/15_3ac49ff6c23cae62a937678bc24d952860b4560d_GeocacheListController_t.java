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
 import com.google.code.geobeagle.io.GpxImporter;
 import com.google.code.geobeagle.ui.ErrorDisplayer;
 
 import android.view.ContextMenu;
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
     private final ErrorDisplayer mErrorDisplayer;
     private final MenuActions mMenuActions;
     private final GpxImporter mGpxImporter;
 
     GeocacheListController(MenuActions menuActions, ContextAction[] contextActions,
             GpxImporter gpxImporter, ErrorDisplayer errorDisplayer) {
         mErrorDisplayer = errorDisplayer;
         mContextActions = contextActions;
         mMenuActions = menuActions;
         mGpxImporter = gpxImporter;
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
