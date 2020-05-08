 package de.tum.in.tumcampus.models;
 
 import java.io.File;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class EventManager extends SQLiteOpenHelper {
 
 	private static final int DATABASE_VERSION = 1;
 
 	public SQLiteDatabase db;
 
 	public EventManager(Context context, String database) {
 		super(context, database, null, DATABASE_VERSION);
 
 		db = this.getWritableDatabase();
 		onCreate(db);
 	}
 
 	public void downloadFromExternal() throws Exception {
 
 		cleanupDb();
 		db.beginTransaction();
 
 		String baseUrl = "https://graph.facebook.com/162327853831856/events?access_token=";
 		String token = URLEncoder
 				.encode("141869875879732|FbjTXY-wtr06A18W9wfhU8GCkwU");
 
 		JSONArray jsonArray = Utils.downloadJson(baseUrl + token).getJSONArray(
 				"data");
 
 		String eventUrl = "http://graph.facebook.com/";
 
 		// TODO limit 25
 		for (int i = 0; i < jsonArray.length(); i++) {
 			String eventId = jsonArray.getJSONObject(i).getString("id");
 			replaceIntoDb(getFromJson(Utils.downloadJson(eventUrl + eventId)));
 		}
 		db.setTransactionSuccessful();
 		db.endTransaction();
 	}
 
 	public List<Event> getAllFromDb() {
 		List<Event> list = new ArrayList<Event>();
 
 		Cursor c = db.rawQuery(
				"SELECT * FROM events WHERE end_time > datetime() ORDER BY start_time asc "
 						+ "LIMIT 25", null);
 
 		while (c.moveToNext()) {
 			// TODO implement
 			/*
 			 * list.add(new Event(c.getInt(c.getColumnIndex("feedId")), c
 			 * .getString(c.getColumnIndex("title")), c.getString(c
 			 * .getColumnIndex("link")), c.getString(c
 			 * .getColumnIndex("description")), Utils.getDate(c
 			 * .getString(c.getColumnIndex("date"))), c.getString(c
 			 * .getColumnIndex("image"))));
 			 */
 		}
 		c.close();
 		return list;
 	}
 
 	/**
 	 * 
 	 * 
 	 * Example JSON: e.g. { "id": "166478443419659", "owner": { "name":
 	 * "TUM Campus App for Android", "category": "Software", "id":
 	 * "162327853831856" }, "name":
 	 * "R\u00fcckmeldung f\u00fcr Wintersemester 2011/12", "description":
 	 * "..."
 	 * , "start_time": "2011-08-15T00:00:00", "end_time": "2011-08-15T03:00:00",
 	 * "location": "TU M\u00fcnchen", "privacy": "OPEN", "updated_time":
 	 * "2011-06-25T06:26:14+0000" }
 	 * 
 	 * @param json
 	 * @return Event
 	 * @throws JSONException
 	 */
 	public static Event getFromJson(JSONObject json) throws Exception {
 
 		String eventId = json.getString("id");
 
 		String picture = "http://graph.facebook.com/" + eventId
 				+ "/Picture?type=large";
 
 		String target = Utils.getCacheDir("events/cache") + eventId + ".jpg";
 
 		if (!new File(target).exists()) {
 			Utils.downloadFile(picture, target);
 		}
 
 		String description = "";
 		if (json.has("description")) {
 			description = json.getString("description");
 		}
 		String location = "";
 		if (json.has("location")) {
 			location = json.getString("location");
 		}
 
 		// TODO implement link?
 
 		return new Event(eventId, json.getString("name"),
 				Utils.getDateTime(json.getString("start_time")),
 				Utils.getDateTime(json.getString("end_time")), location,
 				description, "", target);
 	}
 
 	public void replaceIntoDb(Event e) throws Exception {
 		// Log.d("TumCampus events replaceIntoDb", e.toString());
 
 		if (e.id.length() == 0) {
 			throw new Exception("Invalid id.");
 		}
 		if (e.name.length() == 0) {
 			throw new Exception("Invalid name.");
 		}
 		db.execSQL(
 				"REPLACE INTO events (id, name, start_time, end_time, location, "
 						+ "description, link, image) "
 						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
 				new String[] { String.valueOf(e.id), e.name,
 						Utils.getDateTimeString(e.start_time),
 						Utils.getDateTimeString(e.end_time), e.location,
 						e.description, e.link, e.image });
 	}
 
 	public void deleteAllFromDb() {
 		Log.d("TumCampus events deleteAllFromDb", "");
 		db.execSQL("DELETE FROM events");
 	}
 
 	public void cleanupDb() {
 		db.execSQL("DELETE FROM events WHERE start_time < date('now','-2 week')");
 	}
 
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE IF NOT EXISTS events ("
 				+ "id VARCHAR PRIMARY KEY, name VARCHAR, start_time VARCHAR, "
 				+ "end_time VARCHAR, location VARCHAR, description VARCHAR, "
 				+ "link VARCHAR, image VARCHAR)");
 	}
 
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		onCreate(db);
 	}
 }
