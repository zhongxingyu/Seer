 package test.cli.cloudify.github;
 
 import framework.tools.SGTestHelper;
 import framework.utils.GitUtils;
 import framework.utils.LogUtils;
 import org.apache.commons.io.FileUtils;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.Test;
 import test.cli.cloudify.AbstractLocalCloudTest;
 import test.cli.cloudify.CommandTestUtils;
 
 import java.io.File;
 
 import java.io.IOException;
 
 
 
public class GitRestDataBuildTest extends AbstractLocalCloudTest {
     private File gitDir = null;
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void test() throws IOException, InterruptedException {
         String commandOutput = null;
         String url = "https://github.com/OpenSpaces/RESTData.git";
        gitDir = new File(SGTestHelper.getBuildDir() + "/git/");
         GitUtils.pull(url, gitDir);
 
        String restDataFolder = SGTestHelper.getBuildDir() + "/git/";
 
         LogUtils.log("building Rest Data...");
        commandOutput = CommandTestUtils.runLocalCommand("mvn -f " + restDataFolder + "pom.xml package -Dmaven.test.skip", true, false);
         Assert.assertFalse(commandOutput.contains("BUILD FAILED"));
     }
 
    @AfterMethod
     public void afterTest() {
         try {
             FileUtils.forceDelete(gitDir);
         } catch (IOException e) {
             LogUtils.log("Failed to delete git git folder", e);
         }
         super.afterTest();
     }
 
 }
