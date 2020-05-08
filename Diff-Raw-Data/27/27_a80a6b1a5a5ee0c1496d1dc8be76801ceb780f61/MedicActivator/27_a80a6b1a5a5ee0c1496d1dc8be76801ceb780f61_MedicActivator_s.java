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
 
 package org.eclipse.virgo.medic.impl;
 
 import org.eclipse.equinox.log.ExtendedLogReaderService;
 import org.eclipse.virgo.medic.dump.DumpGenerator;
 import org.eclipse.virgo.medic.impl.config.ConfigurationAdminConfigurationProvider;
 import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
 import org.eclipse.virgo.medic.log.osgi.OSGiLogServiceListener;
 import org.eclipse.virgo.medic.management.MedicMBeanExporter;
 import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.cm.ConfigurationListener;
 import org.osgi.service.log.LogService;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class is threadSafe
  *
  */
 public final class MedicActivator implements BundleActivator {
 
     private final ServiceRegistrationTracker registrationTracker = new ServiceRegistrationTracker();
 
     private volatile ServiceReference<ExtendedLogReaderService> logReaderReference;
 
     private LogController logController = null;
 
     private ConfigurationProvider configurationProvider = null;
 
     private MedicMBeanExporter medicMBeanExporter = null;
     
     public void start(BundleContext context) throws Exception {
         this.configurationProvider = new ConfigurationAdminConfigurationProvider(context);
         this.registrationTracker.track(context.registerService(ConfigurationListener.class.getName(), configurationProvider, null));
 
         this.logController = new LogController(context, configurationProvider, registrationTracker);
 
         configurationProvider.addChangeListener(logController);
 
         logController.logStart();
         logController.eventLogStart();
         DumpGenerator dumpGenerator = logController.dumpStart();
         
         this.medicMBeanExporter = new MedicMBeanExporter(configurationProvider, dumpGenerator);
 
         this.logReaderReference = context.getServiceReference(ExtendedLogReaderService.class);
         ExtendedLogReaderService logReader = context.getService(this.logReaderReference);
         logReader.addLogListener(new OSGiLogServiceListener(LoggerFactory.getLogger(LogService.class)));
     }
 
     public void stop(BundleContext context) throws Exception {
     	MedicMBeanExporter medicMBeanExporter2 = this.medicMBeanExporter;
 		if(medicMBeanExporter2 != null){
     		medicMBeanExporter2.close();
     	}
         this.registrationTracker.unregisterAll();
         ServiceReference<ExtendedLogReaderService> localLogReaderReference = this.logReaderReference;
         if (localLogReaderReference != null) {
             context.ungetService(localLogReaderReference);
         }
 
         LogController logController2 = logController;
 		ConfigurationProvider configurationProvider2 = configurationProvider;
 		if (configurationProvider2 != null) {
             configurationProvider2.removeChangeListener(logController2);
         }
 
         if (logController2 != null) {
             logController2.dumpStop();
             logController2.logStop();
         }
     }
 
 }
