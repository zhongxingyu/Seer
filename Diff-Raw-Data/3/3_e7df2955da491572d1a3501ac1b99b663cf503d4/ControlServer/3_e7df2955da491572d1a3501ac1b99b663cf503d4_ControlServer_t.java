 /**************************************************************************************
  * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
  * http://fusesource.com                                                              *
  * ---------------------------------------------------------------------------------- *
  * The software in this package is published under the terms of the AGPL license      *
  * a copy of which has been included with this distribution in the license.txt file.  *
  **************************************************************************************/
 package org.fusesource.meshkeeper.control;
 
 import java.io.File;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fusesource.meshkeeper.distribution.registry.RegistryClient;
 import org.fusesource.meshkeeper.distribution.registry.RegistryFactory;
 import org.fusesource.meshkeeper.MeshKeeperFactory;
 
 /**
  * ControlServer
  * <p>
  * Description: The control server hosts the servers used to facilitate the
  * distributed test system.
  * 
  * </p>
  * 
  * @author cmacnaug
  * @version 1.0
  */
 public class ControlServer {
 
     Log log = LogFactory.getLog(ControlServer.class);
     private static final ControlServiceFactory SERVICE_FACTORY = new ControlServiceFactory();
 
     public static final String DEFAULT_JMS_URI = "activemq:tcp://localhost:4041";
     public static final String DEFAULT_REMOTING_URI = "rmiviajms:" + DEFAULT_JMS_URI;
     public static final String DEFAULT_REGISTRY_URI = "zk:tcp://localhost:4040";
     public static final String DEFAULT_EVENT_URI = "eventviajms:" + DEFAULT_JMS_URI;
 
     public static final String REMOTING_URI_PATH = "/control/remoting-uri";
     public static final String EVENTING_URI_PATH = "/control/eventing-uri";
     public static final String REPOSITORY_URI_PATH = "/control/repository-uri";
 
     ControlService rmiServer;
     ControlService registryServer;
     RegistryClient registry;
 
     private String jmsUri = DEFAULT_JMS_URI;
     private String registryUri = DEFAULT_REGISTRY_URI;
     private String repositoryUri = System.getProperty("meshkeeper.repository.uri");
 
     private String directory = MeshKeeperFactory.getDefaultServerDirectory().getPath();
     private Thread shutdownHook;
 
     public void start() throws Exception {
 
         shutdownHook = new Thread("MeshKeeper Control Server Shutdown Hook") {
             public void run() {
                 log.debug("Executing Shutdown Hook for " + ControlServer.this);
                 try {
                     ControlServer.this.destroy();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         };
 
         //Start the jms server:
         log.info("Creating JMS Server at " + jmsUri);
         final String SLASH = File.separator;
         try {
             rmiServer = SERVICE_FACTORY.create(jmsUri);
             rmiServer.setDirectory(directory + SLASH + "jms");
             rmiServer.start();
             log.info("JMS Server started: " + rmiServer.getName());
 
         } catch (Exception e) {
             log.error(e);
             destroy();
             throw new Exception("Error starting JMS Server", e);
         }
 
         //Start the registry server:
         log.info("Creating Registry Server at " + registryUri);
         try {
             registryServer = SERVICE_FACTORY.create(registryUri);
             registryServer.setDirectory(directory + SLASH + "registry");
             registryServer.start();
             log.info("Registry Server started: " + registryServer.getName() + " uri: " + registryServer.getServiceUri());
 
         } catch (Exception e) {
             log.error("Error starting regisry server", e);
             destroy();
             throw new Exception("Error starting Registry Server", e);
         }
 
         if (repositoryUri == null) {
             // Just default it to a local directory..  Only really useful in the local test case.
             repositoryUri = new File(directory + SLASH + "repository").toURI().toString();
         }
 
         //Connect to the registry and publish service connection info:
         try {
 
            log.info("Connecting to registry server at " + registryUri);
            registry = new RegistryFactory().create(registryServer.getServiceUri());
 
             //Register the control services:
 
             //(note that we delete these first since
             //in some instances zoo-keeper doesn't shutdown cleanly and hangs
             //on to file handles so that the registry isn't purged:
             registry.removeRegistryData(REMOTING_URI_PATH, true);
             registry.addRegistryObject(REMOTING_URI_PATH, false, new String("rmiviajms:" + rmiServer.getServiceUri()));
             log.info("Registered RMI control server at " + REMOTING_URI_PATH + "=rmiviajms:" + rmiServer.getServiceUri());
 
             registry.removeRegistryData(EVENTING_URI_PATH, true);
             registry.addRegistryObject(EVENTING_URI_PATH, false, new String("eventviajms:" + rmiServer.getServiceUri()));
             log.info("Registered event server at " + EVENTING_URI_PATH + "=eventviajms:" + rmiServer.getServiceUri());
 
             registry.removeRegistryData(REPOSITORY_URI_PATH, true);
             registry.addRegistryObject(REPOSITORY_URI_PATH, false, repositoryUri);
             log.info("Registered repository uri at " + REPOSITORY_URI_PATH + "=" + repositoryUri);
 
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             destroy();
             throw new Exception("Error registering control server", e);
         }
     }
 
     public void destroy() throws Exception {
 
         if (Thread.currentThread() != shutdownHook) {
             Runtime.getRuntime().removeShutdownHook(shutdownHook);
         }
 
         if (registry != null) {
             registry.destroy();
             registry = null;
         }
 
         log.info("Shutting down registry server");
         if (registryServer != null) {
             try {
                 registryServer.destroy();
             } finally {
                 registryServer = null;
             }
         }
 
         log.info("Shutting down rmi server");
         if (rmiServer != null) {
             try {
                 rmiServer.destroy();
             } finally {
                 rmiServer = null;
             }
         }
 
         synchronized (this) {
             notifyAll();
         }
 
     }
 
     public void join() throws InterruptedException {
         synchronized (this) {
             wait();
         }
     }
 
     public void setRepositoryUri(String repositoryProvider) {
         this.repositoryUri = repositoryProvider;
     }
 
     public String getRepositoryUri() {
         return repositoryUri;
     }
 
     public String getJmsUri() {
         return jmsUri;
     }
 
     public void setJmsUri(String jmsProvider) {
         this.jmsUri = jmsProvider;
     }
 
     public String getRegistryUri() {
         return registryUri;
     }
 
     public void setRegistryUri(String registryProvider) {
         this.registryUri = registryProvider;
     }
 
     public void setDirectory(String directory) {
         this.directory = directory;
     }
 
     public String getDirectory() {
         return directory;
     }
 }
