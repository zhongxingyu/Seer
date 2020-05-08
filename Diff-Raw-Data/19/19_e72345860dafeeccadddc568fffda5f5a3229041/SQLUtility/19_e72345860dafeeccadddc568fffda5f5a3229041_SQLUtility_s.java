 package fedora.server.utilities;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 
 import fedora.server.Logging;
 import fedora.server.errors.InconsistentTableSpecException;
 import fedora.server.storage.ConnectionPool;
 
 /**
  *
  * <p><b>Title:</b> SQLUtility.java</p>
  * <p><b>Description:</b> </p>
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public abstract class SQLUtility {
 
     public static void replaceInto(Connection conn, String tableName,
             String[] columns, String[] values, String uniqueColumn)
             throws SQLException {
         replaceInto(conn, tableName, columns, values, uniqueColumn, null, null);
     }
 
     public static void replaceInto(Connection conn, String tableName,
             String[] columns, String[] values, String uniqueColumn,
             boolean[] isNumeric, Logging log)
             throws SQLException {
         // figure out if we need to escape an apostrophe
         for (int i=0; i<values.length; i++) {
             String val=values[i];
             if (val!=null) {
                 StringBuffer newVal=new StringBuffer();
                 boolean apos=false;
                 for (int x=0; x<val.length(); x++) {
                     char c=val.charAt(x);
                     if (c=='\'') {
                         newVal.append("''");
                         apos=true;
                     } else {
                         newVal.append(c);
                     }
                 }
                 if (apos) {
                     values[i]=newVal.toString();
                 }
             }
         }
         StringBuffer s=new StringBuffer(); // set clause
         s.append("SET ");
         String uVal=null;
         for (int i=0; i<columns.length; i++) {
             if (columns[i].equals(uniqueColumn)) {
                 uVal=values[i];
             }
             if (i>0) {
                 s.append(", ");
             }
             s.append(columns[i]);
             s.append(" = ");
             if (values[i]==null) {
                 s.append("NULL");
             } else {
                 if (isNumeric==null || !isNumeric[i]) {
                     s.append("'");
                 }
                s.append(values[i]);
                 if (isNumeric==null || !isNumeric[i]) {
                     s.append("'");
                 }
             }
         }
         StringBuffer w=new StringBuffer(); // where clause
         w.append("WHERE ");
         w.append(uniqueColumn);
         w.append(" = '");
         w.append(uVal);
         w.append("'");
         StringBuffer u=new StringBuffer(); // update statement
         u.append("UPDATE ");
         u.append(tableName);
         u.append("\n");
         u.append(s.toString());
         u.append("\n");
         u.append(w.toString());
         Statement st=null;
         try {
             st=conn.createStatement();
             if (log!=null) {
                 log.logFiner("SQLUtility.executeUpdate, trying: " + u.toString());
             }
             if (st.executeUpdate(u.toString())==0) {
                 StringBuffer i=new StringBuffer(); // insert statement
                 i.append("INSERT INTO ");
                 i.append(tableName);
                 i.append("\n");
                 i.append("(");
                 for (int x=0; x<columns.length; x++) {
                     if (x>0) {
                         i.append(", ");
                     }
                     i.append(columns[x]);
                 }
                 i.append(") VALUES (");
                 for (int x=0; x<values.length; x++) {
                     if (x>0) {
                         i.append(", ");
                     }
                     if (values[x]==null) {
                         i.append("NULL");
                     } else {
                         if (isNumeric==null || !isNumeric[x]) {
                             i.append("'");
                         }
                        i.append(values[x]);
                         if (isNumeric==null || !isNumeric[x]) {
                             i.append("'");
                         }
                     }
                 }
                 i.append(")");
                 if (log!=null) {
                     log.logFiner("SQLUtility.executeUpdate, now trying: " + i.toString());
                 }
                 st.executeUpdate(i.toString());
             }
         } catch (SQLException sqle) {
             throw sqle;
         } finally {
             try {
                 if (st!=null) st.close();
             } catch (SQLException sqle2) {
                 throw sqle2;
             } finally {
                 st=null;
             }
         }
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
             InputStream dbSpec, Logging log)
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
                 SQLUtility.createTables(tcConn, nonExisting, log);
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
 
     public static void createTables(TableCreatingConnection tcConn, List tSpecs,
             Logging log)
             throws SQLException {
         Iterator nii=tSpecs.iterator();
         while (nii.hasNext()) {
             TableSpec spec=(TableSpec) nii.next();
             if (log.loggingConfig()) {
                 StringBuffer sqlCmds=new StringBuffer();
                 Iterator iter=tcConn.getDDLConverter().getDDL(spec).iterator();
                 while (iter.hasNext()) {
                     sqlCmds.append("\n");
                     sqlCmds.append((String) iter.next());
                     sqlCmds.append(";");
                 }
                 log.logConfig("Attempting to create nonexisting "
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
 
 }
