 package com.example.antispam.dao;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 import java.util.UUID;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dj
  * Date: 31.05.13
  * Time: 22:55
  * To change this template use File | Settings | File Templates.
  */
 public class DbHelper extends SQLiteOpenHelper {
 	public final static String TABLE_MESSAGES = "messages";
 	public final static String MESSAGES_ID = "_id";
 	public final static String MESSAGES_FROM = "`from`";
 	public final static String MESSAGES_DATETIME = "sentAt";
 	public final static String MESSAGES_BODY = "`body`";
 
 	private final static String DB_NAME = "db";
	private final static int DB_VERSION = 2;
 	private final static String DB_CREATE =
 			"CREATE TABLE `messages` (`_id` INTEGER PRIMARY KEY, `from` VARCHAR(20) NOT NULL, `sentAt` DATETIME, `body` TEXT);" +
 			"CREATE TABLE `meta` (`_id` VARCHAR(20) PRIMARY KEY, `value` VARCHAR(150));" +
			"INSERT INTO `meta` (`name`, `value`) VALUES ('deviceId', '%s');";
 	private final static String SQL_GET_META = "SELECT `value` FROM `meta` WHERE `_id`='?'";
 
 	public DbHelper(Context context) {
 		super(context, DB_NAME, null, DB_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase sqLiteDatabase) {
 		String query = String.format(DB_CREATE, UUID.randomUUID().toString());
 		sqLiteDatabase.execSQL(query);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i1, int i2) {
		if (i1 == 1 && i2 > 1) {
			sqLiteDatabase.execSQL("CREATE TABLE `senders` (`_id` VARCHAR(20) PRIMARY KEY, `spam` BOOL, `addedAt` TIMESTAMP);");
 		}
 	}
 
 	public String getMeta(String key) {
 		Cursor cur = this.getReadableDatabase().rawQuery(SQL_GET_META, new String[]{key});
 		return cur.getString(0);
 	}
 }
