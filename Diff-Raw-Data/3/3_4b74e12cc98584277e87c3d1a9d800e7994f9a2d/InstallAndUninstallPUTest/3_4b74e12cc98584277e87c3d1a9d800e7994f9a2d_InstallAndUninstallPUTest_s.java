 package org.cloudifysource.quality.iTests.test.cli.cloudify.pu;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.Test;
 
 import org.cloudifysource.quality.iTests.test.cli.cloudify.AbstractLocalCloudTest;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.Constants;
 import iTests.framework.utils.DumpUtils;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.TeardownUtils;
 import iTests.framework.utils.AssertUtils.RepetitiveConditionProvider;
 import org.cloudifysource.quality.iTests.framework.utils.usm.USMTestUtils;
 
 public class InstallAndUninstallPUTest extends AbstractLocalCloudTest {
 
 	private static final String DEFAULT_APPLICATION_NAME = "default";
 	
 	private static final String STATEFUL_FOLDER_NAME = "statefulPU";
 	private static final String STATEFUL_SERVICE_NAME = "stateful";
 	
 	private static final String DATAGRID_FOLDER_NAME = "datagrid";
 	private static final String DATAGRID_SERVICE_NAME = "datagrid";
 	
 	private static final String USM_SERVICE_NAME = "simple";
 	
 	private static final String MIRROR_SERVICE_FOLDER_NAME = "stockAnalyticsMirror";
 	private static final String MIRROR_SERVICE_NAME = "stockAnalyticsMirror";
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testStatefulRecipeInstall() throws IOException, InterruptedException {
 		testRestApiInstall(STATEFUL_SERVICE_NAME, getUsmServicePath(STATEFUL_FOLDER_NAME));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testDataGridRecipeInstall() throws IOException, InterruptedException {
 		testRestApiInstall(DATAGRID_SERVICE_NAME, getUsmServicePath(DATAGRID_FOLDER_NAME));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testMirrorRecipeInstall() throws IOException, InterruptedException {
 		testRestApiInstall(MIRROR_SERVICE_NAME, getUsmServicePath(MIRROR_SERVICE_FOLDER_NAME));
 	}
 	
 	void testRestApiInstall(String serviceName, String servicePath) throws IOException, InterruptedException{
 		
 		LogUtils.log("Installing service " + serviceName);
 		CommandTestUtils.runCommandAndWait("connect " + restUrl + 
 				";install-service --verbose " + servicePath );
 		
 		final String absolutePUName = ServiceUtils.getAbsolutePUName(DEFAULT_APPLICATION_NAME, serviceName);
 		
 		repetitiveAssertTrue("Processing unit: " + absolutePUName + " was not found", new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				LogUtils.log("Trying to debug why PU is not discovered");
 				DumpUtils.dumpProcessingUnit(admin);
 				return (admin.getProcessingUnits().waitFor(absolutePUName, 1000, TimeUnit.SECONDS) != null);
 			}
 		}
 		, 40000);
 		
 		final ProcessingUnit processingUnit = admin.getProcessingUnits().waitFor(absolutePUName, Constants.PROCESSINGUNIT_TIMEOUT_SEC, TimeUnit.SECONDS);
 		
 		assertTrue("Processing unit :" + absolutePUName + " Was not found", processingUnit != null);
 		assertTrue("Instance of '" + absolutePUName + "' service is null", processingUnit != null);
 		repetitiveAssertTrue("No instance of: " + absolutePUName + " is null.", new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				LogUtils.log("Trying to debug why PUI is not discovered");
 				TeardownUtils.snapshot(admin);
 				return (processingUnit.getProcessingUnits().getProcessingUnit(absolutePUName).waitFor(1, 2000, TimeUnit.SECONDS));
 			}
 		}
 		, 60000);
 //        assertTrue("Instance of '" + absolutePUName + "' service was not found", 
 //        		processingUnit.waitFor(1, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
         //assert USM service is in a RUNNING state.
         if (serviceName.equals(USM_SERVICE_NAME)){
         	 LogUtils.log("Verifing USM service state is set to RUNNING");
         	assertTrue(USMTestUtils.waitForPuRunningState(absolutePUName, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS, admin));
         }
 
 
 		
 	    final GridServiceContainer gsc = processingUnit.getInstances()[0].getGridServiceContainer();
 	    
 	    LogUtils.log("Uninstalling service " + serviceName);
 	    CommandTestUtils.runCommandAndWait("connect " + this.restUrl + "; uninstall-service " + serviceName + "; exit;");
 		
 		assertGSCIsNotDiscovered(gsc);
 	}
 
 	private static void assertGSCIsNotDiscovered(final GridServiceContainer gsc) {
 	    repetitiveAssertTrue("Failed waiting for GSC not to be discovered", new RepetitiveConditionProvider() {
             public boolean getCondition() {
                 return !gsc.isDiscovered();
             }
         }, OPERATION_TIMEOUT);
 	}
 	
 	private String getUsmServicePath(String dirOrFilename) {
 		return CommandTestUtils.getPath("src/main/resources/apps/USM/usm/" + dirOrFilename);
 	}
 }
