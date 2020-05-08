 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.openstack.examples;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.cloudifysource.esc.driver.provisioning.CloudProvisioningException;
 import org.cloudifysource.esc.driver.provisioning.ComputeDriverConfiguration;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenStackCloudifyDriver;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenStackComputeClient;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenstackException;
 import org.cloudifysource.esc.driver.provisioning.openstack.rest.NovaServer;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.AbstractExamplesTest;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 /**
  * install a service with 4 instances on the HP cloud. 
  * The 4 agent machines started must start in different availability zones.
  * 
  * @author adaml
  *
  */
 public class OpenstackDistributedAvailabilityZonesTest extends AbstractExamplesTest {
 	
 	private static final String AGENT_MACHINE_SUFFIX = "agent";
 	private static final String AGENT_MACHINE_PREFIX = "openstackdistribute";
 	private static final String SERVICE_PATH = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/simpleAZ");
 	private static final String SERVICE_NAME = "simpleAZ";
 	private OpenStackCloudifyDriver openstackCloudDriver;
 	
 
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 		
 	@Override
 	protected String getCloudName() {
 		return "hp-grizzly";
 	}
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 4, enabled = false)
 	public void testDistributedAvailabilityZones() throws IOException, InterruptedException, OpenstackException, CloudProvisioningException {
 		
 		//we create an instance of the cloud driver so we could query it regarding its availability zones
 		initCloudDriver();
 		
 		// install a simple service with 4 instances.
 		installServiceAndWait(SERVICE_PATH, SERVICE_NAME);
 		
 		//assert the 4 agent machines started in different availability zones
 		assertServersAZ();
 	}
 	
 	private void assertServersAZ() throws OpenstackException {
 		List<NovaServer> servers = ((OpenStackComputeClient) openstackCloudDriver.getComputeContext()).getServers();
		assertTrue("Expecting 4 agent machines got " + servers.size(), servers.size() == 4);
 		int az1 = 0;
 		int az2 = 0;
 		int az3 = 0;
 		for (NovaServer novaServer : servers) {
 			String serverName = novaServer.getName();
 			if (serverName.contains(AGENT_MACHINE_PREFIX) && serverName.contains(AGENT_MACHINE_SUFFIX)) {
 				// this is an agent machine. 
 				final String availabilityZone = novaServer.getAvailabilityZone();
 				if (availabilityZone.equals("az1")) {
 					az1 = az1 + 1;
 				}
 				if (availabilityZone.equals("az2")) {
 					az2 = az2 + 1;
 				}
 				if (availabilityZone.equals("az3")) {
 					az3 = az3 + 1;
 				}
 			}
 		}
 		assertTrue("Expecting 2 machines in az1", az1 == 2);
 		assertTrue("Expecting 1 machines in az2", az2 == 1);
 		assertTrue("Expecting 1 machines in az3", az3 == 1);
 	}
 
 	private void initCloudDriver() throws CloudProvisioningException {
 		openstackCloudDriver = new OpenStackCloudifyDriver();
         ComputeDriverConfiguration conf = new ComputeDriverConfiguration();
         conf.setCloud(getService().getCloud());
         conf.setServiceName("default." + SERVICE_NAME);
         conf.setCloudTemplate("SMALL_LINUX");
         openstackCloudDriver.setConfig(conf);
 	}
 
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}
 
 }
