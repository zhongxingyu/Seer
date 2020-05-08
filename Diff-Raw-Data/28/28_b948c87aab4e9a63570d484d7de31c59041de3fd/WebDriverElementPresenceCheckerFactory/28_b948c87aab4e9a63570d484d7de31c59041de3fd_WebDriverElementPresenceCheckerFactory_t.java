 package com.operativus.senacrs.audit.graph.nodes.webdriver;
 
import com.operativus.senacrs.audit.exceptions.RuntimeExceptionFactory;

 
 public final class WebDriverElementPresenceCheckerFactory {
 
 	private WebDriverElementPresenceCheckerFactory() {
 		
 		super();
 	}
 	
 	public static WebDriverElementPresenceChecker createChecker(WebDriverCheckerTypeEnum type) {
 		
		if (type == null) {
			throw RuntimeExceptionFactory.getInstance().getNullArgumentException("type");
		}
		
		return internCreateChecker(type);
	}

	private static WebDriverElementPresenceChecker internCreateChecker(WebDriverCheckerTypeEnum type) {
		
		WebDriverElementPresenceChecker result = null;

		switch (type) {
		case DASHBOARD:
			result = createDashboardChecker();
			break;
		default:
			throw new IllegalArgumentException(type.name());
		} 
				
		return result;
	}

	protected static WebDriverElementPresenceChecker createDashboardChecker() {

		// TODO Auto-generated method stub
 		return null;
 	}
 }
