 package fedora.server.utilities;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
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
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002, 2003 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
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
             if (st!=null) {
                 try {
                     st.close();
                 } catch (SQLException sqle) { }
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
         if (r != null) r.close();
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
