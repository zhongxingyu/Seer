 /**
  * Bristlecone Test Tools for Databases
  * Copyright (C) 2006-2007 Continuent Inc.
  * Contact: bristlecone@lists.forge.continuent.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of version 2 of the GNU General Public License as
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
  *
  * Initial developer(s): Robert Hodges and Ralph Hannus.
  * Contributor(s):
  */
 
 package com.continuent.bristlecone.croc;
 
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.apache.log4j.Logger;
 
 import com.continuent.bristlecone.benchmark.db.Column;
 import com.continuent.bristlecone.benchmark.db.Table;
 import com.continuent.bristlecone.benchmark.db.TableHelper;
 
 /**
  * Implements a comparator function that can check the contents of 
  * tables in different databases. 
  * 
  * @author rhodges
  */
 public class TableComparator
 {
     private static final Logger logger    = Logger.getLogger(TableComparator.class);
 
     // Parameters.
     private String              masterUrl = null;
     private String              slaveUrl  = null;
     private String              user      = "tungsten";
     private String              password  = "secret";
 
     // Connection parameters.
     private TableHelper         masterTableHelper;
     private Connection          masterConn;
     private Statement           masterStmt;
     private TableHelper         slaveTableHelper;
     private Connection          slaveConn;
     private Statement           slaveStmt;
 
     /** Create a new table comparator. */
     public TableComparator()
     {
     }
 
     public synchronized void setMasterUrl(String masterUrl)
     {
         this.masterUrl = masterUrl;
     }
 
     public synchronized void setSlaveUrl(String slaveUrl)
     {
         this.slaveUrl = slaveUrl;
     }
 
     public synchronized void setUser(String user)
     {
         this.user = user;
     }
 
     public synchronized void setPassword(String password)
     {
         this.password = password;
     }
 
     /**
      * Perform basic initialization.
      */
     public void prepare() throws Exception
     {
         // Open connection to master.
         masterTableHelper = new TableHelper(masterUrl, user, password);
         masterConn = masterTableHelper.getConnection();
         masterStmt = masterConn.createStatement();
 
         // Open connection to slave.
         slaveTableHelper = new TableHelper(slaveUrl, user, password);
         slaveConn = slaveTableHelper.getConnection();
         slaveStmt = slaveConn.createStatement();
     }
 
     /**
      * Compare tables on master and slave using a match-merge approach. 
      * 
      * @param table Table to compare
      * @return True if comparison succeeds, otherwise false
      */
     public boolean compare(Table table) throws CrocException
     {
         logger.debug("Comparing table: " + table.getName());
         ResultSet masterRs = null;
         ResultSet slaveRs = null;
         int row = 0;
         try
         {
             // Select on master.
             String masterSelect = masterTableHelper.getSqlDialect()
                     .getSelectAllSorted(table);
             masterRs = masterStmt.executeQuery(masterSelect);
 
             // Select on slave.
             String slaveSelect = slaveTableHelper.getSqlDialect()
                     .getSelectAllSorted(table);
             slaveRs = slaveStmt.executeQuery(slaveSelect);
 
             // Loop through master results.
             while (masterRs.next())
             {
                 row++;
                 String masterRow = getRowResult(table, masterRs);
                 if (slaveRs.next())
                 {
                     String slaveRow = getRowResult(table, slaveRs);
                     if (masterRow.equals(slaveRow))
                     {
                         if (logger.isDebugEnabled())
                         {
                             logger.debug("ROW: " + row);
                             logger.debug("MASTER: " + masterRow);
                             logger.debug("SLAVE : " + slaveRow);
                         }
                     }
                     else
                     {
                         logger.error("Master and slave differ: table="
                                 + table.getName() + " row=" + row);
                         logger.info("MASTER: " + masterRow);
                         logger.info("SLAVE : " + slaveRow);
                         return false;
                     }
                 }
                 else
                 {
                     logger.error("Master is larger than slave: table="
                             + table.getName() + " row=" + row);
                     logger.info("MASTER: " + masterRow);
                     return false;
                 }
             }
 
             // If there are any remaining slave rows, slave has more rows than
             // master.
             if (slaveRs.next())
             {
                 row++;
                 logger.error("Slave is larger than master: table="
                         + table.getName() + " row=" + row);
                 String slaveRow = getRowResult(table, slaveRs);
                 logger.info("SLAVE: " + slaveRow);
                 return false;
             }
 
             // If there are no rows at all that is bad.
             if (row == 0)
             {
                 logger.error("Table is empty on master and slave: table="
                         + table.getName());
                 return false;
             }
         }
         catch (SQLException e)
         {
             throw new CrocException("Comparison failed: " + e.getMessage(), e);
         }
         finally
         {
             closeResultSet(masterRs);
             closeResultSet(slaveRs);
         }
 
         // If we get this far, the tables are the same.
         logger.debug("Rows compared: " + row);
         return true;
     }
 
     // Reduce a row to a string consisting of name=value pairs with a delimiter
     // between each column.
     private String getRowResult(Table table, ResultSet rs) throws SQLException
     {
         StringBuffer sb = new StringBuffer("[");
         for (Column col : table.getColumns())
         {
             if (sb.length() > 0)
                 sb.append("|");
             sb.append(col.getName());
             sb.append("=");
             Object o = rs.getObject(col.getName());
             if (o == null)
                 sb.append("NULL");
             else if (o instanceof Double)
             {
                 // Doubles have precision issues.  Reduce to 17 places. 
                 double dval = (Double) o;
                 BigDecimal bd = new BigDecimal(dval);
                 bd = bd.setScale(17, BigDecimal.ROUND_HALF_UP);
                 bd.toString();
             }
             else
                 sb.append(o.toString());
         }
         sb.append("]");
         return sb.toString();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see com.continuent.bristlecone.benchmark.Monitor#cleanup()
      */
     public void cleanup()
     {
         closeStatement(masterStmt);
         closeStatement(slaveStmt);
         closeConnection(masterConn);
         closeConnection(slaveConn);
     }
 
     // Private routine to close a JDBC result set.
     private void closeResultSet(ResultSet rs)
     {
         if (rs != null)
         {
             try
             {
                 rs.close();
             }
             catch (SQLException e)
             {
             }
         }
     }
 
     // Private routine to close a JDBC statement.
     private void closeStatement(Statement s)
     {
         if (s != null)
         {
             try
             {
                 s.close();
             }
             catch (SQLException e)
             {
             }
         }
     }
 
     // Private routine to close a JDBC connection.
     private void closeConnection(Connection c)
     {
         if (c != null)
         {
             try
             {
                 c.close();
             }
             catch (SQLException e)
             {
             }
         }
     }
 }
