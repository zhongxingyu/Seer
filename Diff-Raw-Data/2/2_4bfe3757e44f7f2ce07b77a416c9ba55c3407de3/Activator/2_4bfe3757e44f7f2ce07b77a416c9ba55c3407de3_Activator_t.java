 /*
  * Copyright 2008 buschmais GbR
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.
  * See the License for the specific language governing permissions and
  * limitations under the License
  */
 package com.buschmais.osgi.maexo.server.platform.impl;
 
 import java.lang.management.ManagementFactory;
 
 import javax.management.MBeanServer;
 import javax.management.MBeanServerConnection;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * OSGi bundle activator for the the platform mbeans server bundle.
  */
 public final class Activator implements BundleActivator {
 
	private static final Logger logger = LoggerFactory.getLogger(Activator.class);
 
 	/**
 	 * Represents the platform mbean server.
 	 */
 	private ServiceRegistration mbeanServerRegistration;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void start(BundleContext bundleContext) throws Exception {
 		logger.info("Starting maexo Platform MBean Server");
 		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
 		logger.debug("registering instance {} as service", mbeanServer);
 		this.mbeanServerRegistration = bundleContext.registerService(
 				new String[] { MBeanServer.class.getName(),
 						MBeanServerConnection.class.getName() }, mbeanServer,
 				null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void stop(BundleContext bundleContext) throws Exception {
 		logger.info("Stopping maexo Platform MBean Server");
 		this.mbeanServerRegistration.unregister();
 	}
 
 }
