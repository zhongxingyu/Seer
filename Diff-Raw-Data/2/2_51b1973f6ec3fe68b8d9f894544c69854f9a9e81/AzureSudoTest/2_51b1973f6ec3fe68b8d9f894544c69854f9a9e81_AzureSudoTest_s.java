 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.azure;
 
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.NewAbstractCloudTest;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: elip
  * Date: 5/28/13
  * Time: 10:55 AM
  */
 public class AzureSudoTest extends NewAbstractCloudTest {
 
     final private String serviceName = "groovy";
     final private static String RECIPE_DIR_PATH = CommandTestUtils.getPath("src/main/resources/apps/USM/usm/groovySudo");
 
     @Override
     protected String getCloudName() {
         return "azure";
     }
 
     @BeforeClass(alwaysRun = true)
     protected void bootstrap() throws Exception {
         super.bootstrap();
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT)
     public void testSudo() throws IOException, InterruptedException {
         installServiceAndWait(RECIPE_DIR_PATH, serviceName);
         String invokeResult = CommandTestUtils.runCommandAndWait("connect " + getRestUrl()
                 + "; invoke groovy sudo");
         assertTrue("Could not find expected output ('OK') in custom command response", invokeResult.contains("OK"));
         assertTrue("Could not find expected output ('marker.txt') in custom command response", invokeResult.contains("marker.txt"));
 
         uninstallServiceAndWait(serviceName);
 
         super.scanForLeakedAgentNodes();
     }
 
 
     @AfterClass(alwaysRun = true)
     protected void teardown() throws Exception {
         super.teardown();
     }
 
     @Override
     protected boolean isReusableCloud() {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     protected void customizeCloud() throws Exception {
         super.customizeCloud();
        super.getService().getAdditionalPropsToReplace().put("privileged true", "privileged false");
     }
 }
