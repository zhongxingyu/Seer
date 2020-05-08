 package org.neo4j.util.index;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.EmbeddedNeo;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.impl.transaction.TransactionUtil;
 import org.neo4j.util.btree.BTree;
 import org.neo4j.util.btree.KeyEntry;
 
 /**
  * A "multi" index implementation using {@link org.neo4j.util.btree.BTree BTree}
  * that can index multiple nodes per key. They key is checked for equality 
  * using both <CODE>hashCode</CODE> and <CODE>equal</CODE> methods. 
  * <p>
  * Note: this implementation is not thread safe (yet).
  */
 // not thread safe yet
 public class MultiIndex implements Index
 {
 	public static enum RelTypes implements RelationshipType
 	{
 		INDEX_INSTANCE,
 	}
 	
 	private static final String INDEX_NAME = "index_name";
 	private static final String INDEX_KEY = "ik";
 	private static final String INDEX_VALUES = "iv";
 	
 	private final Node underlyingNode;
 	private BTree bTree;
 	private String name;
 	private EmbeddedNeo neo;
 	
 	
 	/**
 	 * Creates/loads a index. The <CODE>underlyingNode</CODE> can either
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
 	public MultiIndex( String name, Node underlyingNode, EmbeddedNeo neo )
 	{
 		if ( underlyingNode == null || neo == null )
 		{
 			throw new IllegalArgumentException( 
 				"Null parameter underlyingNode=" + underlyingNode +
 				" neo=" + neo );
 		}
 		this.underlyingNode = underlyingNode;
 		this.neo = neo;
 		this.neo.registerEnumRelationshipTypes( RelTypes.class );
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
 					this.name = (String) underlyingNode.getProperty( 
 						INDEX_NAME );
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
 	 * <CODE>nodeToIndex</CODE>. This multi index implementation does  
 	 * handle multiple nodes for the same key. It also compares keys using the 
 	 * <CODE>equals</CODE> method.
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
 			Node indexNode = neo.createNode();
 			indexNode.setProperty( INDEX_KEY, indexKey );
 			// KeyEntry entry = bTree.getAsKeyEntry( hashCode );
 			KeyEntry entry = bTree.addIfAbsent( hashCode, indexNode.getId() );
 			if ( entry != null )
 			{
 				indexNode.setProperty( INDEX_VALUES, nodeToIndex.getId() );
 				underlyingNode.createRelationshipTo( indexNode, 
 					RelTypes.INDEX_INSTANCE );
 			}
 			else
 			{
 				indexNode.delete();
 				entry = bTree.getAsKeyEntry( hashCode );
 				for ( long nodeId : getValues( entry ) )
 				{
 					indexNode = neo.getNodeById( nodeId );
 					if ( indexNode.getProperty( INDEX_KEY).equals( indexKey ) )
 					{
 						addOneMoreValue( indexNode, nodeToIndex.getId() );
 						return;
 					}
 				}
 				indexNode = neo.createNode();
 				indexNode.setProperty( INDEX_KEY, indexKey );
 				addOneMoreValue( entry, indexNode.getId() );
 				indexNode.setProperty( INDEX_VALUES, nodeToIndex.getId() );
 				underlyingNode.createRelationshipTo( indexNode, 
 					RelTypes.INDEX_INSTANCE );
 			}
 			tx.success();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	private void addOneMoreValue( KeyEntry entry, long nodeId )
 	{
 		Object value = entry.getValue();
 		if ( value.getClass().isArray() )
 		{
 			long[] values = (long[]) value;
 			long[] newValues = new long[values.length + 1];
 			boolean addNewValues = true;
 			for ( int i = 0; i < values.length; i++ )
 			{
 				if ( values[i] == nodeId )
 				{
 					addNewValues = false;
 					break;
 				}
 				newValues[i] = values[i];
 			}
 			if ( addNewValues )
 			{
 				newValues[newValues.length - 1] = nodeId;
 				entry.setValue( newValues );
 			}
 		}
 		long currentId = (Long) value;
 		if ( currentId != nodeId )
 		{
 			long[] newValues = new long[2];
 			newValues[0] = currentId;
 			newValues[1] = nodeId;
 			entry.setValue( newValues );
 		}
 	}
 	
 	private void addOneMoreValue( Node node, long nodeId )
 	{
 		Object value = node.getProperty( INDEX_VALUES );
 		if ( value.getClass().isArray() )
 		{
 			long[] values = (long[]) value;
 			long[] newValues = new long[values.length + 1];
 			boolean addNewValues = true;
 			for ( int i = 0; i < values.length; i++ )
 			{
 				if ( values[i] == nodeId )
 				{
 					addNewValues = false;
 					break;
 				}
 				newValues[i] = values[i];
 			}
 			if ( addNewValues )
 			{
 				newValues[newValues.length - 1] = nodeId;
 				node.setProperty( INDEX_VALUES, newValues );
 			}
 		}
 		long currentId = (Long) value;
 		if ( currentId != nodeId )
 		{
 			long[] newValues = new long[2];
 			newValues[0] = currentId;
 			newValues[1] = nodeId;
 			node.setProperty( INDEX_VALUES, newValues );
 		}
 	}
 	
 	private boolean removeOneValue( KeyEntry entry, long nodeId )
 	{
 		Object value = entry.getValue();
 		if ( value.getClass().isArray() )
 		{
 			long[] values = (long[]) value;
 			if ( values.length == 1 )
 			{
 				if ( values[0] == nodeId )
 				{
 					return true;
 				}
 				return false;
 			}
 			long[] newValues = new long[values.length - 1];
 			int j = 0;
 			for ( int i = 0; i < values.length; i++ )
 			{
 				if ( values[i] != nodeId )
 				{
 					newValues[j++] = values[i];
 				}
 			}
 			entry.setValue( newValues );
 			return false;
 		}
 		long currentId = (Long) value;
 		if ( currentId == nodeId )
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean removeOneValue( Node node, long nodeId )
 	{
 		Object value = node.getProperty( INDEX_VALUES );
 		if ( value.getClass().isArray() )
 		{
 			long[] values = (long[]) value;
 			if ( values.length == 1 )
 			{
 				if ( values[0] == nodeId )
 				{
 					return true;
 				}
 				return false;
 			}
 			long[] newValues = new long[values.length - 1];
 			int j = 0;
 			for ( int i = 0; i < values.length; i++ )
 			{
 				if ( values[i] != nodeId )
 				{
 					newValues[j++] = values[i];
 				}
 			}
 			node.setProperty( INDEX_VALUES, newValues );
 			return false;
 		}
 		long currentId = (Long) value;
 		if ( currentId == nodeId )
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	private long[] getValues( KeyEntry entry )
 	{
 		Object value = entry.getValue();
 		if ( value.getClass().isArray() )
 		{
 			return (long[]) value;
 		}
 		long values[] = new long[1];
 		values[0] = (Long) value;
 		return values;
 	}
 
 	private long[] getValues( Node node )
 	{
 		Object value = node.getProperty( INDEX_VALUES );
 		if ( value.getClass().isArray() )
 		{
 			return (long[]) value;
 		}
 		long values[] = new long[1];
 		values[0] = (Long) value;
 		return values;
 	}
 	
 	/**
 	 * Removes a index mapping between a node and a key. If the specified 
 	 * mapping doesn't exist this method will quietly return.
 	 * 
 	 * @param nodeToRemove the node that is mapped to the index key
 	 * @param indexKey the index key
 	 */
 	public void remove( Node nodeToRemove, Object indexKey )
 	{
 		Transaction tx = Transaction.begin();
 		try
 		{
 			int hashCode = indexKey.hashCode();
 			KeyEntry entry = bTree.getAsKeyEntry( hashCode );
 			if ( entry != null )
 			{
 				for ( long nodeId : getValues( entry ) )
 				{
 					Node indexNode = neo.getNodeById( nodeId );
 					if ( indexNode.getProperty( INDEX_KEY ).equals( indexKey ) )
 					{
 						if ( removeOneValue( indexNode, nodeToRemove.getId() ) )
 						{
 							indexNode.getSingleRelationship( 
 								RelTypes.INDEX_INSTANCE, 
 								Direction.INCOMING ).delete();
 							if ( removeOneValue( entry, nodeId ) )
 							{
 								bTree.removeEntry( hashCode );
 							}
 							indexNode.delete();
 						}
 						return;
 					}
 				}
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
 	 * Retuns a iterable over all nodes mapped to the specified key or 
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
				return Collections.EMPTY_LIST;
 			}
 			for ( long nodeId : getValues( entry ) )
 			{
 				Node indexNode = neo.getNodeById( nodeId );
 				if ( indexNode.getProperty( INDEX_KEY ).equals( indexKey ) )
 				{
 					List<Node> nodes = new LinkedList<Node>();
 					long[] nodeIds = getValues( indexNode );
 					for ( int i = 0; i < nodeIds.length; i++ )
 					{
 						nodes.add( neo.getNodeById( nodeIds[i] ) );
 					}
 					tx.success();
 					return nodes;
 				}
 			}
			return Collections.EMPTY_LIST;
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
 			for ( Relationship rel : underlyingNode.getRelationships( 
 				RelTypes.INDEX_INSTANCE ) )
 			{
 				rel.getEndNode().delete();
 				rel.delete();
 			}
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
 			int count = 0;
 			for ( Relationship rel : underlyingNode.getRelationships( 
 				RelTypes.INDEX_INSTANCE ) )
 			{
 				rel.getEndNode().delete();
 				rel.delete();
 				if ( count++ >= commitInterval )
 				{
 					TransactionUtil.commitTx( true );
 					TransactionUtil.beginTx();
 				}
 			}
 			underlyingNode.delete();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 
 	/**
 	 * Not supported yet, throws <CODE>UnsupportedOperationException</CODE>.
 	 */
 	public Iterable<Node> values()
     {
 	    throw new UnsupportedOperationException();
     }
 }
