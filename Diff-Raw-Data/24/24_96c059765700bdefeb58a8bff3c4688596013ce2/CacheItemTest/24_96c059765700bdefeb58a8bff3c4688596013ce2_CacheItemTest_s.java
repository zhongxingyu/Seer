 
 package com.google.code.geobeagle.activity.map;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.OverlayItem;
import com.google.code.geobeagle.CacheType;
 import com.google.code.geobeagle.Geocache;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.content.res.Resources;
 
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
 
         EasyMock.expect(geocache.getGeoPoint()).andReturn(geoPoint);
        EasyMock.expect(geocache.getId()).andReturn("GC123");
        EasyMock.expect(geocache.getCacheType()).andReturn(CacheType.MULTI);

         PowerMock.suppressConstructor(CacheItem.class);
         PowerMock.suppressMethod(CacheItem.class, "setMarker");
 
         PowerMock.replayAll();
         new CacheItemFactory(resources).createCacheItem(geocache);
         PowerMock.verifyAll();
     }
 }
