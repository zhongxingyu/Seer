 package org.neo4j.rdf.store;
 
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.rdf.model.Statement;
 import org.neo4j.rdf.store.representation.AbstractStatementRepresentation;
 import org.neo4j.rdf.store.representation.AsrExecutor;
 import org.neo4j.rdf.store.representation.RdfRepresentationStrategy;
 
 /**
  * Default implementation of an {@link RdfStore}.
  */
 public class RdfStoreImpl implements RdfStore
 {
     private final NeoService neo;
     private final RdfRepresentationStrategy representationStrategy;
 
     /**
      * @param neo the {@link NeoService}.
      * @param representationStrategy the {@link RdfRepresentationStrategy}
      * to use when storing statements.
      */
     public RdfStoreImpl( NeoService neo,
         RdfRepresentationStrategy representationStrategy )
     {
         this.neo = neo;
         this.representationStrategy = representationStrategy;
     }
 
     public void addStatement( Statement statement )
     {
         Transaction tx = neo.beginTx();
         try
         {
             AbstractStatementRepresentation fragment = representationStrategy
                 .getAbstractRepresentation( statement );
             getExecutor().addToNodeSpace( fragment );
             tx.success();
         }
         finally
         {
             tx.finish();
         }
     }
 
     private AsrExecutor getExecutor()
     {
         return this.representationStrategy.getAsrExecutor();
     }
 
     public Iterable<Statement> getStatements(
         Statement statementWithOptionalNulls,
         boolean includeInferredStatements )
     {
 //        S, null, null         : No
 //        S, P, null            : Yes
 //        null, null, O         : No
 //        null, P, O            : Yes (for objecttype)
         
 //        if ( theseAreNull( statementWithOptionalNulls, false, true, true ) )
 //        {
 //            
 //        }
 //        else if ( theseAreNull( statementWithOptionalNulls,
 //            false, false, true ) )
 //        {
 //        
 //        }
 //        else if ( theseAreNull( statementWithOptionalNulls,
 //            true, true, false ) )
 //        {
 //        }
 //        else if ( theseAreNull( statementWithOptionalNulls,
 //            true, false, false ) )
 //        {
 //            
 //        }
 //        String query = SparqlBuilder.getQuery( statementWithOptionalNulls );
         
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
 
     private void removeStatementsSimple( Statement statement )
     {
 //        StringBuffer contexts = new StringBuffer();
 //        for ( Context context : statement.getContexts() )
 //        {
 //            contexts.append( context.getUriAsString() + ", " );
 //        }
 //        System.out.println( "removeStatement:" + statement.getSubject() +
 //            ", " + statement.getPredicate() + ", " + statement.getObject() +
 //            " | " + contexts.toString() );
         Transaction tx = neo.beginTx();
         try
         {
             AbstractStatementRepresentation fragment = representationStrategy
                 .getAbstractRepresentation( statement );
             getExecutor().removeFromNodeSpace( fragment );
             tx.success();
         }
         finally
         {
             tx.finish();
         }
     }
 }
