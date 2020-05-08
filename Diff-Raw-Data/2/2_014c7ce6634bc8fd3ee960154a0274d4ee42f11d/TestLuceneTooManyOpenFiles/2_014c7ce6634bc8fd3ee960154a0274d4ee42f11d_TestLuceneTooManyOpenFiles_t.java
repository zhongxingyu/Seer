 package org.neo4j.util.index;
 
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.neo4j.api.core.DynamicRelationshipType;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.util.NeoTestCase;
 
public abstract class TestLuceneTooManyOpenFiles extends NeoTestCase
 {
     private IndexService indexService;
     
     protected IndexService instantiateIndexService()
     {
         return new LuceneIndexService( neo() );
     }
     
     @Override
     protected void setUp() throws Exception
     {
         super.setUp();
         indexService = instantiateIndexService();
     }
     
     protected IndexService indexService()
     {
         return indexService;
     }
     
     @Override
     protected void beforeNeoShutdown()
     {
         indexService().shutdown();
     }
 
     public void testTooManyOpenFilesException() throws Exception
     {
         RelationshipType relType = DynamicRelationshipType.withName( "tmofe" );
         Node root = neo().createNode();
         AtomicInteger counter = new AtomicInteger();
         createChildren( root, 0, counter, relType );
         restartTx();
         
         // Now, do the tight loop which should trigger the error
         long startTime = System.currentTimeMillis();
         long endTime = startTime + 1000 * 15;
         int commitEvery = 20;
         int i = 0;
         for ( ; System.currentTimeMillis() < endTime; i++ )
         {
             Node node = indexService().getSingleNode( "tmofe_name", "Name " +
                 ( new Random().nextInt( counter.get() ) + 1 ) );
             indexService().removeIndex( node, "tmofe_title",
                 node.getProperty( "title" ) );
             node.setProperty( "title", "Something " + i );
             indexService().index( node, "tmofe_title",
                 node.getProperty( "title" ) );
             if ( i > 0 && i % commitEvery == 0 )
             {
                 restartTx();
             }
         }
 //        System.out.println( "test ran fine (" + i + ")" );
         
         deleteDownwards( root, relType );
         root.delete();
 //        indexService().removeIndex( "tmofe_name" );
 //        indexService().removeIndex( "tmofe_type" );
 //        indexService().removeIndex( "tmofe_sex" );
 //        indexService().removeIndex( "tmofe_title" );
     }
 
     private void deleteDownwards( Node root, RelationshipType relType )
     {
         for ( Relationship rel : root.getRelationships( relType ) )
         {
             Node child = rel.getEndNode();
             rel.delete();
             deleteDownwards( child, relType );
             child.delete();
         }
     }
 
     private void createChildren( Node node, int level, AtomicInteger counter,
         RelationshipType relType )
     {
         if ( level > 3 )
         {
             return;
         }
         
         Random random = new Random();
         int numChildren = random.nextInt( 2 ) + 2;
         for ( int i = 0; i < numChildren; i++ )
         {
             Node child = neo().createNode();
             node.createRelationshipTo( child, relType );
             child.setProperty( "name", "Name " + counter.incrementAndGet() );
             indexService().index( child, "tmofe_name",
                 child.getProperty( "name" ) );
             child.setProperty( "type", "blabla" );
             indexService().index( child, "tmofe_type",
                 child.getProperty( "type" ) );
             child.setProperty( "title", "Nothing" );
             indexService().index( child, "tmofe_title",
                 child.getProperty( "title" ) );
             child.setProperty( "sex", random.nextBoolean() ?
                 "male" : "female" );
             indexService().index( child, "tmofe_sex",
                 child.getProperty( "sex" ) );
             createChildren( child, level + 1, counter, relType );
         }
     }
 }
 
