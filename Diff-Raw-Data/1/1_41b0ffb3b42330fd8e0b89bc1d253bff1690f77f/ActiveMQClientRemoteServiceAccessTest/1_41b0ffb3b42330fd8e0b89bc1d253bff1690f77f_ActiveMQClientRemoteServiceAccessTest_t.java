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
 import org.eclipse.ecf.tests.provider.jms.BrokerUtil;
 import org.eclipse.ecf.tests.provider.jms.activemq.ActiveMQ;
 
 
 public class ActiveMQClientRemoteServiceAccessTest extends AbstractRemoteServiceAccessTest {
 
 	private void setupBroker() throws Exception {
 		broker = new BrokerUtil(getContainerManager());
 	}
 
 	private BrokerUtil broker;
 	
 	private void tearDownBroker() throws Exception {
 		if (broker != null) {
 			broker.dispose();
 			broker = null;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		setupBroker();
 		super.setUp();
 		setClientCount(2);
 		createServerAndClients();
 		connectClients();
 		setupRemoteServiceAdapters();
 	}
 
 	
 	protected void tearDown() throws Exception {
 		cleanUpServerAndClients();
 		super.tearDown();
 		tearDownBroker();
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
		props.put("ecf.endpoint.connecttarget.id", getClient(0).getConnectedID().getName());
 		props.put(SERVICE_EXPORTED_INTERFACES,SERVICE_EXPORTED_INTERFACES_WILDCARD);
 		return props;
 	}
 }
