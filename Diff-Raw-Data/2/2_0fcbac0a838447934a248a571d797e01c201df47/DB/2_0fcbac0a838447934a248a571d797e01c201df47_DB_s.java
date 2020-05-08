 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
 package org.barbon.mangaget.data;
 
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
             "SELECT id AS _id, number, title" +
             "    FROM chapters" +
             "    WHERE manga_id = ?" +
             "    ORDER BY number",
             new String[] { Long.toString(mangaId) });
     }
 
     private class DBOpenHelper extends SQLiteOpenHelper {
         public DBOpenHelper(Context context, String name) {
             super(context, name, null, VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             db.execSQL(CREATE_MANGA_TABLE);
             db.execSQL(CREATE_CHAPTERS_TABLE);
 
             // TODO remove canned data
             db.execSQL(
                 "INSERT INTO manga (id, title, pattern, url)" +
                 "    VALUES (1, 'Papillon - Hana to Chou'," +
                "      'Papillon/papillon-%d'," +
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
