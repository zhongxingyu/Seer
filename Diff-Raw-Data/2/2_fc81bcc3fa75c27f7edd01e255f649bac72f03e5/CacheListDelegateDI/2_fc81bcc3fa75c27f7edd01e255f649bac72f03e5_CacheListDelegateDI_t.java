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
 
 import com.google.code.geobeagle.CompassListener;
 import com.google.code.geobeagle.ErrorDisplayer;
 import com.google.code.geobeagle.GeocacheFactory;
 import com.google.code.geobeagle.LocationControlBuffered;
 import com.google.code.geobeagle.LocationControlDi;
 import com.google.code.geobeagle.LocationControlBuffered.GpsDisabledLocation;
 import com.google.code.geobeagle.actions.context.ContextAction;
 import com.google.code.geobeagle.actions.context.ContextActionDelete;
 import com.google.code.geobeagle.actions.context.ContextActionView;
 import com.google.code.geobeagle.activity.ActivityDI;
 import com.google.code.geobeagle.activity.ActivitySaver;
 import com.google.code.geobeagle.activity.cachelist.actions.menu.MenuActionMyLocation;
 import com.google.code.geobeagle.activity.cachelist.actions.menu.MenuActionSearchOnline;
 import com.google.code.geobeagle.activity.cachelist.actions.menu.MenuActionSyncGpx;
 import com.google.code.geobeagle.activity.cachelist.actions.menu.MenuActionToggleFilter;
 import com.google.code.geobeagle.activity.cachelist.actions.menu.MenuActions;
 import com.google.code.geobeagle.activity.cachelist.model.CacheListData;
 import com.google.code.geobeagle.activity.cachelist.model.GeocacheFromMyLocationFactory;
 import com.google.code.geobeagle.activity.cachelist.model.GeocacheVector;
 import com.google.code.geobeagle.activity.cachelist.model.GeocacheVectorFactory;
 import com.google.code.geobeagle.activity.cachelist.model.GeocacheVectors;
 import com.google.code.geobeagle.activity.cachelist.presenter.ActionAndTolerance;
 import com.google.code.geobeagle.activity.cachelist.presenter.AdapterCachesSorter;
 import com.google.code.geobeagle.activity.cachelist.presenter.BearingFormatter;
 import com.google.code.geobeagle.activity.cachelist.presenter.CacheListRefresh;
 import com.google.code.geobeagle.activity.cachelist.presenter.DistanceFormatterManager;
 import com.google.code.geobeagle.activity.cachelist.presenter.DistanceFormatterManagerDi;
 import com.google.code.geobeagle.activity.cachelist.presenter.DistanceUpdater;
 import com.google.code.geobeagle.activity.cachelist.presenter.GeocacheListAdapter;
 import com.google.code.geobeagle.activity.cachelist.presenter.GeocacheListPresenter;
 import com.google.code.geobeagle.activity.cachelist.presenter.ListTitleFormatter;
 import com.google.code.geobeagle.activity.cachelist.presenter.LocationAndAzimuthTolerance;
 import com.google.code.geobeagle.activity.cachelist.presenter.LocationTolerance;
 import com.google.code.geobeagle.activity.cachelist.presenter.RelativeBearingFormatter;
 import com.google.code.geobeagle.activity.cachelist.presenter.SqlCacheLoader;
 import com.google.code.geobeagle.activity.cachelist.presenter.TitleUpdater;
 import com.google.code.geobeagle.activity.cachelist.presenter.ToleranceStrategy;
 import com.google.code.geobeagle.activity.cachelist.presenter.CacheListRefresh.ActionManager;
 import com.google.code.geobeagle.activity.cachelist.presenter.GeocacheListPresenter.CacheListRefreshLocationListener;
 import com.google.code.geobeagle.activity.cachelist.view.GeocacheSummaryRowInflater;
 import com.google.code.geobeagle.activity.main.GeoBeagle;
 import com.google.code.geobeagle.database.CacheWriter;
 import com.google.code.geobeagle.database.DatabaseDI;
 import com.google.code.geobeagle.database.FilterNearestCaches;
 import com.google.code.geobeagle.database.GeocachesSql;
 import com.google.code.geobeagle.database.LocationSaver;
 import com.google.code.geobeagle.database.WhereFactoryAllCaches;
 import com.google.code.geobeagle.database.WhereFactoryNearestCaches;
 import com.google.code.geobeagle.database.DatabaseDI.GeoBeagleSqliteOpenHelper;
 import com.google.code.geobeagle.database.DatabaseDI.SQLiteWrapper;
 import com.google.code.geobeagle.gpsstatuswidget.GpsStatusWidget;
 import com.google.code.geobeagle.gpsstatuswidget.GpsStatusWidgetDelegate;
 import com.google.code.geobeagle.gpsstatuswidget.GpsWidgetAndUpdater;
 import com.google.code.geobeagle.gpsstatuswidget.UpdateGpsWidgetRunnable;
 import com.google.code.geobeagle.gpsstatuswidget.GpsStatusWidget.InflatedGpsStatusWidget;
 import com.google.code.geobeagle.location.CombinedLocationListener;
 import com.google.code.geobeagle.location.CombinedLocationManager;
 import com.google.code.geobeagle.xmlimport.CachePersisterFacade;
 import com.google.code.geobeagle.xmlimport.CachePersisterFacadeDI;
 import com.google.code.geobeagle.xmlimport.GpxImporter;
 import com.google.code.geobeagle.xmlimport.GpxImporterDI;
 import com.google.code.geobeagle.xmlimport.GpxImporterDI.MessageHandler;
 import com.google.code.geobeagle.xmlimport.GpxToCache.Aborter;
 import com.google.code.geobeagle.xmlimport.GpxToCacheDI.XmlPullParserWrapper;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.hardware.SensorManager;
 import android.location.LocationManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.widget.LinearLayout.LayoutParams;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 public class CacheListDelegateDI {
     public static class Timing {
         private long mStartTime;
 
         public void lap(CharSequence msg) {
             long finishTime = Calendar.getInstance().getTimeInMillis();
             Log.v("GeoBeagle", "****** " + msg + ": " + (finishTime - mStartTime));
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
         final ErrorDisplayer errorDisplayer = new ErrorDisplayer(listActivity);
         final LocationManager locationManager = (LocationManager)listActivity
                 .getSystemService(Context.LOCATION_SERVICE);
         final CombinedLocationManager combinedLocationManager = new CombinedLocationManager(
                 locationManager);
         final LocationControlBuffered locationControlBuffered = LocationControlDi
                 .create(locationManager);
         final GeocacheFactory geocacheFactory = new GeocacheFactory();
         final GeocacheFromMyLocationFactory geocacheFromMyLocationFactory = new GeocacheFromMyLocationFactory(
                 geocacheFactory, locationControlBuffered);
 
         final BearingFormatter relativeBearingFormatter = new RelativeBearingFormatter();
 
         final DistanceFormatterManager distanceFormatterManager = DistanceFormatterManagerDi
                 .create(listActivity);
         final GeoBeagleSqliteOpenHelper geoBeagleSqliteOpenHelper = new GeoBeagleSqliteOpenHelper(
                 listActivity);
         final SQLiteDatabase sqliteDatabaseWritable = geoBeagleSqliteOpenHelper
                 .getWritableDatabase();
         final CacheWriter cacheWriter = DatabaseDI.createCacheWriter(sqliteDatabaseWritable);
        final LocationSaver locationSaver = new LocationSaver(cacheWriter);
         final GeocacheVectorFactory geocacheVectorFactory = new GeocacheVectorFactory();
         final ArrayList<GeocacheVector> geocacheVectorsList = new ArrayList<GeocacheVector>(10);
         final GeocacheVectors geocacheVectors = new GeocacheVectors(geocacheVectorFactory,
                 geocacheVectorsList);
         final CacheListData cacheListData = new CacheListData(geocacheVectors);
         final XmlPullParserWrapper xmlPullParserWrapper = new XmlPullParserWrapper();
 
         final GeocacheSummaryRowInflater geocacheSummaryRowInflater = new GeocacheSummaryRowInflater(
                 layoutInflater, geocacheVectors, distanceFormatterManager.getFormatter(),
                 relativeBearingFormatter);
 
         final GeocacheListAdapter geocacheListAdapter = new GeocacheListAdapter(geocacheVectors,
                 geocacheSummaryRowInflater);
 
         final InflatedGpsStatusWidget inflatedGpsStatusWidget = new InflatedGpsStatusWidget(
                 listActivity);
         final GpsStatusWidget gpsStatusWidget = new GpsStatusWidget(listActivity);
 
         /*
          * gpsStatusWidget.addView(linedEditText, new LinearLayout.LayoutParams(
          * LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
          */
         gpsStatusWidget.addView(inflatedGpsStatusWidget, LayoutParams.FILL_PARENT,
                 LayoutParams.WRAP_CONTENT);
         GpsWidgetAndUpdater gpsWidgetAndUpdater = new GpsWidgetAndUpdater(listActivity,
                 gpsStatusWidget, locationControlBuffered, combinedLocationManager,
                 distanceFormatterManager.getFormatter());
         final GpsStatusWidgetDelegate gpsStatusWidgetDelegate = gpsWidgetAndUpdater
                 .getGpsStatusWidgetDelegate();
 
         inflatedGpsStatusWidget.setDelegate(gpsStatusWidgetDelegate);
 
         final CombinedLocationListener combinedLocationListener = new CombinedLocationListener(
                 locationControlBuffered, gpsStatusWidgetDelegate);
 
         final UpdateGpsWidgetRunnable updateGpsWidgetRunnable = gpsWidgetAndUpdater
                 .getUpdateGpsWidgetRunnable();
         updateGpsWidgetRunnable.run();
 
         final WhereFactoryAllCaches whereFactoryAllCaches = new WhereFactoryAllCaches();
         final WhereFactoryNearestCaches whereFactoryNearestCaches = new WhereFactoryNearestCaches();
         final FilterNearestCaches filterNearestCaches = new FilterNearestCaches(
                 whereFactoryAllCaches, whereFactoryNearestCaches);
         final ListTitleFormatter listTitleFormatter = new ListTitleFormatter();
         final SQLiteWrapper sqliteWrapper = new SQLiteWrapper(sqliteDatabaseWritable);
         final GeocachesSql geocachesSql = DatabaseDI.createGeocachesSql(sqliteWrapper);
 
         final CacheListDelegateDI.Timing timing = new CacheListDelegateDI.Timing();
         final TitleUpdater titleUpdater = new TitleUpdater(geocachesSql, listActivity,
                 filterNearestCaches, cacheListData, listTitleFormatter, timing);
         final SqlCacheLoader sqlCacheLoader = new SqlCacheLoader(geocachesSql, filterNearestCaches,
                 cacheListData, locationControlBuffered, titleUpdater, timing);
         final AdapterCachesSorter adapterCachesSorter = new AdapterCachesSorter(cacheListData,
                 timing, locationControlBuffered);
         final GpsDisabledLocation gpsDisabledLocation = new GpsDisabledLocation();
         final DistanceUpdater distanceUpdater = new DistanceUpdater(geocacheListAdapter);
         final ToleranceStrategy sqlCacheLoaderTolerance = new LocationTolerance(500,
                 gpsDisabledLocation, 1000);
         final ToleranceStrategy adapterCachesSorterTolerance = new LocationTolerance(6,
                 gpsDisabledLocation, 1000);
         final LocationTolerance distanceUpdaterLocationTolerance = new LocationTolerance(1,
                 gpsDisabledLocation, 1000);
         final ToleranceStrategy distanceUpdaterTolerance = new LocationAndAzimuthTolerance(
                 distanceUpdaterLocationTolerance, 720);
         final ActionAndTolerance[] actionAndTolerances = new ActionAndTolerance[] {
                 new ActionAndTolerance(sqlCacheLoader, sqlCacheLoaderTolerance),
                 new ActionAndTolerance(adapterCachesSorter, adapterCachesSorterTolerance),
                 new ActionAndTolerance(distanceUpdater, distanceUpdaterTolerance)
         };
         final ActionManager actionManager = new ActionManager(actionAndTolerances);
         final CacheListRefresh cacheListRefresh = new CacheListRefresh(actionManager,
                 locationControlBuffered, timing);
         final MenuActionMyLocation menuActionMyLocation = new MenuActionMyLocation(locationSaver,
                 geocacheFromMyLocationFactory, cacheListRefresh, errorDisplayer);
 
         final CacheListRefreshLocationListener cacheListRefreshLocationListener = new CacheListRefreshLocationListener(
                 cacheListRefresh);
         final SensorManager sensorManager = (SensorManager)listActivity
                 .getSystemService(Context.SENSOR_SERVICE);
         final CompassListener compassListener = new CompassListener(cacheListRefresh,
                 locationControlBuffered, 720);
         distanceFormatterManager.addHasDistanceFormatter(geocacheSummaryRowInflater);
         distanceFormatterManager.addHasDistanceFormatter(gpsStatusWidgetDelegate);
 
         final GeocacheListPresenter geocacheListPresenter = new GeocacheListPresenter(
                 combinedLocationManager, locationControlBuffered, combinedLocationListener,
                 gpsStatusWidget, updateGpsWidgetRunnable, geocacheVectors,
                 cacheListRefreshLocationListener, listActivity, geocacheListAdapter,
                 errorDisplayer, sensorManager, compassListener, distanceFormatterManager,
                 geocacheSummaryRowInflater);
         final MenuActionToggleFilter menuActionToggleFilter = new MenuActionToggleFilter(
                 filterNearestCaches, cacheListRefresh);
 
         final Aborter aborter = new Aborter();
         final MessageHandler messageHandler = MessageHandler.create(geocacheListPresenter,
                 listActivity);
         final CachePersisterFacade cachePersisterFacade = CachePersisterFacadeDI.create(
                 listActivity, messageHandler, cacheWriter);
 
         final GpxImporter gpxImporter = GpxImporterDI.create(geoBeagleSqliteOpenHelper,
                 listActivity, xmlPullParserWrapper, errorDisplayer, geocacheListPresenter, aborter,
                 messageHandler, cachePersisterFacade);
         final MenuActionSyncGpx menuActionSyncGpx = new MenuActionSyncGpx(gpxImporter,
                 cacheListRefresh);
         final MenuActionSearchOnline menuActionSearchOnline = new MenuActionSearchOnline(
                 listActivity);
         final MenuActions menuActions = new MenuActions(menuActionSyncGpx, menuActionMyLocation,
                 menuActionToggleFilter, cacheListRefresh, menuActionSearchOnline);
         final Intent geoBeagleMainIntent = new Intent(listActivity, GeoBeagle.class);
         final ContextActionView contextActionView = new ContextActionView(geocacheVectors,
                 listActivity, geoBeagleMainIntent);
         final ContextActionDelete contextActionDelete = new ContextActionDelete(
                 geocacheListAdapter, cacheWriter, geocacheVectors, titleUpdater);
         ContextAction[] contextActions = new ContextAction[] {
                 contextActionDelete, contextActionView
         };
 
         final GeocacheListController geocacheListController = new GeocacheListController(
                 cacheListRefresh, contextActions, filterNearestCaches, gpxImporter, listActivity,
                 menuActions);
         final ActivitySaver activitySaver = ActivityDI.createActivitySaver(listActivity);
 
         return new CacheListDelegate(activitySaver, geocacheListController, geocacheListPresenter);
     }
 }
