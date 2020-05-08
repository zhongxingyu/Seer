 /**
  * 
  */
 package org.jki.staf.service.util;
 
 import com.ibm.staf.service.STAFServiceInterfaceLevel30.RequestInfo;
 

 /**
  * A tiny class to extract the action from a request
  */
 public class ActionExtractor {
 	/**
 	 * Create a new extractor.
 	 */
 	public ActionExtractor() {
 		super();
 	}
 	
 	/**
 	 * Get the upper case action string from a request.
	 * Before parsing is trims the request string.
	 * @param requestInfo - the staf request info
 	 * @return the upper case action name
 	 */
	public String getAction(RequestInfo requestInfo) {
		String request = requestInfo.request.trim();
		int firstWordPos = request.indexOf(" ");
 
 		if (firstWordPos != -1) {
			return request.substring(0, firstWordPos).toUpperCase();
 		} else {
			return request.toUpperCase();
 		}
 	}
 }
