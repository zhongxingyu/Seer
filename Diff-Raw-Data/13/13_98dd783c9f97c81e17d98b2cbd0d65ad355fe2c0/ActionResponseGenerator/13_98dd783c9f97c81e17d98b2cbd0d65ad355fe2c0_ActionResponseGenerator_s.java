 /**
  * 
  */
 package com.portletguru.portlettester.mocks;
 
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletRequest;
 
 import com.portletguru.portlettester.PortletStatus;
 import com.portletguru.portlettester.TestResultHolder;
 
 /**
  * @author Derek Linde Li
  *
  */
public class ActionResponseGenerator {
 	
	private MockActionResponse actionResponse;
 	
 	public ActionResponseGenerator(PortletStatus portletStatus, PortletRequest portletRequest, TestResultHolder resultHolder) {
		actionResponse = new MockActionResponse(portletStatus, portletRequest, resultHolder);
 	}
 
	public ActionResponse generateActionResponse() {
		return actionResponse;
 	}
 }
