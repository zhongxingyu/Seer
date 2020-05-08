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
 
 package com.google.code.geobeagle.activity.main.view;
 
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.GeocacheFactory;
 import com.google.code.geobeagle.GeocacheFactory.Provider;
 import com.google.code.geobeagle.GeocacheFactory.Source;
 import com.google.code.geobeagle.activity.main.GeoBeagle;
 
 import android.view.View;
 
 public class WebPageAndDetailsButtonEnabler {
     interface CheckButton {
         public void check(Geocache geocache);
     }
 
     public static class CheckButtons {
         private final CheckButton[] mCheckButtons;
 
         public CheckButtons(CheckButton[] checkButtons) {
             mCheckButtons = checkButtons;
         }
 
         public void check(Geocache geocache) {
             for (CheckButton checkButton : mCheckButtons) {
                 checkButton.check(geocache);
             }
         }
     }
 
     public static class CheckDetailsButton implements CheckButton {
         private final View mDetailsButton;
 
         public CheckDetailsButton(View checkDetailsButton) {
             mDetailsButton = checkDetailsButton;
         }
 
         public void check(Geocache geocache) {
             mDetailsButton.setEnabled(geocache.getSourceType() == Source.GPX);
         }
     }
 
     public static class CheckWebPageButton implements CheckButton {
         private final View mWebPageButton;
 
         public CheckWebPageButton(View webPageButton) {
             mWebPageButton = webPageButton;
         }
 
         public void check(Geocache geocache) {
             final Provider contentProvider = geocache.getContentProvider();
             mWebPageButton.setEnabled(contentProvider == Provider.GROUNDSPEAK
                    || contentProvider == GeocacheFactory.Provider.ATLAS_QUEST
                    || contentProvider == GeocacheFactory.Provider.OPENCACHING);
         }
     }
 
     private final CheckButtons mCheckButtons;
     private final GeoBeagle mGeoBeagle;
 
     public WebPageAndDetailsButtonEnabler(GeoBeagle geoBeagle, CheckButtons checkButtons) {
         mGeoBeagle = geoBeagle;
         mCheckButtons = checkButtons;
     }
 
     public void check() {
         mCheckButtons.check(mGeoBeagle.getGeocache());
     }
 }
