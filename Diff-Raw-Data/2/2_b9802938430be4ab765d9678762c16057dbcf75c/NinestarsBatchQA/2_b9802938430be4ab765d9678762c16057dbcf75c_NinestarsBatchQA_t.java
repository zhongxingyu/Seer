 package dk.statsbiblioteket.medieplatform.newspaper.ninestars;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import dk.statsbiblioteket.medieplatform.autonomous.Batch;
 import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 import dk.statsbiblioteket.medieplatform.autonomous.RunnableComponent;
 import dk.statsbiblioteket.newspaper.BatchStructureCheckerComponent;
 import dk.statsbiblioteket.newspaper.md5checker.MD5CheckerComponent;
 import dk.statsbiblioteket.newspaper.metadatachecker.MetadataCheckerComponent;
 import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.MfPakConfiguration;
 import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
 import dk.statsbiblioteket.util.Strings;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 /** This is the main class of the Ninestars QA suite */
 public class NinestarsBatchQA {
 
 
     private static Logger log = LoggerFactory.getLogger(NinestarsBatchQA.class);
 
     public static void main(String... args)
             throws
             Exception {
         int returnCode = doMain(args);
         System.exit(returnCode);
     }
 
     protected static int doMain(String... args) {
         log.info("Entered " + NinestarsBatchQA.class);
 
         Properties properties;
         Batch batch;
 
         try {
             //Create the properties that needs to be passed into the components
             properties = createProperties(args);
             //Get the batch (id) from the command line
             batch = getBatch(args);
         } catch (Exception e) {
             usage();
            System.err.println(e.getMessage());
             return 2;
         }
 
         //This is the list of results so far
         ArrayList<ResultCollector> resultList = new ArrayList<>();
         try {
             //Make the component
             RunnableComponent md5CheckerComponent = new MD5CheckerComponent(properties);
             //Run the component, where the result is added to the resultlist
             runComponent(batch, resultList, md5CheckerComponent);
 
             //Make the component
             MfPakConfiguration mfPakConfiguration = new MfPakConfiguration();
             mfPakConfiguration.setDatabaseUrl(properties.getProperty(ConfigConstants.MFPAK_URL));
             mfPakConfiguration.setDatabaseUser(properties.getProperty(ConfigConstants.MFPAK_USER));
             mfPakConfiguration.setDatabasePassword(properties.getProperty(ConfigConstants.MFPAK_PASSWORD));
             RunnableComponent batchStructureCheckerComponent = new BatchStructureCheckerComponent(properties, new MfPakDAO(mfPakConfiguration));
             //Run the component, where the result is added to the resultlist
             runComponent(batch, resultList, batchStructureCheckerComponent);
 
 
             RunnableComponent metadataCheckerComponent = new MetadataCheckerComponent(properties, new MfPakDAO(mfPakConfiguration));
             runComponent(batch, resultList, metadataCheckerComponent);
             //Add more components as neeeded
 
         } catch (WorkException e) {
             //do nothing, as the failure have already been reported
         }
         ResultCollector mergedResult = NinestarsUtils.mergeResults(resultList);
         String result = NinestarsUtils.convertResult(mergedResult);
         System.out.println(result);
         if (!mergedResult.isSuccess()) {
             return 1;
         } else {
             return 0;
         }
 
     }
 
     /**
      * Get the sql connect string, which should be the second command line parameter. Returns null if there is no
      * second parameter
      *
      * @param args the command line args
      *
      * @return the sql connect string
      */
     private static String getSQLString(String[] args) {
         if (args.length > 1) {
             return args[1];
         } else {
             throw new RuntimeException("Missing sql parameter as second parameter");
         }
     }
 
     /**
      * Create a properties construct with just one property, "scratch". Scratch denotes the folder where the batches
      * reside. It is takes as the parent of the first argument, which should be the path to the batch
      *
      * @param args the args
      *
      * @return a properties construct
      * @throws RuntimeException on trouble parsing arguments.
      */
     private static Properties createProperties(String[] args) throws IOException {
         Properties properties = new Properties(System.getProperties());
         File batchPath = new File(args[0]);
         setIfNotSet(properties, ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER, batchPath.getParent());
         setIfNotSet(properties, ConfigConstants.JPYLYZER_PATH, NinestarsUtils.getJpylyzerPath());
         setIfNotSet(properties, ConfigConstants.AT_NINESTARS, Boolean.TRUE.toString());
         setIfNotSet(properties, ConfigConstants.MFPAK_URL, getSQLString(args));
         setIfNotSet(properties, ConfigConstants.AUTONOMOUS_BATCH_STRUCTURE_STORAGE_DIR,createTempDir().getAbsolutePath());
         return properties;
     }
 
     private static File createTempDir() throws IOException {
           File temp = File.createTempFile("ninestarsQA","");
           temp.delete();
           temp.mkdir();
           temp.deleteOnExit();
           return temp;
       }
 
     private static void setIfNotSet(Properties properties,
                                     String key,
                                     String value) {
         if (properties.getProperty(key) == null) {
             properties.setProperty(key, value);
         } else {
             System.out
                   .println(properties.getProperty(key));
         }
     }
 
     private static void runComponent(Batch batch,
                                      ArrayList<ResultCollector> resultList,
                                      RunnableComponent component1)
             throws
             WorkException {
         log.info("Preparing to run component {}", component1.getComponentName());
         ResultCollector result1 = new ResultCollector(component1.getComponentName(), component1.getComponentVersion());
         resultList.add(result1);
         doWork(batch, component1, result1);
         log.info("Completed run of component {}", component1.getComponentName());
     }
 
     /**
      * Parse the batch and round trip id from the first argument to the script
      *
      * @param args the command line arguments
      *
      * @return the batch id as a batch with no events
      */
     protected static Batch getBatch(String[] args) {
         File batchPath = new File(args[0]);
         if (!batchPath.isDirectory()) {
             throw new RuntimeException("Must have first argument as existing directory");
         }
         String batchFullId = batchPath.getName();
         String[] splits = batchFullId.split(Pattern.quote("-RT"));
         Batch batch = new Batch(splits[0].replaceAll("[^0-9]", "").trim());
         batch.setRoundTripNumber(Integer.parseInt(splits[1].trim()));
         return batch;
     }
 
     /**
      * Call the doWork method on the runnable component, and add a failure to the result collector is the
      * method throws
      *
      * @param batch           the batch to work on
      * @param component       the component doing the work
      * @param resultCollector the result collector
      *
      * @return the resultcollector
      * @throws WorkException if the component threw an exception
      */
     protected static ResultCollector doWork(Batch batch,
                                             RunnableComponent component,
                                             ResultCollector resultCollector)
             throws
             WorkException {
         try {
             component.doWorkOnBatch(batch, resultCollector);
         } catch (Exception e) {
             log.error("Failed to do work on component {}", component.getComponentName(), e);
             resultCollector.addFailure(batch.getFullID(),
                                        "exception",
                                        component.getClass().getSimpleName(),
                                        "Unexpected error in component: " + e.toString(),
                                        Strings.getStackTrace(e));
             throw new WorkException(e);
         }
         return resultCollector;
 
     }
 
     /**
      * Print usage.
      */
     private static void usage() {
         System.err.println("Usage: \n" + "java " + NinestarsFileQA.class.getName()
                                    + " <batchdirectory> <sqlconnectionstring>");
     }
 }
