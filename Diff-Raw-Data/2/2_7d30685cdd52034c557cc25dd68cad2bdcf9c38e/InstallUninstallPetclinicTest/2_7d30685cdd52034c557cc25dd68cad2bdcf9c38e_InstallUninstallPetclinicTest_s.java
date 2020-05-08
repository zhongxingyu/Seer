 package test.cli.cloudify.recipes;
 
 import static org.testng.AssertJUnit.fail;
 
 import java.io.IOException;
 
 import org.openspaces.admin.machine.Machine;
 import org.testng.annotations.Test;
 
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 import framework.utils.ScriptUtils;
 import test.cli.cloudify.AbstractLocalCloudTest;
 
 public class InstallUninstallPetclinicTest extends AbstractLocalCloudTest {
 	
 	private static int ITERATIONS = 3;
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void multipleInstallUninstallTest() throws IOException, InterruptedException {
 		
 		for (int i = 0 ; i < ITERATIONS ; i++) {
 			assertTrue(installPetclinic());
 			assertPetclinicPageExists();
 			assertTrue(uninstallPetclinic());
 		}
 		
 	}
 	
 	private boolean installPetclinic() throws IOException, InterruptedException {
 		String petclinicPath = ScriptUtils.getBuildPath() + "/examples/petclinic";
 		String cliOutput = runCommand("connect " + restUrl + ";install-application --verbose " + petclinicPath);
 		return cliOutput.toLowerCase().contains("application petclinic installed successfully");
 	}
 	
 	private boolean uninstallPetclinic() throws IOException, InterruptedException {
		String cliOutput = runCommand("connect " + restUrl + ";uninstall-application --verbose petclinic-mongo");
 		return cliOutput.toLowerCase().contains("application petclinic uninstalled successfully");
 	}
 	
 	private void assertPetclinicPageExists() {
 		
 		Machine localMachine = admin.getMachines().getMachines()[0];
 		
 		WebClient client = new WebClient(BrowserVersion.getDefault());
 		
         HtmlPage page = null;
         try {
             page = client.getPage("http://" + localMachine.getHostAddress() + ":8080/petclinic-mongo/");
         } catch (IOException e) {
             fail("Could not get a resposne from the petclinic URL " + e.getMessage());
         }
         assertEquals("OK", page.getWebResponse().getStatusMessage());
 		
 		
 	}
 
 }
