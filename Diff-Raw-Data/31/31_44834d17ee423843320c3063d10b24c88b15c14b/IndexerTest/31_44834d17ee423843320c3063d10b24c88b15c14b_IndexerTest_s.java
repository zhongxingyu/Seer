 package indexer;
 
 import java.io.File;
 import java.io.IOException;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 
 /**
  *
  * @author emeij
  */
 public class IndexerTest {
     
   boolean deleteAfterwards = false;
   String indexdir = "testindex";
   File index;
   Indexer instance;
   IndexWriterUtil iwUtil;
   
 	@Before
     public void setUp() throws Exception {
         index = new File(Utilities.getINDEXDIR() + indexdir);
         Utilities.deleteDir(index);
 		instance = new Indexer();
 		iwUtil = new IndexWriterUtil(new ConfigurationHandler("indexconfig.xml"), indexdir);
     }
 
 	@After
     public void tearDown() throws Exception {
 		iwUtil.closeIndexWriter();
     }
     
   /**
    * Test of index method, of class Indexer.
    */
 	@Test
   public void testIndexMSword() throws IOException {
     String dataPath = "datadir" + File.separator + "Grant Update 1-2.doc";
     System.out.println(instance.index(indexdir, dataPath));
     assertEquals("In index", 1, iwUtil.getDocsInIndexCount());
     assertEquals("In cache", 1, Utilities.getDocsInCache(indexdir));
   }
   
   /**
    * Test of index method, of class Indexer.
    */
 	@Test
   public void testIndexPDF() throws IOException {
     String dataPath = "datadir" + File.separator + "dp1LSAintro.pdf";
     System.out.println(instance.index(indexdir, dataPath));
     assertEquals("In index", 1, iwUtil.getDocsInIndexCount());
     assertEquals("In cache", 1, Utilities.getDocsInCache(indexdir));
   }
   
   /**
    * Test of index method, of class Indexer.
    */
 	@Test
   public void testIndexTxt() throws IOException {
     String dataPath = "datadir" + File.separator + "wonder.txt";
     System.out.println(instance.index(indexdir, dataPath));
     assertEquals("In index", 1, iwUtil.getDocsInIndexCount());
     assertEquals("In cache", 1, Utilities.getDocsInCache(indexdir));
   }
   
   /**
    * Test of index method, of class Indexer.
    */
   // TODO issue AIDA-8 medline files can consist of multiple documents
 	@Test
   @Ignore
   public void testIndexMedline() throws IOException {
     String dataPath = "datadir" + File.separator + "100.med";
     System.out.println(instance.index(indexdir, dataPath));
     assertEquals("In index", 143, iwUtil.getDocsInIndexCount());
     assertEquals("In cache", 1, Utilities.getDocsInCache(indexdir));
   }
 
 	@Test
 	public void testIndexZylab() throws IOException {
 		String dataPath = "datadir" + File.separator + "{C2212583-3E6D-4AB2-8F80-2C8934833CAB}.xml";
 		System.out.println(instance.index(indexdir, dataPath));
 		assertEquals("In index", 1, iwUtil.getDocsInIndexCount());
 		assertEquals("In cache", 1, Utilities.getDocsInCache(indexdir));
 	}
 
 	@Test
   public void testIndexAll() throws IOException {
     String dataPath = "datadir" + File.separator;
     System.out.println(instance.index(indexdir, dataPath));
 // TODO issue AIDA-8 medline files can consist of multiple documents
 //    assertEquals("In index", 186, iwUtil.getDocsInIndexCount());
 	assertEquals("In index: ", 6, iwUtil.getDocsInIndexCount());
     
     if (deleteAfterwards)
       assertEquals(Utilities.deleteDir(new File(indexdir)), true);
     
   }
   
   public void testIndexAll(boolean deleteAfterwards) throws IOException {
     this.deleteAfterwards = deleteAfterwards;
     testIndexAll();
   }
 
 }
