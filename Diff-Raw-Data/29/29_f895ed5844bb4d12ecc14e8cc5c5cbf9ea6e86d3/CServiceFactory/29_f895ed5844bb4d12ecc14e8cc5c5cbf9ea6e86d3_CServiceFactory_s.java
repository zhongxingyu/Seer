 /**
  * 
  */
 package net.frontlinesms.camel.smslib;
 
 import java.util.Map;
 
 import org.smslib.CService;
import org.smslib.CService.MessageClass;
 
 class CServiceFactory {
 	private static final int DEFAULT_BAUD = 57600;
 
 	public CService create(String uri, String remaining, Map<String, Object> params) {
 		CService cService = new CService(remaining,
 				getBaud(params),
 				getString(params, "manufacturer"),
 				getString(params, "model"),
 				getString(params, "handler"));
 		cService.setSimPin(getPin(params));
 		cService.setAsyncRecvClass(getBoolean(params, "allMessages")? MessageClass.ALL: MessageClass.UNREAD);
 		return cService;
 	}
 	
 	private boolean getBoolean(Map<String, Object> params, String key) {
 		return Boolean.parseBoolean(getString(params, key));
 	}
 	
 	private String getString(Map<String, Object> params, String key) {
 		String m  = (String) params.get(key);
 		return m == null ? "" : m;
 	}
 	
 	private String getPin(Map<String, Object> params) {
 		String pin = getString(params, "pin");
 		return pin.length() > 0? pin: null;
 	}
 
 	private int getBaud(Map<String, Object> parameters) {
 		String baud = (String) parameters.get("baud");
 		try {
 			return Integer.parseInt(baud);
 		} catch(Exception e) {
 			return DEFAULT_BAUD;
 		}
 	}
 }
