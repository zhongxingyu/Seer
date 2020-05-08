 package org.cloudifysource.quality.iTests.test.cli.cloudify.security;
 
 import iTests.framework.utils.AssertUtils;
 import org.cloudifysource.quality.iTests.framework.utils.LocalCloudBootstrapper;
 import iTests.framework.utils.LogUtils;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.AbstractSecuredLocalCloudTest;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils.ProcessResult;
 import org.testng.Assert;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.Test;
 
 public class LocalCloudSecurityPasswordFailTest extends AbstractSecuredLocalCloudTest {
 
 	private static final String FAKE_PASSWORD = "sgtes";
	private static final String FAIL_PASSWORD_STRING = "Invalid keystore file: Keystore was tampered with, or password was incorrect: Operation failed. CLIStatusException, reason code: invalid_keystore_file, message arguments: Keystore was tampered with, or password was incorrect";
 
 	private LocalCloudBootstrapper bootstrapper;
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void wrongPasswordTest () {
 		bootstrapper = new LocalCloudBootstrapper();
 		bootstrapper.setBootstrapExpectedToFail(true); // new flag which says bootstrapper is about to fail
 		bootstrapper.secured(true).securityFilePath(SecurityConstants.BUILD_SECURITY_FILE_PATH);
 		bootstrapper.keystoreFilePath(SecurityConstants.DEFAULT_KEYSTORE_FILE_PATH).keystorePassword(FAKE_PASSWORD);
 		ProcessResult res = null ;
 		try {
 			res  = super.bootstrap(bootstrapper);
 		} catch (Exception e) {
 			AssertUtils.assertFail("bootstrap was failed NOT because of illegal password", e);
 		} 
 		// The interesting case - bootstrap fails (because of the illegal password)
 		Assert.assertNotNull(res);
 		Assert.assertTrue(res.getOutput().contains(FAIL_PASSWORD_STRING));
 		LogUtils.log("wrongPasswordTest security test passed!");
 	}
 	
 	@AfterClass
 	public void teardown() {
 		// test doesn't execute teardown because no bootstrap was performed
 	}
 }
 
