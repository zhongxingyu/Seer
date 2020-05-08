 package garin.artemiy.sqlitesimple.library.util;
 
 /**
  * author: Artemiy Garin
  * Copyright (C) 2013 SQLite Simple Project
  * *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * *
  * http://www.apache.org/licenses/LICENSE-2.0
  * *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 public class SimpleConstants {
 
     private SimpleConstants() {
     }
 
     // FTS
     public static final int QUERY_LENGTH = 2;
     public static final String FTS_SQL_OR = "OR";
     public static final String FTS_SQL_AND = "AND";
     public static final String FTS_SQL_FORMAT = "SELECT * FROM %s WHERE %s MATCH \"%s:%s*\" ORDER BY %s %s;";
     public static final String FTS_SQL_TABLE_NAME = "%s_FTS";
     public static final String FTS_CREATE_VIRTUAL_TABLE_WITH_CATEGORY =
             "CREATE VIRTUAL TABLE %s USING fts3(%s, %s, %s, tokenize = porter);";
     public static final String FTS_CREATE_VIRTUAL_TABLE =
             "CREATE VIRTUAL TABLE %s USING fts3(%s, %s, tokenize = porter);";
    public static final String FTS_DROP_VIRTUAL_TABLE = "DROP TABLE IF EXISTS %s";
 
     // Shared preferences
     public static final String SHARED_PREFERENCES_DATABASE = "SQLiteSimpleDatabaseHelper";
     public static final String SHARED_DATABASE_TABLES = "SQLiteSimpleDatabaseTables_%s";
     public static final String SHARED_DATABASE_QUERIES = "SQLiteSimpleDatabaseQueries_%s";
     public static final String SHARED_DATABASE_VERSION = "SQLiteSimpleDatabaseVersion";
     public static final String SHARED_DATABASE_VIRTUAL_TABLE_CREATED = "SQLiteSimpleDatabaseVirtualTableCreated";
     public static final String SHARED_PREFERENCES_LIST = "List_%s_%s";
     public static final String SHARED_PREFERENCES_INDEX = "%s_Index";
     public static final String LOCAL_PREFERENCES = "LOCAL";
 
     public static final int FIRST_ELEMENT = 0;
 
     // SQL
     public static final String AUTOINCREMENT = "AUTOINCREMENT";
     public static final String PRIMARY_KEY = "PRIMARY KEY";
     public static final String SQL_IN = "%s IN (%s)";
     public static final String SQL_DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS";
     public static final String SQL_CREATE_TABLE_IF_NOT_EXIST = "CREATE TABLE IF NOT EXISTS %s (";
     public static final String SQL_ALTER_TABLE_ADD_COLUMN = "ALTER TABLE %s ADD COLUMN %s ";
     public static final String SQL_AVG_QUERY = "SELECT AVG(%s) FROM %s";
     public static final String SQL_AVG_QUERY_WHERE = "SELECT AVG(%s) FROM %s WHERE %s = '%s'";
 
     // String.format(..)
     public static final String FORMAT_GLUED = "%s%s";
     public static final String FORMAT_TWINS = "%s %s";
     public static final String FORMAT_COLUMN = "%s=?";
     public static final String FORMAT_BRACKETS = "(%s)";
     public static final String FORMAT_COLUMNS_COMMA = "%s=? AND %s=?";
 
     // Other
     public static final int FIRST_DATABASE_VERSION = 1;
     public static final String SPECIAL_SYMBOLS_REGEX = "[-+.^:,\"']";
     public static final String ID_COLUMN = "_id"; // if we don't make a primary key column
     public static final String DESC = "DESC";
     public static final String ASC = "ASC";
     public static final String SPACE = " ";
     public static final String EMPTY = "";
     public static final String DIVIDER = ",";
     public static final String FIRST_BRACKET = "(";
     public static final String LAST_BRACKET = ");";
     public static final String DOT = ".";
     public static final String UNDERSCORE = "_";
 
 }
