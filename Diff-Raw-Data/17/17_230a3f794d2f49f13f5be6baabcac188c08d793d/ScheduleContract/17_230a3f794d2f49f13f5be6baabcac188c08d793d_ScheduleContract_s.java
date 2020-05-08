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
 
 import android.provider.BaseColumns;
 
 /**
  * A Contract class used in conjunction with {@link ScheduleDatabase}, defining the
  * schema for the Schedule Database.
  */
 public final class ScheduleContract {
 
     public static final int DATABASE_VERSION = 1;
     public static final String DATABASE_NAME = "scheduleDatabase";
 
    static final String CREATE_TABLE_USERS = "CREATE TABLE " + Accounts.TABLE_NAME + " (" +
                                              BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                              Accounts.COLUMN_STUDENT_NAME + " TEXT NOT NULL, " +
                                              Accounts.COLUMN_STUDENT_USERNAME + " TEXT NOT NULL, " +
                                              Accounts.COLUMN_USER_LAST_UPDATE + " INT DEFAULT 0, " +
                                              "UNIQUE (" + Accounts.COLUMN_STUDENT_USERNAME + ") ON CONFLICT REPLACE)";
 
 
     /* Suppress default constructor for noninstantiabiltiy */
     private ScheduleContract() { }
 
     public static abstract class Accounts implements BaseColumns {
         public static final String TABLE_NAME = "users";
         public static final String COLUMN_STUDENT_NAME = "student_name";
         public static final String COLUMN_STUDENT_USERNAME = "student_username";
         public static final String COLUMN_USER_LAST_UPDATE = "user_last_update";
     }
 
 }
