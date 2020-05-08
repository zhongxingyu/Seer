 package com.tort.trade.journals;
 
 import org.testng.annotations.Test;
 import static org.testng.AssertJUnit.*;
 
 @Test(groups = {"functional"})
 public class GoodsBalanceIT extends FunctionalTest {
 	public void getBalance() throws InterruptedException{
 		_selenium.open("/webapp/journal");
 		_selenium.click("//a[@name='balance']");
 		
 		waitForElement("//table[@id='balance']//tr[5]");
 		
 		assertEquals("Goods balance", _selenium.getTitle());
 	}
 }
