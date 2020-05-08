 package org.neo4j.util;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 
 /**
  * Deletes an entire graph, i.e. all connected nodes (and their connected nodes)
  * from a start node (any start node in that graph). It is mostly used in
  * some test cases.
  */
 public class EntireGraphDeletor implements NeoDeletor
 {
 	public void delete( Node startNode )
 	{
 		removeNodeAndThoseConnectedWith( new HashSet<Node>(),
 			new HashSet<Relationship>(), startNode );
 	}
 
 	private void removeNodeAndThoseConnectedWith( Collection<Node> deletedNodes,
 		Collection<Relationship> deletedRels, Node node )
 	{
 		for ( Relationship rel : node.getRelationships() )
 		{
 			Node sub = rel.getOtherNode( node );
 			if ( deletedRels.add( rel ) )
 			{
 				aboutToDeleteRelationship( rel );
 				rel.delete();
 			}
			if ( deletedNodes.add( sub ) )
			{
				removeNodeAndThoseConnectedWith( deletedNodes, deletedRels,
					sub );
			}
 		}
		aboutToDeleteNode( node );
		node.delete();
 	}
 	
 	protected void aboutToDeleteRelationship( Relationship relationship )
 	{
 	}
 	
 	protected void aboutToDeleteNode( Node node )
 	{
 	}
 }
