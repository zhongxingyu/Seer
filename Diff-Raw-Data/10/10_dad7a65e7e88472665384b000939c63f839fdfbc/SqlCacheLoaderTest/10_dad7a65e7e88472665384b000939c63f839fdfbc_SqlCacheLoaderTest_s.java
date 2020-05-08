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
 
 package com.google.code.geobeagle.activity.cachelist.model;
 
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.LocationControlBuffered;
 import com.google.code.geobeagle.activity.cachelist.CacheListDelegateDI.Timing;
 import com.google.code.geobeagle.activity.cachelist.model.GeocacheVector.LocationComparator;
 import com.google.code.geobeagle.activity.cachelist.presenter.DistanceSortStrategy;
 import com.google.code.geobeagle.activity.cachelist.presenter.SqlCacheLoader;
 import com.google.code.geobeagle.activity.cachelist.presenter.TitleUpdater;
 import com.google.code.geobeagle.database.DbFrontend;
 import com.google.code.geobeagle.database.FilterNearestCaches;
 import com.google.code.geobeagle.database.WhereFactory;
 import com.google.inject.Provider;
 
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.location.Location;
 import android.util.FloatMath;
 
 import java.util.ArrayList;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
         DistanceSortStrategy.class, GeocacheVector.class, LocationComparator.class,
         GeocacheVectorTest.class, FloatMath.class
 })
 public class SqlCacheLoaderTest {
     private Timing mTiming;
 
     @Before
     public void ignoreTiming() {
         mTiming = PowerMock.createMock(Timing.class);
 
         mTiming.lap((String)EasyMock.anyObject());
         EasyMock.expectLastCall().anyTimes();
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testRefresh() {
         LocationControlBuffered locationControlBuffered = PowerMock
                 .createMock(LocationControlBuffered.class);
         Location location = PowerMock.createMock(Location.class);
         Provider<DbFrontend> dbFrontendProvider = PowerMock.createMock(Provider.class);
         DbFrontend dbFrontend = PowerMock.createMock(DbFrontend.class);
         WhereFactory whereFactory = PowerMock.createMock(WhereFactory.class);
         CacheListData cacheListData = PowerMock.createMock(CacheListData.class);
         TitleUpdater titleUpdater = PowerMock.createMock(TitleUpdater.class);
         FilterNearestCaches filterNearestCaches = PowerMock.createMock(FilterNearestCaches.class);
         ArrayList<Geocache> geocaches = new ArrayList<Geocache>();
 
         EasyMock.expect(location.getLatitude()).andReturn(37.0);
         EasyMock.expect(location.getLongitude()).andReturn(-122.0);
         EasyMock.expect(filterNearestCaches.getWhereFactory()).andReturn(whereFactory);
         EasyMock.expect(locationControlBuffered.getLocation()).andReturn(location);
         EasyMock.expect(dbFrontendProvider.get()).andReturn(dbFrontend);
         EasyMock.expect(dbFrontend.loadCaches(37.0, -122.0, whereFactory)).andReturn(geocaches);
         EasyMock.expect(cacheListData.size()).andReturn(100);
         EasyMock.expect(dbFrontend.countAll()).andReturn(2300);
         cacheListData.add(geocaches, locationControlBuffered);
         titleUpdater.update(2300, 100);
 
         PowerMock.replayAll();
        new SqlCacheLoader(dbFrontendProvider, filterNearestCaches, cacheListData, locationControlBuffered,
                titleUpdater, mTiming).refresh();
         PowerMock.verifyAll();
     }
 }
