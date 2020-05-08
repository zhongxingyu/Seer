 package org.cloudifysource.quality.iTests.test.cli.cloudify.recipes.excluded;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.quality.iTests.framework.testng.annotations.TestConfiguration;
 import org.cloudifysource.quality.iTests.framework.utils.LogUtils;
 import org.cloudifysource.quality.iTests.framework.utils.ScriptUtils;
 import org.cloudifysource.quality.iTests.framework.utils.ServiceInstaller;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.AbstractLocalCloudTest;
 import org.eclipse.jgit.api.Git;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 
 public class ExcludedServicesTest extends AbstractLocalCloudTest { 
 
 	private static String localGitRepoPath ;
 	
     @BeforeClass(alwaysRun = true)
     public void cloneRecipesRepository() throws Exception{
    	localGitRepoPath = ScriptUtils.getBuildPath() + "/git-recipes";
     	
 	    if (!new File(localGitRepoPath).exists()) {
 	    	String remotePath = "https://github.com/CloudifySource/cloudify-recipes.git";
 	    	
 	    	Git.cloneRepository()
 	    			.setURI(remotePath)
 	    			.setDirectory(new File(localGitRepoPath))
 	    			.call();	    	
 	    }    	
     }
 
     //should work
     @TestConfiguration(os = {TestConfiguration.VM.MAC, TestConfiguration.VM.UNIX} )
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installApache() throws Exception{
         doTest("apache");
     }
 
 
     //cant run on localcloud??
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installCouchbase() throws Exception{
         doTest("couchbase");
     }
 
     //Recipe is not finish
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installCouchDB() throws Exception{
         doTest("couchdb");
     }
     //no groovy file. not a service?
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installBigInsights() throws Exception{
         doTest("biginsights");
     }
     //linux only
     @TestConfiguration(os = {TestConfiguration.VM.MAC, TestConfiguration.VM.UNIX} )
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installElasticsearch() throws Exception{
         doTest("elasticsearch");
     }
 
     //dont work
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installGroovyUtils() throws Exception{
         doTest("groovy-utils");
     }
     //dont work
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installJboss() throws Exception{
         doTest("jboss");
     }
 
     //does not work on our linux boxes (File system loop detected - need to investigate)g
    @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = disalbe)
     public void installMysql() throws Exception{
         doTest("mysql");
     }
 
     //dont work
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installNginx() throws Exception{
         doTest("nginx");
     }
 
     //dont work
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installPlay() throws Exception{
         doTest("play");
     }
 
     //dont work
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installPostgresql() throws Exception{
         doTest("postgresql");
     }
 
     //ubuntu only
     @TestConfiguration(os = TestConfiguration.VM.UNIX)
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installPuppet() throws Exception{
         doTest("puppet");
     }
 
     //dont work
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installRails() throws Exception{
         doTest("rails");
     }
 
     //Recipe is not finish
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installRedis() throws Exception{
         doTest("redis");
     }
 
     //should work
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installSolr() throws Exception{
         doTest("solr");
     }
 
     //didnt run it
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installStorm() throws Exception{
         doTest("storm");
     }
     //linux only
     @TestConfiguration(os = {TestConfiguration.VM.MAC, TestConfiguration.VM.UNIX} )
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installVertx() throws Exception{
         doTest("vertx");
     }
 
     //linux only
     @TestConfiguration(os = {TestConfiguration.VM.MAC, TestConfiguration.VM.UNIX})
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installWebshere() throws Exception{
         doTest("websphere");
     }
 
     //linux only
     @TestConfiguration(os = {TestConfiguration.VM.MAC, TestConfiguration.VM.UNIX} )
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void installZookeeper() throws Exception{
         doTest("zookeeper");
     }
 
     private void doTest(String recipeName) throws IOException, InterruptedException {
     	installServiceAndWait(localGitRepoPath + "/services/" + recipeName, recipeName, false);
     	uninstallService(recipeName);
     }
 
     private void doTest(final String recipeName, final int timeoutInMinutes) throws IOException, InterruptedException {
         installServiceAndWait(localGitRepoPath + "/services/" + recipeName, recipeName, false, timeoutInMinutes);
         uninstallService(recipeName);
     }
 
     @AfterClass(alwaysRun = true)
     public void afterClass() throws Exception {
 
        try {
            FileUtils.deleteDirectory(new File(localGitRepoPath));
        } catch (final Exception e) {
            LogUtils.log("Failed to delete directory : " + localGitRepoPath, e);
        }
 
     }
 }
 
