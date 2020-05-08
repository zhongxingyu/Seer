 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
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
 package com.flexive.core.search.mysql;
 
 import com.flexive.core.DatabaseConst;
 import com.flexive.core.search.*;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxArrayUtils;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.structure.FxDataType;
 import com.flexive.shared.search.SortDirection;
 import com.flexive.shared.exceptions.FxSqlSearchException;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.sqlParser.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.ArrayUtils;
 
 import java.sql.Connection;
 import java.util.*;
 
 /**
  * MySQL specific data selector
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class MySQLDataSelector extends DataSelector {
     private static final transient Log LOG = LogFactory.getLog(MySQLDataSelector.class);
 
     /**
      * All field selectors supported by this implementation
      */
     private static final Map<String, FieldSelector> SELECTORS = new HashMap<String, FieldSelector>();
 
     static {
         SELECTORS.put("MANDATOR", new MySQLGenericSelector(DatabaseConst.TBL_MANDATORS, "id"));
         SELECTORS.put("CREATED_BY", new MySQLGenericSelector(DatabaseConst.TBL_ACCOUNTS, "id"));
         SELECTORS.put("MODIFIED_BY", new MySQLGenericSelector(DatabaseConst.TBL_ACCOUNTS, "id"));
         SELECTORS.put("ACL", new MySQLACLSelector());
         SELECTORS.put("STEP", new MySQLStepSelector());
     }
 
     private static final String[] CONTENT_DIRECT_SELECT = {"ID", "VERSION"};
     private static final String[] CONTENT_DIRECT_SELECT_PROP = {"ID", "VER"};
     private static final String FILTER_ALIAS = "filter";
     private static final String SUBSEL_ALIAS = "sub";
 
     private SqlSearch search;
 
 
     /**
      * Constructor.
      *
      * @param search the search object
      */
     public MySQLDataSelector(SqlSearch search) {
         this.search = search;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Map<String, FieldSelector> getSelectors() {
         return SELECTORS;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String build(final Connection con) throws FxSqlSearchException {
         StringBuffer select = new StringBuffer();
         buildColumnSelectList(select);
         if (LOG.isDebugEnabled()) {
             LOG.debug(search.getFxStatement().printDebug());
         }
         return select.toString();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void cleanup(Connection con) throws FxSqlSearchException {
         // nothing to do
     }
 
     /**
      * Builds the column select.
      * <p/>
      * Example result: "x.oid,x.ver,(select ..xx.. from ...),..."
      *
      * @param select the stringbuffer to write to
      * @throws FxSqlSearchException if the function fails
      */
     private void buildColumnSelectList(final StringBuffer select) throws FxSqlSearchException {
         // Build all value selectors
         int pos = 0;
         final SubSelectValues values[] = new SubSelectValues[search.getFxStatement().getSelectedValues().size()];
         for (SelectedValue selectedValue : search.getFxStatement().getSelectedValues()) {
             // Prepare all values
             final Value value = selectedValue.getValue();
             PropertyEntry entry = null;
             if (value instanceof Property) {
                 final PropertyResolver pr = search.getPropertyResolver();
                 entry = pr.get(search.getFxStatement(), (Property) value);
                 pr.addResultSetColumn(entry);
             }
             values[pos] = selectFromTbl(entry, value, pos);
             pos++;
         }
         // need to check if any order bys from a wildcard selector have not been found
         for (OrderByValue orderBy : search.getFxStatement().getOrderByValues()) {
             if (orderBy.getColumnIndex() < 0 && Math.abs(orderBy.getColumnIndex()) >= pos) {
                 // column has not been selected, thus it is not present in the "ORDER BY" clause
                 throw new FxSqlSearchException("ex.sqlSearch.invalidOrderByIndex", orderBy.getColumnIndex(), pos);
             }
         }
 
         // Build the final select statement
 
         select.append("SELECT \n")
               .append(filterProperties(INTERNAL_RESULTCOLS))
               .append(' ');
         for (SubSelectValues ssv : values) {
             for (SubSelectValues.Item item : ssv.getItems()) {
                 if (ssv.isSorted() && item.isOrderBy()) {
                     select.append(",").append(FILTER_ALIAS).append(".").append(item.getAlias()).append("\n");
                 } else {
                     select.append(",").append(item.getSelect()).append(" ").append(item.getAlias()).append("\n");
                 }
             }
         }
         select.append("FROM (");
         select.append(buildSourceTable(values));
         select.append(") ").append(FILTER_ALIAS).append(" ORDER BY 1");
     }
 
     /**
      * Builds the source table.
      *
      * @param values the selected values
      * @return a select statement which builds the source table
      */
     private String buildSourceTable(final SubSelectValues[] values) {
         // Prepare type filter
         final String typeFilter = search.getTypeFilter() == null ? ""
                 : " and " + FILTER_ALIAS + ".tdef=" + search.getTypeFilter().getId() + " ";
 
         final List<String> orderByNumbers = new ArrayList<String>();
         final List<String> columns = new ArrayList<String>();
 
         // create select columns
         for (String column: INTERNAL_RESULTCOLS) {
             if ("rownr".equals(column)) {
                 columns.add("@rownr:=@rownr+1 rownr");
             } else {
                 columns.add(filterProperties(column));
             }
         }
         columns.add("concat(concat(concat(concat(concat(t.name,'[@pk=')," + filterProperties("id") + "),'.'),"
                 + filterProperties("ver") + "),']') xpathPref");
         // order by starts after the internal selected columns
         int orderByPos = columns.size() + 1;
 
         // create SQL statement
         final StringBuilder sql = new StringBuilder();
         sql.append("select ").append(StringUtils.join(columns, ',')).append(' ');
 
         // add order by indices
         for (SubSelectValues ssv : values) {
             if (ssv.isSorted()) {
                 for (SubSelectValues.Item item : ssv.getItems()) {
                     if (item.isOrderBy()) {
                         // select item from order by
                         sql.append(",").append(item.getSelect()).append(" ").append(item.getAlias()).append("\n");
                         // add index
                         orderByNumbers.add(orderByPos + " " + (ssv.isSortedAscending() ? "asc" : "desc"));
                     } else {
                         sql.append(",null\n");
                     }
                     orderByPos++;
                 }
             }
         }
         sql.append(("FROM " + search.getCacheTable() + " filter, " + DatabaseConst.TBL_STRUCT_TYPES + " t " +
                 "WHERE search_id=" + search.getSearchId() + " AND " + FILTER_ALIAS + ".tdef=t.id " +
                 typeFilter + " "));
 
         // No order by specified = order by id and version
         if (orderByNumbers.size() == 0) {
             orderByNumbers.add("2 asc");
             orderByNumbers.add("3 asc");
         }
         sql.append("ORDER BY ").append(StringUtils.join(orderByNumbers, ','));
 
         // Evaluate the order by, then limit the result by the desired range if needed
         if (search.getStartIndex() > 0 && search.getFetchRows() < Integer.MAX_VALUE) {
             return "SELECT * FROM (" + sql + ") tmp LIMIT " + search.getStartIndex() + "," + search.getFetchRows();
         }
         return sql.toString();
 
     }
 
     /**
      * Returns a comma-separated list of the given property names after adding FILTER_ALIAS to every property.
      *
      * @param names the property name(s)
      * @return  a comma-separated list of the given property names after adding FILTER_ALIAS to every property.
      */
     private String filterProperties(String... names) {
         return FILTER_ALIAS + "." + StringUtils.join(names, "," + FILTER_ALIAS + ".");
     }
 
     /**
      * Build the subselect query needed to get any data from the CONTENT table.
      *
      * @param entry     the entry to select
      * @param prop      the property
      * @param resultPos the position of the property in the select statement
      * @return the SubSelectValues
      * @throws FxSqlSearchException if anything goes wrong
      */
     private SubSelectValues selectFromTbl(PropertyEntry entry, Value prop, int resultPos) throws FxSqlSearchException {
         final SubSelectValues result = new SubSelectValues(resultPos, getSortDirection(resultPos));
         if (prop instanceof Constant || entry == null) {
             result.addItem(prop.getValue().toString(), resultPos, false);
         } else if (entry.getType() == PropertyEntry.Type.NODE_POSITION) {
             long root = FxContext.get().getNodeId();
             if (root == -1) root = FxTreeNode.ROOT_NODE;
             final String sel = "(select tree_nodeIndex(" + root + "," + FILTER_ALIAS + ".id,false))";   // TODO: LIVE/EDIT
             result.addItem(sel, resultPos, false);
         } else if (entry.getType() == PropertyEntry.Type.PATH) {
             final long propertyId = CacheAdmin.getEnvironment().getProperty("CAPTION").getId();
             final String sel = "(select tree_FTEXT1024_Paths(" + FILTER_ALIAS + ".id," +
                     search.getLanguage().getId() + "," + propertyId + ",false))"; // TODO: LIVE/EDIT
             result.addItem(sel, resultPos, false);
         } else {
             switch (entry.getTableType()) {
                 case T_CONTENT:
                     for (String column : entry.getReadColumns()) {
                         final int directSelect = FxArrayUtils.indexOf(CONTENT_DIRECT_SELECT, column, true);
                         if (directSelect > -1) {
                             final String val = FILTER_ALIAS + "." + CONTENT_DIRECT_SELECT_PROP[directSelect];
                             result.addItem(val, resultPos, false);
                         } else {
                             String expr = SUBSEL_ALIAS + "." + column;
                             if (!prop.getSqlFunctions().isEmpty() && PropertyEntry.isDateMillisColumn(column)) {
                                 // need to convert to date before applying a date function
                                 expr = "FROM_UNIXTIME(" + expr + "/1000)";
                             } 
                             final String val = "(SELECT " + expr + " FROM " + DatabaseConst.TBL_CONTENT +
                                     " " + SUBSEL_ALIAS + " WHERE " + SUBSEL_ALIAS + ".id=" + FILTER_ALIAS + ".id AND " +
                                     SUBSEL_ALIAS + ".ver=" + FILTER_ALIAS + ".ver)";
                             result.addItem(val, resultPos, false);
                         }
                     }
                     break;
                 case T_CONTENT_DATA:
                     for (String column : entry.getReadColumns()) {
                         final String val = getContentDataSubselect(column, entry, false);
                         result.addItem(val, resultPos, false);
                     }
                     String xpath = "concat(filter.xpathPref," + getContentDataSubselect("XPATHMULT", entry, true) + ")";
                     result.addItem(xpath, resultPos, true);
                     break;
                 default:
                     throw new FxSqlSearchException(LOG, "ex.sqlSearch.table.typeNotSupported", entry.getTableName());
             }
         }
 
         return result.prepare(this, prop, entry);
     }
 
     /**
      * Returns the {@link SortDirection} for the given column index.
      *
      * @param pos the position to check
      * @return the {@link SortDirection} for the given column index.
      */
     private SortDirection getSortDirection(int pos) {
         final List<OrderByValue> obvs = search.getFxStatement().getOrderByValues();
         for (OrderByValue obv : obvs) {
             if (Math.abs(obv.getColumnIndex()) == pos) {    // also check for negative (=wildcard) order bys
                 return obv.isAscending() ? SortDirection.ASCENDING : SortDirection.DESCENDING;
             }
         }
         return SortDirection.UNSORTED;
     }
 
     /**
      * Returns the subselect for FX_CONTENT_DATA.
      *
      * @param column the column that is being procesed
      * @param entry  the entry
      * @param xpath  if true, the XPath (and not the actual value column(s)) will be selected
      * @return the subselect for FX_CONTENT_DATA.
      */
     private String getContentDataSubselect(String column, PropertyEntry entry, boolean xpath) {
         String select = "(SELECT " + SUBSEL_ALIAS + "." + column +
                 " FROM " + DatabaseConst.TBL_CONTENT_DATA + " " +
                 SUBSEL_ALIAS + " WHERE " +
                 SUBSEL_ALIAS + ".id=" +
                 FILTER_ALIAS + ".id AND " +
                 SUBSEL_ALIAS + ".ver=" +
                 FILTER_ALIAS + ".ver AND " +
                 (entry.isAssignment()
                         ? "ASSIGN=" + entry.getAssignment().getId()
                         : "TPROP=" + entry.getProperty().getId()) + " AND " +
                 "(" + SUBSEL_ALIAS + ".lang=" + search.getLanguage().getId() +
                 " OR " + SUBSEL_ALIAS + ".ismldef=true)" +
                 // fetch exact language match before default
                 " ORDER BY " + SUBSEL_ALIAS + ".ismldef " + 
                 " LIMIT 1 " + ")";
         if (!xpath && entry.getProperty().getDataType() == FxDataType.Binary) {
             // select string-coded form of the BLOB properties
             // TODO link version/quality filtering to the main object version
             select = "(SELECT CONCAT_WS('" + BINARY_DELIM + "'," +
                     StringUtils.join(BINARY_COLUMNS, ',') + ") " +
                     "FROM " + DatabaseConst.TBL_CONTENT_BINARY + " " +
                     "WHERE id=" + select + " " +
                     " AND ver=1 AND quality=1)";
         }
         return select;
     }
 
 
 }
