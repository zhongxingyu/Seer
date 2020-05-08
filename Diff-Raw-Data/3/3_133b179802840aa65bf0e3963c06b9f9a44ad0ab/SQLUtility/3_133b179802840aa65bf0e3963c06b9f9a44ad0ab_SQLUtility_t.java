 package fedora.server.utilities;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.sql.Clob;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import fedora.server.config.DatastoreConfiguration;
 import fedora.server.config.ModuleConfiguration;
 import fedora.server.config.ServerConfiguration;
 import fedora.server.errors.InconsistentTableSpecException;
 import fedora.server.storage.ConnectionPool;
 
 /**
  * SQL-related utility methods.
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public abstract class SQLUtility {
 
     /** Logger for this class. */
     private static final Logger LOG = Logger.getLogger(
             SQLUtility.class.getName());
 
     public static ConnectionPool getConnectionPool(ServerConfiguration fcfg) throws SQLException {
         ModuleConfiguration mcfg = fcfg.getModuleConfiguration("fedora.server.storage.ConnectionPoolManager");
         String defaultPool = mcfg.getParameter("defaultPoolName").getValue();
         DatastoreConfiguration dcfg = fcfg.getDatastoreConfiguration(defaultPool);
         return getConnectionPool(dcfg);
     }
     
     public static ConnectionPool getConnectionPool(DatastoreConfiguration cpDC) throws SQLException {
         String cpUsername = cpDC.getParameter("dbUsername").getValue();
         String cpPassword = cpDC.getParameter("dbPassword").getValue();
         String cpURL = cpDC.getParameter("jdbcURL").getValue();
         String cpDriver = cpDC.getParameter("jdbcDriverClass").getValue();
         String cpDDLConverter = cpDC.getParameter("ddlConverter").getValue();
         int cpMaxActive = Integer.parseInt(cpDC.getParameter("maxActive").getValue());
         int cpMaxIdle = Integer.parseInt(cpDC.getParameter("maxIdle").getValue());
         long cpMaxWait = Long.parseLong(cpDC.getParameter("maxWait").getValue()); 
         int cpMinIdle = Integer.parseInt(cpDC.getParameter("minIdle").getValue());
         long cpMinEvictableIdleTimeMillis = Long.parseLong(cpDC.getParameter("minEvictableIdleTimeMillis").getValue());
         int cpNumTestsPerEvictionRun = Integer.parseInt(cpDC.getParameter("numTestsPerEvictionRun").getValue());
         long cpTimeBetweenEvictionRunsMillis = Long.parseLong(cpDC.getParameter("timeBetweenEvictionRunsMillis").getValue());
         boolean cpTestOnBorrow = Boolean.parseBoolean(cpDC.getParameter("testOnBorrow").getValue());
         boolean cpTestOnReturn = Boolean.parseBoolean(cpDC.getParameter("testOnReturn").getValue());
         boolean cpTestWhileIdle = Boolean.parseBoolean(cpDC.getParameter("testWhileIdle").getValue());
         byte cpWhenExhaustedAction = Byte.parseByte(cpDC.getParameter("whenExhaustedAction").getValue());
         
         DDLConverter ddlConverter = null;
         if (cpDDLConverter != null) {
             try {
                 ddlConverter=(DDLConverter) Class.forName(cpDDLConverter).newInstance();
             } catch (InstantiationException e) {
                 e.printStackTrace();
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             }
         }
         return new ConnectionPool(cpDriver, cpURL, cpUsername, 
                 cpPassword, ddlConverter, cpMaxActive, cpMaxIdle, 
                 cpMaxWait, cpMinIdle, cpMinEvictableIdleTimeMillis, 
                 cpNumTestsPerEvictionRun, cpTimeBetweenEvictionRunsMillis, 
                 cpTestOnBorrow, cpTestOnReturn, cpTestWhileIdle, 
                 cpWhenExhaustedAction);
     }
     
     public static void replaceInto(Connection conn, String tableName,
             String[] columns, String[] values, String uniqueColumn)
             throws SQLException {
         replaceInto(conn, tableName, columns, values, uniqueColumn, null);
     }
 
     /**
      * Adds or replaces a row in the given table.
      *
      * @param conn the connection to use
      * @param table the name of the table
      * @param columns the names of the columns whose values we're setting.
      * @param values associated values
      * @param uniqueColumn which column name is unique?  The value of this
      *        column will be used in the where clause.  It must be
      *        a column which is not numeric.
      * @param numeric for each associated column, is it numeric?
      *        if null, all columns are assumed to be strings.
      */
     public static void replaceInto(Connection conn, String table,
             String[] columns, String[] values, String uniqueColumn,
             boolean[] numeric)
             throws SQLException {
         if (!updateRow(conn, table, columns, values, uniqueColumn, numeric)) {
             addRow(conn, table, columns, values, numeric);
         }
     }
 
     /**
      * Updates an existing row.
      *
      * @return false if the row did not previously exist and therefore was
      *         not updated.
      */
     public static boolean updateRow(Connection conn, String table,
             String[] columns, String[] values, String uniqueColumn,
             boolean[] numeric)
             throws SQLException {
 
         // prepare update statement
         StringBuffer sql = new StringBuffer();
         sql.append("UPDATE " + table + " SET ");
         boolean needComma = false;
         for (int i = 0; i < columns.length; i++) {
             if (!columns[i].equals(uniqueColumn)) {
                 if (needComma) {
                     sql.append(", ");
                 } else {
                     needComma = true;
                 }
                 sql.append(columns[i] + " = ");
                 if (values[i] == null) {
                     sql.append("NULL");
                 } else {
                     sql.append("?");
                 }
             }
         }
         sql.append(" WHERE " + uniqueColumn + " = ?");
         LOG.debug("About to execute: " + sql.toString());
         PreparedStatement stmt = conn.prepareStatement(sql.toString());
 
         try {
             // populate values
             int varIndex = 0;
             for (int i = 0; i < values.length; i++) {
                 if (!columns[i].equals(uniqueColumn) && values[i] != null) {
                     varIndex++;
                     if (numeric != null && numeric[i]) {
                         setNumeric(stmt, varIndex, columns[i], values[i]);
                     } else {
                         stmt.setString(varIndex, values[i]);
                     }
                 }
             }
            varIndex++;
            stmt.setString(varIndex,
                            getSelector(columns, values, uniqueColumn));
 
             // execute and return true if existing row was updated
             return stmt.executeUpdate() > 0;
 
         } finally {
             closeStatement(stmt);
         }
     }
 
     /**
      * Adds a new row.
      *
      * @throws SQLException if the row could not be added.
      */
     public static void addRow(Connection conn, String table,
             String[] columns, String[] values, boolean[] numeric)
             throws SQLException {
 
         // prepare insert statement
         StringBuffer sql = new StringBuffer();
         sql.append("INSERT INTO " + table + " (");
         for (int i = 0; i < columns.length; i++) {
             if (i > 0) {
                 sql.append(", ");
             }
             sql.append(columns[i]);
         }
         sql.append(") VALUES (");
         for (int i = 0; i < columns.length; i++) {
             if (i > 0) {
                 sql.append(", ");
             }
             if (values[i] == null) {
                 sql.append("NULL");
             } else {
                 sql.append("?");
             }
         }
         sql.append(")");
         LOG.debug("About to execute: " + sql.toString());
         PreparedStatement stmt = conn.prepareStatement(sql.toString());
 
         try {
             // populate values
             int varIndex = 0;
             for (int i = 0; i < values.length; i++) {
                 if (values[i] != null) {
                     varIndex++;
                     if (numeric != null && numeric[i]) {
                         setNumeric(stmt, varIndex, columns[i], values[i]);
                     } else {
                         stmt.setString(varIndex, values[i]);
                     }
                 }
             }
 
             // execute
             stmt.executeUpdate();
 
         } finally {
             closeStatement(stmt);
         }
 
     }
 
     /**
      * Sets a numeric value in the prepared statement.
      *
      * Parsing the string is attempted as an int, then
      * a long, and if that fails, a SQLException is thrown.
      */
     private static void setNumeric(PreparedStatement stmt,
             int varIndex, String columnName, String value)
             throws SQLException {
         try {
             stmt.setInt(varIndex, Integer.parseInt(value));
         } catch (NumberFormatException e) {
             try {
                 stmt.setLong(varIndex, Long.parseLong(value));
             } catch (NumberFormatException e2) {
                 throw new SQLException("Value specified for "
                         + columnName + ", '" + value + "' was"
                         + " specified as numeric, but is not");
             }
         }
     }
 
     /** 
      * Gets the value in the given array whose associated column name
      * matches the given uniqueColumn name.
      *
      * @throws SQLException if the uniqueColumn doesn't exist in the given
      *         column array.
      */
     private static String getSelector(String[] columns, String[] values,
             String uniqueColumn)
             throws SQLException {
         String selector = null;
         for (int i = 0; i < columns.length; i++) {
             if (columns[i].equals(uniqueColumn)) {
                 selector = values[i];
             }
         }
         if (selector != null) {
             return selector;
         } else {
             throw new SQLException("Unique column does not exist in given "
                     + "column array");
         }
     }
 
     public static String slashEscaped(String in) {
         StringBuffer out = new StringBuffer();
         for (int i = 0; i < in.length(); i++) {
             char c = in.charAt(i);
             if (c == '\\') {
                 out.append("\\\\");  // slash slash
             } else {
                 out.append(c);
             }
         }
         return out.toString();
     }
 
     /**
      * Get a long string, which could be a TEXT or CLOB type.
      * (CLOBs require special handling -- this method normalizes the reading of them)
      */
     public static String getLongString(ResultSet rs, int pos) throws SQLException {
         String s = rs.getString(pos);
         if (s != null) {
             // It's a String-based datatype, so just return it.
             return s;
         } else {
             // It may be a CLOB.  If so, return the contents as a String.
             try {
                 Clob c = rs.getClob(pos);
                 return c.getSubString(1, (int) c.length());
             } catch (Throwable th) {
                 th.printStackTrace();
                 return null;
             }
         }
     }
 
     public static void createNonExistingTables(ConnectionPool cPool,
             InputStream dbSpec)
             throws IOException, InconsistentTableSpecException, SQLException {
         List nonExisting=null;
         Connection conn=null;
         try {
             conn=cPool.getConnection();
             nonExisting=SQLUtility.getNonExistingTables(conn,
                     TableSpec.getTableSpecs(dbSpec));
         } finally {
             if (conn!=null) {
                 cPool.free(conn);
             }
         }
         if (nonExisting.size()>0) {
             TableCreatingConnection tcConn=null;
             try {
                 tcConn=cPool.getTableCreatingConnection();
                 if (tcConn==null) {
                     throw new SQLException(
                           "Unable to construct CREATE TABLE "
                         + "statement(s) because there is no DDLConverter "
                         + "registered for this connection type.");
                 }
                 SQLUtility.createTables(tcConn, nonExisting);
             } finally {
                 if (tcConn!=null) {
                     cPool.free(tcConn);
                 }
             }
         }
     }
 
     public static List getNonExistingTables(Connection conn,
             List tSpecs)
             throws SQLException {
 
       ArrayList nonExisting=new ArrayList();
       DatabaseMetaData dbMeta=conn.getMetaData();
       Iterator tSpecIter=tSpecs.iterator();
       ResultSet r = null;
       // Get a list of tables that don't exist, if any
       try
       {
         r=dbMeta.getTables(null, null, "%", null);
         HashSet existingTableSet=new HashSet();
         while (r.next()) {
             existingTableSet.add(r.getString("TABLE_NAME").toLowerCase());
         }
         r.close();
         r=null;
         while (tSpecIter.hasNext()) {
             TableSpec spec=(TableSpec) tSpecIter.next();
             if (!existingTableSet.contains(spec.getName().toLowerCase())) {
                 nonExisting.add(spec);
             }
         }
       } catch (SQLException sqle)
       {
         throw new SQLException(sqle.getMessage());
       } finally
       {
         try {
             if (r != null) r.close();
         } catch (SQLException sqle2) {
             throw sqle2;
         } finally {
             r=null;
         }
       }
       return nonExisting;
     }
 
     public static void createTables(TableCreatingConnection tcConn, List tSpecs)
             throws SQLException {
         Iterator nii=tSpecs.iterator();
         while (nii.hasNext()) {
             TableSpec spec=(TableSpec) nii.next();
             if (LOG.isDebugEnabled()) {
                 StringBuffer sqlCmds=new StringBuffer();
                 Iterator iter=tcConn.getDDLConverter().getDDL(spec).iterator();
                 while (iter.hasNext()) {
                     sqlCmds.append("\n");
                     sqlCmds.append((String) iter.next());
                     sqlCmds.append(";");
                 }
                 LOG.debug("Attempting to create nonexisting "
                         + "table '" + spec.getName() + "' with command(s): "
                         + sqlCmds.toString());
             }
             tcConn.createTable(spec);
         }
     }
 
     public static String backslashEscape(String in) {
         if (in==null) return in;
         if (in.indexOf("\\")==-1) return in;
         StringBuffer out=new StringBuffer();
         for (int i=0; i<in.length(); i++) {
           char c=in.charAt(i);
           if (c=='\\') {
             out.append('\\');
           }
           out.append(c);
         }
         return out.toString();
     }
 
     public static String aposEscape(String in) {
         if (in==null) return in;
         if (in.indexOf("'")==-1) return in;
         StringBuffer out=new StringBuffer();
         for (int i=0; i<in.length(); i++) {
             char c=in.charAt(i);
             if (c=='\'') {
                 out.append('\'');
             }
             out.append(c);
         }
         return out.toString();
     }
 
     public static void closeStatement(Statement stmt) {
         if (stmt != null) {
             try {
                 stmt.close();
             } catch (SQLException e) {
                 LOG.warn("Unable to close statement", e);
             }
         }
     }
 
 }
