 /**
  * 
  */
 package provisioning;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.easymock.Capture;
 import org.easymock.EasyMock;
 import org.junit.Test;
 
 import util.ValidConfigurationTest;
 
 import commons.cloud.MachineType;
 import commons.cloud.Provider;
 import commons.cloud.User;
 import commons.cloud.UtilityResult;
 import commons.config.Configuration;
 import commons.io.Checkpointer;
 import commons.io.WorkloadParser;
 import commons.sim.DynamicConfigurable;
 import commons.sim.components.MachineDescriptor;
 import commons.sim.util.SaaSAppProperties;
 
 /**
  * Test class for {@link DPS}.
  * @author Ricardo Ara&uacute;jo Santos - ricardo@lsd.ufcg.edu.br
  */
 public class DynamicProvisioningSystemTest extends ValidConfigurationTest {
 	
 	/**
 	 * {@inheritDoc}
 	 * @throws ConfigurationException 
 	 */
 	@Override
 	public void setUp() throws Exception{
 		super.setUp();
 		buildFullConfiguration();
 	}
 	
 	/**
 	 * Test method for {@link provisioning.DynamicProvisioningSystem#DynamicProvisioningSystem()}.
 	 */
 	@Test
 	public void testDynamicProvisioningSystemWithValidConfigurationData() {
 		assertNotNull(new DynamicProvisioningSystem());
 	}
 	
 	/**
 	 * Test method for {@link provisioning.DPS#registerConfigurable(commons.sim.DynamicConfigurable)}.
 	 */
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testRegisterConfigurableWithMultipleServers() {
 		Configuration.getInstance().setProperty(SaaSAppProperties.APPLICATION_INITIAL_SERVER_PER_TIER, "7");
 		
 		DynamicConfigurable configurable = EasyMock.createStrictMock(DynamicConfigurable.class);
 		Capture<MachineDescriptor> [] descriptor = new Capture[7];
 		for (int i = 0; i < descriptor.length; i++) {
 			descriptor[i] = new Capture<MachineDescriptor>();
 		}
 		configurable.addServer(EasyMock.anyInt(), EasyMock.capture(descriptor [0]), EasyMock.anyBoolean());
 		configurable.addServer(EasyMock.anyInt(), EasyMock.capture(descriptor [1]), EasyMock.anyBoolean());
 		configurable.addServer(EasyMock.anyInt(), EasyMock.capture(descriptor [2]), EasyMock.anyBoolean());
 		configurable.addServer(EasyMock.anyInt(), EasyMock.capture(descriptor [3]), EasyMock.anyBoolean());
 		configurable.addServer(EasyMock.anyInt(), EasyMock.capture(descriptor [4]), EasyMock.anyBoolean());
 		configurable.addServer(EasyMock.anyInt(), EasyMock.capture(descriptor [5]), EasyMock.anyBoolean());
 		configurable.addServer(EasyMock.anyInt(), EasyMock.capture(descriptor [6]), EasyMock.anyBoolean());
 		configurable.setWorkloadParser(EasyMock.anyObject(WorkloadParser.class));
 		configurable.setMonitor(EasyMock.anyObject(Monitor.class));
 		
 		EasyMock.replay(configurable);
 		
 		DynamicProvisioningSystem dps = new DynamicProvisioningSystem();
 		dps.registerConfigurable(configurable);
 		
 		assertEquals(MachineType.C1_MEDIUM, descriptor[0].getValue().getType());
 		assertEquals(MachineType.C1_MEDIUM, descriptor[1].getValue().getType());
 		assertEquals(MachineType.C1_MEDIUM, descriptor[2].getValue().getType());
 		assertEquals(MachineType.C1_MEDIUM, descriptor[3].getValue().getType());
 		assertEquals(MachineType.M1_LARGE, descriptor[4].getValue().getType());
 		assertEquals(MachineType.M1_LARGE, descriptor[5].getValue().getType());
 		assertEquals(MachineType.M1_LARGE, descriptor[6].getValue().getType());
 		
 		EasyMock.verify(configurable);
 	}
 	
 	/**
 	 * Test method for {@link provisioning.DPS#calculateUtility()}.
 	 */
 	@Test
 	public void testCalculateUtilityWithUsersAndProviders() {
 		DynamicProvisioningSystem dps = new DynamicProvisioningSystem();
 		UtilityResult result = dps.calculateUtility();
 		
 		UtilityResult currentResult = new UtilityResult(2, 3);
 		for(User user : Checkpointer.loadUsers()){
 			user.calculateOneTimeFees(currentResult);
 		}
 		for(Provider provider : Checkpointer.loadProviders()){
 			provider.calculateUniqueCost(currentResult);
 		}
 		
 		assertNotNull(result);
 		assertEquals(currentResult.getUtility(), result.getUtility(), 0.0);
 	}
 }
