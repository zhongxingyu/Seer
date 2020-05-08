 package org.neo4j.rdf.store.representation.standard;
 
 import java.util.Map;
 
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.neometa.structure.MetaStructure;
 import org.neo4j.rdf.model.Context;
 import org.neo4j.rdf.model.Literal;
 import org.neo4j.rdf.model.Statement;
 import org.neo4j.rdf.store.representation.AbstractNode;
 import org.neo4j.rdf.store.representation.AbstractRelationship;
 import org.neo4j.rdf.store.representation.AbstractRepresentation;
 import org.neo4j.rdf.store.representation.RepresentationExecutor;
 
 public class AlwaysMiddleNodesStrategy
     extends StandardAbstractRepresentationStrategy
 {
     // :) TODO big time.
     public static final String EXECUTOR_INFO_NODE_TYPE = "nodetype";
     public static final String TYPE_SUBJECT = "subject";
     public static final String TYPE_MIDDLE = "middle";
     public static final String TYPE_LITERAL = "literal";
     public static final String TYPE_OBJECT = "object";
     public static final String TYPE_CONTEXT = "context";
 
     public static final String KEY_OBJECT_URI_ON_MIDDLE_NODE = "object_uri";
 
     public AlwaysMiddleNodesStrategy( RepresentationExecutor executor,
         MetaStructure meta )
     {
         super( executor, meta );
     }
 
     @Override
     protected boolean addToRepresentation(
         AbstractRepresentation representation,
         Map<String, AbstractNode> nodeMapping, Statement statement )
     {
         if ( !super.addToRepresentation(
             representation, nodeMapping, statement ) )
         {
             if ( isObjectType( statement.getObject() ) )
             {
                 addObjectTypeRepresentation( representation, nodeMapping,
                     statement );
             }
             else
             {
                 addLiteralRepresentation( representation, nodeMapping,
                     statement );
             }
         }
         return true;
     }
 
     protected void addLiteralRepresentation(
         AbstractRepresentation representation,
         Map<String, AbstractNode> nodeMapping, Statement statement )
     {
         AbstractNode subjectNode = getSubjectNode( nodeMapping, statement );
         representation.addNode( subjectNode );
         subjectNode.addExecutorInfo( EXECUTOR_INFO_NODE_TYPE, TYPE_SUBJECT );
         AbstractNode middleNode = new AbstractNode( null );
         middleNode.addExecutorInfo( EXECUTOR_INFO_NODE_TYPE, TYPE_MIDDLE );
         representation.addNode( middleNode );
         AbstractNode literalNode = new AbstractNode( null );
         literalNode.addExecutorInfo( EXECUTOR_INFO_NODE_TYPE, TYPE_LITERAL );
         literalNode.addProperty( asUri( statement.getPredicate() ),
             ( ( Literal ) statement.getObject() ).getValue() );
         representation.addNode( literalNode );
 
         connectThreeNodes( representation, subjectNode, middleNode,
             literalNode, statement );
         addNodeToContexts( representation, middleNode, nodeMapping,
             statement );
     }
 
     protected void connectThreeNodes(
         AbstractRepresentation representation, AbstractNode subjectNode,
         AbstractNode middleNode, AbstractNode otherNode, Statement statement )
     {
         String predicate = asUri( statement.getPredicate() );
         AbstractRelationship subjectToMiddle = new AbstractRelationship(
             subjectNode, predicate, middleNode );
         representation.addRelationship( subjectToMiddle );
         AbstractRelationship middleToOther = new AbstractRelationship(
             middleNode, predicate, otherNode );
         representation.addRelationship( middleToOther );
     }
 
     protected void addNodeToContexts(
         AbstractRepresentation representation, AbstractNode middleNode,
         Map<String, AbstractNode> nodeMapping, Statement statement )
     {
         // Connect to contexts (if any)
         for ( Context context : statement.getContexts() )
         {
            if ( context == null )
            {
                continue;
            }

             AbstractNode contextNode = getContextNode( nodeMapping, context );
             AbstractRelationship middleToContext = new AbstractRelationship(
                 middleNode, RelTypes.IN_CONTEXT.name(), contextNode );
             representation.addRelationship( middleToContext );
             contextNode.addExecutorInfo( EXECUTOR_INFO_NODE_TYPE,
                 TYPE_CONTEXT );
             representation.addNode( contextNode );
         }
     }
 
     protected void addObjectTypeRepresentation(
         AbstractRepresentation representation,
         Map<String, AbstractNode> nodeMapping, Statement statement )
     {
         AbstractNode subjectNode = getSubjectNode( nodeMapping, statement );
         representation.addNode( subjectNode );
         subjectNode.addExecutorInfo( EXECUTOR_INFO_NODE_TYPE, TYPE_SUBJECT );
         AbstractNode middleNode = new AbstractNode( null );
         middleNode.addExecutorInfo( EXECUTOR_INFO_NODE_TYPE, TYPE_MIDDLE );
         representation.addNode( middleNode );
         AbstractNode objectNode = getObjectNode( nodeMapping, statement );
         objectNode.addExecutorInfo( EXECUTOR_INFO_NODE_TYPE, TYPE_OBJECT );
         representation.addNode( objectNode );
 
         connectThreeNodes( representation, subjectNode, middleNode, objectNode,
             statement );
         addNodeToContexts( representation, middleNode, nodeMapping, statement );
     }
 
     public static enum RelTypes implements RelationshipType
     {
         IN_CONTEXT,
     }
 }
