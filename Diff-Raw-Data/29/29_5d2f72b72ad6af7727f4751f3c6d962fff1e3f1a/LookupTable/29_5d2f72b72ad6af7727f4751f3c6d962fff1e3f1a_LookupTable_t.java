 package com.com.goldmanalpha.dailydo.db;
 
 import android.database.sqlite.SQLiteDatabase;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Aaron
  * Date: 1/6/12
  * Time: 5:36 AM
  * To change this template use File | Settings | File Templates.
  */
 public class LookupTable extends TableBase {
 
 
     int createInVersion;
 
     public static LookupTable getItemCategoryTable() {
         return new LookupTable("ItemCategory", 11);
     }
 
     private LookupTable(String tableName, int createInVersion) {
         super(tableName);
 
         this.createInVersion = createInVersion;
     }
 
     public String createTableSql(String extraColumns) {
 
         //todo: add ordering
         return super.databaseCreateSql()
                 + extraColumns
                 + "name text not null, "
                 + "description text null "
                 + ");";
     }
 
    @Override
    protected String databaseCreateSql() {
        //since lookup tables will be created at specific version numbers
        //don't do anything at the default database create time
        return null;

    }
 
     @Override
     protected String databaseUpgradeSql(int newVersion) {
 
         if (newVersion == createInVersion) {
             return createTableSql("");
 
         }
         return null;
     }
 
 
 }
