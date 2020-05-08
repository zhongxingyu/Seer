 package org.neo4j.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Iterator;
 
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.util.btree.BTree.RelTypes;
 import org.neo4j.util.sortedtree.SortedTree;
import org.neo4j.commons.iterator.IterableWrapper;
 
 public class IndexedNeoCollection<T extends NodeWrapper>
 	extends AbstractNeoSet<T>
 {
 	private Node rootNode;
 	private Class<T> instanceClass;
 	private Comparator<T> comparator;
 	private SortedTree index;
 	
 	public IndexedNeoCollection( NeoService neo, Node rootNode,
 		Comparator<T> comparator, Class<T> instanceClass )
 	{
 		super( neo );
 		this.rootNode = rootNode;
 		this.instanceClass = instanceClass;
 		this.comparator = comparator;
 		instantiateIndex();
 	}
 	
 	private Node ensureTheresARoot()
 	{
 		Transaction tx = neo().beginTx();
 		try
 		{
 			Node result = null;
 			Relationship relationship = rootNode.getSingleRelationship(
 				RelTypes.TREE_ROOT, Direction.OUTGOING );
 			if ( relationship != null )
 			{
 				result = relationship.getOtherNode( rootNode );
 			}
 			else
 			{
 				result = neo().createNode();
 				rootNode.createRelationshipTo( result, RelTypes.TREE_ROOT );
 			}
 			tx.success();
 			return result;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	private void instantiateIndex()
 	{
 		Transaction tx = neo().beginTx();
 		try
 		{
 			Node treeRootNode = ensureTheresARoot();
 			this.index = new SortedTree( neo(), treeRootNode,
 				new ComparatorWrapper( this.comparator ) );
 			tx.success();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	protected Node rootNode()
 	{
 		return this.rootNode;
 	}
 	
 	protected SortedTree index()
 	{
 		return this.index;
 	}
 	
 	protected T instantiateItem( Node itemNode )
 	{
 		return NodeWrapperImpl.newInstance( instanceClass, neo(), itemNode );
 	}
 	
 	public boolean add( T item )
 	{
 		return index().addNode( item.getUnderlyingNode() );
 	}
 
 	public void clear()
 	{
 		Transaction tx = neo().beginTx();
 		try
 		{
 			index().delete();
 			instantiateIndex();
 			tx.success();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 
 	public boolean contains( Object item )
 	{
 		T nodeItem = ( T ) item;
 		return index().containsNode( nodeItem.getUnderlyingNode() );
 	}
 
 	public boolean isEmpty()
 	{
 		return index().getSortedNodes().iterator().hasNext();
 	}
 
 	public Iterator<T> iterator()
 	{
 		Iterator<T> iterator = new IterableWrapper<T, Node>(
 			index().getSortedNodes() )
 		{
 			@Override
 			protected T underlyingObjectToObject( Node node )
 			{
 				return instantiateItem( node );
 			}
 		}.iterator();
 		return new TxIterator<T>( neo(), iterator );
 	}
 
 	public boolean remove( Object item )
 	{
 		T nodeItem = ( T ) item;
 		return index().removeNode( nodeItem.getUnderlyingNode() );
 	}
 
 	public boolean retainAll( Collection<?> items )
 	{
 		throw new UnsupportedOperationException( "Not implemented yet" );
 	}
 
 	public int size()
 	{
 		return toArray().length;
 	}
 	
 	private <R> Collection<R> toCollection()
 	{
 		Transaction tx = neo().beginTx();
 		try
 		{
 			Collection<R> result = new ArrayList<R>();
 			for ( Node node : index().getSortedNodes() )
 			{
 				result.add( ( R ) instantiateItem( node ) );
 			}
 			tx.success();
 			return result;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 
 	public Object[] toArray()
 	{
 		return toCollection().toArray();
 	}
 
 	public <R> R[] toArray( R[] array )
 	{
 		return toCollection().toArray( array );
 	}
 	
 	/**
 	 * Since this collection creates a sub-root to the supplied collection
 	 * root node, it will have to be explicitly deleted from outside when
 	 * you don't want this collection to exist anymore.
 	 */
 	public void delete()
 	{
 		Transaction tx = neo().beginTx();
 		try
 		{
 			index().delete();
 			tx.success();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	private class ComparatorWrapper implements Comparator<Node>
 	{
 		private Comparator<T> source;
 		
 		ComparatorWrapper( Comparator<T> source )
 		{
 			this.source = source;
 		}
 
 		public int compare( Node o1, Node o2 )
 		{
 			// This is slow, I guess
 			return source.compare(
 				NodeWrapperImpl.newInstance( instanceClass, neo(), o1 ),
 				NodeWrapperImpl.newInstance( instanceClass, neo(), o2 ) );
 		}
 	}
 }
