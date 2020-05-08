 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *   UWSchedule student class and registration sharing interface
  *   Copyright (C) 2013 Sherman Pay, Jeremy Teo, Zachary Iqbal
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by`
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.amgems.uwschedule.provider;
 
 import static com.amgems.uwschedule.provider.ScheduleContract.*;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 import com.amgems.uwschedule.model.Account;
 
 /**
  * A helper class managing {@link android.database.sqlite.SQLiteDatabase} used as
  * an interface to the Schedule related data stored in the client.
  *<p>
  * The data stored in the database is a reflection of the most recent
  * {@link com.amgems.uwschedule.model.Course} and {@link com.amgems.uwschedule.model.Meeting}
  * related data for a given user.
  */
 public class ScheduleDatabase extends SQLiteOpenHelper{
 
     private static ScheduleDatabase sInstance;
 
     private ScheduleDatabase(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     /**
      * Returns an instance of the {@code ScheduleDatabase} object.
     * <p>
     * Ensures that for a given process, only one instance of the ScheduleDatabase
     * object is returned.
      * @param context Context from which the database will be resolved;
      */
     public static synchronized ScheduleDatabase getInstance(Context context) {
         if (sInstance == null) {
             sInstance = new ScheduleDatabase(context);
         }
         return sInstance;
     }
 
     @Override
     public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_ACCOUNTS);
     }
 
     @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
         throw new AssertionError(ScheduleContract.DATABASE_VERSION + "is the only supported version at this time.");
     }
 
     /**
      * Returns an {@link Account} corresponding to a given username,
      * or {@code null} if none is found.
      *
      * @param accountUsername The username to query for.
      * @return The Account associated with the username.
      */
     public Account getAccount(String accountUsername) {
         SQLiteDatabase db = getReadableDatabase();
         String[] projection = {
                 BaseColumns._ID,
                 Accounts.COLUMN_STUDENT_NAME,
                 Accounts.COLUMN_USER_LAST_UPDATE};
         String selection = Accounts.COLUMN_STUDENT_USERNAME + " = ?";
         String[] selectionArgs = { accountUsername };
 
         Cursor result = db.query(Accounts.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
 
         Account account = null;
         if (result.moveToFirst()) {
             int accountId = result.getInt(0);
             String accountName = result.getString(1);
             Long accountLastUpdate = result.getLong(2);
             account = new Account(accountId, accountUsername, accountName, accountLastUpdate);
         }
         return account;
     }
 
 }
