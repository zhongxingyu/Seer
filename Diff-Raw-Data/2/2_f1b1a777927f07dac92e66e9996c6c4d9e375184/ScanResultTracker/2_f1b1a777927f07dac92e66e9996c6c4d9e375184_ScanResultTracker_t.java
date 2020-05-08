 package info.eigenein.openwifi.helpers;
 
 import android.content.Context;
 import android.location.Location;
 import android.net.wifi.ScanResult;
 import android.util.Log;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.stmt.Where;
 import info.eigenein.openwifi.persistency.DatabaseHelper;
 import info.eigenein.openwifi.persistency.entities.StoredLocation;
 import info.eigenein.openwifi.persistency.entities.StoredScanResult;
 
 import java.sql.SQLException;
 import java.util.List;
 
 /**
  * Tracks an access point scan results in the application database.
  */
 public class ScanResultTracker {
     private static final String LOG_TAG = ScanResultTracker.class.getCanonicalName();
 
     /**
      * Adds the scan results to the database.
      */
     public static void add(Context context, Location location, List<ScanResult> scanResults) {
         Log.d(LOG_TAG, "Storing scan results in the database.");
 
         DatabaseHelper databaseHelper = null;
         try {
             databaseHelper = getDatabaseHelper(context);
 
             Dao<StoredScanResult, Integer> scanResultDao = getScanResultDao(databaseHelper);
             Dao<StoredLocation, Long> locationDao = getLocationDao(databaseHelper);
 
             StoredLocation storedLocation = createLocation(locationDao, location);
             for (ScanResult scanResult : scanResults) {
                 createScanResult(scanResultDao, scanResult, storedLocation);
             }
         } catch (SQLException e) {
             Log.e(LOG_TAG, "Error while storing scan results.", e);
             throw new RuntimeException(e);
         } finally {
             if (databaseHelper != null) {
                 Log.v(LOG_TAG, "Done storing scan results.");
                 // Release the helper.
                 Log.v(LOG_TAG, "Release helper.");
                 OpenHelperManager.releaseHelper();
                 //noinspection UnusedAssignment
                 databaseHelper = null;
                 Log.d(LOG_TAG, "Done.");
             } else {
                 Log.w(LOG_TAG, "databaseHelper null");
             }
         }
     }
 
     /**
      * Gets the unique BSSID count (that is an access point count).
      */
     public static long getUniqueBssidCount(Context context) {
         return getScanResultDistinctCount(context, "bssid");
     }
 
     /**
      * Gets the unique SSID count (that is a network count).
      */
     public static long getUniqueSsidCount(Context context) {
         return getScanResultDistinctCount(context, "ssid");
     }
 
     /**
      * Gets the scan result count.
      */
     public static long getScanResultCount(Context context) {
         DatabaseHelper databaseHelper = null;
         try {
             databaseHelper = getDatabaseHelper(context);
             Dao<StoredScanResult, Integer> scanResultDao = getScanResultDao(databaseHelper);
             return scanResultDao.countOf();
         } catch (SQLException e) {
             Log.e(LOG_TAG, "Error while quering scan result count count.", e);
             throw new RuntimeException(e);
         } finally {
             if (databaseHelper != null) {
                 OpenHelperManager.releaseHelper();
                 //noinspection UnusedAssignment
                 databaseHelper = null;
             }
         }
     }
 
     /**
      * Creates a database helper.
      */
     private static DatabaseHelper getDatabaseHelper(Context context) {
         return OpenHelperManager.getHelper(context, DatabaseHelper.class);
     }
 
     /**
      * Gets a stored scan result DAO.
      */
     private static Dao<StoredScanResult, Integer> getScanResultDao(DatabaseHelper databaseHelper)
             throws SQLException {
         return databaseHelper.getDao(StoredScanResult.class);
     }
 
     /**
      * Gets a location DAO.
      */
     private static Dao<StoredLocation, Long> getLocationDao(DatabaseHelper databaseHelper)
             throws SQLException {
         return databaseHelper.getDao(StoredLocation.class);
     }
 
     /**
      * Creates the scan result if it does not exist.
      */
     private static void createScanResult(
             Dao<StoredScanResult, Integer> scanResultDao,
             ScanResult scanResult,
             StoredLocation storedLocation)
             throws SQLException {
         Where<StoredScanResult, Integer> where = scanResultDao.queryBuilder().where();
         @SuppressWarnings("unchecked")
         StoredScanResult storedScanResult = where.and(
                 where.eq("bssid", scanResult.BSSID),
                 where.eq("location_timestamp", storedLocation.getTimestamp()))
                 .queryForFirst();
         if (storedScanResult == null) {
             storedScanResult = new StoredScanResult(
                     scanResult,
                     storedLocation,
                     System.currentTimeMillis());
             scanResultDao.create(storedScanResult);
         } else {
             Log.v(LOG_TAG, "Not creating the scan result - already exists.");
         }
     }
 
     /**
      * Creates the location if it does not exist.
      */
     private static StoredLocation createLocation(
             Dao<StoredLocation, Long> locationDao,
             Location location) throws SQLException {
         StoredLocation storedLocation = locationDao.queryForId(location.getTime());
         if (storedLocation == null) {
             storedLocation = new StoredLocation(location);
             locationDao.create(storedLocation);
         } else {
             Log.v(LOG_TAG, "Not creating the location - already exists.");
         }
         return storedLocation;
     }
 
     /**
      * Gets the count of unique scan results by the specified column.
      */
     private static long getScanResultDistinctCount(Context context, String columnName) {
         DatabaseHelper databaseHelper = null;
         try {
             databaseHelper = getDatabaseHelper(context);
             Dao<StoredScanResult, Integer> scanResultDao = getScanResultDao(databaseHelper);
             return Long.parseLong(scanResultDao.queryRaw(
                     "select count(distinct " +columnName + ") from scan_results;")
                     .getFirstResult()[0]);
         } catch (SQLException e) {
            Log.e(LOG_TAG, "Error while querying unique count.", e);
             throw new RuntimeException(e);
         } finally {
             if (databaseHelper != null) {
                 OpenHelperManager.releaseHelper();
                 //noinspection UnusedAssignment
                 databaseHelper = null;
             }
         }
     }
 }
