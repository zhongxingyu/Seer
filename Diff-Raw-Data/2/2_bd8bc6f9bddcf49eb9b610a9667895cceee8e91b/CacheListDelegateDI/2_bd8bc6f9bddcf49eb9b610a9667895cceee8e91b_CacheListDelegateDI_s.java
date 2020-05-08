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
 
 import com.google.code.geobeagle.CacheTypeFactory;
 import com.google.code.geobeagle.CacheFilter;
 import com.google.code.geobeagle.ErrorDisplayer;
 import com.google.code.geobeagle.GeoFixProvider;
 import com.google.code.geobeagle.GeocacheFactory;
 import com.google.code.geobeagle.IPausable;
 import com.google.code.geobeagle.LocationControlDi;
 import com.google.code.geobeagle.actions.CacheAction;
 import com.google.code.geobeagle.actions.CacheActionDelete;
 import com.google.code.geobeagle.actions.CacheActionEdit;
 import com.google.code.geobeagle.actions.CacheActionToggleFavorite;
 import com.google.code.geobeagle.actions.CacheActionView;
 import com.google.code.geobeagle.actions.CacheFilterUpdater;
 import com.google.code.geobeagle.actions.MenuActionEditFilter;
 import com.google.code.geobeagle.actions.MenuActionFilterList;
 import com.google.code.geobeagle.actions.MenuActionMap;
 import com.google.code.geobeagle.actions.MenuActionSearchOnline;
 import com.google.code.geobeagle.actions.MenuActionSettings;
 import com.google.code.geobeagle.actions.MenuActions;
 import com.google.code.geobeagle.activity.ActivityDI;
 import com.google.code.geobeagle.activity.ActivitySaver;
 import com.google.code.geobeagle.activity.cachelist.CacheListDelegate.ImportIntentManager;
 import com.google.code.geobeagle.activity.cachelist.GeocacheListController.CacheListOnCreateContextMenuListener;
 import com.google.code.geobeagle.activity.cachelist.actions.Abortable;
 import com.google.code.geobeagle.activity.cachelist.actions.MenuActionMyLocation;
 import com.google.code.geobeagle.activity.cachelist.actions.MenuActionSyncGpx;
 import com.google.code.geobeagle.activity.cachelist.actions.MenuActionToggleFilter;
 import com.google.code.geobeagle.activity.cachelist.presenter.BearingFormatter;
 import com.google.code.geobeagle.activity.cachelist.presenter.CacheListAdapter;
 import com.google.code.geobeagle.activity.cachelist.presenter.CacheListPositionUpdater;
 import com.google.code.geobeagle.activity.cachelist.presenter.DistanceFormatterManager;
 import com.google.code.geobeagle.activity.cachelist.presenter.DistanceFormatterManagerDi;
 import com.google.code.geobeagle.activity.cachelist.presenter.GeocacheSummaryRowInflater;
 import com.google.code.geobeagle.activity.cachelist.presenter.RelativeBearingFormatter;
 import com.google.code.geobeagle.activity.cachelist.presenter.TitleUpdater;
 import com.google.code.geobeagle.activity.filterlist.FilterTypeCollection;
 import com.google.code.geobeagle.activity.main.GeoBeagle;
 import com.google.code.geobeagle.database.CachesProviderCenterThread;
 import com.google.code.geobeagle.database.CachesProviderDb;
 import com.google.code.geobeagle.database.CachesProviderCount;
 import com.google.code.geobeagle.database.CachesProviderSorted;
 import com.google.code.geobeagle.database.CachesProviderToggler;
 import com.google.code.geobeagle.database.CachesProviderWaitForInit;
 import com.google.code.geobeagle.database.DbFrontend;
 import com.google.code.geobeagle.database.ICachesProviderCenter;
 import com.google.code.geobeagle.gpsstatuswidget.GpsStatusWidget;
 import com.google.code.geobeagle.gpsstatuswidget.GpsStatusWidgetDelegate;
 import com.google.code.geobeagle.gpsstatuswidget.GpsWidgetAndUpdater;
 import com.google.code.geobeagle.gpsstatuswidget.UpdateGpsWidgetRunnable;
 import com.google.code.geobeagle.gpsstatuswidget.GpsStatusWidget.InflatedGpsStatusWidget;
 import com.google.code.geobeagle.xmlimport.GpxImporterDI.MessageHandler;
 import com.google.code.geobeagle.xmlimport.GpxToCache.Aborter;
 import com.google.code.geobeagle.xmlimport.GpxToCacheDI.XmlPullParserWrapper;
 
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.content.res.Resources;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.widget.LinearLayout.LayoutParams;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 public class CacheListDelegateDI {
     public static class Timing {
         private long mStartTime;
 
         public void lap(CharSequence msg) {
             long finishTime = Calendar.getInstance().getTimeInMillis();
             Log.d("GeoBeagle", "****** " + msg + ": " + (finishTime - mStartTime));
             mStartTime = finishTime;
         }
 
         public void start() {
             mStartTime = Calendar.getInstance().getTimeInMillis();
         }
 
         public long getTime() {
             return Calendar.getInstance().getTimeInMillis();
         }
     }
 
     public static CacheListDelegate create(ListActivity listActivity, LayoutInflater layoutInflater) {
         final OnClickListener onClickListener = new OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
             }
         };
         final ErrorDisplayer errorDisplayer = new ErrorDisplayer(listActivity, onClickListener);
         final GeoFixProvider geoFixProvider = 
             LocationControlDi.create(listActivity);
         final GeocacheFactory geocacheFactory = new GeocacheFactory();
         final BearingFormatter relativeBearingFormatter = new RelativeBearingFormatter();
         final DistanceFormatterManager distanceFormatterManager = DistanceFormatterManagerDi
                 .create(listActivity);
         final XmlPullParserWrapper xmlPullParserWrapper = new XmlPullParserWrapper();
 
         final GeocacheSummaryRowInflater geocacheSummaryRowInflater = new GeocacheSummaryRowInflater(
                 distanceFormatterManager.getFormatter(), layoutInflater,
                 relativeBearingFormatter, listActivity.getResources());
 
         final InflatedGpsStatusWidget inflatedGpsStatusWidget = new InflatedGpsStatusWidget(
                 listActivity);
         final GpsStatusWidget gpsStatusWidget = new GpsStatusWidget(listActivity);
 
         gpsStatusWidget.addView(inflatedGpsStatusWidget, LayoutParams.FILL_PARENT,
                 LayoutParams.WRAP_CONTENT);
         final GpsWidgetAndUpdater gpsWidgetAndUpdater = new GpsWidgetAndUpdater(listActivity,
                 gpsStatusWidget, geoFixProvider,
                 distanceFormatterManager.getFormatter());
         final GpsStatusWidgetDelegate gpsStatusWidgetDelegate = gpsWidgetAndUpdater
                 .getGpsStatusWidgetDelegate();
 
         inflatedGpsStatusWidget.setDelegate(gpsStatusWidgetDelegate);
 
         final UpdateGpsWidgetRunnable updateGpsWidgetRunnable = gpsWidgetAndUpdater
                 .getUpdateGpsWidgetRunnable();
         updateGpsWidgetRunnable.run();
         
         final FilterTypeCollection filterTypeCollection = new FilterTypeCollection(listActivity);
         final CacheFilter cacheFilter = filterTypeCollection.getActiveFilter();
         
         final DbFrontend dbFrontend = new DbFrontend(listActivity, geocacheFactory);
         final CachesProviderDb cachesProviderDb = new CachesProviderDb(dbFrontend, cacheFilter);
         final ICachesProviderCenter cachesProviderCount = new CachesProviderWaitForInit(new CachesProviderCount(cachesProviderDb, 15, 30));
         final CachesProviderSorted cachesProviderSorted = new CachesProviderSorted(cachesProviderCount);
         //final CachesProviderLazy cachesProviderLazy = new CachesProviderLazy(cachesProviderSorted, 0.01, 2000, clock);
         ICachesProviderCenter cachesProviderLazy = cachesProviderSorted;
 
         final CachesProviderDb cachesProviderAll = new CachesProviderDb(dbFrontend, cacheFilter);
         final CachesProviderToggler cachesProviderToggler = 
             new CachesProviderToggler(cachesProviderLazy, cachesProviderAll);
         CachesProviderCenterThread thread = new CachesProviderCenterThread(cachesProviderToggler);
         final TitleUpdater titleUpdater = new TitleUpdater(listActivity, 
                 cachesProviderToggler, dbFrontend);
 
         distanceFormatterManager.addHasDistanceFormatter(geocacheSummaryRowInflater);
         distanceFormatterManager.addHasDistanceFormatter(gpsStatusWidgetDelegate);
         final CacheListAdapter cacheList = new CacheListAdapter(cachesProviderToggler, 
                cachesProviderSorted, geocacheSummaryRowInflater, titleUpdater);
         final CacheListPositionUpdater cacheListUpdater = new CacheListPositionUpdater(
                 geoFixProvider, cacheList, cachesProviderCount, thread /*cachesProviderSorted*/);
         geoFixProvider.addObserver(cacheListUpdater);
         final CacheListView.ScrollListener scrollListener = new CacheListView.ScrollListener(
                 cacheList);
         final CacheTypeFactory cacheTypeFactory = new CacheTypeFactory();
 
         final Aborter aborter = new Aborter();
         final MessageHandler messageHandler = MessageHandler.create(listActivity);
         //final CachePersisterFacadeFactory cachePersisterFacadeFactory = new CachePersisterFacadeFactory(
         //        messageHandler, cacheTypeFactory);
 
         final GpxImporterFactory gpxImporterFactory = new GpxImporterFactory(aborter,
                 errorDisplayer, geoFixProvider, listActivity,
                 messageHandler, xmlPullParserWrapper, cacheTypeFactory);
 
         final Abortable nullAbortable = new Abortable() {
             public void abort() {
             }
         };
 
         final Resources resources = listActivity.getResources();
         final MenuActionSyncGpx menuActionSyncGpx = new MenuActionSyncGpx(nullAbortable,
                 cacheList, gpxImporterFactory, dbFrontend, resources);
         final CacheActionEdit cacheActionEdit = new CacheActionEdit(listActivity);
         final MenuActions menuActions = new MenuActions();
         menuActions.add(menuActionSyncGpx);
         menuActions.add(new MenuActionToggleFilter(cachesProviderToggler, cacheList, resources));
         menuActions.add(new MenuActionMyLocation(errorDisplayer,
                 geocacheFactory, geoFixProvider, dbFrontend, resources, cacheActionEdit));
         menuActions.add(new MenuActionSearchOnline(listActivity));
         List<CachesProviderDb> providers = new ArrayList<CachesProviderDb>();
         providers.add(cachesProviderDb);
         providers.add(cachesProviderAll);
         //SharedPreferences prefs = CacheFilterFactory.getActivePreferences(listActivity);
         final CacheFilterUpdater cacheFilterUpdater = 
             new CacheFilterUpdater(filterTypeCollection, providers);
         menuActions.add(new MenuActionEditFilter(listActivity, 
                 cacheFilterUpdater, cacheList, filterTypeCollection));
         menuActions.add(new MenuActionMap(listActivity, geoFixProvider));
         menuActions.add(new MenuActionFilterList(listActivity));
         menuActions.add(new MenuActionSettings(listActivity));
         final Intent geoBeagleMainIntent = new Intent(listActivity, GeoBeagle.class);
         final CacheActionView cacheActionView = new CacheActionView(
                 listActivity, geoBeagleMainIntent);
         final CacheActionToggleFavorite cacheActionToggleFavorite = 
             new CacheActionToggleFavorite(dbFrontend, cacheList, cacheFilterUpdater);
         final CacheActionDelete cacheActionDelete = 
             new CacheActionDelete(cacheList, titleUpdater, dbFrontend, resources);
             
         final CacheAction[] contextActions = new CacheAction[] {
                 cacheActionView, cacheActionToggleFavorite, 
                 cacheActionEdit, cacheActionDelete
         };
         final GeocacheListController geocacheListController = 
             new GeocacheListController(cacheList, contextActions, menuActionSyncGpx, menuActions, cacheActionView);
 
         final ActivitySaver activitySaver = ActivityDI.createActivitySaver(listActivity);
         final ImportIntentManager importIntentManager = new ImportIntentManager(listActivity);
         final CacheListOnCreateContextMenuListener menuCreator = 
             new CacheListOnCreateContextMenuListener(cachesProviderToggler, contextActions);
         final IPausable pausables[] = { geoFixProvider, thread };
 
         //TODO: It is currently a bug to send cachesProviderArea since cachesProviderAll also need to be notified of db changes.
         return new CacheListDelegate(importIntentManager, activitySaver,
                 geocacheListController, dbFrontend, updateGpsWidgetRunnable, 
                 gpsStatusWidget, menuCreator, cacheList, geocacheSummaryRowInflater, listActivity, 
                 scrollListener, distanceFormatterManager, cachesProviderDb, pausables);
     }
 }
