 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.axiom.sql;
 
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Types;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

 import com.netspective.axiom.ConnectionContext;
 import com.netspective.commons.report.tabular.TabularReport;
 import com.netspective.commons.report.tabular.TabularReportColumn;
 import com.netspective.commons.report.tabular.TabularReportColumns;
 import com.netspective.commons.report.tabular.column.DecimalColumn;
 import com.netspective.commons.report.tabular.column.GeneralColumn;
 import com.netspective.commons.report.tabular.column.NumericColumn;
 import com.netspective.commons.text.TextUtils;
 import com.netspective.commons.value.source.StaticValueSource;
 
 public class QueryResultSet
 {
    private Log log = LogFactory.getLog(QueryResultSet.class);
     private Query query;
     private StoredProcedure sp;
     private ConnectionContext cc;
     private boolean executStmtResult;
     private ResultSet resultSet;
     private QueryExecutionLogEntry executionLogEntry;
 
     public QueryResultSet(Query query, ConnectionContext cc, boolean executStmtResult, ResultSet resultSet)
     {
         this.cc = cc;
         this.executStmtResult = executStmtResult;
         this.resultSet = resultSet;
         this.query = query;
     }
 
     public QueryResultSet(Query query, ConnectionContext cc, boolean executStmtResult, ResultSet resultSet, QueryExecutionLogEntry executionLogEntry)
     {
         this(query, cc, executStmtResult, resultSet);
         this.executionLogEntry = executionLogEntry;
     }
 
     /**
      * Creates a wrapper class to handle the <code>ResultSet</code> object returned
      * by the stored procedure.
      */
     public QueryResultSet(StoredProcedure sp, ConnectionContext cc, ResultSet resultSet)
     {
         this.cc = cc;
         this.resultSet = resultSet;
         this.sp = sp;
 
         // executeQuery() method does not return a boolean value
         this.executStmtResult = true;
     }
 
     /**
      * Creates a wrapper class to handle the <code>ResultSet</code> object returned
      * by the stored procedure.
      */
     public QueryResultSet(StoredProcedure sp, ConnectionContext cc, ResultSet resultSet,
                           QueryExecutionLogEntry executionLogEntry)
     {
         this(sp, cc, resultSet);
         this.executionLogEntry = executionLogEntry;
     }
 
 
     public ConnectionContext getConnectionContext()
     {
         return cc;
     }
 
     /**
      * Retrieves the boolean result returned from javax.jdbc.sql.Statement.execute() method.
      * For stored procedure executions, this returns TRUE always.
      */
     public boolean getExecutStmtResult()
     {
         return executStmtResult;
     }
 
     public ResultSet getResultSet()
     {
         return resultSet;
     }
 
     public Query getQuery()
     {
         return query;
     }
 
     public void fillReportFromMetaData(TabularReport report) throws SQLException
     {
         ResultSetMetaData rsmd = resultSet.getMetaData();
         int numColumns = rsmd.getColumnCount();
 
         TabularReportColumns columns = report.getColumns();
         columns.clear();
 
         for(int c = 1; c <= numColumns; c++)
         {
             TabularReportColumn column = null;
 
             int dataType = rsmd.getColumnType(c);
             switch(dataType)
             {
                 case Types.INTEGER:
                 case Types.SMALLINT:
                 case Types.BIGINT:
                 case Types.TINYINT:
                 case Types.BIT:
                     column = new NumericColumn();
                     break;
 
                 case Types.FLOAT:
                 case Types.REAL:
                     column = new DecimalColumn();
                     break;
 
                 case Types.NUMERIC:
                 case Types.DECIMAL:
                     if(rsmd.getScale(c) > 0)
                         column = new DecimalColumn();
                     else
                         column = new NumericColumn();
                     break;
 
                 default:
                     column = new GeneralColumn();
                     break;
             }
 
             column.setColIndex(c - 1);
             column.setHeading(new StaticValueSource(TextUtils.getInstance().sqlIdentifierToText(rsmd.getColumnLabel(c), true)));
             column.setDataType(dataType);
             column.setWidth(rsmd.getColumnDisplaySize(c));
 
             columns.add(column);
         }
 
         report.finalizeContents();
     }
 
     public QueryExecutionLogEntry getExecutionLogEntry()
     {
         return executionLogEntry;
     }
 
     /**
      * Closes the result set object's database and JDBC resources immediately instead of waiting for this to happen. The related
      * database Connection object is also closed/returned based on the boolean flag passed in.
      */
     public void close(boolean closeConnToo) throws SQLException
     {
         // according to JDK 1.3 javadocs, "When a Statement object is closed, its current ResultSet object, if one exists, is also closed."
         //resultSet.getStatement().close();
        try
        {
            resultSet.close();
        }
        catch(SQLException e)
        {
            log.error("Got error trying to close what should be an open result set. Ignoring the error.", e);
        }

         resultSet = null;
         if(closeConnToo)
         {
             cc.getDatabaseValueContext().returnConnection(cc);
             cc = null;
         }
     }
 
     protected void finalize() throws Throwable
     {
         close(false);
         super.finalize();
     }
 
     /**
      * Throw away the existing result set and re-execute the query given the same parameters -- this is used mainly
      * for non-scrollable cursors used in paged results.
      */
     public void reExecute() throws SQLException
     {
         throw new RuntimeException("Not implemented yet.");
     }
 }
