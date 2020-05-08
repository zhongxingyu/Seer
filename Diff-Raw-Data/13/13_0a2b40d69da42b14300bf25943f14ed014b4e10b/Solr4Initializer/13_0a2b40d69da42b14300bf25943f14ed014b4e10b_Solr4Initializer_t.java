 /* Solr32Initializer.java - created on Jun 24, 2011, Copyright (c) 2011 The European Library, all rights reserved */
 package eu.europeana.uim.solr4;
 
 import java.io.File;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
 import org.apache.solr.core.CoreContainer;
 
 import eu.europeana.uim.common.BlockingInitializer;
 
 /**
  * Initializer for solr 4.0-snapshot based indices.
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since Jun 24, 2011
  */
 public class Solr4Initializer extends BlockingInitializer {
     private static final Logger log = Logger.getLogger(Solr4Initializer.class.getName());
 
     private SolrServer          server;
     private CoreContainer       container;
 
     private final String        url;
     private final String        core;
 
     /**
      * Creates a new instance of this class.
      * 
      * @param url
      * @param core
      */
     public Solr4Initializer(String url, String core) {
         this.url = url;
         this.core = core;
     }
 
     /**
      * @return the solr server
      */
     public SolrServer getServer() {
         return server;
     }
 
     /**
      * @return the curren tcontainter
      */
     public CoreContainer getContainer() {
         return container;
     }
 
     @Override
     protected void initializeInternal() {
         try {
             status = STATUS_BOOTING;
             if (url.startsWith("file://")) {
                 File home = new File(url.substring(7));
                if (!new File(home, "solr.xml").exists()) {
                    throw new SolrException(ErrorCode.SERVICE_UNAVAILABLE, "Cannot open server at url '" + url + "'!");
                }
                container = new CoreContainer(home.getAbsolutePath());
                container.load();
                 server = new EmbeddedSolrServer(container, core);
             } else {
                 server = new HttpSolrServer(new URL(url) + core);
             }
             status = STATUS_INITIALIZED;
         } catch (Throwable t) {
             log.log(Level.SEVERE, "Failed to initialize repository at <" + url + ">", t);
             status = STATUS_FAILED;
             throw new RuntimeException("Failed to setup core <" + core + "> at <" + url + ">", t);
         }
     }
 }
