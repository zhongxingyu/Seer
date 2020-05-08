 package de.tum.in.tumcampus.models;
 
 import java.net.URLEncoder;
 
 import org.json.JSONArray;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class TransportManager extends SQLiteOpenHelper {
 
 	private static final int DATABASE_VERSION = 1;
 
 	public SQLiteDatabase db;
 
 	public TransportManager(Context context, String database) {
 		super(context, database, null, DATABASE_VERSION);
 
 		db = this.getWritableDatabase();
 		onCreate(db);
 	}
 
 	public Cursor getDeparturesFromExternal(String location) throws Exception {
 		String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";
 		String lookupUrl = "http://www.mvg-live.de/ims/dfiStaticAnzeige.svc?haltestelle="
 				+ location;
 		String query = URLEncoder
 				.encode("select content from html where url=\"" + lookupUrl
 						+ "\" and xpath=\"//td[contains(@class,'Column')]/p\"");
 
 		Log.d("TumCampus transports departure", baseUrl + query);
 		
 		JSONArray jsonArray = Utils.downloadJson(baseUrl + query)
 				.getJSONObject("query").getJSONObject("results")
 				.getJSONArray("p");
 
 		if (jsonArray.length() < 3) {
 			throw new Exception("<Keine Abfahrten gefunden>");
 		}
 
 		MatrixCursor mc = new MatrixCursor(
 				new String[] { "name", "desc", "_id" });
 
 		for (int j = 2; j < jsonArray.length(); j = j + 3) {
 			String name = jsonArray.getString(j) + " "
 					+ jsonArray.getString(j + 1).trim();
 
 			String desc = jsonArray.getString(j + 2) + " min";
 
 			mc.addRow(new String[] { name, desc, String.valueOf(j) });
 		}
 		return mc;
 	}
 
 	public Cursor getStationsFromExternal(String location) throws Exception {
 		String baseUrl = "http://query.yahooapis.com/v1/public/yql?format=json&q=";
 		String lookupUrl = "http://www.mvg-live.de/ims/dfiStaticAuswahl.svc?haltestelle="
 				+ location;
 		String query = URLEncoder
 				.encode("select content from html where url=\"" + lookupUrl
 						+ "\" and xpath=\"//a[contains(@href,'haltestelle')]\"");
 
 		Log.d("TumCampus transports station", baseUrl + query);
 
		Object obj = Utils.downloadJson(baseUrl + query)
				.getJSONObject("query").getJSONObject("results").get("a");
		
		JSONArray jsonArray = new JSONArray();
		if (obj instanceof JSONArray) {
			jsonArray = (JSONArray) obj;
		} else {
			jsonArray.put(obj);
		}
 
 		MatrixCursor mc = new MatrixCursor(new String[] { "name", "_id" });
 
 		for (int j = 0; j < jsonArray.length(); j++) {
 			String station = jsonArray.getString(j).replaceAll("\\s+", " ");
 
 			mc.addRow(new String[] { station, String.valueOf(j) });
 		}
 		return mc;
 	}
 
 	public Cursor getAllFromDb() {
 		return db.rawQuery("SELECT name, name as _id FROM transports "
 				+ "ORDER BY name", null);
 	}
 
 	public void replaceIntoDb(String name) throws Exception {
 		Log.d("TumCampus transports replaceIntoDb", name);
 
 		if (name.length() == 0) {
 			throw new Exception("Invalid name.");
 		}
 		db.execSQL("REPLACE INTO transports (name) VALUES (?)",
 				new String[] { name });
 	}
 
 	public void deleteFromDb(String name) {
 		db.execSQL("DELETE FROM transports WHERE name = ?",
 				new String[] { name });
 	}
 
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE IF NOT EXISTS transports ("
 				+ "name VARCHAR PRIMARY KEY)");
 	}
 
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		onCreate(db);
 	}
 }
