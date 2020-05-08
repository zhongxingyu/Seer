 package test.cli.cloudify;
 
 import static framework.utils.LogUtils.log;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.UnknownHostException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
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
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.gsc.GridServiceContainers;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeMethod;
 
 import test.AbstractTest;
 import test.cli.cloudify.CommandTestUtils.ProcessResult;
 
 import com.gigaspaces.internal.sigar.SigarHolder;
 
 import framework.utils.ApplicationInstaller;
 import framework.utils.DumpUtils;
 import framework.utils.LogUtils;
 import framework.utils.PortConnectionUtils;
 import framework.utils.ScriptUtils;
 import framework.utils.ServiceInstaller;
 import framework.utils.SetupUtils;
 import framework.utils.TeardownUtils;
 
 public class AbstractLocalCloudTest extends AbstractTest {
 
 	final int BOOTSTRAP_RETRIES_BEFOREMETHOD = 2;
 	protected final int WAIT_FOR_TIMEOUT_SECONDS = 60;
 	private final int HTTP_STATUS_OK = 200;
 	protected final int restPort = 8100;
 	protected static String restUrl = null;
 	protected static final String MANAGEMENT_APPLICATION_NAME = "management";
 	protected static final String DEFAULT_APPLICATION_NAME = CloudifyConstants.DEFAULT_APPLICATION_NAME;
 
 	protected boolean isDevEnv = false;
 
 	protected boolean isSecured = false;
 	protected String user = null;
 	protected String password = null;
 	protected final int securedRestPort = 8100;
 	protected static String securedRestUrl = null;
 	
 	protected boolean checkIsDevEnv() {
 		if (this.isDevEnv) {
 			return true;
 		}
 
 		final String val = System.getenv("DEV_ENV");
 		if (val != null) {
 			if (val.equalsIgnoreCase("true")) {
 				return true;
 			}
 		}
 
 		final String propVal = System.getProperty("localcloud.DEV_ENV");
 		if (propVal != null) {
 			if (propVal.equalsIgnoreCase("true")) {
 				return true;
 			}
 		}
 
 		return false;
 
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
 				FileUtils.cleanDirectory(userHomeDir);
 			}
 		}
 	}
 
 	@Override
 	@BeforeMethod
 	public void beforeTest() throws TimeoutException, InterruptedException {
 
 		LogUtils.log("Test Configuration Started: " + this.getClass());
 
 		if (admin != null) {
 			LogUtils.log("Admin has not been closed properly in the previous test. Closing old admin");
 			admin.close();
 			admin = null;
 		}
 
 		restUrl = "http://" + getLocalHostIpAddress() + ":" + restPort;
 		
 		if (checkIsDevEnv()) {
 			LogUtils.log("Local cloud test running in dev mode, will use existing localcloud");
 		} else {
 			for (int i = 0; i < BOOTSTRAP_RETRIES_BEFOREMETHOD; i++) {
 
 				try {
 					if (!isRequiresBootstrap()) {
 						break;
 					}
 
 					cleanUpCloudifyLocalDir();
 
 					LogUtils.log("Tearing-down existing localclouds");
 					final ProcessResult teardownResult = CommandTestUtils
 							.runCloudifyCommandAndWait("teardown-localcloud -force -timeout 15");
 					if (teardownResult.getExitcode() != 0) {
 						final String output = teardownResult.getOutput();
 						if (!checkOutputForExceptions(output)) {
 							// we assume that if teardown failed but no
 							// exceptions were found in the output
 							// then the reason was because no cloud was found.
 							LogUtils.log("teardown failed because no cloud was found. proceeding with bootstrap.");
 						} else {
 							Assert.fail("Failed to teardown local cloud. output = "
 									+ output);
 						}
 					}
 
 					final ProcessResult bootstrapResult;
 
 					bootstrapResult = CommandTestUtils
 							.runCloudifyCommandAndWait("bootstrap-localcloud --verbose -timeout 15");
 
 					LogUtils.log(bootstrapResult.getOutput());
 					Assert.assertEquals(bootstrapResult.getExitcode(), 0,
 							"Bootstrap failed");
 				} catch (final Throwable t) {
 					LogUtils.log("Failed to bootstrap localcloud. iteration="
 							+ i, t);
 
 					if (i >= BOOTSTRAP_RETRIES_BEFOREMETHOD - 1) {
 						Assert.fail("Failed to bootstrap localcloud after "
 								+ BOOTSTRAP_RETRIES_BEFOREMETHOD + " retries.",
 								t);
 					}
 				}
 
 			}
 		}
 
 		Assert.assertFalse(isRequiresBootstrap(),
 				"Cannot establish connection with localcloud");
 
 		this.admin = super.createAdmin();
 		final boolean foundLookupService = admin.getLookupServices().waitFor(1,
 				WAIT_FOR_TIMEOUT_SECONDS, TimeUnit.SECONDS);
 		Assert.assertTrue(foundLookupService,
 				"Failed to discover lookup service after "
 						+ WAIT_FOR_TIMEOUT_SECONDS + " seconds");
 
 		final boolean foundMachine = admin.getMachines().waitFor(1,
 				WAIT_FOR_TIMEOUT_SECONDS, TimeUnit.SECONDS);
 		Assert.assertTrue(foundMachine, "Failed to discover machine after "
 				+ WAIT_FOR_TIMEOUT_SECONDS + " seconds");
 		final Machine[] machines = admin.getMachines().getMachines();
 		Assert.assertTrue(machines.length >= 1, "Expected at least one machine");
 		final Machine machine = machines[0];
 		System.out.println("Machine ["
 				+ machine.getHostName()
 				+ "], "
 				+ "TotalPhysicalMem ["
 				+ machine.getOperatingSystem().getDetails()
 				.getTotalPhysicalMemorySizeInGB()
 				+ "GB], "
 				+ "FreePhysicalMem ["
 				+ machine.getOperatingSystem().getStatistics()
 				.getFreePhysicalMemorySizeInGB() + "GB]]");
 
 	}
 
 	protected boolean checkOutputForExceptions(final String output) {
 		if (output.contains("Exception")) {
 			return true;
 		}
 		return false;
 	}
 
 	protected boolean isRequiresBootstrap() {
 		boolean requiredBootstrap = true;
 		try {
 			final ProcessResult connectResult = CommandTestUtils
 					.runCloudifyCommandAndWait(connectCommand());
 			final boolean leakedProcessesFound = killLeakedProcesses();
 			final boolean restPortResponding;
 			
 			if(isSecured){
 				restPortResponding = PortConnectionUtils.isPortOpen("localhost", securedRestPort);
 			}
 			else{
 				restPortResponding = PortConnectionUtils.isPortOpen("localhost", restPort);
 			}
 			
 			if (connectResult.getExitcode() != 0) {
 				LogUtils.log("connect " + getLocalHostIpAddress() + " failed");
 			}
 			if (leakedProcessesFound) {
 				LogUtils.log("Leaked process found");
 			}
 			if (!restPortResponding) {
 				LogUtils.log("Rest port is not responding");
 			}
 
 			requiredBootstrap = !restPortResponding || leakedProcessesFound
 					|| connectResult.getExitcode() != 0;
 
 		} catch (final UnknownHostException e) {
 			Assert.fail("Failed to check if requires bootstrap", e);
 		} catch (final IOException e) {
 			Assert.fail("Failed to check if requires bootstrap", e);
 		} catch (final InterruptedException e) {
 			Assert.fail("Failed to check if requires bootstrap", e);
 		} catch (final SigarException e) {
 			Assert.fail("Failed to check if requires bootstrap", e);
 		}
 
 		return requiredBootstrap;
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
 						if (!checkIsDevEnv()) {
 							SetupUtils.killProcessesByIDs(new HashSet<String>(
 									Arrays.asList("" + pid)));
 						}
 					}
 				}
 			} else if (suspectProcessNames.contains(procDetails.baseName)) {
 				LogUtils.log("Found a leaking process: " + procDetails);
 				failed = true;
 				if (!checkIsDevEnv()) {
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
 
 	@Override
 	@AfterMethod(alwaysRun = true)
 	public void afterTest() throws Exception {
 
 		if (admin != null) {
 			try {
 				try {
 					DumpUtils.dumpLogs(admin);
 				} catch (final Throwable t) {
 					log("failed to dump logs", t);
 				}
 
 				TeardownUtils.snapshot(admin);
 				uninstallAllRunningServices(admin);
 			} finally {
 				admin.close();
 				admin = null;
 			}
 		}
 		LogUtils.log("Test Finished : " + this.getClass());
 	}
 
 	@AfterSuite(alwaysRun = true)
 	public void afterSuite() {
 		if (checkIsDevEnv()) {
 			LogUtils.log("Running in dev mode - cloud will not be torn down");
 		} else {
 			try {
 				LogUtils.log("Tearing-down localcloud");
 				final ProcessResult teardownResult = CommandTestUtils
 						.runCloudifyCommandAndWait("teardown-localcloud -force -timeout 15");
 				final String output = teardownResult.getOutput();
 				if (!checkOutputForExceptions(output)) {
 					// we assume that if teardown failed but no exceptions were
 					// found in the output
 					// then the reason was because no cloud was found.
 					LogUtils.log("teardown failed because no cloud was found. proceeding with bootstrap.");
 				} else {
 					Assert.fail("Failed to teardown local cloud. output = "
 							+ output);
 				}
 			} catch (final Exception e) {
 				log("Exception during teardown", e);
 			}
 
 			try {
 				TeardownUtils.teardownAll(admin);
 			} catch (final Throwable t) {
 				log("failed to teardown", t);
 			}
 			if (admin != null) {
 				admin.close();
 			}
 			admin = null;
 		}
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
 			if (response.getStatusLine().getStatusCode() != HTTP_STATUS_OK) {
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
 			e.printStackTrace();
 		}
 	}
 	
 	protected void uninstallApplicationIfFound(String applicationName) throws IOException, InterruptedException {
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(restUrl, applicationName);
 		applicationInstaller.waitForFinish(true);
 		applicationInstaller.cloudifyUsername(user);
 		applicationInstaller.cloudifyPassword(password);
 		applicationInstaller.uninstallIfFound();
 	}
 
 	protected void uninstallService(final String serviceName) {
 		try {
 			DumpUtils.dumpLogs(admin);
 			runCommand("connect " + restUrl + ";uninstall-service "
 					+ serviceName);
 		} catch (final Exception e) {
 			LogUtils.log("Failed to uninstall " + serviceName, e);
 			e.printStackTrace();
 		}
 	}
 
 	public void uninstallAllRunningServices(final Admin admin) {
 
 		for (final ProcessingUnit pu : admin.getProcessingUnits()
 				.getProcessingUnits()) {
 			if (!pu.getName().equals("webui") && !pu.getName().equals("rest")
 					&& !pu.getName().equals("cloudifyManagementSpace")) {
 				if (!pu.undeployAndWait(90, TimeUnit.SECONDS)) {
 					LogUtils.log("Failed to uninstall " + pu.getName());
 					// kill all GSCs
 					for (final GridServiceAgent gsa : admin
 							.getGridServiceAgents()) {
 						final GridServiceContainers gridServiceContainers = gsa
 								.getMachine().getGridServiceContainers();
 						for (final GridServiceContainer gsc : gridServiceContainers) {
 							if (gsc.getExactZones().isStasfies(
 									pu.getRequiredContainerZones())) {
 								try {
 									log("killing GSC [ID:"
 											+ gsc.getAgentId()
 											+ "] [PID: "
 											+ gsc.getVirtualMachine()
 													.getDetails().getPid()
 											+ " ]");
 									gsc.kill();
 								} catch (final Exception e) {
 									log("GSC kill failed - " + e, e);
 								}
 							}
 						}
 					}
 				} else {
 					LogUtils.log("Uninstalled service: " + pu.getName()
 							+ " Please uninstall-application in test!");
 				}
 			}
 		}
 	}
 
 	protected Application installApplication(final String applicationName) {
 
 		final String applicationDir = CommandTestUtils
 				.getPath("src/main/resources/apps/USM/usm/applications/" + applicationName);
 
 		try {
 			final Application app = ServiceReader.getApplicationFromFile(
 					new File(applicationDir)).getApplication();
 			runCommand(connectCommand() + ";install-application --verbose "
 					+ applicationDir);
 			return app;
 		} catch (final IOException e) {
 			Assert.fail("Failed to install applicaiton", e);
 		} catch (final DSLException e) {
 			Assert.fail("Failed to install applicaiton", e);
 		} catch (final InterruptedException e) {
 			Assert.fail("Failed to install applicaiton", e);
 		}
 
 		return null;
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
 	
 	protected String installServiceAndWait(String servicePath, String serviceName, int timeout, boolean isExpectedToFail) throws IOException, InterruptedException {
 
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
 
 		if(isSecured){
 			
 			StringBuilder builder = new StringBuilder();
 			
 			builder.append("connect ");
 			
 			if(user != null){
 				builder.append("-user " + user + " ");
 			}
 			
 			if(password != null){
 				builder.append("-password " + password + " ");
 			}
 			
 			builder.append(securedRestUrl);
 			
 			return builder.toString();
 		}
 
 		return("connect " + restUrl);
 	}
 }
