 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2;
 
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.NewAbstractCloudTest;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.ec2.Ec2CloudService;
 
 import java.io.IOException;
 
 public class EuWestRegionEc2Test extends NewAbstractCloudTest {
 
 	private Ec2CloudService service;
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testEuWestRegion() throws IOException, InterruptedException {
 		doSanityTest("travel", "travel");
 	}
 
 	@Override
 	protected String getCloudName() {
 		return "ec2";
 	}
 
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}
 
 	@Override
 	protected void customizeCloud() {
 		service = (Ec2CloudService) getService();
		service.setRegion("eu-west-1");
 		service.setKeyPair("ec2-sgtest-eu");
 	} 
 
 }
