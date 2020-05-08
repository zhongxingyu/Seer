 /* memory
  * de.thm.ateam.memory.engine
  * SQLite.java
  * 04.06.2012
  *
  * by Frank Kevin Zey
  */
 package de.thm.ateam.memory.engine.type;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 /**
  * @author Frank Kevin Zey
  *
  */
 public class SQLite extends SQLiteOpenHelper {
 
 	private final static String DB_NAME = "memory.sqlite";
 	private final static int DB_VERSION = 1;
 	
 	public SQLite(Context ctx) {
 		super(ctx, DB_NAME, null, DB_VERSION);
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
 	 */
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE " + PlayerDB.TABLE_NAME + "("
 				+ PlayerDB.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ PlayerDB.NICK + " VARCHAR(42),"
 				+ PlayerDB.WIN + " INTEGER,"
 				+ PlayerDB.LOSE + " INTEGER,"
 				+ PlayerDB.DRAW + " INTEGER,"
 				+ PlayerDB.HIT + " INTEGER,"
 				+ PlayerDB.TURN + " INTEGER,"
 				+ "UNIQUE (" + PlayerDB.NICK + "));");
 		
 		db.execSQL("CREATE TABLE " + DeckDB.TABLE_NAME + "("
 				+ DeckDB.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ DeckDB.NAME + " VARCHAR(42),"
 				+ DeckDB.BACK_CARD + "BLOB,"
 				+ "UNIQUE (" + DeckDB.NAME + "));");
 		
 		db.execSQL("CREATE TABLE " + DeckDB.CARD_TABLE_NAME + "("
 				+ DeckDB.CARD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ DeckDB.CARD_DECK_ID + " INTEGER,"
				+ DeckDB.CARD_BLOB + " BLOB,"
				+ "UNIQUE (" + DeckDB.NAME + "));");
 	}
 
 	/* (non-Javadoc)
 	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
 	 */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		//db.needUpgrade(newVersion);
 	}
 
 }
