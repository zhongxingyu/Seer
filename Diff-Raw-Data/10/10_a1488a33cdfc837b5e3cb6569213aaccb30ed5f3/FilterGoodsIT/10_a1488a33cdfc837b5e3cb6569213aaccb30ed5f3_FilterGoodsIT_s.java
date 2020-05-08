 package com.tort.trade.journals;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.fail;
 
 import org.testng.annotations.Test;
 
 
 @Test(groups = {"functional"})
 public class FilterGoodsIT extends FunctionalTest{
 	public void testFilterGoods() throws Exception {
 		_selenium.open("/webapp/journal");		
		for (int second = 0;; second++) {
			if (second >= 10) fail("timeout");
			try { if (_selenium.isElementPresent("//table[@id='goods']//tr[20]")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
 		assertEquals(_selenium.getTable("goods.0.1"), "джинсы труба голубая");
		assertTrue(_selenium.isElementPresent("//table[@id='goods']//tr[20]"));
 		_selenium.focus("filter");
 		_selenium.type("filter", "дж с");
 		_selenium.keyUp("filter", "с");
 		for (int second = 0;; second++) {
 			if (second >= 10) fail("timeout");
 			try { if (_selenium.isElementPresent("//table[@id='goods']//tr[10]")) break; } catch (Exception e) {}
 			Thread.sleep(1000);
 		}
 		assertFalse(_selenium.isElementPresent("//table[@id='goods']//tr[11]"));
 		assertEquals(_selenium.getTable("goods.0.1"), "дж стеганые");		
 	}
 }
