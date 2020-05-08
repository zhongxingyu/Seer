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
 package com.buschmais.maexo.framework.switchboard.impl;
 
 import javax.management.MBeanServer;
 import javax.management.MBeanServerConnection;
 import javax.management.MBeanServerDelegateMBean;
 import javax.management.MBeanServerInvocationHandler;
 import javax.management.NotificationFilter;
 import javax.management.NotificationListener;
 import javax.management.ObjectName;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * OSGi bundle activator for the switchboard bundle.
  */
 public final class Activator implements BundleActivator {
 
 	/**
 	 * The service property which defines the {@link ObjectName} of an MBean.
 	 */
 	private static final String SERVICEPROPERTY_OBJECTNAME = "objectName";
 
 	/**
 	 * The service property which defines the hand back for a
 	 * {@link NotificationListener}.
 	 */
 	private static final String SERVICEPROPERTY_HANDBACK = "handback";
 
 	/**
 	 * Filter for MBean server connections.
 	 */
 	private static final String FILTER_MBEANSERVERCONNECTION = String.format(
 			"(|(objectClass=%s)(objectClass=%s))", MBeanServerConnection.class
 					.getName(), MBeanServer.class.getName());
 
 	/**
 	 * Filter for MBean servers.
 	 */
 	private static final String FILTER_MBEANSERVER = String.format(
 			"(objectClass=%s)", MBeanServer.class.getName());
 
 	/**
 	 * Filter for MBeans with either a <code>javax.management.ObjectName</code>
 	 * or object name attribute.
 	 */
 	private static final String FILTER_MBEAN = String.format(
 			"(&(objectClass=*MBean)(|(%s=*)(objectName=*)))", ObjectName.class
 					.getName());
 
 	/**
 	 * Filter for notification listeners.
 	 */
 	private static final String FILTER_NOTIFICATIONLISTENER = String.format(
 			"(&(objectClass=%s)(|(%s=*)(objectName=*)))",
 			NotificationListener.class.getName(), ObjectName.class.getName());
 
 	/** The logger. */
 	private static Logger logger = LoggerFactory.getLogger(Activator.class);
 
 	/** The switch board. */
 	private SwitchBoardImpl switchBoard;
 
 	/** Service listener for MBean server connections. */
 	private ServiceListener mbeanServerConnectionServiceListener;
 
 	/** Service listener for MBean server. */
 	private ServiceListener mbeanServerServiceListener;
 
 	/** Service listener for MBeans. */
 	private ServiceListener mbeanServiceListener;
 
 	/** Service listener for notification listeners. */
 	private ServiceListener notificationListenerServiceListener;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void start(BundleContext bundleContext) throws Exception {
 		logger.info("Starting maexo switch board");
 		this.switchBoard = new SwitchBoardImpl();
 		// start the switch board
 		this.switchBoard.start();
 		// register service listeners
 		this.mbeanServerConnectionServiceListener = this
 				.registerMBeanServerConnectionServiceListener(bundleContext);
 		this.mbeanServerServiceListener = this
 				.registerMBeanServerServiceListener(bundleContext);
 		this.mbeanServiceListener = this
 				.registerMBeanServiceListener(bundleContext);
 		this.notificationListenerServiceListener = this
 				.registerNotificationListenerServiceListener(bundleContext);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void stop(BundleContext bundleContext) throws Exception {
 		logger.info("Stopping maexo switch board");
 		// remove service listeners
 		if (this.mbeanServerConnectionServiceListener != null) {
 			bundleContext
 					.removeServiceListener(this.mbeanServerConnectionServiceListener);
 		}
 		if (this.mbeanServerServiceListener != null) {
 			bundleContext
 					.removeServiceListener(this.mbeanServerServiceListener);
 		}
 		if (this.mbeanServiceListener != null) {
 			bundleContext.removeServiceListener(this.mbeanServiceListener);
 		}
 		if (this.notificationListenerServiceListener != null) {
 			bundleContext
 					.removeServiceListener(this.notificationListenerServiceListener);
 		}
 		// stop the switch board
 		this.switchBoard.stop();
 	}
 
 	/**
 	 * Registers a service listener for MBean server connections.
 	 *
 	 * @param bundleContext
 	 *            The bundle context.
 	 * @return The service listener.
 	 * @throws InvalidSyntaxException
 	 *             If the registering fails.
 	 */
 	private ServiceListener registerMBeanServerConnectionServiceListener(
 			final BundleContext bundleContext) throws InvalidSyntaxException {
 		ServiceListener serviceListener = new ServiceListener() {
 
 			/**
 			 * {@inheritDoc}
 			 */
 			public void serviceChanged(ServiceEvent serviceEvent) {
 				ServiceReference serviceReference = serviceEvent
 						.getServiceReference();
 				MBeanServerConnection mbeanServerConnection = (MBeanServerConnection) bundleContext
 						.getService(serviceReference);
 				String agentId = Activator.this
 						.getAgentId(mbeanServerConnection);
 				if (agentId == null) {
 					logger
 							.warn("Cannot get agentId for MBean server connection, skipping (un-)registration.");
 				} else {
 					MBeanServerConnectionRegistration mbeanServerConnectionRegistration = new MBeanServerConnectionRegistration(
 							agentId, mbeanServerConnection);
 					switch (serviceEvent.getType()) {
 					case ServiceEvent.REGISTERED:
 						Activator.this.switchBoard
 								.registerMBeanServerConnection(mbeanServerConnectionRegistration);
 						break;
 					case ServiceEvent.UNREGISTERING:
 						Activator.this.switchBoard
 								.unregisterMBeanServerConnection(mbeanServerConnectionRegistration);
 						break;
 					default:
 						break;
 					}
 				}
 			}
 
 		};
 		bundleContext.addServiceListener(serviceListener,
 				FILTER_MBEANSERVERCONNECTION);
 		// do initial registration of MBeanServerConnections
 		logger
 				.debug("performing initial registration of MBean server connections");
 		this.registerExistingServices(FILTER_MBEANSERVERCONNECTION,
 				bundleContext, serviceListener);
 		return serviceListener;
 	}
 
 	/**
 	 * Registers a service listener for MBean servers.
 	 *
 	 * @param bundleContext
 	 *            The bundle context.
 	 * @return The service listener.
 	 * @throws InvalidSyntaxException
 	 *             If the registering fails.
 	 */
 	private ServiceListener registerMBeanServerServiceListener(
 			final BundleContext bundleContext) throws InvalidSyntaxException {
 		ServiceListener serviceListener = new ServiceListener() {
 
 			/**
 			 * {@inheritDoc}
 			 */
 			public void serviceChanged(ServiceEvent serviceEvent) {
 				ServiceReference serviceReference = serviceEvent
 						.getServiceReference();
 				MBeanServer mbeanServer = (MBeanServer) bundleContext
 						.getService(serviceReference);
				String agentId = Activator.this
						.getAgentId(mbeanServer);
 				if (agentId == null) {
 					logger
 							.warn("Cannot get agentId for MBean server, skipping (un-)registration.");
 				} else {
 					MBeanServerRegistration mBeanServerRegistration = new MBeanServerRegistration(
 							agentId, mbeanServer);
 					switch (serviceEvent.getType()) {
 					case ServiceEvent.REGISTERED:
 						Activator.this.switchBoard
 								.registerMBeanServer(mBeanServerRegistration);
 						break;
 					case ServiceEvent.UNREGISTERING:
 						Activator.this.switchBoard
 								.unregisterMBeanServer(mBeanServerRegistration);
 						break;
 					default:
 						break;
 					}
 				}
 			}
 		};
 		bundleContext.addServiceListener(serviceListener, FILTER_MBEANSERVER);
 		// do initial registration of MBeanServers
 		logger.debug("performing initial registration of MBean servers");
 		this.registerExistingServices(FILTER_MBEANSERVER, bundleContext,
 				serviceListener);
 		return serviceListener;
 	}
 
 	/**
 	 * Registers a service listener for MBeans.
 	 *
 	 * @param bundleContext
 	 *            The bundle context.
 	 * @return The service listener.
 	 * @throws InvalidSyntaxException
 	 *             If the registering fails.
 	 */
 	private ServiceListener registerMBeanServiceListener(
 			final BundleContext bundleContext) throws InvalidSyntaxException {
 		ServiceListener serviceListener = new ServiceListener() {
 
 			/**
 			 * {@inheritDoc}
 			 */
 			public void serviceChanged(ServiceEvent serviceEvent) {
 				ServiceReference serviceReference = serviceEvent
 						.getServiceReference();
 				Object mbean = bundleContext.getService(serviceReference);
				ObjectName objectName = Activator.this.getObjectName(
						serviceReference, bundleContext);
 				if (objectName == null) {
 					Activator.logger
 							.warn("cannot get object name from service reference, skipping (un-)registration of the MBean");
 				} else {
 					MBeanRegistration mbeanRegistration = new MBeanRegistration(
 							objectName, mbean);
 					if (mbeanRegistration != null) {
 						switch (serviceEvent.getType()) {
 						case ServiceEvent.REGISTERED:
 							Activator.this.switchBoard
 									.registerMBean(mbeanRegistration);
 							break;
 						case ServiceEvent.UNREGISTERING:
 							Activator.this.switchBoard
 									.unregisterMBean(mbeanRegistration);
 							break;
 						default:
 							break;
 						}
 					}
 				}
 			}
 
 		};
 		bundleContext.addServiceListener(serviceListener, FILTER_MBEAN);
 		// do initial registration of MBeans
 		logger.debug("performing initial registration of MBeans");
 		this.registerExistingServices(FILTER_MBEAN, bundleContext,
 				serviceListener);
 		return serviceListener;
 	}
 
 	/**
 	 * Registers a service listener for notification listeners.
 	 *
 	 * @param bundleContext
 	 *            The bundle context.
 	 * @return The service listener.
 	 * @throws InvalidSyntaxException
 	 *             If the registering fails.
 	 */
 	private ServiceListener registerNotificationListenerServiceListener(
 			final BundleContext bundleContext) throws InvalidSyntaxException {
 		ServiceListener serviceListener = new ServiceListener() {
 
 			/**
 			 * {@inheritDoc}
 			 */
 			public void serviceChanged(ServiceEvent serviceEvent) {
 				ServiceReference serviceReference = serviceEvent
 						.getServiceReference();
 				NotificationListener notificationListener = (NotificationListener) bundleContext
 						.getService(serviceReference);
				ObjectName objectName = Activator.this.getObjectName(
						serviceReference, bundleContext);
 				NotificationFilter notificationFilter = (NotificationFilter) serviceReference
 						.getProperty(NotificationFilter.class.getName());
 				Object handback = serviceReference
 						.getProperty(SERVICEPROPERTY_HANDBACK);
 				if (objectName == null) {
 					Activator.logger
 							.warn("cannot get object name from service reference, skipping (un-)registration of the notification listener");
 				} else {
 					NotificationListenerRegistration notificationListenerRegistration = new NotificationListenerRegistration(
 							notificationListener, objectName,
 							notificationFilter, handback);
 					if (notificationListenerRegistration != null) {
 						switch (serviceEvent.getType()) {
 						case ServiceEvent.REGISTERED:
 							Activator.this.switchBoard
 									.addNotificationListener(notificationListenerRegistration);
 							break;
 						case ServiceEvent.UNREGISTERING:
 							Activator.this.switchBoard
 									.removeNotificationListener(notificationListenerRegistration);
 							break;
 						default:
 							break;
 						}
 					}
 				}
 			}
 		};
 		bundleContext.addServiceListener(serviceListener,
 				FILTER_NOTIFICATIONLISTENER);
 		// do initial registration of MBeans
 		logger
 				.debug("performing initial registration of notification listeners");
 		this.registerExistingServices(FILTER_NOTIFICATIONLISTENER,
 				bundleContext, serviceListener);
 		return serviceListener;
 	}
 
 	/**
 	 * Registers already existing services.
 	 *
 	 * @param filter
 	 *            The filter for the services to match.
 	 * @param bundleContext
 	 *            The bundle context.
 	 * @param serviceListener
 	 *            The service listener to effect the registration.
 	 * @throws InvalidSyntaxException
 	 *             If the filter has a syntactical error.
 	 */
 	private void registerExistingServices(String filter,
 			BundleContext bundleContext, ServiceListener serviceListener)
 			throws InvalidSyntaxException {
 		ServiceReference[] serviceReferences = bundleContext
 				.getServiceReferences(null, filter);
 		if (serviceReferences != null) {
 			for (ServiceReference serviceReference : serviceReferences) {
 				if (serviceReference != null) {
 					serviceListener.serviceChanged(new ServiceEvent(
 							ServiceEvent.REGISTERED, serviceReference));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Constructs an {@link ObjectName} from the properties of a service
 	 * reference.
 	 *
 	 * @param serviceReference
 	 *            The service reference.
	 * @param bundleContext
	 *            The bundle context.
 	 * @return The {@link ObjectName} instance or <code>null</code> if the
 	 *         properties are not set or an error occurred while construction
 	 *         the instance.
 	 */
	private ObjectName getObjectName(ServiceReference serviceReference,
			BundleContext bundleContext) {
 		// get object name from service properties
 		ObjectName objectName = (ObjectName) serviceReference
 				.getProperty(ObjectName.class.getName());
 		if (objectName != null) {
 			return objectName;
 		}
 		Object objectNameProperty = serviceReference
 				.getProperty(SERVICEPROPERTY_OBJECTNAME);
 		if (objectNameProperty != null) {
 			try {
 				objectName = new ObjectName((objectNameProperty.toString()));
 			} catch (Exception e) {
 				Activator.logger
 						.debug(String
 								.format(
 										"cannot construct object name from service property %s",
 										objectNameProperty.toString(), e));
 			}
 		}
 		return objectName;
 	}
 
 	/**
 	 * Returns the agentId of a MBean server.
 	 *
 	 * @param mbeanServerConnection
 	 *            The MBean server connection
 	 * @return The agentId or <code>null</code>.
 	 */
 	private String getAgentId(MBeanServerConnection mbeanServerConnection) {
 		ObjectName delegateObjectName = null;
 		try {
 			delegateObjectName = new ObjectName(
 					"JMImplementation:type=MBeanServerDelegate");
 			MBeanServerDelegateMBean mbeanServerDelegateMBean = (MBeanServerDelegateMBean) MBeanServerInvocationHandler
 					.newProxyInstance(mbeanServerConnection,
 							delegateObjectName, MBeanServerDelegateMBean.class,
 							false);
 			return mbeanServerDelegateMBean.getMBeanServerId();
 		} catch (Exception e) {
 			logger
 					.debug(
 							"cannot construct object name for mbean server delegate",
 							e);
 		}
 		return null;
 	}
 }
