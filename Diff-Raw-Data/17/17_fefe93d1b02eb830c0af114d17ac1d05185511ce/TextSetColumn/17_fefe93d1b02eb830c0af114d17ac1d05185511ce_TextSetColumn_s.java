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
 * $Id: TextSetColumn.java,v 1.1 2003-03-13 18:25:41 shahid.shah Exp $
  */
 
 package com.netspective.axiom.schema.column.type;
 
 import java.sql.SQLException;
 
 import com.netspective.axiom.schema.Table;
 import com.netspective.axiom.schema.ColumnValue;
 import com.netspective.axiom.schema.ColumnValues;
 import com.netspective.axiom.schema.Row;
 import com.netspective.axiom.schema.Column;
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.axiom.DatabasePolicy;
 import com.netspective.commons.text.TextUtils;
 
 public class TextSetColumn extends TextColumn implements DatabasePolicy.ColumnInsertListener, DatabasePolicy.ColumnUpdateListener, DatabasePolicy.ColumnDeleteListener
 {
     static public final int COLINDEX_SYSTEM_ID   = 0;
     static public final int COLINDEX_PARENT_ID   = 1;
     static public final int COLINDEX_VALUE_INDEX = 2;
     static public final int COLINDEX_VALUE_TEXT  = 3;
 
     private String delimiter = ",";
     private boolean trim = true;
 
     public TextSetColumn(Table table)
     {
         super(table);
     }
 
     public String getDelimiter()
     {
         return delimiter;
     }
 
     public void setDelimiter(String delimiter)
     {
         this.delimiter = delimiter;
     }
 
     public boolean isTrim()
     {
         return trim;
     }
 
     public void setTrim(boolean trim)
     {
         this.trim = trim;
     }
 
     public void afterDelete(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
     {
        Object parentId = columnValues.getByColumnIndex(COLINDEX_PARENT_ID).getValue();
         if(parentId == null)
             return;
 
         Table setTable = getColumnTables().getSole();
         setTable.delete(cc, setTable.createRow(), "parent_id = ?", new Object[] { parentId });
     }
 
     public void afterInsert(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
     {
         String[] values = columnValue.isListValue() ?
                             columnValue.getTextValues() : TextUtils.split(columnValue.getTextValue(), delimiter, true);
         if(values == null)
             return;
 
         Column primaryKeyColumn = getTable().getPrimaryKeyColumns().getSole();
         Table setTable = getColumnTables().getSole();
 
         for(int i = 0; i < values.length; i++)
         {
             Row setRow = setTable.createRow();
             ColumnValues setRowColValues = setRow.getColumnValues();
 
             setRowColValues.getByColumnIndex(COLINDEX_PARENT_ID).setValue(columnValues.getByColumn(primaryKeyColumn));
             setRowColValues.getByColumnIndex(COLINDEX_VALUE_INDEX).setValue(new Integer(i));
             setRowColValues.getByColumnIndex(COLINDEX_VALUE_TEXT).setTextValue(values[i]);
 
             setTable.insert(cc, setRow);
         }
     }
 
     public void afterUpdate(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
     {
         // delete all the existing entries and reinsert them
         afterDelete(cc, flags, columnValue, columnValues);
         afterInsert(cc, flags, columnValue, columnValues);
     }
 
     public void beforeDelete(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
     {
     }
 
     public void beforeInsert(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
     {
     }
 
     public void beforeUpdate(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
     {
     }
 }
