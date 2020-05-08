 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
 package org.barbon.mangaget.data;
 
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 
 import android.database.Cursor;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DB {
     public static final int DOWNLOAD_STOPPED = 0;
     public static final int DOWNLOAD_REQUESTED = 1;
     public static final int DOWNLOAD_STARTED = 2;
     public static final int DOWNLOAD_COMPLETE = 3;
 
     public static final int SUBSCRIPTION_TEMPORARY = 0;
     public static final int SUBSCRIPTION_SAVED = 1;
     public static final int SUBSCRIPTION_FOLLOWING = 2;
 
     public static final String ID = "_id";
     public static final String DOWNLOAD_STATUS = "download_status";
 
     public static final String MANGA_TITLE = "title";
     public static final String MANGA_PATTERN = "pattern";
     public static final String MANGA_URL = "url";
 
     public static final String CHAPTER_MANGA_ID = "manga_id";
     public static final String CHAPTER_NUMBER = "number";
     public static final String CHAPTER_TITLE = "title";
     public static final String CHAPTER_URL = "url";
 
     public static final String PAGE_URL = "url";
     public static final String PAGE_IMAGE_URL = "image_url";
 
     private static final int VERSION = 4;
     private static final String DB_NAME = "manga";
     private static DB theInstance;
 
     private DBOpenHelper openHelper;
     private SQLiteDatabase database;
 
     private static final String CREATE_MANGA_TABLE =
         "CREATE TABLE manga (" +
         "    id INTEGER PRIMARY KEY," +
         "    title TEXT NOT NULL," +
         "    pattern TEXT NOT NULL," +
         "    url TEXT NOT NULL," +
         "    subscription_status INTEGER NOT NULL" +
         ")";
 
     private static final String CREATE_MANGA_METADATA_TABLE =
         "CREATE TABLE manga_metadata (" +
         "    id INTEGER PRIMARY KEY," +
         "    manga_id INTEGER NOT NULL," +
         "    key TEXT NOT NULL," +
         "    value TEXT NOT NULL," +
         "    FOREIGN KEY (manga_id) REFERENCES manga(id) " +
         "        ON DELETE CASCADE" +
         ")";
 
     private static final String CREATE_MANGA_METADATA_TABLE_FK_INDEX =
         "CREATE INDEX manga_metadata_manga_fk ON manga_metadata(manga_id)";
 
     private static final String CREATE_CHAPTERS_TABLE =
         "CREATE TABLE chapters (" +
         "    id INTEGER PRIMARY KEY," +
         "    manga_id INTEGER NOT NULL," +
         "    number INTEGER NOT NULL," +
         "    pages INTEGER NOT NULL," +
         "    title TEXT NOT NULL," +
         "    url TEXT NOT NULL," +
         "    download_status INTEGER NOT NULL," +
         "    FOREIGN KEY (manga_id) REFERENCES manga(id) " +
         "        ON DELETE CASCADE" +
         ")";
 
     private static final String CREATE_CHAPTERS_TABLE_FK_INDEX =
         "CREATE INDEX chapters_manga_fk ON chapters(manga_id)";
 
     private static final String CREATE_PAGES_TABLE =
         "CREATE TABLE pages (" +
         "    id INTEGER PRIMARY KEY," +
         "    chapter_id INTEGER NOT NULL," +
         "    number INTEGER NOT NULL," +
         "    url TEXT NOT NULL," +
         "    image_url TEXT," +
         "    download_status INTEGER NOT NULL," +
         "    FOREIGN KEY (chapter_id) REFERENCES chapters(id)" +
         "        ON DELETE CASCADE" +
         ")";
 
     private static final String CREATE_PAGES_TABLE_FK_INDEX =
         "CREATE INDEX pages_chapters_fk ON pages(chapter_id)";
 
     public static void setInstance(DB instance) {
         theInstance = instance;
     }
 
     public static DB getNewInstance(Context context, String name) {
         return new DB(context, name);
     }
 
     public static DB getInstance(Context context) {
         if (theInstance != null)
             return theInstance;
 
         return theInstance = new DB(context, DB_NAME);
     }
 
     private DB(Context context, String name) {
         openHelper = new DBOpenHelper(context, name);
     }
 
     private synchronized SQLiteDatabase getDatabase() {
         if (database != null)
             return database;
 
         return database = openHelper.getWritableDatabase();
     }
 
     public Cursor getSubscribedMangaList() {
         SQLiteDatabase db = getDatabase();
 
         return db.rawQuery(
             "SELECT id AS _id, title, url" +
             "    FROM manga" +
             "    WHERE subscription_status > 0" +
             "    ORDER BY title",
             null);
     }
 
     public Cursor getChapterList(long mangaId) {
         SQLiteDatabase db = getDatabase();
 
         return db.rawQuery(
             "SELECT id AS _id, number, title, download_status" +
             "    FROM chapters" +
             "    WHERE manga_id = ?" +
             "    ORDER BY number",
             new String[] { Long.toString(mangaId) });
     }
 
     public Cursor getAllChapterList() {
         SQLiteDatabase db = getDatabase();
 
         return db.rawQuery(
             "SELECT id AS _id, number, title, download_status, manga_id" +
             "    FROM chapters" +
             "    ORDER BY number",
             null);
     }
 
     public Cursor getPages(long chapterId) {
         SQLiteDatabase db = getDatabase();
 
         return db.rawQuery(
             "SELECT id AS _id, number, url, image_url, download_status" +
             "    FROM pages" +
             "    WHERE chapter_id = ?" +
             "    ORDER BY number",
             new String[] { Long.toString(chapterId) });
     }
 
     public int getPageCount(long chapterId) {
         Cursor pages = getPages(chapterId);
         int count = pages.getCount();
 
         pages.close();
 
         return count;
     }
 
     public long findManga(String mangaUrl) {
         SQLiteDatabase db = getDatabase();
         Cursor cursor = db.rawQuery(
             "SELECT id AS _id" +
             "    FROM manga" +
             "    WHERE url = ?",
             new String[] { mangaUrl });
 
         long mangaId = -1;
 
         if (cursor.moveToNext())
             mangaId = cursor.getLong(0);
 
         cursor.close();
 
         return mangaId;
     }
 
     public ContentValues getManga(long mangaId) {
         SQLiteDatabase db = getDatabase();
         Cursor cursor = db.rawQuery(
             "SELECT id AS _id, title, pattern, url" +
             "    FROM manga" +
             "    WHERE id = ?",
             new String[] { Long.toString(mangaId)});
 
         ContentValues values = null;
 
         if (cursor.moveToNext()) {
             values = new ContentValues();
 
             values.put("_id", mangaId);
             values.put("title", cursor.getString(1));
             values.put("pattern", cursor.getString(2));
             values.put("url", cursor.getString(3));
         }
 
         cursor.close();
 
         return values;
     }
 
     public ContentValues getMangaMetadata(long mangaId) {
         SQLiteDatabase db = getDatabase();
         Cursor cursor = db.rawQuery(
             "SELECT id AS _id, key, value" +
             "    FROM manga_metadata" +
             "    WHERE manga_id = ?",
             new String[] { Long.toString(mangaId)});
 
         ContentValues values = new ContentValues();
 
         while (cursor.moveToNext())
             values.put(cursor.getString(1), cursor.getString(2));
 
         cursor.close();
 
         return values;
     }
 
     public long getChapterId(long mangaId, int index) {
         SQLiteDatabase db = getDatabase();
         Cursor cursor = db.rawQuery(
             "SELECT id AS _id" +
             "    FROM chapters" +
             "    WHERE manga_id = ? AND number = ?",
             new String[] { Long.toString(mangaId), Long.toString(index)});
 
         long id = -1;
 
         if (cursor.moveToNext())
             id = cursor.getLong(0);
 
         cursor.close();
 
         return id;
     }
 
     public ContentValues getChapter(long chapterId) {
         SQLiteDatabase db = getDatabase();
         Cursor cursor = db.rawQuery(
             "SELECT id AS _id, manga_id, title, number, url, download_status" +
             "    FROM chapters" +
             "    WHERE id = ?",
             new String[] { Long.toString(chapterId)});
 
         ContentValues values = null;
 
         if (cursor.moveToNext()) {
             values = new ContentValues();
 
             values.put("_id", chapterId);
             values.put("manga_id", cursor.getInt(1));
             values.put("title", cursor.getString(2));
             values.put("number", cursor.getString(3));
             values.put("url", cursor.getString(4));
             values.put("download_status", cursor.getString(5));
         }
 
         cursor.close();
 
         return values;
     }
 
     public boolean updateChapterStatus(long chapterId, int downloadStatus) {
         SQLiteDatabase db = getDatabase();
         ContentValues values = new ContentValues();
 
         values.put("download_status", downloadStatus);
 
         return db.update("chapters", values, "id = ?",
                          new String[] { Long.toString(chapterId) }) == 1;
     }
 
     public boolean updatePageStatus(long pageId, int downloadStatus) {
         SQLiteDatabase db = getDatabase();
         ContentValues values = new ContentValues();
 
         values.put("download_status", downloadStatus);
 
         return db.update("pages", values, "id = ?",
                          new String[] { Long.toString(pageId) }) == 1;
     }
 
     public boolean updatePageImage(long pageId, String imageUrl) {
         SQLiteDatabase db = getDatabase();
         ContentValues values = new ContentValues();
 
         values.put("image_url", imageUrl);
 
         return db.update("pages", values, "id = ?",
                          new String[] { Long.toString(pageId) }) == 1;
     }
 
     public long insertManga(String title, String pattern, String url,
                             int subscriptionStatus) {
         SQLiteDatabase db = getDatabase();
         ContentValues values = new ContentValues();
 
         values.put("title", title);
         values.put("pattern", pattern);
         values.put("url", url);
         values.put("subscription_status", subscriptionStatus);
 
         return db.insertOrThrow("manga", null, values);
     }
 
     public boolean updateManga(long mangaId, String title, String pattern,
                                String url, int subscriptionStatus) {
         SQLiteDatabase db = getDatabase();
         ContentValues values = new ContentValues();
 
         values.put("title", title);
         values.put("pattern", pattern);
         values.put("url", url);
         values.put("subscription_status", subscriptionStatus);
 
         return db.update("manga", values, "id = ?",
                          new String[] { Long.toString(mangaId) }) == 1;
     }
 
     public long insertOrUpdateManga(String title, String pattern, String url,
                                     int subscriptionStatus) {
         long mangaId = findManga(url);
 
         if (mangaId == -1)
             return insertManga(title, pattern, url, subscriptionStatus);
 
         updateManga(mangaId, title, pattern, url, subscriptionStatus);
 
         return mangaId;
     }
 
     public long insertChapter(long mangaId, int number, int pages,
                               String title, String url) {
         SQLiteDatabase db = getDatabase();
         ContentValues values = new ContentValues();
 
         values.put("manga_id", mangaId);
         values.put("number", number);
         values.put("pages", pages);
         values.put("title", title);
         values.put("url", url);
         values.put("download_status", DB.DOWNLOAD_STOPPED);
 
         return db.insertOrThrow("chapters", null, values);
     }
 
     public long insertOrUpdateChapter(long mangaId, int number, int pages,
                                       String title, String url) {
         long chapterId = getChapterId(mangaId, number);
 
         if (chapterId == -1)
             return insertChapter(mangaId, number, pages, title, url);
 
         SQLiteDatabase db = getDatabase();
         ContentValues values = new ContentValues();
 
         values.put("manga_id", mangaId);
         values.put("number", number);
         values.put("pages", pages);
         values.put("title", title);
         values.put("url", url);
 
         db.update("chapters", values, "id = ?",
                   new String[] { Long.toString(chapterId) });
 
         return chapterId;
     }
 
     public long insertPage(long chapterId, int number, String url,
                            String imageUrl, int downloadStatus) {
         SQLiteDatabase db = getDatabase();
         ContentValues values = new ContentValues();
 
         values.put("chapter_id", chapterId);
         values.put("number", number);
         values.put("url", url);
         values.put("image_url", imageUrl);
         values.put("download_status", downloadStatus);
 
         return db.insertOrThrow("pages", null, values);
     }
 
     public void insertOrUpdateMetadata(long mangaId, String summary,
                                        List<String> genres) {
         SQLiteDatabase db = getDatabase();
 
         db.delete("manga_metadata", "manga_id = ?",
                   new String[] { Long.toString(mangaId) });
 
         ContentValues values = new ContentValues();
 
         values.put("manga_id", mangaId);
 
         if (summary != null) {
             values.put("key", "summary");
             values.put("value", summary);
 
             db.insertOrThrow("manga_metadata", null, values);
         }
 
         if (genres != null && genres.size() > 0) {
             StringBuffer temp = new StringBuffer();
 
             for (String genre : genres) {
                 temp.append(", ");
                 temp.append(genre);
             }
 
             values.put("key", "genres");
             values.put("value", temp.substring(2));
 
             db.insertOrThrow("manga_metadata", null, values);
         }
     }
 
     public boolean deleteManga(long mangaId) {
         SQLiteDatabase db = getDatabase();
 
         return db.delete("manga", "id = ?",
                          new String[] { Long.toString(mangaId) }) == 1;
     }
 
     public void deleteTemporaryManga() {
         SQLiteDatabase db = getDatabase();
 
         db.delete("manga", "subscription_status = 0", null);
     }
 
     private class DBOpenHelper extends SQLiteOpenHelper {
         public DBOpenHelper(Context context, String name) {
             super(context, name, null, VERSION);
         }
 
         @Override
         public void onOpen(SQLiteDatabase db) {
             super.onOpen(db);
 
             // Enable foreign key constraints
             if (!db.isReadOnly())
                 db.execSQL("PRAGMA foreign_keys=ON");
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             db.execSQL(CREATE_MANGA_TABLE);
             db.execSQL(CREATE_MANGA_METADATA_TABLE);
             db.execSQL(CREATE_CHAPTERS_TABLE);
             db.execSQL(CREATE_PAGES_TABLE);
             db.execSQL(CREATE_MANGA_METADATA_TABLE_FK_INDEX);
             db.execSQL(CREATE_CHAPTERS_TABLE_FK_INDEX);
             db.execSQL(CREATE_PAGES_TABLE_FK_INDEX);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int from, int to) {
            if (from < 2 && to >= 2)
                upgrade1To2(db);
            if (from < 3 && to >= 3)
                upgrade2To3(db);
            if (from < 4 && to >= 4)
                upgrade3To4(db);
         }
 
         private void upgrade1To2(SQLiteDatabase db) {
             db.beginTransaction();
 
             try {
                 db.execSQL(
                     "ALTER TABLE pages" +
                     "    RENAME TO pages_tmp");
                 db.execSQL(
                     "CREATE TABLE pages (" +
                     "    id INTEGER PRIMARY KEY," +
                     "    chapter_id INTEGER NOT NULL," +
                     "    number INTEGER NOT NULL," +
                     "    url TEXT NOT NULL," +
                     "    image_url TEXT," +
                     "    download_status INTEGER NOT NULL," +
                     "    FOREIGN KEY (chapter_id) REFERENCES chapters(id)" +
                     "        ON DELETE CASCADE" +
                     ")");
                 db.execSQL(
                     "INSERT INTO pages" +
                     "    (id, chapter_id, number, url, image_url," +
                     "     download_status)" +
                     "    SELECT id, chapter_id, number, url, image_url, " +
                     "        download_status" +
                     "    FROM pages_tmp");
                 db.execSQL(
                     "DROP TABLE pages_tmp");
                 db.setTransactionSuccessful();
             } finally {
                 db.endTransaction();
             }
         }
 
         private void upgrade2To3(SQLiteDatabase db) {
             db.beginTransaction();
 
             try {
                 db.execSQL(
                     "CREATE TABLE manga_metadata (" +
                     "    id INTEGER PRIMARY KEY," +
                     "    manga_id INTEGER NOT NULL," +
                     "    key TEXT NOT NULL," +
                     "    value TEXT NOT NULL," +
                     "    FOREIGN KEY (manga_id) REFERENCES manga(id) " +
                     "        ON DELETE CASCADE" +
                     ")");
                 db.execSQL(
                     "ALTER TABLE manga" +
                     "    RENAME TO manga_tmp");
                 db.execSQL(
                     "CREATE TABLE manga (" +
                     "    id INTEGER PRIMARY KEY," +
                     "    title TEXT NOT NULL," +
                     "    pattern TEXT NOT NULL," +
                     "    url TEXT NOT NULL," +
                     "    subscription_status INTEGER NOT NULL" +
                     ")");
                 db.execSQL(
                     "INSERT INTO manga" +
                     "    (id, title, pattern, url, subscription_status)" +
                     "    SELECT id, title, pattern, url, 1" +
                     "    FROM manga_tmp");
                 db.execSQL(
                     "DROP TABLE manga_tmp");
                 db.setTransactionSuccessful();
             } finally {
                 db.endTransaction();
             }
         }
 
         private void upgrade3To4(SQLiteDatabase db) {
             db.beginTransaction();
 
             try {
                 db.execSQL(
                     "CREATE INDEX manga_metadata_manga_fk ON manga_metadata(manga_id)");
                 db.execSQL(
                     "CREATE INDEX chapters_manga_fk ON chapters(manga_id)");
                 db.execSQL(
                     "CREATE INDEX pages_chapters_fk ON pages(chapter_id)");
                 db.setTransactionSuccessful();
             } finally {
                 db.endTransaction();
             }
         }
     }
 }
