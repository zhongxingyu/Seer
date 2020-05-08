 package test.cli.cloudify;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.Test;
 
 import test.usm.USMTestUtils;
 
 import org.cloudifysource.dsl.utils.ServiceUtils;
 
 import framework.utils.DeploymentUtils;
 import framework.utils.DumpUtils;
 import framework.utils.LogUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 
 public class InstallAndUninstallServiceTest extends AbstractLocalCloudTest {
 
 	private static final String DEFAULT_APPLICATION_NAME = "default";
 	public static final String SERVLET_WAR_NAME = "servlet.war";
	public static final String SERVLET_SERVICE_NAME = "servlet-3.0.0-SNAPSHOT";
 	
 	public static final String SERVLET_FOLDER_NAME = "statelessPU";
 	public static final String SERVLET_RECIPE_SERVICE_NAME = "statelessPU";
 	
 	public static final String USM_SERVICE_FOLDER_NAME = "simple";
 	public static final String USM_SERVICE_NAME = "simple";
 
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testServletInstall() throws IOException, InterruptedException {
 		testRestApiInstall(SERVLET_SERVICE_NAME, getArchiveServicePath(SERVLET_WAR_NAME));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testUSMInstall() throws IOException, InterruptedException {
 		testRestApiInstall(USM_SERVICE_NAME, getUsmServicePath(USM_SERVICE_FOLDER_NAME));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testServletRecipeInstall() throws IOException, InterruptedException {
 		testRestApiInstall(SERVLET_RECIPE_SERVICE_NAME, getUsmServicePath(SERVLET_FOLDER_NAME));
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
 		repetitiveAssertTrue("No instance of: " + absolutePUName + " is null.", new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				LogUtils.log("Trying to debug why PU is not discovered");
 				DumpUtils.dumpProcessingUnit(admin);
 				return (processingUnit.getProcessingUnits().getProcessingUnit(absolutePUName) != null);
 			}
 		}
 		, 20000);
         assertTrue("Instance of '" + absolutePUName + "' service was not found", 
         		processingUnit != null && 
         		processingUnit.waitFor(1, Constants.PROCESSINGUNIT_TIMEOUT_SEC, TimeUnit.SECONDS));
         //assert USM service is in a RUNNING state.
         if (serviceName.equals(USM_SERVICE_NAME)){
         	 LogUtils.log("Verifing USM service state is set to RUNNING");
         	assertTrue(USMTestUtils.waitForPuRunningState(absolutePUName, 60, TimeUnit.SECONDS, admin));
         }
 
 
 		
 	    final GridServiceContainer gsc = processingUnit.getInstances()[0].getGridServiceContainer();
 	    
 	    LogUtils.log("Uninstalling service " + serviceName);
 	    CommandTestUtils.runCommandAndWait("connect " + restUrl + "; uninstall-service " + serviceName + "; exit;");
 		
 		assertGSCIsNotDiscovered(gsc);
 	}
 
 	private static void assertGSCIsNotDiscovered(final GridServiceContainer gsc) {
 	    repetitiveAssertTrue("Failed waiting for GSC not to be discovered", new RepetitiveConditionProvider() {
             public boolean getCondition() {
                 return !gsc.isDiscovered();
             }
         }, OPERATION_TIMEOUT);
 	}
 	
 	private String getArchiveServicePath(String dirOrFilename) {
 		return DeploymentUtils.getArchive(dirOrFilename).getAbsolutePath();
 	}
 	
 	private String getUsmServicePath(String dirOrFilename) {
 		return CommandTestUtils.getPath("src/main/resources/apps/USM/usm/" + dirOrFilename);
 	}
 }
