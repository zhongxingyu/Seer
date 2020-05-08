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
 
 package org.eclipse.virgo.medic.log.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 
 
 import org.eclipse.virgo.medic.log.LoggingConfigurationPublisher;
 import org.eclipse.virgo.medic.log.appender.StubAppender;
 import org.eclipse.virgo.test.framework.OsgiTestRunner;
 import org.eclipse.virgo.test.framework.TestFrameworkUtils;
 
 @RunWith(OsgiTestRunner.class)
 public class MedicLoggingIntegrationTests {
 	
     private BundleContext bundleContext;
 
     @Before
     public void before() {
         this.bundleContext = TestFrameworkUtils.getBundleContextForTestClass(getClass());
     }
 	
 	@Test
 	public void test() throws BundleException {				
 		bundleContext.installBundle("file:src/test/resources/test-bundle_1").start();
 		assertEquals(1, StubAppender.getAndResetLoggingEvents("bundle1-stub").size());
 		
 		bundleContext.installBundle("file:src/test/resources/test-bundle_2").start();
 		assertEquals(1, StubAppender.getAndResetLoggingEvents("bundle2-stub").size());
 		
 		bundleContext.installBundle("file:src/test/resources/test-bundle_3").start();
		assertEquals(13, StubAppender.getAndResetLoggingEvents("root-stub").size()); //9 When run in Eclipse
 	}
 	
	@Test
 	public void availabilityOfConfigurationPublisher() {
 		assertNotNull(bundleContext.getServiceReference(LoggingConfigurationPublisher.class.getName()));
 	}		
 }
