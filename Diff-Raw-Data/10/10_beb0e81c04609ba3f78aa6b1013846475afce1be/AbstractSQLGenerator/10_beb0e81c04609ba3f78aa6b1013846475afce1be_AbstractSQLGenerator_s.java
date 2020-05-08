 /*___INFO__MARK_BEGIN__*/
 /*************************************************************************
  *
  *  The Contents of this file are made available subject to the terms of
  *  the Sun Industry Standards Source License Version 1.2
  *
  *  Sun Microsystems Inc., March, 2001
  *
  *
  *  Sun Industry Standards Source License Version 1.2
  *  =================================================
  *  The contents of this file are subject to the Sun Industry Standards
  *  Source License Version 1.2 (the "License"); You may not use this file
  *  except in compliance with the License. You may obtain a copy of the
  *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
  *
  *  Software provided under this License is provided on an "AS IS" basis,
  *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
  *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
  *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
  *  See the License for the specific provisions governing your rights and
  *  obligations concerning the Software.
  *
  *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
  *
  *   Copyright: 2001 by Sun Microsystems, Inc.
  *
  *   All Rights Reserved.
  *
  ************************************************************************/
 /*___INFO__MARK_END__*/
 package com.sun.grid.arco.sql;
 
 import java.util.*;
 import com.sun.grid.arco.model.*;
 import com.sun.grid.arco.ArcoConstants;
 import com.sun.grid.arco.util.FieldFunction;
 import com.sun.grid.arco.util.LogicalConnection;
 import com.sun.grid.arco.util.FilterType;
 import com.sun.grid.arco.util.SortType;
 import com.sun.grid.logging.SGELog;
 
 /**
  * This class generates the SQL statements for queries
  * This is an adapter between QueryType and SQLExpression 
  * The main generate function will return the String representation of adapted 
  * SQLExpression
  * The SQLExpression class is defined as a helper protected inner class
  */
 public abstract class AbstractSQLGenerator implements SQLGenerator {
 
    /**
     * This returns an alias for subselect. The empty alias is default for Oracle.
     * The other databases need to return DB specific value e.g.: "as tmp"
     * e.g.: SELECT * FROM (SELECT .... ) as tmp ORDER BY ...
     * @return a <CODE>String</CODE> alias code
     */
    protected String getSubSelectAlias(){
       return "";
    }
    
    /**
     * In Oracle we use a type DATE, to show in a query also the time part, the field needs to be formatted
     * This function determines if the field is a type of java.sql.Types.DATE (only for Oracle), and if
     * it is and formats it as to_char(formatField, 'YYYY-MM-DD HH24:MI:SS').
     * @param dbField database field to be checked for formating
     * @param query query object that must have the clusterName set
     * @param formatField - the String that should be formatted. The format field can be a database field 
     *                      already wrapped with an aggregate or arithmetical function.
     */
    public String formatTimeField(String dbField, QueryType query, String formatField) {
       //default implementation for all databases does not format the field
       //OracleSQLGenerator overrides this function
       return formatField;
    }  
 
    /**
     * generate the sql statement for a query. If the query is
     * an advanced query the sql string of the query is return.
     * Otherwise the sql string will be build by the field and
     * filter information of the query.
     * @param query 
     * @throws com.sun.grid.arco.sql.SQLGeneratorException 
     * @return 
     */
    public String generate(QueryType query, Map lateBindings) throws SQLGeneratorException {
 
       String type = query.getType();
       if (ArcoConstants.ADVANCED.equals(type)) {
          return generateAdvanced(query, lateBindings);
       } else if (ArcoConstants.SIMPLE.equals(type)) {
          String sql = generateSimple(query, lateBindings);
          query.setSql(sql);
          return sql;
       } else {
          throw new SQLGeneratorException("sqlgen.invalidQueryType", new Object[]{type});
       }
    }
 
    protected String generateAdvanced(QueryType query, Map lateBindings) {
 
       String sql = query.getSql();
 
       StringBuffer buf = new StringBuffer();
 
       ArrayList sortedFilter = new ArrayList(query.getFilter());
 
 
       Collections.sort(sortedFilter, new Comparator() {
 
       public int compare(Object o1, Object o2) {
             return ((Filter) o1).getStartOffset() -
                    ((Filter) o2).getStartOffset();
          }
       });
 
       Iterator iter = sortedFilter.iterator();
       Filter filter = null; 
       int lastOffset = 0;
       Object lb = null;
       
       while (iter.hasNext()) {
          filter = (Filter) iter.next();
 
          if (filter.isActive() && filter.isLateBinding()) {
             buf.append(sql.substring(lastOffset, filter.getStartOffset()));
             if (lateBindings != null) {
                lb = lateBindings.get(filter.getName());
             } else {
                lb = null;
             }
             if (lb != null) {
                // Add a column
                buf.append(filter.getName());
                buf.append(" ");
                // Add a condition
                if (filter.isSetCondition()) {
                   buf.append(filter.getCondition());
                   buf.append(" ");
                }
                boolean quote = !isQuoted(lb);
                if (quote) {
                   buf.append("'");
                }
                buf.append(lb);
                if (quote) {
                   buf.append("'");
                }
             }
             lastOffset = filter.getEndOffset();
          }
       }
 
       if (lastOffset < sql.length()) {
          buf.append(sql.substring(lastOffset));
       }
 
       return buf.toString();
 
    }
 
    /**
     * Generate the sql statement for a simple query
     * @param query   the simple query
     * @throws com.sun.grid.arco.sql.SQLGeneratorException 
     *         if the query contains insufficient information 
     * @return the sql statemet
     */
    protected String generateSimple(QueryType query, Map lateBindings) throws SQLGeneratorException {
 
       SQLExpression sqle = new SQLExpression();
       boolean hasAggregateFunction = false;
       com.sun.grid.arco.Util.correctFieldNames(query);
 
       Iterator fieldIter = query.getField().iterator();
       Field field = null;
 
       FieldFunction fieldFunction = null;
 
       while (fieldIter.hasNext() && !hasAggregateFunction) {
          field = (Field) fieldIter.next();
          fieldFunction = getFieldFunction(field);
 
          hasAggregateFunction |= fieldFunction.isAggreagate();
       }
 
       fieldIter = query.getField().iterator();
 
       while (fieldIter.hasNext()) {
          field = (Field) fieldIter.next();
          fieldFunction = getFieldFunction(field);
          final String generateFieldName = generateFieldName(field, fieldFunction, query);
 
          sqle.addSelect(generateFieldName,field.getReportName());
 
          if (hasAggregateFunction && !fieldFunction.isAggreagate()) {
             sqle.addGroup(generateFieldName);
          }
       }
 
       // build the from-clause and append to sql-statement
       sqle.addFrom(query.getTableName());
 
       List extendsFilters = new ArrayList();
 
       if (!query.getFilter().isEmpty()) {
          // build the where-clause and append to sql-statement                  
          Iterator filterIter = query.getFilter().iterator();
          Filter filter = null;
          LogicalConnection lc = null;
          FilterType ft = null;
 
          String param = null;
          String filterName = null;
 
          while (filterIter.hasNext()) {
             filter = (Filter) filterIter.next();
 
             if (!filter.isActive()) {
                continue;
             }
 
             // Test if the fiter filters a user defined field
             field = getFieldForFilter(query, filter);
             if (field != null) {
                fieldFunction = getFieldFunction(field);
                // If the field is function then we need a subselect               
                // the result if the subselect will be filtered by this
                // filter
                if (fieldFunction != FieldFunction.VALUE) {
                   extendsFilters.add(filter);
                   continue;
                }
                
                filterName = field.getDbName();
             } else {
                // We are filtering a column which is not defined
                // in the query
                filterName = filter.getName();
             }
             // filter is now properlly checked, no null check is needed      
             sqle.addWhere(getLogicalSymbol(filter), getFilterExpression(filter, filterName, lateBindings));
          } // end of while
       }
 
 
       // order by
       SortType sortType = null;
       fieldIter = query.getField().iterator();
       while (fieldIter.hasNext()) {
          field = (Field) fieldIter.next();
 
          if (field.isSetSort() && field.getSort() != null) {
             sortType = SortType.getSortTypeByName(field.getSort());
             if (sortType == null) {
                throw new SQLGeneratorException("sqlgen.field.invalidSort",
                        new Object[]{field.getDbName(), field.getSort()});
             } else if (sortType != SortType.NOT_SORTED) {
                sqle.addOrder(generateFieldName(field, query), sortType.getName());
             }
          }
       }
       
       // limit
       if (query.isSetLimit() && query.getLimit() > 0) {
          addRowLimit(query, sqle);
       }
 
       if (!extendsFilters.isEmpty()) {
          sqle = new SQLExpression(sqle.toString());
 
          Iterator filterIter = extendsFilters.iterator();
          Filter filter = null;
          while (filterIter.hasNext()) {
             filter = (Filter) filterIter.next();
             sqle.addWhere(getLogicalSymbol(filter), getFilterExpression(filter, filter.getName(), lateBindings));
          }
       }
 
 
       String ret = sqle.toString();
       SGELog.fine("ret = ", ret);
       return ret;
 
 
    }
    
    /**
     * Modify a SQLExpression with a limit from QueryType
     * It is called just when limit is not zero
     * @param query a <CODE>QueryType</CODE> query
     * @param sqle a <CODE>SQLExpression</CODE> sql
     */
    protected void addRowLimit(QueryType query, SQLExpression sqle) {
       sqle.setLimit(query.getLimit());
    }
 
    /**
     * Test if user input is quoted 
     * @param lb the user input object
     * @return a <code>boolean</code> of true, user input should not be extra quoted
     */
    boolean isQuoted(Object lb) {
       final String lbstrm = lb.toString().trim();
       boolean ret = lbstrm.startsWith("(") && lbstrm.endsWith(")");
       ret = ret || (lbstrm.startsWith("'") && lbstrm.endsWith("'"));
       ret = ret || (lbstrm.startsWith("{") && lbstrm.endsWith("}"));
       ret = ret || (lbstrm.startsWith("\"") && lbstrm.endsWith("\""));
       return ret;
    }
    
    private String getFilterExpression(Filter filter, String filterName, Map lateBindings)
       throws SQLGeneratorException {
       FilterType ft = getFilterType(filter);
       String param = null;
       StringBuffer where = new StringBuffer();
 
       if (filter.isLateBinding()) {
          if (lateBindings == null) {
             where.append(" LATEBINDING { ");
             where.append(filterName);
             where.append(" ; ");
             where.append(ft.getSymbol());
             where.append(" ;  }");
             return where.toString();
          }
          param = (String) lateBindings.get(filter.getName());
       } else {
          param = filter.getParameter();
       }
       where.append(filterName);
       where.append(' ');
       where.append(ft.getSymbol());
       if (ft.getParameterCount() > 0) {
          where.append(' ');
          if (ft == FilterType.IN) {
             where.append('(');
             where.append(param);
             where.append(')');
          } else if (ft == FilterType.BETWEEN) {
             where.append(param);
          } else {
            where.append('\'');
            where.append(param);
            where.append('\'');
          }
       }
       return where.toString();
    }
 
    public static boolean hasActiveFilter(QueryType query) {
       Iterator iter = query.getFilter().iterator();
       Filter filter = null;
       while (iter.hasNext()) {
          filter = (Filter) iter.next();
          if (filter.isActive()) {
             return true;
          }
       }
       return false;
    }
 
    private FilterType getFilterType(Filter filter)
            throws SQLGeneratorException {
       String type = filter.getCondition();
       if (type == null || type.length() == 0) {
          throw new SQLGeneratorException("sqlgen.filter.emptyCondition",
                  new Object[]{filter.getName()});
       }
       FilterType ret = FilterType.getFilterTypeByName(type);
       if (ret == null) {
          throw new SQLGeneratorException("sqlgen.filter.unknownCondition",
                  new Object[]{filter.getName(), type});
       }
       return ret;
    }
 
    private Field getFieldForFilter(QueryType query, Filter filter) {
       Iterator iter = query.getField().iterator();
       Field field = null;
       while (iter.hasNext()) {
          field = (Field) iter.next();
          if (field.getReportName().equals(filter.getName())) {
             return field;
          }
       }
       return null;
    }
  
    /**
     * This function returns a logical symbol from Filter
     * @param filter a filter
     * @return a <CODE>String</CODE> the logical symbol (AND, OR, '')
     * @throws com.sun.grid.arco.sql.SQLGeneratorException
     */
    private String getLogicalSymbol(Filter filter)
       throws SQLGeneratorException {
       LogicalConnection ret=LogicalConnection.NONE;
       String name = filter.getLogicalConnection();
       if (name == null || name.length() == 0) {
          return ret.getSymbol();
       }
       ret = LogicalConnection.getLogicalConnectionByName(name);
       if (ret == null) {
          throw new SQLGeneratorException("sqlgen.filter.unknownLC",
             new Object[]{filter.getName(), name});
       }
       return ret.getSymbol();
    }
 
     /**
     * The the FieldFunction object of a field
     * @param field  the field
     * @throws com.sun.grid.arco.sql.SQLGeneratorException if the field has
     *          no or an unknown function
     * @return  the FieldFunction object
     */
    private FieldFunction getFieldFunction(Field field)
            throws SQLGeneratorException {
       if (field.getFunction() == null) {
          throw new SQLGeneratorException("sqlgen.field.emptyFunction",
                  new Object[]{field.getDbName()});
       }
       FieldFunction ret = FieldFunction.getFieldFunctionByName(field.getFunction());
       if (ret == null) {
          throw new SQLGeneratorException("sqlgen.field.unknownFunction",
                  new Object[]{field.getDbName(), field.getFunction()});
       }
       return ret;
    }
 
    private String generateFieldName(Field field, QueryType query) throws SQLGeneratorException {
       return generateFieldName(field, getFieldFunction(field), query);
    }
 
    private String generateFieldName(Field field, FieldFunction function, QueryType query)
            throws SQLGeneratorException {
       String dbName = field.getDbName();
       
       if (dbName == null || dbName.length() == 0) {
          throw new SQLGeneratorException("sqlgen.field.emptyDbName");
       }
 
       if (FieldFunction.VALUE == function) {
          return formatTimeField(dbName, query, dbName);    
       } else {
          if (FieldFunction.ADDITION == function ||
                  FieldFunction.SUBSTRACTION == function ||
                  FieldFunction.DIVISION == function ||
                  FieldFunction.MULIPLY == function) {
             String parameter = field.getParameter();
             if (parameter == null || parameter.length() == 0) {
                throw new SQLGeneratorException("field {0} with function {1} requires a parameter",
                        new Object[]{dbName, function.getName()});
             }
             StringBuffer ret = new StringBuffer();
             ret.append("(");
             ret.append(dbName);
             ret.append(function.getName());
             ret.append(parameter);
             ret.append(")");
             String f = ret.toString();
             return formatTimeField(dbName, query, f);
          } else {
             StringBuffer ret = new StringBuffer();
             ret.append(function.getName());
             ret.append("(");
             ret.append(dbName);
             ret.append(")");
             String f = ret.toString();
             return formatTimeField(dbName, query, f);
          }
       }
    }
    
    /**
     * SQLExpression String representation class
     * This class handle the SQL clauses separated 
     * and Its toString method generate the SQL correct output.
     */
    protected class SQLExpression {
 
       /**
        * Emprty SQL constructor
        */
       public SQLExpression() {
       }
 
       /**
        * Inner SQL constructor
        * @param a <CODE>String</CODE> inner SQL subselect 
        */
       public SQLExpression(String subselect) {
          this.from.append("( "+subselect+" ) "+getSubSelectAlias());         
       }
       
       // build the select statement
       private StringBuffer select = new StringBuffer();
       private StringBuffer from = new StringBuffer();
       private StringBuffer where = new StringBuffer();
       private StringBuffer group = new StringBuffer();
       private StringBuffer order = new StringBuffer();
       private int limit = 0;
 
       public // build the select statement
          StringBuffer getSelect() {
          return select;
       }
 
       /**
        * Add a selected column
        * @param column a column expression
        * @param alias a column alias
        */
       public void addSelect(String column, String alias) {
          if (this.select.length() > 0) {
             this.select.append(", ");
          }
          this.select.append(column);
          if (alias != null || alias.length() > 0) {
             select.append(" AS \"");
             select.append(alias);
             select.append("\"");
          }         
       }
 
       public StringBuffer getFrom() {
          return from;
       }
       
       /**
        * Add from clause
        * @param from a Table name
        */
       public void addFrom(String from) {
          if (this.from.length() > 0) {
             this.from.append(", ");
          }         
          this.from.append(from);
       }
 
       public StringBuffer getWhere() {
          return where;
       }
       
       /**
        * Add the where clause
        * @param symbol a jooin symbol like AND, OR 
        * @param where a where expression
        */
       public void addWhere(String symbol, String where) {
          if (this.where.length() > 0) {
             this.where.append(' ');
             this.where.append(symbol);
             this.where.append(' ');
          }                
          this.where.append(where);
       }
       
       public StringBuffer getGroup() {
          return group;
       }
       
       /**
        * Add a group by clause
        * @param aggreagate expression
        */
       public void addGroup(String ag) {
          if (this.group.length() > 0) {
             this.group.append(", ");
          }
          this.group.append(ag);
       }
 
       public StringBuffer getOrder() {
          return order;
       }
       
       /**
        * Add a sort clause
        * @param order order expression
        * @param sortType ASC,DESC
        */
       public void addOrder(String order, String sortType) {
          if (this.order.length() > 0) {
             this.order.append(", ");
          }
          this.order.append(order);
          
          if(sortType != null && sortType.length()>0) {
             this.order.append(' ');
             this.order.append(sortType);         
          }
       }
 
       public int getLimit() {
          return limit;
       }
 
       public void setLimit(int limit) {
          this.limit = limit;
       }
       
       /**
        * Overided default toString method
        * @return a SQL correct String representation of the SQL
        */
       public String toString() {
 
          // build statement here
          StringBuffer sql = new StringBuffer();
 
          // select is mandatory
          sql.append("SELECT ");
          sql.append(select);
 
          // from is mandatory
          sql.append(" FROM ");
          sql.append(from);
          if (where.length() > 0) {
             SGELog.finer("where is ''{0}''", where);
             sql.append(" WHERE ");
             sql.append(where);
          }
 
 
          if (group.length() > 0) {
             SGELog.finer("group is ''{0}''", group);
             sql.append(" GROUP BY ");
             sql.append(group);
          }
 
          if (order.length() > 0) {
             SGELog.finer("order is ''{0}''", order);
             sql.append(" ORDER BY ");
             sql.append(order);
          }
          
          if (limit > 0) {
             SGELog.finer("limit is ''{0}''", limit);
             sql.append(" LIMIT ");
             sql.append(limit);
          }
          
          return sql.toString();
       }
    }
 }
