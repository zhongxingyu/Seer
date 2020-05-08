 package org.neo4j.rdf.store;
 
 import java.util.Map;
 
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.rdf.model.CompleteStatement;
 import org.neo4j.rdf.model.Context;
 import org.neo4j.rdf.model.Statement;
 import org.neo4j.rdf.model.Value;
 import org.neo4j.rdf.model.Wildcard;
 import org.neo4j.rdf.model.WildcardStatement;
 import org.neo4j.rdf.store.representation.AbstractElement;
 import org.neo4j.rdf.store.representation.AbstractRepresentation;
 import org.neo4j.rdf.store.representation.RepresentationExecutor;
 import org.neo4j.rdf.store.representation.RepresentationStrategy;
 import org.neo4j.util.matching.PatternElement;
 import org.neo4j.util.matching.PatternMatch;
 import org.neo4j.util.matching.PatternMatcher;
 import org.neo4j.util.matching.PatternNode;
 
 /**
  * Default implementation of an {@link RdfStore}.
  */
 public class RdfStoreImpl implements RdfStore
 {
     private final NeoService neo;
     private final RepresentationStrategy representationStrategy;
 
     /**
      * @param neo the {@link NeoService}.
      * @param representationStrategy the {@link RepresentationStrategy}
      * to use when storing statements.
      */
     public RdfStoreImpl( NeoService neo,
         RepresentationStrategy representationStrategy )
     {
         this.neo = neo;
         this.representationStrategy = representationStrategy;
     }
 
     public void addStatements( CompleteStatement... statements )
     {
//        sysOutStatements( "add", statements );
         Transaction tx = neo.beginTx();
         try
         {
             for ( Statement statement : statements )
             {
                 AbstractRepresentation fragment = representationStrategy
                     .getAbstractRepresentation( statement );
                 getExecutor().addToNodeSpace( fragment );
             }
             tx.success();
         }
         finally
         {
             tx.finish();
         }
     }
 
     private RepresentationExecutor getExecutor()
     {
         return this.representationStrategy.getExecutor();
     }
     
 
     public Iterable<Statement> getStatements( WildcardStatement statement,
         boolean includeInferredStatements )
     {
         if ( weCanHandleStatement( statement ) )
         {
             // TODO: pseudo code below
             return graphMatchingFacade().getMatchingStatements(
                 representationStrategy.getAbstractRepresentation( statement ) );
         }
         throw new UnsupportedOperationException( "We can't handle get() for " +            
             "this statement: " + statement );
     }       
         
     private boolean weCanHandleStatement( WildcardStatement statement )
     {
         return false;
     }
 
     protected boolean wildcardPattern( WildcardStatement statement,
         boolean subjectWildcard, boolean predicateWildcard,
         boolean objectWildcard )
     {
         return valueIsWildcard( statement.getSubject() ) == subjectWildcard &&
             valueIsWildcard( statement.getPredicate() ) == predicateWildcard &&
             valueIsWildcard( statement.getObject() ) == objectWildcard;
     }
 
     private boolean valueIsWildcard( Value potentialWildcard )
     {
         return potentialWildcard instanceof Wildcard;
     }
     
     public Iterable<Statement> oldGetStatements( WildcardStatement statement,
         boolean includeInferredStatements )
     {
 //      S, null, null         : No
 //      S, P, null            : Yes
 //      null, null, O         : No
 //      null, P, O            : Yes (for objecttype)
       
 //      if ( theseAreNull( statementWithOptionalNulls, false, true, true ) )
 //      {
 //          
 //      }
 //      else if ( theseAreNull( statementWithOptionalNulls,
 //          false, false, true ) )
 //      {
 //      
 //      }
 //      else if ( theseAreNull( statementWithOptionalNulls,
 //          true, true, false ) )
 //      {
 //      }
 //      else if ( theseAreNull( statementWithOptionalNulls,
 //          true, false, false ) )
 //      {
 //          
 //      }
 //      String query = SparqlBuilder.getQuery( statementWithOptionalNulls );
       
         throw new UnsupportedOperationException();
     }
 
 
     private boolean theseAreNull( Statement statementWithOptionalNulls,
         boolean subjectIsNull, boolean predicateIsNull, boolean objectIsNull )
     {
         return objectComparesToNull( statementWithOptionalNulls.getSubject(),
             subjectIsNull )
             && objectComparesToNull( statementWithOptionalNulls.getPredicate(),
                 predicateIsNull )
             && objectComparesToNull( statementWithOptionalNulls.getObject(),
                 objectIsNull );
     }
 
     private boolean objectComparesToNull( Object object, boolean shouldBeNull )
     {
         return shouldBeNull ? object == null : object != null;
     }
 
     public void removeStatements( Statement statementWithOptionalNulls )
     {
         if ( !theseAreNull( statementWithOptionalNulls, false, false, false ) )
         {
             throw new UnsupportedOperationException( "Not yet implemented" );
         }
         removeStatementsSimple( statementWithOptionalNulls );
     }
     
    private void sysOutStatements( String what, Statement... statements )
    {
        for ( Statement statement : statements )
        {
            sysOutStatement( what, statement );
        }
    }
    
     private void sysOutStatement( String what, Statement statement )
     {
         StringBuffer contexts = new StringBuffer();
         for ( Context context : statement.getContexts() )
         {
             contexts.append( context.getUriAsString() + ", " );
         }
         System.out.println( what + ":" + statement.getSubject() +
             ", " + statement.getPredicate() + ", " + statement.getObject() +
             " | " + contexts.toString() );
     }
 
     private void removeStatementsSimple( Statement statement )
     {
 //        sysOutStatement( "remove", statement );
         Transaction tx = neo.beginTx();
         try
         {
             AbstractRepresentation fragment = representationStrategy
                 .getAbstractRepresentation( statement );
             getExecutor().removeFromNodeSpace( fragment );
             tx.success();
         }
         finally
         {
             tx.finish();
         }
     }
 
     private static interface GraphMatchingFacade
     {
         /**
          * Takes an abstract representation of a <i>single</i> statement with
          * optional wildcards, and returns all matching statements in the
          * node space.
          * @param oneStatementWithWildcards an abstract representaiton of a
          * single statement, potentially with wildcards
          * @return all statements that match
          * <code>oneStatementWithWildcards</code>
          */
         Iterable<Statement> getMatchingStatements( AbstractRepresentation
             oneStatementWithWildcards );
     }
 
     private GraphMatchingFacade graphMatchingFacade()
     {
         return new GraphMatchingFacade()
         {
             public Iterable<Statement> getMatchingStatements(
                 AbstractRepresentation statementRepresentation )
             {                
                 Map<PatternElement, AbstractElement>
                     patternToRepresentationMap =
                         buildPatternGraphFromAbstractRepresentation(
                             statementRepresentation );
                 
                 Iterable<PatternMatch> matches = runMatchingEngine(
                     figureOutPatternStartNode( patternToRepresentationMap ),
                     figureOutNeoStartNode( statementRepresentation ) );
                 
                 for ( PatternMatch match : matches )
                 {
                     // get abstract element for every pattern element
                     // get value for every abstract element
                     // construct statements
                 }
                 return null;
             }
 
             private Map<PatternElement, AbstractElement>
                 buildPatternGraphFromAbstractRepresentation(
                     AbstractRepresentation statementRepresentation )
             {
                 return null;
             }
 
             private PatternNode figureOutPatternStartNode(
                 Map<PatternElement, AbstractElement> map )
             {
                 return null;
             }
 
             private Iterable<PatternMatch> runMatchingEngine(
                 PatternNode patternStartNode, Node neoStartNode )
             {
                 return PatternMatcher.getMatcher().match( patternStartNode,
                     neoStartNode );
             }
 
             private Node figureOutNeoStartNode( AbstractRepresentation
                 statementRepresentation )
             {
                 // Resolve abstract nodes in representation to neo nodes
                 // through the executor
                 // Investigate meta model, check instance counts etc
                 // Return a smart start node
                 return null;
             }
         };
     }
 
     
 }
