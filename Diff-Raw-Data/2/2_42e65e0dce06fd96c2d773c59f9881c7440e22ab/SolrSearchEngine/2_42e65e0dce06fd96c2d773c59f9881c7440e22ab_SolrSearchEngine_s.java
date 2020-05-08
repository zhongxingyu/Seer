 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.search;
 
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
 import org.apache.solr.common.SolrInputDocument;
 import org.jamwiki.Environment;
 import org.jamwiki.SearchEngine;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.*;
 import org.jamwiki.utils.Pagination;
 import org.jamwiki.utils.WikiLogger;
 
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.List;
 
 /**
  * An implementation of {@link org.jamwiki.SearchEngine} that uses
  * <a href="http://lucene.apache.org/solr/">Apache Solr</a> to perform searches of
  * Wiki content.
  */
 public class SolrSearchEngine implements SearchEngine {
   private static final WikiLogger logger = WikiLogger.getLogger(LuceneSearchEngine.class.getName());
   
   private SolrServer solrServer;
   private boolean autoCommit = true;
 
   public SolrSearchEngine() throws Exception {
    String user = Environment.getValue(Environment.PROP_BASE_SEARCH_SOLR_URL);
     String password = Environment.getValue(Environment.PROP_BASE_SEARCH_SOLR_PASSWORD);
     String url = Environment.getValue(Environment.PROP_BASE_SEARCH_SOLR_URL);        
     
     HttpClient httpclient = new HttpClient(new MultiThreadedHttpConnectionManager());
     solrServer = new CommonsHttpSolrServer(url, httpclient);
     if(!user.isEmpty()) {
       httpclient.getParams().setAuthenticationPreemptive(true);
       Credentials defaultcreds = new UsernamePasswordCredentials(user,password);
       httpclient.getState().setCredentials(AuthScope.ANY, defaultcreds);
 
     }
   }
 
   private void commit(boolean commitNow) throws SolrServerException, IOException {
     if(commitNow) {
       solrServer.commit();
     }
   }
   
   private String genId(Topic topic) {
     return String.format("%s-%d", topic.getVirtualWiki(), topic.getTopicId());
   }
   
   public void addToIndex(Topic topic) {
     if(topic.getTopicType() == TopicType.REDIRECT) {
       return;
     }
     try {
       SolrInputDocument doc = new SolrInputDocument();
       doc.addField("id", genId(topic));
       doc.addField("section", "wiki");
 
       List<RecentChange> changes = WikiBase.getDataHandler().getTopicHistory(topic, new Pagination(1, 0), true);
       if(changes.isEmpty()) {
         doc.addField("user_id", 1);
         doc.addField("topic_user_id", 1);
         doc.addField("postdate", new Timestamp(0));
       } else {
         RecentChange lastChange = changes.get(0);
         doc.addField("user_id", lastChange.getAuthorId());
         doc.addField("topic_user_id", lastChange.getAuthorId());
         doc.addField("postdate", lastChange.getChangeDate());
       }
 
       doc.addField("topic_id", topic.getTopicId());
       doc.addField("group_id", 0);
       doc.addField("title", topic.getName());
       doc.addField("topic_title", topic.getName());
       doc.addField("message", topic.getTopicContent());
       doc.addField("is_comment", false);
       solrServer.add(doc);
       commit(autoCommit);
     } catch (Exception e) {
       logger.error("Exception while adding topic " + topic.getVirtualWiki() + " / " + topic.getName(), e);
     }
   }
   
   public void commit(String virtualWiki) {
     try {
       commit(true);
     } catch (Exception e) {
       logger.error("Exception while committing pending changes for virtual wiki " + virtualWiki, e);
     }
   }
   
   public void deleteFromIndex(Topic topic) {
     try {
       solrServer.deleteById(genId(topic));
       commit(autoCommit);
     } catch (Exception e) {
       logger.error("Exception while remove topic " + topic.getName(), e);
     }
   }
 
   public List<SearchResultEntry> findResults(String virtualWiki, String text, List<Integer> namespaces) {
     return null;
   }
 
   public void refreshIndex() throws Exception {
     solrServer.deleteByQuery("section:wiki");
     List<VirtualWiki> allWikis = WikiBase.getDataHandler().getVirtualWikiList();
     Topic topic;
     for (VirtualWiki virtualWiki : allWikis) {
       List<String> topicNames = WikiBase.getDataHandler().getAllTopicNames(virtualWiki.getName(), false);
       for (String topicName : topicNames) {
         topic = WikiBase.getDataHandler().lookupTopic(virtualWiki.getName(), topicName, false);
         addToIndex(topic);
       }
     }
   }
 
   public void setAutoCommit(boolean autoCommit) {
     this.autoCommit = autoCommit;
   }
 
   public void shutdown() throws IOException {
     logger.debug("Okay");
   }
 
   public void updateInIndex(Topic topic) {
     try {
       deleteFromIndex(topic);
       addToIndex(topic);
       commit(autoCommit);
     } catch (Exception e) {
       logger.error("Exception while updating topic " + topic.getVirtualWiki() + " / " + topic.getName(), e);
     }
   }
 }
