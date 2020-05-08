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
  *     Bob Nettleton (Oracle) - Initial Reference Implementation
  ******************************************************************************/  
 
 package org.eclipse.gemini.naming;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.ServiceFactory;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.framework.SynchronousBundleListener;
 
 class ContextManagerServiceFactoryImpl implements ServiceFactory {
 
 	// map of bundles to context managers (CloseableContextManager)
 	private Map m_mapOfManagers = 
 		Collections.synchronizedMap(new HashMap());
 	
 	/* BundleContext for the Gemini Naming Implementation Bundle */
 	private final BundleContext m_implBundleContext;
 	
 	ContextManagerServiceFactoryImpl(BundleContext implBundleContext) {
 		m_implBundleContext = implBundleContext;
 	}
 	
 	public Object getService(Bundle bundle, ServiceRegistration registration) {
 		CloseableContextManager contextManager = 
 			createContextManager(bundle, m_implBundleContext);
 		m_mapOfManagers.put(bundle, contextManager);
 		bundle.getBundleContext().addBundleListener(new ContextManagerBundleListener());
 		return contextManager;
 	}
 
 	
 
 	public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
 		closeContextManager(bundle);
 	}
 	
 	protected void closeAll() {
 		synchronized(m_mapOfManagers) {
			Iterator iterator = m_mapOfManagers.entrySet().iterator();
 			while(iterator.hasNext()) {
				Map.Entry currentMapEntry = (Map.Entry) iterator.next();
				Object currentMapEntryValue = currentMapEntry.getValue();
				if (currentMapEntryValue != null){
					((CloseableContextManager)currentMapEntryValue).close();
					iterator.remove();
				}
 			}
 		}
 	}
 
 	private void closeContextManager(Bundle bundle) {
 		CloseableContextManager contextManager = 
 			(CloseableContextManager)m_mapOfManagers.get(bundle);
 		if(contextManager != null) {
 			contextManager.close();
 			m_mapOfManagers.remove(bundle);
 		}
 	}
 	
 	
 	private class ContextManagerBundleListener implements SynchronousBundleListener {
 		public void bundleChanged(BundleEvent event) {
 			if(event.getType() == BundleEvent.STOPPED) {
 				if(m_mapOfManagers.containsKey(event.getBundle())) {
 					closeContextManager(event.getBundle());
 				}
 			}
 		}
 		
 	}
 	
 	
 	/**
 	 * Convenience factory method for creating a CloseableContextManager
 	 * instance.  
 	 * @param bundle the Bundle associated with this context manager
 	 * @return a CloseableContextManager that will handle requests for 
 	 *         the given Bundle.  
 	 */
 	private static CloseableContextManager createContextManager(Bundle bundle, BundleContext implBundleContext) {
 		return new SecurityAwareContextManagerImpl(new ContextManagerImpl(bundle, implBundleContext));
 	}
 }
