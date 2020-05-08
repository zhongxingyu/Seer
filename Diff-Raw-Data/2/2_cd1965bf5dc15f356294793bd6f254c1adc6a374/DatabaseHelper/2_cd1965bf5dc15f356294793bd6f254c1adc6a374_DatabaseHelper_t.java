 package com.pc.programmerslife;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import com.pc.framework.Utilities;
 import com.pc.framework.rss.Item;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 	private static final String DATABASE_NAME = "VDPDatabase";
 	private static final int DATABASE_VERSION = 1;
 	
 	private Context context;
 
 	public DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		this.context = context;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		String sql;
 		try {
			sql = Utilities.getStringFromInputStream(context.getAssets().open("create_database_tables"));
 			sql = sql.replace("\n", "");
 			sql = sql.replace("\t", "");
 			String[] queries = sql.split(";");
 			for (String query : queries)
 				db.execSQL(query);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 	}
 	
 	public ArrayList<Commic> getCommics(int starting, int quantity) {
 		SQLiteDatabase db = getReadableDatabase();
 		
 		String selectSQL = "SELECT title, description, content, link, pubDate, isFavorite, isRead, guid FROM commics DESC LIMIT ?, ?";
 		
 		try {
 			Cursor c = db.rawQuery(selectSQL, new String[] {
 					String.valueOf(starting),
 					String.valueOf(quantity)
 				});
 			if (c != null && c.moveToFirst() == true) {
 				ArrayList<Commic> commics = new ArrayList<Commic>(c.getCount());
 				Commic commic;
 				do {
 					commic = new Commic();
 					
 					commic.setTitle(c.getString(0));
 					commic.setDescription(c.getString(1));
 					commic.setContent(c.getString(2));
 					commic.setLink(c.getString(3));
 					commic.setDate(new Date(c.getLong(4)));
 					int favorite, read;
 					favorite = c.getInt(5);
 					commic.setFavorite(favorite == 1);
 					read = c.getInt(6);
 					commic.setRead(read == 1);
 					commic.setGuid(c.getString(7));
 					
 					commics.add(commic);
 					
 				} while (c.moveToNext() == true);
 				
 				c.close();
 				
 				return commics;
 			}
 		} catch (SQLException e) {
 			Log.e("VDP-MANAGER", e.getMessage());
 		}
 		
 		return null;
 	}
 	
 	public ArrayList<Commic> getFavorites() {
 		SQLiteDatabase db = getReadableDatabase();
 		
 		String selectSQL = "SELECT title, description, content, link, pubDate, guid FROM commics WHERE isFavorite == 1";
 		
 		try {
 			Cursor c = db.rawQuery(selectSQL, null);
 			if (c != null && c.moveToFirst() == true) {
 				ArrayList<Commic> commics = new ArrayList<Commic>(c.getCount());
 				Commic commic;
 				do {
 					commic = new Commic();
 					
 					commic.setTitle(c.getString(0));
 					commic.setDescription(c.getString(1));
 					commic.setContent(c.getString(2));
 					commic.setLink(c.getString(3));
 					commic.setDate(new Date(c.getLong(4)));
 					commic.setGuid(c.getString(5));
 					commic.setFavorite(true);
 					commic.setRead(true);
 					
 					commics.add(commic);
 				} while (c.moveToNext() == true);
 				
 				c.close();
 				
 				return commics;
 			}
 		} catch (SQLException e) {
 			Log.e("VDP-MANAGER", e.getMessage());
 		}
 		
 		return null;
 	}
 	
 	public int getCommicsCount() {
 		SQLiteDatabase db = getReadableDatabase();
 		
 		String selectSQL = "SELECT count(title) FROM commics";
 		
 		Cursor c = db.rawQuery(selectSQL, null);
 		
 		int count = (c == null || c.moveToFirst() == false) ? 0 : c.getInt(0);
 		
 		return count;
 	}
 	
 	public boolean updateCommic(Commic commic) {
 		String updateSQL = "UPDATE commics SET isRead = ?, isFavorite = ? WHERE guid = ?";
 		
 		SQLiteDatabase db = getWritableDatabase();
 		Exception exception = null;
 		
 		int favorite = (commic.isFavorite() == true) ? 1 : 0;
 		int read = (commic.isRead() == true) ? 1 : 0;
 		
 		try {
 			db.execSQL(updateSQL, new Object[] {
 					read,
 					favorite,
 					commic.getGuid()
 			});
 		} catch (SQLException e) {
 			exception = e;
 		}
 		
 		return exception == null;
 	}
 	
 	public void saveCommics(ArrayList<Item> items) {
 		SQLiteDatabase db = getWritableDatabase();
 		
 		Exception e = null;
 		long time;
 		
 		String insertSQL = "INSERT OR REPLACE INTO commics (guid, title, description, content, link, pubDate, isFavorite, isRead) VALUES (?, ?, ?, ?, ?, ?, (SELECT isFavorite FROM commics WHERE guid = ?), (SELECT isRead FROM commics WHERE guid = ?))";
 		
 		db.execSQL("BEGIN");
 		
 		for (Item item : items) {
 			time = item.getDate() == null ? 0 : item.getDate().getTime();
 			try {
 				db.execSQL(insertSQL, new Object[] {
 					item.getGuid(),
 					item.getTitle(),
 					item.getDescription(),
 					item.getContent(),
 					item.getLink(),
 					time,
 					item.getGuid(),
 					item.getGuid()
 				});
 			} catch (SQLException insertException) {
 				e = insertException;
 			}
 		}
 		
 		db.execSQL("COMMIT");
 		
 		if (e != null)
 			Log.e("VDP-MANAGER", e.getMessage());
 	}
 	
 	public void saveTweets(ArrayList<Tweet> tweets) {
 	}
 }
