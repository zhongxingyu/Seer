 package com.winenotes.db;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 import android.util.Log;
 import android.util.SparseArray;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 public class WineNotesSQLiteOpenHelper extends SQLiteOpenHelper {
 
     private static final String TAG = WineNotesSQLiteOpenHelper.class.getSimpleName();
 
     private static final String DATABASE_NAME = "sqlite3.db";
     private static final int DATABASE_VERSION = 2;
 
     // lists
     private static final String WINES_TABLE_NAME = "main_wine";
     private static final String AROMA_IMPRESSIONS_TABLE_NAME = "main_aromaimpression";
     private static final String TASTE_IMPRESSIONS_TABLE_NAME = "main_tasteimpression";
     private static final String WINETYPES_TABLE_NAME = "main_winetype";
     private static final String REGIONS_TABLE_NAME = "main_region";
     private static final String GRAPES_TABLE_NAME = "main_grape";
     private static final String WINERY_TABLE_NAME = "main_winery";
     private static final String FLAGS_TABLE_NAME = "main_flag";
 
     // relationships
     private static final String WINE_GRAPES_TABLE_NAME = "main_winegrape";
     private static final String WINE_AROMA_IMPRESSIONS_TABLE_NAME = "main_winearomaimpression";
     private static final String WINE_TASTE_IMPRESSIONS_TABLE_NAME = "main_winetasteimpression";
     private static final String WINE_AFTERTASTE_IMPRESSIONS_TABLE_NAME = "main_wineaftertasteimpression";
     private static final String WINE_PHOTOS_TABLE_NAME = "main_winephoto";
 
     private List<String> sqlCreateStatements;
     private SparseArray<List<String>> sqlUpgradeStatements;
 
     public WineNotesSQLiteOpenHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
 
         sqlCreateStatements = getSqlStatements(context, "sql_create.sql");
         sqlUpgradeStatements = new SparseArray<List<String>>();
         sqlUpgradeStatements.put(2, getSqlStatements(context, "sql_upgrade2.sql"));
     }
 
     private List<String> getSqlStatements(Context context, String assetName) {
         List<String> statements;
         try {
             statements = readSqlStatements(context, assetName);
         } catch (IOException e) {
             statements = Collections.emptyList();
             e.printStackTrace();
         }
         return statements;
     }
 
     static List<String> readSqlStatements(Context context, String assetName) throws IOException {
         List<String> statements = new ArrayList<String>();
         InputStream stream = context.getAssets().open(assetName);
         BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
         String line;
         StringBuilder builder = new StringBuilder();
         while ((line = reader.readLine()) != null) {
             builder.append(line);
             if (line.trim().endsWith(";")) {
                 statements.add(builder.toString());
                 builder = new StringBuilder();
             }
         }
         return statements;
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         for (String sql : sqlCreateStatements) {
             db.execSQL(sql);
             // TODO check success
         }
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         for (int i = oldVersion; i < newVersion; ++i) {
             upgradeToVersion(db, i + 1);
         }
     }
 
     private void upgradeToVersion(SQLiteDatabase db, int version) {
         Log.d(TAG, "upgrade to version " + version);
         for (String sql : sqlUpgradeStatements.get(version)) {
             db.execSQL(sql);
         }
     }
 
     /**
      * Returns new wineId on success or else null
      *
      * @return
      */
     public String newWine() {
         ContentValues values = new ContentValues();
         long createdDt = new Date().getTime();
         values.put("created_dt", createdDt);
         values.put("updated_dt", createdDt);
         long ret = getWritableDatabase().insert(WINES_TABLE_NAME, null, values);
         Log.d(TAG, "insert wine ret = " + ret);
         if (ret >= 0) {
             String wineId = String.valueOf(ret);
             return wineId;
         } else {
             return null;
         }
     }
 
     public boolean saveWine(String wineId, String name, String listingText,
                             int wineryId, float price, String currency,
                             int wineTypeId, int year, String regionId,
                             float aromaRating, float tasteRating, float aftertasteRating, float overallRating,
                             int flagId, String memo) {
         ContentValues values = new ContentValues();
         values.put("name", name);
         values.put("listing_text", listingText);
         values.put("winery_id", wineryId);
         values.put("price", price);
         values.put("currency", currency);
         values.put("winetype_id", wineTypeId);
         values.put("year", year);
         values.put("region_id", regionId);
         values.put("aroma_rating", aromaRating);
         values.put("taste_rating", tasteRating);
         values.put("aftertaste_rating", aftertasteRating);
         values.put("overall_rating", overallRating);
         values.put("flag_id", flagId);
         values.put("memo", memo);
 
         long updatedDt = new Date().getTime();
         values.put("updated_dt", updatedDt);
 
         int ret = getWritableDatabase().update(WINES_TABLE_NAME, values,
                 BaseColumns._ID + " = ?", new String[]{wineId});
         Log.d(TAG, String.format("update wine %s -> %s <- %s", wineId, name, ret));
         return ret == 1;
     }
 
     public boolean deleteWine(String wineId) {
         getWritableDatabase().delete(WINE_GRAPES_TABLE_NAME, "wine_id = ?", new String[]{wineId});
         getWritableDatabase().delete(WINE_AROMA_IMPRESSIONS_TABLE_NAME, "wine_id = ?", new String[]{wineId});
         getWritableDatabase().delete(WINE_TASTE_IMPRESSIONS_TABLE_NAME, "wine_id = ?", new String[]{wineId});
         getWritableDatabase().delete(WINE_AFTERTASTE_IMPRESSIONS_TABLE_NAME, "wine_id = ?", new String[]{wineId});
         getWritableDatabase().delete(WINE_PHOTOS_TABLE_NAME, "wine_id = ?", new String[]{wineId});
         getWritableDatabase().delete(WINES_TABLE_NAME, "_id = ?", new String[]{wineId});
         Log.d(TAG, "deleted wine " + wineId);
         // TODO error handling
         return true;
     }
 
 
     public Cursor getWineTypeListCursor() {
         Log.d(TAG, "get all wine types");
         Cursor cursor = getReadableDatabase().query(
                 WINETYPES_TABLE_NAME,
                 new String[]{BaseColumns._ID, "name",},
                 null, null, null, null, "display_order, _id");
         return cursor;
     }
 
     public Cursor getFlagListCursor() {
         Log.d(TAG, "get all flags");
         Cursor cursor = getReadableDatabase().query(
                 FLAGS_TABLE_NAME,
                 new String[]{BaseColumns._ID, "name",},
                 null, null, null, null, "display_order, _id");
         return cursor;
     }
 
     public Cursor getRegionListCursor() {
         Log.d(TAG, "get all regions");
         Cursor cursor = getReadableDatabase().query(
                 REGIONS_TABLE_NAME,
                 new String[]{BaseColumns._ID, "name", "ascii_name",},
                 null, null, null, null, "name");
         return cursor;
     }
 
 
     public String getOrCreateRegion(String name) {
         String regionId = getRegionIdByName(name);
         if (regionId == null) {
             regionId = newRegion(name);
         }
         return regionId;
     }
 
     /**
      * Returns aromaImpressionId or null if aroma impression does not exist.
      *
      * @param name
      * @return
      */
     public String getRegionIdByName(String name) {
         String regionId = null;
         Cursor cursor = getReadableDatabase().query(
                 REGIONS_TABLE_NAME,
                 new String[]{BaseColumns._ID},
                 "name = ? OR ascii_name = ?",
                 new String[]{name, name},
                 null, null, null, "1");
         if (cursor.moveToNext()) {
             regionId = cursor.getString(0);
             Log.d(TAG, String.format("got region: %s -> %s", regionId, name));
         }
         cursor.close();
         return regionId;
     }
 
     /**
      * Returns new regionId on success or else null
      *
      * @return
      */
     private String newRegion(String name) {
         if (name == null || name.trim().length() == 0) {
             return null;
         }
         ContentValues values = new ContentValues();
         values.put("name", name);
         long createdDt = new Date().getTime();
         values.put("created_dt", createdDt);
         values.put("updated_dt", createdDt);
         long ret = getWritableDatabase().insert(REGIONS_TABLE_NAME, null, values);
         Log.d(TAG, String.format("insert region: %s <- %s", name, ret));
         if (ret >= 0) {
             String regionId = String.valueOf(ret);
             return regionId;
         } else {
             return null;
         }
     }
 
 
     public String getOrCreateGrape(String name) {
         String grapeId = getGrapeIdByName(name);
         if (grapeId == null) {
             grapeId = newGrape(name);
         }
         return grapeId;
     }
 
     /**
      * Returns grapeId or null if grape does not exist.
      *
      * @param name
      * @return
      */
     public String getGrapeIdByName(String name) {
         String grapeId = null;
         Cursor cursor = getReadableDatabase().query(
                 GRAPES_TABLE_NAME,
                 new String[]{BaseColumns._ID},
                 "name = ? OR ascii_name = ?",
                 new String[]{name, name},
                 null, null, null, "1");
         if (cursor.moveToNext()) {
             grapeId = cursor.getString(0);
             Log.d(TAG, String.format("got grape: %s -> %s", grapeId, name));
         }
         cursor.close();
         return grapeId;
     }
 
     /**
      * Returns new grapeId on success or else null
      *
      * @return
      */
     private String newGrape(String name) {
         if (name == null || name.trim().length() == 0) {
             return null;
         }
         ContentValues values = new ContentValues();
         values.put("name", name);
         long createdDt = new Date().getTime();
         values.put("created_dt", createdDt);
         values.put("updated_dt", createdDt);
         long ret = getWritableDatabase().insert(GRAPES_TABLE_NAME, null, values);
         Log.d(TAG, String.format("insert grape: %s <- %s", name, ret));
         if (ret >= 0) {
             String grapeId = String.valueOf(ret);
             return grapeId;
         } else {
             return null;
         }
     }
 
     public boolean addWineGrape(String wineId, String grapeId) {
         ContentValues values = new ContentValues();
         values.put("wine_id", wineId);
         values.put("grape_id", grapeId);
         long createdDt = new Date().getTime();
         values.put("created_dt", createdDt);
         values.put("updated_dt", createdDt);
         long ret = getWritableDatabase().insert(WINE_GRAPES_TABLE_NAME, null, values);
         Log.d(TAG, String.format("insert wine grape: %s, %s <- %s",
                 wineId, grapeId, ret));
         return ret >= 0;
     }
 
     public boolean removeWineGrape(String wineId, String grapeId) {
         int ret = getWritableDatabase().delete(WINE_GRAPES_TABLE_NAME,
                 "wine_id = ? AND grape_id = ?",
                 new String[]{wineId, grapeId});
         Log.d(TAG, String.format("delete wine grape: %s, %s <- %s",
                 wineId, grapeId, ret));
         return ret > 0;
     }
 
     public Cursor getGrapeListCursor() {
         Log.d(TAG, "get all grapes");
         Cursor cursor = getReadableDatabase().query(
                 GRAPES_TABLE_NAME,
                 new String[]{BaseColumns._ID, "name", "ascii_name",},
                 null, null, null, null, "name");
         return cursor;
     }
 
     public Cursor getWineGrapesCursor(String wineId) {
         Log.d(TAG, "get wine grapes " + wineId);
         Cursor cursor = getReadableDatabase().rawQuery(
                 String.format(
                         "SELECT i.name FROM %s ri JOIN %s i ON ri.grape_id = i.%s WHERE ri.wine_id = ? ORDER BY i.name",
                         WINE_GRAPES_TABLE_NAME, GRAPES_TABLE_NAME, BaseColumns._ID
                 ),
                 new String[]{wineId}
         );
         return cursor;
     }
 
 
     public String getOrCreateAromaImpression(String name) {
         String aromaImpressionId = getAromaImpressionIdByName(name);
         if (aromaImpressionId == null) {
             aromaImpressionId = newAromaImpression(name);
         }
         return aromaImpressionId;
     }
 
     /**
      * Returns aromaImpressionId or null if aroma impression does not exist.
      *
      * @param name
      * @return
      */
     public String getAromaImpressionIdByName(String name) {
         String aromaImpressionId = null;
         Cursor cursor = getReadableDatabase().query(
                 AROMA_IMPRESSIONS_TABLE_NAME,
                 new String[]{BaseColumns._ID},
                 "name = ?",
                 new String[]{name},
                 null, null, null, "1");
         if (cursor.moveToNext()) {
             aromaImpressionId = cursor.getString(0);
             Log.d(TAG, String.format("got aroma impression: %s -> %s", aromaImpressionId, name));
         }
         cursor.close();
         return aromaImpressionId;
     }
 
     /**
      * Returns new aromaImpressionId on success or else null
      *
      * @return
      */
     private String newAromaImpression(String name) {
         if (name == null || name.trim().length() == 0) {
             return null;
         }
         ContentValues values = new ContentValues();
         values.put("name", name);
         long createdDt = new Date().getTime();
         values.put("created_dt", createdDt);
         values.put("updated_dt", createdDt);
         long ret = getWritableDatabase().insert(AROMA_IMPRESSIONS_TABLE_NAME, null, values);
         Log.d(TAG, String.format("insert aroma impression: %s <- %s", name, ret));
         if (ret >= 0) {
             String aromaImpressionId = String.valueOf(ret);
             return aromaImpressionId;
         } else {
             return null;
         }
     }
 
     public boolean addWineAromaImpression(String wineId, String aromaImpressionId) {
         ContentValues values = new ContentValues();
         values.put("wine_id", wineId);
         values.put("aroma_impression_id", aromaImpressionId);
         long createdDt = new Date().getTime();
         values.put("created_dt", createdDt);
         values.put("updated_dt", createdDt);
         long ret = getWritableDatabase().insert(WINE_AROMA_IMPRESSIONS_TABLE_NAME, null, values);
         Log.d(TAG, String.format("insert wine aroma impression: %s, %s <- %s",
                 wineId, aromaImpressionId, ret));
         return ret >= 0;
     }
 
     public boolean removeWineAromaImpression(String wineId, String aromaImpressionId) {
         int ret = getWritableDatabase().delete(WINE_AROMA_IMPRESSIONS_TABLE_NAME,
                 "wine_id = ? AND aroma_impression_id = ?",
                 new String[]{wineId, aromaImpressionId});
         Log.d(TAG, String.format("delete wine aroma impression: %s, %s <- %s",
                 wineId, aromaImpressionId, ret));
         return ret > 0;
     }
 
     public Cursor getAromaImpressionListCursor() {
         Log.d(TAG, "get all aroma impressions");
         Cursor cursor = getReadableDatabase().query(
                 AROMA_IMPRESSIONS_TABLE_NAME,
                 new String[]{BaseColumns._ID, "name",},
                 null, null, null, null, "name");
         return cursor;
     }
 
     public Cursor getWineAromaImpressionsCursor(String wineId) {
         Log.d(TAG, "get wine aroma impressions " + wineId);
         Cursor cursor = getReadableDatabase().rawQuery(
                 String.format(
                         "SELECT i.name FROM %s ri JOIN %s i ON ri.aroma_impression_id = i.%s WHERE ri.wine_id = ? ORDER BY i.name",
                         WINE_AROMA_IMPRESSIONS_TABLE_NAME, AROMA_IMPRESSIONS_TABLE_NAME, BaseColumns._ID
                 ),
                 new String[]{wineId}
         );
         return cursor;
     }
 
 
     public String getOrCreateTasteImpression(String name) {
         String tasteImpressionId = getTasteImpressionIdByName(name);
         if (tasteImpressionId == null) {
             tasteImpressionId = newTasteImpression(name);
         }
         return tasteImpressionId;
     }
 
     /**
      * Returns tasteImpressionId or null if taste impression does not exist.
      *
      * @param name
      * @return
      */
     public String getTasteImpressionIdByName(String name) {
         String tasteImpressionId = null;
         Cursor cursor = getReadableDatabase().query(
                 TASTE_IMPRESSIONS_TABLE_NAME,
                 new String[]{BaseColumns._ID},
                 "name = ?",
                 new String[]{name},
                 null, null, null, "1");
         if (cursor.moveToNext()) {
             tasteImpressionId = cursor.getString(0);
             Log.d(TAG, String.format("got taste impression: %s -> %s", tasteImpressionId, name));
         }
         cursor.close();
         return tasteImpressionId;
     }
 
     /**
      * Returns new tasteImpressionId on success or else null
      *
      * @return
      */
     private String newTasteImpression(String name) {
         if (name == null || name.trim().length() == 0) {
             return null;
         }
         ContentValues values = new ContentValues();
         values.put("name", name);
         long createdDt = new Date().getTime();
         values.put("created_dt", createdDt);
         values.put("updated_dt", createdDt);
         long ret = getWritableDatabase().insert(TASTE_IMPRESSIONS_TABLE_NAME, null, values);
         Log.d(TAG, String.format("insert taste impression: %s <- %s", name, ret));
         if (ret >= 0) {
             String tasteImpressionId = String.valueOf(ret);
             return tasteImpressionId;
         } else {
             return null;
         }
     }
 
     public boolean addWineTasteImpression(String wineId, String tasteImpressionId) {
         ContentValues values = new ContentValues();
         values.put("wine_id", wineId);
         values.put("taste_impression_id", tasteImpressionId);
         long createdDt = new Date().getTime();
         values.put("created_dt", createdDt);
         values.put("updated_dt", createdDt);
         long ret = getWritableDatabase().insert(WINE_TASTE_IMPRESSIONS_TABLE_NAME, null, values);
         Log.d(TAG, String.format("insert taste impression: %s, %s <- %s",
                 wineId, tasteImpressionId, ret));
         return ret >= 0;
     }
 
     public boolean removeWineTasteImpression(String wineId, String tasteImpressionId) {
         int ret = getWritableDatabase().delete(WINE_TASTE_IMPRESSIONS_TABLE_NAME,
                 "wine_id = ? AND taste_impression_id = ?",
                 new String[]{wineId, tasteImpressionId});
         Log.d(TAG, String.format("delete wine taste impression: %s, %s <- %s",
                 wineId, tasteImpressionId, ret));
         return ret > 0;
     }
 
     public Cursor getTasteImpressionListCursor() {
         Log.d(TAG, "get all taste impressions");
         Cursor cursor = getReadableDatabase().query(
                 TASTE_IMPRESSIONS_TABLE_NAME,
                 new String[]{BaseColumns._ID, "name",},
                 null, null, null, null, "name");
         return cursor;
     }
 
     public Cursor getWineTasteImpressionsCursor(String wineId) {
         Log.d(TAG, "get wine taste impressions " + wineId);
         Cursor cursor = getReadableDatabase().rawQuery(
                 String.format(
                         "SELECT i.name FROM %s ri JOIN %s i ON ri.taste_impression_id = i.%s WHERE ri.wine_id = ? ORDER BY i.name",
                         WINE_TASTE_IMPRESSIONS_TABLE_NAME, TASTE_IMPRESSIONS_TABLE_NAME, BaseColumns._ID
                 ),
                 new String[]{wineId}
         );
         return cursor;
     }
 
 
     public boolean addWineAftertasteImpression(String wineId, String aftertasteImpressionId) {
         ContentValues values = new ContentValues();
         values.put("wine_id", wineId);
         values.put("aftertaste_impression_id", aftertasteImpressionId);
         long createdDt = new Date().getTime();
         values.put("created_dt", createdDt);
         values.put("updated_dt", createdDt);
         long ret = getWritableDatabase().insert(WINE_AFTERTASTE_IMPRESSIONS_TABLE_NAME, null, values);
         Log.d(TAG, String.format("insert aftertaste impression: %s, %s <- %s",
                 wineId, aftertasteImpressionId, ret));
         return ret >= 0;
     }
 
     public boolean removeWineAftertasteImpression(String wineId, String aftertasteImpressionId) {
         int ret = getWritableDatabase().delete(WINE_AFTERTASTE_IMPRESSIONS_TABLE_NAME,
                 "wine_id = ? AND aftertaste_impression_id = ?",
                 new String[]{wineId, aftertasteImpressionId});
         Log.d(TAG, String.format("delete wine aftertaste impression: %s, %s <- %s",
                 wineId, aftertasteImpressionId, ret));
         return ret > 0;
     }
 
     public Cursor getWineAftertasteImpressionsCursor(String wineId) {
         Log.d(TAG, "get wine aftertaste impressions " + wineId);
         Cursor cursor = getReadableDatabase().rawQuery(
                 String.format(
                         "SELECT i.name FROM %s ri JOIN %s i ON ri.aftertaste_impression_id = i.%s WHERE ri.wine_id = ? ORDER BY i.name",
                         WINE_AFTERTASTE_IMPRESSIONS_TABLE_NAME, TASTE_IMPRESSIONS_TABLE_NAME, BaseColumns._ID
                 ),
                 new String[]{wineId}
         );
         return cursor;
     }
 
 
     public boolean addWinePhoto(String wineId, String filename) {
         ContentValues values = new ContentValues();
         values.put("wine_id", wineId);
         values.put("filename", filename);
         long createdDt = new Date().getTime();
         values.put("created_dt", createdDt);
         values.put("updated_dt", createdDt);
         long ret = getWritableDatabase().insert(WINE_PHOTOS_TABLE_NAME, null, values);
         Log.d(TAG, String.format("insert wine photo: %s, %s <- %s",
                 wineId, filename, ret));
         return ret >= 0;
     }
 
     public boolean removeWinePhoto(String wineId, String filename) {
         int ret = getWritableDatabase().delete(WINE_PHOTOS_TABLE_NAME,
                 "wine_id = ? AND filename = ?",
                 new String[]{wineId, filename});
         Log.d(TAG, String.format("delete wine photo: %s, %s <- %s",
                 wineId, filename, ret));
         return ret > 0;
     }
 
     public Cursor getWineListCursor() {
         Log.d(TAG, "get all wines");
         Cursor cursor = getReadableDatabase().rawQuery(String.format(
                 "SELECT w._id, ifnull(nullif(w.name, ''), '(noname)') name, " +
                         "listing_text summary, overall_rating rating, " +
                         "winetype_id " +
                         "FROM %s w " +
                         "LEFT JOIN %s f ON w.flag_id = f._id " +
                         "ORDER BY ifnull(f.display_order, 99), overall_rating DESC, w.updated_dt DESC",
                 WINES_TABLE_NAME,
                 FLAGS_TABLE_NAME
         ),
                 null);
         return cursor;
     }
 
     public Cursor getWineDetailsCursor(String wineId) {
         Log.d(TAG, "get wine " + wineId);
         String sql = String.format(
                 "SELECT w.name, listing_text, winery_id, f.name winery_name, " +
                         "price, currency, " +
                         "winetype_id, t.name winetype, year, " +
                         "region_id, r.name region, " +
                         "aroma_rating, taste_rating, aftertaste_rating, overall_rating, " +
                         "flag_id, b.name flag, memo " +
                         "FROM %s w " +
                         "LEFT JOIN %s t ON w.winetype_id = t._id " +
                         "LEFT JOIN %s r ON w.region_id = r._id " +
                         "LEFT JOIN %s f ON w.winery_id = f._id " +
                         "LEFT JOIN %s b ON w.flag_id = b._id " +
                         "WHERE w._id = ?",
                 WINES_TABLE_NAME,
                 WINETYPES_TABLE_NAME,
                 REGIONS_TABLE_NAME,
                 WINERY_TABLE_NAME,
                 FLAGS_TABLE_NAME
         );
         Log.d(TAG, sql);
         Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{wineId});
         return cursor;
     }
 
     public Cursor getWinePhotosCursor(String wineId) {
         Log.d(TAG, "get wine photos " + wineId);
         Cursor cursor = getReadableDatabase().rawQuery(
                 String.format(
                         "SELECT filename FROM %s WHERE wine_id = ?",
                         WINE_PHOTOS_TABLE_NAME
                 ),
                 new String[]{wineId}
         );
         return cursor;
     }
 
     public boolean isExistingWineId(String wineId) {
         Log.d(TAG, "isExistingWineId " + wineId);
         boolean exists = false;
         Cursor cursor = getReadableDatabase().query(
                 WINES_TABLE_NAME, new String[]{"name",},
                 BaseColumns._ID + " = ?", new String[]{wineId},
                 null, null, null);
         if (cursor.moveToNext()) {
             exists = true;
         }
         cursor.close();
         Log.d(TAG, "isExistingWineId " + wineId + " -> " + exists);
         return exists;
     }
 
 }
