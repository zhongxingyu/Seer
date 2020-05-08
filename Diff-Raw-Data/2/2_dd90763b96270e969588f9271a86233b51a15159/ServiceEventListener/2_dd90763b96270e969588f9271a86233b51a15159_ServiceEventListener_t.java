 /**
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
 package com.buschmais.osgi.maexo.mbeans.osgi.core.impl.listener;
 
 import javax.management.DynamicMBean;
 import javax.management.ObjectName;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 
 import com.buschmais.osgi.maexo.mbeans.osgi.core.ServiceMBean;
 
 public class ServiceEventListener extends EventListener implements
 		ServiceListener {
 
 	private Bundle bundle;
 
 	public ServiceEventListener(BundleContext bundleContext) {
 		super(bundleContext);
 		this.bundle = bundleContext.getBundle();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.
 	 * ServiceEvent)
 	 */
 	public synchronized void serviceChanged(ServiceEvent serviceEvent) {
 		ServiceReference serviceReference = serviceEvent.getServiceReference();
 		// do not process (mbean) services which have been registered by this
 		// bundle
		if (!this.bundle.equals(serviceReference.getBundle())) {
 			Long id = (Long) serviceReference.getProperty(Constants.SERVICE_ID);
 			switch (serviceEvent.getType()) {
 			case ServiceEvent.REGISTERED: {
 				ObjectName objectName = super
 						.getObjectNameHelper()
 						.getObjectName(serviceReference, ServiceReference.class);
 				super.registerMBeanService(DynamicMBean.class, objectName, id,
 						new ServiceMBean(serviceReference, super
 								.getObjectNameHelper()));
 			}
 				break;
 			case ServiceEvent.UNREGISTERING: {
 				super.unregisterMBeanService(id);
 			}
 				break;
 			}
 		}
 	}
 
 }
