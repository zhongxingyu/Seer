 package garin.artemiy.sqlitesimple.library;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import garin.artemiy.sqlitesimple.library.annotations.Column;
 import garin.artemiy.sqlitesimple.library.util.ColumnType;
 import garin.artemiy.sqlitesimple.library.util.SimpleConstants;
 import garin.artemiy.sqlitesimple.library.util.SimpleDatabaseUtil;
 import garin.artemiy.sqlitesimple.library.util.SimplePreferencesUtil;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
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
 public class SQLiteSimple {
 
     private SQLiteSimpleHelper sqLiteSimpleHelper;
     private SimplePreferencesUtil sharedPreferencesUtil;
     private int databaseVersion;
     private boolean isAddedSQLDivider;
     private String sharedPreferencesPlace;
 
     @SuppressWarnings("unused")
     public SQLiteSimple(Context context, int databaseVersion) {
         sharedPreferencesUtil = new SimplePreferencesUtil(context);
         sharedPreferencesPlace = SimpleConstants.SHARED_LOCAL_PREFERENCES;
         this.databaseVersion = databaseVersion;
 
         commitDatabaseVersion();
 
         sqLiteSimpleHelper = new SQLiteSimpleHelper(context, sharedPreferencesPlace, databaseVersion, null, false);
     }
 
     @SuppressWarnings("unused")
     public SQLiteSimple(Context context) {
         sharedPreferencesUtil = new SimplePreferencesUtil(context);
         sharedPreferencesPlace = SimpleConstants.SHARED_LOCAL_PREFERENCES;
         this.databaseVersion = SimpleConstants.FIRST_DATABASE_VERSION;
 
         commitDatabaseVersion();
 
         sqLiteSimpleHelper = new SQLiteSimpleHelper(context, sharedPreferencesPlace, databaseVersion, null, false);
     }
 
     @SuppressWarnings("unused")
     public SQLiteSimple(Context context, String assetsDatabaseName) {
         sharedPreferencesUtil = new SimplePreferencesUtil(context);
         sharedPreferencesPlace = assetsDatabaseName;
         this.databaseVersion = SimpleConstants.FIRST_DATABASE_VERSION;
 
         sqLiteSimpleHelper = new SQLiteSimpleHelper(context, sharedPreferencesPlace, databaseVersion, assetsDatabaseName, false);
     }
 
     private void commitDatabaseVersion() {
         if (databaseVersion > sharedPreferencesUtil.getDatabaseVersion(sharedPreferencesPlace)) {
             sharedPreferencesUtil.putDatabaseVersion(databaseVersion, sharedPreferencesPlace);
             sharedPreferencesUtil.commit();
         }
     }
 
     private void commit(List<String> tables, List<String> sqlQueries) {
         sharedPreferencesUtil.putList(String.format(SimpleConstants.SHARED_DATABASE_TABLES, sharedPreferencesPlace), tables);
         sharedPreferencesUtil.putList(String.format(SimpleConstants.SHARED_DATABASE_QUERIES, sharedPreferencesPlace), sqlQueries);
         sharedPreferencesUtil.commit();
     }
 
     private void checkingCommit(List<String> tables, List<String> sqlQueries, boolean newDatabaseVersion) {
         if (newDatabaseVersion) {
             commit(tables, sqlQueries);
             SQLiteDatabase database = sqLiteSimpleHelper.getWritableDatabase(); // call onCreate();
             database.close();
         } else {
             commit(tables, sqlQueries);
         }
     }
 
     @SuppressWarnings("unused")
     public void rawQuery(String sql) {
         SQLiteDatabase database = sqLiteSimpleHelper.getWritableDatabase();
         database.execSQL(sql);
         database.close();
     }
 
     public void create(Class<?>... classes) {
 
         List<String> savedTables = sharedPreferencesUtil.
                 getList(String.format(SimpleConstants.SHARED_DATABASE_TABLES, sharedPreferencesPlace));
         List<String> savedSQLQueries = sharedPreferencesUtil.
                 getList(String.format(SimpleConstants.SHARED_DATABASE_QUERIES, sharedPreferencesPlace));
 
         sharedPreferencesUtil.clearAllPreferences(sharedPreferencesPlace, databaseVersion);
 
         List<String> tables = new ArrayList<String>();
         List<String> sqlQueries = new ArrayList<String>();
 
         for (Class classEntity : classes) {
             StringBuilder sqlQueryBuilder = new StringBuilder();
             String table = SimpleDatabaseUtil.getTableName(classEntity);
             sqlQueryBuilder.append(String.format(SimpleConstants.SQL_CREATE_TABLE_IF_NOT_EXIST, table));
 
             List<Field> primaryKeys = new ArrayList<Field>();
 
             int tableFieldsCount = classEntity.getDeclaredFields().length; // remove no annotation fields
             for (Field field : classEntity.getDeclaredFields()) {
                 if (field.getAnnotation(Column.class) == null) {
                     tableFieldsCount--;
                 }
             }
 
             int annotatedFieldsIndex = 0;
 
             for (int i = 0; i < classEntity.getDeclaredFields().length; i++) {
 
                 Field fieldEntity = classEntity.getDeclaredFields()[i];
                 Column fieldEntityAnnotation = fieldEntity.getAnnotation(Column.class);
                 String column = SimpleDatabaseUtil.getColumnName(fieldEntity);
 
                 if (fieldEntityAnnotation != null) { // if field what we need annotated
 
                     if (fieldEntityAnnotation.isPrimaryKey()) {
 
                         primaryKeys.add(fieldEntity);
 
                     } else {
 
                         sqlQueryBuilder.append(String.format(SimpleConstants.FORMAT_TWINS,
                                 column, SimpleDatabaseUtil.getSQLType(fieldEntity, fieldEntityAnnotation)));
 
                         isAddedSQLDivider = false;
 
                         if (fieldEntityAnnotation.isAutoincrement()) {
                             sqlQueryBuilder.append(SimpleConstants.SPACE);
                             sqlQueryBuilder.append(SimpleConstants.AUTOINCREMENT);
                         }
 
                         if (annotatedFieldsIndex != tableFieldsCount - 1) {
                             isAddedSQLDivider = true;
                             sqlQueryBuilder.append(SimpleConstants.DIVIDER);
                             sqlQueryBuilder.append(SimpleConstants.SPACE);
                         }
 
                     }
 
                     annotatedFieldsIndex++;
 
                 }
             }
 
             makeKeyForTable(sqlQueryBuilder, primaryKeys);
 
             sqlQueryBuilder.append(SimpleConstants.LAST_BRACKET);
 
             tables.add(table);
             sqlQueries.add(sqlQueryBuilder.toString());
         }
 
         boolean newDatabaseVersion = false;
 
         if (databaseVersion > sharedPreferencesUtil.getDatabaseVersion(sharedPreferencesPlace))
             newDatabaseVersion = true;
 
         boolean isRebasedTables = false;
 
         if (!newDatabaseVersion) {
 
             isRebasedTables = rebaseTablesIfNeed(savedTables, tables, sqlQueries, savedSQLQueries);
             if (savedSQLQueries.hashCode() != sqlQueries.hashCode() && savedSQLQueries.hashCode() != 1) {
                 addNewColumnsIfNeed(tables, sqlQueries, savedSQLQueries);
             }
 
         }
 
         if (!isRebasedTables) {
             checkingCommit(tables, sqlQueries, newDatabaseVersion);
         }
 
     }
 
     private void makeKeyForTable(StringBuilder sqlQueryBuilder, List<Field> primaryKeys) {
 
         if (!isAddedSQLDivider && !sqlQueryBuilder.toString().endsWith(SimpleConstants.FIRST_BRACKET)) {
             sqlQueryBuilder.append(SimpleConstants.DIVIDER);
             sqlQueryBuilder.append(SimpleConstants.SPACE);
         }
 
         if (primaryKeys.size() == 0) {
 
             sqlQueryBuilder.append(SimpleConstants.ID_COLUMN);
             sqlQueryBuilder.append(SimpleConstants.SPACE);
             sqlQueryBuilder.append(ColumnType.INTEGER);
             sqlQueryBuilder.append(SimpleConstants.SPACE);
             sqlQueryBuilder.append(SimpleConstants.PRIMARY_KEY);
             sqlQueryBuilder.append(SimpleConstants.SPACE);
             sqlQueryBuilder.append(SimpleConstants.AUTOINCREMENT);
 
         } else if (primaryKeys.size() == 1) {
 
             Field fieldEntity = primaryKeys.get(0);
             String column = SimpleDatabaseUtil.getColumnName(fieldEntity);
             Column fieldEntityAnnotation = fieldEntity.getAnnotation(Column.class);
             sqlQueryBuilder.append(String.format(SimpleConstants.FORMAT_TWINS,
                     column, SimpleDatabaseUtil.getSQLType(fieldEntity, fieldEntityAnnotation)));
 
             sqlQueryBuilder.append(SimpleConstants.SPACE);
             sqlQueryBuilder.append(SimpleConstants.PRIMARY_KEY);
 
             if (fieldEntityAnnotation.isAutoincrement()) {
                 sqlQueryBuilder.append(SimpleConstants.SPACE);
                 sqlQueryBuilder.append(SimpleConstants.AUTOINCREMENT);
             }
 
         } else {
 
             StringBuilder primaryKeysBuilder = new StringBuilder();
             boolean isFirst = true;
 
             for (Field fieldEntity : primaryKeys) {
 
                 String column = SimpleDatabaseUtil.getColumnName(fieldEntity);
                 Column fieldEntityAnnotation = fieldEntity.getAnnotation(Column.class);
                 sqlQueryBuilder.append(String.format(SimpleConstants.FORMAT_TWINS,
                         column, SimpleDatabaseUtil.getSQLType(fieldEntity, fieldEntityAnnotation)));
 
                 sqlQueryBuilder.append(SimpleConstants.DIVIDER);
                 sqlQueryBuilder.append(SimpleConstants.SPACE);
 
                 if (!isFirst) {
                     primaryKeysBuilder.append(SimpleConstants.DIVIDER);
                     primaryKeysBuilder.append(SimpleConstants.SPACE);
                 }
 
                 primaryKeysBuilder.append(column);
 
                 isFirst = false;
 
             }
 
             sqlQueryBuilder.append(SimpleConstants.PRIMARY_KEY);
             sqlQueryBuilder.append(SimpleConstants.SPACE);
             sqlQueryBuilder.append(String.format(SimpleConstants.FORMAT_BRACKETS, primaryKeysBuilder.toString()));
 
         }
 
     }
 
     // Delete extra tables
     private boolean rebaseTablesIfNeed(List<String> savedTables, List<String> tables,
                                        List<String> sqlQueries, List<String> savedSQLQueries) {
 
         List<String> extraTables = new ArrayList<String>(savedTables); // possible extra tables
         extraTables.removeAll(tables);
 
         if (extraTables.size() != 0) { // drop tables
             List<String> extraSqlQueries = new ArrayList<String>(savedSQLQueries); // extra SQL queries
             extraSqlQueries.removeAll(sqlQueries);
 
             SQLiteDatabase database = sqLiteSimpleHelper.getWritableDatabase();
             for (String extraTable : extraTables) {
                 database.execSQL(String.format(SimpleConstants.FORMAT_TWINS,
                         SimpleConstants.SQL_DROP_TABLE_IF_EXISTS, extraTable));
             }
 
             commit(tables, sqlQueries);
             sqLiteSimpleHelper.onCreate(database);
             database.close();
             return true;
         }
 
         List<String> tablesToCreate = new ArrayList<String>(tables); // possible tables to need create
         tablesToCreate.removeAll(savedTables);
 
         if (tablesToCreate.size() != 0) {
             List<String> sqlQueriesToCreate = new ArrayList<String>(sqlQueries); // extra SQL queries
             sqlQueriesToCreate.removeAll(savedSQLQueries);
 
             SQLiteDatabase database = sqLiteSimpleHelper.getWritableDatabase();
             for (String sqlQuery : sqlQueriesToCreate) {
                 database.execSQL(sqlQuery);
             }
 
             database.close();
         }
 
         return false;
     }
 
     private boolean addNewColumnsIfNeed(List<String> tables, List<String> sqlQueries, List<String> savedSqlQueries) {
         try {
             // if change name of column or add new column, or delete
             boolean isAddNewColumn = false;
             for (int i = 0; i < tables.size(); i++) {
                 String table = tables.get(i);
                 for (String savedSqlQuery : savedSqlQueries) {
                     if (savedSqlQuery.contains(table)) {
 
                         List<String> savedColumns = Arrays.asList(savedSqlQueries.get(i).
                                 replace(String.format(SimpleConstants.SQL_CREATE_TABLE_IF_NOT_EXIST, table), SimpleConstants.EMPTY).
                                 replace(SimpleConstants.LAST_BRACKET, SimpleConstants.EMPTY).
                                split(SimpleConstants.DIVIDER));
 
                         List<String> columns = Arrays.asList(sqlQueries.get(i).
                                 replace(String.format(SimpleConstants.SQL_CREATE_TABLE_IF_NOT_EXIST, table), SimpleConstants.EMPTY).
                                 replace(SimpleConstants.LAST_BRACKET, SimpleConstants.EMPTY).
                                split(SimpleConstants.DIVIDER));
 
                         if (columns.size() > savedColumns.size()) {
 
                             List<String> extraColumns = new ArrayList<String>(columns);
                             extraColumns.removeAll(savedColumns);
 
                             SQLiteDatabase database = sqLiteSimpleHelper.getWritableDatabase();
                             for (String column : extraColumns) {
                                 database.execSQL(String.format(SimpleConstants.SQL_ALTER_TABLE_ADD_COLUMN, table, column));
                             }
                             database.close();
 
                             isAddNewColumn = true;
                         }
                     }
                 }
             }
 
             return isAddNewColumn;
 
         } catch (IndexOutOfBoundsException exception) {
             throw new IndexOutOfBoundsException("Duplicated class on method create(...)");
         }
 
     }
 
 }
