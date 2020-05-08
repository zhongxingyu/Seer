 package com.projektur.course.bank;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class AccountTest {
 	
 	private Account account;
 	@Before
 	public void setUp() {
 		account = new Account();
 	}
 	@Test
 	public void testCanInstantiate() {
 		assertNotNull(account);
 	}
 	@Test
 	public void testCanSetBalance() {
 		assertTrue(account.setBalance(500));
 	}
	@Test
	public void testCanRetrieveBalance() {
		assertEquals(new Integer(0), account.getBalance());
	}
 
 }
