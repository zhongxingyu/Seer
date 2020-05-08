 /*
  * Copyright (c) 2002-2009 "Neo Technology,"
  *     Network Engine for Objects in Lund AB [http://neotechnology.com]
  *
  * This file is part of Neo4j.
  * 
  * Neo4j is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.neo4j.index.lucene;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.junit.Ignore;
 import org.junit.Test;
 import org.neo4j.graphdb.Node;
 import org.neo4j.index.IndexHits;
 import org.neo4j.index.IndexService;
 import org.neo4j.index.Neo4jWithIndexTestCase;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 
 public class TestLuceneIndexingService extends Neo4jWithIndexTestCase
 {
     @Override
     protected IndexService instantiateIndex()
     {
         return new LuceneIndexService( graphDb() );
     }
     
     @Test
     public void testSimple()
     {
         Node node1 = graphDb().createNode();
         
         assertTrue( !index().getNodes( "a_property", 
             1 ).iterator().hasNext() );
 
         index().index( node1, "a_property", 1 );
         
         IndexHits<Node> hits = index().getNodes( "a_property", 1 );
         Iterator<Node> itr = hits.iterator();
         assertEquals( node1, itr.next() );
         assertEquals( 1, hits.size() );
         assertTrue( !itr.hasNext() );
         
         index().removeIndex( node1, "a_property", 1 );
         assertTrue( !index().getNodes( "a_property", 
             1 ).iterator().hasNext() );
 
         index().index( node1, "a_property", 1 );
         Node node2 = graphDb().createNode();
         index().index( node2, "a_property", 1 );
         
         hits = index().getNodes( "a_property", 1 );
         itr = hits.iterator();
         assertTrue( itr.next() != null );
         assertTrue( itr.next() != null );
         assertTrue( !itr.hasNext() );
         assertTrue( !itr.hasNext() );       
         assertEquals( 2, hits.size() );
         
         index().removeIndex( node1, "a_property", 1 );
         index().removeIndex( node2, "a_property", 1 );
         assertTrue( !index().getNodes( "a_property", 
             1 ).iterator().hasNext() );
         itr = index().getNodes( "a_property", 1 ).iterator();
         assertTrue( !itr.hasNext() );
         restartTx();
         node1.delete();
         node2.delete();
     }
     
     @Test
     public void testMultipleAdd()
     {
         Node node = graphDb().createNode();
         index().index( node, "a_property", 3 );
         restartTx();
         index().index( node, "a_property", 3 );
         restartTx();
         index().removeIndex( node, "a_property", 3 );
         restartTx();
         assertTrue( index().getSingleNode( "a_property", 3 ) == null );
     }
     
     @Test
     public void testCaching()
     {
         String key = "prop";
         Object value = 10;
         assertNull( ( (LuceneIndexService) index() ).getEnabledCacheSize( key ) );
         ( ( LuceneIndexService ) index() ).enableCache( key, 1000 );
         assertEquals( ( Integer ) 1000,
                 ( (LuceneIndexService) index() ).getEnabledCacheSize( key ) );
         Node node1 = graphDb().createNode();
         index().index( node1, key, value );
         index().getNodes( key, value );
         restartTx( false );
         
         Node node2 = graphDb().createNode();
         index().getNodes( key, value );
         index().index( node2, key, value );
         index().getNodes( key, value );
         restartTx();
         
         Node node3 = graphDb().createNode();
         index().getNodes( key, value );
         index().index( node3, key, value );
         index().getNodes( key, value );
         restartTx( false );
         
         Node node4 = graphDb().createNode();
         index().getNodes( key, value );
         index().index( node4, key, value );
         index().getNodes( key, value );
         restartTx();
         
         assertCollection( asCollection( index().getNodes( key, value ) ),
             node2, node4 );
         
         index().removeIndex( node2, key, value );
         index().removeIndex( node4, key, value );
         node2.delete();
         node4.delete();
     }
     
     @Test
     public void testRollback()
     {
         Node node1 = graphDb().createNode();
         Node node2 = graphDb().createNode();
         restartTx();
         index().index( node1, "a_property", 3 );
         assertEquals( node1, index().getSingleNode( "a_property", 3 ) );
         restartTx( false );
         assertEquals( null, index().getSingleNode( "a_property", 3 ) );
         index().index( node2, "a_property", 3 );
         assertEquals( node2, index().getSingleNode( "a_property", 3 ) );
         restartTx();
         assertEquals( node2, index().getSingleNode( "a_property", 3 ) );
     }
     
     @Test
     public void testDoubleIndexing() throws Exception
     {
         Node node = graphDb().createNode();
         index().index( node, "double", "value" );
         restartTx();
         index().index( node, "double", "value" );
         restartTx();
         Set<Node> hits = new HashSet<Node>();
         for ( Node hit : index().getNodes( "double", "value" ) )
         {
             assertTrue( hits.add( hit ) );
         }
         node.delete();
     }
     
     @Test
     @Ignore
     public void testChangeValueBug() throws Exception
     {
         Node andy = graphDb().createNode();
         Node larry = graphDb().createNode();
 
         andy.setProperty( "name", "Andy Wachowski" );
         andy.setProperty( "title", "Director" );
         // Deliberately set Larry's name wrong
         larry.setProperty( "name", "Andy Wachowski" );
         larry.setProperty( "title", "Director" );
         index().index( andy, "name", andy.getProperty( "name" ) );
         index().index( andy, "title", andy.getProperty( "title" ) );
         index().index( larry, "name", larry.getProperty( "name" ) );
         index().index( larry, "title", larry.getProperty( "title" ) );
     
         // Correct Larry's name
         index().removeIndex( larry, "name",
             larry.getProperty( "name" ) );
         larry.setProperty( "name", "Larry Wachowski" );
         index().index( larry, "name",
             larry.getProperty( "name" ) );
     
         assertCollection( asCollection( index().getNodes(
             "name", "Andy Wachowski" ) ), andy );
         assertCollection( asCollection( index().getNodes(
             "name", "Larry Wachowski" ) ), larry );
     }
     
     @Test
     public void testGetNodesBug() throws Exception
     {
         Node node1 = graphDb().createNode();
         Node node2 = graphDb().createNode();
         String key = "getnodesbug";
         ( ( LuceneIndexService ) index() ).enableCache( key, 100 );
         index().index( node1, key, "value" );
         index().index( node2, key, "value" );
         restartTx();
         
         assertCollection( asCollection(
             index().getNodes( key, "value" ) ), node1, node2 );
         // Now that value is cached
         index().removeIndex( node1, key, "value" );
         assertCollection( asCollection(
             index().getNodes( key, "value" ) ), node2 );
         index().removeIndex( node2, key, "value" );
         node1.delete();
         node2.delete();
     }
     
     @Test
     public void testRemoveAll() throws Exception
     {
         Node node1 = graphDb().createNode();
         Node node2 = graphDb().createNode();
         
         String key = "removeall";
         index().index( node1, key, "value1" );
         index().index( node1, key, "value2" );
         index().index( node2, key, "value1" );
         index().index( node2, key, "value2" );
         restartTx();
         assertCollection( asCollection(
             index().getNodes( key, "value1" ) ), node1, node2 );
         index().removeIndex( node1, key );
         index().index( node1, key, "value2" );
         assertCollection( asCollection(
             index().getNodes( key, "value1" ) ), node2 );
         assertCollection( asCollection(
             index().getNodes( key, "value2" ) ), node1, node2 );
         index().index( node1, key, "value1" );
         restartTx();
         index().removeIndex( key );
         assertCollection( asCollection(
             index().getNodes( key, "value2" ) ) );
         index().index( node1, key, "value1" );
         index().index( node2, key, "value2" );
         assertCollection( asCollection(
             index().getNodes( key, "value1" ) ), node1 );
         restartTx();
         index().removeIndex( key );
         assertCollection( asCollection(
             index().getNodes( key, "value1" ) ) );
         assertCollection( asCollection(
             index().getNodes( key, "value2" ) ) );
         
         node2.delete();
         node1.delete();
     }
     
     @Test
     public void testRemoveAllWithCache() throws Exception
     {
         Node node1 = graphDb().createNode();
         String key = "removeallc";
         ( (LuceneIndexService) index() ).enableCache( key, 1000 );
         index().index( node1, key, "value1" );
         index().index( node1, key, "value2" );
         restartTx();
         assertEquals( node1, index().getSingleNode( key, "value1" ) );
         assertEquals( node1, index().getSingleNode( key, "value2" ) );
         index().removeIndex( node1, key );
         assertNull( index().getSingleNode( key, "value1" ) );
         assertNull( index().getSingleNode( key, "value2" ) );
         restartTx();
         assertNull( index().getSingleNode( key, "value1" ) );
         assertNull( index().getSingleNode( key, "value2" ) );
         node1.delete();
     }
     
     @Test
     public void testIndexLargeString() throws Exception
     {
         Node node1 = graphDb().createNode();
         byte[] data = new byte[10*1024*1024];
         String value = new String( data );
         index().index( node1, "large_string", value );
         restartTx();
 //      this will not work            
 //        assertEquals( node1, 
 //                index().getSingleNode( "large_string", value ) );
         index().removeIndex( node1, "large_string" );
         node1.delete();
     }
 
     @Ignore
     @Test
     public void testDifferentTypesWithSameValueIssue()
     {
         String key = "prop";
         Integer valueAsInt = 10;
         String valueAsString = "10";
         
         Node node1 = graphDb().createNode();
         index().index( node1, key, valueAsInt );
         Node node2 = graphDb().createNode();
         index().index( node2, key, valueAsString );
         
         assertCollection( index().getNodes( key, valueAsInt ), node1 );
         assertCollection( index().getNodes( key, valueAsString ), node2 );
         
         restartTx();
         
         assertCollection( index().getNodes( key, valueAsInt ), node1 );
         assertCollection( index().getNodes( key, valueAsString ), node2 );
         
         index().removeIndex( node1, key, valueAsInt );
         index().removeIndex( node2, key, valueAsString );
         
         node2.delete();
         node1.delete();
     }
 
     @Ignore
     @Test
     public void testInsertionSpeed()
     {
         Node node = graphDb().createNode();
         long t = System.currentTimeMillis();
         for ( int i = 0; i < 5000000; i++ )
         {
             index().index( node, "yeah", "value" + i );
             if ( i % 100000 == 0 )
             {
                 restartTx();
                 System.out.print( "." );
             }
         }
         finishTx( true );
         System.out.println( "insert:" + (System.currentTimeMillis() - t) );
         
         t = System.currentTimeMillis();
         for ( int i = 0; i < 100; i++ )
         {
             for ( Node n : index().getNodes( "yeah", "value" + i ) )
             {
             }
         }
         System.out.println( "get:" + (System.currentTimeMillis() - t) );
     }
     
     @Test
     public void testArrayProperties()
     {
         Node node = graphDb().createNode();
         int[] integers = new int[] { 10, 1034, 4321 };
         String[] strings = new String[] { "Array strings can be indexed",
                 "It is a more expected behaviour",
                 "Than not to" };
         index().index( node, "integer", integers );
         index().index( node, "string", strings );
         
         assertEquals( node, index().getSingleNode( "integer", 1034 ) );
         assertNull( index().getSingleNode( "integer", 111111 ) );
         assertEquals( node, index().getSingleNode( "string", strings[0] ) );
         assertEquals( node, index().getSingleNode( "string", strings[1] ) );
         assertEquals( node, index().getSingleNode( "string", strings[2] ) );
         assertNull( index().getSingleNode( "string", "Something else" ) );
         restartTx();
         assertEquals( node, index().getSingleNode( "integer", 1034 ) );
         assertNull( index().getSingleNode( "integer", 111111 ) );
         assertEquals( node, index().getSingleNode( "string", strings[0] ) );
         assertEquals( node, index().getSingleNode( "string", strings[1] ) );
         assertEquals( node, index().getSingleNode( "string", strings[2] ) );
         assertNull( index().getSingleNode( "string", "Something else" ) );
         
         index().removeIndex( node, "integer", integers );
         index().removeIndex( node, "string", strings );
         assertNull( index().getSingleNode( "integer", 1034 ) );
         assertNull( index().getSingleNode( "string", strings[1] ) );
         node.delete();
     }
     
     @Test
     public void testReadWithoutTx()
     {
         Node node1 = graphDb().createNode();
         Node node2 = graphDb().createNode();
         index().index( node1, "wo-tx", "value1" );
         index().index( node2, "wo-tx", "value2" );
         finishTx( true );
         
         assertEquals( node1, index().getSingleNode( "wo-tx", "value1" ) );
         assertEquals( node2, index().getSingleNode( "wo-tx", "value2" ) );
         assertCollection( index().getNodes( "wo-tx", "value1" ), node1 );
         assertCollection( index().getNodes( "wo-tx", "value2" ), node2 );
         
         beginTx();
         index().removeIndex( "wo-tx" );
         node1.delete();
         node2.delete();
     }
     
     protected String dirName()
     {
         return "lucene";
     }
 
     @Test
     public void makeSureDirectoryIsRemovedInRemoveAll()
     {
         Node node = graphDb().createNode();
         String key = "delall";
         index().index( node, key, "value" );
         restartTx();
         File dir = new File( new File( new File(
                 ( (EmbeddedGraphDatabase) graphDb() ).getStoreDir() ), dirName() ), key );
         assertTrue( dir.exists() );
         index().removeIndex( key );
         restartTx();
         assertFalse( dir.exists() );
     }
 }
