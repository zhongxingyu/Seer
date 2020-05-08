 package UltimateFrisbee.Stats;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import android.widget.Toast;
 
 
 class frisbeeOpenHelper extends SQLiteOpenHelper {
 	private static final String DB_DEBUG_TAG = "Debug Ultimate Frizbee Stats DB";
 	private static final String DATABASE_NAME = "frisbee.db";
 	private static final int DATABASE_VERSION = 1;
 	public static final String TOURNAMENT_TN = "tournament";
 	public static final String ROSTER_TN = "roster";
 	public static final String POINT_TN = "point";
 	public static final String GAME_TN = "game";
 	public static final String OPPONENTS_TN = "opponents";
 	
 	private Context context;
 	frisbeeOpenHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		this.context = context;
 	}
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		//example db exec 	
 		//db.execSQL("CREATE TABLE " + TABLE_NAME + "(id INTEGER PRIMARY KEY, name TEXT)");
 		db.execSQL("CREATE TABLE " + TOURNAMENT_TN + "(year INTEGER, name TEXT, date DATE, PRIMARY KEY (year, name))");
		db.execSQL("CREATE TABLE " + ROSTER_TN + "(name TEXT PRIMARY KEY, number INTEGER, time_added TIMESTAMP, games_played INTEGER, tournaments_played INTEGER)");
 		db.execSQL("CREATE TABLE " + POINT_TN + "(id TIMESTAMP PRIMARY KEY, for TEXT(1))");
 		db.execSQL("CREATE TABLE " + GAME_TN + "(time_started TIMESTAMP PRIMARY KEY, time_ended TIMESTAMP, opponent TEXT, our_score INTEGER, thier_score INTEGER, tournament TEXT)");
 		db.execSQL("CREATE TABLE " + OPPONENTS_TN + "(name TEXT PRIMARY KEY, games_won_against INTEGER, games_lost_against INTEGER)");
 	}
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.wtf(DB_DEBUG_TAG, "THIS SHOULD NOT HAPPEN WE NEED TO FIGURE OUT UPGRADEING BEFORE DOING IT");
 		Toast.makeText(context, "OH GOD SOMETHING BAD HAPPENED", Toast.LENGTH_LONG);
 	}
 }
