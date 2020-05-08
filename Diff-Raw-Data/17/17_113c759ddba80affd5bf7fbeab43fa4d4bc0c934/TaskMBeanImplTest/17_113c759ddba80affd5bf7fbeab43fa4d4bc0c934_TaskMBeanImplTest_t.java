 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.jmx.mbean.impl;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.jmx.JMXService;
 import org.openmrs.module.jmx.mbean.TaskMBean;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 
 /**
  * Test case for TaskMBeanImpl class
  */
 public class TaskMBeanImplTest extends BaseModuleContextSensitiveTest {
 	
 	private TaskMBean mbean = new TaskMBeanImpl(1);
 	
 	@Before
     public void setup() throws Exception {
         initializeInMemoryDatabase();
         authenticate();
        executeDataSet("Testing-tasks-data.xml");
         
         JMXService svc = Context.getService(JMXService.class);
 		svc.registerMBean("Testing", "TestTask", mbean);
     }
 	
 	@After
 	public void cleanup() {
 		JMXService svc = Context.getService(JMXService.class);
 		svc.unregisterMBean("Testing", "TestTask");
     }
 	
 	@Test
 	public void getName() {	
 		Assert.assertEquals("Test Task", mbean.getName());
 	}
 	
 	@Test
 	public void getTaskClass() {	
 		Assert.assertEquals("org.openmrs.scheduler.tasks.ConceptIndexUpdateTask", mbean.getTaskClass());
 	}
 	
 	@Test
 	public void isStarted() {	
 		Assert.assertFalse(mbean.isStarted());
 	}
 	
 	@Test
 	public void getLastExecutionTime() {
 		Assert.assertEquals(makeTimestamp(2000, 1, 1), mbean.getLastExecutionTime().getTime());
 	}
 	
	@Test
	public void getNextExecutionTime() {
		Assert.assertNotNull(mbean.getNextExecutionTime());
	}
	
 	private long makeTimestamp(int year, int month, int day) {
 		Calendar calendar = new GregorianCalendar();
 		calendar.set(year, month - 1, day);
		calendar.set(Calendar.AM_PM, Calendar.AM);
 		calendar.set(Calendar.HOUR, 0);
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		calendar.set(Calendar.MILLISECOND, 0);
 		return calendar.getTime().getTime();
 	}
 }
