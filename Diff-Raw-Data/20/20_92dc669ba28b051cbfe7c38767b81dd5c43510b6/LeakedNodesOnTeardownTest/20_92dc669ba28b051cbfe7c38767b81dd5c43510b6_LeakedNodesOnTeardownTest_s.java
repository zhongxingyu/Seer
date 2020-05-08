 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.rackspace;
 
import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.NewAbstractCloudTest;
import org.cloudifysource.quality.iTests.framework.utils.ServiceInstaller;

 /**
  * CLOUDIFY-1273
  * @author elip
  *
  */
 public class LeakedNodesOnTeardownTest extends NewAbstractCloudTest {
 	
	private boolean teardown = false;
	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void testTeardownWithoutUninstall() throws Exception {
 		
 		ServiceInstaller tomcatInstaller = new ServiceInstaller(getRestUrl(), "tomcat");
 		tomcatInstaller.recipePath("tomcat");
 		
 		tomcatInstaller.install();
 		
 		// this will fail if leaked nodes are found after the teardown.
 		super.teardown();
		teardown = true;
 	}
 
 	@Override
 	protected String getCloudName() {
 		return "rackspace";
 	}
 
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}

	@AfterClass(alwaysRun = true)
	protected void teardown() throws Exception {
		if (!teardown) {
			super.teardown();
		}
	}
 }
