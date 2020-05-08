 package ca.charland.db;
 
import android.annotation.SuppressLint;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * This class is the lowest level class containing specific SQL queries and connections.
  * 
  * @author mcharland
  */
 public class DatabaseConnection extends SQLiteOpenHelper {
 
 	private static final int DATABASE_VERSION = 1;
 
 	public static final String DATABASE_NAME = "application.db";
 
 	private final String databaseName;
 	
 	private final String tableName;
 
 	private final String createTableSQL;
	
	public DatabaseConnection(Context context) {
		this(context, DATABASE_NAME, DataTable.TABLE_NAME, DataTable.CREATE_DATABASE_TABLE);
	}
 
 	public DatabaseConnection(Context context, String database, String table, String createTableSQL) {
 		super(context, database, null, DATABASE_VERSION);
 		this.databaseName = database;
 		this.tableName = table;
 		this.createTableSQL = createTableSQL;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase database) {
 		createTables(database);
 	}
 
 	private void createTables(SQLiteDatabase database) {
 		database.execSQL(createTableSQL);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(DatabaseConnection.class.getName(), getMessage(oldVersion, newVersion));
 		dropTables(db);
 		onCreate(db);
 	}
 
 	private void dropTables(SQLiteDatabase db) {
 		db.execSQL("DROP TABLE IF EXISTS" + getTableName());
 	}
 
 	private String getMessage(int oldVersion, int newVersion) {
 		StringBuilder msg = new StringBuilder();
 		msg.append("Upgrading database from version ");
 		msg.append(oldVersion);
 		msg.append(" to ");
 		msg.append(newVersion);
 		msg.append(", which will destroy all old data");
 		return msg.toString();
 	}
 	
	@SuppressLint("Override")
 	public String getDatabaseName() {
 		return databaseName;
 	}
 
 	public String getTableName() {
 		return tableName;
 	}
 	
 	public String getCreateTableSQL() {
 		return createTableSQL;
 	}
 
 }
