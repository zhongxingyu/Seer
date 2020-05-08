 /*    
 	This file is part of Todoist for Android.
 
     Todoist for Android is free software: you can redistribute it and/or 
     modify it under the terms of the GNU General Public License as published 
     by the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Todoist for Android is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Todoist for Android.  If not, see <http://www.gnu.org/licenses/>.
     
     This file incorporates work covered by the following copyright and  
  	permission notice:
  	
  	Copyright [2010] pskink <pskink@gmail.com>
  	Copyright [2010] ys1382 <ys1382@gmail.com>
  	Copyright [2010] JonTheNiceGuy <JonTheNiceGuy@gmail.com>
 
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
 
 package com.android.applications.todoist.handlers;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 
 import com.android.applications.todoist.containers.User;
 
 public class DBHelper extends SQLiteOpenHelper {	
 	private static final String PATH = "/data/data/com.android.applications.todoist/databases/";
 	private static final String NAME = "data.db";
 	private static final String TBL_USER = "user";
 		private static final String TBL_USER_EMAIL = "email";
 		private static final String TBL_USER_FULLNAME = "full_name";
 		private static final String TBL_USER_ID = "id";
 		private static final String TBL_USER_TOKEN = "api_token";
 		private static final String TBL_USER_STARTPAGE = "start_page";
 		private static final String TBL_USER_TIMEZONE = "timezone";
 		private static final String TBL_USER_TZOFFSET = "tz_offset";
 		private static final String TBL_USER_TIMEFORMAT = "time_format";
 		private static final String TBL_USER_DATEFORMAT = "date_format";
 		private static final String TBL_USER_SORTORDER = "sort_order";
 		private static final String TBL_USER_TWITTER = "twitter";
 		private static final String TBL_USER_JABBER = "jabber";
 		private static final String TBL_USER_MSN = "msn";
 		private static final String TBL_USER_MOBILENUMBER = "mobile_number";
 		private static final String TBL_USER_MOBILEHOST = "mobile_host";
 		private static final String TBL_USER_PREMIUMUNTIL = "premium_until";
 		private static final String TBL_USER_DEFAULTREMINDER = "default_reminder";
 	
 	private static final int VERSION = 1;
 	
 	private static final String CREATE_TBL_USER =
 		"CREATE TABLE " + TBL_USER + " (" +
 		TBL_USER_EMAIL + " TEXT, " +
 		TBL_USER_FULLNAME + " TEXT, " +
 		TBL_USER_ID + " TEXT, " +
 		TBL_USER_TOKEN + " TEXT, " +
 		TBL_USER_STARTPAGE + " TEXT, " +
 		TBL_USER_TIMEZONE + " TEXT, " +
 		TBL_USER_TZOFFSET + " TEXT, " +
 		TBL_USER_TIMEFORMAT + " TEXT, " +
 		TBL_USER_DATEFORMAT + " TEXT, " +
 		TBL_USER_SORTORDER + " TEXT, " +
 		TBL_USER_TWITTER + " TEXT, " +
 		TBL_USER_JABBER + " TEXT, " +
 		TBL_USER_MSN + " TEXT, " +
 		TBL_USER_MOBILENUMBER + " TEXT, " +
 		TBL_USER_MOBILEHOST + " TEXT, " +
 		TBL_USER_PREMIUMUNTIL + " TEXT, " +
 		TBL_USER_DEFAULTREMINDER + " TEXT);";
 	
 	private SQLiteDatabase db;
 	private final Context context;
 	
 	public DBHelper(Context context) {
 		super(context, NAME, null, VERSION);
 		this.context = context;
 	}
 	
 	public void storeUser(User user)
 	{
 		String query = "INSERT INTO " + TBL_USER + " (" + 
 		TBL_USER_EMAIL + ", " +
 		TBL_USER_FULLNAME + ", " +
 		TBL_USER_ID + ", " +
 		TBL_USER_TOKEN + ", " +
 		TBL_USER_STARTPAGE + ", " +
 		TBL_USER_TIMEZONE + ", " +
 		TBL_USER_TZOFFSET + ", " +
 		TBL_USER_TIMEFORMAT + ", " +
 		TBL_USER_DATEFORMAT + ", " +
 		TBL_USER_SORTORDER + ", " +
 		TBL_USER_TWITTER + ", " +
 		TBL_USER_JABBER + ", " +
 		TBL_USER_MSN + ", " +
 		TBL_USER_MOBILENUMBER + ", " +
 		TBL_USER_MOBILEHOST + ", " +
 		TBL_USER_PREMIUMUNTIL + ", " +
 		TBL_USER_DEFAULTREMINDER + ") VALUES ('" +
 		user.getEmail() + "', '" +
 		user.getFullName() + "', '" +
 		user.getID() + "', '" +
 		user.getAPIToken() + "', '" +
 		user.getStartPage() + "', '" +
 		user.getTimezone() + "', '" +
 		user.getTzOffset() + "', '" +
 		user.getTimeFormat() + "', '" +
 		user.getDateFormat() + "', '" +
 		user.getSortOrder() + "', '" +
 		user.getTwitter() + "', '" +
 		user.getJabber() + "', '" +
 		user.getMSN() + "', '" +
 		user.getMobileNumber() + "', '" +
 		user.getMobileHost() + "', '" +
 		"" + "', '" +
 		"" + "');";
 		this.db.execSQL(query);
 	}
 	
 	public User getUser()
 	{
 		Cursor cursor = this.db.query(TBL_USER, null, null, null, null, null, null);
 		cursor.moveToPosition(0);
 		try
 		{
 		return new User(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),
 				cursor.getString(5),cursor.getString(6),cursor.getString(7),cursor.getString(8),cursor.getString(9),
 				cursor.getString(10),cursor.getString(11),cursor.getString(12),cursor.getString(13),cursor.getString(14));
 		}
 		catch (Exception e)
 		{
 			return new User();
 		}
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase db)
 	{
 		db.execSQL(CREATE_TBL_USER);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public synchronized void close()
 	{
 		if(this.db != null)
 		{
 			this.db.close();
 		}
 		
 		super.close();
 	}
 	
 	public void createDB() throws IOException
 	{
 		if(!checkDB())
 		{
 			this.getReadableDatabase();
 		}
 	}
 
 	private boolean checkDB()
 	{
 		SQLiteDatabase checkDB = null;
 		
 		try
 		{
 			checkDB = SQLiteDatabase.openDatabase(PATH + NAME, null, SQLiteDatabase.OPEN_READONLY);
 		}
 		catch(SQLiteException e)
 		{
 			//Database doesn't exist
 		}
 		
 		if(checkDB != null)
 		{
 			checkDB.close();
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public void openDB() throws SQLException
 	{
 		this.db = SQLiteDatabase.openDatabase(PATH + NAME, null, SQLiteDatabase.OPEN_READWRITE);
 	}
 }
