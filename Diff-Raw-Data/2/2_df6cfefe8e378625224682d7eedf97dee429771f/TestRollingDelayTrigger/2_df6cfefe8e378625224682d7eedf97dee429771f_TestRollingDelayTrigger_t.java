 /**
  * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.util.concurrent;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
import com.barchart.util.test.concurrent.CallableTest;
 
 public class TestRollingDelayTrigger {
 
 	private TriggerTask task;
 	private RollingDelayTrigger trigger;
 
 	@Before
 	public void setUp() {
 		task = new TriggerTask();
 		trigger = new RollingDelayTrigger(task, 100);
 	}
 
 	@Test
 	public void testDelay() throws Exception {
 		trigger.trigger();
 		assertEquals(0, task.runCount);
 		CallableTest.waitFor(new CallableTest.FieldValue(task, "runCount", 1));
 		assertEquals(1, task.runCount);
 	}
 
 	@Test
 	public void testMultipleTriggers() throws Exception {
 
 		for (int i = 0; i < 10; i++) {
 			trigger.trigger();
 			Thread.sleep(20);
 		}
 
 		assertEquals(0, task.runCount);
 
 		CallableTest.waitFor(new CallableTest.FieldValue(task, "runCount", 1));
 		assertEquals(1, task.runCount);
 
 	}
 
 	@Test
 	public void testMultipleTasks() throws Exception {
 
 		trigger.trigger();
 		CallableTest.waitFor(new CallableTest.FieldValue(task, "runCount", 1));
 		assertEquals(1, task.runCount);
 
 		trigger.trigger();
 		CallableTest.waitFor(new CallableTest.FieldValue(task, "runCount", 2));
 		assertEquals(2, task.runCount);
 
 	}
 
 	public static class TriggerTask implements Runnable {
 
 		public volatile int runCount = 0;
 
 		@Override
 		public void run() {
 			runCount++;
 		}
 
 	}
 
 }
