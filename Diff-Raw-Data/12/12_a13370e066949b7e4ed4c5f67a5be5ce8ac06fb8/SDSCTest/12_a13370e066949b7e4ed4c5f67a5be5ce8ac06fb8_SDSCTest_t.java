 /*
  * The contents of this file are subject to the license and copyright
  * detailed in the LICENSE and NOTICE files at the root of the source
  * tree and available online at
  *
  *     http://duracloud.org/license/
  */
 import org.jclouds.ContextBuilder;
 import org.jclouds.blobstore.domain.PageSet;
 import org.jclouds.openstack.swift.SwiftApiMetadata;
 import org.jclouds.openstack.swift.SwiftAsyncClient;
 import org.jclouds.openstack.swift.SwiftClient;
 import org.jclouds.openstack.swift.domain.ContainerMetadata;
 import org.jclouds.openstack.swift.domain.ObjectInfo;
 import org.jclouds.openstack.swift.options.ListContainerOptions;
 import org.jclouds.rest.RestContext;
 
 import java.text.SimpleDateFormat;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Tests the SDSC provider
  *
  * @author Bill Branan
  *         Date: 8/01/13
  */
 public class SDSCTest {
 
     private static String authUrl =
         "https://duracloud.auth.cloud.sdsc.edu/auth/v1.0";
     
     private int plannedAttempts = 5;
 
     // ENTER CREDENTIALS HERE!
     private String username = <REPLACE>;
     private String password = <REPLACE>;
     
     private SimpleDateFormat dateFormat = 
         new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
 
     private SwiftClient getFreshSwiftClient() {
         String trimmedAuthUrl = // JClouds expects authURL with no version
             authUrl.substring(0, authUrl.lastIndexOf("/"));
         RestContext<SwiftClient, SwiftAsyncClient> context =
             ContextBuilder.newBuilder(new SwiftApiMetadata())
                           .endpoint(trimmedAuthUrl)
                           .credentials(username, password)
                           .build(SwiftApiMetadata.CONTEXT_TOKEN);
         return context.getApi();
     }
 
     public void testSwiftClient() throws Exception {
         System.out.println("STARTING JCLOUDS SWIFT CLIENT ITERATION TEST");
 
         int iterationCompletedCount = 0;
         for(int i=0; i<plannedAttempts; i++) {
            System.out.println("Beginning attempt to iterate through content " +
                               "at time: " + dateFormat.format(new Date()));
             try {
                 iterateThroughContent(getFreshSwiftClient());
                 iterationCompletedCount++;
             } catch(Exception e) {
                System.out.println("Iteration failure on attempt: " + i + 
                                    " at time: " + dateFormat.format(new Date()) + 
                                    ". Error message: " + e.getMessage());
             }
         }
         System.out.println("Swift Client iteration attempts: " + plannedAttempts +
                            "; Iterations completed: " + iterationCompletedCount);
 
         System.out.println("JCLOUDS SWIFT CLIENT ITERATION TEST COMPLETE");
     }
 
     private void iterateThroughContent(SwiftClient swiftClient) {
         long spaceCount = 0;
         long errorCount = 0;        
 
         // Get the list of containers
         Set<ContainerMetadata> containers =
             swiftClient.listContainers(new ListContainerOptions());
 
         // Loop through each contaner
         for(ContainerMetadata container : containers) {
             long contentCount = 0;
 
             spaceCount++;
             String containerId = container.getName();
             System.out.println("Counting container: " + containerId);
 
             String marker = null;
             PageSet<ObjectInfo> contentItems =
                 swiftClient.listObjects(containerId, new ListContainerOptions());
                 
             // Loop through each content item                
             while(null != contentItems && contentItems.size() > 0) {
                 for(ObjectInfo contentItem : contentItems) {
                     String contentId = contentItem.getName();
                     contentCount++;
                     System.out.println("Item: " + contentCount);
                     try {
                         // Get the metadata for the content item
                         swiftClient.getObjectInfo(containerId, contentId);
                     } catch(Exception e) {
                         errorCount++;
                         System.out.println("Exception getting content at time: " + 
                                            dateFormat.format(new Date()) + 
                                           ", error type: " + e.getClass() +
                                            ", error message: " + e.getMessage());
                        if(e != null && e.getMessage() != null && 
                           e.getMessage().contains("401 Unauthorized")) {
                            swiftClient = getFreshSwiftClient();
                        }
                     }
                     marker = contentId;
                 }
                 // Get the next set of items
                 ListContainerOptions listOptions = new ListContainerOptions();
                 listOptions.afterMarker(marker);
                 contentItems = swiftClient.listObjects(containerId, listOptions);
             }
             printCount(spaceCount, contentCount, errorCount);
         }
     }
 
     // Print status
     private void printCount(long spaceCount, long contentCount, long errorCount) {
         System.out.println("Space count: " + spaceCount);
         System.out.println("Content count:" + contentCount);
         System.out.println("Error count:" + errorCount);
     }
 
     // Start the ball rolling    
     public static void main(String[] args) throws Exception {
         SDSCTest tester = new SDSCTest();
         tester.testSwiftClient();
     }
 
 }
