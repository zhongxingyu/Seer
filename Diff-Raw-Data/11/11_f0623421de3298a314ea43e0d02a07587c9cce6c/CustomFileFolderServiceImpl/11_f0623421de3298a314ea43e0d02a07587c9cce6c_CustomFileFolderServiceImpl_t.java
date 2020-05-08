 package com.optaros.alfresco.docasu.wcs;
 
 /*
  *    Copyright (C) 2008 Optaros, Inc. All rights reserved.
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program. If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
 import org.alfresco.service.cmr.dictionary.DictionaryService;
 import org.alfresco.service.cmr.repository.NodeRef;
 import org.alfresco.service.cmr.repository.StoreRef;
 import org.alfresco.service.cmr.search.ResultSet;
 import org.alfresco.service.cmr.search.ResultSetRow;
 import org.alfresco.service.cmr.search.SearchParameters;
 import org.alfresco.service.cmr.search.SearchService;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.optaros.alfresco.docasu.wcs.helper.QueryManager;
 
 /**
  * @author Thomas Verin, Jean-Luc Geering
  * @see org.alfresco.repo.model.filefolder.FileFolderServiceImpl (copied from)
  */
 public class CustomFileFolderServiceImpl implements CustomFileFolderService {
 
 	private static Log logger = LogFactory.getLog(CustomFileFolderServiceImpl.class);
 
 	// Class not found in alfresco-enterprise-2.1.1 !
 	// private TenantService tenantService;
 	private DictionaryService dictionaryService;
 	private SearchService searchService;
 	private Properties blacklist;
 	private Properties whitelist;
 	// FIXME: remove this if no longer used
 	@SuppressWarnings("unused")
 	private DataTypeDefinition dataTypeNodeRef;
 
 	public void init() {
 		dataTypeNodeRef = dictionaryService.getDataType(DataTypeDefinition.NODE_REF);
 	}
 
 	// public void setTenantService(TenantService tenantService) {
 	// this.tenantService = tenantService;
 	// }
 
 	public void setDictionaryService(DictionaryService dictionaryService) {
 		this.dictionaryService = dictionaryService;
 	}
 
 	public void setSearchService(SearchService searchService) {
 		this.searchService = searchService;
 	}
 
 	public void setBlacklist(Properties blacklist) {
 		this.blacklist = blacklist;
 	}
 
 	public void setWhitelist(Properties whitelist) {
 		this.whitelist = whitelist;
 	}
 
 	private List<NodeRef> toNodeRef(ResultSet rs) {
 		List<NodeRef> nodeRefs = new ArrayList<NodeRef>(rs.length());
 		try {
 			for (ResultSetRow row : rs) {
 				nodeRefs.add(row.getNodeRef());
 			}
 		} finally {
 			rs.close();
 		}
 
 		return nodeRefs;
 	}
 
 	public List<NodeRef> listCategory(NodeRef contextNodeRef, String categoryName) {
 		SearchParameters params = new SearchParameters();
 		params.setLanguage(SearchService.LANGUAGE_LUCENE);
 		params.addStore(contextNodeRef.getStoreRef());
 
 		// add query
 		String query = new QueryManager().createCategoryQuery(categoryName, whitelist, blacklist);
 		params.setQuery(query);
 
 		if (logger.isDebugEnabled()) {
 			logger.debug("List query = '" + query + "'");
 		}
 
 		return searchLucene(params);
 	}
 
 	public List<NodeRef> list(NodeRef contextNodeRef, boolean foldersOnly) {
 
 		// contextNodeRef = tenantService.getName(contextNodeRef);
 
 		SearchParameters params = new SearchParameters();
 		params.setLanguage(SearchService.LANGUAGE_LUCENE);
 		params.addStore(contextNodeRef.getStoreRef());
 
 		// add query
 		String query = new QueryManager().createListQuery(contextNodeRef, foldersOnly, whitelist, blacklist);
 		params.setQuery(query);
 
 		// log4j.logger.org.alfresco.repo.search.impl.lucene.LuceneQueryParser=debug
 		if (logger.isDebugEnabled()) {
 			logger.debug("List query = '" + query + "'");
 		}
 
 		return searchLucene(params);
 	}
 
 	public List<NodeRef> search(StoreRef store, String query, SearchType type) {
		query = filterQuery(query);
 		return search(store, query, type, null, null, null, null, null);
 	}
 
 	public List<NodeRef> search(StoreRef store, String query, SearchType type, NodeRef lookInFolder, Date createdFrom, Date createdTo, Date modifiedFrom,
 			Date modifiedTo) {
		query = filterQuery(query);
 		if (logger.isDebugEnabled()) {
 			logger.debug("SEARCH PARAM store = " + store);
 			logger.debug("SEARCH PARAM query = " + query);
 			logger.debug("SEARCH PARAM type = " + type);
 			logger.debug("SEARCH PARAM lookInFolder = " + lookInFolder);
 			logger.debug("SEARCH PARAM createdFrom = " + createdFrom);
 			logger.debug("SEARCH PARAM createdTo = " + createdTo);
 			logger.debug("SEARCH PARAM modifiedFrom = " + modifiedFrom);
 			logger.debug("SEARCH PARAM modifiedTo = " + modifiedTo);
 		}
 
 		SearchParameters params = new SearchParameters();
 		params.setLanguage(SearchService.LANGUAGE_LUCENE);
 		params.addStore(store);
 
 		// add query
 		String luceneQuery = new QueryManager().createSearchQuery(query, type, lookInFolder, createdFrom, createdTo, modifiedFrom, modifiedTo, whitelist,
 				blacklist);
 		params.setQuery(luceneQuery);
 
 		// log4j.logger.org.alfresco.repo.search.impl.lucene.LuceneQueryParser=debug
 		if (logger.isDebugEnabled()) {
 			logger.debug("Search query = '" + luceneQuery + "'");
 		}
 
 		return searchLucene(params);
 	}
 
 	/**
 	 * Performs the search against repository.
 	 * 
 	 * @param params
 	 * @return
 	 */
 	private List<NodeRef> searchLucene(SearchParameters params) {
 		// perform the search against the repository
 		ResultSet rs = searchService.query(params);
 		List<NodeRef> results = toNodeRef(rs);
 		return results;
 	}
	
	private String filterQuery(String query){
	//Replace the double quotes (") with a space 
		query = query.replace('"', ' ');
		return query;
	}
 
 }
