 /**
  * Copyright (c) 2011, Samuel R. Baskinger <basking2@yahoo.com>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a 
  * copy  of this software and associated documentation files (the "Software"),
  * to deal in the Software without restriction, including without limitation 
  * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
  * and/or sell copies of the Software, and to permit persons to whom the 
  * Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included 
  * in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
  * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
  * DEALINGS IN THE SOFTWARE.
  */
 package org.sdsai.dsds.mongo;
 
 import org.sdsai.dsds.node.NodeStore;
 import org.sdsai.dsds.node.Node;
 import org.sdsai.dsds.BTree;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.Ignore;
 
 import com.mongodb.Mongo;
 import com.mongodb.MongoURI;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 
 import java.net.UnknownHostException;
 import java.util.Iterator;
 
 import static org.junit.Assume.assumeNoException;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertEquals;
 import static java.util.UUID.randomUUID;  
 
 public class MongoNodeStoreTest {
     public static final String host = "localhost";
     public static final String user = "test";
     public static final String pass = "test";
     public static final String database = "test";
     public static final String nodeCollection = "testNodeCollection";
     public static final String dataCollection = "testDataCollection";
 
     public static final MongoURI mongoURI = new MongoURI(
         "mongodb://"
         +user
         +":"
         +pass
         +"@"
         +host
         +"/"
         +database
         +"?safe=true");
         
 
     private DB db;
 
     private MongoNodeStore<String, String> ns;
     private BTree<String, String, String> bt;
     
     @Before
     public void setup() throws UnknownHostException
     {
         try
         {
             db = mongoURI.connectDB();
             assertTrue(db.authenticate(user, pass.toCharArray()));
             ns = new MongoNodeStore<String, String>(
                 db.getCollection(nodeCollection),
                 db.getCollection(dataCollection));
             bt = new BTree<String, String, String>("btRoot", ns, 1);
         }
        catch (Throwable e)
         {
             assumeNoException(e);
         }
     }
     
     @Test
     public void testGenerateKey() 
     {
         ns.generateKey(null);
     }
     
     @Test
     public void loadStoreNode()
     {
         Node<String, String> n1 = new Node<String, String>(1,1);
         Node<String, String> n2 = new Node<String, String>(1,1);
         
         String n1id = "node1";
         String n2id = "node2";
         
         n1.getChildren().add(n2id);
         
         n2.getChildren().add(n1id);
         
         ns.store(n1id, n1);
         ns.store(n2id, n2);
         
         assertEquals(ns.loadNode(n1id).getChildren().get(0), n2id);
         assertEquals(ns.loadNode(n2id).getChildren().get(0), n1id);
 
     }
     
     @Test
     public void btreeChurn()
     {
         try
         {
             for(int i = 0; i < 100; i++)
                 bt.put(java.util.UUID.randomUUID()+"", "value"+i);
                 
             assertNotNull(ns.loadNode(ns.convert("btRoot")));
             
             for( final Iterator<String> i = bt.getIterator();
                 i.hasNext(); )
                 i.next();
 
             assertEquals(100, bt.size());  
         }
         finally
         {
             bt.destroy();     
         }  
     }
 }
