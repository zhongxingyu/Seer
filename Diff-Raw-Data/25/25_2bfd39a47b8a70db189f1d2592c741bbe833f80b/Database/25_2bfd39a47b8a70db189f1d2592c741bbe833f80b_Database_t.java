 package edu.rivfader.data;
 
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Class which encapsulates access to the database files.
  * @author harald
  */
 public class Database {
     /**
      * loads the table and returns an iterator over the rows.
      * @param tableName the name of the table to load
      * @return a row iterator.
      */
     public Iterator<Row> loadTable(final String tableName) {
         return null;
     }
 
     /**
      * creates a new table.
      * @param tableName the name of the table to create.
      * @param columnNames the names of the columns, the type is varchar.
      */
     public void createTable(final String tableName,
                             final List<String> columnNames) {
     }
 
     /**
      * deletes a table if it exists.
      * @param tableName the name of the table to delete.
      */
     public void dropTable(final String tableName) {
     }
 
     /**
      * stores a row in a table.
      * @param tableName the name of the table to store to.
      * @param row the row to store.
      */
     public void storeRow(final String tableName, final Row row) {
     }
 
     /**
      * opens a table for writing.
      * @param tableName the table to open
      */
     public void openTableForWriting(final String tableName) {
     }
 
     /**
      * closes the table.
      * @param tableName the table to close
      */
     public void closeTable(final String tableName) {
     }
 
     /**
      * appends a row to a table.
      * @param tableName the table to append to
      * @param row the row to append
      */
     public void appendRow(final String tableName, final Row row) {
     }
 
     /**
      * returns the list of column names of a table.
      * @param tableName the name of the table
      */
    public List<String> getColumnNames(final String tableName) {
         return null;
     }
 }
