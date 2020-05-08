 package com.group5.diceroller;
 
 import java.util.ArrayList;
 
 // For logging factory
 import android.database.sqlite.SQLiteCursorDriver;
 import android.database.sqlite.SQLiteQuery;
 import android.database.sqlite.SQLiteCursor;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.content.Context;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.util.Log;
 
 /**
  * Provides an interface to open the application database and create it if
  * necissary. Also provides functions for saving, loading, and deleting Dice
  * and DiceSets to the database.
  *
  * @Author Padraic Baker
  */
 public class DiceDBOpenHelper extends SQLiteOpenHelper {
 
 	private static final String DATABASE_NAME = "DiceRoller";
	private static final int DATABASE_VERSION = 2;
     public static final String kTag = "DiceDBHelper";
 
     private static DiceDBOpenHelper db_helper = null;
 
 	private DiceDBOpenHelper(Context context) {
 		super(context, DATABASE_NAME, new LoggingCursorFactory(), DATABASE_VERSION);
 	}
 
     public static void initialize(Context context) {
         db_helper = new DiceDBOpenHelper(context);
     }
 
     /**
      * Gets the instance of the database. It returns null if initialize() is
      * not called sometime before. It should be the responsibility of the main
      * activity to call initialize().
      */
     public static DiceDBOpenHelper getDB() {
         return db_helper;
     }
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 
 		String Create_String = "CREATE TABLE dice_table (set_id int, " +
 														"faces int, " +
 				                                        "count int)";
 
 		db.execSQL(Create_String);

 		Create_String = "CREATE TABLE set_table (set_id int, " +
 												"name String," +
 												"modifier int)";

 		db.execSQL(Create_String);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
 		db.execSQL("DROP TABLE IF EXISTS set_table");
 		db.execSQL("DROP TABLE IF EXISTS dice_table");
 		onCreate(db);
 	}
 
 	public void saveSet(DiceSet set) {
 		Log.i(kTag, "Saving DiceSet: "+set.description());
 		SQLiteDatabase db = this.getWritableDatabase();
 
         ContentValues values = new ContentValues();
         values.put("name", set.name);
 
 		if (set.id == DiceSet.NOT_SAVED) {
 			Cursor cursor = db.rawQuery("SELECT set_id FROM set_table ORDER BY set_id DESC LIMIT 1", null);
             if (!cursor.moveToFirst())
                 set.id = 0;
             else
                 set.id = (cursor.getInt(0) + 1);
 
             values.put("set_id", set.id);
            values.put("modifier", set.modifier);
            
 			db.insert("set_table", null, values);
 
 		} else {
             values.put("set_id", set.id);
            values.put("modifier", set.modifier);
 
             // delete all old dice; easier than finding whether dice have been removed
             db.delete("dice_table", "set_id = ?", new String[] { String.valueOf(set.id) });
             db.update("set_table", values, "set_id = ?", new String[] { String.valueOf(set.id)} );
 		}
 
         for (Dice die : set) {
             die.set_id = set.id;
             saveDice(die, db);
         }
 
 		db.close();
 	}
 
     /**
      * Saves a die to the DB. Note, does not check if a given die of (id,faces)
      * exists in the DB first, so try to always insert unique values.
      */
 	private void saveDice(Dice die, SQLiteDatabase db) {
 
 		ContentValues values = new ContentValues();
 		values.put("set_id", die.set_id);
 		values.put("faces", die.faces);
 		values.put("count", die.count);
 
         db.insert("dice_table", null, values);
 	}
 
 	public void deleteSet(DiceSet set) {
 
 		SQLiteDatabase db = this.getWritableDatabase();
 
         Log.i(kTag, "deleting set with id: "+set.id);
 
 		db.delete("set_table", "set_id = ?", new String[] { String.valueOf(set.id) });
 		db.delete("dice_table", "set_id = ?", new String[] { String.valueOf(set.id) });
 
 		db.close();
 	}
 
 	public ArrayList<DiceSet> loadSets()
     {
 		SQLiteDatabase db = this.getWritableDatabase();
 		ArrayList<DiceSet> setList = new ArrayList<DiceSet>();
 
 		Cursor cursor = db.rawQuery("SELECT set_id, name, modifier FROM set_table", null);
 
 		if (cursor.moveToFirst()) {
 			do {
                 int id = cursor.getInt(0);
                 String name = cursor.getString(1);
                int modifier = 0;
 
 				DiceSet set = new DiceSet(id, name, modifier);
                 loadDice(set, db);
 
 				setList.add(set);
 			} while (cursor.moveToNext());
 		}
 
 		db.close();
 		return setList;
 	}
 
     /**
      * Loads the dice associated with the set id of the given set into the
      * given set.
      *
      * @param set The set to load into.
      * @param db The database to load from
      */
 	private void loadDice(DiceSet set, SQLiteDatabase db) {
 
 		Cursor cursor = db.rawQuery(
             "SELECT faces, count FROM dice_table WHERE set_id = ?",
             new String[] { String.valueOf(set.id) });
 
 		if (cursor.moveToFirst()) {
 			do {
 				int faces = cursor.getInt(0);
 				int count = cursor.getInt(1);
 
 				set.add(new Dice(faces, count, set.id));
 			} while (cursor.moveToNext());
 		}
 	}
 
     static class LoggingCursorFactory implements SQLiteDatabase.CursorFactory {
         public Cursor newCursor(SQLiteDatabase db,
             SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
 
             Log.i(kTag, query.toString());
             return new SQLiteCursor(db, driver, editTable, query);
         }
     }
 }
