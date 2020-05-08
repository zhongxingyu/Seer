 package test.cli.cloudify.cloud;
 
 import java.io.IOException;
 
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.CommandTestUtils;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 
 public class ExamplesTest extends AbstractCloudTest {
 	
 	public ExamplesTest() {
 		LogUtils.log("Instansiated " + ExamplesTest.class.getName());
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 3, enabled = true, dataProvider = "supportedClouds")
 	public void testTravel(String cloudName) throws IOException, InterruptedException {
 		doTest(cloudName, "travel", "travel");
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 3, enabled = true, dataProvider = "supportedCloudsWithoutByon")
 	public void testPetclinic(String cloudName) throws IOException, InterruptedException {
 		doTest(cloudName, "petclinic", "petclinic");
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 3, enabled = true, dataProvider = "supportedClouds")
 	public void testPetclinicSimple(String cloudName) throws IOException, InterruptedException {
 		doTest(cloudName, "petclinic-simple", "petclinic");
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 3, enabled = false, dataProvider = "supportedClouds")
 	public void testPetclinicSimpleScalingRules(String cloudName) throws IOException, InterruptedException {
 		doTest(cloudName, "petclinic-simple-scalingRules", "petclinic");
 	}
 	
 	private void doTest(String cloudName, String applicationFolderName, String applicationName) throws IOException, InterruptedException {
 		LogUtils.log("installing application " + applicationName + " on " + cloudName);
 		setCloudToUse(cloudName);
		String applicationPath = ScriptUtils.getBuildPath() + "/recipes/apps/" + applicationFolderName;
 		try {
 			installApplicationAndWait(applicationPath, applicationName);
 		}
 		finally {
 			if ((getService() != null) && (getService().getRestUrls() != null)) {
 				String command = "connect " + getRestUrl() + ";list-applications";
 				String output = CommandTestUtils.runCommandAndWait(command);
 				if (output.contains(applicationName)) {
 					uninstallApplicationAndWait(applicationName);			
 				}
 			}
 		}
 	}
 }
