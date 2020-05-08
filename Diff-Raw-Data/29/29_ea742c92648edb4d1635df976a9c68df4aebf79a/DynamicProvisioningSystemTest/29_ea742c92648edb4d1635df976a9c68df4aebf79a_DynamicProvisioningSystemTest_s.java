 /**
  * 
  */
 package provisioning;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import commons.cloud.Contract;
 import commons.cloud.MachineType;
 import commons.cloud.Provider;
 import commons.cloud.Request;
 import commons.cloud.User;
 import commons.cloud.UtilityResult;
 import commons.cloud.UtilityResultEntry;
 import commons.config.Configuration;
 import commons.config.PropertiesTesting;
 import commons.io.GEISTWorkloadParser;
 import commons.io.WorkloadParser;
 import commons.sim.AccountingSystem;
 import commons.sim.components.MachineDescriptor;
 import commons.sim.util.SaaSAppProperties;
 
 /**
  * @author Ricardo Ara&uacute;jo Santos - ricardo@lsd.ufcg.edu.br
  *
  */
 @RunWith(PowerMockRunner.class)
 public class DynamicProvisioningSystemTest {
 	
 	@Before
 	public void setUp() throws ConfigurationException{
 		Configuration.buildInstance(PropertiesTesting.VALID_FILE);
 	}
 
 	/**
 	 * Test method for {@link provisioning.DynamicProvisioningSystem#DynamicProvisioningSystem()}.
 	 */
 	@Test
 	public void testDynamicProvisioningSystemWithValidConfigurationData() {
 		assertNotNull(new DynamicProvisioningSystem());
 	}
 	
 	/**
 	 * Test method for {@link provisioning.DynamicProvisioningSystem#DynamicProvisioningSystem()}.
 	 */
 	@Test
 	@PrepareForTest(Configuration.class)
 	public void testDynamicProvisioningSystemWithEmptyUsersAndProviders() {
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
 		EasyMock.expect(config.getProviders()).andReturn(new ArrayList<Provider>());
 		EasyMock.expect(config.getUsers()).andReturn(new ArrayList<User>());
 		PowerMock.replayAll(config);
 		
 		assertNotNull(new DynamicProvisioningSystem());
 		
 		PowerMock.verifyAll();
 	}
 	
 	/**
 	 * Test method for {@link provisioning.DynamicProvisioningSystem#DynamicProvisioningSystem()}.
 	 */
 	@Test(expected=NullPointerException.class)
 	@PrepareForTest(Configuration.class)
 	public void testDynamicProvisioningSystemWithNullProviders() {
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
 		EasyMock.expect(config.getProviders()).andReturn(null);
 		EasyMock.expect(config.getUsers()).andReturn(new ArrayList<User>());
 		PowerMock.replayAll(config);
 		
 		new DynamicProvisioningSystem();
 		
 		PowerMock.verifyAll();
 	}
 	
 
 	/**
 	 * Test method for {@link provisioning.DynamicProvisioningSystem#DynamicProvisioningSystem()}.
 	 */
 	@Test(expected=NullPointerException.class)
 	@PrepareForTest(Configuration.class)
 	public void testDynamicProvisioningSystemWithNullUsers() {
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
 		EasyMock.expect(config.getProviders()).andReturn(new ArrayList<Provider>());
 		EasyMock.expect(config.getUsers()).andReturn(null);
 		PowerMock.replayAll(config);
 		
 		new DynamicProvisioningSystem();
 		
 		PowerMock.verifyAll();
 	}
 
 	/**
 	 * Test method for {@link provisioning.DynamicProvisioningSystem#registerConfigurable(provisioning.DynamicConfigurable)}.
 	 */
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testRegisterConfigurableWithMultipleServers() {
 		Configuration.getInstance().setProperty(SaaSAppProperties.APPLICATION_INITIAL_SERVER_PER_TIER, "7");
 		
 		DynamicConfigurable configurable = EasyMock.createStrictMock(DynamicConfigurable.class);
 		configurable.addServer(0, new MachineDescriptor(0, true, MachineType.MEDIUM), false);
 		configurable.addServer(0, new MachineDescriptor(1, true, MachineType.MEDIUM), false);
 		configurable.addServer(0, new MachineDescriptor(2, true, MachineType.MEDIUM), false);
 		configurable.addServer(0, new MachineDescriptor(3, true, MachineType.MEDIUM), false);
 		configurable.addServer(0, new MachineDescriptor(4, true, MachineType.LARGE), false);
 		configurable.addServer(0, new MachineDescriptor(5, true, MachineType.LARGE), false);
 		configurable.addServer(0, new MachineDescriptor(6, true, MachineType.LARGE), false);
 		configurable.setWorkloadParser(EasyMock.anyObject(WorkloadParser.class));
 		
 		EasyMock.replay(configurable);
 		
 		DynamicProvisioningSystem dps = new DynamicProvisioningSystem();
 		dps.registerConfigurable(configurable);
 		
 		EasyMock.verify(configurable);
 	}
 	
 	/**
 	 * Test method for {@link provisioning.DynamicProvisioningSystem#registerConfigurable(provisioning.DynamicConfigurable)}.
 	 * @throws Exception 
 	 */
 	@Test
 	@PrepareForTest(DynamicProvisioningSystem.class)
 	public void testRegisterConfigurableWithAnyServers() throws Exception {
 		GEISTWorkloadParser parser = PowerMock.createStrictMockAndExpectNew(GEISTWorkloadParser.class, "src/test/resources/power.trc");
 		
 		Configuration.getInstance().setProperty(SaaSAppProperties.APPLICATION_INITIAL_SERVER_PER_TIER, "0");
 		
 		DynamicConfigurable configurable = EasyMock.createStrictMock(DynamicConfigurable.class);
 		configurable.setWorkloadParser(EasyMock.isA(WorkloadParser.class));
 		
 		PowerMock.replayAll(configurable, parser);
 		
 		DynamicProvisioningSystem dps = new DynamicProvisioningSystem();
 		dps.registerConfigurable(configurable);
 		
 		PowerMock.verifyAll();
 	}
 	
 	/**
 	 * Test method for {@link provisioning.DynamicProvisioningSystem#calculateUtility()}.
 	 */
 	@Test
 	@PrepareForTest(Configuration.class)
 	public void testCalculateUtilityWithoutUsersAndProviders() {
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
 		EasyMock.expect(config.getProviders()).andReturn(new ArrayList<Provider>());
 		EasyMock.expect(config.getUsers()).andReturn(new ArrayList<User>());
 		PowerMock.replayAll(config);
 
 		DynamicProvisioningSystem dps = new DynamicProvisioningSystem();
 		UtilityResult result = dps.calculateUtility();
 		
 		assertNotNull(result);
 		assertEquals(0.0, result.getUtility(), 0.0);
 	}
 	
 	/**
 	 * Test method for {@link provisioning.DynamicProvisioningSystem#calculateUtility()}.
 	 */
 	@Test
 	public void testCalculateUtilityWithUsersAndProviders() {
 		DynamicProvisioningSystem dps = new DynamicProvisioningSystem();
 		UtilityResult result = dps.calculateUtility();
 		
 		UtilityResult currentResult = new UtilityResult();
 		for(User user : Configuration.getInstance().getUsers()){
 			user.calculateOneTimeFees(currentResult);
 		}
 		for(Provider provider : Configuration.getInstance().getProviders()){
 			provider.calculateUniqueCost(currentResult);
 		}
 		
 		assertNotNull(result);
 		assertEquals(currentResult.getUtility(), result.getUtility(), 0.0);
 	}
 	
 	@Test
 	@PrepareForTest(AccountingSystem.class)
 	public void testChargeUsers() throws Exception{
 		List<Provider> providers = Configuration.getInstance().getProviders();
 		List<User> users = Configuration.getInstance().getUsers();
 
 		Map<Integer, User> usersMap = new TreeMap<Integer, User>();
 		for (User user : users) {
 			usersMap.put(user.getId(), user);
 		}
 		
 		Map<String, Provider> providersMap = new TreeMap<String, Provider>();
 		for (Provider provider : providers) {
 			providersMap.put(provider.getName(), provider);
 		}
 		
 		UtilityResultEntry entry = PowerMock.createStrictMockAndExpectNew(UtilityResultEntry.class, 0L, usersMap, providersMap);
 		entry.addToReceipt(EasyMock.anyInt(), EasyMock.anyObject(String.class), EasyMock.anyLong(), 
 				EasyMock.anyDouble(), EasyMock.anyLong(), EasyMock.anyDouble(), EasyMock.anyDouble());
 		PowerMock.expectLastCall().times(2);
 		//AMAZON
 		entry.addUsageToCost(EasyMock.anyObject(String.class), EasyMock.anyObject(MachineType.class), 
 				EasyMock.anyLong(), EasyMock.anyDouble(), EasyMock.anyLong(), EasyMock.anyDouble(), EasyMock.anyDouble());
 		PowerMock.expectLastCall().times(3);
 		entry.addTransferenceToCost(EasyMock.anyObject(String.class), EasyMock.anyLong(), EasyMock.anyDouble(), EasyMock.anyLong(), EasyMock.anyDouble());
 		//GOGRID
 		entry.addUsageToCost(EasyMock.anyObject(String.class), EasyMock.anyObject(MachineType.class), 
 				EasyMock.anyLong(), EasyMock.anyDouble(), EasyMock.anyLong(), EasyMock.anyDouble(), EasyMock.anyDouble());
 		PowerMock.expectLastCall().times(2);
 		entry.addTransferenceToCost(EasyMock.anyObject(String.class), EasyMock.anyLong(), EasyMock.anyDouble(), EasyMock.anyLong(), EasyMock.anyDouble());
 		//RACKSPACE
 		entry.addUsageToCost(EasyMock.anyObject(String.class), EasyMock.anyObject(MachineType.class), 
 				EasyMock.anyLong(), EasyMock.anyDouble(), EasyMock.anyLong(), EasyMock.anyDouble(), EasyMock.anyDouble());
 		PowerMock.expectLastCall().times(2);
 		entry.addTransferenceToCost(EasyMock.anyObject(String.class), EasyMock.anyLong(), EasyMock.anyDouble(), EasyMock.anyLong(), EasyMock.anyDouble());
 		PowerMock.replayAll();
 		
 		DynamicProvisioningSystem dps = new DynamicProvisioningSystem();
 		dps.chargeUsers(0);
 
 		PowerMock.verifyAll();
 	}
 	
 	@Test
 	@PrepareForTest(Configuration.class)
 	public void testReportLostRequest(){
 		Request request = EasyMock.createStrictMock(Request.class);
		EasyMock.expect(request.getUserID()).andReturn("0");
 		
 		Contract contract = EasyMock.createStrictMock(Contract.class);
 
 		//Configuration mock
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
 		EasyMock.expect(config.getProviders()).andReturn(new ArrayList<Provider>());
 		
 		User user = EasyMock.createStrictMock(User.class);
 		EasyMock.expect(user.getId()).andReturn(0);
 		user.reportLostRequest(request);
 		ArrayList<User> users = new ArrayList<User>();
 		users.add(user);
 		EasyMock.expect(config.getUsers()).andReturn(users);
 		
 		PowerMock.replayAll(request, contract, config, user);
 		
 		//Creating dps
 		DynamicProvisioningSystem dps = new DynamicProvisioningSystem();
 		dps.reportLostRequest(request);
 		
 		PowerMock.verifyAll();
 	}
 	
 	@Test
 	@PrepareForTest(Configuration.class)
 	public void testReportFinishedRequest(){
 		Request request = EasyMock.createStrictMock(Request.class);
		EasyMock.expect(request.getUserID()).andReturn("0");
 		
 		Contract contract = EasyMock.createStrictMock(Contract.class);
 
 		//Configuration mock
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
 		EasyMock.expect(config.getProviders()).andReturn(new ArrayList<Provider>());
 		
 		User user = EasyMock.createStrictMock(User.class);
 		EasyMock.expect(user.getId()).andReturn(0);
 		user.reportFinishedRequest(request);
 		ArrayList<User> users = new ArrayList<User>();
 		users.add(user);
 		EasyMock.expect(config.getUsers()).andReturn(users);
 		
 		PowerMock.replayAll(request, contract, config, user);
 		
 		//Creating dps
 		DynamicProvisioningSystem dps = new DynamicProvisioningSystem();
 		dps.reportRequestFinished(request);
 		
 		PowerMock.verifyAll();
 	}
 }
