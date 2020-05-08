 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.osgi.region;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.isA;
 import static org.easymock.EasyMock.isNull;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 import static org.junit.Assert.assertEquals;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.eclipse.virgo.kernel.core.Shutdown;
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
 import org.eclipse.virgo.teststubs.osgi.framework.StubServiceRegistration;
 import org.junit.Test;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.event.EventAdmin;
 
 public class RegionManagerTests {
 
     @Test
     public void testStartAndStop() throws Exception {
         StubBundleContext bundleContext = new StubBundleContext();
                               
         EventAdmin eventAdmin = createMock(EventAdmin.class);
         
         Dictionary<String, String> properties = new Hashtable<String, String>();
         Configuration config = createMock(Configuration.class);
         expect(config.getProperties()).andReturn(properties);
         
         ConfigurationAdmin configAdmin = createMock(ConfigurationAdmin.class);
         expect(configAdmin.getConfiguration(isA(String.class), (String) isNull())).andReturn(config);
         
         EventLogger eventLogger = createMock(EventLogger.class);       
         Shutdown shutdown = createMock(Shutdown.class);
         
         replay(eventAdmin, configAdmin, config);
         RegionManager manager = new RegionManager(bundleContext, eventAdmin, configAdmin, eventLogger, shutdown);
         manager.start();
         
         List<StubServiceRegistration<Object>> serviceRegistrations = bundleContext.getServiceRegistrations();
        assertEquals("Region services not registered", 9, serviceRegistrations.size());
         
         manager.stop();
         verify(eventAdmin, configAdmin, config);
         
     }
 }
