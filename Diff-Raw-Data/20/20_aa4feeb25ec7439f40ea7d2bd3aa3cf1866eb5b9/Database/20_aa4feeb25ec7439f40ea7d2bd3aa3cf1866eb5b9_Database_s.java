 package com.nigorojr.typebest;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Iterator;
 
 public abstract class Database {
     private String tableName;
 
     private final String protocol = "jdbc:derby:";
     private final String databaseFileName = "typebestDatabase.db";
     private final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
     // Create database file if it doesn't exist
     private boolean create = true;
 
     private Connection connection = null;
     protected Statement statement = null;
 
     /**
      * Connects to the database file. Creates a new database file if none exists
      * and `create` is true.
      * 
      * @param tableName
      *            The name of the table that will be operated using this
     *            instance.
      * @throws SQLException
      *             When the constructor failed to create a new database file.
      */
     public Database(String tableName) throws SQLException {
        this.tableName = tableName;
 
         try {
             Class.forName(driver).newInstance();
         }
         catch (InstantiationException e) {
             e.printStackTrace();
         }
         catch (IllegalAccessException e) {
             e.printStackTrace();
         }
         catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
 
         connection = DriverManager.getConnection(protocol + databaseFileName
                 + ";create=" + create);
         statement = connection.createStatement();
     }
 
     /**
      * Used to check whether the table exists in the database file.
      * 
      * @return True if a table exists, false otherwise.
      */
     public boolean isTableExist() {
         boolean tableExist = false;
 
         try {
             DatabaseMetaData meta = connection.getMetaData();
             ResultSet result = meta.getTables(null, null, tableName, null);
             tableExist = result.next();
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
 
         return tableExist;
     }
 
     /**
      * Creates a table with the name given to the constructor.
      * 
      * @param pair
      *            A HashMap with name of the column as the key and the data type
      *            as the value.
      * @return True if the table was successfully created, false if something
      *         went wrong.
      */
     public boolean createTable(HashMap<String, String> pair) {
         String query = "";
         String columns = "";
 
         Iterator<String> iterator = pair.keySet().iterator();
         while (iterator.hasNext()) {
             String columnName = iterator.next();
             String dataType = pair.get(columnName);
             columns += columnName + " " + dataType;
 
             // Add a comma if it's not the last column
             if (iterator.hasNext())
                 columns += ",";
         }
         // Don't accept empty HashMap
         if (columns.equals(""))
             return false;
 
         query = String.format("CREATE TABLE %s (%s)", tableName, columns);
 
         try {
             statement.execute(query);
         }
         catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 }
