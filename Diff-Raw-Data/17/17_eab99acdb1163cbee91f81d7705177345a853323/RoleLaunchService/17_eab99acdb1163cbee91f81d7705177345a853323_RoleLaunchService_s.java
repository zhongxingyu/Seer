 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hadoop.hoya.yarn.appmaster;
 
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hoya.api.ClusterDescription;
 import org.apache.hadoop.hoya.providers.ProviderRole;
 import org.apache.hadoop.hoya.providers.ProviderService;
 import org.apache.hadoop.hoya.tools.HoyaUtils;
 import org.apache.hadoop.hoya.yarn.appmaster.state.RoleInstance;
 import org.apache.hadoop.hoya.yarn.appmaster.state.RoleStatus;
 import org.apache.hadoop.net.NetUtils;
 import org.apache.hadoop.security.UserGroupInformation;
 import org.apache.hadoop.security.token.Token;
 import org.apache.hadoop.service.AbstractService;
 import org.apache.hadoop.yarn.api.records.Container;
 import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
 import org.apache.hadoop.yarn.api.records.LocalResource;
 import org.apache.hadoop.yarn.security.ContainerTokenIdentifier;
 import org.apache.hadoop.yarn.util.ConverterUtils;
 import org.apache.hadoop.yarn.util.Records;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * A service for launching containers
  */
 public class RoleLaunchService extends AbstractService {
   protected static final Logger log =
     LoggerFactory.getLogger(RoleLaunchService.class);
   /**
    * How long to expect launcher threads to shut down on AM termination:
    * {@value}
    */
   public static final int LAUNCHER_THREAD_SHUTDOWN_TIME = 10000;
   /**
    * Map of launched threads.
    * These are retained so that at shutdown time the AM can signal
    * all threads to stop.
    *
    * However, we don't want to run out of memory even if many containers
    * get launched over time, so the AM tries to purge this
    * of the latest launched thread when the RoleLauncher signals
    * the AM that it has finished
    */
   private final Map<RoleLauncher, Thread> launchThreads =
     new HashMap<RoleLauncher, Thread>();
 
   /**
    * Callback to whatever has the task of actually running the container
    * start operation
    */
   private final ContainerStartOperation containerStarter;
 
 
   private final ProviderService provider;
   /**
    * Filesystem to use for the launch
    */
   private final FileSystem fs;
 
   /**
    * Path in the launch filesystem that refers to a configuration directory
    * -the interpretation of it is left to the Provider
    */
   private final Path generatedConfDirPath;
 
   /**
    * Thread group for the launchers; gives them all a useful name
    * in stack dumps
    */
   private final ThreadGroup launcherThreadGroup = new ThreadGroup("launcher");
 
   /**
    * Construct an instance of the launcher
    * @param startOperation the callback to start the opreation
    * @param provider the provider
    * @param fs filesystem
    * @param generatedConfDirPath path in the FS for the generated dir
    */
   public RoleLaunchService(ContainerStartOperation startOperation,
                            ProviderService provider, FileSystem fs,
                            Path generatedConfDirPath) {
     super("ThreadPoolService");
     containerStarter = startOperation;
     this.fs = fs;
     this.generatedConfDirPath = generatedConfDirPath;
     this.provider = provider;
   }
 
   @Override
   protected void serviceStop() throws Exception {
     joinAllLaunchedThreads();
     super.serviceStop();
   }
 
   /**
    * Start an asychronous launch operation
    * @param container container target
    * @param role role
    * @param clusterSpec cluster spec to use for template
    */
   public void launchRole(Container container, RoleStatus role,
                          ClusterDescription clusterSpec) {
     String roleName = role.getName();
     //emergency step: verify that this role is handled by the provider
     assert provider.isSupportedRole(roleName);
     RoleLaunchService.RoleLauncher launcher =
       new RoleLaunchService.RoleLauncher(container,
                                          role.getProviderRole(),
                                          clusterSpec,
                                          clusterSpec.getOrAddRole(roleName));
     launchThread(launcher,
                  String.format("%s on %s:%d", roleName,
                                container.getNodeId()
                                         .getHost(),
                                container.getNodeId()
                                         .getPort()));
   }
 
 
   public void launchThread(RoleLauncher launcher, String name) {
     Thread launchThread = new Thread(launcherThreadGroup,
                                      launcher,
                                      name);
 
     // launch and start the container on a separate thread to keep
     // the main thread unblocked
     // as all containers may not be allocated at one go.
     synchronized (launchThreads) {
       launchThreads.put(launcher, launchThread);
     }
     launchThread.start();
   }
 
   /**
    * Method called by a launcher thread when it has completed;
    * this removes the launcher of the map of active
    * launching threads.
    * @param launcher launcher that completed
    * @param ex any exception raised
    */
   public void launchedThreadCompleted(RoleLauncher launcher, Exception ex) {
     log.debug("Launched thread {} completed", launcher, ex);
     synchronized (launchThreads) {
       launchThreads.remove(launcher);
     }
   }
 
   /**
    Join all launched threads
    needed for when we time out
    and we need to release containers
    */
   private void joinAllLaunchedThreads() {
 
 
     //first: take a snapshot of the thread list
     List<Thread> liveThreads;
     synchronized (launchThreads) {
       liveThreads = new ArrayList<Thread>(launchThreads.values());
     }
     int size = liveThreads.size();
     if (size > 0) {
       log.info("Waiting for the completion of {} threads", size);
       for (Thread launchThread : liveThreads) {
         try {
           launchThread.join(LAUNCHER_THREAD_SHUTDOWN_TIME);
         } catch (InterruptedException e) {
           log.info("Exception thrown in thread join: " + e, e);
         }
       }
     }
   }
 
 
   /**
    * Thread that runs on the AM to launch a region server.
    */
   private class RoleLauncher implements Runnable {
 
     // Allocated container
     public final Container container;
     public  final String containerRole;
     private final Map<String, String> roleOptions;
     private final ClusterDescription clusterSpec;
     public final ProviderRole role;
 
     public RoleLauncher(Container container,
                         ProviderRole role,
                         ClusterDescription clusterSpec,
                         Map<String, String> roleOptions) {
       assert container != null;
       assert role != null;
       assert roleOptions != null;
       this.container = container;
       this.containerRole = role.name;
       this.role = role;
       this.roleOptions = roleOptions;
       this.clusterSpec = clusterSpec;
     }
 
     @Override
     public String toString() {
       return "RoleLauncher{" +
              "container=" + container.getId() +
              ", containerRole='" + containerRole + '\'' +
              '}';
     }
 
     @Override
     public void run() {
       Exception ex = null;
       try {
         UserGroupInformation user =
           UserGroupInformation.createRemoteUser(container.getId().toString());
         String cmIpPortStr =
           container.getNodeId().getHost() + ":" + container.getNodeId().getPort();
         final InetSocketAddress cmAddress =
           NetUtils.createSocketAddr(cmIpPortStr);
 
        Token<ContainerTokenIdentifier> token =
          ConverterUtils.convertFromYarn(container.getContainerToken(),
                                         cmAddress);
        user.addToken(token);
 
         log.debug("Launching container {} into role {}",
                   container.getId(),
                   containerRole);
 
         ContainerLaunchContext ctx = Records
           .newRecord(ContainerLaunchContext.class);
         //now build up the configuration data
         provider.buildContainerLaunchContext(ctx,
                                              fs,
                                              generatedConfDirPath,
                                              containerRole,
                                              clusterSpec,
                                              roleOptions);
 
         String commandLine = ctx.getCommands().get(0);
         RoleInstance instance = new RoleInstance(container);
         instance.buildUUID();
         log.info("Starting container with command: {}", commandLine);
         Map<String, LocalResource> lr = ctx.getLocalResources();
         List<String> nodeEnv = new ArrayList<String>();
         if (log.isDebugEnabled()) {
           log.debug("{} resources: ", lr.size());
           for (Map.Entry<String, LocalResource> entry : lr.entrySet()) {
 
             String key = entry.getKey();
             LocalResource val = entry.getValue();
             String envElt = key + "=" + HoyaUtils.stringify(val.getResource());
             log.debug(envElt);
           }
         }
         //log the env
         Map<String, String> environment = ctx.getEnvironment();
         log.debug("{} env variables: ", environment.size());
 
         for (Map.Entry<String, String> env : environment.entrySet()) {
           String envElt = String.format("%s=\"%s\"",
                                         env.getKey(),
                                         env.getValue());
           log.debug(envElt);
           nodeEnv.add(envElt);
         }
         instance.command = commandLine;
         instance.role = containerRole;
         instance.roleId = role.id;
         instance.environment = nodeEnv.toArray(new String[nodeEnv.size()]);
         containerStarter.startContainer(container, ctx, instance);
       } catch (Exception e) {
         log.error(
           "Exception thrown while trying to start " + containerRole + ": " + e,
           e);
         ex = e;
       } finally {
         launchedThreadCompleted(this, ex);
       }
     }
 
   }
 }
