 /* UpdateResponseHandler.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		May 22, 2012 Created by pao
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
 */
 package org.zkoss.zats.mimic.impl;
 
 import java.util.Map;
 
 import org.zkoss.zats.mimic.DesktopAgent;
 
 /**
  * The interface of asynchronous update response handler.
  * The handler can process response when client post an AU event,
  * and modify status of desktop if necessary. 
  * @author pao
  */
 public interface UpdateResponseHandler {
 
 	/**
 	 * perform AU response processing.
 	 * @param desktop desktop agent
	 * @param response response data
 	 */
 	void process(DesktopAgent desktop, Map<String, Object> jsonObject);
 }
