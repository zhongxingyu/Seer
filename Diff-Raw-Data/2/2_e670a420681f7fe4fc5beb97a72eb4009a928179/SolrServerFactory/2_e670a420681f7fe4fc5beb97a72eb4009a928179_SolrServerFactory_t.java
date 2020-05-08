 package org.sakaiproject.search.solr.util;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
 import org.apache.solr.core.CoreContainer;
 import org.sakaiproject.component.api.ServerConfigurationService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 /**
  * This class allows us to have 2 spring beans, one for indexing and one for queries with different settings
  * on each, but when running in embedded mode we actually have the same instance.
  *
  * @author Matthew Buckett
  */
 public class SolrServerFactory {
 
     private static final String CORE_NAME = "search";
     private static final String SOLR_HOME_PROPERTY = "solr.solr.home";
     private static final String SOLR_CONFIGURATION_CLASSPATH = "/org/sakaiproject/search/solr/conf/";
     private static final int HTTP_SERVER_TIMEOUT = 10000;
     private static final int HTTP_CONNECT_TIMEOUT = 5000;
     private static final Logger logger = LoggerFactory.getLogger(SolrServerFactory.class);
 
     private SolrServer indexingInstance;
     private SolrServer lookupInstance;
     private ServerConfigurationService serverConfigurationService;
 
     public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
         this.serverConfigurationService = serverConfigurationService;
     }
     /**
      * Sets up an actual SolrServer, embedded or external depending on the configuration.
      * This needs the Sakai component manager to have started up.
      */
     public void init() {
         String serverUrl = serverConfigurationService.getString("search.solr.server");
         if (!serverUrl.isEmpty()) {
             indexingInstance = createHttpSolrServer(serverUrl, 0);
             lookupInstance = createHttpSolrServer(serverUrl, HTTP_SERVER_TIMEOUT);
             logger.info("The Solr server is set up to: " + serverUrl);
         } else {
             logger.info("The Solr server isn't set up, using an embedded one");
             String solrConfigPath = serverConfigurationService.getSakaiHomePath() + "solr/";
 
             if (!new File(solrConfigPath).exists())
                 createDefaultConfiguration(solrConfigPath);
 
             ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
             Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
             System.setProperty(SOLR_HOME_PROPERTY, solrConfigPath);
             try {
                 CoreContainer coreContainer = new CoreContainer.Initializer().initialize();
                 indexingInstance = new EmbeddedSolrServer(coreContainer, CORE_NAME);
                 lookupInstance = indexingInstance;
                 Thread.currentThread().setContextClassLoader(currentClassLoader);
             } catch (FileNotFoundException e) {
                 throw new IllegalStateException("Couldn't create an embedded instance of solr");
             }
         }
     }
 
     /**
      * Creates a solr server.
      * @param serverUrl The URL to the server.
      * @param timeout The timeout for socket reads
      * @return The HTTP Solr server.
      */
     private HttpSolrServer createHttpSolrServer(String serverUrl, int timeout) {
         HttpSolrServer httpSolrServer = new HttpSolrServer(serverUrl);
         httpSolrServer.setConnectionTimeout(HTTP_CONNECT_TIMEOUT);
         httpSolrServer.setSoTimeout(timeout);
         return httpSolrServer;
     }
 
     /**
      * Copies the default solr configuration in Sakai_home in order to use an embedded solr instance.
      */
     private void createDefaultConfiguration(String solrConfigPath) {
         logger.info("Setting up the embedded solr server for the first time");
         copyFromClassPathToSolrHome(solrConfigPath, "solr.xml");
         copyFromClassPathToSolrHome(solrConfigPath, "search/conf/solrconfig.xml");
         copyFromClassPathToSolrHome(solrConfigPath, "search/conf/schema.xml");
         copyFromClassPathToSolrHome(solrConfigPath, "search/conf/lang/stopwords_en.xml");
     }
 
     /**
      * Copies a solr configuration file from the classpath to the solr configuration path.
      *
      * @param fileToCopy relative path of the file to copy.
      */
     private void copyFromClassPathToSolrHome(String solrConfigPath, String fileToCopy) {
         File destinationFile = new File(solrConfigPath + fileToCopy);
         logger.debug("Copying '{}' to '{}'", fileToCopy, destinationFile.getPath());
 
         try {
             destinationFile.getParentFile().mkdirs();
             destinationFile.createNewFile();
 
             IOUtils.copy(SolrServerFactory.class.getResourceAsStream(SOLR_CONFIGURATION_CLASSPATH + fileToCopy),
                     new FileOutputStream(destinationFile));
         } catch (IOException e) {
             logger.error("Couldn't copy '{}' to '{}'", fileToCopy, destinationFile.getPath(), e);
         }
     }
 
 
     /**
      * Returns a Solr Server which should be used for indexing new content (making updates).
      * @return A SolrServer.
      */
     public SolrServer createIndexing() {
         return indexingInstance;
     }
 
     /**
      * Returns a Solr Server which should be used for searching content.
     * @return A SolrServer.
      */
     public SolrServer createLookup() {
         return lookupInstance;
     }
 }
