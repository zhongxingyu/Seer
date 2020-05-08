 /*******************************************************************************
  * Copyright (c) 2009 Markus Alexander Kuppe
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Markus Alexander Kuppe (ecf-dev <at> lemmster <dot> de) - initial API and implementation
  ******************************************************************************/
 package ch.ethz.iks.slp.test;
 
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.List;
 
 import junit.framework.Assert;
 import ch.ethz.iks.slp.ServiceLocationEnumeration;
 import ch.ethz.iks.slp.ServiceLocationException;
 import ch.ethz.iks.slp.ServiceType;
 import ch.ethz.iks.slp.ServiceURL;
 
 public class SecuredSelfDiscoveryTest extends SLPTestCase {
 
	private static final String SERVICE_TYPE = "service:securitygrouptestservice://";
 	private KeyPair keyPair;
 
 	/*
 	 * (non-Javadoc)
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	public void setUp() throws Exception {
 		try {
 			KeyPairGenerator generator = KeyPairGenerator.getInstance("DSA");
 			generator.initialize(1024);
 			keyPair = generator.generateKeyPair();
 			
 			service = new ServiceURL(SERVICE_TYPE + "://" + HOST_AND_PORT, 10800);
 			properties = new Hashtable();
 			TestActivator.advertiser.register(service, properties, keyPair);
 		} catch (ServiceLocationException e) {
 			Assert.fail(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.ethz.iks.slp.test.SLPTestCase#tearDown()
 	 */
 	public void tearDown() throws InterruptedException {
 		try {
 			if(service != null) {
 				TestActivator.advertiser.deregister(service, keyPair);
 			}
 		} catch (ServiceLocationException e) {
 			Assert.fail(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Test method for
 	 * {@link ch.ethz.iks.slp.Locator}.
 	 */
 	public void testService() throws Exception {
 		List list = new ArrayList(1);
 		for (ServiceLocationEnumeration services = TestActivator.locator
 				.findServices(new ServiceType(SERVICE_TYPE), null, null, keyPair); services
 				.hasMoreElements();) {
 			Object actual = services.next();
 			list.add(actual);
 			assertEquals(SERVICE_TYPE + "://"  + HOST_AND_PORT, actual.toString());
 		}
 		assertEquals(Arrays.toString(list.toArray()), 1, list.size());
 	}
 }
