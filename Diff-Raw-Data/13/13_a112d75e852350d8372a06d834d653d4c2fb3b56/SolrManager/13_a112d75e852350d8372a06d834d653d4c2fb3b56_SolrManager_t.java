 package org.aap.monitoring;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
 import org.apache.solr.client.solrj.response.PivotField;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import org.apache.solr.common.SolrInputDocument;
 import org.apache.solr.common.util.NamedList;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 import java.text.DateFormat;
 import java.util.*;
 
 public class SolrManager {
 	
 	private static Logger LOG = LoggerFactory.getLogger(SolrManager.class);
     public static SolrServer solrServer = new HttpSolrServer("http://localhost:8983/solr");
     public static int minCount = 100;
     public void insertDocument(ResultSet result) throws Exception {
         try {
             SolrInputDocument inputDocument = new SolrInputDocument();
             inputDocument.addField("src", result.getString("src"));
             inputDocument.addField("url", result.getString("url"));
             inputDocument.addField("id", result.getString("id"));
             inputDocument.addField("title_md5", result.getString("title_md5"));
             inputDocument.addField("title", result.getString("title"));
             inputDocument.addField("date", result.getDate("publishedDate"));
             inputDocument.addField("image_url", result.getString("imageUrl"));
             inputDocument.addField("content", result.getString("content"));
             inputDocument.addField("author", result.getString("author"));
             inputDocument.addField("category", result.getString("category"));
             inputDocument.addField("comments", result.getString("comments"));
             inputDocument.addField("country", result.getString("country"));
             inputDocument.addField("city", result.getString("city"));
             inputDocument.addField("commentcount", result.getInt("commentCount"));
             inputDocument.addField("keywords", result.getString("keywords"));
             solrServer.add(inputDocument);
             solrServer.commit();
         } catch (Exception e) {
            LOG.error("Exception in adding document" + (String)result.getString("url"));
             throw e;
         }
     }
 
     public void insertFraudyDocument(String url, String content, Date date) {
         try {
             SolrInputDocument inputDocument = new SolrInputDocument();
             inputDocument.addField("src", "dummysrc");
             inputDocument.addField("url", url);
             inputDocument.addField("id", url);
             inputDocument.addField("title_md5", url);
             inputDocument.addField("title", "dummytitle");
             inputDocument.addField("date", date);
             inputDocument.addField("image_url", "dummy_image_url");
             inputDocument.addField("content", content);
             inputDocument.addField("author", "dummy_author");
             inputDocument.addField("category", "dummy_category");
             inputDocument.addField("comments", "dummy_comments");
             inputDocument.addField("country", "dummy_country");
             inputDocument.addField("city", "dummy_city");
             inputDocument.addField("commentcount", 55);
             solrServer.add(inputDocument);
             solrServer.commit();
             System.out.println("Added a doc");
         } catch (Exception e) {
             System.err.println("Exception aa gayi baap. Ab kya hoga ??");
             e.printStackTrace();
         }
     }
 
 
     private SolrQuery getQueryForKeywords(String keywords, int start, int count) {
     	SolrQuery solrQuery =  new SolrQuery();
     	if(!StringUtils.isBlank(keywords)){
     		solrQuery.setQuery("content:" + keywords + " OR title:" + keywords + " OR keywords:" + keywords);
     	}
     	solrQuery.setStart(start);
     	if(count == 0){
     		count = minCount;
     	}
     	solrQuery.setRows(count);
     	return solrQuery;
     }
 
     private void addSrcQuery(String src ,  SolrQuery solrQuery){
     	if(StringUtils.isBlank(src)) return;
     	String queryString = "src:" + src ;
    	if(!StringUtils.isBlank(solrQuery.getQuery())){
    		queryString = "(" + solrQuery.getQuery() + ") AND " + queryString;
     	}
     	solrQuery.setQuery(queryString);
     }
     
     private void createDateFilter(SolrQuery solrQuery, Date startDate, Date endDate){
     	if(startDate == null || endDate == null) return;
     	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
         df.setTimeZone(TimeZone.getTimeZone("GMT+0530"));
         String dateQuery = "date:" + "[" + df.format(startDate) + " TO " + df.format(endDate) + "]";
         solrQuery.addFilterQuery(dateQuery);
     }
 
     public List<Article> getArticlesForSolrQuery(String query, int start, int count) throws SolrServerException {
     	SolrQuery solrQuery = new SolrQuery();
     	solrQuery.setQuery(query);
     	solrQuery.setStart(start);
     	if(count == 0){
     		count = minCount;
     	}
     	solrQuery.setRows(count);
         QueryResponse response = solrServer.query(solrQuery);
         return getArticles(response);
     }
     
     public List<Article> getArticlesForKeywords(String keywords, String src, int start, int count) throws SolrServerException {
     	SolrQuery solrQuery = getQueryForKeywords(keywords, start, count);
     	addSrcQuery(src, solrQuery);
         QueryResponse response = solrServer.query(solrQuery);
         return getArticles(response);
     }
 
     public List<Article> getArticlesForKeywords(String keywords, Date startDate, Date endDate, String src,  int start, int count) throws SolrServerException {
     	SolrQuery solrQuery = getQueryForKeywords(keywords, start, count);
     	createDateFilter(solrQuery,startDate,endDate);
     	addSrcQuery(src, solrQuery);
         QueryResponse response = solrServer.query(solrQuery);
         return getArticles(response);
     }
 
     private List<Article> getArticles(QueryResponse response) {
         List<Article> result = new ArrayList<Article>();
         SolrDocumentList object = (SolrDocumentList) response.getResponse().get("response");
         for (SolrDocument doc : object) {
             Article article = Article.getArticleFrom(doc);
             result.add(article);
         }
         return result;
     }
 
     public ArticleCount getNumArticlesForKeywordsAndDate(String keywords, Date startDate, Date endDate, String src,  int start, int count) throws SolrServerException {
         SolrQuery solrQuery = getQueryForKeywords(keywords, start, count);
         createDateFilter(solrQuery,startDate,endDate);
         addSrcQuery(src, solrQuery);
         solrQuery.setFacet(true);
         solrQuery.setRows(0);
         solrQuery.set("facet.method", "enum");
         solrQuery.set("facet.limit", "-1");
         solrQuery.addFacetPivotField("date,src");
         solrQuery.addFacetPivotField("src,date");
 
         QueryResponse response = solrServer.query(solrQuery);
         NamedList<List<PivotField>> pivots = response.getFacetPivot();
         List<PivotField> dateSource = pivots.get("date,src");
 
         ArticleCount artCountRes  = new ArticleCount();
         Map<String, Map<String, Integer>> dateResults = new TreeMap<String, Map<String, Integer>>();
         for (PivotField dateOnly : dateSource) {
             String dateString = String.valueOf(((Date) dateOnly.getValue()).getTime());
             if (dateResults.get(dateString) == null ) dateResults.put(dateString, new TreeMap<String, Integer>());
             for (PivotField source: dateOnly.getPivot()) {
             	dateResults.get(dateString).put((String) source.getValue(), source.getCount());
             }
         }
         
         List<PivotField> sourceDate = pivots.get("src,date");
         Map<String, Map<String, Integer>> srcResults = new TreeMap<String, Map<String, Integer>>();
         for (PivotField sourceOnly : sourceDate) {
             String sourceString = (String) sourceOnly.getValue();
             if (srcResults.get(sourceString) == null ) srcResults.put(sourceString, new TreeMap<String, Integer>());
             for (PivotField date: sourceOnly.getPivot()) {
             	srcResults.get(sourceString).put(String.valueOf(((Date) date.getValue()).getTime()), date.getCount());
             }
         }
         artCountRes.setCountByDate(dateResults);
         artCountRes.setCountBySrc(srcResults);
         return artCountRes;
     }
 }
