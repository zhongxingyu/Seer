 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
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
 import com.flexive.core.search.mysql.MySQLDataFilter;
 import com.flexive.core.search.mysql.MySQLDataSelector;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.configuration.DBVendor;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxSqlSearchException;
 import com.flexive.shared.interfaces.BriefcaseEngine;
 import com.flexive.shared.interfaces.ResultPreferencesEngine;
 import com.flexive.shared.interfaces.SequencerEngine;
 import com.flexive.shared.interfaces.TreeEngine;
 import com.flexive.shared.search.*;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.structure.FxType;
 import com.flexive.sqlParser.*;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Arrays;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.lang.StringUtils;
 
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
 
     private FxStatement statement;
     private FxType typeFilter;
     private PropertyResolver pr;
     private int parserExecutionTime = -1;
     private long searchId = -1;
     private String cacheTbl;
     private final FxSQLSearchParams params;
     private boolean hasWildcard = false;
     private FxEnvironment environment;
     private final ResultPreferencesEngine conf;
     private FxLanguage language;
 
     /**
      * Ctor
      *
      * @param seq          reference to the sequencer
      * @param briefcase    reference to the briefcase engine
      * @param treeEngine   reference to the tree engine
      * @param query        the query to execute
      * @param startIndex   the start index (0 based)
      * @param maxFetchRows the number of rows to return with the resultset, or null to fetch all rows
      * @param params       all aditional search parameters
      * @param conf         the result set configuration
      * @param location     the location that started the search
      * @param viewType     the view type @throws com.flexive.shared.exceptions.FxSqlSearchException
      *          if the search failed
      * @throws com.flexive.shared.exceptions.FxSqlSearchException   if the search engine could not be initialized
      */
     public SqlSearch(SequencerEngine seq, BriefcaseEngine briefcase, TreeEngine treeEngine, String query, int startIndex, Integer maxFetchRows, FxSQLSearchParams params,
                     ResultPreferencesEngine conf, ResultLocation location, ResultViewType viewType) throws FxSqlSearchException {
         FxSharedUtils.checkParameterEmpty(query, "query");
         // Init
         this.seq = seq;
         this.briefcase = briefcase;
         this.treeEngine = treeEngine;
         this.conf = conf;
         this.environment = CacheAdmin.getEnvironment();
         this.params = params;
         this.startIndex = startIndex;
         this.fetchRows = maxFetchRows == null ? Integer.MAX_VALUE : maxFetchRows;
         this.query = query;
         this.language = FxContext.get().getTicket().getLanguage();
         this.location = location;
         this.viewType = viewType;
 
         // Parameter checks
         if (this.startIndex < 0) {
             throw new FxSqlSearchException(LOG, "ex.sqlSearch.parameter.invalidStartIndex", startIndex);
         }
 
         if (maxFetchRows != null) {
             if (maxFetchRows < 1 && maxFetchRows != -1) {
                 throw new FxSqlSearchException(LOG, "ex.sqlSearch.parameter.fetchRows", maxFetchRows);
             }
         }
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
                     cacheTbl = DatabaseConst.TBL_SEARCHCACHE_PERM;
                     searchId = seq.getId(SequencerEngine.System.SEARCHCACHE_PERM);
                     break;
                 case OFF:
                 case READ_ONLY:
                     cacheTbl = DatabaseConst.TBL_SEARCHCACHE_MEMORY;
                     searchId = seq.getId(SequencerEngine.System.SEARCHCACHE_MEMORY);
                     break;
                 default:
                     // Can never happen
                     cacheTbl = null;
             }
 
             con = Database.getDbConnection();
             pr = new PropertyResolver(con);
 
             // Find all matching objects
             df = getDataFilter(con);
             df.build();
 
             // Wildcard handling depending on the found entries
             if (this.hasWildcard) {
                 replaceWildcard(df);
             }
             if (statement.getOrderByValues().isEmpty()) {
                 // add user-defined order by
                 final ResultPreferences resultPreferences = getResultPreferences(df);
                 for (ResultOrderByInfo column : resultPreferences.getOrderByColumns()) {
                     try {
                         statement.addOrderByValue(new OrderByValue(column.getColumnName(),
                                 column.getDirection().equals(SortDirection.ASCENDING)));
                     } catch (SqlParserException e) {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("Ignoring user preferences column " + column
                                     + " since it was not selected.");
                         }
                     }
                 }
 
             }
 
             // If specified create a briefcase with the found data
             long createdBriefcaseId = -1;
             if (params.getWillCreateBriefcase()) {
                 createdBriefcaseId = copyToBriefcase(con);
             }
 
             // Select all desired rows for the resultset
             ds = getDataSelector();
             selectSql = ds.build(con);
 
             stmt = con.createStatement();
             stmt.executeUpdate("set @rownr=1;");
             stmt.close();
 
             stmt = con.createStatement();
             stmt.setQueryTimeout(params.getQueryTimeout());
 
             // Fetch the result
             ResultSet rs = stmt.executeQuery(selectSql);
             int dbSearchTime = (int) (java.lang.System.currentTimeMillis() - startTime);
             fx_result = new FxResultSetImpl(statement, this.parserExecutionTime, dbSearchTime, startIndex,
                     fetchRows, location, viewType, df.getContentTypes(),
                     getTypeFilter() != null ? getTypeFilter().getId() : -1,
                     createdBriefcaseId);
             fx_result.setTotalRowCount(df.getFoundEntries());
             fx_result.setTruncated(df.isTruncated());
 
             final long fetchStart = java.lang.System.currentTimeMillis();
             while (rs.next()) {
                 Object[] row = new Object[pr.getResultSetColumns().size()];
                 int i = 0;
                 for (PropertyEntry entry : pr.getResultSetColumns()) {
                     //Object val =getValue(rs,entry);
                     Object val = entry.getResultValue(rs, language);
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
             this.hasWildcard = this.hasWildcard();
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
 
     private long copyToBriefcase(Connection con) throws FxSqlSearchException {
         FxSQLSearchParams.BriefcaseCreationData bcd = params.getBriefcaseCreationData();
         Statement stmt = null;
         try {
             // Create the briefcase
             long bid = briefcase.create(bcd.getName(), bcd.getDescription(), bcd.getAclId());
             stmt = con.createStatement();
             stmt.addBatch("SET @pos=0;");
             String sSql = "insert into " + DatabaseConst.TBL_BRIEFCASE_DATA +
                     "(BRIEFCASE_ID,POS,ID,AMOUNT) " +
                     "(select " + bid + ",@pos:=@pos+1 pos,data.id,1 from " +
                     "(SELECT DISTINCT data2.id FROM " + getCacheTable() + " data2 WHERE data2.search_id="
                     + getSearchId() + ") data)";
             stmt.addBatch(sSql);
             stmt.executeBatch();
             return bid;
         } catch (Throwable t) {
             throw new FxSqlSearchException(LOG, t, "ex.sqlSearch.err.failedToBuildBriefcase", bcd.getName());
         } finally {
             Database.closeObjects(MySQLDataSelector.class, null, stmt);
         }
     }
 
     /**
      * Get the DataSelector for the sql searchengine based on the used DB
      *
      * @return DataSelector the data selecttor implementation
      * @throws FxSqlSearchException if the function fails
      */
     public DataSelector getDataSelector() throws FxSqlSearchException {
         DBVendor vendor;
         try {
             vendor = Database.getDivisionData().getDbVendor();
             switch (vendor) {
                 case MySQL:
                     return new MySQLDataSelector(this);
                 default:
                     throw new FxSqlSearchException(LOG, "ex.db.selector.undefined", vendor);
             }
         } catch (SQLException e) {
            throw new FxSqlSearchException(LOG, "ex.db.vendor.notFound", FxContext.get().getDivisionId());
         }
     }
 
     /**
      * Get the DataSelector for the sql searchengine based on the used DB
      *
      * @param con the connection to use
      * @return DataSelector the data selecttor implementation
      * @throws FxSqlSearchException if the function fails
      */
     public DataFilter getDataFilter(Connection con) throws FxSqlSearchException {
         DBVendor vendor;
         try {
             vendor = Database.getDivisionData().getDbVendor();
             switch (vendor) {
                 case MySQL:
                     return new MySQLDataFilter(con, this);
                 default:
                     throw new FxSqlSearchException(LOG, "ex.db.filter.undefined", vendor);
             }
         } catch (SQLException e) {
            throw new FxSqlSearchException(LOG, "ex.db.vendor.notFound", FxContext.get().getDivisionId());
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
 
     private boolean hasWildcard() throws FxSqlSearchException {
         // Find out if we have to deal with a wildcard
         boolean hasWildcard = false;
         for (SelectedValue value : statement.getSelectedValues()) {
             if (value.getValue() instanceof Property) {
                 Property prop = ((Property) value.getValue());
                 if (prop.isWildcard()) {
                     if (hasWildcard) {
                         // Only one wildcard may be used per statement
                         throw new FxSqlSearchException(LOG, "ex.sqlSearch.onlyOneWildcardPermitted");
                     }
                     hasWildcard = true;
                 }
             }
         }
         return hasWildcard;
     }
 
     /**
      * Replaces the wildcard in the fx_statement by the defined properties.
      *
      * @param df the datafilter
      * @throws FxSqlSearchException if the function fails
      */
     private void replaceWildcard(DataFilter df) throws FxSqlSearchException {
 
         try {
             ResultPreferences prefs = getResultPreferences(df);
             ArrayList<SelectedValue> selValues = new ArrayList<SelectedValue>(
                     (statement.getSelectedValues().size() - 1) + prefs.getSelectedColumns().size());
             for (SelectedValue _value : statement.getSelectedValues()) {
                 if (_value.getValue() instanceof Property && ((Property) _value.getValue()).isWildcard()) {
                     Property wildcard = (Property) _value.getValue();
                     // Wildcard, replace it with the correct values
                     for (ResultColumnInfo nfo : prefs.getSelectedColumns()) {
                         Property newProp = new Property(wildcard.getTableAlias(), nfo.getPropertyName(), nfo.getSuffix());
                         SelectedValue newSel = new SelectedValue(newProp, nfo.getLabel(this.environment));
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
         long cType = getTypeFilter() != null ?
                 // Type filter: only one type is contained in the search, use it
                 getTypeFilter().getId() :
                 // No Type filter: see if we got only one type in the result, or use the default for all types
                 df.getContentTypes().size() == 1 ? df.getContentTypes().get(0).getContentTypeId() : -1;
 
         //ArrayList<SelectedValue> selValues = new ArrayList<SelectedValue>(statement.getSelectedValues().size()+25);
         return conf.load(cType, viewType, location);
     }
 
     public String getQuery() {
         return query;
     }
 
     public TreeEngine getTreeEngine() {
         return treeEngine;
     }
 }
