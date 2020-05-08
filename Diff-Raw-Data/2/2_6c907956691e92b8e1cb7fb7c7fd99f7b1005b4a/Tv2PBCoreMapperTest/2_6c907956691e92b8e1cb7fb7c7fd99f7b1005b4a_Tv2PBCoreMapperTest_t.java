 package dk.statsbiblioteket.doms.ingest.reklamepbcoremapper;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.File;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * Test file generations.
  */
 public class Tv2PBCoreMapperTest {
 
     private static final File OUTPUTDIR = new File("target/testoutput");
 
     @Before
     public void setUp() {
         OUTPUTDIR.mkdirs();
     }
 
     @After
     public void tearDown() {
         for (File file : OUTPUTDIR.listFiles()) {
             file.delete();
         }
         OUTPUTDIR.delete();
     }
 
     @Test
     public void testMapCsvDataToPBCoreFiles() throws Exception {
         new Tv2PBCoreMapper().mapCsvDataToPBCoreFiles(new File(getClass().getClassLoader().getResource("199901_1.meta.utf8.csv").getPath()),
                                                       OUTPUTDIR);
         File[] generatedFiles = OUTPUTDIR.listFiles();
         assertEquals(226, generatedFiles.length);
         for (File file : generatedFiles) {
             Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            assertEquals(42, d.getElementsByTagName("*").getLength());
             //TODO Test stuff
         }
     }
 }
