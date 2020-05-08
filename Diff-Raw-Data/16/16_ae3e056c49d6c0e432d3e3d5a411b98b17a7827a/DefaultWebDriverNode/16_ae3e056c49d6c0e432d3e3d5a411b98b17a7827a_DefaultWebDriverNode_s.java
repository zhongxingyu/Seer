 package com.operativus.senacrs.audit.graph.nodes.webdriver;
 
 import org.openqa.selenium.WebDriver;
 
 
 public class DefaultWebDriverNode extends AbstractWebDriverNode {
 	
 	
 	public DefaultWebDriverNode(WebDriverElementPresenceChecker checker) {
 
		// TODO Auto-generated constructor stub
 		super();
 	}
 
 	@Override
 	protected boolean verifyStateConditions(WebDriver driver) {
 
		// TODO Auto-generated method stub
		return false;
 	}
 
 }
