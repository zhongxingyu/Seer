 package org.neo4j.rdf.store;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.neometa.structure.MetaStructure;
 import org.neo4j.rdf.model.CompleteStatement;
 import org.neo4j.rdf.model.Context;
 import org.neo4j.rdf.model.Literal;
 import org.neo4j.rdf.model.Resource;
 import org.neo4j.rdf.model.Statement;
 import org.neo4j.rdf.model.Uri;
 import org.neo4j.rdf.model.Value;
 import org.neo4j.rdf.model.Wildcard;
 import org.neo4j.rdf.model.WildcardStatement;
 import org.neo4j.rdf.store.representation.AbstractNode;
 import org.neo4j.rdf.store.representation.standard.AbstractUriBasedExecutor;
 import org.neo4j.rdf.store.representation.standard.AlwaysMiddleExecutor;
 import org.neo4j.rdf.store.representation.standard.AlwaysMiddleNodesStrategy;
 import org.neo4j.rdf.store.representation.standard.AlwaysMiddleValidatable;
 import org.neo4j.rdf.validation.Validatable;
 
 public class AlwaysMiddleStore extends RdfStoreImpl
 {
     private MetaStructure meta;
 
     public AlwaysMiddleStore( NeoService neo, MetaStructure meta )
     {
         super( neo, new AlwaysMiddleNodesStrategy(
             new AlwaysMiddleExecutor( neo, AbstractUriBasedExecutor.newIndex(
                 neo ), meta ), meta ) );
         this.meta = meta;
     }
 
     protected MetaStructure meta()
     {
         return this.meta;
     }
 
     @Override
     protected AlwaysMiddleNodesStrategy getRepresentationStrategy()
     {
         return ( AlwaysMiddleNodesStrategy ) super.getRepresentationStrategy();
     }
 
     @Override
     public Iterable<Statement> getStatements( WildcardStatement statement,
         boolean includeInferredStatements )
     {
         Transaction tx = neo().beginTx();
         try
         {
             if ( includeInferredStatements )
             {
                 throw new UnsupportedOperationException( "We currently not " +
                     "support getStatements() with reasoning enabled" );
             }
 
             Iterable<Statement> result = null;
             if ( wildcardPattern( statement, false, false, true ) )
             {
                 result = handleSubjectPredicateWildcard( statement );
             }
             else if ( wildcardPattern( statement, false, true, true ) )
             {
                 result = handleSubjectWildcardWildcard( statement );
             }
             else if ( wildcardPattern( statement, true, true, false ) )
             {
                 result = handleWildcardWildcardObject( statement );
             }
             else if ( wildcardPattern( statement, true, false, false ) )
             {
                 result = handleWildcardPredicateObject( statement );
             }
             else if ( wildcardPattern( statement, false, false, false ) )
             {
                 result = handleSubjectPredicateObject( statement );
             }
             else
             {
                 result = super.getStatements( statement,
                     includeInferredStatements );
             }
 
             tx.success();
             return result;
         }
         finally
         {
             tx.finish();
         }
     }
 
     private Iterable<Statement> handleSubjectPredicateObject(
         Statement statement )
     {
         ArrayList<Statement> statements = new ArrayList<Statement>();
         Uri subject = ( Uri ) statement.getSubject();
         Uri predicate = ( Uri ) statement.getPredicate();
 
         AbstractNode abstractSubjectNode = new AbstractNode( subject );
         Node subjectNode = getRepresentationStrategy().getExecutor().
             lookupNode( abstractSubjectNode );
         if ( subjectNode == null )
         {
             return new ArrayList<Statement>();
         }
 
         AlwaysMiddleValidatable validatable = newValidatable( subjectNode );
         if ( statement.getObject() instanceof Uri )
         {
             AbstractNode abstractObjectNode =
                 new AbstractNode( statement.getObject() );
             Node objectNode = getRepresentationStrategy().getExecutor().
                 lookupNode( abstractObjectNode );
             if ( objectNode == null )
             {
                 return new ArrayList<Statement>();
             }
             for ( Validatable complexProperty : validatable.complexProperties(
                 predicate.getUriAsString() ) )
             {
                 if ( complexProperty.getUnderlyingNode().equals( objectNode ) )
                 {
                     statements.add( statement );
                     break;
                 }
             }
         }
         else
         {
             Object value = ( ( Literal ) statement.getObject() ).getValue();
             for ( Object property : validatable.getProperties(
                 predicate.getUriAsString() ) )
             {
                 if ( property.equals( value ) )
                 {
                     statements.add( statement );
                     break;
                 }
             }
         }
         return statements;
     }
 
     private Iterable<Statement> handleWildcardPredicateObject(
         WildcardStatement statement )
     {
         return handleWildcardWildcardObject( statement );
     }
 
     private AlwaysMiddleValidatable newValidatable( Node subjectNode )
     {
         return new AlwaysMiddleValidatable( neo(), subjectNode, meta() );
     }
 
     private void buildStatementsOfObjectHits( Statement statement,
         List<Statement> statements, Literal literal )
     {
         AlwaysMiddleExecutor executor = ( AlwaysMiddleExecutor )
             getRepresentationStrategy().getExecutor();
         Object value = literal.getValue();
         String predicate = statement.getPredicate() instanceof Wildcard ?
             null : ( ( Uri ) statement.getPredicate() ).getUriAsString();
         for ( Node literalNode : executor.findLiteralNodes( value ) )
         {
             for ( Relationship rel : literalNode.getRelationships(
                 Direction.INCOMING ) )
             {
                 Node middleNode = rel.getStartNode();
                 Uri thePredicate = new Uri( rel.getType().name() );
                 if ( predicate != null &&
                     !thePredicate.getUriAsString().equals( predicate ) )
                 {
                     continue;
                 }
 
                 addIfInContext( statement, statements, middleNode,
                     thePredicate.getUriAsString() );
 //                Node subjectNode = middleNode.getSingleRelationship(
 //                    rel.getType(), Direction.INCOMING ).getStartNode();
 //                Validatable validatable = newValidatable( subjectNode );
 //                Uri subject = validatable.getUri();
 //                statements.add( new CompleteStatement( subject, thePredicate,
 //                    new Literal( value ) ) );
             }
         }
     }
 
     private void buildStatementsOfObjectHits( Statement statement,
         List<Statement> statements, Uri object )
     {
         Uri objectUri = ( Uri ) statement.getObject();
         Node objectNode = getRepresentationStrategy().getExecutor().
             lookupNode( new AbstractNode( objectUri ) );
         if ( objectNode == null )
         {
             return;
         }
 
         String predicate = statement.getPredicate() instanceof Wildcard ?
             null : ( ( Uri ) statement.getPredicate() ).getUriAsString();
         for ( Relationship rel : objectNode.getRelationships(
             Direction.INCOMING ) )
         {
             Node middleNode = rel.getStartNode();
             Uri thePredicate = new Uri( rel.getType().name() );
             if ( predicate != null &&
                 !thePredicate.getUriAsString().equals( predicate ) )
             {
                 continue;
             }
 
             addIfInContext( statement, statements, middleNode,
                 thePredicate.getUriAsString() );
         }
     }
 
     private RelationshipType relType( final String name )
     {
         return new RelationshipType()
         {
             public String name()
             {
                 return name;
             }
         };
     }
 
     private void addIfInContext( Statement statement,
         List<Statement> statements, Node middleNode, String predicate )
     {
         Relationship rel = middleNode.getSingleRelationship(
             relType( predicate ), Direction.INCOMING );
         Node subjectNode = rel.getStartNode();
         Validatable validatable = newValidatable( subjectNode );
         Uri subject = validatable.getUri();
         Set<String> contextsInNeo = new HashSet<String>();
         for ( Relationship relationship : middleNode.getRelationships(
             AlwaysMiddleNodesStrategy.RelTypes.IN_CONTEXT,
             Direction.OUTGOING ) )
         {
             Node contextNode = relationship.getEndNode();
             String uri = ( String ) contextNode.getProperty(
                 AbstractUriBasedExecutor.URI_PROPERTY_KEY, null );
             if ( uri != null )
             {
                 contextsInNeo.add( uri );
             }
         }
         for ( Context context : statement.getContexts() )
         {
            if ( context == null || context.getUriAsString() != null &&
                 !contextsInNeo.contains( context.getUriAsString() ) )
             {
                 return;
             }
         }
 
         Node objectNode = middleNode.getSingleRelationship(
             relType( predicate ), Direction.OUTGOING ).getEndNode();
         String uri = ( String ) objectNode.getProperty(
             AbstractUriBasedExecutor.URI_PROPERTY_KEY, null );
         Value object = uri == null ? new Literal( objectNode.getProperty(
             predicate ) ) : new Uri( uri );
         if ( object instanceof Resource )
         {
             statements.add( new CompleteStatement( subject,
                 new Uri( predicate ), ( Resource ) object ) );
         }
         else
         {
             statements.add( new CompleteStatement( subject,
                 new Uri( predicate ), ( Literal ) object ) );
         }
     }
 
     private Iterable<Statement> handleWildcardWildcardObject(
         WildcardStatement statement )
     {
         List<Statement> statements = new ArrayList<Statement>();
         if ( statement.getObject() instanceof Literal )
         {
             buildStatementsOfObjectHits( statement, statements,
                 ( Literal ) statement.getObject() );
         }
         else
         {
             buildStatementsOfObjectHits( statement, statements,
                 ( Uri ) statement.getObject() );
         }
         return statements;
     }
 
     private Iterable<Statement> handleSubjectPredicateWildcard(
         WildcardStatement statement )
     {
         Uri subject = ( Uri ) statement.getSubject();
         Uri predicate = ( Uri ) statement.getPredicate();
 
         AbstractNode abstractSubjectNode = new AbstractNode( subject );
         Node subjectNode = getRepresentationStrategy().getExecutor().
             lookupNode( abstractSubjectNode );
         if ( subjectNode == null )
         {
             return new ArrayList<Statement>();
         }
 
         AlwaysMiddleValidatable validatableInstance =
             newValidatable( subjectNode );
         List<Statement> statementList = new LinkedList<Statement>();
         addObjects( statement, statementList, subject, predicate, subjectNode,
             validatableInstance );
         return statementList;
     }
 
     private void addObjects( Statement statement, List<Statement> statementList,
         Uri subject, Uri predicate, Node subjectNode,
         AlwaysMiddleValidatable validatableInstance )
     {
         if ( getRepresentationStrategy().pointsToObjectType( predicate ) )
         {
             Node[] middleNodes = validatableInstance.
                 getComplexPropertiesAsMiddleNodes( predicate.getUriAsString() );
             for ( Node middleNode : middleNodes )
             {
                 addIfInContext( statement, statementList, middleNode,
                     predicate.getUriAsString() );
             }
         }
         else
         {
             Node[] middleNodes = validatableInstance.
                 getSimplePropertiesAsMiddleNodes( predicate.getUriAsString() );
             for ( Node middleNode : middleNodes )
             {
                 addIfInContext( statement, statementList, middleNode,
                     predicate.getUriAsString() );
             }
         }
     }
 
     private void addObjects( Statement statement, List<Statement> statementList,
         Uri subject, Node subjectNode, AlwaysMiddleValidatable instance )
     {
         for ( String predicate : instance.getAllPropertyKeys() )
         {
             addObjects( statement, statementList, subject, new Uri( predicate ),
                 subjectNode, instance );
         }
 //        for ( MetaStructureClass cls : validatableInstance.getClasses() )
 //        {
 //            statementList.add( new CompleteStatement( subject, new Uri(
 //                AbstractUriBasedExecutor.RDF_TYPE_URI ), new Uri(
 //                    cls.getName() ) ) );
 //        }
     }
 
     private Iterable<Statement> handleSubjectWildcardWildcard(
         WildcardStatement statement )
     {
         Uri subject = ( Uri ) statement.getSubject();
         AbstractNode abstractSubjectNode = new AbstractNode( subject );
         Node subjectNode = getRepresentationStrategy().getExecutor().
             lookupNode( abstractSubjectNode );
         if ( subjectNode == null )
         {
             return new ArrayList<Statement>();
         }
 
         AlwaysMiddleValidatable validatableInstance =
             newValidatable( subjectNode );
         List<Statement> statementList = new ArrayList<Statement>();
         addObjects( statement, statementList, subject,
             subjectNode, validatableInstance );
         return statementList;
     }
 }
