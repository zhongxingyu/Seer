 package de.tum.in.tumcampus.models;
 
 import java.net.URLEncoder;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.text.Html;
import android.util.Log;
 
 public class FeedItemManager extends SQLiteOpenHelper {
 
 	private static final int DATABASE_VERSION = 1;
 
 	private SQLiteDatabase db;
 
 	public FeedItemManager(Context context, String database) {
 		super(context, database, null, DATABASE_VERSION);
 
 		db = this.getWritableDatabase();
 		onCreate(db);
 	}
 
 	public void downloadFromExternal(List<Integer> ids, boolean force)
 			throws Exception {
 
 		cleanupDb();
 		for (int i = 0; i < ids.size(); i++) {
 
 			String syncId = "feeditem" + i;
 			if (!force && !SyncManager.needSync(db, syncId, 900)) {
 				continue;
 			}
 			Cursor feed = db.rawQuery("SELECT feedUrl FROM feeds WHERE id = ?",
 					new String[] { String.valueOf(ids.get(i)) });
 			feed.moveToNext();
 			String feedUrl = feed.getString(0);
 			feed.close();
 
 			// TODO add try catch
 
 			String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";
 			String query = URLEncoder
 					.encode("SELECT title, link, description, pubDate, enclosure.url "
 							+ "FROM rss WHERE url=\"" + feedUrl + "\" LIMIT 25");
 
			JSONArray jsonArray = Utils.downloadJson(baseUrl + query)
 					.getJSONObject("query").getJSONObject("results")
					.getJSONArray("item");
 
 			deleteFromDb(ids.get(i));
 			db.beginTransaction();
 			for (int j = 0; j < jsonArray.length(); j++) {
 				insertIntoDb(getFromJson(ids.get(i), jsonArray.getJSONObject(j)));
 			}
 			SyncManager.replaceIntoDb(db, syncId);
 			db.setTransactionSuccessful();
 			db.endTransaction();
 		}
 	}
 
 	public Cursor getAllFromDb(String feedId) {
 		return db.rawQuery("SELECT image, title, description, link, id as _id "
 				+ "FROM feeds_items WHERE feedId = ? ORDER BY date DESC",
 				new String[] { feedId });
 	}
 
 	/**
 	 * 
 	 * 
 	 * Example JSON: e.g. { "title":
 	 * "US-Truppenabzug aus Afghanistan: \"Verlogen und verkorkst\"",
 	 * "description": "..." , "link":
 	 * "http://www.n-tv.de/politik/pressestimmen/Verlogen-und-verkorkst-article3650731.html"
 	 * , "pubDate": "Thu, 23 Jun 2011 20:06:53 GMT", "enclosure": { "url":
 	 * "http://www.n-tv.de/img/30/304801/Img_4_3_220_Pressestimmen.jpg" }
 	 * 
 	 * @param json
 	 * @return Feeds
 	 * @throws JSONException
 	 */
 	public static FeedItem getFromJson(int feedId, JSONObject json)
 			throws Exception {
 
 		String target = "";
 		if (json.has("enclosure")) {
 			final String enclosure = json.getJSONObject("enclosure").getString(
 					"url");
 
 			target = Utils.getCacheDir("rss/cache") + Utils.md5(enclosure)
 					+ ".jpg";
 			Utils.downloadFileThread(enclosure, target);
 		}
 		Date pubDate = new Date();
 		if (json.has("pubDate")) {
			pubDate = Utils.getDate(json.getString("pubDate"));
 		}
 		String description = "";
		if (json.has("description")) {
 			// decode HTML entites, remove links, images, etc.
 			description = Html.fromHtml(
 					json.getString("description").replaceAll("\\<.*?\\>", ""))
 					.toString();
 		}
 		return new FeedItem(feedId, json.getString("title"),
 				json.getString("link"), description, pubDate, target);
 	}
 
 	public void insertIntoDb(FeedItem n) throws Exception {
		Log.d("TumCampus feeds replaceIntoDb", n.toString());

 		if (n.feedId <= 0) {
 			throw new Exception("Invalid feedId.");
 		}
 		if (n.link.length() == 0) {
 			throw new Exception("Invalid link.");
 		}
 		if (n.title.length() == 0) {
 			throw new Exception("Invalid title.");
 		}
 		db.execSQL(
 				"INSERT INTO feeds_items (feedId, title, link, description, "
 						+ "date, image) VALUES (?, ?, ?, ?, ?, ?)",
 				new String[] { String.valueOf(n.feedId), n.title, n.link,
 						n.description, Utils.getDateString(n.date), n.image });
 	}
 
 	public void removeCache() {
		Log.d("TumCampus feeds deleteAllFromDb", "");

 		db.execSQL("DELETE FROM feeds_items");
 		Utils.emptyCacheDir("rss/cache");
 	}
 
 	public void deleteFromDb(int feedId) {
 		db.execSQL("DELETE FROM feeds_items WHERE feedId = ?",
 				new String[] { String.valueOf(feedId) });
 	}
 
 	public void cleanupDb() {
 		db.execSQL("DELETE FROM feeds_items WHERE date < date('now','-7 day')");
 	}
 
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE IF NOT EXISTS feeds_items ("
 				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, feedId INTEGER, "
 				+ "title VARCHAR, link VARCHAR, description VARCHAR, "
 				+ "date VARCHAR, image VARCHAR)");
 	}
 
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		onCreate(db);
 	}
 }
