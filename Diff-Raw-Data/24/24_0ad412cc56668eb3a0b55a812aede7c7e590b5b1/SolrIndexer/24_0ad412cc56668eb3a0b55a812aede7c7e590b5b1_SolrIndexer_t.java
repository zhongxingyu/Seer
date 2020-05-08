 /*
  *  Copyright 2012 Hippo.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.onehippo.forge.solr.indexer.task;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import javax.jcr.Credentials;
 import javax.jcr.Repository;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 
 import org.apache.commons.lang.mutable.MutableBoolean;
 import org.apache.commons.lang.mutable.MutableInt;
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
 import org.apache.solr.client.solrj.response.SolrPingResponse;
 import org.apache.solr.common.SolrException;
 import org.apache.solr.common.SolrInputDocument;
 import org.hippoecm.hst.site.HstServices;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.util.Assert;
 
 /**
  * Solr indexer: reads document from the JCR repository and index them into a Solr server
  * @author Mickaël Tricot
  * @version $Id: SolrIndexer.java 129982 2012-02-06 22:12:13Z mtricot $
  */
 public final class SolrIndexer implements InitializingBean {
 
     /** Logger */
     private static final Logger log = LoggerFactory.getLogger(SolrIndexer.class);
 
     /** Query for all Solr entries */
     private static final String QUERY_ALL = "*:*";
 
     /**
      * Create a JCR session
     * @param logError Log ERROR if the session cannot be created (otherwise INFO)
      * @return Session (nullable)
      */
    private static Session createSession(boolean logError) {
         Session session = null;
         if (HstServices.isAvailable()) {
             try {
                 Repository repository = HstServices.getComponentManager().getComponent(Repository.class.getName());
                 Credentials credentials =
                         HstServices.getComponentManager().getComponent(Credentials.class.getName() + ".default");
                 session = repository.login(credentials);
             } catch (RepositoryException e) {
                if (logError) {
                    log.error("Cannot create a JCR session", e);
                } else {
                    log.info("Cannot create a JCR session (yet): {}", e.getMessage());
                }
             }
         } else {
            if (logError) {
                log.error("Cannot create a JCR session because HST services are not available.");
            } else {
                log.info("Cannot create a JCR session (yet) because HST services are not available.");
            }
         }
         return session;
     }
 
     /** Wait for the repository to be initialized */
     private static void waitForRepository() {
         Session session = null;
         try {
             while (session == null) {
                session = createSession(false);
                 if (session == null) {
                     log.debug("Repository is not ready yet, wait for 1 more minute");
                     try {
                         Thread.sleep(TimeUnit.MINUTES.toMillis(1L));
                     } catch (InterruptedException e) {
                         //
                     }
                 }
             }
         } finally {
             JcrUtils.closeQuietly(session);
         }
     }
 
     /** Solr runtime exception, for wrapping checked exceptions */
     private static final class SolrRuntimeException extends RuntimeException {
 
         /**
          * Constructor
          * @param e Cause
          */
         private SolrRuntimeException(Exception e) {
             super(e);
         }
     }
 
     /**
      * Flag indicating if an error was intercepted (shared object, because the StreamingUpdateSolrServer
      * implementation is multi-threaded and does not throw directly exceptions)
      */
     private final MutableBoolean errorIntercepted;
 
     /** Queue size */
     private final int queueSize;
 
     /** Server instance */
     private final SolrServer server;
 
     /** Server URL */
     private final String serverUrl;
 
     /** Solr filter properties to index (key = Solr ID, value = JCR property name) */
     private Map<String, String> solrFilterProperties;
 
     /**
      * Constructor
      * @param serverUrl server URL
      * @param queueSize Queue size
      * @param solrFilterProperties Solr filter properties to index (key = Solr ID, value = JCR property name)
      */
     public SolrIndexer(String serverUrl, int queueSize, Map<String, String> solrFilterProperties) {
         errorIntercepted = new MutableBoolean();
         server = createServer(serverUrl, queueSize);
         this.serverUrl = serverUrl;
         this.queueSize = queueSize;
         this.solrFilterProperties = SolrConfiguration.validateSolrFilterProperties(solrFilterProperties);
     }
 
     /** Initialize index if empty */
     @Override
     public void afterPropertiesSet() {
         new Thread(new Runnable() {
             /** {@inheritDoc} */
             @Override
             public void run() {
                 waitForRepository();
                 synchronized (server) {
                     try {
                         if (server.query(new SolrQuery(QUERY_ALL).setRows(1)).getResults().isEmpty()) {
                             log.info("Solr index is empty. Indexation needed...");
                             index();
                         }
                     } catch (SolrServerException e) {
                         log.error("Failed to check if the Solr index was empty", e);
                     }
                 }
             }
         }).start();
     }
 
     /**
      * Create server instance
      * @param serverUrl Server URL
      * @param queueSize Queue size
      * @return Server instance
      */
     private SolrServer createServer(String serverUrl, int queueSize) {
         Assert.notNull(serverUrl, "serverUrl must be not null");
         Assert.isTrue(!serverUrl.isEmpty(), "serverUrl must be not empty");
         Assert.isTrue(queueSize > 0, "queueSize must be positive: " + queueSize);
 
         StreamingUpdateSolrServer s;
         try {
             s = new StreamingUpdateSolrServer(serverUrl, queueSize, 1) {
                 /** {@inheritDoc} */
                 @Override
                 public void handleError(Throwable throwable) {
                     log.error("Error intercepted, check Solr logs for more details: {}", throwable.getMessage());
                     errorIntercepted.setValue(true);
                 }
             };
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException("serverUrl is malformed: " + serverUrl, e);
         }
         return s;
     }
 
     /**
      * Handle rollback exception
      * @param exception Exception
      */
     private void handleRollbackException(Exception exception) {
         log.error("Failed to rollback changes at " + serverUrl, exception);
     }
 
     /**
      * Index documents
      * @param documents Documents
      * @param totalDocuments Total documents (will be updated)
      * @throws IOException -
      * @throws SolrServerException -
      */
     private void index(Collection<SolrInputDocument> documents, MutableInt totalDocuments)
             throws IOException, SolrServerException {
         if (!documents.isEmpty()) {
             log.info("Indexing {} documents", documents.size());
             server.add(documents);
             totalDocuments.add(documents.size());
             documents.clear();
         }
     }
 
     /**
      * Rollback changes when an exception occurred.
      * @param exception Exception
      */
     private void rollback(Throwable exception) {
         if (exception != null) {
             log.error("Failed to perform actions. Rolling back.", exception);
         }
         try {
             server.rollback();
         } catch (SolrServerException e) {
             handleRollbackException(e);
         } catch (SolrException e) {
             handleRollbackException(e);
         } catch (IOException e) {
             handleRollbackException(e);
         }
     }
 
     /** Index all documents */
     public void index() {
 
         log.info("Starting Solr indexation in batches of {} documents", queueSize);
         long startTime = System.currentTimeMillis();
         final MutableInt totalDocuments = new MutableInt(0);
 
         synchronized (server) {
 
            Session session = createSession(true);
             if (session == null) {
                 return;
             }
 
             try {
 
                 // Ping Solr server
                 SolrPingResponse ping = server.ping();
                 log.info("Server ping successful [elapsedTime = {}, QTime = {}, status = {}]",
                         new Object[]{ping.getElapsedTime(), ping.getQTime(), ping.getStatus()});
 
                 // Read Solr configuration from repository
                 SolrConfiguration configuration = new SolrConfiguration(session, solrFilterProperties);
                 if (configuration.isNotValid()) {
                     log.error("Indexing skipped because configuration is not valid: {}", configuration);
                     return;
                 }
                 log.info("{}", configuration);
 
                 // Delete current Solr index
                 log.info("Deleting current Solr index");
                 server.deleteByQuery(QUERY_ALL);
 
                 errorIntercepted.setValue(false);
                 final Collection<SolrInputDocument> documentsQueue = new ArrayList<SolrInputDocument>(queueSize);
 
                 // Index documents when queue is full
                 new SolrOcm(configuration).populateDocumentsQueueThenRun(documentsQueue, new Runnable() {
                     /** {@inheritDoc} */
                     @Override
                     public void run() {
                         if (documentsQueue.size() >= queueSize) {
                             try {
                                 index(documentsQueue, totalDocuments);
                             } catch (SolrServerException e) {
                                 throw new SolrRuntimeException(e);
                             } catch (IOException e) {
                                 throw new SolrRuntimeException(e);
                             }
                         }
                     }
                 });
 
                 // Index remaining documents in the queue
                 index(documentsQueue, totalDocuments);
 
                 server.commit();
 
                 // Errors are intercepted when committing. Not sure if the rollback is useful here.
                 if (errorIntercepted.booleanValue()) {
                     log.error("Error intercepted while indexing documents. Rolling back.");
                     rollback(null);
                     return;
                 }
 
             } catch (SolrRuntimeException e) {
                 rollback(e.getCause());
                 return;
             } catch (SolrServerException e) {
                 rollback(e);
                 return;
             } catch (SolrException e) {
                 rollback(e);
                 return;
             } catch (IOException e) {
                 rollback(e);
                 return;
             } finally {
                 JcrUtils.closeQuietly(session);
             }
         }
 
         log.info("{} documents successfully indexed in {} minutes",
                 new Object[]{totalDocuments, TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - startTime)});
     }
 }
