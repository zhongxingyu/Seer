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
 
 package com.google.code.geobeagle.activity.cachelist.presenter;
 
 import com.google.code.geobeagle.activity.cachelist.SearchTarget;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.util.AttributeSet;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 
 public class NoCachesView extends WebView {
 
    private static final String NO_CACHES_FOUND_HTML = "file:///android_asset/no_caches_found.html";
    private static final String NO_CACHES = "file:///android_asset/no_caches.html";
     private SearchTarget searchTarget;
 
     public NoCachesView(Context context) {
         super(context);
         setup();
     }
 
     public NoCachesView(Context context, AttributeSet attrs) {
         super(context, attrs);
         setup();
     }
 
     public NoCachesView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         setup();
     }
 
     @Override
     public void onDraw(Canvas canvas) {
        // Log.d("GeoBeagle", "getUrl: " + getUrl());
         if (searchTarget == null || searchTarget.getTarget() == null) {
            if (getUrl() == null || 0 != getUrl().compareTo(NO_CACHES))
                loadUrl(NO_CACHES);
        } else if (getUrl() == null || 0 != getUrl().compareTo(NO_CACHES_FOUND_HTML)) {
            loadUrl(NO_CACHES_FOUND_HTML);
         }
         super.onDraw(canvas);
     }
 
     public void setSearchTarget(SearchTarget searchTarget) {
         this.searchTarget = searchTarget;
     }
 
     private void setup() {
         WebSettings webSettings = getSettings();
         webSettings.setSavePassword(false);
         webSettings.setSaveFormData(false);
         webSettings.setSupportZoom(false);
         setBackgroundColor(Color.BLACK);
     }
 
 }
