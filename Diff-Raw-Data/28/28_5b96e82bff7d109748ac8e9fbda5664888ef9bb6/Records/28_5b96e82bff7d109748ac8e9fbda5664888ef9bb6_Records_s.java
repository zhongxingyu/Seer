 package com.nigorojr.typebest;
 
 import java.sql.SQLException;
 import java.util.LinkedHashMap;
 
 public class Records extends Database {
 
     public static final String tableName = "RECORDS";
 
     private static LinkedHashMap<String, String> columnNamesAndTypes = new LinkedHashMap<String, String>() {
         {
            put("RECORD_ID", "INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)");
             put("USER_ID", "INT NOT NULL");
             put("USERNAME", "VARCHAR(100) NOT NULL");
             put("KEYBOARD_LAYOUT", "VARCHAR(100)");
             put("TIME", "INT");
             put("MISS_TYPES", "INT");
             put("DATE", "TIMESTAMP");
             put("CONSTRAINT primary_key", "PRIMARY KEY (id)");
         }
     };
 

     public Records() throws SQLException {
         super(tableName, columnNamesAndTypes);
     }
 }
