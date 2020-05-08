 package org.neo4j.rdf.store.representation.standard;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.neometa.structure.MetaStructure;
 import org.neo4j.rdf.store.representation.AbstractNode;
 import org.neo4j.rdf.store.representation.AbstractRelationship;
 import org.neo4j.rdf.store.representation.AbstractRepresentation;
 import org.neo4j.util.index.IndexService;
 
 public class VerboseQuadExecutor extends UriBasedExecutor
 {
     public static final String LITERAL_DATATYPE_KEY =
         START_OF_ILLEGAL_URI + "datatype";
     public static final String LITERAL_LANGUAGE_KEY =
         START_OF_ILLEGAL_URI + "language";
     
     private static final Collection<String> EXCLUDED_LITERAL_KEYS =
     	new HashSet<String>();
     static
     {
     	EXCLUDED_LITERAL_KEYS.add( LITERAL_DATATYPE_KEY );
     	EXCLUDED_LITERAL_KEYS.add( LITERAL_LANGUAGE_KEY );
     }
     
     public static enum RelTypes implements RelationshipType
     {
     	REF_CONTEXTS,
     	IS_A_CONTEXT,
     }
    
    private Node contextRefNodeCache;
 
     public VerboseQuadExecutor( NeoService neo, IndexService index,
         MetaStructure meta )
     {
         super( neo, index, meta );
     }
     
     public Node getContextsReferenceNode()
     {
    	if ( this.contextRefNodeCache == null )
    	{
    		this.contextRefNodeCache = this.neoUtil().
    			getOrCreateSubReferenceNode( RelTypes.REF_CONTEXTS );
    	}
    	return this.contextRefNodeCache;
     }
 
     @Override
     public void addToNodeSpace( AbstractRepresentation representation )
     {
     	Map<String, AbstractNode> typeToNode =
     		getTypeToNodeMap( representation );
         if ( isLiteralRepresentation( typeToNode ) )
         {
             handleAddLiteralRepresentation( representation, typeToNode );
         }
         else if ( isObjectTypeRepresentation( typeToNode ) )
         {
             handleAddObjectRepresentation( representation, typeToNode );
         }
         else
         {
             super.addToNodeSpace( representation );
         }
     }
 
     private void handleAddLiteralRepresentation(
         AbstractRepresentation representation,
         Map<String, AbstractNode> typeToNode )
     {
         AbstractNode abstractSubjectNode = typeToNode.get(
             VerboseQuadStrategy.TYPE_SUBJECT );
         AbstractNode abstractMiddleNode = typeToNode.get(
             VerboseQuadStrategy.TYPE_MIDDLE );
         NodeContext subjectNode =
         	lookupOrCreateNode( abstractSubjectNode, null );
         AbstractRelationship subjectToMiddle = findAbstractRelationship(
             representation, VerboseQuadStrategy.TYPE_SUBJECT,
             VerboseQuadStrategy.TYPE_MIDDLE );
         AbstractRelationship middleToLiteral = findAbstractRelationship(
             representation, VerboseQuadStrategy.TYPE_MIDDLE,
             VerboseQuadStrategy.TYPE_LITERAL );
         AbstractNode abstractLiteralNode = typeToNode.get(
             VerboseQuadStrategy.TYPE_LITERAL );
 
         Node middleNode = null;
         Node literalNode = null;
         if ( !subjectNode.wasCreated() )
         {
 	        Node[] nodes = findMiddleAndObjectNode( subjectNode.getNode(),
 	        	subjectToMiddle, middleToLiteral, abstractLiteralNode, null );
 	        middleNode = nodes[ 0 ];
 	        literalNode = nodes[ 1 ];
         }
 
         if ( literalNode == null )
         {
             middleNode = createNode( abstractMiddleNode, null );
             createRelationship( subjectNode.getNode(),
             	subjectToMiddle, middleNode );
             literalNode = createLiteralNode( abstractLiteralNode );
             createRelationship( middleNode, middleToLiteral, literalNode );
         }
         ensureContextsAreAdded( representation, middleNode );
     }
     
     private Map<String, AbstractNode> getTypeToNodeMap(
     	AbstractRepresentation representation )
 	{
     	Map<String, AbstractNode> map = new HashMap<String, AbstractNode>();
         for ( AbstractNode node : representation.nodes() )
         {
         	String nodeType = getNodeType( node );
             if ( nodeType != null )
             {
             	map.put( nodeType, node );
             }
         }
         return map;
 	}
 
     private Relationship findContextRelationship(
         AbstractRelationship abstractRelationship, Node middleNode,
         boolean allowCreate )
     {
         AbstractNode abstractContextNode = abstractRelationship.getEndNode();
         Node contextNode = lookupNode( abstractContextNode );
         if ( contextNode == null && !allowCreate )
         {
             return null;
         }
         
         boolean willCreate = contextNode == null;
         contextNode = contextNode != null ? contextNode :
         	createNode( abstractContextNode, null );
         Relationship relationship = null;
         if ( willCreate )
         {
            Node contextRefNode = getContextsReferenceNode();
 	        contextRefNode.createRelationshipTo( contextNode,
 	        	RelTypes.IS_A_CONTEXT );
 	        relationship = createRelationship( middleNode,
 	        	abstractRelationship, contextNode );
         }
         else
         {
             relationship = ensureDirectlyConnected( middleNode,
             	abstractRelationship, contextNode );
            if ( !contextNode.hasRelationship( RelTypes.IS_A_CONTEXT,
            	Direction.INCOMING ) )
            {
                Node contextRefNode = getContextsReferenceNode();
            	contextRefNode.createRelationshipTo( contextNode,
            		RelTypes.IS_A_CONTEXT );
            }
         }
         return relationship;
     }
 
     private void handleAddObjectRepresentation(
         AbstractRepresentation representation,
         Map<String, AbstractNode> typeToNode )
     {
         AbstractNode abstractSubjectNode = typeToNode.get(
             VerboseQuadStrategy.TYPE_SUBJECT );
         AbstractNode abstractMiddleNode = typeToNode.get(
             VerboseQuadStrategy.TYPE_MIDDLE );
         AbstractNode abstractObjectNode = typeToNode.get(
             VerboseQuadStrategy.TYPE_OBJECT );
         NodeContext subjectNode =
         	lookupOrCreateNode( abstractSubjectNode, null );
         NodeContext objectNode = lookupOrCreateNode( abstractObjectNode, null );
         AbstractRelationship subjectToMiddle = findAbstractRelationship(
             representation, VerboseQuadStrategy.TYPE_SUBJECT,
             VerboseQuadStrategy.TYPE_MIDDLE );
         AbstractRelationship middleToObject = findAbstractRelationship(
             representation, VerboseQuadStrategy.TYPE_MIDDLE,
             VerboseQuadStrategy.TYPE_OBJECT );
         
         Node middleNode = null;
         if ( !subjectNode.wasCreated() && !objectNode.wasCreated() )
         {
         	middleNode = findMiddleAndObjectNode( subjectNode.getNode(),
         		subjectToMiddle, middleToObject, abstractObjectNode,
         		objectNode.getNode() )[ 0 ];
         }
 
         if ( middleNode == null )
         {
             middleNode = createNode( abstractMiddleNode, null );
             createRelationship( subjectNode.getNode(), subjectToMiddle,
             	middleNode );
             createRelationship( middleNode, middleToObject,
             	objectNode.getNode() );
         }
         ensureContextsAreAdded( representation, middleNode );
     }
 
     private Node[] findMiddleAndObjectNode( Node subjectNode,
         AbstractRelationship subjectToMiddle,
         AbstractRelationship middleToObject,
         AbstractNode abstractObjectNode, Node objectNodeIfResource )
     {
         Node middleNode = null;
         Node objectNodeToLookFor = null;
         if ( abstractObjectNode.getUriOrNull() != null )
         {
             objectNodeToLookFor = objectNodeIfResource;
         }
 
         Node objectNode = null;
         for ( Relationship relationship : subjectNode.getRelationships(
             relationshipType( subjectToMiddle.getRelationshipTypeName() ),
             Direction.OUTGOING ) )
         {
             Node aMiddleNode = relationship.getEndNode();
             for ( Relationship rel : aMiddleNode.getRelationships(
                 relationshipType( middleToObject.getRelationshipTypeName() ),
                 Direction.OUTGOING ) )
             {
                 Node anObjectNode = rel.getEndNode();
                 if ( ( objectNodeToLookFor != null &&
                     anObjectNode.equals( objectNodeToLookFor ) ) ||
                     ( objectNodeToLookFor == null && containsProperties(
                         anObjectNode, abstractObjectNode.properties(),
                         EXCLUDED_LITERAL_KEYS ) ) )
                 {
                     middleNode = aMiddleNode;
                     objectNode = anObjectNode;
                     break;
                 }
             }
         }
         return new Node[] { middleNode, objectNode };
     }
 
     private void ensureContextsAreAdded(
         AbstractRepresentation representation, Node middleNode )
     {
         for ( AbstractRelationship abstractRelationship :
             getContextRelationships( representation ) )
         {
             findContextRelationship( abstractRelationship,
                 middleNode, true );
         }
     }
 
     private Collection<AbstractRelationship> getContextRelationships(
         AbstractRepresentation representation )
     {
         Collection<AbstractRelationship> list =
             new ArrayList<AbstractRelationship>();
         for ( AbstractRelationship abstractRelationship :
             representation.relationships() )
         {
             if ( relationshipIsType( abstractRelationship,
                 VerboseQuadStrategy.TYPE_MIDDLE,
                 VerboseQuadStrategy.TYPE_CONTEXT ) )
             {
                 list.add( abstractRelationship );
             }
         }
         return list;
     }
 
     private boolean isLiteralRepresentation(
         Map<String, AbstractNode> typeToNode )
     {
         return typeToNode.get( VerboseQuadStrategy.TYPE_LITERAL ) != null;
     }
 
     private boolean isObjectTypeRepresentation(
         Map<String, AbstractNode> typeToNode )
     {
         return typeToNode.get( VerboseQuadStrategy.TYPE_OBJECT ) != null;
     }
 
     private String getNodeType( AbstractNode node )
     {
     	return ( String ) node.getExecutorInfo(
             VerboseQuadStrategy.EXECUTOR_INFO_NODE_TYPE );
     }
 
     private boolean nodeIsType( AbstractNode node, String type )
     {
         String value = getNodeType( node );
         return value != null && value.equals( type );
     }
 
     private boolean relationshipIsType( AbstractRelationship relationship,
         String startType, String endType )
     {
         return nodeIsType( relationship.getStartNode(), startType ) &&
             nodeIsType( relationship.getEndNode(), endType );
     }
 
     private AbstractRelationship findAbstractRelationship(
         AbstractRepresentation representation, String startingType,
         String endingType )
     {
         for ( AbstractRelationship relationship :
             representation.relationships() )
         {
             if ( relationshipIsType( relationship, startingType, endingType ) )
             {
                 return relationship;
             }
         }
         return null;
     }
 
     @Override
     public void removeFromNodeSpace( AbstractRepresentation representation )
     {
         Map<String, AbstractNode> typeToNode =
         	getTypeToNodeMap( representation );
         if ( isLiteralRepresentation( typeToNode ) )
         {
             handleRemoveLiteralRepresentation( representation, typeToNode );
         }
         else if ( isObjectTypeRepresentation( typeToNode ) )
         {
             handleRemoveObjectRepresentation( representation, typeToNode );
         }
         else
         {
             super.addToNodeSpace( representation );
         }
     }
 
     private void handleRemoveObjectRepresentation(
         AbstractRepresentation representation,
         Map<String, AbstractNode> typeToNode )
     {
         AbstractNode abstractSubjectNode = typeToNode.get(
             VerboseQuadStrategy.TYPE_SUBJECT );
         Node subjectNode = lookupNode( abstractSubjectNode );
         AbstractNode abstractObjectNode = typeToNode.get(
             VerboseQuadStrategy.TYPE_OBJECT );
         Node objectNode = lookupNode( abstractObjectNode );
         if ( subjectNode == null || objectNode == null )
         {
             return;
         }
 
         AbstractRelationship subjectToMiddle = findAbstractRelationship(
             representation, VerboseQuadStrategy.TYPE_SUBJECT,
             VerboseQuadStrategy.TYPE_MIDDLE );
         AbstractRelationship middleToObject = findAbstractRelationship(
             representation, VerboseQuadStrategy.TYPE_MIDDLE,
             VerboseQuadStrategy.TYPE_OBJECT );
 
         Node middleNode = findMiddleAndObjectNode( subjectNode, subjectToMiddle,
             middleToObject, abstractObjectNode, objectNode )[ 0 ];
         if ( middleNode == null )
         {
             return;
         }
 
         removeContextRelationships( representation, middleNode );
         if ( !middleNodeHasContexts( middleNode ) )
         {
             disconnectMiddle( middleNode, middleToObject, objectNode,
                 subjectNode, subjectToMiddle );
         }
 
         deleteNodeIfEmpty( abstractSubjectNode, subjectNode );
         // Special case where the subject and object are the same.
         if ( !subjectNode.equals( objectNode ) &&
             nodeIsEmpty( abstractObjectNode, objectNode, true ) )
         {
             deleteNode( objectNode, abstractObjectNode.getUriOrNull() );
         }
     }
 
     static Iterable<Relationship> getExistingContextRelationships(
         Node middleNode )
     {
         return middleNode.getRelationships(
             VerboseQuadStrategy.RelTypes.IN_CONTEXT );
     }
 
     private void removeAllContextRelationships( Node middleNode )
     {
         for ( Relationship relationship :
             getExistingContextRelationships( middleNode ) )
         {
             deleteRelationship( relationship );
         }
     }
 
     private void removeSelectedContextRelationships( Node middleNode,
         Collection<AbstractRelationship> contextRelationships )
     {
         for ( AbstractRelationship contextRelationship :
             contextRelationships )
         {
             Node contextNode = lookupNode(
                 contextRelationship.getEndNode() );
             if ( contextNode != null )
             {
                 Relationship relationship = findDirectRelationship( middleNode,
                     relationshipType(
                     contextRelationship.getRelationshipTypeName() ),
                     contextNode, Direction.OUTGOING );
                 if ( relationship != null )
                 {
                     deleteRelationship( relationship );
                 }
             }
         }
     }
 
     private void removeContextRelationships(
         AbstractRepresentation representation, Node middleNode )
     {
         Collection<AbstractRelationship> contextRelationships =
             getContextRelationships( representation );
         if ( contextRelationships.isEmpty() )
         {
             removeAllContextRelationships( middleNode );
         }
         else
         {
             removeSelectedContextRelationships( middleNode,
                 contextRelationships );
         }
     }
 
     private void handleRemoveLiteralRepresentation(
         AbstractRepresentation representation,
         Map<String, AbstractNode> typeToNode )
     {
         AbstractNode abstractSubjectNode = typeToNode.get(
             VerboseQuadStrategy.TYPE_SUBJECT );
         Node subjectNode = lookupNode( abstractSubjectNode );
         if ( subjectNode == null )
         {
             return;
         }
 
         AbstractRelationship subjectToMiddle = findAbstractRelationship(
             representation, VerboseQuadStrategy.TYPE_SUBJECT,
             VerboseQuadStrategy.TYPE_MIDDLE );
         AbstractRelationship middleToLiteral = findAbstractRelationship(
             representation, VerboseQuadStrategy.TYPE_MIDDLE,
             VerboseQuadStrategy.TYPE_LITERAL );
         AbstractNode abstractLiteralNode = typeToNode.get(
             VerboseQuadStrategy.TYPE_LITERAL );
 
         Node[] nodes = findMiddleAndObjectNode( subjectNode, subjectToMiddle,
             middleToLiteral, abstractLiteralNode, null );
         Node middleNode = nodes[ 0 ];
         Node literalNode = nodes[ 1 ];
         if ( literalNode == null )
         {
             return;
         }
 
         removeContextRelationships( representation, middleNode );
         if ( !middleNodeHasContexts( middleNode ) )
         {
             deleteMiddleAndLiteral( middleNode, middleToLiteral,
                 literalNode, subjectNode, subjectToMiddle );
         }
         deleteNodeIfEmpty( abstractSubjectNode, subjectNode );
     }
 
     private boolean middleNodeHasContexts( Node middleNode )
     {
         return getExistingContextRelationships(
             middleNode ).iterator().hasNext();
     }
 
     private String guessPredicateKey( Iterable<String> keys )
     {
         for ( String key : keys )
         {
             if ( !EXCLUDED_LITERAL_KEYS.contains( key ) )
             {
                 return key;
             }
         }
         return null;
     }
 
     private void deleteMiddleAndLiteral( Node middleNode,
         AbstractRelationship middleToLiteral, Node literalNode,
         Node subjectNode, AbstractRelationship subjectToMiddle )
     {
         disconnectMiddle( middleNode, middleToLiteral, literalNode,
             subjectNode, subjectToMiddle );
         String predicate = guessPredicateKey( literalNode.getPropertyKeys() );
         Object value = literalNode.getProperty( predicate );
         deleteLiteralNode( literalNode, predicate, value );
     }
 
     private void disconnectMiddle( Node middleNode,
         AbstractRelationship middleToOther, Node otherNode,
         Node subjectNode, AbstractRelationship subjectToMiddle )
     {
         ensureDirectlyDisconnected( middleNode, middleToOther, otherNode );
         ensureDirectlyDisconnected( subjectNode, subjectToMiddle, middleNode );
         deleteNode( middleNode, null );
     }
 }
