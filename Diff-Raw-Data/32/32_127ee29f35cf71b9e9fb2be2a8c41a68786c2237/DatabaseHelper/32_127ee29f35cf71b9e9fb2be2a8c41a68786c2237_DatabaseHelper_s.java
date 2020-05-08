 package com.nari.somnium.helper;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonParser;
 import com.nari.somnium.pojo.CCDataPojo;
 import com.nari.somnium.pojo.CLDataPojo;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.util.Log;
 
 /**
  * @author Nari Kim Shin (wassupnari@gmail.com)
  */
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	private final static int NUM_OF_CC_PJT = 10;
 
 	// Database version
 	private static final int DATABASE_VER = 1;
 
 	// Database name
 	private static final String DATABASE_NAME = "Toastmaster_database";
 
 	// Table name
 	private static final String TABLE_NAME_CC = "CC";
 	private static final String TABLE_NAME_CL = "CL";
 	private static final String TABLE_NAME_DRAFT = "Draft";
 
 	// Table column names
 	private static final String KEY_ID = "id";
 	private static final String KEY_OBJECT = "object";
 	
 	private static final String CREATE_TABLE_CC = "CREATE TABLE " + TABLE_NAME_CC + "(" + KEY_ID
 			+ " INTEGER PRIMARY KEY," + KEY_OBJECT + " TEXT" + ")";
 	private static final String CREATE_TABLE_CL = "CREATE TABLE " + TABLE_NAME_CL + "(" + KEY_ID
 			+ " INTEGER PRIMARY KEY," + KEY_OBJECT + " TEXT" + ")";
 	private static final String CREATE_TABLE_DRAFT = "CREATE TABLE " + TABLE_NAME_DRAFT + "(" + KEY_ID
 			+ " INTEGER PRIMARY KEY," + KEY_OBJECT + " TEXT" + ")";
 
 	CCDataPojo[] data = new CCDataPojo[NUM_OF_CC_PJT];
 	
 	private Gson gson = new Gson();
 	private JsonParser jsonParser = new JsonParser();
 
 	public DatabaseHelper(Context context) {
 
		//super(context, DATABASE_NAME, null, DATABASE_VER);
 		// For debugging
		super(context, "/mnt/sdcard/" + DATABASE_NAME, null, DATABASE_VER);
 	}
 	
 	public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
 		super(context, name, factory, version);
 	}
 
 	// Create table
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 
 		db.execSQL(CREATE_TABLE_CC);
 		db.execSQL(CREATE_TABLE_CL);
 		db.execSQL(CREATE_TABLE_DRAFT);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
 
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CC);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CL);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_DRAFT);
 
 		onCreate(db);
 	}
 
 	//================================================= for CC
 	
 	/**
 	 * Add new CC data.
 	 * 
 	 * @param cc
 	 */
 	public void addCCData(int id, CCDataPojo cc) {
 
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		String jsonObj = gson.toJson(cc);
 		values.put(KEY_ID, id);
 		values.put(KEY_OBJECT, jsonObj);
 
 		db.insert(TABLE_NAME_CC, null, values);
 		db.close();
 	}
 
 	/**
 	 * Getting single CC data.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public CCDataPojo getUserCCData(int id) {
 
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		String selectQuery = "SELECT * FROM " + TABLE_NAME_CC + " WHERE "
 				+ KEY_ID + " = " + id;
 
 //		Cursor cursor = db.query(TABLE_NAME_CC, new String[] { KEY_ID, KEY_OBJECT }, KEY_ID + "=?",
 //				new String[] { String.valueOf(id) }, null, null, null, null);
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		if (cursor != null) {
 			cursor.moveToFirst();
 		}
 		if(cursor.getString(1) != null) {
 			CCDataPojo cc = gson.fromJson(jsonParser.parse(cursor.getString(1))
 					.getAsJsonObject(), CCDataPojo.class);
 			return cc;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Getting all CC data.
 	 * 
 	 * @return
 	 */
 	public List<CCDataPojo> getAllCCData() {
 
 		List<CCDataPojo> ccList = new ArrayList<CCDataPojo>();
 
 		String selectQuery = "SELECT * FROM " + TABLE_NAME_CC;
 
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(selectQuery, null);
 
 		if (cursor.moveToFirst()) {
 			do {
 				CCDataPojo cc = new CCDataPojo();
 				cc = gson.fromJson(jsonParser.parse(cursor.getString(1)).getAsJsonObject(), CCDataPojo.class);
 				cc.setId(Integer.parseInt(cursor.getString(0)));
 				ccList.add(cc);
 			} while (cursor.moveToNext());
 		}
 
 		return ccList;
 	}
 
 	/**
 	 * Update single CC data.
 	 * 
 	 * @param cc
 	 * @return
 	 */
 	public int updateCC(CCDataPojo cc) {
 
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		ContentValues values = new ContentValues();
 		String jsonObj = gson.toJson(cc);
 		values.put(KEY_OBJECT, jsonObj);
 
 		return db.update(TABLE_NAME_CC, values, KEY_ID + " = ?",
 				new String[] { String.valueOf(cc.getId()) });
 	}
 
 	/**
 	 * Delete single CC data.
 	 * 
 	 * @param cc
 	 */
 	public void deleteCC(CCDataPojo cc) {
 
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.delete(TABLE_NAME_CC, KEY_ID + " = ?", new String[] { String.valueOf(cc.getId()) });
 		db.close();
 
 	}
 
 	public int getCCCount() {
 
 		int count;
 		String countQuery = "SELECT * FROM " + TABLE_NAME_CC;
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(countQuery, null);
 		count = cursor.getCount();
 		cursor.close();
 		return count;
 	}
 	
 	//================================================= for CL
 	
 	public void addCLData(int id, CLDataPojo cl) {
 
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		String jsonObj = gson.toJson(cl);
 		values.put(KEY_ID, id);
 		values.put(KEY_OBJECT, jsonObj);
 
 		db.insert(TABLE_NAME_CL, null, values);
 		db.close();
 	}
 
 	/**
 	 * Getting single CL data.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public CLDataPojo getUserCLData(int id) {
 
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		String selectQuery = "SELECT * FROM " + TABLE_NAME_CL + " WHERE "
 				+ KEY_ID + " = " + id;
 
 //		Cursor cursor = db.query(TABLE_NAME_CC, new String[] { KEY_ID, KEY_OBJECT }, KEY_ID + "=?",
 //				new String[] { String.valueOf(id) }, null, null, null, null);
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		if (cursor != null) {
 			cursor.moveToFirst();
 		}
 		if(cursor.getString(1) != null) {
 			CLDataPojo cl = gson.fromJson(jsonParser.parse(cursor.getString(1))
 					.getAsJsonObject(), CLDataPojo.class);
 			return cl;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Getting all CL data.
 	 * 
 	 * @return
 	 */
 	public List<CLDataPojo> getAllCLData() {
 
 		List<CLDataPojo> ccList = new ArrayList<CLDataPojo>();
 
 		String selectQuery = "SELECT * FROM " + TABLE_NAME_CL;
 
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(selectQuery, null);
 
 		if (cursor.moveToFirst()) {
 			do {
 				CLDataPojo cl = new CLDataPojo();
 				cl = gson.fromJson(jsonParser.parse(cursor.getString(1)).getAsJsonObject(), CLDataPojo.class);
 				cl.setId(Integer.parseInt(cursor.getString(0)));
 				ccList.add(cl);
 			} while (cursor.moveToNext());
 		}
 
 		return ccList;
 	}
 
 	/**
 	 * Update single CL data.
 	 * 
 	 * @param cl
 	 * @return
 	 */
 	public int updateCL(CLDataPojo cl) {
 
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		ContentValues values = new ContentValues();
 		String jsonObj = gson.toJson(cl);
 		values.put(KEY_OBJECT, jsonObj);
 
 		return db.update(TABLE_NAME_CL, values, KEY_ID + " = ?",
 				new String[] { String.valueOf(cl.getId()) });
 	}
 
 	/**
 	 * Delete single CL data.
 	 * 
 	 * @param cl
 	 */
 	public void deleteCL(CLDataPojo cl) {
 
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.delete(TABLE_NAME_CL, KEY_ID + " = ?", new String[] { String.valueOf(cl.getId()) });
 		db.close();
 
 	}
 
 	public int getCLCount() {
 
 		int count;
 		String countQuery = "SELECT * FROM " + TABLE_NAME_CL;
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(countQuery, null);
 		count = cursor.getCount();
 		cursor.close();
 		return count;
 	}
 
 	
 	//================================================= for Draft
 	
 	public void addDraftData(int id, String draft) {
 
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		//String jsonObj = gson.toJson(draft);
 		values.put(KEY_ID, id);
 		values.put(KEY_OBJECT, draft);
 
 		db.insert(TABLE_NAME_DRAFT, null, values);
 		db.close();
 	}
 
 	public String getDraftData(int id) {
 
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor cursor = db.query(TABLE_NAME_DRAFT, new String[] { KEY_ID, KEY_OBJECT }, KEY_ID + "=?",
 				new String[] { String.valueOf(id) }, null, null, null, null);
 		if (cursor != null) {
 			cursor.moveToFirst();
 		}
 		if(cursor.getString(1) != null) {
 			String draft = cursor.getString(1);
 			return draft;
 		}
 
 		return null;
 	}
 
 	public List<String> getAllDraftData() {
 
 		List<String> ccList = new ArrayList<String>();
 
 		String selectQuery = "SELECT * FROM " + TABLE_NAME_DRAFT;
 
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(selectQuery, null);
 
 		if (cursor.moveToFirst()) {
 			do {
 				//CLDataPojo cc = new CLDataPojo();
 				String draft = cursor.getString(1);
 				ccList.add(draft);
 			} while (cursor.moveToNext());
 		}
 
 		return ccList;
 	}
 
 	public int updateDraft(int key, String draft) {
 
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		ContentValues values = new ContentValues();
 		//String jsonObj = gson.toJson(cl);
 		values.put(KEY_OBJECT, draft);
 
 		return db.update(TABLE_NAME_DRAFT, values, KEY_ID + " = ?",
 				new String[] { String.valueOf(key) });
 	}
 
 	public void deleteDraft(int key) {
 
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.delete(TABLE_NAME_DRAFT, KEY_ID + " = ?", new String[] { String.valueOf(key) });
 		db.close();
 
 	}
 
 	public int getDraftCount() {
 
 		int count;
 		String countQuery = "SELECT * FROM " + TABLE_NAME_DRAFT;
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(countQuery, null);
 		count = cursor.getCount();
 		cursor.close();
 		return count;
 	}
 	
 	public boolean checkDraftExists(int id) {
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor cursor = db.query(TABLE_NAME_DRAFT, null, KEY_ID + "=?",
 				new String[] { String.valueOf(id) }, null, null, null);
 
 		if (!cursor.moveToFirst()) {
 			return false;
 		}
 		return true;
 	}
 	
 }
