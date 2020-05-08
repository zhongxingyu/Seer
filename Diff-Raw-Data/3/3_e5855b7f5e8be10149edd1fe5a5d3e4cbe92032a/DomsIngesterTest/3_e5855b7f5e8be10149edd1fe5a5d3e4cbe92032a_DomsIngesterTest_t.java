 package dk.statsbiblioteket.doms.yousee;
 
 import dk.statsbiblioteket.doms.central.CentralWebservice;
 import dk.statsbiblioteket.util.Files;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Properties;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  */
 public class DomsIngesterTest {
     /**
      * Test DOMSIngester against a mockup-doms-central webservice
      * @throws Exception
      */
     @Test
     public void testIngest() throws Exception {
         // Mock-up arguments
         String[] args = {
                 "-filename", "testfile.mux",
                 "-ffprobe", "src/test/resources/ffprobeSample.xml",
                 "-metadata", "src/test/resources/metadataSample.xml",
                 "-crosscheck", "src/test/resources/crosscheckSample.xml",
                 "-url", "http://localhost/testfile1.mux",
                 "-config", "src/test/resources/yousee-doms-ingester.properties"
         };
 
         // Create an ingest context to test with
         IngestContext context = new OptionParser().parseOptions(args);
         if (context == null) {
             //System.exit(1);
             //return;
             assertTrue(false);
         }
 
         String uuid = "";
         try {
             Properties config = context.getConfig();
 
             CentralWebservice centralWebservice = new MockDomsWebservice();
 
             DomsIngester ingester = new DomsIngester(config, centralWebservice);
 
             uuid = ingester.ingest(context);
         } catch (Exception e) {
             //System.err.println("Unable to ingest '" + context.getFilename()
             //        + "' into doms: " + e);
             //log.error("Unable to ingest '{}' into doms. Context: {}",
             //        new Object[]{context.getFilename(), context, e});
             //System.exit(2);
             //return;
             assertTrue(false);
         }
 
         //System.out.println("{\"domsPid\" : \"" + uuid + "\"}");
         //System.exit(0);
         assertTrue(uuid.equalsIgnoreCase("mockedUpPid"));
     }
 
     /**
      * Test DOMSIngester against the real DOMS central webservice
      * @throws Exception
      */
     @Test
     public void testIngestWithRealDOMS() throws Exception {
         // Mock-up arguments
         String[] args = {
                 "-filename", "testfile.mux",
                 "-ffprobe", "src/test/resources/ffprobeSample.xml",
                 "-metadata", "src/test/resources/metadataSample.xml",
                 "-crosscheck", "src/test/resources/crosscheckSample.xml",
                 "-url", "http://localhost/testfile1.mux",
                 "-config", "src/test/resources/yousee-doms-ingester.properties"
         };
 
         // Create an ingest context to test with
         IngestContext context = new OptionParser().parseOptions(args);
         if (context == null) {
             //System.exit(1);
             //return;
             assertTrue(false);
         }
 
         String uuid = "";
 
             Properties config = context.getConfig();
 
             uuid = new IngesterFactory(context.getConfig()).getIngester().ingest(context);
 
 
         //System.out.println("{\"domsPid\" : \"" + uuid + "\"}");
         //System.exit(0);

        // If test succeeds until here, then everything is ok
     }
 
     /**
      * Test whether the xmlEquals() method of DomsIngester works as expected
      * @throws IOException In case xml files could not be loaded
      */
     @Test
     public void testXmlCompare() throws IOException {
         String ffprobeContents = Files.loadString(new File("src/test/resources/ffprobeSample.xml"));
         String ffprobeContentsDifferent = Files.loadString(new File("src/test/resources/ffprobeSample-different.xml"));
 
         boolean equals = new DomsIngester(null, null).xmlEquals(ffprobeContents, ffprobeContentsDifferent);
         assertTrue(equals);
 
     }
 }
