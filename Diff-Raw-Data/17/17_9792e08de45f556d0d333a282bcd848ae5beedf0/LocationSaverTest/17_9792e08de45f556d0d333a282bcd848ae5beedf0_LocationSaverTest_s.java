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
 
 package com.google.code.geobeagle.database;
 
 import static org.easymock.EasyMock.expect;
 
 import com.google.code.geobeagle.CacheType;
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.GeocacheFactory.Source;
 import com.google.code.geobeagle.database.CacheWriter;
 import com.google.code.geobeagle.database.DatabaseDI;
 import com.google.code.geobeagle.database.LocationSaver;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @PrepareForTest( {
     DatabaseDI.class
 })
 @RunWith(PowerMockRunner.class)
 public class LocationSaverTest {
 
     @Test
     public void testSave() {
         CacheWriter writer = PowerMock.createMock(CacheWriter.class);
         Geocache geocache = PowerMock.createMock(Geocache.class);
         DbFrontend dbFrontend = PowerMock.createMock(DbFrontend.class);
        expect(dbFrontend.getCacheWriter()).andReturn(writer);                
         PowerMock.mockStatic(DatabaseDI.class);
 
         writer.startWriting();
         expect(geocache.getId()).andReturn("LB12345");
         expect(geocache.getName()).andReturn("");
         expect(geocache.getLatitude()).andReturn(122.0);
         expect(geocache.getLongitude()).andReturn(37.0);
         expect(geocache.getSourceType()).andReturn(Source.GPX);
         expect(geocache.getSourceName()).andReturn("manhattan");
         expect(geocache.getCacheType()).andReturn(CacheType.TRADITIONAL);
         writer.insertAndUpdateCache("LB12345", "", 122, 37, Source.GPX, "manhattan",
                CacheType.TRADITIONAL, 0, 0, 0);
         writer.stopWriting();
 
         PowerMock.replayAll();
         new LocationSaver(dbFrontend).saveLocation(geocache);
         PowerMock.verifyAll();
     }
 }
