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
 
 package org.eclipse.virgo.kernel.core.internal.blueprint;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ScheduledExecutorService;
 
 import org.osgi.framework.Bundle;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.eclipse.virgo.kernel.diagnostics.KernelLogEvents;
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 
 /**
  * {@link ApplicationContextDependencyMonitor} is a class that tracks the satisfaction of service dependencies needed
  * during the creation of application contexts and issues log messages for delayed service dependencies.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * This class is thread safe.
  * 
  */
 public final class ApplicationContextDependencyMonitor implements EventHandler {
 
     private static final String TOPIC_BLUEPRINT_EVENTS = "org/osgi/service/blueprint/container/";
 
     private static final String EVENT_WAITING = TOPIC_BLUEPRINT_EVENTS + "WAITING";
 
     private static final String EVENT_GRACE_PERIOD = TOPIC_BLUEPRINT_EVENTS + "GRACE_PERIOD";
 
     private static final String EVENT_FAILURE = TOPIC_BLUEPRINT_EVENTS + "FAILURE";
 
     private static final String EVENT_CREATED = TOPIC_BLUEPRINT_EVENTS + "CREATED";
 
     private static final int MAXIMUM_WARNING_INTERVAL = 60 * 1000;
 
     private static final int WARNING_INTERVAL_INCREASE_RATE_PERCENT = 200;
 
     private static final int INITIAL_WARNING_INTERVAL = 5 * 1000;
 
     private static final int SLOW_WARNING_INTERVAL = 5 * 60 * 1000;
 
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
     private final EventLogger eventLogger;
 
     private final ScheduledExecutorService scheduledExecutorService;
 
     private final Map<Bundle, Map<ServiceDependency, Ticker>> tickers = new HashMap<Bundle, Map<ServiceDependency, Ticker>>();
 
     private final Object monitor = new Object();
 
     /**
      * Construct a {@link ApplicationContextDependencyMonitor} which uses the given {@link ScheduledExecutorService} to
      * schedule its warning messages.
      * 
      * @param scheduledExecutorService the {@link ScheduledExecutorService} for scheduling warning messages
      * @param eventLogger
      */
     public ApplicationContextDependencyMonitor(ScheduledExecutorService scheduledExecutorService, EventLogger eventLogger) {
         this.scheduledExecutorService = scheduledExecutorService;
         this.eventLogger = eventLogger;
     }
 
     /**
      * {@inheritDoc}
      */
     public void handleEvent(Event event) {
         synchronized (this.monitor) {
             Bundle bundle = (Bundle) event.getProperty(EventConstants.BUNDLE);
 
             if (EVENT_WAITING.equals(event.getTopic())) {
                 List<ServiceDependency> serviceDependencies = createServiceDependencies(event);
                 for (ServiceDependency serviceDependency : serviceDependencies) {
                     addServiceDependencyTicker(serviceDependency, bundle);
                 }
             } else if (EVENT_GRACE_PERIOD.equals(event.getTopic())) {
                 List<ServiceDependency> remainingUnsatisfiedDependencies = createServiceDependencies(event);
                 changeInUnsatisfiedDependencies(remainingUnsatisfiedDependencies, bundle);
 
             } else if (EVENT_FAILURE.equals(event.getTopic())) {
                 String[] dependenciesArray = (String[]) event.getProperty("dependencies");
                 if (dependenciesArray != null) {
                     List<ServiceDependency> serviceDependencies = createServiceDependencies(event);
                     serviceDependenciesTimedOut(serviceDependencies, bundle);
                 } else {
                     containerCreationFailed(bundle);
                 }
             } else if (EVENT_CREATED.equals(event.getTopic())) {
                 containerCreated(bundle);
             }
         }
     }
 
     private void serviceDependenciesTimedOut(List<ServiceDependency> timedOutDependencies, Bundle bundle) {
         Map<ServiceDependency, Ticker> bundlesTickers = this.tickers.get(bundle);
 
         if (bundlesTickers != null) {
             for (ServiceDependency timedOutDependency : timedOutDependencies) {
                 Ticker ticker = bundlesTickers.remove(timedOutDependency);
                 if (ticker != null) {
                     dependencyTimedOut(timedOutDependency, ticker, bundle);
                 }
             }
         }
     }
 
     private void containerCreationFailed(Bundle bundle) {
         Map<ServiceDependency, Ticker> tickers = this.tickers.remove(bundle);
         if (tickers != null) {
             for (Entry<ServiceDependency, Ticker> ticker : tickers.entrySet()) {
                 ticker.getValue().cancel();
             }
         }
     }
 
     private void containerCreated(Bundle bundle) {
         Map<ServiceDependency, Ticker> bundlesTickers = this.tickers.remove(bundle);
 
         if (bundlesTickers != null) {
             for (Entry<ServiceDependency, Ticker> entry : bundlesTickers.entrySet()) {
                 dependencySatisfied(entry.getKey(), entry.getValue(), bundle);
             }
         }
     }
 
     private void changeInUnsatisfiedDependencies(List<ServiceDependency> remainingUnsatisfiedDependencies, Bundle bundle) {
         Map<ServiceDependency, Ticker> tickers = this.tickers.get(bundle);
 
         if (tickers != null) {
             Iterator<Entry<ServiceDependency, Ticker>> entries = tickers.entrySet().iterator();
 
             while (entries.hasNext()) {
                 Entry<ServiceDependency, Ticker> entry = entries.next();
 
                 if (!remainingUnsatisfiedDependencies.contains(entry.getKey())) {
                     dependencySatisfied(entry.getKey(), entry.getValue(), bundle);
                     entries.remove();
                 }
             }
         }
     }
 
     private void dependencySatisfied(ServiceDependency serviceDependency, Ticker ticker, Bundle bundle) {
         logger.info("Service dependency '{}' has been satisfied", serviceDependency);
         handleRemovedTicker(ticker, serviceDependency, bundle, true);
     }
 
     private void dependencyTimedOut(ServiceDependency serviceDependency, Ticker ticker, Bundle bundle) {
         logger.info("Service dependency '{}' has timed out", serviceDependency);
         handleRemovedTicker(ticker, serviceDependency, bundle, false);
     }
 
     private void handleRemovedTicker(Ticker ticker, ServiceDependency serviceDependency, Bundle bundle, boolean satisfied) {
         boolean hasTicked = ticker.cancel();
         if (hasTicked) {
             if (satisfied) {
                 this.eventLogger.log(KernelLogEvents.APPLICATION_CONTEXT_DEPENDENCY_SATISFIED, serviceDependency.getBeanName(),
                     bundle.getSymbolicName(), bundle.getVersion(), serviceDependency.getFilter());
             } else {
                 this.eventLogger.log(KernelLogEvents.APPLICATION_CONTEXT_DEPENDENCY_TIMED_OUT, serviceDependency.getBeanName(),
                     bundle.getSymbolicName(), bundle.getVersion(), serviceDependency.getFilter());
             }
         }
     }
 
     /**
      * Add a service dependency ticker for the given application context, given associated bundle, and given service
      * dependency.
      * 
      * @param applicationContext the partially constructed application context which needs the service dependency
      * @param serviceDependency the service dependency
      * @param bundle the {@link Bundle} associated with the given application context
      */
     private void addServiceDependencyTicker(final ServiceDependency serviceDependency, final Bundle bundle) {
         Map<ServiceDependency, Ticker> serviceDependencyTickers = getServiceDependencyTickers(bundle);
         if (serviceDependencyTickers.containsKey(serviceDependency)) {
             logger.warn("Service dependency '{}' already being waited upon", serviceDependency);
         } else {
             // Services which are flagged as likely to be slow to be published are given a longer initial warning
             // interval.
             boolean slowService = serviceDependency.getFilter().contains("(org.eclipse.virgo.server.slowservice=true)");
             serviceDependencyTickers.put(serviceDependency, StandardTicker.createExponentialTicker(slowService ? SLOW_WARNING_INTERVAL
                 : INITIAL_WARNING_INTERVAL, WARNING_INTERVAL_INCREASE_RATE_PERCENT, slowService ? SLOW_WARNING_INTERVAL : MAXIMUM_WARNING_INTERVAL,
                 new Callable<Void>() {
 
                     public Void call() throws Exception {
                         synchronized (ApplicationContextDependencyMonitor.this.monitor) {
                             if (bundle.getState() == Bundle.UNINSTALLED) {
                                 ApplicationContextDependencyMonitor.this.containerCreationFailed(bundle);
                             } else {
                                 eventLogger.log(KernelLogEvents.APPLICATION_CONTEXT_DEPENDENCY_DELAYED, serviceDependency.getBeanName(),
                                     bundle.getSymbolicName(), bundle.getVersion(), serviceDependency.getFilter());
                             }
                             return null;
                         }
                     }
                 }, this.scheduledExecutorService));
         }
     }
 
     /**
      * Get the possibly empty map of service dependency tickers for the given <code>Bundle</code>.
      * 
      * @param bundle the <code>Bundle</code> whose application context's service dependencies are required
      * @return a map of service dependency tickers
      */
     private Map<ServiceDependency, Ticker> getServiceDependencyTickers(Bundle bundle) {
         Map<ServiceDependency, Ticker> tickers = this.tickers.get(bundle);
         if (tickers == null) {
             tickers = new HashMap<ServiceDependency, Ticker>();
             this.tickers.put(bundle, tickers);
         }
         return tickers;
     }
 
     public void stop() {
         this.scheduledExecutorService.shutdown();
     }
 
     private List<ServiceDependency> createServiceDependencies(Event event) {
         String[] filters = (String[]) event.getProperty("dependencies");
         String[] beanNames = (String[]) event.getProperty("bean.name");
 
         List<ServiceDependency> serviceDependencies = new ArrayList<ServiceDependency>();
 
        if (filters != null && beanNames != null) {
             for (int i = 0; i < filters.length; i++) {
                 serviceDependencies.add(new ServiceDependency(filters[i], beanNames[i]));
             }
         }
 
         return serviceDependencies;
     }
 
     private static final class ServiceDependency {
 
         private final String filter;
 
         private final String beanName;
 
         private ServiceDependency(String filter, String beanName) {
             this.filter = filter;
             this.beanName = beanName;
         }
 
         public String getFilter() {
             return filter;
         }
 
         public String getBeanName() {
             return beanName;
         }
 
         @Override
         public int hashCode() {
             final int prime = 31;
             int result = 1;
             result = prime * result + beanName.hashCode();
             result = prime * result + filter.hashCode();
             return result;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (this == obj)
                 return true;
             if (obj == null)
                 return false;
             if (getClass() != obj.getClass())
                 return false;
 
             ServiceDependency other = (ServiceDependency) obj;
 
             if (!beanName.equals(other.beanName))
                 return false;
 
             if (!filter.equals(other.filter))
                 return false;
 
             return true;
         }
 
         public String toString() {
             return this.filter + " " + this.beanName;
         }
     }
 }
