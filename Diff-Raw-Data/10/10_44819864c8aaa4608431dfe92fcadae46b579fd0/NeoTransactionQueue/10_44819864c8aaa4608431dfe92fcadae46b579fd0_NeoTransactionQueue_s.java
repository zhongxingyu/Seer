 package org.neo4j.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.NotFoundException;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.api.core.Transaction;
 
 /**
  * Wraps several {@link NeoQueue} instances (per transaction).
  * See {@link NeoTransactionQueueWorker} for usage.
  * @author mattias
  */
 public class NeoTransactionQueue
 {
 	public static enum QueueRelTypes implements RelationshipType
 	{
 		UPDATE_QUEUE,
 		INTERNAL_QUEUE,
 	}
 	
 	private static final String INDEX_TX_ID = "txid";
 	
 	private static Map<Integer, TxQueue> queueNodes =
 		Collections.synchronizedMap( new HashMap<Integer, TxQueue>() );
 	
 	private NeoService neo;
 	private NeoUtil neoUtil;
 	private Node rootNode;
 	
 	public NeoTransactionQueue( NeoService neo, Node rootNode )
 	{
 		this.neo = neo;
 		this.neoUtil = new NeoUtil( neo );
 		this.rootNode = rootNode;
 		initialize();
 	}
 	
 	private void initialize()
 	{
 		Transaction tx = neo.beginTx();
 		try
 		{
 			Collection<Relationship> toDelete = new ArrayList<Relationship>();
 			for ( Relationship rel : getRefNode().getRelationships(
 				QueueRelTypes.UPDATE_QUEUE, Direction.OUTGOING ) )
 			{
 				TxQueue queue = new TxQueue( rel.getEndNode() );
 				if ( queue.peek() != null )
 				{
 					queueNodes.put( queue.getTxId(), queue );
 				}
 				else
 				{
 					toDelete.add( rel );
 				}
 			}
 
 			for ( Relationship rel : toDelete )
 			{
 				Node node = rel.getEndNode();
 				rel.delete();
 				node.delete();
 			}
 			tx.success();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
	/**
	 * @param object object to be updated, Ise or User f.ex.
	 * @param operation the operation type
	 */
 	public void add( int txId, Map<String, Object> values )
 	{
 		// We must be in a transaction, else the calling code isn't right
 		TxQueue queue = findQueue( txId, true );
 		queue.add( values );
 	}
 	
 	private void remove( TxQueue queue )
 	{
 		int txId = queue.getTxId();
 		queueNodes.remove( txId );
 	}
 	
 	protected Node getRefNode()
 	{
 		return this.rootNode;
 	}
 	
 	private TxQueue findQueue( int txId, boolean allowCreate )
 	{
 		TxQueue queue = queueNodes.get( txId );
 		if ( queue != null )
 		{
 			return queue;
 		}
 		
 		if ( allowCreate )
 		{
 			Node queueNode = neo.createNode();
 			queueNode.setProperty( INDEX_TX_ID, txId );
 			getRefNode().createRelationshipTo( queueNode,
 				QueueRelTypes.UPDATE_QUEUE );
 			queue = new TxQueue( queueNode );
 			queueNodes.put( txId, queue );
 			return queue;
 		}
 		return null;
 	}
 	
 	public Map<Integer, TxQueue> getQueues()
 	{
 		Transaction tx = neo.beginTx();
 		try
 		{
 			Map<Integer, TxQueue> map = new HashMap<Integer, TxQueue>();
 			Map.Entry<Integer, TxQueue>[] entries = queueNodes.entrySet().
 				toArray( new Map.Entry[ queueNodes.size() ] );
 			for ( Map.Entry<Integer, TxQueue> entry : entries )
 			{
 				TxQueue queue = entry.getValue();
 				try
 				{
 					if ( queue.peek() != null )
 					{
 						map.put( queue.getTxId(), queue );
 					}
 				}
 				catch ( NotFoundException e )
 				{
 					// Since queueNodes is a cache which is filled inside
 					// transactions it may be the case that a queue is created
 					// and then the transaction is rolled back... then the
 					// queueNodes map would still have a queue instance for
 					// the node which was created in the transaction, but
 					// not committed.
 					queueNodes.remove( entry.getKey() );
 				}
 			}
 			tx.success();
 			return Collections.unmodifiableMap( map );
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	public class TxQueue
 	{
 		private NeoQueue queue;
 		private Node node;
 		private boolean deleted;
 		
 		public TxQueue( Node rootNode )
 		{
 			queue = new NeoQueue( neo, rootNode, QueueRelTypes.INTERNAL_QUEUE );
 			this.node = rootNode;
 		}
 		
 		Node getRootNode()
 		{
 			return node;
 		}
 		
 		public int getTxId()
 		{
 			return ( Integer ) neoUtil.getProperty( node, INDEX_TX_ID );
 		}
 		
 		private void add( Map<String, Object> values )
 		{
 			Node node = queue.add();
 			for ( Map.Entry<String, Object> entry : values.entrySet() )
 			{
 				node.setProperty( entry.getKey(), entry.getValue() );
 			}
 		}
 		
 		public Map<String, Object> peek()
 		{
 		    Collection<Map<String, Object>> result = peek( 1 );
 		    return result.isEmpty() ? null : result.iterator().next();
 		}
 		
 		public Collection<Map<String, Object>> peek( int max )
 		{
             if ( deleted )
             {
                 return null;
             }
             
             Transaction tx = neo.beginTx();
             try
             {
                 Collection<Map<String, Object>> result =
                     new ArrayList<Map<String,Object>>( max );
                 Node[] nodes = queue.peek( max );
                 for ( Node node : nodes )
                 {
                     result.add( readEntry( node ) );
                 }
                 tx.success();
                 return result;
             }
             finally
             {
                 tx.finish();
             }
 		}
 		
 		private Map<String, Object> readEntry( Node node )
 		{
 			Map<String, Object> result = new HashMap<String, Object>();
 			for ( String key : node.getPropertyKeys() )
 			{
 				result.put( key, node.getProperty( key ) );
 			}
 			return result;
 		}
 		
 		public void remove()
 		{
 		    remove( 1 );
 		}
 		
 		public void remove( int max )
 		{
 			if ( deleted )
 			{
 				throw new IllegalStateException( "Deleted" );
 			}
 			
 			Transaction tx = neo.beginTx();
 			try
 			{
 				queue.remove( max );
 				if ( queue.peek() == null )
 				{
 					NeoTransactionQueue.this.remove( this );
 					deleted = true;
 				}
 				tx.success();
 			}
 			finally
 			{
 				tx.finish();
 			}
 		}
 	}
 }
