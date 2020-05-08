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
 package org.fusesource.meshkeeper.launcher;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
import java.util.concurrent.Callable;
 import java.util.concurrent.CountDownLatch;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fusesource.meshkeeper.MeshKeeper;
 import org.fusesource.meshkeeper.MeshKeeperFactory;
 import org.fusesource.meshkeeper.MeshKeeper.DistributionRef;
 import org.fusesource.meshkeeper.distribution.PluginClassLoader;
 
 /**
  * MeshContainer
  * <p>
  * Description:
  * </p>
  * 
  * @author cmacnaug
  * @version 1.0
  */
 public class MeshContainer implements MeshContainerService {
 
     public static MeshKeeper mesh;
     public static final Log LOG = LogFactory.getLog(LaunchAgent.class);
 
     private HashMap<String, Object> hosted = new HashMap<String, Object>();
     private String name;
 
     private CountDownLatch closeLatch = new CountDownLatch(1);
 
     public MeshContainer(String name) {
         this.name = name;
     }
 
     public synchronized <T> T host(String name, T object, Class<?>... interfaces) throws Exception {
 
         T proxy = null;
         if (!hosted.containsKey(name)) {
             LOG.info(this + " Hosting: " + name + ":" + object);
             proxy = (T) mesh.remoting().export(object);
             hosted.put(name, object);
         } else {
             throw new Exception("Already hosting an object with name " + name);
         }
 
         return proxy;
     }
 
     public synchronized void unhost(String name) throws Exception {
 
         Object d = hosted.get(name);
         if (d != null) {
             mesh.remoting().unexport(d);
         }
     }
 
     public void run(Runnable r) {
         //TODO a thread pool perhaps:
         mesh.getExecutorService().execute(r);
     }
 
     public <T> T call(Callable<T> c) throws Exception {
         return c.call();
     }
 
     public void close() {
         closeLatch.countDown();
     }
 
     public static MeshKeeper getMeshKeeper() {
         return mesh;
     }
 
     public String toString() {
         return name;
     }
 
     public static final void main(String[] args) {
 
         if (args.length == 0) {
             System.err.println("Expected registry path");
         }
         String path = args[0];
 
         MeshContainer container = new MeshContainer(path);
 
         try {
             //Running from RemoteBootstrapper?
             if (Boolean.getBoolean(RemoteBootstrap.BOOTSTRAP_PROPERTY)) {
                 //If we're running from the bootstrapper set the classloader
                 //to the bootstrap classloader:
                 MeshContainer.mesh = RemoteBootstrap.getMeshKeeper();
                 mesh.setUserClassLoader(new MeshContainerClassLoader());
             } else {
                 MeshContainer.mesh = MeshKeeperFactory.createMeshKeeper();
             }
             DistributionRef<MeshContainerService> ref = MeshContainer.getMeshKeeper().distribute(path, false, (MeshContainerService) container, MeshContainerService.class);
             System.out.println("Started MeshContainer: " + ref.getRegistryPath() + " cl: " + container.getClass().getClassLoader());
             container.closeLatch.await();
         } catch (Exception e) {
             LOG.error("MeshContainer error: ", e);
         } finally {
             try {
                 if (mesh != null) {
                     mesh.destroy();
                 }
             } catch (Exception e) {
                 LOG.error("MeshContainer error: ", e);
             }
         }
     }
 
     private static class MeshContainerClassLoader extends ClassLoader {
         ArrayList<ClassLoader> delegates = new ArrayList<ClassLoader>(2);
 
         MeshContainerClassLoader() {
             super(MeshContainer.class.getClassLoader());
 
             //Search first in bootstrap class loader:
             delegates.add(RemoteBootstrap.getClassLoader());
             //Then try the plugin class loader:
             delegates.add(PluginClassLoader.getDefaultPluginLoader());
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
 
 }
