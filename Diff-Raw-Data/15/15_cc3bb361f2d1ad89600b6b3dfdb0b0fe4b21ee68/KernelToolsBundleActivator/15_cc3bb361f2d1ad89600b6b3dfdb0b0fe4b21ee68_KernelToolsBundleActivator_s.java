 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.tools.internal;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 
 
 /**
  * <code>BundleActivator> for the kernel tools bundle
  * <p />
  *
  * <strong>Concurrent Semantics</strong><br />
  * Thread-safe
  *
  */
 public class KernelToolsBundleActivator implements BundleActivator {
     
    ServiceRegistration eventLoggerRegistration;
 
     /** 
      * {@inheritDoc}
      */
     public void start(BundleContext context) throws Exception {
         EventLogger eventLogger = new SilentEventLogger();
        eventLoggerRegistration = context.registerService(EventLogger.class.getName(), eventLogger, null);
     }
 
     /** 
      * {@inheritDoc}
      */
     public void stop(BundleContext context) throws Exception {
        ServiceRegistration localRegistration = this.eventLoggerRegistration;
         this.eventLoggerRegistration = null;
         
         if (localRegistration != null) {
             localRegistration.unregister();
         }
     }
 }
