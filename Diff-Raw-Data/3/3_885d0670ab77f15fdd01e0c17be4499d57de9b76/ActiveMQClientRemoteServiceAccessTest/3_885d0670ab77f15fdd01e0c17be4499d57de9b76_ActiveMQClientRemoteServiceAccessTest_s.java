 /*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
 package org.eclipse.ecf.tests.provider.jms.activemq.remoteservice;
 
 import java.util.Properties;
 
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.tests.osgi.services.distribution.AbstractRemoteServiceAccessTest;
 import org.eclipse.ecf.tests.provider.jms.activemq.ActiveMQ;
 
 
 public class ActiveMQClientRemoteServiceAccessTest extends AbstractRemoteServiceAccessTest {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		setClientCount(2);
 		createServerAndClients();
 		connectClients();
 		setupRemoteServiceAdapters();
 	}
 
 	
 	protected void tearDown() throws Exception {
 		cleanUpServerAndClients();
 		super.tearDown();
 	}
 
 	protected String getServerContainerName() {
 		return ActiveMQ.SERVER_CONTAINER_NAME;
 	}
 
 	protected String getClientContainerName() {
 		return ActiveMQ.CLIENT_CONTAINER_NAME;
 	}
 
 	protected ID createServerID() throws Exception {
 		return IDFactory.getDefault().createID(ActiveMQ.NAMESPACE_NAME, ActiveMQ.TARGET_NAME);
 	}
 	
 	protected String getServerIdentity() {
 		return ActiveMQ.TARGET_NAME;
 	}
 	
 	protected Properties getServiceProperties() {
 		Properties props = new Properties();
 		props.put(SERVICE_EXPORTED_CONFIGS, getClientContainerName());
 		props.put(SERVICE_EXPORTED_CONTAINER_ID, getClient(0).getID());
		props.put(SERVICE_EXPORTED_INTERFACES,
				new String[] { SERVICE_EXPORTED_INTERFACES_WILDCARD });
 		return props;
 	}
 }
