 package org.neo4j.rdf.store;
 
 import org.neo4j.rdf.fulltext.QueryResult;
 import org.neo4j.rdf.model.CompleteStatement;
 import org.neo4j.rdf.model.Context;
 import org.neo4j.rdf.model.Literal;
 import org.neo4j.rdf.model.Uri;
 import org.neo4j.rdf.model.Wildcard;
 import org.neo4j.rdf.model.WildcardStatement;
 import org.neo4j.rdf.store.representation.standard.AbstractUriBasedExecutor;
 
 public class TestVerboseQuadStore extends QuadStoreAbstractTestCase
 {
     public void testIt()
     {
         CompleteStatement mattiasKnowsEmilPublic =
             completeStatement(
                 TestUri.MATTIAS,
                 TestUri.FOAF_KNOWS,
                 TestUri.EMIL,
                 TestUri.MATTIAS_PUBLIC_GRAPH );
         CompleteStatement mattiasNamePublic =
             completeStatement(
                 TestUri.MATTIAS,
                 TestUri.FOAF_NICK,
                 new Literal( "Mattias Persson",
                     new Uri( "http://www.w3.org/2001/XMLSchema#string" ), "sv" ),
                     TestUri.MATTIAS_PUBLIC_GRAPH );
         CompleteStatement mattiasNamePrivate =
             completeStatement(
                 TestUri.MATTIAS,
                 TestUri.FOAF_NICK,
                 new Literal( "Mattias Persson",
                     new Uri( "http://www.w3.org/2001/XMLSchema#string" ), "en" ),
                     TestUri.MATTIAS_PRIVATE_GRAPH );
         CompleteStatement mattiasKnowsEmilPrivate =
             completeStatement(
                 TestUri.MATTIAS,
                 TestUri.FOAF_KNOWS,
                 TestUri.EMIL,
                 TestUri.MATTIAS_PRIVATE_GRAPH );
         
         addStatements(
             mattiasKnowsEmilPublic,
             mattiasKnowsEmilPrivate,
             mattiasNamePublic,
             mattiasNamePrivate );
         
         assertResult(
             wildcardStatement(
                 new Wildcard( "s" ),
                 new Wildcard( "p" ),
                 new Wildcard( "o" ),
                 new Context( TestUri.MATTIAS_PUBLIC_GRAPH.toUri().
                     getUriAsString() ) ),
                     
             mattiasKnowsEmilPublic,
             mattiasNamePublic );
         
         assertResult(
             wildcardStatement(
                 new Wildcard( "s" ),
                 new Wildcard( "p" ),
                 new Wildcard( "o" ),
                 new Wildcard( "g" ) ),
                 
             mattiasKnowsEmilPublic,
             mattiasKnowsEmilPrivate,
             mattiasNamePublic,
             mattiasNamePrivate );
         
         assertResult(
             wildcardStatement(
                 new Wildcard( "s" ),
                 TestUri.FOAF_KNOWS.toUri(),
                 new Wildcard( "o" ),
                 new Wildcard( "g" ) ),
                 
             mattiasKnowsEmilPublic,
             mattiasKnowsEmilPrivate );
         
         deleteEntireNodeSpace();
     }
     
     public void testType() throws Exception
     {
         Uri rdfType = new Uri( AbstractUriBasedExecutor.RDF_TYPE_URI );
         CompleteStatement mattiasKnowsEmilPublic =
             completeStatement(
                 TestUri.MATTIAS,
                 TestUri.FOAF_KNOWS,
                 TestUri.EMIL,
                 TestUri.MATTIAS_PUBLIC_GRAPH );
         CompleteStatement mattiasTypePerson =
             new CompleteStatement(
                 TestUri.MATTIAS.toUri(),
                 rdfType,
                 TestUri.PERSON.toUri(),
                 Context.NULL, null );
         addStatements(
             mattiasTypePerson,
             mattiasKnowsEmilPublic );
         
         assertResult(
             wildcardStatement(
                 TestUri.MATTIAS.toUri(),
                 rdfType,
                 new Wildcard( "o" ),
                 new Wildcard( "g" ) ),
                 
             mattiasTypePerson );
         
         deleteEntireNodeSpace();
     }
     
     public void testDuplicateWithLiterals() throws Exception
     {
         CompleteStatement mattiasTypePerson =
             completeStatement(
                 TestUri.MATTIAS,
                 TestUri.RDF_TYPE,
                 TestUri.PERSON,
                 TestUri.MATTIAS_PUBLIC_GRAPH );
         CompleteStatement mattiasNamePublic =
             completeStatement(
                 TestUri.MATTIAS,
                 TestUri.FOAF_NICK,
                 "Mattias Persson",
                 TestUri.MATTIAS_PUBLIC_GRAPH );
         
         addStatements( mattiasTypePerson, mattiasNamePublic );
         
         restartTx();
         
         assertResult(
             wildcardStatement(
                 new Wildcard( "s" ),
                 new Wildcard( "p" ),
                 new Literal( "Mattias Persson" ),
                 new Wildcard( "g" ) ),
                 
             mattiasNamePublic );
         deleteEntireNodeSpace();
     }
     
     public void testFulltextSearch() throws Exception
     {
         CompleteStatement mattiasTypePerson =
             completeStatement(
                 TestUri.MATTIAS,
                 TestUri.RDF_TYPE,
                 TestUri.PERSON,
                 TestUri.MATTIAS_PUBLIC_GRAPH );
         CompleteStatement mattiasNamePublic =
             completeStatement(
                 TestUri.MATTIAS,
                 TestUri.FOAF_NICK,
                 "Persson",
                 TestUri.MATTIAS_PUBLIC_GRAPH );
         CompleteStatement mattiasNickPublic =
             completeStatement(
                 TestUri.MATTIAS,
                 TestUri.FOAF_NAME,
                 "Mattias Persson",
                 TestUri.MATTIAS_PUBLIC_GRAPH );
         
         addStatements( mattiasTypePerson, mattiasNamePublic,
             mattiasNickPublic );
         
         restartTx();
         waitForFulltextIndex();
         
         Iterable<QueryResult> queryResult =
             this.store().searchFulltext( " Mattias  Persson" );
         int counter = 0;
         for ( QueryResult oneResult : queryResult )
         {
             counter++;
             assertTrue( oneResult.getSnippet().contains( "Persson" ) );
         }
         assertEquals( 2, counter );
         
         queryResult = this.store().searchFulltext(
             "  \t \n Mattias  AND\tPersson \t    " );
         counter = 0;
         for ( QueryResult oneResult : queryResult )
         {
             counter++;
             assertEquals( "Mattias Persson", ( ( Literal )
                 oneResult.getStatement().getObject() ).getValue() );
             assertTrue( oneResult.getSnippet().contains( "Mattias" ) );
             assertTrue( oneResult.getSnippet().contains( "Persson" ) );
         }
         assertEquals( 1, counter );
 
         queryResult = this.store().searchFulltext( "\tpersson  " );
         counter = 0;
         for ( QueryResult oneResult : queryResult )
         {
             counter++;
             assertTrue( oneResult.getSnippet().contains( "Persson" ) );
         }
         assertEquals( 2, counter );
         
        queryResult = this.store().searchFulltext( "\tpers*  " );
        counter = 0;
        for ( QueryResult oneResult : queryResult )
        {
            counter++;
            assertTrue( oneResult.getSnippet().contains( "Persson" ) );
        }
        assertEquals( 2, counter );
        
         removeStatements( new WildcardStatement( mattiasNamePublic ) );
         removeStatements( new WildcardStatement( mattiasNickPublic ) );
         restartTx();
         
         waitForFulltextIndex();
         assertFalse( store().searchFulltext( "Persson" ).iterator().hasNext() );
         
         deleteEntireNodeSpace();
     }
     
     public void testReindexFulltextIndex() throws Exception
     {
         CompleteStatement mattiasTypePerson =
             completeStatement(
                 TestUri.MATTIAS,
                 TestUri.RDF_TYPE,
                 TestUri.PERSON,
                 TestUri.MATTIAS_PUBLIC_GRAPH );
         CompleteStatement mattiasNickPublic =
             completeStatement(
                 TestUri.MATTIAS,
                 TestUri.FOAF_NICK,
                 "Matte",
                 TestUri.MATTIAS_PUBLIC_GRAPH );
         CompleteStatement mattiasNamePublic =
             completeStatement(
                 TestUri.MATTIAS,
                 TestUri.FOAF_NAME,
                 "Mattias Persson",
                 TestUri.MATTIAS_PUBLIC_GRAPH );
         CompleteStatement emilNamePublic =
             completeStatement(
                 TestUri.EMIL,
                 TestUri.FOAF_NAME,
                 "Emil Eifrém",
                 TestUri.EMIL_PUBLIC_GRAPH );
         
         addStatements( mattiasTypePerson, mattiasNickPublic,
             mattiasNamePublic, emilNamePublic );
         restartTx();
         waitForFulltextIndex();
         
         ( ( RdfStoreImpl ) store() ).getFulltextIndex().clear();
         assertFalse( store().searchFulltext( "Persson" ).iterator().hasNext() );
         ( ( RdfStoreImpl ) store() ).reindexFulltextIndex();
         waitForFulltextIndex();
         Iterable<QueryResult> queryResult =
             this.store().searchFulltext( "Persson" );
         int counter = 0;
         for ( QueryResult oneResult : queryResult )
         {
             counter++;
             assertEquals( "Mattias Persson", ( ( Literal )
                 oneResult.getStatement().getObject() ).getValue() );
         }
         assertEquals( 1, counter );
         
         queryResult = this.store().searchFulltext( "Emil" );
         counter = 0;
         for ( QueryResult oneResult : queryResult )
         {
             counter++;
             assertEquals( "Emil Eifrém", ( ( Literal )
                 oneResult.getStatement().getObject() ).getValue() );
         }
         assertEquals( 1, counter );
         
         deleteEntireNodeSpace();
     }
     
     public void testMetadata() throws Exception
     {
         CompleteStatement mattiasTypePerson =
             completeStatement(
                 TestUri.MATTIAS,
                 TestUri.RDF_TYPE,
                 TestUri.PERSON,
                 TestUri.MATTIAS_PUBLIC_GRAPH );
         CompleteStatement mattiasNickPublic =
             completeStatement(
                 TestUri.MATTIAS,
                 TestUri.FOAF_NICK,
                 "Matte",
                 TestUri.MATTIAS_PUBLIC_GRAPH );
         
         addStatements( mattiasTypePerson, mattiasNickPublic );
         CompleteStatement addedNick = store().getStatements(
             new WildcardStatement( mattiasNickPublic ),
             false ).iterator().next();
         String key1 = "testKey1";
         String key2 = "testKey2";
         Literal literal1 = new Literal( "A test value" );
         Uri literal2Uri = new Uri( "http://a-date.com" );
         Literal literal2 = new Literal( "2008-10-10", literal2Uri, "en" );
         assertFalse( addedNick.getMetadata().has( key1 ) );
         assertFalse( addedNick.getMetadata().has( key2 ) );
         addedNick.getMetadata().set( key1, literal1 );
         assertEquals( literal1, addedNick.getMetadata().get( key1 ) );
         assertFalse( addedNick.getMetadata().has( key2 ) );
         addedNick.getMetadata().set( key2, literal2 );
         assertEquals( literal2, addedNick.getMetadata().get( key2 ) );
         assertEquals( literal2.getDatatype(),
             addedNick.getMetadata().get( key2 ).getDatatype() );
         assertEquals( literal2.getLanguage(),
             addedNick.getMetadata().get( key2 ).getLanguage() );
         addedNick.getMetadata().remove( key1 );
         assertFalse( addedNick.getMetadata().has( key1 ) );
         deleteEntireNodeSpace();
     }
 }
