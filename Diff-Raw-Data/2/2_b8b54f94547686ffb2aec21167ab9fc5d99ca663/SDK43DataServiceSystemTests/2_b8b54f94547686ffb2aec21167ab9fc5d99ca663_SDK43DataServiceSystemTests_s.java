 package org.cagrid.iso21090.tests.integration.story;
 
 import gov.nih.nci.cagrid.common.Utils;
 
 import java.io.File;
 
 import junit.framework.TestResult;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cagrid.iso21090.tests.integration.steps.SdkDestroyDatabaseStep;
 import org.junit.After;
 import org.junit.Test;
 
 public class SDK43DataServiceSystemTests {
     
     private static Log LOG = LogFactory.getLog(SDK43DataServiceSystemTests.class);
     
     private long lastTime = 0;
     private File tempApplicationDir = null;
     
     @Test
    public void sdk42DataServiceSystemTests() throws Throwable {
         // create a temporary directory for the SDK application to package things in
         tempApplicationDir = File.createTempFile("SdkWithIsoExample", "temp");
         tempApplicationDir.delete();
         tempApplicationDir.mkdirs();
         LOG.debug("Created temp application base dir: " + tempApplicationDir.getAbsolutePath());
         
         // create the caCORE SDK example project
         splitTime();
         LOG.debug("Running caCORE SDK example project creation story");
         CreateExampleProjectStory createExampleStory = new CreateExampleProjectStory(tempApplicationDir);
         createExampleStory.runBare();
         
         // create and run a caGrid Data Service using the SDK's local API
         splitTime();
         LOG.debug("Running data service using local API story");
         SDK43StyleLocalApiStory localApiStory = new SDK43StyleLocalApiStory();
         localApiStory.runBare();
         
         // create and run a caGrid Data Service using the SDK's remote API
         splitTime();
         LOG.debug("Running data service using remote API story");
         SDK43StyleRemoteApiStory remoteApiStory = new SDK43StyleRemoteApiStory();
         remoteApiStory.runBare();
     }
     
     
     @After
     public void cleanUp() {
         LOG.debug("Cleaning up after tests");
         // tear down the sdk example database
         try {
             new SdkDestroyDatabaseStep().runStep();
         } catch (Exception ex) {
             LOG.warn("Error destroying SDK example project database: " + ex.getMessage());
             ex.printStackTrace();
         }
         // throw away the temp sdk dir
         LOG.debug("Deleting temp application base dir: " + tempApplicationDir.getAbsolutePath());
         Utils.deleteDir(tempApplicationDir);
     }
     
     
     private void splitTime() {
         if (lastTime == 0) {
             LOG.debug("Timer started");
         } else {
             LOG.debug("Time elapsed: " 
                 + (System.currentTimeMillis() - lastTime) / 1000D + " sec");
         }
         lastTime = System.currentTimeMillis();
     }
     
 
     public static void main(String[] args) {
         TestRunner runner = new TestRunner();
         TestResult result = runner.doRun(new TestSuite(SDK43DataServiceSystemTests.class));
         System.exit(result.errorCount() + result.failureCount());
     }
 }
