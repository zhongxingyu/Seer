 package com.nigorojr.typebest;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 
 public abstract class Database {
     private String tableName;
 
     private final String protocol = "jdbc:derby:";
     private final String databaseDirName = "typebest.db";
     private final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
     // Create database file if it doesn't exist
     private boolean create = true;
 
     private Connection connection = null;
     protected Statement statement = null;
 
     /**
      * Establishes a connection with the database.
      */
     public Database() {
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
     }
 
     /**
      * Connects to the database. Creates a new database if none exists
      * and <code>create</code> is true. This constructor creates the table with
      * the given table name if that table does not exist in the database.
      * 
      * @param tableName
      *            The name of the table that will be operated using this
      *            instance. The tableName will be upper-cased.
      * @param columnNamesAndTypes
      *            Set of names and data types of the columns in the table.
      * @throws SQLException
      *             When the constructor failed to create a new database.
      */
     public Database(String tableName,
             LinkedHashMap<String, String> columnNamesAndTypes)
             throws SQLException {
         this(tableName, columnNamesAndTypes, "");
     }
 
     /**
      * 
      * Connects to the database. Creates a new database if none exists
      * and <code>create</code> is true. This constructor creates the table with
      * the given table name if that table does not exist in the database.
      * 
      * @param tableName
      *            The name of the table that will be operated using this
      *            instance. The tableName will be upper-cased.
      * @param columnNamesAndTypes
      *            Set of names and data types of the columns in the table.
      * @param primaryKey
      *            The name of the column that will be used as the primary key.
      * @throws SQLException
      *             When the constructor failed to create a new database.
      */
     public Database(String tableName,
             LinkedHashMap<String, String> columnNamesAndTypes, String primaryKey)
             throws SQLException {
 
         this.tableName = tableName.toUpperCase();
 
         connection = DriverManager.getConnection(String.format(
                 "%s%s;create=%s", protocol, databaseDirName,
                 Boolean.toString(create)));
         statement = connection.createStatement();
         if (!isTableExist()) {
             createTable(columnNamesAndTypes);
             if (!primaryKey.equals("")) {
                 String command = String
                         .format("ALTER TABLE %s ADD CONSTRAINT %s_PRIMARY_KEY PRIMARY KEY(%s)",
                                 tableName, tableName, primaryKey);
                 statement.execute(command);
             }
         }
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
      * @param columnNamesAndTypes
      *            A HashMap with name of the column as the key and the data type
      *            as the value.
      * @return True if the table was successfully created, false if something
      *         went wrong.
      */
     public boolean createTable(LinkedHashMap<String, String> columnNamesAndTypes) {
         String query = "";
         String columns = "";
 
         Iterator<String> iterator = columnNamesAndTypes.keySet().iterator();
         while (iterator.hasNext()) {
             String columnName = iterator.next();
             String dataType = columnNamesAndTypes.get(columnName);
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
 
     /**
      * Getter method for tableName.
      * 
      * @return The table name of this instance.
      */
     public String getTableName() {
         return tableName;
     }
 
     /**
      * Executes the INSERT command for the given pairs of column names and
      * values.
      * 
      * @param columnNamesAndValues
      *            The set of column names and values that will be inserted to
      *            the table.
      */
     public void insert(LinkedHashMap<String, String> columnNamesAndValues) {
         String query = String.format("INSERT INTO %s %s", tableName,
                 formatInsertQuery(columnNamesAndValues));
         try {
             statement.execute(query);
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Executes the SELECT command and queries the database for the given
      * condition.
      * 
      * @param selectColumns
      *            A list of column names that will be selected.
      * @param condition
      *            The condition of the query. This parameter can be empty if
      *            there is no condition. The String should start with the
      *            statement, in other words, it should be something like
      *            "WHERE foo LIKE '%.txt'" or "ORDER BY date DESC"
      * @return The result of the query as a ResultSet object.
      */
     public ResultSet select(String[] selectColumns, String condition) {
         ResultSet result = null;
 
         // join columns to be selected with commas
         String columns = "";
         for (int i = 0; i < selectColumns.length; i++) {
             columns += selectColumns[i];
             if (i < selectColumns.length - 1)
                 columns += ", ";
         }
 
         String query = String.format("SELECT %s FROM %s", columns, tableName);
         // Append condition if it's not empty
         if (!condition.equals(""))
             query += String.format(" %s", condition);
 
         try {
             result = statement.executeQuery(query);
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         return result;
     }
 
     /**
      * Returns all entries in the matching columns in the table.
      * 
      * @param selectColumns
      *            The name of the columns to be selected.
      * @return All entries (rows) in that column.
      */
     public ResultSet select(String[] selectColumns) {
         return select(selectColumns, "");
     }
 
     /**
      * Allow column format in String instead of array of String. This can be
      * used to select all columns by making <code>selectColumns</code> "*" or by
      * manually separating the column names with commas e.g. "foo,bar,baz"
      * 
      * @param selectColumns
      *            A list of column names that will be selected. This can be "*"
      *            to select all the columns in the table.
      * @param condition
      *            The condition starting from a keyword. For example, to select
      *            all rows where the USERNAME is "foo", this variable should be
      *            "WHERE USERNAME = 'foo'".
      * @return A ResultSet object containing the result of the query.
      */
     public ResultSet select(String selectColumns, String condition) {
         return select(new String[] { selectColumns }, condition);
     }
 
     /**
      * Allow column format in String instead of array of String. This can be
      * used to select all columns by making <code>selectColumns</code> "*" or by
      * manually separating the column names with commas e.g. "foo,bar,baz"
      * 
      * @param selectColumns
      *            A list of column names that will be selected. This can be "*"
      *            to select all the columns in the table.
      * @return A ResultSet object containing the result of the query.
      */
     public ResultSet select(String selectColumns) {
         return select(new String[] { selectColumns });
     }
 
     /**
      * Executes the UPDATE command for the table.
      * 
      * @param columnNamesAndValues
      *            The name of the column and the value to be updated to.
      * @param condition
      *            The condition for which column should be updated. The String
      *            will typically start with "WHERE ..." for the sake of
      *            consistency with the select method. For example, if you want
      *            to update a certain ID, this variable will be something like:
      *            "WHERE id = 12345"
      */
     public void update(LinkedHashMap<String, String> columnNamesAndValues,
             String condition) {
         String columns = "";
         Iterator<String> keys = columnNamesAndValues.keySet().iterator();
         while (keys.hasNext()) {
             String key = keys.next();
             columns += String.format("%s = %s", key,
                     columnNamesAndValues.get(key));
             if (keys.hasNext())
                 columns += ", ";
         }
 
         try {
             statement.execute(String.format("UPDATE %s SET %s %s",
                     tableName, columns, condition));
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Executes the DELETE SQL command, deleting the matching rows from the
      * table.
      * 
      * @param condition
      *            The condition beginning from a keyword such as "WHERE".
      */
     public void delete(String condition) {
         try {
            statement.execute(String.format("DELETE FROM %s %s",
                    tableName, condition));
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * This method is used to obtain a formatted String that can be used when
      * using the INSERT command. For example, given the HashMap with the
      * following entries:
      * 
      * username => 'test user'
      * id => 2
      * layout => 'QWERTY'
      * 
      * the method will return a String:
      * <code>(username, id, layout) VALUES ('test user', 2, 'QWERTY')</code>
      * 
      * @param pair
      *            The column names and the values that will be used to generate
      *            the String.
      * @return The formatted String in the form:
      *         <code>(col1, col2, col3...) VALUES (val1, val2, val3...)</code>
      */
     public String formatInsertQuery(LinkedHashMap<String, String> pair) {
         String ret = "";
         String columnNames = "";
         String values = "";
 
         Iterator<String> keys = pair.keySet().iterator();
         while (keys.hasNext()) {
             String key = keys.next();
             columnNames += key;
             values += pair.get(key);
             // Append comma if it's not the last element
             if (keys.hasNext()) {
                 columnNames += ", ";
                 values += ", ";
             }
         }
         ret = String.format("(%s) VALUES (%s)", columnNames, values);
         return ret;
     }
 
     /**
      * Creates a LinkedHashMap that can be used to INSERT/UPDATE database. This
      * method will use the column names defined in the columnNamesAndTypes to
      * ensure correctness of the column names. If the number of values is not
      * exactly the same as the number of columns, the excessive columns/values
      * will be truncated.
      * 
      * @param values
      *            The values in the order of columns.
      * @return LinkedHashMap with the column names as the keys and the values as
      *         hash values.
      */
     public LinkedHashMap<String, String> createLinkedHashMap(
             LinkedHashMap<String, String> columnNamesAndTypes, String[] values) {
         LinkedHashMap<String, String> ret = new LinkedHashMap<String, String>();
         Iterator<String> it = columnNamesAndTypes.keySet().iterator();
 
         int i = 0;
         while (it.hasNext() && i < values.length) {
             ret.put(it.next(), values[i]);
             i++;
         }
 
         return ret;
     }
 }
