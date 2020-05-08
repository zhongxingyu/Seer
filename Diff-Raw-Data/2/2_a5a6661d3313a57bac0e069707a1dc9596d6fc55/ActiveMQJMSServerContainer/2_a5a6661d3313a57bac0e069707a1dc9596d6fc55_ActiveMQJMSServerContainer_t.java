 /*******************************************************************************
  * Copyright (c) 2004, 2007 Composent, Inc. and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Composent, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.ecf.provider.jms.activemq.container;
 
 import org.eclipse.ecf.core.util.ECFException;
 import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
 import org.eclipse.ecf.provider.jms.container.AbstractJMSServer;
 import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;
 
 public class ActiveMQJMSServerContainer extends AbstractJMSServer {
 
 	public static final String PASSWORD_PROPERTY = "password";
 	public static final String USERNAME_PROPERTY = "username";
 	public static final String DEFAULT_PASSWORD = "defaultPassword";
 	public static final String DEFAULT_USERNAME = "defaultUsername";
 
 	public static final String DEFAULT_SERVER_ID = "tcp://localhost:61616/exampleTopic";
 	
 	public ActiveMQJMSServerContainer(JMSContainerConfig config) {
 		super(config);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.provider.jms.container.AbstractJMSServer#start()
 	 */
 	public void start() throws ECFException {
 		JMSContainerConfig config = (JMSContainerConfig) getConfig();
 		final String username = (String) config.getProperties().get(
 				ActiveMQJMSServerContainer.USERNAME_PROPERTY);
 		final String password = (String) config.getProperties().get(
 				ActiveMQJMSServerContainer.PASSWORD_PROPERTY);
 		final ISynchAsynchConnection connection = new ActiveMQServerChannel(
 				this.getReceiver(), config.getKeepAlive(), username, password);
 		setConnection(connection);
 		connection.start();
 	}
 
 	public void dispose() {
 		super.dispose();
 		ISynchAsynchConnection connection = getConnection();
 		if (connection != null) {
 			connection.disconnect();
			//setConnection(null);
 		}
 	}
 
 }
