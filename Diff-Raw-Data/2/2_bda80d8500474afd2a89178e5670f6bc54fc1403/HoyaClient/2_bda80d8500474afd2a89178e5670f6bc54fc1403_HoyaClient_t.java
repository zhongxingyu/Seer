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
 
 package org.apache.hadoop.hoya.yarn.client;
 
 import com.beust.jcommander.JCommander;
 import com.google.common.annotations.VisibleForTesting;
 import groovy.json.JsonOutput;
 import groovy.lang.GroovyObject;
 import groovy.transform.CompileStatic;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileAlreadyExistsException;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hoya.HoyaExitCodes;
 import org.apache.hadoop.hoya.HoyaKeys;
 import org.apache.hadoop.hoya.api.ClusterDescription;
 import org.apache.hadoop.hoya.api.ClusterNode;
 import org.apache.hadoop.hoya.api.HoyaAppMasterProtocol;
 import org.apache.hadoop.hoya.exceptions.BadCommandArgumentsException;
 import org.apache.hadoop.hoya.exceptions.BadConfigException;
 import org.apache.hadoop.hoya.exceptions.HoyaException;
 import org.apache.hadoop.hoya.tools.ConfigHelper;
 import org.apache.hadoop.hoya.tools.Duration;
 import org.apache.hadoop.hoya.tools.HoyaUtils;
 import org.apache.hadoop.hoya.tools.YarnUtils;
 import org.apache.hadoop.hoya.yarn.CommonArgs;
 import org.apache.hadoop.hoya.yarn.HoyaActions;
 import org.apache.hadoop.hoya.yarn.appmaster.EnvMappings;
 import org.apache.hadoop.hoya.yarn.appmaster.HoyaMasterServiceArgs;
 import org.apache.hadoop.ipc.ProtocolProxy;
 import org.apache.hadoop.ipc.RPC;
 import org.apache.hadoop.net.NetUtils;
 import org.apache.hadoop.security.UserGroupInformation;
 import org.apache.hadoop.yarn.api.ApplicationConstants;
 import org.apache.hadoop.yarn.api.protocolrecords.KillApplicationRequest;
 import org.apache.hadoop.yarn.api.protocolrecords.KillApplicationResponse;
 import org.apache.hadoop.yarn.api.records.ApplicationId;
 import org.apache.hadoop.yarn.api.records.ApplicationReport;
 import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
 import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
 import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
 import org.apache.hadoop.yarn.api.records.LocalResource;
 import org.apache.hadoop.yarn.api.records.LocalResourceType;
 import org.apache.hadoop.yarn.api.records.Priority;
 import org.apache.hadoop.yarn.api.records.Resource;
 import org.apache.hadoop.yarn.api.records.YarnApplicationState;
 import org.apache.hadoop.yarn.client.api.YarnClientApplication;
 import org.apache.hadoop.yarn.client.api.impl.YarnClientImpl;
 import org.apache.hadoop.yarn.conf.YarnConfiguration;
 import org.apache.hadoop.yarn.exceptions.YarnException;
 import org.apache.hadoop.yarn.service.launcher.RunService;
 import org.apache.hadoop.yarn.service.launcher.ServiceLauncher;
 import org.apache.hadoop.yarn.util.Records;
 import org.codehaus.jackson.JsonParseException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.impl.Log4jLoggerAdapter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * Client service for Hoya
  */
 @CompileStatic
 
 public final class HoyaClient extends YarnClientImpl implements RunService, HoyaExitCodes {
   protected static final Logger log = LoggerFactory.getLogger(HoyaClient.class);
 
   // App master priority
   public static final int ACCEPT_TIME = 60000;
   public static final String E_CLUSTER_RUNNING = "cluster already running";
   public static final String E_ALREADY_EXISTS = "already exists";
   public static final String E_MISSING_PATH = "Missing path ";
   public static final String E_INCOMPLETE_CLUSTER_SPEC = "Cluster specification is marked as incomplete: ";
   public static final String E_UNKNOWN_CLUSTER = "Unknown cluster ";
   public static final String E_DESTROY_CREATE_RACE_CONDITION = "created while it was being destroyed";
   private int amPriority = 0;
   // Queue for App master
   private String amQueue = "default";
   // Amt. of memory resource to request for to run the App Master
   private int amMemory = 10;
 
   private String[] argv;
   private ClientArgs serviceArgs;
   public ApplicationId applicationId;
 
   /**
    * Entry point from the service launcher
    */
   public HoyaClient() {
   }
 
   /**
    * Constructor that takes the command line arguments and parses them
    * via {@link RunService#setArgs(String...)}. That method 
    * MUST NOT be called afterwards.
    * @param args argument list to be treated as both raw and processed
    * arguments.
    */
   public HoyaClient(String...args) throws Exception {
     setArgs(args);
   }
 
   @Override //Service
   public String getName() {
     return "Hoya";
   }
 
   //@Override
   public void setArgs(String...args) throws Exception {
     this.argv = args;
     serviceArgs = new ClientArgs(args);
     serviceArgs.parse();
     serviceArgs.postProcess();
   }
 
   @Override
   protected void serviceInit(Configuration conf) throws Exception {
     serviceArgs.applyDefinitions(conf);;
     serviceArgs.applyFileSystemURL(conf);
     HoyaUtils.patchConfiguration(conf);
     super.serviceInit(conf);
   }
 
   /**
    * this is where the work is done.
    * @return the exit code
    * @throws Throwable anything that went wrong
    */
   //@Override
   public int runService() throws Throwable {
 
     //choose the action
     String action = serviceArgs.action;
     int exitCode = EXIT_SUCCESS;
     String clusterName = serviceArgs.getClusterName();
     //actions
     if (HoyaActions.ACTION_CREATE.equals(action)) {
       validateClusterName(clusterName);
       exitCode = actionCreate(clusterName);
     } else if (HoyaActions.ACTION_DESTROY.equals(action)) {
         validateClusterName(clusterName);
         exitCode = actionDestroy(clusterName);
     } else if (HoyaActions.ACTION_EXISTS.equals(action)) {
       validateClusterName(clusterName);
       exitCode = actionExists(clusterName);
     } else if (HoyaActions.ACTION_FLEX.equals(action)) {
       validateClusterName(clusterName);
       exitCode = actionFlex(clusterName,
                             serviceArgs.workers,
                             serviceArgs.masters,
                             serviceArgs.persist);
     } else if (HoyaActions.ACTION_GETCONF.equals(action)) {
       File outfile = null;
       if (serviceArgs.output != null) {
         outfile = new File(serviceArgs.output);
       }
       exitCode = actionGetConf(clusterName,
                                serviceArgs.format,
                                outfile);
     } else if (HoyaActions.ACTION_HELP.equals(action)) {
       log.info("HoyaClient {}", serviceArgs.usage());
 
     } else if (HoyaActions.ACTION_LIST.equals(action)) {
       if (!isUnset(clusterName)) {
         validateClusterName(clusterName);
       }
       exitCode = actionList(clusterName);
     } else if (HoyaActions.ACTION_START.equals(action)) {
       exitCode = actionStart(clusterName);
     } else if (HoyaActions.ACTION_STATUS.equals(action)) {
       validateClusterName(clusterName);
       exitCode = actionStatus(clusterName);
     } else if (HoyaActions.ACTION_DESTROY.equals(action)) {
       validateClusterName(clusterName);
       exitCode = actionStop(clusterName, 0);
     } else  {
       throw new HoyaException(EXIT_UNIMPLEMENTED,
                               "Unimplemented: " + action); 
     }
       
     return exitCode;
   }
 
   protected void validateClusterName(String clustername) throws
                                                          BadCommandArgumentsException {
     if (!HoyaUtils.isClusternameValid(clustername)) {
       throw new BadCommandArgumentsException("Illegal cluster name: "+clustername);
     }
   }
 
   /**
    * Destroy a cluster. There's two race conditions here
    * #1 the cluster is started between verifying that there are no live
    * clusters of that name.
    */
   public int actionDestroy(String clustername) throws YarnException,
                                                       IOException {
     //verify that a live cluster isn't there
     validateClusterName(clustername);
     verifyFileSystemArgSet();
     verifyManagerSet();
     verifyNoLiveClusters(clustername);
 
     //create the directory path
     FileSystem fs = getClusterFS();
     Path clusterDirectory = HoyaUtils.createHoyaClusterDirPath(fs, clustername);
     //delete the directory;
     fs.delete(clusterDirectory, true);
 
     // detect any race leading to cluster creation during the check/destroy process
     // and report a problem.
     if (findAllLiveInstances(null, clustername).size() > 0) {
       throw new HoyaException(EXIT_BAD_CLUSTER_STATE,
                             clustername + ": "
                             + E_DESTROY_CREATE_RACE_CONDITION
                             + " :" + findAllLiveInstances(null, clustername).get(0));
     }
     return EXIT_SUCCESS;
   }
   
 
   
   /**
    * Create the cluster -saving the arguments to a specification file first
    */
   private int actionCreate(String clustername) throws
                                                YarnException,
                                                IOException {
 
     //check for arguments that are mandatory with this action
 
     verifyFileSystemArgSet();
     verifyManagerSet();
 
     if (isUnset(serviceArgs.zkhosts)) {
       throw new BadCommandArgumentsException("Required argument "
                                                  + CommonArgs.ARG_ZKHOSTS
                                                  + " missing");
     }
     if (serviceArgs.confdir==null) {
       throw new BadCommandArgumentsException("Missing argument "
                                                  +CommonArgs.ARG_CONFDIR);
     }
     
     //verify that a live cluster isn't there
     verifyNoLiveClusters(clustername);
     
     //build up the initial cluster specification
     ClusterDescription clusterSpec = new ClusterDescription();
     clusterSpec.name = clustername;
     clusterSpec.state = ClusterDescription.STATE_INCOMPLETE;
     clusterSpec.createTime = System.currentTimeMillis();
     int workers = serviceArgs.workers;
     int workerHeap = serviceArgs.workerHeap;
     validateNodeAndHeapValues("worker",workers, workerHeap);
     clusterSpec.workers = workers;
     clusterSpec.workerHeap = workerHeap;
     int masters = serviceArgs.masters;
     int masterHeap = serviceArgs.masterHeap;
     int masterInfoPort = serviceArgs.masterInfoPort;
     int workerInfoPort = serviceArgs.workerInfoPort;
     validateNodeAndHeapValues("master", masters, masterHeap);
     if (masters > 1) {
       throw new BadCommandArgumentsException("No more than one master is currently supported");
     }
     clusterSpec.masters = masters;
     clusterSpec.masterHeap = masterHeap;
     clusterSpec.masterInfoPort = masterInfoPort;
     clusterSpec.workerInfoPort = workerInfoPort;
 
     //HBase home or image
     if (serviceArgs.image != null) {
       if (!isUnset(serviceArgs.hbasehome)) {
         //both args have been set
         throw new BadCommandArgumentsException("only one of "
                                                    + CommonArgs.ARG_IMAGE
                                                    + " and " + CommonArgs.ARG_HBASE_HOME + " can be provided");
       }
       clusterSpec.imagePath = serviceArgs.image.toUri().toString();
     } else {
       //the alternative is HBase home, which now MUST be set
       if (isUnset(serviceArgs.hbasehome)) {
         //both args have been set
         throw new BadCommandArgumentsException("Either " + ClientArgs.ARG_IMAGE
                                                    + " or " + CommonArgs.ARG_HBASE_HOME +" must be provided");
       }
       clusterSpec.hbaseHome = serviceArgs.hbasehome;
     }
 
     //set up the ZK binding
     String zookeeperRoot = serviceArgs.hbasezkpath;
     if (isUnset(serviceArgs.hbasezkpath)) {
       zookeeperRoot = "/yarnapps_" + getAppName() + "_" + getUsername() + "_" + clustername;
     }
     clusterSpec.zkPath = zookeeperRoot;
     clusterSpec.zkPort = serviceArgs.zkport;
     clusterSpec.zkHosts = serviceArgs.zkhosts;
 
     //build up the paths in the DFS
 
     FileSystem fs = getClusterFS();
     Path clusterDirectory = HoyaUtils.createHoyaClusterDirPath(fs, clustername);
     Path origConfPath = new Path(clusterDirectory, HoyaKeys.ORIG_CONF_DIR_NAME);
     Path generatedConfPath = new Path(clusterDirectory, HoyaKeys.GENERATED_CONF_DIR_NAME);
     Path clusterSpecPath = new Path(clusterDirectory, HoyaKeys.CLUSTER_SPECIFICATION_FILE);
     clusterSpec.originConfigurationPath = origConfPath.toUri().toASCIIString();
     clusterSpec.generatedConfigurationPath = generatedConfPath.toUri().toASCIIString();
     //save the specification to get a lock on this cluster name
     try {
       clusterSpec.save(fs, clusterSpecPath, false);
     } catch (FileAlreadyExistsException fae) {
       throw new HoyaException(EXIT_BAD_CLUSTER_STATE,
                               clustername + ": " + E_ALREADY_EXISTS + " :" + clusterSpecPath);
     } catch (IOException e) {
       //this is probably a file exists exception too, but include it in the trace just in case
       throw new HoyaException(EXIT_BAD_CLUSTER_STATE,
                               clustername + ": " + E_ALREADY_EXISTS + " :" + clusterSpecPath,
                               e);
     }
 
     //bulk copy
     //first the original from wherever to the DFS
     HoyaUtils.copyDirectory(getConfig(), serviceArgs.confdir, origConfPath);
     //then build up the generated path
     HoyaUtils.copyDirectory(getConfig(), origConfPath, generatedConfPath);
 
     //HBase
     Path hBaseRootPath = new Path(clusterDirectory, HoyaKeys.HBASE_DATA_DIR_NAME);
 
     log.debug("hBaseRootPath={}", hBaseRootPath);
     clusterSpec.hbaseDataPath = hBaseRootPath.toUri().toString();
 
     //explicit hbase command set
     clusterSpec.xHBaseMasterCommand = serviceArgs.xHBaseMasterCommand;
   
     //check for debug mode
     if (serviceArgs.xTest) {
       clusterSpec.flags.put(CommonArgs.ARG_X_TEST, "true");
     }
     
     //here the configuration is set up. Mark the 
     clusterSpec.state = ClusterDescription.STATE_SUBMITTED;
     clusterSpec.save(fs, clusterSpecPath, true);
     
     //here is where all the work is done
     return executeClusterCreation(clusterSpec);
   }
 
   public void verifyFileSystemArgSet() throws BadCommandArgumentsException {
     if (serviceArgs.filesystemURL == null) {
       throw new BadCommandArgumentsException("Required argument "
                                                  + CommonArgs.ARG_FILESYSTEM
                                                  + " missing");
     }
   }
   
   
   public void verifyManagerSet() throws BadCommandArgumentsException {
     InetSocketAddress rmAddr = YarnUtils.getRmAddress(getConfig());
     if (! YarnUtils.isAddressDefined(rmAddr)) {
       throw new BadCommandArgumentsException(
           "No valid Resource Manager adddress provided in the argument "
              + CommonArgs.ARG_MANAGER
              + " or the configuration property "
              + YarnConfiguration.RM_ADDRESS);
     }
   }
   
   /**
    * Create a cluster to the specification
    * @param clusterSpec cluster specification
    * @return the exit code from the operation
    */
   public int executeClusterCreation(ClusterDescription clusterSpec) throws
                                                                     YarnException,
                                                                     IOException {
 
     //verify that a live cluster isn't there;
     String clustername = clusterSpec.name;
     validateClusterName(clustername);
     verifyNoLiveClusters(clustername);
     //make sure it is valid;
     verifyValidClusterSize(clusterSpec.workers);
 
     Path genConfPath = createPathThatMustExist(clusterSpec.generatedConfigurationPath);
     Path origConfPath = createPathThatMustExist(clusterSpec.originConfigurationPath);
 
     Path imagePath;
     String csip = clusterSpec.imagePath;
     if (!isUnset(csip)) {
       imagePath = createPathThatMustExist(csip);
     } else {
       imagePath = null;
       if (isUnset(clusterSpec.hbaseHome)) {
         throw new HoyaException(EXIT_BAD_CLUSTER_STATE,
                                 "Neither an image path or hbase home were specified");
       }
     }
     
     YarnClientApplication application = createApplication();
     ApplicationSubmissionContext appContext = application.getApplicationSubmissionContext();
     ApplicationId appId = appContext.getApplicationId();
     // set the application name;
     appContext.setApplicationName(clustername);
     //app type used in service enum;
     appContext.setApplicationType(HoyaKeys.APP_TYPE);
 
     if (clusterSpec.flags.get(CommonArgs.ARG_X_TEST)!=null) {
       //test flag set
       appContext.setMaxAppAttempts(1);
     }
 
     FileSystem hdfs = getClusterFS();
     Path tempPath = HoyaUtils.createHoyaAppInstanceTempPath(hdfs,
                                                             clustername,
                                                             appId.toString());
 
     // Set up the container launch context for the application master
     ContainerLaunchContext amContainer =
       Records.newRecord(ContainerLaunchContext.class);
 
     // set local resources for the application master
     // local files or archives as needed
     // In this scenario, the jar file for the application master is part of the local resources			
     Map<String, LocalResource> localResources = new HashMap<String, LocalResource>();
 
     if (!getUsingMiniMRCluster()) {
       //the assumption here is that minimr cluster => this is a test run
       //and the classpath can look after itself
 
       log.info("Copying JARs from local filesystem and add to local environment");
       // Copy the application master jar to the filesystem
       // Create a local resource to point to the destination jar path 
       String bindir = "";
       //add this class
       localResources.put("hoya.jar",  submitJarWithClass(this.getClass(),
                                                       tempPath,
                                                       bindir,
                                                       "hoya.jar"));
       //add lib classes that don't come automatically with YARN AM classpath
       String libdir = bindir + "lib/";
       localResources.put("groovayll.jar", submitJarWithClass(GroovyObject.class,
                                                            tempPath,
                                                            libdir,
                                                            "groovayll.jar"));
 
       localResources.put("jcommander.jar", submitJarWithClass(JCommander.class,
                                                             tempPath,
                                                             libdir,
                                                             "jcommander.jar"));
       
 
       localResources.put("slf4j.jar", submitJarWithClass(Logger.class,
                                                             tempPath,
                                                             libdir,
                                                             "slf4j.jar"));
       
       localResources.put("slf4j-log4j.jar", submitJarWithClass(Log4jLoggerAdapter.class,
                                                             tempPath,
                                                             libdir,
                                                             "slf4j-log4j.jar"));
       
     }
 
     //build up the configuration
 
     //now load the template configuration and build the site. Note that the 
     //use the original configuration when the cluster was first started..)
     Configuration config = getConfig();
     Configuration templateConf = ConfigHelper.loadTemplateConfiguration(config,
                                                                         origConfPath,
                                                                         HoyaKeys.HBASE_TEMPLATE,
                                                                         HoyaKeys.HBASE_TEMPLATE_RESOURCE);
 
     //construct the cluster configuration values
     Map<String, String> clusterConfMap = buildConfMapFromServiceArguments(clusterSpec);
     //merge them
     ConfigHelper.addConfigMap(templateConf, clusterConfMap);
 
     if (log.isDebugEnabled()) {
       ConfigHelper.dumpConf(templateConf);
     }
 
     //save the -site.xml config to the visible-to-all DFS
     //that generatedConfPath is in
     //this is the path for the site configuration
 
     Path sitePath = ConfigHelper.generateConfig(config,
                                                 templateConf,
                                                 genConfPath,
                                                 HoyaKeys.HBASE_SITE);
 
     log.debug("Saving the config to {}",sitePath);
     Map<String, LocalResource> confResources;
     confResources = YarnUtils.submitDirectory(hdfs,
                                               genConfPath,
                                               HoyaKeys.PROPAGATED_CONF_DIR_NAME);
     localResources.putAll(confResources);
     
     //now add the image if it was set
     if (HoyaUtils.maybeAddImagePath(hdfs, localResources, imagePath)) {
       log.debug("Registered image path {}",imagePath);
     }
     
     if (log.isDebugEnabled()) {
       for (String key:localResources.keySet()) {
         LocalResource val = localResources.get(key);
         log.debug("{}={}", key,YarnUtils.stringify(val.getResource()));
       }
     }
 
     // Set the log4j properties if needed 
 /*
     if (!log4jPropFile.isEmpty()) {
       Path log4jSrc = new Path(log4jPropFile);
       Path log4jDst = new Path(fs.getHomeDirectory(), "log4j.props");
       fs.copyFromLocalFile(false, true, log4jSrc, log4jDst);
       FileStatus log4jFileStatus = fs.getFileStatus(log4jDst);
       LocalResource log4jRsrc = Records.newRecord(LocalResource.class);
       log4jRsrc.setType(LocalResourceType.FILE);
       log4jRsrc.setVisibility(LocalResourceVisibility.APPLICATION);
       log4jRsrc.setResource(ConverterUtils.getYarnUrlFromURI(log4jDst.toUri()));
       log4jRsrc.setTimestamp(log4jFileStatus.getModificationTime());
       log4jRsrc.setSize(log4jFileStatus.getLen());
       localResources.put("log4j.properties", log4jRsrc);
     }
 
 */
 
     // Set local resource info into app master container launch context
     amContainer.setLocalResources(localResources);
     Map<String, String> env = new HashMap<String, String>();
 
     env.put("CLASSPATH", buildClasspath());
 
     amContainer.setEnvironment(env);
 
     String rmAddr = serviceArgs.rmAddress;
     //spec out the RM address
     if (isUnset(rmAddr) && YarnUtils.isRmSchedulerAddressDefined(config)) {
       rmAddr = NetUtils.getHostPortString(YarnUtils.getRmSchedulerAddress(config));
     }
 
     //build up the args list, intially as anyting
     List<String> commands = new ArrayList<String>(20);
     commands.add(ApplicationConstants.Environment.JAVA_HOME.$() + "/bin/java");
     //insert any JVM options);
     commands.add(HoyaKeys.JAVA_FORCE_IPV4);
     commands.add(HoyaKeys.JAVA_HEADLESS);
     //add the generic sevice entry point
     commands.add(ServiceLauncher.ENTRY_POINT);
     //immeiately followed by the classname
     commands.add(HoyaMasterServiceArgs.CLASSNAME);
     //now the app specific args
     commands.add(HoyaMasterServiceArgs.ARG_DEBUG);
     commands.add(HoyaActions.ACTION_CREATE);
     commands.add(clustername);
     //min #of nodes
     commands.add(HoyaMasterServiceArgs.ARG_WORKERS);
     commands.add(Integer.toString(clusterSpec.workers));
     commands.add(HoyaMasterServiceArgs.ARG_WORKER_HEAP);
     commands.add(Integer.toString(clusterSpec.workerHeap));
     commands.add(HoyaMasterServiceArgs.ARG_MASTERS);
     commands.add(Integer.toString(clusterSpec.masters));
     commands.add(HoyaMasterServiceArgs.ARG_MASTER_HEAP);
     commands.add(Integer.toString(clusterSpec.masterHeap));
 
 
     if (!isUnset(rmAddr)) {
       commands.add( HoyaMasterServiceArgs.ARG_RM_ADDR);
       commands.add( rmAddr);
     }
 
     //now conf dir path -fileset in the DFS
     commands.add( HoyaMasterServiceArgs.ARG_GENERATED_CONFDIR);
     commands.add( clusterSpec.generatedConfigurationPath);
 
     String hbaseHome = clusterSpec.hbaseHome;
     if (null!=imagePath) {
       commands.add( HoyaMasterServiceArgs.ARG_IMAGE);
       commands.add( imagePath.toString());
     } else {
       //HBase home
       commands.add( HoyaMasterServiceArgs.ARG_HBASE_HOME);
       commands.add( hbaseHome);
     }
     String xHBaseMasterCommand = clusterSpec.xHBaseMasterCommand;
     if (isSet(xHBaseMasterCommand)) {
       //explicit hbase command set
       commands.add( CommonArgs.ARG_X_HBASE_MASTER_COMMAND);
       commands.add( xHBaseMasterCommand);
     }
     if (clusterSpec.flags.get(CommonArgs.ARG_X_TEST) != null) {
       //test flag set
       commands.add( CommonArgs.ARG_X_TEST);
     }
     if (serviceArgs.filesystemURL!=null) {
       commands.add(CommonArgs.ARG_FILESYSTEM);
       commands.add( serviceArgs.filesystemURL.toString());
     }
 
     //path in FS can be unqualified
     commands.add(CommonArgs.ARG_PATH);
     commands.add( "services/hoya/");
     commands.add( "1>"+ApplicationConstants.LOG_DIR_EXPANSION_VAR+"/out.txt");
     commands.add( "2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR +"/err.txt");
 
     String cmdStr = HoyaUtils.join(commands, " ");
     log.info("Completed setting up app master command {}",cmdStr);
 
     amContainer.setCommands(commands);
     // Set up resource type requirements
     Resource capability = Records.newRecord(Resource.class);
     capability.setMemory(amMemory);
     capability.setVirtualCores(1);
     appContext.setResource(capability);
     Map<String, ByteBuffer> serviceData = new HashMap<String, ByteBuffer>();
     // Service data is a binary blob that can be passed to the application
     // Not needed in this scenario
     amContainer.setServiceData(serviceData);
 
     // The following are not required for launching an application master 
     // amContainer.setContainerId(containerId);
 
     appContext.setAMContainerSpec(amContainer);
 
     // Set the priority for the application master
     Priority pri = Records.newRecord(Priority.class);
     // TODO - what is the range for priority? how to decide? 
     pri.setPriority(amPriority);
     appContext.setPriority(pri);
 
     // Set the queue to which this application is to be submitted in the RM
     appContext.setQueue(amQueue);
 
     // Submit the application to the applications manager
     // SubmitApplicationResponse submitResp = applicationsManager.submitApplication(appRequest);
     // Ignore the response as either a valid response object is returned on success 
     // or an exception thrown to denote some form of a failure
     log.info("Submitting application to ASM");
 
     //submit the application
     applicationId = submitApplication(appContext);
 
     int exitCode;
     //wait for the submit state to be reached
     ApplicationReport report = monitorAppToState(new Duration(ACCEPT_TIME),
                                                  YarnApplicationState.ACCEPTED);
 
     //may have failed, so check that
     if (YarnUtils.hasAppFinished(report)) {
       exitCode = buildExitCode(appId, report);
     } else {
       //exit unless there is a wait
       exitCode = EXIT_SUCCESS;
 
       if (serviceArgs.waittime != 0) {
         //waiting for state to change
         Duration duration = new Duration(serviceArgs.waittime * 1000);
         duration.start();
         report = monitorAppToState(duration,
                                    YarnApplicationState.RUNNING);
         if (report!=null && report.getYarnApplicationState()==YarnApplicationState.RUNNING) {
           exitCode = EXIT_SUCCESS;
         } else {
           killRunningApplication(appId, "");
           exitCode = buildExitCode(appId, report);
         }
       }
     }
     return exitCode;
   }
 
   /**
    * Validate the node count and heap size values of a node class 
    * @param name node class name
    * @param count requested node count
    * @param heap requested heap size
    * @throws BadCommandArgumentsException if the values are out of range
    */
   public void validateNodeAndHeapValues(String name, int count, int heap) throws
                                                                           BadCommandArgumentsException {
     if (count < 0) {
       throw new BadCommandArgumentsException("requested no of "+name+" nodes is too low: "+count);
     }
     
     if (heap < HoyaKeys.MIN_HEAP_SIZE) {
       throw new BadCommandArgumentsException("requested heap size of " + name + " nodes is too low: " + count);
     }
     
   }
 
   /**
    * Create a path that must exist in the cluster fs
    * @param uri uri to create
    * @return the path
    * @throws HoyaException if the path does not exist
    */
   public Path createPathThatMustExist(String uri) throws
                                                   HoyaException,
                                                   IOException {
     Path path = new Path(uri);
     verifyPathExists(path);
     return path;
   }
 
   public void verifyPathExists(Path path) throws HoyaException, IOException {
     if (!getClusterFS().exists(path)) {
       throw new HoyaException(EXIT_BAD_CLUSTER_STATE,
                               E_MISSING_PATH + path);
     }
   }
 
   /**
    * verify that a live cluster isn't there
    * @param clustername cluster name
    * @throws HoyaException with exit code EXIT_BAD_CLUSTER_STATE
    * if a cluster of that name is either live or starting up.
    */
   public void verifyNoLiveClusters(String clustername) throws
                                                        IOException,
                                                        YarnException {
     List<ApplicationReport> existing = findAllLiveInstances(null, clustername);
 
     if (!existing.isEmpty()) {
       throw new HoyaException(EXIT_BAD_CLUSTER_STATE,
                 clustername + ": " + E_CLUSTER_RUNNING + " :" + existing.get(0));
     }
   }
 
   public String getUsername() throws IOException {
     return UserGroupInformation.getCurrentUser().getShortUserName();
   }
 
   /**
    * Submit a JAR containing a specific class.
    * @param clazz class to look for
    * @param appPath app path
    * @param subdir subdirectory  (expected to end in a "/")
    * @param jarName <i>At the destination</i>
    * @return the local resource ref
    * @throws IOException trouble copying to HDFS
    */
   private LocalResource submitJarWithClass(Class clazz, Path tempPath, String subdir, String jarName)
         throws IOException, HoyaException{
     File localFile = HoyaUtils.findContainingJar(clazz);
     if (null==localFile) {
       throw new FileNotFoundException("Could not find JAR containing " + clazz);
     }
     LocalResource resource = submitFile(localFile, tempPath, subdir, jarName);
     return resource;
   }
 
   /**
    * Submit a local file to the filesystem references by the instance's cluster
    * filesystem
    * @param localFile filename
    * @param clusterName application path
    * @param subdir subdirectory (expected to end in a "/")
    * @param destFileName destination filename
    * @return the local resource ref
    * @throws IOException trouble copying to HDFS
    */
   private LocalResource submitFile(File localFile, Path tempPath, String subdir, String destFileName) throws IOException {
     Path src = new Path(localFile.toString());
     Path destPath = new Path(tempPath, subdir+destFileName);
 
     getClusterFS().copyFromLocalFile(false, true, src, destPath);
 
     // Set the type of resource - file or archive
     // archives are untarred at destination
     // we don't need the jar file to be untarred for now
     LocalResource resource = YarnUtils.createAmResource(getClusterFS(),
                                destPath,
                                LocalResourceType.FILE);
     return resource;
   }
   
   /**
    * Get the filesystem of this cluster
    * @return the FS of the config
    */
   private FileSystem getClusterFS() throws IOException {
     return FileSystem.get(serviceArgs.filesystemURL, getConfig());
   }
 
   /**
    * Verify that there are enough nodes in the cluster
    * @param requiredNumber required # of nodes
    * @throws BadConfigException if the config is wrong
    */
   private void verifyValidClusterSize(int requiredNumber) throws
                                                           YarnException,
                                                           IOException {
     if (requiredNumber == 0) {
       return;
     }
     int nodeManagers = getYarnClusterMetrics().getNumNodeManagers();
     if (nodeManagers < requiredNumber) {
       throw new BadConfigException("Not enough nodes in the cluster:" +
                                    " need "+requiredNumber +
                                    " -but there are only "+nodeManagers+" nodes");
     }
   }
   
   
 
   private String buildClasspath() {
 // Add AppMaster.jar location to classpath
     // At some point we should not be required to add 
     // the hadoop specific classpaths to the env. 
     // It should be provided out of the box. 
     // For now setting all required classpaths including
     // the classpath to "." for the application jar
     StringBuilder classPathEnv = new StringBuilder();
     // add the runtime classpath needed for tests to work
     if (getUsingMiniMRCluster()) {
       //for mini cluster we pass down the java CP properties
       //and nothing else
       classPathEnv.append(System.getProperty("java.class.path"));
     } else {
       classPathEnv.append(ApplicationConstants.Environment.CLASSPATH.$())
           .append(File.pathSeparatorChar).append("./*");
       for (String c : getConfig().getStrings(
           YarnConfiguration.YARN_APPLICATION_CLASSPATH,
           YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
         classPathEnv.append(File.pathSeparatorChar);
         classPathEnv.append(c.trim());
       }
       classPathEnv.append(File.pathSeparatorChar).append("./log4j.properties");
     }
     return classPathEnv.toString();
   }
 
   /**
    * ask if the client is using a mini MR cluster
    * @return
    */
   private boolean getUsingMiniMRCluster() {
     return getConfig().getBoolean(YarnConfiguration.IS_MINI_YARN_CLUSTER, false);
   }
 
   private String getAppName() {
     return "hoya";
   }
 
   /**
    * Build the conf dir from the service arguments, adding the hbase root
    * to the FS root dir
    * @param hbaseRoot
    * @return a map of the dynamic bindings for this Hoya instance
    */
   @VisibleForTesting
   public Map<String, String> buildConfMapFromServiceArguments(ClusterDescription clusterSpec) {
     Map<String, String> envMap = new HashMap<String, String>();
 
     envMap.put(EnvMappings.KEY_HBASE_CLUSTER_DISTRIBUTED, "true");
     envMap.put(EnvMappings.KEY_HBASE_MASTER_PORT, "0");
     envMap.put(EnvMappings.KEY_HBASE_MASTER_INFO_PORT, Integer.toString(clusterSpec.masterInfoPort));
     envMap.put(EnvMappings.KEY_HBASE_ROOTDIR, clusterSpec.hbaseDataPath);
     envMap.put(EnvMappings.KEY_REGIONSERVER_INFO_PORT,
                Integer.toString(clusterSpec.workerInfoPort));
     envMap.put(EnvMappings.KEY_REGIONSERVER_PORT, "0");
     envMap.put(EnvMappings.KEY_ZNODE_PARENT, clusterSpec.zkPath);
     envMap.put(EnvMappings.KEY_ZOOKEEPER_PORT,
                Integer.toString(clusterSpec.zkPort));
     envMap.put(EnvMappings.KEY_ZOOKEEPER_QUORUM, clusterSpec.zkHosts);
     return envMap;
   }
 
   /**
    * Monitor the submitted application for reaching the requested state.
    * Will also report if the app reaches a later state (failed, killed, etc)
    * Kill application if duration!= null & time expires. 
    * @param appId Application Id of application to be monitored
    * @param duration how long to wait
    * @param desiredState desired state.
    * @return true if application completed successfully
    * @throws YarnException YARN or app issues
    * @throws IOException IO problems
    */
   @VisibleForTesting
   public int monitorAppToCompletion(Duration duration)
       throws YarnException, IOException {
 
     ApplicationReport report = monitorAppToState(duration,
                                        YarnApplicationState.FINISHED);
     return buildExitCode(applicationId, report);
   }
 
   /**
    * Wait for the app to start running (or go past that state)
    * @param duration time to wait
    * @return the app report; null if the duration turned out
    * @throws YarnException YARN or app issues
    * @throws IOException IO problems
    */
   @VisibleForTesting
   public ApplicationReport monitorAppToRunning(Duration duration)
       throws YarnException, IOException {
     return monitorAppToState(duration,
                              YarnApplicationState.RUNNING);
   }
 
   /**
    * Build an exit code for an application Id and its report.
    * If the report parameter is null, the app is killed
    * @param appId app
    * @param report report
    * @return the exit code
    */
   private int buildExitCode(ApplicationId appId, ApplicationReport report) throws
                                                                            IOException,
                                                                            YarnException {
     if (null==report) {
       forceKillApplication("Reached client specified timeout for application");
       return EXIT_TIMED_OUT;
     }
 
     YarnApplicationState state = report.getYarnApplicationState();
     FinalApplicationStatus dsStatus = report.getFinalApplicationStatus();
     switch (state) {
       case FINISHED:
         if (FinalApplicationStatus.SUCCEEDED == dsStatus) {
           log.info("Application has completed successfully");
           return EXIT_SUCCESS;
         } else {
           log.info("Application finished unsuccessfully." +
                    "YarnState = {}, DSFinalStatus = {} Breaking monitoring loop", state, dsStatus);
           return EXIT_YARN_SERVICE_FINISHED_WITH_ERROR;
         }
 
       case KILLED:
         log.info("Application did not finish. YarnState={}, DSFinalStatus={}", state, dsStatus);
         return EXIT_YARN_SERVICE_KILLED;
 
       case FAILED:
         log.info("Application Failed. YarnState={}, DSFinalStatus={}", state, dsStatus);
         return EXIT_YARN_SERVICE_FAILED;
       default:
         //not in any of these states
         return EXIT_SUCCESS;
     }
   }
 /**
  * Monitor the submitted application for reaching the requested state.
  * Will also report if the app reaches a later state (failed, killed, etc)
  * Kill application if duration!= null & time expires. 
  * @param appId Application Id of application to be monitored
  * @param duration how long to wait -must be more than 0
  * @param desiredState desired state.
  * @return the application report -null on a timeout
  * @throws YarnException
  * @throws IOException
  */
   @VisibleForTesting
   public ApplicationReport monitorAppToState(
       Duration duration, YarnApplicationState desiredState)
   throws YarnException, IOException {
     return monitorAppToState(applicationId, desiredState, duration);
   }
   
 /**
  * Monitor the submitted application for reaching the requested state.
  * Will also report if the app reaches a later state (failed, killed, etc)
  * Kill application if duration!= null & time expires. 
  * @param appId Application Id of application to be monitored
  * @param duration how long to wait -must be more than 0
  * @param desiredState desired state.
  * @return the application report -null on a timeout
  * @throws YarnException
  * @throws IOException
  */
   @VisibleForTesting
   public ApplicationReport monitorAppToState(
       ApplicationId appId, YarnApplicationState desiredState, Duration duration)
          throws YarnException, IOException {
     
     duration.start();
     if (duration.limit <= 0) {
       throw new HoyaException("Invalid duration of monitoring");
     }
     while (true) {
 
       // Get application report for the appId we are interested in 
 
       ApplicationReport r = getApplicationReport(appId);
 
       logAppReport(r);
 
       YarnApplicationState state = r.getYarnApplicationState();
       if (state.ordinal() >= desiredState.ordinal()) {
        log.debug("App in desired state (or higher) :{}",state);
         return r;
       }
       if (duration.getLimitExceeded()) {
         return null;
       }
 
       // sleep 1s.
       try {
         Thread.sleep(1000);
       } catch (InterruptedException ignored) {
         log.debug("Thread sleep in monitoring loop interrupted");
       }
     }
   }
 
   /**
    * Kill the submitted application by sending a call to the ASM
    * @throws YarnException
    * @throws IOException
    */
   public boolean forceKillApplication(String reason)
         throws YarnException, IOException {
     if (applicationId != null) {
       killRunningApplication(applicationId, reason);
       return true;
     }
     return false;
   }
 
   /**
    * Kill a running application
    * @param applicationId
    * @return the response
    * @throws YarnException YARN problems
    * @throws IOException IO problems
    */
   private KillApplicationResponse killRunningApplication(ApplicationId applicationId, String reason) throws
       YarnException,
       IOException {
     log.info("Killing application {} - {}", applicationId.getClusterTimestamp(), reason);
     KillApplicationRequest request =
       Records.newRecord(KillApplicationRequest.class);
     request.setApplicationId(applicationId);
     return rmClient.forceKillApplication(request);
   }
 
   /**
    * List Hoya instances belonging to a specific user
    * @param user user: "" means all users
    * @return a possibly empty list of Hoya AMs
    */
   @VisibleForTesting
   public List<ApplicationReport> listHoyaInstances(String user)
         throws YarnException, IOException {
     List<ApplicationReport> allApps = getApplications();
     List<ApplicationReport> results = new ArrayList<ApplicationReport>();
     for (ApplicationReport report: allApps) {
       if (report.getApplicationType().equals(HoyaKeys.APP_TYPE)
           && (user==null || user.equals(report.getUser()))) {
         results.add(report);
       }
     }
     return results;
   }
 
   /**
    * Implement the list action: list all nodes
    * @return exit code of 0 if a list was created
    */
   @VisibleForTesting
   public int actionList(String clustername) throws IOException, YarnException {
     verifyManagerSet();
 
     String user = serviceArgs.user;
     List<ApplicationReport> instances = listHoyaInstances(user);
 
     if (clustername==null || clustername.isEmpty()) {
       log.info("Hoya instances for {}:{}",
                (user!=null ? user : "all users"),
                instances.size());
       for (ApplicationReport report: instances) {
         logAppReport(report);
       }
       return EXIT_SUCCESS;
     } else {
       validateClusterName(clustername);
       log.debug("Listing cluster named {}",clustername);
       ApplicationReport report = findClusterInInstanceList(instances, clustername);
       if (report != null) {
         logAppReport(report);
         return EXIT_SUCCESS;
       } else {
         throw unknownClusterException(clustername);
       }
     }
   }
 
   public void logAppReport(ApplicationReport r) {
     log.info("Name        : {}", r.getName());
     log.info("YARN status : {}", r.getYarnApplicationState());
     log.info("Start Time  : {}", r.getStartTime());
     log.info("Finish Time : {}", r.getFinishTime());
     log.info("RPC         : {}:{}", r.getHost(), r.getRpcPort());
     log.info("Diagnostics : {}", r.getDiagnostics());
   }
 
   /**
    * Implement the islive action: probe for a cluster of the given name existing
    * 
    * @return exit code
    */
   @VisibleForTesting
   public int actionExists(String name) throws YarnException, IOException {
     verifyManagerSet();
     ApplicationReport instance = findInstance(getUsername(), name);
     if (instance==null) {
       throw unknownClusterException(name);
     }
     return EXIT_SUCCESS;
   }
 
   @VisibleForTesting
   public ApplicationReport findInstance(String user, String appname) throws
                                                                      IOException,
                                                                      YarnException {
     List<ApplicationReport> instances = listHoyaInstances(user);
     return findClusterInInstanceList(instances, appname);
   }
 
   /**
    * find all instances of a specific app -if there is >1 in the cluster,
    * this returns them all
    * @param user user
    * @param appname application name
    * @return the list of all matching application instances
    */
   @VisibleForTesting
   public List<ApplicationReport> findAllInstances(String user, String appname) throws
                                                                                IOException,
                                                                                YarnException {
     List<ApplicationReport> instances = listHoyaInstances(user);
     List<ApplicationReport> results = new ArrayList<ApplicationReport>(instances.size());
     for (ApplicationReport report:instances) {
       if (report.getName().equals(appname)) {
         results.add(report);
       }
     }
     return results;
   }
 
   /**
    * find all live instances of a specific app -if there is >1 in the cluster,
    * this returns them all. State should be running or less
    * @param user user
    * @param appname application name
    * @return the list of all matching application instances
    */
   @VisibleForTesting
   public List<ApplicationReport> findAllLiveInstances(String user, String appname) throws
                                                                                    YarnException,
                                                                                    IOException {
     List<ApplicationReport> instances = listHoyaInstances(user);
     List<ApplicationReport> results = new ArrayList<ApplicationReport>(instances.size());
     for (ApplicationReport app : instances) {
       if (app.getName().equals(appname) 
           && app.getYarnApplicationState().ordinal() <= YarnApplicationState.RUNNING
                                                                             .ordinal()) {
         results.add(app);
       }
     }
     return results;
 
   }
 
   public ApplicationReport findClusterInInstanceList(List<ApplicationReport> instances, String appname) {
     ApplicationReport found = null;
     ApplicationReport foundAndLive = null;
     for (ApplicationReport app: instances) {
       if (app.getName().equals(appname)) {
         found = app;
         if (app.getYarnApplicationState().ordinal() <=YarnApplicationState.RUNNING.ordinal()) {
           foundAndLive = app;
         }
       }
     }
     if (foundAndLive!=null) {
       found = foundAndLive;
     }
     return found;
   }
 
   @VisibleForTesting
   public HoyaAppMasterProtocol connect(ApplicationReport app) throws
                                                               YarnException,
                                                               IOException {
     String host = app.getHost();
     int port = app.getRpcPort();
     String address= host + ":" + port;
     if (host==null || 0==port ) {
       throw new HoyaException(EXIT_CONNECTIVTY_PROBLEM,
                               "Hoya instance "+app.getName()+" isn't" +
                               " providing a valid address for the" +
                               " Hoya RPC protocol: "+ address);
     }
     InetSocketAddress addr = NetUtils.createSocketAddrForHost(host, port);
     log.debug("Connecting to Hoya Server at {}",addr);
     ProtocolProxy<HoyaAppMasterProtocol> protoProxy = RPC.getProtocolProxy(HoyaAppMasterProtocol.class,
                         HoyaAppMasterProtocol.versionID,
                         addr,
                         UserGroupInformation.getCurrentUser(),
                         getConfig(),
                         NetUtils.getDefaultSocketFactory(getConfig()),
                         15000,
                         null);
     HoyaAppMasterProtocol hoyaServer = protoProxy.getProxy();
     log.debug("Connected to Hoya AM");
     return hoyaServer;
   }
   /**
    * Status operation; 'name' arg defines cluster name.
    * @return
    */
   @VisibleForTesting
   public int actionStatus(String clustername) throws YarnException, IOException {
     verifyManagerSet();
     validateClusterName(clustername);
     ClusterDescription status = getClusterStatus(clustername);
     log.info(JsonOutput.prettyPrint(status.toJsonString()));
     return EXIT_SUCCESS;
   }
 
   /**
    * Stop the cluster
    * @param clustername cluster name
    * @return the cluster name
    */
   public int actionStop(String clustername, int waittime) throws
                                                           YarnException,
                                                           IOException {
     verifyManagerSet();
     validateClusterName(clustername);
     ApplicationReport app = findInstance(getUsername(), clustername);
     if (app==null) {
       //exit early
       return EXIT_SUCCESS;
     }
     HoyaAppMasterProtocol appMaster = connect(app);
     appMaster.stopCluster();
     if (waittime > 0) {
       monitorAppToState(app.getApplicationId(),
                         YarnApplicationState.FINISHED,
                         new Duration(waittime));
     }
     return EXIT_SUCCESS;
   }
 
   /**
    * get the cluster configuration
    * @param clustername cluster name
    * @return the cluster name
    */
   @SuppressWarnings("UseOfSystemOutOrSystemErr")
   public int actionGetConf(String clustername, String format, File outputfile) throws
                                                                                YarnException,
                                                                                IOException {
     verifyManagerSet();
     validateClusterName(clustername);
     ClusterDescription status = getClusterStatus(clustername);
     Writer writer;
     boolean toPrint;
     if (outputfile != null) {
       writer = new FileWriter(outputfile);
       toPrint = false;
     } else {
       writer = new StringWriter();
       toPrint = true;
     }
     try {
       String description = "Hoya cluster "+clustername;
       if (format.equals(ClientArgs.FORMAT_XML)) {
         Configuration siteConf = new Configuration(false);
         for (String key: status.hBaseClientProperties.keySet()) {
           siteConf.set(key, status.hBaseClientProperties.get(key), description);
         }
         siteConf.writeXml(writer) ;
       } else if (format.equals(ClientArgs.FORMAT_PROPERTIES)) {
         Properties props = new Properties();
         props.putAll(status.hBaseClientProperties);
         props.store(writer, description);
       } else {
           throw new BadCommandArgumentsException("Unknown format: "+format);
       }
     } finally {
       //data is written.
       //close the file
       writer.close();
     }
     //then, if this is not a file write, print it
     if (toPrint) {
       System.out.println(writer.toString());
     } 
     return EXIT_SUCCESS;
   }
 
   /**
    * Restore a cluster
    */
   public int actionStart(String clustername) throws YarnException, IOException {
     //verify that a live cluster isn't there
     validateClusterName(clustername);
     verifyFileSystemArgSet();
     Path clusterSpecPath = locateClusterSpecification(clustername);
     ClusterDescription clusterSpec = ClusterDescription.load(getClusterFS(), clusterSpecPath);
     //spec is loaded, just look at its state
     verifySpecificationValidity(clusterSpecPath, clusterSpec);
     //now see if it is actually running and bail out;
     verifyManagerSet();
     verifyNoLiveClusters(clustername);
 
     return executeClusterCreation(clusterSpec);
   }
 
   /**
    * Perform any post-load cluster validation
    * @param clusterSpecPath
    * @param clusterSpec
    */
   public void verifySpecificationValidity(Path clusterSpecPath, ClusterDescription clusterSpec) throws
                                                                                                 HoyaException {
     if (clusterSpec.state == ClusterDescription.STATE_INCOMPLETE) {
       throw new HoyaException(EXIT_BAD_CLUSTER_STATE, E_INCOMPLETE_CLUSTER_SPEC + clusterSpecPath);
     }
   }
 
   /**
    * get the path of a cluster
    * @param clustername
    * @return the path to the cluster specification
    * @throws HoyaException if the specification is not there
    */
   public Path locateClusterSpecification(String clustername) throws
                                                              YarnException,
                                                              IOException {
     Path clusterDirectory = HoyaUtils.createHoyaClusterDirPath(getClusterFS(), clustername);
     Path clusterSpecPath = new Path(clusterDirectory, HoyaKeys.CLUSTER_SPECIFICATION_FILE);
     if (!getClusterFS().exists(clusterSpecPath)) {
       log.debug("Missing cluster specification file {}", clusterSpecPath);
       throw new HoyaException(EXIT_UNKNOWN_HOYA_CLUSTER,
                               E_UNKNOWN_CLUSTER + clustername +
                               "\n (cluster definition not found at "+clusterSpecPath);
     }
     return clusterSpecPath;
   }
 
   /**
    * Implement flexing
    * @param clustername name of the cluster
    * @param workers number of workers
    * @param masters number of masters
    * @return EXIT_SUCCESS if the #of nodes in a live cluster changed
    */
   public int actionFlex(String clustername, int workers, int masters, boolean persist) throws
                                                                                        YarnException,
                                                                                        IOException {
     verifyManagerSet();
     validateClusterName(clustername);
     if (workers < 0) {
       throw new BadCommandArgumentsException("Requested number of workers is out of range");
     }
     if (persist) {
       Path clusterDirectory = HoyaUtils.createHoyaClusterDirPath(getClusterFS(), clustername);
       Path clusterSpecPath = locateClusterSpecification(clustername);
       ClusterDescription clusterSpec = ClusterDescription.load(getClusterFS(), clusterSpecPath);
       //spec is loaded, just look at its state;
       verifySpecificationValidity(clusterSpecPath, clusterSpec);
 
       //update the specification
  
       if (clusterSpec.workers != workers) {
         clusterSpec.workers = workers;
         if (!HoyaUtils.updateClusterSpecification(getClusterFS(), clusterDirectory, clusterSpecPath, clusterSpec)) {
           log.warn("Failed to save new cluster size to {}",clusterSpecPath);
         }
         //there is no live instance, nothing to do;
         log.info("New cluster size: {} persisted",workers);
       } else {
         log.info("New cluster size: {} is the same as the current persisted size", workers);
       }
     }
     int exitCode = EXIT_FALSE;
 
     //now see if it is actually running and bail out if not
     verifyManagerSet();
     ApplicationReport instance = findInstance(getUsername(), clustername);
     if (instance!=null) {
       log.info("Flexing running cluster to size {}",workers);
       HoyaAppMasterProtocol appMaster = connect(instance);
       if (appMaster.flexNodes(workers)) {
         log.info("Cluster size updated");
         exitCode = EXIT_SUCCESS;
       } else {
         log.info("Requested cluster size is the same as current size: no change");
       }
     } else {
       log.info("No running cluster to update");
     }
     return exitCode;
   }
 
   /**
    * Connect to a live cluster and get its current state
    * @param clustername the cluster name
    * @return its description
    */
   @VisibleForTesting
   public ClusterDescription getClusterStatus(String clustername) throws
                                                                  YarnException,
                                                                  IOException {
     HoyaAppMasterProtocol appMaster = bondToCluster(clustername);
     String statusJson = appMaster.getClusterStatus();
     try {
       return ClusterDescription.fromJson(statusJson);
     } catch (JsonParseException e) {
       log.error(
         "Exception " + e + " parsing:\n" + JsonOutput.prettyPrint(statusJson),
         e);
       throw e;
     }
   }
 
   /**
    *   Bond to a running cluster
    *
    * @param clustername cluster name
    * @return the AM RPC client
    * @throws HoyaException if the cluster is unkown
    */
   private HoyaAppMasterProtocol bondToCluster(String clustername) throws
                                                                   YarnException,
                                                                   IOException {
     verifyManagerSet();
     ApplicationReport instance = findInstance(getUsername(), clustername);
     if (null==instance) {
       throw unknownClusterException(clustername);
     }
     return connect(instance);
   }
 
   /**
    * Wait for the hbase master to be live (or past it in the lifecycle)
    * @param clustername cluster
    * @param timeout time to wait
    * @return the state. If still in CREATED, the cluster didn't come up
    * in the time period. If LIVE, all is well. If >LIVE, it has shut for a reason
    * @throws IOException
    * @throws HoyaException
    */
   public int waitForHBaseMasterLive(String clustername, long timeout)
     throws IOException, YarnException {
     Duration duration = new Duration(timeout).start();
     boolean live = false;
     int state = ClusterDescription.STATE_CREATED;
     while (!live) {
       ClusterDescription cd = getClusterStatus(clustername);
       //see if there is a master node yet
       if (!cd.masterNodes.isEmpty()) {
         //if there is, get the node
         ClusterNode master = cd.masterNodes.get(0);
         state = master.state;
         live = state >= ClusterDescription.STATE_LIVE;
         }
       if (!live && !duration.getLimitExceeded()) {
         try {
           Thread.sleep(1000);
         } catch (InterruptedException ignored) {
           //ignored
         }
       }
     }
     return state;
   }
 
 
   public HoyaException unknownClusterException(String clustername) {
     return new HoyaException(EXIT_UNKNOWN_HOYA_CLUSTER,
                             "Hoya cluster not found: '"+clustername+"' ");
   }
 
 /*
 
   public List<ApplicationReport> getApplicationList() throws YarnException,
                                                           IOException {
     return getApplications(null);
   }
 */
 
   /**
    * Implementation of set-ness, groovy definition of true/false for a string
    * @param s
    * @return
    */
   private static boolean isUnset(String s) { return s ==null || s.isEmpty();}
   private static boolean isSet(String s) { return !isUnset(s);}
 }
