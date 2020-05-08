 /*
 New BSD License
 Copyright (c) 2012, MyBar Team All rights reserved.
 mybar@turbotorsk.se
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 �	Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 �	Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 �	Neither the name of the MyBar nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package se.turbotorsk.mybar.model.database;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 /**
  * A table template. Creates the database table structure for the MyBar Table.
  * (mirrored in remote database).
  * 
  * @author Mathias Karlgren (matkarlg)
  * 
  */
 public class MyBarTable {
 	// Database table template.
 	public static final String TABLE_MYBAR = "mybar";
 	public static final String COLUMN_ID = "_id";
 	public static final String COLUMN_INGREDIENTID = "ingredientid";
 	public static final String COLUMN_LOCATION = "location";
 
 	// Database table creation SQL statement.
 	private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_MYBAR + "(" + COLUMN_ID
 			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_INGREDIENTID + " INT NOT NULL, "
			+ COLUMN_LOCATION + " TEXT NOT NULL, " + ");";
 
 	public static void onCreate(SQLiteDatabase database) {
 		database.execSQL(DATABASE_CREATE);
 	}
 
 	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
 		// Print upgrade warning to LogCat.
 		Log.w(MyBarTable.class.getName(), "Upgrading " + TABLE_MYBAR + " table from version "
 				+ oldVersion + " to " + newVersion + ", which will destroy all old data");
 
 		// Kills the table and existing data.
 		database.execSQL("DROP TABLE IF EXISTS " + TABLE_MYBAR);
 
 		// Recreates the database with a new version.
 		onCreate(database);
 	}
 }
