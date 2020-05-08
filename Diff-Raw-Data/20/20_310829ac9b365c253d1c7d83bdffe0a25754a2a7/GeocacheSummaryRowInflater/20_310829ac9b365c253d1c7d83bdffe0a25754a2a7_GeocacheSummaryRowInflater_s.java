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
 
 package com.google.code.geobeagle.activity.cachelist.view;
 
 import com.google.code.geobeagle.CacheType;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.activity.cachelist.model.GeocacheVector;
 import com.google.code.geobeagle.activity.cachelist.model.GeocacheVectors;
 import com.google.code.geobeagle.activity.cachelist.presenter.AbsoluteBearingFormatter;
 import com.google.code.geobeagle.activity.cachelist.presenter.BearingFormatter;
 import com.google.code.geobeagle.activity.cachelist.presenter.HasDistanceFormatter;
 import com.google.code.geobeagle.activity.cachelist.presenter.RelativeBearingFormatter;
 import com.google.code.geobeagle.formatting.DistanceFormatter;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class GeocacheSummaryRowInflater implements HasDistanceFormatter {
     static class RowViews {
         private final TextView mAttributes;
         private final TextView mCacheName;
         private final TextView mDistance;
         private final ImageView mIcon;
         private final TextView mId;
 
         RowViews(TextView attributes, TextView cacheName, TextView distance, ImageView icon,
                 TextView id) {
            mIcon = icon;
            mCacheName = cacheName;
            mId = id;
             mAttributes = attributes;
             mDistance = distance;
         }
 
         void set(GeocacheVector geocacheVector, DistanceFormatter distanceFormatter,
                 BearingFormatter relativeBearingFormatter) {
             CacheType type = geocacheVector.getGeocache().getCacheType();
             mIcon.setImageResource(type.icon());
             mId.setText(geocacheVector.getId());
             mAttributes.setText(geocacheVector.getFormattedAttributes());
             mCacheName.setText(geocacheVector.getName());
             mDistance.setText(geocacheVector.getFormattedDistance(distanceFormatter,
                     relativeBearingFormatter));
         }
     }
 
     private BearingFormatter mBearingFormatter;
     private DistanceFormatter mDistanceFormatter;
     private final GeocacheVectors mGeocacheVectors;
     private final LayoutInflater mLayoutInflater;
 
     public GeocacheSummaryRowInflater(DistanceFormatter distanceFormatter,
             GeocacheVectors geocacheVectors, LayoutInflater layoutInflater,
             BearingFormatter relativeBearingFormatter) {
         mLayoutInflater = layoutInflater;
         mGeocacheVectors = geocacheVectors;
         mDistanceFormatter = distanceFormatter;
         mBearingFormatter = relativeBearingFormatter;
     }
 
     BearingFormatter getBearingFormatter() {
         return mBearingFormatter;
     }
 
     public View inflate(View convertView) {
         if (convertView != null)
             return convertView;
        // Log.d("GeoBeagle", "SummaryRow::inflate(" + convertView + ")");
 
         View view = mLayoutInflater.inflate(R.layout.cache_row, null);
         RowViews rowViews = new RowViews((TextView)view.findViewById(R.id.txt_gcattributes),
                ((TextView)view
                        .findViewById(R.id.txt_cache)), ((TextView)view.findViewById(R.id.distance)),
                ((ImageView)view.findViewById(R.id.gc_row_icon)), ((TextView)view
                         .findViewById(R.id.txt_gcid)));
         view.setTag(rowViews);
         return view;
     }
 
     public void setBearingFormatter(boolean absoluteBearing) {
         mBearingFormatter = absoluteBearing ? new AbsoluteBearingFormatter()
                 : new RelativeBearingFormatter();
     }
 
     public void setData(View view, int position) {
         ((RowViews)view.getTag()).set(mGeocacheVectors.get(position), mDistanceFormatter,
                 mBearingFormatter);
     }
 
     public void setDistanceFormatter(DistanceFormatter distanceFormatter) {
         mDistanceFormatter = distanceFormatter;
     }
 }
