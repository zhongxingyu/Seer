 package org.neo4j.util.timeline;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser;
 import org.neo4j.api.core.Traverser.Order;
 import org.neo4j.util.btree.BTree;
 
 /**
  * A utility to order nodes after a timestamp. This implementation uses the 
  * {@link org.neo4j.util.btree.BTree BTree} to index (at certain intervals) 
  * nodes in the timeline to speed up range quering.
  * <p>
  * Note: this implementation is not threadsafe (yet). 
  */
 // not thread safe yet
 public class Timeline implements TimelineIndex
 {
 	public static enum RelTypes implements RelationshipType
 	{
 		TIMELINE_INSTANCE,
 		TIMELINE_NEXT_ENTRY,
 	}
 	
 	private static final String TIMESTAMP = "timestamp";
 	private static final String TIMELINE_NAME = "timeline_name";
 	private static final String TIMELINE_IS_INDEXED = "timeline_indexed";
 	private static final String INDEX_COUNT = "index_count";
 	private static final int INDEX_TRIGGER_COUNT = 1000;
 	
 	private final Node underlyingNode;
 	private boolean useIndexing = false;
 	private BTree bTree = null;
 	private final NeoService neo;
 	
 	// lazy init cache holders for first and last
 	private Node firstNode;
 	private Node lastNode;
 
 	private String name; 
 	
 	/**
 	 * Creates/loads a timeline. The <CODE>underlyingNode</CODE> can either
 	 * be a new (just created) node or a node that already represents a 
 	 * previously timeline.
 	 *
 	 * @param name The unique name of the timeline or <CODE>null</CODE> if 
 	 * timeline already exist
 	 * @param underlyingNode The underlying node representing the timeline
 	 * @param indexed Set to <CODE>true</CODE> if this timeline is indexed 
 	 * @param neo The embedded neo instance
 	 */
 	public Timeline( String name, Node underlyingNode, boolean indexed, 
 		NeoService neo )
 	{
 		if ( underlyingNode == null || neo == null )
 		{
 			throw new IllegalArgumentException( 
 				"Null parameter underlyingNode=" + underlyingNode +
 				" neo=" + neo );
 		}
 		useIndexing = false;
 		this.underlyingNode = underlyingNode;
 		this.neo = neo;
 		Transaction tx = neo.beginTx();
 		try
 		{
 			if ( underlyingNode.hasProperty( TIMELINE_NAME ) )
 			{
 				String storedName = (String) underlyingNode.getProperty( 
 					TIMELINE_NAME );
 				if ( name != null && !storedName.equals( name ) )
 				{
 					throw new IllegalArgumentException( "Name of timeline " + 
 						"for node=" + underlyingNode.getId() + "," + 
 						storedName + " is not same as passed in name=" + 
 						name );
 				}
 				if ( name == null )
 				{
 					underlyingNode.setProperty( TIMELINE_NAME, name );
 				}
 				this.name = name;
 			}
 			else
 			{
 				underlyingNode.setProperty( TIMELINE_NAME, name );
 				this.name = name;
 			}
 			if ( underlyingNode.hasProperty( TIMELINE_IS_INDEXED ) )
 			{
 				if ( !underlyingNode.getProperty( TIMELINE_IS_INDEXED ).equals( 
 					indexed ) )
 				{
 					throw new IllegalArgumentException( "indexed=" + 
 						indexed + " do not match timeline" );
 				}
 				this.useIndexing = indexed;
 			}
 			else
 			{
 				underlyingNode.setProperty( TIMELINE_IS_INDEXED, indexed );
 				this.useIndexing = indexed;
 			}
 			if ( useIndexing )
 			{
 				Relationship bTreeRel = underlyingNode.getSingleRelationship( 
 					BTree.RelTypes.TREE_ROOT, Direction.OUTGOING );
 				if ( bTreeRel == null )
 				{
 					Node bTreeNode = neo.createNode();
 					bTreeRel = underlyingNode.createRelationshipTo( bTreeNode, 
 						BTree.RelTypes.TREE_ROOT );
 				}
 				bTree = new BTree( neo, bTreeRel.getEndNode() );
 			}
 			tx.success();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
     /**
      * Creates/loads a indexed timeline. The <CODE>underlyingNode</CODE> can 
      * either be a new (just created) node or a node that already represents a 
      * previously timeline.
      *
      * @param name The unique name of the timeline or <CODE>null</CODE> if 
      * timeline already exist
      * @param underlyingNode The underlying node representing the timeline
      * @param neo The embedded neo instance
      */
     public Timeline( String name, Node underlyingNode, NeoService neo )
     {
         this( name, underlyingNode, true, neo );
     }
     
     /**
 	 * Returns the underlying node representing this timeline.
 	 * 
 	 * @return The underlying node representing this timeline
 	 */
 	public Node getUnderlyingNode()
 	{
 		return underlyingNode;
 	}
 	
 	/**
 	 * Return the last node in the timeline or <CODE>null</CODE> if timeline
 	 * is empty.
 	 * 
 	 * @return The last node in the timeline
 	 */
 	public Node getLastNode()
 	{
 		if ( lastNode != null )
 		{
 			return lastNode;
 		}
 		Transaction tx = neo.beginTx();
 		try
 		{
 			Relationship rel = underlyingNode.getSingleRelationship( 
 				RelTypes.TIMELINE_NEXT_ENTRY, Direction.INCOMING );
 			if ( rel == null )
 			{
 				return null;
 			}
 			lastNode = rel.getStartNode().getRelationships( 
 				RelTypes.TIMELINE_INSTANCE, Direction.OUTGOING 
 				).iterator().next().getEndNode();
 			tx.success();
 			return lastNode;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	/**
 	 * Returns the first node in the timeline or <CODE>null</CODE> if timeline
 	 * is empty.
 	 * 
 	 * @return The first node in the timeline
 	 */
 	public Node getFirstNode()
 	{
 		if ( firstNode != null )
 		{
 			return firstNode;
 		}
 		Transaction tx = neo.beginTx();
 		try
 		{
 			Relationship rel = underlyingNode.getSingleRelationship( 
 				RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING );
 			if ( rel == null )
 			{
 				return null;
 			}
 			firstNode = rel.getEndNode().getRelationships( 
 				RelTypes.TIMELINE_INSTANCE, Direction.OUTGOING 
 				).iterator().next().getEndNode();
 			tx.success();
 			return firstNode;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	/**
 	 * Adds a node in the timeline using <CODE>timestamp</CODE>.
 	 * 
 	 * @param nodeToAdd The node to add to the timeline
 	 * @param timestamp The timestamp to use
 	 * @throws IllegalArgumentException If already added to this timeline or or 
 	 * <CODE>null</CODE> node
 	 */
 	public void addNode( Node nodeToAdd, long timestamp )
 	{
 		if ( nodeToAdd == null )
 		{
 			throw new IllegalArgumentException( "Null node" );
 		}
 		Transaction tx = neo.beginTx();
 		try
 		{
 			for ( Relationship rel : nodeToAdd.getRelationships( 
 				RelTypes.TIMELINE_INSTANCE ) )
 			{
 				if ( rel.getProperty( TIMELINE_NAME, "" ).equals( name ) )
 				{
 					throw new IllegalArgumentException( "Node[" + 
 						nodeToAdd.getId() + 
 						"] already connected to Timeline[" + name + "]" );
 				}
 			}
 			Relationship rel = underlyingNode.getSingleRelationship( 
 				RelTypes.TIMELINE_NEXT_ENTRY, Direction.INCOMING );
 			if ( rel == null )
 			{
 				// timeline was empty
 				Node node = createNewTimeNode( timestamp, nodeToAdd );
 				underlyingNode.createRelationshipTo( node, 
 					RelTypes.TIMELINE_NEXT_ENTRY );
 				node.createRelationshipTo( underlyingNode, 
 					RelTypes.TIMELINE_NEXT_ENTRY );
 				firstNode = nodeToAdd;
 				lastNode = nodeToAdd;
 				updateNodeAdded( timestamp );
 			}
 			else
 			{
 				Node previousLast = rel.getStartNode();
 				long previousTime = ( Long ) previousLast.getProperty( 
 					TIMESTAMP );
 				if ( timestamp > previousTime )
 				{
 					// add it last in chain
 					Node node = createNewTimeNode( timestamp, nodeToAdd );
 					rel.delete();
 					previousLast.createRelationshipTo( node, 
 						RelTypes.TIMELINE_NEXT_ENTRY );
 					node.createRelationshipTo( underlyingNode, 
 						RelTypes.TIMELINE_NEXT_ENTRY );
 					lastNode = nodeToAdd;
 					updateNodeAdded( timestamp );
 				}
 				else if ( timestamp == previousTime )
 				{
 					previousLast.createRelationshipTo( nodeToAdd, 
 						RelTypes.TIMELINE_INSTANCE );
 				}
 				else
 				{
 					// find where to insert
 					Iterator<Node> itr = 
 						getAllTimeNodesAfter( timestamp ).iterator();
 					assert itr.hasNext();
 					Node next = itr.next();
 					rel = next.getSingleRelationship( 
 						RelTypes.TIMELINE_NEXT_ENTRY, Direction.INCOMING );
 					assert rel != null;
 					Node previous = rel.getStartNode();
 					long previousTimestamp = Long.MIN_VALUE;
 					if ( !previous.equals( underlyingNode ) )
 					{
 						previousTimestamp = (Long) previous.getProperty( 
 							TIMESTAMP );
 					}
 					if ( previousTimestamp == timestamp )
 					{
 						// just connect previous with node to add
 						previous.createRelationshipTo( nodeToAdd, 
 							RelTypes.TIMELINE_INSTANCE );
 						return;
 					}
 					long nextTimestamp = (Long) 
 						next.getProperty( TIMESTAMP );
 					if ( nextTimestamp == timestamp )
 					{
 						// just connect next with node to add
 						next.createRelationshipTo( nodeToAdd, 
 							RelTypes.TIMELINE_INSTANCE );
 						return;
 					}
 					
 					assert previousTimestamp < timestamp;
 					assert nextTimestamp > timestamp;
 					
 					Node node = createNewTimeNode( timestamp, nodeToAdd );
 					rel.delete();
 					previous.createRelationshipTo( node, 
 						RelTypes.TIMELINE_NEXT_ENTRY );
 					node.createRelationshipTo( next, 
 						RelTypes.TIMELINE_NEXT_ENTRY );
 					if ( previous.equals( underlyingNode ) )
 					{
 						firstNode = nodeToAdd;
 					}
 					updateNodeAdded( timestamp );
 				}
 			}
 			tx.success();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	private Node createNewTimeNode( long timestamp, Node nodeToAdd )
 	{
 		Node node = neo.createNode();
 		node.setProperty( TIMESTAMP, timestamp );
 		Relationship instanceRel = node.createRelationshipTo( nodeToAdd, 
 			RelTypes.TIMELINE_INSTANCE );
 		instanceRel.setProperty( TIMELINE_NAME, name );
 		return node;
 	}
 	
 	public long getTimestampForNode( Node node )
 	{
 		Transaction tx = neo.beginTx();
 		try
 		{
 			Traverser traverser = node.traverse( Traverser.Order.DEPTH_FIRST,
				StopEvaluator.END_OF_NETWORK, new ReturnableEvaluator()
 			{
 				public boolean isReturnableNode( TraversalPosition position )
 				{
 					Node currentNode = position.currentNode();
 					return currentNode != null && !currentNode.hasRelationship(
 						RelTypes.TIMELINE_INSTANCE, Direction.INCOMING );
 				}
 			}, RelTypes.TIMELINE_INSTANCE, Direction.INCOMING );
 			
 			Iterator<Node> hits = traverser.iterator();
 			Long result = null;
 			if ( hits.hasNext() )
 			{
 				Node hit = hits.next();
 				result = ( Long ) hit.getProperty( TIMESTAMP );
 			}
 			else
 			{
 				throw new RuntimeException( "No timpestamp found for '" + node + 
 					"' maybe it's not in the timeline?" );
 			}
 			tx.success();
 			return result;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	private synchronized void updateNodeAdded( final long timestamp )
 	{
 		if ( !useIndexing )
 		{
 			return;
 		}
 		Long nodeId = (Long) bTree.getClosestHigherEntry( timestamp );
 		if ( nodeId == null )
 		{
 			// no indexing yet, check if time to add index
 			int indexCount = (Integer) 
 				underlyingNode.getProperty( INDEX_COUNT, 0 );
 			indexCount++;
 			if ( indexCount >= INDEX_TRIGGER_COUNT )
 			{
 				indexCount = createIndex( underlyingNode, indexCount );
 			}
 			underlyingNode.setProperty( INDEX_COUNT, indexCount );
 		}
 		else
 		{
 			Node indexedNode = neo.getNodeById( nodeId );
 			int indexCount = (Integer) indexedNode.getProperty( INDEX_COUNT );
 			indexCount++;
 			if ( indexCount >= INDEX_TRIGGER_COUNT )
 			{
 				indexCount = createIndex( indexedNode, indexCount );
 			}
 			indexedNode.setProperty( INDEX_COUNT, indexCount );
 		}
 	}
 	
 	// returns new count to set on next higher index and
 	// creates the new indexing relationship
 	private int createIndex( Node startIndexNode, int currentCount )
 	{
 		assert useIndexing;
 		int newCount = 0;
 		// use 0.33f beacuse most timelines are not random timestamp
 		// insertion, instead they just grow at the end, so 0.33 (instead of 
 		// 0.5) results in less balancing of tree (and tree depth at start)
 		int timesToTraverse = (int) (INDEX_TRIGGER_COUNT * 0.33f);
 		assert timesToTraverse > 0;
 		Node newIndexedNode = startIndexNode;
 		for ( int i = 0; i < timesToTraverse; i++ )
 		{
 			newIndexedNode = newIndexedNode.getSingleRelationship( 
 				RelTypes.TIMELINE_NEXT_ENTRY, 
 				Direction.INCOMING ).getStartNode();
 			newCount++;
 			assert !newIndexedNode.hasProperty( INDEX_COUNT );
 		}
 		long timestamp = (Long) newIndexedNode.getProperty( TIMESTAMP );
 		bTree.addEntry( timestamp, newIndexedNode.getId() );
 		newIndexedNode.setProperty( INDEX_COUNT, 
 			currentCount - timesToTraverse );
 		return newCount;
 	}
 	
 	/**
 	 * Removes a node from the timeline. 
 	 * 
 	 * @param nodeToRemove The node to remove from this timeline
 	 * @throws IllegalArgumentException if <CODE>null</CODE> node or node not 
 	 * connected to this timeline.
 	 */
 	public void removeNode( Node nodeToRemove )
 	{
 		if ( nodeToRemove == null )
 		{
 			throw new IllegalArgumentException( "Null parameter." );
 		}
 		if ( nodeToRemove.equals( underlyingNode ) )
 		{
 			throw new IllegalArgumentException( 
 				"Cannot remove underlying node" );
 		}
 		Transaction tx = neo.beginTx();
 		try
 		{
 			Relationship instanceRel = null;
 			for ( Relationship rel : nodeToRemove.getRelationships( 
 				RelTypes.TIMELINE_INSTANCE ) )
 			{
 				if ( rel.getProperty( TIMELINE_NAME, "" ).equals( name ) )
 				{
 					assert instanceRel == null;
 					instanceRel = rel;
 				}
 			}
 			if ( instanceRel == null )
 			{
 				throw new IllegalArgumentException( "Node[" + 
 					nodeToRemove.getId() + 
 					"] not added to Timeline[" + name + "]" );
 			}
 			Node node = instanceRel.getStartNode();
 			instanceRel.delete();
 			if ( firstNode != null && firstNode.equals( nodeToRemove ) )
 			{
 				firstNode = null;
 			}
 			if ( lastNode != null && lastNode.equals( nodeToRemove ) )
 			{
 				lastNode = null;
 			}
 			if ( node.getRelationships( RelTypes.TIMELINE_INSTANCE 
 				).iterator().hasNext() )
 			{
 				// still have instances connected to this time
 				return;
 			}
 			Relationship incoming = node.getSingleRelationship( 
 				RelTypes.TIMELINE_NEXT_ENTRY, Direction.INCOMING );
 			if ( incoming == null )
 			{
 				throw new RuntimeException( 
 					"No incoming relationship of " + 
 					RelTypes.TIMELINE_NEXT_ENTRY + " found" ); 
 			}
 			Relationship outgoing = node.getSingleRelationship( 
 				RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING );
 			if ( outgoing == null )
 			{
 				throw new RuntimeException( 
 					"No outgoing relationship of " + 
 					RelTypes.TIMELINE_NEXT_ENTRY + " found" ); 
 			}
 			Node previous = incoming.getStartNode();
 			Node next = outgoing.getEndNode();
 			incoming.delete();
 			outgoing.delete();
 			// TODO: this needs proper synchronization
 			if ( node.hasProperty( INDEX_COUNT ) )
 			{
 				long nodeId = (Long) bTree.removeEntry( (Long) node.getProperty( 
 					TIMESTAMP ) );
 				assert nodeId == node.getId();
 				int count = (Integer) node.getProperty( INDEX_COUNT );
 				count--;
 				if ( !previous.equals( underlyingNode ) && 
 					!previous.hasProperty( INDEX_COUNT ) )
 				{
 					previous.setProperty( INDEX_COUNT, count );
 					bTree.addEntry( (Long) previous.getProperty( TIMESTAMP ),
 						previous.getId() );
 				}
 			}
 			else
 			{
 				long timestamp = (Long) node.getProperty( TIMESTAMP );
 				if ( useIndexing )
 				{
 					Long nodeId = (Long) bTree.getClosestHigherEntry( 
 						timestamp );
 					if ( nodeId != null )
 					{
 						Node indexedNode = neo.getNodeById( nodeId );
 						int count = (Integer) indexedNode.getProperty( 
 							INDEX_COUNT );
 						count--;
 						indexedNode.setProperty( INDEX_COUNT, count );
 					}
 					else
 					{
 						if ( underlyingNode.hasProperty( INDEX_COUNT ) )
 						{
 							int count = (Integer) underlyingNode.getProperty( 
 								INDEX_COUNT );
 							count--;
 							underlyingNode.setProperty( INDEX_COUNT, count );
 						}
 					}
 				}
 			}
 			node.delete();
 			if ( !previous.equals( next ) )
 			{
 				previous.createRelationshipTo( next, 
 					RelTypes.TIMELINE_NEXT_ENTRY );
 			}
 			tx.success();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	/**
 	 * Returns all nodes in the timeline ordered by increasing timestamp.
 	 * 
 	 * @return All nodes in the timeline
 	 */
 	public Iterable<Node> getAllNodes()
 	{
 		return underlyingNode.traverse( Order.BREADTH_FIRST, 
			StopEvaluator.END_OF_NETWORK, 
 			new ReturnableEvaluator()
 			{
 				public boolean isReturnableNode( TraversalPosition position )
 				{
 					Relationship last = position.lastRelationshipTraversed();
 					if ( last != null && last.getType().equals( 
 						RelTypes.TIMELINE_INSTANCE ) )
 					{
 						return true;
 					}
 					return false;
 				}
 			}, 
 			RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING, 
 			RelTypes.TIMELINE_INSTANCE, Direction.OUTGOING );
 	}
 
 	Iterable<Node> getAllTimeNodes()
 	{
 		return underlyingNode.traverse( Order.DEPTH_FIRST, 
			StopEvaluator.END_OF_NETWORK, 
 			new ReturnableEvaluator()
 			{
 				public boolean isReturnableNode( TraversalPosition position )
 				{
 					return position.depth() > 0;
 				}
 			}, 
 			RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING );
 	}
 	
 	// from closest lower indexed start relationship
 	private Node getIndexedStartNode( long timestamp )
 	{
 		if ( useIndexing )
 		{
 			Node startNode = underlyingNode;
 			Long nodeId = (Long) bTree.getClosestLowerEntry( timestamp );
 			if ( nodeId != null )
 			{
 				startNode = neo.getNodeById( nodeId );
 			}
 			return startNode;
 		}
 		return underlyingNode;
 	}
 	
     public Iterable<Node> getNodes( long timestamp )
     {
         Node currentNode = getIndexedStartNode( timestamp );
         List<Node> nodeList = new ArrayList<Node>();
         do
         {
             Relationship rel = currentNode.getSingleRelationship( 
                 RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING );
             currentNode = rel.getEndNode();
             long currentTime = (Long) currentNode.getProperty( TIMESTAMP );
             if ( currentTime == timestamp )
             {
                 for ( Relationship instanceRel : currentNode.getRelationships( 
                     RelTypes.TIMELINE_INSTANCE, Direction.OUTGOING ) )
                 {
                     nodeList.add( instanceRel.getEndNode() );
                 }
                 break;
             }
             if ( currentTime > timestamp )
             {
                 break;
             }
         } while ( !currentNode.equals( underlyingNode ) );
         return nodeList;
     }
     
 	/**
 	 * Returns all nodes after (not including) the specified timestamp 
 	 * ordered by increasing timestamp.
 	 * 
 	 * @param timestamp The timestamp value, nodes with greater timestamp 
 	 * value will be returned
 	 * @return All nodes in the timeline after specified timestamp
 	 */
 	public Iterable<Node> getAllNodesAfter( final long timestamp )
 	{
 		Node startNode = getIndexedStartNode( timestamp );
 		return startNode.traverse( Order.DEPTH_FIRST, 
 			new StopEvaluator()
 			{
 				public boolean isStopNode( TraversalPosition position )
 				{
 					if ( position.lastRelationshipTraversed() != null && 
 						position.currentNode().equals( underlyingNode ) )
 					{
 						return true;
 					}
 					return false;
 				}
 			}, 
 			new ReturnableEvaluator()
 			{
 				private boolean timeOk = false;
 			
 				public boolean isReturnableNode( TraversalPosition position )
 				{
 					if ( position.currentNode().equals( underlyingNode ) )
 					{
 						return false;
 					}
 					Relationship last = position.lastRelationshipTraversed();
 					if ( !timeOk && last != null && last.getType().equals(  
 						RelTypes.TIMELINE_NEXT_ENTRY ) )
 					{
 						Node node = position.currentNode();
 						long currentTime = ( Long ) node.getProperty( 
 							TIMESTAMP );
 						timeOk = currentTime > timestamp;
 						return false;
 					}
 					if ( timeOk && last.getType().equals( 
 						RelTypes.TIMELINE_INSTANCE ) )
 					{
 						return true;
 					}
 					return false;
 				}
 			}, 
 			RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING, 
 			RelTypes.TIMELINE_INSTANCE, Direction.OUTGOING);
 	}
 	
 	Iterable<Node> getAllTimeNodesAfter( final long timestamp )
 	{
 		Node startNode = getIndexedStartNode( timestamp );
 		return startNode.traverse( Order.DEPTH_FIRST, 
 			new StopEvaluator()
 			{
 				public boolean isStopNode( TraversalPosition position )
 				{
 					if ( position.lastRelationshipTraversed() != null && 
 						position.currentNode().equals( underlyingNode ) )
 					{
 						return true;
 					}
 					return false;
 				}
 			}, 
 			new ReturnableEvaluator()
 			{
 				private boolean timeOk = false;
 			
 				public boolean isReturnableNode( TraversalPosition position )
 				{
 					if ( position.currentNode().equals( underlyingNode ) )
 					{
 						return false;
 					}
 					Relationship last = position.lastRelationshipTraversed();
 					if ( !timeOk && last != null && last.getType().equals(  
 						RelTypes.TIMELINE_NEXT_ENTRY ) )
 					{
 						Node node = position.currentNode();
 						long currentTime = ( Long ) node.getProperty( 
 							TIMESTAMP );
 						timeOk = currentTime > timestamp;
 					}
 					return timeOk;
 				}
 			}, 
 			RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING );
 	}
 	
 	/**
 	 * Returns all nodes before (not including) the specified timestamp 
 	 * ordered by increasing timestamp.
 	 * 
 	 * @param timestamp The timestamp value, nodes with lesser timestamp 
 	 * value will be returned
 	 * @return All nodes in the timeline after specified timestamp
 	 */
 	public Iterable<Node> getAllNodesBefore( final long timestamp )
 	{
 		return underlyingNode.traverse( Order.DEPTH_FIRST, 
 			new StopEvaluator()
 			{
 				public boolean isStopNode( TraversalPosition position )
 				{
 					Relationship last = position.lastRelationshipTraversed();
 					if ( last != null && last.getType().equals( 
 						RelTypes.TIMELINE_NEXT_ENTRY ) )
 					{
 						Node node = position.currentNode();
 						long currentTime = ( Long ) node.getProperty( 
 							TIMESTAMP );
 						return currentTime >= timestamp;
 					}
 					return false;
 				}
 			},
 			new ReturnableEvaluator()
 			{
 				public boolean isReturnableNode( TraversalPosition position )
 				{
 					Relationship last = position.lastRelationshipTraversed();
 					if ( last != null && last.getType().equals( 
 						RelTypes.TIMELINE_INSTANCE ) )
 					{
 						return true;
 					}
 					return false;
 				}
 			}, 
 			RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING,
 			RelTypes.TIMELINE_INSTANCE, Direction.OUTGOING );
 	}
 	
 	/**
 	 * Returns all nodes between (not including) the specified timestamps 
 	 * ordered by increasing timestamp.
 	 * 
 	 * @param startTime The start timestamp, nodes with greater timestamp 
 	 * value will be returned
 	 * @param startTime The end timestamp, nodes with lesser timestamp 
 	 * value will be returned
 	 * @return All nodes in the timeline between the specified timestamps
 	 */
 	public Iterable<Node> getAllNodesBetween( final long startTime, 
 		final long endTime )
 	{
 		if ( startTime >= endTime )
 		{
 			throw new IllegalArgumentException( 
 				"Start time greater or equal to end time" );
 		}
 		Node startNode = getIndexedStartNode( startTime );
 		return startNode.traverse( Order.DEPTH_FIRST, 
 			new StopEvaluator()
 			{
 				public boolean isStopNode( TraversalPosition position )
 				{
 					Relationship last = position.lastRelationshipTraversed();
 					if ( last != null && position.currentNode().equals( 
 						underlyingNode ) )
 					{
 						return true;
 					}
 					if ( last != null && last.getType().equals( 
 						RelTypes.TIMELINE_NEXT_ENTRY ) )
 					{
 						Node node = position.currentNode();
 						long currentTime = ( Long ) node.getProperty( 
 							TIMESTAMP );
 						return currentTime >= endTime;
 					}
 					return false;
 				}
 			},
 			new ReturnableEvaluator()
 			{
 				private boolean timeOk = false;
 				
 				public boolean isReturnableNode( TraversalPosition position )
 				{
 					if ( position.currentNode().equals( underlyingNode ) )
 					{
 						return false;
 					}
 					Relationship last = position.lastRelationshipTraversed();
 					if ( !timeOk && last != null && last.getType().equals(  
 						RelTypes.TIMELINE_NEXT_ENTRY ) )
 					{
 						Node node = position.currentNode();
 						long currentTime = ( Long ) node.getProperty( 
 							TIMESTAMP );
 						timeOk = currentTime > startTime;
 						return false;
 					}
 					if ( timeOk && last.getType().equals( 
 						RelTypes.TIMELINE_INSTANCE ) )
 					{
 						return true;
 					}
 					return false;
 				}
 			}, 
 			RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING,
 			RelTypes.TIMELINE_INSTANCE, Direction.OUTGOING );
 	}
 	
 	/**
 	 * Deletes this timeline. Nodes added to the timeline will not be deleted, 
 	 * they are just disconnected from this timeline.
 	 */
 	public void delete()
 	{
 		if ( useIndexing )
 		{
 			bTree.delete();
 		}
 		Relationship rel = underlyingNode.getSingleRelationship( 
 			RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING );
 		while ( rel != null )
 		{
 			Node node = rel.getEndNode();
 			if ( !node.equals( underlyingNode ) )
 			{
 				for ( Relationship instance : node.getRelationships( 
 					RelTypes.TIMELINE_INSTANCE ) )
 				{
 					instance.delete();
 				}
 				rel.delete();
 				rel = node.getSingleRelationship( RelTypes.TIMELINE_NEXT_ENTRY, 
 					Direction.OUTGOING );
 				node.delete();
 			}
 			else
 			{
 				rel.delete();
 				rel = null;
 			}
 		}
 		underlyingNode.delete();
 	}
 }
