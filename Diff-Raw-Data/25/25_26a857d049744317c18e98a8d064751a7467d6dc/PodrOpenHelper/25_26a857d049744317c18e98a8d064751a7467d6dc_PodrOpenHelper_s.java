 package com.johandahlberg.podr.data;
 
 import com.johandahlberg.podr.BuildConfig;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 public class PodrOpenHelper extends SQLiteOpenHelper {
 	private static final String LOG_TAG = ".data.PodrOpenHelper";
 	public static final String DATABASE_NAME = "podr_data.db";
 	private static final int DATABASE_VERSION = 5;
 
 	public static final String SUBSCRIPTION_TABLE_NAME = "subscription";
 	public static final String SUBSCRIPTION_COL_TITLE = "title";
 	public static final String SUBSCRIPTION_COL_LINK = "link";
 	public static final String SUBSCRIPTION_COL_LANGUAGE = "language";
 	public static final String SUBSCRIPTION_COL_COPYRIGHT = "copyright";
 	public static final String SUBSCRIPTION_COL_DESC = "desc";
 	public static final String SUBSCRIPTION_COL_LASTUPDATED = "lastUpdated";
 	public static final String SUBSCRIPTION_COL_AUTODOWNLOAD = "autoDownload";
 	public static final String SUBSCRIPTION_COL_ITUNESSUBTITLE = "itunesSubtitle";
 	public static final String SUBSCRIPTION_COL_ITUNESAUTHOR = "itunesAuthor";
 	public static final String SUBSCRIPTION_COL_ITUNESSUMMARY = "itunesSummary";
 	public static final String SUBSCRIPTION_COL_ITUNESOWNERNAME = "itunesOwnerName";
 	public static final String SUBSCRIPTION_COL_ITUNESOWNEREMAIL = "itunesOwnerEmail";
 	public static final String SUBSCRIPTION_COL_ITUNESIMAGE = "itunesImage";
 	public static final String SUBSCRIPTION_COL_ITUNESCATEGORY = "itunesCategory";
 
 	private static final String SUBSCRIPTION_TABLE_CREATE = "CREATE TABLE "
 			+ SUBSCRIPTION_TABLE_NAME + " (" + BaseColumns._ID
 			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + SUBSCRIPTION_COL_TITLE
 			+ " TEXT NOT NULL DEFAULT 'Refresh to fetch info', "
 			+ SUBSCRIPTION_COL_LINK + " TEXT NOT NULL, "
 			+ SUBSCRIPTION_COL_DESC + " TEXT, " + SUBSCRIPTION_COL_LANGUAGE
 			+ " TEXT, " + SUBSCRIPTION_COL_COPYRIGHT + " TEXT, "
 			+ SUBSCRIPTION_COL_LASTUPDATED
 			+ " INTEGER NOT NULL DEFAULT (strftime('%s','now')), "
			+ SUBSCRIPTION_COL_AUTODOWNLOAD + " INTEGER NOT NULL 0, "
 			+ SUBSCRIPTION_COL_ITUNESSUBTITLE + " TEXT, "
 			+ SUBSCRIPTION_COL_ITUNESAUTHOR + " TEXT, "
 			+ SUBSCRIPTION_COL_ITUNESSUMMARY + " TEXT, "
 			+ SUBSCRIPTION_COL_ITUNESOWNERNAME + " TEXT, "
 			+ SUBSCRIPTION_COL_ITUNESOWNEREMAIL + " TEXT, "
 			+ SUBSCRIPTION_COL_ITUNESIMAGE + " TEXT, "
 			+ SUBSCRIPTION_COL_ITUNESCATEGORY + " TEXT);";
 
 	public static final String EPISODE_TABLE_NAME = "episode";
 	public static final String EPISODE_COL_TITLE = "title";
 	public static final String EPISODE_COL_GUID = "guid";
 	public static final String EPISODE_COL_PUBDATE = "pubDate";
 	public static final String EPISODE_COL_ENCLOSURE = "enclosure";
 	public static final String EPISODE_COL_SUBSCRIPTIONID = "subscriptionId";
 	public static final String EPISODE_COL_ITUNESAUTHOR = "itunesAuthor";
 	public static final String EPISODE_COL_ITUNESSUBTITLE = "itunesSubtitle";
 	public static final String EPISODE_COL_ITUNESSUMMARY = "itunesSummary";
 	public static final String EPISODE_COL_ITUNESIMAGE = "itunesImage";
 	public static final String EPISODE_COL_ITUNESDURATION = "itunesDuration";
 	public static final String EPISODE_COL_ITUNESKEYWORDS = "itunesKeywords";
 	public static final String EPISODE_COL_STATUS = "status";
 
 	private static final String EPISODE_TABLE_CREATE = "CREATE TABLE "
 			+ EPISODE_TABLE_NAME + " (" + BaseColumns._ID
 			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + EPISODE_COL_TITLE
 			+ " TEXT NOT NULL, " + EPISODE_COL_GUID + " TEXT, "
 			+ EPISODE_COL_PUBDATE
 			+ " INTEGER NOT NULL DEFAULT (strftime('%s','now')), "
 			+ EPISODE_COL_ENCLOSURE + " TEXT, " + EPISODE_COL_SUBSCRIPTIONID
 			+ " INTEGER NOT NULL DEFAULT -1, " + EPISODE_COL_ITUNESAUTHOR
 			+ " TEXT, " + EPISODE_COL_ITUNESSUBTITLE + " TEXT, "
 			+ EPISODE_COL_ITUNESSUMMARY + " TEXT, " + EPISODE_COL_ITUNESIMAGE
 			+ " TEXT, " + EPISODE_COL_ITUNESDURATION + " TEXT, "
 			+ EPISODE_COL_ITUNESKEYWORDS + " TEXT, " + EPISODE_COL_STATUS
 			+ " INTEGER NOT NULL DEFAULT 0 );";
 
 	public static final String DOWNLOAD_TABLE_NAME = "download";
 	public static final String DOWNLOAD_COL_EPISODEID = "episodeId";
 	public static final String DOWNLOAD_COL_URL = "url";
 	public static final String DOWNLOAD_COL_FILE = "file";
 
 	private static final String DOWNLOAD_TABLE_CREATE = "CREATE TABLE "
 			+ DOWNLOAD_TABLE_NAME + " (" + BaseColumns._ID
 			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + DOWNLOAD_COL_EPISODEID
 			+ " INTEGER NOT NULL DEFAULT -1, " + DOWNLOAD_COL_URL + " TEXT, "
 			+ DOWNLOAD_COL_FILE + " TEXT);";
 
 	PodrOpenHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		if (BuildConfig.DEBUG)
 			Log.v(LOG_TAG, "onCreate()");
 
 		db.execSQL(SUBSCRIPTION_TABLE_CREATE);
 		db.execSQL(EPISODE_TABLE_CREATE);
 		db.execSQL(DOWNLOAD_TABLE_CREATE);
 
 		if (BuildConfig.DEBUG)
 			seedData(db);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		if (BuildConfig.DEBUG)
 			Log.v(LOG_TAG, "onUpgrade(db, [" + oldVersion + "]->[" + newVersion
 					+ "]");
 
 		if (oldVersion == 4 && newVersion == 5) {
 			// Keep the data thats there, and add the column
 			db.execSQL("ALTER TABLE " + SUBSCRIPTION_TABLE_NAME
 					+ " add column " + SUBSCRIPTION_COL_AUTODOWNLOAD + " TEXT");
 		} else {
 			Log.w(LOG_TAG,
 					"Upgrading database. Existing content will be lost. ["
 							+ oldVersion + "]->[" + newVersion + "]");
 			db.execSQL("DROP TABLE IF EXISTS " + SUBSCRIPTION_TABLE_NAME);
 			db.execSQL("DROP TABLE IF EXISTS " + EPISODE_TABLE_NAME);
 			db.execSQL("DROP TABLE IF EXISTS " + DOWNLOAD_TABLE_NAME);
 			onCreate(db);
 		}
 	}
 
 	private void seedData(SQLiteDatabase db) {
 		db.execSQL("INSERT INTO " + SUBSCRIPTION_TABLE_NAME + " ("
 				+ SUBSCRIPTION_COL_LINK + ")"
 				+ " VALUES ('http://sverigesradio.se/api/rss/pod/9946');");
 		db.execSQL("INSERT INTO " + SUBSCRIPTION_TABLE_NAME + " ("
 				+ SUBSCRIPTION_COL_LINK + ")"
 				+ " VALUES ('http://sverigesradio.se/api/rss/pod/7565');");
 		db.execSQL("INSERT INTO " + SUBSCRIPTION_TABLE_NAME + " ("
 				+ SUBSCRIPTION_COL_LINK + ")"
 				+ " VALUES ('http://sverigesradio.se/api/rss/pod/3951');");
 		db.execSQL("INSERT INTO " + SUBSCRIPTION_TABLE_NAME + " ("
 				+ SUBSCRIPTION_COL_LINK + ")"
 				+ " VALUES ('http://sverigesradio.se/api/rss/pod/3953');");
 		db.execSQL("INSERT INTO " + SUBSCRIPTION_TABLE_NAME + " ("
 				+ SUBSCRIPTION_COL_LINK + ")"
 				+ " VALUES ('http://www.npr.org/templates/rss/podlayer.php?id=129577422');");
 	}
 }
