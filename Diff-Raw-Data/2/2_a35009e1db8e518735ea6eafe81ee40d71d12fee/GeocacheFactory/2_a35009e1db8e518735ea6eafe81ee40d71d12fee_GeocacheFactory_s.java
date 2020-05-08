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
 
 package com.google.code.geobeagle.data;
 
 import com.google.code.geobeagle.data.GeocacheFactory.Source.SourceFactory;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class GeocacheFactory {
     public static class CreateGeocacheFromParcel implements Parcelable.Creator<Geocache> {
         private final GeocacheFromParcelFactory mGeocacheFromParcelFactory = new GeocacheFromParcelFactory(
                 new GeocacheFactory());
 
         public Geocache createFromParcel(Parcel in) {
             return mGeocacheFromParcelFactory.create(in);
         }
 
         public Geocache[] newArray(int size) {
             return new Geocache[size];
         }
     }
 
     public static enum Source {
         GPX(0), MY_LOCATION(1), WEB_URL(2);
 
         public static class SourceFactory {
             private final Source mSources[] = new Source[values().length];
 
             public SourceFactory() {
                 for (Source source : values())
                     mSources[source.mIx] = source;
             }
 
             public Source fromInt(int i) {
                 return mSources[i];
             }
         }
 
         private final int mIx;
 
         Source(int ix) {
             mIx = ix;
         }
 
         public int toInt() {
             return mIx;
         }
     }
 
     public static enum Provider {
         ATLAS_QUEST(0), GROUNDSPEAK(1), MY_LOCATION(-1);
 
         private final int mIx;
 
         Provider(int ix) {
             mIx = ix;
         }
 
         public int toInt() {
             return mIx;
         }
     }
 
     private static SourceFactory mSourceFactory;
 
     public GeocacheFactory() {
         mSourceFactory = new SourceFactory();
     }
 
     public Geocache create(CharSequence id, CharSequence name, double latitude, double longitude,
             Source sourceType, String sourceName) {
         if (id.length() < 2) {
             // ID is missing for waypoints imported from the browser; create a
             // new id
             // from the time.
             id = String.format("WP%1$tk%1$tM%1$tS", System.currentTimeMillis());
         }
         return new Geocache(id, name, latitude, longitude, sourceType, sourceName);
     }
 
     public Source sourceFromInt(int sourceIx) {
         return mSourceFactory.fromInt(sourceIx);
     }
 
 }
