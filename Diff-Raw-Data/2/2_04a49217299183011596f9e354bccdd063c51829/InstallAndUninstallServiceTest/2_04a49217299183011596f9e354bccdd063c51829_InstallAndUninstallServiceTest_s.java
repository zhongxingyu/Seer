 package test.cli.cloudify;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.usm.USMTestUtils;
 
 import com.gigaspaces.cloudify.dsl.utils.ServiceUtils;
 
 import framework.utils.LogUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 
 public class InstallAndUninstallServiceTest extends AbstractSingleBootstrapTest {
 
 	private static final String DEFAULT_APPLICATION_NAME = "default";
 	public static final String SERVLET_WAR_NAME = "servlet.war";
 	public static final String SERVLET_SERVICE_NAME = "servlet";
 	
 	public static final String SERVLET_FOLDER_NAME = "statelessPU";
 	public static final String SERVLET_RECIPE_SERVICE_NAME = "statelessPU";
 	
 	public static final String STATEFUL_FOLDER_NAME = "statefulPU";
 	public static final String STATEFUL_SERVICE_NAME = "stateful";
 	
 	public static final String DATAGRID_FOLDER_NAME = "datagrid";
 	public static final String DATAGRID_SERVICE_NAME = "datagrid";
 	
 	public static final String USM_SERVICE_FOLDER_NAME = "simple";
 	public static final String USM_SERVICE_NAME = "simple";
 	
 	public static final String MIRROR_SERVICE_FOLDER_NAME = "stockAnalyticsMirror";
 	public static final String MIRROR_SERVICE_NAME = "stockAnalyticsMirror";
 	
 	@Override
 	@BeforeMethod
 	public void beforeTest() {
 		super.beforeTest();
 	}
 	
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
 		
 		String absolutePUName = ServiceUtils.getAbsolutePUName(DEFAULT_APPLICATION_NAME, serviceName);
 		ProcessingUnit processingUnit = admin.getProcessingUnits().waitFor(absolutePUName, Constants.PROCESSINGUNIT_TIMEOUT_SEC, TimeUnit.SECONDS);
        assertTrue("Instance of '" + serviceName + "' service was not found", 
         		processingUnit != null && 
         		processingUnit.waitFor(1, Constants.PROCESSINGUNIT_TIMEOUT_SEC, TimeUnit.SECONDS));
         //assert USM service is in a RUNNING state.
         if (serviceName.equals(USM_SERVICE_NAME)){
         	 LogUtils.log("Verifing USM service state is set to RUNNING");
         	assertTrue(USMTestUtils.waitForPuRunningState(absolutePUName, 60, TimeUnit.SECONDS, admin));
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
 	
 	private String getArchiveServicePath(String dirOrFilename) {
 		return CommandTestUtils.getPath("apps/archives/" + dirOrFilename);
 	}
 	
 	private String getUsmServicePath(String dirOrFilename) {
 		return CommandTestUtils.getPath("apps/USM/usm/" + dirOrFilename);
 	}
 }
