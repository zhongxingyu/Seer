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
 
 package com.google.code.geobeagle.cachelist;
 
 import static org.easymock.EasyMock.expect;
 
 import com.google.code.geobeagle.activity.cachelist.GeoBeagleTest;
 import com.google.code.geobeagle.activity.cachelist.GpxImporterFactory;
import com.google.code.geobeagle.activity.cachelist.actions.menu.Abortable;
 import com.google.code.geobeagle.activity.cachelist.actions.menu.MenuActionSyncGpx;
 import com.google.code.geobeagle.activity.cachelist.presenter.CacheListRefresh;
 import com.google.code.geobeagle.database.CacheWriter;
 import com.google.code.geobeagle.xmlimport.GpxImporter;
 import com.google.inject.Provider;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @RunWith(PowerMockRunner.class)
 public class MenuActionSyncGpxTest extends GeoBeagleTest {
     @SuppressWarnings("unchecked")
     @Test
     public void testAct() {
         GpxImporter gpxImporter = PowerMock.createMock(GpxImporter.class);
         CacheListRefresh cacheListRefresh = PowerMock.createMock(CacheListRefresh.class);
         GpxImporterFactory gpxImporterFactory = PowerMock.createMock(GpxImporterFactory.class);
         CacheWriter cacheWriter = PowerMock.createMock(CacheWriter.class);
 
         Provider<CacheWriter> dbFrontendProvider = PowerMock.createMock(Provider.class);
         expect(dbFrontendProvider.get()).andReturn(cacheWriter);

         EasyMock.expect(gpxImporterFactory.create(cacheWriter)).andReturn(gpxImporter);
         gpxImporter.importGpxs(cacheListRefresh);
 
         PowerMock.replayAll();
        final MenuActionSyncGpx menuActionSyncGpx = new MenuActionSyncGpx(null, null,
                cacheListRefresh, gpxImporterFactory, dbFrontendProvider);
         menuActionSyncGpx.act();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testAbort() {
        Abortable abortable = PowerMock.createMock(Abortable.class);

         abortable.abort();
 
         PowerMock.replayAll();
        new MenuActionSyncGpx(null, null, null, null, null).abort();
         PowerMock.verifyAll();
     }
 }
