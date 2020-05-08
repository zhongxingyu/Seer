 package io.seqware.queryengine.sandbox.testing;
 
 import io.seqware.queryengine.sandbox.testing.impl.GATKBackendTest;
 import io.seqware.queryengine.sandbox.testing.impl.PicardBackendTest;
 import io.seqware.queryengine.sandbox.testing.utils.Global;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.swing.JEditorPane;
 import javax.swing.text.html.HTMLDocument;
 
 import net.sf.samtools.SAMFileReader;
 
 import org.apache.commons.io.IOUtils;
 import org.json.JSONException;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class GATK_Picard_Test {
     @Test
     public void testGATK() throws IOException, JSONException{
     	 ReturnValue returned = new ReturnValue();
          
         
         
          //Point to local VCF file to be read
          gb.loadFeatureSet(vcffile);  
  		
          //Point to TSV output file to be written to
          Global.outputFilePath = File.createTempFile("output", "txt").getAbsolutePath();
          
  		//Obtain matched features
          returned = gb.getFeatures(jsonTxt2);    	
     }
 
     static PicardBackendTest pb;
     static String bamfile;
     static String jsonTxt;
     
     static GATKBackendTest gb; 
     static String vcffile;
    static String jsonTxt2; //Need to combine these JSON's after
     
     @BeforeClass
     public static void setUp() throws Exception {
       pb = new PicardBackendTest();
       bamfile = "src/main/resources/testdata/HG00310.chrom20.ILLUMINA.bwa.FIN.low_coverage.20120522.bam";
       File jsonQuery = new File("src/main/resources/testdata/query.json");
       InputStream is = new FileInputStream(jsonQuery);
       jsonTxt = IOUtils.toString(is);
       
       
       BufferedReader in;
       //Point to local JSON text file to be read
       String line = new String();
      in = new BufferedReader(new FileReader("src/test/resources/exampleJSONQuery.txt"));
       while ((line = in.readLine()) != null){
       	jsonTxt2 = jsonTxt2.concat(line);
       }
       
       gb = new GATKBackendTest();
      vcffile = "src/test/resources/exampleVCFinput.vcf";
      jsonTxt2 = "src/test/resources/exampleJSONQuery.txt";
     }
     
     @AfterClass
     public static void tearDown() throws Exception {
       pb = null;
       bamfile = null;
       jsonTxt = null;
     }
 
     @Test
     public void testGetIntroductionDocs() {
       pb.getIntroductionDocs();
       HTMLDocument expHtmlReport = new HTMLDocument();
       JEditorPane p = new JEditorPane();
       p.setContentType("text/html");
       p.setText(""
           + " <html>"
           + "   <head>"
           + "     <title>SeqWare Query Engine: PicardBackendTest</title>"
           + "     <style type=\"text/css\">"
           + "       body { background-color: #EEEEEE; }"
           + "       h3  { color: red; }"
           + "     </style>"
           + "     </head>" 
           + "   <body>"
           + "     <h1>SeqWare Query Engine: PicardBackendTest</h1>"
           + "   </body>"
           + "</html>"
        );
       expHtmlReport = (HTMLDocument) p.getDocument();  
       try {
       Assert.assertEquals(pb.getHTMLReport().getText(0, pb.getHTMLReport().getLength()), expHtmlReport.getText(0, expHtmlReport.getLength()));
       } catch (Exception ex) {
         Assert.fail();
       }
     }
 
     @Test
     public void testLoadReadSet() {
       pb.loadReadSet(bamfile);
       Assert.assertNotNull(pb.getFileReader());
     }
     
     @Test
     public void testGetReads() {
       pb.getIntroductionDocs();
       pb.loadReadSet(bamfile);
       pb.getReads(jsonTxt);
       File actualFile = new File("testOutput.bam");
       SAMFileReader actualBAM = new SAMFileReader(actualFile);
       Assert.assertNotNull(actualBAM);
     }
     
     @Test
     public void testGetConclusionDocs() {
       pb.getConclusionDocs();
       Assert.assertNotNull(pb.getHTMLReport());
     }
     
 }
