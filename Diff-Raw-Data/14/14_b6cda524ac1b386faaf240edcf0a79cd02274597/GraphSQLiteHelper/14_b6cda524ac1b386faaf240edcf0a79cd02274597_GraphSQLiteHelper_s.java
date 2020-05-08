 package uk.ks.simple.graphviewers.databases;
 
 import android.database.sqlite.SQLiteOpenHelper;
 
 /**
  * Created by root on 5/18/13.
  */
 public class GraphSQLiteHelper extends SQLiteOpenHelper {
 
 	public static final String TABLE_GRAPHICS = "graphics";
 	public static final String TABLE_POINTS_TO_GRAPHIC = "point_to_graphics";
 	public static final String TABLE_POINTS = "points";
 
 	public static final String ID = "id";
 	public static final String GRAPHIC_ID = "graphicId";
 	public static final String POINT_ID = "pointId";
 	public static final String COLOR = "color";
 	public static final String X_COORDINATE = "xCoordinate";
 	public static final String Y_COORDINATE = "yCoordinate";
 
 	private static final String DATABASE_NAME = "simple_graph_viewers.db";
 	private static final int DATABASE_VERSION = 1;
 
 	// Database creation sql statement
 	private static final String CREATE_GRAPHICS_TABLE = "create table "
 			+ TABLE_GRAPHICS + "("
 			+ ID + " integer primary key autoincrement, "
 			+ COLOR + " integer not null);";
 
 	private static final String CREATE_POINTS_TO_GRAPHIC_TABLE = "create table "
 			+ TABLE_POINTS_TO_GRAPHIC + "("
 			+ GRAPHIC_ID + " integer primary key, "
 			+ POINT_ID + " integer primary key);";
 
 	private static final String CREATE_POINTS_TABLE = "create table "
 			+ TABLE_POINTS + "("
 			+ ID + " integer primary key autoincrement, "
 			+ X_COORDINATE + " integer not null, "
 			+ Y_COORDINATE + " integer not null);";
 
	public MySQLiteHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase database) {
 		database.execSQL(CREATE_GRAPHICS_TABLE);
 		database.execSQL(CREATE_POINTS_TO_GRAPHIC_TABLE);
 		database.execSQL(CREATE_POINTS_TABLE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRAPHICS);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_POINTS_TO_GRAPHIC);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_POINTS);
 		onCreate(db);
 	}
 }
