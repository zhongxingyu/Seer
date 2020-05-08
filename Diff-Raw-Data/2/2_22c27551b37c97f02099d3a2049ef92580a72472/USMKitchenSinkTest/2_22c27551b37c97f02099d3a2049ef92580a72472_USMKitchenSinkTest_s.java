 package test.cli.cloudify;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 import java.util.zip.ZipFile;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.dsl.internal.packaging.PackagingException;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.cloudifysource.restclient.ErrorStatusException;
 import org.cloudifysource.restclient.GSRestClient;
 import org.cloudifysource.restclient.RestException;
 import org.cloudifysource.shell.commands.CLIException;
 import org.cloudifysource.shell.rest.RestAdminFacade;
 import org.cloudifysource.usm.USMException;
 import org.cloudifysource.usm.USMUtils;
 import org.hyperic.sigar.SigarException;
 import org.junit.Assert;
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventListener;
 import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventListener;
 import org.openspaces.admin.internal.esm.DefaultElasticServiceManager;
 import org.openspaces.admin.internal.gsa.DefaultGridServiceAgent;
 import org.openspaces.admin.internal.gsm.DefaultGridServiceManager;
 import org.openspaces.admin.internal.lus.DefaultLookupService;
 import org.openspaces.admin.internal.pu.DefaultProcessingUnitInstance;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.openspaces.admin.pu.ProcessingUnitType;
 import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
 import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventListener;
 import org.openspaces.admin.zone.Zone;
 import org.openspaces.pu.service.ServiceDetails;
 import org.openspaces.pu.service.ServiceMonitors;
 import org.testng.annotations.Test;
 
 import test.usm.USMTestUtils;
 
 import com.gigaspaces.internal.sigar.SigarHolder;
 import com.gigaspaces.log.AllLogEntryMatcher;
 import com.gigaspaces.log.ContinuousLogEntryMatcher;
 import com.gigaspaces.log.LogEntries;
 import com.gigaspaces.log.LogEntry;
 import com.gigaspaces.log.LogProcessType;
 import com.j_spaces.kernel.PlatformVersion;
 
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 
 public class USMKitchenSinkTest extends AbstractLocalCloudTest {
 
 	final private String RECIPE_DIR_PATH = CommandTestUtils
 			.getPath("src/main/resources/apps/USM/usm/kitchensink");
 
 	// set in checkMonitors
 	private long actualPid;
 
 	private static final String[] EXPECTED_STARTUP_EVENT_STRINGS = {
 			"init fired Test Property number 1", "init external class",
 			"init Helper Field", "application name is: default",
 			"Instantiated default.kitchensink-service",
 			"preInstall fired Test Property number 2", "install event fired",
 			"postInstall fired Test Property number 1",
 			"preStart fired Test Property number 2", "postStart fired" };
 	private static final String[] EXPECTED_SHUTDOWN_EVENT_STRINGS = {
 			"preStop fired", "String_with_Spaces", "postStop fired",
 			"shutdown fired" };
 
 	private static final String[] EXPECTED_DETAILS_FIELDS = { "stam", "SomeKey" };
 
 	private static final String[] EXPECTED_MONITORS_FIELDS = {
 			CloudifyConstants.USM_MONITORS_CHILD_PROCESS_ID,
 			CloudifyConstants.USM_MONITORS_ACTUAL_PROCESS_ID, "NumberTwo",
 			"NumberOne" };
 
 	private static final String[] EXPECTED_PROCESS_PRINTOUTS = { "Opening port:" };
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testKitchenSink() throws IOException, InterruptedException,
 			PackagingException, DSLException, RestException {
 
 		installService();
 
 		final ProcessingUnit pu = findPU();
 
 		ProcessingUnitInstance pui = findPUI(pu);
 
 		// check port is open
 		final String host = pui.getGridServiceContainer().getMachine()
 				.getHostAddress();
 
 		assertTrue(
 				"Process port is not open! Process did not start as expected",
 				isPortOpen(host));
 
 		long pid = pui.getGridServiceContainer().getVirtualMachine()
 				.getDetails().getPid();
 
 		ContinuousLogEntryMatcher matcher = new ContinuousLogEntryMatcher(
 				new AllLogEntryMatcher(), new AllLogEntryMatcher());
 
 		// run tests
 		checkManagementComponentPidsAreEqual();
 
 		checkManagementServicesPidsAreEqual();
 
 		validateManagementZones();
 
 		checkForStartupPrintouts(pui, pid, matcher);
 
 		checkDetails(pui);
 
 		checkMonitors(pui);
 
 		checkCustomCommands();
 
 		checkServiceType();
 
 		checkPUDump();
 
 		verifyUninstallManagementFails();
 
 		final long initialActualPid = this.actualPid;
 
 		checkKillGSC(pid, host, pu);
 
 		// pui was restarted, so find the new one
 		pui = findPUI(pu);
 		pid = pui.getGridServiceContainer().getVirtualMachine().getDetails()
 				.getPid();
 
 		// this will reset the actualPid
 		checkMonitors(pui);
 		assertEquals(
 				"The actual PID value should not change after a GSC restart",
 				initialActualPid, this.actualPid);
 
 		// reset the matcher
 		matcher = new ContinuousLogEntryMatcher(new AllLogEntryMatcher(),
 				new AllLogEntryMatcher());
 
 		// check that previous log entries have been printed to this log
 		logger.info("Checking that previous process printouts are printed to the new GSC logs");
 		sleep(5000);
 		checkForPrintouts(pui, pid, matcher, EXPECTED_PROCESS_PRINTOUTS);
 
 		// reset the matcher
 		matcher = new ContinuousLogEntryMatcher(new AllLogEntryMatcher(),
 				new AllLogEntryMatcher());
 		// check that the process printouts only appear once - to make sure that
 		// additional process invocations were not still in the same file
 		final int numOfStartupEntries = calculateNumberOfRecurrencesInGSCLog(
 				pui, pid, matcher, EXPECTED_PROCESS_PRINTOUTS[0]);
 		assertEquals(
 				"Process startup log entries should only appear once in the log file",
 				1, numOfStartupEntries);
 
 		final boolean uninstallResult = pu.undeployAndWait(OPERATION_TIMEOUT,
 				TimeUnit.MILLISECONDS);
 		assertTrue("Undeploy failed", uninstallResult);
 
 		// test shutdown events
 		matcher = new ContinuousLogEntryMatcher(new AllLogEntryMatcher(),
 				new AllLogEntryMatcher());
 		checkForShutdownPrintouts(pui, pid, matcher);
 
 		// check port closed.
 		assertTrue(
 				"Process port is still open! Process did not shut down as expected",
 				!isPortOpen(host));
 
 	}
 
 	private void verifyUninstallManagementFails() throws IOException,
 			InterruptedException {
 		LogUtils.log("Verifing management undeployment fails when excecuted from the CLI");
 		final String commandOUtput = CommandTestUtils
 				.runCommandExpectedFail("connect "
 						+ AbstractLocalCloudTest.restUrl + "; "
 						+ "uninstall-application "
 						+ CloudifyConstants.MANAGEMENT_APPLICATION_NAME + "; "
 						+ "exit");
 		assertTrue(commandOUtput
 				.contains("Management application can not be undeployed"));
 
 		LogUtils.log("Verifing management undeployment fails when excecuted using a direct rest api call");
 		final RestAdminFacade cloudifyAdminFacade = new RestAdminFacade();
 		try {
 			cloudifyAdminFacade.doConnect("", "",
					AbstractLocalCloudTest.restUrl);
 			cloudifyAdminFacade.uninstallApplication(
 					CloudifyConstants.MANAGEMENT_APPLICATION_NAME, 10);
 			AssertFail("Request to uinstall management should have thrown an exception");
 		} catch (final CLIException e) {
 			assertTrue(
 					"The uninstall management response should contain the proper error message",
 					e.getMessage().contains(
 							"cannot_uninstall_management_application"));
 		}
 
 	}
 
 	private void checkManagementServicesPidsAreEqual() throws RemoteException {
 		final DefaultProcessingUnitInstance rest = (DefaultProcessingUnitInstance) admin
 				.getProcessingUnits().getProcessingUnit("rest").getInstances()[0];
 		final DefaultProcessingUnitInstance space = (DefaultProcessingUnitInstance) admin
 				.getProcessingUnits()
 				.getProcessingUnit("cloudifyManagementSpace").getInstances()[0];
 		final DefaultProcessingUnitInstance webui = (DefaultProcessingUnitInstance) admin
 				.getProcessingUnits().getProcessingUnit("webui").getInstances()[0];
 
 		final long restPid = rest.getJVMDetails().getPid();
 		final long spacePid = space.getJVMDetails().getPid();
 		final long webuiPid = webui.getJVMDetails().getPid();
 
 		LogUtils.log("Comparing management component pids");
 		assertTrue(
 				"rest and managementSpace did not start on the same process",
 				restPid == spacePid);
 		assertTrue("rest and webui did not start on the same process",
 				restPid == webuiPid);
 	}
 
 	private void checkManagementComponentPidsAreEqual() throws RemoteException {
 		final DefaultGridServiceManager gsm = (DefaultGridServiceManager) admin
 				.getGridServiceManagers().waitForAtLeastOne(5000,
 						TimeUnit.MILLISECONDS);
 		final DefaultGridServiceAgent gsa = (DefaultGridServiceAgent) admin
 				.getGridServiceAgents().waitForAtLeastOne(5000,
 						TimeUnit.MILLISECONDS);
 		final DefaultLookupService lus = (DefaultLookupService) admin
 				.getLookupServices().getLookupServices()[0];
 		final DefaultElasticServiceManager esm = (DefaultElasticServiceManager) admin
 				.getElasticServiceManagers().waitForAtLeastOne(5000,
 						TimeUnit.MILLISECONDS);
 
 		final long gsmPid = gsm.getJVMDetails().getPid();
 		final long gsaPid = gsa.getJVMDetails().getPid();
 		final long lusPid = lus.getJVMDetails().getPid();
 		final long esmPid = esm.getJVMDetails().getPid();
 
 		LogUtils.log("Comparing management component pids");
 		assertTrue("gsm and gsa did not start on the same process",
 				gsmPid == gsaPid);
 		assertTrue("gsm and lus did not start on the same process",
 				gsmPid == lusPid);
 		assertTrue("gsm and esm did not start on the same process",
 				gsmPid == esmPid);
 	}
 
 	private void validateManagementZones() {
 		// since the gsm starts in the same process as gsa,lus and esm, they
 		// share the same zone property
 		// and thus it is enough to check only the GSM zones.
 		final DefaultGridServiceManager gsm = (DefaultGridServiceManager) admin
 				.getGridServiceManagers().waitForAtLeastOne(5000,
 						TimeUnit.MILLISECONDS);
 
 		final List<String> gsmZones = new ArrayList<String>();
 		for (final Zone zone : gsm.getZones().values()) {
 			gsmZones.add(zone.getName());
 		}
 
 		LogUtils.log("Checking management component zones");
 		assertTrue("gsm zones does not contain management",
 				gsmZones.contains("management"));
 		assertTrue("gsm zones does not contain localcloud",
 				gsmZones.contains("localcloud"));
 	}
 
 	private void checkPUDump() throws IOException, RestException {
 
 		final String[] dumpUrls = {
 				"/service/dump/processing-units/",
 				"/service/dump/machine/"
 						+ admin.getMachines().getMachines()[0].getHostAddress()
 						+ "/" };
 		// Test dump processingUnit and machine according to ip
 		final GSRestClient rc = new GSRestClient("", "", new URL(
 				AbstractLocalCloudTest.restUrl),
 				PlatformVersion.getVersionNumber());
 		for (final String dumpURI : dumpUrls) {
 
 			final String encodedResult = (String) rc.get(dumpURI);
 			LogUtils.log("Machine dump downloaded successfully");
 
 			final byte[] result = Base64.decodeBase64(encodedResult);
 			final File dumpFile = File.createTempFile("dump", ".zip");
 			FileUtils.writeByteArrayToFile(dumpFile, result);
 
 			final ZipFile zip = new ZipFile(dumpFile);
 			Assert.assertTrue("The dump zip file doesn't contain any entries. "
 					+ dumpURI, zip.size() != 0);
 		}
 
 		// Test dump for a ll machines
 		final String machinesDumpUri = "/service/dump/machines/";
 		final Map<String, Object> resultsMap = (Map) rc.get(machinesDumpUri);
 		LogUtils.log("Machines dump downloaded successfully");
 
 		for (final String key : resultsMap.keySet()) {
 			final byte[] result = Base64.decodeBase64(resultsMap.get(key)
 					.toString());
 			final File dumpFile = File.createTempFile("dumpMachines", ".zip");
 			FileUtils.writeByteArrayToFile(dumpFile, result);
 			final ZipFile zip = new ZipFile(dumpFile);
 			Assert.assertTrue("The dump zip file doesn't contain any entries. "
 					+ machinesDumpUri, zip.size() != 0);
 
 		}
 
 	}
 
 	// Check that the "service type" is exposed in the service map obtained by
 	// the AdminApiController.
 	private void checkServiceType() {
 		final String absoluteServiceName = ServiceUtils.getAbsolutePUName(
 				"default", "kitchensink-service");
 
 		Map<String, Object> adminData = null;
 		try {
 			adminData = getAdminData("ProcessingUnits/Names/"
 					+ absoluteServiceName);
 		} catch (final ErrorStatusException e) {
 			AssertFail("Failed to get the service admin data." + e);
 		} catch (final CLIException e) {
 			AssertFail("Failed to get the service admin data." + e);
 		}
 		assertTrue("Test was unable to fetch the " + absoluteServiceName
 				+ " service's admin API data.", adminData != null);
 		assertTrue("Type attribute was not found in service map.",
 				adminData.containsKey("Type-Enumerator"));
 
 		final String kitchensinkServiceType = adminData.get("Type-Enumerator")
 				.toString();
 		assertTrue(
 				"The service type " + kitchensinkServiceType
 						+ " does not match the expected service type.",
 				ProcessingUnitType.UNIVERSAL.toString().equals(
 						kitchensinkServiceType));
 	}
 
 	private static final java.util.logging.Logger logger = java.util.logging.Logger
 			.getLogger(USMKitchenSinkTest.class.getName());
 
 	private static class KitchenSinkEventListener implements
 			GridServiceContainerAddedEventListener,
 			GridServiceContainerRemovedEventListener,
 			ProcessingUnitInstanceAddedEventListener,
 			ProcessingUnitInstanceRemovedEventListener {
 
 		private GridServiceContainer gscRemoved = null;
 		private GridServiceContainer gscAdded = null;
 		private ProcessingUnitInstance puiRemoved = null;
 		private ProcessingUnitInstance puiAdded = null;
 		private boolean errorEncountered = false;
 		private String errorMessage = null;
 
 		@Override
 		public synchronized void gridServiceContainerRemoved(
 				final GridServiceContainer gridServiceContainer) {
 			logger.info("gridServiceContainerRemoved");
 			if (checkPreConditions(this.gscRemoved, gridServiceContainer,
 					"gridServiceContainerRemoved")) {
 				this.gscRemoved = gridServiceContainer;
 				this.notify();
 			}
 
 		}
 
 		private synchronized boolean checkPreConditions(
 				final Object currentObject, final Object newObject,
 				final String objectType) {
 
 			if (this.errorEncountered) {
 				return false;
 			}
 			if (currentObject != null) {
 				this.errorEncountered = true;
 				this.errorMessage = "Message of type " + objectType
 						+ " was encountered twice";
 			}
 
 			return true;
 
 		}
 
 		@Override
 		public synchronized void gridServiceContainerAdded(
 				final GridServiceContainer gridServiceContainer) {
 			logger.info("gridServiceContainerAdded");
 			if (checkPreConditions(this.gscAdded, gridServiceContainer,
 					"gridServiceContainerRemoved")) {
 				this.gscAdded = gridServiceContainer;
 				this.notify();
 			}
 
 		}
 
 		@Override
 		public synchronized void processingUnitInstanceRemoved(
 				final ProcessingUnitInstance processingUnitInstance) {
 			logger.info("processingUnitInstanceRemoved");
 			if (checkPreConditions(this.puiRemoved, processingUnitInstance,
 					"processingUnitInstanceRemoved")) {
 				this.puiRemoved = processingUnitInstance;
 				this.notify();
 			}
 
 		}
 
 		@Override
 		public synchronized void processingUnitInstanceAdded(
 				final ProcessingUnitInstance processingUnitInstance) {
 			logger.info("processingUnitInstanceAdded");
 			if (checkPreConditions(this.puiAdded, processingUnitInstance,
 					"processingUnitInstanceAdded")) {
 				this.puiAdded = processingUnitInstance;
 				this.notify();
 			}
 
 		}
 
 		public GridServiceContainer getGscRemoved() {
 			return gscRemoved;
 		}
 
 		public GridServiceContainer getGscAdded() {
 			return gscAdded;
 		}
 
 		public ProcessingUnitInstance getPuiRemoved() {
 			return puiRemoved;
 		}
 
 		public ProcessingUnitInstance getPuiAdded() {
 			return puiAdded;
 		}
 
 		public boolean isErrorEncountered() {
 			return errorEncountered;
 		}
 
 		public String getErrorMessage() {
 			return errorMessage;
 		}
 
 	}
 
 	private void checkKillGSC(final long pid, final String host,
 			final ProcessingUnit pu) throws InterruptedException {
 
 		final KitchenSinkEventListener listener = new KitchenSinkEventListener();
 
 		this.admin.getGridServiceContainers().getGridServiceContainerAdded()
 				.add(listener, false);
 		this.admin.getGridServiceContainers().getGridServiceContainerRemoved()
 				.add(listener);
 		pu.getProcessingUnitInstanceAdded().add(listener, false);
 		pu.getProcessingUnitInstanceRemoved().add(listener);
 
 		synchronized (listener) {
 			logger.info("Killin GSC (PID - " + pid + ")");
 			killProcess(pid);
 
 			// check that the simple process port is still open.
 			assertTrue(
 					"Process port is not open! Process did not start as expected",
 					isPortOpen(host));
 
 			logger.info("Waiting for Admin API to regsiter GSC Remove/Add and PUI Remove/Add");
 			waitForCorrectListenerState(listener, 10000, 300000);
 
 			// verify correct number of instances
 			final boolean foundInstance = pu.waitFor(1, 30, TimeUnit.SECONDS);
 			assertTrue("New pui was not found after GSC failure", foundInstance);
 
 		}
 		// kill the GSC process and verify it is dead.
 
 		admin.removeEventListener(listener);
 
 		// check that process restarted correctly
 
 	}
 
 	private boolean checkListenerState(final KitchenSinkEventListener listener) {
 		// if(listener.getGscRemoved() == null && listener.getPuiRemoved() ==
 		// null) {
 		// AssertFail("Neither GSC not PUI were removed during the first interval");
 		// }
 
 		if (listener.getGscRemoved() != null
 				&& listener.getPuiRemoved() != null
 				&& listener.getGscAdded() != null
 				&& listener.getPuiAdded() != null) {
 			return true;
 		}
 
 		return false;
 
 	}
 
 	private void waitForCorrectListenerState(
 			final KitchenSinkEventListener listener, final long interval,
 			final long timeout) throws InterruptedException {
 		final long start = System.currentTimeMillis();
 		final long end = start + timeout;
 		boolean checkSucceeded = false;
 		while (!checkSucceeded && System.currentTimeMillis() < end) {
 			listener.wait(interval);
 			checkSucceeded = checkListenerState(listener);
 		}
 
 		assertTrue("GSC and PUI did not start", checkSucceeded);
 	}
 
 	private void killProcess(final long pid) {
 		try {
 			if (ScriptUtils.isLinuxMachine()) {
 				SigarHolder.getSigar().kill(pid, "SIGKILL");
 			} else {
 				SigarHolder.getSigar().kill(pid, "SIGTERM");
 			}
 		} catch (final SigarException e) {
 			throw new IllegalStateException("Failed to kill GSC PID: " + pid, e);
 		}
 
 		sleep(1000);
 
 		try {
 			assertTrue(
 					"Process port is not open after killing GSC! Process died unexpectedly",
 					!USMUtils.isProcessAlive(pid));
 		} catch (final USMException e) {
 			throw new IllegalStateException("Failed to kill GSC PID: " + pid, e);
 		}
 	}
 
 	private void checkForShutdownPrintouts(final ProcessingUnitInstance pui,
 			final long pid, final ContinuousLogEntryMatcher matcher) {
 		final LogEntries shutdownEntries = pui.getGridServiceContainer()
 				.getGridServiceAgent()
 				.logEntries(LogProcessType.GSC, pid, matcher);
 		int shutdownEventIndex = 0;
 		for (final LogEntry logEntry : shutdownEntries) {
 			final String text = logEntry.getText();
 			if (text.contains(EXPECTED_SHUTDOWN_EVENT_STRINGS[shutdownEventIndex])) {
 				++shutdownEventIndex;
 				if (shutdownEventIndex == EXPECTED_SHUTDOWN_EVENT_STRINGS.length) {
 					break;
 				}
 			}
 		}
 
 		if (shutdownEventIndex != EXPECTED_SHUTDOWN_EVENT_STRINGS.length) {
 			AssertFail("An event was not fired. Missing event details: "
 					+ EXPECTED_SHUTDOWN_EVENT_STRINGS[shutdownEventIndex]);
 		}
 	}
 
 	private void checkForPrintouts(final ProcessingUnitInstance pui,
 			final long pid, final ContinuousLogEntryMatcher matcher,
 			final String[] expectedValues) {
 		final LogEntries entries = pui.getGridServiceContainer()
 				.getGridServiceAgent()
 				.logEntries(LogProcessType.GSC, pid, matcher);
 
 		int index = 0;
 		for (final LogEntry logEntry : entries) {
 			final String text = logEntry.getText();
 			logger.info(text);
 			if (text.contains(expectedValues[index])) {
 				++index;
 				if (index == expectedValues.length) {
 					break;
 				}
 			}
 		}
 
 		if (index != expectedValues.length) {
 			AssertFail("A printout entry was not found. The missing entry was: "
 					+ expectedValues[index]);
 		}
 	}
 
 	private int calculateNumberOfRecurrencesInGSCLog(
 			final ProcessingUnitInstance pui, final long pid,
 			final ContinuousLogEntryMatcher matcher, final String expectedValue) {
 		final LogEntries entries = pui.getGridServiceContainer()
 				.getGridServiceAgent()
 				.logEntries(LogProcessType.GSC, pid, matcher);
 		int result = 0;
 		for (final LogEntry logEntry : entries) {
 			final String text = logEntry.getText();
 			if (text.contains(expectedValue)) {
 				++result;
 			}
 		}
 
 		return result;
 	}
 
 	private void checkCustomCommands() throws IOException, InterruptedException {
 		// test custom commands
 		final String invoke1Result = runCommand("connect "
 				+ AbstractLocalCloudTest.restUrl
 				+ "; invoke kitchensink-service cmd1");
 
 		if (!invoke1Result.contains("1: OK")
 				|| !invoke1Result.contains("Result: null")) {
 			AssertFail("Custom command cmd1 returned unexpected result: "
 					+ invoke1Result);
 		}
 
 		final String invoke2Result = CommandTestUtils
 				.runCommandExpectedFail("connect "
 						+ AbstractLocalCloudTest.restUrl
 						+ "; invoke kitchensink-service cmd2");
 
 		if (!invoke2Result.contains("1: FAILED")
 				|| !invoke2Result
 						.contains("This is the cmd2 custom command - This is an error test")) {
 			AssertFail("Custom command cmd2 returned unexpected result: "
 					+ invoke2Result);
 		}
 
 		final String invoke3Result = runCommand("connect "
 				+ AbstractLocalCloudTest.restUrl
 				+ "; invoke kitchensink-service cmd3");
 		if (!invoke3Result.contains("1: OK")
 				|| !invoke3Result
 						.contains("Result: This is the cmd3 custom command. Service Dir is:")) {
 			AssertFail("Custom command cmd3 returned unexpected result: "
 					+ invoke3Result);
 		}
 		final String invoke4Result = runCommand("connect "
 				+ AbstractLocalCloudTest.restUrl
 				+ "; invoke kitchensink-service cmd4");
 		if (!invoke4Result.contains("1: OK")
 				|| !invoke4Result.contains("context_command")
 				|| !invoke4Result.contains("instance is:")) {
 			AssertFail("Custom command cmd4 returned unexpected result: "
 					+ invoke4Result);
 		}
 
 		final String invoke5Result = runCommand("connect "
 				+ AbstractLocalCloudTest.restUrl
 				+ "; invoke kitchensink-service cmd5 2 3");
 
 		if (!invoke5Result.contains("1: OK")
 				|| !invoke5Result
 						.contains("this is the custom parameters command. expecting 123: 123")) {
 			AssertFail("Custom command cmd5 returned unexpected result: "
 					+ invoke5Result);
 		}
 		final String invoke6Result = runCommand("connect "
 				+ AbstractLocalCloudTest.restUrl
 				+ "; invoke kitchensink-service cmd6 1 2");
 
 		if (!invoke6Result.contains("1: OK")
 				|| !invoke6Result.contains("Argument:1")
 				|| !invoke6Result.contains("Argument:2")) {
 			AssertFail("Custom command cmd6 returned unexpected result: "
 					+ invoke6Result);
 		}
 
 		final String invoke7Result = runCommand("connect "
 				+ AbstractLocalCloudTest.restUrl
 				+ "; invoke kitchensink-service cmd7 1");
 
 		if (!invoke7Result.contains("1: OK")
 				|| !invoke7Result.contains("Single parameter test:parameter=1")) {
 			AssertFail("Custom command cmd7 returned unexpected result: "
 					+ invoke7Result);
 		}
 
 		final String invoke8Result = CommandTestUtils
 				.runCommandExpectedFail("connect "
 						+ AbstractLocalCloudTest.restUrl
 						+ "; invoke kitchensink-service cmd8");
 
 		if (!invoke8Result.contains("1: FAILED")
 				|| !invoke8Result
 						.contains("This is the cmd8 custom command - This is an error test")) {
 			AssertFail("Custom command cmd8 returned unexpected result: "
 					+ invoke8Result);
 		}
 
 		final String invoke9Result = CommandTestUtils
 				.runCommandExpectedFail("connect "
 						+ AbstractLocalCloudTest.restUrl
 						+ "; invoke kitchensink-service cmd9");
 
 		if (!invoke9Result.contains("1: OK")
 				|| !invoke9Result.contains("Result: null")) {
 			AssertFail("Custom command cmd9 returned unexpected result: "
 					+ invoke9Result);
 		}
 	}
 
 	private void checkMonitors(final ProcessingUnitInstance pui) {
 		// verify monitors
 		final Collection<ServiceMonitors> allSserviceMonitors = pui
 				.getStatistics().getMonitors().values();
 		final Map<String, Object> allMonitors = new HashMap<String, Object>();
 		for (final ServiceMonitors serviceMonitors : allSserviceMonitors) {
 			allMonitors.putAll(serviceMonitors.getMonitors());
 		}
 
 		for (final String monitorKey : EXPECTED_MONITORS_FIELDS) {
 			assertTrue("Missing Monitor Key: " + monitorKey,
 					allMonitors.containsKey(monitorKey));
 		}
 
 		this.actualPid = (Long) allMonitors
 				.get(CloudifyConstants.USM_MONITORS_ACTUAL_PROCESS_ID);
 		assertTrue("Actual PID should not be zero", this.actualPid > 0);
 	}
 
 	private void checkDetails(final ProcessingUnitInstance pui) {
 		final Collection<ServiceDetails> allServiceDetails = pui
 				.getServiceDetailsByServiceId().values();
 		final Map<String, Object> allDetails = new HashMap<String, Object>();
 		for (final ServiceDetails serviceDetails : allServiceDetails) {
 			allDetails.putAll(serviceDetails.getAttributes());
 		}
 		for (final String detailKey : EXPECTED_DETAILS_FIELDS) {
 			assertTrue("Missing details entry: " + detailKey,
 					allDetails.containsKey(detailKey));
 		}
 
 		final String url = (String) allDetails.get("url");
 		assertNotNull("Missing URL details", url);
 
 		try {
 			new URL(url);
 		} catch (final MalformedURLException e) {
 			AssertFail("URL: " + url + " is not a valid URL", e);
 		}
 	}
 
 	private ContinuousLogEntryMatcher checkForStartupPrintouts(
 			final ProcessingUnitInstance pui, final long pid,
 			final ContinuousLogEntryMatcher matcher) {
 
 		int startupEventIndex = 0;
 		final LogEntries entries = pui.getGridServiceContainer()
 				.getGridServiceAgent()
 				.logEntries(LogProcessType.GSC, pid, matcher);
 		for (final LogEntry logEntry : entries) {
 			final String text = logEntry.getText();
 			if (text.contains("application name is")) {
 				System.out.println("Stop");
 			}
 			if (text.contains(EXPECTED_STARTUP_EVENT_STRINGS[startupEventIndex])) {
 				++startupEventIndex;
 				if (startupEventIndex == EXPECTED_STARTUP_EVENT_STRINGS.length) {
 					break;
 				}
 			}
 		}
 
 		if (startupEventIndex != EXPECTED_STARTUP_EVENT_STRINGS.length) {
 			AssertFail("An event was not fired. Missing event details: "
 					+ EXPECTED_STARTUP_EVENT_STRINGS[startupEventIndex]);
 		}
 		return matcher;
 	}
 
 	private ProcessingUnitInstance findPUI(final ProcessingUnit pu)
 			throws UnknownHostException {
 		final boolean found = pu.waitFor(1, 30, TimeUnit.SECONDS);
 		assertTrue("USM Service state is not RUNNING",
 				USMTestUtils.waitForPuRunningState(
 						"default.kitchensink-service", 20, TimeUnit.SECONDS,
 						admin));
 		assertTrue("Could not find instance of deployed service", found);
 
 		final ProcessingUnitInstance pui = pu.getInstances()[0];
 		return pui;
 	}
 
 	private ProcessingUnit findPU() {
 		final ProcessingUnit pu = this.admin.getProcessingUnits()
 				.waitFor(
 						ServiceUtils.getAbsolutePUName("default",
 								"kitchensink-service"), 30, TimeUnit.SECONDS);
 		assertNotNull("Could not find processing unit for installed service",
 				pu);
 		return pu;
 	}
 
 	private void installService() throws PackagingException, IOException,
 			InterruptedException, DSLException {
 		final File serviceDir = new File(RECIPE_DIR_PATH);
 		ServiceReader.getServiceFromDirectory(serviceDir).getService();
 
 		runCommand("connect " + restUrl + ";install-service --verbose "
 				+ RECIPE_DIR_PATH);
 	}
 
 	private boolean isPortOpen(final String host) {
 		final Socket socket = new Socket();
 		try {
 			socket.connect(new InetSocketAddress(host, 7777));
 			return true;
 		} catch (final IOException e) {
 			return false;
 		} finally {
 			try {
 				socket.close();
 			} catch (final IOException e) {
 				// ignore
 			}
 		}
 
 	}
 
 }
