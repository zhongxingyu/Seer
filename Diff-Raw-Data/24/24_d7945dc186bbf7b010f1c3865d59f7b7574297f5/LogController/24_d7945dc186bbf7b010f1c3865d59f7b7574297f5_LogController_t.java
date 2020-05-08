 /*******************************************************************************
  * Copyright (c) 2011 SAP AG
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Hristo Iliev, SAP AG - initial contribution
  *******************************************************************************/
 package org.eclipse.virgo.medic.impl;
 
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
 import org.eclipse.virgo.medic.impl.config.ConfigurationChangeListener;
 import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
 import org.eclipse.virgo.medic.log.ConfigurationPublicationFailedException;
 import org.eclipse.virgo.medic.log.DelegatingPrintStream;
 import org.eclipse.virgo.medic.log.LoggingConfigurationPublisher;
 import org.eclipse.virgo.medic.log.impl.*;
 import org.eclipse.virgo.medic.log.impl.config.*;
 import org.eclipse.virgo.medic.log.impl.logback.DelegatingContextSelector;
 import org.eclipse.virgo.medic.log.impl.logback.JoranLoggerContextConfigurer;
 import org.eclipse.virgo.medic.log.impl.logback.LoggerContextConfigurer;
 import org.eclipse.virgo.medic.log.impl.logback.StandardContextSelectorDelegate;
 import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleListener;
 import org.osgi.framework.ServiceFactory;
 import org.osgi.framework.ServiceRegistration;
 import org.slf4j.bridge.SLF4JBridgeHandler;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.util.Arrays;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Handler;
 import java.util.logging.Logger;
 
 public class LogController implements ConfigurationChangeListener {
 
     private static final String LOGGER_NAME_SYSERR = "System.err";
 
     private static final String LOGGER_NAME_SYSOUT = "System.out";
 
     private static final String LOGGER_NAME_SYSERR_DELEGATE = "delegating.System.err";
 
     private static final String LOGGER_NAME_SYSOUT_DELEGATE = "delegating.System.out";
 
     private static final String PROPERTY_MEDIC_CONFIG_PATH = "org.eclipse.virgo.medic.log.config.path";
 
     private static final String DEFAULT_CONTEXT_SELECTOR = "ch.qos.logback.classic.selector.DefaultContextSelector";
 
     private static final String PROPERTY_LOGBACK_CONTEXT_SELECTOR = "logback.ContextSelector";
 
     private volatile StandardDumpGenerator dumpGenerator;
 
     private volatile LogBackEventLoggerFactory eventLoggerFactory;
 
     private volatile DumpContributorPublisher dumpContributorPublisher;
 
     private volatile PrintStream sysOut = System.out;
 
     private volatile PrintStream sysErr = System.err;
 
     private PrintStream delegatingSysOut = new StandardDelegatingPrintStream(System.out);
 
     private PrintStream delegatingSysErr = new StandardDelegatingPrintStream(System.err);
 
     private ServiceRegistration<?> delegatingSysOutRegistration;
     private ServiceRegistration<?> delegatingSysErrRegistration;
 
     private ServiceRegistration<?> sysOutRegistration;
     private ServiceRegistration<?> sysErrRegistration;
 
     private volatile ExecutionStackAccessor stackAccessor;
 
     private volatile ConsoleHandler javaConsoleHandler;
 
     private static final List<String> DEFAULT_LOGGING_PACKAGES = Arrays.asList(//
             "org.apache.commons.logging",//
             "org.apache.log4j",//
             "org.slf4j",//
             "org.slf4j.impl",//
             "org.eclipse.virgo.medic.log",//
             "org.eclipse.virgo.medic.log.impl",//
             "org.eclipse.virgo.medic.log.impl.logback");
 
     private BundleContext bundleContext;
     private ConfigurationProvider configurationProvider;
     private ServiceRegistrationTracker registrationTracker;
 
     public LogController(BundleContext ctx, ConfigurationProvider cfgProvider, ServiceRegistrationTracker regTracker) throws ConfigurationPublicationFailedException {
         this.bundleContext = ctx;
         this.configurationProvider = cfgProvider;
         this.registrationTracker = regTracker;
 
         StandardContextSelectorDelegate delegate = createContextSelectorDelegate(bundleContext);
         registrationTracker.track(bundleContext.registerService(BundleListener.class.getName(), delegate, null));
         DelegatingContextSelector.setDelegate(delegate);
 
         StandardLoggingConfigurationPublisher loggingConfigurationPublisher = new StandardLoggingConfigurationPublisher(bundleContext);
         registrationTracker.track(bundleContext.registerService(LoggingConfigurationPublisher.class.getName(), loggingConfigurationPublisher, null));
 
         publishDefaultConfigurationIfAvailable(bundleContext, loggingConfigurationPublisher);
 
         System.setProperty(PROPERTY_LOGBACK_CONTEXT_SELECTOR, DelegatingContextSelector.class.getName());
 
         this.stackAccessor = new SecurityManagerExecutionStackAccessor();
 
         this.sysOut = System.out;
         this.sysErr = System.err;
     }
 
     public void dumpStart() {
         this.dumpGenerator = new StandardDumpGenerator(new StandardDumpContributorResolver(bundleContext), configurationProvider, this.eventLoggerFactory.createEventLogger(bundleContext.getBundle()));
         registrationTracker.track(bundleContext.registerService(DumpGenerator.class.getName(), this.dumpGenerator, null));
 
         this.dumpContributorPublisher = new DumpContributorPublisher(bundleContext);
         this.dumpContributorPublisher.publishDumpContributors();
     }
 
     public void dumpStop() {
         if (this.dumpGenerator != null) {
             this.dumpGenerator.close();
         }
 
         if (this.dumpContributorPublisher != null) {
             this.dumpContributorPublisher.retractDumpContributors();
         }
     }
 
     public void logStart() throws ConfigurationPublicationFailedException {
         Dictionary<String, String> configuration = configurationProvider.getConfiguration();
 
         SLF4JBridgeHandler.install();

        updateLogConfiguration(configuration);
     }
 
     public void logStop() {
         System.setProperty(PROPERTY_LOGBACK_CONTEXT_SELECTOR, DEFAULT_CONTEXT_SELECTOR);
 
         DelegatingContextSelector.setDelegate(null);
 
         if (this.sysOut != null) {
             System.setOut(this.sysOut);
         }
 
         if (this.sysErr != null) {
             System.setErr(this.sysErr);
         }
 
         SLF4JBridgeHandler.uninstall();
         enableJulConsoleLogger();
     }
 
     private void enableJulConsoleLogger() {
         if (this.javaConsoleHandler != null) {
             getJavaRootLogger().addHandler(this.javaConsoleHandler);
         }
     }
 
     private void disableJulConsoleHandler() {
         // remove console handler from root logger
         Logger rootLogger = getJavaRootLogger();
         Handler[] handlers = rootLogger.getHandlers();
         for (Handler handler : handlers) {
             if (handler instanceof ConsoleHandler) {
                 this.javaConsoleHandler = (ConsoleHandler) handler;
                 rootLogger.removeHandler(handler);
             }
         }
     }
 
     public void eventLogStart() {
         this.eventLoggerFactory = createFactory(bundleContext);
         ServiceFactory<EventLogger> serviceFactory = new EventLoggerServiceFactory(this.eventLoggerFactory);
         registrationTracker.track(bundleContext.registerService(EventLoggerFactory.class.getName(), this.eventLoggerFactory, null));
         registrationTracker.track(bundleContext.registerService(EventLogger.class.getName(), serviceFactory, null));
     }
 
     private PrintStream wrapPrintStream(PrintStream printStream, String loggerName, LoggingLevel loggingLevel, ExecutionStackAccessor stackAccessor, ConfigurationProvider configurationProvider, String configurationProperty) {
         LoggingPrintStreamWrapper wrapper = new LoggingPrintStreamWrapper(printStream, loggerName, loggingLevel, stackAccessor, configurationProvider, configurationProperty);
         return wrapper;
     }
 
     private ServiceRegistration<?> publishPrintStream(PrintStream printStream, String name) {
         Hashtable<String, String> properties = new Hashtable<String, String>();
         properties.put("org.eclipse.virgo.medic.log.printStream", name);
 
         ServiceRegistration<?> registration = bundleContext.registerService(PrintStream.class.getName(), printStream, properties);
         registrationTracker.track(registration);
 
         return registration;
     }
 
     private ServiceRegistration<?> publishDelegatingPrintStream(PrintStream printStream, String name) {
         Hashtable<String, String> properties = new Hashtable<String, String>();
         properties.put("org.eclipse.virgo.medic.log.printStream", name);
 
         String[] classes = new String[]{DelegatingPrintStream.class.getName()};
         ServiceRegistration<?> delegatingPrintStreamRegistration = bundleContext.registerService(classes, printStream, properties);
         registrationTracker.track(delegatingPrintStreamRegistration);
 
         return delegatingPrintStreamRegistration;
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
 
     private static StandardContextSelectorDelegate createContextSelectorDelegate(BundleContext bundleContext) {
         ConfigurationLocator configurationLocator = createConfigurationLocator(bundleContext);
         CallingBundleResolver loggingCallerLocator = createLoggingCallerLocator();
         LoggerContextConfigurer loggerContextConfigurer = new JoranLoggerContextConfigurer();
         return new StandardContextSelectorDelegate(loggingCallerLocator, configurationLocator, bundleContext.getBundle(), loggerContextConfigurer);
     }
 
     /**
      * Logging configuration is located by searching up to two sources, depending on the bundle doing the logging.
      * <p/>
      * Firstly, if and only if the bundle has a specific Medic manifest header, the service registry is searched in
      * a location specified in the manifest header. Secondly, if the configuration has not already been found, the
      * the bundle's resources are checked for a Logback configuration file.
      */
     private static ConfigurationLocator createConfigurationLocator(BundleContext bundleContext) {
         return new CompositeConfigurationLocator(new ServiceRegistryConfigurationLocator(bundleContext), new BundleResourceConfigurationLocator());
     }
 
     private static CallingBundleResolver createLoggingCallerLocator() {
         ClassSelector classSelector = createClassSelector();
         ExecutionStackAccessor executionStackAccessor = createExecutionStackAccessor();
 
         return new StandardCallingBundleResolver(executionStackAccessor, classSelector);
     }
 
     private static ClassSelector createClassSelector() {
         return new PackageNameFilteringClassSelector(DEFAULT_LOGGING_PACKAGES);
     }
 
     private static ExecutionStackAccessor createExecutionStackAccessor() {
         return new SecurityManagerExecutionStackAccessor();
     }
 
     private LogBackEventLoggerFactory createFactory(BundleContext context) {
         BundleSearchingPropertyResourceBundleResolver resourceBundleResolver = new BundleSearchingPropertyResourceBundleResolver();
         return new LogBackEventLoggerFactory(resourceBundleResolver, new StandardLocaleResolver(), context.getBundle());
     }
 
     private Logger getJavaRootLogger() {
         return Logger.getLogger("");
     }
 
     @Override
     public void configurationChanged(ConfigurationProvider provider) {
         Dictionary<String, String> configuration = configurationProvider.getConfiguration();
         updateLogConfiguration(configuration);
     }
 
     private synchronized void updateLogConfiguration(Dictionary<String, String> configuration) {
         if (Boolean.valueOf(configuration.get(ConfigurationProvider.KEY_LOG_WRAP_SYSOUT))) {
             delegatingSysOutRegistration = publishDelegatingPrintStream(delegatingSysOut, LOGGER_NAME_SYSOUT_DELEGATE);
             sysOutRegistration = publishPrintStream(this.sysOut, LOGGER_NAME_SYSOUT);
 
             System.setOut(wrapPrintStream(System.out, LOGGER_NAME_SYSOUT, LoggingLevel.INFO, stackAccessor, configurationProvider, ConfigurationProvider.KEY_LOG_WRAP_SYSOUT));
         } else {
             if (delegatingSysOutRegistration != null) {
                 registrationTracker.unregister(delegatingSysOutRegistration);
                 delegatingSysOutRegistration = null;
             }
             if (sysOutRegistration != null) {
                 registrationTracker.unregister(sysOutRegistration);
                 sysOutRegistration = null;
             }
             System.setOut(delegatingSysOut);
         }
 
         if (Boolean.valueOf(configuration.get(ConfigurationProvider.KEY_LOG_WRAP_SYSERR))) {
             delegatingSysErrRegistration = publishDelegatingPrintStream(delegatingSysErr, LOGGER_NAME_SYSERR_DELEGATE);
             sysErrRegistration = publishPrintStream(this.sysErr, LOGGER_NAME_SYSERR);
 
             System.setErr(wrapPrintStream(System.err, LOGGER_NAME_SYSERR, LoggingLevel.ERROR, stackAccessor, configurationProvider, ConfigurationProvider.KEY_LOG_WRAP_SYSERR));
         } else {
             if (delegatingSysErrRegistration != null) {
                 registrationTracker.unregister(delegatingSysErrRegistration);
                 delegatingSysErrRegistration = null;
             }
             if (sysErrRegistration != null) {
                 registrationTracker.unregister(sysErrRegistration);
                 sysErrRegistration = null;
             }
             System.setErr(delegatingSysErr);
         }

        if (Boolean.valueOf(configuration.get(ConfigurationProvider.KEY_ENABLE_JUL_CONSOLE_HANDLER))) {
            enableJulConsoleLogger();
        } else {
            disableJulConsoleHandler();
        }
     }
 }
