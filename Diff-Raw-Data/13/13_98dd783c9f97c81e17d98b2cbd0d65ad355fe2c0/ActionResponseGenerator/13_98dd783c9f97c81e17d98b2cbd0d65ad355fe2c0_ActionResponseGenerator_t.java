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
public class ActionResponseGenerator extends StateAwareResponseGenerator {
 	
 	
 	public ActionResponseGenerator(PortletStatus portletStatus, PortletRequest portletRequest, TestResultHolder resultHolder) {
		super(portletStatus, portletRequest);
		portletResponse = new MockActionResponse(portletStatus, portletRequest, resultHolder);
 	}
 
	public ActionResponse generateResponse() {
		return (ActionResponse) portletResponse;
 	}
 }
