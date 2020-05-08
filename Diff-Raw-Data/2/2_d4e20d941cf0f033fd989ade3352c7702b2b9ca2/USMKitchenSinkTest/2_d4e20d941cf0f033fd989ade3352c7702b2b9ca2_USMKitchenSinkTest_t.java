 package test.cli.cloudify;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.hyperic.sigar.SigarException;
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventListener;
 import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventListener;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
 import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventListener;
 import org.openspaces.pu.service.ServiceDetails;
 import org.openspaces.pu.service.ServiceMonitors;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.usm.USMTestUtils;
 
 import com.gigaspaces.cloudify.dsl.internal.CloudifyConstants;
 import com.gigaspaces.cloudify.dsl.internal.ServiceReader;
 import com.gigaspaces.cloudify.dsl.internal.packaging.PackagingException;
 import com.gigaspaces.cloudify.dsl.utils.ServiceUtils;
 import com.gigaspaces.cloudify.usm.USMException;
 import com.gigaspaces.cloudify.usm.USMUtils;
 import com.gigaspaces.internal.sigar.SigarHolder;
 import com.gigaspaces.log.AllLogEntryMatcher;
 import com.gigaspaces.log.ContinuousLogEntryMatcher;
 import com.gigaspaces.log.LogEntries;
 import com.gigaspaces.log.LogEntry;
 import com.gigaspaces.log.LogProcessType;
 
 public class USMKitchenSinkTest extends AbstractLocalCloudTest {
 
 	private static final String LOCAL_GROUP_NAME = "kitchensinktest";
 
 	@Override
 	@BeforeMethod
 	public void beforeTest() {
 		super.beforeTest();
 	}
 	
 	final private String RECIPE_DIR_PATH = CommandTestUtils
 			.getPath("apps/USM/usm/kitchensink");
 
 	// set in checkMonitors
 	private long actualPid;
 
 	private static final String[] EXPECTED_STARTUP_EVENT_STRINGS = {
 			"init fired Test Property number 1",
 			"preInstall fired Test Property number 2",
 			"postInstall fired Test Property number 1",
 			"preStart fired Test Property number 2", "postStart fired",
 			"Instantiated default.kitchensink-service" };
 	private static final String[] EXPECTED_SHUTDOWN_EVENT_STRINGS = {
 			"preStop fired", "String_with_Spaces", "postStop fired",
 			"shutdown fired" };
 
 	private static final String[] EXPECTED_DETAILS_FIELDS = {"stam", "SomeKey" };
 
 	private static final String[] EXPECTED_MONITORS_FIELDS = {CloudifyConstants.USM_MONITORS_CHILD_PROCESS_ID,
 			CloudifyConstants.USM_MONITORS_ACTUAL_PROCESS_ID, "NumberTwo",
 			"NumberOne" };
 	
 	private static final String[] EXPECTED_PROCESS_PRINTOUTS ={"Opening port:"};
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testKitchenSink() throws IOException, InterruptedException,
 			PackagingException {
 
 		installService();
 
 		ProcessingUnit pu = findPU();
 
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
 		checkForStartupPrintouts(pui, pid, matcher);
 
 		checkDetails(pui);
 
 		checkMonitors(pui);
 
 		checkCustomCommands();
 
 		long initialActualPid = this.actualPid;
 		
 		checkKillGSC(pid, host, pu);
 				
 		// pui was restarted, so find the new one
 		pui = findPUI(pu);
 		pid = pui.getGridServiceContainer().getVirtualMachine()
 				.getDetails().getPid();
 		
 		// this will reset the actualPid
 		checkMonitors(pui);
 		assertEquals("The actual PID value should not change after a GSC restart", initialActualPid, this.actualPid);
 
 		// reset the matcher
 		matcher = new ContinuousLogEntryMatcher(
 				new AllLogEntryMatcher(), new AllLogEntryMatcher());
 
 		// check that previous log entries have been printed to this log
 		logger.info("Checking that previous process printouts are printed to the new GSC logs");
 		sleep(5000);
 		checkForPrintouts(pui, pid, matcher, EXPECTED_PROCESS_PRINTOUTS);
  
 		// reset the matcher
 		matcher = new ContinuousLogEntryMatcher(
 				new AllLogEntryMatcher(), new AllLogEntryMatcher());
 		// check that the process printouts only appear once - to make sure that additional process invocations were not still in the same file
 		final int numOfStartupEntries = calculateNumberOfRecurrencesInGSCLog(pui, pid, matcher, EXPECTED_PROCESS_PRINTOUTS[0]);
 		assertEquals("Process startup log entries should only appear once in the log file", 1, numOfStartupEntries);
 		
 		//TODO:Remove this and uncomment the code below. This is a workaround
 		//that is ment to solve the pu.undeploy getting stuck issue.
 		while (this.admin.getProcessingUnits().waitFor("default.kitchensink-service", 10, TimeUnit.SECONDS).getInstances().length != 0){
 			pu.undeployAndWait(10, TimeUnit.SECONDS);
 		}
 //		// undeploy
 //		pu.undeploy();
 
 		
 		// test shutdown events
 		matcher = new ContinuousLogEntryMatcher(
 				new AllLogEntryMatcher(), new AllLogEntryMatcher());
 		checkForShutdownPrintouts(pui, pid, matcher);
 
 		// check port closed.
 		assertTrue(
 				"Process port is still open! Process did not shut down as expected",
 				!isPortOpen(host));
 
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
 				GridServiceContainer gridServiceContainer) {
 			logger.info("gridServiceContainerRemoved");
 			if (checkPreConditions(this.gscRemoved, gridServiceContainer,
 					"gridServiceContainerRemoved")) {
 				this.gscRemoved = gridServiceContainer;
 				this.notify();
 			}
 
 		}
 
 		private synchronized boolean checkPreConditions(Object currentObject,
 				Object newObject, String objectType) {
 			
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
 				GridServiceContainer gridServiceContainer) {
 			logger.info("gridServiceContainerAdded");
 			if (checkPreConditions(this.gscAdded, gridServiceContainer,
 					"gridServiceContainerRemoved")) {
 				this.gscAdded = gridServiceContainer;
 				this.notify();
 			}
 
 		}
 
 		@Override
 		public synchronized void processingUnitInstanceRemoved(
 				ProcessingUnitInstance processingUnitInstance) {
 			logger.info("processingUnitInstanceRemoved");
 			if (checkPreConditions(this.puiRemoved, processingUnitInstance,
 					"processingUnitInstanceRemoved")) {
 				this.puiRemoved = processingUnitInstance;
 				this.notify();
 			}
 
 		}
 
 		@Override
 		public synchronized void processingUnitInstanceAdded(
 				ProcessingUnitInstance processingUnitInstance) {
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
 
 	private void checkKillGSC(long pid, String host, ProcessingUnit pu)
 			throws InterruptedException {
 
 		KitchenSinkEventListener listener = new KitchenSinkEventListener();
 
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
 
 	private boolean checkListenerState(KitchenSinkEventListener listener) {
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
 
 	private void waitForCorrectListenerState(KitchenSinkEventListener listener,
 			long interval, long timeout) throws InterruptedException {
 		final long start = System.currentTimeMillis();
 		final long end = start + timeout;
 		boolean checkSucceeded = false;
 		while (!checkSucceeded && System.currentTimeMillis() < end) {
 			listener.wait(interval);
 			checkSucceeded = checkListenerState(listener);
 		}
 
 		assertTrue("GSC and PUI did not start", checkSucceeded);
 	}
 
 	private void killProcess(long pid) {
 		try {
 			SigarHolder.getSigar().kill(pid, "SIGTERM");
 		} catch (SigarException e) {
 			throw new IllegalStateException("Failed to kill GSC PID: " + pid, e);
 		}
 
 		sleep(1000);
 
 		try {
 			assertTrue(
 					"Process port is not open after killing GSC! Process died unexpectedly",
 					!USMUtils.isProcessAlive(pid));
 		} catch (USMException e) {
 			throw new IllegalStateException("Failed to kill GSC PID: " + pid, e);
 		}
 	}
 
 	private void checkForShutdownPrintouts(ProcessingUnitInstance pui,
 			long pid, ContinuousLogEntryMatcher matcher) {
 		LogEntries shutdownEntries = pui.getGridServiceContainer()
 				.getGridServiceAgent()
 				.logEntries(LogProcessType.GSC, pid, matcher);
 		int shutdownEventIndex = 0;
 		for (LogEntry logEntry : shutdownEntries) {
 			String text = logEntry.getText();
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
 	
 	private void checkForPrintouts(ProcessingUnitInstance pui,
 			long pid, ContinuousLogEntryMatcher matcher, final String[] expectedValues) {
 		LogEntries entries = pui.getGridServiceContainer()
 				.getGridServiceAgent()
 				.logEntries(LogProcessType.GSC, pid, matcher);
 		
 		int index = 0;
 		for (LogEntry logEntry : entries) {			
 			String text = logEntry.getText();
 			logger.info(text);
 			if (text.contains(expectedValues[index])) {
 				++index;
 				if (index == expectedValues.length) {
 					break;
 				}
 			}
 		}
 
 		if (index != expectedValues.length) {
 			AssertFail("A printout entry was not found. The mi  ssing entry was: "
 					+ expectedValues[index]);
 		}
 	}
 
 
 	private int calculateNumberOfRecurrencesInGSCLog(ProcessingUnitInstance pui,
 			long pid, ContinuousLogEntryMatcher matcher, final String expectedValue) {
 		LogEntries entries = pui.getGridServiceContainer()
 				.getGridServiceAgent()
 				.logEntries(LogProcessType.GSC, pid, matcher);
 		int result = 0;
 		for (LogEntry logEntry : entries) {
 			String text = logEntry.getText();
 			if (text.contains(expectedValue)) {
 				++result;
 			}
 		}
 
 		return result;
 	}
 
 	
 	private void checkCustomCommands() throws IOException, InterruptedException {
 		// test custom commands
 		String invoke1Result = runCommand("connect " + this.restUrl
 				+ "; invoke kitchensink-service cmd1");
 
 		if ((!invoke1Result.contains("1: OK"))
 				|| (!invoke1Result.contains("Result: null"))) {
 			AssertFail("Custom command cmd1 returned unexpected result: "
 					+ invoke1Result);
 		}
 
 		String invoke2Result = runCommand("connect " + this.restUrl
 				+ "; invoke kitchensink-service cmd2");
 
 		if ((!invoke2Result.contains("1: FAILED"))
 				|| (!invoke2Result
 						.contains("This is the cmd2 custom command - This is an error test"))) {
 			AssertFail("Custom command cmd2 returned unexpected result: "
 					+ invoke2Result);
 		}
 
 		String invoke3Result = runCommand("connect " + this.restUrl
 				+ "; invoke kitchensink-service cmd3");
 		if ((!invoke3Result.contains("1: OK"))
 				|| (!invoke3Result
 						.contains("Result: This is the cmd3 custom command. Service Dir is:"))) {
 			AssertFail("Custom command cmd3 returned unexpected result: "
 					+ invoke3Result);
 		}
 		String invoke4Result = runCommand("connect " + this.restUrl
 				+ "; invoke kitchensink-service cmd4");
 		if ((!invoke4Result.contains("1: OK"))
 				|| (!invoke4Result.contains("context_command"))
 				|| (!invoke4Result.contains("instance is:"))) {
 			AssertFail("Custom command cmd4 returned unexpected result: "
 					+ invoke4Result);
 		}
 
 		String invoke5Result = runCommand("connect " + this.restUrl
 				+ "; invoke kitchensink-service cmd5 ['x=2' 'y=3']");
 
 		if ((!invoke5Result.contains("1: OK"))
 				|| (!invoke5Result
 						.contains("this is the custom parameters command. expecting 123: 123"))) {
 			AssertFail("Custom command cmd5 returned unexpected result: "
 					+ invoke1Result);
 		}
 	}
 
 	private void checkMonitors(ProcessingUnitInstance pui) {
 		// verify monitors
 		Collection<ServiceMonitors> allSserviceMonitors = pui.getStatistics()
 				.getMonitors().values();
 		Map<String, Object> allMonitors = new HashMap<String, Object>();
 		for (ServiceMonitors serviceMonitors : allSserviceMonitors) {
 			allMonitors.putAll(serviceMonitors.getMonitors());
 		}
 		
 		 
 
 		for (String monitorKey : EXPECTED_MONITORS_FIELDS) {
 			assertTrue("Missing Monitor Key: " + monitorKey,
 					allMonitors.containsKey(monitorKey));
 		}
 		
 		this.actualPid = (Long) allMonitors.get(CloudifyConstants.USM_MONITORS_ACTUAL_PROCESS_ID);
 		assertTrue("Actual PID should not be zero", this.actualPid > 0);
 	}
 
 	private void checkDetails(ProcessingUnitInstance pui) {
 		Collection<ServiceDetails> allServiceDetails = pui
 				.getServiceDetailsByServiceId().values();
 		Map<String, Object> allDetails = new HashMap<String, Object>();
 		for (ServiceDetails serviceDetails : allServiceDetails) {
 			allDetails.putAll(serviceDetails.getAttributes());
 		}
 		for (String detailKey : EXPECTED_DETAILS_FIELDS) {
 			assertTrue("Missing details entry: " + detailKey,
 					allDetails.containsKey(detailKey));
 		}
 	}
 
 	private ContinuousLogEntryMatcher checkForStartupPrintouts(
 			ProcessingUnitInstance pui, long pid,
 			ContinuousLogEntryMatcher matcher) {
 
 		int startupEventIndex = 0;
 		LogEntries entries = pui.getGridServiceContainer()
 				.getGridServiceAgent()
 				.logEntries(LogProcessType.GSC, pid, matcher);
 		for (LogEntry logEntry : entries) {
 			String text = logEntry.getText();
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
 
 	private ProcessingUnitInstance findPUI(ProcessingUnit pu) throws UnknownHostException {
 		boolean found = pu.waitFor(1, 30, TimeUnit.SECONDS);
		assertTrue("USM Service state is not RUNNING", USMTestUtils.waitForPuRunningState("default.kitchensink-service", 20, TimeUnit.SECONDS, admin));
 		assertTrue("Could not find instance of deployed service", found);
 
 		ProcessingUnitInstance pui = pu.getInstances()[0];
 		return pui;
 	}
 
 	private ProcessingUnit findPU() {
 		ProcessingUnit pu = this.admin.getProcessingUnits().waitFor(
 				ServiceUtils.getAbsolutePUName(DEFAULT_APPLICTION_NAME, "kitchensink-service"), 30, TimeUnit.SECONDS);
 		assertNotNull("Could not find processing unit for installed service",
 				pu);
 		return pu;
 	}
 
 	private void installService() throws FileNotFoundException,
 			PackagingException, IOException, InterruptedException {
 		File serviceDir = new File(RECIPE_DIR_PATH);
 		ServiceReader.getServiceFromDirectory(serviceDir, CloudifyConstants.DEFAULT_APPLICATION_NAME).getService();
 
 		runCommand("connect " + restUrl + ";install-service --verbose " + RECIPE_DIR_PATH);
 	}
 
 	private boolean isPortOpen(String host) {
 		Socket socket = new Socket();
 		try {
 			socket.connect(new InetSocketAddress(host, 7777));
 			return true;
 		} catch (IOException e) {
 			return false;
 		} finally {
 			try {
 				socket.close();
 			} catch (IOException e) {
 				// ignore
 			}
 		}
 
 	}
 
 	@Override
 	@AfterMethod
 	public void afterTest() {
 		try {
 			runCommand("teardown-localcloud");
 		} catch (IOException e) {
 			throw new IllegalStateException(e);
 		} catch (InterruptedException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 }
