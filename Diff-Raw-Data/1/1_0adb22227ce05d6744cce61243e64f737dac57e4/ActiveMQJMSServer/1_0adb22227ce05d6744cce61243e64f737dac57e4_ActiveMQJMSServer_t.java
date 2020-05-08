 /****************************************************************************
  * Copyright (c) 2004 Composent, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Composent, Inc. - initial API and implementation
  *****************************************************************************/
 
 package org.eclipse.ecf.provider.jms.activemq.application;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSServerContainer;
 import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;
 import org.eclipse.ecf.provider.jms.identity.JMSID;
 import org.eclipse.ecf.provider.jms.identity.JMSNamespace;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 
 /**
  * ActiveMQ JMS Server Application.
  */
 public class ActiveMQJMSServer implements IApplication {
 
 	private ActiveMQJMSServerContainer serverContainer = null;
 
 	private String[] mungeArguments(String originalArgs[]) {
 		if (originalArgs == null)
 			return new String[0];
 		final List l = new ArrayList();
 		for (int i = 0; i < originalArgs.length; i++)
 			if (!originalArgs[i].equals("-pdelaunch")) //$NON-NLS-1$
 				l.add(originalArgs[i]);
 		return (String[]) l.toArray(new String[] {});
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
 	 */
 	public Object start(IApplicationContext context) throws Exception {
 		final String[] args = mungeArguments((String[]) context.getArguments().get("application.args")); //$NON-NLS-1$
 		if (args.length < 1) {
 			usage();
 			return IApplication.EXIT_OK;
 		} else {
 			// Create/run ActiveMQ server
 			// Create server ID
 			final JMSID serverID = (JMSID) IDFactory.getDefault().createID(IDFactory.getDefault().getNamespaceByName(JMSNamespace.NAME), args[0]);
 			// Create config
 			final JMSContainerConfig config = new JMSContainerConfig(serverID, ActiveMQJMSServerContainer.DEFAULT_KEEPALIVE);
 
 			synchronized (this) {
 				serverContainer = new ActiveMQJMSServerContainer(config);
 				serverContainer.start();
 				// Wait until stopped
 				this.wait();
 			}
 			return IApplication.EXIT_OK;
 
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.equinox.app.IApplication#stop()
 	 */
 	public void stop() {
 		synchronized (this) {
 			if (serverContainer != null) {
 				serverContainer.dispose();
 				serverContainer = null;
 				this.notifyAll();
 			}
 		}
 	}
 
 	private void usage() {
 		System.out.println("Usage: eclipse.exe -application " //$NON-NLS-1$
 				+ this.getClass().getName() + "<jmsprotocol>://<jmsserver>:<jmsport>/<jmstopic>"); //$NON-NLS-1$
 		System.out.println("   Examples: eclipse -application org.eclipse.ecf.provider.jms.ActiveMQJMSServer tcp://localhost:61616/exampleTopic"); //$NON-NLS-1$
 	}
 
 }
