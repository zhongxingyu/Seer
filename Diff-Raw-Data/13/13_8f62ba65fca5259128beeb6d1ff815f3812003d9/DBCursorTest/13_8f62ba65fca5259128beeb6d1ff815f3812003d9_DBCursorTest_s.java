 /**
  *      Copyright (C) 2008 10gen Inc.
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package com.mongodb;
 
 import java.util.*;
 import java.util.regex.*;
 import java.io.IOException;
 
 import org.testng.annotations.Test;
 
 import com.mongodb.util.*;
 
 public class DBCursorTest extends TestCase {
 
     public DBCursorTest()
         throws IOException , MongoException {
         super();
         _db = new Mongo( "127.0.0.1" ).getDB( "cursortest" );
     }
 
     @Test(groups = {"basic"})
     public void testCount() {
         try {
             DBCollection c = _db.getCollection("test");
             c.drop();
             
             assertEquals(c.find().count(), 0);
         
             BasicDBObject obj = new BasicDBObject();
             obj.put("x", "foo");
             c.insert(obj);
 
             assertEquals(c.find().count(), 1);
 
             BasicDBObject query = new BasicDBObject();
 
             
             BasicDBObject fields = new BasicDBObject();
             fields.put("y", 1);
             assertEquals(c.find(query,fields).count(), 0);
             
 
             query.put("x", "bar");
             assertEquals(c.find(query).count(), 0);
         }
         catch (MongoException e) {
             assertTrue(false);
         }
     }
 
     @Test(groups = {"basic"})
     public void testSnapshot() {
         DBCollection c = _db.getCollection("snapshot1");
         c.drop();
         for ( int i=0; i<100; i++ )
             c.save( new BasicDBObject( "x" , i ) );
         assertEquals( 100 , c.find().count() );
         assertEquals( 100 , c.find().toArray().size() );
         assertEquals( 100 , c.find().snapshot().count() );
         assertEquals( 100 , c.find().snapshot().toArray().size() );
         assertEquals( 100 , c.find().snapshot().limit(50).count() );
         assertEquals( 50 , c.find().snapshot().limit(50).toArray().size() );
     }
 
     @Test
     public void testBig(){
         DBCollection c = _db.getCollection("big1");
         c.drop();
 
         String bigString;
         {
             StringBuilder buf = new StringBuilder( 16000 );
             for ( int i=0; i<16000; i++ )
                 buf.append( "x" );
             bigString = buf.toString();
         }
         
         int numToInsert = ( 15 * 1024 * 1024 ) / bigString.length();
 
         for ( int i=0; i<numToInsert; i++ )
             c.save( BasicDBObjectBuilder.start().add( "x" , i ).add( "s" , bigString ).get() );
         
         assert( 800 < numToInsert );
         
         assertEquals( numToInsert , c.find().count() );
         assertEquals( numToInsert , c.find().toArray().size() );
         assertEquals( numToInsert , c.find().limit(800).count() );
         assertEquals( 800 , c.find().limit(800).toArray().size() );
 
         int x = c.find().limit(-800).toArray().size();
         assertLess( x , 800 );
         
         DBCursor a = c.find();
         assertEquals( numToInsert , a.itcount() );
 
         DBCursor b = c.find().batchSize( 10 );
         assertEquals( numToInsert , b.itcount() );
         assertEquals( 10 , b.getSizes().get(0).intValue() );
 
         assertLess( a.numGetMores() , b.numGetMores() );
         
        DBCursor d = c.find().batchSize( 1 );
        assertEquals( numToInsert , d.itcount() );
         
     }
 
     @Test
     public void testExplain(){
         DBCollection c = _db.getCollection( "explain1" );
         c.drop();
         
         for ( int i=0; i<100; i++ )
             c.save( new BasicDBObject( "x" , i ) );
 
         DBObject q = BasicDBObjectBuilder.start().push( "x" ).add( "$gt" , 50 ).get();
 
         assertEquals( 49 , c.find( q ).count() );
         assertEquals( 49 , c.find( q ).toArray().size() );
         assertEquals( 49 , c.find( q ).itcount() );
         assertEquals( 20 , c.find( q ).limit(20).itcount() );
         
         c.ensureIndex( new BasicDBObject( "x" , 1 ) );
 
         assertEquals( 49 , c.find( q ).count() );
         assertEquals( 49 , c.find( q ).toArray().size() );
         assertEquals( 49 , c.find( q ).itcount() );
         assertEquals( 20 , c.find( q ).limit(20).itcount() );
 
         assertEquals( 49 , c.find( q ).explain().get("n") );
 
         // these 2 are 'reversed' b/c we want the user case to make sense
         assertEquals( 20 , c.find( q ).limit(20).explain().get("n") ); 
         assertEquals( 49 , c.find( q ).limit(-20).explain().get("n") );
         
     }
     
     final DB _db;
 
     public static void main( String args[] )
         throws Exception {
         (new DBCursorTest()).runConsole();
 
     }
 
 }
