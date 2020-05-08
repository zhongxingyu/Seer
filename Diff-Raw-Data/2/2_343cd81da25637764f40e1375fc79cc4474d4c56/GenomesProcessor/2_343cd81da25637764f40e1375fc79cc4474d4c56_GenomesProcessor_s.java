 package flux.listeners;
 
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.ListObjectsRequest;
 import com.amazonaws.services.s3.model.ObjectListing;
 import com.amazonaws.services.s3.model.S3ObjectSummary;
 import flux.*;
 import flux.repository.RepositoryElement;
 import flux.runtimeconfiguration.RuntimeConfigurationNode;
 
 import java.util.*;
 import java.util.concurrent.*;
 
 /**
  * Implementation of Parent workflow as a java listener.
  *
  * @author arul@flux.ly
  */
 public class GenomesProcessor implements ActionListener {
 
     private static final int CORE_WORKERS = 1;
     public static final String genomesBucket = "1000genomes";
     public static final String joinNamespace = "/" + genomesBucket + "/";
     public static final String genomesParentWorkflow = "/genomes parent workflow";
     public static final String genomesChildWorkflowTemplate = "/genome child workflow template";
 
     private final String fluxUsername = "admin";
     private final String fluxPassword = "admin";
 
     private Configuration makeConfig() throws Exception {
         Factory factory = Factory.makeInstance();
         Configuration config = factory.makeConfiguration();
         config.setSecurityEnabled(true);
         return config;
     }
 
     @Override
     public Object actionFired(final KeyFlowContext flowContext) throws Exception {
         long begin = System.currentTimeMillis();
         int NUM_WORKERS = 10;// Default is 10 workers
         int SLEEP_TIME = 5;// Default is 5 secs
         String JOIN_TIME_EXPRESSION = "+5s";
         RuntimeConfigurationNode runtimeConfig = flowContext.getEngine().getRuntimeConfiguration().getChild(genomesBucket);
         String workers = (String) runtimeConfig.get("NUM_WORKERS");
         final int numWorkers = Integer.valueOf(workers);
         System.out.println("Num workers set to " + numWorkers + ".");
         if (numWorkers > 0) {
             NUM_WORKERS = numWorkers;
         }
         String waitTime = (String) runtimeConfig.get("WAIT_TIME");
         final int sleepTime = Integer.valueOf(waitTime);
         System.out.println("Sleep time set to " + sleepTime + "s.");
         final Factory factory = Factory.makeInstance();
         boolean secured = true;
         if (!flowContext.getEngine().isSecured()) {
             secured = false;
         }
 
         Map<String, S3ObjectSummary> objectSummaryMap = getS3ObjectSummaryMap();
         final int numObjects = objectSummaryMap.size();
         final BlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<Runnable>(numObjects);
 
         final ExecutorService executor = new ThreadPoolExecutor(CORE_WORKERS, NUM_WORKERS, 30, TimeUnit.SECONDS, taskQueue);
 
         final CountDownLatch latch = new CountDownLatch(numObjects);
         final RepositoryElement element = flowContext.getEngine().getRepositoryAdministrator().getElement(genomesChildWorkflowTemplate);
         final FlowChart genomesProcessingTemplate = element.getFlowChart();
         if (genomesProcessingTemplate == null) {
             System.out.println("Verify if the correct genome child template is uploaded to repository. Quit processing.");
             return null;
         }
         System.out.println("Found genome processing template from repository " + genomesProcessingTemplate.getName());
         System.out.println("Scheduling genomes for processing # " + new Date());
 
         final ConcurrentMap<String, FlowChart> genomesFailedToSchedule = new ConcurrentHashMap<String, FlowChart>();
         for (final Map.Entry<String, S3ObjectSummary> entry : objectSummaryMap.entrySet()) {
             final boolean remoteSecured = secured;
             Runnable worker = new Runnable() {
                 @Override
                 public void run() {
                     final String key = entry.getKey();
                     Engine remoteEngine = null;
                     RemoteSecurity remoteSecurity = null;
                     if (remoteSecured) {
                         try {
                             remoteSecurity = factory.lookupRemoteSecurity(makeConfig());
                             remoteEngine = remoteSecurity.login(fluxUsername, fluxPassword);
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     } else {
                         try {
                             remoteEngine = factory.lookupRmiEngine(makeConfig());
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     }
                     // Update the name
                     String uuid = UUID.randomUUID().toString();
                     genomesProcessingTemplate.setName(joinNamespace + uuid);
                     genomesProcessingTemplate.getVariableManager().put("file_name", key);
                     genomesProcessingTemplate.getVariableManager().put("file_size", entry.getValue().getSize());
                     try {
                         String name = remoteEngine.put(genomesProcessingTemplate);
                         System.out.println("Scheduled S3 object # " + name);
                     } catch (Exception e) {
                         e.printStackTrace();
                         genomesFailedToSchedule.put(genomesProcessingTemplate.getName(), genomesProcessingTemplate);
                     } finally {
                         try {
                             if (remoteSecured && remoteSecurity != null) {
                                 remoteSecurity.logout(fluxUsername);
                             }
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     }
                     latch.countDown();
                 }
             };
             executor.execute(worker);
         }
 
         System.out.println("Waiting for workers to finish...");
         latch.await();
 
         Engine localEngine = flowContext.getEngine();
         long size = localEngine.size();
         if (sleepTime > 0) {
             JOIN_TIME_EXPRESSION = "+" + sleepTime + "s";
             SLEEP_TIME = sleepTime;
         }
         while (size > 0 && !flowContext.isInterrupted()) {
             System.out.println("Waiting for " + size + " processes in namespace " + joinNamespace + " to complete. Sleeping for " + SLEEP_TIME + " seconds...");
             if (localEngine.join(joinNamespace, JOIN_TIME_EXPRESSION, "+3s")) {
                 size = localEngine.size(joinNamespace);
             } else {
                 size = 0;
             }
         }
 
         if (flowContext.isInterrupted()) {
             System.out.println("Flow context interrupted, quitting now!");
             List<Runnable> waiting = executor.shutdownNow();
             System.out.println("Workers finished scheduling. Waiting to execute = " + waiting.size());
         } else {
             System.out.println("All genomes processed # " + new Date());
             System.out.println("Failed objects = " + genomesFailedToSchedule.size());//TODO: Implement retry for failed S3 objects
             List<Runnable> waiting = executor.shutdownNow();
             System.out.println("Workers finished scheduling. Waiting to execute = " + waiting.size());
         }
         long end = System.currentTimeMillis();
        System.out.println("Action executed in " + (end - begin)/1000 + "s.");
         return null;
     }
 
     private List<S3ObjectSummary> getS3ObjectSummary() {
         AmazonS3 s3 = new AmazonS3Client();
         ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(genomesBucket));
         return objectListing.getObjectSummaries();
     }
 
     private Map<String, S3ObjectSummary> getS3ObjectSummaryMap() {
         List<S3ObjectSummary> objectSummaryList = getS3ObjectSummary();
         Map<String, S3ObjectSummary> objectSummaryMap = new TreeMap<String, S3ObjectSummary>();
         for (S3ObjectSummary objectSummary : objectSummaryList) {
             objectSummaryMap.put(objectSummary.getKey(), objectSummary);
         }
         return objectSummaryMap;
     }
 
 }
