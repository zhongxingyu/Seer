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
 
 package com.google.code.geobeagle.activity.main.intents;
 
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.ResourceProvider;
 
 import java.net.URLEncoder;
 import java.util.Locale;
 
 public class GeocacheToGoogleMap implements GeocacheToUri {
 
     private final ResourceProvider mResourceProvider;
 
     public GeocacheToGoogleMap(ResourceProvider resourceProvider) {
         mResourceProvider = resourceProvider;
     }
 
    /*
     * (non-Javadoc)
     * @see
     * com.google.code.geobeagle.activity.main.intents.GeocacheToUri#convert
     * (com.google.code.geobeagle.Geocache)
     */
     public String convert(Geocache geocache) {
         // "geo:%1$.5f,%2$.5f?name=cachename"
        String idAndName = (String)geocache.getIdAndName();
        idAndName = idAndName.replace("(", "[");
        idAndName = idAndName.replace(")", "]");
        idAndName = URLEncoder.encode(idAndName);
         final String format = String.format(Locale.US, mResourceProvider
                 .getString(R.string.map_intent), geocache.getLatitude(), geocache.getLongitude(),
                 idAndName);
         return format;
     }
 }
