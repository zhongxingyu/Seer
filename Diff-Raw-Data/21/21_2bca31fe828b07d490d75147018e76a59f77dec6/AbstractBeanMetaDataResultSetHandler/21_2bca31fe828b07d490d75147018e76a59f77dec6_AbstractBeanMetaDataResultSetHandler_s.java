 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dao.impl;
 
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.seasar.dao.BeanMetaData;
 import org.seasar.dao.RelationPropertyType;
 import org.seasar.extension.jdbc.PropertyType;
 import org.seasar.extension.jdbc.ResultSetHandler;
 import org.seasar.extension.jdbc.ValueType;
 import org.seasar.framework.beans.PropertyDesc;
 import org.seasar.framework.util.CaseInsensitiveSet;
 import org.seasar.framework.util.ClassUtil;
 import org.seasar.framework.util.StringUtil;
 
 public abstract class AbstractBeanMetaDataResultSetHandler implements
         ResultSetHandler {
 
     private BeanMetaData beanMetaData;
 
     public AbstractBeanMetaDataResultSetHandler(BeanMetaData beanMetaData) {
         this.beanMetaData = beanMetaData;
 
     }
 
     public BeanMetaData getBeanMetaData() {
         return beanMetaData;
     }
 
     protected Object createRow(ResultSet rs, Set columnNames)
             throws SQLException {
 
         Object row = ClassUtil.newInstance(beanMetaData.getBeanClass());
         for (int i = 0; i < beanMetaData.getPropertyTypeSize(); ++i) {
             PropertyType pt = beanMetaData.getPropertyType(i);
             if (columnNames.contains(pt.getColumnName())) {
                 ValueType valueType = pt.getValueType();
                 Object value = valueType.getValue(rs, pt.getColumnName());
                 PropertyDesc pd = pt.getPropertyDesc();
                 pd.setValue(row, value);
             } else if (columnNames.contains(pt.getPropertyName())) {
                 ValueType valueType = pt.getValueType();
                 Object value = valueType.getValue(rs, pt.getPropertyName());
                 PropertyDesc pd = pt.getPropertyDesc();
                 pd.setValue(row, value);
             } else if (!pt.isPersistent()) {
                 for (Iterator iter = columnNames.iterator(); iter.hasNext();) {
                     String columnName = (String) iter.next();
                     String columnName2 = StringUtil
                             .replace(columnName, "_", "");
                     if (columnName2.equalsIgnoreCase(pt.getColumnName())) {
                         ValueType valueType = pt.getValueType();
                         Object value = valueType.getValue(rs, columnName);
                         PropertyDesc pd = pt.getPropertyDesc();
                         pd.setValue(row, value);
                         break;
                     }
                 }
             }
         }
         return row;
     }
 
     protected Object createRelationRow(ResultSet rs, RelationPropertyType rpt,
             Set columnNames, Map relKeyValues) throws SQLException {
 
         Object row = null;
         BeanMetaData bmd = rpt.getBeanMetaData();
         for (int i = 0; i < rpt.getKeySize(); ++i) {
             String columnName = rpt.getMyKey(i);
             if (columnNames.contains(columnName)) {
                 if (row == null) {
                     row = createRelationRow(rpt);
                 }
                 if (relKeyValues != null
                         && relKeyValues.containsKey(columnName)) {
                     Object value = relKeyValues.get(columnName);
                     PropertyType pt = bmd.getPropertyTypeByColumnName(rpt
                             .getYourKey(i));
                     PropertyDesc pd = pt.getPropertyDesc();
                     if (value != null) {
                         pd.setValue(row, value);
                     }
                 }
             }
             continue;
         }
         int existColumn = 0;
         for (int i = 0; i < bmd.getPropertyTypeSize(); ++i) {
             PropertyType pt = bmd.getPropertyType(i);
             String columnName = pt.getColumnName() + "_" + rpt.getRelationNo();
             if (!columnNames.contains(columnName)) {
                 continue;
             }
             existColumn++;
             if (row == null) {
                 row = createRelationRow(rpt);
             }
             Object value = null;
             if (relKeyValues != null && relKeyValues.containsKey(columnName)) {
                 value = relKeyValues.get(columnName);
             } else {
                 ValueType valueType = pt.getValueType();
                 value = valueType.getValue(rs, columnName);
             }
             PropertyDesc pd = pt.getPropertyDesc();
             if (value != null) {
                 pd.setValue(row, value);
             }
         }
         if (existColumn == 0) {
             return null;
         }
         return row;
     }
 
     protected Object createRelationRow(RelationPropertyType rpt) {
         return ClassUtil.newInstance(rpt.getPropertyDesc().getPropertyType());
     }
 
    protected Set createColumnNames(ResultSetMetaData rsmd) throws SQLException {
        int count = rsmd.getColumnCount();
        Set columnNames = new CaseInsensitiveSet();
         for (int i = 0; i < count; ++i) {
            String columnName = rsmd.getColumnLabel(i + 1);
            columnNames.add(columnName);
         }
         return columnNames;
     }
 
 }
