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
 import java.util.Iterator;
 
 import org.apache.tuscany.das.rdb.config.Column;
 import org.apache.tuscany.das.rdb.config.Table;
 
 public class TableWrapper {
 
 	private Table table;
 
 	public TableWrapper(Table table) {
 		this.table = table;
 	}
 
 	public String getTypeName() {
 		return table.getTypeName() == null ? table.getTableName() : table
 				.getTypeName();
 	}
 
 	public Collection getPrimaryKeyNames() {
 		ArrayList pkNames = new ArrayList();
 		Iterator i = table.getColumn().iterator();
 		while (i.hasNext()) {
 			Column c = (Column) i.next();
 			if (c.isPrimaryKey())
 				pkNames.add(c.getColumnName());
 		}
 		return pkNames;
 	}
 
 	public Collection getPrimaryKeyProperties() {
 
 		ArrayList keyProperties = new ArrayList();
 		Iterator columns = table.getColumn().iterator();
 		while (columns.hasNext()) {
 			Column c = (Column) columns.next();
 			if (c.isPrimaryKey()) {
 				keyProperties.add(getColumnPropertyName(c));
 			}
 		}
 
 		return keyProperties;
 	}
 
 	private String getColumnPropertyName(Column c) {
 		if (c.getPropertyName() != null)
 			return c.getPropertyName();
 		else
 			return c.getColumnName();
 	}
 
 	public boolean isGeneratedColumnProperty(String name) {
 		Column c = getColumnByPropertyName(name);
 		return c == null ? false : c.isGenerated();
 	}
 
 
 	public String getConverter(String propertyName) {
 		Column c = getColumnByPropertyName(propertyName);
 		return (c == null) ? null : c.getConverterClassName();
 	}
 
 	public Column getColumnByPropertyName(String propertyName) {
 		Iterator columns = table.getColumn().iterator();
 		while (columns.hasNext()) {
 			Column c = (Column) columns.next();
 			String property = c.getPropertyName();
 			if (property == null)
 				property = c.getColumnName();
 			if (propertyName.equals(property))
 				return c;
 		}
 
 		return null;
 	}
 
 	public Column getCollisionColumn() {
 		Iterator columns = table.getColumn().iterator();
 		while (columns.hasNext()) {
 			Column c = (Column) columns.next();
 			if ( c.isCollision() )
 				return c;
 		}
 
 		return null;
 
 	}
 
 	public String getCollisionColumnPropertyName() {
 		Column c = getCollisionColumn();
 		if ( c.getPropertyName() != null ) 
 			return c.getPropertyName();
 		else
 			return c.getColumnName();
 	}
 
 	public String getManagedColumnPropertyName() {
 		Iterator i = table.getColumn().iterator();
 		while (i.hasNext()) {
 			Column c = (Column) i.next();
 			if (c.isManaged())
 				return c.getPropertyName() == null ? c.getColumnName() : c.getPropertyName();
 		}
 		return null;
 		
 	}

	public Column getManagedColumn() {
		Iterator i = table.getColumn().iterator();
		while (i.hasNext()) {
			Column c = (Column) i.next();
			if (c.isManaged())
				return c;
		}
		return null;
	}
 }
