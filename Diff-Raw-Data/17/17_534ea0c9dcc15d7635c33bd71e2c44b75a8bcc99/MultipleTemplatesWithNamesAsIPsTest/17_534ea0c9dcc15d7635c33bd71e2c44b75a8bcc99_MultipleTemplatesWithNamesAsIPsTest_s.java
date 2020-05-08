 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon;
 
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.byon.MultipleTemplatesByonCloudService;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.NetworkUtils;
 
 
 /**
  * this test is exactly like the super test, except that the hosts are specified as host names instead of IP's.
  * @author elip
  *
  */
 public class MultipleTemplatesWithNamesAsIPsTest extends MultipleMachineTemplatesTest {
 
 	private MultipleTemplatesByonCloudService service = new MultipleTemplatesByonCloudService();
 	
 	@Override
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
		
 		String[] machines = service.getMachines();
		LogUtils.log("converting every other address to host name");
 		String[] machinesHostNames = NetworkUtils.resolveIpsToHostNames(machines);
 		service.setMachines(machinesHostNames);
		super.bootstrap(service);
 	}
 	
 
 	@Override
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void test() throws Exception {
 		super.setService(service);
 		super.test();
 	}
 
 
 	/**
 	 * Gets the address of the machine on which the given processing unit is deployed. 
 	 * @param puName The name of the processing unit to look for
 	 * @return The address of the machine on which the processing unit is deployed.
 	 */
 	@Override
 	protected String getPuHost(final String puName) {
 		ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(puName);
 		Assert.assertNotNull(pu.getInstances()[0], puName + " processing unit is not found");
 		return pu.getInstances()[0].getMachine().getHostName();	
 	}
 
 }
