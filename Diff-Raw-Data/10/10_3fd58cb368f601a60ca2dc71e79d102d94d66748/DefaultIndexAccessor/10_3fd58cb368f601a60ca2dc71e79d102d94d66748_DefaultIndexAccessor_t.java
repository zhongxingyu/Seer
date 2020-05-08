 package com.gentics.cr.lucene.indexaccessor;
 
 /**
  * Derived from code by subshell GmbH
  *
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.	See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.	You may obtain a copy of the License at
  *
  *		 http://www.apache.org/licenses/LICENSE-2.0
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
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Similarity;
 import org.apache.lucene.store.Directory;
 
 /**
  * Provides a default implementation for {@link IndexAccessor}.
  */
 class DefaultIndexAccessor implements IndexAccessor {
 
 	/**
 	 * Creates Threads starting with a certain name
 	 * @author bigbear3001
 	 *
 	 */
 	private static final class NamedThreadFactory implements ThreadFactory {
 		/**
 		 * Counter for naming the threads
 		 */
 		private int i = 0;
 		
 		/**
 		 * Base pool name
 		 */
 		private String poolName;
 
 		/**
 		 * Created a new thread factory that creates a named thread pool
 		 * @param name
 		 */
 		public NamedThreadFactory(String name) {
 			poolName = name + ".pool-";
 		}
 
 		/**
 		 * @return next number for naming the threads
 		 */
 		private synchronized int getNextNumber() {
 			return i++;
 		}
 
 		/**
 		 * Constructs a new thread named by the {@link NamedThreadFactory} ".pool-" and the next available number.
 		 */
 		public Thread newThread(Runnable r) {
 			return new Thread(r, poolName + getNextNumber());
 		}
 	}
 
 	/**
 	 * Log4j logger for error and debug messages.
 	 */
 	private static final Logger LOGGER = Logger.getLogger(DefaultIndexAccessor.class);
 
 	/**
 	 * Pool size for handling threads
 	 */
 	private static final int POOL_SIZE = 2;
 
 	private Analyzer analyzer;
 
 	private IndexReader cachedReadingReader = null;
 	
 	private final Map<IndexReader, Integer> oldReadingReaders = new HashMap<IndexReader, Integer>();
 
 	/**
 	 * cache for searchers.
 	 */
 	protected final Map<Similarity, IndexSearcher> cachedSearchers;
 
 	private IndexWriter cachedWriter = null;
 
 	private IndexReader cachedWritingReader = null;
 
 	protected boolean closed = true;
 
 	private Directory directory;
 
 	protected int readingReaderUseCount = 0;
 
 	protected ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE, new NamedThreadFactory(DefaultIndexAccessor.class.getSimpleName()));
 
 	protected int numReopening = 0;
 
 	protected boolean isReopening = false;
 
 	/**
 	 * use count for cached searcher.
 	 */
 	protected int searcherUseCount = 0;
 
 	protected int writerUseCount = 0;
 
 	protected int numSearchersForRetirment = 0;
 
 	protected int writingReaderUseCount = 0;
 
 	/**
 	 * marker for closing searchers currently in use. this is used when the
 	 * index should be reopened.
 	 * @see #reopen()
 	 */
 	private boolean closeAllReleasedSearchers = true;
 
 	/**
 	 * Creates a new instance with the given {@link Directory} and
 	 * {@link Analyzer}.
 	 * 
 	 * @param dir the directory on top of which to create the
 	 * {@link IndexAccessor}.
 	 * @param indexAnalyzer the analyzer to associate with the
 	 * {@link IndexAccessor}.
 	 */
 	public DefaultIndexAccessor(final Directory dir, final Analyzer indexAnalyzer) {
 		directory = dir;
 		analyzer = indexAnalyzer;
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
 	 * @see com.mhs.indexaccessor.IndexAccessor#close()
 	 */
 	public synchronized void close() {
 
 		if (closed) {
 			return;
 		}
 		closed = true;
 		while (readingReaderUseCount > 0 || searcherUseCount > 0 || writingReaderUseCount > 0 || writerUseCount > 0 || numReopening > 0) {
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
 	}
 
 	/**
 	 * Closes the cached reading Reader if it has been created. This method
 	 * assumes it is invoked in a synchronized context.
 	 */
 	protected void closeCachedReadingReader() {
 		if (cachedReadingReader == null) {
 			return;
 		}
 		LOGGER.debug("closing cached reading reader");
 
 		try {
 			cachedReadingReader.close();
 			for (Entry<IndexReader, Integer> entry: oldReadingReaders.entrySet()) {
 				entry.getKey().close();
 			}
 		} catch (IOException e) {
 			LOGGER.error("error closing reading Reader", e);
 		} finally {
 			cachedReadingReader = null;
 			oldReadingReaders.clear();
 		}
 	}
 
 	/**
 	 * Closes all of the Searchers in the Searcher cache. This method is invoked
 	 * in a synchronized context.
 	 */
 	protected void closeCachedSearchers() {
 		LOGGER.debug("closing cached searchers (" + cachedSearchers.size() + ")");
 
 		for (IndexSearcher searcher : cachedSearchers.values()) {
 			try {
 				searcher.getIndexReader().close();
 			} catch (IOException e) {
 				LOGGER.error("error closing cached Searcher", e);
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
 		LOGGER.debug("closing cached writer");
 
 		try {
 			cachedWriter.close();
 		} catch (IOException e) {
 			LOGGER.error("error closing cached Writer", e);
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
 		LOGGER.debug("closing cached writing reader");
 		try {
 			cachedWritingReader.close();
 		} catch (IOException e) {
 			LOGGER.error("error closing cached writing Reader", e);
 		} finally {
 			cachedWritingReader = null;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
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
 			LOGGER.debug("returning cached reading reader");
 			readingReaderUseCount++;
 		} else {
 			LOGGER.debug("opening new reading reader and caching it");
 
 			cachedReadingReader = IndexReader.open(directory);
 			readingReaderUseCount = 1;
 		}
 
 		notifyAll();
 		return cachedReadingReader;
 	}
 
 	/**
 	 * Fetches a double checked Searcher that has been checked for the presence of a reopen file
 	 * Note that it may occure that a prioritized Searcher may be reopened twice.
 	 * @param indexLocation 
 	 * @return
 	 * @throws IOException
 	 */
 	public IndexSearcher getPrioritizedSearcher() throws IOException {
 		boolean reopened = this.numReopening > 0;
 		IndexSearcher searcher = (IndexSearcher) getSearcher();
 
 		if (reopened) {
 			//REOPEN SEARCHER AS IT WAS PRIORITIZED
 			synchronized (DefaultIndexAccessor.this) {
 				IndexReader reader = searcher.getIndexReader();
 				IndexSearcher oldSearcher = searcher;
 				IndexReader newReader = reader.reopen();
 				if (newReader != reader) {
 					searcher = new IndexSearcher(newReader);
 					searcher.setSimilarity(oldSearcher.getSimilarity());
 					oldSearcher.getIndexReader().close();
 					for (Map.Entry<Similarity, IndexSearcher> e : cachedSearchers.entrySet()) {
 						if (e.getValue() == oldSearcher) {
 							cachedSearchers.put(e.getKey(), searcher);
 						}
 					}
 				}
 			}
 		}
 
 		return searcher;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.mhs.indexaccessor.IndexAccessor#getSearcher()
 	 */
 	public IndexSearcher getSearcher() throws IOException {
 		return getSearcher(Similarity.getDefault(), null);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.mhs.indexaccessor.IndexAccessor#getSearcher(org.apache.lucene.index.IndexReader)
 	 */
 	public IndexSearcher getSearcher(IndexReader indexReader) throws IOException {
 		return getSearcher(Similarity.getDefault(), indexReader);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.mhs.indexaccessor.IndexAccessor#getSearcher(org.apache.lucene.search.Similarity, org.apache.lucene.index.IndexReader)
 	 */
 	public synchronized IndexSearcher getSearcher(Similarity similarity, IndexReader indexReader) throws IOException {
 
 		checkClosed();
 
 		IndexSearcher searcher = cachedSearchers.get(similarity);
 		if (searcher != null) {
 			LOGGER.debug("returning cached searcher");
 		} else {
 			LOGGER.debug("opening new searcher and caching it");
 			searcher = indexReader != null ? new IndexSearcher(indexReader) : new IndexSearcher(directory);
 			searcher.setSimilarity(similarity);
 			cachedSearchers.put(similarity, searcher);
 		}
 		searcherUseCount++;
 		notifyAll();
 		return searcher;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.apache.lucene.indexaccessor.IndexAccessor#getWriter()
 	 */
 	public IndexWriter getWriter() throws IOException {
 		return getWriter(true);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.mhs.indexaccessor.IndexAccessor#getWriter(boolean, boolean)
 	 */
 	private synchronized IndexWriter getWriter(boolean autoCommit) throws IOException {
 
 		checkClosed();
 		if (LOGGER.isDebugEnabled() && writingReaderUseCount > 0) {
 			LOGGER.debug("writer already used (" + writingReaderUseCount + "), waiting for job to release it.");
 		}
 		while (writingReaderUseCount > 0) {
 			try {
 				wait();
 			} catch (InterruptedException e) {
 			}
 		}
 
 		if (cachedWriter != null) {
 			LOGGER.debug("returning cached writer:" + Thread.currentThread().getId());
 
 			writerUseCount++;
 		} else {
 			LOGGER.debug("opening new writer and caching it:" + Thread.currentThread().getId());
 
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
 			LOGGER.debug("returning cached writing reader");
 			writingReaderUseCount++;
 		} else {
 			LOGGER.debug("opening new writing reader");
 			cachedWritingReader = IndexReader.open(directory, false);
 			writingReaderUseCount = 1;
 		}
 
 		notifyAll();
 		return cachedWritingReader;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.mhs.indexaccessor.IndexAccessor#isOpen()
 	 */
 	public boolean isOpen() {
 		return !closed;
 	}
 
 	public boolean isLocked() {
 		boolean locked = false;
 		try {
 			locked = IndexWriter.isLocked(directory);
 		} catch (IOException e) {
 			LOGGER.error(e);
 		}
 		return locked;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.mhs.indexaccessor.IndexAccessor#open()
 	 */
 	public synchronized void open() {
 		closed = false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.mhs.indexaccessor.IndexAccessor#readingReadersOut()
 	 */
 	public int readingReadersOut() {
 		return readingReaderUseCount;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.mhs.indexaccessor.IndexAccessor#release(org.apache.lucene.index.IndexReader, boolean)
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
 	 * @see com.mhs.indexaccessor.IndexAccessor#release(org.apache.lucene.index.IndexWriter)
 	 */
 	public synchronized void release(IndexWriter writer) {
 
 		try {
 			if (writer != cachedWriter) {
 				throw new IllegalArgumentException("writer not opened by this index accessor");
 			}
 			writerUseCount--;
 
 			if (writerUseCount == 0) {
 				LOGGER.debug("closing cached writer:" + Thread.currentThread().getId());
 
 				try {
 					cachedWriter.close();
 				} catch (IOException e) {
 					LOGGER.error("error closing cached Writer:" + Thread.currentThread().getId(), e);
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
 						if (numReopening > 5) {
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
 	 * @see com.mhs.indexaccessor.IndexAccessor#release(org.apache.lucene.search.Searcher)
 	 */
 	public synchronized void release(IndexSearcher searcher) {
 		searcherUseCount--;
 		if (searcherUseCount == 0 && closeAllReleasedSearchers) {
 			closeCachedSearchers();
 			closeAllReleasedSearchers = false;
 		}
 		notifyAll();
 	}
 
 	/** Release the reader that was opened for read-only operations. */
 	private synchronized void releaseReadingReader(IndexReader reader) {
 		// do nothing if no reader was passed to the method or the reader was already released
 		if (reader == null || readingReaderUseCount == 0) {
 			return;
 		}
 		// check if reader is one of the old reading readers
 		if (reader != cachedReadingReader && oldReadingReaders.get(reader) != null) {
 			Integer oldReaderUseCount = oldReadingReaders.get(reader) - 1;
 			if (oldReaderUseCount > 0) {
 				oldReadingReaders.put(reader, oldReaderUseCount);
 			} else {
 				oldReadingReaders.remove(reader);
 				// close unused old reader
 				try {
 					reader.close();
 				} catch (IOException e) {
 					LOGGER.error("error closing reading Reader", e);
 				}	
 			}
 		} else if (reader != cachedReadingReader) {
 			throw new IllegalArgumentException("reading reader not opened by this index accessor");
 		} else {
 			readingReaderUseCount--;
 		}
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
 				LOGGER.debug("closing cached writing Reader");
 
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
 						if (numReopening > 5) {
 							LOGGER.warn("Too many reopens");
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
 		LOGGER.debug("reopening cached searchers (" + cachedSearchers.size() + "):" + Thread.currentThread().getId());
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
 				LOGGER.error("error reopening cached Searcher", e);
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
 		
 		LOGGER.debug("reopening cached reading reader");
 		IndexReader oldReader = cachedReadingReader;
 		if(readingReaderUseCount > 0) {
 			// if the reading reader is currently in use, save it with use count in Map
 			oldReadingReaders.put(oldReader, readingReaderUseCount);
 		}		
 		try {
 			cachedReadingReader = cachedReadingReader.reopen();
 			if (oldReader != cachedReadingReader) {
 				// if the old reader was not used close it 
 				if(readingReaderUseCount == 0) {
 					oldReader.close();
 				} else {
 					// set use count to 0 for the new reader
 					readingReaderUseCount = 0;
 				}
 			}
 		} catch (IOException e) {
 			LOGGER.error("error reopening reading Reader", e);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
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
 				if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
 					System.err.println("Pool did not terminate");
 				}
 			}
 		} catch (InterruptedException ie) {
 			// (Re-)Cancel if current thread also interrupted
 			pool.shutdownNow();
 			// Preserve interrupt status
 			Thread.currentThread().interrupt();
 		}
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		close();
 		super.finalize();
 	}
 
 	/** This method assumes it is invoked in a synchronized context. */
 	private void waitForReadersAndReopenCached() {
 		while (readingReaderUseCount > 0 || searcherUseCount > 0) {
 			try {
 				wait();
 			} catch (InterruptedException e) {
 			}
 		}
 		if (numReopening > 1) {
 			// there are other calls to reopen pending, so we can bail
 			return;
 		}
 		reopenReadingReader();
 		reopenCachedSearchers();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.mhs.indexaccessor.IndexAccessor#writersOut()
 	 */
 	public int writerUseCount() {
 		return writerUseCount;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.mhs.indexaccessor.IndexAccessor#writingReadersOut()
 	 */
 	public int writingReadersUseCount() {
 		return writingReaderUseCount;
 	}
 
 	/**
 	 * reopen the index by releasing all writers and searchers. this is useful
 	 * if the index is changed from outside the jvm.
 	 * @throws IOException - TODO javadoc
 	 */
 	public void reopen() throws IOException {
 		//TODO only open writer if there are already some open
 		if (this.cachedWriter != null) {
 			IndexWriter tempWriter = this.getWriter();
 			this.release(tempWriter);
 		}
 		releaseAllSearchers();
 		synchronized (DefaultIndexAccessor.this) {
 			reopenReadingReader();
 		}
 	}
 
 	/**
 	 * release all searchers to reopen the index.
 	 * @see #reopen()
 	 */
 	private synchronized void releaseAllSearchers() {
 		LOGGER.debug("release all cached searchers");
 		if (searcherUseCount() == 0 && cachedSearchers.size() > 0) {
 			closeCachedSearchers();
 		} else {
 			closeAllReleasedSearchers = true;
 		}
 	}
 	
 
 }
