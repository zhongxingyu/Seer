 package test.webui.recipes.applications;
 
 import java.io.IOException;
 
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.BeforeMethod;
 
 import test.cli.cloudify.CommandTestUtils;
 import test.webui.AbstractSeleniumTest;
 import framework.utils.LogUtils;
 import framework.utils.ProcessingUnitUtils;
 import framework.utils.ScriptUtils;
 
 public class AbstractSeleniumApplicationRecipeTest extends AbstractSeleniumTest {
 	
 	public static final String MANAGEMENT = "management";
 
 	private String pathToApplication;
 	private boolean wait = true;
 	private String applicationName;
 	
 	public void setCurrentApplication(String application) {
 		this.applicationName = application;
 		String gigaDir = ScriptUtils.getBuildPath();	
		this.pathToApplication = gigaDir + "/examples/" + application;
 	}
 	
 	public void setWait(boolean wait) {
 		this.wait = wait;
 	}
 	
 	@BeforeMethod(alwaysRun = true)
 	public void install() throws IOException, InterruptedException {	
 		LogUtils.log("Installing application " + applicationName);
 		installApplication(pathToApplication, wait);
 		LogUtils.log("retrieving webui url");
 		ProcessingUnit webui = admin.getProcessingUnits().waitFor("webui");
 		ProcessingUnitUtils.waitForDeploymentStatus(webui, DeploymentStatus.INTACT);
 		assertTrue(webui != null);
 		assertTrue(webui.getInstances().length != 0);	
 		String url = ProcessingUnitUtils.getWebProcessingUnitURL(webui).toString();	
 		startWebBrowser(url); 
 	}
 	
 	public void installApplication(String pathToApplication, boolean wait) throws InterruptedException, IOException {
 		String command = "connect localhost:8100;install-application --verbose -timeout 25 " + pathToApplication;
 		if (wait) {
 			LogUtils.log("Waiting for install-application to finish...");
 			CommandTestUtils.runCommandAndWait(command);
 		}
 		else {
 			LogUtils.log("Not waiting for application to finish, assuming it will succeed");
 			CommandTestUtils.runCommand(command);
 		}
 	}
 	
 	public void uninstallApplication(String applicationName, boolean wait) throws IOException, InterruptedException {	
 		String command = "connect localhost:8100;uninstall-application --verbose -timeout 25 " + applicationName;
 		if (wait) {
 			LogUtils.log("Waiting for uninstall-application to finish...");
 			CommandTestUtils.runCommandAndWait(command);
 		}
 		else {
 			CommandTestUtils.runCommand(command);
 		}
 	}
 }
