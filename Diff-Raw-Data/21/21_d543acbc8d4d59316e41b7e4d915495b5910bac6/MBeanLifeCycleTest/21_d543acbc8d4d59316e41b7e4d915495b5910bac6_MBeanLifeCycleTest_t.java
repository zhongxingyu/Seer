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
 package com.buschmais.osgi.maexo.test.framework.commons.mbean;
 
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 
 import org.easymock.EasyMock;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.util.tracker.ServiceTracker;
 
 import com.buschmais.osgi.maexo.framework.commons.mbean.lifecycle.DefaultServiceMBeanLifeCycleSupport;
 import com.buschmais.osgi.maexo.framework.commons.mbean.lifecycle.MBeanLifecycleSupport;
 import com.buschmais.osgi.maexo.framework.commons.mbean.lifecycle.ServiceMBeanLifeCycleSupport;
 import com.buschmais.osgi.maexo.framework.commons.mbean.objectname.ObjectNameFactory;
 import com.buschmais.osgi.maexo.test.Constants;
 import com.buschmais.osgi.maexo.test.MaexoTests;
 import com.buschmais.osgi.maexo.test.common.mbeans.Standard;
 import com.buschmais.osgi.maexo.test.common.mbeans.StandardMBean;
 
 /**
  * @see MaexoTests
  */
 public class MBeanLifeCycleTest extends MaexoTests {
 
 	/**
 	 * The object name to use for publishing MBeans.
 	 */
 	private static final String OBJECTNAME_STANDARDMBEAN = "com.buschmais.osgi.maexo:type=StandardMBean";
 
 	/**
 	 * The default timeout to use for the OSGI service tracker.
 	 */
 	private static final long TIMEOUT_SERVICETRACKER = 1000;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.springframework.osgi.test.AbstractDependencyManagerTests#
 	 * getTestBundlesNames()
 	 */
 	@Override
 	protected String[] getTestBundlesNames() {
 		return new String[] { Constants.ARTIFACT_COMMONS_MBEAN,
 				Constants.ARTIFACT_EASYMOCK };
 	}
 
 	/**
 	 * Test if registering/unregistering an MBean using the class
 	 * {@link MBeanLifecycleSupport} is reflected via an appropriate service in
 	 * the OSGi service registry
 	 * 
 	 * @throws MalformedObjectNameException
 	 * @throws NullPointerException
 	 * @throws InterruptedException
 	 */
 	public void test_mbeanLifeCycleSupport()
 			throws MalformedObjectNameException, NullPointerException,
 			InterruptedException {
 		ServiceTracker serviceTracker = new ServiceTracker(super.bundleContext,
 				StandardMBean.class.getName(), null);
 		serviceTracker.open();
 		// initially no StandardMBean must be registered
 		assertNull(serviceTracker.getService());
 		MBeanLifecycleSupport mbeanLifecycleSupport = new MBeanLifecycleSupport(
 				super.bundleContext) {
 		};
 		StandardMBean standardMBean = new Standard();
 		ObjectName objectName = new ObjectName(OBJECTNAME_STANDARDMBEAN);
 		// register a StandardMBean using the MBeanLifeCycleSupport
 		mbeanLifecycleSupport.registerMBeanService(StandardMBean.class,
 				objectName, standardMBean);
 		StandardMBean mbeanFromServiceRegistry = (StandardMBean) serviceTracker
 				.waitForService(TIMEOUT_SERVICETRACKER);
 		assertNotNull(mbeanFromServiceRegistry);
 		// unregister the StandardMBean using the MBeanLifeCycleSupport
 		mbeanLifecycleSupport.unregisterMBeanService(new ObjectName(
 				OBJECTNAME_STANDARDMBEAN));
 		assertNull(serviceTracker.getService());
 		serviceTracker.close();
 	}
 
 	/**
 	 * Test if registering/unregistering a service is reflected via appropriate
 	 * MBean services after starting an instance of the class
 	 * {@link ServiceMBeanLifeCycleSupport}.
 	 * <p>
 	 * Note: The instance of {@link ServiceMBeanLifeCycleSupport} will be
 	 * started before service registration and stopped after service
 	 * unregistration. This is reverse to the behavior in the test method
 	 * {@link #test_defaultServiceMBeanLifeCycleSupport()}.
 	 * 
 	 * @throws MalformedObjectNameException
 	 * @throws NullPointerException
 	 * @throws InterruptedException
 	 */
 	public void test_serviceMBeanLifeCycleSupport() throws InterruptedException {
 		ServiceTracker serviceTracker = new ServiceTracker(super.bundleContext,
 				StandardMBean.class.getName(), null);
 		serviceTracker.open();
 		// initially no StandardMBean must be registered
 		assertNull(serviceTracker.getService());
 		// create a ServiceMBeanLifeCycleSupport instance which tracks services
 		// of the specified interface using the service filter
 		ServiceMBeanLifeCycleSupport serviceMbeanLifecycleSupport = new ServiceMBeanLifeCycleSupport(
 				super.bundleContext) {
 
 			/**
 			 * {@inheritDoc}
 			 */
 			@Override
 			public Object getMBean(ServiceReference serviceReference,
 					Object service) {
 				return new Standard();
 			}
 
 			/**
 			 * {@inheritDoc}
 			 */
 			@Override
 			public Class<?> getMBeanInterface() {
 				return StandardMBean.class;
 			}
 
 			/**
 			 * {@inheritDoc}
 			 */
 			@Override
 			public ObjectName getObjectName(ServiceReference serviceReference,
 					Object service) {
 				try {
 					return new ObjectName(OBJECTNAME_STANDARDMBEAN);
 				} catch (Exception e) {
 					throw new IllegalStateException(e);
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 */
 			@Override
 			public String getServiceFilter() {
 				return "(name=validService)";
 			}
 
 			/**
 			 * {@inheritDoc}
 			 */
 			@Override
 			public Class<?> getServiceInterface() {
 				return ResourceInterfaceA.class;
 			}
 		};
 		serviceMbeanLifecycleSupport.start();
 		assertNull(serviceTracker.getService());
 		// register a service using the correct service interface and a matching
 		// attribute
 		Dictionary<String, Object> properties = new Hashtable<String, Object>();
 		properties.put("name", "validService");
 		ResourceInterfaceA resourceA = EasyMock
 				.createMock(ResourceInterfaceA.class);
 		ServiceRegistration serviceRegistrationA = super.bundleContext
 				.registerService(ResourceInterfaceA.class.getName(), resourceA,
 						properties);
 		StandardMBean mbeanFromServiceRegistry = (StandardMBean) serviceTracker
 				.waitForService(TIMEOUT_SERVICETRACKER);
 		assertNotNull(mbeanFromServiceRegistry);
 		serviceRegistrationA.unregister();
 		assertNull(serviceTracker.getService());
 		// register a service with another service interface and a valid
 		// attribute
 		ResourceInterfaceB resourceB = EasyMock
 				.createMock(ResourceInterfaceB.class);
 		ServiceRegistration serviceRegistrationB = super.bundleContext
 				.registerService(ResourceInterfaceB.class.getName(), resourceB,
 						properties);
 		assertNull(serviceTracker.waitForService(TIMEOUT_SERVICETRACKER));
 		serviceRegistrationB.unregister();
 		// register a service using the correct interface but no matching
 		// attribute
 		serviceRegistrationA = super.bundleContext.registerService(
 				ResourceInterfaceA.class.getName(), resourceA, null);
 		assertNull(serviceTracker.waitForService(TIMEOUT_SERVICETRACKER));
 		serviceRegistrationA.unregister();
 		serviceMbeanLifecycleSupport.stop();
 		serviceTracker.close();
 	}
 
 	/**
 	 * Test if an instance of {@link DefaultServiceMBeanLifeCycleSupport} makes
 	 * use of the registered {@link ObjectNameFactory}.
 	 * <p>
 	 * Note: The instance of {@link ServiceMBeanLifeCycleSupport} will be
 	 * started after service registration and stopped before service
 	 * unregistration. This is reverse to the behavior in the test method
 	 * {@link #test_serviceMBeanLifeCycleSupport()}.
 	 * 
 	 * @throws NullPointerException
 	 * @throws MalformedObjectNameException
 	 * @throws InterruptedException
 	 * 
 	 */
 	public void test_defaultServiceMBeanLifeCycleSupport()
 			throws MalformedObjectNameException, NullPointerException,
 			InterruptedException {
 		ServiceTracker serviceTracker = new ServiceTracker(super.bundleContext,
 				StandardMBean.class.getName(), null);
 		serviceTracker.open();
 		ResourceInterfaceA resourceA = EasyMock
 				.createMock(ResourceInterfaceA.class);
 		ServiceRegistration serviceRegistrationA = super.bundleContext
 				.registerService(ResourceInterfaceA.class.getName(), resourceA,
 						null);
 		// create the map of properties which are expected to be passed to the
 		// object name factory
 		Map<String, Object> expectedProperties = new HashMap<String, Object>();
		for (String expectedProperty : DefaultServiceMBeanLifeCycleSupport.OBJECTNAME_PROPERTIES) {
			expectedProperties.put(expectedProperty, serviceRegistrationA
					.getReference().getProperty(expectedProperty));
		}
 		ObjectNameFactory objectNameFactory = EasyMock
 				.createMock(ObjectNameFactory.class);
 		// the object name factory will be used on registration and
 		// unregistration of the service, thus expect two calls
 		EasyMock.expect(
 				objectNameFactory.getObjectName(resourceA, expectedProperties))
 				.andReturn(new ObjectName(OBJECTNAME_STANDARDMBEAN)).times(2);
 		// create the instance of DefaultServicMBeanLifeCycleSupport
 		DefaultServiceMBeanLifeCycleSupport defaultServiceMBeanLifeCycleSupport = new DefaultServiceMBeanLifeCycleSupport(
 				super.bundleContext) {
 
 			/**
 			 * {@inheritDoc}
 			 */
 			@Override
 			public Object getMBean(ServiceReference serviceReference,
 					Object service) {
 				return new Standard();
 			}
 
 			/**
 			 * {@inheritDoc}
 			 */
 			@Override
 			public Class<?> getMBeanInterface() {
 				return StandardMBean.class;
 			}
 
 			/**
 			 * {@inheritDoc}
 			 */
 			@Override
 			public Class<?> getServiceInterface() {
 				return ResourceInterfaceA.class;
 			}
 
 		};
 		// register the object name factory
 		Dictionary<String, Object> properties = new Hashtable<String, Object>();
 		properties
 				.put(
 						com.buschmais.osgi.maexo.framework.commons.mbean.objectname.Constants.SERVICE_PROPERTY_RESOURCEINTERFACE,
 						ResourceInterfaceA.class.getName());
 		ServiceRegistration serviceRegistrationObjectNameFactory = super.bundleContext
 				.registerService(ObjectNameFactory.class.getName(),
 						objectNameFactory, properties);
 		// start test
 		EasyMock.replay(objectNameFactory);
 		assertNull(serviceTracker.getService());
 		// start the DefaultServiceMBeanLifeCycleSupport
 		defaultServiceMBeanLifeCycleSupport.start();
 		assertNotNull(serviceTracker.waitForService(TIMEOUT_SERVICETRACKER));
 		// stop the DefaultServiceMBeanLifeCycleSupport
 		defaultServiceMBeanLifeCycleSupport.stop();
 		// unregister services
 		serviceRegistrationObjectNameFactory.unregister();
 		serviceRegistrationA.unregister();
 		serviceTracker.close();
 		// verify the mock of the object name factory
 		EasyMock.verify(objectNameFactory);
 	}
 }
