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
 
 import com.google.code.geobeagle.CacheTypeFactory;
 import com.google.code.geobeagle.Geocache.AttributeFormatter;
 import com.google.code.geobeagle.Geocache.AttributeFormatterImpl;
 import com.google.code.geobeagle.Geocache.AttributeFormatterNull;
 import com.google.code.geobeagle.GeocacheFactory.Source.SourceFactory;
 import com.google.code.geobeagle.activity.main.GeocacheFromParcelFactory;
 
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
 
     public static enum Provider {
         ATLAS_QUEST(0, "LB"), GROUNDSPEAK(1, "GC"), MY_LOCATION(-1, "ML"), OPENCACHING(2, "OC");
 
         private final int mIx;
         private final String mPrefix;
 
         Provider(int ix, String prefix) {
             mIx = ix;
             mPrefix = prefix;
         }
 
         public int toInt() {
             return mIx;
         }
 
         public String getPrefix() {
             return mPrefix;
         }
     }
 
     public static Provider ALL_PROVIDERS[] = {
             Provider.ATLAS_QUEST, Provider.GROUNDSPEAK, Provider.MY_LOCATION, Provider.OPENCACHING
     };
 
     public static enum Source {
         GPX(0), LOC(3), MY_LOCATION(1), WEB_URL(2);
         public final static int MIN_SOURCE = 0;
         public final static int MAX_SOURCE = 3;
 
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
 
     static class AttributeFormatterFactory {
         private AttributeFormatterImpl mAttributeFormatterImpl;
         private AttributeFormatterNull mAttributeFormatterNull;
 
         public AttributeFormatterFactory(AttributeFormatterImpl attributeFormatterImpl,
                 AttributeFormatterNull attributeFormatterNull) {
             mAttributeFormatterImpl = attributeFormatterImpl;
             mAttributeFormatterNull = attributeFormatterNull;
         }
 
         AttributeFormatter getAttributeFormatter(Source sourceType) {
             if (sourceType == Source.GPX)
                 return mAttributeFormatterImpl;
             return mAttributeFormatterNull;
         }
     }
 
     private static CacheTypeFactory mCacheTypeFactory;
     private static SourceFactory mSourceFactory;
     private AttributeFormatterFactory mAttributeFormatterFactory;
 
     public GeocacheFactory() {
         mSourceFactory = new SourceFactory();
         mCacheTypeFactory = new CacheTypeFactory();
         mAttributeFormatterFactory = new AttributeFormatterFactory(new AttributeFormatterImpl(),
                 new AttributeFormatterNull());
     }
 
     public CacheType cacheTypeFromInt(int cacheTypeIx) {
         return mCacheTypeFactory.fromInt(cacheTypeIx);
     }
 
     public Geocache create(CharSequence id, CharSequence name, double latitude, double longitude,
             Source sourceType, String sourceName, CacheType cacheType, int difficulty, int terrain,
             int container) {
         if (id.length() < 2) {
             // ID is missing for waypoints imported from the browser; create a
             // new id from the time.
             id = String.format("WP%1$tk%1$tM%1$tS", System.currentTimeMillis());
         }
         if (name == null)
             name = "";
         final AttributeFormatter attributeFormatter = mAttributeFormatterFactory
                 .getAttributeFormatter(sourceType);
         return new Geocache(id, name, latitude, longitude, sourceType, sourceName, cacheType,
                 difficulty, terrain, container, attributeFormatter);
     }
 
     public Source sourceFromInt(int sourceIx) {
         return mSourceFactory.fromInt(sourceIx);
     }
 }
