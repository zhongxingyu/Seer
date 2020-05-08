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
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.OverlayItem;
 import com.google.code.geobeagle.Geocache;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.content.res.Resources;
import android.graphics.drawable.Drawable;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
         OverlayItem.class, CacheItem.class
 })
 public class CacheItemTest {
     @Test
     public void testCreateCacheItem() {
         Geocache geocache = PowerMock.createMock(Geocache.class);
         GeoPoint geoPoint = PowerMock.createMock(GeoPoint.class);
         Resources resources = PowerMock.createMock(Resources.class);
        Drawable iconMap = PowerMock.createMock(Drawable.class);
 
         EasyMock.expect(geocache.getGeoPoint()).andReturn(geoPoint);
        EasyMock.expect(geocache.getIconMap(resources)).andReturn(iconMap);
         PowerMock.suppressConstructor(CacheItem.class);
         PowerMock.suppressMethod(CacheItem.class, "setMarker");
 
         PowerMock.replayAll();
         new CacheItemFactory(resources).createCacheItem(geocache);
         PowerMock.verifyAll();
     }
 }
