 /**
  * 
  */
 package tests;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import code.AbstractObserver;
 import code.Accounts;
 import code.Auditor;
 
 /**
  * @author Oded
  *
  */
 public class AuditorTest {
 
 	 Accounts accounts;
 	 AbstractObserver auditor1;
 	 AbstractObserver auditor2;
 		
 	@Before
 	public  void setUp() throws Exception {
 		accounts = new Accounts();
 		accounts.openNewAccount();
 		accounts.openNewAccount();
 		
 		auditor1 = new Auditor(accounts,1);
 		auditor2 = new Auditor(accounts,2);
 		
 		accounts.attach(auditor1);
		accounts.attach(auditor2);
 		
 	}
 		
 	@Test
 	public void test1() {
 		assertTrue("Account number 1 does not exist although it should", accounts.doesAccountExist(1));
 	}
 
 	@Test
 	public void test2() {
 		assertTrue("Account number 2 does not exist although it should", accounts.doesAccountExist(2));
 	}
 	
 	@Test
 	public void test3() {
 		assertFalse("Account number 3 exists although it should not", accounts.doesAccountExist(3));
 	}
 
 	@Test
 	public void test4() {
 		Integer accountTested = 1;
 		Integer sumDeposited = 100;
 		Integer accountBalance;
 		
 		accounts.deposit(accountTested, sumDeposited);
 		accountBalance = accounts.getBalance(accountTested); 
 		
 		assertEquals("Problems with account balance", sumDeposited, accountBalance);
 	}
 	
 	@Test
 	public void test5() {
 		Integer accountTested = 2;
 		Integer sumWithdrawn = 100;
 		Integer accountBalance;
 		
 		accounts.withdraw(accountTested, sumWithdrawn);
 		accountBalance = -accounts.getBalance(accountTested); 
 		
 		assertEquals("Problems with account balance", sumWithdrawn, accountBalance);
 	}
 	
 	@Test
 	public void test6() {
 		Integer expectAccountNumber = 3;
 		Integer actualAcountNumber = accounts.openNewAccount();
 		
 		assertEquals("Problems with account balance", expectAccountNumber, actualAcountNumber);
 	}
 
 }
