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
 
 import com.google.android.maps.Overlay;
 import com.google.code.geobeagle.R;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 
 import android.app.Activity;
 import android.util.Log;
 
 import java.util.List;
 
 public class OverlayManager {
     static final int DENSITY_MAP_ZOOM_THRESHOLD = 13;
     private final CachePinsOverlayFactory mCachePinsOverlayFactory;
     private final DensityOverlay mDensityOverlay;
     private boolean mUsesDensityMap;
     private final GeoMapActivity mActivity;
 
    public OverlayManager(Activity activity,
            DensityOverlay densityOverlay,
            CachePinsOverlayFactory cachePinsOverlayFactory,
            NullOverlay nullOverlay) {
        mActivity = (GeoMapActivity)activity;
        mDensityOverlay = densityOverlay;
        mCachePinsOverlayFactory = cachePinsOverlayFactory;
        init(nullOverlay);
    }


     @Inject
     public OverlayManager(Injector injector) {
         mActivity = (GeoMapActivity)injector.getInstance(Activity.class);
         mDensityOverlay = injector.getInstance(DensityOverlay.class);
         mCachePinsOverlayFactory = injector.getInstance(CachePinsOverlayFactory.class);
         NullOverlay nullOverlay = injector.getInstance(NullOverlay.class);
        init(nullOverlay);
    }


    void init(NullOverlay nullOverlay) {
         mUsesDensityMap = false;
         GeoMapView geoMapView = (GeoMapView)mActivity.findViewById(R.id.mapview);
         Overlay myLocationOverlay = mActivity.getMyLocationOverlay();
         final List<Overlay> mapOverlays = geoMapView.getOverlays();
         mapOverlays.add(nullOverlay);
         mapOverlays.add(myLocationOverlay);
     }
 
     public void selectOverlay() {
         GeoMapView geoMapView = (GeoMapView)mActivity.findViewById(R.id.mapview);
         final List<Overlay> mapOverlays = geoMapView.getOverlays();
         final int zoomLevel = geoMapView.getZoomLevel();
         Log.d("GeoBeagle", "Zoom: " + zoomLevel);
         boolean newZoomUsesDensityMap = zoomLevel < OverlayManager.DENSITY_MAP_ZOOM_THRESHOLD;
         if (newZoomUsesDensityMap && mUsesDensityMap)
             return;
         mUsesDensityMap = newZoomUsesDensityMap;
         mapOverlays.set(0, mUsesDensityMap ? mDensityOverlay : mCachePinsOverlayFactory
                 .getCachePinsOverlay());
     }
 
     public boolean usesDensityMap() {
         return mUsesDensityMap;
     }
 }
