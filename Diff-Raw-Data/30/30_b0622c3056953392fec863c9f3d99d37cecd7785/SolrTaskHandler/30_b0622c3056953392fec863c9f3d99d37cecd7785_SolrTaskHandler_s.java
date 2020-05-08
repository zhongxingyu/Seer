 package uk.ac.ox.oucs.search.solr.indexing;
 
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.util.ClientUtils;
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.search.api.EntityContentProducer;
 import org.sakaiproject.search.api.SearchService;
 import org.sakaiproject.thread_local.api.ThreadLocalManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.ObjectFactory;
 import uk.ac.ox.oucs.search.indexing.Task;
 import uk.ac.ox.oucs.search.indexing.TaskHandler;
 import uk.ac.ox.oucs.search.indexing.exception.TaskHandlingException;
 import uk.ac.ox.oucs.search.indexing.exception.TemporaryTaskHandlingException;
 import uk.ac.ox.oucs.search.producer.ContentProducerFactory;
 import uk.ac.ox.oucs.search.queueing.DefaultTask;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Queue;
 
 import static uk.ac.ox.oucs.search.queueing.DefaultTask.Type.*;
import static uk.ac.ox.oucs.search.solr.indexing.SolrTask.Type.OPTIMISE_INDEX;
import static uk.ac.ox.oucs.search.solr.indexing.SolrTask.Type.REMOVE_ALL_DOCUMENTS;
import static uk.ac.ox.oucs.search.solr.indexing.SolrTask.Type.REMOVE_SITE_DOCUMENTS;
 
 /**
  * @author Colin Hebert
  */
 public class SolrTaskHandler implements TaskHandler {
     private static final Logger logger = LoggerFactory.getLogger(SolrTaskHandler.class);
     private ContentProducerFactory contentProducerFactory;
     private ObjectFactory solrServerFactory;
     private SolrTools solrTools;
 
     @Override
     public void executeTask(Task task) {
        logger.debug("Attempt to handle '" + task+"'");
         try {
             String taskType = task.getType();
             SolrServer solrServer = (SolrServer) solrServerFactory.getObject();
 
             if (INDEX_DOCUMENT.getTypeName().equals(taskType)) {
                 indexDocument(task.getProperty(DefaultTask.RESOURCE_NAME), task.getCreationDate(), solrServer);
             } else if (REMOVE_DOCUMENT.getTypeName().equals(taskType)) {
                 removeDocument(task.getProperty(DefaultTask.RESOURCE_NAME), task.getCreationDate(), solrServer);
             } else if (INDEX_SITE.getTypeName().equals(taskType)) {
                 indexSite(task.getProperty(DefaultTask.SITE_ID), task.getCreationDate(), solrServer);
             } else if (REFRESH_SITE.getTypeName().equals(taskType)) {
                 refreshSite(task.getProperty(DefaultTask.SITE_ID), task.getCreationDate(), solrServer);
             } else if (INDEX_ALL.getTypeName().equals(taskType)) {
                 indexAll(task.getCreationDate(), solrServer);
             } else if (REFRESH_ALL.getTypeName().equals(taskType)) {
                 refreshAll(task.getCreationDate(), solrServer);
             } else if (REMOVE_SITE_DOCUMENTS.getTypeName().equals(taskType)) {
                 removeSiteDocuments(task.getProperty(DefaultTask.SITE_ID), task.getCreationDate(), solrServer);
             } else if (REMOVE_ALL_DOCUMENTS.getTypeName().equals(taskType)) {
                 removeAllDocuments(task.getCreationDate(), solrServer);
             } else if (OPTIMISE_INDEX.getTypeName().equals(taskType)) {
                 optimiseSolrIndex(solrServer);
             } else {
                 throw new TaskHandlingException("Task '" + task + "' can't be handled");
             }
             solrServer.commit();
         } catch (TaskHandlingException e) {
             throw e;
         } catch (IOException e) {
             throw new TemporaryTaskHandlingException("Couldn't execute the task '" + task + "'", e);
         } catch (Exception e) {
             throw new TaskHandlingException("Couldn't execute the task '" + task + "'", e);
         }
     }
 
     public void indexDocument(String resourceName, Date actionDate, SolrServer solrServer) {
         logger.debug("Add '" + resourceName + "' to the index");
         EntityContentProducer contentProducer = contentProducerFactory.getContentProducerForElement(resourceName);
         if (!solrTools.isDocumentOutdated(contentProducer.getId(resourceName), actionDate)) {
             logger.debug("Indexation not useful as the document was updated earlier");
             return;
         }
 
         try {
             solrServer.request(SolrTools.toSolrRequest(resourceName, actionDate, contentProducer));
         } catch (IOException e) {
             throw new TemporaryTaskHandlingException("An exception occurred while indexing the document '" + resourceName + "'", e);
         } catch (Exception e) {
             throw new TaskHandlingException("An exception occurred while indexing the document '" + resourceName + "'", e);
         }
     }
 
     public void removeDocument(String resourceName, Date actionDate, SolrServer solrServer) {
         EntityContentProducer contentProducer = contentProducerFactory.getContentProducerForElement(resourceName);
         logger.debug("Remove '" + resourceName + "' from the index");
         try {
             solrServer.deleteByQuery(
                     SearchService.DATE_STAMP + ":{* TO " + solrTools.format(actionDate) + "} AND " +
                             SearchService.FIELD_ID + ":" + ClientUtils.escapeQueryChars(contentProducer.getId(resourceName)));
         } catch (IOException e) {
             throw new TemporaryTaskHandlingException("An exception occurred while removing the document '" + resourceName + "'", e);
         } catch (Exception e) {
             throw new TaskHandlingException("An exception occurred while removing the document '" + resourceName + "'", e);
         }
     }
 
     public void indexSite(final String siteId, Date actionDate, SolrServer solrServer) {
         logger.info("Rebuilding the index for '" + siteId + "'");
         try {
             Queue<String> siteReferences = solrTools.getSiteDocumentsReferences(siteId);
             while (siteReferences.peek() != null)
                 indexDocument(siteReferences.poll(), actionDate, solrServer);
             removeSiteDocuments(siteId, actionDate, solrServer);
         } finally {
             //Clean up the localThread after each site
             ThreadLocalManager threadLocalManager = (ThreadLocalManager) ComponentManager.get(ThreadLocalManager.class);
             threadLocalManager.clear();
         }
     }
 
     public void refreshSite(String siteId, Date actionDate, SolrServer solrServer) {
         logger.info("Refreshing the index for '" + siteId + "'");
         try {
             //Get the currently indexed resources for this site
             Queue<String> resourceNames = solrTools.getResourceNames(siteId);
             logger.debug(resourceNames.size() + " elements will be refreshed");
             while (!resourceNames.isEmpty()) {
                 indexDocument(resourceNames.poll(), actionDate, solrServer);
             }
             removeSiteDocuments(siteId, actionDate, solrServer);
         } finally {
             //Clean up the localThread after each site
             ThreadLocalManager threadLocalManager = (ThreadLocalManager) ComponentManager.get(ThreadLocalManager.class);
             threadLocalManager.clear();
         }
     }
 
     public void indexAll(Date actionDate, SolrServer solrServer) {
         logger.info("Rebuilding the index for every indexable site");
         Queue<String> reindexedSites = solrTools.getIndexableSites();
         while (!reindexedSites.isEmpty()) {
             indexSite(reindexedSites.poll(), actionDate, solrServer);
         }
         removeAllDocuments(actionDate, solrServer);
     }
 
     public void refreshAll(Date actionDate, SolrServer solrServer) {
         logger.info("Refreshing the index for every indexable site");
         Queue<String> refreshedSites = solrTools.getIndexableSites();
         while (!refreshedSites.isEmpty()) {
             refreshSite(refreshedSites.poll(), actionDate, solrServer);
         }
         removeAllDocuments(actionDate, solrServer);
     }
 
     public void removeSiteDocuments(String siteId, Date creationDate, SolrServer solrServer) {
         logger.info("Remove old documents from '" + siteId + "'");
         try {
             solrServer.deleteByQuery(
                     SearchService.DATE_STAMP + ":{* TO " + solrTools.format(creationDate) + "} AND " +
                             SearchService.FIELD_SITEID + ":" + ClientUtils.escapeQueryChars(siteId));
         } catch (IOException e) {
             throw new TemporaryTaskHandlingException("Couldn't remove old documents the site '" + siteId + "'", e);
         } catch (Exception e) {
             throw new TaskHandlingException("Couldn't remove old documents the site '" + siteId + "'", e);
         }
     }
 
     public void removeAllDocuments(Date creationDate, SolrServer solrServer) {
         logger.info("Remove old documents from every sites");
         try {
             solrServer.deleteByQuery(SearchService.DATE_STAMP + ":{* TO " + solrTools.format(creationDate) + "}");
         } catch (IOException e) {
             throw new TemporaryTaskHandlingException("Couldn't remove old documents from the entire instance", e);
         } catch (Exception e) {
             throw new TaskHandlingException("Couldn't refresh the entire instance", e);
         }
     }
 
     public void optimiseSolrIndex(SolrServer solrServer) {
         try {
             solrServer.optimize();
         } catch (IOException e) {
             throw new TemporaryTaskHandlingException("Couldn't optimise the index", e);
         } catch (Exception e) {
             throw new TaskHandlingException("Couldn't optimise the index", e);
         }
     }
 
     public void setContentProducerFactory(ContentProducerFactory contentProducerFactory) {
         this.contentProducerFactory = contentProducerFactory;
     }
 
     public void setSolrServerFactory(ObjectFactory solrServerFactory) {
         this.solrServerFactory = solrServerFactory;
     }
 
     public void setSolrTools(SolrTools solrTools) {
         this.solrTools = solrTools;
     }
 }
