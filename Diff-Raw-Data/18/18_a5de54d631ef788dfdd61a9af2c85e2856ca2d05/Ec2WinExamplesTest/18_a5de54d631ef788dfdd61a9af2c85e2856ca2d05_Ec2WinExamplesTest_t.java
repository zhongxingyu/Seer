 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2win;
 
 import java.io.File;
 import java.io.IOException;
 
 import junit.framework.Assert;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.AbstractExamplesTest;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.utils.LogUtils;
 
 public class Ec2WinExamplesTest extends AbstractExamplesTest {
 	
 	private static final String WINDOWS_APPS_PATH = CommandTestUtils.getPath("src/main/resources/apps/USM/usm/applications/windows");
 	
 	@Override
 	protected String getCloudName() {
 		return "ec2-win";
 	}
 
 	@Override
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		try {
 			prepareApplications();
 		} catch (final Exception e) {
 			Assert.fail("Failed preparing windows applications for deployment. Reason : " + e.getMessage());
 		}
 		super.bootstrap();
 	}
 
 	protected void prepareApplications() throws IOException {
 		prepareApplication("travel-win");
 		prepareApplication("petclinic-simple-win");
 		prepareApplication("petclinic-win");
 		prepareApplication("helloworld-win");
 	}
 
 	protected void prepareApplication(String applicationName) throws IOException {
 
 		String applicationSGPath = WINDOWS_APPS_PATH + "/" + applicationName;
 		String applicationBuildPath = SGTestHelper.getBuildDir() + "/recipes/apps/";
 		
 		LogUtils.log("copying " + applicationSGPath + " to " + applicationBuildPath);
 		FileUtils.copyDirectoryToDirectory(new File(applicationSGPath), new File(applicationBuildPath));
 		
 	}
 
 	@Override
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 4, enabled = true)
 	public void testTravel() throws Exception {
 		super.testTravel();
 	}
 
 	@Override
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 4, enabled = true)
 	public void testPetclinicSimple() throws Exception {
 		super.testPetclinicSimple();
 	}
 
 	@Override
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 4, enabled = true)
 	public void testPetclinic() throws Exception {
 		super.testPetclinic();
 	}
 
 	@Override
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 4, enabled = true)
 	public void testHelloWorld() throws Exception {
 		super.testHelloWorld();
 	}
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 4, enabled = true)
 	public void testLinuxAgent() throws Exception {
		super.testLinuxAgent();
 	}
 
 	@Override
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}
 }
