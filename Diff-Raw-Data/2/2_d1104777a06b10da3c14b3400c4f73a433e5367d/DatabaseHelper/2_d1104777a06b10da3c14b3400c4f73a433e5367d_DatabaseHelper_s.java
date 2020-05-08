 package net.erickelly.score;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import static android.provider.BaseColumns._ID;
 import static net.erickelly.score.Constants.TABLE_NAME;
 import static net.erickelly.score.Constants.NAME;
 import static net.erickelly.score.Constants.SCORE;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 	
 	private static final String DATABASE_NAME = "players.db";
 	private static final int DATABASE_VERSION = 1;
 	
 	public DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + _ID
 				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME
				+ " STRING," + SCORE + " INTEGER);");
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
 		onCreate(db);
 	}
 
 }
