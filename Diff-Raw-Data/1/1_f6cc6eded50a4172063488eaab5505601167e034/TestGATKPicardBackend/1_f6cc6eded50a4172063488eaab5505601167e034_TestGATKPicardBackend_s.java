 package io.seqware.queryengine.sandbox.testing;
 
 import static org.junit.Assert.assertEquals;
 import io.seqware.queryengine.sandbox.testing.impl.GATKPicardBackendTest;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 
 import org.apache.commons.io.IOUtils;
 import org.json.JSONException;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TestGATKPicardBackend {
     // @Test
     public void testAll() throws Exception{
     	ReturnValue returned = new ReturnValue();
         
 
          //Point to local VCF file to be read
     	backend.loadFeatureSet(vcffile);  
  		         
          //Obtain matched features
         returned = backend.getFeatures(jsonTxt);    	
     }
 
     static GATKPicardBackendTest backend; 
     static String vcffile;
     static String bamfile;
     static String jsonTxt;
     static String jsonTxt2 = new String(); //Need to combine these JSON's after
     
     
     @BeforeClass
     public static void setUp() throws Exception {
       File jsonQuery = new File("src/main/resources/testdata/query.json");
   	  InputStream is = new FileInputStream(jsonQuery);
   	  jsonTxt = IOUtils.toString(is);
 	    
   	  backend = new GATKPicardBackendTest();
       bamfile = "src/main/resources/testdata/HG00310.chrom20.ILLUMINA.bwa.FIN.low_coverage.20120522.bam";
       vcffile = "src/main/resources/testdata/exampleVCFinput.vcf";
       
       PrintWriter writer = new PrintWriter(System.getProperty("user.home") + "/Report.html", "UTF-8");
       fillOutHeader(writer);
       writer.println(backend.getIntroductionDocs().getKv().get(BackendTestInterface.DOCS));
       writer.println(backend.getConclusionDocs().getKv().get(BackendTestInterface.DOCS));
       fillOutFooter(writer);
       writer.close();
     }
     
     @AfterClass
     public static void tearDown() throws Exception {
       backend = null;
       bamfile = null;
       jsonTxt = null;
     }
 
     private static void fillOutHeader(PrintWriter o) {
         o.write("<html><body>");
     }
 
     private static void fillOutFooter(PrintWriter o) {
         o.write("</body></html>");
     }
     
 //    @Test
     public void testGetIntroductionDocs() {
       String expHtmlReport = "<h2>GATKPicardBackend: Introduction</h2>";  
       assertEquals(expHtmlReport, backend.getIntroductionDocs().getKv().get(BackendTestInterface.DOCS));
     }
 
 //    @Test
     public void testLoadReadSet() {
       Assert.assertNotNull(backend.loadReadSet(bamfile).getKv().get(BackendTestInterface.READ_SET_ID));
     }
     
 //    @Test
     public void testGetReads() {
       backend.loadReadSet(bamfile);
       Assert.assertNotNull(backend.getReads(jsonTxt).getKv().get(BackendTestInterface.QUERY_RESULT_FILE));
     }
     
 //    @Test
     public void testGetConclusionDocs() {
       String expHtmlReport = "<h2>Conclusion</h2>"; 
       assertEquals(expHtmlReport, backend.getConclusionDocs().getKv().get(BackendTestInterface.DOCS));
     }
     
 }
