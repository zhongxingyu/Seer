 /*******************************************************************************
  * Copyright (c) 2010 Oracle.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * and Apache License v2.0 which accompanies this distribution. 
  * The Eclipse Public License is available at
  *     http://www.eclipse.org/legal/epl-v10.html
  * and the Apache License v2.0 is available at 
  *     http://www.opensource.org/licenses/apache2.0.php.
  * You may elect to redistribute this code under either of these licenses.
  *
  * Contributors:
  *     Bob Nettleton - Initial Developer tests for Reference Implementation
  ******************************************************************************/
 
 
 /**
  * This class is a common base class for the developer tests for the Gemini 
  * Naming project.  
  */
 
 package org.eclipse.gemini.naming.test;
 
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;
 
 public abstract class NamingTestCase extends AbstractConfigurableBundleCreatorTests {
 
 	private static String VERSION = "1.0.0-SNAPSHOT";
 	
 	private Map m_mapOfServicesToRegistrations = 
 		new HashMap();
 
 	protected void onSetUp() throws Exception {
 		m_mapOfServicesToRegistrations = new HashMap();
 	}
 	
 	protected void onTearDown() throws Exception {
 		super.onTearDown();
         unregisterAllServices();
 	}
 	
 	
 	/**
 	 * Override getTestFrameworkBundlesNames() in order to modify the
 	 * dynamic dependency on Apache Log4J.  The Spring OSGi Test framework
 	 * relies by default on a SNAPSHOT version of Log4J.  
 	 * 
 	 * This method should be considered a workaround for this issue.  If future
 	 * versions of Spring OSGi Test use a later version of Log4J, then this 
 	 * method can/should be removed.  
 	 */
 	protected String[] getTestFrameworkBundlesNames() {
 		String[] originalBundleNames = super.getTestFrameworkBundlesNames();
 		
 		for(int i = 0; i < originalBundleNames.length; i++) {
 			if(originalBundleNames[i].startsWith("org.springframework.osgi,log4j.osgi")) {
 				// set framework to use apache log4j dependency instead
 				String newBundleName = 
 					"org.apache.log4j,com.springsource.org.apache.log4j,1.2.15";
 				originalBundleNames[i] = newBundleName;
 			}
 		}
 		
 		return originalBundleNames;
 	}
 	
 	
 	/**
 	 * Declaratively specify the dependency on the Gemini Naming bundle.  
 	 */
 	protected String[] getTestBundlesNames() {
        return new String[]{ "org.eclipse.gemini.naming, org.eclipse.gemini.naming," + VERSION };
     }
 
 
 	protected void registerService(String serviceType, Object service, Dictionary properties) {
     	ServiceRegistration registration = 
     		bundleContext.registerService(serviceType, service, properties);
     	m_mapOfServicesToRegistrations.put(service, registration);
     	
     }
     
     protected void unregisterService(Object service) {
     	if(m_mapOfServicesToRegistrations.containsKey(service)) {
     		ServiceRegistration registration = 
     			(ServiceRegistration)m_mapOfServicesToRegistrations.get(service);
     		registration.unregister();
     	}
     }
     
     protected void unregisterAllServices() {
     	Set keySet = m_mapOfServicesToRegistrations.keySet();
     	Iterator iterator = keySet.iterator();
     	while(iterator.hasNext()) {
     		unregisterService(iterator.next());
     	}
     }
     
     protected BundleContext getContext() {
    	  	return bundleContext;
     }
 	
 }
