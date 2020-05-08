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
 
 import java.util.Hashtable;
 
 import junit.framework.Assert;
 import ch.ethz.iks.slp.ServiceLocationException;
 import ch.ethz.iks.slp.ServiceURL;
 
 public class SecuredSelfDiscoveryTest extends SLPTestCase {
 
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	public void setUp() throws InterruptedException {
 		try {
 			service = new ServiceURL("service:osgi://" + HOST_AND_PORT, 10800);
 			properties = new Hashtable();
 			TestActivator.advertiser.register(service, properties);
 		} catch (ServiceLocationException e) {
 			Assert.fail(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 }
