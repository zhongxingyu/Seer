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
 
 package org.eclipse.ecf.tests.provider.jms.activemq;
 
 
 /**
  *
  */
 public interface ActiveMQ {
 
 	public static final String CLIENT_CONTAINER_NAME = "ecf.jms.activemq.tcp.client";
 	public static final String SERVER_CONTAINER_NAME = "ecf.jms.activemq.tcp.manager";
	public static final String TARGET_NAME = System.getProperty("org.eclipse.ecf.tests.provider.jms.activemq", "tcp://localhost:61616/exampleTopic");
 	public static final String NAMESPACE_NAME = "ecf.namespace.jmsid";
 }
