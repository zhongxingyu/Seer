 /*
  * Copyright (C) 2010 Christian Gawron
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cgawron.agoban.provider;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import java.lang.reflect.Field;
 
 class SGFDBOpenHelper extends SQLiteOpenHelper
 {
 	private static final String TAG = "SGFDBOpenHelper";
 	private static final int DATABASE_VERSION = 3;
 	private static final String DATABASE_NAME = "AGoban";
 	static final String SGF_TABLE_NAME = "sgf";
 
 	SGFDBOpenHelper(Context context)
 	{
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		String cs = getCreateStatement();
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db)
 	{
 		Log.d(TAG, "onCreate");
 		db.execSQL(getCreateStatement());
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
 	{
 		String upgradeStatement = null;
 		if (oldVersion == 1 && newVersion == 2) {
 			StringBuilder sb = new StringBuilder();
 			sb.append("ALTER TABLE ").append(SGF_TABLE_NAME)
 					.append(" ADD COLUMN ")
 					.append(GameInfo.KEY_REMOTE_MODIFIED_DATE)
 					.append(" INTEGER;");
 			upgradeStatement = sb.toString();
 		}
 		else if (oldVersion == 1 && newVersion == 3) {
 			StringBuilder sb = new StringBuilder();
 			sb.append("ALTER TABLE ").append(SGF_TABLE_NAME)
 					.append(" ADD COLUMN ")
 					.append(GameInfo.KEY_REMOTE_MODIFIED_DATE)
 					.append(" INTEGER ")
 					.append(", ")
 					.append(GameInfo.KEY_METADATA_DATE)
 					.append(" INTEGER;");
 			upgradeStatement = sb.toString();
 		}
 		else if (oldVersion == 2 && newVersion == 3) {
 			StringBuilder sb = new StringBuilder();
 			sb.append("ALTER TABLE ").append(SGF_TABLE_NAME)
 					.append(" ADD COLUMN ")
 					.append(GameInfo.KEY_METADATA_DATE)
 					.append(" INTEGER;");
 			upgradeStatement = sb.toString();
 		}
 
 		if (upgradeStatement != null) {
 			Log.d(TAG, "upgrade: " + upgradeStatement);
 			db.execSQL(upgradeStatement);
 		} else {
 			Log.e(TAG, String.format(
 					"upgrade from version %d to %d not possible", oldVersion,
 					newVersion));
 			throw new RuntimeException("upgrade not possible");
 		}
 	}
 
 	public String getCreateStatement()
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append("CREATE TABLE ").append(SGF_TABLE_NAME).append(" (")
 				.append(GameInfo.KEY_ID).append(" INTEGER PRIMARY KEY, ");
 		Field[] fields = GameInfo.class.getFields();
 		for (Field field : fields) {
 			if (field.getAnnotation(GameInfo.Column.class) != null) {
 				try {
 					Log.d(TAG, "Key: " + field.getName() + " " + field.get(null));
 					sb.append(field.get(null)).append(" TEXT, ");
 				} catch (IllegalAccessException ex) {
 					throw new RuntimeException(ex);
 				}
 			}
 		}
 		sb.append(GameInfo.KEY_LOCAL_MODIFIED_DATE).append(" INTEGER, ");
		sb.append(GameInfo.KEY_REMOTE_MODIFIED_DATE).append(" INTEGER);");
 		Log.d(TAG, "SQL: " + sb.toString());
 
 		return sb.toString();
 	}
 
 }
