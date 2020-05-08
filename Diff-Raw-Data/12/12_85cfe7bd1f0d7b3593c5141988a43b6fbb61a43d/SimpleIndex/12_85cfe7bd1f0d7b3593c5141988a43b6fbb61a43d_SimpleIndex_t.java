 package org.neo4j.util.index;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.EmbeddedNeo;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.util.btree.BTree;
 import org.neo4j.util.btree.KeyEntry;
 
 /**
  * A simple index implementation using {@link org.neo4j.util.btree.BTree BTree}
  * that can index one node per key. They key is not checked for equality, 
  * instead only the <CODE>hashCode</CODE> of the key is calculated and used 
  * as the real key. This means that two different objects with same 
  * <CODE>hashCode</CODE> are considered equal.
  * <p>
  * Note: this implementation is not thread safe (yet).
  */
 //not thread safe yet
 public class SimpleIndex implements Index
 {
 	private static final String INDEX_NAME = "index_name";
 	
 	private final Node underlyingNode;
 	private BTree bTree;
 	private String name;
 	private final EmbeddedNeo neo;
 	
 	
 	/**
 	 * Creates/loads a simple index. The <CODE>underlyingNode</CODE> can either
 	 * be a new (just created) node or a node that already represents a 
 	 * previously created index.
 	 *
 	 * @param name The unique name of the index or null if index already
 	 * created (using specified underlying node)
 	 * @param underlyingNode The underlying node representing the index
 	 * @param neo The embedded neo instance
 	 * @throws IllegalArgumentException if the underlying node is a index with
 	 * a different name set.
 	 */
 	public SimpleIndex( String name, Node underlyingNode, EmbeddedNeo neo )
 	{
 		if ( underlyingNode == null || neo == null )
 		{
 			throw new IllegalArgumentException( 
 				"Null parameter underlyingNode=" + underlyingNode +
 				" neo=" + neo );
 		}
 		this.underlyingNode = underlyingNode;
 		this.neo = neo;
 		this.neo.registerEnumRelationshipTypes( 
 			org.neo4j.util.btree.BTree.RelTypes.class );
 		Transaction tx = Transaction.begin();
 		try
 		{
 			if ( underlyingNode.hasProperty( INDEX_NAME ) )
 			{
 				String storedName = (String) underlyingNode.getProperty( 
 					INDEX_NAME );
 				if ( name != null && !storedName.equals( name ) )
 				{
 					throw new IllegalArgumentException( "Name of index " + 
 						"for node=" + underlyingNode.getId() + "," + 
 						storedName + " is not same as passed in name=" + 
 						name );
 				}
 				if ( name == null )
 				{
 					this.name = (String) underlyingNode.getProperty( INDEX_NAME );
 				}
 				else
 				{
 					this.name = name;
 				}
 			}
 			else
 			{
 				underlyingNode.setProperty( INDEX_NAME, name );
 				this.name = name;
 			}
 			Relationship bTreeRel = underlyingNode.getSingleRelationship( 
 				org.neo4j.util.btree.BTree.RelTypes.TREE_ROOT, 
 				Direction.OUTGOING );
 			if ( bTreeRel != null )
 			{
 				bTree = new BTree( neo, bTreeRel.getEndNode() );
 			}
 			else
 			{
 				Node bTreeNode = neo.createNode();
 				underlyingNode.createRelationshipTo( bTreeNode, 
 					org.neo4j.util.btree.BTree.RelTypes.TREE_ROOT );
 				bTree = new BTree( neo, bTreeNode );
 			}
 			tx.success();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	/**
 	 * Return the name of this index.
 	 * @return the name of this index
 	 */
 	public String getName()
 	{
 		return this.name;
 	}
 	
 	Node getUnderlyingNode()
 	{
 		return underlyingNode;
 	}
 	
 	/**
 	 * Creates a index mapping for <CODE>indexKey</CODE> to 
 	 * <CODE>nodeToIndex</CODE>. This simple index implementation does not 
 	 * handle multiple nodes for the same key so trying to index an already 
 	 * existing key will result in a runtime exception. Note, the 
 	 * <CODE>hashCode</CODE> of <CODE>indexKey</CODE> is used as the real 
 	 * key so two different objects returning same <CODE>hashCode</CODE> will
 	 * not work very well either.
 	 * 
 	 * @param nodeToIndex the node to index with the specified key
 	 * @param indexKey the key
 	 */
 	public void index( Node nodeToIndex, Object indexKey )
 	{
 		if ( nodeToIndex == null )
 		{
 			throw new IllegalArgumentException( "Null node" );
 		}
 		Transaction tx = Transaction.begin();
 		try
 		{
 			int hashCode = indexKey.hashCode();
 			bTree.addEntry( hashCode, nodeToIndex.getId() );
 			tx.success();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	/**
 	 * Removes a index mapping between a node and a key. If no such index 
 	 * exist or the node mapped is not the expected <CODE>nodeToRemove</CODE> a 
 	 * runtime exception is thrown (but the mapping is removed).
 	 * 
 	 * @param nodeToRemove the node that is mapped to the index key
 	 * @param indexKey the index key
 	 * @throws IllegalArgumentException if null parameter
 	 */
 	public void remove( Node nodeToRemove, Object indexKey )
 	{
 		if ( nodeToRemove == null || indexKey == null )
 		{
 			throw new IllegalArgumentException( "Null parameter, " + 
 				"nodeToRemove=" + nodeToRemove + " indexKey=" + indexKey );
 		}
 		Transaction tx = Transaction.begin();
 		try
 		{
 			int hashCode = indexKey.hashCode();
 			Long nodeId = (Long) bTree.removeEntry( hashCode );
 			if ( nodeId == null || nodeId != nodeToRemove.getId() )
 			{
 				throw new RuntimeException( "Removed index " + indexKey + 
 					" and found node id " + nodeId + " but expected " +
 					nodeToRemove.getId() );
 			}
 			tx.success();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	/**
 	 * Public only for testing purposes. Validates the interal b-tree index and 
 	 * if any error is found a runtime exception is thrown.
 	 */
 	public void validate()
 	{
 		bTree.validateTree();
 	}
 	
 	/**
 	 * Retuns a iterable over a single node mapped to the specified key or 
 	 * empty iterable if no node is mapped to the specified key.
 	 * 
 	 * @param indexKey the key
 	 * @return the node mapped to the key
 	 */
 	public Iterable<Node> getNodesFor( Object indexKey )
 	{
 		Transaction tx = Transaction.begin();
 		try
 		{
 			int hashCode = indexKey.hashCode();
 			KeyEntry entry = bTree.getAsKeyEntry( hashCode );
 			if ( entry == null )
 			{
				return Collections.emptyList();
 			}
 			List<Node> nodes = new LinkedList<Node>();
 			nodes.add( neo.getNodeById( (Long) entry.getValue() ) );
 			tx.success();
 			return nodes;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	/**
 	 * Deletes this index in a single (or the current) transaction including
 	 * the underlying node.
 	 */
 	public void drop()
 	{
 		Transaction tx = Transaction.begin();
 		try
 		{
 			bTree.delete();
 			underlyingNode.delete();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	/**
 	 * Deletes this index using a commit interval including the underlying node.
 	 * 
 	 * @parram commitInterval number of index removes before a new transaction
 	 * is started.
 	 */
 	public void drop( int commitInterval )
 	{
 		Transaction tx = Transaction.begin();
 		try
 		{
 			bTree.delete( commitInterval );
 			underlyingNode.delete();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 
 	/**
 	 * Returns all nodes that are indexed in this index.
 	 * 
 	 * @return all indexed nodes.
 	 */
 	public Iterable<Node> values()
     {
 		return new NodeLookupTraverser( neo, bTree.values() );
 	}
 	
 	private static class NodeLookupTraverser implements Iterable<Node>, 
 		Iterator<Node>
 	{
 		private final EmbeddedNeo neo;
 		private final Iterator<Object> itr;
 		
 		NodeLookupTraverser( EmbeddedNeo neo, Iterable<Object> nodeIds )
 		{
 			this.neo = neo;
 			itr = nodeIds.iterator();
 		}
 
 		public Iterator<Node> iterator()
         {
 			return this;
         }
 
 		public boolean hasNext()
         {
 			return itr.hasNext();
         }
 
 		public Node next()
         {
 			return neo.getNodeById( (Long) itr.next() );
         }
 
 		public void remove()
         {
 			itr.remove();
         }		
 	}
 }
