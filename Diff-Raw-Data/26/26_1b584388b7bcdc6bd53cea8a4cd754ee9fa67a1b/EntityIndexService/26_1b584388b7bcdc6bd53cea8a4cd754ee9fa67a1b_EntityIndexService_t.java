 package com.cnnic.whois.service;
 
 import java.net.MalformedURLException;
 import java.util.List;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
 import org.apache.solr.client.solrj.response.QueryResponse;
 
 import com.cnnic.whois.bean.PageBean;
 import com.cnnic.whois.bean.index.EntityIndex;
 import com.cnnic.whois.bean.index.SearchCondition;
 import com.cnnic.whois.service.index.SearchResult;
 import com.cnnic.whois.util.WhoisProperties;
 
 public class EntityIndexService {
 	private static EntityIndexService indexService = new EntityIndexService(
 			WhoisProperties.getEntitySolrUrl());
 	private static final String ARRAY_SPLITER = "'~'";
 	private static final String FUZZY_MARK = "*";
 	private static final String Q_OR = " OR ";
 	private CommonsHttpSolrServer server;
 
 	public static EntityIndexService getIndexService() {
 		return indexService;
 	}
 
 	public SearchResult<EntityIndex> preciseQueryEntitiesByHandleOrName(
 			String handleOrName) {
 		handleOrName = handleOrName.replace(" ", "\\ ").replace(":", "\\:");
 		String entityNamePrefix = "entityNames:";
 		String entityNamesQ = entityNamePrefix + handleOrName;
 		String entityNamesQP = entityNamePrefix + "*" + "'~'" + handleOrName;
 		String entityNamesQS = entityNamePrefix + handleOrName + "'~'" + "*";
 		String entityNamesQPS = entityNamePrefix + "*" + "'~'" + handleOrName
 				+ "'~'" + "*";
 		String entityNamesQFull = entityNamesQ + " OR " + entityNamesQP
 				+ " OR " + entityNamesQS + " OR " + entityNamesQPS;
 		String queryStr = "(roles:registrar AND (" + entityNamesQFull + ")) "
				+ "OR (handle:" + handleOrName + ")";
 		queryStr = queryStr.replace("~", "\\~");
 		SearchCondition searchCondition = new SearchCondition(queryStr);
 		searchCondition.setRow(QueryService.MAX_SIZE_FUZZY_QUERY);
 		return queryEntities(searchCondition);
 	}
 
 	public SearchResult<EntityIndex> fuzzyQueryEntitiesByHandleAndName(
 			String fuzzyQueryParamName, String handleOrName, PageBean page) {
 		handleOrName = handleOrName.replace(" ", "\\ ").replace(":", "\\:");
 		String kVSplit = ":";
 		String entityNamesQ = fuzzyQueryParamName + kVSplit + handleOrName;
 		String entityNamesQP = fuzzyQueryParamName + kVSplit + "*" + "'~'"
 				+ handleOrName;
 		String queryStr = entityNamesQ + " OR " + entityNamesQP;
 		queryStr = queryStr.replace("~", "\\~");
 		SearchCondition searchCondition = new SearchCondition(queryStr);
 		int startPage = page.getCurrentPage() - 1;
 		startPage = startPage >= 0 ? startPage : 0;
 		int start = startPage * page.getMaxRecords();
 		searchCondition.setStart(start);
 		searchCondition.setRow(page.getMaxRecords());
 		SearchResult<EntityIndex> result = queryEntities(searchCondition);
 		page.setRecordsCount(Long.valueOf(result.getTotalResults()).intValue());
 		return result;
 	}
 
 	public SearchResult<EntityIndex> queryEntities(
 			SearchCondition searchCondition) {
 		SolrQuery solrQuery = new SolrQuery();
 		solrQuery.setQuery(searchCondition.getSearchword());
 		solrQuery.setStart(Integer.valueOf(searchCondition.getStart()));
 		solrQuery.setRows(Integer.valueOf(searchCondition.getRow()));
 		QueryResponse queryResponse = null;
 		try {
 			queryResponse = this.server.query(solrQuery);
 		} catch (SolrServerException e) {
 			e.printStackTrace();
 		}
 		SearchResult searchResult = new SearchResult();
 		try {
 			setSearchResult(searchResult, queryResponse);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return searchResult;
 	}
 
 	private void setSearchResult(SearchResult<EntityIndex> searchResult,
 			QueryResponse queryResponse) {
 		searchResult.setSearchTime(queryResponse.getElapsedTime() / 1000.0D);
 		searchResult.setTotalResults(queryResponse.getResults().getNumFound());
 		List indexes = queryResponse.getBeans(EntityIndex.class);
 		searchResult.setResultList(indexes);
 	}
 
 	public EntityIndexService(String url) {
 		try {
 			this.server = new CommonsHttpSolrServer(url);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 }
