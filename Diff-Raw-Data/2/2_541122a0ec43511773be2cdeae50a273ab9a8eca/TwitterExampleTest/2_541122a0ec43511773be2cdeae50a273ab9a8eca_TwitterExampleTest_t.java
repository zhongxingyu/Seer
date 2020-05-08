 package test.cli.cloudify.cloud.ec2.bigdata;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.type.TypeFactory;
 import org.codehaus.jackson.type.JavaType;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.AbstractTestSupport;
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 import framework.tools.SGTestHelper;
 import framework.utils.AssertUtils;
 import framework.utils.LogUtils;
 import framework.utils.SSHUtils;
 import framework.utils.ScriptUtils;
 import framework.utils.WebUtils;
 
 public class TwitterExampleTest extends NewAbstractCloudTest{
 
 	private String username = "tgrid";
 	private String password = "tgrid";
 	private String restUrl;
 	private String applicationName;
 	private final String applicationFolderName = "bigDataApp";
 	private final String streamingApplicationFolderName = "streaming-bigdata";
 	private final static int REPEATS = 3;
 	private static final String GLOBAL_COUNTER_PROPERTY = "org.openspaces.bigdata.common.counters.GlobalCounter";
 	private final static String ENTRIES_AMOUNT_REST_URL = "/admin/Spaces/Names/space/Spaces/Names/space/RuntimeDetails/CountPerClassName/" + GLOBAL_COUNTER_PROPERTY;
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}
 	
 	@BeforeMethod
 	public void prepareApplication() throws IOException, InterruptedException {
 		String buildDir = SGTestHelper.getBuildDir();
 		File appFolder = new File(buildDir + "/recipes/apps/" + streamingApplicationFolderName);
 		String hostAddress = "127.0.0.1";
 		
 		SSHUtils.runCommand(hostAddress, AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, 
 				"cd " + appFolder + ";" + "mvn install", username, password);
 
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
 	public void testTwitter() throws Exception {
 
 		String applicationPath = ScriptUtils.getBuildPath() + "/recipes/apps/" + streamingApplicationFolderName + "/" + applicationFolderName;
 		File applicationDslFilePath = new File(applicationPath + "/bigDataApp-application.groovy");
 		applicationName = ServiceReader.getApplicationFromFile(applicationDslFilePath).getApplication().getName();
 		LogUtils.log("installing application " + applicationName + " on " + this.getCloudName());
 		
 		installApplicationAndWait(applicationPath, applicationName);
 		
 		LogUtils.log("verifing successful installation");
 		restUrl = getRestUrl();
 		URL cassandraPuAdminUrl = new URL(restUrl + "/admin/ProcessingUnits/Names/big_data_app.cassandra");
 		URL processorPuAdminUrl = new URL(restUrl + "/admin/ProcessingUnits/Names/big_data_app.processor");
 		URL feederPuAdminUrl = new URL(restUrl + "/admin/ProcessingUnits/Names/big_data_app.feeder");
 
 		assertTrue(WebUtils.isURLAvailable(cassandraPuAdminUrl));
 		assertTrue(WebUtils.isURLAvailable(processorPuAdminUrl));
 		assertTrue(WebUtils.isURLAvailable(feederPuAdminUrl));
 		
 		Client client = Client.create(new DefaultClientConfig());
 		final WebResource service = client.resource(this.getRestUrl());
 		String entriesString = service.path(ENTRIES_AMOUNT_REST_URL).get(String.class);
 		Map<String, Object> entriesAmountJsonMap = jsonToMap(entriesString);
 		String entriesAmountString = (String) entriesAmountJsonMap.get(GLOBAL_COUNTER_PROPERTY);
 		int entriesAmount = Integer.parseInt(entriesAmountString);
 		
 		String newEntriesString;
 		Map<String, Object> newEntriesAmountJsonMap;
 		String newEntriesAmountString;
 		int newEntriesAmount;
 		
 		for(int i = 0; i < REPEATS; i++){
 			
 			Thread.sleep(70000);
 			
 			newEntriesString = service.path(ENTRIES_AMOUNT_REST_URL).get(String.class);
 			newEntriesAmountJsonMap = jsonToMap(newEntriesString);
 			newEntriesAmountString = (String) newEntriesAmountJsonMap.get(GLOBAL_COUNTER_PROPERTY);			
 			newEntriesAmount = Integer.parseInt(newEntriesAmountString);
 			
 			AssertUtils.assertTrue("TokenCounter entries are not written to the space. Entries in space: " + newEntriesAmount, newEntriesAmount > entriesAmount);
 			
 			entriesAmount = newEntriesAmount;
 		}
 		
 		uninstallApplicationAndWait(applicationName);
 		
 		super.scanForLeakedAgentNodes();
 	}
 
 	@Override
 	protected void beforeBootstrap() throws Exception {
 
 		/* copy premium license to cloudify-overrides in order to run xap pu's */
 		String overridesFolder = getService().getPathToCloudFolder() + "/upload/cloudify-overrides";
		File cloudifyPremiumLicenseFile = new File(SGTestHelper.getSGTestRootDir() + "/src/main/config/gslicense.xml");
 		FileUtils.copyFileToDirectory(cloudifyPremiumLicenseFile, new File(overridesFolder));
 	}
 
 	@Override
 	protected String getCloudName() {
 		return "ec2";
 	}
 
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}
 	
 	private static Map<String, Object> jsonToMap(final String response) throws IOException {
 		final JavaType javaType = TypeFactory.type(Map.class);
 		ObjectMapper om = new ObjectMapper();
 		return om.readValue(response, javaType);
 	}
 
 }
