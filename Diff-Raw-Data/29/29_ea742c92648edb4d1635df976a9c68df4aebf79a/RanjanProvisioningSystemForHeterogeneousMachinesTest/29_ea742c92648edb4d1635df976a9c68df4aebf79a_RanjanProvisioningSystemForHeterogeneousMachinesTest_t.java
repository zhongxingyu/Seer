 package provisioning;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import commons.cloud.Contract;
 import commons.cloud.MachineType;
 import commons.cloud.Provider;
 import commons.cloud.Request;
 import commons.cloud.TypeProvider;
 import commons.cloud.User;
 import commons.config.Configuration;
 import commons.config.PropertiesTesting;
 import commons.io.GEISTWorkloadParser;
 import commons.io.WorkloadParser;
 import commons.sim.SimpleSimulator;
 import commons.sim.components.MachineDescriptor;
 import commons.sim.jeevent.JEEventScheduler;
 import commons.sim.jeevent.JETime;
 import commons.sim.provisioningheuristics.RanjanStatistics;
 import commons.sim.util.SaaSAppProperties;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(Configuration.class)
 public class RanjanProvisioningSystemForHeterogeneousMachinesTest {
 
 	private RanjanProvisioningSystemForHeterogeneousMachines dps;
 
 	@Test(expected=NullPointerException.class)
 	public void testWithEmptyStatistics(){
 		//Mocks
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
 		
 		EasyMock.expect(config.getProviders()).andReturn(new ArrayList<Provider>());
 		EasyMock.expect(config.getUsers()).andReturn(new ArrayList<User>());
 		
 		PowerMock.replay(Configuration.class);
 		EasyMock.replay(config);
 		
 		this.dps = new RanjanProvisioningSystemForHeterogeneousMachines();
 		this.dps.evaluateNumberOfServersForNextInterval(null);
 		
 		PowerMock.verify(Configuration.class);
 		EasyMock.verify(config);
 	}
 	
 	/**
 	 * This scenario verifies that an utilization that is not so much greater than the target
 	 * utilization (0.66) indicates that a small number of servers should be added
 	 */
 	@Test
 	public void testEvaluateNumberOfServersWithASingleServer(){
 		//Creating simulated statistics
 		double averageUtilisation = 0.7;
 		long totalRequestsArrivals = 10;
 		long totalRequestsCompletions = 10;
 		long totalNumberOfServers = 1;
 		RanjanStatistics statistics = new RanjanStatistics(averageUtilisation, totalRequestsArrivals, totalRequestsCompletions, totalNumberOfServers);
 		
 		//Mocks
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
 		
 		EasyMock.expect(config.getProviders()).andReturn(new ArrayList<Provider>());
 		EasyMock.expect(config.getUsers()).andReturn(new ArrayList<User>());
 		
 		PowerMock.replay(Configuration.class);
 		EasyMock.replay(config);
 		
 		this.dps = new RanjanProvisioningSystemForHeterogeneousMachines();
 		assertEquals(1, this.dps.evaluateNumberOfServersForNextInterval(statistics));
 		
 		PowerMock.verify(Configuration.class);
 		EasyMock.verify(config);
 	}
 	
 	/**
 	 * This scenario verifies that an utilization that is so much greater than the target
 	 * utilization (0.66), and so the number of completions is small, indicates that a 
 	 * large number of servers should be added
 	 */
 	@Test
 	public void testEvaluateNumberOfServersWithMultipleServers(){
 		//Creating simulated statistics
 		double averageUtilisation = 1.0;
 		long totalRequestsArrivals = 55;
 		long totalRequestsCompletions = 15;
 		long totalNumberOfServers = 3;
 		RanjanStatistics statistics = new RanjanStatistics(averageUtilisation, totalRequestsArrivals, totalRequestsCompletions, totalNumberOfServers);
 		
 		//Mocks
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
 		
 		EasyMock.expect(config.getProviders()).andReturn(new ArrayList<Provider>());
 		EasyMock.expect(config.getUsers()).andReturn(new ArrayList<User>());
 		
 		PowerMock.replay(Configuration.class);
 		EasyMock.replay(config);
 		
 		this.dps = new RanjanProvisioningSystemForHeterogeneousMachines();
 		assertEquals(14, this.dps.evaluateNumberOfServersForNextInterval(statistics));
 		
 		PowerMock.verify(Configuration.class);
 		EasyMock.verify(config);
 	}
 	
 	/**
 	 * This scenario verifies that an utilization that is lower than the target
 	 * utilization (0.66), and so the number of completions is high, indicates that a 
 	 * large number of servers should be removed
 	 */
 	@Test
 	public void testEvaluateNumberOfServersConsideringMultipleServersWithLowUtilization(){
 		//Creating simulated statistics
 		double averageUtilisation = 0.3;
 		long totalRequestsArrivals = 333;
 		long totalRequestsCompletions = 279;
 		long totalNumberOfServers = 20;
 		RanjanStatistics statistics = new RanjanStatistics(averageUtilisation, totalRequestsArrivals, totalRequestsCompletions, totalNumberOfServers);
 		
 		//Mocks
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
 		
 		EasyMock.expect(config.getProviders()).andReturn(new ArrayList<Provider>());
 		EasyMock.expect(config.getUsers()).andReturn(new ArrayList<User>());
 		
 		PowerMock.replay(Configuration.class);
 		EasyMock.replay(config);
 		
 		this.dps = new RanjanProvisioningSystemForHeterogeneousMachines();
 		assertEquals(-9, this.dps.evaluateNumberOfServersForNextInterval(statistics));
 		
 		PowerMock.verify(Configuration.class);
 		EasyMock.verify(config);
 	}
 	
 	/**
 	 * This scenario verifies that an utilization that is so much lower than the target
 	 * utilization (0.66), and so the number of completions is high, indicates that a 
 	 * minimal amount of servers should remain in the infrastructure
 	 */
 	@Test
 	public void testEvaluateNumberOfServersConsideringMultipleServersWithLowUtilization2(){
 		//Creating simulated statistics
 		double averageUtilisation = 0.01;
 		long totalRequestsArrivals = 100;
 		long totalRequestsCompletions = 100;
 		long totalNumberOfServers = 20;
 		RanjanStatistics statistics = new RanjanStatistics(averageUtilisation, totalRequestsArrivals, totalRequestsCompletions, totalNumberOfServers);
 		
 		//Mocks
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
 		
 		EasyMock.expect(config.getProviders()).andReturn(new ArrayList<Provider>());
 		EasyMock.expect(config.getUsers()).andReturn(new ArrayList<User>());
 		
 		PowerMock.replay(Configuration.class);
 		EasyMock.replay(config);
 		
 		this.dps = new RanjanProvisioningSystemForHeterogeneousMachines();
 		assertEquals(-19, this.dps.evaluateNumberOfServersForNextInterval(statistics));
 		
 		PowerMock.verify(Configuration.class);
 		EasyMock.verify(config);
 	}
 	
 	/**
 	 * This scenario verifies a scenario where no demand has occurred
 	 */
 	@Test
 	public void testEvaluateNumberOfServersConsideringMultipleServersWithLowUtilization3(){
 		//Creating simulated statistics
 		double averageUtilisation = 0.0;
 		long totalRequestsArrivals = 0;
 		long totalRequestsCompletions = 0;
 		long totalNumberOfServers = 20;
 		RanjanStatistics statistics = new RanjanStatistics(averageUtilisation, totalRequestsArrivals, totalRequestsCompletions, totalNumberOfServers);
 		
 		//Mocks
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
 		
 		EasyMock.expect(config.getProviders()).andReturn(new ArrayList<Provider>());
 		EasyMock.expect(config.getUsers()).andReturn(new ArrayList<User>());
 		
 		PowerMock.replay(Configuration.class);
 		EasyMock.replay(config);
 		
 		this.dps = new RanjanProvisioningSystemForHeterogeneousMachines();
 		
 		assertEquals(-20, this.dps.evaluateNumberOfServersForNextInterval(statistics));
 		
 		PowerMock.verify(Configuration.class);
 		EasyMock.verify(config);
 	}
 	
 	/**
 	 * This scenario verifies a scenario where no machines were available and a high demand
 	 * has arrived
 	 */
 	@Test
 	public void testEvaluateNumberOfServersWithoutPreviousMachines(){
 		//Creating simulated statistics
 		double averageUtilisation = 0.0;
 		long totalRequestsArrivals = 100;
 		long totalRequestsCompletions = 0;
 		long totalNumberOfServers = 0;
 		RanjanStatistics statistics = new RanjanStatistics(averageUtilisation, totalRequestsArrivals, totalRequestsCompletions, totalNumberOfServers);
 		
 		//Mocks
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(2);
 		
 		EasyMock.expect(config.getProviders()).andReturn(new ArrayList<Provider>());
 		EasyMock.expect(config.getUsers()).andReturn(new ArrayList<User>());
 		
 		PowerMock.replay(Configuration.class);
 		EasyMock.replay(config);
 		
 		this.dps = new RanjanProvisioningSystemForHeterogeneousMachines();
 		
 		assertEquals(1, this.dps.evaluateNumberOfServersForNextInterval(statistics));
 		
 		PowerMock.verify(Configuration.class);
 		EasyMock.verify(config);
 	}
 	
 	/**
 	 * This scenarios verifies that after evaluating that a machine should be added, the RANJAN provisioning
 	 * system creates a machine and adds it to simulator.
 	 * @throws Exception 
 	 */
 	@Test
 	@PrepareForTest({Configuration.class, DynamicProvisioningSystem.class})
 	public void testEvaluateUtilisationWithOneServerToBeAdded() throws Exception{
 		int reservationLimit = 1;
 		int onDemandLimit = 1;
 
 		double averageUtilisation = 0.0;
 		long totalRequestsArrivals = 100;
 		long totalRequestsCompletions = 0;
 		long totalNumberOfServers = 0;
 		RanjanStatistics statistics = new RanjanStatistics(averageUtilisation, totalRequestsArrivals, totalRequestsCompletions, totalNumberOfServers);
 		
 		JEEventScheduler scheduler = EasyMock.createStrictMock(JEEventScheduler.class);
 		EasyMock.expect(scheduler.now()).andReturn(new JETime(0));
 		EasyMock.replay(scheduler);
 		
 		SimpleSimulator configurable = EasyMock.createMock(SimpleSimulator.class);
 		configurable.addServer(0, new MachineDescriptor(0, true, MachineType.MEDIUM), true);
 		configurable.setWorkloadParser(EasyMock.isA(WorkloadParser.class));
 		
 		EasyMock.replay(configurable);
 		
 		//Mocks
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(5);
 		
 		ArrayList<Provider> providers = new ArrayList<Provider>();
 		ArrayList<TypeProvider> types = new ArrayList<TypeProvider>();
 		types.add(new TypeProvider(MachineType.MEDIUM, 0.1, 0.05, 100, 180, reservationLimit));//Machine available
 		
 		Provider provider = new Provider("1", onDemandLimit, reservationLimit, 0.15, new long[]{}, new double[]{}, new long[]{}, new double[]{}, types);
 		providers.add(provider);
 		EasyMock.expect(config.getProviders()).andReturn(providers);
 		EasyMock.expect(config.getUsers()).andReturn(new ArrayList<User>());
 		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_INITIAL_SERVER_PER_TIER)).andReturn(new int[]{0});
 		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.VALID_WORKLOAD});
 		EasyMock.expect(config.getRelativePower(MachineType.MEDIUM)).andReturn(1d).times(2);
 		
 		GEISTWorkloadParser parser = PowerMock.createStrictMockAndExpectNew(GEISTWorkloadParser.class, PropertiesTesting.VALID_WORKLOAD);
 		
 		PowerMock.replayAll(config, parser);
 		
 		this.dps = new RanjanProvisioningSystemForHeterogeneousMachines();
 		this.dps.registerConfigurable(configurable);
 		this.dps.evaluateUtilisation(0, statistics, 0);
 		
 		PowerMock.verifyAll();
 		
 		assertFalse(provider.canBuyMachine(true, MachineType.MEDIUM));
 	}
 	
 	/**
 	 * This scenario verifies that an utilization that is so much greater than the target
 	 * utilization (0.66), and so the number of completions is small, indicates that a 
 	 * large number of servers should be added. Different machine types exist in the provider in
 	 * order to supply the demand.
 	 * @throws Exception 
 	 */
 	@Test
 	@PrepareForTest({Configuration.class, DynamicProvisioningSystem.class})
 	public void testEvaluateUtilisationWithOnDemandAndReservedServersToBeAdded() throws Exception{
 		int reservationLimit = 5;
 		int onDemandLimit = 10;
 		
 		double averageUtilisation = 1.0;
 		long totalRequestsArrivals = 55;
 		long totalRequestsCompletions = 15;
 		long totalNumberOfServers = 3;
 
 		RanjanStatistics statistics = new RanjanStatistics(averageUtilisation, totalRequestsArrivals, totalRequestsCompletions, totalNumberOfServers);
 		
 		JEEventScheduler scheduler = EasyMock.createStrictMock(JEEventScheduler.class);
 		EasyMock.replay(scheduler);
 		
 		SimpleSimulator configurable = EasyMock.createMock(SimpleSimulator.class);
 		configurable.setWorkloadParser(EasyMock.isA(WorkloadParser.class));
 		configurable.addServer(0, new MachineDescriptor(0, true, MachineType.MEDIUM), true);
 		configurable.addServer(0, new MachineDescriptor(1, true, MachineType.MEDIUM), true);
 		configurable.addServer(0, new MachineDescriptor(2, true, MachineType.MEDIUM), true);
 		configurable.addServer(0, new MachineDescriptor(3, true, MachineType.MEDIUM), true);
 		configurable.addServer(0, new MachineDescriptor(4, true, MachineType.MEDIUM), true);
 		configurable.addServer(0, new MachineDescriptor(5, false, MachineType.SMALL), true);
 		configurable.addServer(0, new MachineDescriptor(6, false, MachineType.SMALL), true);
 		configurable.addServer(0, new MachineDescriptor(7, false, MachineType.SMALL), true);
 		configurable.addServer(0, new MachineDescriptor(8, false, MachineType.SMALL), true);
 		EasyMock.replay(configurable);
 		
 		//Configuration mock
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(5);
 		
 		ArrayList<Provider> providers = new ArrayList<Provider>();
 		ArrayList<TypeProvider> types = new ArrayList<TypeProvider>();
 		types.add(new TypeProvider(MachineType.MEDIUM, 0.1, 0.05, 100, 180, reservationLimit));//Reserved machines available
 		types.add(new TypeProvider(MachineType.SMALL, 0.1, 0.05, 100, 180, 0));//On-demand machines available
 		
 		Provider provider = new Provider("1", onDemandLimit, 10, 0.15, new long[]{}, new double[]{}, new long[]{}, new double[]{}, types);
 		providers.add(provider);
 		EasyMock.expect(config.getProviders()).andReturn(providers);
 		EasyMock.expect(config.getUsers()).andReturn(new ArrayList<User>());
 		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_INITIAL_SERVER_PER_TIER)).andReturn(new int[]{0});
 		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.VALID_WORKLOAD});
 		EasyMock.expect(config.getRelativePower(MachineType.MEDIUM)).andReturn(2d).times(12);
 		EasyMock.expect(config.getRelativePower(MachineType.SMALL)).andReturn(1d).times(10);
 		
 		PowerMock.replay(Configuration.class);
 		EasyMock.replay(config);
 		
 		GEISTWorkloadParser parser = PowerMock.createStrictMockAndExpectNew(GEISTWorkloadParser.class, PropertiesTesting.VALID_WORKLOAD);
 		PowerMock.replay(GEISTWorkloadParser.class);
 		PowerMock.replay(parser);
 		
 		this.dps = new RanjanProvisioningSystemForHeterogeneousMachines();
 		this.dps.registerConfigurable(configurable);
 
 		this.dps.evaluateUtilisation(0, statistics, 0);
 		
 		PowerMock.verifyAll();
 	}
 	
 	/**
 	 * This scenario verifies that an utilization that is so much greater than the target
 	 * utilization (0.66), and so the number of completions is small, indicates that a 
 	 * large number of servers should be added. The reserved machine type supplies all the demand
 	 * with a lower number of machines than the number of machines purchased at 
 	 * {@link provisioning.RanjanProvisioningSystemForHeterogeneousMachinesTest#testEvaluateUtilisationWithOnDemandAndReservedServersToBeAdded()}
 	 * @throws Exception 
 	 */
 	@Test
 	@PrepareForTest({Configuration.class, DynamicProvisioningSystem.class})
 	public void testEvaluateUtilisationWithOnlyReservedServersToBeAdded() throws Exception{
 		int reservationLimit = 5;
 		int onDemandLimit = 10;
 		
 		double averageUtilisation = 1.0;
 		long totalRequestsArrivals = 55;
 		long totalRequestsCompletions = 15;
 		long totalNumberOfServers = 3;
 
 		RanjanStatistics statistics = new RanjanStatistics(averageUtilisation, totalRequestsArrivals, totalRequestsCompletions, totalNumberOfServers);
 		
 		JEEventScheduler scheduler = EasyMock.createStrictMock(JEEventScheduler.class);
 		EasyMock.replay(scheduler);
 		
 		SimpleSimulator configurable = EasyMock.createMock(SimpleSimulator.class);
 		configurable.setWorkloadParser(EasyMock.isA(WorkloadParser.class));
 		configurable.addServer(0, new MachineDescriptor(0, true, MachineType.MEDIUM), true);
 		configurable.addServer(0, new MachineDescriptor(1, true, MachineType.MEDIUM), true);
 		configurable.addServer(0, new MachineDescriptor(2, true, MachineType.MEDIUM), true);
 		configurable.addServer(0, new MachineDescriptor(3, true, MachineType.MEDIUM), true);
 		EasyMock.replay(configurable);
 		
 		//Configuration mock
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(5);
 		
 		ArrayList<Provider> providers = new ArrayList<Provider>();
 		ArrayList<TypeProvider> types = new ArrayList<TypeProvider>();
 		types.add(new TypeProvider(MachineType.MEDIUM, 0.1, 0.05, 100, 180, reservationLimit));//Reserved machines available
 		
 		Provider provider = new Provider("1", onDemandLimit, 10, 0.15, new long[]{}, new double[]{}, new long[]{}, new double[]{}, types);
 		providers.add(provider);
 		EasyMock.expect(config.getProviders()).andReturn(providers);
 		EasyMock.expect(config.getUsers()).andReturn(new ArrayList<User>());
 		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_INITIAL_SERVER_PER_TIER)).andReturn(new int[]{0});
 		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.VALID_WORKLOAD});
 		EasyMock.expect(config.getRelativePower(MachineType.MEDIUM)).andReturn(4d).times(8);
 		
 		PowerMock.replay(Configuration.class);
 		EasyMock.replay(config);
 		
 		GEISTWorkloadParser parser = PowerMock.createStrictMockAndExpectNew(GEISTWorkloadParser.class, PropertiesTesting.VALID_WORKLOAD);
 		PowerMock.replay(GEISTWorkloadParser.class);
 		PowerMock.replay(parser);
 		
 		this.dps = new RanjanProvisioningSystemForHeterogeneousMachines();
 		this.dps.registerConfigurable(configurable);
 
 		this.dps.evaluateUtilisation(0, statistics, 0);
 		
 		PowerMock.verifyAll();
 	}
 	
 	/**
 	 * This scenarios verifies that after evaluating that nineteen machines should be removed, some calls
 	 * to simulator are performed.
 	 * @throws Exception 
 	 */
 	@Test
 	@PrepareForTest({Configuration.class, DynamicProvisioningSystem.class})
 	public void handleEvaluateUtilisationWithServersToRemove() throws Exception{
 		int reservationLimit = 3;
 		int onDemandLimit = 10;
 
 		double totalUtilization = 0.2;
 		long totalRequestsArrivals = 100;
 		long totalRequestsCompletions = 100;
 		long totalNumberOfServers = 20;
 		RanjanStatistics statistics = new RanjanStatistics(totalUtilization, totalRequestsArrivals, totalRequestsCompletions, totalNumberOfServers);
 		
 		SimpleSimulator configurable = EasyMock.createMock(SimpleSimulator.class);
 		configurable.setWorkloadParser(EasyMock.isA(WorkloadParser.class));
 		configurable.removeServer(0, false);
 		EasyMock.expectLastCall().times(19);
 		
 		EasyMock.replay(configurable);
 		
 		//Configuration mock
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(4);
 		
 		ArrayList<Provider> providers = new ArrayList<Provider>();
 		ArrayList<TypeProvider> types = new ArrayList<TypeProvider>();
 		types.add(new TypeProvider(MachineType.MEDIUM, 0.1, 0.05, 100, 180, 0));//Reserved machines available
 		types.add(new TypeProvider(MachineType.SMALL, 0.1, 0.05, 100, 180, 0));//On-demand machines available
 		
 		Provider provider = new Provider("1", onDemandLimit, reservationLimit, 0.15, new long[]{}, new double[]{}, new long[]{}, new double[]{}, types);
 		providers.add(provider);
 		EasyMock.expect(config.getProviders()).andReturn(providers);
 		EasyMock.expect(config.getUsers()).andReturn(new ArrayList<User>());
 		EasyMock.expect(config.getIntegerArray(SaaSAppProperties.APPLICATION_INITIAL_SERVER_PER_TIER)).andReturn(new int[]{0});
 		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.VALID_WORKLOAD});
 		
 		PowerMock.replay(Configuration.class);
 		EasyMock.replay(config);
 		
 		GEISTWorkloadParser parser = PowerMock.createStrictMockAndExpectNew(GEISTWorkloadParser.class, PropertiesTesting.VALID_WORKLOAD);
 		PowerMock.replay(GEISTWorkloadParser.class);
 		PowerMock.replay(parser);
 		
 		//Creating dps
 		this.dps = new RanjanProvisioningSystemForHeterogeneousMachines();
 		this.dps.registerConfigurable(configurable);
 		
 		//Buying some on-demand machines
 		this.dps.buyMachine(provider, MachineType.SMALL, false);
 		this.dps.buyMachine(provider, MachineType.SMALL, false);
 		this.dps.buyMachine(provider, MachineType.SMALL, false);
 		this.dps.buyMachine(provider, MachineType.SMALL, false);
 		this.dps.buyMachine(provider, MachineType.SMALL, false);
 		this.dps.buyMachine(provider, MachineType.SMALL, false);
 		this.dps.buyMachine(provider, MachineType.MEDIUM, false);
 		this.dps.buyMachine(provider, MachineType.MEDIUM, false);
 		this.dps.buyMachine(provider, MachineType.MEDIUM, false);
 		this.dps.buyMachine(provider, MachineType.MEDIUM, false);
 		
 		this.dps.evaluateUtilisation(0, statistics, 0);
 		
 		PowerMock.verifyAll();
 	}
 	
 	@Test
 	public void testReportQueuedRequest(){
 		Request request = EasyMock.createStrictMock(Request.class);
		EasyMock.expect(request.getSaasClient()).andReturn("0");
 		
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
 		this.dps = new RanjanProvisioningSystemForHeterogeneousMachines();
 		this.dps.requestQueued(0, request, 0);
 		
 		PowerMock.verifyAll();
 	}
 	
 	@Test
 	public void testReportFinishedRequest(){
 		Request request = EasyMock.createStrictMock(Request.class);
		EasyMock.expect(request.getSaasClient()).andReturn("0");
 		
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
 		RanjanProvisioningSystemForHeterogeneousMachines dps = new RanjanProvisioningSystemForHeterogeneousMachines();
 		dps.reportRequestFinished(request);
 		
 		PowerMock.verifyAll();
 	}
 }
