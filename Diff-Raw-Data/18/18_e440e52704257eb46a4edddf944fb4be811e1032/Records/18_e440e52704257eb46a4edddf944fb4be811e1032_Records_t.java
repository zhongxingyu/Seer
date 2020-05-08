 package com.nigorojr.typebest;
 
 import java.sql.SQLException;
 
 public class Records extends Database {
 
    public static final String tableName = "RECORDS";

     public Records() throws SQLException {
        super(tableName);
     }
 }
