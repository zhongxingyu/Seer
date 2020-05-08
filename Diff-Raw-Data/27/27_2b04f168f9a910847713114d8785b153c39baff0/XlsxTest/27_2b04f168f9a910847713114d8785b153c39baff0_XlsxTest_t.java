 package ice.test.importers;
 
 import org.junit.Ignore;
 import org.apache.log4j.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author HLN Consulting, LLC
  */
 public class XlsxTest {
 
     private final static Logger logger = Logger.getLogger(XlsxTest.class);
 
     public XlsxTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of importFromFile method, of class Xlsx.
      * @throws Exception
      */
     @Test
     public void testImportFromFile() throws Exception {
         logger.info("importFromFile");
         String filename = "ICE3 Test Cases - HepB.xlsx";
         Xlsx.importFromFile(filename);
         assertTrue(true);
     }
 }
