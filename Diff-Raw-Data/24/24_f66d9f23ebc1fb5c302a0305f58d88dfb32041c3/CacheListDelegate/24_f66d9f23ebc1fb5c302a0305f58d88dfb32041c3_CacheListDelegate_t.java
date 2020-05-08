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
 
 package com.google.code.geobeagle.ui;
 
 import com.google.code.geobeagle.CacheListActions;
 import com.google.code.geobeagle.LocationControl;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.ResourceProvider;
 import com.google.code.geobeagle.data.CacheListData;
 import com.google.code.geobeagle.data.Destination;
 import com.google.code.geobeagle.data.Destination.DestinationFactory;
 import com.google.code.geobeagle.io.Database;
 import com.google.code.geobeagle.io.GpxImporter;
 import com.google.code.geobeagle.io.LocationBookmarksSql;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.location.LocationManager;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnCreateContextMenuListener;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 public class CacheListDelegate {
 
     static class CacheListOnCreateContextMenuListener implements OnCreateContextMenuListener {
         public static class Factory {
             public OnCreateContextMenuListener create(CacheListData cacheListData) {
                 return new CacheListOnCreateContextMenuListener(cacheListData);
             }
         }
 
         CacheListData mCacheListData;
 
         CacheListOnCreateContextMenuListener(CacheListData cacheListData) {
             mCacheListData = cacheListData;
         }
 
         public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
             AdapterContextMenuInfo acmi = (AdapterContextMenuInfo)menuInfo;
             menu.setHeaderTitle(mCacheListData.getId(acmi.position));
             menu.add(0, MENU_VIEW, 0, "View");
             if (acmi.position > 0)
                 menu.add(0, MENU_DELETE, 1, "Delete");
         }
     }
 
     public static class SimpleAdapterFactory {
         public SimpleAdapter create(Context context, ArrayList<Map<String, Object>> arrayList,
                 int view_layout, String[] from, int[] to) {
             return new SimpleAdapter(context, arrayList, view_layout, from, to);
         }
     }
 
     public static final String[] ADAPTER_FROM = {
             "cache", "distance"
     };
     public static final int[] ADAPTER_TO = {
             R.id.txt_cache, R.id.distance
     };
     public static final int MENU_DELETE = 0;
     public static final int MENU_VIEW = 1;
     public static final String SELECT_CACHE = "SELECT_CACHE";
 
     public static CacheListDelegate create(ListActivity parent) {
         final ErrorDisplayer errorDisplayer = new ErrorDisplayer(parent);
         final Database database = Database.create(parent);
         final ResourceProvider resourceProvider = new ResourceProvider(parent);
         // TODO: add a create function that takes a resourceid
         final Pattern[] destinationPatterns = Destination.getDestinationPatterns(resourceProvider);
         final DestinationFactory destinationFactory = new DestinationFactory(destinationPatterns);
         final LocationBookmarksSql locationBookmarks = LocationBookmarksSql.create(parent,
                 database, destinationFactory, errorDisplayer);
         final SimpleAdapterFactory simpleAdapterFactory = new SimpleAdapterFactory();
         final CacheListData cacheListData = CacheListData.create(destinationFactory, parent);
         final LocationControl locationControl = LocationControl.create(((LocationManager)parent
                 .getSystemService(Context.LOCATION_SERVICE)));
         final CacheListActions.Action actions[] = CacheListActions.create(parent, database,
                 cacheListData, errorDisplayer);
         final CacheListOnCreateContextMenuListener.Factory factory = new CacheListOnCreateContextMenuListener.Factory();
         final GpxImporter gpxImporter = GpxImporter.create(database, errorDisplayer, parent);
 
         return new CacheListDelegate(parent, locationBookmarks, locationControl,
                 simpleAdapterFactory, cacheListData, errorDisplayer, actions, factory, gpxImporter);
     }
 
     private final CacheListActions.Action mActions[];
     private final CacheListData mCacheListData;
     private final CacheListOnCreateContextMenuListener.Factory mCreateContextMenuFactory;
     private final ErrorDisplayer mErrorDisplayer;
     private final GpxImporter mGpxImporter;
     private final LocationBookmarksSql mLocationBookmarks;
     private final LocationControl mLocationControl;
     private final ListActivity mParent;
     private SimpleAdapter mSimpleAdapter;
     private final SimpleAdapterFactory mSimpleAdapterFactory;
 
     public CacheListDelegate(ListActivity parent, LocationBookmarksSql locationBookmarks,
             LocationControl locationControl, SimpleAdapterFactory simpleAdapterFactory,
             CacheListData cacheListData, ErrorDisplayer errorDisplayer,
             CacheListActions.Action[] actions,
             CacheListOnCreateContextMenuListener.Factory factory, GpxImporter gpxImporter) {
         mParent = parent;
         mLocationBookmarks = locationBookmarks;
         mLocationControl = locationControl;
         mSimpleAdapterFactory = simpleAdapterFactory;
         mCacheListData = cacheListData;
         mErrorDisplayer = errorDisplayer;
         mActions = actions;
         mCreateContextMenuFactory = factory;
         mGpxImporter = gpxImporter;
     }
 
     public boolean onContextItemSelected(MenuItem menuItem) {
         try {
             AdapterContextMenuInfo adapterContextMenuInfo = (AdapterContextMenuInfo)menuItem
                     .getMenuInfo();
             mActions[menuItem.getItemId()].act(adapterContextMenuInfo.position, mSimpleAdapter);
             return true;
         } catch (final Exception e) {
             mErrorDisplayer.displayErrorAndStack(e);
         }
         return false;
     }
 
     public void onCreate() {
         mParent.setContentView(R.layout.cache_list);
         mParent.getListView().setOnCreateContextMenuListener(
                 mCreateContextMenuFactory.create(mCacheListData));
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(R.string.menu_import_gpx);
         return true;
     }
 
     public void onListItemClick(ListView l, View v, int position, long id) {
         try {
             mActions[MENU_VIEW].act(position, mSimpleAdapter);
         } catch (final Exception e) {
             mErrorDisplayer.displayErrorAndStack(e);
         }
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
        mGpxImporter.importGpxs(this);
        return true;
     }
 
     public void onPause() {
         try {
             mGpxImporter.abort();
         } catch (InterruptedException e) {
             // Nothing we can do here! There is no chance to communicate to the
             // user.
         }
     }
 
     public void onResume() {
         try {
             mLocationBookmarks.onResume(null);
             mCacheListData.add(mLocationBookmarks.getLocations(), mLocationControl.getLocation());
             mSimpleAdapter = mSimpleAdapterFactory.create(mParent, mCacheListData.getAdapterData(),
                     R.layout.cache_row, ADAPTER_FROM, ADAPTER_TO);
             mParent.setListAdapter(mSimpleAdapter);
         } catch (final Exception e) {
             mErrorDisplayer.displayErrorAndStack(e);
         }
     }
 
 }
