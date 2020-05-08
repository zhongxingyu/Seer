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
 
 package org.eclipse.virgo.web.test;
 
 import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
 import static javax.servlet.http.HttpServletResponse.SC_OK;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.lang.management.ManagementFactory;
 import java.net.URI;
 
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 
 import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.deployer.core.DeploymentOptions;
 import org.junit.Test;
 
 public class SessionListenerAppNameTests extends AbstractWebIntegrationTests {
 
 	protected final URI uri = new File("src/test/apps/listener-test.war")
 			.toURI();
 
 	private final MBeanServer mBeanServer = ManagementFactory
 			.getPlatformMBeanServer();
 
 	@Test
 	public void testSessionListener() throws Exception {
 
 		String context = "test";
 
 		DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(
 				this.uri, new DeploymentOptions(false, false, true));
 		this.deployedWebApps.add(context);
 
 		// Uncomment if you'd like to pause the test and view the results in a
 		// web browser.
 		// System.in.read();
 
 		assertGetRequest(context, "hello.jsp", false, SC_OK, null);
 
 		assertEquals(1, getSessionCount());
 		assertNotNull(getApplicationName());
 		assertTrue(getApplicationName().startsWith("listenertest"));
 
 		this.mBeanServer.invoke(getObjectName(), "invalidate", null, null);
 		this.mBeanServer.invoke(getObjectName(), "awaitNextDecrement", null,
 				null);
 
 		assertEquals(0, getSessionCount());
 		assertNotNull(getApplicationName());
 		assertTrue(getApplicationName().startsWith("listenertest"));
 
 		this.appDeployer.undeploy(deploymentIdentity);
 		assertGetRequest(context, "hello.jsp", SC_BAD_REQUEST, null);
 	}
 
 	private int getSessionCount() throws Exception {
 		ObjectName oname = getObjectName();
 		int count = (Integer) this.mBeanServer.getAttribute(oname,
 				"SessionCount");
 		return count;
 	}
 
 	private String getApplicationName() throws Exception {
 		ObjectName oname = getObjectName();
 		return (String) this.mBeanServer.getAttribute(oname, "ApplicationName");
 	}
 
 	/**
 	 * @return
 	 * @throws MalformedObjectNameException
 	 */
 	private ObjectName getObjectName() throws MalformedObjectNameException {
 		return ObjectName.getInstance("test:name=SessionListener");
 	}
 }
