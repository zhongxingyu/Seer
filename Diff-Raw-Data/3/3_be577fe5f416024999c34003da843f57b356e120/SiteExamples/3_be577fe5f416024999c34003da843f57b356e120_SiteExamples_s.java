 package examples;
 
 import java.io.File;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.index.lucene.LuceneIndexService;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 import org.neo4j.rdf.fulltext.FulltextIndex;
 import org.neo4j.rdf.fulltext.QueryResult;
 import org.neo4j.rdf.fulltext.SimpleFulltextIndex;
 import org.neo4j.rdf.model.CompleteStatement;
 import org.neo4j.rdf.model.Context;
 import org.neo4j.rdf.model.Literal;
 import org.neo4j.rdf.model.Uri;
 import org.neo4j.rdf.model.Wildcard;
 import org.neo4j.rdf.model.WildcardStatement;
 import org.neo4j.rdf.store.Neo4jTestCase;
 import org.neo4j.rdf.store.RdfStore;
 import org.neo4j.rdf.store.VerboseQuadStore;
 
 public class SiteExamples
 {
     @BeforeClass
     public static void clear()
     {
         Neo4jTestCase.deleteFileOrDirectory( new File( "target/var/examples" ) );
     }
     
     @Test
     public void rdfStoreUsage()
     {
         // START SNIPPET: rdfStoreUsage
         GraphDatabaseService graphDb = new EmbeddedGraphDatabase( "target/var/examples" );
         LuceneIndexService indexService = new LuceneIndexService( graphDb );
         RdfStore store = new VerboseQuadStore( graphDb, indexService );
         
         CompleteStatement firstStatement = new CompleteStatement(
                 new Uri( "http://neo4j.org/mattias" ),
                 new Uri( "http://neo4j.org/knows" ),
                 new Uri( "http://neo4j.org/emil" ), Context.NULL );
         CompleteStatement secondStatement = new CompleteStatement(
                 new Uri( "http://neo4j.org/mattias" ),
                 new Uri( "http://neo4j.org/name" ),
                 new Literal( "Mattias" ), Context.NULL );
         store.addStatements( firstStatement, secondStatement );
             
         for ( CompleteStatement statement : store.getStatements( new WildcardStatement(
                 new Uri( "http://neo4j.org/mattias" ),
                 new Wildcard( "predicate" ),
                 new Wildcard( "object" ), Context.NULL ), false ) )
         {
             System.out.println( "Found statement " + statement );
         }
         // END SNIPPET: rdfStoreUsage
     }
     
     @Test
     public void fulltextIndexing()
     {
         // START SNIPPET: fulltextIndexing
         GraphDatabaseService graphDb = new EmbeddedGraphDatabase( "target/var/examples" );
         LuceneIndexService indexService = new LuceneIndexService( graphDb );
         FulltextIndex fulltextIndex = new SimpleFulltextIndex( graphDb,
                 new File( "target/var/examples/fulltext-index" ) );
         RdfStore store = new VerboseQuadStore( graphDb, indexService, null, fulltextIndex );
         
         // ...add some statements
         
         for ( QueryResult searchHit : store.searchFulltext( "rdf AND store" ) )
         {
             System.out.println( searchHit.getStatement() );
         }
         // END SNIPPET: fulltextIndexing
                 
         store.shutDown();
         indexService.shutdown();
         graphDb.shutdown();
     }
 }
