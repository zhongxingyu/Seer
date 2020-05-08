 /**
  * 
  */
 package commons.cloud;
 
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 
 import commons.config.Configuration;
 import commons.config.PropertiesTesting;
 import commons.sim.components.MachineDescriptor;
 import commons.util.CostCalculus;
 
 /**
  * test class for {@link Provider} 
  * @author Ricardo Ara&uacute;jo Santos - ricardo@lsd.ufcg.edu.br
  *
  */
 public class ProviderTest {
 	
 	private Provider amazon;
 	
 	@Before
 	public void setUp() throws ConfigurationException{
 		Configuration.buildInstance(PropertiesTesting.VALID_FILE);
 		amazon = Configuration.getInstance().getProviders().get(1);
 		assert amazon.getName().equals("amazon"): "Check providers order in iaas.providers file.";
 	}
 
 	/**
 	 * Test method for {@link commons.cloud.Provider#canBuyMachine(boolean, commons.cloud.MachineType)}.
 	 */
 	@Test
 	public void testCanBuyOnDemandMachineWithAvailableMachines() {
 		TypeProvider typeProvider = EasyMock.createStrictMock(TypeProvider.class);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.LARGE);
 		EasyMock.replay(typeProvider);
 		
 		Provider provider = new Provider("amazon", 3, 0, 3.0, new long[]{}, new double[]{}, new long[]{}, new double[]{}, Arrays.asList(typeProvider) );
		assertTrue(provider.canBuyMachine(false, null));
 		
 		EasyMock.verify(typeProvider);
 	}
 
 	/**
 	 * Test method for {@link commons.cloud.Provider#canBuyMachine(boolean, commons.cloud.MachineType)}.
 	 */
 	@Test
 	public void testCanBuyOnDemandMachineWithoutAvailableMachines() {
 		TypeProvider typeProvider = EasyMock.createStrictMock(TypeProvider.class);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.LARGE);
 		EasyMock.replay(typeProvider);
 		
 		Provider provider = new Provider("amazon", 0, 0, 3.0, new long[]{}, new double[]{}, new long[]{}, new double[]{}, Arrays.asList(typeProvider) );
 		assertFalse(provider.canBuyMachine(false, MachineType.LARGE));
 		
 		EasyMock.verify(typeProvider);
 	}
 
 	/**
 	 * Test method for {@link commons.cloud.Provider#canBuyMachine(boolean, commons.cloud.MachineType)}.
 	 */
 	@Test
 	public void testCanBuyReservedMachineNotProvided() {
 		TypeProvider typeProvider = EasyMock.createStrictMock(TypeProvider.class);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.LARGE);
 		EasyMock.replay(typeProvider);
 		
 		Provider provider = new Provider("amazon", 0, 0, 3.0, new long[]{}, new double[]{}, new long[]{}, new double[]{}, Arrays.asList(typeProvider) );
 		assertFalse(provider.canBuyMachine(true, MachineType.SMALL));
 		
 		EasyMock.verify(typeProvider);
 	}
 
 	/**
 	 * Test method for {@link commons.cloud.Provider#canBuyMachine(boolean, commons.cloud.MachineType)}.
 	 */
 	@Test
 	public void testCanBuyReservedMachineProvidedButUnavailable() {
 		TypeProvider typeProvider = EasyMock.createStrictMock(TypeProvider.class);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.LARGE);
 		EasyMock.expect(typeProvider.canBuy()).andReturn(false);
 		EasyMock.replay(typeProvider);
 		
 		Provider provider = new Provider("amazon", 0, 0, 3.0, new long[]{}, new double[]{}, new long[]{}, new double[]{}, Arrays.asList(typeProvider) );
 		assertFalse(provider.canBuyMachine(true, MachineType.LARGE));
 		
 		EasyMock.verify(typeProvider);
 	}
 
 	/**
 	 * Test method for {@link commons.cloud.Provider#buyMachine(boolean, commons.cloud.MachineType)}.
 	 */
 	@Test
 	public void testBuyMachineOnDemandMachineUntilNoMachineIsAvailable() {
 		TypeProvider typeProvider = EasyMock.createStrictMock(TypeProvider.class);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.SMALL);
 		EasyMock.expect(typeProvider.buyMachine(false)).andReturn(new MachineDescriptor(1, false, MachineType.SMALL));
 		EasyMock.expect(typeProvider.buyMachine(false)).andReturn(null);
 		EasyMock.replay(typeProvider);
 		
 		Provider provider = new Provider("amazon", 1, 0, 3.0, new long[]{}, new double[]{}, new long[]{}, new double[]{}, Arrays.asList(typeProvider) );
 		assertTrue(provider.canBuyMachine(false, MachineType.SMALL));
 		MachineDescriptor descriptor = provider.buyMachine(false, MachineType.SMALL);
 		assertNotNull(descriptor);
 		assertFalse(descriptor.isReserved());
 		assertEquals(MachineType.SMALL, descriptor.getType());
 		
 		assertFalse(provider.canBuyMachine(false, MachineType.SMALL));
 		assertNull(provider.buyMachine(false, MachineType.SMALL));
 
 		EasyMock.verify(typeProvider);
 	}
 
 	/**
 	 * Test method for {@link commons.cloud.Provider#buyMachine(boolean, commons.cloud.MachineType)}.
 	 */
 	@Test
 	public void testBuyReservedMachineProvidedAndAvailableUntilIsOver() {
 		TypeProvider typeProvider = EasyMock.createStrictMock(TypeProvider.class);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.LARGE);
 		EasyMock.expect(typeProvider.canBuy()).andReturn(true);
 		EasyMock.expect(typeProvider.buyMachine(true)).andReturn(new MachineDescriptor(1, true, MachineType.LARGE));
 		EasyMock.expect(typeProvider.canBuy()).andReturn(false);
 		EasyMock.expect(typeProvider.buyMachine(true)).andReturn(null);
 		EasyMock.replay(typeProvider);
 		
 		Provider provider = new Provider("amazon", 0, 0, 3.0, new long[]{}, new double[]{}, new long[]{}, new double[]{}, Arrays.asList(typeProvider) );
 		assertTrue(provider.canBuyMachine(true, MachineType.LARGE));
 		MachineDescriptor descriptor = provider.buyMachine(true, MachineType.LARGE);
 		assertNotNull(descriptor);
 		assertTrue(descriptor.isReserved());
 		assertEquals(MachineType.LARGE, descriptor.getType());
 		
 		assertFalse(provider.canBuyMachine(true, MachineType.LARGE));
 		assertNull(provider.buyMachine(true, MachineType.LARGE));
 		
 		EasyMock.verify(typeProvider);
 	}
 
 	/**
 	 * Test method for {@link commons.cloud.Provider#shutdownMachine(commons.sim.components.MachineDescriptor)}.
 	 */
 	@Test
 	public void testShutdownInexistentOnDemandMachine() {
 		MachineDescriptor descriptor = new MachineDescriptor(1, false, MachineType.LARGE);
 
 		TypeProvider typeProvider = EasyMock.createStrictMock(TypeProvider.class);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.LARGE);
 		EasyMock.expect(typeProvider.shutdownMachine(descriptor)).andReturn(false);
 		EasyMock.replay(typeProvider);
 		
 		Provider provider = new Provider("amazon", 3, 0, 3.0, new long[]{}, new double[]{}, new long[]{}, new double[]{}, Arrays.asList(typeProvider) );
 		assertFalse(provider.shutdownMachine(descriptor));
 		
 		EasyMock.verify(typeProvider);
 	}
 
 	/**
 	 * Test method for {@link commons.cloud.Provider#shutdownMachine(commons.sim.components.MachineDescriptor)}.
 	 */
 	@Test
 	public void testShutdownInexistentReservedMachine() {
 		MachineDescriptor descriptor = new MachineDescriptor(1, true, MachineType.LARGE);
 		
 		TypeProvider typeProvider = EasyMock.createStrictMock(TypeProvider.class);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.LARGE);
 		EasyMock.expect(typeProvider.shutdownMachine(descriptor)).andReturn(false);
 		EasyMock.replay(typeProvider);
 		
 		Provider provider = new Provider("amazon", 3, 0, 3.0, new long[]{}, new double[]{}, new long[]{}, new double[]{}, Arrays.asList(typeProvider) );
 		assertFalse(provider.shutdownMachine(descriptor));
 		
 		EasyMock.verify(typeProvider);
 	}
 
 	/**
 	 * Test method for {@link commons.cloud.Provider#shutdownMachine(commons.sim.components.MachineDescriptor)}.
 	 */
 	@Test
 	public void testShutdownOnDemandMachine() {
 		TypeProvider typeProvider = EasyMock.createStrictMock(TypeProvider.class);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.SMALL);
 		EasyMock.expect(typeProvider.buyMachine(false)).andReturn(new MachineDescriptor(1, false, MachineType.SMALL));
 		EasyMock.expect(typeProvider.shutdownMachine(EasyMock.isA(MachineDescriptor.class))).andReturn(true);
 		
 		EasyMock.replay(typeProvider);
 		
 		Provider provider = new Provider("amazon", 1, 0, 3.0, new long[]{}, new double[]{}, new long[]{}, new double[]{}, Arrays.asList(typeProvider) );
 		MachineDescriptor descriptor = provider.buyMachine(false, MachineType.SMALL);
 		assertNotNull(descriptor);
 		assertFalse(provider.canBuyMachine(false, MachineType.SMALL));
 		assertTrue(provider.shutdownMachine(descriptor));
 		assertTrue(provider.canBuyMachine(false, MachineType.SMALL));
 		
 		EasyMock.verify(typeProvider);
 	}
 
 	/**
 	 * Test method for {@link commons.cloud.Provider#shutdownMachine(commons.sim.components.MachineDescriptor)}.
 	 */
 	@Test
 	public void testShutdownReservedMachine() {
 		MachineDescriptor descriptorToSell = new MachineDescriptor(1, true, MachineType.LARGE);
 		
 		TypeProvider typeProvider = EasyMock.createStrictMock(TypeProvider.class);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.LARGE);
 		EasyMock.expect(typeProvider.buyMachine(true)).andReturn(descriptorToSell);
 		EasyMock.expect(typeProvider.canBuy()).andReturn(false);
 		EasyMock.expect(typeProvider.shutdownMachine(descriptorToSell)).andReturn(true);
 		EasyMock.expect(typeProvider.canBuy()).andReturn(true);
 		EasyMock.replay(typeProvider);
 		
 		Provider provider = new Provider("amazon", 1, 0, 3.0, new long[]{}, new double[]{}, new long[]{}, new double[]{}, Arrays.asList(typeProvider) );
 		MachineDescriptor descriptor = provider.buyMachine(true, MachineType.LARGE);
 		assertNotNull(descriptor);
 		assertFalse(provider.canBuyMachine(true, MachineType.LARGE));
 		assertTrue(provider.shutdownMachine(descriptor));
 		assertTrue(provider.canBuyMachine(true, MachineType.LARGE));
 		
 		EasyMock.verify(typeProvider);
 	}
 
 	/**
 	 * Test method for {@link commons.cloud.Provider#calculateCost(UtilityResultEntry, long)}.
 	 */
 	@Test
 	public void testCalculateCostWithNoTransference() {
 		UtilityResultEntry entry = EasyMock.createStrictMock(UtilityResultEntry.class);
 		entry.addProvider("amazon");
 		entry.addTransferenceToCost(0, 0, 0, 0);
 
 		TypeProvider typeProvider = EasyMock.createStrictMock(TypeProvider.class);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.SMALL);
 		EasyMock.expect(typeProvider.getTotalTransferences()).andReturn(new long[]{0, 0});
 		typeProvider.calculateMachinesCost(entry, 0, 3.0);
 		EasyMock.replay(entry, typeProvider);
 		
 		Provider provider = new Provider("amazon", 1, 0, 3.0, new long[]{0}, new double[]{0,0}, new long[]{1,10240,51200,153600}, new double[]{0,0.12,0.09,0.07,0.05}, Arrays.asList(typeProvider) );
 		provider.calculateCost(entry , 0);
 		
 		EasyMock.verify(entry, typeProvider);
 	}
 
 	/**
 	 * Test method for {@link commons.cloud.Provider#calculateCost(UtilityResultEntry, long)}.
 	 */
 	@Test
 	public void testCalculateCostWithInTransferenceAndNoOutTransference() {
 		UtilityResultEntry entry = EasyMock.createStrictMock(UtilityResultEntry.class);
 		entry.addProvider("amazon");
 		entry.addTransferenceToCost(5 * CostCalculus.GB_IN_BYTES, 0, 0, 0);
 
 		TypeProvider typeProvider = EasyMock.createStrictMock(TypeProvider.class);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.SMALL);
 		EasyMock.expect(typeProvider.getTotalTransferences()).andReturn(new long[]{5 * CostCalculus.GB_IN_BYTES, 0});
 		typeProvider.calculateMachinesCost(entry, 0, 3.0);
 		EasyMock.replay(entry, typeProvider);
 		
 		Provider provider = new Provider("amazon", 1, 0, 3.0, new long[]{0}, new double[]{0,0}, new long[]{1,10240,51200,153600}, new double[]{0,0.12,0.09,0.07,0.05}, Arrays.asList(typeProvider) );
 		provider.calculateCost(entry , 0);
 		
 		EasyMock.verify(entry, typeProvider);
 	}
 
 	/**
 	 * Test method for {@link commons.cloud.Provider#calculateCost(UtilityResultEntry, long)}.
 	 */
 	@Test
 	public void testCalculateCostWithOutTransferenceBelowMinimum() {
 		UtilityResultEntry entry = EasyMock.createStrictMock(UtilityResultEntry.class);
 		entry.addProvider("amazon");
 		entry.addTransferenceToCost(0, 0, CostCalculus.GB_IN_BYTES/2, 0);
 
 		TypeProvider typeProvider = EasyMock.createStrictMock(TypeProvider.class);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.SMALL);
 		EasyMock.expect(typeProvider.getTotalTransferences()).andReturn(new long[]{0, CostCalculus.GB_IN_BYTES/2});
 		typeProvider.calculateMachinesCost(entry, 0, 3.0);
 		EasyMock.replay(entry, typeProvider);
 		
 		Provider provider = new Provider("amazon", 1, 0, 3.0, new long[]{0}, new double[]{0,0}, new long[]{1,10240,51200,153600}, new double[]{0,0.12,0.09,0.07,0.05}, Arrays.asList(typeProvider) );
 		provider.calculateCost(entry , 0);
 		
 		EasyMock.verify(entry, typeProvider);
 	}
 
 	/**
 	 * Test method for {@link commons.cloud.Provider#calculateCost(UtilityResultEntry, long)}.
 	 */
 	@Test
 	public void testCalculateCostWithOutTransferenceAboveMaximum() {
 		long[] transferOutLimits = new long[]{1,10240,51200,153600};
 		double[] transferOutCosts = new double[]{0,0.12,0.09,0.07,0.05};
 		
 		UtilityResultEntry entry = EasyMock.createStrictMock(UtilityResultEntry.class);
 		entry.addProvider("amazon");
 		long outTransference = 154000;
 		double expectedCost = transferOutLimits[0] * transferOutCosts[0] + 
 								(transferOutLimits[1]-transferOutLimits[0]) * transferOutCosts[1] +
 								(transferOutLimits[2]-transferOutLimits[1]) * transferOutCosts[2] +
 								(transferOutLimits[3]-transferOutLimits[2]) * transferOutCosts[3] +
 								(outTransference - transferOutLimits[3]) * transferOutCosts[4];
 		
 		entry.addTransferenceToCost(0, 0, outTransference * CostCalculus.GB_IN_BYTES, expectedCost);
 
 		TypeProvider typeProvider = EasyMock.createStrictMock(TypeProvider.class);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.SMALL);
 		EasyMock.expect(typeProvider.getTotalTransferences()).andReturn(new long[]{0, outTransference * CostCalculus.GB_IN_BYTES});
 		typeProvider.calculateMachinesCost(entry, 0, 3.0);
 		EasyMock.replay(entry, typeProvider);
 		
 		Provider provider = new Provider("amazon", 1, 0, 3.0, new long[]{0}, new double[]{0,0}, transferOutLimits, transferOutCosts, Arrays.asList(typeProvider) );
 		provider.calculateCost(entry , 0);
 		
 		EasyMock.verify(entry, typeProvider);
 	}
 
 	/**
 	 * Test method for {@link commons.cloud.Provider#calculateUniqueCost(UtilityResult)}.
 	 */
 	@Test
 	public void testCalculateUnicCostWithNoConsumption() {
 		UtilityResult result = EasyMock.createStrictMock(UtilityResult.class);
 		result.addProviderUniqueCost("amazon", MachineType.SMALL, 0);
 		
 		TypeProvider typeProvider = EasyMock.createStrictMock(TypeProvider.class);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.SMALL);
 		EasyMock.expect(typeProvider.calculateUniqueCost()).andReturn(0.0);
 		EasyMock.expect(typeProvider.getType()).andReturn(MachineType.SMALL);
 		EasyMock.replay(result, typeProvider);
 		
 		Provider provider = new Provider("amazon", 1, 0, 3.0, new long[]{}, new double[]{}, new long[]{}, new double[]{}, Arrays.asList(typeProvider) );
 		provider.calculateUniqueCost(result);
 		
 		EasyMock.verify(result);
 	}
 }
