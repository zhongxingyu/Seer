 /*
  * $HeadURL$
  * $Id$
  *
  * Copyright (c) 2006-2010 by Public Library of Science
  * http://plos.org
  * http://ambraproject.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.ambraproject.search.service;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.MalformedURLException;
 
 /**
  * A factory class that holds Solr server instance. It handles situations when Solr server is
  * not configured in ambra.xml
  *
  * @author Dragisa Krsmanovic
  */
 public class SolrServerFactory {
 
   private static final Logger log = LoggerFactory.getLogger(SolrServerFactory.class);
 
   private static final String URL_CONFIG_PARAM = "ambra.services.search.server.url";
 
   private SolrServer server = null;
 
   public SolrServerFactory(Configuration configuration) throws MalformedURLException {
     String serverUrl = configuration.getString(URL_CONFIG_PARAM, null);
     if (serverUrl != null) {
       log.info("Creating SolrServer instance at " + serverUrl);
       server = new CommonsHttpSolrServer(serverUrl);
      ((CommonsHttpSolrServer) server).setConnectionManagerTimeout(60000l);
      ((CommonsHttpSolrServer) server).setSoTimeout(60000);
     } else {
       log.warn(URL_CONFIG_PARAM + " not set. SolrServer instance will not be created.");
     }
   }
 
   //Added for testing
   public SolrServerFactory() {
     log.warn("SolrServer instance will not be created.  No argument constructor should only be used for testing.");
   }
 
   /**
    * Get Solr Server instance. It should be a singleton shared for all queries.
    *
    * @return Solr server. Null if <i>ambra.services.search.server.url</i> is not configured.
    */
   public SolrServer getServer() {
     return server;
   }
 }
