 /*
  * Copyright (c) 2002-2009 "Neo Technology,"
  *     Network Engine for Objects in Lund AB [http://neotechnology.com]
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
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.transaction.Synchronization;
 import javax.transaction.SystemException;
 import javax.transaction.Transaction;
 import javax.transaction.TransactionManager;
 import javax.transaction.xa.XAResource;
 
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.Hits;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.TermQuery;
 import org.neo4j.commons.iterator.CombiningIterator;
 import org.neo4j.commons.iterator.IteratorAsIterable;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.NotInTransactionException;
 import org.neo4j.index.IndexHits;
 import org.neo4j.index.IndexService;
 import org.neo4j.index.impl.GenericIndexService;
 import org.neo4j.index.impl.IdToNodeIterator;
 import org.neo4j.index.impl.SimpleIndexHits;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 import org.neo4j.kernel.impl.cache.LruCache;
 import org.neo4j.kernel.impl.transaction.LockManager;
 import org.neo4j.kernel.impl.transaction.TxModule;
 import org.neo4j.kernel.impl.util.ArrayMap;
 
 /**
  * An implementation of {@link IndexService} which uses Lucene as backend.
  * Additional features to {@link IndexService} is:
  * <ul>
  *   <li>{@link #enableCache(String, int)} will enable a LRU cache for the
  *   specific key and will boost performance in performance-critical areas.</li>
  *   <li>{@link #getNodes(String, Object, Sort)} where you can pass in a
  *   {@link Sort} option to control in which order lucene returns the
  *   results</li>
  *   <li>{@link #setLazySearchResultThreshold(int)} will control the threshold
  *   for then a search result is considered big enough to be returned as a
  *   lazy iteration, making {@link #getNodes(String, Object)} return very fast,
  *   but skips caching</li>
  * </ul>
  * 
  * See more information at http://wiki.neo4j.org/content/Indexing_with_IndexService
  */
 public class LuceneIndexService extends GenericIndexService
 {
     /**
      * The default value for {@link #getLazySearchResultThreshold()}
      */
     public static final int DEFAULT_LAZY_SEARCH_RESULT_THRESHOLD = 100;
     
     protected static final String DOC_ID_KEY = "id";
     protected static final String DOC_INDEX_KEY = "index";
     protected static final String DIR_NAME = "lucene";
     
     private final TransactionManager txManager;
     private final ConnectionBroker broker;
     private final LuceneDataSource xaDs;
     
     private int lazynessThreshold = DEFAULT_LAZY_SEARCH_RESULT_THRESHOLD;
 
     /**
      * @param graphDb the {@link GraphDatabaseService} to use.
      */
     public LuceneIndexService( GraphDatabaseService graphDb )
     {
         super( graphDb );
         EmbeddedGraphDatabase embeddedGraphDb =
             ( ( EmbeddedGraphDatabase ) graphDb );
         String luceneDirectory = 
             embeddedGraphDb.getConfig().getTxModule().getTxLogDirectory() +
                 "/" + getDirName();
         TxModule txModule = embeddedGraphDb.getConfig().getTxModule();
         txManager = txModule.getTxManager();
         byte resourceId[] = getXaResourceId();
         Map<Object,Object> params = getDefaultParams();
         params.put( "dir", luceneDirectory );
         params.put( LockManager.class, 
             embeddedGraphDb.getConfig().getLockManager() );
         xaDs = ( LuceneDataSource ) txModule.registerDataSource( getDirName(),
             getDataSourceClass().getName(), resourceId, params, true );
         broker = new ConnectionBroker( txManager, xaDs );
         xaDs.setIndexService( this );
     }
     
     protected Class<? extends LuceneDataSource> getDataSourceClass()
     {
         return LuceneDataSource.class;
     }
     
     protected String getDirName()
     {
         return DIR_NAME;
     }
     
     protected byte[] getXaResourceId()
     {
         return "162373".getBytes();
     }
     
     private Map<Object,Object> getDefaultParams()
     {
         Map<Object,Object> params = new HashMap<Object,Object>();
         params.put( LuceneIndexService.class, this );
         return params;
     }
 
     /**
      * Enables an LRU cache for a specific index (specified by {@code key})
      * so that the {@code maxNumberOfCachedEntries} number of results found with
      * {@link #getNodes(String, Object)} are cached for faster consecutive
      * lookups. It's prefered to enable cache at construction time.
      * 
      * @param key the index to enable cache for.
      * @param maxNumberOfCachedEntries the max size of the cache before old
      * ones are flushed from the cache.
      */
     public void enableCache( String key, int maxNumberOfCachedEntries )
     {
         xaDs.enableCache( key, maxNumberOfCachedEntries );
     }
     
     /**
      * Sets the threshold for when a result is considered big enough to
      * skip cache and be returned as a totally lazy iterator so that
      * {@link #getNodes(String, Object)} will return very fast and all the
      * reading and fetching of nodes is done lazily before each step in the
      * iteration of the returned result. The default value is
      * {@link #DEFAULT_LAZY_SEARCH_RESULT_THRESHOLD}.
      * @param numberOfHitsBeforeLazyLoading the threshold where results which
      * are bigger than that threshold becomes lazy.
      */
     public void setLazySearchResultThreshold(
         int numberOfHitsBeforeLazyLoading )
     {
         this.lazynessThreshold = numberOfHitsBeforeLazyLoading;
         xaDs.invalidateCache();
     }
     
     /**
      * Returns the threshold for when a result is considered big enough to
      * skip cache and be returned as a totally lazy iterator so that
      * {@link #getNodes(String, Object)} will return very fast and all the
      * reading and fetching of nodes is done lazily before each step in the
      * iteration of the returned result. The default value is
      * {@link #DEFAULT_LAZY_SEARCH_RESULT_THRESHOLD}.
      * 
      * @return the threshold for when a result is considered big enough
      * to be returned as a lazy iteration.
      */
     public int getLazySearchResultThreshold()
     {
         return this.lazynessThreshold;
     }
 
     @Override
     protected void indexThisTx( Node node, String key, Object value )
     {
         getConnection().index( node, key, value );
     }
     
     public IndexHits<Node> getNodes( String key, Object value )
     {
         return getNodes( key, value, null );
     }
     
     /**
      * Just like {@link #getNodes(String, Object)}, but with sorted result.
      * 
      * @param key the index to query.
      * @param value the value to query for.
      * @param sortingOrNull lucene sorting behaviour for the result. Ignored
      * if {@code null}.
      * @return nodes that has been indexed with key and value, optionally
      * sorted with {@code sortingOrNull}.
      */
     public IndexHits<Node> getNodes( String key, Object value,
         Sort sortingOrNull )
     {
         List<Long> nodeIds = new ArrayList<Long>();
         LuceneTransaction luceneTx = getConnection().getLuceneTx();
         Set<Long> addedNodes = Collections.emptySet();
         Set<Long> deletedNodes = Collections.emptySet();
         if ( luceneTx != null )
         {
             addedNodes = luceneTx.getNodesFor( key, value );
             nodeIds.addAll( addedNodes );
             deletedNodes = luceneTx.getDeletedNodesFor( key, value );
         }
         xaDs.getReadLock();
         Iterator<Long> nodeIdIterator = null;
         Integer nodeIdIteratorSize = null;
         IndexSearcherRef searcher = null;
         boolean isLazy = false;
         try
         {
             searcher = xaDs.getIndexSearcher( key );
             if ( searcher != null )
             {
                 LruCache<String,Collection<Long>> cachedNodesMap = 
                     xaDs.getFromCache( key );
                 String valueAsString = value.toString();
                 boolean foundInCache = fillFromCache( cachedNodesMap, nodeIds,
                     key, valueAsString, deletedNodes );
                 if ( !foundInCache )
                 {
                     DocToIdIterator searchedNodeIds = searchForNodes( searcher,
                         key, value, sortingOrNull, deletedNodes );
                     if ( searchedNodeIds.size() >= this.lazynessThreshold )
                     {
                         // Instantiate a lazy iterator
                         isLazy = true;
                         if ( cachedNodesMap != null )
                         {
                             cachedNodesMap.remove( valueAsString );
                         }
                         
                         Collection<Iterator<Long>> iterators =
                             new ArrayList<Iterator<Long>>();
                         iterators.add( nodeIds.iterator() );
                         iterators.add( searchedNodeIds );
                         nodeIdIterator =
                             new CombiningIterator<Long>( iterators );
                         nodeIdIteratorSize = nodeIds.size() +
                             searchedNodeIds.size();
                     }
                     else
                     {
                         // Loop through result here (and cache it if possible)
                         readNodesFromHits( searchedNodeIds, nodeIds,
                             cachedNodesMap, valueAsString );
                     }
                 }
             }
         }
         finally
         {
             // The DocToIdIterator closes the IndexSearchRef instance anyways,
             // or the LazyIterator if it's a lazy one. So no need here.
             xaDs.releaseReadLock();
         }
         
         if ( nodeIdIterator == null )
         {
             nodeIdIterator = nodeIds.iterator();
             nodeIdIteratorSize = nodeIds.size();
         }
         
         IndexHits<Node> hits = new SimpleIndexHits<Node>(
             new IteratorAsIterable<Node>(
                 instantiateIdToNodeIterator( nodeIdIterator ) ),
                     nodeIdIteratorSize );
         if ( isLazy )
         {
             hits = new LazyIndexHits<Node>( hits, searcher );
         }
         return hits;
     }
     
     private void readNodesFromHits( DocToIdIterator searchedNodeIds,
         Collection<Long> nodeIds,
         LruCache<String, Collection<Long>> cachedNodesMap,
         String valueAsString )
     {
         ArrayList<Long> readNodeIds = new ArrayList<Long>();
         while ( searchedNodeIds.hasNext() )
         {
             Long readNodeId = searchedNodeIds.next();
             nodeIds.add( readNodeId );
             readNodeIds.add( readNodeId );
         }
         if ( cachedNodesMap != null )
         {
             cachedNodesMap.put( valueAsString, readNodeIds );
         }
     }
 
     private boolean fillFromCache(
         LruCache<String, Collection<Long>> cachedNodesMap, List<Long> nodeIds,
         String key, String valueAsString, Set<Long> deletedNodes )
     {
         boolean found = false;
         if ( cachedNodesMap != null )
         {
             Collection<Long> cachedNodes =
                 cachedNodesMap.get( valueAsString );
             if ( cachedNodes != null )
             {
                 found = true;
                 for ( Long cachedNodeId : cachedNodes )
                 {
                     if ( !deletedNodes.contains( cachedNodeId ) )
                     {
                         nodeIds.add( cachedNodeId );
                     }
                 }
             }
         }
         return found;
     }
 
     protected Iterator<Node> instantiateIdToNodeIterator(
         final Iterator<Long> ids )
     {
         return new IdToNodeIterator( ids, getGraphDb() );
     }
     
     protected Query formQuery( String key, Object value )
     {
         return new TermQuery( new Term( DOC_INDEX_KEY, value.toString() ) );
     }
 
     /**
      * Returns a lazy iterator with the node ids.
      */
     private DocToIdIterator searchForNodes( IndexSearcherRef searcher,
         String key, Object value, Sort sortingOrNull, Set<Long> deletedNodes )
     {
         Query query = formQuery( key, value );
         try
         {
            searcher.incRef();
             Hits hits = sortingOrNull != null ?
                 searcher.getSearcher().search( query, sortingOrNull ) :
                 searcher.getSearcher().search( query );
             return new DocToIdIterator( new HitsIterator( hits ),
                 deletedNodes, searcher );
         }
         catch ( IOException e )
         {
             throw new RuntimeException( "Unable to search for " + key + ","
                 + value, e );
         }
     }
 
     public Node getSingleNode( String key, Object value )
     {
         IndexHits<Node> hits = null;
         try
         {
             hits = getNodes( key, value );
             Iterator<Node> nodes = hits.iterator();
             Node node = nodes.hasNext() ? nodes.next() : null;
             if ( nodes.hasNext() )
             {
                 throw new RuntimeException( "More than one node for " + key + "="
                     + value );
             }
             return node;
         }
         finally
         {
             if ( hits != null )
             {
                 hits.close();
             }
         }
     }
 
     @Override
     protected void removeIndexThisTx( Node node, String key, Object value )
     {
         if ( value == null )
         {
             throw new IllegalArgumentException( "Value is null" );
         }
         getConnection().removeIndex( node, key, value );
     }
 
     @Override
     public synchronized void shutdown()
     {
         super.shutdown();
         EmbeddedGraphDatabase embeddedGraphDb =
             ( ( EmbeddedGraphDatabase ) getGraphDb() );
         TxModule txModule = embeddedGraphDb.getConfig().getTxModule();
         if ( txModule.getXaDataSourceManager().hasDataSource( getDirName() ) )
         {
             txModule.getXaDataSourceManager().unregisterDataSource(
                 getDirName() );
         }
         xaDs.close();
     }
 
     LuceneXaConnection getConnection()
     {
         return broker.acquireResourceConnection();
     }
         
     private static class ConnectionBroker
     {
         private final ArrayMap<Transaction,LuceneXaConnection> txConnectionMap =
             new ArrayMap<Transaction,LuceneXaConnection>( 5, true, true );
         private final TransactionManager transactionManager;
         private final LuceneDataSource xaDs;
 
         ConnectionBroker( TransactionManager transactionManager,
             LuceneDataSource xaDs )
         {
             this.transactionManager = transactionManager;
             this.xaDs = xaDs;
         }
 
         LuceneXaConnection acquireResourceConnection()
         {
             LuceneXaConnection con = null;
             Transaction tx = this.getCurrentTransaction();
             con = txConnectionMap.get( tx );
             if ( con == null )
             {
                 try
                 {
                     con = (LuceneXaConnection) xaDs.getXaConnection();
                     if ( !tx.enlistResource( con.getXaResource() ) )
                     {
                         throw new RuntimeException( "Unable to enlist '"
                             + con.getXaResource() + "' in " + tx );
                     }
                     tx.registerSynchronization( new TxCommitHook( tx ) );
                     txConnectionMap.put( tx, con );
                 }
                 catch ( javax.transaction.RollbackException re )
                 {
                     String msg = "The transaction is marked for rollback only.";
                     throw new RuntimeException( msg, re );
                 }
                 catch ( javax.transaction.SystemException se )
                 {
                     String msg = 
                         "TM encountered an unexpected error condition.";
                     throw new RuntimeException( msg, se );
                 }
             }
             return con;
         }
 
         void releaseResourceConnectionsForTransaction( Transaction tx )
             throws NotInTransactionException
         {
             LuceneXaConnection con = txConnectionMap.remove( tx );
             if ( con != null )
             {
                 con.destroy();
             }
         }
 
         void delistResourcesForTransaction() throws NotInTransactionException
         {
             Transaction tx = this.getCurrentTransaction();
             LuceneXaConnection con = txConnectionMap.get( tx );
             if ( con != null )
             {
                 try
                 {
                     tx.delistResource( con.getXaResource(),
                         XAResource.TMSUCCESS );
                 }
                 catch ( IllegalStateException e )
                 {
                     throw new RuntimeException(
                         "Unable to delist lucene resource from tx", e );
                 }
                 catch ( SystemException e )
                 {
                     throw new RuntimeException(
                         "Unable to delist lucene resource from tx", e );
                 }
             }
         }
 
         private Transaction getCurrentTransaction()
             throws NotInTransactionException
         {
             try
             {
                 Transaction tx = transactionManager.getTransaction();
                 if ( tx == null )
                 {
                     throw new NotInTransactionException(
                         "No transaction found for current thread" );
                 }
                 return tx;
             }
             catch ( SystemException se )
             {
                 throw new NotInTransactionException(
                     "Error fetching transaction for current thread", se );
             }
         }
 
         private class TxCommitHook implements Synchronization
         {
             private final Transaction tx;
 
             TxCommitHook( Transaction tx )
             {
                 this.tx = tx;
             }
 
             public void afterCompletion( int param )
             {
                 releaseResourceConnectionsForTransaction( tx );
             }
 
             public void beforeCompletion()
             {
                 delistResourcesForTransaction();
             }
         }
     }
 }
