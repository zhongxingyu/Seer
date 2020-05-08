 package test.cli.cloudify.cloud.ec2;
 
import com.j_spaces.kernel.PlatformVersion;
import framework.testng.annotations.TestConfiguration;
import framework.utils.AssertUtils;
import framework.utils.ScriptUtils;
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.restclient.GSRestClient;
import org.cloudifysource.restclient.RestException;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.storage.file.FileRepository;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 import test.cli.cloudify.cloud.AbstractServicesTest;
 
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
 
 
 public class Ec2ExcludedServicesTest extends AbstractServicesTest {
 
     private static String localPath, remotePath;
     private static Repository localRepo;
     private static Git git;
     private File excludedRecipesDir;
     private static final String STATUS_PROPERTY = "DeclaringClass-Enumerator";
     
     @Override
     protected String getCloudName() {
         return "ec2";
     }
 
     @BeforeClass(alwaysRun = true)
     protected void bootstrap() throws Exception {
         localPath = ScriptUtils.getBuildPath() + "/excludedRecipes";
         remotePath = "https://github.com/CloudifySource/cloudify-recipes.git";
         localRepo = new FileRepository(localPath + "/.git");
         git = new Git(localRepo);
         excludedRecipesDir = new File(localPath + "/services");
         Git.cloneRepository()
                 .setURI(remotePath)
                 .setDirectory(new File(localPath))
                 .call();
         super.bootstrap();
     }
 
     @AfterClass(alwaysRun = true)
     protected void teardown() throws Exception {
         FileUtils.deleteDirectory(new File(localPath));
         super.teardown();
     }
 
     //works
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
     public void testApache() throws Exception {
         testService("apache");
     }
     
     //cant run on localcloud??
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
     public void testCouchbase() throws Exception{
     	testService("couchbase");
     }
 
     //not our recipe. fails.
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void testCouchDBe() throws Exception{
         testService("couchdb");
     }
     //doesn't work
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void testBigInsights() throws Exception{
         testService("biginsights");
     }
     //works
     @TestConfiguration(os = {TestConfiguration.VM.MAC, TestConfiguration.VM.UNIX} )
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
     public void testElasticSearch() throws Exception{
         testService("elasticsearch");
     }
 
     //should not be tested.
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void testGroovyUtils() throws Exception{
         testService("groovy-utils");
     }
     //should work
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
     public void testJboss() throws Exception{
         testService("jboss");
     }
 
     //works
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
     public void testMysql() throws Exception{
         testService("mysql");
     }
 
     //not our recipe. fails.
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void testNginx() throws Exception{
         testService("nginx");
     }
 
     //needs configuration to work.
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void testPlay() throws Exception{
         testService("play");
     }
 
     //not our recipe. fails.
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void testPostgresql() throws Exception{
         testService("postgresql");
     }
 
     //ubuntu only. works
     @TestConfiguration(os = TestConfiguration.VM.UNIX)
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
     public void testPuppet() throws Exception{
         testService("puppet");
     }
 
     //dont work. should work as part of an app.
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void testRails() throws Exception{
         testService("rails");
     }
 
     //Recipe is not finish
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void testRedis() throws Exception{
         testService("redis");
     }
 
     //works
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
     public void testSolr() throws Exception{
         testService("solr");
     }
 
     //should work. takes time to install.
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 3, enabled = true)
     public void testStorm() throws Exception{
         testService("storm");
     }
     //linux only. works
     @TestConfiguration(os = {TestConfiguration.VM.MAC, TestConfiguration.VM.UNIX} )
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
     public void testVertx() throws Exception{
         testService("vertx");
     }
 
     //linux only. doesn't work. needs configuration to work.
     @TestConfiguration(os = {TestConfiguration.VM.MAC, TestConfiguration.VM.UNIX})
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = false)
     public void testWebshere() throws Exception{
         testService("websphere");
     }
 
     //linux only. works
     @TestConfiguration(os = {TestConfiguration.VM.MAC, TestConfiguration.VM.UNIX} )
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
     public void testZookeeper() throws Exception{
         testService("zookeeper");
     }
 
     @Override
    public void testService(String serviceFolderName, String serviceName) throws IOException, InterruptedException, RestException {
         installServiceAndWait(excludedRecipesDir.getAbsolutePath() + "/" + serviceName, serviceName);
 
         String restUrl = getRestUrl();
         GSRestClient client = new GSRestClient("", "", new URL(restUrl), PlatformVersion.getVersionNumber());
         Map<String, Object> entriesJsonMap  = client.getAdminData("ProcessingUnits/Names/default." + serviceName + "/Status");
         String serviceStatus = (String)entriesJsonMap.get(STATUS_PROPERTY);
 
         AssertUtils.assertTrue("service is not intact", serviceStatus.equalsIgnoreCase("INTACT"));
 
         uninstallServiceAndWait(serviceName);
     }
     
     public void testService(String serviceName) throws Exception {
     	testService(serviceName, serviceName);
     }
 
 
 }
 
