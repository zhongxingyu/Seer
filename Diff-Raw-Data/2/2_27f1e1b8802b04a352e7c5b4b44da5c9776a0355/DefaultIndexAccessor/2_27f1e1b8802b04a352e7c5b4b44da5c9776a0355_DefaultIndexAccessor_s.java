 package com.gentics.cr.lucene.indexaccessor;
 
 /**
  * Derived from code by subshell GmbH
  *
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Searcher;
 import org.apache.lucene.search.Similarity;
 import org.apache.lucene.store.Directory;
 
 /**
  * Provides a default implementation for {@link IndexAccessor}.
  */
 class DefaultIndexAccessor implements IndexAccessor {
 
   protected final static Logger logger = Logger.getLogger(DefaultIndexAccessor.class.getPackage()
       .getName());
 
   private static final int POOL_SIZE = 10;
 
   private Analyzer analyzer;
 
   private IndexReader cachedReadingReader = null;
 
   protected final Map<Similarity, IndexSearcher> cachedSearchers;
 
   private IndexWriter cachedWriter = null;
 
   private IndexReader cachedWritingReader = null;
 
   protected boolean closed = true;
 
   private Directory directory;
 
   protected int readingReaderUseCount = 0;
 
   protected final ExecutorService pool;
   
   protected int numReopening = 0;
 
   protected boolean isReopening = false;
   
   protected int searcherUseCount = 0;
 
   protected int writerUseCount = 0;
 
   protected int numSearchersForRetirment = 0;
 
   protected List<IndexSearcher> createdSearchers = new CopyOnWriteArrayList<IndexSearcher>();
 
   protected int writingReaderUseCount = 0;
 
   /**
    * Creates a new instance with the given {@link Directory} and
    * {@link Analyzer}.
    * 
    * @param dir
    *          the directory on top of which to create the {@link IndexAccessor}.
    * @param analyzer
    *          the analyzer to associate with the {@link IndexAccessor}.
    */
   public DefaultIndexAccessor(Directory dir, Analyzer analyzer) {
     this.directory = dir;
     this.analyzer = analyzer;
     pool = Executors.newFixedThreadPool(POOL_SIZE);
     cachedSearchers = new HashMap<Similarity, IndexSearcher>();
   }
 
   /**
    * Throws an Exception if IndexAccessor is closed. This method assumes it is
    * invoked in a synchronized context.
    */
   private void checkClosed() {
     if (closed) {
       throw new IllegalStateException("index accessor has been closed");
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.IndexAccessor#close()
    */
   public synchronized void close() {
 
     if (closed) {
       return;
     }
     closed = true;
     while ((readingReaderUseCount > 0) || (searcherUseCount > 0) || (writingReaderUseCount > 0)
         || (writerUseCount > 0) || (numReopening > 0)) {
       try {
         wait();
       } catch (InterruptedException e) {
       }
     }
 
     closeCachedReadingReader();
     closeCachedSearchers();
     closeCachedWritingReader();
     closeCachedWriter();
     shutdownAndAwaitTermination(pool);
 //    int i = 0;
 //    for (IndexSearcher s : createdSearchers) {
 //      System.out.println(i++ + ":" + s.getIndexReader().refCount + " :" + s.getIndexReader());
 //    }
   }
 
   /**
    * Closes the cached reading Reader if it has been created. This method
    * assumes it is invoked in a synchronized context.
    */
   protected void closeCachedReadingReader() {
     if (cachedReadingReader == null) {
       return;
     }
 
     if (logger.isLoggable(Level.FINE)) {
       logger.fine("closing cached reading reader");
     }
 
     try {
       cachedReadingReader.close();
     } catch (IOException e) {
       logger.log(Level.SEVERE, "error closing reading Reader", e);
     } finally {
       cachedReadingReader = null;
     }
   }
 
   /**
    * Closes all of the Searchers in the Searcher cache. This method is invoked
    * in a synchronized context.
    */
   protected void closeCachedSearchers() {
     if (logger.isLoggable(Level.FINE)) {
       logger.fine("closing cached searchers (" + cachedSearchers.size() + ")");
     }
 
     for (IndexSearcher searcher : cachedSearchers.values()) {
       try {
         searcher.getIndexReader().close();
       } catch (IOException e) {
         logger.log(Level.SEVERE, "error closing cached Searcher", e);
       }
     }
 
     cachedSearchers.clear();
   }
 
   /**
    * Closes the cached Writer if it has been created. This method is invoked in
    * a synchronized context.
    */
   protected void closeCachedWriter() {
     if (cachedWriter == null) {
       return;
     }
 
     if (logger.isLoggable(Level.FINE)) {
       logger.fine("closing cached writer");
     }
 
     try {
       cachedWriter.close();
     } catch (IOException e) {
       logger.log(Level.SEVERE, "error closing cached Writer", e);
     } finally {
       cachedWriter = null;
     }
   }
 
   /**
    * Closes the cache writing Reader if it has been created. This method is
    * invoked in a synchronized context.
    */
   protected void closeCachedWritingReader() {
     if (cachedWritingReader == null) {
       return;
     }
 
     if (logger.isLoggable(Level.FINE)) {
       logger.fine("closing cached writing reader");
     }
 
     try {
       cachedWritingReader.close();
     } catch (IOException e) {
       logger.log(Level.SEVERE, "error closing cached writing Reader", e);
     } finally {
       cachedWritingReader = null;
     }
   }
 
   
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.IndexAccessor#getReader(boolean)
    */
   public IndexReader getReader(boolean write) throws IOException {
     return write ? getWritingReader() : getReadingReader();
   }
 
   /**
    * Return the reader that was opened for read-only operations, or a new one if
    * it hasn't been opened already.
    */
   private synchronized IndexReader getReadingReader() throws IOException {
 
     checkClosed();
 
     if (cachedReadingReader != null) {
       if (logger.isLoggable(Level.FINE)) {
         logger.fine("returning cached reading reader");
       }
 
       readingReaderUseCount++;
     } else {
       if (logger.isLoggable(Level.FINE)) {
         logger.fine("opening new reading reader and caching it");
       }
 
       cachedReadingReader = IndexReader.open(directory);
       readingReaderUseCount = 1;
     }
 
     notifyAll();
     return cachedReadingReader;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.IndexAccessor#getSearcher()
    */
   public Searcher getSearcher() throws IOException {
     return getSearcher(Similarity.getDefault(), null);
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.IndexAccessor#getSearcher(org.apache.lucene.index.IndexReader)
    */
   public Searcher getSearcher(IndexReader indexReader) throws IOException {
     return getSearcher(Similarity.getDefault(), indexReader);
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.IndexAccessor#getSearcher(org.apache.lucene.search.Similarity,
    *      org.apache.lucene.index.IndexReader)
    */
   public synchronized Searcher getSearcher(Similarity similarity, IndexReader indexReader)
       throws IOException {
 
     checkClosed();
 
     IndexSearcher searcher = cachedSearchers.get(similarity);
     if (searcher != null) {
       if (logger.isLoggable(Level.FINE)) {
         logger.fine("returning cached searcher");
       }
 
       searcherUseCount++;
     } else {
       if (logger.isLoggable(Level.FINE)) {
         logger.fine("opening new searcher and caching it");
       }
       searcher = indexReader != null ? new IndexSearcher(indexReader)
           : new IndexSearcher(directory);
       createdSearchers.add(searcher);
       searcher.setSimilarity(similarity);
       cachedSearchers.put(similarity, searcher);
 
       searcherUseCount++;
     }
 
     notifyAll();
     return searcher;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see org.apache.lucene.indexaccessor.IndexAccessor#getWriter()
    */
   public IndexWriter getWriter() throws IOException {
     return getWriter(true);
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.IndexAccessor#getWriter(boolean, boolean)
    */
   private synchronized IndexWriter getWriter(boolean autoCommit) throws IOException {
 
     checkClosed();
 
     while (writingReaderUseCount > 0) {
       try {
         wait();
       } catch (InterruptedException e) {
       }
     }
 
     if (cachedWriter != null) {
       if (logger.isLoggable(Level.FINE)) {
         logger.fine("returning cached writer:" + Thread.currentThread().getId());
       }
 
       writerUseCount++;
     } else {
       if (logger.isLoggable(Level.FINE)) {
         logger.fine("opening new writer and caching it:" + Thread.currentThread().getId());
       }
 
       cachedWriter = new IndexWriter(directory, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
       writerUseCount = 1;
     }
 
     notifyAll();
     return cachedWriter;
   }
 
   /**
    * Return the reader that was opened for read-write operations, or a new one
    * if it hasn't been opened already.
    */
   private synchronized IndexReader getWritingReader() throws CorruptIndexException, IOException {
 
     checkClosed();
 
     while (writerUseCount > 0) {
       try {
         wait();
       } catch (InterruptedException e) {
       }
     }
 
     if (cachedWritingReader != null) {
       if (logger.isLoggable(Level.FINE)) {
         logger.fine("returning cached writing reader");
       }
 
       writingReaderUseCount++;
     } else {
       if (logger.isLoggable(Level.FINE)) {
         logger.fine("opening new writing reader");
       }
 
      cachedWritingReader = IndexReader.open(directory);
       writingReaderUseCount = 1;
     }
 
     notifyAll();
     return cachedWritingReader;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.IndexAccessor#isOpen()
    */
   public boolean isOpen() {
     return !closed;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.IndexAccessor#open()
    */
   public synchronized void open() {
     closed = false;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.IndexAccessor#readingReadersOut()
    */
   public int readingReadersOut() {
     return readingReaderUseCount;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.IndexAccessor#release(org.apache.lucene.index.IndexReader,
    *      boolean)
    */
   public void release(IndexReader reader, boolean write) {
     if (reader != null) {
       if (write) {
         releaseWritingReader(reader);
       } else {
         releaseReadingReader(reader);
       }
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.IndexAccessor#release(org.apache.lucene.index.IndexWriter)
    */
   public synchronized void release(IndexWriter writer) {
 
     try {
       if (writer != cachedWriter) {
         throw new IllegalArgumentException("writer not opened by this index accessor");
       }
       writerUseCount--;
 
       if (writerUseCount == 0) {
         if (logger.isLoggable(Level.FINE)) {
           logger.fine("closing cached writer:" + Thread.currentThread().getId());
         }
 
         try {
           cachedWriter.close();
         } catch (IOException e) {
           logger.log(Level.SEVERE, "error closing cached Writer:" + Thread.currentThread().getId(),
               e);
         } finally {
           cachedWriter = null;
         }
       }
 
     } finally {
       notifyAll();
     }
 
     if (writerUseCount == 0) {
       numReopening++;
       pool.execute(new Runnable() {
         public void run() {
           synchronized (DefaultIndexAccessor.this) {
             if(numReopening > 5) {
               // there are too many reopens pending, so just bail
               numReopening--;
               return;
             }
             waitForReadersAndReopenCached();
             numReopening--;
             DefaultIndexAccessor.this.notifyAll();
           }
         }
       });
 
     }
 
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.IndexAccessor#release(org.apache.lucene.search.Searcher)
    */
   public synchronized void release(Searcher searcher) {
     // TODO: could clear searchers here, but may be better to start a timer that does it periodically
     // if (warm && retiredSearchers.size() > 0) {
     // pool.execute(new Runnable() {
     // public void run() {
     // synchronized (DefaultIndexAccessor.this) {
     // retireSearchers();
     // }
     // }
     //
     // });
     // }
 
     searcherUseCount--;
     notifyAll();
   }
 
   /** Release the reader that was opened for read-only operations. */
   private synchronized void releaseReadingReader(IndexReader reader) {
     if (reader == null) {
       return;
     }
 
     if (reader != cachedReadingReader) {
       throw new IllegalArgumentException("reading reader not opened by this index accessor");
     }
 
     readingReaderUseCount--;
     notifyAll();
   }
 
   /** Release the reader that was opened for read-write operations. */
   private synchronized void releaseWritingReader(IndexReader reader) {
     if (reader == null) {
       return;
     }
 
     try {
     	
       if (reader != cachedWritingReader) {
         throw new IllegalArgumentException("writing Reader not opened by this index accessor");
       }
 
       writingReaderUseCount--;
 
       if (writingReaderUseCount == 0) {
         if (logger.isLoggable(Level.FINE)) {
           logger.fine("closing cached writing Reader");
         }
 
         try {
           cachedWritingReader.close();
         } catch (IOException e) {
         } finally {
           cachedWritingReader = null;
         }
       }
     } finally {
       notifyAll();
     }
 
     if (writingReaderUseCount == 0) {
       numReopening++;
       pool.execute(new Runnable() {
         public void run() {
           synchronized (DefaultIndexAccessor.this) {
             if(numReopening > 5) {
               logger.log(Level.WARNING,"Too many reopens");
             }
             waitForReadersAndReopenCached();
             numReopening--;
             DefaultIndexAccessor.this.notifyAll();
           }
         }
       });
     }
   }
 
   /**
    * Reopens all of the Searchers in the Searcher cache. This method is invoked
    * in a synchronized context.
    */
   private void reopenCachedSearchers() {
     if (logger.isLoggable(Level.FINE)) {
       logger.fine("reopening cached searchers (" + cachedSearchers.size() + "):"
           + Thread.currentThread().getId());
     }
     Set<Similarity> keys = cachedSearchers.keySet();
     for (Similarity key : keys) {
       IndexSearcher searcher = cachedSearchers.get(key);
       try {
         IndexReader oldReader = searcher.getIndexReader();
         IndexSearcher oldSearcher = searcher;
         IndexReader newReader = oldReader.reopen();
 
         if (newReader != oldReader) {
 
           cachedSearchers.remove(key);
           searcher = new IndexSearcher(newReader);
           searcher.setSimilarity(oldSearcher.getSimilarity());
           oldSearcher.getIndexReader().close();
           cachedSearchers.put(key, searcher);
 
         }
 
       } catch (IOException e) {
         logger.log(Level.SEVERE, "error reopening cached Searcher", e);
       }
     }
 
   }
 
   /**
    * Reopens the cached reading Reader. This method assumes it is invoked in a
    * synchronized context.
    */
   private void reopenReadingReader() {
     if (cachedReadingReader == null) {
       return;
     }
 
     if (logger.isLoggable(Level.FINE)) {
       logger.fine("reopening cached reading reader");
     }
     IndexReader oldReader = cachedReadingReader;
     try {
       cachedReadingReader = cachedReadingReader.reopen();
       if (oldReader != cachedReadingReader) {
         oldReader.close();
       }
     } catch (IOException e) {
       logger.log(Level.SEVERE, "error reopening reading Reader", e);
     }
 
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.IndexAccessor#activeSearchers()
    */
   public int searcherUseCount() {
     return searcherUseCount;
   }
 
   protected void shutdownAndAwaitTermination(ExecutorService pool) {
     pool.shutdown(); // Disable new tasks from being submitted
     try {
       // Wait a while for existing tasks to terminate
       if (!pool.awaitTermination(20, TimeUnit.SECONDS)) {
         pool.shutdownNow(); // Cancel currently executing tasks
         // Wait a while for tasks to respond to being cancelled
         if (!pool.awaitTermination(60, TimeUnit.SECONDS))
           System.err.println("Pool did not terminate");
       }
     } catch (InterruptedException ie) {
       // (Re-)Cancel if current thread also interrupted
       pool.shutdownNow();
       // Preserve interrupt status
       Thread.currentThread().interrupt();
     }
   }
 
   /** This method assumes it is invoked in a synchronized context. */
   private void waitForReadersAndReopenCached() {
     while ((readingReaderUseCount > 0) || (searcherUseCount > 0)) {
       try {
         wait();
       } catch (InterruptedException e) {
       }
     }
     if(numReopening > 1) {
       // there are other calls to reopen pending, so we can bail
       return;
     }
     reopenReadingReader();
     reopenCachedSearchers();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.IndexAccessor#writersOut()
    */
   public int writerUseCount() {
     return writerUseCount;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.IndexAccessor#writingReadersOut()
    */
   public int writingReadersUseCount() {
     return writingReaderUseCount;
   }
 
 }
