 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2;
 
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.utils.IOUtils;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.cloudifysource.esc.installer.remoteExec.BootstrapScriptErrors;
 import org.cloudifysource.quality.iTests.framework.utils.CloudBootstrapper;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.NewAbstractCloudTest;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 /**
  * This test makes a bootstrap on ec2 fail by changing the JAVA_HOME path to a bad one in the bootstrap-management.sh file.
  * <p>After the bootstrap fails, the test checks if the management machine was shutdown.
  * 
  * @author noak
  *
  */
 public class BootstrapErrorCodesTest extends NewAbstractCloudTest {
 
 	private static final String STANDARD_BOOTSTRAP_SCRIPT = "bootstrap-management.sh";
 
 	private String badBootstrapScript = null;
 	private CloudBootstrapper bootstrapper;
 
 	@BeforeClass
 	public void init() throws Exception {
 		bootstrapper = new CloudBootstrapper();
 		bootstrapper.scanForLeakedNodes(true);
 		bootstrapper.setBootstrapExpectedToFail(true);
 	}
 	
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void wrongJavaDownloadUrlTest() throws Exception {
 		badBootstrapScript = "wrong-java-path-bootstrap-management.sh";
 		super.bootstrap(bootstrapper);
 		String bootstrapOutput = bootstrapper.getLastActionOutput();
 		assertTrue("Java download URL is wrong but the wrong error was thrown. Reported error: " + bootstrapOutput,
 				isBootstrapErrorCorrect(bootstrapOutput, BootstrapScriptErrors.JAVA_DOWNLOAD_FAILED));
 	}
 
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void wrongCloudifyOverridesUrlTest() throws Exception {
 		badBootstrapScript = "wrong-cloudify-overrides-bootstrap-management.sh";
 		super.bootstrap(bootstrapper);
 		String bootstrapOutput = bootstrapper.getLastActionOutput();
 		assertTrue("Cloudify overrides URL is wrong but the wrong error was thrown. Reported error: " + bootstrapOutput,
 				isBootstrapErrorCorrect(bootstrapOutput, BootstrapScriptErrors.CLOUDIFY_OVERRIDES_DOWNLOAD_FAILED));
 	}
 	
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void wrongChmodCommandTest() throws Exception {
 		badBootstrapScript = "wrong-chmod-bootstrap-management.sh";
 		super.bootstrap(bootstrapper);
 		String bootstrapOutput = bootstrapper.getLastActionOutput();
 		assertTrue("The chmod command is wrong but the wrong error was thrown. Reported error: " + bootstrapOutput,
 				isBootstrapErrorCorrect(bootstrapOutput, BootstrapScriptErrors.CLOUDIFY_CHMOD_FAILED));
 	}
 	
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void failedCloudifyExecutionTest() throws Exception {
 		badBootstrapScript = "failed-cloudify-execution-bootstrap-management.sh";
 		super.bootstrap(bootstrapper);
 		String bootstrapOutput = bootstrapper.getLastActionOutput();
 		assertTrue("Cloudify execution failed but the wrong error was thrown. Reported error: " + bootstrapOutput,
				isBootstrapErrorCorrect(bootstrapOutput, BootstrapScriptErrors.CUSTOM_ERROR));
 	}
 	
 	
 	@AfterMethod
 	public void teardown() throws Exception {
 		super.teardown();
 	}
 
 	@Override
 	protected String getCloudName() {
 		return "ec2";
 	}
 
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}
 	
 	protected void customizeCloud() throws IOException {
 		//replace the bootstrap-management with a bad version, to fail the bootstrap.
 		File standardBootstrapFile = new File(getService().getPathToCloudFolder() + "/upload", STANDARD_BOOTSTRAP_SCRIPT);
 		File badBootstrapFile = new File(SGTestHelper.getSGTestRootDir() + "/src/main/resources/apps/cloudify/cloud/ec2/" + badBootstrapScript);
 		IOUtils.replaceFile(standardBootstrapFile, badBootstrapFile);
 		File newFile = new File(getService().getPathToCloudFolder() + "/upload", badBootstrapScript);
 		if (newFile.exists()) {
 			newFile.renameTo(standardBootstrapFile);
 		}
 		
 		System.out.println("replacing line ending (DOS2UNIX)");
 		IOUtils.replaceTextInFile(standardBootstrapFile.getAbsolutePath(), "\r\n", "\n");// DOS2UNIX
 	}
 	
 	private static boolean isBootstrapErrorCorrect(final String messageText, final BootstrapScriptErrors expectedError) {
 		return (messageText.contains(String.valueOf(expectedError.getErrorCode())) &&
 				messageText.contains(expectedError.getErrorMessage()));
 	}
 
 }
