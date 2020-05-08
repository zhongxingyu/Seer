 package test.cli.cloudify.recipes;
 
 import static org.testng.AssertJUnit.fail;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.openspaces.admin.machine.Machine;
 import org.testng.annotations.Test;
 
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 import test.cli.cloudify.AbstractLocalCloudTest;
 import framework.utils.AssertUtils;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 
 public class ProductExamplesTest extends AbstractLocalCloudTest {
 
 	private String examplesDirPath = ScriptUtils.getBuildPath() + "/examples";
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void installTravel() throws IOException, InterruptedException {
 		String travelDirPath = examplesDirPath + "/travel";
 		String cliOutput = runCommand("connect " + restUrl + ";install-application --verbose " + travelDirPath);
 		assertTrue("travel app couln't be installed",
 				cliOutput.toLowerCase().contains("application travel installed successfully"));
 	}
 
 	private void checkOSSupportForMongo() throws IOException, InterruptedException {
 		if (!isWindows()) {
 			String cmd = "uname -a";
 			Runtime run = Runtime.getRuntime();
 			Process pr = run.exec(cmd);
 			pr.waitFor();
 			BufferedReader buf = null;
 			StringBuilder sb = new StringBuilder();
 			try {
 				buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
 
 				
 				String line = "";
 				while ((line = buf.readLine()) != null) {
 					sb.append(line).append(System.getProperty("line.separator"));
 				}
 				
 			} finally {
 				if (buf != null) {
 					buf.close();
 				}
 			}
 			
 
 			String out = sb.toString();
 			LogUtils.log("Running Linux verion: " + out);
 			out = out.toLowerCase();
 			if (out.contains("2007") && out.contains("linux")) {
 				AssertUtils.AssertFail("Mongo service is not supported in this version of linux");
 			}
 		}
 	}
 
 	private static boolean isWindows() {
 		return System.getProperty("os.name").toLowerCase().startsWith("win");
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void installPetclinic() throws IOException, InterruptedException {
 
 		//TODO: fix and un-comment this according to the supported Linux versions.
 		//checkOSSupportForMongo();
 		String petclinicDirPath = examplesDirPath + "/petclinic";
 		String cliOutput = runCommand("connect " + restUrl + ";install-application --verbose " + petclinicDirPath);
 		assertTrue("petclinic app couln't be installed",
				cliOutput.toLowerCase().contains("application petclinic-mongo installed successfully"));
 		
 		
 	}
 	
 	private void assertPetclinicPageExists() {
 		
 		Machine localMachine = admin.getMachines().getMachines()[0];
 		
 		WebClient client = new WebClient(BrowserVersion.getDefault());
 		
         HtmlPage page = null;
         try {
             page = client.getPage("http://" + localMachine.getHostAddress() + ":8080/petclinic-mongo");
         } catch (IOException e) {
             fail("Could not get a resposne from the petclinic URL " + e.getMessage());
         }
         assertEquals("OK", page.getWebResponse().getStatusMessage());
 		
 		
 	}
 }
