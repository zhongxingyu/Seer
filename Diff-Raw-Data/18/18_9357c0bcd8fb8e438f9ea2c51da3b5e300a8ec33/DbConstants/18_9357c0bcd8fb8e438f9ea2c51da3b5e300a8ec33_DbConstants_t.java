 package net.erickelly.huskyhunters.data;
 
 import android.provider.BaseColumns;
 
 /**
  * 
  * @author francis
  *
  */
 public final class DbConstants {
 	public static final String DB_NAME = "cluedata";
 	public static final int DB_VERSION = 1;
 	
 	public static final String ROWID = "_id";
 	public static final String CLUEID = "clueid";
 	
 	
 	public static final String TIME = "time";
 	
 	// server data table
 	public static final String SERVER_TABLE = "server_table";
 	public static final String DOWNLOADED_TABLE = "download_table";
 	public static final String SERVER_TEXT = "cluetext";
 	public static final String SERVER_ANS = "clueanswer";
 	public static final String SERVER_POINTS = "points";
 	public static final String SERVER_PICS_REQD = "picsreqd";
 	public static final String SERVER_PICS_ON_SERVER = "picsonserver";
 	
 	
 	// device data table
 	public static final String DEVICE_TABLE = "device_table";
 	public static final String DEVICE_PICS_ON_DEVICE = "picsondevice";
 	
 	// photo table
 	public static final String PHOTO_TABLE = "photo_table";
 	public static final String PHOTO_PICURI = "picuri";
 	public static final String PHOTO_CRTIME = "crtime";
 	
 	
 	
 	
 	// table creation strings
 	public static final String SQL_DEVICE_TABLE_CREATION =
 			"CREATE TABLE " + DEVICE_TABLE + " (" + ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
 			+ CLUEID + " TEXT NOT NULL UNIQUE, " + DEVICE_PICS_ON_DEVICE + " INTEGER NOT NULL);";
 	public static final String SQL_PHOTO_TABLE_CREATION = 
 			"CREATE TABLE " + PHOTO_TABLE + " (" + ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
 			+ PHOTO_PICURI + " TEXT NOT NULL, " + PHOTO_CRTIME + " INTEGER NOT NULL);";
 	
 	// table creation string for server data - is method that takes name of table
 	// to allow for temporary table
 	/**
 	 * Table creation string for server data.  Takes name of table to provide string
 	 * for creation of temporary table for storing downloaded data.
 	 * @param tableName
 	 * @return a SQL command to create a table of the given name.
 	 */
 	public static final String SQL_SERVER_TABLE_CREATION(String tableName)
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append("CREATE TABLE ");
 		sb.append(tableName);
 		sb.append(" (");
 		sb.append(BaseColumns._ID);
 		sb.append(" integer primary key autoincrement, ");
 		sb.append(CLUEID);
 		sb.append(" text not null, ");
 		sb.append(SERVER_TEXT);
 		sb.append(" text not null, ");
 		sb.append(SERVER_ANS);
 		sb.append(" text not null, ");
 		sb.append(SERVER_POINTS);
 		sb.append(" integer not null, ");
 		sb.append(SERVER_PICS_REQD);
 		sb.append(" integer not null, ");
 		sb.append(SERVER_PICS_ON_SERVER);
 		sb.append(" integer not null);");
 		return sb.toString();
 	}
 	
 	// Drop server table
	public static final String SQL_DROP_SERVER_TABLE = "DROP TABLE IF EXISTS " + SERVER_TABLE;
 	
 	// Rename downloaded table to server table
	public static final String SQL_RENAME_DOWNLOADED_TO_SERVER = "ALTER TABLE " + DOWNLOADED_TABLE
 			+ " RENAME TO " + SERVER_TABLE;
 	
 	// Clear database, drop all tables
	public static final String SQL_DROP_ALL_TABLES = SQL_DROP_SERVER_TABLE
 			+ "; DROP TABLE IF EXISTS " + DEVICE_TABLE
 			+ "; DROP TABLE IF EXISTS " + DOWNLOADED_TABLE
 			+ "; DROP TABLE IF EXISTS " + PHOTO_TABLE + ";";
 	
 	// Query command 
	public static final String SQL_SELECT = "" +
 			"SELECT ? FROM " + DbConstants.SERVER_TABLE + "LEFT JOIN " +
 			"(SELECT COUNT(" + DbConstants.CLUEID + ") AS device_pics FROM " + 
 			DbConstants.PHOTO_TABLE + " GROUP BY " + DbConstants.CLUEID + ") " +
			"AS device_table ON " + DbConstants.CLUEID + " = " + DbConstants.CLUEID;
	public static final String SQL_WHERE = " WHERE ? ";
	public static final String SQL_GROUP = " GROUP BY ?";	
 }
