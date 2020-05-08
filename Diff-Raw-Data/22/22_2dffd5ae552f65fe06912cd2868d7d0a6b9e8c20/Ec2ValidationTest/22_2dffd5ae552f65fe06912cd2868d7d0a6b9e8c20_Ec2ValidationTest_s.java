 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2;
 
 import java.io.File;
 import java.io.IOException;
 
 import iTests.framework.tools.SGTestHelper;
 import org.cloudifysource.quality.iTests.framework.utils.CloudBootstrapper;
 import org.cloudifysource.quality.iTests.framework.utils.IOUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.NewAbstractCloudTest;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 public class Ec2ValidationTest  extends NewAbstractCloudTest {
 
 	private String groovyFileName;
 	CloudBootstrapper bootstrapper;
 
 	
 	@BeforeClass
 	public void init() throws Exception {
 		bootstrapper = new CloudBootstrapper();
 		bootstrapper.scanForLeakedNodes(false);
 	}
 	
	
	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
	public void wrongProviderTest() throws IOException, InterruptedException {
		try {
			groovyFileName = "wrongprovider-ec2-cloud.groovy";
			super.bootstrap(bootstrapper);
			assertTrue("The provider name is wrong yet no error was thrown", false);
		} catch (Throwable ae) {
			assertTrue("The provider is wrong but the wrong error was thrown. Error was: " + ae.getMessage(),
					ae.getMessage().contains("Provider not supported"));
		}
	}

	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void wrongCredentialsTest() throws IOException, InterruptedException {
 		try {
 			groovyFileName = "wrongcredentials-ec2-cloud.groovy";
 			super.bootstrap(bootstrapper);
 			assertTrue("The credentials are wrong yet no error was thrown", false);
 		} catch (Throwable ae) {
 			assertTrue("The credentials are wrong but the wrong error was thrown. Error was: " + ae.getMessage(),
 					ae.getMessage().contains("HTTP/1.1 401 Unauthorized"));
 		}
 	}
 
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void wrongImageTest() throws IOException, InterruptedException {
 		try {
 			groovyFileName = "wrongimage-ec2-cloud.groovy";
 			super.bootstrap(bootstrapper);
 			assertTrue("The imageId is invalid yet no error was thrown", false);
 		} catch (Throwable ae) {
 			assertTrue("The imageId is invalid but the wrong error was thrown. Error was: " + ae.getMessage(),
 					ae.getMessage().contains("Invalid template configuration: could not get imageId"));
 		}
 	}
 	
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void wrongHardwareTest() throws IOException, InterruptedException {
 		try {
 			groovyFileName = "wronghardware-ec2-cloud.groovy";
 			super.bootstrap(bootstrapper);
 			assertTrue("The hardwareId is invalid yet no error was thrown", false);
 		} catch (Throwable ae) {
 			assertTrue("The hardwareId is invalid but the wrong error was thrown. Error was: " + ae.getMessage(),
 					ae.getMessage().contains("Invalid template configuration: hardwareId"));
 		}
 	}
 	
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void wrongSecurityGroupTest() throws IOException, InterruptedException {
 		try {
 			groovyFileName = "wrongsecuritygroup-ec2-cloud.groovy";
 			super.bootstrap(bootstrapper);
 			assertTrue("The security group name is wrong yet no error was thrown", false);
 		} catch (Throwable ae) {
 			assertTrue("The security group name is wrong but the wrong error was thrown. "
 					+ "Error was: "	+ ae.getMessage(), 
 					ae.getMessage().contains("Invalid security groups configuration: Invalid security group name"));
 		}
 	}
 	
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void wrongKeyPairTest() throws IOException, InterruptedException {
 		try {
 			groovyFileName = "wrongkeypair-ec2-cloud.groovy";
 			super.bootstrap(bootstrapper);
 			assertTrue("The key-pair name is wrong yet no error was thrown", false);
 		} catch (Throwable ae) {
 			assertTrue("The key-pair name is wrong but the wrong error was thrown. Error was: " + ae.getMessage(),
					ae.getMessage().contains("Invalid key-pair configuration: Invalid key-pair name"));
 		}
 	}
 	
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void wrongCloudifyUrlTest() throws IOException, InterruptedException {
 		try {
 			groovyFileName = "wrongcloudifyurl-ec2-cloud.groovy";
 			super.bootstrap(bootstrapper);
 			assertTrue("The cloudify URL is wrong yet no error was thrown", false);
 		} catch (Throwable ae) {
 			assertTrue("The cloudify URL is wrong but the wrong error was thrown. Error was: " + ae.getMessage(),
 					ae.getMessage().contains("Invalid cloudify URL"));
 		}
 	}
 	
 
 	@AfterClass
 	public void teardown() throws Exception {
 		//assertTrue("Leaked node were found", !getService().scanLeakedAgentAndManagementNodes());
 		//JCloudsUtils.closeContext();
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
 		//replace the ec2-cloud.groovy with a wrong version, to fail the validation.
 		File standardGroovyFile = new File(getService().getPathToCloudFolder(), "ec2-cloud.groovy");
		File wrongproviderGroovyFile = new File(SGTestHelper.getSGTestRootDir() + "/src/main/resources/apps/cloudify/cloud/ec2/" + groovyFileName);
		IOUtils.replaceFile(standardGroovyFile, wrongproviderGroovyFile);
 		File newFile = new File(getService().getPathToCloudFolder(), groovyFileName);
 		if (newFile.exists()) {
 			newFile.renameTo(standardGroovyFile);
 		}
 	}
 
 }
