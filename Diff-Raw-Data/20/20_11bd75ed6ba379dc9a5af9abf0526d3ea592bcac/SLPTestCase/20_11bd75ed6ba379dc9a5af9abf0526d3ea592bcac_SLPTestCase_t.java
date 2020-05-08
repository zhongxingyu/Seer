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
 
 import java.util.Dictionary;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 import ch.ethz.iks.slp.ServiceLocationException;
 import ch.ethz.iks.slp.ServiceURL;
 
 public abstract class SLPTestCase extends TestCase {
 
 	protected static final String HOST_AND_PORT = System.getProperty("net.slp.tests.hostAndPort", "gantenbein:123");
 	protected ServiceURL service;
 	protected Dictionary properties;
 
 	public void tearDown() throws InterruptedException {
 		try {
 			if(service != null) {
 				TestActivator.advertiser.deregister(service);
 			}
 		} catch (ServiceLocationException e) {
 			Assert.fail(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 }
