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
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import javax.transaction.xa.XAResource;
 import javax.transaction.xa.Xid;
 
 import junit.framework.TestCase;
 
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.index.IndexService;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 import org.neo4j.kernel.impl.transaction.XidImpl;
 
 public class TestRecovery extends TestCase
 {
     private void deleteFileOrDirectory( File file )
     {
         if ( !file.exists() )
         {
             return;
         }
         
         if ( file.isDirectory() )
         {
             for ( File child : file.listFiles() )
             {
                 deleteFileOrDirectory( child );
             }
         }
         else
         {
             file.delete();
         }
     }
     
     private String getNeoPath()
     {
         return "target/recovery";
     }
     
     private GraphDatabaseService newNeoService()
     {
         String path = getNeoPath();
         deleteFileOrDirectory( new File( path ) );
         return new EmbeddedGraphDatabase( path );
     }
     
     public void testRecovery() throws Exception
     {
         final GraphDatabaseService neo = newNeoService();
         final IndexService index = new LuceneIndexService( neo );
         
         neo.beginTx();
         Node node = neo.createNode();
         Random random = new Random();
         Thread stopper = new Thread()
         {
             @Override public void run()
             {
                 sleepNice( 1000 );
                 index.shutdown();
                 neo.shutdown();
             }
         };
         try
         {
             stopper.start();
             for ( int i = 0; i < 500; i++ )
             {
                 index.index( node, "" + random.nextInt(), random.nextInt() );
                 sleepNice( 10 );
             }
         }
         catch ( Exception e )
         {
             // Ok
         }
         
         sleepNice( 1000 );
         final GraphDatabaseService newNeo =
             new EmbeddedGraphDatabase( getNeoPath() );
         final IndexService newIndexService = new LuceneIndexService( newNeo );
         sleepNice( 1000 );
         newIndexService.shutdown();
         newNeo.shutdown();
     }
     
     private static void sleepNice( long time )
     {
         try
         {
             Thread.sleep( time );
         }
         catch ( InterruptedException e )
         {
             // Ok
         }
     }
     
     public void testReCommit()
     {
         GraphDatabaseService neo = newNeoService();
         IndexService idx = new LuceneIndexService( neo );
         Transaction tx = neo.beginTx();
         assertEquals( null, idx.getSingleNode( "test", "1" ) );
         Node refNode = neo.getReferenceNode();
         tx.finish();
         idx.shutdown();
         Map<Object,Object> params = new HashMap<Object,Object>();
         String luceneDir = getNeoPath() + "/lucene";
         params.put( "dir", luceneDir );
         try
         {
             LuceneDataSource xaDs = new LuceneDataSource( params );
             LuceneXaConnection xaC = (LuceneXaConnection) xaDs.getXaConnection();
             XAResource xaR = xaC.getXaResource();
             Xid xid = new XidImpl( new byte[1], new byte[1] );
             xaR.start( xid, XAResource.TMNOFLAGS );
             xaC.index( refNode, "test", "1" );
             xaR.end( xid, XAResource.TMSUCCESS );
             xaR.prepare( xid );
             xaR.commit( xid, false );
             copyLogicalLog( luceneDir + "/lucene.log.active", 
                 luceneDir + "/lucene.log.active.bak" );
             copyLogicalLog( luceneDir + "/lucene.log.1", 
                 luceneDir + "/lucene.log.1.bak" );
             // test recovery re-commit
             idx = new LuceneIndexService( neo );
             tx = neo.beginTx();
             assertEquals( refNode, idx.getSingleNode( "test", "1" ) );
             tx.finish();
             idx.shutdown();
             assertTrue( 
                 new File( luceneDir + "/lucene.log.active" ).delete() );
             // test recovery again on same log and only still only get 1 node
             copyLogicalLog( luceneDir + "/lucene.log.active.bak", 
                 luceneDir + "/lucene.log.active" );
             copyLogicalLog( luceneDir + "/lucene.log.1.bak", 
                 luceneDir + "/lucene.log.1" );
             idx = new LuceneIndexService( neo );
             tx = neo.beginTx();
             assertEquals( refNode, idx.getSingleNode( "test", "1" ) );
             tx.finish();
             idx.shutdown();
             neo.shutdown();
         }
         catch ( Exception e )
         {
             e.printStackTrace();
             fail( "" + e );
         }
         
     }
 
     private void copyLogicalLog( String name, String copy ) throws IOException
     {
         ByteBuffer buffer = ByteBuffer.allocate( 1024 );
         assertTrue( new File( name ).exists() );
         FileChannel source = new RandomAccessFile( name, "r" ).getChannel();
         assertTrue( !new File( copy ).exists() );
         FileChannel dest = new RandomAccessFile( copy, "rw" ).getChannel();
         int read = -1;
         do
         {
             read = source.read( buffer );
             buffer.flip();
             dest.write( buffer );
             buffer.clear();
         }
         while ( read == 1024 );
         source.close();
         dest.close();
     }
 }
