 /**
  * Mule Google Calendars Cloud Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.google.calendar.automation.testcases;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.mule.api.MuleEvent;
 import org.mule.api.processor.MessageProcessor;
 import org.mule.module.google.calendar.model.AclRule;
 import org.mule.module.google.calendar.model.Calendar;
 
 public class GetAclRuleByIdTestCases extends GoogleCalendarTestParent {
 	
 	
 	@Before
 	public void setUp() {
 		try {
			testObjects = (Map<String, Object>) context.getBean("getAclRuleById");
 			
 			// Insert calendar and get reference to retrieved calendar
 			Calendar calendar = insertCalendar((Calendar) testObjects.get("calendarRef"));
 			
 			// Replace old calendar instance with new instance
 			testObjects.put("calendarRef", calendar);
 			testObjects.put("calendarId", calendar.getId());
 			
 			
 			// Insert the ACL rule
 			MessageProcessor flow = lookupFlowConstruct("insert-acl-rule");
 			MuleEvent event = flow.process(getTestEvent(testObjects));
 						
 			AclRule returnedAclRule = (AclRule) event.getMessage().getPayload();
 			testObjects.put("aclRule", returnedAclRule);	
 			testObjects.put("ruleId", returnedAclRule.getId());
 			
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@Category({SmokeTests.class, RegressionTests.class})	
 	@Test
 	public void testGetAclRuleById() {
 		try {
 			String ruleIdBefore = testObjects.get("ruleId").toString();
 			
 			MessageProcessor flow = lookupFlowConstruct("get-acl-rule-by-id");
 			MuleEvent event = flow.process(getTestEvent(testObjects));
 			
 			AclRule afterProc = (AclRule) event.getMessage().getPayload();
 			String ruleIdAfter = afterProc.getId();
 			
 			assertEquals(ruleIdBefore,ruleIdAfter);		
 			
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	
 	@After
 	public void tearDown() {
 		try {
 			String calendarId = testObjects.get("calendarId").toString();
 			deleteCalendar(calendarId);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 }
