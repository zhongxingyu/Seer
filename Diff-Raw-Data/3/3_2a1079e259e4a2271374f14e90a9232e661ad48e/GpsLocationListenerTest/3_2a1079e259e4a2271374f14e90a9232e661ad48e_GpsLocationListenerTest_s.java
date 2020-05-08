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
 
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.easymock.classextension.EasyMock.replay;
 import static org.easymock.classextension.EasyMock.verify;
 
 import com.google.code.geobeagle.ui.LocationViewer;
 
 import android.location.Location;
 import android.location.LocationProvider;
 
 import junit.framework.TestCase;
 
 public class GpsLocationListenerTest extends TestCase {
 
     private LocationViewer mLocationViewer;
     private LocationControl mGpsControl;
 
     public void setUp() {
         mLocationViewer = createMock(LocationViewer.class);
         mGpsControl = createMock(LocationControl.class);
     }
 
     public void testOnLocationChanged() {
         Location location = createMock(Location.class);
         mLocationViewer.setLocation(location);
 
         replay(location);
         replay(mGpsControl);
         new GeoBeagleLocationListener(mGpsControl, mLocationViewer).onLocationChanged(location);
         verify(location);
         verify(mGpsControl);
     }
 
     public void testOnStatusChange() {
         mLocationViewer.setStatus("gps", LocationProvider.OUT_OF_SERVICE);
 
         replay(mLocationViewer);
         new GeoBeagleLocationListener(null, mLocationViewer).onStatusChanged("gps",
                 LocationProvider.OUT_OF_SERVICE, null);
         verify(mLocationViewer);
     }
 
     public void testOnEnabled() {
         mLocationViewer.setEnabled();
 
         replay(mLocationViewer);
         new GeoBeagleLocationListener(null, mLocationViewer).onProviderEnabled(null);
         verify(mLocationViewer);
     }
 
     public void testOnDisabled() {
         mLocationViewer.setDisabled();
 
         replay(mLocationViewer);
         new GeoBeagleLocationListener(null, mLocationViewer).onProviderDisabled(null);
         verify(mLocationViewer);
 
     }
 }
