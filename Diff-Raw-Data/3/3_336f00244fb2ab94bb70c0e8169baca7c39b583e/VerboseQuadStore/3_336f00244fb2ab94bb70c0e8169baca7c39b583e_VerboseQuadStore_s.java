 package org.neo4j.rdf.store;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.Traverser.Order;
 import org.neo4j.neometa.structure.MetaStructure;
 import org.neo4j.rdf.fulltext.FulltextIndex;
 import org.neo4j.rdf.fulltext.QueryResult;
 import org.neo4j.rdf.fulltext.RawQueryResult;
 import org.neo4j.rdf.model.CompleteStatement;
 import org.neo4j.rdf.model.Context;
 import org.neo4j.rdf.model.Literal;
 import org.neo4j.rdf.model.Resource;
 import org.neo4j.rdf.model.Statement;
 import org.neo4j.rdf.model.StatementMetadata;
 import org.neo4j.rdf.model.Uri;
 import org.neo4j.rdf.model.Value;
 import org.neo4j.rdf.model.Wildcard;
 import org.neo4j.rdf.model.WildcardStatement;
 import org.neo4j.rdf.store.representation.AbstractNode;
 import org.neo4j.rdf.store.representation.standard.AbstractUriBasedExecutor;
 import org.neo4j.rdf.store.representation.standard.VerboseQuadExecutor;
 import org.neo4j.rdf.store.representation.standard.VerboseQuadStrategy;
 import org.neo4j.util.FilteringIterable;
 import org.neo4j.util.FilteringIterator;
 import org.neo4j.util.IterableWrapper;
 import org.neo4j.util.NeoUtil;
 import org.neo4j.util.NestingIterator;
 import org.neo4j.util.OneOfRelTypesReturnableEvaluator;
 import org.neo4j.util.PrefetchingIterator;
 import org.neo4j.util.RelationshipToNodeIterable;
 import org.neo4j.util.index.IndexService;
 
 public class VerboseQuadStore extends RdfStoreImpl
 {
     private final MetaStructure meta;
     
     public VerboseQuadStore( NeoService neo, IndexService indexer )
     {
         this( neo, indexer, null, null );
     }
     
     public VerboseQuadStore( NeoService neo, IndexService indexer,
         MetaStructure meta, FulltextIndex fulltextIndex )
     {
         super( neo, new VerboseQuadStrategy( new VerboseQuadExecutor( neo,
             indexer, meta, fulltextIndex ), meta ) );
         this.meta = meta;
         debug( "I'm initialized!" );
     }
     
     protected MetaStructure meta()
     {
         return this.meta;
     }
     
     @Override
     protected VerboseQuadStrategy getRepresentationStrategy()
     {
         return ( VerboseQuadStrategy ) super.getRepresentationStrategy();
     }
     
     @Override
     public Iterable<CompleteStatement> getStatements(
         WildcardStatement statement,
         boolean includeInferredStatements )
     {
         //        debug( "getStatements( " + statement + " )" );
         Transaction tx = neo().beginTx();
         try
         {
             if ( includeInferredStatements )
             {
                 throw new UnsupportedOperationException( "We currently not " +
                 "support getStatements() with reasoning enabled" );
             }
             
             Iterable<CompleteStatement> result = null;
             if ( wildcardPattern( statement, false, false, true ) )
             {
                 result = handleSubjectPredicateWildcard( statement );
             }
             else if ( wildcardPattern( statement, false, true, true ) )
             {
                 result = handleSubjectWildcardWildcard( statement );
             }
             else if ( wildcardPattern( statement, false, true, false ) )
             {
                 result = handleSubjectWildcardObject( statement );
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
             else if ( wildcardPattern( statement, true, false, true ) )
             {
                 result = handleWildcardPredicateWildcard( statement );
             }
             else if ( wildcardPattern( statement, true, true, true ) )
             {
                 result = handleWildcardWildcardWildcard( statement );
             }
             else
             {
                 result = super.getStatements( statement,
                     includeInferredStatements );
             }
             
             if ( result == null )
             {
                 result = new LinkedList<CompleteStatement>();
             }
             
             tx.success();
             return result;
         }
         finally
         {
             tx.finish();
         }
     }
     
     @Override
     public void reindexFulltextIndex()
     {
         Transaction tx = neo().beginTx();
         try
         {
             Iterable<Node> allMiddleNodes = getMiddleNodesFromAllContexts();
             Iterable<Object[]> allQuads = new MiddleNodeToQuadIterable(
                 new WildcardStatement( new Wildcard( "s" ), new Wildcard( "p" ),
                     new Wildcard( "o" ), new Wildcard( "g" ) ),
                     allMiddleNodes );
             int counter = 0;
             FulltextIndex fulltextIndex = getFulltextIndex();
             for ( Object[] quad : allQuads )
             {
                 String predicate = ( String ) quad[ 1 ];
                 Node objectNode = ( Node ) quad[ 2 ];
                 Value objectValue =
                     getValueForObjectNode( predicate, objectNode );
                 if ( objectValue instanceof Literal )
                 {
                     fulltextIndex.index( objectNode, new Uri( predicate ),
                         ( ( Literal ) objectValue ).getValue() );
                     if ( ++counter % 5000 == 0 )
                     {
                         fulltextIndex.end( true );
                     }
                 }
             }
             fulltextIndex.end( true );
             tx.success();
         }
         finally
         {
             tx.finish();
         }
     }
     
     @Override
     public Iterable<QueryResult> searchFulltext( String query )
     {
         FulltextIndex fulltextIndex = getFulltextIndex();
         if ( fulltextIndex == null )
         {
             throw new RuntimeException( "No fulltext index instantiated, " +
                 "please supply a FulltextIndex instance at construction time " +
             "to get this feature" );
         }
         
         Iterable<RawQueryResult> rawResult = fulltextIndex.search( query );
         final RawQueryResult[] latestQueryResult = new RawQueryResult[ 1 ];
         Iterable<Node> middleNodes = new LiteralToMiddleNodeIterable(
             new IterableWrapper<Node, RawQueryResult>( rawResult )
             {
                 @Override
                 protected Node underlyingObjectToObject( RawQueryResult object )
                 {
                     latestQueryResult[ 0 ] = object;
                     return object.getNode();
                 }
             } );
         
         Statement fakeWildcardStatement = new WildcardStatement(
             new Wildcard( "S" ), new Wildcard( "P" ),
             new Wildcard( "O" ), new Wildcard( "C" ) );
         Iterable<CompleteStatement> statementIterator = statementIterator(
             fakeWildcardStatement, middleNodes );
         return new IterableWrapper<QueryResult, CompleteStatement>(
             statementIterator )
             {
             @Override
             protected QueryResult underlyingObjectToObject(
                 CompleteStatement object )
             {
                 return new QueryResult( object,
                     latestQueryResult[ 0 ].getScore(),
                     latestQueryResult[ 0 ].getSnippet() );
             }
             };
     }
     
     @Override
     public int size( Context... contexts )
     {
         Transaction tx = neo().beginTx();
         try
         {
             Iterable<Node> contextNodes = null;
             if ( contexts.length == 0 )
             {
                 Node contextRefNode = getRepresentationStrategy().getExecutor().
                 getContextsReferenceNode();
                 contextNodes = new RelationshipToNodeIterable( contextRefNode,
                     contextRefNode.getRelationships(
                         VerboseQuadExecutor.RelTypes.IS_A_CONTEXT,
                         Direction.OUTGOING ) );
             }
             else
             {
                 ArrayList<Node> nodes = new ArrayList<Node>();
                 for ( Context context : contexts )
                 {
                     Node node = getRepresentationStrategy().getExecutor().
                     lookupNode( new AbstractNode( context ) );
                     if ( node != null )
                     {
                         nodes.add( node );
                     }
                 }
                 contextNodes = nodes;
             }
             
             int size = 0;
             for ( Node node : contextNodes )
             {
                 size += ( Integer ) node.getProperty(
                     VerboseQuadExecutor.STATEMENT_COUNT, 0 );
             }
             tx.success();
             return size;
         }
         finally
         {
             tx.finish();
         }
     }
     
     private void debug( String message )
     {
         //        System.out.println( "====> VerboseQuadStore: " + message );
     }
     
     private String getNodeUriOrNull( Node node )
     {
         return ( String ) node.getProperty(
             AbstractUriBasedExecutor.URI_PROPERTY_KEY, null );
     }
     
     private Value getValueForObjectNode( String predicate, Node objectNode )
     {
         String uri = ( String ) objectNode.getProperty(
             AbstractUriBasedExecutor.URI_PROPERTY_KEY, null );
         if ( uri != null )
         {
             return new Uri( uri );
         }
         else
         {
             Object value = objectNode.getProperty(
                 AbstractUriBasedExecutor.LITERAL_VALUE_KEY );
             String datatype = ( String ) objectNode.getProperty(
                 VerboseQuadExecutor.LITERAL_DATATYPE_KEY, null );
             String language = ( String ) objectNode.getProperty(
                 VerboseQuadExecutor.LITERAL_LANGUAGE_KEY, null );
             return new Literal( value, datatype == null ? null :
                 new Uri( datatype ), language );
         }
     }
     
     private Iterable<Node> getMiddleNodesFromLiterals( Statement statement )
     {
         Literal literal = ( Literal ) statement.getObject();
         Iterable<Node> literalNodes = getRepresentationStrategy().
             getExecutor().findLiteralNodes( literal.getValue() );
         return new LiteralToMiddleNodeIterable( literalNodes );
     }
     
     private Iterable<Node> getMiddleNodesFromAllContexts()
     {
         return getRepresentationStrategy().getExecutor().
         getContextsReferenceNode().traverse( Order.DEPTH_FIRST,
             StopEvaluator.END_OF_GRAPH,
             new OneOfRelTypesReturnableEvaluator(
                 VerboseQuadStrategy.RelTypes.IN_CONTEXT ),
                 VerboseQuadExecutor.RelTypes.IS_A_CONTEXT, Direction.OUTGOING,
                 VerboseQuadStrategy.RelTypes.IN_CONTEXT, Direction.INCOMING );
     }
     
     private Iterable<CompleteStatement> handleSubjectPredicateWildcard(
         Statement statement )
     {
         Node subjectNode = lookupNode( statement.getSubject() );
         if ( subjectNode == null )
         {
             return null;
         }
         Iterable<Node> middleNodes = new RelationshipToNodeIterable(
             subjectNode, subjectNode.getRelationships( relType( statement ),
                 Direction.OUTGOING ) );
         return statementIterator( statement, middleNodes );
     }
     
     private Iterable<CompleteStatement> handleSubjectWildcardWildcard(
         Statement statement )
     {
         Node subjectNode = lookupNode( statement.getSubject() );
         if ( subjectNode == null )
         {
             return null;
         }
         Iterable<Node> middleNodes = new RelationshipToNodeIterable(
             subjectNode, subjectNode.getRelationships( Direction.OUTGOING ) );
         return statementIterator( statement, middleNodes );
     }
     
     private Iterable<CompleteStatement> handleSubjectWildcardObject(
         final Statement statement )
     {
         // TODO Optimization: maybe check which has least rels (S or O)
         // and start there.
         Node subjectNode = lookupNode( statement.getSubject() );
         if ( subjectNode == null )
         {
             return null;
         }
         Iterable<Relationship> relationships =
             subjectNode.getRelationships( Direction.OUTGOING );
         relationships = new ObjectFilteredRelationships( statement,
             relationships );
         Iterable<Node> middleNodes = new RelationshipToNodeIterable(
             subjectNode, relationships );
         return statementIterator( statement, middleNodes );
     }
     
     private Iterable<CompleteStatement> handleSubjectPredicateObject(
         Statement statement )
     {
         Node subjectNode = lookupNode( statement.getSubject() );
         if ( subjectNode == null )
         {
             return null;
         }
         Iterable<Relationship> relationships = subjectNode.getRelationships(
             relType( statement ), Direction.OUTGOING );
         relationships = new ObjectFilteredRelationships( statement,
             relationships );
         Iterable<Node> middleNodes = new RelationshipToNodeIterable(
             subjectNode, relationships );
         return statementIterator( statement, middleNodes );
     }
     
     private Iterable<CompleteStatement> handleWildcardWildcardObject(
         Statement statement )
     {
         Iterable<Node> middleNodes = null;
         if ( statement.getObject() instanceof Literal )
         {
             middleNodes = getMiddleNodesFromLiterals( statement );
         }
         else
         {
             Node objectNode = lookupNode( statement.getObject() );
             if ( objectNode == null )
             {
                 return null;
             }
             middleNodes = new RelationshipToNodeIterable(
                 objectNode, objectNode.getRelationships( Direction.INCOMING ) );
         }
         return statementIterator( statement, middleNodes );
     }
     
     private Iterable<CompleteStatement> handleWildcardPredicateWildcard(
         Statement statement )
     {
         Iterable<Node> middleNodes = null;
         if ( statement.getContext().isWildcard() )
         {
             middleNodes = getMiddleNodesFromAllContexts();
         }
         else
         {
             Node contextNode = lookupNode( statement.getContext() );
             if ( contextNode == null )
             {
                 return null;
             }
             middleNodes = new RelationshipToNodeIterable(
                 contextNode, contextNode.getRelationships(
                     VerboseQuadStrategy.RelTypes.IN_CONTEXT,
                     Direction.INCOMING ) );
         }
         middleNodes = new PredicateFilteredNodes( statement, middleNodes );
         return statementIterator( statement, middleNodes );
     }
     
     private Iterable<CompleteStatement> handleWildcardPredicateObject(
         Statement statement )
     {
         Iterable<Node> middleNodes = null;
         if ( statement.getObject() instanceof Literal )
         {
             middleNodes = new PredicateFilteredNodes( statement,
                 getMiddleNodesFromLiterals( statement ) );
         }
         else
         {
             Node objectNode = lookupNode( statement.getObject() );
             if ( objectNode == null )
             {
                 return null;
             }
             middleNodes = new RelationshipToNodeIterable(
                 objectNode, objectNode.getRelationships( relType( statement ),
                     Direction.INCOMING ) );
         }
         return statementIterator( statement, middleNodes );
     }
     
     private Iterable<CompleteStatement> handleWildcardWildcardWildcard(
         Statement statement )
     {
         Iterable<Node> middleNodes = null;
         if ( statement.getContext().isWildcard() )
         {
             middleNodes = getMiddleNodesFromAllContexts();
         }
         else
         {
             Node contextNode = lookupNode( statement.getContext() );
             if ( contextNode == null )
             {
                 return null;
             }
             middleNodes = new RelationshipToNodeIterable(
                 contextNode, contextNode.getRelationships(
                     VerboseQuadStrategy.RelTypes.IN_CONTEXT,
                     Direction.INCOMING ) );
         }
         return statementIterator( statement, middleNodes );
     }
     
     private Iterable<CompleteStatement> statementIterator(
         Statement statement, Iterable<Node> middleNodes )
     {
         return new QuadToStatementIterable( new MiddleNodeToQuadIterable(
             statement, middleNodes ) );
         
         // Enable this when we implement inferencing.
         //    	return new QuadToStatementIterable(
         //    		new QuadWithInferencingIterable(
         //    		new MiddleNodeToQuadIterable( statement, middleNodes ) ) );
     }
     
     private class QuadToStatementIterable
         extends IterableWrapper<CompleteStatement, Object[]>
     {
         QuadToStatementIterable( Iterable<Object[]> source )
         {
             super( source );
         }
         
         @Override
         protected CompleteStatement underlyingObjectToObject( Object[] quad )
         {
             Node subjectNode = ( Node ) quad[ 0 ];
             Uri subject = new Uri( getNodeUriOrNull( subjectNode ) );
             Uri predicate = new Uri( ( String ) quad[ 1 ] );
             Node objectNode = ( Node ) quad[ 2 ];
             Value object = getValueForObjectNode( predicate.getUriAsString(),
                 objectNode );
             Node contextNode = ( Node ) quad[ 3 ];
             Context context = new Context( getNodeUriOrNull( contextNode ) );
             Relationship contextRelationship = ( Relationship ) quad[ 5 ];
             StatementMetadata metadata = new VerboseQuadStatementMetadata(
                 contextRelationship );
             return object instanceof Literal ?
                 new CompleteStatement( subject, predicate, ( Literal ) object,
                     context, metadata ) :
                 new CompleteStatement( subject, predicate, ( Resource ) object,
                     context, metadata );
         }
     }
     
     //    private class QuadWithInferencingIterable
     //        extends NestingIterable<Object[]>
     //    {
     //        QuadWithInferencingIterable( Iterable<Object[]> quads )
     //        {
     //            super( quads );
     //        }
     //        
     //        @Override
     //        protected Iterator<Object[]> createNestedIterator( Object[] item )
     //        {
     //            return new SingleIterator<Object[]>( item );
     //        }
     //    }
     
     //    private class SingleIterator<T> extends PrefetchingIterator<T>
     //    {
     //        private T item;
     //    	
     //        SingleIterator( T item )
     //        {
     //            this.item = item;
     //        }
     //    	
     //        @Override
     //        protected T fetchNextOrNull()
     //        {
     //            T result = item;
     //            item = null;
     //            return result;
     //        }
     //    }
     
     /**
      * The Object[] will contain
      * {
      *     Node subject
      *     String predicate
      *     Node object
      *     Node context
      *     Node middleNode
      *     Relationship middleNodeToContextRelationship
      * }
      */
     private class MiddleNodeToQuadIterable implements Iterable<Object[]>
     {
         private Statement statement;
         private Iterable<Node> middleNodes;
         
         MiddleNodeToQuadIterable( Statement statement,
             Iterable<Node> middleNodes )
         {
             this.statement = statement;
             this.middleNodes = middleNodes;
         }
         
         public Iterator<Object[]> iterator()
         {
             return new MiddleNodeToQuadIterator( statement,
                 middleNodes.iterator() );
         }
     }
     
     private class MiddleNodeToQuadIterator
         extends PrefetchingIterator<Object[]>
     {
         private Statement statement;
         private NestingIterator<Node, Node> middleNodesWithContexts;
         private Relationship lastContextRelationship;
         
         MiddleNodeToQuadIterator( Statement statement,
             Iterator<Node> middleNodes )
         {
             this.statement = statement;
             this.middleNodesWithContexts =
                 new NestingIterator<Node, Node>( middleNodes )
             {
                 @Override
                 protected Iterator<Node> createNestedIterator( Node item )
                 {
                     return newContextIterator( item );
                 }
             };
         }
         
         @Override
         protected Object[] fetchNextOrNull()
         {
             return middleNodesWithContexts.hasNext() ? nextQuad() : null;
         }
         
         private Iterator<Node> newContextIterator( Node middleNode )
         {
             // TODO With a traverser it's... somewhat like
             // 1000 times slower, why Johan why?
             Iterator<Node> iterator = new RelationshipToNodeIterable( 
                 middleNode, middleNode.getRelationships(
                     VerboseQuadStrategy.RelTypes.IN_CONTEXT,
                     Direction.OUTGOING ) )
             {
                 @Override
                 protected Node underlyingObjectToObject(
                     Relationship relationship )
                 {
                     lastContextRelationship = relationship;
                     return super.underlyingObjectToObject( relationship );
                 }
             }.iterator();
             
             if ( !statement.getContext().isWildcard() )
             {
                 iterator = new FilteringIterator<Node>( iterator )
                 {
                     @Override
                     protected boolean passes( Node contextNode )
                     {
                         String contextUri = getNodeUriOrNull( contextNode );
                         return new Context( contextUri ).equals(
                             statement.getContext() );
                     }
                 };
             }
             return iterator;
         }
         
         private Object[] nextQuad()
         {
             Node contextNode = middleNodesWithContexts.next();
             Node middleNode = middleNodesWithContexts.getCurrentSurfaceItem();
             Relationship subjectRelationship = middleNode.getRelationships(
                 Direction.INCOMING ).iterator().next();
             String predicate = subjectRelationship.getType().name();
             Node subjectNode = subjectRelationship.getOtherNode( middleNode );
             try
             {
                 Node objectNode = middleNode.getSingleRelationship(
                     subjectRelationship.getType(),
                     Direction.OUTGOING ).getEndNode();
                 return new Object[] { subjectNode, predicate,
                    objectNode, contextNode, middleNode, lastContextRelationship };
             }
             catch ( RuntimeException e )
             {
                 System.out.println( "hmm, middle node " + middleNode );
                 for ( Relationship rel : middleNode.getRelationships() )
                 {
                     System.out.println( rel.getStartNode() + " --[" +
                         rel.getType().name() + "]--> " + rel.getEndNode() );
                 }
                 throw e;
             }
         }
     }
     
     private class PredicateFilteredNodes extends FilteringIterable<Node>
     {
         private Statement statement;
         
         PredicateFilteredNodes( Statement statment, Iterable<Node> source )
         {
             super( source );
             this.statement = statment;
         }
         
         @Override
         protected boolean passes( Node middleNode )
         {
             Iterator<Relationship> rels = middleNode.getRelationships(
                 Direction.INCOMING ).iterator();
             if ( !rels.hasNext() )
             {
                 return false;
             }
             Relationship relationship = rels.next();
             return relationship.getType().name().equals( ( ( Uri )
                 statement.getPredicate() ).getUriAsString() );
         }
     }
     
     private class ObjectFilteredRelationships
         extends FilteringIterable<Relationship>
     {
         private Statement statement;
         
         ObjectFilteredRelationships( Statement statement,
             Iterable<Relationship> source )
         {
             super( source );
             this.statement = statement;
         }
         
         @Override
         protected boolean passes( Relationship subjectToMiddleRel )
         {
             Node middleNode = subjectToMiddleRel.getEndNode();
             Node objectNode = middleNode.getSingleRelationship(
                 subjectToMiddleRel.getType(), Direction.OUTGOING ).getEndNode();
             Value objectValue = getValueForObjectNode(
                 subjectToMiddleRel.getType().name(), objectNode );
             return objectValue.equals( statement.getObject() );
         }
     }
     
     //    private class SubjectFilteredRelationships
     //        extends FilteringIterable<Relationship>
     //    {
     //        private Node subjectNode;
     //        
     //        SubjectFilteredRelationships( Node subjectNode,
     //            Iterable<Relationship> source )
     //        {
     //            super( source );
     //            this.subjectNode = subjectNode;
     //        }
     //        
     //        @Override
     //        protected boolean passes( Relationship middleToObjectRel )
     //        {
     //            Node thisSubjectNode = middleToObjectRel.getStartNode().
     //            getSingleRelationship( middleToObjectRel.getType(),
     //                Direction.INCOMING ).getStartNode();
     //            return thisSubjectNode.equals( this.subjectNode );
     //        }
     //    }
     
     private class LiteralToMiddleNodeIterable
         extends IterableWrapper<Node, Node>
     {
         LiteralToMiddleNodeIterable( Iterable<Node> literalNodes )
         {
             super( literalNodes );
         }
         
         @Override
         protected Node underlyingObjectToObject( Node literalNode )
         {
             Iterator<Relationship> relationships = literalNode.getRelationships(
                 Direction.INCOMING ).iterator();
             if ( !relationships.hasNext() )
             {
                 throw new RuntimeException( literalNode + " is a node which " +
                     "should've been a literal node and, hence, had an " +
                     "INCOMING relationship representing the relationship to " +
                     "the middle node of the statement. Instead it has\n" +
                     new NeoUtil( neo() ).sumNodeContents( literalNode ) );
             }
             return relationships.next().getStartNode();
         }
     }
     
     private static class VerboseQuadStatementMetadata
         implements StatementMetadata
     {
         private static final String PREFIX_VALUE = "value.";
         private static final String PREFIX_DATATYPE = "datatype.";
         private static final String PREFIX_LANGUAGE = "language.";
         
         private Relationship relationship;
         
         private VerboseQuadStatementMetadata(
             Relationship relationshipBetweenMiddleNodeAndContext )
         {
             this.relationship = relationshipBetweenMiddleNodeAndContext;
         }
         
         public Literal get( String key )
         {
             Object value = relationship.getProperty( PREFIX_VALUE + key );
             String datatype = ( String )
             relationship.getProperty( PREFIX_DATATYPE + key, null );
             String language = ( String )
             relationship.getProperty( PREFIX_LANGUAGE + key, null );
             Literal literal = null;
             if ( datatype != null )
             {
                 Uri datatypeUri = new Uri( datatype );
                 literal = language != null ?
                     new Literal( value, datatypeUri, language ) :
                         new Literal( value, datatypeUri );
             }
             else
             {
                 literal = new Literal( value );
             }
             return literal;
         }
         
         public boolean has( String key )
         {
             return relationship.hasProperty( PREFIX_VALUE + key );
         }
         
         public void remove( String key )
         {
             relationship.removeProperty( PREFIX_VALUE + key );
             setOrRemoveIfExists( PREFIX_DATATYPE + key, null );
             setOrRemoveIfExists( PREFIX_LANGUAGE + key, null );
         }
         
         public void set( String key, Literal value )
         {
             Object literalValue = value.getValue();
             Uri datatypeUri = value.getDatatype();
             String language = value.getLanguage();
             relationship.setProperty( PREFIX_VALUE + key, literalValue );
             setOrRemoveIfExists( PREFIX_DATATYPE + key,
                 datatypeUri != null ? datatypeUri.getUriAsString() : null );
             setOrRemoveIfExists( PREFIX_LANGUAGE + key, language );
         }
         
         private void setOrRemoveIfExists( String key, Object value )
         {
             if ( value != null )
             {
                 relationship.setProperty( key, value );
             }
             else if ( relationship.hasProperty( key ) )
             {
                 relationship.removeProperty( key );
             }
         }
         
         public Iterable<String> getKeys()
         {
             Collection<String> keys = new ArrayList<String>();
             for ( String key : relationship.getPropertyKeys() )
             {
                 if ( key.startsWith( PREFIX_VALUE ) )
                 {
                     keys.add( key.substring( PREFIX_VALUE.length() ) );
                 }
             }
             return keys;
         }
     }
 }
