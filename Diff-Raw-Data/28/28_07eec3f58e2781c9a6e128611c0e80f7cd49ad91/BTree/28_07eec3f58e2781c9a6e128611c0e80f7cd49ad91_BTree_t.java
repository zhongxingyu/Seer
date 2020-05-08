 package org.neo4j.util.btree;
 
 import java.util.Iterator;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser;
 import org.neo4j.api.core.Traverser.Order;
 
 /**
  * A b-tree implementation ontop of neo (using nodes/relationships 
  * and properties).
  * <p>
  * This implementation is not thread safe (yet).
  */
 //not thread safe yet
 public class BTree
 {
 	public static enum RelTypes implements RelationshipType
 	{
 		TREE_ROOT,
 		SUB_TREE,
 		
 		// a relationship type where relationship actually is the *key entry*
 		KEY_ENTRY 
 	};
 	
 	private NeoService neo;
 	private TreeNode treeRoot;
 	
 	/**
 	 * Creates a <CODE>BTree</CODE> using <CODE>rootNode</CODE> as root. The 
 	 * root node must have a incoming relationship of {@link RelTypes TREE_ROOT}
 	 * or a runtime exception will be thrown.
 	 * 
 	 * @param neo the embedded neo instance
 	 * @param rootNode root node with incoming <CODE>TREE_ROOT</CODE> 
 	 * relationship
 	 */
 	public BTree( NeoService neo, Node rootNode )
 	{
 		this.neo = neo;
 		this.treeRoot = new TreeNode( this, rootNode );
 	}
 	
 	void makeRoot( TreeNode newRoot )
 	{
 		Relationship rel = treeRoot.getUnderlyingNode().getSingleRelationship( 
 			RelTypes.TREE_ROOT, Direction.INCOMING );
 		Node startNode = rel.getStartNode();
 		rel.delete();
 		startNode.createRelationshipTo( newRoot.getUnderlyingNode(), 
 			RelTypes.TREE_ROOT );
 		treeRoot = newRoot;
 	}
 	
 	/**
 	 * Deletes this b-tree.
 	 */
 	public void delete()
 	{
 		Relationship rel = treeRoot.getUnderlyingNode().getSingleRelationship( 
 			RelTypes.TREE_ROOT, Direction.INCOMING );
 		treeRoot.delete();
 		rel.delete();
 	}
 	
 	/**
 	 * Deletes this b-tree using a commit interval.
 	 * 
 	 * @param commitInterval number of entries to remove before a new 
 	 * transaction is started
 	 */
 	public void delete( int commitInterval )
 	{
 		Relationship rel = treeRoot.getUnderlyingNode().getSingleRelationship( 
 			RelTypes.TREE_ROOT, Direction.INCOMING );
 		treeRoot.delete( commitInterval, 0);
 		rel.delete();
 	}
 	
 	/**
 	 * Public for testing purpose. Validates this b-tree making sure it is 
 	 * balanced and consistent.
 	 */
 	public void validateTree()
 	{
 		long currentValue = Long.MIN_VALUE;
 		KeyEntry entry = null;
 		KeyEntry keyEntry = treeRoot.getFirstEntry();
 		boolean hasSubTree = false;
 		int entryCount = 0;
 		while ( keyEntry != null )
 		{
 			entry = keyEntry;
 			entryCount++;
 			if ( entry.getKey() <= currentValue )
 			{
 				throw new RuntimeException( "Key entry ordering inconsistency");
 			}
 			currentValue = entry.getKey();
 			TreeNode subTree = entry.getBeforeSubTree();
 			if ( subTree != null )
 			{
 				hasSubTree = true;
 				validateAllLessThan( subTree, currentValue );
 			}
 			else if ( hasSubTree )
 			{
 				throw new RuntimeException( "Leaf/no leaf inconsistency");
 			}
 			keyEntry = keyEntry.getNextKey();
 		}
 		// root so we don't validate to few entries
 		if ( entryCount >= getOrder() )
 		{
 			throw new RuntimeException( "To many entries" );
 		}
 		if ( hasSubTree )
 		{
 			TreeNode subTree = entry.getAfterSubTree();
 			if ( subTree == null )
 			{
 				throw new RuntimeException( "Leaf/no leaf inconsistency" );
 			}
 			validateAllGreaterThan( subTree, currentValue );
 		}
 	}
 	
 	private void validateAllLessThan( TreeNode treeNode, long value )
 	{
 		long currentValue = Long.MIN_VALUE;
 		KeyEntry entry = null;
 		KeyEntry keyEntry = treeNode.getFirstEntry();
 		boolean hasSubTree = false;
 		int entryCount = 0;
 		while ( keyEntry != null )
 		{
 			entryCount++;
 			entry = keyEntry;
 			if ( entry.getKey() >= value )
 			{
 				throw new RuntimeException( "Depth key inconsistency" );
 			}
 			if ( entry.getKey() <= currentValue )
 			{
 				throw new RuntimeException( "Key entry ordering inconsistency");
 			}
 			currentValue = entry.getKey();
 			TreeNode subTree = entry.getBeforeSubTree();
 			if ( subTree != null )
 			{
 				hasSubTree = true;
 				validateAllLessThan( subTree, currentValue );
 			}
 			else if ( hasSubTree )
 			{
 				throw new RuntimeException( "Leaf/no leaf inconsistency");
 			}
 			keyEntry = keyEntry.getNextKey();
 		}
 		if ( entryCount < getOrder() / 2 - 1 )
 		{
 			throw new RuntimeException( "To few entries" );
 		}
 		if ( entryCount >= getOrder() )
 		{
 			throw new RuntimeException( "To many entries" );
 		}
 		if ( hasSubTree )
 		{
 			TreeNode subTree = entry.getAfterSubTree();
 			if ( subTree == null )
 			{
 				throw new RuntimeException( "Leaf/no leaf inconsistency" );
 			}
 			validateAllGreaterThan( subTree, currentValue );
 		}
 	}
 
 	private void validateAllGreaterThan( TreeNode treeNode, long value )
 	{
 		long currentValue = Long.MIN_VALUE;
 		KeyEntry entry = null;
 		KeyEntry keyEntry = treeNode.getFirstEntry();
 		boolean hasSubTree = false;
 		int entryCount = 0;
 		while ( keyEntry != null )
 		{
 			entryCount++;
 			entry = keyEntry;
 			if ( entry.getKey() <= value )
 			{
 				throw new RuntimeException( "Depth key inconsistency" );
 			}
 			if ( entry.getKey() <= currentValue )
 			{
 				throw new RuntimeException( "Key entry ordering inconsistency");
 			}
 			currentValue = entry.getKey();
 			TreeNode subTree = entry.getBeforeSubTree();
 			if ( subTree != null )
 			{
 				hasSubTree = true;
 				validateAllLessThan( subTree, currentValue );
 			}
 			else if ( hasSubTree )
 			{
 				throw new RuntimeException( "Leaf/no leaf inconsistency");
 			}
 			keyEntry = keyEntry.getNextKey();
 		}
 		if ( entryCount < getOrder() / 2 - 1 )
 		{
 			throw new RuntimeException( "To few entries" );
 		}
 		if ( entryCount >= getOrder() )
 		{
 			throw new RuntimeException( "To many entries" );
 		}
 		if ( hasSubTree )
 		{
 			TreeNode subTree = entry.getAfterSubTree();
 			if ( subTree == null )
 			{
 				throw new RuntimeException( "Leaf/no leaf inconsistency" );
 			}
 			validateAllGreaterThan( subTree, currentValue );
 		}
 	}
 	
 	/**
 	 * Adds a entry to this b-tree. If key already exist a runtime exception
 	 * is thrown. The <CODE>value</CODE> has to be a valid Neo property.
 	 * 
 	 * @param key the key of the entry
 	 * @param value value of the entry
 	 * @return the added entry
 	 */
 	public KeyEntry addEntry( long key, Object value )
 	{
 		return treeRoot.addEntry( key, value );
 	}
 	
 	/**
 	 * Adds the entry to this b-tree. If key already exist nothing is modified 
 	 * and <CODE>null</CODE> is returned. The <CODE>value</CODE> has to be a 
 	 * valid Neo property.
 	 * 
 	 * @param key the key of the entry
 	 * @param value value of the entry
 	 * @return the added entry or <CODE>null</CODE> if key already existed
 	 */
 	public KeyEntry addIfAbsent( long key, Object value )
 	{
 		return treeRoot.addEntry( key, value, true );
 	}
 	
 	/**
 	 * Returns the value of an entry or null if no such entry exist.
 	 * 
 	 * @param key for the entry
 	 * @return value of the entry
 	 */
 	public Object getEntry( long key )
 	{
 		KeyEntry entry = treeRoot.getEntry( key );
 		if ( entry != null )
 		{
 			return entry.getValue();
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the closest entry value where <CODE>Entry.key &lt= key</CODE> or
 	 * null if no such entry exist. 
 	 * 
 	 * @param key the key
 	 * @return the value of the closest lower entry
 	 */
 	public Object getClosestLowerEntry( long key )
 	{
 		KeyEntry entry = treeRoot.getClosestLowerEntry( null, key );
 		if ( entry != null )
 		{
 			return entry.getValue();
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the closest entry value where <CODE>Entry.key &gt= key</CODE> or
 	 * null if no such entry exist.
 	 * 
 	 * @param key the key
 	 * @return the value of the closest lower entry
 	 */
 	public Object getClosestHigherEntry( long key )
 	{
 		KeyEntry entry = treeRoot.getClosestHigherEntry( null, key );
 		if ( entry != null )
 		{
 			return entry.getValue();
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the <CODE>KeyEntry</CODE> for a key or null if it doesn't exist.
 	 * 
 	 * @param key the key
 	 * @return the entry connected to the key
 	 */
 	public KeyEntry getAsKeyEntry( long key )
 	{
 		return treeRoot.getEntry( key );
 	}
 	
 	/**
 	 * Removes a entry and returns the value of the entry. If entry doesn't 
 	 * exist null is returned.
 	 * 
 	 * @param key the key
 	 * @return value of removed entry
 	 */
 	public Object removeEntry( long key )
 	{
 		return treeRoot.removeEntry( key );
 	}
 	
 	int getOrder()
 	{
 		return 9;
 	}
 	
 	NeoService getNeo()
 	{
 		return neo;
 	}
 	
 	/**
 	 * Returns the values of all entries in this b-tree
 	 * @return all values in this b-tree.
 	 */
 	public Iterable<Object> values()
 	{
 		Traverser trav = treeRoot.getUnderlyingNode().traverse( 
			Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, 
 			new ReturnableEvaluator()
 			{
 				public boolean isReturnableNode( TraversalPosition pos )
 				{
 					Relationship last = pos.lastRelationshipTraversed();
 					if ( last != null && last.getType().equals( 
 						RelTypes.KEY_ENTRY ) )
 					{
 						return true;
 					}
 					return false;
 				}
 			}, RelTypes.KEY_ENTRY, Direction.OUTGOING, 
 			RelTypes.SUB_TREE, Direction.OUTGOING );
 		return new ValueTraverser( trav );
 	}
 	
 	private static class ValueTraverser implements Iterable<Object>, 
 		Iterator<Object>
 	{
 		private Iterator<Node> itr;
 		
 		ValueTraverser( Traverser trav )
 		{
 			this.itr = trav.iterator();
 		}
 
 		public boolean hasNext()
         {
 			return itr.hasNext();
         }
 
 		public Object next()
         {
 			Node node = itr.next();
 	        return node.getSingleRelationship( RelTypes.KEY_ENTRY, 
 	        	Direction.INCOMING ).getProperty( KeyEntry.VALUE );
         }
 
 		public void remove()
         {
 			throw new UnsupportedOperationException();
         }
 
 		public Iterator<Object> iterator()
         {
 			return this;
         }
 	}
 	
 	public Iterable<KeyEntry> entries()
 	{
 		EntryReturnableEvaluator entryEvaluator = 
 			new EntryReturnableEvaluator();
 		
 		Traverser trav = treeRoot.getUnderlyingNode().traverse( 
			Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, 
 			entryEvaluator, RelTypes.KEY_ENTRY, Direction.OUTGOING, 
 			RelTypes.SUB_TREE, Direction.OUTGOING );
 		return new EntryTraverser( trav, this, entryEvaluator );
 	}
 	
 	private static class EntryTraverser implements Iterable<KeyEntry>, 
 		Iterator<KeyEntry>
 	{
 		private EntryReturnableEvaluator entryEvaluator;
 		private BTree bTree;
 		private Iterator<Node> itr;
 		
 		EntryTraverser( Traverser trav, BTree tree, 
 			EntryReturnableEvaluator entry )
 		{
 			this.itr = trav.iterator();
 			this.bTree = tree;
 			this.entryEvaluator = entry;
 		}
 	
 		public boolean hasNext()
 	    {
 			return itr.hasNext();
 	    }
 	
 		public KeyEntry next()
 	    {
 			Node node = itr.next();
 			TreeNode treeNode = new TreeNode( bTree, 
 				entryEvaluator.getCurrentTreeNode() );
 	        return new KeyEntry( treeNode, node.getSingleRelationship( 
 	        	RelTypes.KEY_ENTRY, Direction.INCOMING ) );
 	    }
 	
 		public void remove()
 	    {
 			throw new UnsupportedOperationException();
 	    }
 	
 		public Iterator<KeyEntry> iterator()
 	    {
 			return this;
 	    }
 	}
 	
 	private static class EntryReturnableEvaluator implements ReturnableEvaluator
 	{
 		private Node currentTreeNode = null;
 		
 		public Node getCurrentTreeNode()
 		{
 			return currentTreeNode;
 		}
 		
 		public boolean isReturnableNode( TraversalPosition pos )
         {
 			if ( !pos.notStartNode() )
 			{
 				currentTreeNode = pos.currentNode();
 				return false;
 			}
 			Relationship last = pos.lastRelationshipTraversed();
 			if ( last.isType( RelTypes.KEY_ENTRY ) )
 			{
 				return true;
 			}
 			if ( last.isType( RelTypes.SUB_TREE ) )
 			{
 				currentTreeNode = pos.currentNode();
 			}
 			return false;
         }
 	}
 }
