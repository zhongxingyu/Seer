 package com.amee.platform.search;
 
 import com.amee.base.domain.ResultsWrapper;
 import org.apache.commons.io.comparator.LastModifiedFileComparator;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.*;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 import org.springframework.beans.factory.annotation.Value;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * LuceneIndexWrapper wraps a Lucene file system index and provides a simplified abstraction of
  * the Lucene API.
  * <p/>
  * Instances of this class should only be used by one thread. Care should be taken to respect the
  * sequence of method calls as described in the method documentation.
  */
 public class LuceneServiceImpl implements LuceneService {
 
     private final Log log = LogFactory.getLog(getClass());
 
     public final static int MAX_NUM_HITS = 1000;
 
     /**
      * Path to the snapshooter script
      */
     private String snapShooterPath = "";
 
     /**
      * Path to the dir containing lucene index
      */
     private String indexDirPath = "";
 
     /**
      * Number of seconds to wait until taking a new snapshot
      */
     private int snapTime = 0;
 
     private Analyzer analyzer;
     private Directory directory;
     private IndexWriter indexWriter;
 
     /**
      * Is this instance the master index node? There can be only one!
      */
     private boolean masterIndex = false;
 
     /**
      * A start-up option to force the Lucene index to be cleared. Intended for infrequent use.
      * This will also disable the snapshot process.
      */
     private boolean clearIndex = false;
 
     /**
      * Conduct a search in the Lucene index based on the supplied Query, constrained by resultStart and resultLimit.
      * <p/>
      * At most this will allow up to MAX_RESULT_LIMIT search hits, with a return window based
      * on resultStart and resultLimit.
      *
      * @param query       to search with
      * @param resultStart 0 based index of first result
      * @param resultLimit results limit
      * @return a List of Lucene Documents
      */
     @Override
     public ResultsWrapper<Document> doSearch(Query query, final int resultStart, final int resultLimit) {
         log.debug("doSearch(query, resultStart, resultLimit)");
         try {
             // Cannot go above MAX_NUM_HITS.
             int numHits = resultStart + resultLimit;
             if (numHits > MAX_NUM_HITS) {
                 numHits = MAX_NUM_HITS;
             }
             // Get Collector limited to numHits + 1, so we can detect truncations.
             TopScoreDocCollector collector = TopScoreDocCollector.create(numHits + 1, true);
             // Get the IndexSearcher and do the search.
             IndexSearcher searcher = new IndexSearcher(getDirectory(), true);
             searcher.search(query, collector);
             // Get hits within our start and limit range.
             ScoreDoc[] hits = collector.topDocs(resultStart, resultLimit + 1).scoreDocs;
             // Assemble List of Documents.
             List<Document> documents = new ArrayList<Document>();
             for (ScoreDoc hit : hits) {
                 documents.add(searcher.doc(hit.doc));
             }
             // Safe to close the IndexSearcher now.
             searcher.close();
             // Trim resultLimit if we're close to MAX_NUM_HITS.
             int resultLimitWithCeiling = resultLimit;
            if (resultStart >= MAX_NUM_HITS) {
                 // Never return results.
                 resultLimitWithCeiling = 0;
             } else if ((resultStart + resultLimit) > MAX_NUM_HITS) {
                 // Only return those results from resultStart to MAX_NUM_HITS.
                 resultLimitWithCeiling = MAX_NUM_HITS - resultStart;
             }
             // Create ResultsWrapper appropriate for our limit.
             return new ResultsWrapper<Document>(
                     documents.size() > resultLimitWithCeiling ? documents.subList(0, resultLimitWithCeiling) : documents,
                    (documents.size() > resultLimitWithCeiling) && !((resultStart + resultLimitWithCeiling) >= MAX_NUM_HITS),
                     resultStart,
                     resultLimit,
                     collector.getTotalHits() > MAX_NUM_HITS ? MAX_NUM_HITS : collector.getTotalHits());
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     /**
      * Conduct a search in the Lucene index based on the supplied Query (unconstrained.
      * <p/>
      * At most this will allow up to MAX_RESULT_LIMIT search hits.
      *
      * @param query to search with
      * @return a List of Lucene Documents
      */
     @Override
     public ResultsWrapper<Document> doSearch(Query query) {
         log.debug("doSearch(query)");
         try {
             // Get Collector limited to numHits + 1, so we can detect truncations.
             TopScoreDocCollector collector = TopScoreDocCollector.create(MAX_NUM_HITS + 1, true);
             // Get the IndexSearcher and do the search.
             IndexSearcher searcher = new IndexSearcher(getDirectory(), true);
             searcher.search(query, collector);
             // Get all hits.
             ScoreDoc[] hits = collector.topDocs().scoreDocs;
             // Assemble List of Documents.
             List<Document> documents = new ArrayList<Document>();
             for (ScoreDoc hit : hits) {
                 documents.add(searcher.doc(hit.doc));
             }
             // Safe to close the IndexSearcher now.
             searcher.close();
             // Create ResultsWrapper containing all Documents.
             return new ResultsWrapper<Document>(
                     documents,
                     documents.size() > MAX_NUM_HITS);
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     @Override
     public synchronized void addDocument(Document document) {
         if (!masterIndex) return;
         try {
             getIndexWriter().addDocument(document);
             closeIndexWriter();
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     @Override
     public synchronized void addDocuments(Collection<Document> documents) {
         if ((documents != null) && !documents.isEmpty()) {
             if (!masterIndex) return;
             try {
                 for (Document document : documents) {
                     getIndexWriter().addDocument(document);
                 }
                 closeIndexWriter();
             } catch (IOException e) {
                 throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
             }
         }
     }
 
     /**
      * This will remove the Document matching the supplied Terms and then add the supplied Document.
      *
      * @param document to add
      * @param terms    Terms to form Query to remove existing Document.
      */
     @Override
     public synchronized void updateDocument(Document document, Term... terms) {
         if (!masterIndex) return;
         BooleanQuery q = new BooleanQuery();
         for (Term t : terms) {
             q.add(new TermQuery(t), BooleanClause.Occur.MUST);
         }
         try {
             getIndexWriter().deleteDocuments(q);
             getIndexWriter().addDocument(document);
             closeIndexWriter();
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     @Override
     public synchronized void deleteDocuments(Term... terms) {
         if (!masterIndex) return;
         BooleanQuery q = new BooleanQuery();
         for (Term t : terms) {
             q.add(new TermQuery(t), BooleanClause.Occur.MUST);
         }
         try {
             getIndexWriter().deleteDocuments(q);
             closeIndexWriter();
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     /**
      * Clear the Lucene index.
      */
     @Override
     public synchronized void clearIndex() {
         try {
             // First ensure index is not locked (perhaps from a crash).
             unlockIndex();
             // Create a new index.
             IndexWriter indexWriter = getNewIndexWriter(true);
             // Close the index.
             indexWriter.optimize();
             indexWriter.commit();
             indexWriter.close();
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
         closeIndexWriter();
     }
 
     /**
      * Unlocks the Lucene index. Useful following JVM crashes.
      */
     @Override
     public synchronized void unlockIndex() {
         try {
             if (IndexReader.indexExists(getDirectory())) {
                 IndexReader.open(getDirectory());
                 if (IndexWriter.isLocked(getDirectory())) {
                     IndexWriter.unlock(getDirectory());
                 }
             }
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     /**
      * Ensure Lucene objects are closed.
      *
      * @throws Throwable the <code>Exception</code> raised by this method
      */
     @Override
     protected synchronized void finalize() throws Throwable {
         super.finalize();
         closeIndexWriter();
         closeDirectory();
     }
 
     /**
      * Get the Analyzer. Will call getNewAnalyzer if it does not yet exist.
      *
      * @return the Analyzer
      */
     private Analyzer getAnalyzer() {
         if (analyzer == null) {
             synchronized (this) {
                 if (analyzer == null) {
                     analyzer = getNewAnalyzer();
                 }
             }
         }
         return analyzer;
     }
 
     /**
      * Create a new Analyzer.
      *
      * @return Analyzer
      */
     private Analyzer getNewAnalyzer() {
         return new StandardAnalyzer(Version.LUCENE_30);
     }
 
     /**
      * Get the Directory. Will call createDirectory if it does not yet exist.
      *
      * @return the Directory
      */
     private synchronized Directory getDirectory() {
         if (directory == null) {
             createDirectory();
         }
         return directory;
     }
 
     /**
      * Open the Directory.
      */
     private void createDirectory() {
         try {
             String path = System.getProperty("amee.lucenePath", "/var/www/apps/platform-api/lucene/index");
             setDirectory(FSDirectory.open(new File(path)));
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     /**
      * Closes the Lucene directory.
      */
     private void closeDirectory() {
         try {
             if (directory != null) {
                 directory.close();
             }
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     /**
      * Set the Directory.
      *
      * @param directory to set
      */
     private void setDirectory(Directory directory) {
         this.directory = directory;
     }
 
     /**
      * Gets IndexWriter. Will call getNewIndexWriter if an IndexWriter is not yet created. Can
      * be called multiple times within a thread. The create parameter is only effective when the
      * IndexWriter has not previously been created.
      * <p/>
      * Later, closeIndexWriter must be called at least once.
      *
      * @return the IndexWriter
      */
     private synchronized IndexWriter getIndexWriter() {
         if (indexWriter == null) {
             indexWriter = getNewIndexWriter(false);
         }
         return indexWriter;
     }
 
     /**
      * Construct and set a new IndexWriter. Should only be called once within a thread.
      * <p/>
      * Later, closeIndexWriter must be called at least once.
      *
      * @param create a new index if true
      * @return IndexWriter
      */
     private IndexWriter getNewIndexWriter(boolean create) {
         try {
             return new IndexWriter(
                     getDirectory(),
                     getAnalyzer(),
                     create,
                     IndexWriter.MaxFieldLength.UNLIMITED);
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     /**
      * Closes the IndexWriter. Will optimise the index prior to closing.
      * <p/>
      * This must be called at least once following previous calls to getIndexWriter.
      */
     private synchronized void closeIndexWriter() {
         if (indexWriter != null) {
             try {
                 indexWriter.optimize();
                 indexWriter.commit();
                 if (!clearIndex && pastSnapTime()) {
                     log.debug("closeIndexWriter() Passed time threshold for snapshot.");
                     takeSnapshot();
                 }
                 indexWriter.close();
                 indexWriter = null;
             } catch (IOException e) {
                 throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
             }
         }
     }
 
     /**
      * Takes a snapshot of the lucene index using the solr snapshooter shell script.
      * http://wiki.apache.org/solr/SolrCollectionDistributionScripts
      */
     private void takeSnapshot() {
         String command = getSnapShooterPath() + " -d " + getIndexDirPath();
         log.debug("takeSnapshot() - executing " + command);
         try {
             Runtime.getRuntime().exec(command);
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     /**
      * @return true if the last snapshot was taken longer than snapTime seconds ago.
      */
     private boolean pastSnapTime() {
         Date now = new Date();
         return (now.getTime() - getLastSnapshotTime()) > (getSnapTime() * 1000);
     }
 
     /**
      * Gets the last modified time of the latest snapshot.
      *
      * @return A long value representing the time the snapshot was taken,
      *         measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970),
      *         or 0L if no snapshot exists or if an I/O error occurs.
      */
     private long getLastSnapshotTime() {
         File snapshotDir = new File(getIndexDirPath());
         File[] snapshotFiles = snapshotDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.startsWith("snapshot");
             }
         });
 
         if (snapshotFiles != null && snapshotFiles.length > 0) {
             Arrays.sort(snapshotFiles, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
             return snapshotFiles[0].lastModified();
         } else {
             return 0L;
         }
     }
 
     @Value("#{ systemProperties['amee.masterIndex'] }")
     public void setMasterIndex(Boolean masterIndex) {
         this.masterIndex = masterIndex;
     }
 
     @Value("#{ systemProperties['amee.clearIndex'] }")
     public void setClearIndex(Boolean clearIndex) {
         this.clearIndex = clearIndex;
     }
 
     public String getIndexDirPath() {
         return indexDirPath;
     }
 
     public void setIndexDirPath(String indexDirPath) {
         this.indexDirPath = indexDirPath;
     }
 
     public String getSnapShooterPath() {
         return snapShooterPath;
     }
 
     public void setSnapShooterPath(String snapShooterPath) {
         this.snapShooterPath = snapShooterPath;
     }
 
     public int getSnapTime() {
         return snapTime;
     }
 
     public void setSnapTime(int snapTime) {
         this.snapTime = snapTime;
     }
 }
