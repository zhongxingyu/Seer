 package com.operativus.senacrs.audit.graph.nodes.webdriver.checkers;
 
 import com.operativus.senacrs.audit.exceptions.RuntimeExceptionFactory;
 import com.operativus.senacrs.audit.properties.xpath.XPathCentral;
 import com.operativus.senacrs.audit.properties.xpath.XPathKeyPrefix;
 
 public final class WebDriverElementPresenceCheckerFactory {
 
 	private WebDriverElementPresenceCheckerFactory() {
 
 		super();
 	}
 
 	public static WebDriverElementPresenceChecker createChecker(final XPathKeyPrefix prefix) {
 
 		if (prefix == null) {
 			throw RuntimeExceptionFactory.getInstance().getNullArgumentException("prefix");
 		}
 
 		return internCreateChecker(prefix);
 	}
 
 	private static WebDriverElementPresenceChecker internCreateChecker(final XPathKeyPrefix prefix) {
 
 		WebDriverElementPresenceChecker result = null;
 		String[] xpathElements = null;
 
 		xpathElements = XPathCentral.getXPathByPrefix(prefix);
 		result = new WebDriverElementPresenceChecker(xpathElements);
 
 		return result;
 	}
 }
