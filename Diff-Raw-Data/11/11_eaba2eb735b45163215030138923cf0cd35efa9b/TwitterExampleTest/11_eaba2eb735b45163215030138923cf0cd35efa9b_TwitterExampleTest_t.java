 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.utils.*;
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
import org.cloudifysource.quality.iTests.test.cli.cloudify.security.SecurityConstants;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.type.TypeFactory;
 import org.codehaus.jackson.type.JavaType;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import javax.ws.rs.core.HttpHeaders;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * User: nirb
  * Date: 7/10/13
  */
 public class TwitterExampleTest extends AbstractLocalCloudTest{
 
     private String username = "tgrid";
     private String password = "tgrid";
     private static final String APPLICATION_FOLDER_NAME = "bigDataApp";
     private static final int EXPECTED_NUMBER_OF_UNIQUE_WORDS_IN_MOCK_TWEETS = 84;
     private static final String GLOBAL_COUNTER_PROPERTY = "org.openspaces.bigdata.common.counters.GlobalCounter";
     private final static String ENTRIES_AMOUNT_REST_URL = "/admin/Spaces/Names/space/Spaces/Names/space/RuntimeDetails/CountPerClassName/" + GLOBAL_COUNTER_PROPERTY;
     private static final String BIG_DATA_APP_APPLICATION_GROOVY = "bigDataApp-application.groovy";
 
     private String appName;
     private static final String APP_FOLDER_NAME = "streaming-bigdata";
     private static final String FEEDER_PROPERTIES_FILE_PATH = SGTestHelper.getBuildDir() + "/recipes/apps/" + APP_FOLDER_NAME + "/feeder/src/main/resources/META-INF/spring/feeder.properties";
     private static String backupFeederPropsFilePath;
 
    private static final String TWITTER_CREDENTIALS_FILE_PATH = SecurityConstants.CREDENTIALS_FOLDER + "/twitter/twitter-cred.properties";
     private final Properties twitterProperties = SGTestHelper.getPropertiesFromFile(TWITTER_CREDENTIALS_FILE_PATH);
 
     @BeforeClass(alwaysRun = true)
     protected void init() throws Exception {
         prepareApplication();
     }
 
     @AfterClass(alwaysRun = true)
     protected void teardown() throws Exception {
         IOUtils.replaceFileWithMove(new File(FEEDER_PROPERTIES_FILE_PATH), new File(backupFeederPropsFilePath));
     }
 
     // uses list-feeder and writes to DB.
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testTwitterDefault() throws Exception {
         testTwitter(APP_FOLDER_NAME, appName);
     }
 
     private void testTwitter(String appFolderName, String appName) throws Exception {
         LogUtils.log("installing application " + appFolderName + " on localcloud");
 
         installApplicationAndWait(getApplicationPath(appFolderName), appName, 30);
 
         verifyApplicationInstallation(appName);
 
         final Client client = Client.create(new DefaultClientConfig());
         final WebResource service = client.resource(restUrl);
 
         AssertUtils.repetitiveAssertTrue("Expected GlobalCounter to reach " + EXPECTED_NUMBER_OF_UNIQUE_WORDS_IN_MOCK_TWEETS, new AssertUtils.RepetitiveConditionProvider() {
 
             int prevNumberOfGlobalCounters = 0;
 
             @Override
             public boolean getCondition() {
                 final int numberOfGlobalCounters = getGlobalCounter(service);
                 LogUtils.log("Number of global counters is " + numberOfGlobalCounters +". Expected " + EXPECTED_NUMBER_OF_UNIQUE_WORDS_IN_MOCK_TWEETS);
                 assertTrue("Number of global counters is not expected to decrease. prevNumberOfGlobalCounters=" + prevNumberOfGlobalCounters + " numberOfGlobalCounters="+numberOfGlobalCounters,
                         prevNumberOfGlobalCounters < numberOfGlobalCounters);
                 prevNumberOfGlobalCounters = numberOfGlobalCounters;
                 return numberOfGlobalCounters == EXPECTED_NUMBER_OF_UNIQUE_WORDS_IN_MOCK_TWEETS;
             }
         }, OPERATION_TIMEOUT);
 
         uninstallApplication(appName);
 
     }
 
     private void verifyApplicationInstallation(String appName) throws Exception {
         LogUtils.log("verifing successful installation");
 
         final URL cassandraPuAdminUrl = new URL(restUrl + "/admin/ProcessingUnits/Names/"+appName+".cassandra");
         AbstractTestSupport.assertTrue(WebUtils.isURLAvailable(cassandraPuAdminUrl));
 
         final URL processorPuAdminUrl = new URL(restUrl + "/admin/ProcessingUnits/Names/"+appName+".processor");
         AbstractTestSupport.assertTrue(WebUtils.isURLAvailable(processorPuAdminUrl));
 
         final URL feederPuAdminUrl = new URL(restUrl + "/admin/ProcessingUnits/Names/"+appName+".feeder");
         AbstractTestSupport.assertTrue(WebUtils.isURLAvailable(feederPuAdminUrl));
     }
 
     private int getGlobalCounter(final WebResource service)  {
         final String newEntriesString = service.path(ENTRIES_AMOUNT_REST_URL).header(HttpHeaders.CACHE_CONTROL, "no-cache").get(String.class);
         LogUtils.log("newEntriesString = " + newEntriesString);
         Map<String, Object> newEntriesAmountJsonMap;
         try {
             newEntriesAmountJsonMap = jsonToMap(newEntriesString);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         final String newEntriesAmountString = (String) newEntriesAmountJsonMap.get(GLOBAL_COUNTER_PROPERTY);
         final int newEntriesAmount = Integer.parseInt(newEntriesAmountString);
         return newEntriesAmount;
     }
 
     private static Map<String, Object> jsonToMap(final String response) throws IOException {
         final JavaType javaType = TypeFactory.type(Map.class);
         ObjectMapper om = new ObjectMapper();
         return om.readValue(response, javaType);
     }
 
 
     private void prepareApplication() throws IOException, InterruptedException, DSLException {
         final String recipesDir = getRecipesDir();
 
         File appFolder = new File(recipesDir+ APP_FOLDER_NAME);
         String hostAddress = "127.0.0.1";
 
         String Consumerkey = twitterProperties.getProperty("Consumerkey");
         String ConsumerSecret = twitterProperties.getProperty("ConsumerSecret");
         String AccessToken = twitterProperties.getProperty("AccessToken");
         String AccessTokenSecret = twitterProperties.getProperty("AccessTokenSecret");
         String ConsumerkeyProp = "twitter.oauth.consumerKey";
         String ConsumerSecretProp = "twitter.oauth.consumerSecret";
         String AccessTokenProp = "twitter.oauth.accessToken";
         String AccessTokenSecretProp = "twitter.oauth.accessTokenSecret";
 
         backupFeederPropsFilePath = IOUtils.backupFile(FEEDER_PROPERTIES_FILE_PATH);
         Map<String, String> replaceMap = new HashMap<String, String>();
         replaceMap.put(ConsumerkeyProp + "=", ConsumerkeyProp + "=" + Consumerkey);
         replaceMap.put(ConsumerSecretProp + "=", ConsumerSecretProp + "=" + ConsumerSecret);
         replaceMap.put(AccessTokenProp + "=", AccessTokenProp + "=" + AccessToken);
         replaceMap.put(AccessTokenSecretProp + "=", AccessTokenSecretProp + "=" + AccessTokenSecret);
         IOUtils.replaceTextInFile(new File(FEEDER_PROPERTIES_FILE_PATH), replaceMap);
 
 		SSHUtils.runCommand(hostAddress, AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2,
                 "cd " + appFolder + ";" + "mvn install", username, password);
 
         String appName = ServiceReader.getApplicationFromFile(getApplicationDslFile(appFolder)).getApplication().getName();
         this.appName = appName;
     }
 
     private File getApplicationDslFile(File appFolder) {
         final String applicationPath = getApplicationPath(APP_FOLDER_NAME);
         final File applicationDslFilePath = new File(applicationPath + "/"+ BIG_DATA_APP_APPLICATION_GROOVY);
         return applicationDslFilePath;
     }
 
     private String getRecipesDir() {
         final String recipesDir = SGTestHelper.getBuildDir() + "/recipes/apps/";
         return recipesDir;
     }
 
     private String getApplicationPath(String appFolderName) {
         return getRecipesDir() + appFolderName + "/" + APPLICATION_FOLDER_NAME;
     }
 }
