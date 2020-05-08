 /**
  * Copyright (c) 2002-2011 "Neo Technology,"
  * Network Engine for Objects in Lund AB [http://neotechnology.com]
  *
  * This file is part of Neo4j.
  *
  * Neo4j is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.neo4j.index.lucene;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.nio.ByteBuffer;
 import java.nio.channels.ReadableByteChannel;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.LowerCaseFilter;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.WhitespaceTokenizer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Field.Index;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriter.MaxFieldLength;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.neo4j.kernel.Config;
 import org.neo4j.kernel.impl.cache.LruCache;
 import org.neo4j.kernel.impl.transaction.xaframework.LogBackedXaDataSource;
 import org.neo4j.kernel.impl.transaction.xaframework.XaCommand;
 import org.neo4j.kernel.impl.transaction.xaframework.XaCommandFactory;
 import org.neo4j.kernel.impl.transaction.xaframework.XaConnection;
 import org.neo4j.kernel.impl.transaction.xaframework.XaContainer;
 import org.neo4j.kernel.impl.transaction.xaframework.XaDataSource;
 import org.neo4j.kernel.impl.transaction.xaframework.XaLogicalLog;
 import org.neo4j.kernel.impl.transaction.xaframework.XaTransaction;
 import org.neo4j.kernel.impl.transaction.xaframework.XaTransactionFactory;
 import org.neo4j.kernel.impl.util.ArrayMap;
 
 /**
  * An {@link XaDataSource} optimized for the {@link LuceneIndexService}.
  * This class is public because the XA framework requires it.
  */
 public class LuceneDataSource extends LogBackedXaDataSource
 {
     /**
      * Default {@link Analyzer} for fulltext parsing.
      */
     public static final Analyzer LOWER_CASE_WHITESPACE_ANALYZER =
         new Analyzer()
     {
         @Override
         public TokenStream tokenStream( String fieldName, Reader reader )
         {
             return new LowerCaseFilter( new WhitespaceTokenizer( reader ) );
         }
     };
 
     private final Map<String, IndexWriter> recoveryWriters = new HashMap<String, IndexWriter>();
     
     private final ArrayMap<String,IndexSearcherRef> indexSearchers = 
         new ArrayMap<String,IndexSearcherRef>( 6, true, true );
 
     private final XaContainer xaContainer;
     private final String storeDir;
     private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(); 
     private final Analyzer fieldAnalyzer;
     private final LuceneIndexStore store;
     private LuceneIndexService indexService;
     
     private Map<String,LruCache<String,Collection<Long>>> caching = 
         Collections.synchronizedMap( 
             new HashMap<String,LruCache<String,Collection<Long>>>() );
 
     /**
      * Constructs this data source.
      * 
      * @param params XA parameters.
      * @throws InstantiationException if the data source couldn't be
      * instantiated
      */
     public LuceneDataSource( Map<Object,Object> params ) 
         throws InstantiationException
     {
         super( params );
         this.storeDir = (String) params.get( "dir" );
         this.fieldAnalyzer = instantiateAnalyzer();
         String dir = storeDir;
         File file = new File( dir );
         if ( !file.exists() )
         {
             try
             {
                 autoCreatePath( dir );
             }
             catch ( IOException e )
             {
                 throw new RuntimeException(
                     "Unable to create directory " + dir, e );
             }
         }
         this.store = new LuceneIndexStore( storeDir + "/lucene-store.db" );
         XaCommandFactory cf = new LuceneCommandFactory();
         XaTransactionFactory tf = new LuceneTransactionFactory( store );
         xaContainer = XaContainer.create( this, dir + "/lucene.log", cf, tf, params );
         setLogicalLogAtCreationTime( xaContainer.getLogicalLog() );
         try
         {
             xaContainer.openLogicalLog();
         }
         catch ( IOException e )
         {
             throw new RuntimeException( "Unable to open lucene log in " + dir,
                 e );
         }
         configureLog( params );
     }
     
     protected XaLogicalLog getLogicalLog()
     {
         return xaContainer.getLogicalLog();
     }
     
     protected void configureLog( Map<?,?> config )
     {
         if ( shouldKeepLog( (String) config.get( Config.KEEP_LOGICAL_LOGS ), "lucene" ) )
         {
             getLogicalLog().setKeepLogs( true );
         }
     }
    
     /**
     * This is here so that {@link LuceneIndexService#formQuery(String, Object)}
     * can be used when getting stuff from inside a transaction.
     * @param indexService the {@link LuceneIndexService} instance which
     * created it.
      */
     protected void setIndexService( LuceneIndexService indexService )
     {
         this.indexService = indexService;
     }
     
     /**
      * @return the {@link LuceneIndexService} instance associated with this
      * data source.
      */
     public LuceneIndexService getIndexService()
     {
         return this.indexService;
     }
 
     private Analyzer instantiateAnalyzer()
     {
         return LOWER_CASE_WHITESPACE_ANALYZER;
     }
 
     private void autoCreatePath( String dirs ) throws IOException
     {
         File directories = new File( dirs );
         if ( !directories.exists() )
         {
             if ( !directories.mkdirs() )
             {
                 throw new IOException( "Unable to create directory path["
                     + dirs + "] for Neo4j store." );
             }
         }
     }
 
     @Override
     public void close()
     {
         for ( IndexSearcherRef searcher : indexSearchers.values() )
         {
             try
             {
                 searcher.dispose();
             }
             catch ( IOException e )
             {
                 e.printStackTrace();
             }
         }
         indexSearchers.clear();
         xaContainer.close();
         store.close();
     }
 
     @Override
     public XaConnection getXaConnection()
     {
         return new LuceneXaConnection( storeDir, xaContainer
             .getResourceManager(), getBranchId() );
     }
     
     protected Analyzer getAnalyzer()
     {
         return this.fieldAnalyzer;
     }
     
     private class LuceneCommandFactory extends XaCommandFactory
     {
         LuceneCommandFactory()
         {
             super();
         }
 
         @Override
         public XaCommand readCommand( ReadableByteChannel channel, 
             ByteBuffer buffer ) throws IOException
         {
             return LuceneCommand.readCommand( channel, buffer );
         }
     }
     
     private class LuceneTransactionFactory extends XaTransactionFactory
     {
         private final LuceneIndexStore store;
         
         LuceneTransactionFactory( LuceneIndexStore store )
         {
             this.store = store;
         }
         
         @Override
         public XaTransaction create( int identifier )
         {
             return createTransaction( identifier, this.getLogicalLog() );
         }
 
         @Override
         public void flushAll()
         {
             // Not much we can do...
         }
 
         @Override
         public long getCurrentVersion()
         {
             return store.getVersion();
         }
         
         @Override
         public long getAndSetNewVersion()
         {
             return store.incrementVersion();
         }
 
         @Override
         public long getLastCommittedTx()
         {
             return store.getLastCommittedTx();
         }
         
         @Override
         public void recoveryComplete()
         {
             for ( Map.Entry<String, IndexWriter> entry : recoveryWriters.entrySet() )
             {
                 removeWriter( entry.getKey(), entry.getValue() );
             }
             recoveryWriters.clear();
         }
     }
     
     void getReadLock()
     {
         lock.readLock().lock();
     }
     
     void releaseReadLock()
     {
         lock.readLock().unlock();
     }
     
     void getWriteLock()
     {
         lock.writeLock().lock();
     }
     
     void releaseWriteLock()
     {
         lock.writeLock().unlock();
     }
     
     /**
      * If nothing has changed underneath (since the searcher was last created
      * or refreshed) {@code null} is returned. But if something has changed a
      * refreshed searcher is returned. It makes use if the
      * {@link IndexReader#reopen()} which faster than opening an index from
      * scratch.
      * 
      * @param searcher the {@link IndexSearcher} to refresh.
      * @return a refreshed version of the searcher or, if nothing has changed,
      * {@code null}.
      * @throws IOException if there's a problem with the index.
      */
     private IndexSearcherRef refreshSearcher( IndexSearcherRef searcher )
     {
         try
         {
             IndexReader reader = searcher.getSearcher().getIndexReader();
             IndexReader reopened = reader.reopen();
             if ( reopened != reader )
             {
                 IndexSearcher newSearcher = new IndexSearcher( reopened );
                 searcher.detachOrClose();
                 return new IndexSearcherRef( searcher.getKey(), newSearcher );
             }
             return null;
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
     }
     
     private Directory getDirectory( String key ) throws IOException
     {
         return FSDirectory.open( getIndexDir( key ) );
     }
     
     private File getIndexDir( String key )
     {
         return new File( storeDir, key );
     }
     
     IndexSearcherRef getIndexSearcher( String key )
     {
         try
         {
             IndexSearcherRef searcher = indexSearchers.get( key );
             if ( searcher == null )
             {
                 Directory dir = getDirectory( key );
                 try
                 {
                     String[] files = dir.listAll();
                     if ( files == null || files.length == 0 )
                     {
                         return null;
                     }
                 }
                 catch ( IOException e )
                 {
                     return null;
                 }
                 IndexReader indexReader = IndexReader.open( dir, false );
                 IndexSearcher indexSearcher = new IndexSearcher( indexReader );
                 searcher = new IndexSearcherRef( key, indexSearcher );
                 indexSearchers.put( key, searcher );
             }
             return searcher;
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
     }
 
     XaTransaction createTransaction( int identifier,
         XaLogicalLog logicalLog )
     {
         return new LuceneTransaction( identifier, logicalLog, this );
     }
 
     void invalidateIndexSearcher( String key )
     {
         IndexSearcherRef searcher = indexSearchers.get( key );
         if ( searcher != null )
         {
             IndexSearcherRef refreshedSearcher = refreshSearcher( searcher );
             if ( refreshedSearcher != null )
             {
                 indexSearchers.put( key, refreshedSearcher );
             }
         }
     }
     
     void closeIndexSearcher( String key )
     {
         try
         {
             IndexSearcherRef searcher = indexSearchers.remove( key );
             if ( searcher != null )
             {
                 searcher.dispose();
             }
         }
         catch ( IOException e )
         { // OK
         }
     }
     
     synchronized IndexWriter getRecoveryIndexWriter( String key )
     {
         IndexWriter writer = recoveryWriters.get( key );
         if ( writer == null )
         {
             writer = getIndexWriter( key );
             recoveryWriters.put( key, writer );
         }
         return writer;
     }
     
     synchronized void removeRecoveryIndexWriter( String key )
     {
         recoveryWriters.remove( key );
     }
 
     synchronized IndexWriter getIndexWriter( String key )
     {
         try
         {
             Directory dir = getDirectory( key );
             IndexWriter writer = new IndexWriter( dir, getAnalyzer(),
                 MaxFieldLength.UNLIMITED );
             
             // TODO We should tamper with this value and see how it affects the
             // general performance. Lucene docs says rather <10 for mixed
             // reads/writes 
 //            writer.setMergeFactor( 8 );
             
             return writer;
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
     }
     
     /*
      * Returns true if the entire index was deleted (even on disk)
      */
     protected boolean deleteDocumentsUsingWriter( IndexWriter writer,
         Long nodeId, String key, Object value )
     {
         try
         {
             if ( nodeId == null && value == null )
             {
                 writer.close();
                 deleteIndex( key );
                 return true;
             }
             else
             {
                 BooleanQuery query = new BooleanQuery();
                 if ( value != null )
                 {
                     query.add( new TermQuery( new Term( getDeleteDocumentsKey(),
                         value.toString() ) ), Occur.MUST );
                 }
                 query.add( new TermQuery( new Term(
                     LuceneIndexService.DOC_ID_KEY, "" + nodeId ) ),
                     Occur.MUST );
                 writer.deleteDocuments( query );
                 return false;
             }
         }
         catch ( IOException e )
         {
             throw new RuntimeException( "Unable to delete for " + nodeId + ","
                 + "," + value + " using" + writer, e );
         }
     }
     
     private void deleteIndex( String key )
     {
         deleteFileOrDirectory( getIndexDir( key ) );
     }
 
     private static void deleteFileOrDirectory( File file )
     {
         if ( file.exists() )
         {
             if ( file.isDirectory() )
             {
                 for ( File child : file.listFiles() )
                 {
                     deleteFileOrDirectory( child );
                 }
             }
             file.delete();
         }
     }
     
     protected String getDeleteDocumentsKey()
     {
         return LuceneIndexService.DOC_INDEX_KEY;
     }
 
     void removeWriter( String key, IndexWriter writer )
     {
         try
         {
             writer.close();
         }
         catch ( IOException e )
         {
             throw new RuntimeException( "Unable to close lucene writer "
                 + writer, e );
         }
     }
 
     LruCache<String,Collection<Long>> getFromCache( String key )
     {
         return caching.get( key );
     }
 
     void enableCache( String key, int maxNumberOfCachedEntries )
     {
         this.caching.put( key, new LruCache<String,Collection<Long>>( key,
             maxNumberOfCachedEntries, null ) );
     }
     
     /**
      * Returns the enabled cache size or {@code null} if not enabled
      * for {@code key}.
      * @param key the key to get the cache size for.
      * @return the cache size for {@code key} or {@code null}.
      */
     Integer getEnabledCacheSize( String key )
     {
         LruCache<String, Collection<Long>> cache = this.caching.get( key );
         return cache != null ? cache.maxSize() : null;
     }
 
     void invalidateCache( String key, Object value )
     {
         LruCache<String,Collection<Long>> cache = caching.get( key );
         if ( cache != null )
         {
             cache.remove( value.toString() );
         }
     }
     
     void invalidateCache( String key )
     {
         caching.remove( key );
     }
     
     void invalidateCache()
     {
         caching.clear();
     }
 
     protected void fillDocument( Document document, long nodeId, String key,
         Object value )
     {
         document.add( new Field( LuceneIndexService.DOC_ID_KEY,
             String.valueOf( nodeId ), Field.Store.YES,
             Field.Index.NOT_ANALYZED ) );
         document.add( new Field( LuceneIndexService.DOC_INDEX_KEY,
             value.toString(), Field.Store.NO,
             getIndexStrategy( key, value ) ) );
     }
 
     protected Index getIndexStrategy( String key, Object value )
     {
         return Field.Index.NOT_ANALYZED;
     }
 
     @Override
     public long getCreationTime()
     {
         return store.getCreationTime();
     }
     
     @Override
     public long getRandomIdentifier()
     {
         return store.getRandomNumber();
     }
     
     @Override
     public long getCurrentLogVersion()
     {
         return store.getVersion();
     }
     
     @Override
     public long getLastCommittedTxId()
     {
         return store.getLastCommittedTx();
     }
     
     public void setLastCommittedTxId( long txId )
     {
         store.setLastCommittedTx( txId );
     }
     
     @Override
     public XaContainer getXaContainer()
     {
         return xaContainer;
     }
 }
