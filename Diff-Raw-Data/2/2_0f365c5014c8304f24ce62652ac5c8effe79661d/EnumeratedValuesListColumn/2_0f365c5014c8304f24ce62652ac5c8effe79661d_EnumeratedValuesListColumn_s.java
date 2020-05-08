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
 package com.netspective.axiom.schema.column.type;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import javax.naming.NamingException;
 
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.axiom.DatabasePolicy;
 import com.netspective.axiom.schema.ColumnValue;
 import com.netspective.axiom.schema.ColumnValues;
 import com.netspective.axiom.schema.Columns;
 import com.netspective.axiom.schema.Row;
 import com.netspective.axiom.schema.Table;
 import com.netspective.axiom.schema.table.type.EnumeratedValuesListTable;
 import com.netspective.axiom.schema.table.type.EnumerationTable;
 import com.netspective.axiom.schema.table.type.EnumerationTableRow;
 import com.netspective.axiom.schema.table.type.EnumerationTableRows;
 import com.netspective.commons.text.TextUtils;
 
 /**
  * This class is used to define a text column that can contain a delimited list of enumeration Ids. The idea is that
  * this column can contain one or more actual enumeration ids (a list of ids) in a delimited text format (separated by
  * commas by default). As this column's value is inserted, updated, or removed a corresponding table, identified by
  * enumeratedValuesListTable is automatically maintained with the separate values of this column. So, this column manages
  * the "joined" values and a child table manages the separated values. This allows the EnumeratedValuesListColumn as
  * a denormalized column so that a simple select will allow a user to retrieve all the values at once instead of
  * performing a join. However, the related enumeratedValuesListTable will maintain the normalized version of the data
  * so it can be searched using normal relational logic (joins, etc).
  */
 public class EnumeratedValuesListColumn extends TextColumn implements DatabasePolicy.ColumnInsertListener, DatabasePolicy.ColumnUpdateListener, DatabasePolicy.ColumnDeleteListener
 {
     private String delimiter = ",";
     private boolean trim = true;
     private String enumeratedIdsListMemberTableName;
     private int thisTablePrimaryKeyColumnIndex;
     private EnumeratedValuesListTable enumeratedIdsListMemberTable;
     private EnumerationTable listMemberTableValueColEnumerationTable;
     private EnumerationTableRows listMemberTableValueColEnumerationRows;
     private int listMemberTableParentIdColIndex;
     private int listMemberTableValueIndexColIndex;
     private int listMemberTableValueColIndex;
 
     public EnumeratedValuesListColumn(Table table)
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
 
     public String getEnumeratedIdsListMemberTableName()
     {
         return enumeratedIdsListMemberTableName;
     }
 
     public void setEnumeratedIdsListMemberTableName(String enumeratedIdsListMemberTableName)
     {
         this.enumeratedIdsListMemberTableName = enumeratedIdsListMemberTableName;
     }
 
     public EnumeratedValuesListTable getEnumeratedIdsListMemberTable()
     {
         return enumeratedIdsListMemberTable;
     }
 
     public void finishConstruction()
     {
         super.finishConstruction();
         enumeratedIdsListMemberTable = (EnumeratedValuesListTable) getSchema().getTables().getByName(enumeratedIdsListMemberTableName);
         if(enumeratedIdsListMemberTable == null)
             throw new RuntimeException(getQualifiedName() + " column's helper table " + getEnumeratedIdsListMemberTableName() + " not found.");
 
         thisTablePrimaryKeyColumnIndex = getTable().getPrimaryKeyColumns().getSole().getIndexInRow();
         listMemberTableParentIdColIndex = enumeratedIdsListMemberTable.getColumns().getColumnIndexInRowByName(enumeratedIdsListMemberTable.getParentIdColName());
         listMemberTableValueIndexColIndex = enumeratedIdsListMemberTable.getColumns().getColumnIndexInRowByName(enumeratedIdsListMemberTable.getValueIndexColName());
         listMemberTableValueColIndex = enumeratedIdsListMemberTable.getColumns().getColumnIndexInRowByName(enumeratedIdsListMemberTable.getValueColName());
 
         if(listMemberTableParentIdColIndex == Columns.COLUMN_INDEX_NOT_FOUND ||
            listMemberTableValueIndexColIndex == Columns.COLUMN_INDEX_NOT_FOUND ||
            listMemberTableValueColIndex == Columns.COLUMN_INDEX_NOT_FOUND)
             throw new RuntimeException("Unable to find all required columns in " + getQualifiedName() + " column's helper table " + getEnumeratedIdsListMemberTableName() + ".");
 
         listMemberTableValueColEnumerationTable = (EnumerationTable) enumeratedIdsListMemberTable.getColumns().get(listMemberTableValueColIndex).getForeignKey().getReferencedColumns().getFirst().getTable();
         listMemberTableValueColEnumerationRows = listMemberTableValueColEnumerationTable.getEnums();
     }
 
     public void afterDelete(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
     {
     }
 
     public void afterInsert(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
     {
         String[] values = columnValue.isListValue()
                           ?
                           columnValue.getTextValues()
                           : TextUtils.getInstance().split(columnValue.getTextValue(), delimiter, true);
         if(values == null)
             return;
 
         for(int i = 0; i < values.length; i++)
         {
             Row memberRow = enumeratedIdsListMemberTable.createRow();
             ColumnValues memberRowColValues = memberRow.getColumnValues();
 
             memberRowColValues.getByColumnIndex(listMemberTableParentIdColIndex).setValue(columnValues.getByColumnIndex(thisTablePrimaryKeyColumnIndex));
             memberRowColValues.getByColumnIndex(listMemberTableValueIndexColIndex).setValue(new Integer(i));
             memberRowColValues.getByColumnIndex(listMemberTableValueColIndex).setValue(listMemberTableValueColEnumerationRows.getByIdOrCaptionOrAbbrev(values[i]).getIdAsInteger());
 
             enumeratedIdsListMemberTable.insert(cc, memberRow);
         }
     }
 
     public void afterUpdate(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
     {
         // delete all the existing entries and reinsert them
         beforeDelete(cc, flags, columnValue, columnValues);
         afterInsert(cc, flags, columnValue, columnValues);
     }
 
     public void beforeDelete(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
     {
         Object parentId = columnValues.getByColumnIndex(thisTablePrimaryKeyColumnIndex).getValue();
         if(parentId == null)
             return;
 
         try
         {
             PreparedStatement removeStmt = cc.getConnection().prepareStatement(enumeratedIdsListMemberTable.getDeleteChildrenSql());
             removeStmt.setObject(1, parentId);
             removeStmt.executeUpdate();
             removeStmt.close();
         }
         catch(NamingException e)
         {
             throw new SQLException(e.getMessage());
         }
     }
 
     public void beforeInsert(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
     {
         String[] values = columnValue.isListValue()
                           ?
                           columnValue.getTextValues()
                           : TextUtils.getInstance().split(columnValue.getTextValue(), delimiter, true);
         if(values == null)
             return;
 
         // in case the values are passed in as enumeration captions or abbrevs convert them to their IDs instead
         for(int i = 0; i < values.length; i++)
         {
             final EnumerationTableRow row = listMemberTableValueColEnumerationRows.getByIdOrCaptionOrAbbrev(values[i]);
             if(row != null)
                 values[i] = row.getIdAsInteger().toString();
             else
                throw new RuntimeException("Unable to find enumeration '" + values[i] + "' in table " + listMemberTableValueColEnumerationTable.getName() + ". Valid values are " + listMemberTableValueColEnumerationRows.getValidValues());
         }
 
         columnValue.setTextValue(TextUtils.getInstance().join(values, ","));
     }
 
     public void beforeUpdate(ConnectionContext cc, int flags, ColumnValue columnValue, ColumnValues columnValues) throws SQLException
     {
         beforeInsert(cc, flags, columnValue, columnValues);
     }
 }
