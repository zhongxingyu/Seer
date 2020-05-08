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
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.util.regex.Pattern;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.neo4j.index.Neo4jTestCase;
 import org.neo4j.kernel.impl.batchinsert.BatchInserterImpl;
 
 public class TestBatchInsert
 {
     private BatchInserterImpl inserter;
     private LuceneIndexBatchInserterImpl index;
 
     private String getDbPath()
     {
         return "target/var/batch-insert";
     }
     
     @Before
     public void setUpBatchInserter() throws Exception
     {
         Neo4jTestCase.deleteFileOrDirectory( new File( getDbPath() ) );
         inserter = new BatchInserterImpl( getDbPath() );
     }
     
     @After
     public void tearDownBatchInserter()
     {
         if ( index != null )
         {
             index.shutdown();
             index = null;
         }
         inserter.shutdown();
     }
     
     @Test
     public void testSimpleBatchInsert()
     {
         index = new LuceneIndexBatchInserterImpl( inserter );
         long node = inserter.createNode( null );
         assertTrue( !index.getNodes( "test-key", 
             "test-value" ).iterator().hasNext() );
         index.index( node, "test-key", "test-value" );
         assertTrue( index.getNodes( "test-key", 
             "test-value" ).iterator().hasNext() );
     }
 
     @Test
     public void testSimpleFulltextBatchInsert()
     {
         index = new LuceneFulltextIndexBatchInserter( inserter );
         long node = inserter.createNode( null );
         assertTrue( !index.getNodes( "test-key", 
             "test-value" ).iterator().hasNext() );
         index.index( node, "test-key", "test-value" );
         assertTrue( index.getNodes( "test-key", 
             "test-value" ).iterator().hasNext() );
         
         String value = "A decent value for indexing";
         String key = "my key";
         index.index( node, key, value );
         for ( String word : value.split( Pattern.quote( " " ) ) )
         {
             assertTrue( index.getNodes( key, word ).iterator().hasNext() );
         }
         assertFalse( index.getNodes( key,
             "abcdefghijklmnop" ).iterator().hasNext() );
     }
     
     @Test
     public void testMoreFulltextBatchInsert()
     {
         index = new LuceneFulltextIndexBatchInserter( inserter );
         // Should be quite slow, i.e. don't build your code like this :)
         long time = System.currentTimeMillis();
         for ( int i = 0; i < 1000; i++ )
         {
             index.index( i, "mykey1", i );
             assertEquals( i, index.getSingleNode( "mykey1", i ) );
         }
         long slowTime = System.currentTimeMillis() - time;
         
         // Should be much faster
         time = System.currentTimeMillis();
         for ( int i = 0; i < 1000; i++ )
         {
             index.index( i, "mykey2", i );
         }
         index.optimize();
         for ( int i = 0; i < 1000; i++ )
         {
             assertEquals( i, index.getSingleNode( "mykey2", i ) );
         }
         long fastTime = System.currentTimeMillis() - time;
         assertTrue( fastTime < slowTime / 5 );
     }
     
     @Test
     public void testHmm() throws Exception
     {
         index = new LuceneIndexBatchInserterImpl( inserter );
         int titleIdStart = 0;
         for ( int i = 0; i < 100; i++ )
         {
             index.index( titleIdStart + i, "ID_TITLE", i );
             index.index( titleIdStart + i, "NLC", "something" );
             index.index( titleIdStart + i, "TITLE", "title" );
             index.index( titleIdStart + i, "TYPE", "type" );
         }
 
         int imageIdStart = 1000;
         for ( int i = 0; i < 100; i++ )
         {
             index.index( imageIdStart + i, "ID_IMAGE", i );
             index.index( imageIdStart + i, "NLC", "something" );
             index.index( imageIdStart + i, "FILENAME", "filename" + i );
             index.index( imageIdStart + i, "TYPE", "type" );
         }
         index.optimize();
         
         for ( int i = 0; i < 100; i++ )
         {
             long tail = index.getSingleNode( "ID_TITLE", i );
             long head = index.getSingleNode( "ID_IMAGE", i );
             assertTrue( "tail -1", tail != -1 );
             assertTrue( "head -1", head != -1 );
         }
     }
 }
