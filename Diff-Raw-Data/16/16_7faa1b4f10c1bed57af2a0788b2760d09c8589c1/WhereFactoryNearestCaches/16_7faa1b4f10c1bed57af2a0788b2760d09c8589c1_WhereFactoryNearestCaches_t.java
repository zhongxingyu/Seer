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
 
 package com.google.code.geobeagle.database;
 
 import com.google.code.geobeagle.activity.cachelist.SearchWhereFactory;
 import com.google.code.geobeagle.database.DatabaseDI.SearchFactory;
 import com.google.inject.Inject;
 
 import android.database.Cursor;
 import android.util.Log;
 
 public class WhereFactoryNearestCaches implements WhereFactory {
     static class BoundingBox {
         public static final String[] ID_COLUMN = new String[] {
             "Id"
         };
         private final ISQLiteDatabase mSqliteWrapper;
         private final WhereStringFactory mWhereStringFactory;
         private final double mLatitude;
         private final double mLongitude;
        private final SearchWhereFactory mSearchWhereFactory;
 
         BoundingBox(double latitude, double longitude, ISQLiteDatabase sqliteWrapper,
                WhereStringFactory whereStringFactory, SearchWhereFactory searchWhereFactory) {
             mLatitude = latitude;
             mLongitude = longitude;
             mSqliteWrapper = sqliteWrapper;
             mWhereStringFactory = whereStringFactory;
            mSearchWhereFactory = searchWhereFactory;
         }
 
         int getCount(float degreesDelta, int maxCount) {
            String where = mWhereStringFactory.getWhereString(mLatitude, mLongitude, degreesDelta)
                    + mSearchWhereFactory.getWhereString();
 
             Cursor cursor = mSqliteWrapper.query(Database.TBL_CACHES, ID_COLUMN, where, null, null,
                     null, "" + maxCount);
             int count = cursor.getCount();
             Log.d("GeoBeagle", "search: " + degreesDelta + ", count/maxCount: " + count + "/"
                     + maxCount + " where: " + where);
             cursor.close();
 
             return count;
         }
     }
 
     static class Search {
         private final BoundingBox mBoundingBox;
         private final SearchDown mSearchDown;
         private final SearchUp mSearchUp;
 
         public Search(BoundingBox boundingBox, SearchDown searchDown, SearchUp searchUp) {
             mBoundingBox = boundingBox;
             mSearchDown = searchDown;
             mSearchUp = searchUp;
         }
 
         public float search(float guess, int target) {
             if (mBoundingBox.getCount(guess, target + 1) > target)
                 return mSearchDown.search(guess, target);
             return mSearchUp.search(guess, target);
         }
     }
 
     static public class SearchDown {
         private final BoundingBox mHasValue;
         private final float mMin;
 
         public SearchDown(BoundingBox boundingBox, float min) {
             mHasValue = boundingBox;
             mMin = min;
         }
 
         public float search(float guess, int targetMin) {
             final float lowerGuess = guess / WhereFactoryNearestCaches.DISTANCE_MULTIPLIER;
             if (lowerGuess < mMin)
                 return guess;
             if (mHasValue.getCount(lowerGuess, targetMin + 1) >= targetMin)
                 return search(lowerGuess, targetMin);
             return guess;
         }
     }
 
     static class SearchUp {
         private final BoundingBox mBoundingBox;
         private final float mMax;
 
         public SearchUp(BoundingBox boundingBox, float max) {
             mBoundingBox = boundingBox;
             mMax = max;
         }
 
         public float search(float guess, int targetMin) {
             final float nextGuess = guess * WhereFactoryNearestCaches.DISTANCE_MULTIPLIER;
             if (nextGuess > mMax)
                 return guess;
             if (mBoundingBox.getCount(guess, targetMin) < targetMin) {
                 return search(nextGuess, targetMin);
             }
             return guess;
         }
     }
 
     // 1 degree ~= 111km
     static final float DEGREES_DELTA = 0.1f;
     static final float DISTANCE_MULTIPLIER = 1.414f;
     static final int GUESS_MAX = 180;
     static final float GUESS_MIN = 0.01f;
     static final int MAX_NUMBER_OF_CACHES = 100;
 
     public static class WhereStringFactory {
         String getWhereString(double latitude, double longitude, float degrees) {
             double latLow = latitude - degrees;
             double latHigh = latitude + degrees;
             double lat_radians = Math.toRadians(latitude);
             double cos_lat = Math.cos(lat_radians);
             double lonLow = Math.max(-180, longitude - degrees / cos_lat);
             double lonHigh = Math.min(180, longitude + degrees / cos_lat);
             return "Visible = 1 AND Latitude > " + latLow + " AND Latitude < " + latHigh
                     + " AND Longitude > " + lonLow + " AND Longitude < " + lonHigh;
         }
     }
 
     private float mLastGuess = 0.1f;
     private final SearchFactory mSearchFactory;
     private final WhereStringFactory mWhereStringFactory;
     private final DbFrontend mDbFrontend;
     private final SearchWhereFactory mSearchWhereFactory;
 
     @Inject
     public WhereFactoryNearestCaches(SearchFactory searchFactory,
             WhereStringFactory whereStringFactory,
             SearchWhereFactory searchWhereFactory,
             DbFrontend dbFrontend) {
         mSearchFactory = searchFactory;
         mWhereStringFactory = whereStringFactory;
         mDbFrontend = dbFrontend;
         mSearchWhereFactory = searchWhereFactory;
     }
 
     @Override
     public String getWhere(ISQLiteDatabase sqliteWrapper, double latitude, double longitude) {
         int totalCaches = mDbFrontend.countAll();
         int maxNumberOfCaches = Math.min(totalCaches, MAX_NUMBER_OF_CACHES);
         mLastGuess = mSearchFactory.createSearch(latitude, longitude, GUESS_MIN, GUESS_MAX,
                 sqliteWrapper).search(mLastGuess, maxNumberOfCaches);
         return mWhereStringFactory.getWhereString(latitude, longitude, mLastGuess)
                 + mSearchWhereFactory.getWhereString();
     }
 
 }
