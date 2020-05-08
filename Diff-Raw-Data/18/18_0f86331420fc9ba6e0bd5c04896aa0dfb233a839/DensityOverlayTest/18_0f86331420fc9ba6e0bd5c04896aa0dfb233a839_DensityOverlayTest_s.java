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
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.Projection;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Rect;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
         DensityMatrix.class, GeoPoint.class, MapView.class, Overlay.class, Rect.class
 })
 public class DensityOverlayTest {
     @Test
     public void testDensityOverlayShadow() {
         DensityOverlayDelegate densityOverlayDelegate = new DensityOverlayDelegate(null, null,
                null, null, null, null);
         densityOverlayDelegate.draw(null, null, true);
     }
 
     @Test
     public void testDensityOverlay() {
         Canvas canvas = PowerMock.createMock(Canvas.class);
         MapView mapView = PowerMock.createMock(MapView.class);
         Point screenTopLeft = PowerMock.createMock(Point.class);
         Point screenBottomRight = PowerMock.createMock(Point.class);
         Rect screenRect = PowerMock.createMock(Rect.class);
         Paint paint = PowerMock.createMock(Paint.class);
         Projection projection = PowerMock.createMock(Projection.class);
         DensityPatchManager densityPatchManager = PowerMock.createMock(DensityPatchManager.class);
         DensityMatrix.DensityPatch densityPatch = PowerMock
                 .createMock(DensityMatrix.DensityPatch.class);
         GeoPoint newGeoTopLeft = PowerMock.createMock(GeoPoint.class);
         GeoPoint newGeoBottomRight = PowerMock.createMock(GeoPoint.class);
 
         List<DensityMatrix.DensityPatch> densityPatches = new ArrayList<DensityMatrix.DensityPatch>();
         densityPatches.add(densityPatch);
         EasyMock.expect(densityPatchManager.getDensityPatches(mapView)).andReturn(densityPatches);
 
         EasyMock.expect(mapView.getProjection()).andReturn(projection);
         EasyMock.expect(mapView.getRight()).andReturn(100);
         EasyMock.expect(mapView.getBottom()).andReturn(100);
         EasyMock.expect(projection.fromPixels(100, 100)).andReturn(newGeoBottomRight);
         EasyMock.expect(projection.fromPixels(0, 0)).andReturn(newGeoTopLeft);
 
         // EasyMock.expect(projection.fromPixels(100,
         // 100)).andReturn(newGeoBottomRight);
 
         EasyMock.expect(projection.toPixels(newGeoTopLeft, screenTopLeft)).andReturn(screenTopLeft);
         screenTopLeft.x = 0;
         screenTopLeft.y = 0;
         screenBottomRight.x = 100;
         screenBottomRight.y = 100;
         EasyMock.expect(projection.toPixels(newGeoBottomRight, screenBottomRight)).andReturn(
                 screenBottomRight);
 
         EasyMock.expect(newGeoTopLeft.getLatitudeE6()).andReturn(37000000);
         EasyMock.expect(newGeoTopLeft.getLongitudeE6()).andReturn(-122000000);
         EasyMock.expect(newGeoBottomRight.getLatitudeE6()).andReturn(36000000);
         EasyMock.expect(newGeoBottomRight.getLongitudeE6()).andReturn(-121000000);
         EasyMock.expect(densityPatch.getLatLowE6()).andReturn(36400000);
         EasyMock.expect(densityPatch.getLonLowE6()).andReturn(-121500000);
         screenRect.set(50, 59, 52, 60);
         EasyMock.expect(densityPatch.getAlpha()).andReturn(27);
         paint.setAlpha(27);
         canvas.drawRect(screenRect, paint);
 
         PowerMock.replayAll();
         DensityOverlayDelegate densityOverlayDelegate = new DensityOverlayDelegate(screenRect,
                paint, screenTopLeft, screenBottomRight, null, densityPatchManager);
         densityOverlayDelegate.draw(canvas, mapView, false);
         PowerMock.verifyAll();
     }
 }
