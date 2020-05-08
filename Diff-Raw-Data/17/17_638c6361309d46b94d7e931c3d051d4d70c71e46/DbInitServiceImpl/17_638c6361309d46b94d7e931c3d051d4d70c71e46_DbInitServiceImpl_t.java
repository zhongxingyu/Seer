 package com.sanyu.jpgrammar.service.impl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import android.content.res.AssetManager;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.text.TextUtils;
 
 import com.sanyu.jpgrammar.service.DbInitService;
 import com.sanyu.jpgrammar.util.SqlConstants;
 
 public class DbInitServiceImpl implements DbInitService {
 
 	@Override
 	public void initDatabase(SQLiteDatabase db, AssetManager assetManager) {
		if (!checkHasInitialized(db)) {
 			try {
 				InputStream in = assetManager.open("init.sql");
 				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
 				String sqlReader = "";
 				String sql = "";
 				while ((sqlReader = bufferedReader.readLine()) != null) {
 					sql = sql + "\n\r\n\r" + sqlReader;
 					if (sqlReader.indexOf(';') > -1) {
 						if (!TextUtils.isEmpty(sqlReader)) {
 							db.execSQL(sql);
 							sql = "";
 						}
 					}
 				}
 				bufferedReader.close();
 				in.close();
 			} catch (SQLException e) {
 			} catch (IOException e) {
 			}
 		}
 	}
 
	private boolean checkHasInitialized(SQLiteDatabase db) {
		Boolean result = false;
		Cursor cursor = null;
		String sql = "select count(*) as c from sqlite_master where type ='table' and name ='fav_gram' ";
		cursor = db.rawQuery(sql, null);
		if (cursor.moveToNext()) {
			int count = cursor.getInt(0);
			if (count > 0) {
				result = true;
			}
		}
		return result;
	}

 	private boolean checkHasData(SQLiteDatabase db) {
 		Cursor cursor1 = db.rawQuery(SqlConstants.getN1GramNo, null);
 		cursor1.moveToNext();
 		int no1 = cursor1.getInt(cursor1.getColumnIndex("count"));
 		Cursor cursor2 = db.rawQuery(SqlConstants.getN2GramNo, null);
 		cursor2.moveToNext();
 		int no2 = cursor2.getInt(cursor2.getColumnIndex("count"));
 		cursor1.close();
 		cursor2.close();
 		return (no2 == 170 ? true : false) && (no1 == 231 ? true : false);
 	}
 
 }
