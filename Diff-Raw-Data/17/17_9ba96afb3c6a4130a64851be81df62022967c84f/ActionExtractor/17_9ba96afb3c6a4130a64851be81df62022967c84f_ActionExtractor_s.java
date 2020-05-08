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
	 * @param reqInfo - the staf request
 	 * @return the upper case action name
 	 */
	public String getAction(RequestInfo reqInfo) {
		int firstWordPos = reqInfo.request.indexOf(" ");
 
 		if (firstWordPos != -1) {
			return reqInfo.request.substring(0, firstWordPos).toUpperCase();
 		} else {
			return reqInfo.request.toUpperCase();
 		}
 	}
 }
