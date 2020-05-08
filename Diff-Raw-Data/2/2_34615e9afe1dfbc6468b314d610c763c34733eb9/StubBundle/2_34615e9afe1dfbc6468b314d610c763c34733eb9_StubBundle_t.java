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
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.Version;
 
 /**
  * A stub testing implementation of {@link Bundle} as defined in section 6.1.4 of the OSGi Service Platform Core
  * Specification.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Threadsafe
  * 
  */
 public final class StubBundle implements Bundle {
 
     private static final Long DEFAULT_BUNDLE_ID = Long.valueOf(1);
 
     private static final String DEFAULT_SYMBOLIC_NAME = "org.eclipse.virgo.teststubs.osgi.testbundle";
 
     private static final Version DEFAULT_VERSION = Version.emptyVersion;
 
     private static final String DEFAULT_LOCATION = "/";
 
     private final Long bundleId;
 
     private final String symbolicName;
 
     private final Version version;
 
     private final String location;
 
     private volatile int state;
 
     private final Object stateMonitor = new Object();
 
     private volatile long lastModified;
 
     private final Object lastModifiedMonitor = new Object();
 
     private volatile StubBundleContext bundleContext;
 
     private final Object bundleContextMonitor = new Object();
 
     private volatile Dictionary<String, String> headers = new Hashtable<String, String>();
 
     private final Object headersMonitor = new Object();
 
     private volatile Dictionary<String, String> localizedHeaders = new Hashtable<String, String>();
 
     private final Object localizedHeadersMonitor = new Object();
 
     private final Map<String, Class<?>> loadClasses = new HashMap<String, Class<?>>();
 
     private final Object loadClassesMonitor = new Object();
 
     private final Map<String, URL> entries = new HashMap<String, URL>();
 
     private final Object entriesMonitor = new Object();
 
     private final Map<String, Enumeration<String>> entryPaths = new HashMap<String, Enumeration<String>>();
 
     private final Object entryPathsMonitor = new Object();
 
     private final Map<Object, Boolean> permissions = new HashMap<Object, Boolean>();
 
     private final Object permissionsMonitor = new Object();
 
     private final Map<String, URL> resource = new HashMap<String, URL>();
 
     private final Object resourceMonitor = new Object();
 
     private final Map<String, Enumeration<URL>> resources = new HashMap<String, Enumeration<URL>>();
 
     private final Object resourcesMonitor = new Object();
 
     private volatile FindEntriesDelegate findEntriesDelegate;
 
     private final Object findEntriesMonitor = new Object();
 
     private final List<StubServiceReference<Object>> registeredServices = new ArrayList<StubServiceReference<Object>>();
 
     private final Object registeredServicesMonitor = new Object();
 
     private final List<StubServiceReference<Object>> servicesInUse = new ArrayList<StubServiceReference<Object>>();
 
     private final Object servicesInUseMonitor = new Object();
 
     private volatile UpdateDelegate updateDelegate;
 
     private final Object updateDelegateMonitor = new Object();
 
     /**
      * Creates a new {@link StubServiceRegistration} and sets its initial state. This constructor sets
      * <code>bundleId</code> to <code>1</code>, <code>symbolicName</code> to
      * <code>org.eclipse.virgo.teststubs.osgi.testbundle</code>, <code>version</code> to <code>0.0.0</code>, and
      * <code>location</code> to <code>/</code>.
      */
     public StubBundle() {
         this(DEFAULT_SYMBOLIC_NAME, DEFAULT_VERSION);
     }
 
     /**
      * Creates a new {@link StubServiceRegistration} and sets its initial state. This constructor sets
      * <code>bundleId</code> to <code>1</code> and <code>location</code> to <code>/</code>.
      * 
      * @param symbolicName The symbolic name of this bundle
      * @param version The version of this bundle
      */
     public StubBundle(String symbolicName, Version version) {
         this(DEFAULT_BUNDLE_ID, symbolicName, version, DEFAULT_LOCATION);
     }
 
     /**
      * Creates a new {@link StubBundle} and sets its initial state
      * 
      * @param bundleId The id of this bundle
      * @param symbolicName The symbolic name of this bundle
      * @param version The version of this bundle
      * @param location The location of this bundle
      */
     public StubBundle(Long bundleId, String symbolicName, Version version, String location) {
         assertNotNull(bundleId, "bundleId");
         assertNotNull(symbolicName, "symbolicName");
         assertNotNull(version, "version");
         assertNotNull(location, "location");
 
         this.bundleId = bundleId;
         this.symbolicName = symbolicName;
         this.version = version;
         this.location = location;
         this.bundleContext = new StubBundleContext(this);
         this.state = STARTING;
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
         synchronized (this.findEntriesMonitor) {
             if (this.findEntriesDelegate != null) {
                 return this.findEntriesDelegate.findEntries(path, filePattern, recurse);
             }
             return null;
         }
     }
 
     /**
      * Sets the {@link FindEntriesDelegate} to use for all subsequent calls to
      * {@link #findEntries(String, String, boolean)}.
      * 
      * @param delegate the delegate to use
      * 
      * @return <code>this</code> instance of the {@link StubBundle}
      */
     public StubBundle setFindEntriesDelegate(FindEntriesDelegate delegate) {
         synchronized (this.findEntriesMonitor) {
             this.findEntriesDelegate = delegate;
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public BundleContext getBundleContext() {
         synchronized (this.bundleContextMonitor) {
             return this.bundleContext;
         }
     }
 
     /**
      * Sets the {@link BundleContext} to return for all subsequent calls to {@link #getBundleContext()}.
      * 
      * @param bundleContext The @{link BundleContext} to return
      * 
      * @return <code>this</code> instance of the {@link StubBundle}
      */
     public StubBundle setBundleContext(StubBundleContext bundleContext) {
         assertNotNull(bundleContext, "bundleContext");
         synchronized (bundleContextMonitor) {
             this.bundleContext = bundleContext;
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public long getBundleId() {
         return this.bundleId;
     }
 
     /**
      * {@inheritDoc}
      */
     public URL getEntry(String path) {
         synchronized (this.entriesMonitor) {
             return this.entries.get(path);
         }
     }
 
     /**
      * Adds a mapping from a path to a {@link URL} for all subsequent calls to {@link #getEntry(String)}.
      * 
      * @param path The path to map from
      * @param url The {@link URL} to map to
      * @return <code>this</code> instance of the {@link StubBundle}
      */
     public StubBundle addEntry(String path, URL url) {
         synchronized (this.entriesMonitor) {
             this.entries.put(path, url);
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Enumeration<String> getEntryPaths(String path) {
         synchronized (this.entryPathsMonitor) {
             return this.entryPaths.get(path);
         }
     }
 
     /**
      * Adds a mapping from a path to a {@link Enumeration} for all subsequent calls to {@link #getEntryPaths(String)}.
      * 
      * @param path The path to map from
      * @param paths The {@link Enumeration} to map to
      * @return <code>this</code> instance of the {@link StubBundle}
      */
     public StubBundle addEntryPaths(String path, Enumeration<String> paths) {
         synchronized (this.entryPathsMonitor) {
             this.entryPaths.put(path, paths);
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Dictionary<String, String> getHeaders() {
         synchronized (this.headersMonitor) {
             return shallowCopy(this.headers);
         }
     }
 
     /**
      * Adds a header mapping for all subsequent calls to {@link #getHeaders()}.
      * 
      * @param key The key to map from
      * @param value The value to map to
      * 
      * @return <code>this</code> instance of the {@link StubBundle}
      */
     public StubBundle addHeader(String key, String value) {
         synchronized (this.headersMonitor) {
             this.headers.put(key, value);
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Dictionary<String, String> getHeaders(String locale) {
         synchronized (this.localizedHeadersMonitor) {
             return shallowCopy(this.localizedHeaders);
         }
     }
 
     /**
      * Sets the localized headers to return for all subsequent calls to {@link #getHeaders(String)}.
      * 
      * @param dictionary The headers to return
      * 
      * @return <code>this</code> instance of the {@link StubBundle}
      */
     public StubBundle setLocalizedHeaders(Dictionary<String, String> dictionary) {
         synchronized (this.localizedHeadersMonitor) {
             this.localizedHeaders = dictionary;
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public long getLastModified() {
         synchronized (this.lastModifiedMonitor) {
             return this.lastModified;
         }
     }
 
     /**
      * Sets the last modified date to return for all subsequent calls to {@link #getLastModified()}. A call to any other
      * modifying method will update this.
      * 
      * @param lastModified The new last modified date
      * @return <code>this</code> instance of the {@link StubBundle}
      */
     public StubBundle setLastModified(long lastModified) {
         synchronized (this.lastModifiedMonitor) {
             this.lastModified = lastModified;
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public String getLocation() {
         return this.location;
     }
 
     /**
      * {@inheritDoc}
      */
     public ServiceReference<?>[] getRegisteredServices() {
         synchronized (this.registeredServicesMonitor) {
             if (this.registeredServices.isEmpty()) {
                 return null;
             }
             return this.registeredServices.toArray(new ServiceReference[this.registeredServices.size()]);
         }
     }
 
     /**
      * Adds a {@link ServiceReference} for all subsequent calls to {@link #getRegisteredServices()}.
      * 
      * @param serviceReferences The {@link ServiceReference}s
      * @return <code>this</code> instance of the {@link StubBundle}
      */
     public StubBundle addRegisteredService(StubServiceReference<Object>... serviceReferences) {
         synchronized (this.registeredServicesMonitor) {
             for (StubServiceReference<Object> serviceReference : serviceReferences) {
                 serviceReference.setBundle(this);
                 this.registeredServices.add(serviceReference);
             }
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public URL getResource(String name) {
         synchronized (this.resourceMonitor) {
             return this.resource.get(name);
         }
     }
 
     /**
      * Adds a mapping from a name to a {@link URL} for all subsequent calls to {@link #getResource(String)}.
      * 
      * @param name The name to map from
      * @param url The {@link URL} to map to
      * @return <code>this</code> instance of the {@link StubBundle}
      */
     public StubBundle addResource(String name, URL url) {
         synchronized (this.resourceMonitor) {
             this.resource.put(name, url);
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Enumeration<URL> getResources(String name) throws IOException {
         synchronized (this.resourcesMonitor) {
             return this.resources.get(name);
         }
     }
 
     /**
      * Adds a mapping from a name to a {@link Enumeration} for all subsequent calls to {@link #getResources(String)}.
      * 
      * @param name The name to map from
      * @param resources The {@link Enumeration} to map to
      * @return <code>this</code> instance of the {@link StubBundle}
      */
     public StubBundle addResources(String name, Enumeration<URL> resources) {
         synchronized (this.resourcesMonitor) {
             this.resources.put(name, resources);
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public ServiceReference<?>[] getServicesInUse() {
         synchronized (this.servicesInUseMonitor) {
             if (this.servicesInUse.isEmpty()) {
                 return null;
             }
             return this.servicesInUse.toArray(new ServiceReference[this.servicesInUse.size()]);
         }
     }
 
     /**
      * Adds a {@link ServiceReference} for all subsequent calls to {@link #getServicesInUse()}.
      * 
      * @param serviceReferences The {@link ServiceReference}s
      * @return <code>this</code> instance of the {@link StubBundle}
      */
     public StubBundle addServiceInUse(StubServiceReference<Object>... serviceReferences) {
         synchronized (this.servicesInUseMonitor) {
             for (StubServiceReference<Object> serviceReference : serviceReferences) {
                 serviceReference.addUsingBundles(this);
                 this.servicesInUse.add(serviceReference);
             }
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public int getState() {
         synchronized (this.stateMonitor) {
             return this.state;
         }
     }
 
     /**
      * Sets the state to return for all subsequent calls to {@link #getState()}. A call to any state modifying method
      * will change this value.
      * 
      * @param state The state to return
      * 
      * @return <code>this</code> instance of the {@link StubBundle}
      */
     public StubBundle setState(int state) {
         synchronized (this.stateMonitor) {
             this.state = state;
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public String getSymbolicName() {
         return this.symbolicName;
     }
 
     /**
      * {@inheritDoc}
      */
     public Version getVersion() {
         return this.version;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean hasPermission(Object permission) {
         synchronized (this.permissionsMonitor) {
             if (this.permissions.containsKey(permission)) {
                 return this.permissions.get(permission);
             }
             return true;
         }
     }
 
     /**
      * Adds a mapping from a permission to a {@link Boolean} of whether that permission is valid for all subsequent
      * calls to {@link #hasPermission(Object)}.
      * 
      * @param permission the permission to add
      * @param hasPermission whether this permission is valid
      * @return <code>this</code> instance of the {@link StubBundle}
      */
     public StubBundle addPermission(Object permission, boolean hasPermission) {
         synchronized (this.permissionsMonitor) {
             this.permissions.put(permission, hasPermission);
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Class<?> loadClass(String name) throws ClassNotFoundException {
         synchronized (this.loadClassesMonitor) {
             if (this.loadClasses.containsKey(name)) {
                 return this.loadClasses.get(name);
             }
             throw new ClassNotFoundException("'" + name + "' cannot be loaded");
         }
     }
 
     /**
      * Adds a mapping from a class name to a {@link Class} for all subsequent calls to {@link #loadClass(String)}.
      * 
      * @param name The name of the class to map from
      * @param clazz The class to map to
      * @return <code>this</code> instance of the {@link StubBundle}
      */
     public StubBundle addLoadClass(String name, Class<?> clazz) {
         synchronized (this.loadClassesMonitor) {
             this.loadClasses.put(name, clazz);
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void start() throws BundleException {
         start(0);
     }
 
     /**
      * {@inheritDoc}
      */
     public void start(int options) throws BundleException {
         synchronized (this.stateMonitor) {
             if (this.getState() == ACTIVE) {
                 return;
             }
             setState(RESOLVED);
             setState(STARTING);
             setState(ACTIVE);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void stop() throws BundleException {
         stop(0);
     }
 
     /**
      * {@inheritDoc}
      */
     public void stop(int options) throws BundleException {
         synchronized (this.stateMonitor) {
             if (this.getState() != ACTIVE) {
                 return;
             }
 
             setState(STOPPING);
             synchronized (this.registeredServicesMonitor) {
                 for (StubServiceReference<Object> serviceReference : this.registeredServices) {
                     serviceReference.setBundle(null);
                 }
                 this.registeredServices.clear();
             }
 
             synchronized (this.servicesInUseMonitor) {
                 for (StubServiceReference<Object> serviceReference : this.servicesInUse) {
                     serviceReference.removeUsingBundles(this);
                 }
                 this.servicesInUse.clear();
             }
 
             setState(RESOLVED);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void uninstall() throws BundleException {
         synchronized (this.stateMonitor) {
             int initialState = getState();
             stopBundleIfNeeded(initialState);
             setState(UNINSTALLED);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void update() throws BundleException {
         synchronized (this.updateDelegateMonitor) {
             synchronized (this.stateMonitor) {
                 int initialState = getState();
                 stopBundleIfNeeded(initialState);
 
                 if (this.updateDelegate != null) {
                     this.updateDelegate.update(this);
                 }
 
                 setState(INSTALLED);
                 startBundleIfNeeded(initialState);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void update(InputStream in) throws BundleException {
         update();
     }
 
     /**
      * Sets the {@link UpdateDelegate} to use for all subsequent calls to {@link #update()} or
      * {@link #update(InputStream)}.
      * 
      * @param delegate the delegate to use
      * 
      * @return <code>this</code> instance of the {@link StubBundle}
      */
     public StubBundle setUpdateDelegate(UpdateDelegate delegate) {
         synchronized (this.updateDelegateMonitor) {
             this.updateDelegate = delegate;
             return this;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + bundleId.hashCode();
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         StubBundle other = (StubBundle) obj;
         if (!bundleId.equals(other.bundleId)) {
             return false;
         }
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         return String.format("id: %d, symbolic name: %s, version: %s, state: %d", this.bundleId, this.symbolicName, this.version, this.state);
     }
 
     private void startBundleIfNeeded(int initialState) throws BundleException {
         if (initialState == ACTIVE) {
             this.start();
         }
     }
 
     private void stopBundleIfNeeded(int initialState) throws BundleException {
         if (initialState == Bundle.ACTIVE || initialState == Bundle.STARTING || initialState == Bundle.STOPPING) {
             this.stop();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public int compareTo(Bundle o) {
        int bundleIdCompare = Long.valueOf(o.getBundleId()).compareTo(this.bundleId);
         if (bundleIdCompare != 0) {
             return bundleIdCompare;
         }
         int symbolicNameCompare = o.getSymbolicName().compareTo(this.symbolicName);
         if (symbolicNameCompare != 0) {
             return symbolicNameCompare;
         }
         int bundleVersionCompare = o.getVersion().compareTo(this.version);
         if (bundleVersionCompare != 0) {
             return bundleVersionCompare;
         }
         int bundleLocationCompare = o.getLocation().compareTo(this.location);
         if (bundleLocationCompare != 0) {
             return bundleLocationCompare;
         }
         return 0;
     }
 
     /**
      * {@inheritDoc}
      */
     public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int signersType) {
         return new HashMap<X509Certificate, List<X509Certificate>>();
     }
 
     /**
      * {@inheritDoc}
      */
     public <A> A adapt(Class<A> type) {
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public File getDataFile(String filename) {
         return null;
     }
 
 }
