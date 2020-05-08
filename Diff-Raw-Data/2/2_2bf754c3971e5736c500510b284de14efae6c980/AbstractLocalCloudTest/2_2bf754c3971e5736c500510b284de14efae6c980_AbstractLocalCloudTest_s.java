 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.io.FileUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.cloudifysource.dsl.Application;
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.cloudifysource.quality.iTests.framework.tools.SGTestHelper;
 import org.cloudifysource.quality.iTests.framework.utils.ApplicationInstaller;
 import org.cloudifysource.quality.iTests.framework.utils.AssertUtils;
 import org.cloudifysource.quality.iTests.framework.utils.DumpUtils;
 import org.cloudifysource.quality.iTests.framework.utils.LocalCloudBootstrapper;
 import org.cloudifysource.quality.iTests.framework.utils.LogUtils;
 import org.cloudifysource.quality.iTests.framework.utils.ScriptUtils;
 import org.cloudifysource.quality.iTests.framework.utils.ServiceInstaller;
 import org.cloudifysource.quality.iTests.framework.utils.SetupUtils;
 import org.cloudifysource.quality.iTests.framework.utils.WebUtils;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.restclient.ErrorStatusException;
 import org.cloudifysource.restclient.StringUtils;
 import org.cloudifysource.shell.commands.CLIException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.type.TypeFactory;
 import org.codehaus.jackson.type.JavaType;
 import org.hyperic.sigar.ProcExe;
 import org.hyperic.sigar.ProcState;
 import org.hyperic.sigar.Sigar;
 import org.hyperic.sigar.SigarException;
 import org.hyperic.sigar.SigarPermissionDeniedException;
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.AdminFactory;
 import org.openspaces.admin.application.Applications;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnits;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeSuite;
 
 import com.gigaspaces.internal.sigar.SigarHolder;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 public class AbstractLocalCloudTest extends AbstractTestSupport {
 
 	protected static Admin admin;
 	private static final String COM_GS_HOME = "com.gs.home";
 	protected static String restUrl =  "http://127.0.0.1:" + CloudifyConstants.DEFAULT_REST_PORT;;
 	protected static final String MANAGEMENT_APPLICATION_NAME = CloudifyConstants.MANAGEMENT_APPLICATION_NAME;
 	protected static final String DEFAULT_APPLICATION_NAME = CloudifyConstants.DEFAULT_APPLICATION_NAME;
 
 
 
 	@BeforeSuite(alwaysRun = true)
 	public void bootstrapIfNeeded() throws Exception {
 
 		setGsHome();
 
 		LogUtils.log("================ BeforeSuite Started ===================");
 
 		if (isRestPortResponding()) {
 			LogUtils.log("Detected a localcloud running on the machine. not performing bootstrap");
 				LogUtils.log("Creating admin to connect to existing localcloud");
 				admin = super.createAdminAndWaitForManagement();
 		} else {
 			cleanUpCloudifyLocalDir();
 
 			LocalCloudBootstrapper bootstrapper = new LocalCloudBootstrapper();
 			bootstrapper.verbose(true).timeoutInMinutes(5);
 			bootstrapper.bootstrap();
 
 			LogUtils.log("Creating admin");
 			admin = super.createAdminAndWaitForManagement();
 		}
 
 		LogUtils.log("================ BeforeSuite Ended ===================");
 	}
 
 	private void setGsHome() {
     	String buildDir = SGTestHelper.getBuildDir();
     	File buildDirFile = new File(buildDir);
     	String canonicalPath = null;
 		try {
 			canonicalPath = buildDirFile.getCanonicalPath();
 		} catch (IOException e) {
 			throw new IllegalStateException("Failed to get canonical path to build dir", e);
 		}
     	LogUtils.log("Setting GS home to: " + canonicalPath);
     	//gsHome = System.getProperty(COM_GS_HOME);
 		System.setProperty(COM_GS_HOME, buildDir);
 	}
 
 	@AfterMethod(alwaysRun = true)
 	public void cleanup() throws Exception {
 
 		uninstallAll();
 
 		LogUtils.log("Scanning for leaked processes...");
 		AssertUtils.assertTrue("Found leaking processes after test ended.", killLeakedProcesses() == false);
 	}
 
 	protected void cleanUpCloudifyLocalDir() throws IOException {
 		String userHomeProp = null;
 		if (ScriptUtils.isLinuxMachine()) {
 			userHomeProp = System.getProperty("user.home");
 		} else {
 			// TODO eli - fix this hack. not very nice and generic
 			userHomeProp = System.getProperty("USERPROFILE"); // windows
 			// machine
 		}
 		final File userHomeDir = new File(userHomeProp, ".cloudify");
 		LogUtils.log("Cleaning up cloudify folder under 'user.home' folder at: "
 				+ userHomeDir);
 
 		if (!userHomeDir.exists()) {
 			LogUtils.log(userHomeDir + " does not exist");
 		} else {
 			if (!userHomeDir.isDirectory()) {
 				LogUtils.log(userHomeDir + " is not a directory!");
 			} else {
 				try {
 					FileUtils.cleanDirectory(userHomeDir);
 				} catch (final Exception e) {
 					LogUtils.log("Failed Cleaning .cloudify directory", e);
 				}
 			}
 		}
 	}
 
 
 	protected boolean isRestPortResponding() throws Exception {
 
 		boolean restPortResponding = false;
 
 		final URL restUrl = new URL("http://" + InetAddress.getLocalHost().getHostAddress() + ":" + CloudifyConstants.DEFAULT_REST_PORT);
 		if (WebUtils.isURLAvailable(restUrl)) {
 			restPortResponding = ServiceUtils.isPortOccupied("localhost", CloudifyConstants.DEFAULT_REST_PORT);
 		}
 
 		if (!restPortResponding) {
 			LogUtils.log("Rest port is not responding");
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	protected String getLocalHostIpAddress() {
 		return "127.0.0.1";
 	}
 
 	private static class ProcessDetails {
 
 		private long pid;
 		private String baseName;
 		private String fullName;
 		private String[] args;
 		private long parentPid;
 
 		@Override
 		public String toString() {
 			return "ProcessDetails [pid=" + pid + ", baseName=" + baseName
 					+ ", fullName=" + fullName + ", args="
 					+ Arrays.toString(args) + ", parentPid=" + parentPid + "]";
 		}
 
 	}
 
 	private static final Set<String> suspectProcessNames = new HashSet<String>(
 			Arrays.asList("mongo", "mongod", "mongos", "nc"));
 
 	private static final Set<String> suspectJavaProcessNames = new HashSet<String>(
 			Arrays.asList(
 					"org.codehaus.groovy.tools.GroovyStarter", // groovy script
 																// executable
 					"simplejavaprocess.jar",
 					"org.apache.catalina.startup.Bootstrap",
 					"org.apache.cassandra.thrift.CassandraDaemon"));
 
 	/**
 	 * Scans for running processes that are in the suspect list and kills them.
 	 *
 	 * @return true if found and killed leaked processes
 	 */
 	private boolean killLeakedProcesses() throws SigarException {
 		final Map<Long, ProcessDetails> processTable = createProcessTable();
 
 		boolean failed = false;
 		final Set<Entry<Long, ProcessDetails>> entries = processTable
 				.entrySet();
 		for (final Entry<Long, ProcessDetails> entry : entries) {
 			final long pid = entry.getKey();
 			final ProcessDetails procDetails = entry.getValue();
 
			if (procDetails.baseName.contains("java")) {
 				final String[] args = procDetails.args;
 				for (final String arg : args) {
 					if (suspectJavaProcessNames.contains(arg)) {
 						LogUtils.log("Found a leaking java process (" + arg
 								+ "): " + procDetails);
 						failed = true;
 						if (!SGTestHelper.isDevMode()) {
 							SetupUtils.killProcessesByIDs(new HashSet<String>(
 									Arrays.asList("" + pid)));
 						}
 					}
 				}
 			} else if (suspectProcessNames.contains(procDetails.baseName)) {
 				LogUtils.log("Found a leaking process: " + procDetails);
 				failed = true;
 				if (!SGTestHelper.isDevMode()) {
 					SetupUtils.killProcessesByIDs(new HashSet<String>(Arrays
 							.asList("" + pid)));
 				}
 			}
 
 		}
 		return failed;
 	}
 
 	private Map<Long, ProcessDetails> createProcessTable()
 			throws SigarException {
 		final Sigar sigar = SigarHolder.getSigar();
 		final long[] allpids = sigar.getProcList();
 
 		final Map<Long, ProcessDetails> processDetailsByPid = new HashMap<Long, ProcessDetails>();
 
 		for (final long pid : allpids) {
 			try {
 				final ProcessDetails details = new ProcessDetails();
 				final ProcState state = sigar.getProcState(pid);
 				details.baseName = state.getName();
 				details.parentPid = state.getPpid();
 
 				final ProcExe exe = sigar.getProcExe(pid);
 				details.fullName = exe.getName();
 				details.args = sigar.getProcArgs(pid);
 
 				details.pid = pid;
 				processDetailsByPid.put(pid, details);
 
 			} catch (final SigarPermissionDeniedException e) {
 				// ignore
 			} catch (final SigarException e) {
 				// this often happens for security reasons, as procs from other
 				// users will fail on this.
 				LogUtils.log("Failed to read process details for pid: " + pid
 						+ ". Error was: " + e.getMessage());
 			}
 		}
 
 		return processDetailsByPid;
 	}
 
 
 
 	@AfterSuite(alwaysRun = true)
 	public void teardownIfNeeded() throws IOException, InterruptedException {
 
 		LogUtils.log("================ AfterSuite Started ===================");
 
 		if (SGTestHelper.isDevMode()) {
 			LogUtils.log("Running in dev mode - cloud will not be torn down");
 		} else {
 			LogUtils.log("Tearing-down localcloud");
 			LocalCloudBootstrapper bootstrapper = new LocalCloudBootstrapper();
 			bootstrapper.timeoutInMinutes(15);
 			bootstrapper.setRestUrl(restUrl);
 			bootstrapper.teardown();
 		}
 
 		admin.close();
 
 		LogUtils.log("================ AfterSuite Ended ===================");
 	}
 
 	@Override
 	protected AdminFactory createAdminFactory() {
 
 		final String nicAddress = getLocalHostIpAddress();
 
 		final AdminFactory factory = new AdminFactory();
 		final String locator = nicAddress + ":"
 				+ CloudifyConstants.DEFAULT_LOCALCLOUD_LUS_PORT;
 		LogUtils.log("adding locator to admin : " + locator);
 		factory.addLocator(locator);
 		return factory;
 
 	}
 
 	// This method implementation is used in order to access the admin api
 	// without having to worry about locators issue.
 	protected Map<String, Object> getAdminData(final String relativeUrl)
 			throws CLIException, ErrorStatusException {
 		final String url = getFullUrl("/admin/" + relativeUrl);
 		LogUtils.log("performing http get to url: " + url);
 		final HttpGet httpMethod = new HttpGet(url);
 		return readHttpAdminMethod(httpMethod);
 	}
 
 	private String getFullUrl(final String relativeUrl) {
 		return restUrl + relativeUrl;
 	}
 
 	private Map<String, Object> readHttpAdminMethod(
 			final HttpRequestBase httpMethod) throws CLIException,
 			ErrorStatusException {
 		InputStream instream = null;
 		try {
 			final DefaultHttpClient httpClient = new DefaultHttpClient();
 			final HttpResponse response = httpClient.execute(httpMethod);
 			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
 				LogUtils.log(httpMethod.getURI() + " response code "
 						+ response.getStatusLine().getStatusCode());
 				throw new CLIException(response.getStatusLine().toString());
 			}
 			final HttpEntity entity = response.getEntity();
 			if (entity == null) {
 				final ErrorStatusException e = new ErrorStatusException(
 						"comm_error");
 				LogUtils.log(httpMethod.getURI() + " response entity is null",
 						e);
 				throw e;
 			}
 			instream = entity.getContent();
 			final String responseBody = StringUtils
 					.getStringFromStream(instream);
 			LogUtils.log(httpMethod.getURI() + " http get response: "
 					+ responseBody);
 			final Map<String, Object> responseMap = jsonToMap(responseBody);
 			return responseMap;
 		} catch (final ClientProtocolException e) {
 			LogUtils.log(httpMethod.getURI() + " Rest api error", e);
 			throw new ErrorStatusException("comm_error", e, e.getMessage());
 		} catch (final IOException e) {
 			LogUtils.log(httpMethod.getURI() + " Rest api error", e);
 			throw new ErrorStatusException("comm_error", e, e.getMessage());
 		} finally {
 			if (instream != null) {
 				try {
 					instream.close();
 				} catch (final IOException e) {
 				}
 			}
 			httpMethod.abort();
 		}
 	}
 
 	// returns the number of processing unit instances of the specified service
 	protected int getProcessingUnitInstanceCount(final String absolutePUName)
 			throws CLIException, ErrorStatusException {
 		final String puNameAdminUrl = "processingUnits/Names/" + absolutePUName;
 		final Map<String, Object> mongoProcessingUnitAdminData = getAdminData(puNameAdminUrl);
 		return (Integer) mongoProcessingUnitAdminData.get("Instances-Size");
 	}
 
 	private static Map<String, Object> jsonToMap(final String response)
 			throws IOException {
 		final JavaType javaType = TypeFactory.type(Map.class);
 		final ObjectMapper objectMapper = new ObjectMapper();
 		return objectMapper.readValue(response, javaType);
 	}
 
 	protected String runCommand(final String command) throws IOException,
 			InterruptedException {
 		return CommandTestUtils.runCommandAndWait(command);
 	}
 
 	protected void uninstallApplication(final String applicationName) {
 		try {
 			DumpUtils.dumpLogs(admin);
 			runCommand(connectCommand()
 					+ ";uninstall-application --verbose " + applicationName);
 		} catch (final Exception e) {
 			LogUtils.log("Failed to uninstall " + applicationName, e);
 		}
 	}
 
 	protected void uninstallApplicationIfFound(String applicationName) throws IOException, InterruptedException {
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(restUrl, applicationName);
 		applicationInstaller.waitForFinish(true);
 		applicationInstaller.uninstallIfFound();
 	}
 
 	protected void uninstallService(final String serviceName) throws IOException, InterruptedException {		
         ServiceInstaller serviceInstaller = new ServiceInstaller(restUrl, serviceName);
         serviceInstaller.waitForFinish(true);
         serviceInstaller.uninstall();
 	}
 
 	public void uninstallAll() throws IOException, InterruptedException {
 		if(admin == null){
             LogUtils.log("Admin is null ,cant uninstall applications");
             return;
         }
 		Applications applications = admin.getApplications();
 		for (org.openspaces.admin.application.Application application : applications) {
 			String applicationName = application.getName();
 			if (!applicationName.equals(CloudifyConstants.MANAGEMENT_APPLICATION_NAME)) {
 				ApplicationInstaller installer = new ApplicationInstaller(restUrl, applicationName);
 				try {
 					installer.uninstall();
 				} catch (Throwable t) {
 					LogUtils.log("Failed to uninstall application " + applicationName);
 				}
 			}
 		}
 		ProcessingUnits processingUnits = admin.getProcessingUnits();
 		for (ProcessingUnit processingUnit : processingUnits) {
 			String serviceName = processingUnit.getName();
 			if (!(serviceName.equals("rest") || serviceName.equals("webui") || serviceName.equals("cloudifyManagementSpace"))) {
 				ServiceInstaller installer = new ServiceInstaller(serviceName, serviceName);
 				try {
 					installer.uninstall();
 				} catch (Throwable t) {
 					LogUtils.log("Failed to uninstall service " + serviceName);
 				}
 			}
 		}
 
 	}
 
 	protected void doTest(String applicationPath, String applicationFolderName, String applicationName) throws Exception {
 
 		LogUtils.log("installing application " + applicationName);
 
 		if(applicationPath == null){
 			applicationPath = ScriptUtils.getBuildPath() + "/recipes/apps/" + applicationFolderName;;
 		}
 		else{
 			applicationPath = applicationPath + "/" + applicationFolderName;
 		}
 
 		installApplicationAndWait(applicationPath, applicationName, OPERATION_TIMEOUT/1000);
 
 		if (applicationFolderName.equals("computers")){
 
 			String[] services = {"mysql", "apacheLB", "play"};
 
 			verifyServices(applicationName, services);
 
 			verifyApplicationUrls(applicationName, true);
 		}
 
 		if (applicationFolderName.equals("drupal-babies")){
 
 			String[] services = {"mysql", "drupal"};
 
 			verifyServices(applicationName, services);
 
 			verifyApplicationUrls(applicationName, false);
 		}
 
 		if (applicationFolderName.equals("hadoop-biginsights")){
 
 			String[] services = {"master", "data", "dataOnDemand"};
 
 			verifyServices(applicationName, services);
 
 			verifyApplicationUrls(applicationName, false);
 		}
 
 		if (applicationFolderName.equals("lamp")){
 
 			String[] services = {"mysql", "apache", "apacheLB"};
 
 			verifyServices(applicationName, services);
 
 			verifyApplicationUrls(applicationName, true);
 		}
 
 		if (applicationFolderName.equals("masterslave")){
 
 			String[] services = {"mysql"};
 
 			verifyServices(applicationName, services);
 
 			verifyApplicationUrls(applicationName, false);
 		}
 
 		if (applicationFolderName.equals("travel-lb")){
 
 			String[] services = {"cassandra", "apacheLB", "tomcat"};
 
 			verifyServices(applicationName, services);
 
 			verifyApplicationUrls(applicationName, true);
 		}
 	}
 
 	private void verifyApplicationUrls(String appName, boolean hasApacheLB) {
 
 		Client client = Client.create(new DefaultClientConfig());
 		final WebResource service = client.resource(restUrl);
 
 		if(hasApacheLB){
 
 			String restApacheService = service.path("/admin/ProcessingUnits/Names/" + appName + ".apacheLB/ProcessingUnitInstances/0/ServiceDetailsByServiceId/USM/Attributes/Cloud%20Public%20IP").get(String.class);
 			int urlStartIndex = restApacheService.indexOf(":") + 2;
 			int urlEndIndex = restApacheService.indexOf("\"", urlStartIndex);
 
 			String apacheServiceHostURL = restApacheService.substring(urlStartIndex, urlEndIndex);
 			String apachePort = "8090";
 
 			assertPageExists("http://" + apacheServiceHostURL + ":" + apachePort + "/");
 		}
 	}
 
 	private void verifyServices(String applicationName, String[] services) throws IOException, InterruptedException {
 
 		String command = "connect " + restUrl + ";use-application " + applicationName + ";list-services";
 		String output = CommandTestUtils.runCommandAndWait(command);
 
 		for(String singleService : services){
 			AssertUtils.assertTrue("the service " + singleService + " is not running", output.contains(singleService));
 		}
 	}
 
 	protected void assertPageExists(String url) {
 
 		try {
 			WebUtils.isURLAvailable(new URL(url));
 		} catch (Exception e) {
 			AssertUtils.assertFail(e.getMessage());
 		}
 	}
 
 	protected Application installApplication(final String applicationName) throws IOException, InterruptedException, DSLException {
 
 		final String applicationDir = CommandTestUtils.getPath("src/main/resources/apps/USM/usm/applications/" + applicationName);
 		ApplicationInstaller installer = new ApplicationInstaller(restUrl, applicationName);
 		installer.recipePath(applicationDir);
 		installer.install();
 		return ServiceReader.getApplicationFromFile(new File(applicationDir)).getApplication();
 	}
 
 	protected void installApplicationAndWait(String applicationPath, String applicationName, long timeout) throws IOException, InterruptedException {
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(restUrl, applicationName);
 		applicationInstaller.recipePath(applicationPath);
 		applicationInstaller.waitForFinish(true);
 		applicationInstaller.timeoutInMinutes(timeout);
 		applicationInstaller.install();
 	}
 
 	protected void installService(final String serviceName) {
 		final String serviceDir = CommandTestUtils.getPath("src/main/resources/apps/USM/usm/"
 				+ serviceName);
 		try {
 			ServiceReader.getServiceFromDirectory(new File(serviceDir))
 			.getService();
 			runCommand(connectCommand() + ";install-service --verbose "
 					+ serviceDir);
 		} catch (final FileNotFoundException e) {
 			Assert.fail("Failed to install service", e);
 		} catch (final DSLException e) {
 			Assert.fail("Failed to install service", e);
 		} catch (final IOException e) {
 			Assert.fail("Failed to install service", e);
 		} catch (final InterruptedException e) {
 			Assert.fail("Failed to install service", e);
 		}
 	}
 
 	protected String installServiceAndWait(String servicePath, String serviceName, boolean isExpectedToFail) throws IOException, InterruptedException {
 
 		ServiceInstaller serviceInstaller = new ServiceInstaller(restUrl, serviceName);
 		serviceInstaller.recipePath(servicePath);
 		serviceInstaller.waitForFinish(true);
 		serviceInstaller.expectToFail(isExpectedToFail);
 
 		return serviceInstaller.install();
 	}
 
 	protected String listApplications(boolean expectedFail) {
 		String command = connectCommand() + ";list-applications";
 		try {
 			if (expectedFail) {
 				return CommandTestUtils.runCommandExpectedFail(command);
 			}
 			return CommandTestUtils.runCommandAndWait(command);
 		} catch (IOException e) {
 			Assert.fail("Failed to list applications", e);
 		} catch (InterruptedException e) {
 			Assert.fail("Failed to list applications", e);
 		}
 
 		return null;
 	}
 
 	protected String listInstances(String applicationName, String serviceName, boolean expectedFail){
 		String command = connectCommand() + ";use-application " + applicationName +";list-instances " + serviceName;
 		try {
 			if (expectedFail) {
 				return CommandTestUtils.runCommandExpectedFail(command);
 			}
 			return CommandTestUtils.runCommandAndWait(command);
 		} catch (IOException e) {
 			Assert.fail("Failed to list applications", e);
 		} catch (InterruptedException e) {
 			Assert.fail("Failed to list applications", e);
 		}
 
 		return null;
 	}
 
 	protected String listServices(String applicationName, boolean expectedFail){
 		String command = connectCommand() + ";use-application " + applicationName + ";list-services";
 		try {
 			if (expectedFail) {
 				return CommandTestUtils.runCommandExpectedFail(command);
 			}
 			return CommandTestUtils.runCommandAndWait(command);
 
 		} catch (IOException e) {
 			Assert.fail("Failed to list applications", e);
 		} catch (InterruptedException e) {
 			Assert.fail("Failed to list applications", e);
 		}
 
 		return null;
 	}
 
 	protected String connect(boolean failCommand){
 
 		String output = "no output";
 		try {
 			if (failCommand) {
 				output = CommandTestUtils.runCommandExpectedFail(connectCommand());
 			} else {
 				output = CommandTestUtils.runCommandAndWait(connectCommand());
 			}
 		} catch (IOException e) {
 			Assert.fail("Failed to connect");
 		} catch (InterruptedException e) {
 			Assert.fail("Failed to connect");
 		}
 
 		return output;
 	}
 
 	protected String connectCommand(){
 		return "connect " + restUrl;
 	}
 }
