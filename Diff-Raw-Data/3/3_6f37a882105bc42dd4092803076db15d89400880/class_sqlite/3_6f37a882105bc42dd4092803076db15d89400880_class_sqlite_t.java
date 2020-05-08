 package com.dimoshka.ua.classes;
 
 import com.dimoshka.ua.jwp.R;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class class_sqlite extends SQLiteOpenHelper {
 
     public SQLiteDatabase database;
     public class_functions funct = new class_functions();
     private Context context;
 
     public class_sqlite(Context context) {
         super(context, context.getString(R.string.db_name), null, Integer
                 .valueOf(context.getString(R.string.db_version)));
         this.context = context;
         database = this.getWritableDatabase();
     }
 
     public SQLiteDatabase openDataBase() {
         return database;
     }
 
     @Override
     public void onCreate(SQLiteDatabase database) {
         Log.i(getClass().getName(), "Start create SQLITE");
         try {
             // -- Table: type
             database.execSQL("CREATE TABLE type (_id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, name VARCHAR(16) UNIQUE, code VARCHAR(6) NOT NULL UNIQUE);");
             database.execSQL("INSERT INTO [type] ([_id], [name], [code]) VALUES (1, 'EPUB', 'epub');");
             database.execSQL("INSERT INTO [type] ([_id], [name], [code]) VALUES (2, 'PDF', 'pdf');");
             database.execSQL("INSERT INTO [type] ([_id], [name], [code]) VALUES (3, 'MP3', 'mp3');");
             database.execSQL("INSERT INTO [type] ([_id], [name], [code]) VALUES (4, 'AAC', 'm4b');");
             // -- Table: publication
             database.execSQL("CREATE TABLE publication (_id  INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, name VARCHAR(128) NOT NULL UNIQUE, code VARCHAR(3) NOT NULL UNIQUE);");
             database.execSQL("INSERT INTO [publication] ([_id], [name], [code]) VALUES (1, 'THE WATCHTOWER (STUDY EDITION)', 'w');");
             database.execSQL("INSERT INTO [publication] ([_id], [name], [code]) VALUES (2, 'THE WATCHTOWER', 'wp');");
             database.execSQL("INSERT INTO [publication] ([_id], [name], [code]) VALUES (3, 'AWAKE!', 'g');");
             database.execSQL("INSERT INTO [publication] ([_id], [name], [code]) VALUES (4, 'Books and brochures', 'b');");
             // -- Table: magazine
             database.execSQL("CREATE TABLE magazine (_id INTEGER PRIMARY KEY ASC AUTOINCREMENT UNIQUE, name VARCHAR(128) UNIQUE, title VARCHAR(128), id_pub INTEGER NOT NULL, id_lang INTEGER NOT NULL, img BOOLEAN DEFAULT (0), link_img VARCHAR(256), date DATE NOT NULL);");
             database.execSQL("INSERT INTO [magazine] ([_id], [name], [title], [id_pub], [id_lang], [img], [link_img], [date]) VALUES (5334, 'bh_U.pdf', 'Чему на самом деле учит Библия?', 4, 3, 0, 'http://www.jw.org/assets/a/bh/bh_U/bh_U.prd_md.jpg', '20130517');");
             database.execSQL("INSERT INTO [magazine] ([_id], [name], [title], [id_pub], [id_lang], [img], [link_img], [date]) VALUES (5913, 'yb13_U.pdf', 'Ежегодник Свидетелей Иеговы 2013', 4, 3, 0, 'http://www.jw.org/assets/a/yb13/yb13_U/yb13_U.prd_md.jpg', '20130517');");
             // -- Table: files
             database.execSQL("CREATE TABLE files (_id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, id_magazine INTEGER NOT NULL, id_type INTEGER NOT NULL, name VARCHAR(32) NOT NULL UNIQUE, link VARCHAR(256) NOT NULL, pubdate DATE NOT NULL, title VARCHAR(256) NOT NULL, file BOOLEAN DEFAULT (0));");
             database.execSQL("INSERT INTO [files] ([_id], [id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES (null, 5334, 2, 'bh_U.pdf', 'http://download.jw.org/files/media_books/5e/bh_U.pdf', '20130517', '', 0);");
             database.execSQL("INSERT INTO [files] ([_id], [id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES (null, 5913, 2, 'yb13_U.pdf', 'http://download.jw.org/files/media_books/0f/yb13_U.pdf', '20130517', '', 0);");
             // -- Table: news
             database.execSQL("CREATE TABLE news (_id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, id_lang INTEGER NOT NULL, title VARCHAR(256) NOT NULL, link VARCHAR(256) NOT NULL UNIQUE, link_img VARCHAR(256), description VARCHAR(256) NOT NULL, pubdate DATETIME NOT NULL, img BOOLEAN DEFAULT (0));");
             // -- Table: language
             database.execSQL("CREATE TABLE language (_id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(64) NOT NULL UNIQUE, code VARCHAR(4) NOT NULL UNIQUE, code_an VARCHAR(6) NOT NULL UNIQUE, news_rss VARCHAR(24) NOT NULL);");
             database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (1, 'English', 'E', 'en', 'en/news');");
             database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (2, 'French', 'F', 'fr', 'fr/actualites');");
             database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (3, 'Русский', 'U', 'ru', 'ru/%D0%BD%D0%BE%D0%B2%D0%BE%D1%81%D1%82%D0%B8');");
             database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (4, 'Українська', 'K', 'uk', 'uk/%D0%BD%D0%BE%D0%B2%D0%B8%D0%BD%D0%B8');");
             database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (5, 'Deutsch', 'X', 'de', 'de/aktuelle-meldungen');");
             database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (6, 'Spanish', 'S', 'es', 'es/noticias');");
             database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (7, '漢語繁體字', 'CH', 'zh', 'zh-hant/%E6%96%B0%E8%81%9E');");
             database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (8, '汉语简化字', 'CHS', 'zhc', 'zh-hans/%E6%96%B0%E9%97%BB');");
 
             // -- Index: idx_files
             database.execSQL("CREATE INDEX idx_files ON files (id_magazine COLLATE NOCASE ASC, id_type COLLATE NOCASE ASC);");
         } catch (Exception ex) {
             funct.send_bug_report(context, ex, getClass().getName(), 63);
         }
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase database, int oldVersion,
                           int newVersion) {
         Log.i("JWP" + getClass().getName(), "Start update SQLITE");
         switch (oldVersion) {
             case 1:
 
                 // -- Table: magazine
                database.execSQL("ALTER TABLE [magazine] ADD link_img VARCHAR(256);");
                database.execSQL("ALTER TABLE [magazine] ADD title VARCHAR(128);");
 
                 // -- Table: publication
                 database.execSQL("INSERT INTO [publication] ([_id], [name], [code]) VALUES (4, 'Books and brochures', 'b');");
 
                 // -- Table: magazine
                 database.execSQL("INSERT INTO [magazine] ([_id], [name], [title], [id_pub], [id_lang], [img], [link_img], [date]) VALUES (5334, 'bh_U.pdf', 'Чему на самом деле учит Библия?', 4, 3, 0, 'http://www.jw.org/assets/a/bh/bh_U/bh_U.prd_md.jpg', '20130517');");
                 database.execSQL("INSERT INTO [magazine] ([_id], [name], [title], [id_pub], [id_lang], [img], [link_img], [date]) VALUES (5913, 'yb13_U.pdf', 'Ежегодник Свидетелей Иеговы 2013', 4, 3, 0, 'http://www.jw.org/assets/a/yb13/yb13_U/yb13_U.prd_md.jpg', '20130517');");
 
 
                 // -- Table: files
                 database.execSQL("INSERT INTO [files] ([_id], [id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES (null, 5334, 2, 'bh_U.pdf', 'http://download.jw.org/files/media_books/5e/bh_U.pdf', '20130517', '', 0);");
                 database.execSQL("INSERT INTO [files] ([_id], [id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES (null, 5913, 2, 'yb13_U.pdf', 'http://download.jw.org/files/media_books/0f/yb13_U.pdf', '20130517', '', 0);");
 
                 break;
             default:
                 database.execSQL("DROP TABLE IF EXISTS type");
                 database.execSQL("DROP TABLE IF EXISTS language");
                 database.execSQL("DROP TABLE IF EXISTS publication");
                 database.execSQL("DROP TABLE IF EXISTS magazine");
                 onCreate(database);
                 break;
         }
 
     }
 
     public void close() {
         if (database != null) {
             database.close();
         }
     }
 }
