 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.tehbeard.beardstat.dataproviders;
 
 import com.tehbeard.beardstat.containers.EntityStatBlob;
 import com.tehbeard.beardstat.containers.documents.DocumentFile;
 import com.tehbeard.beardstat.dataproviders.metadata.CategoryMeta;
 import com.tehbeard.beardstat.dataproviders.metadata.DomainMeta;
 import com.tehbeard.beardstat.dataproviders.metadata.StatisticMeta;
 import com.tehbeard.beardstat.dataproviders.metadata.WorldMeta;
 import java.io.File;
 import net.dragonzone.promise.Promise;
import org.junit.After;
 import org.junit.AfterClass;
import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author James
  */
 public abstract class IStatDataProviderTest {
     
     protected IStatDataProvider instance;
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
 
     /**
      * Test of pullEntityBlob method, of class IStatDataProvider.
      */
     @Test
     public void testPullEntityBlob() {
         System.out.println("pullEntityBlob");
         ProviderQuery query = new ProviderQuery("tehbeard", IStatDataProvider.PLAYER_TYPE, null, false);
         Promise<EntityStatBlob> result = instance.pullEntityBlob(query);
         EntityStatBlob blob = result.getValue();
         assertEquals(blob.getName(), "Tehbeard");
        // TODO review the generated test code and remove the default call to fail.
         
     }
 
     /**
      * Test of pullEntityBlobDirect method, of class IStatDataProvider.
      */
     @Test
     public void testPullEntityBlobDirect() {
        fail();
         System.out.println("pullEntityBlobDirect");
        ProviderQuery query = null;
        EntityStatBlob expResult = null;
         EntityStatBlob result = instance.pullEntityBlobDirect(query);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
     }
 
     /**
      * Test of pushEntityBlob method, of class IStatDataProvider.
      */
     @Test
     public void testPushEntityBlob() {
         System.out.println("pushEntityBlob");
         EntityStatBlob blob = null;
         instance.pushEntityBlob(blob);
         // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
     }
 
     /**
      * Test of hasEntityBlob method, of class IStatDataProvider.
      */
     @Test
     public void testHasEntityBlob() {
         System.out.println("hasEntityBlob");
         ProviderQuery query = null;
         boolean expResult = false;
         boolean result = instance.hasEntityBlob(query);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of deleteEntityBlob method, of class IStatDataProvider.
      */
     @Test
     public void testDeleteEntityBlob() {
         System.out.println("deleteEntityBlob");
         EntityStatBlob blob = null;
         boolean expResult = false;
         boolean result = instance.deleteEntityBlob(blob);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of queryDatabase method, of class IStatDataProvider.
      */
     @Test
     public void testQueryDatabase() {
         System.out.println("queryDatabase");
         ProviderQuery query = null;
         ProviderQueryResult[] expResult = null;
         ProviderQueryResult[] result = instance.queryDatabase(query);
         assertArrayEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of flushSync method, of class IStatDataProvider.
      */
     @Test
     public void testFlushSync() {
         System.out.println("flushSync");
         instance.flushSync();
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of flush method, of class IStatDataProvider.
      */
     @Test
     public void testFlush() {
         System.out.println("flush");
         instance.flush();
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of getDomain method, of class IStatDataProvider.
      */
     @Test
     public void testGetDomain() {
         System.out.println("getDomain");
         String gameTag = "";
         DomainMeta expResult = null;
         DomainMeta result = instance.getDomain(gameTag);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of getWorld method, of class IStatDataProvider.
      */
     @Test
     public void testGetWorld() {
         System.out.println("getWorld");
         String gameTag = "";
         WorldMeta expResult = null;
         WorldMeta result = instance.getWorld(gameTag);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of getCategory method, of class IStatDataProvider.
      */
     @Test
     public void testGetCategory() {
         System.out.println("getCategory");
         String gameTag = "";
         CategoryMeta expResult = null;
         CategoryMeta result = instance.getCategory(gameTag);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of getStatistic method, of class IStatDataProvider.
      */
     @Test
     public void testGetStatistic() {
         System.out.println("getStatistic");
         String gameTag = "";
         StatisticMeta expResult = null;
         StatisticMeta result = instance.getStatistic(gameTag);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of generateBackup method, of class IStatDataProvider.
      */
     @Test
     public void testGenerateBackup() {
         System.out.println("generateBackup");
         File file = null;
         instance.generateBackup(file);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of pullDocument method, of class IStatDataProvider.
      */
     @Test
     public void testPullDocument() {
         System.out.println("pullDocument");
         int entityId = 0;
         String domain = "";
         String key = "";
         DocumentFile expResult = null;
         DocumentFile result = instance.pullDocument(entityId, domain, key);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of pushDocument method, of class IStatDataProvider.
      */
     @Test
     public void testPushDocument() throws Exception {
         System.out.println("pushDocument");
         int entityId = 0;
         DocumentFile document = null;
         DocumentFile expResult = null;
         DocumentFile result = instance.pushDocument(entityId, document);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of deleteDocument method, of class IStatDataProvider.
      */
     @Test
     public void testDeleteDocument() {
         System.out.println("deleteDocument");
         int entityId = 0;
         String domain = "";
         String key = "";
         String revision = "";
         instance.deleteDocument(entityId, domain, key, revision);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of getDocumentKeysInDomain method, of class IStatDataProvider.
      */
     @Test
     public void testGetDocumentKeysInDomain() {
         System.out.println("getDocumentKeysInDomain");
         int entityId = 0;
         String domain = "";
         String[] expResult = null;
         String[] result = instance.getDocumentKeysInDomain(entityId, domain);
         assertArrayEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
 }
