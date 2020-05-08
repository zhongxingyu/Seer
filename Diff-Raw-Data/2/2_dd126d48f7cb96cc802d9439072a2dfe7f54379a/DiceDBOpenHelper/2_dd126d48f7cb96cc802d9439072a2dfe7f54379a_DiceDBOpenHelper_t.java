 package com.group5.diceroller;
 
 import java.util.ArrayList;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.content.Context;
 import android.content.ContentValues;
 import android.database.Cursor;
 
 public class DiceDBOpenHelper extends SQLiteOpenHelper {
 	
 	private static final String DATABASE_NAME = "DiceRoller";
 	private static final int DATABASE_VERSION = 2;
 
 	public DiceDBOpenHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		
 		String Create_String = "CREATE TABLE dice_table (set_id int, " +
 														"faces int, " +
 				                                        "count int)";
 		
 		db.execSQL(Create_String);
 		
 		Create_String = "CREATE TABLE set_table (set_id int, " +
 												"name String)";
 		
 		db.execSQL(Create_String);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		
 		db.execSQL("DROP TABLE IF EXISTS set_table");
 		onCreate(db);
 	}
 	
 	public void saveSet(DiceSet set) {
 		
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put("set_id", set.id);
 		values.put("name", set.name);
 		
 		if (set.id == DiceSet.NOT_SAVED) {		
 			
 			Cursor cursor = db.rawQuery("SELECT set_id FROM set_table ORDER BY set_id DESC LIMIT 1", null);
 			set.id = (cursor.getInt(0) + 1);
 			db.insert("set_table", null, values);
 		
 		}
 		
 		else if (set.id != DiceSet.NOT_SAVED) {
 			
 			db.update("set_table", values, "set_id = ?", new String[] { String.valueOf(set.id)} );
 			
 		}
 		
 		db.close();
 	}
 	
 	public void saveDice(Dice die) {
 		
 		boolean exists = false;
 		
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		Cursor cursor = db.query ("dice_table", new String[] {"set_id", "faces", "count"}, "set_id = ?", new String[] {String.valueOf(die.set_id)}, null, null, null, null);
 		if (cursor != null) exists = true;
 		
 		ContentValues values = new ContentValues();
 		values.put("set_id", die.set_id);
 		values.put("faces", die.faces);
 		values.put("count", die.count);
 		
 		if (!exists) {
 			
 			db.insert("dice_table", null, values);
 		}
 		
 		if (exists) {
 			
 			db.update("dice_table", values, "set_id = ?", new String[] { String.valueOf(die.set_id) });
 		}
 		
 		db.close();
 	}
 	
 	public void deleteSet(DiceSet set) {
 		
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		db.delete("set_table", "set_id = ?", new String[] { String.valueOf(set.id) });
 		db.delete("dice_table", "set_id = ?", new String[] { String.valueOf(set.id) });
 		
 		db.close();
 	}
 	
 	public void deleteDice(Dice die) {
 		
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		db.delete("dice_table", "set_id = ? AND faces = ?", new String[] { String.valueOf(die.set_id), String.valueOf(die.faces) });
 		
 		db.close();
 	}
 	
 	public ArrayList<DiceSet> loadSets() {
 		
 		SQLiteDatabase db = this.getWritableDatabase();
 		ArrayList<DiceSet> setList = new ArrayList<DiceSet>();
 		
 		Cursor cursor = db.rawQuery("SELECT set_id, name FROM set_table", null);
 		
 		if (cursor.moveToFirst()) {
 			do {
 				
				DiceSet set = new DiceSet(cursor.getInt(0), cursor.getString(1), 0);
 				setList.add(set);
 				
 			} while (cursor.moveToNext());
 		}
 		
 		db.close();
 		return setList;
 	}
 	
 	public ArrayList<Dice> loadDice(int set_id) {
 		
 		SQLiteDatabase db = this.getWritableDatabase();
 		ArrayList<Dice> diceList = new ArrayList<Dice>();
 		
 		Cursor cursor = db.rawQuery("SELECT set_id, faces, count FROM dice_table WHERE set_id = ?", new String[] { String.valueOf(set_id) });
 		
 		if (cursor.moveToFirst()) {
 			do {
 				Dice newDie = new Dice ();
 				
 				newDie.set_id = cursor.getInt(0);
 				newDie.faces = cursor.getInt(1);
 				newDie.count = cursor.getInt(2);
 				
 				diceList.add(newDie);
 			} while (cursor.moveToNext());
 		}
 		
 		db.close();
 		return diceList;
 	}
 }
