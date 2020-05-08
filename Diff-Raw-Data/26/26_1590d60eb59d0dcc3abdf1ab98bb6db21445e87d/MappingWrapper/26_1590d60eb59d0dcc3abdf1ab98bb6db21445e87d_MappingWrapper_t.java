 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.tuscany.das.rdb.config.wrapper;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.tuscany.das.rdb.config.Column;
 import org.apache.tuscany.das.rdb.config.Command;
 import org.apache.tuscany.das.rdb.config.Config;
 import org.apache.tuscany.das.rdb.config.ConfigFactory;
 import org.apache.tuscany.das.rdb.config.ConnectionInfo;
 import org.apache.tuscany.das.rdb.config.ConnectionProperties;
 import org.apache.tuscany.das.rdb.config.Create;
 import org.apache.tuscany.das.rdb.config.Delete;
 import org.apache.tuscany.das.rdb.config.KeyPair;
 import org.apache.tuscany.das.rdb.config.Relationship;
 import org.apache.tuscany.das.rdb.config.Table;
 import org.apache.tuscany.das.rdb.config.Update;
 import org.apache.tuscany.das.rdb.util.LoggerFactory;
 
 import commonj.sdo.Property;
 
 public class MappingWrapper {
 
     private static final ConfigFactory FACTORY = ConfigFactory.INSTANCE;
 
     private final Logger logger = LoggerFactory.INSTANCE.getLogger(MappingWrapper.class);
 
     private Config config;
 
     public MappingWrapper() {
         config = FACTORY.createConfig();
     }
 
     public MappingWrapper(Config mapping) {
         if (mapping == null) {
             this.config = FACTORY.createConfig();
         } else {
             this.config = mapping;
         }
     }
 
     public Config getConfig() {
         return this.config;
     }
 
     public Table getTable(String tableName) {
 
         if (this.logger.isDebugEnabled()) {
             this.logger.debug("Looking for table " + tableName);
         }
 
         Iterator i = config.getTable().iterator();
         while (i.hasNext()) {
             Table t = (Table) i.next();
             if (tableName.equalsIgnoreCase(t.getTableName())) {
                 return t;
             }
         }
 
         return null;
     }
 
     public Table getTableByTypeName(String typeName) {
 
         if (this.logger.isDebugEnabled()) {
             this.logger.debug("Looking for table by property: " + typeName);
         }
 
         Iterator i = config.getTable().iterator();
         while (i.hasNext()) {
             Table t = (Table) i.next();
             TableWrapper wrapper = new TableWrapper(t);
             if (typeName.equals(wrapper.getTypeName())) {
                 return t;
             }
         }
         return null;
 
     }
 
     public void addImpliedRelationship(String parentTableName, String childTableName, String fkColumnName) {
 
         // Don't create a relationship for something like Book.Book_ID
         if (parentTableName.equalsIgnoreCase(childTableName)) {
             return;
         }
 
         // Don't create a relationship if one already exists in the config
         Iterator i = config.getRelationship().iterator();
         while (i.hasNext()) {
             Relationship r = (Relationship) i.next();
             if (r.getPrimaryKeyTable().equals(parentTableName) && r.getForeignKeyTable().equals(childTableName)) {
                 return;
             }
         }
 
         Relationship r = FACTORY.createRelationship();
         r.setName(childTableName);
         r.setPrimaryKeyTable(parentTableName);
         r.setForeignKeyTable(childTableName);
 
         KeyPair pair = FACTORY.createKeyPair();
         pair.setPrimaryKeyColumn("ID");
         pair.setForeignKeyColumn(fkColumnName);
 
         r.getKeyPair().add(pair);
         r.setMany(true);
 
         config.getRelationship().add(r);
     }
 
     public Relationship addRelationship(String parentName, String childName) {
 
         QualifiedColumn parent = new QualifiedColumn(parentName);
         QualifiedColumn child = new QualifiedColumn(childName);
 
         Relationship r = FACTORY.createRelationship();
         r.setName(child.getTableName());
         r.setPrimaryKeyTable(parent.getTableName());
         r.setForeignKeyTable(child.getTableName());
 
         if (this.logger.isDebugEnabled()) {
             this.logger.debug("Created relationship from " + r.getPrimaryKeyTable() 
                     + " to " + r.getForeignKeyTable() + " named " + r.getName());
         }
 
         KeyPair pair = FACTORY.createKeyPair();
         pair.setPrimaryKeyColumn(parent.getColumnName());
         pair.setForeignKeyColumn(child.getColumnName());
 
         r.getKeyPair().add(pair);
         r.setMany(true);
 
         config.getRelationship().add(r);
 
         return r;
 
     }
 
     public void addPrimaryKey(String columnName) {
         addPrimaryKey(Collections.singletonList(columnName));
     }
 
     public void addPrimaryKey(List columnNames) {
 
         Iterator i = columnNames.iterator();
         while (i.hasNext()) {
             String columnName = (String) i.next();
 
             QualifiedColumn pkColumn = new QualifiedColumn(columnName);
             Table t = findOrCreateTable(pkColumn.getTableName());
             Column c = findOrCreateColumn(t, pkColumn.getColumnName());
             c.setPrimaryKey(true);
         }
     }
 
     public String getTableTypeName(String tableName) {
         Table t = getTable(tableName);
         if (t == null) {
             return tableName;
         }
         String propertyName = t.getTypeName();
 
         if (propertyName == null) {
             return tableName;
         }
 
         return propertyName;
     }
 
     public Column getColumn(Table t, String columnName) {
         if (t == null) {
             return null;
         }
         Iterator i = t.getColumn().iterator();
         while (i.hasNext()) {
             Column c = (Column) i.next();
             if (c.getColumnName().equals(columnName)) {
                 return c;
             }
         }
 
         if (this.logger.isDebugEnabled()) {
             this.logger.debug("WARNING: Could not find column " + columnName + " in table " + t.getTableName());
         }
 
         return null;
     }
 
     public Column getColumnByPropertyName(Table t, String propertyName) {
         if (t == null) {
             return null;
         }
         Iterator i = t.getColumn().iterator();
         while (i.hasNext()) {
             Column c = (Column) i.next();
             if (c.getColumnName().equals(propertyName)) {
                 return c;
             }
             
             if (c.getPropertyName() != null && c.getPropertyName().equals(propertyName)) {
                 return c;
             }
         }
 
         if (this.logger.isDebugEnabled()) {
             this.logger.debug("WARNING: Could not find column " + propertyName + " in table " + t.getTableName());
         }
 
         return null;
     }
 
     public String getColumnPropertyName(String tableName, String columnName) {
         Table t = getTable(tableName);
         Column c = getColumn(t, columnName);
         if (c == null) {
             return columnName;
         }
 
         String propertyName = c.getPropertyName();
         if (propertyName == null) {
             return c.getColumnName();
         }
 
         return propertyName;
     }
 
     public Table addTable(String tableName, String typeName) {
         Table table = getTable(tableName);
         if (table != null) {
             throw new RuntimeException("Table " + tableName + "already exists");
         }
 
         table = ConfigFactory.INSTANCE.createTable();
         table.setTableName(tableName);
         table.setTypeName(typeName);
         config.getTable().add(table);
 
         return table;
     }
 
     public Column addColumn(Table table, String name, String propertyName) {
         Column column = ConfigFactory.INSTANCE.createColumn();
         column.setColumnName(name);
         column.setPropertyName(propertyName);
         
         table.getColumn().add(column);
         return column;
     }
     
     private Table findOrCreateTable(String tableName) {
         Table table = getTable(tableName);
         if (table == null) {
             table = ConfigFactory.INSTANCE.createTable();
             table.setTableName(tableName);
             config.getTable().add(table);
         }
         return table;
 
     }
 
     private Column findOrCreateColumn(Table t, String name) {
         Iterator i = t.getColumn().iterator();
         while (i.hasNext()) {
             Column c = (Column) i.next();
             if (name.equals(c.getColumnName())) {
                 return c;
             }
         }
 
         Column c = ConfigFactory.INSTANCE.createColumn();
         c.setColumnName(name);
         t.getColumn().add(c);
         return c;
     }
 
     public boolean hasRecursiveRelationships() {
         if (config != null) {
             Iterator i = getConfig().getRelationship().iterator();
             while (i.hasNext()) {
                 Relationship r = (Relationship) i.next();
                 if (r.getPrimaryKeyTable().equals(r.getForeignKeyTable())) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public Collection getRelationshipsByChildTable(String name) {
         List results = new ArrayList();
         if (config != null) {
             Iterator i = getConfig().getRelationship().iterator();
             while (i.hasNext()) {
                 Relationship r = (Relationship) i.next();
                 if (name.equals(r.getForeignKeyTable())) {
                     results.add(r);
                 }
             }
         }
         return results;
     }
 
     // TODO optimize
     public List getInsertOrder() {
         if (this.logger.isDebugEnabled()) {
             this.logger.debug("Getting insert order");
         }
 
         List inserts = new ArrayList();
         Map parentToChild = new HashMap();
 
         List parents = new ArrayList();
         List children = new ArrayList();
         if (config != null) {
             Iterator i = getConfig().getRelationship().iterator();
             while (i.hasNext()) {
                 Relationship r = (Relationship) i.next();
                 parents.add(r.getPrimaryKeyTable());
                 children.add(r.getForeignKeyTable());
                 parentToChild.put(r.getPrimaryKeyTable(), r.getForeignKeyTable());
             }
             while (parents.size() > 0) {
                 String parent = (String) parents.get(0);
                 if (!children.contains(parent)) {
                     if (!inserts.contains(parent)) {
                         inserts.add(parent);
                     }
                     String child = (String) parentToChild.get(parent);
                     if (!inserts.contains(child)) {
                         inserts.add(child);
                     }
                     parents.remove(parent);
                     children.remove(child);
                 } else {
                     parents.add(parents.remove(0));
                 }
             }
             inserts.addAll(children);
         }
 
         if (this.logger.isDebugEnabled()) {
             this.logger.debug(inserts);
         }
 
         return inserts;
     }
 
     public List getDeleteOrder() {
         List deleteOrder = new ArrayList();
         deleteOrder.addAll(getInsertOrder());
         Collections.reverse(deleteOrder);
         return deleteOrder;
     }
 
     public void addConverter(String name, String converter) {
 
         QualifiedColumn column = new QualifiedColumn(name);
         Table t = findOrCreateTable(column.getTableName());
         Column c = findOrCreateColumn(t, column.getColumnName());
         c.setConverterClassName(converter);
 
     }
 
     public String getConverter(String tableName, String columnName) {
         Table t = getTable(tableName);
         Column c = getColumn(t, columnName);
         if (c != null) {
             return c.getConverterClassName();
         }
         return null;
     }
 
     public Map getConverters(Table table) {
         Map converters = new HashMap();
 
         Iterator columns = table.getColumn().iterator();
         while (columns.hasNext()) {
             Column c = (Column) columns.next();
             if (c.getConverterClassName() != null) {
                 String property = c.getPropertyName();
                 if (property == null) {
                     property = c.getColumnName();
                 }
                 converters.put(property, c.getConverterClassName());
             }
         }
         return converters;
     }
 
     public Relationship getRelationshipByReference(Property ref) {
         Iterator i = config.getRelationship().iterator();
         while (i.hasNext()) {
             Relationship r = (Relationship) i.next();
             if (ref.getName().equals(r.getName()) || ref.getOpposite().getName().equals(r.getName())) {
                 return r;
             }
         }
         throw new RuntimeException("Could not find relationship " + ref.getName() + " in the configuration");
     }
 
     public Relationship getRelationshipByName(String name) {
         Iterator i = config.getRelationship().iterator();
         while (i.hasNext()) {
             Relationship r = (Relationship) i.next();
             if (name.equals(r.getName())) {
                 return r;
             }
         }
         throw new RuntimeException("Could not find relationship " + name + " in the configuration");
     }
 
     public void addUpdateStatement(Table table, String statement, String parameters) {
 
         Update update = ConfigFactory.INSTANCE.createUpdate();
         update.setSql(statement);
         update.setParameters(parameters);
         table.setUpdate(update);
 
     }
 
     public void addDeleteStatement(Table table, String statement, String parameters) {
 
         Delete delete = ConfigFactory.INSTANCE.createDelete();
         delete.setSql(statement);
         delete.setParameters(parameters);
         table.setDelete(delete);
 
     }
 
     public void addCreateStatement(Table table, String statement, String parameters) {
 
         Create create = ConfigFactory.INSTANCE.createCreate();
         create.setSql(statement);
         create.setParameters(parameters);
         table.setCreate(create);
 
     }
 
     //JIRA-948 support for driver manager connection
     public void addConnectionInfo(String dataSourceName, boolean managedtx, boolean useDriverManager, String driverClass, String user, String password, int loginTimeout) {
         ConnectionInfo info = ConfigFactory.INSTANCE.createConnectionInfo();
         info.setDataSource(dataSourceName);
         info.setManagedtx(managedtx);
        info.setUseDriverManager(useDriverManager);
         
         ConnectionProperties connectionProperties = ConfigFactory.INSTANCE.createConnectionProperties(); 
         connectionProperties.setDriverClass(driverClass);
                  
         connectionProperties.setUserName(user);
         connectionProperties.setPassword(password);
         connectionProperties.setLoginTimeout(loginTimeout);            
 
         info.setConnectionProperties(connectionProperties);
         config.setConnectionInfo(info);
     }
     //JIRA-948 end
 
     public Command addCommand(String name, String sql, String kind) {
         Command cmd = ConfigFactory.INSTANCE.createCommand();
         cmd.setName(name);
         cmd.setKind(kind);
         cmd.setSQL(sql);
 
         config.getCommand().add(cmd);
 
         return cmd;
     }
 
     public void addImpliedPrimaryKey(String tableName, String columnName) {
         Table t = findOrCreateTable(tableName);
 
         Iterator i = t.getColumn().iterator();
         boolean hasPK = false;
         while (i.hasNext()) {
             Column c = (Column) i.next();
             if (c.isPrimaryKey()) {
                 hasPK = true;
             }
         }
 
         if (!hasPK) {
             Column c = findOrCreateColumn(t, columnName);
             c.setPrimaryKey(true);
         }
 
     }
 
 }
