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
 package com.netspective.axiom.schema.column;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.naming.NamingException;
 
 import org.apache.commons.lang.exception.NestableRuntimeException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.axiom.schema.BasicSchema;
 import com.netspective.axiom.schema.Column;
 import com.netspective.axiom.schema.ColumnValue;
 import com.netspective.axiom.schema.Columns;
 import com.netspective.axiom.schema.ForeignKey;
 import com.netspective.axiom.schema.GeneratedValueColumn;
 import com.netspective.axiom.schema.Row;
 import com.netspective.axiom.schema.Rows;
 import com.netspective.axiom.schema.Schema;
 import com.netspective.axiom.schema.Table;
 import com.netspective.axiom.schema.Tables;
 import com.netspective.axiom.schema.constraint.BasicForeignKey;
 import com.netspective.axiom.schema.constraint.BasicTableColumnReference;
 import com.netspective.axiom.schema.constraint.ParentForeignKey;
 import com.netspective.axiom.schema.constraint.SelfForeignKey;
 import com.netspective.axiom.schema.table.BasicTable;
 import com.netspective.axiom.schema.table.TableQueryDefinition;
 import com.netspective.axiom.schema.table.TablesCollection;
 import com.netspective.axiom.schema.table.type.EnumerationTable;
 import com.netspective.axiom.schema.table.type.EnumerationTableRow;
 import com.netspective.axiom.schema.table.type.EnumerationTableRows;
 import com.netspective.axiom.sql.DbmsSqlText;
 import com.netspective.axiom.sql.DbmsSqlTexts;
 import com.netspective.axiom.sql.JdbcTypesEnumeratedAttribute;
 import com.netspective.commons.text.TextUtils;
 import com.netspective.commons.validate.ValidationRule;
 import com.netspective.commons.validate.ValidationRules;
 import com.netspective.commons.validate.ValidationRulesCollection;
 import com.netspective.commons.value.AbstractValue;
 import com.netspective.commons.value.source.StaticValueSource;
 import com.netspective.commons.xdm.XmlDataModelSchema;
 import com.netspective.commons.xml.NodeIdentifiers;
 import com.netspective.commons.xml.template.Template;
 import com.netspective.commons.xml.template.TemplateConsumer;
 import com.netspective.commons.xml.template.TemplateConsumerDefn;
 import com.netspective.commons.xml.template.TemplateContentHandler;
 import com.netspective.commons.xml.template.TemplateElement;
 import com.netspective.commons.xml.template.TemplateNode;
 import com.netspective.commons.xml.template.TemplateProducer;
 import com.netspective.commons.xml.template.TemplateProducerParent;
 import com.netspective.commons.xml.template.TemplateProducers;
 import com.netspective.commons.xml.template.TemplateText;
 
 public class BasicColumn implements Column, TemplateProducerParent, TemplateConsumer
 {
     public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options();
     private static final Log log = LogFactory.getLog(BasicColumn.class);
     public static final String ATTRNAME_TYPE = "type";
     public static final String[] ATTRNAMES_FKEYREFS = new String[]{
         "parentref", "parent-ref", "lookupref", "lookup-ref", "selfref", "self-ref"
     };
     public static final String[] ATTRNAMES_SET_BEFORE_CONSUMING = new String[]{"name"};
 
     static
     {
         XML_DATA_MODEL_SCHEMA_OPTIONS.setIgnorePcData(true);
     }
 
     protected class DataTypeTemplateConsumerDefn extends TemplateConsumerDefn
     {
         public DataTypeTemplateConsumerDefn()
         {
             super(getSchema().getDataTypesTemplatesNameSpaceId(), ATTRNAME_TYPE, ATTRNAMES_SET_BEFORE_CONSUMING);
         }
 
         public String getAlternateClassName(TemplateContentHandler contentHandler, List templates, String elementName, Attributes attributes) throws SAXException
         {
             String altClassName = attributes.getValue(NodeIdentifiers.ATTRNAME_ALTERNATE_CLASS_NAME);
             if(altClassName != null)
                 return altClassName;
 
             // if we have a reference to a foreign key, mark it as a placeholder and the Schema will come back to it
             for(int i = 0; i < ATTRNAMES_FKEYREFS.length; i++)
             {
                 String attrName = ATTRNAMES_FKEYREFS[i];
                 String attrValue = attributes.getValue(attrName);
                 if(attrValue != null)
                     return ForeignKeyPlaceholderColumn.class.getName();
             }
 
             return super.getAlternateClassName(contentHandler, templates, elementName, attributes);
         }
     }
 
     protected class ColumnPresentationTemplate extends TemplateProducer
     {
         public ColumnPresentationTemplate()
         {
             super(schema.getPresentationTemplatesNameSpaceId(), BasicSchema.TEMPLATEELEMNAME_PRESENTATION, null, null, false, false);
         }
 
         public String getTemplateName(String url, String localName, String qName, Attributes attributes) throws SAXException
         {
             return getQualifiedName();
         }
     }
 
     protected class ColumnHibernateIdTemplate extends TemplateProducer
     {
         public ColumnHibernateIdTemplate()
         {
             super(schema.getHibernateTemplatesNameSpaceId(), BasicSchema.TEMPLATEELEMNAME_HIBERNATE_ID, null, null, false, false);
         }
 
         public String getTemplateName(String url, String localName, String qName, Attributes attributes) throws SAXException
         {
             return getQualifiedName();
         }
     }
 
     protected class ColumnHibernateTemplate extends TemplateProducer
     {
         public ColumnHibernateTemplate()
         {
             super(schema.getHibernateTemplatesNameSpaceId(), BasicSchema.TEMPLATEELEMNAME_HIBERNATE, null, null, false, false);
         }
 
         public String getTemplateName(String url, String localName, String qName, Attributes attributes) throws SAXException
         {
             return getQualifiedName();
         }
     }
 
     public class BasicColumnValue extends AbstractValue implements ColumnValue
     {
         private DbmsSqlTexts sqlExprs;
 
         public BasicColumnValue()
         {
             DbmsSqlTexts columnDefaultExprs = valueDefn.getDefaultSqlExprValues();
             if(columnDefaultExprs.size() > 0)
                 sqlExprs = columnDefaultExprs.getCopy();
         }
 
         public Column getColumn()
         {
             return BasicColumn.this;
         }
 
         public void setValue(ColumnValue value)
         {
             setValue(value.getValue());
             sqlExprs = value.getSqlExprs();
         }
 
         public boolean isSqlExpr()
         {
             return sqlExprs != null;
         }
 
         public DbmsSqlText createSqlExpr()
         {
             if(sqlExprs == null) sqlExprs = new DbmsSqlTexts(this, "value");
             return sqlExprs.create();
         }
 
         public DbmsSqlTexts getSqlExprs()
         {
             return sqlExprs;
         }
 
         public void addSqlExpr(DbmsSqlText sqlText)
         {
             sqlExprs.add(sqlText);
         }
 
         public Row getReferencedForeignKeyRow(ConnectionContext cc) throws NamingException, SQLException
         {
             if(!hasValue())
                 return null;
 
             ForeignKey fKey = getForeignKey();
             if(fKey != null)
             {
                 Columns fkCol = fKey.getReferencedColumns();
                 Table fkTable = fkCol.getFirst().getTable();
                 if(fkTable instanceof EnumerationTable)
                 {
                     int id = getIntValue();
                     EnumerationTableRows rows = ((EnumerationTable) fkTable).getEnums();
                     return rows.getById(id);
                 }
                 else
                     return fKey.getFirstReferencedRow(cc, this);
             }
             else
                 throw new RuntimeException("Column '" + getQualifiedName() + "' does not have a foreign key.");
         }
 
         public Rows getReferencedForeignKeyRows(ConnectionContext cc) throws NamingException, SQLException
         {
             if(!hasValue())
                 return null;
 
             ForeignKey fKey = getForeignKey();
             if(fKey != null)
             {
                 Columns fkCol = fKey.getReferencedColumns();
                 Table fkTable = fkCol.getFirst().getTable();
                 if(fkTable instanceof EnumerationTable)
                 {
                     int id = getIntValue();
                     EnumerationTableRows allRows = ((EnumerationTable) fkTable).getEnums();
                     Rows matchingRows = fkTable.createRows();
                     matchingRows.addRow(allRows.getById(id));
                     return matchingRows;
                 }
                 else
                     return fKey.getReferencedRows(cc, this);
             }
             else
                 throw new RuntimeException("Column '" + getQualifiedName() + "' does not have a foreign key.");
         }
 
         public EnumerationTableRow getReferencedEnumRow()
         {
             try
             {
                 return (EnumerationTableRow) getReferencedForeignKeyRow(null);
             }
             catch(NamingException e)
             {
                 log.error("This should never happen!", e);
                 throw new NestableRuntimeException("This should never happen!", e);
             }
             catch(SQLException e)
             {
                 log.error("This should never happen!", e);
                 throw new NestableRuntimeException("This should never happen!", e);
             }
         }
 
         public String toString()
         {
             StringBuffer sb = new StringBuffer();
             sb.append(getName());
             sb.append("=");
             sb.append(getValue());
             if(sqlExprs != null) sb.append(" (SQL Exprs: " + sqlExprs.size() + ")");
             return sb.toString();
         }
     }
 
     //TODO: need to add required validation
     /*
     protected class RequiredValueValidationRule extends BasicValidationRule
     {
         public boolean isValid(ValidationContext vc, ScalarValue value)
         {
             if(isRequiredByApp() && ! value.hasValue())
             {
                 vc.addValidationError(BasicColumn.this, "Column '"+ getQualifiedName() +"' is required but has no value.");
                 return false;
             }
 
             return true;
         }
     }
     */
 
     private Schema schema;
     private Table table;
     private String name;
     private String abbrev;
     private String caption;
     private String xmlNodeName;
     private String javaPropertyName;
     private List dataTypesConsumed = new ArrayList();
     private int requirement = RequirementEnumeratedAttribute.NOT_REQUIRED;
     private boolean primaryKey;
     private boolean unique;
     private boolean indexed;
     private boolean allowAddToTable;
     private boolean updateManagedByDbms;
     private boolean insertManagedByDbms;
     private boolean quoteNameInSql;
     private int size = -1;
     private int indexInRow = -1;
     private SqlDataDefns sqlDataDefn = new SqlDataDefns(this);
     private ValueDefns valueDefn = new ValueDefns(this);
     private JdbcTypesEnumeratedAttribute jdbcDefn;
     private String descr;
     private ForeignKey foreignKey;
     private String sequenceName;
     private Set dependentFKeys;
     private Tables autoGeneratedColumnTables;
     private ColumnPresentationTemplate presentation;
     private ColumnHibernateTemplate hibernateMappingTemplate;
     private ColumnHibernateIdTemplate hibernateIdMappingTemplate;
     private TemplateProducers templateProducers;
     private TemplateConsumerDefn templateConsumer;
     private ValidationRules validationRules;
     private Class foreignKeyReferenceeClass;
 
     static public String translateColumnNameForMapKey(String name)
     {
         return name != null ? name.toLowerCase() : null;
     }
 
     public BasicColumn(Table table)
     {
         setTable(table);
         setForeignKeyReferenceeClass(this.getClass());
         setUpdateManagedByDbms(false);
         setInsertManagedByDbms(false);
     }
 
     public TemplateConsumerDefn getTemplateConsumerDefn()
     {
         if(templateConsumer == null)
             templateConsumer = new DataTypeTemplateConsumerDefn();
         return templateConsumer;
     }
 
     public void registerTemplateConsumption(Template template)
     {
         dataTypesConsumed.add(template.getTemplateName());
     }
 
     public TemplateProducer getPresentation()
     {
         if(presentation == null)
             presentation = new ColumnPresentationTemplate();
 
         return presentation;
     }
 
     public TemplateProducer getHibernateIdMappingTemplateProducer()
     {
         if(hibernateIdMappingTemplate == null)
             hibernateIdMappingTemplate = new ColumnHibernateIdTemplate();
 
         return hibernateIdMappingTemplate;
     }
 
     public TemplateProducer getHibernateMappingTemplateProducer()
     {
         if(hibernateMappingTemplate == null)
             hibernateMappingTemplate = new ColumnHibernateTemplate();
 
         return hibernateMappingTemplate;
     }
 
     public Map getHibernateMappingTemplateVars()
     {
         Map result = new HashMap();
         result.put("column", this);
         return result;
     }
 
     public TemplateProducers getTemplateProducers()
     {
         if(templateProducers == null)
         {
             templateProducers = new TemplateProducers();
             templateProducers.add(getPresentation());
             templateProducers.add(getHibernateIdMappingTemplateProducer());
             templateProducers.add(getHibernateMappingTemplateProducer());
         }
         return templateProducers;
     }
 
     public ColumnValue constructValueInstance()
     {
         return new BasicColumnValue();
     }
 
     public void initValidationRules(ValidationRules rules)
     {
         // empty here, but may be overridden by children
     }
 
     public ValidationRules getValidationRules()
     {
         if(validationRules == null)
         {
             validationRules = new ValidationRulesCollection();
             initValidationRules(validationRules);
         }
         return validationRules;
     }
 
     public ValidationRules createValidation()
     {
         return getValidationRules();
     }
 
     public void addValidation(ValidationRules rules)
     {
         // nothing to do here
     }
 
     public ColumnQueryDefnField createQueryDefnField(TableQueryDefinition owner)
     {
         ColumnQueryDefnField result = (ColumnQueryDefnField) owner.createField();
         result.setName(getName());
         if(isQuoteNameInSql())
         {
             final String sqlName = getSqlName();
             result.setColumnExpr(sqlName);
             result.setWhereExpr(sqlName);
             result.setOrderByExpr(sqlName);
         }
         else
             result.setColumn(getName());
         result.setTableColumn(this);
         result.setCaption(TextUtils.getInstance().sqlIdentifierToText(getName(), true));
         return result;
     }
 
     public void finishConstruction()
     {
         if(this instanceof ForeignKeyPlaceholderColumn)
         {
             ForeignKey fkey = getForeignKey();
             if(fkey.getReferencedColumns() == null)
                 throw new RuntimeException("Invalid Foreign Key " + fkey + " in column " + this);
 
             Column referenced = fkey.getReferencedColumns().getSole();
             if(referenced == null)
                 throw new RuntimeException("Unable to finish construction of '" + getQualifiedName() + "': referenced foreign key '" + fkey + "' not found.");
 
             // make sure the referenced column has completed its construction
             referenced.finishConstruction();
 
             Column actualColumn;
             try
             {
                 actualColumn = getTable().createColumn(referenced.getForeignKeyReferenceeClass());
             }
             catch(Exception e)
             {
                 throw new NestableRuntimeException(e);
             }
 
             ((BasicColumn) actualColumn).inheritForeignKeyReferencedColumn(referenced);
             ((BasicColumn) actualColumn).inheritForeignKeyPlaceholderColumn((ForeignKeyPlaceholderColumn) this);
 
             getTable().getColumns().replace(this, actualColumn);
         }
 
         // we want to make sure each of the rules have a valid caption
         if(validationRules != null)
         {
             for(int i = 0; i < validationRules.size(); i++)
             {
                 ValidationRule rule = validationRules.get(i);
                 if(rule.getCaption() == null)
                     rule.setCaption(new StaticValueSource(getQualifiedName()));
             }
         }
     }
 
     protected void inheritForeignKeyReferencedColumn(Column column)
     {
         setSize(column.getSize());
         getSqlDdl().mergeReferenced(column.getSqlDdl());
         dataTypesConsumed.addAll(column.getDataTypeNames());
         presentation = (ColumnPresentationTemplate) column.getPresentation();
         hibernateIdMappingTemplate = (ColumnHibernateIdTemplate) column.getHibernateIdMappingTemplateProducer();
         hibernateMappingTemplate = (ColumnHibernateTemplate) column.getHibernateMappingTemplateProducer();
     }
 
     protected void inheritForeignKeyPlaceholderColumn(ForeignKeyPlaceholderColumn column)
     {
         // todo: SQL defns for foreign keys, child tables and other stuff still required??
 
         setName(column.getName());
         setAbbrev(column.getAbbrev());
         setXmlNodeName(column.getXmlNodeName());
        setJavaPropertyName(column.getJavaPropertyName());
         setSize(column.getSize());
         setIndexInRow(column.getIndexInRow());
         setDescr(column.getDescr());
         setForeignKey(column.getForeignKey());
         Columns srcColumns = new ColumnsCollection();
         srcColumns.add(this);
         foreignKey.setSourceColumns(srcColumns);
         setSequenceName(column.getSequenceName());
         setRequired(new RequirementEnumeratedAttribute(column.getRequirement()));
         setPrimaryKey(column.isPrimaryKey());
         setIndexed(column.isIndexed());
         setUnique(column.isUnique());
         getSqlDdl().merge(column.getSqlDdl());
         getValueDefns().merge(column.getValueDefns());
         getValidationRules().merge(column.getValidationRules());
         if(column.getPresentation().getInstances().size() > 0) presentation = (ColumnPresentationTemplate) column.getPresentation();
         if(column.getHibernateIdMappingTemplateProducer().getInstances().size() > 0) hibernateIdMappingTemplate = (ColumnHibernateIdTemplate) column.getHibernateIdMappingTemplateProducer();
         if(column.getHibernateMappingTemplateProducer().getInstances().size() > 0) hibernateMappingTemplate = (ColumnHibernateTemplate) column.getHibernateMappingTemplateProducer();
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public String getName()
     {
         return name;
     }
 
     public String getSqlName()
     {
         return quoteNameInSql ? "\"" + name + "\"" : name;
     }
 
     public String getAbbrev()
     {
         return abbrev != null ? abbrev : name;
     }
 
     public String getCaption()
     {
         return caption == null ? TextUtils.getInstance().sqlIdentifierToText(getName(), true) : caption;
     }
 
     public void setCaption(String caption)
     {
         this.caption = caption;
     }
 
     public String getQualifiedName()
     {
         return BasicTableColumnReference.createReference(this);
     }
 
     public String getSqlQualifiedName()
     {
         return BasicTableColumnReference.createSqlReference(this);
     }
 
     public String getNameForMapKey()
     {
         return translateColumnNameForMapKey(name);
     }
 
     public boolean isQuoteNameInSql()
     {
         return quoteNameInSql;
     }
 
     public void setQuoteNameInSql(boolean quoteNameInSql)
     {
         this.quoteNameInSql = quoteNameInSql;
     }
 
     public String getXmlNodeName()
     {
         return xmlNodeName == null ? TextUtils.getInstance().xmlTextToNodeName(getName()) : xmlNodeName;
     }
 
     public String getJavaPropertyName()
     {
         return javaPropertyName == null ? TextUtils.getInstance().xmlTextToJavaIdentifier(getName(), false) : javaPropertyName;
     }
 
     public String getJavaPropertyName(String defaultJavaPropName)
     {
         return javaPropertyName == null ? defaultJavaPropName : javaPropertyName;
     }
 
     public void setName(String value)
     {
         name = value;
     }
 
     public void setAbbrev(String abbrev)
     {
         this.abbrev = abbrev;
     }
 
     public void setXmlNodeName(String xmlNodeName)
     {
         this.xmlNodeName = xmlNodeName;
     }
 
     public void setJavaPropertyName(String javaPropertyName)
     {
         this.javaPropertyName = javaPropertyName;
     }
 
     public List getDataTypeNames()
     {
         return dataTypesConsumed;
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public int getIndexInRow()
     {
         return indexInRow;
     }
 
     public void setIndexInRow(int value)
     {
         indexInRow = value;
     }
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public Schema getSchema()
     {
         return schema;
     }
 
     public void setSchema(Schema owner)
     {
         this.schema = owner;
     }
 
     public Table getTable()
     {
         return table;
     }
 
     public void setTable(Table value)
     {
         table = value;
         setSchema(table.getSchema());
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public String getSequenceName()
     {
         return sequenceName != null ? sequenceName : (table.getAbbrev() + "_" + getName() + "_SEQ").toUpperCase();
     }
 
     public void setSequenceName(String value)
     {
         sequenceName = value;
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public SqlDataDefns getSqlDdl()
     {
         return sqlDataDefn;
     }
 
     public SqlDataDefns createSqlDdl()
     {
         return sqlDataDefn;
     }
 
     public void addSqlDdl(SqlDataDefns sqlDataDefn)
     {
         // do nothing -- we have the instance already created, but the XML data model will call this anyway
     }
 
     public String formatSqlLiteral(Object value)
     {
         return value != null ? value.toString() : null;
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public ValueDefns getValueDefns()
     {
         return valueDefn;
     }
 
     public ValueDefns createValueDefn()
     {
         return valueDefn;
     }
 
     public void addValueDefn(ValueDefns valueDefns)
     {
         // do nothing -- we have the instance already created, but the XML data model will call this anyway
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public JdbcTypesEnumeratedAttribute getJdbcType()
     {
         return jdbcDefn;
     }
 
     public void setJdbcType(JdbcTypesEnumeratedAttribute type)
     {
         jdbcDefn = type;
     }
 
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public String getDescr()
     {
         return descr;
     }
 
     public void setDescr(String value)
     {
         descr = value;
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public int getSize()
     {
         return size;
     }
 
     public void setSize(int value)
     {
         size = value;
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public Class getForeignKeyReferenceeClass()
     {
         return foreignKeyReferenceeClass;
     }
 
     public void setForeignKeyReferenceeClass(Class cls)
     {
         foreignKeyReferenceeClass = cls;
     }
 
     public ForeignKey getForeignKey()
     {
         return foreignKey;
     }
 
     public void setForeignKey(ForeignKey foreignKey)
     {
         this.foreignKey = foreignKey;
     }
 
     public void setLookupRef(String reference)
     {
         setForeignKey(new BasicForeignKey(this, new BasicTableColumnReference(schema, reference)));
     }
 
     public void setForeignKeyLogical(boolean logical)
     {
         if(foreignKey != null)
             ((BasicForeignKey) foreignKey).setLogical(logical);
     }
 
     public void setParentRef(String reference)
     {
         setForeignKey(new ParentForeignKey(this, new BasicTableColumnReference(schema, reference)));
     }
 
     public void setSelfRef(String reference)
     {
         setForeignKey(new SelfForeignKey(this, new BasicTableColumnReference(schema, reference)));
     }
 
     public Set getDependentForeignKeys()
     {
         return dependentFKeys;
     }
 
     public void removeForeignKeyDependency(ForeignKey fKey)
     {
         if(dependentFKeys != null)
             dependentFKeys.remove(fKey);
         fKey.getSourceColumns().getFirst().getTable().removeForeignKeyDependency(fKey);
     }
 
     public void registerForeignKeyDependency(ForeignKey fKey)
     {
         if(dependentFKeys == null) dependentFKeys = new HashSet();
         dependentFKeys.add(fKey);
         fKey.getSourceColumns().getFirst().getTable().registerForeignKeyDependency(fKey);
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public int getRequirement()
     {
         return requirement;
     }
 
     public boolean isRequiredByApp()
     {
         return requirement == RequirementEnumeratedAttribute.REQUIRED_BY_APP;
     }
 
     public boolean isRequiredByDbms()
     {
         return requirement == RequirementEnumeratedAttribute.REQUIRED_BY_DBMS;
     }
 
     public void setRequired(RequirementEnumeratedAttribute requirement)
     {
         this.requirement = requirement.getValueIndex();
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public boolean isIndexed()
     {
         return indexed;
     }
 
     public void setIndexed(boolean flag)
     {
         indexed = flag;
     }
 
     public boolean isPrimaryKey()
     {
         return primaryKey;
     }
 
     public void setPrimaryKey(boolean flag)
     {
         primaryKey = flag;
     }
 
     public boolean isUnique()
     {
         return unique;
     }
 
     public void setUnique(boolean flag)
     {
         unique = flag;
     }
 
     public boolean isAllowAddToTable()
     {
         return allowAddToTable;
     }
 
     public void setAllowAddToTable(boolean flag)
     {
         allowAddToTable = flag;
     }
 
     public boolean isInsertManagedByDbms()
     {
         return insertManagedByDbms;
     }
 
     public void setInsertManagedByDbms(boolean flag)
     {
         insertManagedByDbms = flag;
     }
 
     public boolean isUpdateManagedByDbms()
     {
         return updateManagedByDbms;
     }
 
     public void setUpdateManagedByDbms(boolean flag)
     {
         updateManagedByDbms = flag;
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public void addTable(Table table)
     {
         if(autoGeneratedColumnTables == null)
             autoGeneratedColumnTables = new TablesCollection();
         autoGeneratedColumnTables.add(table);
         schema.addTable(table);
     }
 
     public Table createTable()
     {
         return new BasicTable(this);
     }
 
     public Tables getColumnTables()
     {
         return autoGeneratedColumnTables;
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public CompositeColumns createComposite()
     {
         return new CompositeColumns(this);
     }
 
     public void addComposite(CompositeColumns instance)
     {
         // nothing to do here, composite's children have already been added by the CompositeColumns.addColumn() method
         // since composites are "virtual" columns, we don't add them -- only their children get added
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     public String toString()
     {
         StringBuffer sb = new StringBuffer();
         sb.append(TextUtils.getInstance().getRelativeClassName(BasicColumn.class, getClass()));
         sb.append(" [" + getIndexInRow() + "] ");
         sb.append(getName());
 
         ForeignKey fkey = getForeignKey();
         if(fkey != null)
         {
             sb.append(" ");
             sb.append(fkey);
         }
         return sb.toString();
     }
 
     /* ------------------------------------------------------------------------------------------------------------- */
 
     protected void addEnumerationSchemaRecordEditorDialogTemplates(TemplateElement dialogTemplate, Map jexlVars)
     {
         ForeignKey fKey = getForeignKey();
         EnumerationTable enumTable = (EnumerationTable) fKey.getReferencedColumns().getFirst().getTable();
         dialogTemplate.addChild("field", new String[][]{
             {"name", getName()},
             {"type", "select"},
             {"caption", getCaption()},
             {"style", "combo"},
             {"choices", "schema-enum:" + enumTable.getSchema().getName() + "." + enumTable.getName()},
         });
     }
 
     public void addSchemaRecordEditorDialogTemplates(TemplateElement dialogTemplate, Map jexlVars)
     {
         jexlVars.put("column", this);
 
         ForeignKey fKey = getForeignKey();
         if(fKey != null && fKey.getReferencedColumns().getFirst().getTable() instanceof EnumerationTable)
         {
             addEnumerationSchemaRecordEditorDialogTemplates(dialogTemplate, jexlVars);
             return;
         }
 
         TemplateProducer columnPresentationTemplates = getPresentation();
         if(columnPresentationTemplates.getInstances().size() > 0)
         {
             // get only the last template because if there was inheritace of a data-type we want the "final" one
             Template columnPresentationTemplate = (Template) columnPresentationTemplates.getInstances().get(columnPresentationTemplates.getInstances().size() - 1);
             List copyColumnPresTmplChildren = columnPresentationTemplate.getChildren();
             for(int i = 0; i < copyColumnPresTmplChildren.size(); i++)
             {
                 TemplateNode colTmplChildNode = (TemplateNode) copyColumnPresTmplChildren.get(i);
                 if(colTmplChildNode instanceof TemplateElement)
                 {
                     TemplateElement elem = dialogTemplate.addCopyOfChildAndReplaceExpressions((TemplateElement) colTmplChildNode, jexlVars, true);
                     if(elem.getElementName().equals("field"))
                     {
                         boolean changedAttrs = false;
                         AttributesImpl attrs = new AttributesImpl(elem.getAttributes());
                         if(isPrimaryKey() &&
                            (attrs.getIndex("primary-key") == -1 && attrs.getIndex("primarykey") == -1 &&
                             attrs.getIndex("primary-key-generated") == -1 && attrs.getIndex("primarykeygenerated") == -1))
                         {
                             if(this instanceof GeneratedValueColumn)
                                 attrs.addAttribute(null, null, "primary-key-generated", "CDATA", "yes");
                             else
                                 attrs.addAttribute(null, null, "primary-key", "CDATA", "yes");
                             if(attrs.getIndex("required") == -1) // unless required is being overidden, make the primary key field required
                                 attrs.addAttribute(null, null, "required", "CDATA", "yes");
                             changedAttrs = true;
                         }
 
                         if(isRequiredByApp() && attrs.getIndex("required") == -1)
                         {
                             attrs.addAttribute(null, null, "required", "CDATA", "yes");
                             changedAttrs = true;
                         }
                         if(changedAttrs)
                             elem.setAttributes(attrs);
                     }
                 }
                 else if(colTmplChildNode instanceof TemplateText)
                     dialogTemplate.addChild(new TemplateText(dialogTemplate, ((TemplateText) colTmplChildNode).getText()));
                 else
                     throw new RuntimeException("This should never happen.");
             }
         }
     }
 }
