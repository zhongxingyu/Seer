 /**
  * 
  */
 package org.qburst.search.services;
 
 import java.util.HashMap;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import org.apache.solr.common.SolrInputDocument;
 import org.qburst.search.model.Authentication;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.core.env.Environment;
 import org.springframework.stereotype.Repository;
 import org.springframework.stereotype.Service;
 
 /**
  * @author Cyril
  *
  */
 @PropertySource(value = "classpath:application.properties")
 @Service("SolrUtilsService")
 @Repository
 public class SolrUtils implements ISolrUtils{
     @Autowired
     private Environment env;
 
 	public QueryResponse queryBooks(String q) throws Exception{
 		HttpSolrServer solr = new HttpSolrServer(
 				env.getProperty("solr.books"));
 		try{
 			SolrQuery query = new SolrQuery();
 			query.setQuery(q);
 			query.setFields("content", "author", "url", "title", "id");
 			query.setHighlight(true);
 			query.setRows(50);
 			query.addHighlightField("content");
 			query.setHighlightSnippets(1000);
 			query.setHighlightSimplePost("</span>");
 			query.setHighlightSimplePre("<span class='label label-important'>");
 			QueryResponse response = solr.query(query);
 			return response;
 		} finally{
 			solr.shutdown();
 		}
 	}
 
 	public void writeBookMeta(Authentication user, String url) throws Exception{
 		HttpSolrServer solr = new HttpSolrServer(
 				env.getProperty("solr.users"));
 		try{
 			SolrInputDocument doc = new SolrInputDocument();
 			doc.addField( "id", (url.hashCode() + "").replace("-", "_"));
 			doc.addField( "email", user.getEmail() );
 			doc.addField( "firstname", user.getFirstname() );
 			doc.addField( "lastname", user.getLastname() );
 			doc.addField( "fullname", user.getFullname() );
 			doc.addField( "url",  url);
 			solr.add(doc);
 			solr.commit();
 		} finally {
 			solr.shutdown();
 		}
 	}
 	public SolrDocumentList queryUserBooks(Authentication user) throws Exception{
 		HttpSolrServer solrUsers = new HttpSolrServer(env.getProperty("solr.users"));
 		String email = user.getEmail();
 		SolrQuery queryUser = new SolrQuery();
 		queryUser.setQuery("email:" +email);
		queryUser.setRows(1000);
 		QueryResponse responseUser = solrUsers.query(queryUser);
 		SolrDocumentList books = new SolrDocumentList();
 		for(SolrDocument doc :  responseUser.getResults()){
 		   HttpSolrServer solrBooks = new HttpSolrServer(env.getProperty("solr.books"));
 		   SolrQuery queryBooks = new SolrQuery();
 		   queryBooks.setQuery("id:" +doc.getFieldValue("id"));
 		   QueryResponse responseBooks = solrBooks.query(queryBooks);
 		   SolrDocumentList results = responseBooks.getResults();
 		   books.add(results.get(0));
 		}
 		return books;
 	}
 
 	@Override
 	public HashMap<String, String> getUserInfo(String url) throws Exception {
 		HttpSolrServer solr = new HttpSolrServer(
 				env.getProperty("solr.users"));
 		HashMap<String, String> hm = new HashMap<String, String>();
 		try{
 			SolrQuery query = new SolrQuery();
 			query.setQuery(("id:" + url.hashCode()).replace("-", "_"));
 			query.setFields("email", "firstname", "lastname", "fullname");
 			QueryResponse response = solr.query(query);
 			SolrDocumentList results = response.getResults();
 			SolrDocument sd = results.get(0);
 			hm.put("email", sd.getFieldValue("email").toString());
 			hm.put("firstname", sd.getFieldValue("firstname").toString());
 			hm.put("lastname", sd.getFieldValue("lastname").toString());
 			hm.put("fullname", sd.getFieldValue("fullname").toString());
 			return hm;
 		} finally{
 			solr.shutdown();
 		}
 	}
 
 	@Override
 	public String getBookFolder() {
 		return env.getProperty("solr.bookfolder");
 	}
 
 }
