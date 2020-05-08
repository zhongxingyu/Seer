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
 
 import java.util.ArrayList;
 
 import android.database.Cursor;
 import android.database.sqlite.*;
 
 public class StringTable {
 	//
 	static final String TABLE_NAME = "Strings";
 	//
 	static final String C_Id = "id";
 	static final String C_Value = "value";
 
 	public StringTable() { };
 	
 	class StringItem {
 		long mId;
 		String mValue;
 		
 		StringItem(long id, String value) {
 			mId = id;
 			mValue = value;
 		}
 		
 		long getId() { return mId; }
 		String getValue() { return mValue; }
 	}
 	
 	ArrayList<StringItem> mCachedItems;
 
 	// DA methods
 	public String getString(SQLiteDatabase db, long id) {
 		if (mCachedItems == null) {
 			buildCache(db);
 		}
 		
 		int i, l, r;
 		
 		l = 0;
		r = mCachedItems.size();
 		while (r >= l) {
 			StringItem item;
 			long cid;
 
 			i = l + (r - l) / 2;
 			item = mCachedItems.get(i);
 			cid = item.getId();
 			if (cid == id) {
 				return mCachedItems.get(i).getValue();
 			} else if (cid > id) {
 				r = i - 1;
 			} else /* if (cid < id) */ {
 				l = i + 1;
 			}
 		}
 		return "";
 	}
 	
 	void buildCache(SQLiteDatabase db) {
 		Cursor cursor;
 		String []columns = { C_Id, C_Value };
 
 		mCachedItems = new ArrayList<StringItem>();
 		try {
 		cursor = db.query(TABLE_NAME, columns, null, null, null, null, C_Id);
 		} catch (SQLiteException e) {
 			return;
 		}
 		
 		if (cursor.getCount() < 1) {
 			// no matched row in table
 			cursor.close();
 			return;
 		}
 		cursor.moveToFirst();
 		do {
 			StringItem item;
 			
 			item = new StringItem(cursor.getLong(cursor.getColumnIndex(C_Id)), cursor.getString(cursor.getColumnIndex(C_Value)));
 			mCachedItems.add(item);
 
 			cursor.moveToNext();
 		} while (!cursor.isLast());
 		cursor.close();
 	}
 }
