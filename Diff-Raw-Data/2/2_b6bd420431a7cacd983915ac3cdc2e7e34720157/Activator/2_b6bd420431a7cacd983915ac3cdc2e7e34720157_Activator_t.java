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
 package com.buschmais.osgi.maexo.test.testbundle.impl;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 
 import com.buschmais.osgi.maexo.test.testbundle.TestInterface;
 
 /**
  * Implementation of a bundle that simulates common behavior like registration
  * and import of services.
  * <p>
  * This bundle may be used in test implementations to verify metadata and
  * execute lifecycle operations (e.g. start/stop/uninstall) without destroying
  * an existing OSGi container setup.
  */
 public final class Activator implements BundleActivator {
 
 	/**
 	 * The registration of the test service.
 	 */
 	private ServiceRegistration testServiceRegistration;
 
 	/**
 	 * The imported test service reference.
 	 */
 	private ServiceReference serviceReference;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void start(BundleContext bundleContext) throws Exception {
 		TestInterface testInterface = new TestClass();
 		// register an instance of the test service
 		this.testServiceRegistration = bundleContext.registerService(
 				TestInterface.class.getName(), testInterface, null);
 		// import the service
 		this.serviceReference = bundleContext
 				.getServiceReference(TestInterface.class.getName());
		bundleContext.getService(this.serviceReference);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void stop(BundleContext bundleContext) throws Exception {
 		// unget the service
 		bundleContext.ungetService(this.serviceReference);
 		// unregister the test service
 		this.testServiceRegistration.unregister();
 	}
 
 }
