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
 
 import static org.junit.Assert.*;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.Projection;
 import com.google.code.geobeagle.GeocacheList;
 import com.google.code.geobeagle.GeocacheListPrecomputed;
 import com.google.code.geobeagle.database.CachesProviderLazyArea;
 
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.View;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
         View.class, Log.class, CachePinsOverlay.class, GeoMapView.class,
         Context.class, Drawable.class, GeoPoint.class
 })
 public class CachePinsOverlayFactoryTest {
     @Before
     public void mockLogging() {
         PowerMock.mockStatic(Log.class);
         EasyMock.expect(Log.d((String)(EasyMock.anyObject()), (String)(EasyMock.anyObject())))
                 .andReturn(0).anyTimes();
     }
 
     @Test
     public void testGetCachePinsOverlay() throws Exception {
         GeoMapView geoMapView = PowerMock.createMock(GeoMapView.class);
         Context context = PowerMock.createMock(Context.class);
         Drawable defaultMarker = PowerMock.createMock(Drawable.class);
         CacheItemFactory cacheItemFactory = PowerMock.createMock(CacheItemFactory.class);
         CachesProviderLazyArea lazyArea = PowerMock.createMock(CachesProviderLazyArea.class);
         CachePinsOverlay cachePinsOverlay = PowerMock.createMock(CachePinsOverlay.class);
         Projection projection = PowerMock.createMock(Projection.class);
         GeoPoint newTopLeft = PowerMock.createMock(GeoPoint.class);
         GeoPoint newBottomRight = PowerMock.createMock(GeoPoint.class);
         GeocacheList list = new GeocacheListPrecomputed();
 
         EasyMock.expect(geoMapView.getProjection()).andReturn(projection);
         EasyMock.expect(projection.fromPixels(0, 0)).andReturn(newTopLeft);
         EasyMock.expect(geoMapView.getRight()).andReturn(100);
         EasyMock.expect(geoMapView.getBottom()).andReturn(200);
         EasyMock.expect(projection.fromPixels(100, 200)).andReturn(newBottomRight);
        EasyMock.expect(newTopLeft.getLatitudeE6()).andReturn(38000000);
        EasyMock.expect(newTopLeft.getLongitudeE6()).andReturn(-122000000);
        EasyMock.expect(newBottomRight.getLatitudeE6()).andReturn(37000000);
        EasyMock.expect(newBottomRight.getLongitudeE6()).andReturn(-121000000);
        lazyArea.setBounds(37, -122, 38, -121);
        EasyMock.expect(lazyArea.hasChanged()).andReturn(true);
        EasyMock.expect(lazyArea.tooManyCaches()).andReturn(false);
        EasyMock.expect(lazyArea.getCaches()).andReturn(list);
        lazyArea.resetChanged();
         PowerMock.expectNew(CachePinsOverlay.class, cacheItemFactory, context, defaultMarker, list)
                 .andReturn(cachePinsOverlay);
 
         PowerMock.replayAll();
         final CachePinsOverlayFactory cachePinsOverlayFactory = new CachePinsOverlayFactory(
                 geoMapView, context, defaultMarker, cacheItemFactory, null, lazyArea, null);
         assertEquals(cachePinsOverlay, cachePinsOverlayFactory.getCachePinsOverlay());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testGetCachePinsOverlayDoesntNeedLoading() throws Exception {
         GeoMapView geoMapView = PowerMock.createMock(GeoMapView.class);
        Context context = PowerMock.createMock(Context.class);
        Drawable defaultMarker = PowerMock.createMock(Drawable.class);
        CacheItemFactory cacheItemFactory = PowerMock.createMock(CacheItemFactory.class);
         CachesProviderLazyArea lazyArea = PowerMock.createMock(CachesProviderLazyArea.class);
         CachePinsOverlay cachePinsOverlay = PowerMock.createMock(CachePinsOverlay.class);
         Projection projection = PowerMock.createMock(Projection.class);
         GeoPoint newTopLeft = PowerMock.createMock(GeoPoint.class);
         GeoPoint newBottomRight = PowerMock.createMock(GeoPoint.class);
 
         EasyMock.expect(geoMapView.getProjection()).andReturn(projection);
         EasyMock.expect(projection.fromPixels(0, 0)).andReturn(newTopLeft);
         EasyMock.expect(geoMapView.getRight()).andReturn(100);
         EasyMock.expect(geoMapView.getBottom()).andReturn(200);
         EasyMock.expect(projection.fromPixels(100, 200)).andReturn(newBottomRight);
        EasyMock.expect(newTopLeft.getLatitudeE6()).andReturn(38000000);
        EasyMock.expect(newTopLeft.getLongitudeE6()).andReturn(-122000000);
        EasyMock.expect(newBottomRight.getLatitudeE6()).andReturn(37000000);
        EasyMock.expect(newBottomRight.getLongitudeE6()).andReturn(-121000000);
        lazyArea.setBounds(37, -122, 38, -121);
        EasyMock.expect(lazyArea.hasChanged()).andReturn(false);
 
         PowerMock.replayAll();
         final CachePinsOverlayFactory cachePinsOverlayFactory = new CachePinsOverlayFactory(
                geoMapView, context, defaultMarker, cacheItemFactory, cachePinsOverlay, lazyArea, null);
         assertEquals(cachePinsOverlay, cachePinsOverlayFactory.getCachePinsOverlay());
         PowerMock.verifyAll();
     }
 }
