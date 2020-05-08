 package com.bytopia.oboobs.db;
 
 import static com.bytopia.oboobs.db.BoobsDbOpenHelper.AUTHOR;
 import static com.bytopia.oboobs.db.BoobsDbOpenHelper.FAVORITES_TABLE_NAME;
 import static com.bytopia.oboobs.db.BoobsDbOpenHelper.FILE_NAME;
 import static com.bytopia.oboobs.db.BoobsDbOpenHelper.ID;
 import static com.bytopia.oboobs.db.BoobsDbOpenHelper.MODEL;
 import static com.bytopia.oboobs.db.BoobsDbOpenHelper.PREVIEW;
 import static com.bytopia.oboobs.db.BoobsDbOpenHelper.RANK;
 
 import java.util.ArrayList;
 import java.util.List;
 
import android.annotation.TargetApi;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
 
 import com.bytopia.oboobs.model.Boobs;
 
 public class DbUtils {
 	public static BoobsDbOpenHelper helper;
 
	@TargetApi(8)
 	public static boolean addFavorite(Boobs boobs, String savedFileName) {
 
 		SQLiteDatabase database = helper.getWritableDatabase();
 		ContentValues values = createValues(boobs, savedFileName);
 
		long res;
		if (Build.VERSION.SDK_INT > 7) {
			res = database.insertWithOnConflict(FAVORITES_TABLE_NAME,
					null, values, SQLiteDatabase.CONFLICT_REPLACE);
		} else {
			res = database.insert(FAVORITES_TABLE_NAME,
					null, values);
		}
 
 		database.close();
 
 		return res != -1;
 	}
 
 	public static boolean removeFromFavorites(int id) {
 
 		SQLiteDatabase database = helper.getWritableDatabase();
 		int res = database.delete(FAVORITES_TABLE_NAME, ID + "=?",
 				new String[] { "" + id });
 		return res != 0;
 	}
 
 	private static ContentValues createValues(Boobs boobs, String savedFileName) {
 		ContentValues values = new ContentValues();
 
 		values.put(ID, boobs.id);
 		values.put(MODEL, boobs.model);
 		values.put(AUTHOR, boobs.author);
 		values.put(PREVIEW, boobs.preview);
 		values.put(RANK, boobs.rank);
 		values.put(FILE_NAME, savedFileName);
 
 		return values;
 	}
 
 	public static List<Boobs> getFavoriteBoobs() {
 		SQLiteDatabase database = helper.getReadableDatabase();
 
 		Cursor cursor = database.query(FAVORITES_TABLE_NAME, null, null, null,
 				null, null, null);
 
 		List<Boobs> boobs = new ArrayList<Boobs>();
 
 		while (cursor.moveToNext()) {
 			Boobs b = new Boobs();
 			b.model = cursor.getString(cursor.getColumnIndex(MODEL));
 			b.id = cursor.getInt(cursor.getColumnIndex(ID));
 			b.rank = cursor.getInt(cursor.getColumnIndex(RANK));
 			b.author = cursor.getString(cursor.getColumnIndex(AUTHOR));
 			b.filePath = cursor.getString(cursor.getColumnIndex(FILE_NAME));
 			b.hasFile = true;
 			b.preview = cursor.getString(cursor.getColumnIndex(PREVIEW));
 
 			boobs.add(b);
 		}
 
 		cursor.close();
 
 		database.close();
 
 		return boobs;
 	}
 
 }
