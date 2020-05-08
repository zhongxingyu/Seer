 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.search.query;
 
 import com.flexive.shared.*;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxInvalidStateException;
 import com.flexive.shared.interfaces.SearchEngine;
 import com.flexive.shared.search.*;
 import com.flexive.shared.search.query.QueryOperatorNode.Operator;
 import com.flexive.shared.structure.FxAssignment;
 import com.flexive.shared.structure.FxProperty;
 import com.flexive.shared.value.FxValue;
 import com.google.common.collect.Lists;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 
 import java.io.Serializable;
 import java.util.*;
 
 import static com.flexive.shared.FxSharedUtils.checkParameterNull;
 
 /**
  * Query Builder for flexive SQL queries. Supports incremental adding
  * of conditions, filter and order by columns.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class SqlQueryBuilder implements Serializable {
     static final long serialVersionUID = -4805627533111750389L;
 
     private static final String[] BASIC_SELECTS = new String[]{"@pk", "mandator",
             "created_by", "acl", "step", SearchEngine.PROP_USERWILDCARD};
     /**
      * Start column for user defined properties
      */
     public static final int COL_USERPROPS = ArrayUtils.indexOf(BASIC_SELECTS, SearchEngine.PROP_USERWILDCARD);
 
     private final ResultLocation location;
     private ResultViewType viewType;
 
     private final List<String> selectColumns;
     private final List<String> filters;
     private final StringBuilder whereConditions;
     private final List<String> orderBy;
     private final Set<Table> tables;
     private final Stack<Operator> operatorStack;
     private final Stack<Integer> expressionCounter;
 
     private int startRow;
     private int fetchRows = -1;
     private int maxRows = -1;
     private FxSQLSearchParams params;
 
     private boolean includeBasicSelects;
     private boolean frozen;     // when the query is frozen, no more conditions may be added. Getting the query or the search conditions freezes the query builder.
 
     /**
      * Instantiates an empty query builder.
      */
     public SqlQueryBuilder() {
         this(AdminResultLocations.DEFAULT, ResultViewType.LIST);
     }
 
     /**
      * Instantiates an empty query builder for the specified result location and view type.
      *
      * @param location  the result location
      * @param viewType  the view type
      */
     public SqlQueryBuilder(ResultLocation location, ResultViewType viewType) {
         this(new ArrayList<String>(), new ArrayList<String>(), new StringBuilder(), new ArrayList<String>(),
                 new HashSet<Table>(), new Stack<Operator>(), new Stack<Integer>(), true, false, location, viewType);
     }
 
     /**
      * Copy constructor. Creates an independent query builder based on the query builder <code>other</code>
      *
      * @param other the query builder to be copied
      */
     public SqlQueryBuilder(SqlQueryBuilder other) {
         //noinspection unchecked
         this(new ArrayList<String>(other.selectColumns), new ArrayList<String>(other.filters),
                 new StringBuilder(other.whereConditions), new ArrayList<String>(other.orderBy),
                 new HashSet<Table>(other.tables), (Stack<Operator>) other.operatorStack.clone(),
                 (Stack<Integer>) other.expressionCounter.clone(),
                 other.includeBasicSelects, other.frozen, other.location, other.viewType);
         this.startRow = other.startRow;
         this.fetchRows = other.fetchRows;
         this.maxRows = other.maxRows;
         this.params = other.params == null ? null : other.params.copy();
     }
     
     private SqlQueryBuilder(List<String> selectColumns, List<String> filters, StringBuilder whereConditions,
                             List<String> orderBy, Set<Table> tables,
                             Stack<Operator> operatorStack, Stack<Integer> expressionCounter,
                             boolean includeBasicSelects, boolean frozen,
                             ResultLocation location, ResultViewType viewType) {
         this.selectColumns = selectColumns;
         this.filters = filters;
         this.orderBy = orderBy;
         this.whereConditions = whereConditions;
         this.tables = tables;
         this.operatorStack = operatorStack;
         this.expressionCounter = expressionCounter;
         this.includeBasicSelects = includeBasicSelects;
         this.frozen = frozen;
         this.location = location;
         this.viewType = viewType;
         if (getFilterIndex("VERSION") == -1) {
             // return only maximum object version by default
             uniqueFilter("VERSION", VersionFilter.MAX.name());
         }
     }
 
     /**
      * Returns a new instance of this query builder.
      *
      * @return  a new instance of this query builder.
      * @since 3.1
      */
     public SqlQueryBuilder copy() {
         return new SqlQueryBuilder(this);
     }
 
     /**
      * Returns the complete query generated by the builder.
      *
      * @return the SQL search query
      */
     public String getQuery() {
         final String conditions = getConditions();
         if (tables.size() == 0 && StringUtils.isNotBlank(conditions) && !"()".equals(conditions)) {
             throw new FxInvalidStateException("ex.sqlQueryBuilder.tables.empty").asRuntimeException();
         } else if (tables.size() == 0 && StringUtils.isBlank(conditions)) {
             // default query: select all content
             tables.add(Table.CONTENT);
         }
         StringBuilder query = new StringBuilder();
         // build select
         query.append("SELECT ").append(buildSelect(getColumnNames())).append(' ');
         // build from
         if (tables.size() > 1) {
             query.append("\nFROM ");
             for (Table table : tables) {
                 query.append(table.getName()).append(' ').append(table.getAlias());
                 query.append(',');
             }
             query.setCharAt(query.length() - 1, ' ');    // replace last comma
         }
         // build filters
         if (!filters.isEmpty()) {
             query.append("\nFILTER ").append(getFilters());
         }
         if (conditions.length() > 0) {
             // build conditions
             query.append("\nWHERE ").append(conditions);
         }
         if (!orderBy.isEmpty()) {
             query.append("\nORDER BY ").append(getOrderBy());
         }
         return query.toString();
     }
 
     private String buildSelect(List<String> expressions) {
         final StringBuilder out = new StringBuilder();
         for (String column: expressions) {
             if (out.length() > 0) {
                 out.append(", ");
             }
             out.append(injectQuotes(column));
         }
         return out.toString();
     }
 
     /**
      * Adds quotes around the selected property to the given expression. Supports functions, i.e.
      * "year(article/caption)" becomes "year(#article/caption)".
      *
      * @param column    the column expression, e.g. "created_at" or "year(created_at)"
      * @return  the input column name, including the table alias
      */
     private String injectQuotes(String column) {
         checkParameterNull(column, "column");
         if (column.indexOf('/') == -1) {
             return column;
         }
         final StringBuilder out = new StringBuilder();
         if (column.indexOf('(') > 0) {
             // column has one or more functions
             final int propertyStart = column.lastIndexOf('(') + 1;
             out.append(column.substring(0, propertyStart));
             if (column.charAt(propertyStart) != '#') {
                out.append('#');
             }
             out.append(column.substring(propertyStart));
         } else if (column.startsWith("#")) {
             return column;
         } else {
             out.append('#').append(column);
         }
         return out.toString();
     }
 
 
     /**
      * Return the filters defined for this query (if any).
      *
      * @return  the filters defined for this query   
      */
      public String getFilters() {
          return StringUtils.join(filters.iterator(), ", ");
      }
 
     /**
      * Return the order by columns defined for this query.
      *
      * @return  the order by columns defined for this query.
      */
     public String getOrderBy() {
         return orderBy.isEmpty() ? "" : StringUtils.join(orderBy.iterator(), ", ");
     }
 
     /**
      * Render a condition for a common FxAssignment query.
      *
      * @param assignment the assignment
      * @param comparator the comparator to be used
      * @param value      the value to be compared against
      * @return this
      */
     public SqlQueryBuilder condition(FxAssignment assignment, PropertyValueComparator comparator,
                                         FxValue<?, ?> value) {
         checkParameterNull(comparator, "comparator");
         renderCondition(comparator.getSql(assignment, value));
         tables.add(Table.CONTENT);
         return this;
     }
 
     /**
      * Render a condition for a common FxProperty query.
      *
      * @param property   the property
      * @param comparator the comparator to be used
      * @param value      the value to be compared against
      * @return this
      */
     public SqlQueryBuilder condition(FxProperty property, PropertyValueComparator comparator,
                                         FxValue<?, ?> value) {
         checkParameterNull(comparator, "comparator");
         renderCondition(comparator.getSql(property, value));
         tables.add(Table.CONTENT);
         return this;
     }
 
     /**
      * Render a condition for a FxProperty query.
      *
      * @param propertyName   the property name
      * @param comparator the comparator to be used
      * @param value      the value to be compared against
      * @return this
      */
     public SqlQueryBuilder condition(String propertyName, PropertyValueComparator comparator, FxValue<?, ?> value) {
         checkParameterNull(comparator, "comparator");
         renderCondition(comparator.getSql(injectQuotes(propertyName), value));
         tables.add(Table.CONTENT);
         return this;
     }
 
     /**
      * Render a condition for a FxProperty query.
      *
      * @param propertyName   the property name
      * @param comparator the comparator to be used
      * @param value      the value to be compared against
      * @return this
      */
     public SqlQueryBuilder condition(String propertyName, PropertyValueComparator comparator, Object value) {
         checkParameterNull(comparator, "comparator");
         renderCondition(comparator.getSql(injectQuotes(propertyName), value));
         tables.add(Table.CONTENT);
         return this;
     }
 
     /**
      * Performs a fulltext query over all indexed properties against the given text.
      *
      * @param value the fulltext query
      * @return this
      */
     public SqlQueryBuilder fulltext(String value) {
         checkParameterNull(value, "value");
         return condition("*", PropertyValueComparator.EQ, value);
     }
 
     /**
      * Adds a condition that selects only objects of the given type. Note that this is different
      * to {@link #filterType(String)}, since filtering is applied on the result that also may contain
      * other types, but this is a standard search condition that restrains the search result.
      *
      * @param typeName  the type name to be selected
      * @return  this
      */
     public SqlQueryBuilder type(String typeName) {
         checkParameterNull(typeName, "typeName");
         // get type from environment to fail early if it doesn't exist
         condition("typedef", PropertyValueComparator.EQ, CacheAdmin.getEnvironment().getType(typeName).getName());
         return this;
     }
 
     /**
      * Adds a condition that selects only objects of the given type. Note that this is different
      * to {@link #filterType(String)}, since filtering is applied on the result that also may contain
      * other types, but this is a standard search condition that restrains the search result.
      *
      * @param typeName  the type name to be selected
      * @param includeSubTypes   if subtypes should be included
      * @return  this
      * @since 3.1
      */
     public SqlQueryBuilder type(String typeName, boolean includeSubTypes) {
         if (includeSubTypes) {
             condition(
                     "typedef",
                     includeSubTypes ? PropertyValueComparator.GE : PropertyValueComparator.GT,
                     typeName
             );
             return this;
         } else {
             return type(typeName);
         }
     }
 
     /**
      * Adds a condition that selects only objects of the given type. Note that this is different
      * to {@link #filterType(long)}, since filtering is applied on the result that also may contain
      * other types, but this is a standard search condition that restrains the search result.
      *
      * @param typeId the type id to be selected
      * @return  this
      */
     public SqlQueryBuilder type(long typeId) {
         condition("typedef", PropertyValueComparator.EQ, CacheAdmin.getEnvironment().getType(typeId).getName());
         return this;
     }
 
     /**
      * Adds a condition that selects only objects of the given type. Note that this is different
      * to {@link #filterType(long)}, since filtering is applied on the result that also may contain
      * other types, but this is a standard search condition that restrains the search result.
      *
      * @param typeId the type id to be selected
      * @param includeSubTypes   if subtypes should be included
      * @return  this
      * @since 3.1
      */
     public SqlQueryBuilder type(long typeId, boolean includeSubTypes) {
         return type(
                 CacheAdmin.getEnvironment().getType(typeId).getName(),
                 includeSubTypes
         );
     }
 
     /**
      * Limits the (sub-)query to children of the given node.
      *
      * @param nodeId    the root node for the (sub-)query
      * @return  this
      */
     public SqlQueryBuilder isChild(long nodeId) {
         renderCondition("IS CHILD OF " + nodeId);
         tables.add(Table.CONTENT);
         return this;
     }
 
     /**
      * Limits the (sub-)query to children of the given node.
      *
      * @param path   the tree path of the node
      * @return  this
      */
     public SqlQueryBuilder isChild(String path) {
         checkParameterNull(path, "path");
         renderCondition("IS CHILD OF " + FxFormatUtils.escapeForSql(path));
         tables.add(Table.CONTENT);
         return this;
     }
 
     /**
      * Limits the (sub-)query to the direct children of the given node.
      *
      * @param nodeId    the root node for the (sub-)query
      * @return  this
      */
     public SqlQueryBuilder isDirectChild(long nodeId) {
         renderCondition("IS DIRECT CHILD OF " + nodeId);
         tables.add(Table.CONTENT);
         return this;
     }
 
     /**
      * Limits the (sub-)query to the direct children of the given node.
      *
      * @param path   the tree path of the node
      * @return  this
      */
     public SqlQueryBuilder isDirectChild(String path) {
         checkParameterNull(path, "path");
         renderCondition("IS DIRECT CHILD OF " + FxFormatUtils.escapeForSql(path));
         tables.add(Table.CONTENT);
         return this;
     }
 
     /**
      * Return the conditional ("WHERE") statement(s) contained in this query. If the initial
      * subquery scope is still open, it will be closed. 
      *
      * @return the conditional statement(s)
      */
     public String getConditions() {
         if (operatorStack.size() == 1) {
             // close outmost scope automatically
             closeSub();
         }
         assertValidQuery();
         frozen = true;
         return whereConditions.toString();
     }
 
     /**
      * Return the selected column names.
      *
      * @return the selected column names.
      */
     public List<String> getColumnNames() {
         final List<String> names = new ArrayList<String>();
         if (includeBasicSelects) {
             names.addAll(Arrays.asList(BASIC_SELECTS));
         }
         names.addAll(selectColumns);
         return names;
 
     }
 
     /**
      * Select one or more columns.
      *
      * @param columns   columns to be selected
      * @return  this
      */
     public SqlQueryBuilder select(String... columns) {
         checkParameterNull(columns, "columns");
         selectColumns.clear();
         addSelectColumns(columns);
         if (columns.length > 0) {
             includeBasicSelects = false;
         }
         return this;
     }
 
     protected void addSelectColumns(String... columns) {
         // trim whitespace between keywords
         for (String column : columns) {
             selectColumns.add(StringUtils.join(StringUtils.split(column, ' '), ' '));
         }
     }
 
     /**
      * Select one or more columns.
      *
      * @param columns   columns to be selected
      * @return  this
      * @since 3.1
      */
     public SqlQueryBuilder select(Collection<String> columns) {
         return select(columns.toArray(new String[columns.size()]));
     }
 
     /**
      * Return only objects of the given briefcase.
      *
      * @param briefcaseId   the briefcase ID
      * @return  this
      */
     public SqlQueryBuilder filterBriefcase(long briefcaseId) {
         return uniqueFilter("briefcase", briefcaseId);
     }
 
     /**
      * Uses a content type filter for the given content type name.
      *
      * @param name  the content type name
      * @return  this
      */
     public SqlQueryBuilder filterType(String name) {
         return setTypeFilter(name);
     }
 
     /**
      * Uses a content type filter for the given content type.
      *
      * @param typeId the content type ID, or -1 to disable the content type filter
      * @return  this
      */
     public SqlQueryBuilder filterType(long typeId) {
         return setTypeFilter(typeId != -1 ? typeId : null);
     }
 
     /**
      * Filter the result set using the given version filter mode (live, max, auto or all).
      *
      * @param filter    the version filter to be applied
      * @return  this
      */
     public SqlQueryBuilder filterVersion(VersionFilter filter) {
         checkParameterNull(filter, "filter");
         removeFilter("VERSION");
         return uniqueFilter("VERSION", filter.name());
     }
 
     /**
      * Limit the language(s) in which queries are performed (applied to multilingual properties only).
      *
      * @param languages     the language(s)
      * @return              this
      * @since               3.1.6
      */
     public SqlQueryBuilder searchLanguages(Collection<FxLanguage> languages) {
         final List<String> isoCodes = Lists.newArrayListWithCapacity(languages.size());
         for (FxLanguage language : languages) {
             isoCodes.add(language.getIso2digit());
         }
         return uniqueFilter("SEARCH_LANGUAGES", StringUtils.join(isoCodes, "|"));
     }
 
     private SqlQueryBuilder setTypeFilter(Object value) {
         final String filter = "TYPE";
         removeFilter(filter);
         if (value != null) {
             uniqueFilter(filter, value);
         }
         return this;
     }
 
     private SqlQueryBuilder uniqueFilter(String base, Object value) {
         assertNoFilterStartsWith(base);
         filters.add(base + "=" + value);
         return this;
     }
 
     private void assertNoFilterStartsWith(final String substring) {
         if (getFilterIndex(substring) != -1) {
             throw new FxInvalidParameterException("FILTER", "ex.sqlQueryBuilder.filter.unique",
                     substring, filters.get(getFilterIndex(substring))).asRuntimeException();
         }
     }
 
     private int getFilterIndex(String name) {
         final String nameUpper = name.toUpperCase() + "=";
         for (int i = 0; i < filters.size(); i++) {
             if (filters.get(i).toUpperCase().startsWith(nameUpper)) {
                 return i;
             }
         }
         return -1;
     }
 
     private SqlQueryBuilder removeFilter(String name) {
         for (Iterator<String> iterator = filters.iterator(); iterator.hasNext(); ) {
             if (iterator.next().startsWith(name + "=")) {
                 iterator.remove();
                 return this;
             }
         }
         return this;
     }
 
     /**
      * Order the results by the given column. Any previously set order by columns
      * are removed.
      *
      * @param column    the column to be sorted
      * @param direction the sort direction
      * @return  this
      */
     public SqlQueryBuilder orderBy(String column, SortDirection direction) {
         clearOrderBy();
         return addOrderBy(column, direction);
     }
 
     private SqlQueryBuilder addOrderBy(String column, SortDirection direction) {
         checkParameterNull(column, "column");
         final int columnIndex = FxSharedUtils.getColumnIndex(getColumnNames(), column);
         if (columnIndex == -1) {
             throw new FxInvalidParameterException("column", "ex.sqlQueryBuilder.column.invalid", column).asRuntimeException();
         }
         orderBy.add(columnIndex + direction.getSqlSuffix());
         return this;
     }
 
     /**
      * Order the results by the given column (1-based). Any previously set order by columns
      * are removed.
      *
      * @param columnIndex    the 1-based index of the column to be sorted
      * @param direction the sort direction
      * @return  this
      */
     public SqlQueryBuilder orderBy(int columnIndex, SortDirection direction) {
         clearOrderBy();
         return addOrderBy(columnIndex, direction);
     }
 
     private SqlQueryBuilder addOrderBy(int columnIndex, SortDirection direction) {
         checkParameterNull(direction, "direction");
         final List<String> names = getColumnNames();
         if (columnIndex <= names.size()) {
             orderBy.add(columnIndex + direction.getSqlSuffix());
         } else if (isWildcardSelected() && columnIndex > 0) {
             // allow invalid index since it may become valid after the co.* wildcard is resolved
             // (if it remains invalid, the search engine will throw an exception)
             orderBy.add(columnIndex + " " + direction.getSqlSuffix());
         } else {
             throw new FxInvalidParameterException("column", "ex.sqlQueryBuilder.column.invalidIndex", columnIndex).asRuntimeException();
         }
         return this;
     }
 
     /**
      * Order the results by the given column indices (1-based). Any previously set order by columns
      * are removed.
      *
      * @param columns   the order by columns
      * @return  this
      * @since 3.1
      */
     public SqlQueryBuilder orderByIndices(Pair<Integer, SortDirection>... columns) {
         return orderByIndices(Arrays.asList(columns));
     }
 
     /**
      * Order the results by the given column indices (1-based). Any previously set order by columns
      * are removed.
      *
      * @param columns   the order by columns
      * @return  this
      * @since 3.1.5
      */
     public SqlQueryBuilder orderByIndices(List<Pair<Integer, SortDirection>> columns) {
         clearOrderBy();
         for (Pair<Integer, SortDirection> column : columns) {
             addOrderBy(column.getFirst(), column.getSecond());
         }
         return this;
     }
 
     /**
      * Order the results by the given column names. Any previously set order by columns
      * are removed.
      *
      * @param columns   the order by columns
      * @return  this
      * @since 3.1
      */
     public SqlQueryBuilder orderByColumns(Pair<String, SortDirection>... columns) {
         return orderByColumns(Arrays.asList(columns));
     }
 
     /**
      * Order the results by the given column names. Any previously set order by columns
      * are removed.
      *
      * @param columns   the order by columns
      * @return  this
      * @since 3.1.5
      */
     public SqlQueryBuilder orderByColumns(List<Pair<String, SortDirection>> columns) {
         clearOrderBy();
         for (Pair<String, SortDirection> column : columns) {
             addOrderBy(column.getFirst(), column.getSecond());
         }
         return this;
     }
 
     /**
      * Clears the current order by settings.
      */
     public void clearOrderBy() {
         orderBy.clear();
     }
 
     /**
      * Returns true if the predefined columns are selected by the condition.
      *
      * @return  true if the predefined columns are selected by the condition.
      */
     public boolean isIncludeBasicSelects() {
         return includeBasicSelects;
     }
 
     /**
      * Enables or disables the default select columns. By default they are included
      * only if no select clause was specified by {@link #select(String...)}.
      *
      * @param includeBasicSelects   true if the pre-defined selects should be included
      * @return  this
      */
     public SqlQueryBuilder setIncludeBasicSelects(boolean includeBasicSelects) {
         this.includeBasicSelects = includeBasicSelects;
         return this;
     }
 
     /**
      * Create a new subquery scope with the given operator. Subsequent conditions will
      * be added to that scope. Not that all subscopes (except the first one) must be closed
      * before the query can be generated.
      *
      * @param operator the operator to be used inside the new scope
      * @return this
      */
     public SqlQueryBuilder enterSub(Operator operator) {
         checkParameterNull(operator, "operator");
         assertNotFrozen();
         if (expressionCounter.size() > 0 && expressionCounter.peek() > 0) {
             whereConditions.append(" ").append(operatorStack.peek()).append(" ");
         }
         whereConditions.append("(");
         operatorStack.add(operator);
         expressionCounter.add(0);
         return this;
     }
 
     /**
      * Shorthand for {@link #enterSub(Operator)} with {@link Operator#AND} as the operator.
      * @return this
      */
     public SqlQueryBuilder andSub() {
         return enterSub(Operator.AND);
     }
 
     /**
      * Shorthand for {@link #enterSub(Operator)} with {@link Operator#OR} as the operator.
      * @return this
      */
     public SqlQueryBuilder orSub() {
         return enterSub(Operator.OR);
     }
 
     /**
      * Close a scope.
      *
      * @return this
      */
     public SqlQueryBuilder closeSub() {
         assertStackNotEmpty();
         assertNotFrozen();
         operatorStack.pop();
         expressionCounter.pop();
         if (expressionCounter.size() > 0) {
             expressionCounter.push(expressionCounter.pop() + 1);
         }
         whereConditions.append(')');
         if (operatorStack.isEmpty()) {
             // no more conditions can be rendered after closing the last scope
             frozen = true;
         }
         return this;
     }
 
     /**
      * Return true if this query builder is frozen and cannot be further modified.
      * A query builder freezes when the query or the query conditions are queried
      * with the {@link #getQuery()} and {@link #getConditions()} methods.
      * A frozen query builder does not guarantee, however, that the query itself is valid.
      *
      * @return  true if this query builder is frozen and cannot be further modified. 
      */
     public boolean isFrozen() {
         return frozen;
     }
 
     /**
      * Return the result location used in this query builder.
      *
      * @return  the result location used in this query builder.
      */
     public ResultLocation getLocation() {
         return location;
     }
 
     /**
      * Return the result view type used in this query builder.
      *
      * @return  the result view type used in this query builder.
      */
     public ResultViewType getViewType() {
         return viewType;
     }
 
     public SqlQueryBuilder viewType(ResultViewType viewType) {
         checkParameterNull(viewType, "viewType");
         this.viewType = viewType;
         return this;
     }
 
     public int getStartRow() {
         return startRow;
     }
 
     public SqlQueryBuilder startRow(int startRow) {
         this.startRow = startRow;
         return this;
     }
 
     public int getMaxRows() {
         return maxRows;
     }
 
     /**
      * Set the maximum number of rows that the query should return. This determines the size of the entire
      * result set. You can then specify a window of rows using
      * {@link #startRow(int)} and {@link #fetchRows(int)} to return a subset of the search result.
      * <p>
      * This value defaults to {@link SearchEngine#DEFAULT_MAX_ROWS}.
      * </p>
      * <p>
      * <strong>Note:</strong> Be aware that restricting the result size with
      * this parameter can lead to random results, especially in combination
      * with ORDER BY. This is because first the result set is built using
      * at most {@code maxRows} rows, which are then sorted and
      * returned to the client. In general you might want to increase this
      * value to guarantee precise results for large datasets and
      * unspecific queries.
      * </p>
      *
      * @param maxRows   the maximum number of rows in the query result
      * @return          this
      */
     public SqlQueryBuilder maxRows(int maxRows) {
         uniqueFilter("MAX_RESULTROWS", maxRows);
         this.maxRows = maxRows;
         return this;
     }
 
     public int getFetchRows() {
         return fetchRows;
     }
 
     /**
      * Set the maximum number of rows to be returned to the caller when using {@link #getResult()}.
      * Unless specified, all rows will be returned.
      *
      * @param fetchRows the number of rows to be returned
      * @return  this
      * @since 3.1
      */
     public SqlQueryBuilder fetchRows(int fetchRows) {
         this.fetchRows = fetchRows;
         return this;
     }
 
     /**
     * Remove row limitations on the search result and fetch all rows.
     *
     * @return this
     * @since 3.1.7
     */
    public SqlQueryBuilder allRows() {
        maxRows(Integer.MAX_VALUE);
        fetchRows(Integer.MAX_VALUE);
        return this;
    }

    /**
     * Don't fetch result set metadata such as the total number of rows, found content types, or
     * XPaths of the selected values. This speeds up queries considerably in cases where these informations
     * are not required (usually for internal lookup queries).
     *
     * @return  this
     * @since 3.1.7
     */
    public SqlQueryBuilder noResultInfo() {
        getParams().setHintNoResultInfo(true);
        getParams().setHintIgnoreXPath(true);
        return this;
    }

    /**
      * Saves the result of this query in a briefcase (if the result is obtained via
      * #{@link #getResult()}).
      *
      * @param name          the briefcase name (required)
      * @param description   the briefcase description (optional)
      * @param aclId         the briefcase ACL (if -1, the briefcase will be private for the calling user)
      * @return  this
      */
     public SqlQueryBuilder saveInBriefcase(String name, String description, long aclId) {
         getParams().saveResultInBriefcase(name, description, aclId != -1 ? aclId : null);
         return this;
     }
 
     /**
      * Sets the query timeout in seconds. Will be applied only when the result is fetched with
      * {@link #getResult()}.
      *
      * @param seconds   the query timeout in seconds (default: 120 seconds)
      * @return  this
      * @since 3.0.2
      */
     public SqlQueryBuilder timeout(int seconds) {
         getParams().setQueryTimeout(seconds);
         return this;
     }
 
     /**
      * Saves the result of this query in a briefcase (if the result is obtained via
      * #{@link #getResult()}).
      *
      * @param name          the briefcase name (required)
      * @return  this
      */
     public SqlQueryBuilder saveInBriefcase(String name) {
         return saveInBriefcase(name, null, -1);
     }
 
     /**
      * Convenience method for executing a search query using this query builder.
      *
      * @return  the search result for the current query
      * @throws FxApplicationException   if the search failed
      */
     public FxResultSet getResult() throws FxApplicationException {
         return EJBLookup.getSearchEngine().search(getQuery(), startRow, fetchRows,
                 params, location, viewType);
     }
 
     /**
      * @return  the optional search parameters for submitting the query.
      * @since   3.1.6
      */
     public FxSQLSearchParams getParams() {
         if (params == null) {
             params = new FxSQLSearchParams();
         }
         return params;
     }
 
     /**
      * Render the given condition in the current scope.
      *
      * @param condition the condition to be rendered.
      */
     private void renderCondition(String condition) {
         checkParameterNull(condition, "condition");
         if (operatorStack.isEmpty() && whereConditions.length() == 0) {
             // implicitly open first scope
             andSub();
         }
         assertStackNotEmpty();
         assertNotFrozen();
         if (expressionCounter.peek() == 0) {
             // first condition, open scope
 /*            int level = expressionCounter.size() - 1;
             while (level >= 0 && expressionCounter.get(level) == 0) {
                 // nested condition, add operator
                 if (level > 0 && expressionCounter.get(level - 1) > 0) {
                     whereConditions.append(' ')
                             .append(operatorStack.get(level - 1).getSqlRepresentation())
                             .append(' ');
                 }
                 whereConditions.append('(');
                 level--;
             }*/
         } else {
             // render operator between nodes
             whereConditions.append(' ').append(operatorStack.peek().getSqlRepresentation()).append(' ');
         }
         whereConditions.append(condition);
         expressionCounter.add(expressionCounter.pop() + 1);
     }
 
     private void assertStackNotEmpty() {
         if (operatorStack.isEmpty()) {
             throw new FxInvalidStateException("ex.sqlQueryBuilder.operatorStack.empty").asRuntimeException();
         }
         if (expressionCounter.isEmpty()) {
             throw new FxInvalidStateException("ex.sqlQueryBuilder.counterStack.empty").asRuntimeException();
         }
     }
 
     private void assertValidQuery() {
         if (!operatorStack.isEmpty() || !expressionCounter.isEmpty()) {
             throw new FxInvalidStateException("ex.sqlQueryBuilder.query.incomplete", operatorStack.size()).asRuntimeException();
 		}
 	}
 
     private void assertNotFrozen() {
         if (frozen) {
             throw new FxInvalidStateException("ex.sqlQueryBuilder.query.frozen").asRuntimeException();
         }
     }
 
     private boolean isWildcardSelected() {
         final List<String> names = getColumnNames();
         return names.contains(SearchEngine.PROP_WILDCARD) || names.contains(SearchEngine.PROP_USERWILDCARD);
     }
 }
