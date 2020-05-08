 /*
    Copyright 2011 kanata3249
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package com.github.kanata3249.ffxieq.android.db;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.StreamCorruptedException;
 
 import com.github.kanata3249.ffxi.FFXIDAO;
 import com.github.kanata3249.ffxieq.Equipment;
 import com.github.kanata3249.ffxieq.FFXICharacter;
 import com.github.kanata3249.ffxieq.R;
 
 import android.app.Activity;
 import android.app.backup.BackupManager;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.*;
 import android.os.Environment;
 
 public class FFXIEQSettings extends SQLiteOpenHelper {
 	private static final String DB_NAME = "ffxisettings";
 	private static final String TABLE_NAME_CHARINFO = "Characters";
 	public static final String C_Id = "_id";
 	public static final String C_Name = "Name";
 	public static final String C_CharInfo = "CharInfo";
 	private static final String TABLE_NAME_FILTERS = "Filters";
 	public static final String C_Filter = "Filter";
 	public static final String C_LastUsed = "LastUsed";
 	private static final int MAX_FILTERS = 16;
 	
 	BackupManager mBackupManager;
 	Context mContext;
 	
 	AugmentTable mAugmentTable;
 
 	// Constructor
 	public FFXIEQSettings(Context context) {
 		super(context, DB_NAME, null, 1);
 		
 		mContext = context;
 		mAugmentTable = new AugmentTable();
 
 		if (android.os.Build.VERSION.SDK_INT > 7) {
 			mBackupManager = new BackupManager(context);
 		}
 	}
 
 	private void dataChanged() {
 		if (mBackupManager != null) {
 			mBackupManager.dataChanged();
 		}
 	}
 
 	public String getFullPath() {
 		return Environment.getDataDirectory() + "/data/" + mContext.getPackageName() + "/databases/" + DB_NAME;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.beginTransaction();
 		
 		try {
 			StringBuilder createSql = new StringBuilder();
 			
 			createSql.append("create table " + TABLE_NAME_CHARINFO + " (");
 			createSql.append(C_Id + " integer primary key autoincrement not null,");
 			createSql.append(C_Name + " text not null,");
 			createSql.append(C_CharInfo + " blob");
 			createSql.append(")");
 			
 			db.execSQL(createSql.toString());
 
 			createSql.setLength(0);
 			createSql.append("create table " + TABLE_NAME_FILTERS + " (");
 			createSql.append(C_Id + " integer primary key autoincrement not null,");
 			createSql.append(C_Filter + " text not null,");
 			createSql.append(C_LastUsed + " integer not null");
 			createSql.append(")");
 			
 			db.execSQL(createSql.toString());
 			
 			mAugmentTable.create_table(db);
 
 			db.setTransactionSuccessful();
 		} finally {
 			db.endTransaction();
 		}
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 	}
 	
 	public long getFirstCharacterId() {
 		Cursor cursor;
 		String []columns = { C_Id };
 		SQLiteDatabase db;
 		long id;
 
 		db = getReadableDatabase();
 		cursor = db.query(TABLE_NAME_CHARINFO, columns, null, null, null, null, null, null);
 		if (cursor.getCount() > 0) {
 			cursor.moveToFirst();
 			id = cursor.getLong(cursor.getColumnIndex(C_Id));
 			cursor.close();
 		} else {
 			cursor.close();
 
 			// Create new character
 			String name = mContext.getString(R.string.NewCharacterName);
 			id = saveCharInfo(-1, name, new FFXICharacter());
 		}
 		
 		return id;
 	}
 
 	public FFXICharacter loadCharInfo(long id) {
 		Cursor cursor;
 		String []columns = { C_CharInfo };
 		SQLiteDatabase db;
 		byte [] chardata;
 		FFXICharacter charInfo;
 
 		db = getReadableDatabase();
 		cursor = db.query(TABLE_NAME_CHARINFO, columns, C_Id + " = '" + id + "'", null, null, null, null, null);
 		
 		if (cursor.getCount() < 1) {
 			// no matched row in table
 			cursor.close();
 			return null;
 		}
 		cursor.moveToFirst();
 		chardata = cursor.getBlob(0);
 		cursor.close();
 
 		charInfo = null;
 		try {
 			ByteArrayInputStream bais = new ByteArrayInputStream(chardata);
 			ObjectInputStream ois = new ObjectInputStream(bais);
 			charInfo = (FFXICharacter)ois.readObject();
 		} catch (StreamCorruptedException e) {
 		} catch (ClassNotFoundException e) {
 		} catch (IOException e) {
 		}
 		return charInfo;
 	}
 	public long saveCharInfo(long id, String name, FFXICharacter charInfo) {
 		byte [] chardata;
 		
 		charInfo.reload();
 		try {
 			ObjectOutputStream oos;
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();;
 
 			oos = new ObjectOutputStream(baos);
 			oos.writeObject(charInfo);
 			oos.close();
 			chardata = baos.toByteArray();
 			baos.close();
 		} catch (IOException e) {
 			return -1;
 		}
 
 		return saveCharInfo(id, name, chardata);
 	}
 	public long saveCharInfo(long id, String name, byte chardata[]) {
 		SQLiteDatabase db;
 		ContentValues values = new ContentValues();;
 		long newId;
 		
 		db = getWritableDatabase();
 
 		values.put(C_Name, name);
 		values.put(C_CharInfo, chardata);
 		db.beginTransaction();
 		newId = id;
 		try {
 			if (id >= 0) {
 				db.update(TABLE_NAME_CHARINFO, values, C_Id + " ='" + id + "'", null);
 			} else {
 				newId = db.insert(TABLE_NAME_CHARINFO, null, values);
 			}
 			db.setTransactionSuccessful();
 		} finally {
 			db.endTransaction();
 			dataChanged();
 		}
 		return newId;
 	}
 	
 	public void deleteCharInfo(long id) {
 		SQLiteDatabase db;
 
 		db = getWritableDatabase();
 		db.delete(TABLE_NAME_CHARINFO, C_Id + " = '" + id + "'", null);
 		dataChanged();
 
 		return;
 	}
 
 	public Cursor getCharactersCursor(String [] columns, String orderBy) {
 		return getReadableDatabase().query(TABLE_NAME_CHARINFO, columns, null, null, null, null, orderBy);
 	}
 	
 	public String getCharacterName(long id) {
 		String []columns = { C_Name };
 		String name;
 		Cursor cursor = getReadableDatabase().query(TABLE_NAME_CHARINFO, columns, C_Id + "='" + id + "'", null, null, null, null, null);
 		if (cursor.getCount() != 1) {
 			cursor.close();
 			return "";
 		}
 		cursor.moveToFirst();
 		name = cursor.getString(cursor.getColumnIndex(C_Name));
 		cursor.close();
 		
 		return name;
 	}
 
 	public Cursor getFilterCursor(String [] columns) {
 		SQLiteDatabase db;
 		
 		db = getWritableDatabase();
 		db.beginTransaction();
 		
 		try {
 			StringBuilder createSql = new StringBuilder();
 			
 			createSql.append("create table " + TABLE_NAME_FILTERS + " (");
 			createSql.append(C_Id + " integer primary key autoincrement not null,");
 			createSql.append(C_Filter + " text not null,");
 			createSql.append(C_LastUsed + " integer not null");
 			createSql.append(")");
 			
 			db.execSQL(createSql.toString());
 			db.setTransactionSuccessful();
 		} catch (SQLiteException e) {
 			/* ignore */
 		} finally {
 			db.endTransaction();
 		}
 
 		return getReadableDatabase().query(TABLE_NAME_FILTERS, columns, null, null, null, null, C_LastUsed + " DESC");
 	}
 
 	public long addFilter(String filter) {
 		ContentValues values = new ContentValues();
 		SQLiteDatabase db = getWritableDatabase();
 		long id;
 
 		values.put(C_Filter, filter);
 		values.put(C_LastUsed, System.currentTimeMillis());
 		db.beginTransaction();
 		id = -1;
 		try {
 			id = db.insert(TABLE_NAME_FILTERS, null, values);
 			db.setTransactionSuccessful();
 		} finally {
 			db.endTransaction();
 		}
 		
 		removeOldFilters();
 		dataChanged();
 		return id;
 	}
 	
 	public void removeOldFilters() {
 		Cursor cursor;
 		SQLiteDatabase db = getWritableDatabase();
 		String [] columns = { C_Id };
 
 
 		cursor = getFilterCursor(columns);
 		if (cursor.getCount() > MAX_FILTERS) {
 			int i;
 			
 			cursor.moveToFirst();
 			for (i = 0; i < MAX_FILTERS; i++) {
 				cursor.moveToNext();
 			}
 			for ( ; i < cursor.getCount(); i++) {
				db.delete(TABLE_NAME_FILTERS, C_Id + " = '" + cursor.getLong(cursor.getColumnIndex(C_Id)), null);
 				cursor.moveToNext();
 			}
 		}
 		cursor.close();
 	}
 	public void useFilter(long id) {
 		ContentValues values = new ContentValues();
 		SQLiteDatabase db = getWritableDatabase();
 
 		values.put(C_LastUsed, System.currentTimeMillis());
 		db.beginTransaction();
 		try {
 			db.update(TABLE_NAME_FILTERS, values, C_Id + " ='" + id + "'", null);
 			db.setTransactionSuccessful();
 		} finally {
 			db.endTransaction();
 		}
 		dataChanged();
 	}
 	
 	public String getFilter(long id) {
 		String []columns = { C_Filter };
 		String filter;
 		Cursor cursor = getReadableDatabase().query(TABLE_NAME_FILTERS, columns, C_Id + "='" + id + "'", null, null, null, null, null);
 		if (cursor.getCount() != 1) {
 			cursor.close();
 			return "";
 		}
 		cursor.moveToFirst();
 		filter = cursor.getString(cursor.getColumnIndex(C_Filter));
 		cursor.close();
 		
 		return filter;
 	}
 	
 	public boolean useExternalDB() {
 		return mContext.getSharedPreferences("ffxieq", Activity.MODE_PRIVATE).getBoolean("useExternalDB", false);
 	}
 	
 	public void setUseExternalDB(boolean useExternalDB) {
 		SharedPreferences.Editor editor;
 		
 		editor = mContext.getSharedPreferences("ffxieq", Activity.MODE_PRIVATE).edit(); 
 		editor.putBoolean("useExternalDB", useExternalDB);
 		editor.commit();
 	}
 
 	public Cursor getAugmentCursor(FFXIDAO dao, int part, int race, int job, int level, String[] columns, String orderBy, String filter, String weaponType) {
 		return mAugmentTable.getCursor(dao, getReadableDatabase(), part, race, job, level, columns, orderBy, filter, weaponType);
 	}
 	public Cursor getAugmentCursor(String[] columns, String orderBy) {
 		return mAugmentTable.getCursor(getReadableDatabase(), columns, orderBy);
 	}
 	public String []getAvailableAugmentWeaponTypes(FFXIDAO dao, int part, int race, int job, int level, String filter) {
 		return mAugmentTable.getAvailableWeaponTypes(dao, getReadableDatabase(), part, race, job, level, filter);
 	}
 	
 	public Equipment instantiateEquipment(FFXIDAO dao, long id, long augId) {
 		return mAugmentTable.newInstance(dao, getReadableDatabase(), id, augId);
 	}
 	public long saveAugment(FFXIDAO dao, long id, long augId, String augment) {
 		Equipment eq = dao.instantiateEquipment(id, -1);
 		if (eq == null)
 			return -1;
 		
 		SQLiteDatabase db = getWritableDatabase();
 		long newId = mAugmentTable.saveAugment(db, augId, augment, eq);
 		dataChanged();
 		return newId;
 	}
 	public long saveAugment(long id, long baseId, String name, String part, String weapon, String job, String race, int level, boolean rare, boolean ex, String description, String augment) {
 		SQLiteDatabase db = getWritableDatabase();
 		long newId = mAugmentTable.saveAugment(db, id, baseId, name, part, weapon, job, race, level, rare, ex, description, augment);
 		dataChanged();
 		return newId;
 	}
 	public void deleteAugment(FFXIDAO dao, long augId) {
 		mAugmentTable.deleteAugment(getWritableDatabase(), augId);
 		dataChanged();
 	}
 }
 
