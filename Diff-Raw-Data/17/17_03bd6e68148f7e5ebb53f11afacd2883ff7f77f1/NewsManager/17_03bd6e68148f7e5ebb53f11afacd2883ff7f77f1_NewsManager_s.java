 ﻿package de.tum.in.tumcampus.models;
 
 import java.net.URLEncoder;
 import java.util.Date;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import de.tum.in.tumcampus.Const;
 
 /**
  * News Manager, handles database stuff, external imports
  */
 public class NewsManager extends SQLiteOpenHelper {
 
 	/**
 	 * Database connection
 	 */
 	private SQLiteDatabase db;
 
 	/**
 	 * Last insert counter
 	 */
 	public static int lastInserted = 0;
 
 	/**
 	 * Constructor, open/create database, create table if necessary
 	 * 
 	 * <pre>
 	 * @param context Context
 	 * @param database Filename, e.g. database.db
 	 * </pre>
 	 */
 	public NewsManager(Context context, String database) {
 		super(context, database, null, Const.dbVersion);
 
 		db = getWritableDatabase();
 		onCreate(db);
 	}
 
 	/**
 	 * Download news from external interface (JSON)
 	 * 
 	 * <pre>
 	 * @param force True to force download over normal sync period, else false
 	 * @throws Exception
 	 * </pre>
 	 */
 	public void downloadFromExternal(boolean force) throws Exception {
 
 		if (!force && !SyncManager.needSync(db, this, 86400)) {
 			return;
 		}
 
		String url = "https://graph.facebook.com/162327853831856/feed/?limit=50&access_token=";
 		String token = "141869875879732|FbjTXY-wtr06A18W9wfhU8GCkwU";
 
 		JSONArray jsonArray = Utils
 				.downloadJson(url + URLEncoder.encode(token)).getJSONArray(
 						"data");
 
 		cleanupDb();
 		int count = Utils.dbGetTableCount(db, "news");
 
 		db.beginTransaction();
 		try {
 			int countItems = 0;
 			for (int i = 0; i < jsonArray.length(); i++) {
 				JSONObject obj = jsonArray.getJSONObject(i);
 
 				// filter out events, empty items
				if (obj.has("properties")
						|| (!obj.has("message") && !obj.has("description") && !obj
								.has("caption"))
 						|| !obj.getJSONObject("from").getString("id")
 								.equals("162327853831856")) {
 					continue;
 				}
 				if (countItems > 24) {
 					break;
 				}
 				replaceIntoDb(getFromJson(obj));
 				countItems++;
 			}
 			SyncManager.replaceIntoDb(db, this);
 			db.setTransactionSuccessful();
 		} finally {
 			db.endTransaction();
 		}
 		// update last insert counter
 		lastInserted += Utils.dbGetTableCount(db, "news") - count;
 	}
 
 	/**
 	 * Get all news from the database
 	 * 
 	 * @return Database cursor (image, message, date_de, link, _id)
 	 */
 	public Cursor getAllFromDb() {
 		return db.rawQuery("SELECT image, message, strftime('%d.%m.%Y', date) "
 				+ "as date_de, link, id as _id "
 				+ "FROM news ORDER BY date DESC", null);
 	}
 
 	/**
 	 * Convert JSON object to News and download news image
 	 * 
 	 * Example JSON: e.g. { "id": "162327853831856_174943842570257", "from": {
 	 * ... }, "message": "Testing ...", "picture":
 	 * "http://photos-d.ak.fbcdn.net/hphotos-ak-ash4/268937_174943835903591_162327853831856_476156_7175901_s.jpg"
 	 * , "link":
 	 * "https://www.facebook.com/photo.php?fbid=174943835903591&set=a.174943832570258.47966.162327853831856&type=1"
 	 * , "name": "Wall Photos", "icon":
 	 * "http://static.ak.fbcdn.net/rsrc.php/v1/yz/r/StEh3RhPvjk.gif", "type":
 	 * "photo", "object_id": "174943835903591", "created_time":
 	 * "2011-07-04T01:58:25+0000", "updated_time": "2011-07-04T01:58:25+0000" },
 	 * 
 	 * <pre>
 	 * @param json see above
 	 * @return News
 	 * @throws Exception
 	 * </pre>
 	 */
 	public static News getFromJson(JSONObject json) throws Exception {
 
 		String target = "";
 		if (json.has("picture")) {
 			String picture = json.getString("picture");
 			target = Utils.getCacheDir("news/cache") + Utils.md5(picture)
 					+ ".jpg";
 			Utils.downloadFileThread(picture, target);
 		}
 		String link = "";
 		if (json.has("link")
 				&& !json.getString("link").contains("www.facebook.com")) {
 			link = json.getString("link");
 		}
 		if (link.length() == 0 && json.has("object_id")) {
 			link = "http://graph.facebook.com/" + json.getString("object_id")
 					+ "/Picture?type=normal";
 		}
 
 		// message empty => description empty => caption
 		String message = "";
 		if (json.has("message")) {
 			message = json.getString("message");
 		} else if (json.has("description")) {
 			message = json.getString("description");
 		} else if (json.has("caption")) {
 			message = json.getString("caption");
 		}
 		Date date = Utils.getDate(json.getString("created_time"));
 
 		return new News(json.getString("id"), message, link, target, date);
 	}
 
 	/**
 	 * Replace or Insert a event in the database
 	 * 
 	 * <pre>
 	 * @param n News object
 	 * @throws Exception
 	 * </pre>
 	 */
 	public void replaceIntoDb(News n) throws Exception {
 		Utils.log(n.toString());
 
 		if (n.id.length() == 0) {
 			throw new Exception("Invalid id.");
 		}
 		if (n.message.length() == 0) {
 			throw new Exception("Invalid message.");
 		}
 		db.execSQL("REPLACE INTO news (id, message, link, image, date) "
 				+ "VALUES (?, ?, ?, ?, ?)", new String[] { n.id, n.message,
 				n.link, n.image, Utils.getDateString(n.date) });
 	}
 
 	/**
 	 * Removes all cache items
 	 */
 	public void removeCache() {
 		db.execSQL("DELETE FROM news");
 		Utils.emptyCacheDir("news/cache");
 	}
 
 	/**
 	 * Removes all old items (older than 3 months)
 	 */
 	public void cleanupDb() {
 		db.execSQL("DELETE FROM news WHERE date < date('now','-3 month')");
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// create table if needed
 		db.execSQL("CREATE TABLE IF NOT EXISTS news ("
 				+ "id VARCHAR PRIMARY KEY, message VARCHAR, link VARCHAR, "
 				+ "image VARCHAR, date VARCHAR)");
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		onCreate(db);
 	}
 }
