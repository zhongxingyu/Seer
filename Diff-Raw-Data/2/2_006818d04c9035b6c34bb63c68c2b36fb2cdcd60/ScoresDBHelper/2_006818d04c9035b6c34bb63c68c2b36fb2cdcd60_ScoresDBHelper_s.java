 package edu.uwg.jamestwyford.connect64.db;
 
 import edu.uwg.jamestwyford.connect64.db.ScoresContract.Scores;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class ScoresDBHelper extends SQLiteOpenHelper {
 	private static final String DATABASE_NAME = "scores.db";
 	private static final int DATABASE_VERSION = 1;
 	
 	private static final String	COMMA_SEP =	", ";
 	private static final String	TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
 	private static final String	SQL_CREATE_DATABASE	= "CREATE TABLE "
 	+ Scores.SCORES_TABLE_NAME + " (" + Scores.ID
 	+ " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP
 	+ Scores.PLAYER + TEXT_TYPE + COMMA_SEP
 	+ Scores.PUZZLE + INTEGER_TYPE + COMMA_SEP
 	+ Scores.COMPLETION_TIME + INTEGER_TYPE + ");"
 	;
 
 	public ScoresDBHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(SQL_CREATE_DATABASE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 	}
 }
