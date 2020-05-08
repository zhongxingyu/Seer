 /**
  * 
  */
 package provisioning;
 
 import static org.junit.Assert.fail;
 
 import java.util.Arrays;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import commons.cloud.MachineType;
 import commons.cloud.Provider;
 import commons.cloud.Request;
 import commons.cloud.User;
 import commons.cloud.UtilityResultEntry;
 import commons.config.Configuration;
 import commons.config.PropertiesTesting;
 import commons.sim.components.MachineDescriptor;
 
 /**
  * @author Ricardo Ara&uacute;jo Santos - ricardo@lsd.ufcg.edu.br
  *
  */
 @RunWith(PowerMockRunner.class)
 public class MonitorTest {
 
 	private Monitor monitor;
 
 	@Before
 	public void setUp() throws ConfigurationException{
 		Configuration.buildInstance(PropertiesTesting.VALID_FILE);
 		monitor = new DynamicProvisioningSystem();
 	}
 
 	/**
 	 * Test method for {@link provisioning.Monitor#reportRequestFinished(commons.cloud.Request)}.
 	 */
 	@Test(expected=RuntimeException.class)
 	public void testReportRequestFinished() {
 		Request request = EasyMock.createStrictMock(Request.class);
		EasyMock.expect(request.getUserID()).andReturn("100").times(2);
 		EasyMock.replay(request);
 		
 		monitor.reportRequestFinished(request);
 		
 		EasyMock.verify(request);
 	}
 
 	/**
 	 * Test method for {@link provisioning.Monitor#requestQueued(long, commons.cloud.Request, int)}.
 	 */
 	@Test
 	public void testRequestQueued() {
 		fail("Not yet implemented");
 	}
 
 	/**
 	 * Test method for {@link provisioning.Monitor#evaluateUtilisation(long, commons.sim.provisioningheuristics.RanjanStatistics, int)}.
 	 */
 	@Test
 	public void testEvaluateUtilisation() {
 		fail("Not yet implemented");
 	}
 
 	/**
 	 * Test method for {@link provisioning.Monitor#machineTurnedOff(MachineDescriptor)}.
 	 */
 	@Test(expected=RuntimeException.class)
 	public void testMachineTurnedOffWithInexistentMachine() {
 		monitor.machineTurnedOff(new MachineDescriptor(111, true, MachineType.SMALL));
 	}
 
 	/**
 	 * Test method for {@link provisioning.Monitor#machineTurnedOff(MachineDescriptor)}.
 	 */
 	@Test
 	public void testMachineTurnedOffWithExistentMachine() {
 		MachineDescriptor machineDescriptor = Configuration.getInstance().getProviders().get(0).buyMachine(false, MachineType.LARGE);
 		monitor.machineTurnedOff(machineDescriptor);
 	}
 
 	/**
 	 * Test method for {@link provisioning.Monitor#chargeUsers(long)}.
 	 */
 	@PrepareForTest(Configuration.class)
 	@Test
 	public void testChargeUsers() {
 		Provider provider = EasyMock.createStrictMock(Provider.class);
 		EasyMock.expect(provider.getName()).andReturn("amazon");
 		EasyMock.expect(provider.getAvailableTypes()).andReturn(new MachineType[]{MachineType.SMALL});
 		provider.calculateCost(EasyMock.isA(UtilityResultEntry.class), EasyMock.anyLong());
 		
 		User user = EasyMock.createStrictMock(User.class);
 		EasyMock.expect(user.getId()).andReturn(1);
 		user.calculatePartialReceipt(EasyMock.isA(UtilityResultEntry.class));
 		
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		EasyMock.expect(config.getProviders()).andReturn(Arrays.asList(provider));
 		EasyMock.expect(config.getUsers()).andReturn(Arrays.asList(user));
 		
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
 		
 		PowerMock.replayAll(config, provider, user);
 		
 		new DynamicProvisioningSystem().chargeUsers(0);
 		
 		PowerMock.verifyAll();
 	}
 
 }
