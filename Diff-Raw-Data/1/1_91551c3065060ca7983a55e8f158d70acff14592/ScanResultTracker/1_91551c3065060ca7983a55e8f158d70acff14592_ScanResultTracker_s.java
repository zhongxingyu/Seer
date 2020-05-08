 package info.eigenein.openwifi.helpers;
 
 import android.content.Context;
 import android.location.Location;
 import android.net.wifi.ScanResult;
 import android.util.Log;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.RawRowMapper;
 import com.j256.ormlite.stmt.DeleteBuilder;
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
 
     // TODO: make this configurable.
     private static final int MAX_SCAN_RESULTS_FOR_BSSID = 4;
 
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
                 purgeOldScanResults(scanResultDao, scanResult.BSSID);
             }
         } catch (SQLException e) {
             Log.e(LOG_TAG, "Error while storing scan results.", e);
             throw new RuntimeException(e);
         } finally {
             if (databaseHelper != null) {
                 Log.d(LOG_TAG, "Done storing scan results.");
                 // Release the helper.
                 Log.d(LOG_TAG, "Release helper.");
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
         return getScanResultDistinctCount(context, StoredScanResult.BSSID);
     }
 
     /**
      * Gets the unique SSID count (that is a network count).
      */
     public static long getUniqueSsidCount(Context context) {
         return getScanResultDistinctCount(context, StoredScanResult.SSID);
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
             Log.e(LOG_TAG, "Error while querying scan result count count.", e);
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
      * Gets the stored scan results in the specified area.
      */
     public static List<StoredScanResult> getScanResults(
             Context context,
             double minLatitude,
             double minLongitude,
             double maxLatitude,
             double maxLongitude) {
         Log.d(LOG_TAG, String.format("getScanResults %s %s %s %s",
                 minLatitude,
                 minLongitude,
                 maxLatitude,
                 maxLongitude));
 
         DatabaseHelper databaseHelper = null;
         try {
             databaseHelper = getDatabaseHelper(context);
 
             Dao<StoredScanResult, Integer> scanResultDao = getScanResultDao(databaseHelper);
             Dao<StoredLocation, Long> locationDao = getLocationDao(databaseHelper);
 
             final String query = "select sr1.bssid, sr1.ssid, loc.accuracy, loc.latitude, loc.longitude \n" +
                     "from scan_results sr1\n" +
                     "join locations loc\n" +
                     "on loc.timestamp = sr1.location_timestamp\n" +
                     "where loc.latitude >= ? and loc.latitude <= ?\n" +
                     "and loc.longitude >= ? and loc.longitude <= ?\n" +
                     "order by sr1.bssid, sr1.location_timestamp desc;";
             List<StoredScanResult> scanResults = scanResultDao.queryRaw(
                     query,
                     GetScanResultsRawRowMapper.getInstance(),
                     Double.toString(minLatitude),
                     Double.toString(maxLatitude),
                     Double.toString(minLongitude),
                     Double.toString(maxLongitude))
                     .getResults();
             return scanResults;
         } catch (SQLException e) {
             Log.e(LOG_TAG, "Error while querying scan results.", e);
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
                     storedLocation);
             scanResultDao.create(storedScanResult);
         } else {
             Log.d(LOG_TAG, "Not creating the scan result - already exists.");
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
             Log.d(LOG_TAG, "Not creating the location - already exists.");
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
 
     /**
      * Deletes old scan results for the specified BSSID.
      */
     private static void purgeOldScanResults(Dao<StoredScanResult, Integer> dao, String bssid)
             throws SQLException {
         Log.d(LOG_TAG + ".purgeOldScanResults", bssid);
         @SuppressWarnings("deprecation")
         List<StoredScanResult> scanResults = dao.queryBuilder()
                 .orderBy(StoredScanResult.LOCATION_TIMESTAMP, false)
                 .limit(MAX_SCAN_RESULTS_FOR_BSSID)
                 .where().eq(StoredScanResult.BSSID, bssid)
                 .query();
 
         if (scanResults.size() != MAX_SCAN_RESULTS_FOR_BSSID) {
             Log.d(LOG_TAG + ".purgeOldScanResults", scanResults.size() + " scan results for " + bssid);
             return;
         }
 
         // Obtain the timestamp of the oldest result.
         long lastLocationTimestamp = scanResults.get(MAX_SCAN_RESULTS_FOR_BSSID - 1)
                 .getLocation()
                 .getTimestamp();
         Log.d(LOG_TAG + ".purgeOldScanResults", "lastLocationTimestamp " + lastLocationTimestamp);
         // Delete all results that are even older.
         DeleteBuilder<StoredScanResult, Integer> deleteBuilder = dao.deleteBuilder();
         deleteBuilder.where().lt(StoredScanResult.LOCATION_TIMESTAMP, lastLocationTimestamp);
         deleteBuilder.delete();
 
         Log.d(LOG_TAG + ".purgeOldScanResults", "deleted");
     }
 
     private static class GetScanResultsRawRowMapper implements RawRowMapper<StoredScanResult> {
         private static final GetScanResultsRawRowMapper instance = new GetScanResultsRawRowMapper();
 
         public static GetScanResultsRawRowMapper getInstance() {
             return instance;
         }
 
         @Override
         public StoredScanResult mapRow(String[] columnNames, String[] resultColumns)
                 throws SQLException {
             StoredLocation storedLocation = new StoredLocation();
             StoredScanResult storedScanResult = new StoredScanResult();
 
             storedScanResult.setLocation(storedLocation);
 
             storedScanResult.setBssid(resultColumns[0]);
             storedScanResult.setSsid(resultColumns[1]);
             storedLocation.setAccuracy(Float.parseFloat(resultColumns[2]));
             storedLocation.setLatitude(Double.parseDouble(resultColumns[3]));
             storedLocation.setLongitude(Double.parseDouble(resultColumns[4]));
 
             return storedScanResult;
         }
     }
 }
