 package edu.gatech.CS2340.GrandTheftPoke.tests;
 
 //import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.*;
 
 //import org.junit.Before;
 import org.junit.Test;
 
 import edu.gatech.CS2340.GrandTheftPoke.backend.Wallet;
 
 public class WalletTest extends junit.framework.TestCase {
	private final float AMOUNT = 10;;
 	/**
 	 * toString
 	 * @return the name of the class
 	 */
 	public String toString() {
 		return "WalletTest";
 	}
 	@Test
 	public void testWallet(){
 		
 		Wallet wallet = new Wallet(AMOUNT);
 		float money;
 		money = wallet.getMoney();
 		assertEquals(money,AMOUNT);
 	}
 	public void testUpdateMoney(){
 		Wallet wallet = new Wallet((float) 0);
 		wallet.updateMoney(AMOUNT);
 		assertEquals(wallet.getMoney(),AMOUNT);
 	}
 	public void testCheckAmount(){
 		Wallet wallet1 = new Wallet(AMOUNT);
 		Wallet wallet2 = new Wallet ((float)0);
 		float money;
 		money = wallet2.getMoney();
 		assertTrue(wallet1.checkAmount(money));
 		
 	}
 }
