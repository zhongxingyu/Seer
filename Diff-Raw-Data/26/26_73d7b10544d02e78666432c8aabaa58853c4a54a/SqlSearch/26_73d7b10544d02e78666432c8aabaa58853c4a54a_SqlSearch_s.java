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
 package com.flexive.core.search;
 
 import com.flexive.core.Database;
 import com.flexive.core.DatabaseConst;
 import com.flexive.core.storage.DBStorage;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.shared.*;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxSqlSearchException;
 import com.flexive.shared.interfaces.BriefcaseEngine;
 import com.flexive.shared.interfaces.ResultPreferencesEngine;
 import com.flexive.shared.interfaces.SequencerEngine;
 import com.flexive.shared.interfaces.TreeEngine;
 import com.flexive.shared.search.*;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.structure.FxProperty;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.value.FxNoAccess;
 import com.flexive.shared.value.FxValue;
 import com.flexive.sqlParser.*;
 import com.google.common.collect.Lists;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The main search engine class
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class SqlSearch {
     private static final Log LOG = LogFactory.getLog(SqlSearch.class);
 
     private final int startIndex;
     private final int fetchRows;
     private final String query;
     private final ResultLocation location;
     private final ResultViewType viewType;
     private final SequencerEngine seq;
     private final BriefcaseEngine briefcase;
     private final TreeEngine treeEngine;
 
     private DBStorage storage;
     private FxStatement statement;
     private FxType typeFilter;
     private PropertyResolver pr;
     private int parserExecutionTime = -1;
     private long searchId = -1;
     private String cacheTbl;
     private final FxSQLSearchParams params;
     private boolean hasUserPropsWildcard = false;
     private int indexOfUserPropsWildcard = -1;
     private FxEnvironment environment;
     private final ResultPreferencesEngine conf;
     private FxLanguage language;
     private List<Long> searchLanguageIds;
     private ResultPreferences resultPreferences;
 
     /**
      * Ctor
      *
      *
      * @param seq            reference to the sequencer
      * @param briefcase      reference to the briefcase engine
      * @param treeEngine     reference to the tree engine
      * @param query          the query to execute
      * @param startIndex     the start index (0 based)
      * @param maxFetchRows   the number of rows to return with the resultset, or -1 to fetch all rows
      * @param params         all additional search parameters
      * @param conf           the result set configuration
      * @param location       the location that started the search
      * @param viewType       the view type @throws com.flexive.shared.exceptions.FxSqlSearchException
      *                       if the search failed
      * @throws com.flexive.shared.exceptions.FxSqlSearchException
      *          if the search engine could not be initialized
      */
     public SqlSearch(SequencerEngine seq, BriefcaseEngine briefcase, TreeEngine treeEngine, String query, int startIndex, int maxFetchRows, FxSQLSearchParams params,
                      ResultPreferencesEngine conf, ResultLocation location, ResultViewType viewType) throws FxSqlSearchException {
         FxSharedUtils.checkParameterEmpty(query, "query");
         // Parameter checks
         if (startIndex < 0) {
             throw new FxSqlSearchException(LOG, "ex.sqlSearch.parameter.invalidStartIndex", startIndex);
         }
         if (maxFetchRows == 0) {
             throw new FxSqlSearchException(LOG, "ex.sqlSearch.parameter.fetchRows", maxFetchRows);
         }
 
         // Init
         this.seq = seq;
         this.briefcase = briefcase;
         this.treeEngine = treeEngine;
         this.conf = conf;
         this.environment = CacheAdmin.getEnvironment();
         this.params = params;
         this.startIndex = startIndex;
         this.fetchRows = maxFetchRows == -1 ? Integer.MAX_VALUE : maxFetchRows;
         this.query = query;
         if (params != null && !params.getResultLanguages().isEmpty()) {
             this.language = params.getResultLanguages().get(0);
             if (params.getResultLanguages().size() > 1) {
                 LOG.warn("FxSQL: multiple result languages are not implemented yet");
             }
         } else {
             this.language = FxContext.get().getTicket().getLanguage();
         }
         this.location = location;
         this.viewType = viewType;
         this.storage = StorageManager.getStorageImpl();
     }
 
     /**
      * Returns the content type filter, or null if the filter is not set.
      *
      * @return the content type filter, or null
      */
     public FxType getTypeFilter() {
         return typeFilter;
     }
 
     /**
      * Returns the language of this search.
      *
      * @return the language of this search
      */
     public FxLanguage getLanguage() {
         return language;
     }
 
     /**
      * Get the used storage implementation
      *
      * @return used storage implementation
      */
     public DBStorage getStorage() {
         return storage;
     }
 
     /**
      * Executes the search.
      *
      * @return the resultset
      * @throws FxSqlSearchException if the search failed
      */
     public FxResultSet executeQuery() throws FxSqlSearchException {
         parseQuery();
 
         // Check if the statement will produce any resultset at all
         if (statement.getType() == FxStatement.Type.EMPTY) {
             return new FxResultSetImpl(statement, this.parserExecutionTime, 0,
                     startIndex, fetchRows, location, viewType, null, -1, -1);
         }
 
         // Execute select
         Statement stmt = null;
         Connection con = null;
         FxResultSetImpl fx_result = null;
         final long startTime = java.lang.System.currentTimeMillis();
         DataSelector ds = null;
         DataFilter df = null;
         String selectSql = null;
         try {
 
             // Init
             switch (params.getCacheMode()) {
                 case ON:
                     /*cacheTbl = DatabaseConst.TBL_SEARCHCACHE_PERM;
                     searchId = seq.getId(SequencerEngine.System.SEARCHCACHE_PERM);
                     break;*/
                 case OFF:
                 case READ_ONLY:
                     if (params.isHintNoResultInfo() && storage.isDirectSearchSupported()) {
                         cacheTbl = "";      // will be set to the SELECT statement later
                         searchId = 1;       // not relevant, since no cache table is populated
                     } else {
                         cacheTbl = DatabaseConst.TBL_SEARCHCACHE_MEMORY;
                         searchId = seq.getId(FxSystemSequencer.SEARCHCACHE_MEMORY);
                     }
                     break;
                 default:
                     // Can never happen
                     cacheTbl = null;
             }
 
             // initialize search languages
             final String[] searchLanguages = statement.getTables()[0].getSearchLanguages();
             if (searchLanguages == null || searchLanguages.length == 0) {
                 this.searchLanguageIds = null;
             } else {
                 this.searchLanguageIds = Lists.newArrayListWithCapacity(searchLanguages.length);
                 for (String searchLanguage : searchLanguages) {
                     this.searchLanguageIds.add(environment.getLanguage(searchLanguage).getId());
                 }
             }
 
             con = Database.getDbConnection();
             pr = new PropertyResolver(con);
 
             //init filter and selector
             df = StorageManager.getDataFilter(con, this);
             ds = StorageManager.getDataSelector(this);
 
             // Find all matching objects
             df.build();
 
             // Wildcard handling depending on the found entries
             replaceWildcard(df);
             if (statement.getOrderByValues().isEmpty()) {
                 // add user-defined order by
                 for (ResultOrderByInfo column : getResultPreferences(df).getOrderByColumns()) {
                     try {
                         statement.addOrderByValue(new OrderByValue(column.getColumnName(),
                                 column.getDirection().equals(SortDirection.ASCENDING)));
                     } catch (SqlParserException e) {
                         if (LOG.isTraceEnabled()) {
                             LOG.trace("Ignoring user preferences column " + column
                                     + " since it was not selected.");
                         }
                     }
                 }
 
             }
 
             // If specified create a briefcase with the found data
             long createdBriefcaseId = -1;
             if (params.getWillCreateBriefcase()) {
                 createdBriefcaseId = copyToBriefcase(con, ds, df);
             }
 
             final UserTicket ticket = FxContext.getUserTicket();
 
             //list containing all used types with property permission checks enabled
             final List<FxType> propertyPermTypes = new ArrayList<FxType>(df.getContentTypes().size());
 
             //cache for assignments that are allowed/denied for types with property permission checks enabled
             List<String> allowedAssignment = null;
             List<String> deniedAssignment = null;
 
             //gather all types with property permission checks enabled
             if (!ticket.isGlobalSupervisor()) {
                 for (FxFoundType check : df.getContentTypes()) {
                     FxType c = environment.getType(check.getContentTypeId());
                     if (c.isUsePropertyPermissions())
                         propertyPermTypes.add(c);
                 }
             }
 
             if (params.isHintNoResultInfo() && storage.isDirectSearchSupported()) {
                 // take the filter SQL and select directly
                 cacheTbl = "(" + df.getDataSelectSql() + ")";
             }
 
             // Select all desired rows for the resultset
             selectSql = ds.build(con);
 
            if (LOG.isDebugEnabled()) {
                LOG.debug("Select SQL: " + selectSql);
            }


             stmt = con.createStatement();
             if (df.isQueryTimeoutSupported())
                 stmt.setQueryTimeout(params.getQueryTimeout());
             df.setVariable(stmt, "rownr", "1");
             // Fetch the result
             ResultSet rs = stmt.executeQuery(selectSql);
             int dbSearchTime = (int) (java.lang.System.currentTimeMillis() - startTime);
             fx_result = new FxResultSetImpl(statement, this.parserExecutionTime, dbSearchTime, startIndex,
                     fetchRows, location, viewType, df.getContentTypes(),
                     getTypeFilter() != null ? getTypeFilter().getId() : -1,
                     createdBriefcaseId);
             fx_result.setUserWildcardIndex(indexOfUserPropsWildcard != -1 ? indexOfUserPropsWildcard + 1 : -1);
             fx_result.setTotalRowCount(params.isHintNoResultInfo() ? -1 : df.getFoundEntries());
             fx_result.setTruncated(df.isTruncated());
 
             final long fetchStart = java.lang.System.currentTimeMillis();
             while (rs.next()) {
                 Object[] row = new Object[pr.getResultSetColumns().size()];
                 int i = 0;
                 final long typeId = rs.getLong(DataSelector.COL_TYPEID);
                 for (PropertyEntry entry : pr.getResultSetColumns()) {
                     Object val = entry.getResultValue(rs, language.getId(), true, typeId);
 
                     //in case we have types with property permissions enabled, inaccessible
                     //properties have to be wrapped with with FxNoAccess objects
                     if (val instanceof FxValue && !((FxValue) val).isEmpty() && propertyPermTypes.size() > 0) {
                         if (allowedAssignment == null)
                             allowedAssignment = new ArrayList<String>(20);
                         if (deniedAssignment == null)
                             deniedAssignment = new ArrayList<String>(20);
                         FxValue v = (FxValue) val;
                         String xp = XPathElement.toXPathNoMult(v.getXPath());
                         if (!allowedAssignment.contains(xp)) {
                             if (!deniedAssignment.contains(xp)) {
                                 FxPropertyAssignment pa = (FxPropertyAssignment) environment.getAssignment(xp);
                                 if (pa.getAssignedType().isUsePropertyPermissions()
                                         && !ticket.mayReadACL(pa.getACL().getId(), rs.getLong(DataSelector.COL_CREATED_BY))) {
                                     deniedAssignment.add(xp);
                                     val = new FxNoAccess(ticket, (FxValue) val);
                                 } else
                                     allowedAssignment.add(xp);
                             } else
                                 val = new FxNoAccess(ticket, (FxValue) val);
                         }
                     }
 
                     row[i] = val;
                     i++;
                 }
                 fx_result.addRow(row);
                 if (fx_result.getRowCount() == fetchRows) {
                     // Maximum fetch size reached, stop
                     break;
                 }
             }
             int timeSpent = (int) (java.lang.System.currentTimeMillis() - fetchStart);
             fx_result.setFetchTime(timeSpent);
             return fx_result;
         } catch (FxSqlSearchException exc) {
             throw exc;
         } catch (SQLException exc) {
             if (StorageManager.isQueryTimeout(exc)) {
                 if (LOG.isWarnEnabled()) {
                     LOG.warn("Query timeout after " + params.getQueryTimeout() + " seconds:\n"
                             + query + "\n\nSQL:\n" + selectSql);
                 }
                 throw new FxSqlSearchException(exc, "ex.sqlSearch.query.timeout", params.getQueryTimeout());
             } else if (StorageManager.isDeadlock(exc)) {
                 if (LOG.isInfoEnabled()) {
                     LOG.info("Deadlock detected during query executing, waiting 100ms and retrying...");
                     try {
                         Thread.sleep(100);
                     } catch (InterruptedException e) {
                         // ignore
                     }
                     return executeQuery();
                 }
             }
             throw new FxSqlSearchException(LOG, exc, "ex.sqlSearch.failed", exc.getMessage(), query, selectSql);
         } catch (Exception e) {
             throw new FxSqlSearchException(LOG, e, "ex.sqlSearch.failed", e.getMessage(), query, selectSql);
         } finally {
             try {
                 if (ds != null) ds.cleanup(con);
             } catch (Throwable t) {/*ignore*/}
             try {
                 if (df != null) df.cleanup();
             } catch (Throwable t) {/*ignore*/}
             Database.closeObjects(SqlSearch.class, con, stmt);
             if (fx_result != null) {
                 int timeSpent = (int) (java.lang.System.currentTimeMillis() - startTime);
                 fx_result.setTotalTime(timeSpent);
             }
         }
     }
 
     private void parseQuery() throws FxSqlSearchException {
         // Parse the statement
         try {
             final long start = java.lang.System.currentTimeMillis();
             statement = FxStatement.parseSql(query);
             this.indexOfUserPropsWildcard = this.indexOfUserWildcard();
             this.hasUserPropsWildcard = this.indexOfUserPropsWildcard != -1;
             this.parserExecutionTime = (int) (java.lang.System.currentTimeMillis() - start);
         } catch (SqlParserException pe) {
             // Catch the parse exception and convert it to an localized one
             throw new FxSqlSearchException(LOG, pe);
         } catch (Throwable t) {
             throw new FxSqlSearchException(LOG, t, "ex.sqlSearch.parser.error", t.getMessage(), query);
         }
 
         // Process content type filter
         if (statement.hasContentTypeFilter()) {
             String type = statement.getContentTypeFilter();
             try {
                 typeFilter = StringUtils.isNumeric(type)
                         ? environment.getType(Long.parseLong(type))
                         : environment.getType(type);
             } catch (Throwable t) {
                 throw new FxSqlSearchException(LOG, t, "ex.sqlSearch.filter.invalidContentTypeFilterValue", type);
             }
         } else {
             typeFilter = null;
         }
     }
 
     /**
      * Copy the query result to a briefcase
      *
      * @param con connection
      * @param ds  DataSelector
      * @param df DataFilter
      * @return briefcase id
      * @throws FxSqlSearchException on errors
      */
     private long copyToBriefcase(Connection con, DataSelector ds, DataFilter df) throws FxSqlSearchException {
         FxSQLSearchParams.BriefcaseCreationData bcd = params.getBriefcaseCreationData();
         Statement stmt = null;
         try {
             // Create the briefcase
             long bid = briefcase.create(bcd.getName(), bcd.getDescription(), bcd.getAclId());
             stmt = con.createStatement();
             df.setVariable(stmt, "pos", "0");
 //            stmt.addBatch("SET @pos=0;");
             String sSql = "insert into " + DatabaseConst.TBL_BRIEFCASE_DATA +
                     "(BRIEFCASE_ID,POS,ID,AMOUNT) " +
                     "(select " + bid + "," + ds.getCounterStatement("pos") + ",data.id,1 from " +
                     "(SELECT DISTINCT data2.id FROM " + getCacheTable() + " data2 WHERE data2.search_id="
                     + getSearchId() + ") data)";
 //            stmt.addBatch(sSql);
             stmt.execute(sSql);
             return bid;
         } catch (Throwable t) {
             throw new FxSqlSearchException(LOG, t, "ex.sqlSearch.err.failedToBuildBriefcase", bcd.getName());
         } finally {
             Database.closeObjects(SqlSearch.class, null, stmt);
         }
     }
 
     public int getStartIndex() {
         return startIndex;
     }
 
     public int getFetchRows() {
         return fetchRows;
     }
 
     public FxStatement getFxStatement() {
         return statement;
     }
 
     public PropertyResolver getPropertyResolver() {
         return pr;
     }
 
     public FxSQLSearchParams getParams() {
         return this.params;
     }
 
     public String getCacheTable() {
         return cacheTbl;
     }
 
     /**
      * Returns the unique id of this search.
      *
      * @return the unique id of this search
      */
     public long getSearchId() {
         return searchId;
     }
 
     private int indexOfUserWildcard() throws FxSqlSearchException {
         int index = -1;
         for (int i = 0; i < statement.getSelectedValues().size(); i++) {
             final SelectedValue value = statement.getSelectedValues().get(i);
             if (value.getValue() instanceof Property) {
                 Property prop = ((Property) value.getValue());
                 if (prop.isUserPropsWildcard()) {
                     if (index != -1) {
                         // Only one wildcard may be used per statement
                         throw new FxSqlSearchException(LOG, "ex.sqlSearch.onlyOneWildcardPermitted");
                     }
                     index = i;
                 }
             }
         }
         return index;
     }
 
     /**
      * Replaces the wildcard in the fx_statement by the defined properties.
      *
      * @param df the datafilter
      * @throws FxSqlSearchException if the function fails
      */
     private void replaceWildcard(DataFilter df) throws FxSqlSearchException {
 
         try {
             ArrayList<SelectedValue> selValues = new ArrayList<SelectedValue>();
             for (SelectedValue _value : statement.getSelectedValues()) {
                 final Property propValue = _value.getValue() instanceof Property ? (Property) _value.getValue() : null;
                 if (propValue != null && propValue.isWildcard()) {
                     // Wildcard, select all properties of the result type
                     for (FxProperty property : getAllProperties(df)) {
                         final Property prop = new Property(propValue.getTableAlias(), property.getName(), null);
                         selValues.add(new SelectedValue(prop, null));
                     }
                 } else if (propValue != null && propValue.isUserPropsWildcard()) {
                     // User preferences wildcard
                     for (ResultColumnInfo nfo : getResultPreferences(df).getSelectedColumns()) {
                         Property newProp = new Property(propValue.getTableAlias(), nfo.getPropertyName(), nfo.getSuffix());
                         SelectedValue newSel = new SelectedValue(newProp, null);
                         selValues.add(newSel);
                     }
                 } else {
                     // Normal property, use it as is
                     selValues.add(_value);
                 }
             }
             statement.setSelectedValues(selValues);
         } catch (Throwable t) {
             throw new FxSqlSearchException(LOG, t, "ex.sqlSearch.wildcardProcessingFailed");
         }
     }
 
     private ResultPreferences getResultPreferences(DataFilter df) throws FxApplicationException {
         if (resultPreferences == null) {
             resultPreferences = conf.load(getContentTypeId(df), viewType, location);
         }
         return resultPreferences;
     }
 
     private long getContentTypeId(DataFilter df) {
         return getTypeFilter() != null ?
                 // Type filter: only one type is contained in the search, use it
                 getTypeFilter().getId() :
                 // No Type filter: see if we got only one type in the result, or use the default for all types
                 df.getContentTypes().size() == 1 ? df.getContentTypes().get(0).getContentTypeId() : -1;
     }
 
     private List<FxProperty> getAllProperties(DataFilter df) {
         long typeId = getContentTypeId(df);
         if (typeId == -1) {
             // return all properties attached to the root
             typeId = FxType.ROOT_ID;
         }
         final List<FxProperty> typeProps = new ArrayList<FxProperty>();
         final List<FxProperty> systemProps = new ArrayList<FxProperty>();
         // return all properties of the type - first user-defined type props, then the system-internal props
         for (FxPropertyAssignment assignment : environment.getType(typeId).getAssignedProperties()) {
             if (assignment.getProperty().isSearchable()) {
                 (assignment.isSystemInternal() ? systemProps : typeProps).add(assignment.getProperty());
             }
         }
         typeProps.addAll(systemProps);
         return typeProps;
     }
 
     public String getQuery() {
         return query;
     }
 
     public TreeEngine getTreeEngine() {
         return treeEngine;
     }
 
     public FxEnvironment getEnvironment() {
         return environment;
     }
 
     public List<Long> getSearchLanguageIds() {
         return searchLanguageIds;
     }
 }
