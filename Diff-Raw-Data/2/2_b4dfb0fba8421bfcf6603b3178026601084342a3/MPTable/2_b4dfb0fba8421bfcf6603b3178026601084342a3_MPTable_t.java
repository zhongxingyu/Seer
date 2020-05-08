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
 
 import android.database.Cursor;
 import android.database.sqlite.*;
 
 public class MPTable {
 	//
 	static final String TABLE_NAME = "MP";
 	static final String TABLE_NAME_SUB = "MPsub";
 	//
 	static final String C_Id = "ID";
 	static final String C_RaceRank = "RaceRank";
 	static final String C_JobRank = "JobRank";
 	static final String C_JobLevel = "Lv";
 	static final String C_MP = "MP";
 	static final String C_MP2 = "MP2";
 
 	public MPTable() { };
 
 	// DA methods
 	public int getMP(SQLiteDatabase db, String racerank, String jobrank, int joblevel, String subjobrank, int subjoblevel) {
 		Cursor cursor;
 		int value;
 		String []columns = { C_MP };
 		String []columns2 = { C_MP2 };
 
 		value = 0;
 		if (!jobrank.equals("-")) {
 			try {
 				cursor = db.query(true, TABLE_NAME, columns,
 									C_RaceRank + " = '" + racerank + "' AND " + C_JobRank + " = '" + jobrank + "' AND " + C_JobLevel + " <= '" + joblevel + "'",
 									null, null, null, C_JobLevel + " DESC", null);
 			} catch (SQLiteException e) {
 				return 0;
 			}
 
 			if (cursor.getCount() < 1) {
 				// no matched row in table
 				cursor.close();
 				return 0;
 			}
 			cursor.moveToFirst();
 			value = cursor.getInt(0);
 			cursor.close();
 			if (!subjobrank.equals("-") && subjoblevel > 0) {
 				try {
					cursor = db.query(true, TABLE_NAME_SUB, columns,
 							C_RaceRank + " = '" + racerank + "' AND " + C_JobRank + " = '" + subjobrank + "' AND " + C_JobLevel + " <= '" + subjoblevel + "'",
 							null, null, null, C_JobLevel + " DESC", null);
 				} catch (SQLiteException e) {
 					return 0;
 				}
 
 				if (cursor.getCount() < 1) {
 					// no matched row in table
 					cursor.close();
 					return 0;
 				}
 				cursor.moveToFirst();
 				value += cursor.getInt(0);
 				cursor.close();
 			}
 		} else {
 			if (!subjobrank.equals("-")&& subjoblevel > 0) {
 				try {
 					cursor = db.query(true, TABLE_NAME_SUB, columns2,
 							C_RaceRank + " = '" + racerank + "' AND " + C_JobRank + " = '" + subjobrank + "' AND " + C_JobLevel + " <= '" + subjoblevel + "'",
 							null, null, null, C_JobLevel + " DESC", null);
 				} catch (SQLiteException e) {
 					return 0;
 				}
 
 				if (cursor.getCount() < 1) {
 					// no matched row in table
 					cursor.close();
 					return 0;
 				}
 				cursor.moveToFirst();
 				value = cursor.getInt(0);
 				cursor.close();
 			}
 		}
 
 		return value;
 	}
 }
 
