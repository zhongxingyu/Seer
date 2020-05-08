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
 
 package com.google.code.geobeagle;
 
 import com.google.code.geobeagle.ui.GpsStatusWidget;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 
 /*
  * Listener for the Location control.
  */
 public class GeoBeagleLocationListener implements LocationListener {
     private final LocationControl mLocationControl;
    private final GpsStatusWidget mLocationViewer;
 
     public GeoBeagleLocationListener(LocationControl locationControl, GpsStatusWidget gpsStatusWidget) {
        mLocationViewer = gpsStatusWidget;
         mLocationControl = locationControl;
     }
 
     public void onLocationChanged(Location location) {
         // Ask the location control to pick the most accurate location (might
         // not be this one).
        mLocationViewer.setLocation(mLocationControl.getLocation());
     }
 
     public void onProviderDisabled(String provider) {
        mLocationViewer.setDisabled();
     }
 
     public void onProviderEnabled(String provider) {
        mLocationViewer.setEnabled();
     }
 
     public void onStatusChanged(String provider, int status, Bundle extras) {
        mLocationViewer.setStatus(provider, status);
     }
 }
