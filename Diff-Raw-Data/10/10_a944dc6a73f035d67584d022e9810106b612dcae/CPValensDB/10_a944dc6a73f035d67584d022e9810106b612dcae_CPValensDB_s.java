 package ntnu.stud.valens.contentprovider;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class CPValensDB extends SQLiteOpenHelper{
 	private static final String DATABASE_NAME="valens.db";
 	private static final int DATABASE_VERSION=2;
 
 	public static final String COMMA = ", ";
 	public static final String START_PAR = " (";
 	public static final String END_PAR = ") ";
 	public static final String DOT = ".";
 	public static final String EQUAL = "=";
 	
 	public CPValensDB(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		final String CREATE_TABLE_RAW_STEPS = "CREATE TABLE "
 				+ DBSchema.RawSteps.TABLE_NAME + START_PAR
 				+ DBSchema.RawSteps.COLUMN_NAME_TIMESTAMP
 				+ " INTEGER PRIMARY KEY,"
 				+ DBSchema.RawSteps.COLUMN_NAME_SOURCE + END_PAR;
 		final String CREATE_TABLE_TESTS = "CREATE TABLE "
 				+ DBSchema.Tests.TABLE_NAME + START_PAR
 				+ DBSchema.Tests.COLUMN_NAME_TIMESTAMP
 				+ " INTEGER PRIMARY KEY,"
 				+ DBSchema.Tests.COLUMN_NAME_SCORE + COMMA
 				+ DBSchema.Tests.COLUMN_NAME_TYPE_CODE_KEY +END_PAR;
 		final String CREATE_TABLE_TEST_TYPES = "CREATE TABLE "
 				+ DBSchema.TestTypes.TABLE_NAME + START_PAR
 				+ DBSchema.TestTypes.COLUMN_NAME_CODE_KEY
 				+ " INTEGER PRIMARY KEY,"
 				+ DBSchema.TestTypes.COLUMN_NAME_DESCRIPTION + COMMA
 				+ DBSchema.TestTypes.COLUMN_NAME_NAME + COMMA 
 				+ DBSchema.TestTypes.COLUMN_NAME_SCORE_DESCRIPTION + END_PAR;
 		final String CREATE_TABLE_STEPS = "CREATE TABLE "
 				+ DBSchema.Steps.TABLE_NAME + START_PAR
 				+ DBSchema.Steps.COLUMN_NAME_TIMESTAMP
 				+ " INTEGER PRIMARY KEY " + END_PAR;
 		final String CREATE_TABLE_GAITS = "CREATE TABLE "
 				+ DBSchema.Gaits.TABLE_NAME + START_PAR
 				+ DBSchema.Gaits.COLUMN_NAME_SPEED + COMMA
 				+ DBSchema.Gaits.COLUMN_NAME_VARIABILITY + COMMA
 				+ DBSchema.Gaits.COLUMN_NAME_START + COMMA 
 				+ DBSchema.Gaits.COLUMN_NAME_STOP + END_PAR;
 		db.execSQL(CREATE_TABLE_RAW_STEPS);
 		db.execSQL(CREATE_TABLE_TEST_TYPES);
 		db.execSQL(CREATE_TABLE_TESTS);
 		db.execSQL(CREATE_TABLE_STEPS);
 		db.execSQL(CREATE_TABLE_GAITS);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 		reset(db);
 	}
 	
 	public void reset(SQLiteDatabase db) {
 		try {
 			db.execSQL("DROP TABLE " + DBSchema.RawSteps.TABLE_NAME);
 		} catch (SQLiteException e) {
 			// Do nothing...
 		}
 		try {
 			db.execSQL("DROP TABLE " + DBSchema.Tests.TABLE_NAME);
 		} catch (SQLiteException e) {
 			// Do nothing...
 		}
 		try {
 			db.execSQL("DROP TABLE " + DBSchema.TestTypes.TABLE_NAME);
 		} catch (SQLiteException e) {
 			// Do nothing...
 		}
 		onCreate(db);
 	}
 
 }
