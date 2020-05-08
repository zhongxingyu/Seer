 package org.neo4j.util;
 
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.RelationshipType;
 
 /**
  * The default implementation of {@link Link}.
  * @author mattias
  *
  * @param <T> the type of objects used in this instance.
  */
 public class LinkImpl<T extends NodeWrapper> extends AbstractLink<T>
 {
 	private Class<? extends T> theClass;
 	
 	/**
 	 * @param node the node to act as the link.
 	 * @param type the relationship type to be the link relationship.
 	 * @param thisIsGenericsFault well, even if we have T we must send the
 	 * same class here to make instantiation work.
 	 */
 	public LinkImpl( NeoService neo, Node node, RelationshipType type,
 		Class<? extends T> thisIsGenericsFault )
 	{
 	    super( neo, node, type );
 		this.theClass = thisIsGenericsFault;
 	}
 	
 	/**
 	 * @param node the node to act as the link.
 	 * @param type the relationship type to be the link relationship.
 	 * @param thisIsGenericsFault well, even if we have T we must send the
 	 * same class here to make instantiation work.
 	 * @param direction the direction of the relationship.
 	 */
 	public LinkImpl( NeoService neo, Node node, RelationshipType type,
 		Direction direction, Class<T> thisIsGenericsFault )
 	{
 	    super( neo, node, type, direction );
 	    this.theClass = thisIsGenericsFault;
 	}
 	
 	protected Class<? extends T> classType()
 	{
 		return this.theClass;
 	}
 	
 	@Override
 	protected T newObject( Node node )
 	{
 		return NodeWrapperImpl.newInstance( this.classType(), neo(), node );
 	}
 }
