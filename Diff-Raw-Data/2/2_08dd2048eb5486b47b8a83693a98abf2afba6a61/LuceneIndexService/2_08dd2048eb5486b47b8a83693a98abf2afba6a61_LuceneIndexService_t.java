 /*
  * Copyright (c) 2002-2008 "Neo Technology,"
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
 package org.neo4j.util.index;
 
 import java.io.IOException;
 import java.util.ArrayList;
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
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.Hits;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.TermQuery;
 import org.neo4j.api.core.EmbeddedNeo;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.NotFoundException;
 import org.neo4j.api.core.NotInTransactionException;
 import org.neo4j.impl.cache.LruCache;
 import org.neo4j.impl.transaction.LockManager;
 import org.neo4j.impl.transaction.TxModule;
 import org.neo4j.impl.util.ArrayMap;
 
 // TODO:
 // o Run optimize when starting up
 public class LuceneIndexService extends GenericIndexService
 {
     protected static final String DOC_ID_KEY = "id";
     protected static final String DOC_INDEX_KEY = "index";
     
     private final TransactionManager txManager;
     private final ConnectionBroker broker;
     private final LuceneDataSource xaDs;
     private Sort sorting;
 
     public LuceneIndexService( NeoService neo )
     {
         super( neo );
         EmbeddedNeo embeddedNeo = ((EmbeddedNeo) neo);
         String luceneDirectory = 
             embeddedNeo.getConfig().getTxModule().getTxLogDirectory() +
                 "/" + getDirName();
         TxModule txModule = embeddedNeo.getConfig().getTxModule();
         txManager = txModule.getTxManager();
         byte resourceId[] = getXaResourceId();
         Map<Object,Object> params = getDefaultParams();
         params.put( "dir", luceneDirectory );
         params.put( LockManager.class, 
             embeddedNeo.getConfig().getLockManager() );
         xaDs = ( LuceneDataSource ) txModule.registerDataSource( getDirName(),
             getDataSourceClass().getName(), resourceId, params, true );
         broker = new ConnectionBroker( txManager, xaDs );
         xaDs.setIndexService( this );
     }
     
     public void rotate() throws IOException
     {
         xaDs.rotateLogicalLog();
     }
     
     protected Class<? extends LuceneDataSource> getDataSourceClass()
     {
         return LuceneDataSource.class;
     }
     
     protected String getDirName()
     {
         return "lucene";
     }
     
     protected byte[] getXaResourceId()
     {
         return "162373".getBytes();
     }
     
     protected Field.Index getIndexStrategy()
     {
         return Field.Index.NOT_ANALYZED;
     }
     
     private Map<Object,Object> getDefaultParams()
     {
         Map<Object,Object> params = new HashMap<Object,Object>();
         params.put( LuceneIndexService.class, this );
         return params;
     }
 
     public void enableCache( String key, int maxNumberOfCachedEntries )
     {
         xaDs.enableCache( key, maxNumberOfCachedEntries );
     }
 
     @Override
     protected void indexThisTx( Node node, String key, Object value )
     {
         getConnection().index( node, key, value );
     }
     
     public Iterable<Node> getNodes( String key, Object value )
     {
         List<Node> nodes = new ArrayList<Node>();
         LuceneTransaction luceneTx = getConnection().getLuceneTx();
         Set<Long> addedNodes = Collections.emptySet();
         Set<Long> deletedNodes = Collections.emptySet();
         if ( luceneTx != null )
         {
             addedNodes = luceneTx.getNodesFor( key, value );
             for ( long id : addedNodes )
             {
                 nodes.add( getNeo().getNodeById( id ) );
             }
             deletedNodes = luceneTx.getDeletedNodesFor( key, value );
         }
         IndexSearcher searcher = xaDs.acquireIndexSearcher( key );
         try
         {
             if ( searcher != null )
             {
                 LruCache<String,Iterable<Long>> cachedNodesMap = 
                     xaDs.getFromCache( key );
                 boolean foundInCache = false;
                 String valueAsString = value.toString();
                 if ( cachedNodesMap != null )
                 {
                     Iterable<Long> cachedNodes =
                         cachedNodesMap.get( valueAsString );
                     if ( cachedNodes != null )
                     {
                         foundInCache = true;
                         for ( Long cachedNodeId : cachedNodes )
                         {
                             nodes.add( getNeo().getNodeById( cachedNodeId ) );
                         }
                     }
                 }
                 if ( !foundInCache )
                 {
                     Iterable<Node> readNodes = searchForNodes( key, value,
                         deletedNodes );
                     ArrayList<Long> readNodeIds = new ArrayList<Long>();
                     for ( Node readNode : readNodes )
                     {
                         nodes.add( readNode );
                         readNodeIds.add( readNode.getId() );
                     }
                     if ( cachedNodesMap != null )
                     {
                        cachedNodesMap.put( valueAsString, readNodeIds );
                     }
                 }
             }
         }
         finally
         {
             xaDs.releaseIndexSearcher( key, searcher );
         }
         return nodes;
     }
     
     public void setSorting( Sort sortingOrNullForNone )
     {
         this.sorting = sortingOrNullForNone;
     }
     
     protected Query formQuery( String key, Object value )
     {
         return new TermQuery( new Term( DOC_INDEX_KEY, value.toString() ) );
     }
 
     private Iterable<Node> searchForNodes( String key, Object value,
         Set<Long> deletedNodes )
     {
         Query query = formQuery( key, value );
         IndexSearcher searcher = xaDs.acquireIndexSearcher( key );
         try
         {
             ArrayList<Node> nodes = new ArrayList<Node>();
             Hits hits = sorting != null ?
                 searcher.search( query, sorting ) :
                 searcher.search( query );
             for ( int i = 0; i < hits.length(); i++ )
             {
                 Document document = hits.doc( i );
                 try
                 {
                     long id = Long.parseLong( document.getField( DOC_ID_KEY )
                         .stringValue() );
                     if ( !deletedNodes.contains( id ) )
                     {
                         nodes.add( getNeo().getNodeById( id ) );
                     }
                 }
                 catch ( NotFoundException e )
                 {
                     // deleted in this tx
                 }
             }
             return nodes;
         }
         catch ( IOException e )
         {
             throw new RuntimeException( "Unable to search for " + key + ","
                 + value, e );
         }
         finally
         {
             xaDs.releaseIndexSearcher( key, searcher );
         }
     }
 
     public Node getSingleNode( String key, Object value )
     {
         // TODO Temporary code, this can be implemented better
         Iterator<Node> nodes = getNodes( key, value ).iterator();
         Node node = nodes.hasNext() ? nodes.next() : null;
         while ( nodes.hasNext() )
         {
             if ( !nodes.next().equals( node ) )
             {
                 throw new RuntimeException( "More than one node for " + key + "="
                     + value );
             }
         }
         return node;
     }
 
     @Override
     protected void removeIndexThisTx( Node node, String key, Object value )
     {
         getConnection().removeIndex( node, key, value );
     }
 
     @Override
     public synchronized void shutdown()
     {
         super.shutdown();
         EmbeddedNeo embeddedNeo = ( ( EmbeddedNeo ) getNeo() );
         TxModule txModule = embeddedNeo.getConfig().getTxModule();
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
