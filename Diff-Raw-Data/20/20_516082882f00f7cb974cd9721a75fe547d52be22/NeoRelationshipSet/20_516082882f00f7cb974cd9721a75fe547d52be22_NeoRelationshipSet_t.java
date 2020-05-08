 package org.neo4j.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.api.core.Transaction;
import org.neo4j.commons.iterator.FilteringIterator;
import org.neo4j.commons.iterator.IteratorWrapper;
 
 /**
  * A {@link Set} implemented with neo.
  * @author mattias
  *
  * @param <T> the type of objects in the set.
  */
 public abstract class NeoRelationshipSet<T> extends AbstractNeoSet<T>
 	implements Set<T>
 {
 	private Node node;
 	private RelationshipType type;
 	private Direction direction;
 	
 	/**
 	 * @param node the {@link Node} to act as the collection.
 	 * @param type the relationship type to use internally for each object.
 	 */
 	public NeoRelationshipSet( NeoService neo, Node node,
 		RelationshipType type )
 	{
 		this( neo, node, type, Direction.OUTGOING );
 	}
 	
 	/**
 	 * @param node the {@link Node} to act as the collection.
 	 * @param direction the direction to use for the relationships.
 	 * @param type the relationship type to use internally for each object.
 	 */
 	public NeoRelationshipSet( NeoService neo, Node node, RelationshipType type,
 		Direction direction )
 	{
 		super( neo );
 		
 		if ( direction == null || direction == Direction.BOTH )
 		{
 			throw new IllegalArgumentException(
 				"Only OUTGOING and INCOMING direction is allowed, since " +
 				"this is a read/write collection" );
 		}
 		
 		this.node = node;
 		this.type = type;
 		this.direction = direction;
 	}
 	
 	protected Node getUnderlyingNode()
 	{
 		return this.node;
 	}
 	
 	protected RelationshipType getRelationshipType()
 	{
 		return this.type;
 	}
 	
 	protected Direction getDirection()
 	{
 		return direction;
 	}
 	
 	protected Direction getInverseDirection()
 	{
 		return getDirection().reverse();
 	}
 	
 	protected boolean directionIsOut()
 	{
 		return getDirection() == Direction.OUTGOING;
 	}
 	
 	public boolean add( T item )
 	{
 		Transaction tx = neo().beginTx();
 		try
 		{
 			if ( contains( item ) )
 			{
 				return false;
 			}
 			
 			Node otherNode = getNodeFromItem( item );
 			Node startNode = directionIsOut() ? node : otherNode;
 			Node endNode = directionIsOut() ? otherNode : node;
 			Relationship relationship =
 				startNode.createRelationshipTo( endNode, type );
 			itemAdded( item, relationship );
 			tx.success();
 			return true;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	protected void itemAdded( T item, Relationship relationship )
 	{
 	}
 	
 	protected abstract Node getNodeFromItem( Object item );
 
 	public void clear()
 	{
 		Transaction tx = neo().beginTx();
 		try
 		{
 			Iterator<Relationship> itr = getAllRelationships();
 			while ( itr.hasNext() )
 			{
 				removeItem( itr.next() );
 			}
 			tx.success();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 
 	public boolean contains( Object item )
 	{
 		Transaction tx = neo().beginTx();
 		try
 		{
 			boolean result = findRelationship( item ) != null;
 			tx.success();
 			return result;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	protected boolean shouldIncludeRelationship( Relationship rel )
 	{
 		return true;
 	}
 	
 	protected Iterator<Relationship> getAllRelationships()
 	{
 		return new FilteringIterator<Relationship>(
 			node.getRelationships( type, getDirection() ).iterator() )
 		{
             @Override
             protected boolean passes( Relationship item )
             {
                 return shouldIncludeRelationship( item );
             }
 		};
 	}
 	
 	protected Relationship findRelationship( Object item )
 	{
 		Node otherNode = getNodeFromItem( item );
 		Relationship result = null;
 		for ( Relationship rel : otherNode.getRelationships(
 			type, getInverseDirection() ) )
 		{
 		    if ( !shouldIncludeRelationship( rel ) )
 		    {
 		        continue;
 		    }
 		    
 			if ( rel.getOtherNode( otherNode ).equals( node ) )
 			{
 				result = rel;
 				break;
 			}
 		}
 		return result;
 	}
 
 	public boolean isEmpty()
 	{
 		Transaction tx = neo().beginTx();
 		try
 		{
 			return !getAllRelationships().hasNext();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 
 	public Iterator<T> iterator()
 	{
 		Transaction tx = neo().beginTx();
 		try
 		{
 			Iterator<T> result = new IteratorWrapper<T, Relationship>(
 			    getAllRelationships() )
 		    {
                 @Override
                 protected T underlyingObjectToObject( Relationship rel )
                 {
                     return newObject( rel.getOtherNode(
                         NeoRelationshipSet.this.node ), rel );
                 }
 		    };
 			tx.success();
 			return result;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	protected void removeItem( Relationship rel )
 	{
 		rel.delete();
 	}
 
 	public boolean remove( Object item )
 	{
 		Transaction tx = neo().beginTx();
 		try
 		{
 			Relationship rel = findRelationship( item );
 			boolean changed = false;
 			if ( rel != null )
 			{
 				removeItem( rel );
 				changed = true;
 			}
 			tx.success();
 			return changed;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 
 	public boolean retainAll( Collection<?> items )
 	{
 		Transaction tx = neo().beginTx();
 		try
 		{
 		    Collection<Relationship> relationships =
 		        new HashSet<Relationship>();
 		    Iterator<Relationship> allRelationships = getAllRelationships();
 		    while ( allRelationships.hasNext() )
 		    {
 		        relationships.add( allRelationships.next() );
 		    }
 		    
 			for ( Object item : items )
 			{
 				Relationship rel = findRelationship( item );
 				if ( rel != null )
 				{
 				    relationships.remove( rel );
 				}
 			}
 		    
 			Collection<T> itemsToRemove = new HashSet<T>();
 			for ( Relationship rel : relationships )
 			{
 			    itemsToRemove.add( newObject( getOtherNode( rel ), rel ) );
 			}
 			
 			boolean result = this.removeAll( itemsToRemove );
 			tx.success();
 			return result;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 
 	public int size()
 	{
 		Transaction tx = neo().beginTx();
 		try
 		{
 			int counter = 0;
 			Iterator<Relationship> itr = getAllRelationships();
 			while ( itr.hasNext() )
 			{
 				itr.next();
 				counter++;
 			}
 			tx.success();
 			return counter;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	protected abstract T newObject( Node node, Relationship relationship );
 	
 	protected Node getOtherNode( Relationship relationship )
 	{
 		return relationship.getOtherNode( this.getUnderlyingNode() );
 	}
 
 	public Object[] toArray()
 	{
 		Transaction tx = neo().beginTx();
 		try
 		{
 			Iterator<Relationship> itr = getAllRelationships();
 			Collection<Object> result = newArrayCollection();
 			while ( itr.hasNext() )
 			{
 				Relationship rel = itr.next();
 				result.add( newObject( getOtherNode( rel ), rel ) );
 			}
 			tx.success();
 			return result.toArray();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 
 	public <R> R[] toArray( R[] array )
 	{
 		Transaction tx = neo().beginTx();
 		try
 		{
 			Iterator<Relationship> itr = getAllRelationships();
 			Collection<R> result = newArrayCollection();
 			while ( itr.hasNext() )
 			{
 				Relationship rel = itr.next();
 				result.add( ( R ) newObject( getOtherNode( rel ), rel ) );
 			}
 			
 			int i = 0;
 			for ( R item : result )
 			{
 				array[ i++ ] = item;
 			}
 			tx.success();
 			return array;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	protected <R> Collection<R> newArrayCollection()
 	{
 		return new ArrayList<R>();
 	}
 }
