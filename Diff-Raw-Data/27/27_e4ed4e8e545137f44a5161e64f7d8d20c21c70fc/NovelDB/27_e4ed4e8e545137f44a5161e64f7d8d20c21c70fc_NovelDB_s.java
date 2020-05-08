 package com.novel.subscription;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.novel.subscription.NovelDatabaseHelper;
 
 public class NovelDB {
 	private NovelDatabaseHelper dbHelper;  
 	private SQLiteDatabase database;  
 
 	public final static String SRC_TABLE="source";  
 	public final static String _ID="_id"; 
 	public final static String ID="id";
 	public final static String SRC_NAME="name";
 	public final static String SRC_DOMAIN="domain";  
 	public final static String SRC_PATTERN_CHAPTER = "pattern_chapter";
 	public final static String SRC_PATTERN_IMG = "pattern_img";
 	public final static String SRC_PATTERN_LATEST_TITLE = "pattern_latest_title";
 	public final static String SRC_PATTERN_BODY = "pattern_body";
 	public final static String SRC_PATTERN_ORDER = "pattern_order";
 	public final static String SRC_PATTERN_CONTENT = "pattern_content";
 	public final static String SRC_PATTERN_PAGE = "pattern_page"; 
 	
 	public final static String BOOK_TYPE_TABLE = "book_type";
 	public final static String TYPE_ID="type_id";
 	public final static String TYPE_NAME="type_name";
 	
 	public final static String BOOK_LIST_TABLE = "book_list";
 	public final static String SRC_ID="src_id";
 	public final static String BOOK_URL="url";
 	public final static String BOOK_AUTHOR="author";
 	public final static String BOOK_NAME="name";
 	public final static String BOOK_RECOMMEND="recommend";
 	public final static String BOOK_UPDATE_ORDER="updateOrder";
 	public final static String BOOK_SITE="site";
 	
 	public final static String SUBSCRIPTION_TABLE = "subscription";
 	public final static String SUBSCRIPTION_BOOK_ID = "book_id";
 	public final static String SUBSCRIPTION_LATEST_TITLE = "latest_title";
 	public final static String SUBSCRIPTION_LATEST_URL = "latest_url";
 	public final static String SUBSCRIPTION_READ_TITLE = "read_title";
 	public final static String SUBSCRIPTION_READ_URL = "read_url";
 	public final static String SUBSCRIPTION_VALID = "valid";
 	public final static String SUBSCRIPTION_NAME = "book_name";
 	
 	public final static String BOOK_CHAPTER_TABLE = "book_chapter";
 	public final static String CHAPTER_TITLE = "title";
 	public final static String CHAPTER_BOOK_ID = "book_id";
 	public final static String CHAPTER_URL = "url";
 	public final static String CHAPTER_READ = "isRead";
 	public final static String CHAPTER_DOWNLOAD = "isDownload";
 	public final static String CHAPTER_UPDATE = "updateFlag";
 	public final static String CHAPTER_CONTENT = "content";
 	public final static String CHAPTER_ORDER = "orderNum";
 	public final static String CHAPTER_IS_UPDATE = "isUpdate";
 	
 	public NovelDB(Context context){  
 	    dbHelper = new NovelDatabaseHelper(context);  
 	    database = dbHelper.getWritableDatabase();  
 	}
 	
 	public long addChapter(BookChapterEntity chapter) {
 		Cursor cursor = database.rawQuery("select * from book_chapter where title = '" 
 				+ chapter.getChapterTitle()
 				+ "' and book_id="
 				+ chapter.getBook().getPriId(), null);
 		try {
 			if(cursor != null && cursor.moveToFirst()) {
 				return cursor.getInt(0);
 			}
 		} finally {
 			cursor.close();
 		}
 		
 //		ContentValues bkValues = new ContentValues();
 //		bkValues.put(BOOK_UPDATE_ORDER, chapter.getOrder());
 //		database.update(BOOK_LIST_TABLE, bkValues, "_id=" + chapter.getBook().getPriId(), null);
 		
 		ContentValues values = new ContentValues();
 		values.put(CHAPTER_BOOK_ID, chapter.getBook().getPriId());
 		values.put(CHAPTER_TITLE, chapter.getChapterTitle());
 		values.put(CHAPTER_URL, chapter.getChapterUrl());
 		values.put(CHAPTER_ORDER, chapter.getOrder());
 		values.put(CHAPTER_IS_UPDATE, chapter.getIsUpdate());
 		if(chapter.getContent() != null)
 			values.put(CHAPTER_CONTENT, chapter.getContent());
 		
 //		values.put(CHAPTER_CONTENT, "");
 		
 		return database.insert(BOOK_CHAPTER_TABLE, null, values);
 	}
 	
 	public long addSubscription(BookEntity book) {
 		Cursor cursor = database.rawQuery("select * from subscription where book_name = ?", new String[]{ book.getName()});
 		try {
 			if(cursor != null && cursor.moveToFirst()) {
 				if(cursor.getInt(6) == 1) {
 					return -1;
 				}
 				else { 
 					ContentValues updateValues = new ContentValues();
 					updateValues.put(SUBSCRIPTION_VALID, 1);
 					database.update(SUBSCRIPTION_TABLE, updateValues, "book_name = ?", new String[]{book.getName()});
 					return -2;
 				}
 			}
 		} finally {
 			cursor.close();
 		}
 		
 		ContentValues values = new ContentValues();
 		values.put(SUBSCRIPTION_BOOK_ID, book.getPriId());
 		values.put(SUBSCRIPTION_LATEST_TITLE, book.getLatestTitle());
 		values.put(SUBSCRIPTION_LATEST_URL, "");
 		values.put(SUBSCRIPTION_READ_TITLE, "");
 		values.put(SUBSCRIPTION_READ_URL, "");
 		values.put(SUBSCRIPTION_VALID, 1);
 		values.put(SUBSCRIPTION_NAME, book.getName());
 		
 		return database.insert(SUBSCRIPTION_TABLE, null, values);
 	}
 
 	public long createBookRecords(JSONObject jsonObj) throws JSONException, UnsupportedEncodingException{  
 		ContentValues values = new ContentValues();  
 		values.put(BOOK_NAME, URLDecoder.decode(jsonObj.getString("name"), "utf-8"));
 		values.put(SRC_ID, jsonObj.getInt("source_id"));
 		values.put(BOOK_URL, jsonObj.getString("url"));
 		values.put(TYPE_ID, jsonObj.getInt("type"));
 		values.put(BOOK_AUTHOR, URLDecoder.decode(jsonObj.getString("author"), "utf-8"));
 		values.put(BOOK_RECOMMEND, jsonObj.getInt("recommend"));
 		return database.insert(BOOK_LIST_TABLE, null, values);  
 	}
 	
 	public long createTypeRecords(JSONObject jsonObj) throws JSONException, UnsupportedEncodingException {
 		ContentValues values = new ContentValues();
 		values.put(TYPE_ID, jsonObj.getInt("type_id"));
 		values.put(TYPE_NAME, URLDecoder.decode(jsonObj.getString("type_name"), "utf-8"));
 		return database.insert(BOOK_TYPE_TABLE, null, values);
 	}
 	
 	public long createTypeRecords(int typeId, String typeName) throws UnsupportedEncodingException {
 		ContentValues values = new ContentValues();
 		values.put(TYPE_ID, typeId);
 		values.put(TYPE_NAME, URLDecoder.decode(typeName, "utf-8"));
 		return database.insert(BOOK_TYPE_TABLE, null, values);
 	}
 	
 	public long createSourceRecords(JSONObject jsonObj) throws JSONException, UnsupportedEncodingException {
 		ContentValues values = new ContentValues();
 		values.put(ID, jsonObj.getInt(ID));
		values.put(SRC_NAME, URLDecoder.decode(jsonObj.getString("name"), "utf-8"));
 		values.put(SRC_DOMAIN, jsonObj.getString("domain"));
 		values.put(SRC_PATTERN_CHAPTER, jsonObj.getString(SRC_PATTERN_CHAPTER));
 		values.put(SRC_PATTERN_IMG, jsonObj.getString(SRC_PATTERN_IMG));
 		values.put(SRC_PATTERN_LATEST_TITLE, jsonObj.getString(SRC_PATTERN_LATEST_TITLE));
 		values.put(SRC_PATTERN_BODY, jsonObj.getString(SRC_PATTERN_BODY));
 		values.put(SRC_PATTERN_ORDER, jsonObj.getString(SRC_PATTERN_ORDER));
 		values.put(SRC_PATTERN_CONTENT, jsonObj.getString(SRC_PATTERN_CONTENT));
 		values.put(SRC_PATTERN_PAGE, jsonObj.getString(SRC_PATTERN_PAGE));
 		return database.insert(SRC_TABLE, null, values);
 	}
 	
 	public long updateSourceRecords(JSONObject jsonObj) throws JSONException, UnsupportedEncodingException {
 		ContentValues values = new ContentValues();
 		values.put(ID, jsonObj.getInt(ID));
 		values.put(SRC_DOMAIN, jsonObj.getString("domain"));
 		values.put(SRC_PATTERN_CHAPTER, jsonObj.getString(SRC_PATTERN_CHAPTER));
 		values.put(SRC_PATTERN_IMG, jsonObj.getString(SRC_PATTERN_IMG));
 		values.put(SRC_PATTERN_LATEST_TITLE, jsonObj.getString(SRC_PATTERN_LATEST_TITLE));
 		values.put(SRC_PATTERN_BODY, jsonObj.getString(SRC_PATTERN_BODY));
 		values.put(SRC_PATTERN_ORDER, jsonObj.getString(SRC_PATTERN_ORDER));
 		values.put(SRC_PATTERN_CONTENT, jsonObj.getString(SRC_PATTERN_CONTENT));
 		values.put(SRC_PATTERN_PAGE, jsonObj.getString(SRC_PATTERN_PAGE));
 		String name = URLDecoder.decode(jsonObj.getString("name"), "utf-8");
 		return database.update(SRC_TABLE, values, "name = ?", new String[]{name});
 	}
 	
 	
 	public void updateChapterContent(String content, int _id) {
 		ContentValues values = new ContentValues();
 		values.put(CHAPTER_CONTENT, content);
 		values.put(CHAPTER_READ, 1);
 		values.put(CHAPTER_DOWNLOAD, 1);
 		database.update(BOOK_CHAPTER_TABLE, values, "_id=" + _id, null);
 	}
 
 	public void executeSQL(String sql) {
 		database.execSQL(sql);
 	}
 	
 	public void update(String tblName, ContentValues values, String whereClause, String[] whereArgs) {
 		database.update(tblName, values, whereClause, whereArgs);
 	}
 	
 	public void delete(String tblName, String whereClause, String[] whereArgs) {
 		database.delete(tblName, whereClause, whereArgs);
 	}
 	
 	public long insert(String tblName, String nullColumnHack, ContentValues values) {
 		return database.insert(tblName, nullColumnHack, values);
 	}
 	
 	public Cursor getRecordsBySQL(String sql) {
 		 Cursor mCursor = database.rawQuery(sql, null);
 		 if(mCursor != null)
 			 mCursor.moveToFirst();
 		 return mCursor; 
 	}
 	
 	 public void close() {
 		 dbHelper.close();
 	 }
 	 
 	 public void beginTransaction() {
 		 database.beginTransaction();        //ֶÿʼ
 	 }
 
 	 public void setTransactionSuccessful() {
 		 database.setTransactionSuccessful(); //ɹûԶعύ
 	 }
 
 	 public void endTransaction() {
 		 database.endTransaction();		// 
 	 }
 	 
 
 
 }
