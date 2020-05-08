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
 * $Id: DialogContextUtils.java,v 1.2 2003-09-29 02:07:56 shahid.shah Exp $
  */
 
 package com.netspective.sparx.form;
 
 import java.util.Map;
 import java.util.Iterator;
 import java.util.Enumeration;
 import java.sql.SQLException;
 import java.sql.ResultSetMetaData;
 import java.sql.ResultSet;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.discovery.tools.DiscoverSingleton;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.Log;
 
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.axiom.sql.Query;
 import com.netspective.axiom.sql.QueryResultSet;
 import com.netspective.axiom.sql.DbmsSqlText;
 import com.netspective.axiom.schema.Row;
 import com.netspective.axiom.schema.ColumnValues;
 import com.netspective.axiom.schema.ColumnValue;
 import com.netspective.axiom.schema.Table;
 import com.netspective.axiom.SqlManager;
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.commons.text.GloballyUniqueIdentifier;
 
 public class DialogContextUtils
 {
     private static final Log log = LogFactory.getLog(DialogContextUtils.class);
     private static final DialogContextUtils INSTANCE = (DialogContextUtils) DiscoverSingleton.find(DialogContextUtils.class, DialogContextUtils.class.getName());
 
     public static DialogContextUtils getInstance()
     {
         return INSTANCE;
     }
 
     public DialogContextUtils()
     {
     }
 
     /**
      * Copy any request parameters or attributes that match field names in the dialog
      */
     public void populateFieldValuesFromRequestParamsAndAttrs(DialogContext dc)
     {
         HttpServletRequest request = dc.getHttpRequest();
         Map params = request.getParameterMap();
         DialogContext.DialogFieldStates fieldStates = dc.getFieldStates();
         Iterator i = params.entrySet().iterator();
         while(i.hasNext())
         {
             Map.Entry entry = (Map.Entry) i.next();
             String name = (String) entry.getKey();
             DialogField.State state = fieldStates.getState(name, null);
             if(state != null)
             {
                 String[] values = (String[]) entry.getValue();
                 state.getValue().setValue(values);
             }
         }
 
         Enumeration e = request.getAttributeNames();
         while(e.hasMoreElements())
         {
             String name = (String) e.nextElement();
             DialogField.State state = fieldStates.getState(name, null);
             if(state != null)
                 state.getValue().setValue(request.getAttribute(name));
         }
     }
 
     public void populateFieldValuesFromStatement(DialogContext dc, String statementId)
     {
         populateFieldValuesFromQuery(dc, (String) null, statementId, null);
     }
 
     public void populateFieldValuesFromStatement(DialogContext dc, String statementId, Object[] params)
     {
         populateFieldValuesFromQuery(dc, (String) null, statementId, params);
     }
 
     public void populateFieldValuesFromResultSet(DialogContext dc, ResultSet rs) throws SQLException
     {
         if(rs.next())
         {
             ResultSetMetaData rsmd = rs.getMetaData();
             int colsCount = rsmd.getColumnCount();
             DialogContext.DialogFieldStates fieldStates = dc.getFieldStates();
             for(int i = 1; i <= colsCount; i++)
             {
                 String fieldName = rsmd.getColumnName(i).toLowerCase();
                 DialogField.State state = fieldStates.getState(fieldName, null);
                 if(state != null)
                    state.getValue().setTextValue(rs.getString(i));
             }
         }
     }
 
     public void populateFieldValuesFromResultSet(DialogContext dc, QueryResultSet qrs) throws SQLException
     {
         populateFieldValuesFromResultSet(dc, qrs.getResultSet());
     }
 
     public void populateFieldValuesFromQuery(DialogContext dc, String dataSourceId, String statementId, Object[] params)
     {
         SqlManager sqlManager = dc.getSqlManager();
         Query query = sqlManager.getQuery(statementId);
 
         QueryResultSet queryResultSet = null;
         try
         {
             queryResultSet = query.execute(dc, dataSourceId, params);
             populateFieldValuesFromResultSet(dc, queryResultSet);
         }
         catch (Exception e)
         {
             log.error("Unable to populate values from statement", e);
         }
         finally
         {
             try
             {
                 if(queryResultSet != null)
                     queryResultSet.close(true);
             }
             catch (SQLException e)
             {
                 log.error("Unable to close query result set", e);
             }
         }
     }
 
     public void populateFieldValuesFromQuery(DialogContext dc, ConnectionContext cc, String statementId, Object[] params)
     {
         SqlManager sqlManager = dc.getSqlManager();
         Query query = sqlManager.getQuery(statementId);
 
         QueryResultSet queryResultSet = null;
         try
         {
             queryResultSet = query.execute(cc, params, false);
             populateFieldValuesFromResultSet(dc, queryResultSet);
         }
         catch (Exception e)
         {
             log.error("Unable to populate values from statement", e);
         }
         finally
         {
             try
             {
                 if(queryResultSet != null)
                     queryResultSet.close(false);
             }
             catch (SQLException e)
             {
                 log.error("Unable to close query result set", e);
             }
         }
     }
 
     public void populateFieldValuesFromSql(DialogContext dc, String sql)
     {
         populateFieldValuesFromSql(dc, (String) null, sql, null);
     }
 
     public void populateFieldValuesFromSql(DialogContext dc, String sql, Object[] params)
     {
         populateFieldValuesFromSql(dc, (String) null, sql, params);
     }
 
     public void populateFieldValuesFromSql(DialogContext dc, String dataSourceId, String sql, Object[] params)
     {
         SqlManager sqlManager = dc.getSqlManager();
         Query query = sqlManager.createQuery();
 
         try
         {
             query.setName("populateFieldValuesFromSql-" + GloballyUniqueIdentifier.getRandomGUID(false));
         }
         catch (Exception e)
         {
             log.error("Unable to set the query name", e);
         }
 
         DbmsSqlText dbmsSqlText = query.createSql();
         dbmsSqlText.setSql(sql);
         query.addSql(dbmsSqlText);
 
         QueryResultSet queryResultSet = null;
         try
         {
             queryResultSet = query.execute(dc, dataSourceId, params);
             populateFieldValuesFromResultSet(dc, queryResultSet);
         }
         catch (Exception e)
         {
             log.error("Unable to populate values from statement", e);
         }
         finally
         {
             try
             {
                 if(queryResultSet != null)
                     queryResultSet.close(true);
             }
             catch (SQLException e)
             {
                 log.error("Unable to close query result set", e);
             }
         }
     }
 
     public void populateFieldValuesFromSql(DialogContext dc, ConnectionContext cc, String sql, Object[] params)
     {
         SqlManager sqlManager = dc.getSqlManager();
         Query query = sqlManager.createQuery();
 
         try
         {
             query.setName("populateFieldValuesFromSql-" + GloballyUniqueIdentifier.getRandomGUID(false));
         }
         catch (Exception e)
         {
             log.error("Unable to set the query name", e);
         }
 
         DbmsSqlText dbmsSqlText = query.createSql();
         dbmsSqlText.setSql(sql);
         query.addSql(dbmsSqlText);
 
         QueryResultSet queryResultSet = null;
         try
         {
             queryResultSet = query.execute(cc, params, false);
             populateFieldValuesFromResultSet(dc, queryResultSet);
         }
         catch (Exception e)
         {
             log.error("Unable to populate values from statement", e);
         }
         finally
         {
             try
             {
                 if(queryResultSet != null)
                     queryResultSet.close(false);
             }
             catch (SQLException e)
             {
                 log.error("Unable to close query result set", e);
             }
         }
     }
 
     public void populateFieldValuesFromRow(DialogContext dc, Row row)
     {
         populateFieldValuesFromColumnValues(dc, row.getColumnValues());
     }
 
     public void populateFieldValuesFromColumnValues(DialogContext dc, ColumnValues columnValues)
     {
         DialogContext.DialogFieldStates states = dc.getFieldStates();
         for(int i = 0; i < columnValues.size(); i++)
         {
             ColumnValue columnValue = columnValues.getByColumnIndex(i);
             DialogField.State state = states.getState(columnValue.getColumn());
             if(state != null)
                 state.getValue().copyValueByReference(columnValue);
         }
     }
 
     public Row createRowWithFieldValues(DialogContext dc, Table table)
     {
         Row row = table.createRow();
         return populateRowWithFieldValues(dc, row);
     }
 
     public Row populateRowWithFieldValues(DialogContext dc, Row row)
     {
         populateColumnValuesWithFieldValues(dc, row.getColumnValues());
         return row;
     }
 
     public ColumnValues populateColumnValuesWithFieldValues(DialogContext dc, ColumnValues columnValues)
     {
         DialogContext.DialogFieldStates states = dc.getFieldStates();
         for(int i = 0; i < columnValues.size(); i++)
         {
             ColumnValue columnValue = columnValues.getByColumnIndex(i);
             DialogField.State state = states.getState(columnValue.getColumn());
             if(state != null)
                 columnValue.copyValueByReference(state.getValue());
         }
         return columnValues;
     }
 }
