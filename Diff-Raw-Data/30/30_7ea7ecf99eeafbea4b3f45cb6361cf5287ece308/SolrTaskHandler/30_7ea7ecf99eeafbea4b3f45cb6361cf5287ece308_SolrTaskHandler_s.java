 package org.sakaiproject.search.solr.indexing;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.util.ClientUtils;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrException;
 import org.apache.solr.common.SolrInputDocument;
 import org.apache.solr.common.params.SolrParams;
 import org.sakaiproject.search.api.SearchService;
 import org.sakaiproject.search.indexing.DefaultTask;
 import org.sakaiproject.search.indexing.Task;
 import org.sakaiproject.search.indexing.TaskHandler;
 import org.sakaiproject.search.indexing.exception.NestedTaskHandlingException;
 import org.sakaiproject.search.indexing.exception.TaskHandlingException;
 import org.sakaiproject.search.indexing.exception.TemporaryTaskHandlingException;
 import org.sakaiproject.thread_local.api.ThreadLocalManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Queue;
 
 import static org.sakaiproject.search.indexing.DefaultTask.Type.*;
 import static org.sakaiproject.search.solr.indexing.SolrTask.Type.*;
 
 /**
  * Component in charge of executing a Task by generating a solr request and modifying the search index.
  *
  * @author Colin Hebert
  */
 public class SolrTaskHandler implements TaskHandler {
     private static final Logger logger = LoggerFactory.getLogger(SolrTaskHandler.class);
     private static final String VERSION_FIELD = "_version_";
     private SolrServer solrServer;
     private SolrTools solrTools;
     private ThreadLocalManager threadLocalManager;
 
     @Override
     public void executeTask(Task task) {
         logger.debug("Attempt to handle '{}'", task);
         try {
             String taskType = task.getType();
             if (INDEX_DOCUMENT.getTypeName().equals(taskType)) {
                 indexDocument(task.getProperty(DefaultTask.REFERENCE), task.getCreationDate());
             } else if (REMOVE_DOCUMENT.getTypeName().equals(taskType)) {
                 removeDocument(task.getProperty(DefaultTask.REFERENCE), task.getCreationDate());
             } else if (INDEX_SITE.getTypeName().equals(taskType)) {
                 indexSite(task.getProperty(DefaultTask.SITE_ID), task.getCreationDate());
             } else if (REFRESH_SITE.getTypeName().equals(taskType)) {
                 refreshSite(task.getProperty(DefaultTask.SITE_ID), task.getCreationDate());
             } else if (INDEX_ALL.getTypeName().equals(taskType)) {
                 indexAll(task.getCreationDate());
             } else if (REFRESH_ALL.getTypeName().equals(taskType)) {
                 refreshAll(task.getCreationDate());
             } else if (REMOVE_SITE_DOCUMENTS.getTypeName().equals(taskType)) {
                 removeSiteDocuments(task.getProperty(DefaultTask.SITE_ID), task.getCreationDate());
             } else if (REMOVE_ALL_DOCUMENTS.getTypeName().equals(taskType)) {
                 removeAllDocuments(task.getCreationDate());
             } else if (OPTIMISE_INDEX.getTypeName().equals(taskType)) {
                 optimiseSolrIndex();
             } else {
                 throw new TaskHandlingException("Task '" + task + "' can't be executed");
             }
         } catch (Exception e) {
             throw wrapException(e, "Couldn't execute the task '" + task + "'", task);
         }
     }
 
     /**
      * Indexes a document based on the reference to the document and a date.
      * <p>
      * If the document has been updated since the actionDate, the document won't be updated.
      * </p>
      *
      * @param reference  reference to the document.
      * @param actionDate creation date of the task.
      */
     public void indexDocument(String reference, Date actionDate) {
         logger.debug("Add '{}' to the index", reference);
 
         try {
             // Real-time get the last version of the document
             SolrParams q = new SolrQuery()
                     .setRequestHandler("/get")
                     .set("id", reference)
                     .set("fl", VERSION_FIELD + "," + SearchService.DATE_STAMP);
             SolrDocument currentDocument = (SolrDocument) solrServer.query(q).getResponse().get("doc");
 
             // Check if the document exists and hasn't been indexed since the creation of the task
             if (currentDocument != null
                     && actionDate.compareTo((Date) currentDocument.getFieldValue(SearchService.DATE_STAMP)) <= 0) {
                 logger.debug("Indexation not useful as the document was updated earlier");
                 return;
             }
 
             SolrInputDocument document = solrTools.toSolrDocument(reference, actionDate);
             if (currentDocument != null) {
                 document.setField(VERSION_FIELD, currentDocument.getFieldValue(VERSION_FIELD));
             }
             logger.debug("Adding the document '{}'", document);
             solrServer.add(document);
         } catch (Exception e) {
             Task task = new DefaultTask(INDEX_DOCUMENT, actionDate).setProperty(DefaultTask.REFERENCE, reference);
             throw wrapException(e, "An exception occurred while indexing the document '" + reference + "'", task);
         }
     }
 
     /**
      * Removes a document from the index based on the given reference.
      * <p>
      * If the action date is inferior to the indexation date of the document, the document won't be removed.
      * </p>
      *
      * @param reference  reference to the document.
      * @param actionDate creation date of the task.
      */
     public void removeDocument(String reference, Date actionDate) {
         logger.debug("Remove '{}' from the index", reference);
         try {
             solrServer.deleteByQuery(
                     SearchService.DATE_STAMP + ":{* TO " + solrTools.format(actionDate) + "} AND "
                             + SearchService.FIELD_REFERENCE + ":" + ClientUtils.escapeQueryChars(reference));
         } catch (Exception e) {
             Task task = new DefaultTask(REMOVE_DOCUMENT, actionDate).setProperty(DefaultTask.REFERENCE, reference);
             throw wrapException(e, "An exception occurred while removing the document '" + reference + "'", task);
         }
     }
 
     /**
      * Indexes every document available within a site.
      * <p>
      * Every document indexed before the actionDate will be removed.
      * </p>
      *
      * @param siteId     id of the site to index.
      * @param actionDate creation date of the task.
      */
     public void indexSite(final String siteId, Date actionDate) {
         logger.info("Rebuilding the index for '{}'", siteId);
         NestedTaskHandlingException nthe = new NestedTaskHandlingException(
                 "An exception occurred while indexing the site '" + siteId + "'");
         Queue<String> siteReferences = solrTools.getSiteDocumentsReferences(siteId);
         while (siteReferences.peek() != null) {
             try {
                 indexDocument(siteReferences.poll(), actionDate);
             } catch (TaskHandlingException t) {
                 nthe.addTaskHandlingException(t);
             }
         }
 
         try {
             removeSiteDocuments(siteId, actionDate);
         } catch (TaskHandlingException t) {
             nthe.addTaskHandlingException(t);
         }
 
         if (!nthe.isEmpty()) throw nthe;
     }
 
     /**
      * Updates the documents currently indexed for a given site.
      * <p>
      * Only the documents already indexed will be updated or removed if necessary.
      * </p>
      *
      * @param siteId     Id of the site to update
      * @param actionDate creation date of the task to execute
      */
     public void refreshSite(String siteId, Date actionDate) {
         logger.info("Refreshing the index for '{}'", siteId);
         NestedTaskHandlingException nthe = new NestedTaskHandlingException(
                 "An exception occurred while indexing the site '" + siteId + "'");
         // Get the currently indexed resources for this site
         Queue<String> references;
         try {
             references = solrTools.getValidReferences(siteId);
         } catch (Exception e) {
             Task task = new DefaultTask(REFRESH_SITE, actionDate).setProperty(DefaultTask.SITE_ID, siteId);
             throw wrapException(e, "Couldn't obtain the list of documents to refresh for '" + siteId + "'", task);
         }
 
         logger.debug("{} elements will be refreshed", references.size());
 
         // Index already indexed documents
         while (!references.isEmpty()) {
             try {
                 indexDocument(references.poll(), actionDate);
             } catch (TaskHandlingException t) {
                 nthe.addTaskHandlingException(t);
             }
         }
 
         // Remove documents that were indexed before
         try {
             removeSiteDocuments(siteId, actionDate);
         } catch (TaskHandlingException t) {
             nthe.addTaskHandlingException(t);
         }
 
         if (!nthe.isEmpty()) throw nthe;
     }
 
     /**
      * Indexes every available site.
      * <p>
      * Every document indexed before the creation date of the task will be removed.
      * </p>
      *
      * @param actionDate creation date of the task
      */
     public void indexAll(Date actionDate) {
         logger.info("Rebuilding the index for every indexable site");
         NestedTaskHandlingException nthe = new NestedTaskHandlingException(
                 "An exception occurred while reindexing everything");
         Queue<String> reindexedSites = solrTools.getIndexableSites();
         while (!reindexedSites.isEmpty()) {
             try {
                 indexSite(reindexedSites.poll(), actionDate);
             } catch (TaskHandlingException t) {
                 nthe.addTaskHandlingException(t);
             } finally {
                 // Clean up the localThread after each site
                 threadLocalManager.clear();
             }
         }
         try {
             removeAllDocuments(actionDate);
         } catch (TaskHandlingException t) {
             nthe.addTaskHandlingException(t);
         }
         try {
             optimiseSolrIndex();
         } catch (TaskHandlingException t) {
             nthe.addTaskHandlingException(t);
         }
 
         if (nthe.isEmpty()) throw nthe;
     }
 
     /**
      * Updates the content of every document indexed and remove every document that shouldn't be here.
      *
      * @param actionDate creation date of the task
      */
     public void refreshAll(Date actionDate) {
         logger.info("Refreshing the index for every indexable site");
         NestedTaskHandlingException nthe = new NestedTaskHandlingException(
                 "An exception occurred while refreshing everything");
         Queue<String> refreshedSites = solrTools.getIndexableSites();
         while (!refreshedSites.isEmpty()) {
             try {
                 refreshSite(refreshedSites.poll(), actionDate);
             } catch (TaskHandlingException t) {
                 nthe.addTaskHandlingException(t);
             } finally {
                 // Clean up the localThread after each site
                 threadLocalManager.clear();
             }
         }
         try {
             removeAllDocuments(actionDate);
         } catch (TaskHandlingException t) {
             nthe.addTaskHandlingException(t);
         }
         try {
             optimiseSolrIndex();
         } catch (TaskHandlingException t) {
             nthe.addTaskHandlingException(t);
         }
 
         if (nthe.isEmpty()) throw nthe;
     }
 
     /**
      * Removes every document from a site.
      * <p>
      * Every document of this site, indexed before the creation date of that task will be removed from the index.
      * </p>
      *
      * @param siteId       Identifier of the site to clean.
      * @param creationDate creation date of the task.
      */
     public void removeSiteDocuments(String siteId, Date creationDate) {
         logger.info("Remove old documents from '{}'", siteId);
         try {
             solrServer.deleteByQuery(
                     SearchService.DATE_STAMP + ":{* TO " + solrTools.format(creationDate) + "} AND "
                             + SearchService.FIELD_SITEID + ":" + ClientUtils.escapeQueryChars(siteId));
         } catch (Exception e) {
             Task task = new SolrTask(REMOVE_SITE_DOCUMENTS, creationDate).setProperty(DefaultTask.SITE_ID, siteId);
             throw wrapException(e, "Couldn't remove old documents the site '" + siteId + "'", task);
         }
     }
 
     /**
      * Removes every document from the index.
      * <p>
      * Every document indexed before the creation date of that task will be removed from the index.
      * </p>
      *
      * @param creationDate creation date of the task.
      */
     public void removeAllDocuments(Date creationDate) {
         logger.info("Remove old documents from every sites");
         try {
             solrServer.deleteByQuery(SearchService.DATE_STAMP + ":{* TO " + solrTools.format(creationDate) + "}");
         } catch (Exception e) {
             Task task = new SolrTask(REMOVE_ALL_DOCUMENTS, creationDate);
             throw wrapException(e, "Couldn't remove old documents from the entire instance", task);
         }
     }
 
     /**
      * Optimises the solr index.
      */
     public void optimiseSolrIndex() {
         logger.info("Optimise the index");
         try {
             solrServer.optimize();
         } catch (Exception e) {
             Task task = new SolrTask(OPTIMISE_INDEX);
             throw wrapException(e, "Couldn't optimise the index", task);
         }
     }
 
     /**
      * Wraps an Exception in a TaskHandlingException that can be thrown.
      *
      * @param e                caught Exception
      * @param message          message to associate with the wrapper
      * @param potentialNewTask new task that could be executed if the caught throwable is considered
      *                         as a temporary failure
      * @return the wrapped exception or the original one if it was already wrapped
      */
     private TaskHandlingException wrapException(Exception e, String message, Task potentialNewTask) {
         if (e instanceof SolrServerException && ((SolrServerException) e).getRootCause() instanceof IOException) {
             return new TemporaryTaskHandlingException(message, e, potentialNewTask);
         } else if (e instanceof SolrException
                 && (((SolrException) e).code() == SolrException.ErrorCode.SERVICE_UNAVAILABLE.code
                 || ((SolrException) e).code() == SolrException.ErrorCode.SERVER_ERROR.code
                 || ((SolrException) e).code() == SolrException.ErrorCode.CONFLICT.code)) {
             return new TemporaryTaskHandlingException(message, e, potentialNewTask);
         } else if (e instanceof IOException) {
             return new TemporaryTaskHandlingException(message, e, potentialNewTask);
         } else if (e instanceof TaskHandlingException) {
             return (TaskHandlingException) e;
         } else {
             return new TaskHandlingException(message, e);
         }
     }
 
     public void setSolrServer(SolrServer solrServer) {
         this.solrServer = solrServer;
     }
 
     public void setSolrTools(SolrTools solrTools) {
         this.solrTools = solrTools;
     }
 
     public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
         this.threadLocalManager = threadLocalManager;
     }
 }
