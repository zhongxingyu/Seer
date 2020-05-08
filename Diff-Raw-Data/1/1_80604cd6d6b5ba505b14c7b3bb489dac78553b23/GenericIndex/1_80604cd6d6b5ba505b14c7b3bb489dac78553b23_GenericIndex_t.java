 package net.cyklotron.cms.search.util;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.Fields;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.MultiFields;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.NumericRangeQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.SearcherManager;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.Directory;
 import org.jcontainer.dna.Logger;
 import org.objectledge.authentication.UserManager;
 import org.objectledge.context.Context;
 import org.objectledge.coral.query.MalformedQueryException;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.filesystem.FileSystem;
 import org.objectledge.longops.LongRunningOperation;
 import org.objectledge.longops.LongRunningOperationRegistry;
 import org.objectledge.longops.LongRunningOperationSecurityCallback;
 import org.objectledge.longops.OperationCancelledException;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.threads.Task;
 
 import net.cyklotron.cms.search.SearchConstants;
 import net.cyklotron.cms.search.analysis.AnalyzerProvider;
 
 /**
  * GenericIndex is wrapper around Lucene 4.0 index readers and writers which provide useful methods
  * like add, update, addAll, updateAll resources. These operations are transactional. To use
  * GenericIndex you should implement FromDocumentMapper, ToDocumentMapper contracts and create
  * instance of ResourceProvider. To create GenericIndex one should use GenericIndexFactory.
  * 
  * @see FromDocumentMapper
  * @see ToDocumentMapper
  * @see ResourceProvider
  * @see GenericIndexFactory
  * @see AnalyzerProvider
  * @author Marek Lewandowski
  * @param <T> concrete resource type
  */
 public class GenericIndex<T extends Resource, U>
     implements Closeable
 {
 
     private final Logger logger;
 
     private final Directory directory;
 
     private Analyzer analyzer;
 
     private final FromDocumentMapper<U> fromDocumentMapper;
 
     private final ToDocumentMapper<T> toDocumentMapper;
 
     private final ResourceProvider resourceProvider;
 
     private final IndexWriterConfig iwConf;
 
     private SearcherManager searcherManager;
 
     GenericIndex(FileSystem fileSystem, Logger logger, String indexPath,
         AnalyzerProvider analyzerProvider, FromDocumentMapper<U> fromDocumentMapper,
         ToDocumentMapper<T> toDocumentMapper, ResourceProvider resourceProvider, Directory directory)
         throws IOException
     {
         this.logger = logger;
         this.directory = directory;
         this.resourceProvider = resourceProvider;
         this.analyzer = analyzerProvider.getAnalyzer();
         this.toDocumentMapper = toDocumentMapper;
         this.fromDocumentMapper = fromDocumentMapper;
         iwConf = new IndexWriterConfig(SearchConstants.LUCENE_VERSION, analyzer);
         this.searcherManager = new SearcherManager(directory, null);
     }
 
     private IndexWriter getWriter()
         throws IOException
     {
         return new IndexWriter(directory, iwConf);
     }
 
     /**
      * Add new resource to index
      * 
      * @param resource
      * @throws IOException
      */
     public synchronized void add(T resource)
         throws IOException
     {
         addAll(Arrays.asList(resource));
     }
 
     public synchronized void update(T resource)
         throws IOException
     {
         Term identifier = toDocumentMapper.getIdentifier(resource);
         Document document = toDocumentMapper.toDocument(resource);
         try(IndexWriter writer = getWriter())
         {
             writer.prepareCommit();
             try
             {
                 writer.updateDocument(identifier, document);
                 writer.commit();
             }
             catch(RuntimeException | IOException e)
             {
                 writer.rollback();
                 throw e;
             }
         }
     }
 
     public U getResource(Long id)
         throws IOException
     {
         final IndexSearcher searcher = searcherManager.acquire();
         try
         {
             Query query = NumericRangeQuery.newLongRange("id", id, id, true, true);
             return singleResult(searcher.search(query, 1));
         }
         catch(Exception e)
         {
             logger.error("search error", e);
             return null;
         }
         finally
         {
             searcherManager.release(searcher);
         }
     }
 
     protected U singleResult(TopDocs result)
         throws IOException
     {
         if(result.totalHits == 1)
         {
             final IndexSearcher searcher = searcherManager.acquire();
             try
             {
                 return fromDocumentMapper.fromDocument(searcher.doc(result.scoreDocs[0].doc));
             }
             finally
             {
                 searcherManager.release(searcher);
             }
         }
         return null;
     }
 
     /**
      * Adds new resources to index.
      * 
      * @param resources
      * @throws IOException
      */
 
     public synchronized void addAll(Collection<T> resources)
         throws IOException
     {
         try(IndexWriter writer = getWriter())
         {
             writer.prepareCommit();
             Collection<Document> documents = getDocuments(resources);
             try
             {
                 writer.addDocuments(documents);
                 writer.commit();
             }
             catch(RuntimeException | IOException e)
             {
                 writer.rollback();
                 throw e;
             }
         }
     }
 
     /**
      * Removes only documents which should be updated
      * 
      * @param resources
      * @throws IOException
      */
     public synchronized void updateAll(Collection<T> resources)
         throws IOException
     {
         try(IndexWriter writer = getWriter())
         {
             writer.prepareCommit();
             try
             {
                 for(T resource : resources)
                 {
                     writer.updateDocument(toDocumentMapper.getIdentifier(resource),
                         toDocumentMapper.toDocument(resource));
                     writer.commit();
                 }
             }
             catch(RuntimeException | IOException e)
             {
                 writer.rollback();
                 throw e;
             }
         }
     }
 
     /**
      * Reindexes all resources. Deletes all documents and adds them again
      * 
      * @param resources
      * @return
      * @throws IOException
      * @throws MalformedQueryException
      * @throws IllegalStateException
      */
     public synchronized void reindexAll()
         throws IOException, IllegalStateException, MalformedQueryException
     {
         try(IndexWriter writer = getWriter())
         {
             writer.prepareCommit();
             try
             {
                 writer.deleteAll();
                 for(QueryResults.Row row : resourceProvider.runQuery())
                 {
                     Document document = toDocumentMapper.toDocument((T)row.get());
                     if(document != null)
                     {
                         writer.addDocument(document);
                     }
                 }
                 writer.commit();
             }
             catch(RuntimeException | IOException | MalformedQueryException e)
             {
                 writer.rollback();
                 throw e;
             }
         }
     }
 
     Collection<String> getAllFieldsNames()
         throws IOException
     {
         try(IndexReader reader = DirectoryReader.open(directory))
         {
             Fields fields = MultiFields.getFields(reader);
             if(fields == null)
             {
                 return Collections.emptyList();
             }
             Set<String> fieldNames = new HashSet<>();
             for(String fieldName : fields)
             {
                 fieldNames.add(fieldName);
             }
             return fieldNames;
         }
     }
 
     public Task createReindexTask(final String code, final String description,
         final Principal requestor, final LongRunningOperationSecurityCallback securityCallback,
         final CoralSessionFactory coralSessionFactory,
         final LongRunningOperationRegistry longRunningOperationRegistry,
         final UserManager userManager)
     {
         return new Task()
             {
                 @Override
                 public void process(Context context)
                     throws ProcessingException
                 {
                     try(CoralSession coralSession = coralSessionFactory.getRootSession())
                     {
                         final LongRunningOperation operation = longRunningOperationRegistry
                             .register(code, description, requestor, 0, securityCallback);
                         try
                         {
                             synchronized(GenericIndex.this)
                             {
                                 QueryResults rset = resourceProvider.runQuery();
                                 int total = rset.rowCount();
                                 int counter = 0;
                                 try(IndexWriter writer = getWriter())
                                 {
                                     writer.prepareCommit();
                                     try
                                     {
                                         writer.deleteAll();
                                         for(QueryResults.Row row : rset)
                                         {
                                             Document document = toDocumentMapper.toDocument((T)row
                                                 .get());
                                             if(document != null)
                                             {
                                                 writer.addDocument(document);
                                             }
                                             longRunningOperationRegistry.update(operation,
                                                 counter++, total);
                                         }
                                         writer.commit();
                                     }
                                     catch(RuntimeException | IOException
                                                     | OperationCancelledException e)
                                     {
                                         writer.rollback();
                                         throw e;
                                     }
                                 }
                             }
                         }
                         catch(IOException e)
                         {
                             logger.error("operation " + code + " has failed", e);
                         }
                         catch(OperationCancelledException e)
                         {
                             logger.info("operation " + code + " has been cancelled");
                         }
                         finally
                         {
                             longRunningOperationRegistry.unregister(operation);
                         }
                     }
                     catch(MalformedQueryException e)
                     {
                         logger.error("unexpected MalformedQueryException", e);
                     }
                 }
 
                 @Override
                 public String getName()
                 {
                     return code;
                 }
             };
     }
 
     public Collection<U> search(PerformSearch performSearch)
         throws IOException
     {
        searcherManager.maybeRefresh();
         IndexSearcher searcher = searcherManager.acquire();
         try
         {
             Collection<Document> results = performSearch.doSearch(searcher);
             Collection<U> resources = new ArrayList<>();
             for(Document document : results)
             {
                 resources.add(fromDocumentMapper.fromDocument(document));
             }
             return resources;
         }
         finally
         {
             searcherManager.release(searcher);
         }
     }
 
     private Collection<Document> getDocuments(Collection<T> resources)
     {
         Collection<Document> documents = new ArrayList<>();
         for(T resource : resources)
         {
             Document document = toDocumentMapper.toDocument(resource);
             if(document != null)
             {
                 documents.add(document);
             }
         }
         return documents;
     }
 
     public Analyzer getAnalyzer()
     {
         return analyzer;
     }
 
     @Override
     public void close()
         throws IOException
     {
         directory.close();
     }
 }
