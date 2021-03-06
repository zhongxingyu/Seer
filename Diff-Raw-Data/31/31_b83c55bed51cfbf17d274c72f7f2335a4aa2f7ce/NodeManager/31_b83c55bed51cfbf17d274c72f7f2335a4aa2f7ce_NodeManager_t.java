 /*
  * Copyright 2002-2007 Network Engine for Objects in Lund AB [neotechnology.com]
  * 
  * This program is free software: you can redistribute it and/or modify
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
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.neo4j.impl.core;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.transaction.TransactionManager;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Traverser;
 import org.neo4j.api.core.Traverser.Order;
 import org.neo4j.impl.cache.AdaptiveCacheManager;
 import org.neo4j.impl.cache.LruCache;
 import org.neo4j.impl.event.Event;
 import org.neo4j.impl.event.EventData;
 import org.neo4j.impl.event.EventManager;
 import org.neo4j.impl.event.ProActiveEventListener;
 import org.neo4j.impl.persistence.IdGenerator;
 import org.neo4j.impl.persistence.PersistenceManager;
 import org.neo4j.impl.transaction.IllegalResourceException;
 import org.neo4j.impl.transaction.LockManager;
 import org.neo4j.impl.transaction.LockNotFoundException;
 import org.neo4j.impl.transaction.LockType;
 import org.neo4j.impl.transaction.NotInTransactionException;
 import org.neo4j.impl.transaction.TxManager;
 import org.neo4j.impl.traversal.TraverserFactory;
 
 public class NodeManager
 {
 	private static Logger log = Logger.getLogger( NodeManager.class.getName() );
 	
 	private int referenceNodeId = 0;
 	
 	private final LruCache<Integer,Node> nodeCache; 
 	private final LruCache<Integer,Relationship> relCache;
 	
 	private final AdaptiveCacheManager cacheManager;
 	private final LockManager lockManager;
 	private final TransactionManager transactionManager;
 	private final LockReleaser lockReleaser;
 	private final PropertyIndexManager propertyIndexManager;
 	private final TraverserFactory traverserFactory;
 	private final EventManager eventManager;
 	private final RelationshipTypeHolder relTypeHolder;
 	private final PurgeEventListener purgeEventListener;
 	private final PersistenceManager persistenceManager;
 	private final IdGenerator idGenerator;
     private final NeoConstraintsListener neoConstraintsListener;
 	
 	NodeManager( AdaptiveCacheManager cacheManager, LockManager lockManager, 
 		TransactionManager transactionManager, LockReleaser lockReleaser, 
 		EventManager eventManager, PersistenceManager persistenceManager, 
 		IdGenerator idGenerator )
 	{
 		this.cacheManager = cacheManager;
 		this.lockManager = lockManager;
 		this.transactionManager = transactionManager;
 		this.lockReleaser = lockReleaser;
 		this.eventManager = eventManager;
 		this.persistenceManager = persistenceManager;
 		this.idGenerator = idGenerator;
 		this.propertyIndexManager = new PropertyIndexManager( 
 			transactionManager, persistenceManager, idGenerator );
 		this.traverserFactory = new TraverserFactory();
 		this.relTypeHolder = new RelationshipTypeHolder( transactionManager, 
 			persistenceManager, idGenerator );
 		nodeCache = new LruCache<Integer, Node>( "NodeCache", 1500, 
 			this.cacheManager );
 		relCache = new LruCache<Integer, Relationship>( "RelationshipCache", 
 			3500, this.cacheManager );
 		this.purgeEventListener = new PurgeEventListener();
         this.neoConstraintsListener = new NeoConstraintsListener( 
             transactionManager );
 	}
 	
 	public void start()
 	{
 		cacheManager.registerCache( nodeCache, 0.77f, 0 );
 		cacheManager.registerCache( relCache, 0.77f, 0 );
 		try
 		{
 			eventManager.registerProActiveEventListener( purgeEventListener, 
 				Event.PURGE_NODE );
 			eventManager.registerProActiveEventListener( purgeEventListener, 
 				Event.PURGE_REL );
 			eventManager.registerProActiveEventListener( purgeEventListener, 
 				Event.PURGE_REL_TYPE );
 			eventManager.registerProActiveEventListener( purgeEventListener, 
 				Event.PURGE_PROP_INDEX );
 		}
 		catch ( Exception e )
 		{
 			throw new RuntimeException( e );
 		}
 	}
 	
 	public void stop()
 	{
 		cacheManager.unregisterCache( nodeCache ); 
 		cacheManager.unregisterCache( relCache );
 		relTypeHolder.clear();
 		try
 		{
 			eventManager.unregisterProActiveEventListener( purgeEventListener, 
 				Event.PURGE_NODE );
 			eventManager.unregisterProActiveEventListener( purgeEventListener, 
 				Event.PURGE_REL );
 			eventManager.unregisterProActiveEventListener( purgeEventListener, 
 				Event.PURGE_REL_TYPE );
 			eventManager.unregisterProActiveEventListener( purgeEventListener, 
 				Event.PURGE_PROP_INDEX );
 		}
 		catch ( Exception e )
 		{
 			throw new RuntimeException( e );
 		}
 	}
 	
 	public Node createNode() // throws CreateException
 	{
 		int id = idGenerator.nextId( Node.class );
 		NodeImpl node = new NodeImpl( id, true, this );
 		acquireLock( node, LockType.WRITE );
         boolean success = false;
 		try
 		{
             persistenceManager.nodeCreate( id );
             nodeCache.add( (int) node.getId(), node );
             success = true;
 			return new NodeProxy( id, this );
 		}
 		finally
 		{
 			releaseLock( node, LockType.WRITE );
             if ( !success )
             {
                 setRollbackOnly();
             }
 		}
 	}
 	
 	public Relationship createRelationship( Node startNode, Node endNode, 
 		RelationshipType type )
 	{
 		if ( startNode == null || endNode == null || type == null )
 		{
 			throw new IllegalArgumentException( "Null parameter, startNode=" + 
 				startNode + ", endNode=" + endNode + ", type=" + type );
 		}
 		if ( !relTypeHolder.isValidRelationshipType( type ) )
 		{
 			relTypeHolder.addValidRelationshipType( type.name(), true );
 		}
 		
         int startNodeId = (int) startNode.getId();
 		NodeImpl firstNode = getLightNode( startNodeId );
 		if ( firstNode == null ||
             neoConstraintsListener.nodeIsDeleted( startNodeId ) )
 		{
 			setRollbackOnly();
 			throw new RuntimeException( "First node[" + startNode.getId() + 
 				"] deleted" );
 		}
         int endNodeId = (int) endNode.getId();
 		NodeImpl secondNode = getLightNode( endNodeId );
 		if ( secondNode == null || 
             neoConstraintsListener.nodeIsDeleted( endNodeId ) )
 		{
 			setRollbackOnly();
 			throw new RuntimeException( "Second node[" + endNode.getId() + 
 				"] deleted" );
 		}
         
         int id = idGenerator.nextId( Relationship.class );
 		RelationshipImpl rel = new RelationshipImpl( id, startNodeId, 
 			endNodeId, type, true, this );
 		boolean firstNodeTaken = false;
 		boolean secondNodeTaken = false;
 		acquireLock( rel, LockType.WRITE );
         boolean success = false;
 		try
 		{
 			acquireLock( firstNode, LockType.WRITE );
 			firstNodeTaken = true;
 			if ( firstNode.isDeleted() )
 			{
 				setRollbackOnly();
 				throw new IllegalStateException( "" + startNode + 
 					" has been deleted in other transaction" );
 			}
 			acquireLock( secondNode, LockType.WRITE );
 			secondNodeTaken = true;
 			if ( secondNode.isDeleted() )
 			{
 				setRollbackOnly();
 				throw new IllegalStateException( "" + endNode + 
 					" has been deleted in other transaction" );
 			}
 			int typeId = getRelationshipTypeIdFor( type ); 
 			persistenceManager.relationshipCreate( id, typeId, startNodeId, 
                 endNodeId );
 			firstNode.addRelationship( type, id );
 			secondNode.addRelationship( type, id );
             relCache.add( (int) rel.getId(), rel );
             success = true;
 			return new RelationshipProxy( id, this );
 		}
 		finally
 		{
 			boolean releaseFailed = false;
 			if ( firstNodeTaken )
 			{
 				try
 				{
 					releaseLock( firstNode, LockType.WRITE );
 				}
 				catch ( Exception e )
 				{
 					releaseFailed = true;
 					e.printStackTrace();
 					log.severe( "Failed to release lock" );
 				}
 			}
 			if ( secondNodeTaken )
 			{
 				try
 				{
 					releaseLock( secondNode, LockType.WRITE );
 				}
 				catch ( Exception e )
 				{
 					releaseFailed = true;
 					e.printStackTrace();
 					log.severe( "Failed to release lock" );
 				}
 			}
 			releaseLock( rel, LockType.WRITE );
             if ( !success )
             {
                 setRollbackOnly();
             }
 			if ( releaseFailed )
 			{
 				throw new RuntimeException( "Unable to release locks [" + 
 					startNode + "," + endNode + "] in relationship create->" + 
 					rel );
 			}
 		}
 	}
 	
 	/**
 	 * Gets a node by id.
 	 * <p>
 	 * First checks the cache, if the node is in cache it is returned.
 	 * If the node isn't in cache it is loaded from persistent storage.
 	 * 
 	 * @param id the node id
 	 * @return the node with node id <CODE>id</CODE>
 	 * @throws NotFoundException
 	 */
 	public Node getNodeById( int nodeId ) throws NotFoundException
 	{
 		if ( nodeId < 0 )
 		{
 			throw new IllegalArgumentException( "Negative node id " + nodeId );
 		}
 		Node node = nodeCache.get( nodeId );
 		if ( node != null )
 		{
 			return new NodeProxy( nodeId, this );
 		}
 		node = new NodeImpl( nodeId, this );
 		acquireLock( node, LockType.WRITE );
 		try
 		{
     		if ( nodeCache.get( nodeId ) != null )
     		{
     			node = nodeCache.get( nodeId );
     			return new NodeProxy( nodeId, this );
     		}
     		if ( persistenceManager.loadLightNode( nodeId ) == null )
     		{
     			throw new NotFoundException( "Node[" + nodeId + "]" );
     		}
     			
     		// If we get here, loadLightNode didn't throw exception and
     		// a node with the given id does exist... good.
     		nodeCache.add( nodeId, node );
     		return new NodeProxy( nodeId, this );
 		}
 		finally
 		{
 			forceReleaseWriteLock( node );
 		}
 	}
 	
 	NodeImpl getLightNode( int nodeId )
 	{
 		Node node = nodeCache.get( nodeId );
 		if ( node != null )
 		{
 			return (NodeImpl) node;
 		}
 		node = new NodeImpl( nodeId, this );
 		acquireLock( node, LockType.WRITE );
 		try
 		{
 			if ( nodeCache.get( nodeId ) != null )
 			{
 				node = nodeCache.get( nodeId );
 				return (NodeImpl) node;
 			}
 			if ( persistenceManager.loadLightNode( nodeId ) == null )
 			{
                 return neoConstraintsListener.getDeletedNode( nodeId );
 			}
 			nodeCache.add( nodeId, node );
 			return (NodeImpl) node;
 		}
 		finally
 		{
 			forceReleaseWriteLock( node );
 		}
 	}
 	
 	NodeImpl getNodeForProxy( int nodeId )
 	{
 		Node node = nodeCache.get( nodeId );
 		if ( node != null )
 		{
 			return (NodeImpl) node;
 		}
 		node = new NodeImpl( nodeId, this );
 		acquireLock( node, LockType.WRITE );
 		try
 		{
 			if ( nodeCache.get( nodeId ) != null )
 			{
 				node = nodeCache.get( nodeId );
 				return (NodeImpl) node;
 			}
 			if ( persistenceManager.loadLightNode( nodeId ) == null )
 			{
 				throw new NotFoundException( "Node[" + nodeId + 
                     "] not found." );
 			}
 			nodeCache.add( nodeId, node );
 			return (NodeImpl) node;
 		}
 		finally
 		{
 			forceReleaseWriteLock( node );
 		}
 	}
 	
 	/**
 	 * Returns the reference node in this node space. If the node isn't found 
 	 * (reference node hasn't been configured properly) a 
 	 * {@link NotFoundException} is thrown. See {@link NeoModule} for more 
 	 * information
 	 *
 	 * @return the reference node
 	 * @throws NotFoundException
 	 */
 	public Node getReferenceNode() throws NotFoundException
 	{
 		if ( referenceNodeId == -1 )
 		{
 			throw new NotFoundException( "No reference node set" );
 		}
 		return getNodeById( referenceNodeId );
 	}
 	
 	void setReferenceNodeId( int nodeId )
 	{
 		this.referenceNodeId = nodeId;
 	}
 	
 	// checks the cache first, if not in cache load it using PM
 	Relationship getRelationshipById( int relId ) 
 		throws NotFoundException
 	{
 		Relationship relationship = relCache.get( relId );
 		if ( relationship != null )
 		{
 			return new RelationshipProxy( relId, this );
 		}
 		relationship = new RelationshipImpl( relId, this );
 		acquireLock( relationship, LockType.WRITE );
 		try
 		{
 			if ( relCache.get( relId ) != null )
 			{
 				relationship = relCache.get( relId );
 				return new RelationshipProxy( relId, this );
 			}
 
 			RawRelationshipData data = 
 				persistenceManager.loadLightRelationship( relId );
 			if ( data == null )
 			{
 				throw new NotFoundException( "Relationship[" + relId + 
 					"] not found" );
 			}
 			RelationshipType type = getRelationshipTypeById( data.getType() );
 			if ( type == null )
 			{
 				throw new RuntimeException( "Relationship[" + data.getId() +
 					"] exist but relationship type[" + data.getType() +
 					"] not found." );
 			}
 			relationship = new RelationshipImpl( relId, data.getFirstNode(), 
 				data.getSecondNode(), type, false, this );
 			relCache.add( relId, relationship );
 			return new RelationshipProxy( relId, this );
 		}
 		finally
 		{
 			forceReleaseWriteLock( relationship );
 		}
 	}
 	
 	RelationshipType getRelationshipTypeById( int id )
 	{
 		return relTypeHolder.getRelationshipType( id );
 	}
 	
 	Relationship getRelForProxy( int relId )
 	{
 		Relationship relationship = relCache.get( relId );
 		if ( relationship != null )
 		{
 			return relationship;
 		}
 		relationship = new RelationshipImpl( relId, this );
 		acquireLock( relationship, LockType.WRITE );
 		try
 		{
 			if ( relCache.get( relId ) != null )
 			{
 				relationship = relCache.get( relId );
 				return relationship;
 			}
 			RawRelationshipData data = 
 				persistenceManager.loadLightRelationship( relId );
 			if ( data == null )
 			{
 				throw new NotFoundException( "Relationship[" + relId + 
 					"] not found, has been deleted?" );
 			}
 			RelationshipType type = getRelationshipTypeById( data.getType() );
 			if ( type == null )
 			{
 				throw new RuntimeException( "Relationship[" + data.getId() +
 					"] exist but relationship type[" + data.getType() +
 					"] not found." );
 			}
 			relationship = new RelationshipImpl( relId, data.getFirstNode(), 
 				data.getSecondNode(), type, false, this );
 			relCache.add( relId, relationship );
 			return relationship;
 		}
 		finally
 		{
 			forceReleaseWriteLock( relationship );
 		}
 	}
 	
 	public void removeNodeFromCache( int nodeId )
 	{
 		nodeCache.remove( nodeId );
 	}
 	
 	public void removeRelationshipFromCache( int id )
 	{
 		relCache.remove( id );
 	}
 
 	Object loadPropertyValue( int id )
 	{
 		return persistenceManager.loadPropertyValue( id );
 	}
 	
 	List<Relationship> loadRelationships( NodeImpl node )
 	{
 		try
 		{
 			RawRelationshipData rawRels[] = 
 				persistenceManager.loadRelationships( (int) node.getId() );
 			List<Relationship> relList = new ArrayList<Relationship>();
 			for ( RawRelationshipData rawRel : rawRels )
 			{
 				int relId = rawRel.getId();
 				Relationship rel = relCache.get( relId );
 				if ( rel == null )
 				{
 					RelationshipType type = getRelationshipTypeById( 
                         rawRel.getType() ); 
 					if ( type == null )
 					{
 						// relationship with type that hasn't been registered
 						continue;
 					}
 					rel = new RelationshipImpl( relId, 
 						rawRel.getFirstNode(), rawRel.getSecondNode(), 
 						type, false, this );
 					relCache.add( relId, rel );
 				}
 				relList.add( rel );
 			}
 			return relList;
 		}
 		catch ( Exception e )
 		{
 			log.severe( "Failed loading relationships for node[" +
 				node.getId() + "]" );
 			throw new RuntimeException( e );
 		}
 	}
 	
 	RawPropertyData[] loadProperties( NodeImpl node )
 	{
 		try
 		{
 			RawPropertyData properties[]  = 
 				persistenceManager.loadNodeProperties( (int) node.getId() );
 			return properties;
 		}
 		catch ( Exception e )
 		{
 			log.severe( "Failed loading properties for node[" +
 				node.getId() + "]" );
 			throw new RuntimeException( e );
 		}
 	}
 	
 	RawPropertyData[] loadProperties( RelationshipImpl relationship )
 	{
 		try
 		{
 			RawPropertyData properties[]  = 
 				persistenceManager.loadRelProperties( 
                     (int) relationship.getId() );
 			return properties;
 		}
 		catch ( Exception e )
 		{
 			log.severe( "Failed loading properties for relationship[" +
 				relationship.getId() + "]" );
 			throw new RuntimeException( e );
 		}
 	}
 	
 	int getNodeMaxCacheSize()
 	{
 		return nodeCache.maxSize();
 	}
 	
 	int getRelationshipMaxCacheSize()
 	{
 		return relCache.maxSize();
 	}
 	
 	public void clearCache()
 	{
 		nodeCache.clear();
 		relCache.clear();
 	}
 	
 	void setRollbackOnly()
 	{
 		try
 		{
 			transactionManager.setRollbackOnly();
 		}
 		catch ( javax.transaction.SystemException se )
 		{
 			se.printStackTrace();
 			log.severe( "Failed to set transaction rollback only" );
 		}
 	}
 
 	void acquireLock( Object resource, LockType lockType )
 	{
 		try
 		{
 			if ( lockType == LockType.READ )
 			{
 				lockManager.getReadLock( resource );
 			}
 			else if ( lockType == LockType.WRITE )
 			{
 				lockManager.getWriteLock( resource );
 			}
 			else
 			{
 				throw new RuntimeException( "Unkown lock type: " + lockType );
 			}
 		}
 		catch ( IllegalResourceException e )
 		{
 			throw new RuntimeException( e );
 		}
 	}
 	
 	void releaseLock( Object resource, LockType lockType )
 	{
 		try
 		{
 			if ( lockType == LockType.READ )
 			{
 				lockManager.releaseReadLock( resource );
 			}
 			else if ( lockType == LockType.WRITE )
 			{
                 lockReleaser.addLockToTransaction( resource, lockType );
             }
 			else
 			{
 				throw new RuntimeException( "Unkown lock type: " + 
 					lockType );
 			}
 		}
 		catch ( NotInTransactionException e )
 		{
 			throw new RuntimeException( 
 				"Unable to get transaction isolation level.", e );
 		}
 		catch ( LockNotFoundException e )
 		{
 			throw new RuntimeException( 
 				"Unable to release locks.", e );
 		}
 		catch ( IllegalResourceException e )
 		{
 			throw new RuntimeException( 
 				"Unable to release locks.", e );
 		}
 	}
 	
 //	private void forceReleaseReadLock( Object resource )
 //	{
 //		try
 //		{
 //			lockManager.releaseReadLock( resource );
 //		}
 //		catch ( LockNotFoundException e )
 //		{
 //			throw new RuntimeException( 
 //				"Unable to release lock.", e );
 //		}
 //		catch ( IllegalResourceException e )
 //		{
 //			throw new RuntimeException( 
 //				"Unable to release lock.", e );
 //		}
 //	}
 	
     // used when loading nodes/rels to cache
     private void forceReleaseWriteLock( Object resource )
     {
         try
         {
             lockManager.releaseWriteLock( resource );
         }
         catch ( LockNotFoundException e )
         {
             throw new RuntimeException( 
                 "Unable to release lock.", e );
         }
         catch ( IllegalResourceException e )
         {
             throw new RuntimeException( 
                 "Unable to release lock.", e );
         }
     }
     
 	public boolean isValidRelationshipType( RelationshipType type )
 	{
 		return relTypeHolder.isValidRelationshipType( type );
 	}
 
 	public void addEnumRelationshipTypes( 
 		Class<? extends RelationshipType> relationshipTypes )
     {
 		relTypeHolder.addValidRelationshipTypes( relationshipTypes );
     }
 
 	public RelationshipType registerRelationshipType( String name, 
 		boolean create )
     {
 		return relTypeHolder.addValidRelationshipType( name, create );
     }
 	
 	public int getHighestPossibleIdInUse( Class<?> clazz )
 	{
 		return idGenerator.getHighestPossibleIdInUse( clazz );
 	}
 	
 	public int getNumberOfIdsInUse( Class<?> clazz )
 	{
 		return idGenerator.getNumberOfIdsInUse( clazz );
 	}
 
 	public void removeRelationshipTypeFromCache( int id )
     {
 		relTypeHolder.removeRelType( id );
     }
 
	Traverser createTraverser( Order traversalOrder, NodeImpl node,
	    RelationshipType relationshipType, Direction direction,
	    StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator )
	{
		return traverserFactory.createTraverser( traversalOrder, new NodeProxy(
		    ( int ) node.getId(), this ), relationshipType, direction,
		    stopEvaluator, returnableEvaluator );
	}
 
	Traverser createTraverser( Order traversalOrder, NodeImpl node,
	    RelationshipType[] types, Direction[] dirs,
	    StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator )
	{
		return traverserFactory.createTraverser( traversalOrder, new NodeProxy(
		    ( int ) node.getId(), this ), types, dirs, stopEvaluator,
		    returnableEvaluator );
	}
 
 	void addPropertyIndexes( RawPropertyIndex[] propertyIndexes )
     {
 		propertyIndexManager.addPropertyIndexes( propertyIndexes );
     }
 
 	void setHasAllpropertyIndexes( boolean hasAll )
     {
 		propertyIndexManager.setHasAll( hasAll );
     }
 
 	void clearPropertyIndexes()
     {
 		propertyIndexManager.clear();
     }
 
 	PropertyIndex getIndexFor( int keyId )
     {
 	    return propertyIndexManager.getIndexFor( keyId );
     }
 
 	Iterable<PropertyIndex> index( String key )
     {
 		return propertyIndexManager.index( key );
     }
 
 	boolean hasAllPropertyIndexes()
     {
 	    return propertyIndexManager.hasAll();
     }
 
 	boolean hasIndexFor( int keyId )
     {
 		return propertyIndexManager.hasIndexFor( keyId );
     }
 
 	PropertyIndex createPropertyIndex( String key )
     {
 		return propertyIndexManager.createPropertyIndex( key );
     }
 
 	int getRelationshipTypeIdFor( RelationshipType type )
     {
 	    return relTypeHolder.getIdFor( type ); 
     }
 
 	void addRawRelationshipTypes( RawRelationshipTypeData[] relTypes )
     {
 		relTypeHolder.addRawRelationshipTypes( relTypes );
     }
 
 	void addValidRelationshipTypes( Class<? extends RelationshipType> relTypeClass )
     {
 		relTypeHolder.addValidRelationshipTypes( relTypeClass );
     }
 
 	RelationshipType getRelationshipTypeByName( String name )
     {
 		return relTypeHolder.getRelationshipTypeByName( name );
     }
 
 	Iterable<RelationshipType> getRelationshipTypes()
     {
 		return relTypeHolder.getRelationshipTypes();
     }
 
 	boolean hasRelationshipType( String name )
     {
 		return relTypeHolder.hasRelationshipType( name );
     }
 
 	RelationshipType addValidRelationshipType( String name, boolean create )
     {
 		return relTypeHolder.addValidRelationshipType( name, create );
     }
 
 	private class PurgeEventListener implements ProActiveEventListener
 	{
 		public boolean proActiveEventReceived( Event event, EventData data )
         {
 			if ( Event.PURGE_NODE == event )
 			{
 				removeNodeFromCache( (Integer) data.getData() );
 				return true;
 			}
 			if ( Event.PURGE_REL == event )
 			{
 				removeRelationshipFromCache( (Integer) data.getData() );
 				return true;
 			}
 			if ( Event.PURGE_REL_TYPE == event )
 			{
 				removeRelationshipTypeFromCache( (Integer) data.getData() );
 				return true;
 			}
 	        // TODO Auto-generated method stub
 	        return false;
         }
 	}
 
     void deleteNode( NodeImpl node )
     {
         neoConstraintsListener.deleteNode( node );
         int nodeId = (int) node.getId();
         persistenceManager.nodeDelete( nodeId );
 //        nodeCache.remove( nodeId );
     }
 
     int nodeAddProperty( NodeImpl node, PropertyIndex index, Object value )
     {
         int nodeId = (int) node.getId();
         neoConstraintsListener.nodePropertyOperation( nodeId );
         return persistenceManager.nodeAddProperty( nodeId, index, 
             value );
     }
 
     void nodeChangeProperty( NodeImpl node, int propertyId, Object value )
     {
         int nodeId = (int) node.getId();
         neoConstraintsListener.nodePropertyOperation( nodeId );
         persistenceManager.nodeChangeProperty( nodeId, propertyId, value );
     }
 
     void nodeRemoveProperty( NodeImpl node, int propertyId )
     {
         int nodeId = (int) node.getId();
         neoConstraintsListener.nodePropertyOperation( nodeId );
         if ( neoConstraintsListener.nodeIsDeleted( nodeId ) )
         {
             // property will be deleted
             return;
         }
         persistenceManager.nodeRemoveProperty( nodeId, propertyId );
     }
 
     public void deleteRelationship( RelationshipImpl rel )
     {
         neoConstraintsListener.deleteRelationship( rel );
         int relId = (int) rel.getId();
         persistenceManager.relDelete( relId );
 //        relCache.remove( relId );
     }
 
     int relAddProperty( RelationshipImpl rel, PropertyIndex index, 
         Object value )
     {
         int relId = (int) rel.getId();
         neoConstraintsListener.relPropertyOperation( relId );
         return persistenceManager.relAddProperty( relId, index, value );
     }
 
     void relChangeProperty( RelationshipImpl rel, int propertyId, Object value )
     {
         int relId = (int) rel.getId();
         neoConstraintsListener.relPropertyOperation( relId );
         persistenceManager.relChangeProperty( relId, propertyId, value );
     }
 
     void relRemoveProperty( RelationshipImpl rel, int propertyId )
     {
         int relId = (int) rel.getId();
         neoConstraintsListener.relPropertyOperation( relId );
         if ( neoConstraintsListener.relIsDeleted( relId ) )
         {
             // property will be deleted
             return;
         }
         persistenceManager.relRemoveProperty( relId, propertyId );
     }
 
     public int getTransactionId()
     {
         return ((TxManager) transactionManager).getEventIdentifier();
     }
 
     public void addCowToTxHook( NeoPrimitive primitive )
     {
         lockReleaser.addCowToTransaction( primitive );
     }
 }
