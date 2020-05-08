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
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.apache.tuscany.das.rdb.config.Relationship;
 import org.apache.tuscany.das.rdb.config.Table;
 import org.apache.tuscany.das.rdb.config.wrapper.MappingWrapper;
 import org.apache.tuscany.das.rdb.config.wrapper.RelationshipWrapper;
 import org.apache.tuscany.das.rdb.config.wrapper.TableWrapper;
 import org.apache.tuscany.das.rdb.impl.InsertCommandImpl;
 import org.apache.tuscany.das.rdb.impl.ParameterImpl;
 import org.apache.tuscany.das.rdb.util.LoggerFactory;
 
 import commonj.sdo.DataObject;
 import commonj.sdo.Property;
 
 public final class InsertGenerator extends BaseGenerator {
 
     public static final InsertGenerator INSTANCE = new InsertGenerator();
 
     private final Logger logger = LoggerFactory.INSTANCE.getLogger(InsertGenerator.class);
 
     private InsertGenerator() {
         super();
     }
 
     public InsertCommandImpl getInsertCommand(MappingWrapper config, DataObject changedObject, Table t) {
         List parameters = new ArrayList();
         TableWrapper table = new TableWrapper(t);
         StringBuffer statement = new StringBuffer("insert into ");
         statement.append(t.getTableName());
 
         Iterator i = getAttributeProperties(changedObject, config, table).iterator();
 
         List attributes = new ArrayList();
         List generatedKeys = new ArrayList();
         while (i.hasNext()) {
             Property attr = (Property) i.next();
             if (table.isGeneratedColumnProperty(attr.getName())) {
                 generatedKeys.add(attr.getName());
             } else {
                 attributes.add(attr.getName());
                 parameters.add(changedObject.getType().getProperty(attr.getName()));
             }
         }
 
         statement.append("(");
         Iterator attrs = attributes.iterator();
         while (attrs.hasNext()) {
             String name = (String) attrs.next();
             statement.append("");
            statement.append(name);
             if (attrs.hasNext()) {
                 statement.append(", ");
             } else {
                 statement.append(")");
             }
         }
 
         statement.append(" values (");
         for (int idx = 1; idx <= attributes.size(); idx++) {
             statement.append('?');
             if (idx < attributes.size()) {
                 statement.append(", ");
             } else {
                 statement.append(")");
             }
         }
 
         InsertCommandImpl cmd = new InsertCommandImpl(statement.toString(), 
                 (String[]) generatedKeys.toArray(new String[0]));
         Iterator params = parameters.iterator();
         for (int idx = 1; params.hasNext(); idx++) {
             Property property = (Property) params.next();
             ParameterImpl p = new ParameterImpl();
             p.setName(property.getName());
             p.setType(property.getType());
             p.setConverter(getConverter(table.getConverter(property.getName())));
             p.setIndex(idx);
             cmd.addParameter(p);
 
         }
         if (this.logger.isDebugEnabled()) {
             this.logger.debug(statement.toString());
         }
 
         return cmd;
 
     }
 
     private List getAttributeProperties(DataObject obj, MappingWrapper config, TableWrapper tw) {
         List fields = new ArrayList();
         Iterator i = obj.getType().getProperties().iterator();
         while (i.hasNext()) {
             Property p = (Property) i.next();
             if (p.getType().isDataType()) {
                 if (obj.isSet(p)) {
                     fields.add(p);
                 }
             } else {
                 if (obj.isSet(p)) {
                     Relationship relationship = config.getRelationshipByReference(p);
                     if ((p.getOpposite() != null && p.getOpposite().isMany()) 
                             || (hasState(tw, relationship, obj))) {
                         RelationshipWrapper r = new RelationshipWrapper(relationship);
                         Iterator keys = r.getForeignKeys().iterator();
                         while (keys.hasNext()) {
                             String key = (String) keys.next();
                             String keyProperty = config.getColumnPropertyName(tw.getTableName(), key);
                             Property keyProp = obj.getType().getProperty(keyProperty);
                             fields.add(keyProp);
                         }
                     }
 
                 }
             }
         }
 
         return fields;
 
     }
 
     private boolean hasState(TableWrapper tw, Relationship rel, DataObject changedObject) {
 
         if (!rel.isMany()) {
                    
             RelationshipWrapper rw = new RelationshipWrapper(rel);
             if ((rel.getForeignKeyTable().equals(tw.getTableName())) 
                     && (Collections.disjoint(tw.getPrimaryKeyProperties(), rw.getForeignKeys()))) {
                 return true;
             }
         }
 
         return false;
     }
 
 }
