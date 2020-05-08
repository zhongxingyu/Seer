 /**
  *  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.fusesource.meshkeeper.distribution;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fusesource.meshkeeper.MeshKeeper;
 import org.fusesource.meshkeeper.MeshKeeperFactory;
 import org.fusesource.meshkeeper.RegistryWatcher;
 import org.fusesource.meshkeeper.control.ControlServer;
 import org.fusesource.meshkeeper.distribution.event.EventClient;
 import org.fusesource.meshkeeper.distribution.event.EventClientFactory;
 import org.fusesource.meshkeeper.distribution.registry.RegistryClient;
 import org.fusesource.meshkeeper.distribution.registry.RegistryFactory;
 import org.fusesource.meshkeeper.distribution.remoting.RemotingClient;
 import org.fusesource.meshkeeper.distribution.remoting.RemotingFactory;
 import org.fusesource.meshkeeper.distribution.repository.RepositoryClient;
 import org.fusesource.meshkeeper.distribution.repository.RepositoryProviderFactory;
 
 /**
  * Distributor
  * <p>
  * Description:
  * </p>
  * 
  * @author cmacnaug
  * @version 1.0
  */
 class DefaultDistributor implements MeshKeeper {
 
     private Log log = LogFactory.getLog(this.getClass());
 
     private String registryUri;
     private String remotingUri;
     private String eventingUri;
     private String repositoryUri;
     private String workingDirectory;
     private String localRepositoryDirectory;
     
     private RemotingWrapper remoting;
     private RegistryClient registry;
     private EventClient eventing;
     private RepositoryClient repository;
     private LaunchClient launchClient;
     private ClassLoader userClassLoader;
     private UserFirstClassLoader pluginUserClassLoader;
 
     private boolean uuidCreator = true;
     private String uuid;
 
     private final HashMap<Object, DistributionRef<?>> distributed = new HashMap<Object, DistributionRef<?>>();
 
     private AtomicBoolean started = new AtomicBoolean(false);
     private AtomicBoolean destroyed = new AtomicBoolean(false);
 
     DefaultDistributor() {
         uuid = System.getProperty(MeshKeeperFactory.MESHKEEPER_UUID_PROPERTY);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.fusesource.meshkeeper.MeshKeeper#getExecutorService()
      */
     public ScheduledExecutorService getExecutorService() {
         return DistributorFactory.getExecutorService();
     }
 
     public void setUserClassLoader(ClassLoader classLoader) {
         if (userClassLoader != classLoader) {
             userClassLoader = classLoader;
             if (userClassLoader == null) {
                 pluginUserClassLoader = null;
             } else {
                 pluginUserClassLoader = new UserFirstClassLoader(userClassLoader);
             }
 
             if (registry != null) {
                 registry.setUserClassLoader(pluginUserClassLoader);
             }
             if (remoting != null) {
                 remoting.setUserClassLoader(pluginUserClassLoader);
             }
             if (eventing != null) {
                 eventing.setUserClassLoader(pluginUserClassLoader);
             }
             if (launchClient != null) {
                 launchClient.setUserClassLoader(pluginUserClassLoader);
             }
         }
 
     }
 
     public ClassLoader getUserClassLoader() {
         return userClassLoader;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.fusesource.meshkeeper.Distributor#getDistributorUri()
      */
     public String getRegistryConnectUri() {
         return registryUri;
     }
 
     private synchronized <T extends PluginClient> T createPluginClient(String uri, AbstractPluginFactory<T> factory, String lookupPath, String defaultUri) throws PluginCreationException {
         if (destroyed.get()) {
             throw new IllegalStateException("destroyed");
         }
         try {
             //Create Remoting client:
             if (uri == null) {
                 if (lookupPath != null) {
                     uri = registry().getRegistryObject(lookupPath);
                 }
                 if (uri == null) {
                     uri = defaultUri;
                 }
             }
             T ret = factory.create(uri);
             ret.setMeshKeeper(this);
             if (userClassLoader != null) {
                 ret.setUserClassLoader(pluginUserClassLoader);
             }
             ret.start();
             return ret;
         } catch (Exception e) {
             PluginCreationException re = new PluginCreationException("Unable to create plugin client from " + factory.getClass().getSimpleName(), e);
            if(log.isDebugEnabled()) {
                log.debug(re.getMessage(), re);
            }
             throw re;
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.fusesource.meshkeeper.MeshKeeper#getRegistry()
      */
     public synchronized Registry registry() throws PluginCreationException {
         if (registry == null) {
             synchronized (this) {
                 if (registry == null) {
                     registry = new RegistryWrapper(createPluginClient(registryUri, new RegistryFactory(), null, ControlServer.DEFAULT_REGISTRY_URI));
                 }
             }
         }
         return registry;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.fusesource.meshkeeper.MeshKeeper#getEventing()
      */
     public Eventing eventing() throws PluginCreationException {
         if (eventing == null) {
             synchronized (this) {
                 if (eventing == null) {
                     eventing = createPluginClient(eventingUri, new EventClientFactory(), ControlServer.EVENTING_URI_PATH, ControlServer.DEFAULT_EVENT_URI);
                 }
             }
         }
         return eventing;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.fusesource.meshkeeper.MeshKeeper#getRemoting()
      */
     public Remoting remoting() throws PluginCreationException {
         if (remoting == null) {
             synchronized (this) {
                 if (remoting == null) {
                     if (log.isDebugEnabled()) {
                         log.debug("Creating Remoting Client");
                     }
                     remoting = new RemotingWrapper(createPluginClient(remotingUri, new RemotingFactory(), ControlServer.REMOTING_URI_PATH, ControlServer.DEFAULT_REMOTING_URI));
 
                 }
             }
         }
         return remoting;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.fusesource.meshkeeper.MeshKeeper#getRepository()
      */
     public Repository repository() {
         if (repository == null) {
             if (destroyed.get()) {
                 throw new IllegalStateException("destroyed");
             }
             synchronized (this) {
                 if(repository == null)
                 {
                     try
                     {
                         repository = createPluginClient(repositoryUri,  new RepositoryProviderFactory(), ControlServer.REPOSITORY_URI_PATH, ControlServer.DEFAULT_REPOSITORY_URI);
                         if(localRepositoryDirectory == null) {
                             localRepositoryDirectory = workingDirectory + File.separator + "local-repo";
                         }
                         repository.setLocalRepoDir(localRepositoryDirectory);
                     }
                     catch (Exception e) {
                         RuntimeException re = new RuntimeException("Error creating repository client", e);
                        if(log.isDebugEnabled()) {
                            log.debug(re);
                        }
                         throw re;
                     }
                 }
             }
         }
 
         return repository;
     }
 
     public Launcher launcher() {
         if (launchClient == null) {
             if (destroyed.get()) {
                 throw new IllegalStateException("destroyed");
             }
             synchronized (this) {
                 if (launchClient == null) {
                     launchClient = new LaunchClient();
                     launchClient.setMeshKeeper(this);
                     try {
                         launchClient.start();
                     } catch (Exception e) {
                        if(log.isDebugEnabled()) {
                            log.debug("Error starting launch client", e);
                        }
                         throw new PluginCreationException("Error starting launch client", e);
                     }
 
                     if (userClassLoader != null) {
                         launchClient.setUserClassLoader(pluginUserClassLoader);
                     }
                 }
             }
         }
         return launchClient;
     }
 
     public synchronized void start() throws Exception {
         if (destroyed.get()) {
             throw new IllegalStateException("Can't start destoyed MeshKeeper");
         }
 
         //Default the user class loader to this class loader:
         setUserClassLoader(getClass().getClassLoader());
 
         try {
             //Start up the registry client:
             registry();
 
             //TODO Consider deferring startup of these?
             remoting();
             eventing();
             //repository();
         } catch (PluginCreationException re) {
             if (re.getCause() != null && re.getCause() instanceof Exception) {
                 throw (Exception) re.getCause();
             }
             throw re;
         }
 
         started.set(true);
     }
 
     public synchronized void destroy() throws Exception {
         if (destroyed.compareAndSet(false, true)) {
             log.debug("Shutting down");
 
             Exception first = null;
 
             if (launchClient != null) {
                 try {
                     launchClient.destroy();
                 } catch (Exception e) {
                     first = first == null ? e : first;
                 } finally {
                     launchClient = null;
                 }
             }
 
             if (eventing != null) {
                 try {
                     eventing.destroy();
                 } catch (Exception e) {
                     first = first == null ? e : first;
                 } finally {
                     eventing = null;
                 }
             }
 
             for (DistributionRef<?> ref : distributed.values()) {
                 ref.unregister();
             }
 
             if (remoting != null) {
                 try {
                     remoting.destroy();
                 } catch (Exception e) {
                     first = first == null ? e : first;
                 } finally {
                     remoting = null;
                 }
 
             }
 
             if (registry != null) {
 
                 //Clear out the registry of unique data if we are the creator
                 //of the uuid:
                 if (uuidCreator) {
                     try {
                         registry().removeRegistryData("/" + uuid, true);
                     } catch (Exception e) {
                         first = (first == null ? e : first);
                     }
                 }
 
                 try {
                     registry.destroy();
                 } catch (Exception e) {
                     first = first == null ? e : first;
                 } finally {
                     registry = null;
                 }
             }
 
             if (repository != null) {
                 try {
                     repository.destroy();
                 } catch (Exception e) {
                     first = first == null ? e : first;
                 } finally {
                     repository = null;
                 }
             }
 
             if (first != null) {
                 throw first;
             }
 
             log.debug("Shut down");
 
         }
     }
 
     public String toString() {
         return "Distributor [exporter: " + remoting + " registry: " + registry + "]";
     }
 
     /**
      * @param registryUri
      *            the registryUri to set
      */
     void setRegistryUri(String registryUri) {
         this.registryUri = registryUri;
     }
 
     /**
      * @param remotingUri
      *            Set the uri to used to create a remoting client
      */
     void setRemotingUri(String remotingUri) {
         this.remotingUri = remotingUri;
     }
 
     /**
      * @param eventingUri
      *            Set the uri used to create an eventing client
      */
     public void setEventingUri(String eventingUri) {
         this.eventingUri = eventingUri;
     }
 
     /**
      * @param repositoryProvider
      *            Set the uri used to create a repository client
      */
     public void setRepositoryUri(String repositoryUri) {
         this.repositoryUri = repositoryUri;
     }
 
     /**
      * @param directory
      */
     public void setWorkingDirectory(String workingDirectory) {
         this.workingDirectory = workingDirectory;
     }
     
     public void setLocalRepositoryDirectory(String localRepositoryDirectory) {
         this.localRepositoryDirectory = localRepositoryDirectory;
     }
 
     public String getLocalRepositoryDirectory() {
         return localRepositoryDirectory;
     }
 
     /**
      * Returns the URI of the meshkeeper registry to which this distributor is
      * connected.
      * 
      * @return the registryUri
      */
     public String getRegistryUri() {
         return registryUri;
     }
 
     @SuppressWarnings("unchecked")
     private <T, S extends T> DistributionRef<T> getRef(S object, boolean create, Class<?>... serviceInterfaces) {
         DistributionRef<T> ref = null;
         synchronized (distributed) {
 
             ref = (DistributionRef<T>) distributed.get(object);
             if (ref == null && create) {
                 ref = new DistributionRef<T>(object, serviceInterfaces);
                 distributed.put(object, ref);
             }
         }
         return ref;
     }
 
     /**
      * Exports an object, returning a stub that can be used to perform remote
      * method invocation for the object. This method additionally registers the
      * stub at the provided path in this distributor's registry.
      * 
      * @param <T>
      * @param object
      *            The object to export.
      * @param path
      *            The path in the registry at which to store the stub.
      * @return A reference to the distributed object.
      * @throws Exception
      *             If there is an error distributing the object.
      */
     public final <T, S extends T> DistributionRef<T> distribute(String path, boolean sequential, S object, Class<?>... serviceInterfaces) throws Exception {
         DistributionRef<T> ref = getRef((T) object, true, serviceInterfaces);
         ref.register(path, sequential);
         return ref;
     }
 
     /**
      * Unexports and unregisters a previously registered object.
      * 
      * @param object
      *            The object.
      * @throws Exception
      *             If there is an error exporting the object.
      */
     public final void undistribute(Object object) throws Exception {
         DistributionRef<?> ref = getRef(object, false);
         if (ref != null) {
             ref.unregister();
             synchronized (distributed) {
                 distributed.remove(object);
             }
         }
     }
 
     public String getUUID() {
         return setUUID("");
     }
 
     public String setUUID(String prefix) {
         if (uuid == null) {
             synchronized (this) {
                 if (uuid != null) {
                     return uuid;
                 }
 
                 uuidCreator = true;
 
                 if (prefix == null) {
                     prefix = "";
                 }
                 try {
                     String id = registry().addRegistryData(MeshKeeper.Registry.MESH_KEEPER_ROOT + "/UUID/" + prefix, true, null);
                     uuid = id.substring(1 + id.lastIndexOf("/"));
                 } catch (Exception e) {
                     throw new RuntimeException("UUID creation error", e);
                 }
             }
         }
 
         return uuid;
     }
 
     /**
      * RemotingWrapper
      * <p>
      * Description: The remoting wrapper intercepts export calls looking for
      * distribution refs.
      * </p>
      * 
      * @author cmacnaug
      * @version 1.0
      */
     private final class RemotingWrapper implements RemotingClient {
 
         private final RemotingClient delegate;
 
         RemotingWrapper(RemotingClient delegate) {
             this.delegate = delegate;
         }
 
         /**
          * @param meshKeeper
          *            the meshkeeper instance that created the plugin client
          */
         public void setMeshKeeper(MeshKeeper meshKeeper) {
             delegate.setMeshKeeper(meshKeeper);
         }
 
         /**
          * @return the meshkeeper instance that created the plugin client
          */
         public MeshKeeper getMeshKeeper() {
             return delegate.getMeshKeeper();
         }
 
         /**
          * Exports an object, returning a proxy that can be used to perform
          * remote method invocation for the object.
          * 
          * @param <T>
          * @param object
          *            The object to export.
          * @return A reference to the distributed object.
          * @throws Exception
          *             If there is an error distributing the object.
          */
         public final <T> T export(T object, Class<?>... serviceInterfaces) throws Exception {
             DistributionRef<T> ref = getRef(object, true, serviceInterfaces);
             ref.export();
             return ref.stub;
         }
 
         /**
          * Exports a object returning an RMI proxy to it, but to a specific
          * address. This allows users to register multiple objects sharing the
          * same interfaces to a single location thus allowing multicast method
          * call to all objects registered at the adress The proxy can then be
          * passed to other applications in the mesh to use via RMI. It is best
          * practice to unexport the object when it is no longer used.
          * 
          * @param <T>
          *            The type to which to cast the returned stub
          * @param obj
          *            The object to export
          * @param address
          *            The address (e.g. ServiceInterfaceFoo
          * @param interfaces
          *            The interfaces to which to limit the export.
          * @return The proxy that can be used to invoke method calls on the
          *         exported object.
          * @throws Exception
          *             If there is an error exporting
          */
         public <T> T exportMulticast(T obj, String address, Class<?>... interfaces) throws Exception {
             DistributionRef<T> ref = getRef(obj, true, interfaces);
             ref.setMultiCastPrefix(address);
             ref.export();
             return ref.stub;
         }
 
         /**
          * Gets a proxy object for a multicast export.
          * 
          * @param <T>
          * @param address
          *            The address to which multicast objects are exported.
          * @param interfaces
          *            The interfaces for the proxy.
          * @return The proxy for the multicast address.
          * @throws Exception
          *             If there is an error
          */
         @SuppressWarnings("unchecked")
         public <T> T getMulticastProxy(String address, Class<?> mainInterface, Class<?>... extraInterfaces) throws Exception {
             return (T) delegate.getMulticastProxy(address, mainInterface, extraInterfaces);
         }
 
         /**
          * Unexports an object. If the object's proxy was registered it will be
          * unregistered as well.
          * 
          * @param <T>
          * @param object
          *            The object to unexport.
          * @throws Exception
          *             If there is an error unexporting the object.
          */
         public final void unexport(Object object) throws Exception {
             DistributionRef<?> ref = getRef(object, false);
             if (ref != null) {
                 ref.unregister();
                 synchronized (distributed) {
                     distributed.remove(object);
                 }
             }
         }
 
         public void destroy() throws Exception {
             delegate.destroy();
         }
 
         public ClassLoader getUserClassLoader() {
             return delegate.getUserClassLoader();
         }
 
         public void setUserClassLoader(ClassLoader classLoader) {
             delegate.setUserClassLoader(classLoader);
         }
 
         public void start() throws Exception {
             delegate.start();
         }
 
         public Remoting delegate() {
             return delegate;
         }
 
         public String toString() {
             return delegate.toString();
         }
     }
 
     private final Remoting remotingDelegate() {
         remoting();
         return remoting.delegate();
     }
 
     /**
      * PluginCreationException
      * <p>
      * Description: Thrown when there is an error creating a meshkeeper plugin.
      * </p>
      * 
      * @author cmacnaug
      * @version 1.0
      */
     public static class PluginCreationException extends RuntimeException {
         /**
          * @param string
          * @param e
          */
         public PluginCreationException(String string, Exception e) {
             super(string, e);
         }
 
         private static final long serialVersionUID = 1L;
     }
 
     private class DistributionRef<D> implements MeshKeeper.DistributionRef<D> {
         private D object;
         private D stub;
         private String path;
         private String multiCastPrefix;
         private Class<?>[] serviceInterfaces;
 
         DistributionRef(D object, Class<?>... serviceInterfaces) {
             this.object = object;
             this.serviceInterfaces = serviceInterfaces;
         }
 
         public void setMultiCastPrefix(String multiCastPrefix) {
             this.multiCastPrefix = multiCastPrefix;
         }
 
         public D getProxy() {
             return stub;
         }
 
         public D getTarget() {
             return object;
         }
 
         public String getRegistryPath() {
             return path;
         }
 
         private synchronized D export() throws Exception {
             if (stub == null) {
                 if (multiCastPrefix != null) {
                     stub = (D) remotingDelegate().exportMulticast(object, multiCastPrefix, serviceInterfaces);
                 } else {
                     stub = (D) remotingDelegate().export(object, serviceInterfaces);
                 }
                 if (log.isDebugEnabled())
                     log.debug("Exported: " + object + " to " + stub);
             }
             return stub;
         }
 
         private synchronized String register(String path, boolean sequential) throws Exception {
             if (this.path == null) {
                 if (stub == null) {
                     export();
                 }
                 this.path = registry().addRegistryObject(path, sequential, (Serializable) stub);
             }
             return this.path;
         }
 
         private synchronized void unexport() throws Exception {
             if (stub != null) {
                 remotingDelegate().unexport(stub);
                 stub = null;
             }
 
             if (path != null) {
                 registry.removeRegistryData(path, true);
             }
         }
 
         private synchronized void unregister() throws Exception {
             unexport();
         }
     }
 
     /**
      * UserFirstClassLoader
      * <p>
      * This class loader is passed to Plugins allowing them to resolve user
      * classes first when necessary, while still including the PluginClassLoader
      * itself. This is typically useful for Remoting and Registry interfaces
      * that operate in the plugin classloader, but deserialize and load user
      * classes.
      * </p>
      * 
      * @author cmacnaug
      * @version 1.0
      */
     private static class UserFirstClassLoader extends ClassLoader {
         ArrayList<ClassLoader> delegates = new ArrayList<ClassLoader>(2);
         ArrayList<String> exceptions = new ArrayList<String>();
 
         UserFirstClassLoader(ClassLoader userLoader) {
             super(userLoader.getParent());
 
             exceptions.add("net.sf.cglib");
 
             //Search first in bootstrap class loader:
             delegates.add(userLoader);
             //Then try the plugin class loader:
             delegates.add(PluginClassLoader.getDefaultPluginLoader());
         }
 
         public Class<?> loadClass(String name) throws ClassNotFoundException {
             for (String e : exceptions) {
                 if (name.startsWith(e)) {
                     return PluginClassLoader.getDefaultPluginLoader().loadClass(name);
                 }
             }
 
             return super.loadClass(name);
 
         }
 
         protected Class<?> findClass(String name) throws ClassNotFoundException {
             //System.out.println("Finding class: " + name);
             //Look for an already loaded class:
             try {
                 return super.findClass(name);
             } catch (ClassNotFoundException cnfe) {
             }
 
             for (ClassLoader delegate : delegates) {
                 //Try the delegates
                 try {
                     return delegate.loadClass(name);
                 } catch (ClassNotFoundException cnfe) {
                 }
             }
 
             throw new ClassNotFoundException(name);
 
         }
 
         protected URL findResource(String name) {
             //Look for an already loaded class:
             URL url = super.findResource(name);
 
             for (ClassLoader delegate : delegates) {
                 if (url == null) {
                     url = delegate.getResource(name);
                 } else {
                     break;
                 }
             }
             return url;
         }
 
         protected Enumeration<URL> findResources(String name) throws IOException {
             Enumeration<URL> urls = null;
             try {
                 urls = super.findResources(name);
             } catch (IOException ioe) {
             }
 
             for (ClassLoader delegate : delegates) {
                 if (urls == null) {
                     //Try the plugin classloader:
                     try {
                         urls = delegate.getResources(name);
                     } catch (IOException ioe) {
                     }
                 } else {
                     break;
                 }
             }
             return urls;
         }
     }
 
     /**
      * RegistryWrapper Performs path substitutions. </p>
      * 
      * @author cmacnaug
      * @version 1.0
      */
     private static final String[] SYSTEM_ROOTS = new String[] { "/meshkeeper", "/zookeeper" };
 
     private class RegistryWrapper implements RegistryClient {
         final RegistryClient client;
 
         RegistryWrapper(RegistryClient client) {
             this.client = client;
         }
 
         public final String addRegistryData(String path, boolean sequential, byte[] data) throws Exception {
             path = doPathSubstitutions(path);
             return client.addRegistryData(path, sequential, data);
         }
 
         public final String addRegistryObject(String path, boolean sequential, Serializable o) throws Exception {
             path = doPathSubstitutions(path);
             return client.addRegistryObject(path, sequential, o);
         }
 
         public final void addRegistryWatcher(String path, RegistryWatcher watcher) throws Exception {
             path = doPathSubstitutions(path);
             client.addRegistryWatcher(path, watcher);
         }
 
         public final void destroy() throws Exception {
             client.destroy();
         }
 
         public final MeshKeeper getMeshKeeper() {
             return client.getMeshKeeper();
         }
 
         public final byte[] getRegistryData(String path) throws Exception {
             path = doPathSubstitutions(path);
             return client.getRegistryData(path);
         }
 
         @SuppressWarnings("unchecked")
         public final <T> T getRegistryObject(String path) throws Exception {
             path = doPathSubstitutions(path);
             return (T) client.getRegistryObject(path);
         }
 
         public final ClassLoader getUserClassLoader() {
             return client.getUserClassLoader();
         }
 
         public final Collection<String> list(String path, boolean recursive, String... filters) throws Exception {
             if (path.equals("*")) {
                 path = "/";
                 filters = null;
                 return client.list(path, recursive);
             }
 
             path = doPathSubstitutions(path);
             if (path.equals("/")) {
                 if (filters == null) {
                     filters = SYSTEM_ROOTS;
                 } else {
                     ArrayList<String> filterList = new ArrayList<String>(SYSTEM_ROOTS.length + filters.length);
                     filterList.addAll(Arrays.asList(SYSTEM_ROOTS));
                     filterList.addAll(Arrays.asList(filters));
                     filters = filterList.toArray(new String[0]);
                 }
 
             }
 
             return client.list(path, recursive, filters);
         }
 
         public final void removeRegistryData(String path, boolean recursive) throws Exception {
             path = doPathSubstitutions(path);
             client.removeRegistryData(path, recursive);
         }
 
         public final void removeRegistryWatcher(String path, RegistryWatcher watcher) throws Exception {
             path = doPathSubstitutions(path);
             client.removeRegistryWatcher(path, watcher);
         }
 
         public final void setMeshKeeper(MeshKeeper meshKeeper) {
             client.setMeshKeeper(meshKeeper);
         }
 
         public final void setUserClassLoader(ClassLoader classLoader) {
             client.setUserClassLoader(classLoader);
         }
 
         public final void start() throws Exception {
             client.start();
         }
 
         @SuppressWarnings("unchecked")
         public final <T> T waitForRegistration(String path, long timeout) throws TimeoutException, Exception {
             path = doPathSubstitutions(path);
             return (T) client.waitForRegistration(path, timeout);
         }
 
         @SuppressWarnings("unchecked")
         public final <T> Collection<T> waitForRegistrations(String path, int min, long timeout) throws TimeoutException, Exception {
             path = doPathSubstitutions(path);
             return (Collection<T>) client.waitForRegistrations(path, min, timeout);
         }
 
         /**
          * 
          * @param path
          * @return
          */
         private final String doPathSubstitutions(String path) {
             if (path == null) {
                 path = "";
             }
             //If the path isn't absolute prefix with UUID:
             if (!path.startsWith("/")) {
                 path = "/" + getUUID() + "/" + path;
             }
 
             return path;
         }
     }
 
 }
