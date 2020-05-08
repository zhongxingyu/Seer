 package com.tort.trade.journals;
 
import static org.testng.Assert.*;

 import org.testng.annotations.Test;
 
 @Test(groups = {"functional"})
 public class NewTransitionsIT extends FunctionalTest{
	public void positive() throws InterruptedException{
 		_selenium.open("/webapp/journal.html");
		for (int second = 0;; second++) {
			if (second >= 60) fail("timeout");
			try { if (_selenium.isElementPresent("//table[@id='goods']//tr[20]")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}

		_selenium.keyDown("filter", "4");
		_selenium.type("//tr[2]/td[3]/input", "+3В,-1С,1$250");
		_selenium.keyPress("//tr[2]/td[3]/input", "\\13");
		for (int second = 0;; second++) {
			if (second >= 60) fail("timeout");
			try { if (!_selenium.isEditable("//tr[2]/td[3]/input")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}		

		_selenium.click("//table[@id='goods']/tbody/tr[6]/td[2]");
		_selenium.type("//tr[3]/td[3]/input", "sdssdf");
		_selenium.keyPress("//tr[3]/td[3]/input", "\\13");
		
		assertEquals(_selenium.getAttribute("//tr[3]/td[3]/input@class"), "badTransitionText");		
 	}
 }
