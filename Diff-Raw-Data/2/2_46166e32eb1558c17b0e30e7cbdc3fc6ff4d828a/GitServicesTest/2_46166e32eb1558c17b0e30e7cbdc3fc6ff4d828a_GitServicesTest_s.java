 package org.cloudifysource.quality.iTests.test.cli.cloudify.backwards;
 
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.utils.JGitUtils;
 import iTests.framework.utils.ScriptUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.AbstractLocalCloudTest;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.io.IOException;
 
 /**
  *
  * @author Eli Polonsky
  */
 public class GitServicesTest extends AbstractLocalCloudTest {
 
    private static String localGitRepoPath;
 
    private static String BRANCH_NAME = SGTestHelper.getBranchName();
 
    @BeforeClass(alwaysRun = true)
    protected void bootstrap() throws Exception {
       localGitRepoPath = ScriptUtils.getBuildPath() + "/git-recipes-" + this.getClass().getSimpleName() ;
       String remotePath = "https://github.com/CloudifySource/cloudify-recipes.git";
      JGitUtils.clone(localGitRepoPath, remotePath, "2_6_2");
    }
 
    @Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
    public void testTomcat() throws IOException, InterruptedException {
       installServiceAndWait(localGitRepoPath + "/services/tomcat", "tomcat", false);
    }
 
 
 }
