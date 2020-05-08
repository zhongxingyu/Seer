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
 
 /**
  * Generates generic SQL that is supported by HSQLDB, MySQL and PostgreSQL.
  * 
  * @author Jonatan Kronqvist / IT Mill Ltd
  */
 public class DefaultSQLGenerator implements SQLGenerator {
 
     /**
      * Escape character used by the underlying database.
      */
     protected String searchStringEscape;
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator#
      * generateSelectQuery(java.lang.String, java.util.List, java.util.List,
      * int, int, java.lang.String)
      */
     public String generateSelectQuery(String tableName, List<Filter> filters,
             List<OrderBy> orderBys, int offset, int pagelength, String toSelect) {
         if (tableName == null || tableName.trim().equals("")) {
             throw new IllegalArgumentException("Table name must be given.");
         }
 
         StringBuffer query = new StringBuffer();
         if (toSelect == null) {
             query.append("SELECT * FROM ");
         } else {
             query.append("SELECT " + toSelect + " FROM ");
         }
         query.append(escapeQuotes(tableName));
 
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
         if (pagelength != 0) {
             generateLimits(query, offset, pagelength);
         }
         return query.toString();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator#
      * generateUpdateQuery(java.lang.String,
      * com.vaadin.addon.sqlcontainer.RowItem)
      */
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
 
     /**
      * Creates filtering as a WHERE -clause
      * 
      * @param sb
      *            StringBuffer to which the clause is appended.
      * @param f
      *            Filter to be added to the sb.
      * @param firstFilter
      *            If true, this is the first Filter to be added.
      * @return
      */
     protected StringBuffer generateFilter(StringBuffer sb, Filter f,
             boolean firstFilter) {
         if (firstFilter) {
             sb.append(" WHERE ");
         } else {
             sb.append(" AND ");
         }
         /* Null filter equates to 1=1 where clause */
         if (f.getValue() == null) {
             sb.append("1=1");
             return sb;
         }
 
         String name = f.getColumn();
         String value = String.valueOf(f.getValue());
         f.setNeedsQuotes(false);
 
         if (f.getValue() instanceof String) {
             // Try to determine if the filter value is numeric
             try {
                 Long.parseLong(String.valueOf(f.getValue()));
                 Double.parseDouble(String.valueOf(f.getValue()));
                 value = String.valueOf(f.getValue());
             } catch (NumberFormatException nfe) {
                 // The filter value is indeed a String and thus needs wild card
                 // removal, quote escaping and quotes around it in the query
                 value = escapeQuotes(escapeWildCards((String) f.getValue()));
                 f.setNeedsQuotes(true);
                 switch (f.getComparisonType()) {
                 case STARTS_WITH:
                     value += "%";
                     break;
                 case CONTAINS:
                     value = "%" + value + "%";
                     break;
                 case ENDS_WITH:
                     value = "%" + value;
                     break;
                 }
             }
         }
 
         if (f.isNeedsQuotes()) {
             if (!f.isCaseSensitive()) {
                 name = "LOWER(\"" + f.getColumn() + "\")";
                 value = "LOWER('" + value + "')";
             } else {
                 value = "'" + value + "'";
             }
         }
         if (f.isCaseSensitive()) {
             name = "\"" + name + "\"";
         }
 
         sb.append(name);
 
         switch (f.getComparisonType()) {
         case STARTS_WITH:
         case CONTAINS:
         case ENDS_WITH:
             /*
              * LIKE for Strings, = for numeric types. Prevents starts/ends_with
              * filtering with numeric types, which would not be very functional
              * anyway.
              */
             if (f.isNeedsQuotes()) {
                 sb.append(" LIKE ");
             } else {
                 sb.append(" = ");
             }
             break;
         case LESS:
             sb.append(" < ");
             break;
         case LESS_OR_EQUAL:
             sb.append(" <= ");
             break;
         case EQUALS:
             sb.append(" = ");
             break;
         case GREATER:
             sb.append(" > ");
             break;
         case GREATER_OR_EQUAL:
             sb.append(" >= ");
             break;
         }
         sb.append(value);
         return sb;
     }
 
     /**
      * Generates sorting rules as an ORDER BY -clause
      * 
      * @param sb
      *            StringBuffer to which the clause is appended.
      * @param o
      *            OrderBy object to be added into the sb.
      * @param firstOrderBy
      *            If true, this is the first OrderBy.
      * @return
      */
     protected StringBuffer generateOrderBy(StringBuffer sb, OrderBy o,
             boolean firstOrderBy) {
         if (firstOrderBy) {
             sb.append(" ORDER BY ");
         } else {
             sb.append(", ");
         }
         sb.append("\"" + o.getColumn() + "\"");
         if (o.isAscending()) {
             sb.append(" ASC");
         } else {
             sb.append(" DESC");
         }
         return sb;
     }
 
     /**
      * Generates the LIMIT and OFFSET clause.
      * 
      * @param sb
      *            StringBuffer to which the clause is appended.
      * @param offset
      *            Value for offset.
      * @param pagelength
      *            Value for pagelength.
      * @return StringBuffer with LIMIT and OFFSET clause added.
      */
     protected StringBuffer generateLimits(StringBuffer sb, int offset,
             int pagelength) {
         sb.append(" LIMIT ");
         sb.append(pagelength);
         sb.append(" OFFSET ");
         sb.append(offset);
         return sb;
     }
 
     /**
      * This function will escape wildcard characters used in SQL search strings,
      * e.g. with the LIKE keyword. If the escape string is not set, the original
      * input will be returned as is.
      * 
      * @param constant
      *            Original string to perform the escaping on.
      * @return Escaped search string.
      */
     protected String escapeWildCards(String constant) {
         String fixedConstant;
         if (searchStringEscape == null) {
             return constant;
         }
         if (constant != null) {
             fixedConstant = constant.replace("%", searchStringEscape + "%");
             fixedConstant = fixedConstant
                     .replace("_", searchStringEscape + "_");
             return fixedConstant;
         }
         return null;
     }
 
     /**
      * Replaces single quotes (') with two single quotes ('').
      * 
      * Note! If escaping a single quote is attempted by the user with e.g. (\'),
      * both the single quote and the escape character(s) preceding it will be
      * removed completely.
      * 
      * Also note! The escaping done here may or may not be enough to prevent any
      * and all SQL injections so it is recommended to check user input before
      * giving it to the SQLContainer/TableQuery.
      * 
      * @param constant
      * @return \\\'\'
      */
     protected String escapeQuotes(String constant) {
         String fixedConstant;
         if (constant != null) {
             fixedConstant = constant;
             while (fixedConstant.contains("\\\'")) {
                 fixedConstant = fixedConstant.replace("\\\'", "");
             }
             fixedConstant = fixedConstant.replace("\'", "\'\'");
             return fixedConstant;
         }
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator#
      * setSearchStringEscape(java.lang.String)
      */
     public void setSearchStringEscape(String searchStringEscape) {
         this.searchStringEscape = searchStringEscape;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator#
      * generateDeleteQuery(java.lang.String,
      * com.vaadin.addon.sqlcontainer.RowItem)
      */
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
