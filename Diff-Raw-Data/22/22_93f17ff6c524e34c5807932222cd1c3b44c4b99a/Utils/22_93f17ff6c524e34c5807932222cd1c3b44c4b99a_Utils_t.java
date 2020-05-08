 package com.googlecode.usc;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.Set;
 
 /**
  *
  * @author ShunLi
  */
 public class Utils {
     public static boolean isBlank(CharSequence cs) {
         int strLen;
         if (cs == null || (strLen = cs.length()) == 0) {
             return true;
         }
         for (int i = 0; i < strLen; i++) {
             if ((Character.isWhitespace(cs.charAt(i)) == false)) {
                 return false;
             }
         }
         return true;
     }
 
     public static String buildInsertSql(Set<String> columnNames, String criteria) {
        StringBuffer insertSql = new StringBuffer("INSERT INTO " + criteria.split("\\s", 2)[0] + " (");
         insertSql.append(buildParams(columnNames, ""));
         insertSql.append(") VALUES (");
         insertSql.append(buildParams(columnNames, ":"));
         insertSql.append(") ");
         return insertSql.toString();
     }
 
     public static Properties loadPropertiesFile(String outSideConfigFile, InputStream inSideConfigFile) {
         Properties prop = new Properties();
 
         InputStream propertiesFile = null;
         try {
             File file = new File(outSideConfigFile);
             if (file.exists()) {
                 propertiesFile = new FileInputStream(file);
             } else {
                 propertiesFile = inSideConfigFile;
             }
 
             if (propertiesFile != null) {
                 prop.load(propertiesFile);
             }
         } catch (IOException e) {
         }
 
         return prop;
     }
 
     public static Properties loadPropertiesFile(InputStream propertiesFileName) {
         Properties prop = new Properties();
 
         InputStream propertiesFile = propertiesFileName;
         try {
             if (propertiesFile != null) {
                 prop.load(propertiesFile);
             }
         } catch (IOException e) {
         }
 
         return prop;
     }
 
     public static Boolean parseString2Boolean(String value, Boolean defaultValue) {
         if ("true".equalsIgnoreCase(value)) {
             return Boolean.TRUE;
         } else if ("false".equalsIgnoreCase(value)) {
             return Boolean.FALSE;
         }
 
         return defaultValue;
     }
 
     public static Long parseString2Long(String value, Long defaultValue) {
         try {
             return Long.parseLong(value);
         } catch (NumberFormatException e) {
         }
 
         return defaultValue;
     }
 
     private static String buildParams(Set<String> columnNames, String prefix) {
         StringBuffer params = new StringBuffer();
 
         boolean isFirstColumn = true;
         for (String columnName : columnNames) {
             if (!isFirstColumn) {
                 params.append(", " + prefix + columnName);
             } else {
                 params.append(prefix + columnName);
                 isFirstColumn = false;
             }
         }
 
         return params.toString();
     }
 
 }
