 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
 package org.barbon.mangaget.data;
 
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
 
     private static final int VERSION = 1;
     private static final String DB_NAME = "manga";
     private static DB theInstance;
 
     private DBOpenHelper openHelper;
 
     private static final String CREATE_MANGA_TABLE =
         "CREATE TABLE manga (" +
         "    id INTEGER PRIMARY KEY," +
         "    title TEXT NOT NULL," +
         "    pattern TEXT NOT NULL," +
         "    url TEXT NOT NULL" +
         ")";
 
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
 
     private static final String CREATE_PAGES_TABLE =
         "CREATE TABLE pages (" +
         "    id INTEGER PRIMARY KEY," +
         "    chapter_id INTEGER NOT NULL," +
         "    number INTEGER NOT NULL," +
         "    url TEXT NOT NULL," +
         "    image_url TEXT," +
         "    download_status INTEGER NOT NULL," +
         "    FOREIGN KEY (chapter_id) REFERENCES chapter(id)" +
         "        ON DELETE CASCADE" +
         ")";
 
     public static DB getNewInstance(Context context, String name) {
         return new DB(context, name);
     }
 
     public static DB getInstance(Context context) {
         if (theInstance != null)
             return theInstance;
 
         return new DB(context, DB_NAME);
     }
 
     private DB(Context context, String name) {
         openHelper = new DBOpenHelper(context, name);
     }
 
     public Cursor getMangaList() {
         SQLiteDatabase db = openHelper.getWritableDatabase();
 
         return db.rawQuery(
             "SELECT id AS _id, title" +
             "    FROM manga" +
             "    ORDER BY title",
             null);
     }
 
     public Cursor getChapterList(long mangaId) {
         SQLiteDatabase db = openHelper.getWritableDatabase();
 
         return db.rawQuery(
             "SELECT id AS _id, number, title, download_status" +
             "    FROM chapters" +
             "    WHERE manga_id = ?" +
             "    ORDER BY number",
             new String[] { Long.toString(mangaId) });
     }
 
     public Cursor getPages(long chapterId) {
         SQLiteDatabase db = openHelper.getWritableDatabase();
 
         return db.rawQuery(
             "SELECT id AS _id, number, url, image_url, download_status" +
             "    FROM pages" +
             "    WHERE chapter_id = ?" +
             "    ORDER BY number",
             new String[] { Long.toString(chapterId) });
     }
 
     public ContentValues getManga(long mangaId) {
         SQLiteDatabase db = openHelper.getWritableDatabase();
         Cursor cursor = db.rawQuery(
             "SELECT id AS _id, title, pattern, url" +
             "    FROM manga" +
             "    WHERE id = ?",
             new String[] { Long.toString(mangaId)});
 
         ContentValues values = null;
 
         if (cursor.moveToNext()) {
             values = new ContentValues();
 
            values.put("title", cursor.getString(1));
             values.put("pattern", cursor.getString(2));
             values.put("url", cursor.getString(3));
         }
 
         cursor.close();
 
         return values;
     }
 
     public long getChapterId(long mangaId, int index) {
         SQLiteDatabase db = openHelper.getWritableDatabase();
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
         SQLiteDatabase db = openHelper.getWritableDatabase();
         Cursor cursor = db.rawQuery(
             "SELECT id AS _id, manga_id, title, number, url, download_status" +
             "    FROM chapters" +
             "    WHERE id = ?",
             new String[] { Long.toString(chapterId)});
 
         ContentValues values = null;
 
         if (cursor.moveToNext()) {
             values = new ContentValues();
 
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
         SQLiteDatabase db = openHelper.getWritableDatabase();
         ContentValues values = new ContentValues();
 
         values.put("download_status", downloadStatus);
 
         return db.update("chapters", values, "id = ?",
                          new String[] { Long.toString(chapterId) }) == 1;
     }
 
     public boolean updatePageStatus(long pageId, int downloadStatus) {
         SQLiteDatabase db = openHelper.getWritableDatabase();
         ContentValues values = new ContentValues();
 
         values.put("download_status", downloadStatus);
 
         return db.update("pages", values, "id = ?",
                          new String[] { Long.toString(pageId) }) == 1;
     }
 
     public boolean updatePageImage(long pageId, String imageUrl) {
         SQLiteDatabase db = openHelper.getWritableDatabase();
         ContentValues values = new ContentValues();
 
         values.put("image_url", imageUrl);
 
         return db.update("pages", values, "id = ?",
                          new String[] { Long.toString(pageId) }) == 1;
     }
 
     public long insertManga(String title, String pattern, String url) {
         SQLiteDatabase db = openHelper.getWritableDatabase();
         ContentValues values = new ContentValues();
 
         values.put("title", title);
         values.put("pattern", pattern);
         values.put("url", url);
 
         return db.insertOrThrow("manga", null, values);
     }
 
     public long insertChapter(long mangaId, int number, int pages,
                               String title, String url) {
         SQLiteDatabase db = openHelper.getWritableDatabase();
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
 
         SQLiteDatabase db = openHelper.getWritableDatabase();
         ContentValues values = new ContentValues();
 
         values.put("manga_id", mangaId);
         values.put("number", number);
         values.put("pages", pages);
         values.put("title", title);
         values.put("url", url);
         values.put("download_status", DB.DOWNLOAD_STOPPED);
 
         db.update("chapters", values, "id = ?",
                   new String[] { Long.toString(chapterId) });
 
         return chapterId;
     }
 
     public long insertPage(long chapterId, int number, String url,
                            String imageUrl, int downloadStatus) {
         SQLiteDatabase db = openHelper.getWritableDatabase();
         ContentValues values = new ContentValues();
 
         values.put("chapter_id", chapterId);
         values.put("number", number);
         values.put("url", url);
         values.put("image_url", imageUrl);
         values.put("download_status", downloadStatus);
 
         return db.insertOrThrow("pages", null, values);
     }
 
     private class DBOpenHelper extends SQLiteOpenHelper {
         public DBOpenHelper(Context context, String name) {
             super(context, name, null, VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             db.execSQL(CREATE_MANGA_TABLE);
             db.execSQL(CREATE_CHAPTERS_TABLE);
             db.execSQL(CREATE_PAGES_TABLE);
 
             // TODO remove canned data
             db.execSQL(
                 "INSERT INTO manga (id, title, pattern, url)" +
                 "    VALUES (1, 'Papillon - Hana to Chou'," +
                 "      'Pictures/Comics/Papillon/papillon-hana-to-chou-%02d.cbz'," +
                 "      'http://manga.animea.net/papillon-hana-to-chou.html')");
 
             db.execSQL(
                 "INSERT INTO chapters (manga_id, number, pages, title," +
                 "                      url, download_status)" +
                 "    VALUES (1, 1, 45, 'Wish'," +
                 "      'http://manga.animea.net/papillon-hana-to-chou-chapter-1-page-1.html'," +
                 "      3)");
 
             db.execSQL(
                 "INSERT INTO chapters (manga_id, number, pages, title," +
                 "                      url, download_status)" +
                 "    VALUES (1, 2, 27, 'The Depressing First Date'," +
                 "      'http://manga.animea.net/papillon-hana-to-chou-chapter-2-page-1.html'," +
                 "      1)");
 
             db.execSQL(
                 "INSERT INTO chapters (manga_id, number, pages, title," +
                 "                      url, download_status)" +
                 "    VALUES (1, 3, 29, 'Goodbye, Unrequited Love'," +
                 "      'http://manga.animea.net/papillon-hana-to-chou-chapter-3-page-1.html'," +
                 "      0)");
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int from, int to) {
             throw new UnsupportedOperationException("Upgrade not supported");
         }
     }
 }
