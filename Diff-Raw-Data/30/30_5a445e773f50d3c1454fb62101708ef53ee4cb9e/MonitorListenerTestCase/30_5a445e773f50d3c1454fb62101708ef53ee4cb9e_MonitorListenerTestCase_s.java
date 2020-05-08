 /*******************************************************************************
  * Copyright (c) 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.internet.monitor.core.tests;
 
 import org.eclipse.wst.internet.monitor.core.*;
 import junit.framework.Test;
 import junit.framework.TestCase;
 
 public class MonitorListenerTestCase extends TestCase {
 	private static IMonitor monitor;
 	
 	protected static IMonitor addEvent;
 	protected static IMonitor changeEvent;
 	protected static IMonitor removeEvent;
 	protected static int count;
 
 	protected static IMonitorListener listener = new IMonitorListener() {
 		public void monitorAdded(IMonitor monitor2) {
 			addEvent = monitor2;
 			count++;
 		}
 
 		public void monitorChanged(IMonitor monitor2) {
 			changeEvent = monitor2;
 			count++;
 		}
 
 		public void monitorRemoved(IMonitor monitor2) {
 			removeEvent = monitor2;
 			count++;
 		}
 	};
 
 	public MonitorListenerTestCase() {
 		super();
 	}
 
 	public static Test suite() {
 		return new OrderedTestSuite(MonitorListenerTestCase.class, "MonitorListenerTestCase");
 	}
 
 	public void test0AddListener() throws Exception {
 		MonitorCore.addMonitorListener(listener);
 	}
 	
 	public void test1AddListener() throws Exception {
 		MonitorCore.addMonitorListener(listener);
 	}
 	
 	public void test2AddListener() throws Exception {
 		IMonitorWorkingCopy wc = MonitorCore.createMonitor();
 		monitor = wc.save();
 		
 		assertTrue(addEvent == monitor);
 		assertTrue(changeEvent == null);
 		assertTrue(removeEvent == null);
 		assertTrue(count == 1);
 		addEvent = null;
 		count = 0;
 	}
 
 	public void test3ChangeListener() throws Exception {
 		IMonitorWorkingCopy wc = monitor.createWorkingCopy();
 		wc.setLocalPort(1);
 		monitor = wc.save();
 
 		assertTrue(addEvent == null);
 		assertTrue(changeEvent == monitor);
 		assertTrue(removeEvent == null);
 		assertTrue(count == 1);
 		changeEvent = null;
 		count = 0;
 	}
 
 	public void test4RemoveListener() throws Exception {
 		monitor.delete();
 		
 		assertTrue(addEvent == null);
 		assertTrue(changeEvent == null);
 		assertTrue(removeEvent == monitor);
 		assertTrue(count == 1);
 		removeEvent = null;
 		count = 0;
 	}
 	
 	public void test5RemoveListener() throws Exception {
 		MonitorCore.removeMonitorListener(listener);
 	}
 	
 	public void test6RemoveListener() throws Exception {
 		MonitorCore.removeMonitorListener(listener);
 	}
 }
