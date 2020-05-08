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
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * SQLite helper object to manage and create our database. Uses *Table.java as a
  * template for every table that should be in the database. Uses onUpgrade() to
  * update the database if a newer version of the database is out. (a software
  * update may change the DATABASE_VERSION to a higher number calling
  * onUpgrade()).
  * 
  * @author Mathias Karlgren (matkarlg)
  * 
  */
 public class MyBarDatabaseHelper extends SQLiteOpenHelper {
 	private static final String DATABASE_NAME = "turbotorsk_mybar.db";
	private static final int DATABASE_VERSION = 2;
 
 	public MyBarDatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	// Method is called during creation of the database
 	@Override
 	public void onCreate(SQLiteDatabase database) {
 		// Populate the database using our table classes
 		DrinkTable.onCreate(database);
 		IngredientTable.onCreate(database);
 	}
 
 	// Method is called during an upgrade of the database
 	@Override
 	public void onUpgrade(SQLiteDatabase database, int oldVersion,
 			int newVersion) {
 		// Upgrade our database with the new table versions
 		Log.w(this.getClass().getName(), "Upgrading database from version "
 				+ oldVersion + " to " + newVersion);
 		DrinkTable.onUpgrade(database, oldVersion, newVersion);
 		IngredientTable.onUpgrade(database, oldVersion, newVersion);
 	}
 }
