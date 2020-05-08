 package gov.usgs.cida.ncetl.utils;
 
import org.junit.Ignore;
 import java.io.InputStream;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author jwalker
  */
 public class NetCDFUtilTest {
     
     public NetCDFUtilTest() {
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
      * Test of writeNetCDFFile method, of class NetCDFUtil.
      
     @Test
     public void testWriteNetCDFFile() throws Exception {
         System.out.println("writeNetCDFFile");
         InputStream ncmlIn = null;
         String outfile = "";
         NetCDFUtil.writeNetCDFFile(ncmlIn, outfile);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
      */
 
     /**
      * Test of globalAttributesToMeta method, of class NetCDFUtil.
      */
     @Test
    @Ignore
     public void testGlobalAttributesToMeta() throws Exception {
         System.out.println("globalAttributesToMeta");
         String filename = DCPTConfig.FILE_STORE + "/QPE.20110214.009.105.nc";
         NetCDFUtil.globalAttributesToMeta(filename);
         // TODO review the generated test code and remove the default call to fail.
         assertTrue(true);
     }
 }
