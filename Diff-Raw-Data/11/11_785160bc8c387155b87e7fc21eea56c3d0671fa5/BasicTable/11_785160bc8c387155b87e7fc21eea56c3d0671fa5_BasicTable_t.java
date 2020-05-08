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
 package com.netspective.axiom.schema.table;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.naming.NamingException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.axiom.DatabasePolicy;
 import com.netspective.axiom.schema.BasicSchema;
 import com.netspective.axiom.schema.Column;
 import com.netspective.axiom.schema.ColumnValue;
 import com.netspective.axiom.schema.ColumnValues;
 import com.netspective.axiom.schema.Columns;
 import com.netspective.axiom.schema.ForeignKey;
 import com.netspective.axiom.schema.Index;
 import com.netspective.axiom.schema.IndexColumns;
 import com.netspective.axiom.schema.Indexes;
 import com.netspective.axiom.schema.PrimaryKeyColumnValues;
 import com.netspective.axiom.schema.PrimaryKeyColumns;
 import com.netspective.axiom.schema.Row;
 import com.netspective.axiom.schema.RowDeleteType;
 import com.netspective.axiom.schema.Rows;
 import com.netspective.axiom.schema.Schema;
 import com.netspective.axiom.schema.Table;
 import com.netspective.axiom.schema.TableHierarchyReference;
 import com.netspective.axiom.schema.TableRowTrigger;
 import com.netspective.axiom.schema.Tables;
 import com.netspective.axiom.schema.column.BasicColumn;
 import com.netspective.axiom.schema.column.ColumnsCollection;
 import com.netspective.axiom.schema.column.PrimaryKeyColumnsCollection;
 import com.netspective.axiom.schema.constraint.ParentForeignKey;
 import com.netspective.axiom.sql.Queries;
 import com.netspective.axiom.sql.Query;
 import com.netspective.axiom.sql.QueryExecutionLog;
 import com.netspective.axiom.sql.QueryResultSet;
 import com.netspective.axiom.sql.dynamic.QueryDefnCondition;
 import com.netspective.axiom.sql.dynamic.QueryDefnConditionConnectorEnumeratedAttribute;
 import com.netspective.axiom.sql.dynamic.QueryDefnField;
 import com.netspective.axiom.sql.dynamic.QueryDefnFieldReference;
 import com.netspective.axiom.sql.dynamic.QueryDefnJoin;
 import com.netspective.axiom.sql.dynamic.QueryDefnSelect;
 import com.netspective.axiom.sql.dynamic.SqlComparisonEnumeratedAttribute;
 import com.netspective.axiom.sql.dynamic.exception.QueryDefinitionException;
 import com.netspective.axiom.sql.dynamic.exception.QueryDefnFieldNotFoundException;
 import com.netspective.axiom.sql.dynamic.exception.QueryDefnSqlComparisonNotFoundException;
 import com.netspective.commons.io.InputSourceLocator;
 import com.netspective.commons.text.TextUtils;
 import com.netspective.commons.validate.ValidationUtils;
 import com.netspective.commons.xdm.XmlDataModelSchema;
 import com.netspective.commons.xml.template.Template;
 import com.netspective.commons.xml.template.TemplateConsumer;
 import com.netspective.commons.xml.template.TemplateConsumerDefn;
 import com.netspective.commons.xml.template.TemplateElement;
 import com.netspective.commons.xml.template.TemplateNode;
 import com.netspective.commons.xml.template.TemplateProducer;
 import com.netspective.commons.xml.template.TemplateProducerParent;
 import com.netspective.commons.xml.template.TemplateProducers;
 import com.netspective.commons.xml.template.TemplateText;
 
 public class BasicTable implements Table, TemplateProducerParent, TemplateConsumer
 {
     private static final Log log = LogFactory.getLog(BasicTable.class);
     public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options();
     public static final String ATTRNAME_TYPE = "type";
     public static final String[] ATTRNAMES_SET_BEFORE_CONSUMING = new String[]{"name", "abbrev"};
 
     static
     {
         XML_DATA_MODEL_SCHEMA_OPTIONS.setIgnorePcData(true);
         XML_DATA_MODEL_SCHEMA_OPTIONS.addIgnoreNestedElements(new String[]{"row"});
     }
 
     protected class TableTypeTemplateConsumerDefn extends TemplateConsumerDefn
     {
         public TableTypeTemplateConsumerDefn()
         {
             super(getSchema().getTableTypesTemplatesNameSpaceId(), ATTRNAME_TYPE, ATTRNAMES_SET_BEFORE_CONSUMING);
         }
     }
 
     protected class TablePresentationTemplate extends TemplateProducer
     {
         public TablePresentationTemplate()
         {
             super(getSchema().getPresentationTemplatesNameSpaceId(), BasicSchema.TEMPLATEELEMNAME_PRESENTATION, null, null, false, false);
         }
 
         public String getTemplateName(String url, String localName, String qName, Attributes attributes) throws SAXException
         {
             return getName();
         }
     }
 
     private InputSourceLocator inputSourceLocator;
     private Schema schema;
     private Column parentColumn;
     private String name;
     private boolean quoteNameInSql;
     private String abbrev;
     private String caption;
     private String xmlNodeName;
     private List tableTypesConsumed = new ArrayList();
     private String description;
     private Tables parentTables = new TablesCollection();
     private TableHierarchyReference hierarchyRef;
     private Columns columns = new ColumnsCollection();
     private Tables childTables = new TablesCollection();
     private PrimaryKeyColumns primaryKeyColumns;
     private Columns parentRefColumns;
     private QueryExecutionLog execLog = new QueryExecutionLog();
     private TableQueryDefinition queryDefn = null;
     private QueryDefnSelect selectByPrimaryKey;
     private QueryDefnSelect selectCount;
     private String primaryKeyWhereClauseExpr;
     private QueryDefnSelect selectByParentKey;
     private RowDeleteType rowDeleteType = new RowDeleteType(RowDeleteType.PHYSICAL);
     private String logicalDeleteUpdateSqlSetClauseFormat;
     private Rows staticData;
     private Indexes indexes = new IndexesCollection(true);
     private TablePresentationTemplate presentation;
     private TemplateProducers templateProducers;
     private TemplateConsumerDefn templateConsumer;
     private TableRowTrigger[] triggers = new TableRowTrigger[0];
     private TableSqlDataDefns sqlDataDefns = new TableSqlDataDefns(this);
 
     static public String translateTableNameForMapKey(String name)
     {
         return name != null ? name.toUpperCase() : null;
     }
 
     public InputSourceLocator getInputSourceLocator()
     {
         return inputSourceLocator;
     }
 
     public void setInputSourceLocator(InputSourceLocator inputSourceLocator)
     {
         this.inputSourceLocator = inputSourceLocator;
     }
 
     public TemplateConsumerDefn getTemplateConsumerDefn()
     {
         if(templateConsumer == null)
             templateConsumer = new TableTypeTemplateConsumerDefn();
         return templateConsumer;
     }
 
     public void registerTemplateConsumption(Template template)
     {
         tableTypesConsumed.add(template.getTemplateName());
     }
 
     public List getTableTypes()
     {
         return tableTypesConsumed;
     }
 
     public TemplateProducer getPresentation()
     {
         if(presentation == null)
             presentation = new TablePresentationTemplate();
         return presentation;
     }
 
     public TemplateProducers getTemplateProducers()
     {
         if(templateProducers == null)
         {
             templateProducers = new TemplateProducers();
             templateProducers.add(getPresentation());
         }
         return templateProducers;
     }
 
     public BasicTable(Schema schema)
     {
         setSchema(schema);
     }
 
     public BasicTable(Column parentColumn)
     {
         setParentColumn(parentColumn);
     }
 
     public void finishConstruction()
     {
         columns.finishConstruction();
         if(hierarchyRef != null)
         {
             Table parentTable = schema.getTables().getByName(hierarchyRef.getParent());
             if(parentTable == null)
                 throw new RuntimeException("Unable to find table '" + hierarchyRef.getParent() + "' for '" + getName() + "' parent hierarchy");
             parentTable.registerChildTable(this);
         }
     }
 
     public Queries getContainer()
     {
         return getSchema().getSqlManager().getQueries(); // we're storing queries in the global queries list
     }
 
     public String getNameSpaceId()
     {
         return getSchema().getName() + "." + getName();  // our namespace is the schema.tableName
     }
 
     public void setContainer(Queries container)
     {
         // we're storing queries in the global queries list so we ignore this
     }
 
     public void setNameSpaceId(String identifier)
     {
         // our namespace is the schema.tableName so we ignore this
     }
 
     public boolean isApplicationTable()
     {
         return true;
     }
 
     public Schema getSchema()
     {
         return schema;
     }
 
     public void setSchema(Schema value)
     {
         schema = value;
     }
 
     public Column getParentColumn()
     {
         return parentColumn;
     }
 
     public void setParentColumn(Column parentColumn)
     {
         this.parentColumn = parentColumn;
         setSchema(parentColumn.getSchema());
     }
 
     public String getName()
     {
         return name;
     }
 
     public String getSqlName()
     {
         return quoteNameInSql ? "\"" + name + "\"" : name;
     }
 
     public String getNameForMapKey()
     {
         return name != null ? name.toUpperCase() : null;
     }
 
     public boolean isQuoteNameInSql()
     {
         return quoteNameInSql;
     }
 
     public void setQuoteNameInSql(boolean quoteNameInSql)
     {
         this.quoteNameInSql = quoteNameInSql;
     }
 
     public void setName(String value)
     {
         name = value;
     }
 
     public String getXmlNodeName()
     {
         return xmlNodeName == null ? TextUtils.getInstance().xmlTextToNodeName(getName()) : xmlNodeName;
     }
 
     public void setXmlNodeName(String value)
     {
         xmlNodeName = value;
     }
 
     public String getAbbrev()
     {
         return abbrev != null ? abbrev : getName();
     }
 
     public void setAbbrev(String abbrev)
     {
         this.abbrev = abbrev;
     }
 
     public String getCaption()
     {
         return caption == null ? TextUtils.getInstance().sqlIdentifierToText(getName(), true) : caption;
     }
 
     public void setCaption(String caption)
     {
         this.caption = caption;
     }
 
     public String getDescription()
     {
         return description;
     }
 
     public void setDescription(String value)
     {
         description = value;
     }
 
     public PrimaryKeyColumns getPrimaryKeyColumns()
     {
         if(primaryKeyColumns == null)
         {
             primaryKeyColumns = new PrimaryKeyColumnsCollection();
             for(int i = 0; i < columns.size(); i++)
             {
                 Column column = columns.get(i);
                 if(column.isPrimaryKey())
                     primaryKeyColumns.add(column);
             }
         }
         return primaryKeyColumns;
     }
 
     public Columns getForeignKeyColumns(int fkeyType)
     {
         Columns result = new ColumnsCollection();
         for(int i = 0; i < columns.size(); i++)
         {
             Column column = columns.get(i);
             ForeignKey fkey = column.getForeignKey();
             if(fkey != null && fkey.getType() == fkeyType)
                 result.add(column);
         }
         return result;
     }
 
     public Columns getForeignKeyColumns()
     {
         Columns result = new ColumnsCollection();
         for(int i = 0; i < columns.size(); i++)
         {
             Column column = columns.get(i);
             ForeignKey fkey = column.getForeignKey();
             if(fkey != null)
                 result.add(column);
         }
         return result;
     }
 
     public Columns getParentRefColumns()
     {
         if(parentRefColumns == null)
             parentRefColumns = getForeignKeyColumns(ForeignKey.FKEYTYPE_PARENT);
 
         return parentRefColumns;
     }
 
     public Column createColumn()
     {
         return new BasicColumn(this);
     }
 
     public Column createColumn(Class cls) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
     {
         if(Column.class.isAssignableFrom(cls))
         {
             Constructor c = cls.getConstructor(new Class[]{Table.class});
             return (Column) c.newInstance(new Object[]{this});
         }
         else
             throw new RuntimeException("Don't know what to do with with class: " + cls);
     }
 
     public Tables getChildTables()
     {
         return childTables;
     }
 
     public Columns getColumns()
     {
         return columns;
     }
 
     public void addColumn(Column column)
     {
         // columns that contain <composite> don't want to be added since they have children that have been added
         if(column.isAllowAddToTable())
         {
             int indexInRow = columns.add(column);
             column.setIndexInRow(indexInRow);
         }
     }
 
     public Rows getData()
     {
         return staticData;
     }
 
     public Rows createData()
     {
         if(staticData == null)
             staticData = createRows();
         return staticData;
     }
 
     public void addData(Rows data)
     {
         // staticData is already present, do nothing
     }
 
     public boolean isChildTable()
     {
         return parentTables.size() > 0;
     }
 
     public Tables getParentTables()
     {
         return parentTables;
     }
 
     public boolean isParentTable()
     {
         return childTables != null && childTables.size() > 0;
     }
 
     public void registerChildTable(Table table)
     {
         if(childTables == null)
             childTables = new TablesCollection();
         childTables.add(table);
         table.getParentTables().add(table);
     }
 
     public void removeChildTable(Table table)
     {
         if(childTables != null)
             childTables.remove(table);
     }
 
     public TableHierarchyReference createHierarchy()
     {
         if(hierarchyRef == null)
             hierarchyRef = new BasicTableHierarchyReference();
         return hierarchyRef;
     }
 
     public TableHierarchyReference getHierarchy()
     {
         return hierarchyRef;
     }
 
     public void addHierarchy(TableHierarchyReference hierarchy)
     {
         // do nothing
     }
 
     public Row createRow()
     {
         return new BasicRow(this);
     }
 
     public Rows createRows()
     {
         return new BasicRows(this);
     }
 
     public Row createRow(ParentForeignKey pfKey, Row parentRow)
     {
         Row childRow = createRow();
         pfKey.fillChildValuesFromParentConnector(childRow.getColumnValues(), parentRow.getColumnValues());
         return childRow;
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public TableSqlDataDefns getSqlDataDefns()
     {
         return sqlDataDefns;
     }
 
     public TableSqlDataDefns createSqlDdl()
     {
         return sqlDataDefns;
     }
 
     public void addSqlDdl(TableSqlDataDefns sqlDataDefn)
     {
         // do nothing -- we have the instance already created, but the XML data model will call this anyway
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public void refreshData(ConnectionContext cc, Row row) throws NamingException, SQLException
     {
         getRowByPrimaryKeys(cc, row.getPrimaryKeyValues(), row);
     }
 
     public boolean dataChangedInStorage(ConnectionContext cc, Row row) throws NamingException, SQLException
     {
         Row compareTo = getRowByPrimaryKeys(cc, row.getPrimaryKeyValues(), null);
         return !row.equals(compareTo);
     }
 
     public Row getRowByPrimaryKeys(ConnectionContext cc, PrimaryKeyColumnValues values, Row row) throws NamingException, SQLException
     {
         return getRowByPrimaryKeys(cc, values.getValuesForSqlBindParams(), row);
     }
 
     public Row getRowByPrimaryKeys(ConnectionContext cc, Object[] pkValues, Row row) throws NamingException, SQLException
     {
         QueryDefnSelect pkSelect = getAccessorByPrimaryKeyEquality();
 
         Row resultRow = row;
         QueryResultSet qrs = pkSelect.execute(cc, pkValues, false);
         if(qrs != null)
         {
             ResultSet rs = qrs.getResultSet();
             if(rs.next())
             {
                 if(resultRow == null) resultRow = createRow();
                 resultRow.getColumnValues().populateValues(rs, ColumnValues.RESULTSETROWNUM_SINGLEROW);
             }
             qrs.close(false);
         }
 
         return resultRow;
     }
 
     public Row getRowByAccessor(ConnectionContext cc, QueryDefnSelect accessor, Object[] bindValues, Row row) throws NamingException, SQLException
     {
         Row resultRow = row;
         QueryResultSet qrs = accessor.execute(cc, bindValues, false);
         if(qrs != null)
         {
             ResultSet rs = qrs.getResultSet();
             if(rs.next())
             {
                 if(resultRow == null) resultRow = createRow();
                 resultRow.getColumnValues().populateValues(rs, ColumnValues.RESULTSETROWNUM_SINGLEROW);
             }
             qrs.close(false);
         }
         return resultRow;
     }
 
     public Rows getRowsByAccessor(ConnectionContext cc, QueryDefnSelect accessor, Object[] bindValues) throws NamingException, SQLException
     {
         Rows resultRows = createRows();
         QueryResultSet qrs = accessor.execute(cc, bindValues, false);
         if(qrs != null)
         {
             ResultSet rs = qrs.getResultSet();
             while(rs.next())
             {
                 Row resultRow = createRow();
                 resultRow.getColumnValues().populateValues(rs, ColumnValues.RESULTSETROWNUM_SINGLEROW);
                 resultRows.addRow(resultRow);
             }
             qrs.close(false);
         }
         return resultRows;
     }
 
     public Rows getRowsByWhereCond(ConnectionContext cc, String whereCond, Object[] bindValues) throws NamingException, SQLException
     {
         Rows resultRows = createRows();
 
         StringBuffer findRecsToDeleteSql = new StringBuffer("select ");
         Columns columns = getColumns();
         for(int i = 0; i < columns.size(); i++)
         {
             if(i > 0) findRecsToDeleteSql.append(", ");
             findRecsToDeleteSql.append(columns.get(i).getName());
         }
         findRecsToDeleteSql.append(" from " + cc.getDatabasePolicy().resolveTableName(this));
         if(whereCond != null)
         {
             findRecsToDeleteSql.append(" ");
             if(!whereCond.startsWith("where"))
                 findRecsToDeleteSql.append("where");
             findRecsToDeleteSql.append(" ");
             findRecsToDeleteSql.append(whereCond);
         }
 
         PreparedStatement stmt = cc.getConnection().prepareStatement(findRecsToDeleteSql.toString());
         try
         {
             if(bindValues != null)
             {
                 for(int i = 0; i < bindValues.length; i++)
                     stmt.setObject(i + 1, bindValues[i]);
             }
             ResultSet rs = stmt.executeQuery();
             try
             {
                 while(rs.next())
                 {
                     Row resultRow = createRow();
                     resultRow.getColumnValues().populateValues(rs, ColumnValues.RESULTSETROWNUM_SINGLEROW);
                     resultRows.addRow(resultRow);
                 }
             }
             finally
             {
                 rs.close();
             }
         }
         finally
         {
             stmt.close();
         }
 
         return resultRows;
     }
 
     public long getCount(ConnectionContext cc, String whereCond, Object[] bindValues) throws NamingException, SQLException
     {
         StringBuffer sql = new StringBuffer("select count(*) from ");
         sql.append(cc.getDatabasePolicy().resolveTableName(this));
         if(whereCond != null)
         {
             sql.append(" ");
             if(!whereCond.startsWith("where"))
                 sql.append("where");
             sql.append(" ");
             sql.append(whereCond);
         }
 
         PreparedStatement stmt = cc.getConnection().prepareStatement(sql.toString());
         try
         {
             if(bindValues != null)
             {
                 for(int i = 0; i < bindValues.length; i++)
                     stmt.setObject(i + 1, bindValues[i]);
             }
             ResultSet rs = stmt.executeQuery();
             try
             {
                 if(rs.next())
                     return rs.getLong(1);
                 else
                     return 0;
             }
             finally
             {
                 rs.close();
             }
         }
         finally
         {
             stmt.close();
         }
     }
 
     public long getCount(ConnectionContext cc) throws NamingException, SQLException
     {
         StringBuffer sql = new StringBuffer("select count(*) from ");
         sql.append(cc.getDatabasePolicy().resolveTableName(this));
         PreparedStatement stmt = cc.getConnection().prepareStatement(sql.toString());
         try
         {
             ResultSet rs = stmt.executeQuery();
             try
             {
                 if(rs.next())
                     return rs.getLong(1);
                 else
                     return 0;
             }
             finally
             {
                 rs.close();
             }
         }
         finally
         {
             stmt.close();
         }
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public void registerForeignKeyDependency(ForeignKey fKey)
     {
         // if we are the "referenced" foreign key, then the source is a child of ours
         if(fKey.getType() == ForeignKey.FKEYTYPE_PARENT)
         {
             Table parentTable = fKey.getReferencedColumns().getFirst().getTable();
             parentTable.registerChildTable(this);
         }
     }
 
     public void removeForeignKeyDependency(ForeignKey fKey)
     {
         // if we are the "referenced" foreign key, then the source is a child of ours
         if(fKey.getType() == ForeignKey.FKEYTYPE_PARENT)
         {
             Table parentTable = fKey.getReferencedColumns().getFirst().getTable();
             parentTable.removeChildTable(this);
         }
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public Indexes getIndexes()
     {
         return indexes;
     }
 
     public void addIndex(Index index)
     {
         indexes.add(index);
     }
 
     public Index createIndex()
     {
         return new BasicIndex(this);
     }
 
     public Index createIndex(Column column)
     {
         if(column.getTable() != this)
             throw new RuntimeException("Unable to create index -- column '" + column.getQualifiedName() + "' does not belong to table '" + getName() + "'. (" + this + " != " + column.getTable() + ")");
         else
         {
             Index index = new BasicIndex(column);
             index.setUnique(column.isUnique());
             return index;
         }
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public QueryExecutionLog getDmlExecutionLog()
     {
         return execLog;
     }
 
     public void insert(ConnectionContext cc, Row row) throws SQLException
     {
         for(int i = 0; i < triggers.length; i++)
             triggers[i].beforeTableRowInsert(cc, row);
 
         try
         {
             cc.getDatabasePolicy().insertValues(cc, DatabasePolicy.DMLFLAG_EXECUTE | DatabasePolicy.DMLFLAG_USE_BIND_PARAMS, row.getColumnValues(), row);
         }
         catch(NamingException e)
         {
             log.error("Error while inserting row " + row, e);
             throw new SQLException(e.getMessage());
         }
 
         for(int i = 0; i < triggers.length; i++)
             triggers[i].afterTableRowInsert(cc, row);
 
     }
 
     public void update(ConnectionContext cc, Row row) throws SQLException
     {
         update(cc, row, primaryKeyWhereClauseExpr, row.getPrimaryKeyValues().getValuesForSqlBindParams());
     }
 
     public void update(ConnectionContext cc, Row row, String whereCond, Object[] whereCondBindParams) throws SQLException
     {
         for(int i = 0; i < triggers.length; i++)
             triggers[i].beforeTableRowUpdate(cc, row);
 
         try
         {
             cc.getDatabasePolicy().updateValues(cc, DatabasePolicy.DMLFLAG_EXECUTE | DatabasePolicy.DMLFLAG_USE_BIND_PARAMS, row.getColumnValues(), row, whereCond, whereCondBindParams);
         }
         catch(NamingException e)
         {
             log.error("Error while updating row " + row + " whereCond " + whereCond, e);
             throw new SQLException(e.getMessage());
         }
 
         for(int i = 0; i < triggers.length; i++)
             triggers[i].afterTableRowUpdate(cc, row);
     }
 
     public void delete(ConnectionContext cc, Row row) throws SQLException
     {
         delete(cc, row, primaryKeyWhereClauseExpr, row.getPrimaryKeyValues().getValuesForSqlBindParams());
     }
 
     protected void deleteChildren(ConnectionContext cc, Row row, String whereCond, Object[] whereCondBindParams) throws NamingException, SQLException
     {
         Rows parentRows = getRowsByWhereCond(cc, whereCond, whereCondBindParams);
         for(int pr = 0; pr < parentRows.size(); pr++)
         {
             Row parentRow = parentRows.getRow(pr);
 
             Tables children = getChildTables();
             for(int ct = 0; ct < children.size(); ct++)
             {
                 Table childTable = children.get(ct);
 
                 Columns fKeyColumns = childTable.getForeignKeyColumns(ForeignKey.FKEYTYPE_PARENT);
                 for(int fki = 0; fki < fKeyColumns.size(); fki++)
                 {
                     ParentForeignKey fKey = (ParentForeignKey) fKeyColumns.get(fki).getForeignKey();
                     Rows childRows = fKey.getChildRowsByParentRow(cc, parentRow);
 
                     for(int cr = 0; cr < childRows.size(); cr++)
                     {
                         Row childRow = childRows.getRow(cr);
                         childTable.delete(cc, childRow);
                     }
                 }
             }
         }
     }
 
     public void delete(ConnectionContext cc, Row row, String whereCond, Object[] whereCondBindParams) throws SQLException
     {
         for(int i = 0; i < triggers.length; i++)
             triggers[i].beforeTableRowDelete(cc, row);
 
         final RowDeleteType rowDeleteType = getRowDeleteType();
         if(rowDeleteType.isCasadeChildren())
         {
             try
             {
                 deleteChildren(cc, row, whereCond, whereCondBindParams);
             }
             catch(NamingException e)
             {
                 throw new SQLException(e.getMessage());
             }
         }
 
         try
         {
             cc.getDatabasePolicy().deleteValues(cc, DatabasePolicy.DMLFLAG_EXECUTE | DatabasePolicy.DMLFLAG_USE_BIND_PARAMS, row.getColumnValues(), row, whereCond, whereCondBindParams);
         }
         catch(NamingException e)
         {
             log.error("Error executing delete type '" + rowDeleteType + "' for row " + row + " whereCond " + whereCond, e);
             throw new SQLException(e.getMessage());
         }
 
         for(int i = 0; i < triggers.length; i++)
             triggers[i].afterTableRowDelete(cc, row);
     }
 
     /**
      * Given a row full of data, either insert the record if no duplicates are found or update it if a duplicate
      * record is found.
      *
      * @param cc  The connection context
      * @param row The row with data to insert or update
      */
     public void insertOrUpdateIfDuplicateRowFound(ConnectionContext cc, Row row) throws NamingException, SQLException
     {
         final ColumnValues columnValues = row.getColumnValues();
 
         // if a record exists with the same primary key, do an immediate update and leave
         final Object[] valuesForSqlBindParams = row.getPrimaryKeyValues().getValuesForSqlBindParams();
         int nonNullsCount = 0;
         for(int i = 0; i < valuesForSqlBindParams.length; i++)
         {
             if(valuesForSqlBindParams[i] != null)
                 nonNullsCount++;
         }
         if(nonNullsCount == valuesForSqlBindParams.length)
         {
             if(getAccessorByPrimaryKeyEquality().recordsExist(cc, valuesForSqlBindParams))
             {
                 update(cc, row);
                 return;
             }
         }
 
         // now look to see if there are any rows with the same values as a unique index somewhere in the table; if so,
         // replace the existing row's data with this row using the primary key of the duplicate row that was found
 
         Indexes indexes = getIndexes();
         for(int j = 0; j < indexes.size(); j++)
         {
             Index index = indexes.get(j);
             if(!index.isUnique())
                 continue;
 
             IndexColumns indexColumns = index.getColumns();
             Object[] params = new Object[indexColumns.size()];
             for(int k = 0; k < indexColumns.size(); k++)
             {
                 //Iterate through all of the columns in the index
                 Column indexColumn = indexColumns.get(k);
                 ColumnValue columnValue = columnValues.getByColumnIndex(indexColumn.getIndexInRow());
                 params[k] = columnValue.getValueForSqlBindParam();
             }
 
             QueryDefnSelect indexAccessor;
             indexAccessor = getAccessorByIndexEquality(index);
            QueryResultSet qrs = null;
            try
            {
                qrs = indexAccessor.execute(cc, params, false);
            }
            catch(SQLException e)
            {
                log.error("Error while checking for columns " + index.getColumns().getOnlyNames(",") + " in unique index " + index.getName(), e);
                continue;
            }
             ResultSet rs = qrs.getResultSet();
             if(rs.next())
             {
                 Row duplicateRow = createRow();
                 duplicateRow.getColumnValues().populateValues(rs, 0);
 
                 // we want to use the same PK value as the duplicate row but we want to keep our own data
                 final PrimaryKeyColumnValues dupRowPKColVals = duplicateRow.getPrimaryKeyValues();
                 for(int k = 0; k < dupRowPKColVals.size(); k++)
                     columnValues.getByColumnIndex(k).setValue((ColumnValue) dupRowPKColVals.getByColumnIndex(k));
 
                 update(cc, row);
                 return;
             }
         }
 
         // no duplicate rows found, inserting a new recod instead
         insert(cc, row);
     }
 
     public void addTrigger(TableRowTrigger trigger)
     {
         TableRowTrigger[] result = new TableRowTrigger[triggers.length + 1];
         for(int i = 0; i < triggers.length; i++)
             result[i] = triggers[i];
         result[triggers.length] = trigger;
         triggers = result;
     }
 
     public TableRowTrigger createTrigger()
     {
         return null;
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public void initQueryDefinition(TableQueryDefinition tqd)
     {
         tqd.setName(getName());
 
         QueryDefnJoin join = tqd.createJoin();
         join.setName(getName());
         join.setTable(getName());
         if(isQuoteNameInSql())
             join.setFromExpr(getSqlName());
         join.setAutoInclude(true);
         tqd.addJoin(join);
 
         for(int i = 0; i < columns.size(); i++)
         {
             QueryDefnField field = columns.get(i).createQueryDefnField(tqd);
             try
             {
                 if(field.getJoin() == null)
                     field.setJoin(join.getName());
             }
             catch(QueryDefinitionException e)
             {
                 log.error("Error while initializing query definition.", e);
             }
             tqd.addField(field);
         }
 
         // automatically create and add the primary key accessor
         getAccessorByPrimaryKeyEquality();
 
         // automatically create and add the parent key accessor
         getAccessorByParentKeyEquality();
 
         // create all the accessors by single column equality
         for(int i = 0; i < columns.size(); i++)
         {
             Column column = columns.get(i);
             QueryDefnSelect selectByColumn = tqd.createSelect();
             selectByColumn.setName("by-" + column.getXmlNodeName() + "-equality");
             selectByColumn.setDistinct(false);
             tqd.addSelect(selectByColumn, new String[]{"by-" + column.getName() + "-equality"});
 
             try
             {
                 populateTableColumnsAsDisplayFields(selectByColumn);
                 populateColumnEqualityCondition(selectByColumn, column);
             }
             catch(QueryDefinitionException e)
             {
                 log.error(e.getMessage(), e);
             }
         }
 
         // create all the accessors by index equality
         Indexes indexes = getIndexes();
         for(int i = 0; i < indexes.size(); i++)
         {
             Index index = indexes.get(i);
 
             QueryDefnSelect selectByIndex = tqd.createSelect();
             selectByIndex.setName("by-index-" + index.getName() + "-equality");
             selectByIndex.setDistinct(false);
             tqd.addSelect(selectByIndex);
 
             try
             {
                 populateTableColumnsAsDisplayFields(selectByIndex);
                 populateColumnEqualityConditions(selectByIndex, index.getColumns());
             }
             catch(QueryDefinitionException e)
             {
                 log.error(e.getMessage(), e);
             }
         }
     }
 
     protected void populateTableColumnsAsDisplayFields(QueryDefnSelect select) throws QueryDefinitionException
     {
         Columns columns = getColumns();
         for(int i = 0; i < columns.size(); i++)
         {
             QueryDefnFieldReference qdfr = select.createDisplay();
             qdfr.setField(columns.get(i).getName());
             select.addDisplay(qdfr);
         }
     }
 
     protected void populateColumnEqualityCondition(QueryDefnSelect qds, Column column) throws QueryDefnFieldNotFoundException, QueryDefnSqlComparisonNotFoundException
     {
         QueryDefnCondition cond = qds.createCondition();
         cond.setField(column.getName());
         cond.setAllowNull(true);
 
         SqlComparisonEnumeratedAttribute scea = new SqlComparisonEnumeratedAttribute();
         scea.setValue("equals");
         cond.setComparison(scea);
 
         qds.addCondition(cond);
     }
 
     protected void populateColumnEqualityConditions(QueryDefnSelect qds, Columns cols) throws QueryDefnFieldNotFoundException, QueryDefnSqlComparisonNotFoundException
     {
         int lastColumn = cols.size() - 1;
         for(int c = 0; c <= lastColumn; c++)
         {
             QueryDefnCondition cond = qds.createCondition();
             cond.setField(cols.get(c).getName());
             cond.setAllowNull(true);
 
             SqlComparisonEnumeratedAttribute scea = new SqlComparisonEnumeratedAttribute();
             scea.setValue("equals");
             cond.setComparison(scea);
 
             if(c < lastColumn)
             {
                 QueryDefnConditionConnectorEnumeratedAttribute qdccea = new QueryDefnConditionConnectorEnumeratedAttribute();
                 qdccea.setValue("and");
                 cond.setConnector(qdccea);
             }
 
             qds.addCondition(cond);
         }
     }
 
     public QueryDefnSelect getAccessorByPrimaryKeyEquality()
     {
         if(selectByPrimaryKey == null)
         {
             TableQueryDefinition queryDefn = getQueryDefinition();
             selectByPrimaryKey = queryDefn.createSelect();
             selectByPrimaryKey.setName("by-primary-keys-equality");
             selectByPrimaryKey.setDistinct(false);
             queryDefn.addSelect(selectByPrimaryKey);
 
             try
             {
                 populateTableColumnsAsDisplayFields(selectByPrimaryKey);
                 populateColumnEqualityConditions(selectByPrimaryKey, getPrimaryKeyColumns());
             }
             catch(QueryDefinitionException e)
             {
                 log.error(e.getMessage(), e);
             }
 
             PrimaryKeyColumns columns = getPrimaryKeyColumns();
             StringBuffer whereClausExprBuf = new StringBuffer();
             int lastColumn = columns.size() - 1;
             for(int c = 0; c <= lastColumn; c++)
             {
                 if(c > 0) whereClausExprBuf.append(" and ");
                 whereClausExprBuf.append(columns.get(c).getName());
                 whereClausExprBuf.append(" = ");
                 whereClausExprBuf.append(" ? ");
             }
             primaryKeyWhereClauseExpr = whereClausExprBuf.toString();
         }
 
         return selectByPrimaryKey;
     }
 
     private QueryDefnSelect getAccessorByParentKeyEquality()
     {
         Columns parentRefCols = getParentRefColumns();
         if(selectByParentKey == null && parentRefCols.size() > 0)
         {
             TableQueryDefinition queryDefn = getQueryDefinition();
             selectByParentKey = queryDefn.createSelect();
             selectByParentKey.setName("by-parent-key-equality");
             selectByParentKey.setDistinct(false);
             queryDefn.addSelect(selectByParentKey);
 
             try
             {
                 populateTableColumnsAsDisplayFields(selectByParentKey);
                 populateColumnEqualityConditions(selectByParentKey, parentRefCols);
             }
             catch(QueryDefinitionException e)
             {
                 log.error(e.getMessage(), e);
             }
         }
 
         return selectByParentKey;
     }
 
     public TableQueryDefinition getQueryDefinition()
     {
         if(queryDefn == null)
         {
             queryDefn = new TableQueryDefinition(this);
             initQueryDefinition(queryDefn);
         }
 
         return queryDefn;
     }
 
     public QueryDefnSelect createAccessor() throws QueryDefinitionException
     {
         return getQueryDefinition().createSelect();
     }
 
     public void addAccessor(QueryDefnSelect accessor) throws QueryDefinitionException
     {
         if(accessor.getDisplayFields().size() == 0)
             populateTableColumnsAsDisplayFields(accessor);
 
         getQueryDefinition().addSelect(accessor);
     }
 
     public QueryDefnSelect getAccessorByColumnEquality(Column column)
     {
         TableQueryDefinition tqd = getQueryDefinition();
         return tqd.getSelects().get("by-" + column.getXmlNodeName() + "-equality");
     }
 
     public QueryDefnSelect getAccessorByIndexEquality(Index index)
     {
         TableQueryDefinition tqd = getQueryDefinition();
 
         String accessorName = "by-index-" + index.getName() + "-equality";
 
         if(index.getColumns().size() == 1)
             accessorName = "by-" + index.getColumns().get(0).getName() + "-equality";
 
         return tqd.getSelects().get(accessorName);
     }
 
     public QueryDefnSelect getAccessorByColumnsEquality(Columns columns)
     {
         TableQueryDefinition tqd = getQueryDefinition();
         String accessorName = "by-" + columns.getOnlyXmlNodeNames("-") + "-equality";
         QueryDefnSelect qds = tqd.getSelects().get("by-" + columns.getOnlyXmlNodeNames("-") + "-equality");
         if(qds == null)
         {
             qds = queryDefn.createSelect();
             qds.setName(accessorName);
             qds.setDistinct(false);
             tqd.addSelect(qds);
 
             try
             {
                 populateTableColumnsAsDisplayFields(qds);
                 populateColumnEqualityConditions(qds, columns);
             }
             catch(QueryDefinitionException e)
             {
                 log.error(e.getMessage(), e);
             }
         }
         return qds;
     }
 
     public String getLogicalDeleteUpdateSqlSetClauseFormat()
     {
         return logicalDeleteUpdateSqlSetClauseFormat;
     }
 
     public void setLogicalDeleteUpdateSqlSetClauseFormat(String logicalDeleteUpdateSqlSetClauseFormat)
     {
         this.logicalDeleteUpdateSqlSetClauseFormat = logicalDeleteUpdateSqlSetClauseFormat;
     }
 
     public RowDeleteType getRowDeleteType()
     {
         return rowDeleteType;
     }
 
     public void setRowDeleteType(RowDeleteType rowDeleteType)
     {
         this.rowDeleteType = rowDeleteType;
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public Query createQuery()
     {
         Query result = getSchema().getSqlManager().constructQuery();
         result.setNameSpace(this);
         return result;
     }
 
     public void addQuery(Query query)
     {
         getSchema().getSqlManager().appendQuery(query);
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     protected void copySchemaRecordEditorDialogTemplate(Template dialogsPackageTemplate, TemplateElement elem, Map jexlVars)
     {
         TemplateElement dialogTemplate = dialogsPackageTemplate.addCopyOfChildAndReplaceExpressions(elem, jexlVars, false);
 
         boolean foundAnyDataElement = false;
 
         List copyChildren = elem.getChildren();
         for(int i = 0; i < copyChildren.size(); i++)
         {
             TemplateNode dialogChildNode = (TemplateNode) copyChildren.get(i);
             if(dialogChildNode instanceof TemplateElement)
             {
                 TemplateElement dialogChildElem = (TemplateElement) dialogChildNode;
                 final String elementName = dialogChildElem.getElementName();
                 if(elementName.equals("data-type-presentation"))
                 {
                     String colsFilterRegEx = dialogChildElem.getAttributes().getValue("columns");
                     if(colsFilterRegEx == null || colsFilterRegEx.length() == 0 || colsFilterRegEx.equals("*"))
                     {
                         Columns columns = getColumns();
                         for(int c = 0; c < columns.size(); c++)
                         {
                             Column column = columns.get(c);
                             column.addSchemaRecordEditorDialogTemplates(dialogTemplate, jexlVars);
                         }
                     }
                     else
                     {
                         Columns columns = getColumns();
                         for(int c = 0; c < columns.size(); c++)
                         {
                             Column column = columns.get(c);
                             ValidationUtils.matchRegexp(column.getName(), colsFilterRegEx);
                             column.addSchemaRecordEditorDialogTemplates(dialogTemplate, jexlVars);
                         }
                     }
                 }
                 else
                 {
                     if(elementName.equals("on-add-data") || elementName.equals("on-edit-data") || elementName.equals("on-delete-data"))
                         foundAnyDataElement = true;
                     dialogTemplate.addCopyOfChildAndReplaceExpressions((TemplateElement) dialogChildNode, jexlVars, true);
                 }
             }
             else if(dialogChildNode instanceof TemplateText)
                 dialogTemplate.addChild(new TemplateText(dialogTemplate, ((TemplateText) dialogChildNode).getText()));
             else
                 throw new RuntimeException("This should never happen.");
         }
 
         // if no meta data editor is provided, add defaults -- if any of the on-add/edit/delete-data templates are found
         // it means the user wants full control so we will leave it to them to implement
         if(!foundAnyDataElement)
         {
             TemplateElement addDataTemplateElement = dialogTemplate.addChild("on-add-data", null);
             addDataTemplateElement.addChild(getSchema().getName() + "." + getXmlNodeName(), new String[][]{
                 {"_auto-map", "*"}
             });
 
             TemplateElement editDataTemplateElement = dialogTemplate.addChild("on-edit-data", null);
             editDataTemplateElement.addChild(getSchema().getName() + "." + getXmlNodeName(), new String[][]{
                 {"_pk-value", "request:" + getPrimaryKeyColumns().getSole().getName()},
                 {"_auto-map", "*"}
             });
 
             TemplateElement deleteDataTemplateElement = dialogTemplate.addChild("on-delete-data", null);
             deleteDataTemplateElement.addChild(getSchema().getName() + "." + getXmlNodeName(), new String[][]{
                 {"_pk-value", "request:" + getPrimaryKeyColumns().getSole().getName()},
                 {"_auto-map", "*"}
             });
         }
     }
 
     public void addSchemaRecordEditorDialogTemplates(Template dialogsPackageTemplate)
     {
         TemplateProducer tablePresentation = getPresentation();
         List instances = tablePresentation.getInstances();
 
         if(instances.size() < 1)
             return;
 
         Map jexlVars = new HashMap();
         jexlVars.put("table", this);
 
         // only get the last instance since the final one is the one we're going to use (it will override earlier templates)
         Template tmpl = (Template) instances.get(instances.size() - 1);
         List presentationTmplChildren = tmpl.getChildren();
         for(int j = 0; j < presentationTmplChildren.size(); j++)
         {
             TemplateNode presentationTmplChildNode = (TemplateNode) presentationTmplChildren.get(j);
             if(presentationTmplChildNode instanceof TemplateElement)
             {
                 TemplateElement elem = (TemplateElement) presentationTmplChildNode;
                 if(elem.getElementName().equals("dialog"))
                     copySchemaRecordEditorDialogTemplate(dialogsPackageTemplate, elem, jexlVars);
                 else
                     dialogsPackageTemplate.addCopyOfChildAndReplaceExpressions(elem, jexlVars, true);
             }
         }
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public Schema.TableTreeNode createTreeNode(Schema.TableTree owner, Schema.TableTreeNode parent, int level)
     {
         return new BasicTableTreeNode(owner, parent, level);
     }
 
     public class BasicTableTreeNode implements Schema.TableTreeNode
     {
         private Schema.TableTree owner;
         private Schema.TableTreeNode parent;
         private ParentForeignKey parentForeignKey;
         private int level;
         private List children = new ArrayList();
         private List ancestors;
 
         public BasicTableTreeNode(Schema.TableTree owner, Schema.TableTreeNode parent, int level)
         {
             this.owner = owner;
             this.parent = parent;
             this.level = level;
 
             Columns parentRefColumns = getParentRefColumns();
             for(int i = 0; i < parentRefColumns.size(); i++)
             {
                 Column parentRefColumn = parentRefColumns.get(i);
                 ParentForeignKey fKey = (ParentForeignKey) parentRefColumn.getForeignKey();
                 if(fKey.getReferencedColumns().getFirst().getTable() == parent.getTable())
                     parentForeignKey = fKey;
             }
 
             Set sortedChildren = new TreeSet(BasicSchema.TABLE_TREE_NODE_COMPARATOR);
             Tables childTables = getChildTables();
             for(int i = 0; i < childTables.size(); i++)
             {
                 Table childTable = childTables.get(i);
                 sortedChildren.add(childTable.createTreeNode(owner, this, level + 1));
             }
             children.addAll(sortedChildren);
         }
 
         public Schema.TableTree getOwner()
         {
             return owner;
         }
 
         public Table getTable()
         {
             return BasicTable.this;
         }
 
         public Schema.TableTreeNode getParentNode()
         {
             return parent;
         }
 
         public ParentForeignKey getParentForeignKey()
         {
             return parentForeignKey;
         }
 
         public List getChildren()
         {
             return children;
         }
 
         public String getAncestorTableNames(String delimiter)
         {
             List ancestors = getAncestors();
             if(ancestors.size() == 0)
                 return null;
 
             StringBuffer sb = new StringBuffer();
             for(int i = 0; i < ancestors.size(); i++)
             {
                 Schema.TableTreeNode node = (Schema.TableTreeNode) ancestors.get(i);
                 if(i > 0)
                     sb.append(delimiter);
                 sb.append(node.getTable().getName());
             }
             return sb.toString();
         }
 
         public List getAncestors()
         {
             if(ancestors == null)
             {
                 ancestors = new ArrayList();
                 Schema.TableTreeNode activeParentNode = getParentNode();
                 while(activeParentNode != null)
                 {
                     if(ancestors.size() == 0)
                         ancestors.add(activeParentNode);
                     else
                         ancestors.add(0, activeParentNode);
 
                     activeParentNode = activeParentNode.getParentNode();
                 }
             }
 
             return ancestors;
         }
 
         public boolean hasChildren()
         {
             return children.size() > 0;
         }
 
         public boolean hasGrandchildren()
         {
             if(!hasChildren())
                 return false;
 
             for(int i = 0; i < children.size(); i++)
             {
                 // if any of our children have children then we have grandchildren
                 if(((Schema.TableTreeNode) children.get(i)).hasChildren())
                     return true;
             }
 
             return false;
         }
 
         public String toString()
         {
             StringBuffer sb = new StringBuffer();
 
             for(int i = 0; i < level; i++)
                 sb.append("  ");
             sb.append(getTable().getName());
             sb.append(" [");
             sb.append(parentForeignKey);
             sb.append("] (");
             sb.append(getAncestorTableNames("."));
             sb.append(")");
             sb.append("\n");
 
             for(int c = 0; c < children.size(); c++)
                 sb.append(children.get(c));
 
             return sb.toString();
         }
     }
 
     public String toString()
     {
         StringBuffer sb = new StringBuffer();
         sb.append("Table " + getName() + "\n");
         sb.append(columns.toString());
         return sb.toString();
     }
 }
