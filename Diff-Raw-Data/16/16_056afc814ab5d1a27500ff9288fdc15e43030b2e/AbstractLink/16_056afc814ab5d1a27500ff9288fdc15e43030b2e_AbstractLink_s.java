 package org.neo4j.util;
 
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.EmbeddedNeo;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.impl.transaction.IllegalResourceException;
 import org.neo4j.impl.transaction.LockManager;
 
 public abstract class AbstractLink<T> implements Link<T>
 {
     private final NeoService neo;
     private final Node node;
     private final RelationshipType type;
     private final Direction direction;
     
     public AbstractLink( NeoService neo, Node node, RelationshipType type,
         Direction direction )
     {
         this.neo = neo;
         this.node = node;
         this.type = type;
         this.direction = direction;
     }
     
     public AbstractLink( NeoService neo, Node node, RelationshipType type )
     {
         this( neo, node, type, Direction.OUTGOING );
     }
     
     protected NeoService neo()
     {
         return this.neo;
     }
     
     protected Node node()
     {
         return this.node;
     }
     
     protected Direction direction()
     {
         return this.direction;
     }
     
     protected RelationshipType type()
     {
         return this.type;
     }
     
     protected abstract T newObject( Node node );
     
     public T get()
     {
         Transaction tx = neo.beginTx();
         try
         {
             Relationship rel = this.node().getSingleRelationship( this.type(),
                 this.direction() );
             if ( rel == null )
             {
                 throw new RuntimeException( "No link relationship found for " +
                     this.node() + ":" + this.direction() + ":" + this.type() );
             }
             Node otherNode = rel.getOtherNode( this.node() );
             T result = this.newObject( otherNode );
             tx.success();
             return result;
         }
         finally
         {
             tx.finish();
         }
     }
 
     public boolean has()
     {
         Transaction tx = neo.beginTx();
         try
         {
             boolean result = this.node().getRelationships(
                 this.type(), this.direction() ).iterator().hasNext();
             tx.success();
             return result;
         }
         finally
         {
             tx.finish();
         }
     }
 
     public void remove()
     {
         Transaction tx = neo.beginTx();
         try
         {
             this.node().getSingleRelationship(
                 this.type(), this.direction() ).delete();
             tx.success();
         }
         finally
         {
             tx.finish();
         }
     }
 
     public void set( T entity )
     {
         Transaction tx = neo.beginTx();
         LockManager lockManager =
             ( ( EmbeddedNeo ) neo ).getConfig().getLockManager();
         try
         {
             lockManager.getWriteLock( this.node() );
             if ( has() )
             {
                 remove();
             }
             
            Node entityNode = ( ( NodeWrapper ) entity ).getUnderlyingNode();
             Node startNode = this.direction() == Direction.OUTGOING ?
                 this.node() : entityNode;
             Node endNode = this.direction() == Direction.OUTGOING ?
                 entityNode : this.node();
             Relationship rel =
                 startNode.createRelationshipTo( endNode, this.type() );
             entitySet( entity, rel );
             tx.success();
         }
         catch ( IllegalResourceException e )
         {
             throw new RuntimeException( e );
         }
         finally
         {
             try
             {
                 lockManager.releaseWriteLock( this.node() );
             }
             catch ( Exception e )
             {
                 throw new RuntimeException( e );
             }
             tx.finish();
         }
     }
     
     protected void entitySet( T entity, Relationship createdRelationship )
     {
     }
 }
