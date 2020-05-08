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
 package org.apache.tuscany.das.rdb.generator.impl;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.apache.tuscany.das.rdb.config.Column;
 import org.apache.tuscany.das.rdb.config.Table;
 import org.apache.tuscany.das.rdb.config.wrapper.MappingWrapper;
 import org.apache.tuscany.das.rdb.config.wrapper.RelationshipWrapper;
 import org.apache.tuscany.das.rdb.config.wrapper.TableWrapper;
 import org.apache.tuscany.das.rdb.impl.ManagedParameterImpl;
 import org.apache.tuscany.das.rdb.impl.OptimisticWriteCommandImpl;
 import org.apache.tuscany.das.rdb.impl.ParameterImpl;
 import org.apache.tuscany.das.rdb.impl.UpdateCommandImpl;
 import org.apache.tuscany.das.rdb.util.LoggerFactory;
 
 import commonj.sdo.ChangeSummary;
 import commonj.sdo.DataObject;
 import commonj.sdo.Property;
 import commonj.sdo.Type;
 
 public class UpdateGenerator extends BaseGenerator {
 
     private final Logger logger = LoggerFactory.INSTANCE.getLogger(UpdateGenerator.class);
 
 	public static UpdateGenerator instance = new UpdateGenerator();
 
 	private UpdateGenerator() {
 		super();
 	}
 
 	public UpdateCommandImpl getUpdateCommand(MappingWrapper mapping, DataObject changedObject, Table table) {
 		ArrayList updatedProperties = new ArrayList();
 		ArrayList managedProperties = new ArrayList();
 		ArrayList whereClauseProperties = new ArrayList();
 		Type type = changedObject.getType();
 		TableWrapper t = new TableWrapper(table);
 		StringBuffer statement = new StringBuffer("update ");
 		statement.append(table.getTableName());
 		statement.append(" set ");
 
 
 		ChangeSummary summary = changedObject.getDataGraph().getChangeSummary();
 		Iterator i = getChangedFields(mapping, summary, changedObject).iterator();
 
 		while (i.hasNext()) {
 			Property property = (Property) i.next();
 			Column c = t.getColumnByPropertyName(property.getName());
 			if ((c != null) && (c.isCollision() || c.isPrimaryKey())) {
 				// get rid of comma if OCC or PK is last field
 				if (!i.hasNext()) {
 					statement
 							.delete(statement.length() - 2, statement.length());
 				}
 			} else {
 				updatedProperties.add(property);
 				statement.append(c == null ? property.getName() : c.getColumnName());
 				statement.append(" = ?");
 				if (i.hasNext())
 					statement.append(", ");
 			}
 		}
 
		Column c = t.getManagedColumn();
		if ( c != null ) {
 			statement.append(", ");
			statement.append(c.getColumnName());
 			statement.append(" = ?");
 			managedProperties.add(changedObject.getProperty(t.getManagedColumnPropertyName()));
 		}
 		statement.append(" where ");
 
 		Iterator names = t.getPrimaryKeyNames().iterator();
 		Iterator pkProperties = t.getPrimaryKeyProperties().iterator();
 		while (names.hasNext() && pkProperties.hasNext()) {
 			String name = (String) names.next();
 			String property = (String) pkProperties.next();
 			statement.append(name);
 			statement.append(" = ?");
 			if (names.hasNext() && pkProperties.hasNext())
 				statement.append(" and ");
 			whereClauseProperties.add(type.getProperty(property));
 		}
 
 		if (t.getCollisionColumn() != null) {
 			statement.append(" and ");
 			statement.append(t.getCollisionColumn().getColumnName());
 			statement.append(" = ?");
 			whereClauseProperties.add(type.getProperty(t.getCollisionColumnPropertyName()));
 		}
 
 
 		UpdateCommandImpl updateCommand;
 		if ( t.getCollisionColumn() != null )
 			updateCommand = new OptimisticWriteCommandImpl(statement.toString());
 		else
 			updateCommand = new UpdateCommandImpl(statement.toString());
 
 
 		int idx = 1;
 		Iterator params = updatedProperties.iterator();
 		while ( params.hasNext()) {
 			Property p = (Property)params.next();
 			updateCommand.addParameter(createParameter(t, p, idx++));
 		}
 
 		params = managedProperties.iterator();
 		while ( params.hasNext()) {
 			Property p = (Property)params.next();
 			updateCommand.addParameter(createManagedParameter(t, p, idx++));
 		}
 
 		params = whereClauseProperties.iterator();
 		while ( params.hasNext()) {
 			Property p = (Property)params.next();
 			updateCommand.addParameter(createParameter(t, p, idx++));
 		}
 
         if(this.logger.isDebugEnabled())
 		    this.logger.debug(statement.toString());
 
 		return updateCommand;
 	}
 
 
 
 	private List getChangedFields(MappingWrapper mapping, ChangeSummary summary, DataObject obj) {
 		ArrayList changes = new ArrayList();
 		Iterator i = summary.getOldValues(obj).iterator();
 		while (i.hasNext()) {
 			ChangeSummary.Setting setting = (ChangeSummary.Setting) i.next();
 			if (setting.getProperty().getType().isDataType()) {
 				changes.add(setting.getProperty());
 			} else  {
 				Property ref = setting.getProperty();
 				if ( !ref.isMany() ) {
 					RelationshipWrapper r = new RelationshipWrapper(mapping.getRelationshipByReference(ref));
 
 					Iterator keys = r.getForeignKeys().iterator();
 					while ( keys.hasNext()) {
 						String key = (String) keys.next();
 						Property p = obj.getType().getProperty(key);
 						changes.add(p);
 					}
 				}
 
 			}
 		}
 		return changes;
 	}
 
 
 
 	private ParameterImpl createManagedParameter(TableWrapper table, Property property, int idx) {
 		ParameterImpl param = new ManagedParameterImpl();
 		param.setName(property.getName());
 		param.setType(property.getType());
 		param.setConverter(getConverter(table.getConverter(property.getName())));
 		if ( idx != -1)
 			param.setIndex(idx);
 
 		return param;
 	}
 
 	private ParameterImpl createParameter(TableWrapper table, Property property, int idx) {
 		ParameterImpl param = new ParameterImpl();
 		param.setName(property.getName());
 		param.setType(property.getType());
 		param.setConverter(getConverter(table.getConverter(property.getName())));
 		if ( idx != -1)
 			param.setIndex(idx);
 
 		return param;
 	}
 
 
 }
 
