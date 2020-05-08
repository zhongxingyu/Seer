 /**
  * Copyright (C) 2009 Anthonin Bonnefoy and David Duponchel
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
 
 package airline.dao.impl;
 
 import airline.connector.Connector;
 import airline.dao.AirlineDAO;
 import airline.model.Table;
 import airline.model.TableColumn;
 import airline.model.TableRow;
 import airline.criteria.model.SelectRequest;
 import airline.criteria.model.IRequest;
 import com.google.inject.Inject;
 
 import java.sql.*;
 import java.util.*;
 
 /**
  * Dao implementation
  */
 public class AirlineDAOImpl implements AirlineDAO {
     private Connection connection;
 
     @Inject
     public AirlineDAOImpl(Connector connector) {
         connection = connector.getConnection();
     }
 
 
     public Map<String, Table> getTables() {
         Map<String, Table> res = new LinkedHashMap<String, Table>();
         Table tablesEntity;
         try {
             DatabaseMetaData metas = connection.getMetaData();
             ResultSet results = metas.getTables(null, null, null, null);
             while (results.next()) {
                 if (!results.getString(Table.TYPE).contains("SYSTEM")) {
                     String name = results.getString(Table.NAME);
                     tablesEntity = new Table(name);
                     tablesEntity.setType(results.getString(Table.TYPE));
                     tablesEntity.setSchema(results.getString(Table.SCHEMA));
                     res.put(name, tablesEntity);
                 }
             }
             return res;
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public List<TableColumn> getTableColumns(Table table) {
         List<TableColumn> res = new LinkedList<TableColumn>();
         List<String> primaryKeys = new ArrayList<String>();
         TableColumn tableColumn;
         try {
             DatabaseMetaData metas = connection.getMetaData();
             ResultSet results;
             results = metas.getPrimaryKeys(null, null, table.getName());
             while (results.next()) {
                 primaryKeys.add(results.getString(TableColumn.NAME));
             }
 
             results = metas.getColumns(null, null, table.getName(), null);
             while (results.next()) {
                 tableColumn = new TableColumn();
                 String name = results.getString(TableColumn.NAME);
                 tableColumn.setName(name);
                 tableColumn.setType(results.getString(TableColumn.TYPE));
                 tableColumn.setDataType(results.getShort(TableColumn.DATA_TYPE));
                 tableColumn.setTable(table);
                 tableColumn.setPrimaryKey(primaryKeys.contains(name));
                 res.add(tableColumn);
             }
             return res;
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public void executeRequest(IRequest request) throws SQLException {
         try {
             Statement statement = connection.createStatement();
             System.out.println("Request :" + request.buildQuery());
             statement.execute(request.buildQuery());
             connection.commit();
         } catch (SQLException e) {
             try {
                 connection.rollback();
             } catch (SQLException e1) {
                 e1.printStackTrace();
             }
             e.printStackTrace();
             throw e;
         }
     }
 
     public Set<TableRow> executeRequest(SelectRequest selectRequest) throws SQLException {
         Set<TableRow> res = new LinkedHashSet<TableRow>();
         TableRow tableRow;
         if (selectRequest.getColumnList().size() == 0) {
             for (TableColumn columns : getTableColumns(selectRequest.getTable())) {
                 selectRequest.addColumn(columns);
             }
         }
         Statement statement = connection.createStatement();
         System.out.println("Request :" + selectRequest.buildQuery());
         ResultSet result = statement.executeQuery(selectRequest.buildQuery());
         while (result.next()) {
             tableRow = new TableRow();
             for (TableColumn tableColumn : selectRequest.getColumnList()) {
                 Object obj = result.getObject(tableColumn.getName());
                tableRow.put(tableColumn, (obj != null) ? obj.toString() : null);
             }
             res.add(tableRow);
         }
         return res;
     }
 
     public List<TableRow> getTablesRows(Table tables) {
         List<TableRow> res = new LinkedList<TableRow>();
         List<TableColumn> tablesColumns = getTableColumns(tables);
         TableRow tableRow;
         try {
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(String.format(
                     "SELECT * FROM %s", tables.getName()));
             while (result.next()) {
                 tableRow = new TableRow();
                 for (TableColumn columnsEntry : tablesColumns) {
                     Object obj = result.getObject(columnsEntry.getName());
                     tableRow.put(columnsEntry, obj.toString());
                 }
                 res.add(tableRow);
             }
             return res;
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return null;
     }
 }
