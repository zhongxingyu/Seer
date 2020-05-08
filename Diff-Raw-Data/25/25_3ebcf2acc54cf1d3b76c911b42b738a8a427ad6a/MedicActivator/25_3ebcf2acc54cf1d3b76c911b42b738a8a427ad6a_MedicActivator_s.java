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
 
 import java.io.File;
 import java.io.PrintStream;
 import java.util.Arrays;
 import java.util.Dictionary;
 import java.util.List;
 import java.util.Properties;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleListener;
 import org.osgi.framework.ServiceFactory;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.cm.ConfigurationListener;
 import org.osgi.service.packageadmin.PackageAdmin;
 
 
 import org.eclipse.virgo.medic.dump.DumpGenerator;
 import org.eclipse.virgo.medic.dump.impl.DumpContributorPublisher;
 import org.eclipse.virgo.medic.dump.impl.StandardDumpContributorResolver;
 import org.eclipse.virgo.medic.dump.impl.StandardDumpGenerator;
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 import org.eclipse.virgo.medic.eventlog.EventLoggerFactory;
 import org.eclipse.virgo.medic.eventlog.impl.BundleSearchingPropertyResourceBundleResolver;
 import org.eclipse.virgo.medic.eventlog.impl.EventLoggerServiceFactory;
 import org.eclipse.virgo.medic.eventlog.impl.StandardLocaleResolver;
 import org.eclipse.virgo.medic.eventlog.impl.logback.LogBackEventLoggerFactory;
 import org.eclipse.virgo.medic.impl.config.ConfigurationAdminConfigurationProvider;
 import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
 import org.eclipse.virgo.medic.log.ConfigurationPublicationFailedException;
 import org.eclipse.virgo.medic.log.DelegatingPrintStream;
 import org.eclipse.virgo.medic.log.LoggingConfigurationPublisher;
 import org.eclipse.virgo.medic.log.impl.CallingBundleResolver;
 import org.eclipse.virgo.medic.log.impl.ClassSelector;
 import org.eclipse.virgo.medic.log.impl.ExecutionStackAccessor;
 import org.eclipse.virgo.medic.log.impl.LoggingLevel;
 import org.eclipse.virgo.medic.log.impl.LoggingPrintStreamWrapper;
 import org.eclipse.virgo.medic.log.impl.PackageNameFilteringClassSelector;
 import org.eclipse.virgo.medic.log.impl.SecurityManagerExecutionStackAccessor;
 import org.eclipse.virgo.medic.log.impl.StandardCallingBundleResolver;
 import org.eclipse.virgo.medic.log.impl.StandardDelegatingPrintStream;
 import org.eclipse.virgo.medic.log.impl.config.BundleResourceConfigurationLocator;
 import org.eclipse.virgo.medic.log.impl.config.CompositeConfigurationLocator;
 import org.eclipse.virgo.medic.log.impl.config.ConfigurationLocator;
 import org.eclipse.virgo.medic.log.impl.config.ServiceRegistryConfigurationLocator;
 import org.eclipse.virgo.medic.log.impl.config.StandardLoggingConfigurationPublisher;
 import org.eclipse.virgo.medic.log.impl.logback.DelegatingContextSelector;
 import org.eclipse.virgo.medic.log.impl.logback.JoranLoggerContextConfigurer;
 import org.eclipse.virgo.medic.log.impl.logback.LoggerContextConfigurer;
 import org.eclipse.virgo.medic.log.impl.logback.StandardContextSelectorDelegate;
 import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
 
 public final class MedicActivator implements BundleActivator {
 
     private static final String LOGGER_NAME_SYSERR = "System.err";
 
 	private static final String LOGGER_NAME_SYSOUT = "System.out";
 	
     private static final String LOGGER_NAME_SYSERR_DELEGATE = "delegating.System.err";
 
     private static final String LOGGER_NAME_SYSOUT_DELEGATE = "delegating.System.out";
 
 	private static final String PROPERTY_MEDIC_CONFIG_PATH = "org.eclipse.virgo.medic.log.config.path";
 
 	private static final String DEFAULT_CONTEXT_SELECTOR = "ch.qos.logback.classic.selector.DefaultContextSelector";
 
 	private static final String PROPERTY_LOGBACK_CONTEXT_SELECTOR = "logback.ContextSelector";
 	
 	private final ServiceRegistrationTracker registrationTracker = new ServiceRegistrationTracker();
 
     private volatile StandardDumpGenerator dumpGenerator;
 
     private volatile LogBackEventLoggerFactory eventLoggerFactory;
 
     private volatile DumpContributorPublisher dumpContributorPublisher;
 
     private volatile ServiceReference packageAdminReference;
         
     private volatile PrintStream sysOut;
     
     private volatile PrintStream sysErr;
 
     private static final List<String> DEFAULT_LOGGING_PACKAGES = Arrays.asList(//
         "org.apache.commons.logging",//
         "org.apache.log4j",//
         "org.slf4j",//
         "org.slf4j.impl",//
         "org.eclipse.virgo.medic.log",//
         "org.eclipse.virgo.medic.log.impl",//
         "org.eclipse.virgo.medic.log.impl.logback");
 
     public void start(BundleContext context) throws Exception {
     	ConfigurationProvider configurationProvider = new ConfigurationAdminConfigurationProvider(context);   
     	this.registrationTracker.track(context.registerService(ConfigurationListener.class.getName(), configurationProvider, null));
 
     	logStart(context, configurationProvider);
     	eventLogStart(context);
     	dumpStart(context, configurationProvider);
     }
 
     public void stop(BundleContext context) throws Exception {
     	
     	this.registrationTracker.unregisterAll();
     	
         dumpStop();                	    	    	
         logStop(context);
     }
 
     private void dumpStart(BundleContext context, ConfigurationProvider configurationProvider) {
         
         this.dumpGenerator = new StandardDumpGenerator(new StandardDumpContributorResolver(context), configurationProvider,
             this.eventLoggerFactory.createEventLogger(context.getBundle()));
         this.registrationTracker.track(context.registerService(DumpGenerator.class.getName(), this.dumpGenerator, null));
 
         this.dumpContributorPublisher = new DumpContributorPublisher(context, configurationProvider);
         this.dumpContributorPublisher.publishDumpContributors();               
     }
 
     private void dumpStop() {        
         if (this.dumpGenerator != null) {
             this.dumpGenerator.close();
         }
 
         if (this.dumpContributorPublisher != null) {
             this.dumpContributorPublisher.retractDumpContributors();
         }
     }
      
     @SuppressWarnings("unchecked")
 	private void logStart(BundleContext context, ConfigurationProvider configurationProvider) throws ConfigurationPublicationFailedException {    	    	
         this.packageAdminReference = context.getServiceReference(PackageAdmin.class.getName());
         PackageAdmin packageAdmin = (PackageAdmin) context.getService(this.packageAdminReference);
 
         StandardContextSelectorDelegate delegate = createContextSelectorDelegate(context, packageAdmin);
         this.registrationTracker.track(context.registerService(BundleListener.class.getName(), delegate, null));
         DelegatingContextSelector.setDelegate(delegate);
         
         StandardLoggingConfigurationPublisher loggingConfigurationPublisher = new StandardLoggingConfigurationPublisher(context);
         this.registrationTracker.track(context.registerService(LoggingConfigurationPublisher.class.getName(), loggingConfigurationPublisher, null));
         
         publishDefaultConfigurationIfAvailable(context, loggingConfigurationPublisher);
         
         System.setProperty(PROPERTY_LOGBACK_CONTEXT_SELECTOR, DelegatingContextSelector.class.getName());
         
         ExecutionStackAccessor stackAccessor = new SecurityManagerExecutionStackAccessor();
         
         Dictionary configuration = configurationProvider.getConfiguration();
         
         PrintStream delegatingSysOut = new StandardDelegatingPrintStream(System.out);
         PrintStream delegatingSysErr = new StandardDelegatingPrintStream(System.err);
         
         this.sysOut = System.out;
         this.sysErr = System.err;
         
         System.setOut(delegatingSysOut);
         System.setErr(delegatingSysErr);
         
         if (Boolean.valueOf((String)configuration.get(ConfigurationProvider.KEY_LOG_WRAP_SYSOUT))) {                	        	
         	publishDelegatingPrintStream(delegatingSysOut, LOGGER_NAME_SYSOUT_DELEGATE, context);
             publishPrintStream(this.sysOut, LOGGER_NAME_SYSOUT, context);
         	
         	System.setOut(wrapPrintStream(System.out, LOGGER_NAME_SYSOUT, LoggingLevel.INFO, stackAccessor, configurationProvider, ConfigurationProvider.KEY_LOG_WRAP_SYSOUT));
         }
         
         if (Boolean.valueOf((String)configuration.get(ConfigurationProvider.KEY_LOG_WRAP_SYSERR))) {            
             publishDelegatingPrintStream(delegatingSysErr, LOGGER_NAME_SYSERR_DELEGATE, context);
             publishPrintStream(this.sysErr, LOGGER_NAME_SYSERR, context);
             
         	System.setErr(wrapPrintStream(System.err, LOGGER_NAME_SYSERR, LoggingLevel.ERROR, stackAccessor, configurationProvider, ConfigurationProvider.KEY_LOG_WRAP_SYSERR));
         }
     }
     
     private PrintStream wrapPrintStream(PrintStream printStream, String loggerName, LoggingLevel loggingLevel, ExecutionStackAccessor stackAccessor, ConfigurationProvider configurationProvider, String configurationProperty) {
         LoggingPrintStreamWrapper wrapper = new LoggingPrintStreamWrapper(printStream, loggerName, loggingLevel, stackAccessor, configurationProvider, configurationProperty);
         return wrapper;
     }
     
     private void publishPrintStream(PrintStream printStream, String name, BundleContext context) {        
         Properties properties = new Properties();
         properties.put("org.eclipse.virgo.medic.log.printStream", name);
                                        
         this.registrationTracker.track(context.registerService(PrintStream.class.getName(), printStream, properties));  
     }
     
     private void publishDelegatingPrintStream(PrintStream printStream, String name, BundleContext context) {        
         Properties properties = new Properties();
         properties.put("org.eclipse.virgo.medic.log.printStream", name);
         
         String[] classes = new String[] {DelegatingPrintStream.class.getName()};                        
         this.registrationTracker.track(context.registerService(classes, printStream, properties));  
     }
     
     private void publishDefaultConfigurationIfAvailable(BundleContext context, StandardLoggingConfigurationPublisher publisher) throws ConfigurationPublicationFailedException {
         String logConfigPath = context.getProperty(PROPERTY_MEDIC_CONFIG_PATH);
         if (logConfigPath != null) {
         	File logConfigFile = new File(logConfigPath);
         	if (logConfigFile.exists()) {
         		publisher.publishDefaultConfiguration(new File(logConfigPath));
         	}
         }        
     }
 
     private static StandardContextSelectorDelegate createContextSelectorDelegate(BundleContext bundleContext, PackageAdmin packageAdmin) {
         ConfigurationLocator configurationLocator = createConfigurationLocator(bundleContext);
        CallingBundleResolver loggingCallerLocator = createLoggingCallerLocator(packageAdmin);
         LoggerContextConfigurer loggerContextConfigurer = new JoranLoggerContextConfigurer();
         return new StandardContextSelectorDelegate(loggingCallerLocator, configurationLocator, bundleContext.getBundle(), loggerContextConfigurer);
     }
 
     /**
      * Logging configuration is located by searching up to two sources, depending on the bundle doing the logging.
      * <p>
      * Firstly, if and only if the bundle has a specific Medic manifest header, the service registry is searched in
      * a location specified in the manifest header. Secondly, if the configuration has not already been found, the
      * the bundle's resources are checked for a Logback configuration file.
      */
     private static ConfigurationLocator createConfigurationLocator(BundleContext bundleContext) {
         return new CompositeConfigurationLocator(new ServiceRegistryConfigurationLocator(bundleContext), new BundleResourceConfigurationLocator());
     }
 
    private static CallingBundleResolver createLoggingCallerLocator(PackageAdmin packageAdmin) {
         ClassSelector classSelector = createClassSelector();
         ExecutionStackAccessor executionStackAccessor = createExecutionStackAccessor();
 
        return new StandardCallingBundleResolver(executionStackAccessor, classSelector, packageAdmin);
     }
 
     private static ClassSelector createClassSelector() {
         return new PackageNameFilteringClassSelector(DEFAULT_LOGGING_PACKAGES);
     }
 
     private static ExecutionStackAccessor createExecutionStackAccessor() {
         return new SecurityManagerExecutionStackAccessor();
     }
 
     private void logStop(BundleContext context) {
     	
     	System.setProperty(PROPERTY_LOGBACK_CONTEXT_SELECTOR, DEFAULT_CONTEXT_SELECTOR);
     	
         if (this.packageAdminReference != null) {
             context.ungetService(this.packageAdminReference);
             this.packageAdminReference = null;
         }
 
         DelegatingContextSelector.setDelegate(null);        
         
         if (this.sysOut != null) {
         	System.setOut(this.sysOut);
         }
         
         if (this.sysErr != null) {
         	System.setErr(this.sysErr);
         }                
     }
 
     private void eventLogStart(BundleContext context) {
         this.eventLoggerFactory = createFactory(context);
         ServiceFactory serviceFactory = new EventLoggerServiceFactory(this.eventLoggerFactory);
         this.registrationTracker.track(context.registerService(EventLoggerFactory.class.getName(), this.eventLoggerFactory, null));
         this.registrationTracker.track(context.registerService(EventLogger.class.getName(), serviceFactory, null));
     }
 
     private LogBackEventLoggerFactory createFactory(BundleContext context) {
         BundleSearchingPropertyResourceBundleResolver resourceBundleResolver = new BundleSearchingPropertyResourceBundleResolver();
         return new LogBackEventLoggerFactory(resourceBundleResolver, new StandardLocaleResolver(), context.getBundle());
     }
 }
