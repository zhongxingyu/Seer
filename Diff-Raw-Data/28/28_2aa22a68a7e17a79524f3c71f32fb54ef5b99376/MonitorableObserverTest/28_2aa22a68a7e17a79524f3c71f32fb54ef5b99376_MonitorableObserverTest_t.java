 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.core.monitorable.observer.impl;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import org.eclipse.osgi.framework.internal.core.FilterImpl;
 import org.hamcrest.Description;
 import org.jmock.Expectations;
 import org.jmock.api.Action;
 import org.jmock.api.Invocation;
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Filter;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventAdmin;
 import org.osgi.service.monitor.MonitorAdmin;
 import org.osgi.service.monitor.Monitorable;
 import org.osgi.service.monitor.StatusVariable;
 
 public class MonitorableObserverTest extends MockObjectTestCase {
 	private BundleContext bc;
 	
 	private ServiceReference eventAdminRef;
 	private EventAdmin eventAdmin;
 	
 	private ServiceReference monitorAdminRef;
 	private MonitorAdmin monitorAdmin;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		// mocking an OSGi bundle context
 		bc = mock(BundleContext.class);
 		this.eventAdminRef = mock(ServiceReference.class,"eventAdminRef");
 		this.eventAdmin = mock(EventAdmin.class);
 		this.monitorAdminRef = mock(ServiceReference.class,"monitorAdminRef");
 		this.monitorAdmin = mock(MonitorAdmin.class);		
 		
 		
 		checking(new Expectations(){{
 			// creating the ldap styled filter
 			allowing(bc).createFilter(with(any(String.class)));
 			will(new Action(){
 				public void describeTo(Description arg0) {}
 				public Object invoke(Invocation invocation) throws Throwable {					
					return FilterImpl.newInstance((String)invocation.getParameter(0));
 				}				
 			});
 
 			allowing(bc).addServiceListener(with(any(ServiceListener.class)), with(any(String.class)));
 			allowing(bc).registerService(with(any(String.class)), with(anything()), with(any(Dictionary.class)));
 			
 			// getting the monitor-admin service			
 			allowing(bc).getServiceReference(MonitorAdmin.class.getName()); will(returnValue(monitorAdminRef));
 			allowing(bc).getService(monitorAdminRef); will(returnValue(monitorAdmin));
 			allowing(monitorAdmin).startScheduledJob(with(any(String.class)), with(any(String[].class)), with(any(int.class)), with(any(int.class)));
 			
 			// functions to return the event-admin
 			allowing(bc).getServiceReference(EventAdmin.class.getName()); will(returnValue(eventAdminRef));
 			allowing(bc).getService(eventAdminRef); will(returnValue(eventAdmin));
 		}});
 	}
 	
 //	public void testExtractMonitorables() {
 //		// extracting the list of used monitorable-variables from the filter-expression
 //		Map<String,Set<String>> variables = MonitorableObserver.extractMonitorables(
 //				"(|(java.lang.runtime/memory.free <= 10240)(os.disk/disk.space.free<=256))"
 //		);
 //		
 //		assertNotNull(variables);
 //		assertEquals(2, variables.size());
 //		
 //		assertTrue(variables.containsKey("java.lang.runtime"));
 //		assertTrue(variables.get("java.lang.runtime").contains("memory.free"));
 //		
 //		assertTrue(variables.containsKey("os.disk"));
 //		assertTrue(variables.get("os.disk").contains("disk.space.free"));
 //	}
 	
 	public void testFilter() throws InvalidSyntaxException {
		Filter f = FilterImpl.newInstance("(|(java.lang.runtime/memory.free <= 10240)(os.disk/disk.space.free<=256))");
 		
 		Dictionary<String, Object> values = new Hashtable<String, Object>();
 		assertFalse(f.match(values));
 		
 		values.put("java.lang.runtime/memory.free", Integer.valueOf(20000));
 		assertFalse(f.match(values));
 		
 		values.put("java.lang.runtime/memory.free", Integer.valueOf(10000));
 		assertTrue(f.match(values));
 		
		f = FilterImpl.newInstance("(os.usage.cpu/cpu.usage.total >= 0.8)");
 		values = new Hashtable<String, Object>();
 		values.put("os.usage.cpu/cpu.usage.total", new Float(0.7));
 		assertFalse(f.match(values));
 		
 		values.put("os.usage.cpu/cpu.usage.total", new Float(0.8));
 		assertTrue(f.match(values));
 	}
 	
 	@SuppressWarnings("serial")
 	public void testMonitorableObserver() throws InvalidSyntaxException {
 		Filter testFilter = bc.createFilter("(|(java.lang.runtime/memory.free <= 10240)(os.disk/disk.space.free<=256))");
 		
 		// a dummy monitorable
 		final String MONITORABLE_PID = "java.lang.runtime";
 		final ServiceReference monitorableRef = mock(ServiceReference.class);
 		final Monitorable monitorable = mock(Monitorable.class);
 		
 		// a dummy monitorable-variable
 		final String VAR_NAME = "memory.free";
 		final int varValue = 10239;
 		final StatusVariable var = new StatusVariable(VAR_NAME,StatusVariable.CM_GAUGE,varValue);
 		
 		// defining more expectations
 		checking(new Expectations(){{			
 			// query of available monitorables
 			allowing(bc).getServiceReferences(Monitorable.class.getName(), null); will(returnValue(new ServiceReference[]{monitorableRef}));
 			allowing(bc).getService(monitorableRef); will(returnValue(monitorable));
 			
 			// getting monitorable properties
 			allowing(monitorableRef).getProperty(Constants.SERVICE_PID); will(returnValue(MONITORABLE_PID));
 			allowing(monitorable).getStatusVariable(VAR_NAME); will(returnValue(var));
 			
 			// at least one event must be sent
 			atMost(1).of(eventAdmin).sendEvent(with(any(Event.class)));
 		}});
 		
 		// creating an observer
 		MonitorableObserver observer = new MonitorableObserver(
         		bc, 
         		new ObserverRule(
         				// Filter based condition
         				new ObserverFilterCondition(testFilter),
         				// triggers an event
         				new ObserverEventSenderConsequence(bc, new Hashtable<String, Object>(){
 						{
         			        put("prop1","test1");
         			        put("prop2", Boolean.FALSE);
         				}}, false)
         		)
         ); 
 		
 		// creating a dummy event
 		Event event = new Event("test", new Hashtable<String, Object>(){{
 			put("mon.monitorable.pid",MONITORABLE_PID);
 			put("mon.statusvariable.name",VAR_NAME);
 			put("mon.statusvariable.value",Integer.toString(varValue));
 		}});		
 		
 		// send 1. event which causes an observer event to be sent
 		observer.handleEvent(event);
 		
 		// send 2. event which does not change the value of the variable
 		observer.handleEvent(event);
 	}
 }
