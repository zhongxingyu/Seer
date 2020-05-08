 package com.mattstine.polyglotosgi.vendingmachine.tests.java;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.ops4j.pax.exam.Inject;
 import org.osgi.framework.BundleContext;
 import org.osgi.util.tracker.ServiceTracker;
 
 import com.mattstine.polyglotosgi.vendingmachine.api.Money;
 import com.mattstine.polyglotosgi.vendingmachine.api.VendingMachine;
 
import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;

 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.junit.Configuration;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 
 @RunWith(JUnit4TestRunner.class)
 public class VendingMachineJavaTests {
 	
 	@Inject
 	private BundleContext bundleContext;
 	protected VendingMachine vendingMachine;
 
 	@Test
 	public void testEmptyInventory() {
 		assertEquals("[A:0, B:0, C:0]", vendingMachine.showInventory());
 	}
 
 	@Before
 	public void constructLanguageVendingMachine() throws InterruptedException {
 		ServiceTracker tracker = new ServiceTracker(bundleContext,
 				VendingMachine.class.getName(), null);
 		tracker.open();
 		vendingMachine = (VendingMachine) tracker.waitForService(5000);
 		System.out.println("VM IMPL = " + vendingMachine);
 		tracker.close();
 	}
 
 	@Test
 	public void testEmptyBank() {
 		assertEquals("[Nickle:0, Dime:0, Quarter:0, Dollar:0]", vendingMachine
 				.showBank());
 	}
 
 	@Test
 	public void testService() {
 		vendingMachine.service();
 
 		assertEquals("[Nickle:50, Dime:50, Quarter:50, Dollar:0]",
 				vendingMachine.showBank());
 		assertEquals("[A:10, B:10, C:10]", vendingMachine.showInventory());
 	}
 
 	@Test
 	public void testNoMoneyInserted() {
 		assertEquals("[Nickle:0, Dime:0, Quarter:0, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 	}
 
 	@Test
 	public void testInsertQuarter() {
 		vendingMachine.insert(Money.QUARTER);
 		assertEquals("[Nickle:0, Dime:0, Quarter:1, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 		assertEquals(Money.QUARTER.getAmount(), vendingMachine
 				.currentAmountInserted());
 	}
 
 	@Test
 	public void testInsertDime() {
 		vendingMachine.insert(Money.DIME);
 		assertEquals("[Nickle:0, Dime:1, Quarter:0, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 		assertEquals(Money.DIME.getAmount(), vendingMachine
 				.currentAmountInserted());
 	}
 
 	@Test
 	public void testInsertNickle() {
 		vendingMachine.insert(Money.NICKLE);
 		assertEquals("[Nickle:1, Dime:0, Quarter:0, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 		assertEquals(Money.NICKLE.getAmount(), vendingMachine
 				.currentAmountInserted());
 	}
 
 	@Test
 	public void testInsertDollar() {
 		vendingMachine.insert(Money.DOLLAR);
 		assertEquals("[Nickle:0, Dime:0, Quarter:0, Dollar:1]", vendingMachine
 				.currentMoneyInserted());
 		assertEquals(Money.DOLLAR.getAmount(), vendingMachine
 				.currentAmountInserted());
 	}
 
 	@Test
 	public void testInsertFiftyCents() {
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.DIME);
 		vendingMachine.insert(Money.DIME);
 		vendingMachine.insert(Money.NICKLE);
 
 		assertEquals("[Nickle:1, Dime:2, Quarter:1, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 		assertEquals(50, vendingMachine.currentAmountInserted());
 	}
 
 	@Test
 	public void testCoinReturn() {
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.DIME);
 		vendingMachine.insert(Money.DIME);
 		vendingMachine.insert(Money.NICKLE);
 
 		assertEquals("Q, D, D, N", vendingMachine.coinReturn());
 		assertEquals("[Nickle:0, Dime:0, Quarter:0, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 		assertEquals(0, vendingMachine.currentAmountInserted());
 
 		vendingMachine.insert(Money.DOLLAR);
 		assertEquals("DOLLAR", vendingMachine.coinReturn());
 		assertEquals("[Nickle:0, Dime:0, Quarter:0, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 		assertEquals(0, vendingMachine.currentAmountInserted());
 	}
 
 	@Test
 	public void testBuyBWithExactChange() {
 		// Initialize machine with inventory
 		vendingMachine.service();
 
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.QUARTER);
 
 		assertEquals("B", vendingMachine.buy("B"));
 		assertEquals("[A:10, B:9, C:10]", vendingMachine.showInventory());
 		assertEquals("[Nickle:50, Dime:50, Quarter:54, Dollar:0]",
 				vendingMachine.showBank());
 		assertEquals("[Nickle:0, Dime:0, Quarter:0, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 	}
 
 	@Test
 	public void testBuyAWithExactChange() {
 		// Initialize machine with inventory
 		vendingMachine.service();
 
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.DIME);
 		vendingMachine.insert(Money.NICKLE);
 
 		assertEquals("A", vendingMachine.buy("A"));
 		assertEquals("[A:9, B:10, C:10]", vendingMachine.showInventory());
 		assertEquals("[Nickle:51, Dime:51, Quarter:52, Dollar:0]",
 				vendingMachine.showBank());
 		assertEquals("[Nickle:0, Dime:0, Quarter:0, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 	}
 
 	@Test
 	public void testBuyAWithNoMoneyInserted() {
 		// Initialize machine with inventory
 		vendingMachine.service();
 
 		try {
 			vendingMachine.buy("A");
 			fail();
 		} catch (Exception e) {
 		}
 	}
 
 	@Test
 	public void testBuyAWithNoInventory() {
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.DIME);
 		vendingMachine.insert(Money.NICKLE);
 
 		try {
 			vendingMachine.buy("A");
 			fail();
 		} catch (Exception e) {
 		}
 	}
 
 	@Test
 	public void testBuyBWithNoMoneyInserted() {
 		// Initialize machine with inventory
 		vendingMachine.service();
 
 		try {
 			vendingMachine.buy("B");
 			fail();
 		} catch (Exception e) {
 		}
 	}
 
 	@Test
 	public void testBuyBWithNoInventory() {
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.QUARTER);
 
 		try {
 			vendingMachine.buy("B");
 			fail();
 		} catch (Exception e) {
 		}
 	}
 
 	@Test
 	public void testBuyCWithExactChange() {
 		// Initialize machine with inventory
 		vendingMachine.service();
 
 		vendingMachine.insert(Money.DOLLAR);
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.QUARTER);
 
 		assertEquals("C", vendingMachine.buy("C"));
 		assertEquals("[A:10, B:10, C:9]", vendingMachine.showInventory());
 		assertEquals("[Nickle:50, Dime:50, Quarter:52, Dollar:1]",
 				vendingMachine.showBank());
 		assertEquals("[Nickle:0, Dime:0, Quarter:0, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 	}
 
 	@Test
 	public void testBuyCWithTwoDollars() {
 		// Initialize machine with inventory
 		vendingMachine.service();
 
 		vendingMachine.insert(Money.DOLLAR);
 		vendingMachine.insert(Money.DOLLAR);
 
 		assertEquals("C, Q, Q", vendingMachine.buy("C"));
 		assertEquals("[A:10, B:10, C:9]", vendingMachine.showInventory());
 		assertEquals("[Nickle:50, Dime:50, Quarter:48, Dollar:2]",
 				vendingMachine.showBank());
 		assertEquals("[Nickle:0, Dime:0, Quarter:0, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 	}
 
 	@Test
 	public void testBuyCWithNoMoneyInserted() {
 		// Initialize machine with inventory
 		vendingMachine.service();
 
 		try {
 			vendingMachine.buy("C");
 			fail();
 		} catch (Exception e) {
 		}
 	}
 
 	@Test
 	public void testBuyCWithNoInventory() {
 		vendingMachine.insert(Money.DOLLAR);
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.QUARTER);
 
 		try {
 			vendingMachine.buy("C");
 			fail();
 		} catch (Exception e) {
 		}
 	}
 
 	@Test
 	public void testBuyAWithADollar() {
 		// Initialize machine with inventory
 		vendingMachine.service();
 
 		vendingMachine.insert(Money.DOLLAR);
 		assertEquals("A, Q, D", vendingMachine.buy("A"));
 		assertEquals("[A:9, B:10, C:10]", vendingMachine.showInventory());
 		assertEquals("[Nickle:50, Dime:49, Quarter:49, Dollar:1]",
 				vendingMachine.showBank());
 		assertEquals("[Nickle:0, Dime:0, Quarter:0, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 
 	}
 
 	@Test
 	public void testBuyAWithThreeQuarters() {
 		// Initialize machine with inventory
 		vendingMachine.service();
 
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.QUARTER);
 
 		assertEquals("A, D", vendingMachine.buy("A"));
 		assertEquals("[A:9, B:10, C:10]", vendingMachine.showInventory());
 		assertEquals("[Nickle:50, Dime:49, Quarter:53, Dollar:0]",
 				vendingMachine.showBank());
 		assertEquals("[Nickle:0, Dime:0, Quarter:0, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 	}
 
 	@Test
 	public void testBuyAWithThreeQuartersAndNoChangeAvailable() {
 		// Initialize machine with inventory
 		vendingMachine.service();
 		vendingMachine.cashOut(Money.DIME);
 		vendingMachine.cashOut(Money.NICKLE);
 
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.QUARTER);
 		vendingMachine.insert(Money.QUARTER);
 
 		try {
 			vendingMachine.buy("A");
 			fail();
 		} catch (Exception e) {
 		}
 
 		assertEquals("[A:10, B:10, C:10]", vendingMachine.showInventory());
 		assertEquals("[Nickle:0, Dime:0, Quarter:50, Dollar:0]", vendingMachine
 				.showBank());
 		assertEquals("[Nickle:0, Dime:0, Quarter:3, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 	}
 
 	@Test
 	public void testBuyAWithADollarNoQuartersAvailableForChange() {
 		// Initialize machine with inventory
 		vendingMachine.service();
 		vendingMachine.cashOut(Money.QUARTER);
 
 		vendingMachine.insert(Money.DOLLAR);
 		assertEquals("A, D, D, D, N", vendingMachine.buy("A"));
 		assertEquals("[A:9, B:10, C:10]", vendingMachine.showInventory());
 		assertEquals("[Nickle:49, Dime:47, Quarter:0, Dollar:1]",
 				vendingMachine.showBank());
 		assertEquals("[Nickle:0, Dime:0, Quarter:0, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 	}
 
 	@Test
 	public void testBuyAWithADollarNoQuartersOrDimesAvailableForChange() {
 		// Initialize machine with inventory
 		vendingMachine.service();
 		vendingMachine.cashOut(Money.QUARTER);
 		vendingMachine.cashOut(Money.DIME);
 
 		vendingMachine.insert(Money.DOLLAR);
 		assertEquals("A, N, N, N, N, N, N, N", vendingMachine.buy("A"));
 		assertEquals("[A:9, B:10, C:10]", vendingMachine.showInventory());
 		assertEquals("[Nickle:43, Dime:0, Quarter:0, Dollar:1]", vendingMachine
 				.showBank());
 		assertEquals("[Nickle:0, Dime:0, Quarter:0, Dollar:0]", vendingMachine
 				.currentMoneyInserted());
 	}
 
 	@Test
 	public void testBuyAWithADollarNoChangeAvailable() {
 		// Initialize machine with inventory
 		vendingMachine.service();
 		vendingMachine.cashOut(Money.QUARTER);
 		vendingMachine.cashOut(Money.DIME);
 		vendingMachine.cashOut(Money.NICKLE);
 
 		vendingMachine.insert(Money.DOLLAR);
 
 		try {
 			vendingMachine.buy("A");
 			fail();
 		} catch (Exception e) {
 		}
 
 		assertEquals("[A:10, B:10, C:10]", vendingMachine.showInventory());
 		assertEquals("[Nickle:0, Dime:0, Quarter:0, Dollar:0]", vendingMachine
 				.showBank());
 		assertEquals("[Nickle:0, Dime:0, Quarter:0, Dollar:1]", vendingMachine
 				.currentMoneyInserted());
 	}
 
 	@Configuration
     public static Option[] configuration()
     {
       return options(equinox(), provision(              
            mavenBundle().groupId("com.mattstine.polyglotosgi.vendingmachine").artifactId("pgo-vm-api"),
            mavenBundle().groupId("com.mattstine.polyglotosgi.vendingmachine").artifactId("pgo-vm-java-impl")
        ));
     }
 }
