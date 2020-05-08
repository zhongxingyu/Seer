 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
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
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: QueryResultSetDataSource.java,v 1.2 2003-06-29 03:17:21 aye.thu Exp $
  */
 
 package com.netspective.sparx.sql;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.apache.commons.lang.exception.NestableRuntimeException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.netspective.sparx.report.tabular.AbstractHtmlTabularReportDataSource;
 import com.netspective.axiom.sql.QueryResultSet;
 import com.netspective.commons.value.ValueSource;
 
 public class QueryResultSetDataSource extends AbstractHtmlTabularReportDataSource
 {
     private static final Log log = LogFactory.getLog(QueryResultSetDataSource.class);
 
     protected int activeRowIndex = -1;
     protected QueryResultSet queryResultSet;
     protected ResultSet resultSet;
     protected boolean scrollable;
     protected ValueSource message;
     protected boolean calculatedTotalRows;
     protected int totalRows = TOTAL_ROWS_UNKNOWN;
 
     public QueryResultSetDataSource(ValueSource noDataMessage) throws SQLException
     {
         super();
         this.message = noDataMessage;
     }
 
     public void close()
     {
         super.close();
         try
         {
             queryResultSet.close(true);
         }
         catch (SQLException e)
         {
             log.error("Unable to close result set", e);
             throw new NestableRuntimeException(e);
         }
     }
 
     public QueryResultSet getQueryResultSet()
     {
         return queryResultSet;
     }
 
     public void setQueryResultSet(QueryResultSet queryResultSet)
     {
         this.queryResultSet = queryResultSet;
         setResultSet(queryResultSet.getResultSet());
     }
 
     protected void setResultSet(ResultSet resultSet)
     {
         this.resultSet = queryResultSet.getResultSet();
 
         try
         {
             scrollable = resultSet.getType() != ResultSet.TYPE_FORWARD_ONLY ? true : false;
         }
         catch (SQLException e)
         {
             log.error("Unable to set result set", e);
             throw new NestableRuntimeException(e);
         }
 
         activeRowIndex = -1;
         calculatedTotalRows = false;
     }
 
     public Object getActiveRowColumnData(int columnIndex, int flags)
     {
         try
         {
             return queryResultSet.getResultSet().getObject(columnIndex + 1);
         }
         catch (SQLException e)
         {
             log.error("Unable to retrieve column data", e);
             return e.toString();
         }
     }
 
    /**
     * Calculates the total number of rows in the result set by moving the cursor to the last row and getting
     * the row number. It then returns the cursor to the original row position.
     * @return
     */
     public int getTotalRows()
     {
         if(calculatedTotalRows)
             return totalRows;
 
         try
         {
             if(scrollable)
             {
                // get the current row number
                int currentRow = resultSet.getRow();
                 resultSet.last();
                 totalRows = resultSet.getRow();
                if (currentRow == 0)
                {
                    // there is no current row so THE DEFAULT BEHAVIOR is to send the cursor to before the first row
                    resultSet.beforeFirst();
                }
                else
                {
                    // set the cusor back to the original row
                    resultSet.absolute(currentRow);
                }
             }
         }
         catch (SQLException e)
         {
             log.error("Unable to get total rows", e);
             throw new NestableRuntimeException(e);
         }
 
         calculatedTotalRows = true;
         return totalRows;
     }
 
     public boolean hasMoreRows()
     {
         try
         {
             return ! resultSet.isAfterLast();
         }
         catch (SQLException e)
         {
             log.error("Unable to check if more rows are available", e);
             throw new NestableRuntimeException(e);
         }
     }
 
     public boolean isScrollable()
     {
         return scrollable;
     }
 
     public void setActiveRow(int rowNum)
     {
         try
         {
             if(scrollable)
             {
                 resultSet.absolute(rowNum);
                 resultSet.previous();
             }
             else
             {
                 queryResultSet.reExecute();
                 setResultSet(queryResultSet.getResultSet());
 
                 if(rowNum > 0)
                 {
                     int atRow = 0;
                     while(resultSet.next())
                     {
                         if(atRow >= rowNum)
                             break;
 
                         atRow++;
                     }
                 }
             }
         }
         catch (SQLException e)
         {
             log.error("Unable to set active row", e);
             throw new NestableRuntimeException(e);
         }
     }
 
     public boolean next()
     {
         try
         {
             if(resultSet.next())
             {
                 activeRowIndex++;
                 return true;
             }
         }
         catch (SQLException e)
         {
             log.error("Unable to move to next row", e);
             throw new NestableRuntimeException(e);
         }
 
         return false;
     }
 
     public int getActiveRowNumber()
     {
         return activeRowIndex + 1;
     }
 
     public ValueSource getNoDataFoundMessage()
     {
         return message;
     }
 }
