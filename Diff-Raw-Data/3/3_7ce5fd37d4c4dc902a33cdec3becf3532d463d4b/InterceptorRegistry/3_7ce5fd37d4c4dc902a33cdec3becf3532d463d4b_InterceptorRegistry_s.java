 /**
  *
  */
 /*******************************************************************************
  * Copyright (c) 2008 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Andreas Mattes - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.core.planner;
 
 import java.util.Map;
 
 import org.eclipse.swordfish.api.Interceptor;
 import org.eclipse.swordfish.api.SwordfishException;
 import org.eclipse.swordfish.core.util.RegistryImpl;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.springframework.osgi.context.BundleContextAware;
 import org.springframework.osgi.service.importer.ImportedOsgiServiceProxy;
 import org.springframework.osgi.service.importer.ServiceReferenceProxy;
 
 /**
  * @author dwolz
  *
  */
 public class InterceptorRegistry extends RegistryImpl<Interceptor> implements BundleContextAware {
 private BundleContext bundleContext;
 public void setBundleContext(BundleContext bundleContext) {
     this.bundleContext = bundleContext;
 }
 @Override
 public void register(Interceptor key, Map<String, ?> properties)
         throws SwordfishException {
     Interceptor actualInterceptor = key;
     if (actualInterceptor instanceof ImportedOsgiServiceProxy) {
         ServiceReference serviceReference = ((ImportedOsgiServiceProxy) actualInterceptor).getServiceReference();
         if (serviceReference instanceof ServiceReferenceProxy) {
             serviceReference = ((ServiceReferenceProxy) serviceReference).getTargetServiceReference();
         }
         actualInterceptor = (Interceptor) bundleContext.getService(serviceReference);
         if (key.getProperties() != null) {
             ((Map)key.getProperties()).put(Interceptor.TYPE_PROPERTY, actualInterceptor.getClass());
         }
     }
     super.register(key, properties);
 }
 }
