 package test.cli.cloudify;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.Test;
 
 import test.usm.USMTestUtils;
 
 
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import com.gigaspaces.log.LogEntry;
 import com.gigaspaces.log.LogEntryMatcher;
 import com.gigaspaces.log.LogEntryMatchers;
 
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 /**
  * @since 8.0.4
  * @author dank
  *
  */
 public class UninstallWithDelayTest extends AbstractLocalCloudTest {
 
     private static final String APPLICATION_DIRECTORY = CommandTestUtils.getPath("apps/USM/usm/applications/simple-delay");
     private static final String SERVICE_NAME = "simple";
     private static final String SERVICE_DIRECTORY = CommandTestUtils.getPath("apps/USM/usm/applications/simple-delay/" + SERVICE_NAME);
     private static final String MESSAGE1 = "__preStop__ Waking up after sleep";
     private static final String MESSAGE2 = "__postStop__ Waking up after sleep";
     private static final LogEntryMatcher MATCHER1 = LogEntryMatchers.containsString(MESSAGE1);
     private static final LogEntryMatcher MATCHER2 = LogEntryMatchers.containsString(MESSAGE2);
     
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled=true)
 	public void testUninstallService() throws IOException, InterruptedException {
 		runCommand("connect " + this.restUrl + ";" + "install-service " + SERVICE_DIRECTORY + ";");
 		
		String absolutePUName = ServiceUtils.getAbsolutePUName(DEFAULT_APPLICTION_NAME, SERVICE_NAME);
 		ProcessingUnit processingUnit = admin.getProcessingUnits().waitFor(absolutePUName);
         assertTrue("Instance of '" + SERVICE_NAME + "' service was not found", processingUnit.waitFor(1, Constants.PROCESSINGUNIT_TIMEOUT_SEC,  TimeUnit.SECONDS));
 		
         final GridServiceContainer gsc = processingUnit.getInstances()[0].getGridServiceContainer();
 
         runCommand("connect " + this.restUrl + ";" + "uninstall-service " + SERVICE_NAME + ";");
 		
 		assertGSCLogsContain(MATCHER1, MESSAGE1, gsc);
 		assertGSCLogsContain(MATCHER2, MESSAGE2, gsc);
 		
 		assertGSCIsNotDiscovered(gsc);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled=true)
 	public void testUninstallApplication() throws IOException, InterruptedException {
 	    runCommand("connect " + this.restUrl + ";" + "install-application " + APPLICATION_DIRECTORY + ";");
 	    
 	    String absolutePUName = ServiceUtils.getAbsolutePUName("simple", SERVICE_NAME);
 	    ProcessingUnit processingUnit = admin.getProcessingUnits().waitFor(absolutePUName);
 	    assertTrue("Instance of '" + SERVICE_NAME + "' service was not found", processingUnit.waitFor(1, Constants.PROCESSINGUNIT_TIMEOUT_SEC,  TimeUnit.SECONDS));
 	    assertTrue("USM Service State is not RUNNING", USMTestUtils.waitForPuRunningState(absolutePUName, 20, TimeUnit.SECONDS, admin));
 	    
 	    final GridServiceContainer gsc = processingUnit.getInstances()[0].getGridServiceContainer();
 	    
 	    runCommand("connect " + this.restUrl + ";" + "uninstall-application " + SERVICE_NAME + ";");
 	    
         assertGSCLogsContain(MATCHER1, MESSAGE1, gsc);
         assertGSCLogsContain(MATCHER2, MESSAGE2, gsc);
         
         assertGSCIsNotDiscovered(gsc);
 	}
 	
 	private static void assertGSCLogsContain(final LogEntryMatcher matcher, final String message, final GridServiceContainer gsc) {
        repetitiveAssertTrue("Failed finding: " + message + " in log", new RepetitiveConditionProvider() {
             public boolean getCondition() {
                 List<LogEntry> entries = gsc.logEntries(matcher).getEntries();
                 for (LogEntry entry : entries) {
                     if (entry.getText().contains(message)) {
                         return true;
                     }
                 }
                 return false;
             }
         }, OPERATION_TIMEOUT);
 	}
 	
 	private static void assertGSCIsNotDiscovered(final GridServiceContainer gsc) {
 	    repetitiveAssertTrue("Failed waiting for GSC not to be discovered", new RepetitiveConditionProvider() {
             public boolean getCondition() {
                 return !gsc.isDiscovered();
             }
         }, OPERATION_TIMEOUT);
 	}
 	
 }
