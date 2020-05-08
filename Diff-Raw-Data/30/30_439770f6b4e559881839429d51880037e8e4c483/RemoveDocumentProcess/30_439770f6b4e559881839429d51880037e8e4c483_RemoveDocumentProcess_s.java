 package uk.ac.ox.oucs.search.solr.process;
 
 import org.apache.solr.client.solrj.SolrServer;
 import org.sakaiproject.search.api.EntityContentProducer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Colin Hebert
  */
 public class RemoveDocumentProcess implements SolrProcess {
     private static final Logger logger = LoggerFactory.getLogger(RemoveDocumentProcess.class);
     private final SolrServer solrServer;
     private final EntityContentProducer entityContentProducer;
     private final String resourceName;
     private final boolean commit;
 
     public RemoveDocumentProcess(SolrServer solrServer, EntityContentProducer entityContentProducer, String resourceName) {
         this(solrServer, entityContentProducer, resourceName, true);
     }
 
     public RemoveDocumentProcess(SolrServer solrServer, EntityContentProducer entityContentProducer, String resourceName, boolean commit) {
         this.solrServer = solrServer;
         this.entityContentProducer = entityContentProducer;
         this.resourceName = resourceName;
         this.commit = commit;
     }
 
     @Override
     public void execute() {
         logger.debug("Remove '" + resourceName + "' from the index");
         try {
             solrServer.deleteById(entityContentProducer.getId(resourceName));
             if (commit)
                 solrServer.commit();
         } catch (Exception e) {
            logger.error("Error while removing the document '" + resourceName + "'", e);
         }
     }
 }
