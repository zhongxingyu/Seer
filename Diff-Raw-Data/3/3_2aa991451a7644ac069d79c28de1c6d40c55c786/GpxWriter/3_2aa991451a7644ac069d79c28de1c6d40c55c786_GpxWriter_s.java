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
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 import roboguice.inject.ContextScoped;
 @ContextScoped
 public class GpxWriter {
     private String mGpxTime;
     private final Provider<ISQLiteDatabase> sqliteProvider;
 
     @Inject
     GpxWriter(Provider<ISQLiteDatabase> sqliteProvider) {
         this.sqliteProvider = sqliteProvider;
     }
 
     /**
      * Return True if the gpx is already loaded. Mark this gpx and its caches in
      * the database to protect them from being nuked when the load is complete.
      *
      * @param gpxName
      * @param gpxTime
      * @return
      */
     public boolean isGpxAlreadyLoaded(String gpxName, String gpxTime) {
         mGpxTime = gpxTime;
         // TODO:countResults is slow; replace with a query, and moveToFirst.
         ISQLiteDatabase sqliteDatabase = sqliteProvider.get();
         boolean gpxAlreadyLoaded = sqliteDatabase.countResults(Database.TBL_GPX,
                 Database.SQL_MATCH_NAME_AND_EXPORTED_LATER, gpxName, gpxTime) > 0;
         if (gpxAlreadyLoaded) {
             sqliteDatabase.execSQL(Database.SQL_CACHES_DONT_DELETE_ME, gpxName);
             sqliteDatabase.execSQL(Database.SQL_GPX_DONT_DELETE_ME, gpxName);
         }
         return gpxAlreadyLoaded;
     }
 
     public void writeGpx(String gpxName) {
         sqliteProvider.get().execSQL(Database.SQL_REPLACE_GPX, gpxName, mGpxTime);
     }
 
 }
