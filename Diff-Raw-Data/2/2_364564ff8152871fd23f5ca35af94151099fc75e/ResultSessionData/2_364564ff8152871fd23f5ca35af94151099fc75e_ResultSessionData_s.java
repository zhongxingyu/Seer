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
 package com.flexive.faces.beans;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.shared.search.FxFoundType;
 import com.flexive.shared.search.ResultLocation;
 import com.flexive.shared.search.ResultViewType;
 import com.flexive.shared.search.FxResultSet;
 import com.flexive.shared.search.query.SqlQueryBuilder;
 import com.flexive.shared.search.query.VersionFilter;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.value.BinaryDescriptor;
 
 import javax.servlet.http.HttpSession;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Formatter;
 import java.util.List;
 
 /**
  * Wrapper class for all session data needed by a search result page.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class ResultSessionData implements Serializable {
     private static final long serialVersionUID = -3471062917140804393L;
 
     private String tabTitle;
     private ResultLocation location;
     private SqlQueryBuilder queryBuilder;
     private BinaryDescriptor.PreviewSizes previewSize;
     private long briefcaseId;
     private ResultViewType viewType = ResultViewType.LIST;
     private int startRow;
     private int fetchRows = 25;
     private long typeId = -1;
     private String sortColumnKey;
     private String sortDirection;
     private int paginatorPage;
     private VersionFilter versionFilter = VersionFilter.MAX;
     private List<FxFoundType> contentTypes = new ArrayList<FxFoundType>(0);
 
     private FxResultSet result; // only when ResultLocation#isCacheInSession is true
 
    private long folderId;
     private FxTreeMode treeMode;
     private boolean onlyDirectChildren;
     private boolean enableFolderActions;
 
     // Keep a reference on the current session if not retrieved from a JSF context
     private transient HttpSession session = null;
 
     /**
      * Session key for storing the current search session data. The first parameter
      * identifies the {@link com.flexive.shared.search.AdminResultLocations}
      * for the current search result. Thus each "location" is independent from the state
      * of others, but it is not possible to have two separate instances of the same location
      * (i.e. two browser windows).
      */
     private static String SESSION_DATA_STORE = "SearchResultBean/%s";
 
     /**
      * Creates an empty session data object.
      *
      * @param location the search result location
      */
     private ResultSessionData(ResultLocation location) {
         this(location, new SqlQueryBuilder(), BinaryDescriptor.PreviewSizes.PREVIEW2, -1);
     }
 
     /**
      * Copy constructor. Creates an independent copy of the given session data object.
      *
      * @param other the session data object to be copied
      */
     public ResultSessionData(ResultSessionData other) {
         this(other.location, new SqlQueryBuilder(other.queryBuilder), other.previewSize, other.briefcaseId);
     }
 
     private ResultSessionData(ResultLocation location, SqlQueryBuilder queryBuilder, BinaryDescriptor.PreviewSizes previewSize, long briefcaseId) {
         this.location = location;
         this.queryBuilder = queryBuilder;
         this.previewSize = previewSize;
         this.briefcaseId = briefcaseId;
     }
 
     public static ResultSessionData getSessionData(HttpSession session, ResultLocation location) {
         ResultSessionData data = (ResultSessionData) session.getAttribute(getSessionKey(location));
         if (data == null) {
             data = new ResultSessionData(location);
             data.session = session;
             data.saveInSession();
         }
         data.session = session;
         return data;
     }
 
     private static String getSessionKey(ResultLocation location) {
         return new Formatter().format(ResultSessionData.SESSION_DATA_STORE, location.getName()).toString();
     }
 
     private void saveInSession() {
         (session == null ? FxJsfUtils.getSession() : session).setAttribute(getSessionKey(location), this);
     }
 
     public ResultLocation getLocation() {
         return location;
     }
 
     public SqlQueryBuilder getQueryBuilder() {
         return queryBuilder;
     }
 
     public void setQueryBuilder(SqlQueryBuilder queryBuilder) {
         if (this.queryBuilder != null) {
             this.result = null;
         }
         this.queryBuilder = queryBuilder;
         saveInSession();
     }
 
     public BinaryDescriptor.PreviewSizes getPreviewSize() {
         return previewSize;
     }
 
     public void setPreviewSize(BinaryDescriptor.PreviewSizes previewSize) {
         this.previewSize = previewSize;
         saveInSession();
     }
 
     public long getBriefcaseId() {
         return briefcaseId;
     }
 
     public void setBriefcaseId(long briefcaseId) {
         if (this.briefcaseId != briefcaseId) {
             this.result = null;
         }
         this.briefcaseId = briefcaseId;
         saveInSession();
     }
 
     public ResultViewType getViewType() {
         return viewType;
     }
 
     public void setViewType(ResultViewType viewType) {
         if (this.viewType == null || !this.viewType.equals(viewType)) {
             this.result = null;
         }
         this.viewType = viewType;
         saveInSession();
     }
 
     public int getStartRow() {
         return startRow;
     }
 
     public void setStartRow(int startRow) {
         this.startRow = startRow;
         saveInSession();
     }
 
     public int getFetchRows() {
         return fetchRows;
     }
 
     public void setFetchRows(int fetchRows) {
         this.fetchRows = fetchRows;
         saveInSession();
     }
 
     public long getTypeId() {
         return typeId;
     }
 
     public void setTypeId(long typeId) {
         if (typeId != this.typeId) {
             this.result = null;
         }
         this.typeId = typeId;
         saveInSession();
     }
 
     public List<FxFoundType> getContentTypes() {
         return contentTypes;
     }
 
     public void setContentTypes(List<FxFoundType> contentTypes) {
         this.contentTypes = contentTypes;
         saveInSession();
     }
 
     public VersionFilter getVersionFilter() {
         return versionFilter;
     }
 
     public void setVersionFilter(VersionFilter versionFilter) {
         if (versionFilter != this.versionFilter) {
             this.result = null;
         }
         this.versionFilter = versionFilter;
         saveInSession();
     }
 
     public String getSortColumnKey() {
         return sortColumnKey;
     }
 
     public void setSortColumnKey(String sortColumnKey) {
         this.sortColumnKey = sortColumnKey;
         saveInSession();
     }
 
     public String getSortDirection() {
         return sortDirection;
     }
 
     public void setSortDirection(String sortDirection) {
         this.sortDirection = sortDirection;
         saveInSession();
     }
 
     public int getPaginatorPage() {
         return paginatorPage;
     }
 
     public void setPaginatorPage(int paginatorPage) {
         this.paginatorPage = paginatorPage;
         saveInSession();
     }
 
     public FxResultSet getResult() {
         return result;
     }
 
     public void setResult(FxResultSet result) {
         this.result = result;
         saveInSession();
     }
 
     public String getTabTitle() {
         return tabTitle;
     }
 
     public void setTabTitle(String tabTitle) {
         this.tabTitle = tabTitle;
         saveInSession();
     }
 
     public long getFolderId() {
         return folderId;
     }
 
     public void setFolderId(long folderId) {
         this.folderId = folderId;
         saveInSession();
     }
 
     public boolean isEnableFolderActions() {
         return enableFolderActions;
     }
 
     public void setEnableFolderActions(boolean enableFolderActions) {
         this.enableFolderActions = enableFolderActions;
         saveInSession();
     }
 
     public FxTreeMode getTreeMode() {
         return treeMode;
     }
 
     public void setTreeMode(FxTreeMode treeMode) {
         this.treeMode = treeMode;
         saveInSession();
     }
 
     public boolean isOnlyDirectChildren() {
         return onlyDirectChildren;
     }
 
     public void setOnlyDirectChildren(boolean onlyDirectChildren) {
         this.onlyDirectChildren = onlyDirectChildren;
         saveInSession();
     }
 }
