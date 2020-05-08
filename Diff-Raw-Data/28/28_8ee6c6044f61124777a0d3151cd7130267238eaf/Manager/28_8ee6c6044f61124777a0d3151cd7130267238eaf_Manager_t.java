 package com.sunnyd.database;
 
 import com.sunnyd.database.query.QueryExecutorHook;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 public class Manager
 {
 
     public static void main(String[] args)
     {
         final Logger logger = LoggerFactory.getLogger(Manager.class);
         logger.info("Hello world");
 
         // sample test for CRUD below:
         HashMap<String, Object> map = new HashMap<String, Object>();
         map.put("firstName", "newguy");
         //map.put("id", 3);
 
         // FIND:
         // System.out.println(find(0, "persons"));
 
         // FIND ALL:
 
 //         ArrayList<HashMap<String, Object>> r = findAll("persons", map); for
 //         (HashMap<String, Object> h : r){ for (String key : h.keySet()){
 //         System.out.println(key + ":" + h.get(key)); } }
 
         // SAVE:
         //System.out.println(save("persons", map));
 
         // DESTROY:
          // System.out.println(destroy(3, "persons"));
 
         // UPDATE:
          // System.out.println(update(0, "persons", map));
 
         // converter Java to SQL:
        
 //          HashMap<String,String> c = convertJavaSQL(map); for (Object key :
 //          c.keySet()) { System.out.println(key + " " + c.get(key));
 //          
 //          }
          
         
 
 
     }
 
     // find by id, return single row
     public static HashMap<String, Object> find(int id, String tableName)
     {
         Connection connection = null;
         Statement stmt = null;
         ResultSet rs = null;
 
         try
         {
             connection = Connector.getConnection();
             stmt = connection.createStatement();
             rs = stmt.executeQuery("SELECT * FROM " + tableName
                     + " WHERE ID = " + id);
             
             if (rs.next()){
                 return convertSQLJava(rs);
             }
             
         } catch (SQLException e)
         {
             e.printStackTrace();
         }
         return null;
     }
 
     // find multiple by criteria
     public static ArrayList<HashMap<String, Object>> findAll(String tableName,
             HashMap<String, Object> conditions)
     {
         Connection connection = null;
         Statement stmt = null;
         ResultSet rs = null;
         ArrayList<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
 
         String where = "";
 
         HashMap<String, String> SQLConditions = convertJavaSQL(conditions);
 
         for (String key : SQLConditions.keySet())
         {
             where += key + " = " + SQLConditions.get(key) + " AND ";
         }
         // remove trailing comma
         where = where.replaceAll(" AND $", ""); // col1, col2, col3
 
         if (!where.equals(""))
         {
             where = " WHERE " + where;
         }
 
         try
         {
             connection = Connector.getConnection();
             stmt = connection.createStatement();
             rs = stmt.executeQuery("SELECT * FROM " + tableName + where);
             while (rs.next())
             { 
                 HashMap<String, Object> row = new HashMap<String, Object>();
                 row = convertSQLJava(rs);
                 results.add(row);
             }
         } catch (SQLException e)
         {
             e.printStackTrace();
         }
         return results;
 
     }
 
     public static int save(String tableName, HashMap<String, Object> hashmap)
     {
         QueryExecutorHook.beforeSave(hashmap);
         Connection connection = null;
         Statement stmt = null;
         ResultSet rs = null;
 
         String columns = "";
         String values = "";
 
         int id = 0;
 
         try
         {
             connection = Connector.getConnection();
 
             HashMap<String, String> SQLHashmap = convertJavaSQL(hashmap);
 
             // get column value pairs from hashmap as val,val,val...
             for (String key : SQLHashmap.keySet())
             {
                 columns += key + ",";
                 values += SQLHashmap.get(key) + ",";
             }
             // remove trailing comma
             columns = columns.replaceAll(",$", "");
             values = values.replaceAll(",$", "");
 
             stmt = connection.createStatement();
             stmt.executeUpdate("INSERT INTO " + tableName + " (" + columns
                     + ") VALUES (" + values + ")",
                     Statement.RETURN_GENERATED_KEYS);
 
             /**
              * stmt = connection.prepareStatement("INSERT INTO " + tableName +
              * " (" + columns + ") VALUES (" + values + ")", new String[] { "id"
              * });
              */
 
             rs = stmt.getGeneratedKeys();
             if (rs.next()){
                 id = rs.getInt(1);
             }
             
         } catch (SQLException e)
         {
             e.printStackTrace();
         }
         return id;
     }
 
     public static boolean destroy(int id, String tableName)
     {
         Connection connection = null;
         Statement stmt = null;
         boolean isDestroyed = true;
 
         try
         {
             connection = Connector.getConnection();
             stmt = connection.createStatement();
             stmt.execute("DELETE FROM " + tableName + " WHERE ID = " + id);
         } catch (SQLException e)
         {
             e.printStackTrace();
             isDestroyed = false;
         }
         return isDestroyed;
     }
 
     // update 1 or more fields of a single row
     public static boolean update(int id, String tableName,
             HashMap<String, Object> hashmap)
     {
         Connection connection = null;
         Statement stmt = null;
         boolean isUpdated = true;
 
         try
         {
             connection = Connector.getConnection();
             stmt = connection.createStatement();
 
             HashMap<String, String> SQLHashmap = convertJavaSQL(hashmap);
 
             for (Object key : SQLHashmap.keySet())
             {
                 String column = (String) key;
                 String newvalue = SQLHashmap.get(key);
                 stmt.execute("UPDATE " + tableName + " SET " + column + " = "
                         + newvalue + " WHERE ID = " + id);
             }
 
         } catch (SQLException e)
         {
             e.printStackTrace();
             isUpdated = false;
         }
         return isUpdated;
     }
 
     // helper method to convert java type (string, int, date) to mysql type
     // written in str
     private static HashMap<String, String> convertJavaSQL(
             HashMap<String, Object> original)
     {
         HashMap<String, String> converted = new HashMap<String, String>();
 
         for (String key : original.keySet())
         {
             Class<?> fieldType = original.get(key).getClass();
             String type = fieldType.getSimpleName();
 
             switch (type)
             {
                 case "Boolean":
                     converted
                             .put(key, "'" + original.get(key).toString() + "'");
                     break;
                 case "Integer":
                     converted.put(key,
                             Integer.toString((int) original.get(key)));
                     break;
                 case "Double":
                     converted.put(key,
                             Double.toString((double) original.get(key)));
                     break;
                 case "String":
                     converted.put(key, "'" + original.get(key) + "'");
                     break;
                 case "Date":
                     Date dt = (Date) original.get(key);
                     DateFormat parser = new SimpleDateFormat(
                             "yyyy-mm-dd hh:mm:ss");
                     converted.put(key, "'" + parser.format(dt) + "'");
                     break;
                 default:
                    System.out.println("Manager.java doesnt know this type: " + key + "=" + original.get(key));
                     break;
             }
         }
         return converted;
     }
 
     // convert result got from sql to java type
     private static HashMap<String, Object> convertSQLJava(ResultSet resultset) throws SQLException
     {
         HashMap<String, Object> converted = new HashMap<String, Object>();
         ResultSetMetaData rsmd = resultset.getMetaData();
         int columnCount = rsmd.getColumnCount();
         for (int i = 1; i < columnCount + 1; i++)
         {
             String columnName = rsmd.getColumnName(i);
             String type = rsmd.getColumnTypeName(i);
           
             switch(type){
                 case "INT UNSIGNED":
                    converted.put(columnName, (int) resultset.getLong(columnName));
                     break;
                 case "INT":
                    converted.put(columnName, (Integer) resultset.getInt(columnName));
                     break;
                 case "TINYINT": //boolean
                    converted.put(columnName, resultset.getBoolean(columnName));
                     break;
                 case "VARCHAR":
                    converted.put(columnName, resultset.getString(columnName));
                     break;
                 case "DATETIME":
                    converted.put(columnName, (Date) resultset.getDate(columnName));
                    break;
                case "TIMESTAMP":
                    converted.put(columnName, (Date) resultset.getTimestamp(columnName));
                    break;
                 default:
                    System.out.println("Manager.java doesnt know this type: " + columnName + "=" + type + "=" + resultset.getObject(columnName));
                     break;
             }
             
         }
         
         return converted;
     }
 
 }
