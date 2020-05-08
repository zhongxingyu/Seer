 package org.neo4j.util;
 
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 
 public class RelationshipToNodeIterable
 	extends IterableWrapper<Node, Relationship>
 {
 	private Node startNode;
 	
 	public RelationshipToNodeIterable( Node startNode,
 		Iterable<Relationship> relationships )
 	{
 		super( relationships );
 		this.startNode = startNode;
 	}
 
 	@Override
 	protected Node underlyingObjectToObject( Relationship relationship )
 	{
 		return relationship.getOtherNode( startNode );
 	}
 }
