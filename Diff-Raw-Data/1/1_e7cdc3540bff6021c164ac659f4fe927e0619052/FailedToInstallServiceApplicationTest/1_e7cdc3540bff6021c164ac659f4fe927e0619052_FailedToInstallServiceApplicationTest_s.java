 package test.cli.cloudify;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.Test;
 
 import org.cloudifysource.dsl.utils.ServiceUtils;
 
 public class FailedToInstallServiceApplicationTest extends AbstractLocalCloudTest {
 
 	public static final String USM_SERVICE_FOLDER_NAME = "simpleService";
 	public static final String USM_SERVICE_NAME = "simple";
 	public static final String USM_APPLICATION_SERVICE_NAME = "simple";
 	public static final String USM_APPLICATION_FOLDER_NAME = "simpleApplication";
 
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testBadInstallService() throws IOException, InterruptedException {
 		testBadServiceInstall(getUsmBadServicePath(USM_SERVICE_FOLDER_NAME), ServiceUtils.getAbsolutePUName(DEFAULT_APPLICATION_NAME, USM_SERVICE_NAME));
 	}
 	
 	private String testBadServiceInstall(String servicePath, String serviceName) throws IOException, InterruptedException {
 		String output = CommandTestUtils.runCommandExpectedFail("connect " + this.restUrl +
 				";install-service --verbose -timeout 1 " + servicePath + 
 				";disconnect;");
 	
 		ProcessingUnit processingUnit = admin.getProcessingUnits().waitFor(serviceName, Constants.PROCESSINGUNIT_TIMEOUT_SEC, TimeUnit.SECONDS);
 		assertTrue("Deployed Successfully. Test Failed", 
     		processingUnit == null || processingUnit.waitFor(0, Constants.PROCESSINGUNIT_TIMEOUT_SEC, TimeUnit.SECONDS));
 		return output;
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testBadInstallApplication() throws IOException, InterruptedException {
 		testBadApplicationInstall(getUsmBadServicePath(USM_APPLICATION_FOLDER_NAME), ServiceUtils.getAbsolutePUName("simple", USM_APPLICATION_SERVICE_NAME));
 	}
 	
 	private void testBadApplicationInstall(String usmBadServicePath,
 			String usmServiceName) throws IOException, InterruptedException {
 		CommandTestUtils.runCommandExpectedFail("connect " + this.restUrl +
 				";install-application --verbose -timeout 1 " + usmBadServicePath + 
 				";disconnect;");
 	
 		ProcessingUnit processingUnit = admin.getProcessingUnits().waitFor(usmServiceName, Constants.PROCESSINGUNIT_TIMEOUT_SEC, TimeUnit.SECONDS);
 		assertTrue("Deployed Successfully. Test Failed", 
     		processingUnit == null || processingUnit.waitFor(0, Constants.PROCESSINGUNIT_TIMEOUT_SEC, TimeUnit.SECONDS));
 	}
 
 	private String getUsmBadServicePath(String dirOrFilename) {
 		return CommandTestUtils.getPath("src/main/resources/apps/USM/badUsmServices/" + dirOrFilename);
 	}
 }
