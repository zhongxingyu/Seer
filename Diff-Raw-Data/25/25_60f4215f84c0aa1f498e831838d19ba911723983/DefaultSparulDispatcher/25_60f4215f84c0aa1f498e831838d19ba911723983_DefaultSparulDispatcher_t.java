 package com.computas.sublima.query.impl;
 
 import com.computas.sublima.query.SparulDispatcher;
 import com.computas.sublima.query.service.SettingsService;
 import com.computas.sublima.query.service.CachingService;
 import com.hp.hpl.jena.query.larq.ARQLuceneException;
 import com.hp.hpl.jena.query.larq.LARQ;
 import com.hp.hpl.jena.sparql.ARQNotImplemented;
 import com.hp.hpl.jena.update.*;
 import org.apache.cocoon.configuration.Settings;
 import org.apache.log4j.Logger;
 import net.spy.memcached.MemcachedClient;
 
 /**
  * This component queries RDF triple stores using Sparul. It is threadsafe.
  */
 public class DefaultSparulDispatcher implements SparulDispatcher {
 
   private Settings cocoonSettings;
   private static Logger logger = Logger.getLogger(DefaultSparulDispatcher.class);
 
   public boolean query(String query) {
 
     //Get a GraphStore and load the graph from the Model
     GraphStore graphStore = GraphStoreFactory.create();
     graphStore.setDefaultGraph(SettingsService.getModel().getGraph());
 
     try {
       //Try to execute the updateQuery (SPARQL/Update)
       logger.info("SPARULdispatcher executing.\n" + query + "\n");
       UpdateRequest updateRequest = UpdateFactory.create(query);
       updateRequest.exec(graphStore);
       LARQ.setDefaultIndex(SettingsService.getIndexBuilderNode(null).getIndex());
     } catch (ARQNotImplemented e) {
       logger.warn("DefaultSparulDispatcher.query --> ARQNotImplemented exception. Returning TRUE and flagging reindexing. " + e.getMessage());
       logger.info("SPARULdispatcher executing again because of exception.\n" + query + "\n");
       UpdateRequest updateRequest = UpdateFactory.create(query);
 
       updateRequest.exec(graphStore);
       LARQ.setDefaultIndex(SettingsService.getIndexBuilderNode(null).getIndex());
       return true;
     } catch (UpdateException e) {
       logger.warn("DefaultSparulDispatcher.query --> UpdateException. Returning FALSE. " + e.getMessage());
       e.printStackTrace();
       return false;
     } catch (ARQLuceneException e) {
       logger.warn("DefaultSparulDispatcher.query --> ARQLuceneException. Returning FALSE. " + e.getMessage());
       e.printStackTrace();
       return false;
     }catch (Exception e) {
       logger.warn("DefaultSparulDispatcher.query --> Exception. Returning FALSE. " + e.getMessage());
       e.printStackTrace();
       return false;
     }
 
     logger.debug("DefaultSparulDispatcher invalidates the cache.");  
     CachingService cache = new CachingService();
     MemcachedClient memcached = cache.connect();
     cache.modelChanged(memcached);
    cache.close(memcached);
     System.gc();
     // Return true if update success
     return true;
   }
 
   public void setCocoonSettings(Settings cocoonSettings) {
     this.cocoonSettings = cocoonSettings;
   }
 
 }
