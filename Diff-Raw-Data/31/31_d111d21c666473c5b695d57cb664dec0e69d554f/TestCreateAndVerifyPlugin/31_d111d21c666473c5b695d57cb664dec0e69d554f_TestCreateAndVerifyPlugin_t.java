 package com.atlassian.sdk.accept;
 
 import static com.atlassian.sdk.accept.SdkHelper.runSdkScript;
 
 import java.io.File;
 import java.io.IOException;
 
 public class TestCreateAndVerifyPlugin extends SdkTestBase
 {
     public void testJIRA() throws IOException, InterruptedException
     {
         createAndVerify("jira");
     }
 
     public void testRefapp() throws IOException, InterruptedException
     {
         createAndVerify("refapp");
     }
 
     public void testConfluence() throws IOException, InterruptedException
     {
         createAndVerify("confluence");
     }
 
     public void testFeCru() throws IOException, InterruptedException
     {
         createAndVerify("fecru");
     }
 
     public void testCrowd() throws IOException, InterruptedException
     {
         createAndVerify("crowd");
     }
 
    public void testBamboo() throws IOException, InterruptedException
    {
        createAndVerify("bamboo");
    }

     private void createAndVerify(String productId)
             throws IOException, InterruptedException
     {
         runSdkScript(sdkHome, baseDir, "atlas-create-" + productId + "-plugin",
                 "-a", "foo-" + productId + "-plugin",
                 "-g", "com.example",
                 "-p", "com.example.foo",
                 "-v", "1.0-SNAPSHOT",
                 "--non-interactive");
 
         File appDir = new File(baseDir, "foo-" + productId + "-plugin");
         assertTrue(appDir.exists());
 
         runSdkScript(sdkHome, appDir, "atlas-integration-test");
 
         File pluginJar = new File(new File(appDir, "target"), "foo-" + productId + "-plugin-1.0-SNAPSHOT.jar");
         assertTrue(pluginJar.exists());
     }
 }
