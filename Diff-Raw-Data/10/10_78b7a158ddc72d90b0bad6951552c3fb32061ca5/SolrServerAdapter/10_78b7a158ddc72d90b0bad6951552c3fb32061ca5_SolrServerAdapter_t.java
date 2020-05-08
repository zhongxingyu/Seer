 package org.sakaiproject.search.solr.util;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.solr.client.solrj.SolrRequest;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.StreamingResponseCallback;
 import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
 import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.client.solrj.response.SolrPingResponse;
 import org.apache.solr.client.solrj.response.UpdateResponse;
 import org.apache.solr.common.SolrInputDocument;
 import org.apache.solr.common.params.SolrParams;
 import org.apache.solr.common.util.NamedList;
 import org.apache.solr.core.CoreContainer;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * SolrServerAdapter allows to generate a SolrServer object on the fly depending on the configuration in
  * sakai.properties.
  * <p>
  * By default an embedded server will be spawned, otherwise, if search.solr.server is set in sakai.properties,
  * a client for that server will be created.
  * </p>
  *
  * @author Colin Hebert
  */
 public class SolrServerAdapter extends SolrServer {
     private static final String CORE_NAME = "search";
     private static final String SOLR_HOME_PROPERTY = "solr.solr.home";
     private static final String SOLR_CONFIGURATION_PATH = ServerConfigurationService.getSakaiHomePath() + "solr/";
     private static final String SOLR_NODE_PATH = SOLR_CONFIGURATION_PATH + "search/conf/";
     private static final Logger logger = LoggerFactory.getLogger(SolrServerAdapter.class);
     private SolrServer instance;
 
    /**
     * Sets up an actual SolrServer, embedded or external depending on the configuration.
     */
     public void init() {
         String serverUrl = ServerConfigurationService.getString("search.solr.server");
         if (!serverUrl.isEmpty()) {
             logger.info("The Solr server is set up");
             instance = new HttpSolrServer(serverUrl);
         } else {
             logger.info("The Solr server isn't set up, using an embedded one");
             if (!isConfigurationPresent()) {
                 try {
                     createDefaultConfiguration();
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             }
 
             ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
             Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
             System.setProperty(SOLR_HOME_PROPERTY, SOLR_CONFIGURATION_PATH);
             CoreContainer coreContainer = new CoreContainer.Initializer().initialize();
             instance = new EmbeddedSolrServer(coreContainer, CORE_NAME);
             Thread.currentThread().setContextClassLoader(currentClassLoader);
         }
     }
 
     /**
      * Copies the default solr configuration in Sakai_home in order to use an embedded solr instance.
      *
      * @throws IOException
      */
     private void createDefaultConfiguration() throws IOException {
         logger.info("Setting up the embedded solr server for the first time");
 
         File solrXmlFile = new File(SOLR_CONFIGURATION_PATH + "solr.xml");
         if (logger.isDebugEnabled())
             logger.debug("Copying '" + solrXmlFile.getPath() + "'");
         solrXmlFile.createNewFile();
         IOUtils.copy(
                 SolrServerAdapter.class.getResourceAsStream("/org/sakaiproject/search/solr/conf/solr.xml"),
                 new FileOutputStream(solrXmlFile));
 
         File solrConfigFile = new File(SOLR_NODE_PATH + "solrconfig.xml");
         if (logger.isDebugEnabled())
             logger.debug("Copying '" + solrConfigFile.getPath() + "'");
         solrConfigFile.createNewFile();
         IOUtils.copy(
                 SolrServerAdapter.class.getResourceAsStream("/org/sakaiproject/search/solr/conf/search/solrconfig.xml"),
                 new FileOutputStream(solrConfigFile));
 
         File schemaFile = new File(SOLR_NODE_PATH + "schema.xml");
         if (logger.isDebugEnabled())
             logger.debug("Copying '" + schemaFile.getPath() + "'");
         schemaFile.createNewFile();
         IOUtils.copy(
                 SolrServerAdapter.class.getResourceAsStream("/org/sakaiproject/search/solr/conf/search/schema.xml"),
                 new FileOutputStream(schemaFile));
     }
 
     /**
      * Checks whether the solr configuration is already in Sakai_home or not.
      *
      * @return true if the configuration is already there, false otherwise.
      */
     private boolean isConfigurationPresent() {
         File nodeConfigDir = new File(SOLR_NODE_PATH);
 
         //Check and create the directory if it isn't available
         logger.error("Creating dirs '" + SOLR_CONFIGURATION_PATH + "'");
         if (!nodeConfigDir.exists() && !nodeConfigDir.mkdirs() || !nodeConfigDir.isDirectory()) {
             throw new IllegalStateException("The solr configuration directory '" + SOLR_CONFIGURATION_PATH + "' "
                     + "couldn't be created");
         }
 
         File solrXmlFile = new File(SOLR_CONFIGURATION_PATH + "solr.xml");
         File solrConfigFile = new File(SOLR_NODE_PATH + "solrconfig.xml");
         File solrSchemaFile = new File(SOLR_NODE_PATH + "schema.xml");
 
         return solrXmlFile.exists() && solrConfigFile.exists() && solrSchemaFile.exists();
     }
 
     @Override
     public UpdateResponse add(Collection<SolrInputDocument> docs) throws SolrServerException, IOException {
         return instance.add(docs);
     }
 
     @Override
     public UpdateResponse add(Collection<SolrInputDocument> docs, int commitWithinMs)
             throws SolrServerException, IOException {
         return instance.add(docs, commitWithinMs);
     }
 
     @Override
     public UpdateResponse addBeans(Collection<?> beans) throws SolrServerException, IOException {
         return instance.addBeans(beans);
     }
 
     @Override
     public UpdateResponse addBeans(Collection<?> beans, int commitWithinMs) throws SolrServerException, IOException {
         return instance.addBeans(beans, commitWithinMs);
     }
 
     @Override
     public UpdateResponse add(SolrInputDocument doc) throws SolrServerException, IOException {
         return instance.add(doc);
     }
 
     @Override
     public UpdateResponse add(SolrInputDocument doc, int commitWithinMs) throws SolrServerException, IOException {
         return instance.add(doc, commitWithinMs);
     }
 
     @Override
     public UpdateResponse addBean(Object obj) throws IOException, SolrServerException {
         return instance.addBean(obj);
     }
 
     @Override
     public UpdateResponse addBean(Object obj, int commitWithinMs) throws IOException, SolrServerException {
         return instance.addBean(obj, commitWithinMs);
     }
 
     @Override
     public UpdateResponse commit() throws SolrServerException, IOException {
         return instance.commit();
     }
 
     @Override
     public UpdateResponse optimize() throws SolrServerException, IOException {
         return instance.optimize();
     }
 
     @Override
     public UpdateResponse commit(boolean waitFlush, boolean waitSearcher) throws SolrServerException, IOException {
         return instance.commit(waitFlush, waitSearcher);
     }
 
     @Override
     public UpdateResponse commit(boolean waitFlush, boolean waitSearcher, boolean softCommit)
             throws SolrServerException, IOException {
         return instance.commit(waitFlush, waitSearcher, softCommit);
     }
 
     @Override
     public UpdateResponse optimize(boolean waitFlush, boolean waitSearcher) throws SolrServerException, IOException {
         return instance.optimize(waitFlush, waitSearcher);
     }
 
     @Override
     public UpdateResponse optimize(boolean waitFlush, boolean waitSearcher, int maxSegments)
             throws SolrServerException, IOException {
         return instance.optimize(waitFlush, waitSearcher, maxSegments);
     }
 
     @Override
     public UpdateResponse rollback() throws SolrServerException, IOException {
         return instance.rollback();
     }
 
     @Override
     public UpdateResponse deleteById(String id) throws SolrServerException, IOException {
         return instance.deleteById(id);
     }
 
     @Override
     public UpdateResponse deleteById(String id, int commitWithinMs) throws SolrServerException, IOException {
         return instance.deleteById(id, commitWithinMs);
     }
 
     @Override
     public UpdateResponse deleteById(List<String> ids) throws SolrServerException, IOException {
         return instance.deleteById(ids);
     }
 
     @Override
     public UpdateResponse deleteById(List<String> ids, int commitWithinMs) throws SolrServerException, IOException {
         return instance.deleteById(ids, commitWithinMs);
     }
 
     @Override
     public UpdateResponse deleteByQuery(String query) throws SolrServerException, IOException {
         return instance.deleteByQuery(query);
     }
 
     @Override
     public UpdateResponse deleteByQuery(String query, int commitWithinMs) throws SolrServerException, IOException {
         return instance.deleteByQuery(query, commitWithinMs);
     }
 
     @Override
     public SolrPingResponse ping() throws SolrServerException, IOException {
         return instance.ping();
     }
 
     @Override
     public QueryResponse query(SolrParams params) throws SolrServerException {
         return instance.query(params);
     }
 
     @Override
     public QueryResponse query(SolrParams params, SolrRequest.METHOD method) throws SolrServerException {
         return instance.query(params, method);
     }
 
     @Override
     public QueryResponse queryAndStreamResponse(SolrParams params, StreamingResponseCallback callback)
             throws SolrServerException, IOException {
         return instance.queryAndStreamResponse(params, callback);
     }
 
     @Override
     public NamedList<Object> request(SolrRequest solrRequest) throws SolrServerException, IOException {
         return instance.request(solrRequest);
     }
 
     @Override
     public DocumentObjectBinder getBinder() {
         return instance.getBinder();
     }
 
     @Override
     public void shutdown() {
         instance.shutdown();
     }
 }
