 package com.joy.launcher2.download;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import com.joy.launcher2.LauncherApplication;
 /**
  * 数据库相关操作的类
  * 
  * @author wanghao
  */
 public class DownLoadDBHelper {
 	
 	private final boolean isDebug = false;
 	private final String TAG = "DownLoadDBHelper";
 	
 	// 数据库名
 	private static final String DATABASE_NAME = "download44.db";
 
 	//数据表名
 	private static final String DATABASE_TABLE = "downinfo111";
 
 	//数据库版本
 	private static final int DATABASE_VERSION = 1;
 
 	//id 指定对于的apk
 	private static final String ID = "id";
 
 	//文件名
 	private static final String NAME = "name";
 
 	//本地名
 	private static final String LOCAL_NAME = "local_name";
 	
 	//apk下载地址
 	private static final String URL = "url";
 	
 	//文件大小
 	private static final String FILE_SIZE = "file_size";
 
 	//文件已下载大小
 	private static final String COMPLETE_SIZE = "complete_size";
  
 	private final Context context;
  
 	private DatabaseHelper mDBHelper;
  
 	private SQLiteDatabase db;
 	
 	static DownLoadDBHelper dbHelper;
 	public DownLoadDBHelper(Context ctx) {
 		context = ctx;
 		mDBHelper = new DatabaseHelper(context);
 	}
 
 	static public DownLoadDBHelper getInstances() {
 		
 		if (dbHelper == null) {
 			dbHelper = new DownLoadDBHelper(LauncherApplication.mContext);
 		}
 		return dbHelper;
 	}
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 		public DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			// TODO Auto-generated method stub
 
 		 String DATABASE_CREATE = "create table "+DATABASE_TABLE+" (_id INTEGER PRIMARY KEY, "
 					+ "id INTEGER, "
 					+ "name TEXT, "
 					+ "local_name TEXT, "
 					+ "url TEXT, "
 					+ "file_size INTEGER, "
 					+ "complete_size INTEGER "
 					+");";
 		 
 			db.execSQL(DATABASE_CREATE);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			// TODO Auto-generated method stub
 			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
 			onCreate(db);
 		}
 	}
 
 	/**
 	 * 打开数据库
 	 * 
 	 * @return
 	 * @throws SQLException
 	 */
 	public synchronized SQLiteDatabase open() throws SQLException {
 		
 		db = mDBHelper.getWritableDatabase();
 
 		return db;
 	}
 
 	/**
 	 * 关闭数据库
 	 */
 	public synchronized void close() throws SQLException {
 		if(isDebug) Log.i(TAG, "-----db = "+db);
 		if(isDebug) Log.i(TAG, "-----mDBHelper = "+mDBHelper);
 		if(db != null) db.close();
 	}
 
 	/**
 	 * 向数据库中插入数据
 	 */
 	public synchronized void insert(DownloadInfo info) {
 
 		open();
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(ID, info.getId());
 		initialValues.put(NAME, info.getFilename());
 		initialValues.put(LOCAL_NAME, info.getLocalname());
 		initialValues.put(URL, info.getUrl());
 		initialValues.put(FILE_SIZE, info.getFilesize());
 		initialValues.put(COMPLETE_SIZE, info.getCompletesize());
 		
 		db.insert(DATABASE_TABLE, null, initialValues);
 		
 		close();
 	}
 	/**
 	 * 删除数据,根据指定id删除
 	 */
 	public synchronized void delete(DownloadInfo info) {
 		delete(info.getId());
 	}
 	/**
 	 * 删除数据,根据指定id删除
 	 */
 	public synchronized void delete(int id) {
 
 		 open();
 		 db.delete(DATABASE_TABLE, ID + "=" + id, null);
 		 close();
 	}
 
 	/**
 	 * 更改数据，根据指定id更改
 	 */
 	public synchronized void update(DownloadInfo info) {
 		open();
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(ID, info.getId());
 		initialValues.put(NAME, info.getFilename());
 		initialValues.put(LOCAL_NAME, info.getLocalname());
 		initialValues.put(URL, info.getUrl());
 		initialValues.put(FILE_SIZE, info.getFilesize());
 		initialValues.put(COMPLETE_SIZE, info.getCompletesize());
 		
 		db.update(DATABASE_TABLE, initialValues, ID + "="+ info.getId(), null);
 		close();
 	}
 
 	public synchronized Cursor getAll() {
 
 		Cursor cur = db.query(DATABASE_TABLE, null, null, null, null, null,null);
 		return cur;
 	}
 
 	/**
 	 * 根据当前id获取下载信息
 	 * @param id
 	 * @return
 	 */
 	public synchronized DownloadInfo get(int id){
 		DownloadInfo info = null;
 		open();
 		Cursor cur = db.query(true, DATABASE_TABLE, new String[] { ID,
 				NAME, LOCAL_NAME, URL , FILE_SIZE, COMPLETE_SIZE },
 
 		ID + "=" + id, null, null, null, null, null);
 		if (cur != null) {
 			cur.moveToFirst();
 			if (cur.getCount() <=0 ) {
 				return null;
 			}
 			info = new DownloadInfo();
 			info.setId(cur.getInt(0));
 			info.setFilename(cur.getString(1));
 			info.setLocalname(cur.getString(2));
 			info.setUrl(cur.getString(3));
 			info.setFilesize(cur.getInt(4));
 			info.setCompletesize(cur.getInt(5));
 			cur.close();
 			if(isDebug) Log.i(TAG, "-----dbHelper---getCount = "+cur.getCount());
 		}
		close();
 
 		return info;
 	}
 
 }
