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
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
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
 import org.apache.tuscany.das.rdb.config.Parameters;
 import org.apache.tuscany.das.rdb.config.Relationship;
 import org.apache.tuscany.das.rdb.config.Table;
 import org.apache.tuscany.das.rdb.config.Update;
 
 import commonj.sdo.Property;
 
 public class MappingWrapper {
 
     private static final ConfigFactory FACTORY = ConfigFactory.INSTANCE;
 
     private final Logger logger = Logger.getLogger(MappingWrapper.class);
 
     private Config config;
     
 	// TUSCANY-2288
 	private List insertOrder;
 	private List deleteOrder;
 	private Set rootTableNames;
 	// --
 
 
     public MappingWrapper() {
         config = FACTORY.createConfig();
         //JIRA-952
         this.checkSchemaNameSupport();
     }
 
 	//JIRA-952 - check if there is any entry with no schemaName when support is ON
     public void checkSchemaNameSupport(){
         if(config.isDatabaseSchemaNameSupported()){
         	List tableList = config.getTable();
         	for(int i=0; i<tableList.size(); i++){
         		Table t = (Table)tableList.get(i);
         		if(t.getSchemaName() == null || t.getSchemaName().equals("")){
         			throw new RuntimeException("No schemaName provided for tableName "+t.getTableName()+" when schemaNameSupport is ON");
         		}
         		
         		if(t.getTypeName()==null){
         			t.setTypeName(t.getSchemaName()+"."+t.getTableName());
         		}
         	}
         }
     }
     public MappingWrapper(Config mapping) {
         if (mapping == null) {
             this.config = FACTORY.createConfig();
         } else {
             this.config = mapping;
         }
         //JIRA-952
         checkSchemaNameSupport();
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
             //JIRA-952
             if(this.config.isDatabaseSchemaNameSupported()){
             	if (tableName.equalsIgnoreCase(t.getSchemaName()+"."+t.getTableName())) {
                     return t;
                 }
             }else{
             if (tableName.equalsIgnoreCase(t.getTableName())) {
                 return t;
                 }            	
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
             if (r.getPrimaryKeyTable().equalsIgnoreCase(parentTableName) && r.getForeignKeyTable().equalsIgnoreCase(childTableName)) {
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
     	return addRelationship(parentName, childName, null); 
     }
     
     public Relationship addRelationship(String parentName, String childName, String relationshipName) {
 
     	//JIRA-952
         QualifiedColumn parent = null;
         QualifiedColumn child = null;
 
         if(this.config.isDatabaseSchemaNameSupported()){
         	parent = new QualifiedColumn(parentName, true);
         	child = new QualifiedColumn(childName, true);
         }
         else{
         	parent = new QualifiedColumn(parentName);
         	child = new QualifiedColumn(childName);        	
         }
         
         Relationship r = FACTORY.createRelationship();
         //JIRA-952
         if(this.config.isDatabaseSchemaNameSupported()){        	
         	if(relationshipName == null)
         		r.setName(child.getSchemaName()+"."+child.getTableName());
         	else
         		r.setName(relationshipName);
         	
             r.setPrimaryKeyTable(parent.getSchemaName()+"."+parent.getTableName());
             r.setForeignKeyTable(child.getSchemaName()+"."+child.getTableName());        	
         }
         else{
         	if(relationshipName == null)
         		r.setName(child.getTableName());
         	else
         		r.setName(relationshipName);
         	
 	        r.setPrimaryKeyTable(parent.getTableName());
 	        r.setForeignKeyTable(child.getTableName());
         }
 
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
     
     public Relationship addRelationship(Vector parentNames, Vector childNames) {
     	return addRelationship(parentNames, childNames, null);
     }
     
     public Relationship addRelationship(Vector parentNames, Vector childNames, String relationshipName) {
         //create vector for each
         if(parentNames.size() != childNames.size()){
             throw new RuntimeException("Can not for relationship for multiple keys, different sizes");
         }
         
         Vector parentColumns = new Vector();
         Vector childColumns = new Vector();
         
         for(int i=0; i<parentNames.size(); i++){
             QualifiedColumn parent = null;
             QualifiedColumn child = null;
             
             if(this.config.isDatabaseSchemaNameSupported()){
             	parent = new QualifiedColumn((String)parentNames.get(i), true);
             	child = new QualifiedColumn((String)childNames.get(i), true);
             }
             else{
             	parent = new QualifiedColumn((String)parentNames.get(i));
             	child = new QualifiedColumn((String)childNames.get(i));
             }
             
             parentColumns.add(parent);
             childColumns.add(child);
         }
 
         //all parent columns should be from same table and schema
         //all child columns should be from same table and schema
         checkTableNames(parentColumns);
     	checkSchemaNames(parentColumns);
         
         checkTableNames(childColumns);
     	checkSchemaNames(childColumns);
         
         Relationship r = FACTORY.createRelationship();
         //JIRA-952
         if(this.config.isDatabaseSchemaNameSupported()){
         	if(relationshipName == null)
         		r.setName(((QualifiedColumn)childColumns.get(0)).getSchemaName()+"."
         				+((QualifiedColumn)childColumns.get(0)).getTableName());
         	else
         		r.setName(relationshipName);
         	
             r.setPrimaryKeyTable(((QualifiedColumn)parentColumns.get(0)).getSchemaName()+"."
             		+((QualifiedColumn)parentColumns.get(0)).getTableName());
             r.setForeignKeyTable(((QualifiedColumn)childColumns.get(0)).getSchemaName()+"."
             		+((QualifiedColumn)childColumns.get(0)).getTableName());        	
         }
         else{
         	if(relationshipName == null)
         		r.setName(((QualifiedColumn)childColumns.get(0)).getTableName());
         	else
         		r.setName(relationshipName);
         	
 	        r.setPrimaryKeyTable(((QualifiedColumn)parentColumns.get(0)).getTableName());
 	        r.setForeignKeyTable(((QualifiedColumn)childColumns.get(0)).getTableName());
         }
 
         if (this.logger.isDebugEnabled()) {
             this.logger.debug("Created relationship from " + r.getPrimaryKeyTable() 
                     + " to " + r.getForeignKeyTable() + " named " + r.getName());
         }
         
         KeyPair pair = null;
         
         for(int i=0; i<parentColumns.size(); i++){
             pair = FACTORY.createKeyPair();
             pair.setPrimaryKeyColumn(((QualifiedColumn)parentColumns.get(i)).getColumnName());
             pair.setForeignKeyColumn(((QualifiedColumn)childColumns.get(i)).getColumnName());
             r.getKeyPair().add(pair);
         }
         
         r.setMany(true);
         config.getRelationship().add(r);
 
         return r;
     }    
     
     public void checkTableNames(Vector columns){
         String expectedTableName = ((QualifiedColumn)columns.get(0)).getTableName();        
 
         for(int i=0; i<columns.size(); i++){
             QualifiedColumn currColumn = (QualifiedColumn)columns.get(i);
             String currTableName = currColumn.getTableName();
             
             if(!currTableName.equals(expectedTableName)){
                 throw new RuntimeException("Columns in one side of relationship can not be from different tables");
             }           
         }       
     }
     
     public void checkSchemaNames(Vector columns){
     	if(!this.config.isDatabaseSchemaNameSupported()){
     		return;
     	}
     	
     	 String	expectedSchemaName = ((QualifiedColumn)columns.get(0)).getSchemaName();
 
     	for(int i=0; i<columns.size(); i++){
     		QualifiedColumn currColumns = (QualifiedColumn)columns.get(i);
     		String	currSchemaName = currColumns.getSchemaName();
     		    		
     		if(!currSchemaName.equals(expectedSchemaName)){
     			throw new RuntimeException("Columns in one side of relationship can not be from different schema");
     		}
     	}
     }
     public void addPrimaryKey(String columnName) {
         addPrimaryKey(Collections.singletonList(columnName));
     }
 
     public void addPrimaryKey(String columnName, String propertyName) {
         addPrimaryKey(Collections.singletonList(columnName), propertyName);
     }
     
     public void addPrimaryKey(List columnNames) {
 
         Iterator i = columnNames.iterator();
         while (i.hasNext()) {
             String columnName = (String) i.next();
 
             QualifiedColumn pkColumn = null;
            	pkColumn = new QualifiedColumn(columnName, this.config.isDatabaseSchemaNameSupported());
             //Table t = findOrCreateTable(pkColumn.getTableName());
             Table t = findOrCreateTable(pkColumn);
             Column c = findOrCreateColumn(t, pkColumn.getColumnName());
             c.setPrimaryKey(true);
         }
     }
 
     public void addPrimaryKey(List columnNames, String propertyName) {
 
         Iterator i = columnNames.iterator();
         while (i.hasNext()) {
             String columnName = (String) i.next();
 
             QualifiedColumn pkColumn = null;
            	pkColumn = new QualifiedColumn(columnName, this.config.isDatabaseSchemaNameSupported());
             //Table t = findOrCreateTable(pkColumn.getTableName());
             Table t = findOrCreateTable(pkColumn);
             Column c = findOrCreateColumn(t, pkColumn.getColumnName());
             c.setPrimaryKey(true);
             c.setPropertyName(propertyName);
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
 
     //JIRA-952
     public Table addTable(String tableName, String schemaName, String typeName) {    	
         Table table = null;
         
         if(this.config.isDatabaseSchemaNameSupported()){
         	table = getTable(schemaName+"."+tableName);	
         }
         else{
         	table = getTable(tableName);
         }
         
         if (table != null) {
         	if(this.config.isDatabaseSchemaNameSupported()){
         		throw new RuntimeException("Table " + schemaName+"."+tableName + "already exists");	
         	}
         	else{
         		throw new RuntimeException("Table " + tableName + "already exists");
         	}            
         }
 
         table = ConfigFactory.INSTANCE.createTable();
         table.setTableName(tableName);
         
         if(this.config.isDatabaseSchemaNameSupported()){
         	table.setSchemaName(schemaName);
         }
         else{
         	table.setSchemaName("");
         }
         
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
     
     //JIRA-952
     private Table findOrCreateTable(String schemaName, String tableName) {
         Table table = null;
         
         if(this.config.isDatabaseSchemaNameSupported()){
         	table = getTable(schemaName+"."+tableName);	
         }
         else{
         	table = getTable(tableName);	
         }        
         if (table == null) {
             table = ConfigFactory.INSTANCE.createTable();
             table.setTableName(tableName);
             table.setSchemaName(schemaName);
             
             if(this.config.isDatabaseSchemaNameSupported()){
             	table.setTypeName(schemaName+"."+tableName);
             }
             else{
             	table.setTypeName(tableName);
             }
             config.getTable().add(table);
         }
         return table;
 
     }
 
     //JIRA-952
     private Table findOrCreateTable(QualifiedColumn column) {   	
     	Table table = null;
     	if(this.config.isDatabaseSchemaNameSupported()){
     		table = getTable(column.getSchemaName()+"."+column.getTableName());
     	}
     	else{
     		table = getTable(column.getTableName());
     	}
 
         if (table == null) {
             table = ConfigFactory.INSTANCE.createTable();
             table.setTableName(column.getTableName());
             table.setSchemaName(column.getSchemaName());
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
     
     public Set getRecursiveTypeNames() {
     	Set typeNames = new HashSet();
         if (config != null) {
             Iterator i = getConfig().getRelationship().iterator();
             while (i.hasNext()) {
                 Relationship r = (Relationship) i.next();
                 if (r.getPrimaryKeyTable().equals(r.getForeignKeyTable())) {
                 	typeNames.add(getTableTypeName(r.getPrimaryKeyTable()));
                 }
             }
         }
         return typeNames;
     	
     }
 
     /*Parameter name is SDO Type name and so requires mapping in table<->type, if one is available in Config*/
     public Collection getRelationshipsByChildTable(String name) {
     	Table table = getTableByTypeName(name);
     	
     	if(table != null) {
 	    	if(config.isDatabaseSchemaNameSupported()) {
 	    		name = table.getSchemaName()+"."+table.getTableName();	
 	    	} else {
 	    		name = table.getTableName();
 	    	}
     	}
     	//table can be null, when no <Table> in Config and Query based Dynamic Types are used during query
     	
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
 
 	public Set getRootTableNames() {
 		if (this.logger.isDebugEnabled()) {
 			this.logger.debug("Getting root tables");
 		}
 		if (rootTableNames == null) {
 			rootTableNames = new HashSet();
 			if (config == null) return rootTableNames;
 			// parse all relationships
 			Set allParents = new HashSet();
 			Set allChildren = new HashSet();
 			Iterator i = getConfig().getRelationship().iterator();
 			while (i.hasNext()) {
 				Relationship r = (Relationship) i.next();
 				String parent = r.getPrimaryKeyTable();
 				String child = r.getForeignKeyTable();
 				if (parent.equals(child)) {
 					// self-relationship
 					// do not add to all children list to allow root detection
 					allParents.add(parent);
 				} else {
 					allParents.add(parent);
 					allChildren.add(child);
 				}
 			}
 			// find roots (depth 0)
 			// roots are tables that are present in the parents set, but not in the children set
 			for (Iterator itParents = allParents.iterator(); itParents.hasNext(); ) {
 				String parent = (String) itParents.next();
 				if (!allChildren.contains(parent)) {
 					rootTableNames.add(parent);
 				}
 			}
 		}
 		if (this.logger.isDebugEnabled()) {
 			this.logger.debug(rootTableNames);
 		}
 		return rootTableNames;
 	}
     
 	// TUSCANY-2288
 	public List getInsertOrder() {
 		if (this.logger.isDebugEnabled()) {
 			this.logger.debug("Getting insert order");
 		}
 		if (insertOrder == null) {
 			insertOrder = new ArrayList();
 			if (config == null) return insertOrder;
 			// correct insert order: tables sorted by ascending depth
 			// parse all relationships
 			Set allParents = new HashSet();
 			Set allChildren = new HashSet();
 			Map parentToChild = new HashMap();
 			Iterator i = getConfig().getRelationship().iterator();
 			while (i.hasNext()) {
 				Relationship r = (Relationship) i.next();
 				String parent = r.getPrimaryKeyTable();
 				String child = r.getForeignKeyTable();
 				if (parent.equals(child)) {
 					// self-relationship
					// do not add to parent to child map to avoid loops
 					// do not add to all children list to allow root detection
 					allParents.add(parent);
 				} else {
 					allParents.add(parent);
 					allChildren.add(child);
 					Set children = (Set) parentToChild.get(parent);
 					if (children == null) {
 						children = new HashSet();
 						parentToChild.put(parent, children);
 					}
 					children.add(child);
 				}
 			}
 			// build list of tables ordered by depth
 			List depthList = new ArrayList();
 			// find roots (depth 0)
 			// roots are tables that are present in the parents set, but not in the children set
 			Set roots = new HashSet();
 			for (Iterator itParents = allParents.iterator(); itParents.hasNext(); ) {
 				String parent = (String) itParents.next();
 				if (!allChildren.contains(parent)) {
 					roots.add(parent);
 				}
 			}
 			// traverse table graph to populate depth list
 			traverseTableGraph(roots, 0, parentToChild, depthList, new ArrayList());
 			// build insert order from depth list
 			for (Iterator itDepths = depthList.iterator(); itDepths.hasNext(); ) {
 				Set tables = (Set) itDepths.next();
 				insertOrder.addAll(tables);
 			}
 		}
 		if (this.logger.isDebugEnabled()) {
 			this.logger.debug(insertOrder);
 		}
 		return insertOrder;
 	}
 
 	private void traverseTableGraph(Set parents, int parentDepth, Map parentToChild, List depthList, List branch) {
 		int childDepth = parentDepth + 1;
 		// expand depth list if necessary
 		for (int i = depthList.size() - 1; i < parentDepth; i++) {
 			Set tables = new HashSet();
 			depthList.add(tables);
 		}
 		// loop thru parents
 		for (Iterator itParents = parents.iterator(); itParents.hasNext(); ) {
 			String parent = (String) itParents.next();
 			if (branch.contains(parent)) {
 				// we found a cycle
				// we don't handle cycles
				// stop traversing branch to avoid infinite loop
 				break;
 			}
 			// add parent to depth list
 			addTableToDepthList(parent, parentDepth, depthList);
 			// make recursive call on children
 			Set children = (Set) parentToChild.get(parent);
 			if (children != null && children.size() > 0) {
 				List parentBranch = new ArrayList();
 				parentBranch.addAll(branch);
 				parentBranch.add(parent);
 				traverseTableGraph(children, childDepth, parentToChild, depthList, parentBranch);
 			}
 		}
 	}
 
 	private void addTableToDepthList(String table, int depth, List depthList) {
 		// in this method, keep in mind that the same table can appear multiple times at multiple depths inside the table graph
 		// check if table is already somewhere in depth list
 		int currentDepth = getTableDepth(table, depthList);
 		if (currentDepth == -1) {
 			// table not in depth list
 			// add it
 			Set tables = (Set) depthList.get(depth);
 			tables.add(table);
 		} else if (currentDepth < depth) {
 			// table already in depth list, at a lower depth
 			// the table should only be present once, at the greatest possible depth
 			// replace table at the new depth
 			Set tables = (Set) depthList.get(currentDepth);
 			tables.remove(table);
 			tables = (Set) depthList.get(depth);
 			tables.add(table);
 		} else {
 			// table already in depth list, at a greater or same depth
 			// nothing to do, since the table should only be present once, at the greatest possible depth
 		}
 	}
 
 	private int getTableDepth(String table, List depthList) {
 		int depth = -1;
 		// loop thru depth list
 		int count = depthList.size();
 		for (int i = 0; i < count; i++) {
 			Set tables = (Set) depthList.get(i);
 			if (tables != null && tables.contains(table)) {
 				depth = i;
 				break;
 			}
 		}
 		return depth;
 	}
 
 
 	public List getDeleteOrder() {
 		if (deleteOrder == null) {
 			deleteOrder = new ArrayList();
 			deleteOrder.addAll(getInsertOrder());
 			Collections.reverse(deleteOrder);
 		}
 		return deleteOrder;
 	}
 
 	// --
 
 
     //JIRA-952
     public void addConverter(String name, String converter) {
         QualifiedColumn column = new QualifiedColumn(name, this.config.isDatabaseSchemaNameSupported());
         Table t = null;
        	t = findOrCreateTable(column);
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
             if (ref.getName().equals(r.getName())) {
             	if (getTableTypeName(r.getPrimaryKeyTable()).equals(ref.getContainingType().getName())) return r;
             } else if(ref.getOpposite().getName().equals(r.getName())) {
             	if (getTableTypeName(r.getForeignKeyTable()).equals(ref.getContainingType().getName())) return r;
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
 
     public void addUpdateStatement(Table table, String statement, Parameters parameters) {
 
         Update update = ConfigFactory.INSTANCE.createUpdate();
         update.setSql(statement);
         update.setParameters(parameters);
         table.setUpdate(update);
 
     }
 
     public void addDeleteStatement(Table table, String statement, Parameters parameters) {
 
         Delete delete = ConfigFactory.INSTANCE.createDelete();
         delete.setSql(statement);
         delete.setParameters(parameters);
         table.setDelete(delete);
 
     }
 
     public void addCreateStatement(Table table, String statement, Parameters parameters) {
 
         Create create = ConfigFactory.INSTANCE.createCreate();
         create.setSql(statement);
         create.setParameters(parameters);
         table.setCreate(create);
 
     }
 
     //JIRA-948 support for driver manager connection
     public void addConnectionInfo(String dataSourceName, boolean managedtx){
         ConnectionInfo info = ConfigFactory.INSTANCE.createConnectionInfo();
         info.setDataSource(dataSourceName);
         info.setManagedtx(managedtx);
 
         config.setConnectionInfo(info);
     }
     
     public void addConnectionInfo(String driverClass, String connectionURL, String user, String password, int loginTimeout) {
         ConnectionInfo info = ConfigFactory.INSTANCE.createConnectionInfo();
         
         ConnectionProperties connectionProperties = ConfigFactory.INSTANCE.createConnectionProperties(); 
         connectionProperties.setDriverClass(driverClass);
         connectionProperties.setDatabaseURL(connectionURL);
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
 
     //JIRA-952
     public void addImpliedPrimaryKey(String schemaName, String tableName, String columnName) {
         Table t = findOrCreateTable(schemaName, tableName);//JIRA-952
 
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
