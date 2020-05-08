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
 
 package org.eclipse.virgo.teststubs.osgi.framework;
 
 import static org.eclipse.virgo.teststubs.osgi.internal.Assert.assertNotNull;
 import static org.eclipse.virgo.teststubs.osgi.internal.Duplicator.shallowCopy;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.BundleListener;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Filter;
 import org.osgi.framework.FrameworkListener;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.framework.Version;
 
 import org.eclipse.virgo.teststubs.osgi.support.TrueFilter;
 
 /**
  * A stub testing implementation of {@link BundleContext} as defined in section 6.1.6 of the OSGi Service Platform Core
  * Specification.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Threadsafe
  * 
  */
 public final class StubBundleContext implements BundleContext {
 
     private final StubBundle bundle;
 
     private final Object bundleMonitor = new Object();
 
     private final List<BundleListener> bundleListeners = new ArrayList<BundleListener>();
 
     private final Object bundleListenersMonitor = new Object();
 
     private final List<FrameworkListener> frameworkListeners = new ArrayList<FrameworkListener>();
 
     private final Object frameworkListenersMonitor = new Object();
 
     private final List<ServiceListener> serviceListeners = new ArrayList<ServiceListener>();
 
     private final Object serviceListenersMonitor = new Object();
 
     private volatile long installedBundleId = Long.valueOf(2);
 
     private final Map<Long, StubBundle> installedBundles = new HashMap<Long, StubBundle>();
 
     private final Object installedBundlesMonitor = new Object();
 
     private final List<StubServiceRegistration<Object>> serviceRegistrations = new ArrayList<StubServiceRegistration<Object>>();
 
     private final Map<StubServiceReference<Object>, Object> services = new HashMap<StubServiceReference<Object>, Object>();
 
     private final Object servicesMonitor = new Object();
 
     private final Map<String, File> dataFiles = new HashMap<String, File>();
 
     private final Object dataFilesMonitor = new Object();
 
     private final Map<String, String> properties = new HashMap<String, String>();
 
     private final Object propertiesMonitor = new Object();
 
     private final Map<String, Filter> filters = new HashMap<String, Filter>();
 
     private final Object filtersMonitor = new Object();
 
     /**
      * Creates a new {@link StubBundleContext} and sets its initial state
      */
     public StubBundleContext() {
         this(new StubBundle());
     }
 
     /**
      * Creates a new {@link StubBundleContext} and sets its initial state
      * 
      * @param bundle The context bundle
      */
     public StubBundleContext(StubBundle bundle) {
         assertNotNull(bundle, "bundle");
         this.bundle = bundle;
     }
 
     /**
      * {@inheritDoc}
      */
     public void addBundleListener(BundleListener listener) {
         synchronized (this.bundleListenersMonitor) {
             this.bundleListeners.add(listener);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void addFrameworkListener(FrameworkListener listener) {
         synchronized (this.frameworkListenersMonitor) {
             this.frameworkListeners.add(listener);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void addServiceListener(ServiceListener listener) {
         try {
             addServiceListener(listener, null);
         } catch (InvalidSyntaxException e) {
             // In theory this exception can never be thrown
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
         synchronized (this.serviceListenersMonitor) {
             this.serviceListeners.add(listener);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Filter createFilter(String filter) throws InvalidSyntaxException {
         synchronized (this.filtersMonitor) {
             if (filter == null) {
                 throw new NullPointerException();
             }
             if (!this.filters.containsKey(filter)) {
                 throw new InvalidSyntaxException(String.format(
                     "You must first add a filter mapping for '%s' with the addFilter(String, Filter) method", filter), filter);
             }
             return this.filters.get(filter);
         }
     }
 
     /**
      * Adds a mapping from a filter string to a {@link Filter} for all subsequent calls to {@link #createFilter(String)}
      * 
      * @param filterString The filterString to map from
      * @param filter The {@link Filter} to map to
      * @return <code>this</code> instance of the {@link StubBundleContext}
      */
     public StubBundleContext addFilter(String filterString, Filter filter) {
         synchronized (this.filtersMonitor) {
             this.filters.put(filterString, filter);
             return this;
         }
     }
 
     /**
      * Adds filters for all subsequent calls to {@link #createFilter(String)}
      * 
      * @param filters The {@link Filter}s to add
      * @return <code>this</code> instance of the {@link StubBundleContext}
      */
     public StubBundleContext addFilter(Filter... filters) {
         for (Filter filter : filters) {
             addFilter(filter.toString(), filter);
         }
         return this;
     }
 
     /**
      * {@inheritDoc}
      */
     public ServiceReference<?>[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
         return getServiceReferences(clazz, filter);
     }
 
     /**
      * {@inheritDoc}
      */
     public Bundle getBundle() {
         synchronized (this.bundleMonitor) {
             return this.bundle;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Bundle getBundle(long id) {
         synchronized (this.installedBundlesMonitor) {
             return this.installedBundles.get(id);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Bundle[] getBundles() {
         synchronized (this.installedBundlesMonitor) {
             return this.installedBundles.values().toArray(new Bundle[this.installedBundles.values().size()]);
         }
     }
 
     /**
      * Adds a collection of {@link Bundle}s to this {@link BundleContext} to be returned for all subsequent calls to
      * {@link #getBundle(long)} or {@link #getBundles()}.
      * 
      * @param bundles The bundles to add
      * @return <code>this</code> instance of the {@link StubBundleContext}
      */
     public StubBundleContext addInstalledBundle(StubBundle... bundles) {
         synchronized (this.installedBundlesMonitor) {
             for (StubBundle bundle : bundles) {
                 this.installedBundles.put(bundle.getBundleId(), bundle);
             }
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public File getDataFile(String filename) {
         synchronized (this.dataFilesMonitor) {
             return this.dataFiles.get(filename);
         }
     }
 
     /**
      * Adds a mapping from a filename to a {@link File} for all subsequent calls to {@link #getDataFile(String)}.
      * 
      * @param filename The filename to map from
      * @param file The {@link File} to map to
      * @return <code>this</code> instance of the {@link StubBundleContext}
      */
     public StubBundleContext addDataFile(String filename, File file) {
         synchronized (this.dataFilesMonitor) {
             this.dataFiles.put(filename, file);
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public String getProperty(String key) {
         synchronized (this.propertiesMonitor) {
             return this.properties.get(key);
         }
     }
 
     /**
      * Adds a mapping from a key to a value for all subsequent calls to {@link #getProperty(String)}.
      * 
      * @param key The key to map from
      * @param value The value to map to
      * @return <code>this</code> instance of the {@link StubBundleContext}
      */
     public StubBundleContext addProperty(String key, String value) {
         synchronized (this.propertiesMonitor) {
             this.properties.put(key, value);
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public <S> S getService(ServiceReference<S> reference) {
         synchronized (this.servicesMonitor) {
             if (serviceUnregistered(reference)) {
                 return null;
             }
 
             return (S) this.services.get(reference);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public ServiceReference<?> getServiceReference(String clazz) {
         ServiceReference<?>[] serviceReferences = null;
         try {
             serviceReferences = getServiceReferences(clazz, null);
         } catch (InvalidSyntaxException e) {
             // In theory this exception can never be thrown
         }
 
         if (serviceReferences == null) {
             return null;
         } else if (serviceReferences.length == 1) {
             return serviceReferences[0];
         } else {
             Arrays.sort(serviceReferences);
             return serviceReferences[serviceReferences.length - 1];
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public ServiceReference<?>[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
         synchronized (this.servicesMonitor) {
             List<ServiceReference<?>> candidateReferences = new ArrayList<ServiceReference<?>>();
 
             Filter f = getFilter(filter);
             for (ServiceReference<?> serviceReference : this.services.keySet()) {
                 String[] objectClasses = (String[]) serviceReference.getProperty(Constants.OBJECTCLASS);
                 if (f.match(serviceReference) && matchesClass(clazz, objectClasses)) {
                     candidateReferences.add(serviceReference);
                 }
             }
 
             if (candidateReferences.isEmpty()) {
                 return null;
             } else {
                 return candidateReferences.toArray(new ServiceReference[candidateReferences.size()]);
             }
         }
     }
 
     private Filter getFilter(String filter) throws InvalidSyntaxException {
         if (filter == null) {
             return new TrueFilter();
         } else {
             return createFilter(filter);
         }
     }
 
     private boolean matchesClass(String clazz, String[] objectClasses) {
         if (clazz == null) {
             return true;
         }
 
         for (String objectClass : objectClasses) {
             if (clazz.equals(objectClass)) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public Bundle installBundle(String location) throws BundleException {
         StubBundle bundle = new StubBundle(this.installedBundleId++, location, Version.emptyVersion, location);
         bundle.setState(Bundle.INSTALLED);
         return bundle;
     }
 
     /**
      * {@inheritDoc}
      */
     public Bundle installBundle(String location, InputStream input) throws BundleException {
         return installBundle(location);
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public ServiceRegistration<?> registerService(String[] clazzes, Object service, Dictionary<String, ?> properties) {
         StubServiceRegistration serviceRegistration = createServiceRegistration(clazzes, properties);
         StubServiceReference serviceReference = createServiceReference(clazzes, service, serviceRegistration);
 
         synchronized (this.servicesMonitor) {
             this.serviceRegistrations.add(serviceRegistration);
             this.services.put(serviceReference, service);
         }
 
         return serviceRegistration;
     }
 
     private StubServiceRegistration<Object> createServiceRegistration(String[] clazzes, Dictionary<String, ?> properties) {
         StubServiceRegistration<Object> serviceRegistration = new StubServiceRegistration<Object>(this, clazzes);
         serviceRegistration.setProperties(properties);
         return serviceRegistration;
     }
 
     private <S> StubServiceReference<S> createServiceReference(String[] clazzes, Object service, StubServiceRegistration<S> serviceRegistration) {
         StubServiceReference<S> serviceReference = new StubServiceReference<S>(serviceRegistration);
         serviceRegistration.setServiceReference(serviceReference);
         return serviceReference;
     }
 
     /**
      * {@inheritDoc}
      */
     public ServiceRegistration<?> registerService(String clazz, Object service, Dictionary<String, ?> properties) {
         return registerService(new String[] { clazz }, service, properties);
     }
 
     /**
      * Gets the collection of {@link ServiceRegistration}s for this {@link BundleContext}
      * 
      * @return The collection of {@link ServiceRegistration}s
      */
     public List<StubServiceRegistration<Object>> getServiceRegistrations() {
         synchronized (this.servicesMonitor) {
             return shallowCopy(this.serviceRegistrations);
         }
     }
 
     /**
      * Removes a collection of {@link ServiceRegistration}s from this {@link StubBundleContext} to be returned for all
      * subsequent calls to {@link #getServiceReference(String)} and {@link #getServiceReferences(String, String)}.
      * 
      * @param serviceRegistrations The service registrations to remove
      * @return <code>this</code> instance of the {@link StubBundleContext}
      */
     public StubBundleContext removeRegisteredService(ServiceRegistration<?>... serviceRegistrations) {
         synchronized (this.servicesMonitor) {
             this.serviceRegistrations.removeAll(Arrays.asList(serviceRegistrations));
            for (ServiceRegistration registration: serviceRegistrations) {
                this.services.remove(registration.getReference());
            }
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeBundleListener(BundleListener listener) {
         synchronized (this.bundleListenersMonitor) {
             this.bundleListeners.remove(listener);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeFrameworkListener(FrameworkListener listener) {
         synchronized (this.frameworkListenersMonitor) {
             this.frameworkListeners.remove(listener);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeServiceListener(ServiceListener listener) {
         synchronized (this.serviceListenersMonitor) {
             this.serviceListeners.remove(listener);
         }
     }
 
     /**
      * Gets the collection of {@link FrameworkListener}s for this {@link BundleContext}
      * 
      * @return The collection of {@link BundleListener}s
      */
     public List<FrameworkListener> getFrameworkListeners() {
         synchronized (this.frameworkListenersMonitor) {
             return shallowCopy(this.frameworkListeners);
         }
     }
 
     /**
      * Gets the collection of {@link BundleListener}s for this {@link BundleContext}
      * 
      * @return The collection of {@link BundleListener}s
      */
     public List<BundleListener> getBundleListeners() {
         synchronized (this.bundleListenersMonitor) {
             return shallowCopy(this.bundleListeners);
         }
     }
 
     /**
      * Gets the collection of {@link ServiceListener}s for this {@link BundleContext}
      * 
      * @return The collection of {@link ServiceListener}s
      */
     public List<ServiceListener> getServiceListeners() {
         synchronized (this.serviceListenersMonitor) {
             return shallowCopy(this.serviceListeners);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean ungetService(ServiceReference<?> reference) {
         return !serviceUnregistered(reference);
     }
 
     /**
      * @return Returns the context bundle for this {@link BundleContext}
      */
     public StubBundle getContextBundle() {
         synchronized (this.bundleMonitor) {
             return this.bundle;
         }
     }
 
     private boolean serviceUnregistered(ServiceReference<?> reference) {
         return ((StubServiceReference<?>) reference).getServiceRegistration().getUnregistered();
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
         return (ServiceRegistration<S>) registerService(clazz.getName(), service, properties);
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
         return (ServiceReference<S>) getServiceReference(clazz.getName());
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter) throws InvalidSyntaxException {
         Collection<ServiceReference<S>> references = new ArrayList<ServiceReference<S>>();
         ServiceReference<S>[] matchingReferences = (ServiceReference<S>[]) getServiceReferences(clazz.getName(), filter);
         if (matchingReferences != null) {
             for (ServiceReference<S> reference : matchingReferences) {
                 references.add(reference);
             }
         }
         return references;
     }
 
     /**
      * {@inheritDoc}
      */
     public Bundle getBundle(String location) {
         return null;
     }
 }
