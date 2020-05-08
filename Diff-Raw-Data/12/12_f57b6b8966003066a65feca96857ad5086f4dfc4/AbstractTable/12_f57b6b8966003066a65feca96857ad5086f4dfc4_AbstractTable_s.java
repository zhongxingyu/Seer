 /*
  * Created by IntelliJ IDEA.
  * User: snshah
  * Date: Nov 11, 2001
  * Time: 1:27:54 PM
  * To change template for new class use 
  * Code Style | Class Templates options (Tools | IDE Options).
  */
 package com.xaf.db.schema;
 
 import java.util.*;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.ResultSet;
 
 import com.xaf.sql.query.*;
 import com.xaf.sql.DmlStatement;
 import com.xaf.db.ConnectionContext;
 import com.xaf.xml.XmlSource;
 
 import javax.naming.NamingException;
 
 /**
  * NOTE: SUPPORTS ONLY TABLES WITH SINGLE-COLUMN FOREIGN AND PRIMARY KEYS (NO COMPOUND PRIMARY OR FOREIGN KEYS)
  */
 
 public abstract class AbstractTable implements Table
 {
     private Schema schema;
     private String name;
     private String xmlNodeName;
     private String description;
     private List columnsList = new ArrayList();
     private Map columnsMap = new HashMap();
     private List columnNames = new ArrayList(); // useful for caching just the names (used in DmlStatement)
     private String columNamesForSelect;
     private String selectByPrimaryKeySql;
     private String primaryKeyBindSql;
     private Map childTables;
     private Column[] allColumns;
     private Column[] requiredColumns;
     private Column[] sequencedColumns;
 	private Column[] primaryKeys;
 
     static public String convertTableNameForMapKey(String name)
     {
         return name.toUpperCase();
     }
 
     public AbstractTable(Schema schema, String name)
     {
         setName(name);
         setParentSchema(schema);
         setNameForXmlNode(XmlSource.xmlTextToNodeName(name));
         initializeDefn();
         schema.addTable(this);
     }
 
     public Schema getParentSchema() { return schema; }
     public void setParentSchema(Schema value) { schema = value; }
 
     abstract public void initializeDefn();
 
     public void finalizeDefn()
     {
         List sequencedCols = new ArrayList();
         List requiredCols = new ArrayList();
 
         for(Iterator i = getColumnsList().iterator(); i.hasNext(); )
         {
             Column column = (Column) i.next();
             column.finalizeDefn();
             if(column.isPrimaryKey())
                 primaryKeyBindSql = column.getName() + " = ?";
             if(column.getSequenceName() != null)
                 sequencedCols.add(column);
             if(column.isRequired() && ! column.isSequencedPrimaryKey())
                 requiredCols.add(column);
         }
 
         if(allColumns == null)
             allColumns = (Column[]) columnsList.toArray(new Column[columnsList.size()]);
 
         if(sequencedCols.size() > 0)
             sequencedColumns = (Column[]) sequencedCols.toArray(new Column[sequencedCols.size()]);
 
         if(requiredCols.size() > 0)
             requiredColumns = (Column[]) requiredCols.toArray(new Column[requiredCols.size()]);
     }
 
     public String getName() { return name; }
     public String getNameForMapKey() { return name.toUpperCase(); }
     public void setName(String value) { name = value; }
 
     public String getNameForXmlNode() { return xmlNodeName; }
     public void setNameForXmlNode(String value) { xmlNodeName = value; }
 
     public String getDescription() { return description; }
     public void setDescription(String value) { description = value; }
 
     public int getColumnsCount() { return columnsList.size(); }
     public List getColumnsList() { return columnsList; }
     public Map getColumnsMap() { return columnsMap; }
     public Column getColumn(String name) { return (Column) columnsMap.get(AbstractColumn.convertColumnNameForMapKey(name)); }
     public Column getColumn(int index) { return (Column) columnsList.get(index); }
     public List getColumnNames() { return columnNames; }
 
     public Column[] getAllColumns() { return allColumns; }
     public Column[] getSequencedColumns() { return sequencedColumns; }
     public Column[] getPrimaryKeyColumns() { return primaryKeys; }
 
     protected void setAllColumns(Column[] value) { allColumns = value; }
 
     public void addColumn(Column column)
     {
         if(columnsMap.containsKey(column.getNameForMapKey()))
             return;
 
         columnNames.add(column.getName());
         columnsList.add(column);
         columnsMap.put(column.getNameForMapKey(), column);
     }
 
     public String getColumnNamesForSelect()
     {
         if(columNamesForSelect == null)
         {
             StringBuffer str = new StringBuffer();
             for(int c = 0; c < columnsList.size(); c++)
             {
                 if(str.length() > 0)
                     str.append(", ");
                 str.append(((Column) columnsList.get(c)).getName());
             }
             columNamesForSelect = str.toString();
         }
         return columNamesForSelect;
     }
 
     abstract public Row createRow();
     abstract public Rows createRows();
 
     public void refreshData(ConnectionContext cc, Row row) throws NamingException, SQLException
     {
         getRecordByPrimaryKey(cc, row.getActivePrimaryKeyValue(), row);
     }
 
     public boolean dataChangedInStorage(ConnectionContext cc, Row row) throws NamingException, SQLException
     {
         Row compareTo = getRecordByPrimaryKey(cc, row.getActivePrimaryKeyValue(), null);
         return ! row.equals(compareTo);
     }
 
     protected Row getRecordByPrimaryKey(ConnectionContext cc, Object pkValue, Row row) throws NamingException, SQLException
     {
         Row result = row;
 
         if(selectByPrimaryKeySql == null)
             selectByPrimaryKeySql = "select " + getColumnNamesForSelect() + " from " + getName() + " where " + primaryKeyBindSql;
 
         Connection conn = cc.getConnection();
         try
         {
             PreparedStatement stmt = null;
             try
             {
                 stmt = conn.prepareStatement(selectByPrimaryKeySql);
                 stmt.setObject(1, pkValue);
                 if(stmt.execute())
                 {
                     ResultSet rs = stmt.getResultSet();
                     if(rs.next())
                     {
                         if(result == null) result = createRow();
                         result.populateDataByIndexes(rs);
                     }
                 }
                 return result;
             }
             catch(SQLException e)
             {
                 throw new SQLException(e.toString() + " ["+ selectByPrimaryKeySql +" (bind = "+ (pkValue != null ? "'" + pkValue + "' {"+ pkValue.getClass().getName() + "}" : "none") +")]");
             }
             finally
             {
                 if(stmt != null) stmt.close();
             }
         }
         finally
         {
             cc.returnConnection();
         }
     }
 
     protected Rows getRecordsByEquality(ConnectionContext cc, String colName, Object colValue, Rows rows) throws NamingException, SQLException
     {
         Rows result = rows;
         String selectSql = "select " + getColumnNamesForSelect() + " from " + getName() + " where " + colName + " = ?";
 
         Connection conn = cc.getConnection();
         try
         {
             PreparedStatement stmt = null;
             try
             {
                 stmt = conn.prepareStatement(selectSql);
                 stmt.setObject(1, colValue);
                 if(stmt.execute())
                 {
                     ResultSet rs = stmt.getResultSet();
                     if(result == null) result = createRows();
                     result.populateDataByIndexes(rs);
                 }
                 return result;
             }
             catch(SQLException e)
             {
                 throw new SQLException(e.toString() + " ["+ selectSql +" (bind = "+ (colValue != null ? "'" + colValue + "' {"+ colValue.getClass().getName() + "}" : "none") +")]");
             }
             finally
             {
                 if(stmt != null) stmt.close();
             }
         }
         finally
         {
             cc.returnConnection();
         }
     }
 
     /**
      * Retrieve multiple rows  based on column name and value pairs.
      * Columns with NULL values are not allowed.
      *
      */
     protected Rows getRecordsByEquality(ConnectionContext cc, String[] colNames, Object[] colValues, Rows rows) throws NamingException, SQLException
     {
         return getRecordsByEquality(cc, colNames, colValues, rows, false);
     }
 
     /**
      * Retrieve multiple rows  based on column name and value pairs.
      * Columns with NULL values are allowed based on the 'allowNull' parameter.
      */
     protected Rows getRecordsByEquality(ConnectionContext cc, String[] colNames, Object[] colValues, Rows rows, boolean allowNull) throws NamingException, SQLException
     {
         Rows result = rows;
         Connection conn = cc.getConnection();
         try
         {
             PreparedStatement stmt = null;
             try
             {
                 ResultSet rs = this.getResultset(conn, stmt, colNames, colValues, allowNull);
                 if(rs != null)
                 {
                     if(result == null) result = createRows();
                     result.populateDataByIndexes(rs);
                 }
                 return result;
             }
             catch(SQLException e)
             {
                 // rethrow the exception with the select SQL and bind parameter information added
                 e.printStackTrace();
                 if (stmt != null)
                     throw new SQLException(e.toString() + stmt.toString());
                 else
                     throw e;
             }
             finally
             {
                 if(stmt != null) stmt.close();
             }
         }
         finally
         {
             cc.returnConnection();
         }
     }
 
     /**
      * Retrieve Row object based on unique column name and value pairs.
      * Column values cannot be NULL.
      */
     protected Row getRecordByEquality(ConnectionContext cc, String[] colNames, Object[] colValues, Row row) throws NamingException, SQLException
     {
         Row result = row;
         Connection conn = cc.getConnection();
         try
         {
             PreparedStatement stmt = null;
             try
             {
                 ResultSet rs = this.getResultset(conn, stmt, colNames, colValues);
                 if(rs != null)
                 {
                     if(result == null) result = createRow();
                     result.populateDataByIndexes(rs);
                 }
                 return result;
             }
             catch(SQLException e)
             {
                 // rethrow the exception with the select SQL and bind parameter information added
                 e.printStackTrace();
                 if (stmt != null)
                     throw new SQLException(e.toString() + stmt.toString());
                 else
                     throw e;
             }
             finally
             {
                 if(stmt != null) stmt.close();
             }
         }
         finally
         {
             cc.returnConnection();
         }
     }
 
     /**
      * Retrieve Row object based on unique column name and value pairs.
      * Column values may contain nulls.
      */
     protected Row getRecordByEquality(ConnectionContext cc, String[] colNames, Object[] colValues, Row row, boolean allowNull) throws NamingException, SQLException
     {
         Row result = row;
         Connection conn = cc.getConnection();
         try
         {
             PreparedStatement stmt = null;
             try
             {
                 ResultSet rs = this.getResultset(conn, stmt, colNames, colValues, allowNull);
                 if(rs != null)
                 {
                     if(result == null) result = createRow();
                     result.populateDataByIndexes(rs);
                 }
                 return result;
             }
             catch(SQLException e)
             {
                 // rethrow the exception with the select SQL and bind parameter information added
                 e.printStackTrace();
                 if (stmt != null)
                     throw new SQLException(e.toString() + stmt.toString());
                 else
                     throw e;
             }
             finally
             {
                 if(stmt != null) stmt.close();
             }
         }
         finally
         {
             cc.returnConnection();
         }
     }
 
 
     /**
      * Creates a PreparedStatement from the passed in column names and values
      * and retrieve a ResultSet
      *
      * @param colNames name of columns for the WHERE clause
      * @param colValues column values
      * @param allowNull Flag to indicate whether or not NULL values are allowed
      * @returns ResultSet
      */
     protected ResultSet getResultset(Connection conn, PreparedStatement stmt, String[] colNames, Object[] colValues, boolean allowNull) throws SQLException, NamingException
     {
         if (colNames == null || colNames.length == 0)
             return null;
         String sqlString = "";
 
         if (allowNull)
             sqlString = this.createPreparedStatmentString(colNames, colValues);
         else
             sqlString = this.createPreparedStatmentString(colNames);
         stmt = conn.prepareStatement(sqlString);
         for (int k = 0; k < colValues.length; k++)
         {
            stmt.setObject(k+1, colValues[k]);
         }
         if(stmt.execute())
         {
             ResultSet rs = stmt.getResultSet();
             return rs;
         }
         return null;
     }
 
     /**
      * Calls getResultset(Connection conn, PreparedStatement stmt, String[] colNames, Object[] colValues, boolean allowNull)
      * with FALSE passed in for allowNull parameter.
      *
      */
     protected ResultSet getResultset(Connection conn, PreparedStatement stmt, String[] colNames, Object[] colValues) throws SQLException, NamingException
     {
         return this.getResultset(conn, stmt, colNames, colValues, false);
     }
 
     /**
      * Generates SQL string for a PreparedStatement and if any of the columns have NULL values,
      * the string 'is NULL' is added instead of ' = NULL'.
      */
     protected String createPreparedStatmentString(String[] colNames, Object[] colValues)
     {
         Object[] bindValues = new Object[colValues.length];
         StringBuffer selectSqlBuffer = new StringBuffer("select " + getColumnNamesForSelect() + " from " + getName() + " where ");
 
         int newIndex = 0;
         for (int i=0; i < colNames.length; i++)
         {
             if (colValues[i] != null)
             {
                 selectSqlBuffer.append(colNames[i] + " = ? ");
                 // Need this column value as a bind parameter
                 bindValues[newIndex] = colValues[i];
                 newIndex++;
             }
             else
             {
                 // the NULL column value is not included in the column values list
                 // since it is not needed as a bind parameter anymore
                 selectSqlBuffer.append(colNames[i] + " is null ");
             }
             if (i != colNames.length - 1)
                 selectSqlBuffer.append("and ");
         }
         colValues = bindValues;
         return selectSqlBuffer.toString();
     }
 
     /**
      * Generates SQL string for a PreparedStatement.Assumes all the column values bound to the
      * staement later on will not have any NULLs
      */
     protected String createPreparedStatmentString(String[] colNames)
     {
         StringBuffer selectSqlBuffer = new StringBuffer("select " + getColumnNamesForSelect() + " from " + getName() + " where ");
         for (int i=0; i < colNames.length; i++)
         {
             selectSqlBuffer.append(colNames[i] + " = ? ");
             if (i != colNames.length - 1)
                 selectSqlBuffer.append("and ");
         }
         return selectSqlBuffer.toString();
     }
 
     protected void deleteRecordsByEquality(ConnectionContext cc, String colName, Object colValue) throws NamingException, SQLException
     {
         String deleteSql = "delete from " + getName() + " where " + colName + " = ?";
 
         Connection conn = cc.getConnection();
         try
         {
             PreparedStatement stmt = null;
             try
             {
                 stmt = conn.prepareStatement(deleteSql);
                 stmt.setObject(1, colValue);
                 stmt.execute();
             }
             catch(SQLException e)
             {
                 throw new SQLException(e.toString() + " ["+ deleteSql +" (bind = "+ (colValue != null ? "'" + colValue + "' {"+ colValue.getClass().getName() + "}" : "none") +")]");
             }
             finally
             {
                 if(stmt != null) stmt.close();
             }
         }
         finally
         {
             cc.returnConnection();
         }
     }
 
     public void registerForeignKeyDependency(ForeignKey fKey)
     {
         // if we are the "referenced" foreign key, then the source is a child of ours
         if(fKey.getType() == ForeignKey.FKEYTYPE_PARENT)
         {
             if(childTables == null) childTables = new HashMap();
             Table childTable = fKey.getSourceColumn().getParentTable();
             childTables.put(childTable.getNameForMapKey(), childTable);
         }
     }
 
     public void validateDmlValues(DmlStatement dml) throws SQLException
     {
         List columnValues = dml.getColumnValues();
         for(int i = 0; i < allColumns.length; i++)
         {
             Column col = allColumns[i];
             if((col.isRequired() && ! col.isSequencedPrimaryKey()) && columnValues.get(i) == null)
                 throw new SQLException("Required column '"+ col.getName() +"' does not have a value in table '"+ col.getParentTable().getName() +"'");
         }
     }
 
     public boolean executeDml(ConnectionContext cc, Row row, DmlStatement dml, Object[] additionalBindParams) throws NamingException, SQLException
     {
         boolean result = false;
         PreparedStatement stmt = null;
         try
         {
             stmt = cc.getConnection().prepareStatement(dml.getSql());
 
             int columnNum = 1;
             boolean[] bindValues = dml.getBindValues();
             List columnValues = dml.getColumnValues();
             if(bindValues != null)
             {
                 for(int c = 0; c < bindValues.length; c++)
                 {
                     if(bindValues[c])
                     {
                         stmt.setObject(columnNum, columnValues.get(c));
                         columnNum++;
                     }
                 }
             }
 
             if(additionalBindParams != null)
             {
                 for(int i = 0; i < additionalBindParams.length; i++)
                 {
                     stmt.setObject(columnNum, additionalBindParams[i]);
                     columnNum++;
                 }
             }
 
             return stmt.execute();
         }
         catch(SQLException e)
         {
             StringBuffer bindParams = new StringBuffer();
             int columnNum = 1;
             boolean[] bindValues = dml.getBindValues();
             List columnValues = dml.getColumnValues();
             if(bindValues != null)
             {
                 for(int c = 0; c < bindValues.length; c++)
                 {
                     if(bindValues[c])
                     {
                         Object value = columnValues.get(c);
                         if(columnNum > 1)
                             bindParams.append(", ");
                         bindParams.append(columnNum);
                         bindParams.append(": ");
                         bindParams.append(value == null ? "NULL" : (value.toString() + " {" + value.getClass().getName() + "}"));
                         columnNum++;
                     }
                 }
             }
 
             if(additionalBindParams != null)
             {
                 for(int i = 0; i < additionalBindParams.length; i++)
                 {
                     Object value = additionalBindParams[i];
                     if(columnNum > 1)
                         bindParams.append(", ");
                     bindParams.append(columnNum);
                     bindParams.append(": ");
                     bindParams.append(value == null ? "NULL" : (value.toString() + " {" + value.getClass().getName() + "}"));
                     columnNum++;
                 }
             }
 
             throw new SQLException(e.toString() + " ["+ dml.getSql() +"\n(bind "+ (bindParams.length() > 0 ? bindParams.toString() : "none") +")\n"+ row.toString() +"]");
         }
         finally
         {
             if(stmt != null) stmt.close();
         }
     }
 
     public boolean insert(ConnectionContext cc, Row row) throws NamingException, SQLException
     {
         DmlStatement dml = row.createInsertDml(this);
         validateDmlValues(dml);
 
         if(! row.beforeInsert(cc, dml))
             return false;
 
         boolean successful = executeDml(cc, row, dml, null);
         row.afterInsert(cc);
 
         cc.returnConnection();
         return successful;
     }
 
     public boolean update(ConnectionContext cc, Row row) throws NamingException, SQLException
     {
         return update(cc, row, primaryKeyBindSql, new Object[] { row.getActivePrimaryKeyValue() });
     }
 
     public boolean update(ConnectionContext cc, Row row, String whereCond, Object[] whereCondBindParams) throws NamingException, SQLException
     {
         DmlStatement dml = row.createUpdateDml(this, whereCond);
         validateDmlValues(dml);
 
         if(! row.beforeUpdate(cc, dml))
             return false;
 
         boolean successful = executeDml(cc, row, dml, whereCondBindParams);
         row.afterUpdate(cc);
 
         cc.returnConnection();
         return successful;
     }
 
     public boolean delete(ConnectionContext cc, Row row) throws NamingException, SQLException
     {
         return delete(cc, row, primaryKeyBindSql, new Object[] { row.getActivePrimaryKeyValue() });
     }
 
     public boolean delete(ConnectionContext cc, Row row, String whereCond, Object[] whereCondBindParams) throws NamingException, SQLException
     {
         DmlStatement dml = row.createDeleteDml(this, whereCond);
         if(! row.beforeDelete(cc, dml))
             return false;
 
         boolean successful = executeDml(cc, row, dml, whereCondBindParams);
         row.afterDelete(cc);
 
         cc.returnConnection();
         return successful;
     }
 
     public boolean update(ConnectionContext cc, Row row, String whereCond, Object whereCondBindParam) throws NamingException, SQLException
     {
         return update(cc, row, whereCond, new Object[] { whereCondBindParam });
     }
 
     public boolean delete(ConnectionContext cc, Row row, String whereCond, Object whereCondBindParam) throws NamingException, SQLException
     {
         return delete(cc, row, whereCond, new Object[] { whereCondBindParam });
     }
 }
