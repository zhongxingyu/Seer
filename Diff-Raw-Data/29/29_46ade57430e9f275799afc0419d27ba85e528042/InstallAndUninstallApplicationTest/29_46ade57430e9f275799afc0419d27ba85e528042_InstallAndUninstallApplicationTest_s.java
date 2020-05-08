 package test.cli.cloudify;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.Test;
 
import test.AbstractTest;
 import test.usm.USMTestUtils;
 
 import org.cloudifysource.dsl.utils.ServiceUtils;
 
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 
 public class InstallAndUninstallApplicationTest extends AbstractLocalCloudTest {
 
 	private static final String SERVICE_NAME = "simple";
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testInstallAndUninstall() throws IOException, InterruptedException {
 		doTest(SERVICE_NAME);
 	}
 
 	private void doTest(final String serviceGroovyFilename) throws IOException, InterruptedException {
 
 		final String applicationDir = CommandTestUtils.getPath("apps/USM/usm/applications/" + serviceGroovyFilename);
 		
 
 		runCommand("connect " + this.restUrl + ";install-application --verbose " + applicationDir);
 		String absolutePUName = ServiceUtils.getAbsolutePUName("simple", "simple");
 		final ProcessingUnit processingUnit = admin.getProcessingUnits().waitFor(absolutePUName, 30, TimeUnit.SECONDS);
 		if (processingUnit == null) {
			AbstractTest.AssertFail("Processing unit '" + absolutePUName + "' was not found");
 		}
		AbstractTest.assertTrue("Instance of '" + absolutePUName + "' service was not found",
		processingUnit.waitFor(1, 30, TimeUnit.SECONDS));
 		assertTrue(USMTestUtils.waitForPuRunningState(absolutePUName, 60, TimeUnit.SECONDS, admin));
 
 	    final GridServiceContainer gsc = processingUnit.getInstances()[0].getGridServiceContainer();
 
 		runCommand("connect " + this.restUrl + ";uninstall-application -timeout 5 " + serviceGroovyFilename);
 
 		assertGSCIsNotDiscovered(gsc);
 	}
 
 	private static void assertGSCIsNotDiscovered(final GridServiceContainer gsc) {
 	    repetitiveAssertTrue("Failed waiting for GSC not to be discovered", new RepetitiveConditionProvider() {
             @Override
 			public boolean getCondition() {
                 return !gsc.isDiscovered();
             }
         }, OPERATION_TIMEOUT);
 	}
 }
