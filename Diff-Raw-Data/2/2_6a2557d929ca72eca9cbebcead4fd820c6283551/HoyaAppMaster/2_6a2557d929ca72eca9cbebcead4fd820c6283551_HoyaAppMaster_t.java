 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  *  or more contributor license agreements.  See the NOTICE file
  *  distributed with this work for additional information
  *  regarding copyright ownership.  The ASF licenses this file
  *  to you under the Apache License, Version 2.0 (the
  *  "License"); you may not use this file except in compliance
  *  with the License.  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package org.apache.hadoop.hoya.yarn.appmaster;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hoya.api.StandardRoleOptions;
 import org.apache.hadoop.hoya.exceptions.NoSuchNodeException;
 import org.apache.hadoop.hoya.providers.hbase.HBaseCommands;
 import org.apache.hadoop.hoya.HoyaExitCodes;
 import org.apache.hadoop.hoya.HoyaKeys;
 import org.apache.hadoop.hoya.api.ClusterDescription;
 import org.apache.hadoop.hoya.api.ClusterNode;
 import org.apache.hadoop.hoya.api.HoyaAppMasterProtocol;
 import org.apache.hadoop.hoya.exceptions.BadCommandArgumentsException;
 import org.apache.hadoop.hoya.exceptions.HoyaException;
 import org.apache.hadoop.hoya.exceptions.HoyaInternalStateException;
 import org.apache.hadoop.hoya.exec.ApplicationEventHandler;
 import org.apache.hadoop.hoya.exec.RunLongLivedApp;
 import org.apache.hadoop.hoya.tools.ConfigHelper;
 import org.apache.hadoop.hoya.tools.HoyaUtils;
 import org.apache.hadoop.hoya.tools.YarnUtils;
 import org.apache.hadoop.hoya.yarn.CommonArgs;
 import org.apache.hadoop.hoya.yarn.HoyaActions;
 import org.apache.hadoop.ipc.ProtocolSignature;
 import org.apache.hadoop.ipc.RPC;
 import org.apache.hadoop.ipc.Server;
 import org.apache.hadoop.net.NetUtils;
 import org.apache.hadoop.security.UserGroupInformation;
 import org.apache.hadoop.service.CompositeService;
 import org.apache.hadoop.yarn.api.ApplicationConstants;
 import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterResponse;
 import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
 import org.apache.hadoop.yarn.api.records.ApplicationId;
 import org.apache.hadoop.yarn.api.records.Container;
 import org.apache.hadoop.yarn.api.records.ContainerExitStatus;
 import org.apache.hadoop.yarn.api.records.ContainerId;
 import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
 import org.apache.hadoop.yarn.api.records.ContainerState;
 import org.apache.hadoop.yarn.api.records.ContainerStatus;
 import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
 import org.apache.hadoop.yarn.api.records.NodeReport;
 import org.apache.hadoop.yarn.api.records.Priority;
 import org.apache.hadoop.yarn.api.records.Resource;
 import org.apache.hadoop.yarn.client.api.AMRMClient;
 import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync;
 import org.apache.hadoop.yarn.client.api.async.NMClientAsync;
 import org.apache.hadoop.yarn.client.api.async.impl.NMClientAsyncImpl;
 import org.apache.hadoop.yarn.conf.YarnConfiguration;
 import org.apache.hadoop.yarn.exceptions.YarnException;
 import org.apache.hadoop.yarn.ipc.YarnRPC;
 import org.apache.hadoop.yarn.service.launcher.RunService;
 import org.apache.hadoop.yarn.util.ConverterUtils;
 import org.apache.hadoop.yarn.util.Records;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * This is the AM, which directly implements the callbacks from the AM and NM
  * It does not tag interface methods as @Override as the groovyc plugin
  * for maven seems to build in Java 5 syntax, not java6
  */
 public class HoyaAppMaster extends CompositeService
   implements AMRMClientAsync.CallbackHandler,
              NMClientAsync.CallbackHandler,
              RunService,
              HoyaExitCodes,
              HoyaAppMasterProtocol,
              ApplicationEventHandler,
              StandardRoleOptions {
   protected static final Logger log =
     LoggerFactory.getLogger(HoyaAppMaster.class);
 
   /**
    * How long to expect launcher threads to shut down on AM termination:
    * {@value}
    */
   public static final int LAUNCHER_THREAD_SHUTDOWN_TIME = 10000;
   /**
    * time to wait from shutdown signal being rx'd to telling
    * the AM: {@value}
    */
   public static final int TERMINATION_SIGNAL_PROPAGATION_DELAY = 1000;
   /**
    * Max failures to tolerate for the containers
    */
   public static final int MAX_TOLERABLE_FAILURES = 10;
   public static final String ROLE_UNKNOWN = "unknown";
   public static final int HEARTBEAT_INTERVAL = 1000;
 
   /** YARN RPC to communicate with the Resource Manager or Node Manager */
   private YarnRPC rpc;
 
   /** Handle to communicate with the Resource Manager*/
   private AMRMClientAsync asyncRMClient;
 
   /** Handle to communicate with the Node Manager*/
   public NMClientAsync nmClientAsync;
 
   /** RPC server*/
   private Server server;
   /** Hostname of the container*/
   private String appMasterHostname = "";
   /* Port on which the app master listens for status updates from clients*/
   private int appMasterRpcPort = 0;
   /** Tracking url to which app master publishes info for clients to monitor*/
   private String appMasterTrackingUrl = "";
 
   /** Application Attempt Id ( combination of attemptId and fail count )*/
   private ApplicationAttemptId appAttemptID;
   // App Master configuration
   /** No. of containers to run shell command on*/
   private int expectedContainerCount = 0;
 
   /**
    * container memory
    */
   private int containerMemory = 10;
   /** Priority of the request*/
   private int requestPriority;
 
   /**
    * Hash map of the containers we have
    */
   private final ConcurrentMap<ContainerId, ContainerInfo> containers =
     new ConcurrentHashMap<ContainerId, ContainerInfo>();
 
   /**
    * Hash map of the containers we have released, but we
    * are still awaiting acknowledgements on. Any failure of these
    * containers is treated as a successful outcome
    */
   private final ConcurrentMap<ContainerId, Container> containersBeingReleased =
     new ConcurrentHashMap<ContainerId, Container>();
 
   // Counter for completed containers ( complete denotes successful or failed )
   private final AtomicInteger numCompletedContainers = new AtomicInteger();
 
   // Count of containers already requested from the RM
   // Needed as once requested, we should not request for containers again.
   // Only request for more if the original requirement changes.
   private final AtomicInteger numRequestedContainers = new AtomicInteger();
 
   /**
    * counter of how many outstanding release requests we have
    */
   private final AtomicInteger numReleaseRequests = new AtomicInteger();
 
   // Allocated container count so that we know how many containers has the RM
   // allocated to us
   private final AtomicInteger numAllocatedContainers = new AtomicInteger();
 
   // Count of failed containers
   private final AtomicInteger numFailedContainers = new AtomicInteger();
 
   /**
    * # of started containers
    */
   private final AtomicInteger startedContainers = new AtomicInteger();
 
   /**
    * # of containers that failed to start 
    */
   private final AtomicInteger startFailedContainers = new AtomicInteger();
 
   /**
    * Command to launch
    */
   private String hbaseCommand = HBaseCommands.MASTER;
 
   // Launch threads
   private final List<Thread> launchThreads = new ArrayList<Thread>();
   /**
    * Thread group for the launchers; gives them all a useful name
    * in stack dumps
    */
   final ThreadGroup launcherThreadGroup = new ThreadGroup("launcher");
 
   /**
    * model the state using locks and conditions
    */
   private final ReentrantLock AMExecutionStateLock = new ReentrantLock();
   private final Condition isAMCompleted = AMExecutionStateLock.newCondition();
   private final AtomicBoolean amCompletionFlag = new AtomicBoolean(false);
   private volatile boolean localProcessTerminated = false;
   private volatile boolean localProcessStarted = false;
   private volatile boolean success;
 
   private String[] argv;
   /* Arguments passed in */
   private HoyaMasterServiceArgs serviceArgs;
   
   
 
   /**
    The cluster description published to callers
    This is used as a synchronization point on activities that update
    the CD, and also to update some of the structures that
    feed in to the CD
    */
   public ClusterDescription clusterDescription = new ClusterDescription();
 
   public final Object descriptionUpdateLock = new Object();
   
   
   /**
    * List of completed nodes. This isn't kept in the CD as it gets too
    * big for the RPC responses. Indeed, we should think about how deep to get this
    */
   private final Map<ContainerId, ClusterNode> completedNodes
    = new ConcurrentHashMap<ContainerId, ClusterNode>();
 
   /**
    * Nodes that failed to start.
    * Again, kept out of the CD
    */
   public final Map<ContainerId, ClusterNode> failedNodes =
     new ConcurrentHashMap<ContainerId, ClusterNode>();
 
 
   /**
    * Map of containerID -> cluster nodes, for status reports.
    * Access to this should be synchronized on the clusterDescription
    */
   private final Map<ContainerId, ClusterNode> liveNodes =
     new ConcurrentHashMap<ContainerId, ClusterNode>();
 
 
   /**
    * Flag set if there is no master
    */
   private boolean noMaster;
 
   /**
    * the hbase master runner
    */
   private RunLongLivedApp hbaseMaster;
 
   /**
    * The master node. This is a shared reference with the clusterDescription;
    * operations on it MUST be synchronised with that object
    */
   private ClusterNode masterNode;
 
 
   /**
    * Map of requested nodes. This records the command used to start it,
    * resources, etc. When container started callback is received,
    * the node is promoted from here to the containerMap
    */
   private final Map<ContainerId, ClusterNode> requestedNodes =
     new ConcurrentHashMap<ContainerId, ClusterNode>();
   private int processExitCode;
   private ContainerId localContainerId;
 
 
   public HoyaAppMaster() {
     super("HoyaMasterService");
   }
 
 /* =================================================================== */
 /* service lifecycle methods */
 /* =================================================================== */
 
   @Override //AbstractService
   public synchronized void serviceInit(Configuration conf) throws Exception {
     HoyaUtils.patchConfiguration(conf);
     //sort out the location of the AM
     serviceArgs.applyDefinitions(conf);
     serviceArgs.applyFileSystemURL(conf);
 
     String rmAddress = serviceArgs.rmAddress;
     if (rmAddress != null) {
       log.debug("Setting rm address from the command line: {}", rmAddress);
       YarnUtils.setRmSchedulerAddress(conf, rmAddress);
     }
 
     super.serviceInit(conf);
   }
   
 /* =================================================================== */
 /* RunService methods called from ServiceLauncher */
 /* =================================================================== */
 
   /**
    * pick up the args from the service launcher
    * @param args argument list
    */
   @Override // RunService
   public void setArgs(String... args) throws Exception {
     this.argv = args;
     serviceArgs = new HoyaMasterServiceArgs(argv);
     serviceArgs.parse();
     serviceArgs.postProcess();
   }
 
 
   /**
    * this is where the work is done.
    * @return the exit code
    * @throws Throwable
    */
   @Override
   public int runService() throws Throwable {
 
     //choose the action
     String action = serviceArgs.action;
     List<String> actionArgs = serviceArgs.actionArgs;
     int exitCode = EXIT_SUCCESS;
     if (action.equals(HoyaActions.ACTION_HELP)) {
       log.info(getName() + serviceArgs.usage());
     } else if (action.equals(HoyaActions.ACTION_CREATE)) {
       exitCode = createAndRunCluster(actionArgs.get(0));
     } else {
       throw new HoyaException("Unimplemented: " + action);
     }
     return exitCode;
   }
 
 /* =================================================================== */
 
   /**
    * Create and run the cluster
    * @return exit code
    * @throws Throwable on a failure
    */
   private int createAndRunCluster(String clustername) throws Throwable {
 
     //load the cluster description from the cd argument
     String hoyaClusterDir = serviceArgs.hoyaClusterURI;
     URI hoyaClusterURI = new URI(hoyaClusterDir);
     Path clusterDirPath = new Path(hoyaClusterURI);
     Path clusterSpecPath =
       new Path(clusterDirPath, HoyaKeys.CLUSTER_SPECIFICATION_FILE);
     FileSystem fs = getClusterFS();
     ClusterDescription.verifyClusterSpecExists(clustername, fs,
                                                clusterSpecPath);
 
     clusterDescription = ClusterDescription.load(fs, clusterSpecPath);
 
 
     clusterDescription.state = ClusterDescription.STATE_CREATED;
     clusterDescription.startTime = System.currentTimeMillis();
     if (0 == clusterDescription.createTime) {
       clusterDescription.createTime = clusterDescription.startTime;
     }
 /*
     clusterDescription.hbaseHome = serviceArgs.hbasehome;
     clusterDescription.imagePath = serviceArgs.image;
     clusterDescription.xHBaseMasterCommand = serviceArgs.xHBaseMasterCommand;
     clusterDescription.masterInfoPort = serviceArgs.masterInfoPort;
     clusterDescription.masters = serviceArgs.masters;
     clusterDescription.workerHeap = serviceArgs.workerHeap;
     clusterDescription.masterHeap = serviceArgs.masterHeap;
 */
 
     YarnConfiguration conf = new YarnConfiguration(getConfig());
     InetSocketAddress address = YarnUtils.getRmSchedulerAddress(conf);
     log.info("RM is at {}", address);
     rpc = YarnRPC.create(conf);
 
     localContainerId = ConverterUtils.toContainerId(
       HoyaUtils.mandatoryEnvVariable(
         ApplicationConstants.Environment.CONTAINER_ID.name()));
     appAttemptID = localContainerId.getApplicationAttemptId();
 
     ApplicationId appid = appAttemptID.getApplicationId();
     log.info("Hoya AM for ID {}", appid.getId() );
 
 
     int heartbeatInterval = HEARTBEAT_INTERVAL;
 
 
     //add the RM client -this brings the callbacks in
     asyncRMClient = AMRMClientAsync.createAMRMClientAsync(heartbeatInterval,
                                                           this);
     addService(asyncRMClient);
     //now bring it up
     asyncRMClient.init(conf);
     asyncRMClient.start();
 
 
     //nmclient relays callbacks back to this class
     nmClientAsync = new NMClientAsyncImpl("hoya", this);
     addService(nmClientAsync);
     nmClientAsync.init(conf);
     nmClientAsync.start();
 
     //bring up the Hoya RPC service
     startHoyaRPCServer();
 
     String hostname = NetUtils.getConnectAddress(server).getHostName();
     appMasterHostname = hostname;
     appMasterRpcPort = server.getPort();
     appMasterTrackingUrl = null;
     log.info("HoyaAM Server is listening at {}:{}", appMasterHostname, appMasterRpcPort);
 
 
     // work out a port for the AM
     if (0 == clusterDescription.masterInfoPort) {
       int port =
         YarnUtils.findFreePort(EnvMappings.DEFAULT_MASTER_INFO_PORT, 128);
       //need to get this to the app
       clusterDescription.masterInfoPort = port;
     }
     appMasterTrackingUrl =
       "http://" + appMasterHostname + ":" + clusterDescription.masterInfoPort;
 
 
     // Register self with ResourceManager
     // This will start heartbeating to the RM
     address = YarnUtils.getRmSchedulerAddress(asyncRMClient.getConfig());
     log.info("Connecting to RM at {},address");
     RegisterApplicationMasterResponse response = asyncRMClient
       .registerApplicationMaster(appMasterHostname,
                                  appMasterRpcPort,
                                  appMasterTrackingUrl);
     configureContainerMemory(response);
 
     //before bothering to start the containers, bring up the
     //hbase master.
     //This ensures that if the master doesn't come up, less
     //cluster resources get wasted
 
     //start hbase command
     //pull out the command line argument if set
     if (clusterDescription.xHBaseMasterCommand != null) {
       hbaseCommand = clusterDescription.xHBaseMasterCommand;
     }
 
     File confDir = getLocalConfDir();
     if (!confDir.exists() || !confDir.isDirectory()) {
 
       throw new BadCommandArgumentsException(
         "Configuration directory " + confDir +
         " doesn't exist");
     }
 
     //now validate the dir by loading in a hadoop-site.xml file from it
     File hBaseSiteXML = new File(confDir, HBaseCommands.HBASE_SITE);
     if (!hBaseSiteXML.exists()) {
       StringBuilder builder = new StringBuilder();
       String[] confDirEntries = confDir.list();
       for (String entry : confDirEntries) {
         builder.append(entry).append("\n");
       }
       throw new FileNotFoundException(
         "Conf dir " + confDir + " doesn't contain " + HBaseCommands.HBASE_SITE +
         "\n" + builder);
     }
 
     //now read it in
     Configuration siteConf = ConfigHelper.loadConfFromFile(hBaseSiteXML);
     
     TreeSet<String> confKeys = ConfigHelper.sortedConfigKeys(siteConf);
     //update the values
     clusterDescription.hbaseDataPath =
       siteConf.get(EnvMappings.KEY_HBASE_ROOTDIR);
     clusterDescription.zkHosts = siteConf.get(EnvMappings.KEY_ZOOKEEPER_QUORUM);
     clusterDescription.zkPort =
       siteConf.getInt(EnvMappings.KEY_ZOOKEEPER_PORT, 0);
     clusterDescription.zkPath = siteConf.get(EnvMappings.KEY_ZNODE_PARENT);
 
     noMaster = clusterDescription.masters <= 0;
     log.debug(" Contents of {}", hBaseSiteXML);
 
     for (String key : confKeys) {
       String val = siteConf.get(key);
       log.debug("{}={}", key, val);
       clusterDescription.clientProperties.put(key, val);
     }
     if (clusterDescription.zkPort == 0) {
       throw new BadCommandArgumentsException(
         "ZK port property not provided at" +
         EnvMappings.KEY_ZOOKEEPER_PORT + " in configuration file " +
         hBaseSiteXML);
     }
 
     clusterDescription.statusTime = System.currentTimeMillis();
     clusterDescription.state = ClusterDescription.STATE_LIVE;
     masterNode = new ClusterNode(hostname);
     masterNode.containerId = localContainerId;
     masterNode.role = HBaseCommands.ROLE_MASTER;
     masterNode.uuid = UUID.randomUUID().toString();
     
     List<String> launchSequence = new ArrayList<String>(8);
     launchSequence.add(HBaseCommands.ARG_CONFIG);
     launchSequence.add(confDir.getAbsolutePath());
     launchSequence.add(hbaseCommand);
     launchSequence.add(HBaseCommands.ACTION_START);
 
     if (noMaster) {
       log.info("skipping master launch");
       localProcessStarted = true;
     } else {
       addLaunchedContainer(localContainerId, masterNode);
       Map<String, String> env = new HashMap<String, String>();
       env.put("HBASE_LOG_DIR", buildHBaseLogdir());
       launchHBaseServer(clusterDescription,
                         launchSequence,
                         env);
     }
 
     try {
       //if we get here: success
       success = true;
       
       //here see if the launch worked.
       if (localProcessTerminated) {
         //exit faster
         return processExitCode;
       }
 
       //now ask for the workers
       flexClusterNodes(clusterDescription.workers);
 
       //now block waiting to be told to exit the process
       waitForAMCompletionSignal();
       //shutdown time
     } finally {
       finish();
     }
 
     return success ? processExitCode : EXIT_TASK_LAUNCH_FAILURE;
   }
 
   /**
    * Build the configuration directory passed in or of the target FS
    * @return the file
    */
   public File getLocalConfDir() {
     File confdir =
       new File(HoyaKeys.PROPAGATED_CONF_DIR_NAME).getAbsoluteFile();
     return confdir;
   }
 
   public String getDFSConfDir() {
     return clusterDescription.generatedConfigurationPath;
   }
 
   /**
    * Get the filesystem of this cluster
    * @return the FS of the config
    */
   public FileSystem getClusterFS() throws IOException {
     return FileSystem.get(getConfig());
   }
 
   /**
    * build the log directory
    * @return the log dir
    */
   public String buildHBaseLogdir() throws IOException {
     String logdir = System.getenv("LOGDIR");
     if (logdir == null) {
       logdir =
         "/tmp/hoya-" + UserGroupInformation.getCurrentUser().getShortUserName();
     }
     return logdir;
   }
 
   /**
    * Build the log dir env variable for the containers
    * @return the container's log dir
    */
   public String buildHBaseContainerLogdir() throws IOException {
     return buildHBaseLogdir();
   }
   
   
 
   /**
    * Block until it is signalled that the AM is done
    */
   private void waitForAMCompletionSignal() {
     AMExecutionStateLock.lock();
     try {
       if (!amCompletionFlag.get()) {
         log.debug("blocking until signalled to terminate");
         isAMCompleted.awaitUninterruptibly();
       }
     } finally {
       AMExecutionStateLock.unlock();
     }
     //add a sleep here for about a second. Why? it
     //stops RPC calls breaking so dramatically when the cluster
     //is torn down mid-RPC
     try {
       Thread.sleep(TERMINATION_SIGNAL_PROPAGATION_DELAY);
     } catch (InterruptedException ignored) {
       //ignored
     }
   }
 
   /**
    * Declare that the AM is complete
    */
   public void signalAMComplete(String reason) {
     log.info("Triggering shutdown of the AM: {}", reason);
     AMExecutionStateLock.lock();
     try {
       amCompletionFlag.set(true);
       isAMCompleted.signal();
     } finally {
       AMExecutionStateLock.unlock();
     }
   }
 
   /**
    * shut down the cluster 
    */
   private synchronized void finish() {
     //stop the daemon & grab its exit code
     Integer exitCode = stopHBase();
 
     // Join all launched threads
     // needed for when we time out
     // and we need to release containers
 
     //first: take a snapshot of the thread list
     List<Thread> liveThreads;
     synchronized (launchThreads) {
       liveThreads = new ArrayList<Thread>(launchThreads);
     }
     log.info("Waiting for the completion of {} threads", liveThreads.size());
     for (Thread launchThread : liveThreads) {
       try {
         launchThread.join(LAUNCHER_THREAD_SHUTDOWN_TIME);
       } catch (InterruptedException e) {
         log.info("Exception thrown in thread join: " + e, e);
       }
     }
 
     // When the application completes, it should send a finish application
     // signal to the RM
     log.info("Application completed. Signalling finish to RM");
 
     FinalApplicationStatus appStatus;
     String appMessage = null;
     success = true;
     String exitCodeString = exitCode != null ? exitCode.toString() : "n/a";
     if (numFailedContainers.get() == 0) {
       appStatus = FinalApplicationStatus.SUCCEEDED;
       appMessage = "completed. Local daemon exit code = " 
                    + exitCodeString;
     } else {
       appStatus = FinalApplicationStatus.FAILED;
       appMessage = "Completed with "+ numFailedContainers.get()+" failed containers: "
                    + " Local daemon exit code =  " +
                    exitCodeString + " - " + getContainerDiagnosticInfo();
       success = false;
     }
     try {
       log.info("Unregistering AM status={} message={}", appStatus, appMessage);
       asyncRMClient.unregisterApplicationMaster(appStatus, appMessage, null);
     } catch (YarnException e) {
       log.info("Failed to unregister application: " + e, e);
     } catch (IOException e) {
       log.info("Failed to unregister application: " + e, e);
     }
     if (server != null) {
       server.stop();
     }
   }
 
   private String getContainerDiagnosticInfo() {
     return " total=" + expectedContainerCount +
            " requested=" + numRequestedContainers.get() +
            " allocated=" + numAllocatedContainers.get() +
            " completed=" + numCompletedContainers.get() +
            " failed=" + numFailedContainers.get();
   }
 
   private void configureContainerMemory(RegisterApplicationMasterResponse response) {
     Resource maxResources =
       response.getMaximumResourceCapability();
     containerMemory = 0;
     if (clusterDescription.workerHeap != 0) {
       containerMemory = clusterDescription.workerHeap;
     }
     log.info("Setting container ask to {} from max of {}",
              containerMemory,
              maxResources);
   }
 
   public Object getProxy(Class protocol, InetSocketAddress addr) {
     return rpc.getProxy(protocol, addr, getConfig());
   }
 
   /**
    * Register self as a server
    * @return the new server
    */
   private Server startHoyaRPCServer() throws IOException {
     server = new RPC.Builder(getConfig())
       .setProtocol(HoyaAppMasterProtocol.class)
       .setInstance(this)
       .setPort(0)
       .setNumHandlers(5)
 //        .setSecretManager(sm)
       .build();
     server.start();
 
     return server;
   }
 
   /**
    * Setup the request that will be sent to the RM for the container ask.
    *
    *
    * @param role@return the setup ResourceRequest to be sent to RM
    */
   private AMRMClient.ContainerRequest buildContainerRequest(String role) {
     // setup requirements for hosts
     // using * as any host initially
     String[] hosts = null;
     String[] racks = null;
     Priority pri = Records.newRecord(Priority.class);
     
     // Set up resource type requirements
     Resource capability = Records.newRecord(Resource.class);
     synchronized (clusterDescription) {
       // Set up resource requirements from role valuesx
       capability.setVirtualCores(clusterDescription.getRoleOptInt(role,
                                   YARN_CORES,
                                   DEF_YARN_CORES));
       capability.setMemory(clusterDescription.getRoleOptInt(role,
                                                             YARN_MEMORY,
                                                             DEF_YARN_MEMORY));
       pri.setPriority(clusterDescription.getRoleOptInt(role,
                                                        YARN_PRIORITY,
                                                        DEF_YARN_REQUEST_PRIORITY));
     }
     AMRMClient.ContainerRequest request;
     request = new AMRMClient.ContainerRequest(capability,
                                               hosts,
                                               racks,
                                               pri,
                                               true);
     log.info("Requested container ask: {}", request);
     return request;
   }
 
 /* =================================================================== */
 /* AMRMClientAsync callbacks */
 /* =================================================================== */
 
   /**
    * Callback event when a container is allocated
    * @param allocatedContainers list of containers that are now ready to be
    * given work
    */
   @Override //AMRMClientAsync
   public void onContainersAllocated(List<Container> allocatedContainers) {
     log.info("onContainersAllocated({})",
              allocatedContainers.size());
     List<Container> surplus = new ArrayList<Container>();
     for (Container container : allocatedContainers) {
       String containerHostInfo = container.getNodeId().getHost()
                                  + ":" +
                                  container.getNodeId().getPort();
       log.info(getContainerDiagnosticInfo());
      if ( numAllocatedContainers.incrementAndGet() > expectedContainerCount) {
         log.info("Discarding surplus container {} on {}", container.getId(),
                  containerHostInfo);
         surplus.add(container);
       } else {
         
         log.info("Launching shell command on a new container.," +
                  " containerId={}," +
                  " containerNode={}:{}," +
                  " containerNodeURI={}," +
                  " containerResource={}",
                  container.getId(),
                  container.getNodeId().getHost(),
                  container.getNodeId().getPort(),
                  container.getNodeHttpAddress(),
                  container.getResource());
 
         HoyaRegionServiceLauncher launcher =
           new HoyaRegionServiceLauncher(this,
                                         container,
                                         HBaseCommands.REGION_SERVER,
                                         HBaseCommands.ROLE_WORKER);
         Thread launchThread = new Thread(launcherThreadGroup,
                                          launcher,
                                          "container-" +
                                          containerHostInfo);
 
         // launch and start the container on a separate thread to keep
         // the main thread unblocked
         // as all containers may not be allocated at one go.
         synchronized (launchThreads) {
           launchThreads.add(launchThread);
         }
         launchThread.start();
       }
     }
     //now discard those surplus containers
     for (Container container : surplus) {
       ContainerId id = container.getId();
       log.info("Releasing surplus container {} on {}:{}",
                id.getApplicationAttemptId(),
                container.getNodeId().getHost(),
                container.getNodeId().getPort());
       containersBeingReleased.put(id, container);
       numReleaseRequests.incrementAndGet();
       asyncRMClient.releaseAssignedContainer(id);
     }
     log.info("Diagnostics: "+ getContainerDiagnosticInfo());
   }
 
   @Override //AMRMClientAsync
   public synchronized void onContainersCompleted(List<ContainerStatus> completedContainers) {
     log.info("onContainersCompleted([{}]", completedContainers.size());
     for (ContainerStatus status : completedContainers) {
       ContainerId id = status.getContainerId();
       log.info("Container Completion for" +
                " containerID={}," +
                " state={}," +
                " exitStatus={}," +
                " diagnostics={}",
                id, status.getState(),
                status.getExitStatus(),
                status.getDiagnostics());
 
       // non complete containers should not be here
       assert (status.getState() == ContainerState.COMPLETE);
       //record the complete node's details for the status report
       updateCompletedNode(status);
       boolean markCompleted =
         status.getExitStatus() != ContainerExitStatus.ABORTED;
       if (containersBeingReleased.containsKey(id)) {
         log.info("Container was queued for release");
         markCompleted = true;
         containersBeingReleased.remove(id);
         numReleaseRequests.decrementAndGet();
       }
       if (markCompleted) {
         //if it isn't a failure , decrement the container pool
         noteContainerCompleted();
       }
     }
 
     // ask for more containers if any failed
     // In the case of Hoya, we don't expect containers to complete since
     // Hoya is a long running application. Keep track of how many containers
     // are completing. If too many complete, abort the application
     // TODO: this needs to be better thought about (and maybe something to
     // better handle in Yarn for long running apps)
 /*
 
     if ((numCompletedContainers.addAndGet(completedContainers.size())
             >= maximumContainerFailureLimit()) &&
         numCompletedContainers.get() == numTotalContainers) {
       log.info("Too many containers " +numCompletedContainers.get() +
               "  completed unexpectedly -stopping")
       signalAMComplete();
     }
     
 */
     reviewRequestAndReleaseNodes();
 
 /*
     int completedContainerCount = numCompletedContainers.get();
     if (completedContainerCount == numTotalContainers && noMaster) {
       log.info("All containers have completed and there is no running master -stopping")
       signalAMComplete();
     }*/
 
   }
 
   /**
    * How many failures to tolerate
    * On test runs, the numbers are low to keep things under control
    * @return the max #of failures
    */
   public int maximumContainerFailureLimit() {
 
     return clusterDescription.getFlag(CommonArgs.ARG_X_TEST,false) ? 1 : MAX_TOLERABLE_FAILURES;
   }
 
   /**
    * Handle completion of a container by decrementing the requested and alloc'd numbers
    */
   private synchronized void noteContainerCompleted() {
     numAllocatedContainers.decrementAndGet();
     numRequestedContainers.decrementAndGet();
   }
 
   /**
    * Implementation of cluster flexing.
    * This is synchronized so that it doesn't get confused by other requests coming
    * in.
    * It should be the only way that anything -even the AM itself on startup-
    * asks for nodes. 
    * @param workers #of workers to add
    * @param masters #of masters to request (if supported)
    * @return true if the number of workers changed
    * @throws IOException
    */
   private synchronized boolean flexClusterNodes(int workers) throws
                                                              IOException {
     log.info("Flexing cluster count from {} to {}", expectedContainerCount,
              workers);
     if (expectedContainerCount == workers) {
       //no-op
       log.info("Flex is a no-op");
       return false;
     }
     //update the #of workers
     expectedContainerCount = workers;
 
     // ask for more containers if needed
     reviewRequestAndReleaseNodes();
     return true;
   }
 
   /**
    * Look at where the current node state is -and whether it should be changed
    */
   private synchronized void reviewRequestAndReleaseNodes() {
     log.debug("in reviewRequestAndReleaseNodes()");
     if (amCompletionFlag.get()) {
       log.info("Ignoring node review operation: shutdown in progress");
     }
     
     int expected = expectedContainerCount;
     int req = numRequestedContainers.get();
     int alloced = numAllocatedContainers.get();
     int inuse = req + alloced;
     int delta = expected - inuse;
 
     if (delta > 0) {
       log.info("Asking for {} more worker(s) for a total of {} (req={} alloced={})",
                delta, expected, req, alloced);
       //more workers needed than we have -ask for more
       numRequestedContainers.addAndGet(delta);
       for (int i = 0; i < delta; i++) {
         AMRMClient.ContainerRequest containerAsk =
           buildContainerRequest(HBaseCommands.ROLE_WORKER);
         log.info("Container ask is {}", containerAsk);
         asyncRMClient.addContainerRequest(containerAsk);
       }
     } else if (delta < 0) {
 
       //special case: there are no more containers
 /*
       if (total == 0 && !noMaster) {
         //just exit the entire application here, rather than a node at a time.
         signalAMComplete("#of workers is set to zero: exiting");
         return;
       }
 */
 
       log.info("Asking for {} fewer worker(s) for a total of {}", delta, expected);
       //reduce the number expected (i.e. subtract the delta)
       numRequestedContainers.addAndGet(delta);
 
       //then pick some containers to kill
       int excess = -delta;
       Collection<ContainerInfo> targets = containers.values();
       for (ContainerInfo ci : targets) {
         if (excess > 0) {
           Container possible = ci.container;
           ContainerId id = possible.getId();
           if (!ci.released) {
             log.info("Requesting release of container {}", id);
             ci.released = true;
             containersBeingReleased.put(id, possible);
             numReleaseRequests.incrementAndGet();
             asyncRMClient.releaseAssignedContainer(id);
             excess--;
           }
         }
       }
       //here everything should be freed up, though there may be an excess due 
       //to race conditions with requests coming in
       if (excess > 0) {
         log.warn(
           "After releasing all worker nodes that could be free, there was an excess of {} nodes",
           excess);
       }
 
     }
   }
 
   /**
    * RM wants to shut down the AM
    */
   @Override //AMRMClientAsync
   public void onShutdownRequest() {
     signalAMComplete("Shutdown requested from RM");
   }
 
   /**
    * Monitored nodes have been changed
    * @param updatedNodes list of updated notes
    */
   @Override //AMRMClientAsync
   public void onNodesUpdated(List<NodeReport> updatedNodes) {
     log.info("Nodes updated");
   }
 
   /**
    * Use this as a generic heartbeater: 
    * 0 = not started, 50 = live, 100 = finished
    * @return
    */
   @Override //AMRMClientAsync
   public float getProgress() {
     if (hbaseMaster == null) {
       return 0.0f;
     } else {
       return 50.0f;
     }
   }
 
   @Override //AMRMClientAsync
   public void onError(Throwable e) {
     //callback says it's time to finish
     log.error("AMRMClientAsync.onError() received " + e, e);
     signalAMComplete("AMRMClientAsync.onError() received " + e);
   }
   
 /* =================================================================== */
 /* HoyaAppMasterApi */
 /* =================================================================== */
 
   @Override   //HoyaAppMasterApi
   public ProtocolSignature getProtocolSignature(String protocol,
                                                 long clientVersion,
                                                 int clientMethodsHash) throws
                                                                        IOException {
     return ProtocolSignature.getProtocolSignature(
       this, protocol, clientVersion, clientMethodsHash);
   }
 
   @Override   //HoyaAppMasterApi
   public void stopCluster() throws IOException {
     log.info("HoyaAppMasterApi.stopCluster()");
     signalAMComplete("stopCluster() invoked");
   }
 
   @Override   //HoyaAppMasterApi
   public boolean flexNodes(int workers) throws IOException {
     log.info("HoyaAppMasterApi.flexNodes({})", workers);
     return flexClusterNodes(workers);
   }
 
 
   @Override   //HoyaAppMasterApi
   public long getProtocolVersion(String protocol, long clientVersion) throws
                                                                       IOException {
     return versionID;
   }
 
   @Override //HoyaAppMasterApi
   public synchronized String getClusterStatus() throws IOException {
     updateClusterDescription();
     String status = clusterDescription.toJsonString();
     return status;
   }
 
   @Override
   public String[] listNodesByRole(String role) {
     List<ClusterNode> nodes = enumNodesByRole(role);
     String[] result = new String[nodes.size()];
     int count = 0;
     for (ClusterNode node: nodes) {
       result[count++] = node.uuid;
     }
     return result;
   }
 
   public List<ClusterNode> enumNodesByRole(String role) {
     List<ClusterNode> nodes = new ArrayList<ClusterNode>();
     synchronized (descriptionUpdateLock) {
       for (ClusterNode node : liveNodes.values()) {
         if (role.equals(node.role)) {
           nodes.add(node);
         }
       }
     }
     return nodes;
   }
 
   @Override
   public String getNode(String uuid) throws IOException, NoSuchNodeException {
     //todo: optimise
     synchronized (descriptionUpdateLock) {
       for (ClusterNode node : liveNodes.values()) {
         if (uuid.equals(node.uuid)) {
           return node.toJsonString();
         }
       }
     }
     //at this point: no node
     throw new NoSuchNodeException(uuid);
   }
 
   
 /* =================================================================== */
 /* END */
 /* =================================================================== */
 
   /**
    * Update the cluster description with anything interesting
    */
   private void updateClusterDescription() {
 
 
     long t = System.currentTimeMillis();
     synchronized (descriptionUpdateLock) {
       clusterDescription.statusTime = t;
       if (masterNode != null) {
         if (hbaseMaster != null) {
           masterNode.command = HoyaUtils.join(hbaseMaster.getCommands(), " ");
           if (hbaseMaster.isRunning()) {
             masterNode.state = ClusterDescription.STATE_LIVE;
           } else {
             masterNode.state = ClusterDescription.STATE_STOPPED;
             masterNode.diagnostics = "Exit code = " + hbaseMaster.getExitCode();
           }
           //pull in recent lines of output from the HBase master
           List<String> output = hbaseMaster.getRecentOutput();
           masterNode.output = output.toArray(new String[output.size()]);
         } else {
           masterNode.state = ClusterDescription.STATE_DESTROYED;
           masterNode.output = new String[0];
         }
       }
       List<ClusterNode> nodes = enumNodesByRole(HBaseCommands.ROLE_WORKER);
       int workerCount = nodes.size();
       clusterDescription.workers = workerCount;
       clusterDescription.instances = buildInstanceMap();
       Map<String, Long> stats = new HashMap<String, Long>();
       stats.put(STAT_CONTAINERS_REQUESTED, numRequestedContainers.longValue());
       stats.put(STAT_CONTAINERS_ALLOCATED, numAllocatedContainers.longValue());
       stats.put(STAT_CONTAINERS_COMPLETED, numCompletedContainers.longValue());
       stats.put(STAT_CONTAINERS_FAILED, startFailedContainers.longValue());
       stats.put(STAT_CONTAINERS_STARTED, startedContainers.longValue());
       stats.put(STAT_CONTAINERS_STARTED_FAILED,
                 startFailedContainers.longValue());
       clusterDescription.stats = stats;
     }
   }
 
   /**
    * Build an instance map.
    * This code does not acquire any locks and is not thread safe; caller is
    * expected to hold the lock.
    * @return the map of instance -> count
    */
   private Map<String, Integer> buildInstanceMap()  {
     Map<String, Integer> map = new HashMap<String, Integer>();
     for (ClusterNode node:liveNodes.values()) {
       Integer entry = map.get(node.role);
       int current = entry!=null? entry : 0;
       current ++; 
       map.put(node.role, current);
     }
     return map;
   }
   /**
    * handle completed node in the CD -move something from the live
    * server list to the completed server list
    * @param completed the node that has just completed
    */
   private void updateCompletedNode(ContainerStatus completed) {
 
     //add the node
     synchronized (descriptionUpdateLock) {
       ContainerId id = completed.getContainerId();
       ClusterNode node = liveNodes.remove(id);
       if (node == null) {
         node = new ClusterNode();
         node.name = id.toString();
         node.containerId = id;
       }
       node.state = ClusterDescription.STATE_DESTROYED;
       node.exitCode = completed.getExitStatus();
       node.diagnostics = completed.getDiagnostics();
       completedNodes.put(id, node);
     }
   }
 
   /**
    * add a launched container to the node map for status responss
    * @param id id
    * @param node node details
    */
   public void addLaunchedContainer(ContainerId id, ClusterNode node) {
     node.containerId = id;
     if (node.role == null) {
       log.warn("Unknown role for node {}", node);
       node.role = ROLE_UNKNOWN;
     }
     if (node.uuid==null) {
       node.uuid = UUID.randomUUID().toString();
       log.warn("creating UUID for node {}", node);
     }
     synchronized (descriptionUpdateLock) {
       liveNodes.put(node.containerId, node);
     }
 
   }
 
   /**
    * Launch the hbase server
    * @param commands list of commands -bin/hbase is inserted on the front
    * @param env environment variables above those generated by
    * @throws IOException IO problems
    * @throws HoyaException anything internal
    */
   protected synchronized void launchHBaseServer(ClusterDescription cd,
                                                 List<String> commands,
                                                 Map<String, String> env)
     throws IOException, HoyaException {
     if (hbaseMaster != null) {
       throw new HoyaInternalStateException("trying to launch hbase server" +
                                            " when one is already running");
     }
     //prepend the hbase command itself
     File binHbaseSh = buildHBaseBinPath(cd);
     String scriptPath = binHbaseSh.getAbsolutePath();
     if (!binHbaseSh.exists()) {
       throw new BadCommandArgumentsException("Missing script " + scriptPath);
     }
     commands.add(0, scriptPath);
     hbaseMaster = new RunLongLivedApp(commands);
     hbaseMaster.setApplicationEventHandler(this);
     //set the env variable mapping
     hbaseMaster.putEnvMap(env);
 
     //now spawn the process -expect updates via callbacks
     hbaseMaster.spawnApplication();
     
 
   }
 
   @Override // ApplicationEventHandler
   public void onApplicationStarted(RunLongLivedApp application) {
     log.info("Process has started");
     localProcessStarted = true;
     synchronized (descriptionUpdateLock) {
       masterNode.state = ClusterDescription.STATE_LIVE;
     }
   }
 
   /**
    * This is the callback on the HBaseMaster process 
    * -it's raised when that process terminates
    * @param application application
    * @param exitCode exit code
    */
   @Override // ApplicationEventHandler
   public void onApplicationExited(RunLongLivedApp application, int exitCode) {
     log.info("Process has exited with exit code {}", exitCode);
     localProcessTerminated = true;
     synchronized (descriptionUpdateLock) {
       processExitCode = exitCode;
       masterNode.exitCode = processExitCode;
       masterNode.state = ClusterDescription.STATE_STOPPED;
     }
     //tell the AM the cluster is complete 
     signalAMComplete("HBase master exited with " + exitCode);
   }
 
   /**
    * Get the path to hbase home
    * @return the hbase home path
    */
   public File buildHBaseBinPath(ClusterDescription cd) {
     File hbaseScript = new File(buildHBaseDir(cd),
                                 HBaseCommands.HBASE_SCRIPT);
     return hbaseScript;
   }
 
   public File buildHBaseDir(ClusterDescription cd) {
     File hbasedir;
     if (cd.imagePath != null) {
       hbasedir = new File(new File(HoyaKeys.HBASE_LOCAL),
                           HBaseCommands.HBASE_ARCHIVE_SUBDIR);
     } else {
       hbasedir = new File(cd.hbaseHome);
     }
     return hbasedir;
   }
 
   /**
    * stop hbase process if it the running process var is not null
    * @return the hbase exit code -null if it is not running
    */
   protected synchronized Integer stopHBase() {
     Integer exitCode;
     if (hbaseMaster != null) {
       hbaseMaster.stop();
       exitCode = hbaseMaster.getExitCode();
       hbaseMaster = null;
     } else {
       exitCode = null;
     }
     return exitCode;
   }
 
   /**
    * Add a property to the hbase client properties list in the
    * cluster description
    * @param key property key
    * @param val property value
    */
   public void noteHBaseClientProperty(String key, String val) {
     clusterDescription.clientProperties.put(key, val);
   }
 
   public void startContainer(Container container,
                              ContainerLaunchContext ctx,
                              ClusterNode node) {
     node.state = ClusterDescription.STATE_SUBMITTED;
     node.containerId = container.getId();
     synchronized (descriptionUpdateLock) {
       requestedNodes.put(container.getId(), node);
     }
     ContainerInfo containerInfo = new ContainerInfo();
     containerInfo.container = container;
     containerInfo.role = node.role;
     containerInfo.createTime = System.currentTimeMillis();
     containers.putIfAbsent(container.getId(), containerInfo);
     nmClientAsync.startContainerAsync(container, ctx);
   }
 
   @Override //  NMClientAsync.CallbackHandler 
   public void onContainerStopped(ContainerId containerId) {
     log.info("onContainerStopped {} ", containerId);
     containers.remove(containerId);
     //Removing live container?
     synchronized (descriptionUpdateLock) {
       ClusterNode node = liveNodes.remove(containerId);
       if (node!=null) {
         completedNodes.put(containerId, node);
       }
     }
   }
 
   @Override //  NMClientAsync.CallbackHandler 
   public void onContainerStarted(ContainerId containerId,
                                  Map<String, ByteBuffer> allServiceResponse) {
     log.debug("Started Container {} ", containerId);
     startedContainers.incrementAndGet();
     ContainerInfo cinfo = null;
     //update the model
     synchronized (descriptionUpdateLock) {
       ClusterNode node = requestedNodes.remove(containerId);
       if (null == node) {
         log.warn("Creating a new node description for an unrequested node");
         node = new ClusterNode(containerId.toString());
         node.role = ROLE_UNKNOWN;
       }
       node.state = ClusterDescription.STATE_LIVE;
       node.uuid = UUID.randomUUID().toString();
       addLaunchedContainer(containerId, node);
       cinfo = containers.get(containerId);
     }
     if (cinfo != null) {
       cinfo.startTime = System.currentTimeMillis();
       //trigger an async container status
       nmClientAsync.getContainerStatusAsync(containerId,
                                             cinfo.container.getNodeId());
     } else {
       //this is a hypothetical path not seen. We react by warning
       //there's not much else to do
       log.error("Notified of started container that isn't pending {}",
                 containerId);
     }
   }
 
   @Override //  NMClientAsync.CallbackHandler 
   public void onStartContainerError(ContainerId containerId, Throwable t) {
     log.error("Failed to start Container " + containerId, t);
     containers.remove(containerId);
     numFailedContainers.incrementAndGet();
     startFailedContainers.incrementAndGet();
     synchronized (descriptionUpdateLock) {
       ClusterNode node = requestedNodes.remove(containerId);
       if (null != node) {
         if (null != t) {
           node.diagnostics = HoyaUtils.stringify(t);
         }
         failedNodes.put(containerId, node);
       }
     }
   }
 
   @Override //  NMClientAsync.CallbackHandler 
   public void onContainerStatusReceived(ContainerId containerId,
                                         ContainerStatus containerStatus) {
     log.debug("Container Status: id={}, status={}", containerId,
               containerStatus);
   }
 
   @Override //  NMClientAsync.CallbackHandler 
   public void onGetContainerStatusError(
     ContainerId containerId, Throwable t) {
     log.error("Failed to query the status of Container {}", containerId);
   }
 
   @Override //  NMClientAsync.CallbackHandler 
   public void onStopContainerError(ContainerId containerId, Throwable t) {
     log.error("Failed to stop Container {}", containerId);
     containers.remove(containerId);
     ClusterNode node = failNode(containerId, t);
   }
 
   /**
    * Move a node from the live set to the failed list
    * @param containerId container ID to look for
    * @param nodeList list to scan from (& remove found)
    * @return the node, if found
    */
   public ClusterNode failNode(ContainerId containerId,
                               Throwable t) {
     ClusterNode node;
     synchronized (descriptionUpdateLock) {
      node =  liveNodes.remove(containerId);
 
       if (node != null) {
         if (t != null) {
           node.diagnostics = HoyaUtils.stringify(t);
         }
         failedNodes.put(containerId, node);
       }
     }
     return node;
   }
 }
