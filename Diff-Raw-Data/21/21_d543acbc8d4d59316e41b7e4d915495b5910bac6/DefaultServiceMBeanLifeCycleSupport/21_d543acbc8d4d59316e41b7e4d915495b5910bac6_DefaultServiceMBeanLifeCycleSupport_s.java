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
 package com.buschmais.osgi.maexo.framework.commons.mbean.lifecycle;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.management.ObjectName;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 
 import com.buschmais.osgi.maexo.framework.commons.mbean.objectname.ObjectNameFactory;
 
 /**
  * Provides an abstract default implementation for the
  * {@link ServiceMBeanLifeCycleSupport}.
  * <p>
  * The following assumptions are used:
  * <ul>
  * <li>No filter is used for tracking services.</li>
  * <li>The object name may be constructed by using an
  * {@link com.buschmais.osgi.maexo.framework.commons.mbean.objectname.ObjectNameFactory}
  * which must have been registered for the service interface provided by the
  * method {@link ServiceMBeanLifeCycleSupport#getServiceInterface()}.
  * </ul>
  */
 public abstract class DefaultServiceMBeanLifeCycleSupport extends
 		ServiceMBeanLifeCycleSupport {
 
 	/**
 	 * Defines the service properties which will be passed to the method
 	 * {@link ObjectNameFactory#getObjectName(Object, Map)}.
 	 */
	private static final String[] OBJECTNAME_PROPERTIES = new String[] {
 			Constants.SERVICE_DESCRIPTION, Constants.SERVICE_ID,
 			Constants.SERVICE_PID, Constants.SERVICE_RANKING,
 			Constants.SERVICE_VENDOR };
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param bundleContext
 	 *            the bundle context of the exporting bundle
 	 */
 	public DefaultServiceMBeanLifeCycleSupport(BundleContext bundleContext) {
 		super(bundleContext);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getServiceFilter() {
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ObjectName getObjectName(ServiceReference serviceReference,
 			Object service) {
 		Map<String, Object> properties = new HashMap<String, Object>();
 		for (String property : OBJECTNAME_PROPERTIES) {
 			properties.put(property, serviceReference.getProperty(property));
 		}
 		return super.getObjectNameHelper().getObjectName(service,
 				this.getServiceInterface(), properties);
 	}
 }
