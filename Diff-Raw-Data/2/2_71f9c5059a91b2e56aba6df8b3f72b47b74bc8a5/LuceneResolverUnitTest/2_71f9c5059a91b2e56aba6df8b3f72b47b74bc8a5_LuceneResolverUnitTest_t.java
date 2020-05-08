 /*
  * Copyright 2008 The Topaz Foundation 
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  *
  * Contributions:
  */
 
 package org.mulgara.resolver.lucene;
 
 import java.io.File;
 import java.io.StringWriter;
 import java.io.PrintWriter;
 import java.net.URI;
 
 import javax.transaction.xa.XAResource;
 import javax.transaction.xa.Xid;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.apache.log4j.Logger;
 import org.mulgara.itql.TqlInterpreter;
 import org.mulgara.query.Answer;
 import org.mulgara.query.ModelResource;
 import org.mulgara.query.Query;
 import org.mulgara.query.operation.Modification;
 import org.mulgara.query.rdf.Mulgara;
 import org.mulgara.query.rdf.URIReferenceImpl;
 import org.mulgara.resolver.Database;
 import org.mulgara.resolver.JotmTransactionManagerFactory;
 import org.mulgara.server.Session;
 import org.mulgara.util.FileUtil;
 
 /**
  * Unit tests for the lucene resolver.
  *
  * @created 2008-10-13
  * @author Ronald Tschalär
  * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
  * @licence Apache License v2.0
  */
 public class LuceneResolverUnitTest extends TestCase {
   private static final Logger logger = Logger.getLogger(LuceneResolverUnitTest.class);
 
   private static final URI databaseURI = URI.create("local:database");
   private static final URI modelURI = URI.create("local:lucene");
   private static final URI luceneModelType = URI.create(Mulgara.NAMESPACE + "LuceneModel");
   private final static String textDirectory =
       System.getProperty("cvs.root") + File.separator + "data" + File.separator + "fullTextTestData";
 
   private static Database database = null;
   private static TqlInterpreter ti = null;
 
   public LuceneResolverUnitTest(String name) {
     super(name);
   }
 
   public static Test suite() {
     TestSuite suite = new TestSuite();
     suite.addTest(new LuceneResolverUnitTest("testConcurrentQuery"));
     suite.addTest(new LuceneResolverUnitTest("testConcurrentReadTransction"));
     suite.addTest(new LuceneResolverUnitTest("testTransactionIsolation"));
 
     return suite;
   }
 
   /**
    * Create test objects.
    */
   public void setUp() throws Exception {
     if (database == null) {
       // Create the persistence directory
       File persistenceDirectory = new File(new File(System.getProperty("cvs.root")), "testDatabase");
       if (persistenceDirectory.isDirectory()) {
         if (!FileUtil.deleteDirectory(persistenceDirectory)) {
           throw new RuntimeException("Unable to remove old directory " + persistenceDirectory);
         }
       }
       if (!persistenceDirectory.mkdirs()) {
         throw new Exception("Unable to create directory " + persistenceDirectory);
       }
 
       // Define the the node pool factory
       String nodePoolFactoryClassName = "org.mulgara.store.stringpool.xa11.XA11StringPoolFactory";
 
       // Define the string pool factory
       String stringPoolFactoryClassName = "org.mulgara.store.stringpool.xa11.XA11StringPoolFactory";
 
       String tempNodePoolFactoryClassName = "org.mulgara.store.nodepool.memory.MemoryNodePoolFactory";
 
       // Define the string pool factory
       String tempStringPoolFactoryClassName = "org.mulgara.store.stringpool.memory.MemoryStringPoolFactory";
 
       // Define the resolver factory used to manage system models
       String systemResolverFactoryClassName = "org.mulgara.resolver.store.StatementStoreResolverFactory";
 
       // Define the resolver factory used to manage system models
       String tempResolverFactoryClassName = "org.mulgara.resolver.memory.MemoryResolverFactory";
 
       // Create a database which keeps its system models on the Java heap
       database = new Database(
                    databaseURI,
                    persistenceDirectory,
                    null,                            // no security domain
                    new JotmTransactionManagerFactory(),
                    0,                               // default transaction timeout
                    0,                               // default idle timeout
                    nodePoolFactoryClassName,        // persistent
                    new File(persistenceDirectory, "xaNodePool"),
                    stringPoolFactoryClassName,      // persistent
                    new File(persistenceDirectory, "xaStringPool"),
                    systemResolverFactoryClassName,  // persistent
                    new File(persistenceDirectory, "xaStatementStore"),
                    tempNodePoolFactoryClassName,    // temporary nodes
                    null,                            // no dir for temp nodes
                    tempStringPoolFactoryClassName,  // temporary strings
                    null,                            // no dir for temp strings
                    tempResolverFactoryClassName,    // temporary models
                    null,                            // no dir for temp models
                    "",                              // no rule loader
                    "org.mulgara.content.n3.N3ContentHandler");
 
       database.addResolverFactory("org.mulgara.resolver.lucene.LuceneResolverFactory", persistenceDirectory);
 
       ti = new TqlInterpreter();
     }
   }
 
   private synchronized Query parseQuery(String q) throws Exception {
     return (Query) ti.parseCommand(q);
   }
 
   /**
    * The teardown method for JUnit
    */
   public void tearDown() {
   }
 
   /**
    * Two queries, in parallel.
    */
   public void testConcurrentQuery() throws Exception {
     logger.info("Testing concurrentQuery");
 
     try {
       // Load some test data
       Session session = database.newSession();
 
       URI fileURI = new File(textDirectory + File.separator + "data.n3").toURI();
       if (session.modelExists(modelURI)) {
         session.removeModel(modelURI);
       }
       session.createModel(modelURI, luceneModelType);
       session.setModel(modelURI, new ModelResource(fileURI));
 
       // Run the queries
       try {
         String q = "select $x from <foo:bar> where $x <foo:hasText> 'American' in <" + modelURI + ">;";
         Query qry1 = parseQuery(q);
         Query qry2 = parseQuery(q);
 
         Answer answer1 = session.query(qry1);
        Answer answer2 = session.query(qry2);
 
         compareResults(answer1, answer2);
 
         answer1.close();
         answer2.close();
       } finally {
         session.close();
       }
     } catch (Exception e) {
       fail(e);
     }
   }
 
   /**
    * Two queries, in concurrent transactions.
    */
   public void testConcurrentReadTransction() throws Exception {
     logger.info("Testing concurrentReadTransaction");
 
     try {
       Session session1 = database.newSession();
       try {
         XAResource resource1 = session1.getReadOnlyXAResource();
         Xid xid1 = new TestXid(1);
         resource1.start(xid1, XAResource.TMNOFLAGS);
 
         final boolean[] flag = new boolean[] { false };
 
         Thread t2 = new Thread("tx2Test") {
           public void run() {
             try {
               Session session2 = database.newSession();
               try {
                 XAResource resource2 = session2.getReadOnlyXAResource();
                 Xid xid2 = new TestXid(2);
                 resource2.start(xid2, XAResource.TMNOFLAGS);
 
                 synchronized (flag) {
                   flag[0] = true;
                   flag.notify();
                 }
 
                 // Evaluate the query
                 String q = "select $x from <foo:bar> where $x <foo:hasText> 'Study' in <" + modelURI + ">;";
                 Answer answer = session2.query(parseQuery(q));
 
                 compareResults(expectedStudyResults(), answer);
                 answer.close();
 
                 synchronized (flag) {
                   while (flag[0])
                     flag.wait();
                 }
 
                 resource2.end(xid2, XAResource.TMSUCCESS);
                 resource2.commit(xid2, true);
               } finally {
                 session2.close();
               }
             } catch (Exception e) {
               fail(e);
             }
           }
         };
         t2.start();
 
         synchronized (flag) {
           if (!flag[0]) {
             try {
               flag.wait(2000L);
             } catch (InterruptedException ie) {
               logger.error("wait for tx2-started interrupted", ie);
               fail(ie);
             }
           }
           assertTrue("second transaction should have proceeded", flag[0]);
         }
 
         String q = "select $x from <foo:bar> where $x <foo:hasText> 'Group' in <" + modelURI + ">;";
         Answer answer = session1.query(parseQuery(q));
 
         compareResults(expectedGroupResults(), answer);
         answer.close();
 
         synchronized (flag) {
           flag[0] = false;
           flag.notify();
         }
 
         try {
           t2.join(2000L);
         } catch (InterruptedException ie) {
           logger.error("wait for tx2-terminated interrupted", ie);
           fail(ie);
         }
         assertFalse("second transaction should've terminated", t2.isAlive());
 
         resource1.end(xid1, XAResource.TMSUCCESS);
         resource1.commit(xid1, true);
       } finally {
         session1.close();
       }
     } catch (Exception e) {
       fail(e);
     }
   }
 
   /**
    * Two concurrent transactions, one reader, one writer. Verify transaction isolation.
    */
   public void testTransactionIsolation() throws Exception {
     logger.info("Testing transactionIsolation");
 
     try {
       Session session1 = database.newSession();
       try {
         // start read-only txn
         XAResource resource1 = session1.getReadOnlyXAResource();
         Xid xid1 = new TestXid(1);
         resource1.start(xid1, XAResource.TMNOFLAGS);
 
         // run query before second txn starts
         String q = "select $x from <foo:bar> where $x <foo:hasText> 'Group' in <" + modelURI + ">;";
         Answer answer = session1.query(parseQuery(q));
 
         compareResults(expectedGroupResults(), answer);
         answer.close();
 
         // run a second transaction that writes new data
         final boolean[] flag = new boolean[] { false };
 
         Thread t2 = new Thread("tx2Test") {
           public void run() {
             try {
               Session session2 = database.newSession();
               try {
                 XAResource resource2 = session2.getXAResource();
                 Xid xid2 = new TestXid(2);
                 resource2.start(xid2, XAResource.TMNOFLAGS);
 
                 synchronized (flag) {
                   flag[0] = true;
                   flag.notify();
 
                   while (flag[0])
                     flag.wait();
                 }
 
                 String q = "insert <foo:nodeX> <foo:hasText> 'Another Group text' into <" + modelURI + ">;";
                 synchronized (LuceneResolverUnitTest.this) {
                   session2.insert(modelURI, ((Modification) ti.parseCommand(q)).getStatements());
                 }
 
                 synchronized (flag) {
                   flag[0] = true;
                   flag.notify();
 
                   while (flag[0])
                     flag.wait();
                 }
 
                 resource2.end(xid2, XAResource.TMSUCCESS);
                 resource2.commit(xid2, true);
               } finally {
                 session2.close();
               }
             } catch (Exception e) {
               fail(e);
             }
           }
         };
         t2.start();
 
         // wait for 2nd txn to have started
         synchronized (flag) {
           while (!flag[0])
             flag.wait();
         }
 
         // run query before insert
         answer = session1.query(parseQuery(q));
         compareResults(expectedGroupResults(), answer);
         answer.close();
 
         // wait for insert to complete
         synchronized (flag) {
           flag[0] = false;
           flag.notify();
 
           while (!flag[0])
             flag.wait();
         }
 
         // run query after insert and before commit
         answer = session1.query(parseQuery(q));
         compareResults(expectedGroupResults(), answer);
         answer.close();
 
         // wait for commit to complete
         synchronized (flag) {
           flag[0] = false;
           flag.notify();
         }
 
         try {
           t2.join(2000L);
         } catch (InterruptedException ie) {
           logger.error("wait for tx2-terminated interrupted", ie);
           fail(ie);
         }
         assertFalse("second transaction should've terminated", t2.isAlive());
 
         // run query after commit
         answer = session1.query(parseQuery(q));
         compareResults(expectedGroupResults(), answer);
         answer.close();
 
         // clean up
         resource1.end(xid1, XAResource.TMSUCCESS);
         resource1.commit(xid1, true);
 
         // start new tx - we should see new data now
         xid1 = new TestXid(3);
         resource1.start(xid1, XAResource.TMNOFLAGS);
 
         answer = session1.query(parseQuery(q));
         compareResults(concat(expectedGroupResults(), new String[][] { { "foo:nodeX" } }), answer);
         answer.close();
 
         resource1.end(xid1, XAResource.TMSUCCESS);
         resource1.commit(xid1, true);
       } finally {
         session1.close();
       }
     } catch (Exception e) {
       fail(e);
     }
   }
 
   private String[][] expectedStudyResults() {
     return new String[][] {
         { "foo:node3" }, { "foo:node4" }, { "foo:node11" }, { "foo:node13" }, { "foo:node19" },
         { "foo:node22" },
     };
   }
 
   private String[][] expectedGroupResults() {
     return new String[][] {
         { "foo:node1" }, { "foo:node2" }, { "foo:node4" }, { "foo:node9" }, { "foo:node11" },
         { "foo:node12" }, { "foo:node13" }, { "foo:node14" }, { "foo:node18" },
     };
   }
 
   private static String[][] concat(String[][] a1, String[][] a2) {
     String[][] res = new String[a1.length + a2.length][];
     System.arraycopy(a1, 0, res, 0, a1.length);
     System.arraycopy(a2, 0, res, a1.length, a2.length);
     return res;
   }
 
   private void compareResults(String[][] expected, Answer answer) throws Exception {
     try {
       answer.beforeFirst();
       for (int i = 0; i < expected.length; i++) {
         assertTrue("Answer short at row " + i, answer.next());
         assertEquals(expected[i].length, answer.getNumberOfVariables());
         for (int j = 0; j < expected[i].length; j++) {
           URIReferenceImpl uri = new URIReferenceImpl(new URI(expected[i][j]));
           assertEquals(uri, answer.getObject(j));
         }
       }
       assertFalse(answer.next());
     } catch (Exception e) {
       logger.error("Failed test - " + answer);
       answer.close();
       throw e;
     }
   }
 
   private void compareResults(Answer answer1, Answer answer2) throws Exception {
     answer1.beforeFirst();
     answer2.beforeFirst();
     assertEquals(answer1.getNumberOfVariables(), answer2.getNumberOfVariables());
     while (answer1.next()) {
       assertTrue(answer2.next());
       for (int i = 0; i < answer1.getNumberOfVariables(); i++) {
         assertEquals(answer1.getObject(i), answer2.getObject(i));
       }
     }
     assertFalse(answer2.next());
   }
 
   private void fail(Throwable throwable) {
     StringWriter stringWriter = new StringWriter();
     throwable.printStackTrace(new PrintWriter(stringWriter));
     fail(stringWriter.toString());
   }
 
   private static class TestXid implements Xid {
     private final int xid;
 
     public TestXid(int xid) {
       this.xid = xid;
     }
 
     public int getFormatId() {
       return 'X';
     }
 
     public byte[] getBranchQualifier() {
       return new byte[] { (byte)(xid >> 0x00), (byte)(xid >> 0x08) };
     }
 
     public byte[] getGlobalTransactionId() {
       return new byte[] { (byte)(xid >> 0x10), (byte)(xid >> 0x18) };
     }
   }
 }
