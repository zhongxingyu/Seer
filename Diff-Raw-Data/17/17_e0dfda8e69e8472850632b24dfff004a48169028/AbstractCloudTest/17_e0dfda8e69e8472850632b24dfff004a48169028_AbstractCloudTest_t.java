 package test.cli.cloudify.cloud;
 
 import java.io.IOException;
 import java.io.InputStream;
import java.util.ArrayList;
 import java.util.HashMap;
import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.TimeUnit;
 
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.DataProvider;
 
 import test.AbstractTest;
 import test.cli.cloudify.CommandTestUtils;
 import test.cli.cloudify.cloud.byon.ByonCloudService;
 import test.cli.cloudify.cloud.ec2.Ec2CloudService;
 import test.cli.cloudify.cloud.hp.HpCloudService;
 
 import com.j_spaces.kernel.JSpaceUtilities;
 
 import framework.report.MailReporterProperties;
 import framework.tools.SimpleMail;
 import framework.utils.LogUtils;
 
 public class AbstractCloudTest extends AbstractTest {
 	
 	private static final Map<String, CloudService> defaultServices = new HashMap<String, CloudService>();
 
 	private static String[][] SUPPORTED_CLOUDS = null;
 	private static final String SUPPORTED_CLOUDS_PROP = "supported-clouds";
 	private static final String BYON = "byon";
 	private static final String OPENSTACK = "openstack";
 	private static final String EC2 = "ec2";
 	private CloudService service;
 	
 	public AbstractCloudTest() {
 		LogUtils.log("Instansiated " + AbstractCloudTest.class.getName());
 	}
 	
 	public void putService(CloudService service) {
 		defaultServices.put(service.getCloudName(), service);
 	}
 	
 	public CloudService getDefaultService(String cloudName) {
 		return defaultServices.get(cloudName);
 	}
 	
 	
 	/**
 	 * set the service CloudService instance to a specific cloud provider.
 	 * all install/uninstall commands will be executed on the specified cloud.
 	 * @param cloudName
 	 * @throws InterruptedException 
 	 * @throws IOException 
 	 */
 	public void setCloudToUse(String cloudName) throws IOException, InterruptedException {
 		LogUtils.log("List of available services are :" + defaultServices.keySet());
 		LogUtils.log("Retrieving service for cloud " + cloudName);
 		service = defaultServices.get(cloudName);
 		if (service == null) {
 			Assert.fail("service for " + cloudName + " is null");
 		}
 		if (!service.isBootstrapped()) {
 			LogUtils.log("service is not bootstrapped, bootstrapping service");
 			service.bootstrapCloud();
 		}
 	}
 	
 	public void setService(CloudService service) {
 		this.service = service;
 	}
 	
 	public CloudService getService() {
 		return service;
 	}
 	
 	@DataProvider(name = "supportedClouds")
 	public String[][] supportedClouds() {
 		return SUPPORTED_CLOUDS;
 	}
 	
 	@DataProvider(name = "supportedCloudsWithoutByon")
 	public String[][] supportedCloudsWithoutByon() {
 		String property = System.getProperty(SUPPORTED_CLOUDS_PROP);
 		String[] clouds = property.split(",");
		List<String> result = new ArrayList<String>();
 		for (int i = 0 ; i < clouds.length ; i++) {
 			if (!clouds[i].equals(BYON)) {
				result.add(clouds[i]);
 			}
 		}
		String[][] resultFinal = new String[result.size()][1];
		for (int i = 0 ; i < result.size() ; i++) {
			resultFinal[i][0] = result.get(i);
		}
 		
		return resultFinal;
 
 		
 	}
 	
 	/**
 	 * Before the suite starts bootstrap all clouds.
 	 * @throws SecurityException 
 	 * @throws NoSuchMethodException 
 	 */
 	@BeforeSuite(alwaysRun = true, enabled = true)
 	public void bootstrapSupportedClouds() throws NoSuchMethodException, SecurityException {
 		
 		String clouds = System.getProperty(SUPPORTED_CLOUDS_PROP);
 		SUPPORTED_CLOUDS = toTwoDimentionalArray(System.getProperty(SUPPORTED_CLOUDS_PROP));
 		setupCloudManagmentMethods();
 		LogUtils.log("bootstrapping to clouds : " + clouds);
         boolean success = false;
 		try {
         	success = bootstrapClouds();
         	if (success) {
         		LogUtils.log("Bootstrapping to clouds finished");
         	}
 		} 
 		finally {
         	if (!success) {
         		teardownClouds();
         		Assert.fail("bootstrap-cloud failed.");
         	}
         }
 		LogUtils.log("finished bootstrapped to clouds : " + clouds);
 	}
 
 	private void setupCloudManagmentMethods() throws NoSuchMethodException, SecurityException {
 		LogUtils.log("Adding byon to services");
 		defaultServices.put(BYON, new ByonCloudService());
 		LogUtils.log("Adding openstack to services");
 		defaultServices.put(OPENSTACK, new HpCloudService());
 		LogUtils.log("Adding ec2 to services");
 		defaultServices.put(EC2, new Ec2CloudService());
 	}
 
 	/**
 	 * After suite ends teardown all bootstrapped clouds.
 	 */
 	@AfterSuite(enabled = true)
 	public void teardownSupportedClouds() {
 		
 		String clouds = System.getProperty(SUPPORTED_CLOUDS_PROP);
 		
 		LogUtils.log("tearing down clouds : " + clouds);
 		
 		teardownClouds();	
 		
 		LogUtils.log("finished tearing down clouds : " + clouds);
 	}
 	
 	private boolean bootstrapClouds() {
 		
 		int numberOfSuccesfullyBootstrappedClouds = 0;
 		
 		for (int j = 0 ; j < SUPPORTED_CLOUDS.length ; j++){
 			String supportedCloud = SUPPORTED_CLOUDS[j][0];
 			try {
 				defaultServices.get(supportedCloud).bootstrapCloud();
 				numberOfSuccesfullyBootstrappedClouds++;
 			}
 			catch (Throwable e) {
 				LogUtils.log("caught an exception while bootstrapping " + supportedCloud, e);
 			}
 		}
 
 		if (numberOfSuccesfullyBootstrappedClouds > 0) return true;
 		return false;	
 	}
 	
 	@Override
 	@AfterMethod
 	public void afterTest() {
 	}
 	
 	@Override
 	@BeforeMethod
 	public void beforeTest() {
 	}
 
 	
 	private void teardownClouds() {
 		
 		for (int j = 0 ; j < SUPPORTED_CLOUDS.length ; j++){
 			String supportedCloud = SUPPORTED_CLOUDS[j][0];
 			try{
 				defaultServices.get(supportedCloud).teardownCloud();
 			}
 			catch (Throwable e) {
 				LogUtils.log("caught an exception while bootstrapping " + supportedCloud, e);
 				sendTeardownCloudFailedMail(supportedCloud, e);
 			}
 		}
 	}
 	
 	/**
 	 * installs a service on a specific cloud and waits for the installation to complete.
 	 * @param servicePath - full path to the -service.groovy file on the local file system.
 	 * @param serviceName - the name of the service.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void installServiceAndWait(String servicePath, String serviceName) throws IOException, InterruptedException {
 		
 		if (service.getRestUrls() == null) {
 			Assert.fail("Test failed becuase the cloud was not bootstrapped properly");
 		}
 		
 		String restUrl = service.getRestUrls()[0];
 		
 		String connectCommand = "connect " + restUrl + ";";
 		String installCommand = new StringBuilder()
 			.append("install-service ")
 			.append("--verbose ")
 			.append("-timeout ")
 			.append(TimeUnit.MILLISECONDS.toMinutes(DEFAULT_TEST_TIMEOUT * 2)).append(" ")
 			.append((servicePath.toString()).replace('\\', '/'))
 			.toString();
 		String output = CommandTestUtils.runCommandAndWait(connectCommand + installCommand);
 		String excpectedResult = serviceName + " service installed successfully";
 		assertTrue(output.toLowerCase().contains(excpectedResult.toLowerCase()));
 	}
 	
 	/**
 	 * installs an application on a specific cloud and waits for the installation to complete.
 	 * @param applicationPath - full path to the -application.groovy file on the local file system.
 	 * @param applicationName - the name of the service.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void installApplicationAndWait(String applicationPath, String applicationName) throws IOException, InterruptedException {
 		installApplication(applicationPath , applicationName, 0 , true , false);
 	}
 	
 	/**
 	 * installs an application on a specific cloud and waits for the installation to complete.
 	 * @param applicationPath - full path to the -application.groovy file on the local file system.
 	 * @param applicationName - the name of the service.
 	 * @param wait - used for determining if to wait for command 
 	 * @param failCommand  - used for determining if the command is expected to fail 
 	 * @param timeout - time in minutes to wait for command to finish. Give non-negative value to override 30 minute default.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void installApplication(String applicationPath, String applicationName,int timeout ,boolean wait ,boolean failCommand) throws IOException, InterruptedException {
 		
 		if (service.getRestUrls() == null) {
 			Assert.fail("Test failed becuase the cloud was not bootstrapped properly");
 		}
 		
 		String restUrl = service.getRestUrls()[0];
 		
 		long timeoutToUse;
 		if(timeout > 0)
 			timeoutToUse = timeout;
 		else
 			timeoutToUse = TimeUnit.MILLISECONDS.toMinutes(DEFAULT_TEST_TIMEOUT * 2);
 		
 		String connectCommand = "connect " + restUrl + ";";
 		String installCommand = new StringBuilder()
 			.append("install-application ")
 			.append("--verbose ")
 			.append("-timeout ")
 			.append(timeoutToUse).append(" ")
 			.append((applicationPath.toString()).replace('\\', '/'))
 			.toString();
 		String output = CommandTestUtils.runCommand(connectCommand + installCommand, wait, failCommand);
 		String excpectedResult = "Application " + applicationName + " installed successfully";
 		if(!failCommand)
 			assertTrue(output.toLowerCase().contains(excpectedResult.toLowerCase()));
 		else
 			assertTrue(output.toLowerCase().contains("operation failed"));
 
 	}
 	
 	/**
 	 * uninstalls a service from a specific cloud and waits for the uninstallation to complete.
 	 * @param serviceName - the name of the service.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void uninstallServiceAndWait(String serviceName) throws IOException, InterruptedException {
 		
 		if (service.getRestUrls() == null) {
 			Assert.fail("Test failed becuase the cloud was not bootstrapped properly");
 		}
 		
 		String restUrl = service.getRestUrls()[0];
 		
 		String connectCommand = "connect " + restUrl + ";";
 		String installCommand = new StringBuilder()
 			.append("uninstall-service ")
 			.append("--verbose ")
 			.append("-timeout ")
 			.append(TimeUnit.MILLISECONDS.toMinutes(DEFAULT_TEST_TIMEOUT * 2)).append(" ")
 			.append(serviceName)
 			.toString();
 		String output = CommandTestUtils.runCommandAndWait(connectCommand + installCommand);
 		String excpectedResult = serviceName + " service uninstalled successfully";
 		assertTrue(output.toLowerCase().contains(excpectedResult.toLowerCase()));
 
 	}
 	
 	/**
 	 * uninstalls an application from a specific cloud and waits for the uninstallation to complete.
 	 * @param applicationName - the name of the application.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void uninstallApplicationAndWait(String applicationName) throws IOException, InterruptedException {
 		
 		if (service.getRestUrls() == null) {
 			Assert.fail("Test failed becuase the cloud was not bootstrapped properly");
 		}
 		
 		String restUrl = service.getRestUrls()[0];
 		
 		String connectCommand = "connect " + restUrl + ";";
 		String installCommand = new StringBuilder()
 			.append("uninstall-application ")
 			.append("--verbose ")
 			.append("-timeout ")
 			.append(TimeUnit.MILLISECONDS.toMinutes(DEFAULT_TEST_TIMEOUT * 2)).append(" ")
 			.append(applicationName)
 			.toString();
 		String output = CommandTestUtils.runCommandAndWait(connectCommand + installCommand);
 		String excpectedResult = "Application " + applicationName + " uninstalled successfully";
 		assertTrue(output.toLowerCase().contains(excpectedResult.toLowerCase()));
 
 		
 	}
 	
 	protected void sendTeardownCloudFailedMail(String cloudName, Throwable error) {
 		
 		if (!isDevMode()) {
 			String url = "";		
 			Properties props = new Properties();
 			InputStream in = this.getClass().getResourceAsStream("mailreporter.properties");
 			try {
 				props.load(in);
 				in.close();
 				System.out.println("mailreporter.properties: " + props);
 			} catch (IOException e) {
 				throw new RuntimeException("failed to read mailreporter.properties file - " + e, e);
 			}
 			
 			String title = "teardown-cloud " + cloudName + " failure";
 			
 	        StringBuilder sb = new StringBuilder();
 	        sb.append("<html>").append("\n");
 	        sb.append("<h2>A failure occurerd while trying to teardown " + cloudName + " cloud.</h2><br>").append("\n");
 	        sb.append("<h4>This may have been caused because bootstrapping to this cloud was unsuccessul, or because of a different exception.<h4><br>").append("\n");
 	        sb.append("<p>here is the exception : <p><br>").append("\n");
 	        sb.append(JSpaceUtilities.getStackTrace(error)).append("\n");
 	        sb.append("<h4>in any case, please make sure the machines are terminated<h4><br>");
 	        sb.append(url).append("\n");
 	        sb.append("</html>");
 	        
 			MailReporterProperties mailProperties = new MailReporterProperties(props);
 	        try {
 				SimpleMail.send(mailProperties.getMailHost(), mailProperties.getUsername(), 
 						mailProperties.getPassword(),
 						title, sb.toString(), 
 						mailProperties.getCloudifyRecipients());
 			} catch (Exception e) {
 				LogUtils.log("Failed sending mail to recipents : " + mailProperties.getCloudifyRecipients());
 			}	
 		}		
 	}
 	
 	private boolean isDevMode() {
 		
 		String user = System.getenv("USER");
 		return ((user == null) || !(user.equals("tgrid")));
 	}
 	
 	private String[][] toTwoDimentionalArray(String property) {
 		
 		String[] clouds = property.split(",");
 		String[][] result = new String[clouds.length][1];
 		for (int i = 0 ; i < clouds.length ; i++) {
 			result[i][0] = clouds[i];
 		}
 		
 		return result;
 	}
 }
