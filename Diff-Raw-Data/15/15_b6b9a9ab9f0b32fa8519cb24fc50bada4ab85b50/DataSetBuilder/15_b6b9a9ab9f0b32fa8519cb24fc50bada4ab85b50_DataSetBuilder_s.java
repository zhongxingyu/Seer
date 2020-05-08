 /**
  * Copyright (C) 2009 kiy0taka.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.kiy0taka.dbunit;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.dbunit.dataset.DataSetException;
 import org.dbunit.dataset.DefaultDataSet;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.ITable;
 import org.dbunit.dataset.ReplacementDataSet;
 import org.dbunit.dataset.ReplacementTable;
 import org.dbunit.dataset.filter.DefaultColumnFilter;
 
 /**
  * DataSet Builder.
  * @author kiy0taka
  */
 public class DataSetBuilder {
 
     private final IDataSet dataSet;
 
     private final Map<Object, Object> replaceMap = new HashMap<Object, Object>();
 
     private final List<String> defaultExcludeColumns = new ArrayList<String>();
 
     private final Map<String, List<String>> tableExcludeColumns = new HashMap<String, List<String>>();
 
     private boolean isTrim;
 
     /**
      * Create new Builder.
      * @param dataSet {@link IDataSet}
      */
     public DataSetBuilder(IDataSet dataSet) {
         this.dataSet = dataSet;
     }
 
     /**
      * Add null replacement.
      * @param nullValue value to replace null
      * @return this builder
      */
     public DataSetBuilder nullValue(Object nullValue) {
         return replacement(nullValue, null);
     }
 
     /**
      * Add replacement.
      * @param orig Original value
      * @param replacement replacement value
      * @return this builder
      */
     public DataSetBuilder replacement(Object orig, Object replacement) {
         replaceMap.put(orig, replacement);
         return this;
     }
 
     /**
      * Add exclude columns.
      * @param excludeColumnNames exclude column names (i. e. "empno", "emp.empno")
      * @return this builder
      */
     public DataSetBuilder excludeColumns(String... excludeColumnNames) {
         for (String columnName : excludeColumnNames) {
             int dotIndex = columnName.indexOf('.');
             if (dotIndex > 0) {
                 String tableName = columnName.substring(0, dotIndex);
                 List<String> columnNames = tableExcludeColumns.get(tableName);
                 if (columnNames == null) {
                     columnNames = new ArrayList<String>();
                     tableExcludeColumns.put(tableName, columnNames);
                 }
                 columnNames.add(columnName.substring(dotIndex + 1));
             } else {
                 defaultExcludeColumns.add(columnName);
             }
         }
         return this;
     }
 
    public DataSetBuilder trim(boolean isTrim) {
        this.isTrim = isTrim;
         return this;
     }
 
     /**
      * Convert this to {@link IDataSet}.
      * @return converted {@link IDataSet}
      * @throws DataSetException DataSet creation failure.
      */
     public IDataSet toDataSet() throws DataSetException {
         IDataSet result = dataSet;
         if (!replaceMap.isEmpty()) {
             result = new ReplacementDataSet(result, replaceMap, null);
         }
         if (!defaultExcludeColumns.isEmpty() || !tableExcludeColumns.isEmpty()) {
             DefaultDataSet defaultDataSet = new DefaultDataSet();
             for (String tableName : result.getTableNames()) {
                 Set<String> excludeColumnNameSet = new HashSet<String>(defaultExcludeColumns);
                 List<String> excludeColumns = tableExcludeColumns.get(tableName);
                 if (excludeColumns != null) {
                     excludeColumnNameSet.addAll(excludeColumns);
                 }
                 defaultDataSet.addTable(DefaultColumnFilter.excludedColumnsTable(result.getTable(tableName),
                         excludeColumnNameSet.toArray(new String[excludeColumnNameSet.size()])));
             }
             result = defaultDataSet;
         }
         if (isTrim) {
             DefaultDataSet defaultDataSet = new DefaultDataSet();
             for (String tableName : result.getTableNames()) {
                 defaultDataSet.addTable(new TrimTable(result.getTable(tableName)));
             }
             result = defaultDataSet;
         }
         return result;
     }
 
     public static DataSetBuilder dataSet(IDataSet dataSet) {
         return new DataSetBuilder(dataSet);
     }
 
     private static class TrimTable extends ReplacementTable {
 
         public TrimTable(ITable table) {
             super(table);
         }
 
         @Override
         public Object getValue(int row, String column) throws DataSetException {
             Object result = super.getValue(row, column);
             if (result instanceof String) {
                 return ((String) result).trim();
             }
             return result;
         }
     }
 }
