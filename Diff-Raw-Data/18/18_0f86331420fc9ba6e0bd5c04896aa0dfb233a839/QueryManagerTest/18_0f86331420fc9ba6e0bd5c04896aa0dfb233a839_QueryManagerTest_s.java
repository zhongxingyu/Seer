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
 
 import static org.junit.Assert.assertEquals;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.code.geobeagle.Geocache;
import com.google.code.geobeagle.database.GeocachesLoader;
 import com.google.code.geobeagle.database.WhereFactoryFixedArea;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import java.util.ArrayList;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
         DensityPatchManager.class, MapView.class, QueryManager.class
 })
 public class QueryManagerTest {
     @Test
     public void testLoad() throws Exception {
         GeoPoint topLeft = PowerMock.createMock(GeoPoint.class);
         GeoPoint bottomRight = PowerMock.createMock(GeoPoint.class);
         QueryManager.PeggedLoader peggedLoader = PowerMock
                 .createMock(QueryManager.PeggedLoader.class);
         WhereFactoryFixedArea whereFactoryFixedArea = PowerMock
                 .createMock(WhereFactoryFixedArea.class);
         ArrayList<Geocache> list = new ArrayList<Geocache>();
         int[] latLonMinMax = {
                 0, 0, 0, 0
         };
 
         PowerMock.expectNew(WhereFactoryFixedArea.class, 35.99, -122.02, 37.01, -120.98).andReturn(
                 whereFactoryFixedArea);
         EasyMock.expect(bottomRight.getLatitudeE6()).andReturn(36000000);
         EasyMock.expect(bottomRight.getLongitudeE6()).andReturn(-121000000);
         EasyMock.expect(topLeft.getLatitudeE6()).andReturn(37000000);
         EasyMock.expect(topLeft.getLongitudeE6()).andReturn(-122000000);
         EasyMock.expect(
                 peggedLoader.load(35990000, -122020000, 37010000, -120980000,
                         whereFactoryFixedArea, latLonMinMax)).andReturn(list);
         PowerMock.replayAll();
 
         QueryManager queryManager = new QueryManager(peggedLoader, latLonMinMax);
         assertEquals(list, queryManager.load(topLeft, bottomRight));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testNeedsLoadingFalse() {
         GeoPoint newTopLeft = PowerMock.createMock(GeoPoint.class);
         GeoPoint newBottomRight = PowerMock.createMock(GeoPoint.class);
 
         EasyMock.expect(newTopLeft.getLatitudeE6()).andReturn(36000000);
         EasyMock.expect(newBottomRight.getLatitudeE6()).andReturn(35000000);
 
         EasyMock.expect(newTopLeft.getLongitudeE6()).andReturn(-121000000);
         EasyMock.expect(newBottomRight.getLongitudeE6()).andReturn(-120000000);
 
         PowerMock.replayAll();
         int[] latLonMinMax = {
                 34000000, -122000000, 37000000, -119000000
         };
         QueryManager queryManager = new QueryManager(null, latLonMinMax);
         assertEquals(false, queryManager.needsLoading(newTopLeft, newBottomRight));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testNeedsLoadingTrue() {
         GeoPoint newTopLeft = PowerMock.createMock(GeoPoint.class);
         GeoPoint newBottomRight = PowerMock.createMock(GeoPoint.class);
 
         EasyMock.expect(newTopLeft.getLatitudeE6()).andReturn(37000000);
 
         PowerMock.replayAll();
         int[] latLonMinMax = {
                 0, 0, 0, 0
         };
         QueryManager queryManager = new QueryManager(null, latLonMinMax);
         assertEquals(true, queryManager.needsLoading(newTopLeft, newBottomRight));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testPeggedLoader() {
        GeocachesLoader geocachesLoader = PowerMock.createMock(GeocachesLoader.class);
         WhereFactoryFixedArea where = PowerMock.createMock(WhereFactoryFixedArea.class);
         ArrayList<Geocache> nullList = new ArrayList<Geocache>();
         int[] newBounds = {
                 0, 0, 0, 0
         };
         ArrayList<Geocache> fullList = new ArrayList<Geocache>();
         EasyMock.expect(geocachesLoader.count(0, 0, where)).andReturn(100);
         EasyMock.expect(geocachesLoader.loadCaches(0, 0, where)).andReturn(fullList);
         PowerMock.replayAll();
         final ArrayList<Geocache> list = new QueryManager.PeggedLoader(geocachesLoader, nullList)
                 .load(0, 1, 2, 3, where, newBounds);
         assertEquals(fullList, list);
         assertEquals(newBounds[0], 0);
         assertEquals(newBounds[1], 1);
         assertEquals(newBounds[2], 2);
         assertEquals(newBounds[3], 3);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testPeggedLoaderTooMany() {
        GeocachesLoader geocachesLoader = PowerMock.createMock(GeocachesLoader.class);
         WhereFactoryFixedArea where = PowerMock.createMock(WhereFactoryFixedArea.class);
         ArrayList<Geocache> nullList = new ArrayList<Geocache>();
         int[] newBounds = {
                 0, 0, 0, 0
         };
 
         EasyMock.expect(geocachesLoader.count(0, 0, where)).andReturn(2000);
 
         PowerMock.replayAll();
         final ArrayList<Geocache> list = new QueryManager.PeggedLoader(geocachesLoader, nullList)
                 .load(0, 1, 2, 3, where, newBounds);
         assertEquals(nullList, list);
         assertEquals(newBounds[0], 0);
         assertEquals(newBounds[1], 0);
         assertEquals(newBounds[2], 0);
         assertEquals(newBounds[3], 0);
         PowerMock.verifyAll();
     }
 
 }
