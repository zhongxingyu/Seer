 package com.operativus.senacrs.audit.graph.nodes.webdriver.checkers;
 
import com.operativus.senacrs.audit.exceptions.ExceptionMessagesEnum;
 import com.operativus.senacrs.audit.exceptions.RuntimeExceptionFactory;
import com.operativus.senacrs.audit.properties.messages.MessagesCentral;
 import com.operativus.senacrs.audit.properties.xpath.XPathCentral;
 import com.operativus.senacrs.audit.properties.xpath.XPathKeyPrefix;
 
 public final class WebDriverElementPresenceCheckerFactory {
 
 	private WebDriverElementPresenceCheckerFactory() {
 
 		super();
 	}
 
 	public static WebDriverElementPresenceChecker createChecker(final XPathKeyPrefix prefix) {
		
		String msg = null;
 
 		if (prefix == null) {
 			throw RuntimeExceptionFactory.getInstance().getNullArgumentException("prefix");
 		}
		if (prefix.paramAmount() != 0) {
			msg = MessagesCentral.getMessage(ExceptionMessagesEnum.ILLEGAL_XPATH_PARAM_AMOUNT, 0, prefix.paramAmount());
			throw new IllegalArgumentException(msg);
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
