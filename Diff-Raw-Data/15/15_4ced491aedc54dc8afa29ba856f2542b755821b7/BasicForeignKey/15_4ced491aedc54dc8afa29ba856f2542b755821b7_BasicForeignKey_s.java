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
 * $Id: BasicForeignKey.java,v 1.3 2003-06-21 21:39:21 shahid.shah Exp $
  */
 
 package com.netspective.axiom.schema.constraint;
 
 import java.sql.SQLException;
 import java.sql.ResultSet;
 
 import javax.naming.NamingException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.netspective.axiom.schema.Column;
 import com.netspective.axiom.schema.ForeignKey;
 import com.netspective.axiom.schema.TableColumnsReference;
 import com.netspective.axiom.schema.Row;
 import com.netspective.axiom.schema.ColumnValue;
 import com.netspective.axiom.schema.Rows;
 import com.netspective.axiom.schema.Table;
 import com.netspective.axiom.schema.ColumnValues;
 import com.netspective.axiom.schema.Columns;
 import com.netspective.axiom.schema.column.ForeignKeyPlaceholderColumn;
 import com.netspective.axiom.schema.column.ColumnsCollection;
 import com.netspective.axiom.sql.dynamic.QueryDefnSelect;
 import com.netspective.axiom.sql.QueryResultSet;
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.commons.text.TextUtils;
 
 public class BasicForeignKey implements ForeignKey
 {
     private static final Log log = LogFactory.getLog(BasicForeignKey.class);
 
     private TableColumnsReference reference;
     private Columns source;
     private Columns referenced;
 
     public BasicForeignKey(Column source, TableColumnsReference reference)
     {
         Columns srcCols = new ColumnsCollection();
         srcCols.add(source);
         setSourceColumns(srcCols);
         setReference(reference);
     }
 
     public BasicForeignKey(Columns source, TableColumnsReference reference)
     {
         setSourceColumns(source);
         setReference(reference);
     }
 
     public TableColumnsReference getReference()
     {
         return reference;
     }
 
     public String getConstraintName()
     {
         return ("FK_" + source.getFirst().getTable().getAbbrev() + "_" + source.getOnlyAbbreviations("_")).toUpperCase();
     }
 
     public void setReference(TableColumnsReference reference)
     {
         this.reference = reference;
         setReferencedColumns(null);
     }
 
     public short getType()
     {
         return ForeignKey.FKEYTYPE_LOOKUP;
     }
 
     public Columns getSourceColumns()
     {
         return source;
     }
 
     public void setSourceColumns(Columns columns)
     {
         source = columns;
     }
 
     public Columns getReferencedColumns()
     {
         if(reference != null && referenced == null)
         {
             Columns refCol = reference.getColumns(source.getFirst().getSchema());
             setReferencedColumns(refCol);
             return refCol;
         }
         else
             return referenced;
     }
 
     public void setReferencedColumns(Columns columns)
     {
        // never store a placeholder
        if(columns instanceof ForeignKeyPlaceholderColumn)
             return;
 
         // if we have an existing referenced column, remove it from the dependencies
         if(referenced != null)
         {
             for(int i = 0; i < referenced.size(); i++)
                 referenced.get(i).removeForeignKeyDependency(this);
         }
 
         // now add the dependency
         referenced = columns;
         if(referenced != null)
         {
             for(int i = 0; i < referenced.size(); i++)
                 referenced.get(i).registerForeignKeyDependency(this);
         }
     }
 
     public void fillSourceValuesFromReferencedConnector(ColumnValues sourceValues, ColumnValues refValues)
     {
         Columns srcColumns = getSourceColumns();
         Columns refColumns = getReferencedColumns();
 
         for(int i = 0; i < srcColumns.size(); i++)
         {
             ColumnValue parentValue = refValues.getByColumn(refColumns.get(i));
             ColumnValue childValue = sourceValues.getByColumn(srcColumns.get(i));
 
             childValue.setValue(parentValue);
         }
     }
 
     public Row getFirstReferencedRow(ConnectionContext cc, ColumnValue value) throws NamingException, SQLException
     {
         Column refColumn = getReferencedColumns().getSole();
         Table refTable = refColumn.getTable();
 
         QueryDefnSelect accessor = refColumn.getTable().getAccessorByColumnEquality(refColumn);
         if(accessor == null)
             throw new SQLException("Unable to accessor for selecting reference column " + refColumn);
 
         Object bindValue = value.getValueForSqlBindParam();
         if(bindValue == null)
             throw new SQLException("No bind value provided for reference column " + refColumn);
 
         Row resultRow = null;
         QueryResultSet qrs = accessor.execute(cc, new Object[] { bindValue }, false);
         if(qrs != null)
         {
             ResultSet rs = qrs.getResultSet();
             if(rs.next())
             {
                 resultRow = refTable.createRow();
                 resultRow.getColumnValues().populateValues(rs, ColumnValues.RESULTSETROWNUM_SINGLEROW);
             }
         }
         qrs.close(false);
         return resultRow;
     }
 
     public Rows getReferencedRows(ConnectionContext cc, ColumnValue value) throws NamingException, SQLException
     {
         Column refColumn = getReferencedColumns().getSole();
         Table refTable = refColumn.getTable();
 
         QueryDefnSelect accessor = refColumn.getTable().getAccessorByColumnEquality(refColumn);
         if(accessor == null)
             throw new SQLException("Unable to accessor for selecting reference column " + refColumn);
 
         Object bindValue = value.getValueForSqlBindParam();
         if(bindValue == null)
             throw new SQLException("No bind value provided for reference column " + refColumn);
 
         Rows resultRows = refTable.createRows();
         QueryResultSet qrs = accessor.execute(cc, new Object[] { bindValue }, false);
         if(qrs != null)
             resultRows.populateDataByIndexes(qrs.getResultSet());
         qrs.close(false);
         return resultRows;
     }
 
     public Rows getReferencedRows(ConnectionContext cc, ColumnValues values) throws NamingException, SQLException
     {
         Columns refColumns = getReferencedColumns();
         Table refTable = refColumns.getFirst().getTable();
 
         QueryDefnSelect accessor = refTable.getAccessorByColumnsEquality(refColumns);
         if(accessor == null)
             throw new SQLException("Unable to accessor for selecting reference columns " + refColumns);
 
         Object[] bindValues = values.getByColumns(refColumns).getValuesForSqlBindParams();
         if(bindValues == null)
             throw new SQLException("No bind value provided for reference columns " + refColumns);
 
         Rows resultRows = refTable.createRows();
         QueryResultSet qrs = accessor.execute(cc, bindValues, false);
         if(qrs != null)
             resultRows.populateDataByIndexes(qrs.getResultSet());
         qrs.close(false);
         return resultRows;
     }
 
     public Rows getReferencedRows(ConnectionContext cc, Row row) throws NamingException, SQLException
     {
         if(row.getTable() != getReferencedColumns().getFirst().getTable())
             throw new SQLException("Row value is from table '"+ row.getTable().getName() +"' while referenced column is from table '" + getReferencedColumns().getFirst().getTable().getName() + "'.");
         return getReferencedRows(cc, row.getColumnValues());
     }
 
     public String toString()
     {
         StringBuffer sb = new StringBuffer();
         sb.append(TextUtils.getRelativeClassName(BasicForeignKey.class, getClass()));
         sb.append(" ");
 
         Columns srcCols = getSourceColumns();
         Columns refCols = getReferencedColumns();
 
         sb.append("Sources: ");
         sb.append(srcCols.getFirst().getTable().getName());
         sb.append(".");
         sb.append(srcCols.getOnlyNames(","));
         sb.append("; Referenced Columns: ");
         sb.append(refCols != null ? refCols.getFirst().getTable().getName() : null);
         sb.append(".");
         sb.append(refCols != null ? (refCols.getOnlyNames(",")) : null);
 
         return sb.toString();
     }
 }
