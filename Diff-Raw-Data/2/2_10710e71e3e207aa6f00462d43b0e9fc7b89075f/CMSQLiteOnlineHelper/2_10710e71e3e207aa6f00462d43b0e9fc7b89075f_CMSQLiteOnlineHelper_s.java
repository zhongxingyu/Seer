 package com.softmo.smssafe.dbengine.provider;
 
 import com.softmo.smssafe.dbengine.TTypFolder;
 import com.softmo.smssafe.dbengine.TTypIsNew;
 import com.softmo.smssafe.dbengine.IMDbQuerySetting.TTypSetting;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 
 public class CMSQLiteOnlineHelper extends SQLiteOpenHelper implements BaseColumns {
 	
 	public static final String DB_NAME = "soft-mo.db";
 	public static final int DB_VERSION = 2;
 	
 	public static final String TABLE_SETTING = "M__SETTING";
 	public static final String SETTING_VAL = "VAL";
 	
 	public static final String TABLE_SMS = "M__SMS";
 	public static final String SMS_INDEX_HASHDATE = "idxHASH";
 	public static final String SMS_INDEX_SMSID = "idxSMSID";
 	
 	public static final String SMS_DIRECTION = "DIRECTION";
 	public static final String SMS_FOLDER = "FOLDER";
 	public static final String SMS_ISNEW = "ISNEW";
 	public static final String SMS_HASH = "HASH";
 	public static final String SMS_PHONE = "PHONE";
 	public static final String SMS_TEXT = "TXT";
 	public static final String SMS_DATE = "DAT";
 	public static final String SMS_STATUS = "STATUS";
 	public static final String SMS_SMSID = "SMSID";
 	
 	public static final String TABLE_SMSGROUP = "M__SMSGROUP";
 	public static final String SMSGROUP_INDEX_HASH = "idxGrHASH";
 	public static final String SMSGROUP_INDEX_MAXDATE = "idxGrDATE";
 	
 	public static final String SMSGROUP_HASH = "HASH";
 	public static final String SMSGROUP_PHONE = "PHONE";
 	public static final String SMSGROUP_COUNT = "count";
 	public static final String SMSGROUP_COUNTNEW = "countnew";
 	public static final String SMSGROUP_MAXDATE = "maxdat";	
 	
 	public static final String QUERY_SMSGROUP = "(select "+
 			SMS_HASH+" as "+SMSGROUP_HASH+","+
     		"(select "+SMS_PHONE+" "+
 				"from "+TABLE_SMS+" as B "+
 				"where B."+SMS_HASH+"=SMS."+SMS_HASH+" "+
 				"LIMIT 1"+
 				") as "+SMSGROUP_PHONE+","+
 			"count(*) as "+SMSGROUP_COUNT+","+
     		"(select "+
 				"count(*) "+
 				"from "+TABLE_SMS+" as A "+
 				"where A."+SMS_HASH+"=SMS."+SMS_HASH+" and A."+SMS_FOLDER+"="+TTypFolder.EInbox+" and A."+SMS_ISNEW+">="+TTypIsNew.ENew+
 				") as "+SMSGROUP_COUNTNEW+","+
 			"MAX("+SMS_DATE+") as "+SMSGROUP_MAXDATE+" "+
 			"from  "+TABLE_SMS+" as SMS "+
 			"group by "+SMS_HASH+
 			")";
 	
 
 	public CMSQLiteOnlineHelper(Context context) {
 		super(context, DB_NAME, null, DB_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		//1
 		db.execSQL("DROP TABLE IF EXISTS "+TABLE_SETTING);;
 		db.execSQL("CREATE TABLE "+TABLE_SETTING+"("+
 				_ID + " INTEGER PRIMARY KEY, "
 				+SETTING_VAL+" TEXT)");
 		
 		ContentValues values = new ContentValues();
         
         values.put(_ID, +TTypSetting.EDbVersion.getValue());
         values.put(SETTING_VAL, "1");
         db.insert(TABLE_SETTING, SETTING_VAL, values);
         
 		values.clear();
         values.put(_ID, +TTypSetting.EPassTimout.getValue());
         values.put(SETTING_VAL, "300");
         db.insert(TABLE_SETTING, SETTING_VAL, values);
         
 		values.clear();
         values.put(_ID, +TTypSetting.ERsaPub.getValue());
         values.put(SETTING_VAL, "");
         db.insert(TABLE_SETTING, SETTING_VAL, values);
         
 		values.clear();
         values.put(_ID, +TTypSetting.ERsaPriv.getValue());
         values.put(SETTING_VAL, "");
         db.insert(TABLE_SETTING, SETTING_VAL, values);
         
 		
 		db.execSQL("DROP TABLE IF EXISTS "+TABLE_SMS);
 		db.execSQL("CREATE TABLE "+TABLE_SMS+"("+
 				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
 				SMS_DIRECTION + " INTEGER," +
 				SMS_FOLDER + "  INTEGER," +
 				SMS_ISNEW + " INTEGER," +
 				SMS_HASH+ " varchar(100)," +
 				SMS_PHONE + " TEXT," +
 				SMS_TEXT + " TEXT," +
 				SMS_DATE + " DATETIME,"+
 				SMS_STATUS+ " INTEGER,"+
 				SMS_SMSID+ " INTEGER)");
 		
 		db.execSQL("DROP INDEX IF EXISTS "+SMS_INDEX_HASHDATE);
 		db.execSQL("CREATE INDEX "+SMS_INDEX_HASHDATE+" ON "+TABLE_SMS+"("+SMS_HASH+","+SMS_DATE+")");
 		
 		db.execSQL("DROP INDEX IF EXISTS "+SMS_INDEX_SMSID);
 		db.execSQL("CREATE INDEX "+SMS_INDEX_SMSID+" ON "+TABLE_SMS+"("+SMS_SMSID+")");
 		
 		db.execSQL("DROP TABLE IF EXISTS "+TABLE_SMSGROUP);
 		db.execSQL("CREATE TABLE "+TABLE_SMSGROUP+"("+
 				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
 				SMSGROUP_HASH + " varchar(100)," +
 				SMSGROUP_PHONE + " TEXT," +
 				SMSGROUP_COUNT + " INTEGER," +
 				SMSGROUP_COUNTNEW+ " INTEGER,"+
 				SMSGROUP_MAXDATE + " DATETIME)"
 				);
 		
 		db.execSQL("DROP INDEX IF EXISTS "+SMSGROUP_INDEX_HASH);
 		db.execSQL("CREATE INDEX "+SMSGROUP_INDEX_HASH+" ON "+TABLE_SMSGROUP+"("+SMSGROUP_HASH+")");
 		
 		db.execSQL("DROP INDEX IF EXISTS "+SMSGROUP_INDEX_MAXDATE);
 		db.execSQL("CREATE INDEX "+SMSGROUP_INDEX_MAXDATE+" ON "+TABLE_SMSGROUP+"("+SMSGROUP_MAXDATE+")");
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
 		if (oldVersion<2) {
 			db.execSQL("ALTER TABLE "+TABLE_SMS+" ADD "+SMS_SMSID+" INTEGER");
			db.execSQL("UPDATE TABLE "+TABLE_SMS+" SET "+SMS_SMSID+" = -1");
 			
 			db.execSQL("DROP INDEX IF EXISTS "+SMS_INDEX_SMSID);
 			db.execSQL("CREATE INDEX "+SMS_INDEX_SMSID+" ON "+TABLE_SMS+"("+SMS_SMSID+")");
 			
 			oldVersion = 2;
 		}
 	}
 
 }
