 package test.cli.cloudify;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 
 import org.cloudifysource.dsl.internal.DSLException;
 import test.AbstractTest;
 import framework.tools.SGTestHelper;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 
 public class AbstractCloudEc2Test extends AbstractTest {
     
 	private static final String AWS_API_KEY = "fPdu7rYBF0mtdJs1nmzcdA8yA/3kbV20NgInn4NO";
 	private static final  String AWS_USER= "0VCFNJS3FXHYC7M6Y782";
 	
 	protected static final String WEBUI_PORT = String.valueOf(8099); 
 	protected static final String REST_PORT = String.valueOf(8100); 
 	private static final String IP_REGEX= "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"; 
 	private static final String WEBUI_URL_REGEX= "Webui service is available at: (http://" + IP_REGEX + ":" + WEBUI_PORT +")";
 	private static final String REST_URL_REGEX= "Rest service is available at: (http://" + IP_REGEX + ":" + REST_PORT + ")";
 	protected static final int NUM_OF_MANAGEMENT_MACHINES = 1;
 	protected static final String INSTALL_TRAVEL_EXPECTED_OUTPUT = "Application travel installed successfully";
 	protected static final String UNINSTALL_TRAVEL_EXPECTED_OUTPUT = "Application travel uninstalled successfully";
 	
     protected URL[] restAdminUrl = new URL[NUM_OF_MANAGEMENT_MACHINES];
     protected URL[] webUIUrl = new URL[NUM_OF_MANAGEMENT_MACHINES];
     
 	private File originalEc2DslFile;
 	private File backupEc2DslFile;
 	private File targetPem;
     
 	@Override
     @BeforeMethod
     public void beforeTest() {
         boolean success = false;
 		try {
         	bootstrapCloud();
         	success = true;
 		} 
 		catch (IOException e) {
 			LogUtils.log("bootstrap-cloud failed.", e);
 		} 
 		catch (InterruptedException e) {
 			LogUtils.log("bootstrap-cloud failed.", e);
 		} 
 		catch (Exception e) {
 		    LogUtils.log("bootstrap-cloud failed.", e);
 		}
 		finally {
         	if (!success) {
         		teardownCloud();
         		Assert.fail("bootstrap-cloud failed.");
         	}
         }
     }
     
 	private void bootstrapCloud() throws IOException, InterruptedException, DSLException {
 		
 	    //String applicationPath = (new File(ScriptUtils.getBuildPath(), "examples/travel").toString()).replace('\\', '/');
		String ec2TestPath = (SGTestHelper.getSGTestRootDir() + "/apps/cloudify/cloud/ec2").replace('\\', '/');
 		String sshKeyPemName = "cloud-demo.pem";
 		
 		// ec2 plugin should include recipe that includes secret key 
 		File ec2PluginDir = new File(ScriptUtils.getBuildPath() , "tools/cli/plugins/esc/ec2/");
 		originalEc2DslFile = new File(ec2PluginDir, "ec2-cloud.groovy");
 		backupEc2DslFile = new File(ec2PluginDir, "ec2-cloud.backup");
 		
 		
 		// Read file contents
 		final String originalDslFileContents = FileUtils.readFileToString(originalEc2DslFile);
 		Assert.assertTrue(originalDslFileContents.contains("ENTER_USER"), "Missing ENTER_USER statement in ec2-cloud.groovy");
 		Assert.assertTrue(originalDslFileContents.contains("ENTER_API_KEY"), "Missing ENTER_API_KEY statement in ec2-cloud.groovy");
 		
 		// first make a backup of the original file
 		FileUtils.copyFile(originalEc2DslFile, backupEc2DslFile);
 		
 		final String modifiedDslFileContents = originalDslFileContents.replace("ENTER_USER", AWS_USER).replace("ENTER_API_KEY", AWS_API_KEY);
 		FileUtils.write(originalEc2DslFile, modifiedDslFileContents);
 	
 		// upload dir needs to contain the sshKeyPem 
 		targetPem = new File(ScriptUtils.getBuildPath(), "tools/cli/plugins/esc/ec2/upload/" + sshKeyPemName);
         FileUtils.copyFile(new File(ec2TestPath, sshKeyPemName), targetPem);
         assertTrue("File not found", targetPem.isFile());
 		
 		String output = CommandTestUtils.runCommandAndWait("bootstrap-cloud --verbose ec2");
 
 		Pattern webUIPattern = Pattern.compile(WEBUI_URL_REGEX);
 		Pattern restPattern = Pattern.compile(REST_URL_REGEX);
 		
 		Matcher webUIMatcher = webUIPattern.matcher(output);
 		Matcher restMatcher = restPattern.matcher(output);
 		
 		// This is sort of hack.. currently we are outputing this over ssh and locally with different results
 		
 		assertTrue("Could not find remote (internal) webui url", webUIMatcher.find()); 
 		assertTrue("Could not find remote (internal) rest url", restMatcher.find());
 		
 		for (int i = 0; i < NUM_OF_MANAGEMENT_MACHINES ; i++) {
 			assertTrue("Could not find actual webui url", webUIMatcher.find());
 			assertTrue("Could not find actual rest url", restMatcher.find());
 
 			String rawWebUIUrl = webUIMatcher.group(1);
 			String rawRestAdminUrl = restMatcher.group(1);
 			
 			webUIUrl[i] = new URL(rawWebUIUrl);
 			restAdminUrl[i] = new URL(rawRestAdminUrl);
 		}
 	}
 
 
 	@Override
     @AfterMethod
     public void afterTest() {
         teardownCloud();
     }
 
 	private void deleteCloudFiles() throws IOException {
 		// undo all the changes we made in the local ec2 folder
 		FileUtils.copyFile(backupEc2DslFile, originalEc2DslFile);
 		FileUtils.deleteQuietly(backupEc2DslFile);
 		FileUtils.deleteQuietly(targetPem);
 		
 	}
 
 	private void teardownCloud() {
 		
 		try {
 			CommandTestUtils.runCommandAndWait("teardown-cloud --verbose -force ec2");
 		} catch (IOException e) {
 			Assert.fail("teardown-cloud failed. SHUTDOWN VIRTUAL MACHINES MANUALLY !!!",e);
 		} catch (InterruptedException e) {
 			Assert.fail("teardown-cloud failed. SHUTDOWN VIRTUAL MACHINES MANUALLY !!!",e);
 		}
 		finally {
 			try {
 				deleteCloudFiles();
 			} catch (IOException e) {
 				AssertFail("Failed to clean up after test finished: " + e.getMessage(), e);
 			}
 		}
 	}
 	
 	// TODO: commit pending code to CLI
 	private static void uploadCloudifyInstallationToS3() throws IOException, InterruptedException {
 	    
 	    String container = "test-repository-ec2dev";
 	    String path = "cloudify/gigaspaces.zip";
 	    File cloudifyInstallation = ScriptUtils.getGigaspacesZipFile();
 
 	    String command = new StringBuilder()
 	        .append("upload-cloud ")
 	        .append("-check-changed ")
 	        .append("-public ")
 	        .append("-destination ").append(path).append(" ")
 	        .append("-container ").append(container).append(" ")
 	        .append("-source ").append(cloudifyInstallation.getAbsolutePath().replace('\\', '/')).append(" ")
 	        .append("--verbose ")
 	        .append("ec2")
 	        .toString();
 	    
 	    CommandTestUtils.runCommandAndWait(command);
 	    
 	}
 	
 }
