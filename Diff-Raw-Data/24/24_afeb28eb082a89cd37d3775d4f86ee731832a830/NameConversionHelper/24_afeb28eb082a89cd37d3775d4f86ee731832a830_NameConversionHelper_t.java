 package com.blogspot.ostas.dbpojo.utils;
 
 public interface NameConversionHelper {
     String tableNameToClassName(String tableName);
     String columnNameToIdentifier(String columnName);
    String normalizeField(String nameCandidate);
 }
