 package server.index;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.LimitTokenCountAnalyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Field.Index;
 import org.apache.lucene.document.Field.Store;
 import org.apache.lucene.index.*;
 import org.apache.lucene.index.IndexWriter.MaxFieldLength;
 import org.apache.lucene.queryParser.MultiFieldQueryParser;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.search.*;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.LockObtainFailedException;
 import org.apache.lucene.store.SimpleFSDirectory;
 import org.apache.lucene.util.Version;
 import org.apache.lucene.xmlparser.CoreParser;
 import org.apache.lucene.xmlparser.ParserException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import server.dao.DAOFactory;
 import server.dao.IndexItemDAO;
 import server.exceptions.CinnamonException;
 import server.global.ConfThreadLocal;
 import server.index.queryBuilder.RegexQueryBuilder;
 import server.index.queryBuilder.WildcardQueryBuilder;
 import utils.ParamParser;
 
 import javax.persistence.EntityManager;
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * LuceneBridge provides the following functionality:<br>
  * <ul>
  * <li>Send your indexable items over the bridge into Lucene's index.</li>
  * <li>Fetch those items back from the index via a search.</li>
  * <li>Throw old items over the bridge when they have been deleted in the repository
  * and thereby purge them from the index.</li>
  * </ul>
  * <p>In less fanciful prose: you can use this class to add items to the index, search for
  * them and remove them from the index. To update an item, you have to remove it from
  * the index and add it again (this is the recommended Lucene way).</p>
  * <p>Note: At the moment this class uses a rather expensive approach as it will open the
  * index for reading and writing each time an object is changed or searched. That's quite a
  * lot I/O. The index server will reduce this probably by re-using the IndexReader/IndexSearcher
  * objects.</p>
  */
 public class LuceneBridge {
 
     static Properties luceneProperties = ConfThreadLocal.getConf().findProperties("lucene.properties");
     transient Logger log = LoggerFactory.getLogger(this.getClass());
     static DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
 
     File indexFolder;
     Directory indexDir;
     Analyzer analyzer;//new CinnamonStandardAnalyzer(Version.LUCENE_CURRENT);
     String repository;
     IndexItemDAO iiDao;
     List<IndexItem> iiList;// = iiDao.list();
 
     IndexWriter indexWriter;
 
     final ReentrantLock lock = new ReentrantLock();
     Long lockTimeout;
 
     public LuceneBridge() {
     }
 
     public LuceneBridge(String repository, EntityManager em) {
         this.repository = repository;
         indexFolder = new File(luceneProperties.getProperty("indexDir"), repository);
         try {
             indexDir = new SimpleFSDirectory(indexFolder);
         } catch (IOException e) {
             throw new CinnamonException("error.lucene.IO", e);
         }
         Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_34);
         analyzer = new LimitTokenCountAnalyzer(standardAnalyzer, Integer.MAX_VALUE);
 
         indexWriter = createWriter(indexDir);
 
         this.iiDao = daoFactory.getIndexItemDAO(em);
         this.iiList = iiDao.list();
         log.debug("# of IndexItems found: " + iiList.size());
     }
 
     IndexWriter createWriter(Directory dir) {
         IndexWriter writer;
         lockTimeout = Long.parseLong(luceneProperties.getProperty("lockTimeout", "5000"));
         IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_34, analyzer);
         try {
             File indexLock = new File(indexFolder, "write.lock");
             if(indexLock.exists()){
                 // low level cleanup
                 Boolean deleteResult = indexLock.delete();
                 if(!deleteResult){
                     log.warn("It is possible that the indexLock ("+indexLock.getAbsolutePath()+") has not been deleted.");
                 }
             }
             // cleanup; after server restart there could still be a lock lying around.
             if (IndexWriter.isLocked(dir)) {
                 log.debug("IndexDir " + dir.toString() + " is locked - unlocking.");
                 IndexWriter.unlock(dir);
             }
 
             /*
             * Set timeout for write-locks.
             * TODO: timeouts should be > than a single re-index run.
             */
             if (luceneProperties.containsKey("writeLockTimeout")) {
                 Long timeout = Long.parseLong(luceneProperties.getProperty("writeLockTimeout"));
                 writerConfig.setWriteLockTimeout(timeout);
             }
             writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
             writer = new IndexWriter(indexDir, writerConfig);
 
         } catch (IOException e) {
             throw new CinnamonException("error.lucene.IO", e);
         }
         return writer;
     }
 
     /**
      * Tries to acquire a lock on this LuceneBridge object. Will throw a CinnamonException if the following happens:
      * <ul>
      * <li>waiting longer than lockTimeout: error.timeout_waiting_for_lucene_lock</li>
      * <li>thread is interrupted while trying to acquire lock: error.interrupted_while_waiting_for_lucene_lock</li>
      * </ul>
      * You must release the lock in a finally block after acquiring it.
      */
     void acquireLock() {
         try {
             log.debug("Trying to get lock on LuceneBridge.");
             if (!lock.tryLock(lockTimeout, TimeUnit.MILLISECONDS)) {
                 throw new CinnamonException("error.timeout_waiting_for_lucene_lock");
             }
             log.debug("Lock acquired.");
         } catch (InterruptedException e) {
             throw new CinnamonException("error.interrupted_while_waiting_for_lucene_lock");
         }
     }
 
     /**
      * If this thread holds the lock on the LuceneBridge, release it.
      */
     void releaseLock() {
         if (lock.isHeldByCurrentThread()) {
             lock.unlock();
             log.debug("Unlocked LuceneBridge.");
         }
         else {
             log.warn("Warning: tried to release lock which this thread does not hold.");
         }
     }
 
     public void addObjectToIndex(Indexable indexable) {
         try {
             try {
                 Document doc = new Document();
                 log.debug("store standard fields");
                 storeStandardFields(indexable, doc);
                 log.debug("finished store standard fields");
 
                 ContentContainer content;
                 if(indexable.hasXmlContent()){
                         content = new ContentContainer(indexable, repository);
                 }
                 else{
                     content = new ContentContainer("<empty />".getBytes());
                 }
 //                String content = indexable.getContent(repository);
                 log.debug("finished: getContent");
                 ContentContainer metadata = new ContentContainer(indexable.getMetadata().getBytes());
 //                String metadata = indexable.getMetadata();
                 log.debug("store systemMetadata");
 //                String systemMetadata = indexable.getSystemMetadata();
                 ContentContainer systemMetadata = new ContentContainer(indexable.getSystemMetadata().getBytes());
                 log.debug("got sysMetadata, start indexObject loop");
 
                 for (IndexItem item : iiList) {
                     /*
                      * At the moment, the OSDs and Folders do not cache
                      * their responses to getSystemMetadata or getContent.
                      * In a repository with many IndexItems, this would cause
                      * quite some strain on the server's resources.
                      */
                     try {
 //							log.debug("indexObject for field '"+item.fieldname+"' with content: "+content);
                         item.indexObject(content, metadata, systemMetadata, doc);
                     } catch (Exception e) {
                         log.debug("*** failed *** to execute IndexItem " + item.getId(), e);
                     }
                 }
 
                 acquireLock();
                 log.debug("create new IndexWriter");
 //                Boolean create = !IndexReader.indexExists(indexDir);
 //                writer = new IndexWriter(indexDir, analyzer, create, MaxFieldLength.UNLIMITED);
                 log.debug("before addDocument");
                 indexWriter.addDocument(doc);
                 log.debug("added Document");
                 indexable.setIndexed(new Date());
                 indexable.setIndexOk(true);
                 indexWriter.commit();
             } catch (OutOfMemoryError e) {
                 log.warn("OOM-error during indexing:", e);
                 // according to Lucene docs, we should close the writer after OOM-Problems.
                 indexWriter.close();
                 indexWriter = createWriter(indexDir);
 
             } finally {
                 unlockIfNecessary();
 
             }
         } catch (IOException e) {
             log.debug("IOException during indexing.", e);
             throw new CinnamonException("error.lucene.IO", e);
         } catch (Exception e) {
             log.debug("addObjectToIndex failed:", e);
             throw new CinnamonException("error.add.to.index", e);
         } finally {
             /*
              * Unlock after commit.
              */
             releaseLock();
         }
         log.debug("finished addObjectToIndex");
     }
 
     void unlockIfNecessary() throws IOException {
         if (IndexWriter.isLocked(indexDir)) {
             // we failed to commit or close the IndexWriter.
             try {
                 indexWriter.close();
                 log.debug("Unlocking indexDir.");
             } catch (CorruptIndexException e) {
                 log.error("Lucene Index has been corrupted!", e);
                 throw new CinnamonException("error.lucene.IO", e, indexDir.toString());
             } catch (IOException e) {
                 throw new CinnamonException("error.lucene.IO", e, indexDir.toString());
             } finally {
                 IndexWriter.unlock(indexDir);
                 indexWriter = createWriter(indexDir);
             }
         }
     }
 
     /**
      * Search for all documents matching the given params, which must be an
      * Lucene XML-Query-Parser document.
      *
      * @param params input for XML-Query-Parser
      * @return a ResultCollector, which contains a collection of all documents found.
      */
     public ResultCollector search(String params) {
         log.debug("starting search");
         ResultCollector results = new ResultCollector();
         try {
             InputStream bais = new ByteArrayInputStream(params.getBytes("UTF-8"));
             CoreParser coreParser = new CoreParser("content", analyzer);
             coreParser.addQueryBuilder("WildcardQuery", new WildcardQueryBuilder());
             coreParser.addQueryBuilder("RegexQuery", new RegexQueryBuilder());
             Query query = coreParser.parse(bais);
 
             acquireLock();
 
             IndexSearcher searcher = new IndexSearcher(indexDir, true);
             results.setSearcher(searcher);
             searcher.search(query, results);
             searcher.close();
         } catch (IOException e) {
             throw new CinnamonException("error.lucene.IO", e);
         } catch (ParserException e) {
             throw new CinnamonException("error.parsing.lucene.query", e, params);
         } finally {
             releaseLock();
         }
         log.debug("finished search; results: " + results.getDocuments().size());
         return results;
     }
 
 
     /**
      * Search for documents matching the given input. Returns the top maxResults items.
      *
      * @param queryString text query params
      * @return a ResultCollector, which contains a collection of all documents found.
      */
     public SearchResult searchMultipleFields(String queryString, Integer page, Integer pageSize, String[] fields) {
         log.debug("starting search");
         SearchResult searchResult = null;
         try {
 
 //            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_31,fields, analyzer);
             BooleanClause.Occur[] flags = new BooleanClause.Occur[fields.length];
             for(int x = 0; x < flags.length; x++){
                 flags[x] = BooleanClause.Occur.SHOULD;
             }
             Query query = MultiFieldQueryParser.parse(Version.LUCENE_34,queryString ,fields, flags,analyzer );
 
             acquireLock();
 
             IndexSearcher searcher = new IndexSearcher(indexDir, true);
             Integer startingResult = page*pageSize;
             Integer endResult = startingResult + pageSize-1;
             TopDocs hits = searcher.search(query, endResult+1);
             searchResult = new SearchResult(hits, searcher, startingResult, pageSize);
 
             searcher.close();
             log.debug("finished search; totalResults: "+searchResult.totalResults);
         } catch (IOException e) {
             throw new CinnamonException("error.lucene.IO", e);
         } catch (ParseException e) {
             throw new CinnamonException("error.parsing.lucene.query", e, queryString);
         } finally {
             releaseLock();
         }
 
         return searchResult;
     }
 
     /**
      * Remove an Indexable object from the index.
      *
      * @param indexable the Indexable object which will be removed from the index.
      */
     public void removeObjectFromIndex(Indexable indexable) {
         try {
             log.debug("check if index exists");
             if (!IndexReader.indexExists(indexDir)) {
                 log.debug("Index does not exist.");
                 // nothing to do.
                 return;
             }
             if (indexable == null) {
                 log.debug("indexable is NULL");
                 return;
             }
             log.debug("creating reader");
             log.debug("parsing sysmeta");
             org.dom4j.Document sysMeta = ParamParser.parseXmlToDocument(indexable.getSystemMetadata(),
                     "error.invalid.system_metadata");
             String id = sysMeta.valueOf("/sysMeta/@id");
             log.debug("removing document with id " + id);
             log.debug("delete document");
 
             acquireLock();
             deleteDocument(new Term("uniqueId", id), 5);
         } catch (FileNotFoundException f) {
             log.warn("File not found - if the index does not yet exist, " +
                     "removeObjectFromIndex is expected to fail", f);
         } catch (IOException e) {
             throw new CinnamonException("error.lucene.IO", e);
         } finally {
             releaseLock();
         }
     }
 
     /**
      * Delete the documents found by the given Term from this Bridge's index.
      *
      * @param term    the search term
      * @param retries how often to retry deleting the document. Probably useful only
      *                if other programs / servlets are also interacting with the index.
      * @throws IOException if anything IO-related goes wrong.
      */
     void deleteDocument(Term term, Integer retries) throws IOException {
         try {
             indexWriter.deleteDocuments(term);
         } catch (Exception e) {
             log.debug("", e);
             log.debug("retry-delete document");
             if (retries != null && retries > 0) {
                 deleteDocument(term, --retries);
             }
             indexWriter.commit();
         } catch (OutOfMemoryError e) {
             log.warn("OOM-error during indexing:", e);
             // according to Lucene docs, we should close the writer after OOM-Problems.
             indexWriter.close();
             indexWriter = createWriter(indexDir);
         } finally {
             unlockIfNecessary();
         }
     }
 
 
     /**
      * Remove an old Indexable from the index and add it anew, so
      * all fields are updated. The hibernateId and class of the indexable
      * must never be changed (otherwise Lucene will not be able to find
      * the Indexable in its index and you will be plagued by ghost objects
      * until the next complete re-index run).
      *
      * @param indexable the object to update
      */
     public void updateObjectInIndex(Indexable indexable) {
         log.debug("remove object from index");
         removeObjectFromIndex(indexable);
         log.debug("add object to index.");
         addObjectToIndex(indexable);
     }
 
     /**
      * Fetch the information that every indexed object needs:
      * a unique id and its class. The fields every object has are:
      * <ul>
      * <li>hibernateId: this object's database id</li>
      * <li>javaClass: the Java class of this object.</li>
      * <li>uniqueId: the unique combination of javaClass and hibernateId.</li>
      * </ul>
      *
      * @param indexable the indexable whose standard fields will be stored
      * @param doc       the doc to store the fields in.
      */
     void storeStandardFields(Indexable indexable, Document doc) {
 //        log.debug("start. sSF");
 //		log.debug("storeStandardFields: "+indexable.getSystemMetadata());
         org.dom4j.Document sysMeta = ParamParser.parseXmlToDocument(indexable.getSystemMetadata(),
                 "error.invalid.system_metadata");
         String hibernateId = sysMeta.valueOf("/sysMeta/@hibernateId");
         String className = sysMeta.valueOf("/sysMeta/@javaClass");
         String uniqueId = sysMeta.valueOf("/sysMeta/@id");
         log.debug("indexing of: " + uniqueId);
 //		log.debug(String.format("adding fields: %s, %s, %s",hibernateId, className, uniqueId));
         doc.add(new Field("hibernateId", hibernateId, Store.YES, Index.NOT_ANALYZED));
 //        log.debug("stored hibernateId");
         doc.add(new Field("javaClass", className, Store.YES, Index.NOT_ANALYZED));
 //        log.debug("stored javaClass");
         doc.add(new Field("uniqueId", uniqueId, Store.YES, Index.NOT_ANALYZED));
 //        log.debug("stored uniqueId");
     }
 
     /**
      * Remove all documents belonging to a given Java class from the
      * index. <em>Warning</em>: this will remove <i>ALL</remove> documents
      * and is intended only for clearing an index for testing purposes.
      * This method is used by server.extension.Initializer.
      *
      * @param clazz Class of the documents you wish to remove.
      */
     public void removeClassFromIndex(Class<? extends Indexable> clazz) throws IOException {
         try {
             // create index if needed:
             if (!IndexReader.indexExists(indexDir)) {
                 return;
                 // nothing to do - index is empty.
             }
 //            IndexReader reader = IndexReader.open(indexDir, false);
 //            reader.deleteDocuments(new Term("javaClass", clazz.getName()));
 //            reader.close();
 
             indexWriter.deleteDocuments(new Term("javaClass", clazz.getName()));
             indexWriter.commit();
         } catch (IOException e) {
             log.warn("IOException occurred during removeClassFromIndex", e);
             throw new CinnamonException("error.lucene.IO", e);
         } catch (OutOfMemoryError e) {
             indexWriter.close();
             indexWriter = createWriter(indexDir);
         } finally {
             unlockIfNecessary();
         }
     }
 
     String getRepository() {
         return repository;
     }
 
     /**
      * Lucene caches the available index items for performance reasons.
      * If you need to update the IndexItem cache without a server restart,
      * call setIndexItemList(indexItemDao.list()).
      * Note: you should do this only in a synchronized context or during testing,
      * when no Lucene indexing is ongoing.
      *
      * @param items the List of IndexItem objects which will be used from now on.
      */
     public void setIndexItemList(List<IndexItem> items) {
         iiList = items;
     }
 }
