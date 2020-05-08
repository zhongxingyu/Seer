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
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fusesource.meshkeeper.HostProperties;
 import org.fusesource.meshkeeper.LaunchDescription;
 import org.fusesource.meshkeeper.MeshKeeper;
 import org.fusesource.meshkeeper.MeshKeeperFactory;
 import org.fusesource.meshkeeper.MeshProcess;
 import org.fusesource.meshkeeper.MeshProcessListener;
 import org.fusesource.meshkeeper.util.internal.FileSupport;
 
 /**
  * @author chirino
  */
 public class LaunchAgent implements LaunchAgentService {
     public static final long CLEANUP_TIMEOUT = 60000;
     public static final String LOCAL_REPO_PROP = "org.fusesource.testrunner.localRepoDir";
     public static final Log LOG = LogFactory.getLog(LaunchAgent.class);
 
     static final public String PROPAGATED_SYSTEM_PROPERTIES[] = new String[] { "meshkeeper.home", "meshkeeper.base", "mop.base", "mop.online", "mop.allways-check-local-repo" };
 
     private String exclusiveOwner;
 
     private String agentId; //The unique identifier for this agent (specified in ini file);
     private boolean started = false;
     private File directory = MeshKeeperFactory.getDefaultAgentDirectory();
 
     //ProcessHandlers:
     private final Map<Integer, LocalProcess> processes = new HashMap<Integer, LocalProcess>();
     int pidCounter = 0;
     private Thread shutdownHook;
 
     private HostPropertiesImpl properties = new HostPropertiesImpl();
 
     private Monitor monitor = new Monitor(this);
     private MeshKeeper meshKeeper;
 
     public List<Integer> reserveTcpPorts(int count) throws Exception {
         return Arrays.asList(PortReserver.reservePorts(PortReserver.TCP, count));
     }
 
     public void releaseTcpPorts(Collection<Integer> ports) {
         PortReserver.releasePorts(PortReserver.TCP, ports);
     }
 
     synchronized public void bind(String owner) throws Exception {
         if (exclusiveOwner == null) {
             exclusiveOwner = owner;
             LOG.info("Now bound to: " + exclusiveOwner);
         } else if (!exclusiveOwner.equals(owner)) {
             throw new Exception("Bind failure, already bound: " + exclusiveOwner);
         }
     }
 
     synchronized public void unbind(String owner) throws Exception {
         if (exclusiveOwner == null) {
         } else if (exclusiveOwner.equals(owner)) {
             LOG.info("Bind to " + exclusiveOwner + " released");
             exclusiveOwner = null;
         } else {
             throw new Exception("Release failure, different owner: " + exclusiveOwner);
         }
     }
 
     synchronized public MeshProcess launch(LaunchDescription launchDescription, String sourceRegistryPath, MeshProcessListener handler) throws Exception {
         int pid = pidCounter++;
         LocalProcess rc = createLocalProcess(launchDescription, handler, pid);
         rc.setOwnerRegistryPath(sourceRegistryPath);
         processes.put(pid, rc);
         try {
             rc.start();
         } catch (Exception e) {
             processes.remove(pid);
             throw e;
         }
 
         return rc.getProxy();
     }
 
     protected LocalProcess createLocalProcess(LaunchDescription launchDescription, MeshProcessListener handler, int pid) throws Exception {
         return new LocalProcess(this, launchDescription, handler, pid);
     }
 
     public synchronized void start() throws Exception {
         if (started) {
             return;
         }
 
         System.getProperties().setProperty(LOCAL_REPO_PROP, meshKeeper.repository().getLocalRepoDirectory().getCanonicalPath());
 
         started = true;
         if (agentId == null) {
 
             try {
                 setAgentId(java.net.InetAddress.getLocalHost().getHostName());
             } catch (java.net.UnknownHostException uhe) {
                 LOG.warn("Error determining hostname.");
                 uhe.printStackTrace();
                 setAgentId("UNDEFINED");
             }
         }
 
         shutdownHook = new Thread(getAgentId() + "-Shutdown") {
             public void run() {
                 LOG.debug("Executing Shutdown Hook for " + LaunchAgent.this);
                 try {
                     LaunchAgent.this.stop();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         };
 
         properties.fillIn(this);
 
         Runtime.getRuntime().addShutdownHook(shutdownHook);
 
         monitor.start();
 
         meshKeeper.distribute(getRegistryPath(), false, this);
 
         LOG.info("PROCESS LAUNCHER " + getAgentId() + " STARTED\n");
 
     }
 
     private String getRegistryPath() {
         return LaunchAgent.LAUNCH_AGENT_REGISTRY_PATH + "/" + getAgentId();
     }
 
     public void stop() throws Exception {
 
         synchronized (this) {
             if (!started) {
                 return;
             }
 
             if (Thread.currentThread() != shutdownHook) {
                 Runtime.getRuntime().removeShutdownHook(shutdownHook);
             }
 
             started = false;
 
             for (LocalProcess process : processes.values()) {
                 try {
                     process.kill();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
             processes.clear();
         }
 
         monitor.requestCleanup();
         monitor.stop();
 
         meshKeeper.undistribute(this);
 
         synchronized (this) {
             notifyAll();
         }
     }
 
     public synchronized void join() throws InterruptedException {
         wait();
     }
 
     /**
      * Clears the launchers local resource cache.
      * 
      * @throws IOException
      *             If there is an error purging the cache.
      */
     public void purgeResourceRepository() throws IOException {
         meshKeeper.repository().purgeLocalRepo();
     }
 
     /**
      * Sets the base directory where the agent puts it's data.
      * 
      * @param directory
      */
     public void setDirectory(File directory) {
         this.directory = directory;
     }
 
     /**
      * @return Gets the data directory where the launcher stores files.
      */
     public File getDirectory() {
         return directory;
     }
 
     /**
      * Gets properties about the agent and it's host machine.
      * 
      * @return
      */
     public HostProperties getHostProperties() {
         return properties;
     }
 
     /**
      * Sets the name of the agent id. Once set it cannot be changed.
      * 
      * @param id
      *            the name of the agent id.
      */
     public void setAgentId(String id) {
         if (agentId == null && id != null) {
             agentId = id.trim().toUpperCase();
         }
     }
 
     /**
      * @return This agent's id.
      */
     public String getAgentId() {
         return agentId;
     }
 
     public MeshKeeper getMeshKeeper() {
         return meshKeeper;
     }
 
     public void setMeshKeeper(MeshKeeper meshKeeper) {
         this.meshKeeper = meshKeeper;
     }
 
     public Map<Integer, LocalProcess> getProcesses() {
         return processes;
     }
 
     public String toString() {
         return "ProcessLauncer-" + getAgentId();
     }
 
     /**
      * @param timeout
      */
     public void checkForRogueProcesses(int timeout) {
         ArrayList<LocalProcess> runningProcs = null;
         synchronized (this) {
             runningProcs = new ArrayList<LocalProcess>(processes.size());
             runningProcs.addAll(processes.values());
         }
 
         HashSet<String> deadLaunchers = new HashSet<String>();
         HashSet<String> runningLaunchers = new HashSet<String>();
         for (LocalProcess p : runningProcs) {
 
             if (runningLaunchers.contains(runningLaunchers.contains(p.getOwnerRegistryPath()))) {
                 continue;
             }
 
             if (!deadLaunchers.contains(p.getOwnerRegistryPath())) {
                 LaunchClientService launcher = null;
                 try {
                     launcher = meshKeeper.registry().getRegistryObject(p.getOwnerRegistryPath());
                 } catch (Exception e) {
                     LOG.warn("Error looking up LaunchClient: " + p.getOwnerRegistryPath(), e);
                 }
 
                 if (launcher != null) {
                     //TODO how to do this with a reasonable timeout?
                     //launcher.ping();
                     runningLaunchers.add(p.getOwnerRegistryPath());
                     continue;
                 } else {
                     deadLaunchers.add(p.getOwnerRegistryPath());
                 }
             }
 
             LOG.warn("Killing rogue process:  " + p);
             try {
                 p.kill();
             } catch (Exception e) {
                 LOG.error("", e);
             }
         }
 
     }
 
     private class Monitor implements Runnable {
         Log log = LogFactory.getLog(this.getClass());
         private final LaunchAgent processLauncher;
 
         Thread thread;
         private String tempDirectory;
         private boolean cleanupRequested = false;
 
         public Monitor(LaunchAgent processLauncher) {
             this.processLauncher = processLauncher;
         }
 
         public void start() {
             tempDirectory = processLauncher.getDirectory() + File.separator + processLauncher.getAgentId() + File.separator + "temp";
             thread = new Thread(this, processLauncher.getAgentId() + "-Process Monitor");
             thread.start();
         }
 
         public void stop() {
             thread.interrupt();
             try {
                 thread.join();
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
             }
         }
 
         public void run() {
             while (true) {
                 synchronized (this) {
                     try {
                         wait(LaunchAgent.CLEANUP_TIMEOUT);
                     } catch (InterruptedException ie) {
                         cleanupRequested = true;
                         return;
                     } finally {
                         processLauncher.checkForRogueProcesses(15000);
                         if (cleanupRequested) {
                             cleanUpTempFiles();
                             cleanupRequested = false;
                         }
                     }
                 }
 
             }
         }
 
         public void cleanUpTempFiles() {
             //If we aren't running anything cleanup: temp parts
             Map<Integer, LocalProcess> processes = processLauncher.getProcesses();
             if (processes == null || processes.size() == 0) {
                 File tempDir = new File(tempDirectory);
                 String[] subDirs = tempDir != null ? tempDir.list() : null;
 
                 log.debug("*************Cleaning up temporary parts*************");
                 for (int i = 0; subDirs != null && i < subDirs.length; i++) {
                     try {
                         FileSupport.recursiveDelete(tempDir + File.separator + subDirs[i]);
                     } catch (Exception e) {
                         log.warn("ERROR cleaning up temporary parts:", e);
                     }
                 }
             }
         }
 
         /**
          * Requests cleanup of temporary files
          */
         public synchronized void requestCleanup() {
             cleanupRequested = true;
             notify();
         }
     }
 
     /**
      * @param exitValue
      */
     public synchronized void onProcessExit(LocalProcess process, int exitValue) {
         processes.remove(process.getPid());
     }
 }
