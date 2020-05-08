 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.dsl.internal.CloudifyConstants.USMState;
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.dsl.internal.packaging.PackagingException;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.cloudifysource.quality.iTests.framework.utils.usm.USMTestUtils;
 import org.cloudifysource.restclient.RestException;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.testng.annotations.Test;
 
 
 public class SelfHealingDisabledTest extends AbstractLocalCloudTest {
 
 	final private String RECIPE_DIR_PATH = CommandTestUtils
 			.getPath("src/main/resources/apps/USM/usm/failedGroovy");
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testNoSelfHealing() throws IOException, InterruptedException,
 			PackagingException, DSLException, RestException {
 
 		final String result = installService();
 		assertTrue("Could not find expected error message",
 				result.contains("This is a failure test"));
 
 		final ProcessingUnit pu = findPU();
 
 		USMTestUtils.getUSMServiceState("default.groovy", admin);
 		final ProcessingUnitInstance pui = findPUI(pu);
 		final USMState state = USMTestUtils.getPUIUSMState(pui);
 		assertEquals(USMState.ERROR, state);
 
 		uninstallService();
 
 	}
 
 	private ProcessingUnitInstance findPUI(final ProcessingUnit pu)
 			throws UnknownHostException {
 		final boolean found = pu.waitFor(1, 30, TimeUnit.SECONDS);
 		assertTrue("Could not find instance of deployed service", found);
 
 		final ProcessingUnitInstance pui = pu.getInstances()[0];
 		return pui;
 	}
 
 	private ProcessingUnit findPU() {
 		final ProcessingUnit pu = this.admin.getProcessingUnits().waitFor(
 				ServiceUtils.getAbsolutePUName("default", "groovy"), 30,
 				TimeUnit.SECONDS);
 		assertNotNull("Could not find processing unit for installed service",
 				pu);
 		return pu;
 	}
 
 	private String installService() throws PackagingException, IOException,
 			InterruptedException, DSLException {
 		final File serviceDir = new File(RECIPE_DIR_PATH);
 		ServiceReader.getServiceFromDirectory(serviceDir).getService();
 
		return runCommand("connect " + restUrl
 				+ ";install-service -disableSelfHealing --verbose "
 				+ RECIPE_DIR_PATH);
 	}
 
 	private String uninstallService() throws PackagingException, IOException,
 			InterruptedException, DSLException {
 
 		return runCommand("connect " + restUrl
 				+ ";uninstall-service --verbose groovy");
 	}
 
 }
