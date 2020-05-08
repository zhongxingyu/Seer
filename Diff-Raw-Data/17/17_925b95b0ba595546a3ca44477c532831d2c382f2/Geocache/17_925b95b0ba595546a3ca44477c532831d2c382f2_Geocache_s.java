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
 
 import com.google.code.geobeagle.GeocacheFactory.Provider;
 import com.google.code.geobeagle.GeocacheFactory.Source;
 
 import android.content.SharedPreferences.Editor;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 /**
  * Geocache or letterbox description, id, and coordinates.
  */
 public class Geocache implements Parcelable {
 
     public static Parcelable.Creator<Geocache> CREATOR = new GeocacheFactory.CreateGeocacheFromParcel();
     public static final String ID = "id";
     public static final String LATITUDE = "latitude";
     public static final String LONGITUDE = "longitude";
     public static final String NAME = "name";
     public static final String SOURCE_NAME = "sourceName";
     public static final String SOURCE_TYPE = "sourceType";
 
     private final CharSequence mId;
     private final double mLatitude;
     private final double mLongitude;
     private final CharSequence mName;
     private final String mSourceName;
     private final Source mSourceType;
     private float[] mDistanceAndBearing = new float[2];
 
     public Geocache(CharSequence id, CharSequence name, double latitude, double longitude,
             Source sourceType, String sourceName) {
         mId = id;
         mName = name;
         mLatitude = latitude;
         mLongitude = longitude;
         mSourceType = sourceType;
         mSourceName = sourceName;
     }
 
     public float[] calculateDistanceAndBearing(Location here) {
         if (here != null) {
             Location.distanceBetween(here.getLatitude(), here.getLongitude(), getLatitude(),
                     getLongitude(), mDistanceAndBearing);
 
             return mDistanceAndBearing;
         }
         mDistanceAndBearing[0] = -1;
         mDistanceAndBearing[1] = -1;
         return mDistanceAndBearing;
     }
 
     public int describeContents() {
         return 0;
     }
 
     public GeocacheFactory.Provider getContentProvider() {
         // Must use toString() rather than mId.subSequence(0,2).equals("GC"),
         // because editing the text in android produces a SpannableString rather
         // than a String, so the CharSequences won't be equal.
         String prefix = mId.subSequence(0, 2).toString();
         if (prefix.equals("ML"))
             return Provider.MY_LOCATION;
         if (prefix.equals("LB"))
             return Provider.ATLAS_QUEST;
         else
             return Provider.GROUNDSPEAK;
     }
 
     public CharSequence getId() {
         return mId;
     }
 
     public CharSequence getIdAndName() {
         if (mId.length() == 0)
             return mName;
         else if (mName.length() == 0)
             return mId;
         else
             return mId + ": " + mName;
     }
 
     public double getLatitude() {
         return mLatitude;
     }
 
     public double getLongitude() {
         return mLongitude;
     }
 
     public CharSequence getName() {
         return mName;
     }
 
     public CharSequence getShortId() {
         if (mId.length() > 2)
             return mId.subSequence(2, mId.length());
         else
             return "";
     }
 
     public String getSourceName() {
         return mSourceName;
     }
 
     public Source getSourceType() {
         return mSourceType;
     }
 
     public void writeToParcel(Parcel out, int flags) {
         Bundle bundle = new Bundle();
         saveToBundle(bundle);
         out.writeBundle(bundle);
     }
 
     public void saveToBundle(Bundle bundle) {
         bundle.putCharSequence(ID, mId);
         bundle.putCharSequence(NAME, mName);
         bundle.putDouble(LATITUDE, mLatitude);
         bundle.putDouble(LONGITUDE, mLongitude);
         bundle.putInt(SOURCE_TYPE, mSourceType.toInt());
         bundle.putString(SOURCE_NAME, mSourceName);
     }
 
     public void writeToPrefs(Editor editor) {
         // Must use toString(), see comment above in getCommentProvider.
         editor.putString(ID, mId.toString());
         editor.putString(NAME, mName.toString());
         editor.putFloat(LATITUDE, (float)mLatitude);
         editor.putFloat(LONGITUDE, (float)mLongitude);
         editor.putInt(SOURCE_TYPE, mSourceType.toInt());
         editor.putString(SOURCE_NAME, mSourceName);
     }
 }
