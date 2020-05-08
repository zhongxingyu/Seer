 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.diff.db;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import java.util.LinkedList;
 import java.util.Properties;
 
 import de.cismet.diff.container.TableColumn;
 
 /**
  * This class uses a single JDBC connection to claim data from the database. It provides information about the names of
  * the tables in the database and provides information about the columns of tables corresponding to the tables name.
  * Moreover it provides a static method to execute update sql statements. It is essential for this class to have <code>
  * runtime.properties</code> that contain the url, the username, the password and the driver class as properties. The
  * package where the <code>runtime.properties</code> can be found and the single connection params has to be altered if
  * they change. They can be altered in the constructor of the class.
  *
  * @author   Martin Scholl
  * @version  1.0 2007-03-08
  */
 public class SimpleTablesDataProvider {
 
     //~ Static fields/initializers ---------------------------------------------
 
     // default type of a table, will probably not have to be altered
     private static final String[] TYPES = { "TABLE" }; // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient Connection con;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new instance of <code>SimpleTablesDataProvider</code> initializing the runtime parameters from the
      * "runtime.properties" file and finally connecting to the database.
      *
      * @param   runtime  DOCUMENT ME!
      *
      * @throws  SQLException  if the database runtime could not be initialized
      */
     public SimpleTablesDataProvider(final Properties runtime) throws SQLException {
         con = DatabaseConnection.getConnection(runtime);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Retrieves all names of tables in the database excluding any system table starting with "cs_" and the two PostGIS
      * tables "spatial_ref_sys" and "geometry_columns".
      *
      * @return  String[] containing the names of the tables in the database
      *
      * @throws  SQLException  if any error occurs when fetching the results
      */
     public String[] getTableNames() throws SQLException {
         ResultSet set = null;
         final LinkedList<String> names = new LinkedList<String>();
         try {
             set = con.getMetaData().getTables(null, null, null, TYPES);
             while (set.next()) {
                 final String table = set.getString(3);
                 if (!(table.startsWith("cs_")                           // NOI18N
                                 || table.equals("spatial_ref_sys")      // NOI18N
                                 || table.equals("geometry_columns"))) { // NOI18N
                    names.add(table);
                 }
             }
             if (names.isEmpty()) {
                 return null;
             }
             final String[] ret = new String[names.size()];
             return names.toArray(ret);
         } finally {
             if (set != null) {
                 set.close();
             }
         }
     }
 
     /**
      * Retrives the name, the type name its size and default value and whether the column is nullable from all columns
      * of the table with the given name.
      *
      * @param   tableName  the name of the table which columns shall be retrieved
      *
      * @return  TableColumn[] containing all the information about the columns of the table
      *
      * @throws  SQLException  if an error occurs during retrieval
      */
     public TableColumn[] getColumns(final String tableName) throws SQLException {
         ResultSet set = null;
         final LinkedList<String> keys = new LinkedList<String>();
         final LinkedList<TableColumn> columns = new LinkedList<TableColumn>();
         try {
            // set = con.getMetaData().getPrimaryKeys(schema, schemaPattern, tableName);
            set = con.getMetaData().getPrimaryKeys(null, null, tableName);
             // 4 = column name
             while (set.next()) {
                 keys.add(set.getString(4));
             }
            // set = con.getMetaData().getColumns(schema, schemaPattern, tableName,null);
            set = con.getMetaData().getColumns(null, null, tableName, null);
             while (set.next()) {
                 // 4 = column name
                 // 6 = type name
                 // 7 = column size
                 // 9 = decimal digits
                 // 11 = nullable
                 // 13 = column default value (may be 'null')
                 if (keys.contains(set.getString(4))) {
                     columns.add(
                         new TableColumn(
                             set.getString(4),
                             set.getString(6),
                             set.getInt(7),
                             set.getInt(9),
                             set.getString(13),
                             set.getShort(11),
                             true));
                 } else {
                     columns.add(
                         new TableColumn(
                             set.getString(4),
                             set.getString(6),
                             set.getInt(7),
                             set.getInt(9),
                             set.getString(13),
                             set.getShort(11),
                             false));
                 }
             }
             if (columns.isEmpty()) {
                 return null;
             }
             final TableColumn[] ret = new TableColumn[columns.size()];
             return columns.toArray(ret);
         } finally {
             if (set != null) {
                 set.close();
             }
         }
     }
 }
