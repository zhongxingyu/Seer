 package dk.statsbiblioteket.newspaper.metadatachecker;
 
import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.ConfigurationProperties;
import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.MfPakConfiguration;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.util.Streams;
import org.testng.annotations.Test;

 import dk.statsbiblioteket.medieplatform.autonomous.Batch;
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventHandlerFactory;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
 
import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.util.Properties;
 
 import static org.testng.Assert.assertTrue;
 
 /**
  */
 public class MetadataCheckerComponentIT {
 
     private final static String TEST_BATCH_ID = "400022028241";
 
     /** Tests that the BatchStructureChecker can parse a production like batch. */
     @Test(groups = "integrationTest")
     public void testMetadataCheck() throws Exception {
         String pathToProperties = System.getProperty("integration.test.newspaper.properties");
         Properties properties = new Properties();
 
 
         properties.load(new FileInputStream(pathToProperties));
 
         TreeIterator iterator = getIterator();
         EventRunner batchStructureChecker = new EventRunner(iterator);
         ResultCollector resultCollector = new ResultCollector("Batch Structure Checker", "v0.1");
         Batch batch = new Batch();
         batch.setBatchID(TEST_BATCH_ID);
         batch.setRoundTripNumber(1);
         InputStream batchXmlStructureStream = retrieveBatchStructure(batch);
 
         if (batchXmlStructureStream == null) {
             throw new RuntimeException("Failed to resolve batch manifest from data collector");
         }
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        Streams.pipe(batchXmlStructureStream, temp);
        String batchXmlManifest = new String(temp.toByteArray(), "UTF-8");
 
         MfPakConfiguration mfPakConfiguration = new MfPakConfiguration();
         mfPakConfiguration.setDatabaseUrl(properties.getProperty(ConfigurationProperties.DATABASE_URL));
         mfPakConfiguration.setDatabaseUser(properties.getProperty(ConfigurationProperties.DATABASE_USER));
         mfPakConfiguration.setDatabasePassword(properties.getProperty(ConfigurationProperties.DATABASE_PASSWORD));
         EventHandlerFactory eventHandlerFactory = new MetadataChecksFactory(resultCollector,
                                                                             true,
                                                                             getBatchFolder().getParentFile()
                                                                                     .getAbsolutePath(),
                                                                             getJpylyzerPath(),
                                                                             null,
                                                                             new MfPakDAO(mfPakConfiguration),
                                                                             batch,
                                                                             batchXmlManifest);
         batchStructureChecker.runEvents(eventHandlerFactory.createEventHandlers());
         System.out.println(resultCollector.toReport());
         assertTrue(resultCollector.isSuccess());
         //Assert.fail();
     }
 
     private InputStream retrieveBatchStructure(Batch batch) {
         return Thread.currentThread().getContextClassLoader().getResourceAsStream("assumed-valid-structure.xml");
     }
 
     private String getJpylyzerPath() {
         return "src/main/extras/jpylyzer-1.10.1/jpylyzer.py";
     }
 
     /**
      * Creates and returns a iteration based on the test batch file structure found in the test/ressources folder.
      *
      * @return A iterator the the test batch
      * @throws URISyntaxException
      */
     public TreeIterator getIterator() throws URISyntaxException {
         File file = getBatchFolder();
         System.out.println(file);
         return new TransformingIteratorForFileSystems(file, "\\.", ".*\\.jp2$", ".md5");
     }
 
     private File getBatchFolder() {
         String pathToTestBatch = System.getProperty("integration.test.newspaper.testdata");
         return new File(pathToTestBatch, "small-test-batch/B" + TEST_BATCH_ID + "-RT1");
     }
 }
