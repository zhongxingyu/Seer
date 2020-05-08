 package dk.illution.computer.info;
 
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 
 public class MainDatabase {
 	private static final  String DATABASE_NAME = "mydatabase.db";
 	private static final int DATABASE_VERSION = 1;
 	static final String TABLE_NAME = "credentials";
 	private static Context context;
 	static SQLiteDatabase db;
 
 	public MainDatabase (Context context) {
 		MainDatabase.context = context;
 		OpenHelper openHelper = new OpenHelper(MainDatabase.context);
 		MainDatabase.db = openHelper.getWritableDatabase();
 
 	}
 
 	public void insertCredential (String key, String value) {
		this.deleteCredential(key);
 		SQLiteStatement statement = this.db.compileStatement("insert into credentials (key, value) values (?, ?)");
 		statement.bindString(1, key);
 		statement.bindString(2, value);
 		statement.executeInsert();
 	}
 
 	public String selectCredential (String key) {
 		SQLiteStatement statement = this.db.compileStatement("select value from credentials where key=?");
 		statement.bindString(1, key);
 
 		return statement.simpleQueryForString();
 	}
 
 	public void deleteCredential (String key) {
 		SQLiteStatement statement = this.db.compileStatement("delete from credentials where key=?");
 		statement.bindString(1, key);
 
 		statement.execute();
 	}
 
 	private static class OpenHelper extends SQLiteOpenHelper {
 
 		OpenHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL("CREATE TABLE " + TABLE_NAME + " (key TEXT PRIMARY KEY, value TEXT)");
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
 			onCreate(db);
 		}
 	}
 
 
 
 }
