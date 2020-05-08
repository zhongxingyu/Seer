 package com.operativus.senacrs.audit.graph.nodes.webdriver;
 
 import org.openqa.selenium.WebDriver;
 
import com.operativus.senacrs.audit.exceptions.RuntimeExceptionFactory;

 
 public class DefaultWebDriverNode extends AbstractWebDriverNode {
 	
	private final WebDriverElementPresenceChecker checker;
 	
 	public DefaultWebDriverNode(WebDriverElementPresenceChecker checker) {
 
 		super();

		if (checker == null) {
			throw RuntimeExceptionFactory.getInstance().getNullArgumentException("checker");
		}
		this.checker = checker;
 	}
 
 	@Override
 	protected boolean verifyStateConditions(WebDriver driver) {
 
		return this.checker.hasAll(driver);
 	}
 
 }
