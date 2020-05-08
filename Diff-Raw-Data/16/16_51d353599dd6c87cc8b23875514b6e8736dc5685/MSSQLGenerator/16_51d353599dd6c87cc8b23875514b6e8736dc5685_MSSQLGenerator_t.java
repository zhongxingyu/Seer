 package com.vaadin.addon.sqlcontainer.query.generator;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.vaadin.addon.sqlcontainer.ColumnProperty;
 import com.vaadin.addon.sqlcontainer.RowItem;
 import com.vaadin.addon.sqlcontainer.TemporaryRowId;
 import com.vaadin.addon.sqlcontainer.query.Filter;
 import com.vaadin.addon.sqlcontainer.query.OrderBy;
 
 public class MSSQLGenerator extends DefaultSQLGenerator {
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.addon.sqlcontainer.query.generator.DefaultSQLGenerator#
      * generateSelectQuery(java.lang.String, java.util.List, java.util.List,
      * int, int, java.lang.String)
      */
     @Override
     public String generateSelectQuery(String tableName, List<Filter> filters,
             List<OrderBy> orderBys, int offset, int pagelength, String toSelect) {
         if (tableName == null || tableName.trim().equals("")) {
             throw new IllegalArgumentException("Table name must be given.");
         }
         if (pagelength > 1) {
             offset++;
             pagelength--;
         }
         StringBuffer query = new StringBuffer();
 
         /* Row count request is handled here */
         if ("COUNT(*)".equalsIgnoreCase(toSelect)) {
             query
                     .append("SELECT COUNT(*) AS \"rowcount\" FROM (SELECT * FROM ");
             query.append(tableName);
             if (filters != null && !filters.isEmpty()) {
                 for (Filter f : filters) {
                     generateFilter(query, f, filters.indexOf(f) == 0);
                 }
             }
             query.append(") AS t");
             return query.toString();
         }
 
         /* SELECT without row number constraints */
         if (offset == 0 && pagelength == 0) {
             query.append("SELECT ");
             if (toSelect != null) {
                 query.append(toSelect);
             } else {
                 query.append("*");
             }
             query.append(" FROM ");
             query.append(tableName);
             if (filters != null) {
                 for (Filter f : filters) {
                     generateFilter(query, f, filters.indexOf(f) == 0);
                 }
             }
             if (orderBys != null) {
                 for (OrderBy o : orderBys) {
                     generateOrderBy(query, o, orderBys.indexOf(o) == 0);
                 }
             }
             return query.toString();
         }
 
         /* Remaining SELECT cases are handled here */
         query.append("SELECT * FROM (SELECT row_number() OVER (");
         if (orderBys != null) {
             for (OrderBy o : orderBys) {
                 generateOrderBy(query, o, orderBys.indexOf(o) == 0);
             }
         }
         if (toSelect == null) {
             query.append(") AS rownum, * FROM ");
         } else {
             query.append(") AS rownum, " + toSelect + " FROM ");
         }
 
         query.append(tableName);
 
         if (filters != null) {
             for (Filter f : filters) {
                 generateFilter(query, f, filters.indexOf(f) == 0);
             }
         }
 
         query.append(") AS a WHERE a.rownum BETWEEN ");
         query.append(offset);
         query.append(" AND ");
         query.append(Integer.toString(offset + pagelength));
         return query.toString();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.addon.sqlcontainer.query.generator.DefaultSQLGenerator#
      * generateUpdateQuery(java.lang.String,
      * com.vaadin.addon.sqlcontainer.RowItem)
      */
     @Override
     public String generateUpdateQuery(String tableName, RowItem item) {
         if (tableName == null || tableName.trim().equals("")) {
             throw new IllegalArgumentException("Table name must be given.");
         }
         if (item == null) {
             throw new IllegalArgumentException("Updated item must be given.");
         }
 
         StringBuffer query = new StringBuffer();
         query.append("UPDATE ");
         query.append(tableName);
         query.append(" SET");
 
         /* Generate column<->value map */
         Map<String, String> columnToValueMap = new HashMap<String, String>();
         Map<String, String> rowIdentifiers = new HashMap<String, String>();
         for (Object id : item.getItemPropertyIds()) {
             ColumnProperty cp = (ColumnProperty) item.getItemProperty(id);
             if (cp.getPropertyId().equalsIgnoreCase("rownum")) {
                 continue;
             }
             String value = cp.getValue() == null ? null : cp.getValue()
                     .toString();
             /*
              * Only include properties whose read-only status can be altered,
              * and which are not set as version columns. The rest of the columns
              * are used as identifiers.
              */
             if (cp.isReadOnlyChangeAllowed() && !cp.isVersionColumn()) {
                 columnToValueMap.put(cp.getPropertyId(), value);
             } else {
                 rowIdentifiers.put(cp.getPropertyId(), value);
             }
         }
 
         /* Generate columns and values to update */
         boolean first = true;
         for (String column : columnToValueMap.keySet()) {
             if (first) {
                 query.append(" ");
             } else {
                 query.append(", ");
             }
             query.append("\"" + column + "\"");
             query.append(" = '");
             query.append(escapeQuotes(columnToValueMap.get(column)));
             query.append("'");
             first = false;
         }
 
         /* Generate identifiers for the row to be updated */
         first = true;
         for (String column : rowIdentifiers.keySet()) {
             if (first) {
                 query.append(" WHERE ");
             } else {
                 query.append(" AND ");
             }
             query.append("\"" + column + "\"");
             query.append(" = ");
             query.append(escapeQuotes(rowIdentifiers.get(column)));
             first = false;
         }
 
         return query.toString();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator#
      * generateInsertQuery(java.lang.String,
      * com.vaadin.addon.sqlcontainer.RowItem)
      */
     @Override
     public String generateInsertQuery(String tableName, RowItem item) {
         if (tableName == null || tableName.trim().equals("")) {
             throw new IllegalArgumentException("Table name must be given.");
         }
         if (item == null) {
             throw new IllegalArgumentException("New item must be given.");
         }
         if (!(item.getId() instanceof TemporaryRowId)) {
             throw new IllegalArgumentException(
                     "Cannot generate an insert query for item already in database.");
         }
         StringBuffer query = new StringBuffer();
         query.append("INSERT INTO ");
         query.append(tableName);
         query.append(" (");
 
         /* Generate column<->value map */
         Map<String, String> columnToValueMap = new HashMap<String, String>();
         for (Object id : item.getItemPropertyIds()) {
             ColumnProperty cp = (ColumnProperty) item.getItemProperty(id);
             if (cp.getPropertyId().equalsIgnoreCase("rownum")) {
                 continue;
             }
             String value = cp.getValue() == null ? null : cp.getValue()
                     .toString();
             /* Only include properties whose read-only status can be altered */
            if (cp.isReadOnlyChangeAllowed() && !cp.isVersionColumn()) {
                 columnToValueMap.put(cp.getPropertyId(), value);
             }
         }
 
         /* Generate column names for insert query */
         boolean first = true;
         for (String column : columnToValueMap.keySet()) {
             if (!first) {
                 query.append(", ");
             }
             query.append("\"" + column + "\"");
             first = false;
         }
 
         /* Generate values for insert query */
         query.append(") VALUES (");
         first = true;
         for (String column : columnToValueMap.keySet()) {
             if (!first) {
                 query.append(", ");
             }
             query.append("'");
             query.append(escapeQuotes(columnToValueMap.get(column)));
             query.append("'");
             first = false;
         }
         query.append(")");
 
         return query.toString();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator#
      * generateDeleteQuery(java.lang.String,
      * com.vaadin.addon.sqlcontainer.RowItem)
      */
     @Override
     public String generateDeleteQuery(String tableName, RowItem item) {
         if (tableName == null || tableName.trim().equals("")) {
             throw new IllegalArgumentException("Table name must be given.");
         }
         if (item == null) {
             throw new IllegalArgumentException(
                     "Item to be deleted must be given.");
         }
         StringBuffer query = new StringBuffer();
         query.append("DELETE FROM ");
         query.append(tableName);
         query.append(" WHERE");
         Collection<?> propIds = item.getItemPropertyIds();
         int count = 1;
         for (Object p : propIds) {
             if (p.toString().equalsIgnoreCase("rownum")) {
                 count++;
                 continue;
             }
             if (item.getItemProperty(p).getValue() != null) {
                 query.append(" ");
                 query.append("\"" + p.toString() + "\"");
                 query.append(" = '");
                 query.append(escapeQuotes(item.getItemProperty(p).getValue()
                         .toString()));
                 query.append("'");
             }
             if (count < propIds.size()) {
                 query.append(" AND");
             }
             count++;
         }
         /* Make sure that the where clause does not end with an AND */
         if (" AND".equals(query.substring(query.length() - 4))) {
             return query.substring(0, query.length() - 4);
         }
         return query.toString();
     }
 }
