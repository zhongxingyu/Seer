 package com.google.code.geobeagle.database;
 
 import com.google.code.geobeagle.CacheFilter;
 import com.google.code.geobeagle.GeocacheList;
 import com.google.code.geobeagle.Tags;
 
 import android.util.Log;
 
 /** Uses a DB to fetch the caches within a defined region, or all caches if no 
  * bounds were specified */
 public class CachesProviderDb implements ICachesProviderArea {
 
     private DbFrontend mDbFrontend;
     private double mLatLow;
     private double mLonLow;
     private double mLatHigh;
     private double mLonHigh;
     /** The complete SQL query for the current coordinates and settings,
      *  except from 'SELECT x'. If null, mSql needs to be re-calculated. */
     private String mSql = null;
     private GeocacheList mCaches;
     private boolean mHasChanged = true;
     private boolean mHasLimits = false;
     /** The 'where' part of the SQL clause that comes from the active CacheFilter */ 
     private String mFilter = "";
     /** The tag used for filtering. Tags.NULL means any tag allowed. */
     private int mRequiredTag = Tags.NULL;
     /** If greater than zero, this is the max number that mCaches 
      * was allowed to contain when loaded. (This limit can change on subsequent loads) */
     private int mCachesCappedToCount = 0;
 
     public CachesProviderDb(DbFrontend dbFrontend) {
         mDbFrontend = dbFrontend;
     }
     
     /** Returns a complete SQL query except from 'SELECT x' */
     private String getSql() {
         if (mSql == null) {
             String where = "";
             if (mHasLimits) {
                 where = "Latitude >= " + mLatLow + " AND Latitude < " + mLatHigh + 
                 " AND Longitude >= " + mLonLow + " AND Longitude < " + mLonHigh;
                 if (!mFilter.equals(""))
                     where += " AND " + mFilter;
             } else {
                 where = mFilter;
             }
 
             if (mRequiredTag == Tags.NULL) {
                 if (where.equals(""))
                     mSql = "FROM CACHES";  //Return all caches
                 else
                     mSql = "FROM CACHES WHERE " + where;
             } else {
                 if (where.equals(""))
                     mSql = "FROM CACHES, CACHETAGS WHERE TagId=" + mRequiredTag
                       + " AND Id=CacheId";
                 else
                     mSql = "FROM CACHES, CACHETAGS WHERE TagId=" + mRequiredTag
                       + " AND Id=CacheId AND " + where;
             }
            Log.d("GeoBeagle", "CachesProviderDb created sql " + mSql);
         }
         return mSql;
     }
     
     @Override
     public GeocacheList getCaches() {
         if (mCaches == null || mCachesCappedToCount > 0) {
             mCaches = mDbFrontend.loadCachesRaw("SELECT Id " + getSql());
         }
         return mCaches;
     }
 
     @Override
     public GeocacheList getCaches(int maxResults) {
         if (mCaches == null || (mCachesCappedToCount > 0 && 
                                 mCachesCappedToCount < maxResults)) {
             mCaches = mDbFrontend.loadCachesRaw("SELECT Id " + getSql() + " LIMIT 0, " + maxResults);
             if (mCaches.size() == maxResults)
                 mCachesCappedToCount = maxResults;
             else
                 //The cap didn't limit the search result
                 mCachesCappedToCount = 0;
         }
         return mCaches;
     }
     
     @Override
     public int getCount() {
         if (mCaches == null || mCachesCappedToCount > 0) {
             return mDbFrontend.countRaw("SELECT COUNT(*) " + getSql());
         }
         return mCaches.size();
     }
 
     @Override
     public void setBounds(double latLow, double lonLow, double latHigh, double lonHigh) {
         //TODO: OK to compare doubles?
         if (latLow == mLatLow && latHigh == mLatHigh 
                 && lonLow == mLonLow && lonHigh == mLonHigh) {
             return;
         }
         mLatLow = latLow;
         mLatHigh = latHigh;
         mLonLow = lonLow;
         mLonHigh = lonHigh;
         mCaches = null;  //Flush old caches
         mSql = null;
         mHasChanged = true;
         mHasLimits = true;
     }
 
     @Override
     public boolean hasChanged() {
         return mHasChanged;
     }
 
     @Override
     public void resetChanged() {
         mHasChanged = false;
     }
 
     /** Tells this class that the contents in the database have changed. 
      * The cached list isn't reliable any more. */
     public void notifyOfDbChange() {
         mCaches = null;
         mHasChanged = true;
     }
     
     public void setFilter(CacheFilter cacheFilter) {
         String newFilter = cacheFilter.getSqlWhereClause();
         int newTag = cacheFilter.getRequiredTag();
         
         if (newFilter.equals(mFilter) && newTag == mRequiredTag)
             return;
         
         mHasChanged = true;
         mFilter = newFilter;
         mRequiredTag = newTag;
         mSql = null;   //Flush
         mCaches = null;  //Flush old caches
     }
     
     public int getTotalCount() {
         return mDbFrontend.count(mFilter);
     }
 }
