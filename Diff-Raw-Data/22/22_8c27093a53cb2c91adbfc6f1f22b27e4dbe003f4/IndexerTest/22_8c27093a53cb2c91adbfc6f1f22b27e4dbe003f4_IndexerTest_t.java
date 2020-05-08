 package indexer;
 
 import java.io.File;
 import junit.framework.TestCase;
 
 /**
  *
  * @author emeij
  */
 public class IndexerTest extends TestCase {
     
   boolean deleteAfterwards = false;
   String indexdir = "testindex";
   File index;
   
     public IndexerTest(String testName) {
         super(testName);
     }            
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         index = new File(Utilities.getINDEXDIR() + indexdir);
         Utilities.deleteDir(index);
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
     
   /**
    * Test of index method, of class Indexer.
    */
   public void testIndexMSword() {
     Indexer instance = new Indexer();
     String dataPath = "datadir" + File.separator + "Grant Update 1-2.doc";
     System.out.println(instance.index(indexdir, dataPath));
    assertEquals(1, Utilities.getDocsInIndex(indexdir));
    assertEquals(1, Utilities.getDocsInCache(indexdir));
   }
   
   /**
    * Test of index method, of class Indexer.
    */
   public void testIndexPDF() {
     Indexer instance = new Indexer();
     String dataPath = "datadir" + File.separator + "dp1LSAintro.pdf";
     System.out.println(instance.index(indexdir, dataPath));
    assertEquals(1, Utilities.getDocsInIndex(indexdir));
    assertEquals(1, Utilities.getDocsInCache(indexdir));
   }
   
   /**
    * Test of index method, of class Indexer.
    */
   public void testIndexTxt() {
     Indexer instance = new Indexer();
     String dataPath = "datadir" + File.separator + "wonder.txt";
     System.out.println(instance.index(indexdir, dataPath));
    assertEquals(1, Utilities.getDocsInIndex(indexdir));
    assertEquals(1, Utilities.getDocsInCache(indexdir));
   }
   
   /**
    * Test of index method, of class Indexer.
    */
   public void testIndexMedline() {
     Indexer instance = new Indexer();
     String dataPath = "datadir" + File.separator + "100.med";
     System.out.println(instance.index(indexdir, dataPath));
    assertEquals(143, Utilities.getDocsInIndex(indexdir));
    assertEquals(1, Utilities.getDocsInCache(indexdir));
   }
   
   public void testIndexAll() {
     Indexer instance = new Indexer();
     String dataPath = "datadir" + File.separator;
     System.out.println(instance.index(indexdir, dataPath));
    assertEquals(186, Utilities.getDocsInIndex(indexdir));
     
     if (deleteAfterwards)
       assertEquals(Utilities.deleteDir(new File(indexdir)), true);
     
   }
   
   public void testIndexAll(boolean deleteAfterwards) {
     this.deleteAfterwards = deleteAfterwards;
     testIndexAll();
   }
 
 }
