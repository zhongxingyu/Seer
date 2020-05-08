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
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileAlreadyExistsException;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hoya.HoyaExitCodes;
 import org.apache.hadoop.hoya.HoyaKeys;
 import org.apache.hadoop.hoya.api.ClusterDescription;
 import org.apache.hadoop.hoya.api.ClusterNode;
 import org.apache.hadoop.hoya.api.HoyaClusterProtocol;
 import org.apache.hadoop.hoya.api.OptionKeys;
 import org.apache.hadoop.hoya.api.RoleKeys;
 import org.apache.hadoop.hoya.api.proto.Messages;
 import org.apache.hadoop.hoya.exceptions.BadCommandArgumentsException;
 import org.apache.hadoop.hoya.exceptions.BadConfigException;
 import org.apache.hadoop.hoya.exceptions.HoyaException;
 import org.apache.hadoop.hoya.exceptions.NoSuchNodeException;
 import org.apache.hadoop.hoya.exceptions.WaitTimeoutException;
 import org.apache.hadoop.hoya.providers.ClientProvider;
 import org.apache.hadoop.hoya.providers.HoyaProviderFactory;
 import org.apache.hadoop.hoya.providers.ProviderRole;
 import org.apache.hadoop.hoya.tools.ConfigHelper;
 import org.apache.hadoop.hoya.tools.Duration;
 import org.apache.hadoop.hoya.tools.HoyaUtils;
 import org.apache.hadoop.hoya.yarn.CommonArgs;
 import org.apache.hadoop.hoya.yarn.HoyaActions;
 import org.apache.hadoop.hoya.yarn.appmaster.HoyaMasterServiceArgs;
 import org.apache.hadoop.hoya.yarn.appmaster.rpc.RpcBinder;
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
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 /**
  * Client service for Hoya
  */
 
 public class HoyaClient extends YarnClientImpl implements RunService,
                                                           HoyaExitCodes,
                                                           HoyaKeys {
   protected static final Logger log = LoggerFactory.getLogger(HoyaClient.class);
 
   public static final int ACCEPT_TIME = 60000;
   public static final String E_CLUSTER_RUNNING = "cluster already running";
   public static final String E_ALREADY_EXISTS = "already exists";
   public static final String E_MISSING_PATH = "Missing path ";
   public static final String E_INCOMPLETE_CLUSTER_SPEC =
     "Cluster specification is marked as incomplete: ";
   public static final String E_UNKNOWN_CLUSTER = "Unknown cluster ";
   public static final String E_DESTROY_CREATE_RACE_CONDITION =
     "created while it was being destroyed";
   public static final int DEFAULT_AM_MEMORY = 10;
   public static final String HOYA_JAR = "hoya.jar";
   public static final String JCOMMANDER_JAR = "jcommander.jar";
   public static final String SLF4J_JAR = "slf4j.jar";
   public static final String SLF4J_LOG4J_JAR = "slf4j-log4j.jar";
   private int amPriority = 0;
   // Queue for App master
   private String amQueue = "default";
 
 
   private String[] argv;
   private ClientArgs serviceArgs;
   public ApplicationId applicationId;
   private String deployedClusterName;
 
   /**
    * Entry point from the service launcher
    */
   public HoyaClient() {
     //make sure all the yarn configs get loaded
     YarnConfiguration yarnConfiguration = new YarnConfiguration();
     //register the unique hoya resource *after*
     URL clientconf = HoyaUtils.registerHoyaClientResource();
     if (clientconf == null) {
       log.debug("failed to find {} on the classpath", HOYA_CLIENT_RESOURCE);
     } else {
       log.debug("loaded client resources from {}", clientconf);
     } 
   }
 
   @Override //Service
   public String getName() {
     return "Hoya";
   }
 
   @Override
   public Configuration bindArgs(Configuration config, String... args) throws Exception {
     this.argv = args;
     serviceArgs = new ClientArgs(args);
     serviceArgs.parse();
     serviceArgs.postProcess();
     //yarn-ify
     YarnConfiguration yarnConfiguration = new YarnConfiguration(config);
     return HoyaUtils.patchConfiguration(yarnConfiguration);
   }
 
   @Override
   protected void serviceInit(Configuration conf) throws Exception {
     Configuration clientConf = HoyaUtils.loadHoyaClientConfigurationResource();
     ConfigHelper.mergeConfigurations(conf, clientConf, HOYA_CLIENT_RESOURCE);
     serviceArgs.applyDefinitions(conf);
     serviceArgs.applyFileSystemURL(conf);
     super.serviceInit(conf);
   }
 
   /**
    * this is where the work is done.
    * @return the exit code
    * @throws Throwable anything that went wrong
    */
   @Override
   public int runService() throws Throwable {
 
     //choose the action
     String action = serviceArgs.action;
     int exitCode = EXIT_SUCCESS;
     String clusterName = serviceArgs.getClusterName();
     //actions
     if (HoyaActions.ACTION_BUILD.equals(action)) {
       actionBuild(clusterName);
       exitCode = EXIT_SUCCESS;
     } else if (HoyaActions.ACTION_CREATE.equals(action)) {
       exitCode = actionCreate(clusterName);
     } else if (HoyaActions.ACTION_FREEZE.equals(action)) {
       exitCode = actionFreeze(clusterName, serviceArgs.waittime, "stopping cluster");
     } else if (HoyaActions.ACTION_THAW.equals(action)) {
       exitCode = actionThaw(clusterName);
     } else if (HoyaActions.ACTION_DESTROY.equals(action)) {
       validateClusterName(clusterName);
       exitCode = actionDestroy(clusterName);
     } else if (HoyaActions.ACTION_EXISTS.equals(action)) {
       validateClusterName(clusterName);
       exitCode = actionExists(clusterName);
     } else if (HoyaActions.ACTION_FLEX.equals(action)) {
       validateClusterName(clusterName);
       exitCode = actionFlex(clusterName);
     } else if (HoyaActions.ACTION_GETCONF.equals(action)) {
       File outfile = null;
       if (serviceArgs.output != null) {
         outfile = new File(serviceArgs.output);
       }
       exitCode = actionGetConf(clusterName,
                                serviceArgs.format,
                                outfile);
     } else if (HoyaActions.ACTION_HELP.equals(action) ||
                HoyaActions.ACTION_USAGE.equals(action)) {
       log.info("HoyaClient {}", serviceArgs.usage());
 
     } else if (HoyaActions.ACTION_LIST.equals(action)) {
       if (!isUnset(clusterName)) {
         validateClusterName(clusterName);
       }
       exitCode = actionList(clusterName);
     } else if (HoyaActions.ACTION_STATUS.equals(action)) {
       validateClusterName(clusterName);
       exitCode = actionStatus(clusterName);
     } else {
       throw new HoyaException(EXIT_UNIMPLEMENTED,
                               "Unimplemented: " + action);
     }
 
     return exitCode;
   }
 
   /**
    * verify that the supplied cluster name is valid
    * @param clustername cluster name
    * @throws BadCommandArgumentsException if it is invalid
    */
   protected void validateClusterName(String clustername) throws
                                                          BadCommandArgumentsException {
     if (!HoyaUtils.isClusternameValid(clustername)) {
       throw new BadCommandArgumentsException(
         "Illegal cluster name: " + clustername);
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
     Path clusterDirectory = HoyaUtils.buildHoyaClusterDirPath(fs, clustername);
     //delete the directory;
     fs.delete(clusterDirectory, true);
 
     List<ApplicationReport> instances = findAllLiveInstances(null, clustername);
     // detect any race leading to cluster creation during the check/destroy process
     // and report a problem.
     if (!instances.isEmpty()) {
       throw new HoyaException(EXIT_BAD_CLUSTER_STATE,
                               clustername + ": "
                               + E_DESTROY_CREATE_RACE_CONDITION
                               + " :" +
                               instances.get(0));
     }
     log.info("Destroyed cluster {}", clustername);
     return EXIT_SUCCESS;
   }
 
   /**
    * Get the provider for this cluster
    * @param clusterSpec cluster spec
    * @return the provider instance
    * @throws HoyaException problems building the provider
    */
   private ClientProvider createClientProvider(ClusterDescription clusterSpec)
     throws HoyaException {
     HoyaProviderFactory factory =
       HoyaProviderFactory.createHoyaProviderFactory(clusterSpec);
     return factory.createClientProvider();
   }
 
   /**
    * Get the provider for this cluster
    * @param provider the name of the provider
    * @return the provider instance
    * @throws HoyaException problems building the provider
    */
   private ClientProvider createClientProvider(String provider)
     throws HoyaException {
     HoyaProviderFactory factory =
       HoyaProviderFactory.createHoyaProviderFactory(provider);
     return factory.createClientProvider();
   }
 
   /**
    * Create the cluster -saving the arguments to a specification file first
    * 
    * @param clustername cluster name
    * @return the status code
    * @throws YarnException Yarn problems
    * @throws IOException other problems
    * @throws BadCommandArgumentsException bad arguments.
    */
   private int actionCreate(String clustername) throws
                                                YarnException,
                                                IOException {
 
     actionBuild(clustername);
     return startCluster(clustername);
   }
   
   /**
    * Build up the cluster specification/directory 
    * @param clustername cluster name
    * @throws YarnException Yarn problems
    * @throws IOException other problems
    * @throws BadCommandArgumentsException bad arguments.
    */
   private void actionBuild(String clustername) throws
                                                YarnException,
                                                IOException {
 
     //verify that a live cluster isn't there
     validateClusterName(clustername);
     verifyManagerSet();
     verifyFileSystemArgSet();
     verifyNoLiveClusters(clustername);
     //build up the initial cluster specification
     ClusterDescription clusterSpec = new ClusterDescription();
 
     requireArgumentSet(CommonArgs.ARG_ZKHOSTS, serviceArgs.zkhosts);
     requireArgumentSet(CommonArgs.ARG_HBASE_VER, serviceArgs.hbasever);
     Path appconfdir = serviceArgs.confdir;
     requireArgumentSet(CommonArgs.ARG_CONFDIR, appconfdir);
     //Provider 
     requireArgumentSet(CommonArgs.ARG_PROVIDER, serviceArgs.provider);
 
     ClientProvider provider = createClientProvider(serviceArgs.provider);
 
     //remember this
     clusterSpec.type = provider.getName();
     clusterSpec.name = clustername;
     clusterSpec.state = ClusterDescription.STATE_INCOMPLETE;
     clusterSpec.createTime = System.currentTimeMillis();
     //build up the options map
     clusterSpec.options = provider.getDefaultClusterOptions();
     HoyaUtils.mergeMap(clusterSpec.options, serviceArgs.getOptionsMap());
 
 
     //get the list of supported roles
     List<ProviderRole> supportedRoles = provider.getRoles();
     //and any extra
     Map<String, String> roleMap = serviceArgs.getRoleMap();
 
     Map<String, Map<String, String>> clusterRoleMap =
       new HashMap<String, Map<String, String>>();
 
     //build the role map from default; set the instances
     for (ProviderRole role : supportedRoles) {
       String roleName = role.name;
       Map<String, String> clusterRole =
         provider.createDefaultClusterRole(roleName);
       //get the command line instance count
       String instanceCount = roleMap.get(roleName);
       //this is here in case we want to extract from the provider
       //the min #of instances
       int defInstances =
         HoyaUtils.getIntValue(clusterRole, RoleKeys.ROLE_INSTANCES, 0, 0, -1);
      instanceCount = Integer.toString(HoyaUtils.parseAndValidate(
          "count of role " + roleName, instanceCount, defInstances, 0, -1));
       clusterRole.put(RoleKeys.ROLE_INSTANCES, instanceCount);
       clusterRoleMap.put(roleName, clusterRole);
     }
 
     //now enhance the role option map with all command line options 
     Map<String, Map<String, String>> commandOptions =
       serviceArgs.getRoleOptionMap();
     HoyaUtils.applyCommandLineOptsToRoleMap(clusterRoleMap, commandOptions);
 
     clusterSpec.roles = clusterRoleMap;
 
     //App home or image
     if (serviceArgs.image != null) {
       if (!isUnset(serviceArgs.appHomeDir)) {
         //both args have been set
         throw new BadCommandArgumentsException("only one of "
                                                + CommonArgs.ARG_IMAGE
                                                + " and " +
                                                CommonArgs.ARG_APP_HOME +
                                                " can be provided");
       }
       clusterSpec.imagePath = serviceArgs.image.toUri().toString();
     } else {
       //the alternative is app home, which now MUST be set
       if (isUnset(serviceArgs.appHomeDir)) {
         //both args have been set
         throw new BadCommandArgumentsException("Either " + CommonArgs.ARG_IMAGE
                                                + " or " +
                                                CommonArgs.ARG_APP_HOME +
                                                " must be provided");
       }
       clusterSpec.applicationHome = serviceArgs.appHomeDir;
     }
 
     //set up the ZK binding
     String zookeeperRoot = serviceArgs.appZKPath;
     if (isUnset(serviceArgs.appZKPath)) {
       zookeeperRoot =
         "/yarnapps_" + getAppName() + "_" + getUsername() + "_" + clustername;
     }
     clusterSpec.zkPath = zookeeperRoot;
     clusterSpec.zkPort = serviceArgs.zkport;
     clusterSpec.zkHosts = serviceArgs.zkhosts;
     clusterSpec.hbasever = serviceArgs.hbasever;
     
     //another sanity check before the cluster dir is created: the config
     //dir
     FileSystem srcFS = FileSystem.get(appconfdir.toUri(), getConfig());
     if (!srcFS.exists(appconfdir)) {
       throw new BadCommandArgumentsException("Configuration directory specified in %s not found: %s",
                                              ClientArgs.ARG_CONFDIR, appconfdir.toString());
     }
     //build up the paths in the DFS
 
     FileSystem fs = getClusterFS();
     Path clusterDirectory = HoyaUtils.createHoyaClusterDirPath(fs, clustername);
     Path origConfPath = new Path(clusterDirectory, HoyaKeys.ORIG_CONF_DIR_NAME);
     Path generatedConfPath =
       new Path(clusterDirectory, HoyaKeys.GENERATED_CONF_DIR_NAME);
     Path clusterSpecPath =
       new Path(clusterDirectory, HoyaKeys.CLUSTER_SPECIFICATION_FILE);
     clusterSpec.originConfigurationPath = origConfPath.toUri().toASCIIString();
     clusterSpec.generatedConfigurationPath =
       generatedConfPath.toUri().toASCIIString();
     //save the specification to get a lock on this cluster name
     try {
       clusterSpec.save(fs, clusterSpecPath, false);
     } catch (FileAlreadyExistsException fae) {
       throw new HoyaException(EXIT_BAD_CLUSTER_STATE,
                               clustername + ": " + E_ALREADY_EXISTS + " :" +
                               clusterSpecPath);
     } catch (IOException e) {
       //this is probably a file exists exception too, but include it in the trace just in case
       throw new HoyaException(EXIT_BAD_CLUSTER_STATE, e,
                               clustername + ": " + E_ALREADY_EXISTS + " :" +
                               clusterSpecPath);
     }
 
     //bulk copy
     //first the original from wherever to the DFS
     HoyaUtils.copyDirectory(getConfig(), appconfdir, origConfPath);
     //then build up the generated path
     HoyaUtils.copyDirectory(getConfig(), origConfPath, generatedConfPath);
 
     //Data Directory
     Path datapath =
       new Path(clusterDirectory, HoyaKeys.DATA_DIR_NAME);
     //create the data dir
     fs.mkdirs(datapath);
 
     log.debug("datapath={}", datapath);
     clusterSpec.dataPath = datapath.toUri().toString();
 
     //final specification review
     provider.reviewAndUpdateClusterSpec(clusterSpec);
     
     //here the configuration is set up. Mark it
     clusterSpec.state = ClusterDescription.STATE_CREATED;
     clusterSpec.save(fs, clusterSpecPath, true);
 
   }
 
   private void requireArgumentSet(String argname, String argfield) throws
                                                                    BadCommandArgumentsException {
     if (isUnset(argfield)) {
       throw new BadCommandArgumentsException("Required argument "
                                              + argname
                                              + " missing");
     }
   }
  private void requireArgumentSet(String argname, Object argfield) throws
                                                                    BadCommandArgumentsException {
     if (argfield == null) {
       throw new BadCommandArgumentsException("Required argument "
                                              + argname
                                              + " missing");
     }
   }
 
 
   public void verifyFileSystemArgSet() throws BadCommandArgumentsException {
     requireArgumentSet(CommonArgs.ARG_FILESYSTEM, serviceArgs.filesystemURL);
   }
 
 
   public void verifyManagerSet() throws BadCommandArgumentsException {
     InetSocketAddress rmAddr = HoyaUtils.getRmAddress(getConfig());
     if (!HoyaUtils.isAddressDefined(rmAddr)) {
       throw new BadCommandArgumentsException(
         "No valid Resource Manager address provided in the argument "
         + CommonArgs.ARG_MANAGER
         + " or the configuration property "
         + YarnConfiguration.RM_ADDRESS 
         + " value :" + rmAddr);
     }
   }
 
   /**
    * Create a cluster to the specification
    * @param clusterSpec cluster specification
    * @return the exit code from the operation
    */
   public int executeClusterStart(Path clusterDirectory,
                                  ClusterDescription clusterSpec) throws
                                                                     YarnException,
                                                                     IOException {
 
     //verify that a live cluster isn't there;
     String clustername = clusterSpec.name;
     deployedClusterName = clustername;
     validateClusterName(clustername);
     verifyNoLiveClusters(clustername);
     Configuration config = getConfig();
     //Provider 
     ClientProvider provider = createClientProvider(clusterSpec);
     //make sure it is valid;
 
     Path generatedConfDirPath =
       createPathThatMustExist(clusterSpec.generatedConfigurationPath);
     Path origConfPath =
       createPathThatMustExist(clusterSpec.originConfigurationPath);
 
     //now build up the image path
     //TODO: consider supporting apps that don't have an image path
     Path imagePath;
     String csip = clusterSpec.imagePath;
     if (!isUnset(csip)) {
       imagePath = createPathThatMustExist(csip);
     } else {
       imagePath = null;
       if (isUnset(clusterSpec.applicationHome)) {
         throw new HoyaException(EXIT_BAD_CLUSTER_STATE,
                                 "Neither an image path or binary home dir were specified");
       }
     }
 
     //final specification review
     provider.validateClusterSpec(clusterSpec);
 
     //do a quick dump of the values first
     if (log.isDebugEnabled()) {
       Map<String, Map<String, String>> roleopts = clusterSpec.roles;
       for (Map.Entry<String, Map<String, String>> role : roleopts.entrySet()) {
         log.debug("Role: {}", role.getKey());
         log.debug(HoyaUtils.stringifyMap(role.getValue()));
       }
     }
 
     YarnClientApplication application = createApplication();
     ApplicationSubmissionContext appContext =
       application.getApplicationSubmissionContext();
     ApplicationId appId = appContext.getApplicationId();
     // set the application name;
     appContext.setApplicationName(clustername);
     //app type used in service enum;
     appContext.setApplicationType(HoyaKeys.APP_TYPE);
 
     if (clusterSpec.getOptionBool(OptionKeys.OPTION_TEST, false)) {
       //test flag set
       appContext.setMaxAppAttempts(1);
     }
 
     FileSystem fs = getClusterFS();
     Path tempPath = HoyaUtils.createHoyaAppInstanceTempPath(fs,
                                                             clustername,
                                                             appId.toString());
 
     // Set up the container launch context for the application master
     ContainerLaunchContext amContainer =
       Records.newRecord(ContainerLaunchContext.class);
 
     // set local resources for the application master
     // local files or archives as needed
     // In this scenario, the jar file for the application master is part of the local resources			
     Map<String, LocalResource> localResources =
       new HashMap<String, LocalResource>();
     //conf directory setup
     Path remoteHoyaConfPath = null;
     String relativeHoyaConfDir = null;
     String hoyaConfdirProp = System.getProperty(HoyaKeys.PROPERTY_HOYA_CONF_DIR);
     if (hoyaConfdirProp == null || hoyaConfdirProp.isEmpty()) {
       log.debug("No local configuration directory provided as system property");
     } else {
       File hoyaConfDir = new File(hoyaConfdirProp);
       if (!hoyaConfDir.exists()) {
         throw new BadConfigException("Conf dir \"%s\" not found", hoyaConfDir);
       }
       Path localConfDirPath = HoyaUtils.createLocalPath(hoyaConfDir);
       remoteHoyaConfPath = new Path(clusterDirectory,
                                    HoyaKeys.SUBMITTED_HOYA_CONF_DIR);
       HoyaUtils.copyDirectory(config, localConfDirPath, remoteHoyaConfPath);
     }
     
     if (!getUsingMiniMRCluster()) {
       //the assumption here is that minimr cluster => this is a test run
       //and the classpath can look after itself
       
       //insert conf dir first
       if (remoteHoyaConfPath != null) {
         relativeHoyaConfDir = HoyaKeys.SUBMITTED_HOYA_CONF_DIR;
         Map<String, LocalResource> submittedConfDir =
           HoyaUtils.submitDirectory(fs, remoteHoyaConfPath, relativeHoyaConfDir);
         HoyaUtils.mergeMaps(localResources, submittedConfDir);
       }
 
 
       log.info("Copying JARs from local filesystem");
       // Copy the application master jar to the filesystem
       // Create a local resource to point to the destination jar path 
       String bindir = "";
       //add this class
       localResources.put(HOYA_JAR, submitJarWithClass(this.getClass(),
                                                         tempPath,
                                                         bindir,
                                                         HOYA_JAR));
       //add lib classes that don't come automatically with YARN AM classpath
       String libdir = bindir + "lib/";
 
 
       localResources.put(JCOMMANDER_JAR, submitJarWithClass(JCommander.class,
                                                               tempPath,
                                                               libdir,
                                                               JCOMMANDER_JAR));
 
 
       localResources.put(SLF4J_JAR, submitJarWithClass(Logger.class,
                                                          tempPath,
                                                          libdir,
                                                          SLF4J_JAR));
 
       localResources.put(SLF4J_LOG4J_JAR,
                          submitJarWithClass(Log4jLoggerAdapter.class,
                                             tempPath,
                                             libdir,
                                             SLF4J_LOG4J_JAR));
 
     }
 
     //build up the configuration -and have it add any other provider
     //specific artifacts to the local resource map
 
 
     
     Map<String, LocalResource> confResources;
     confResources = provider.prepareAMAndConfigForLaunch(fs,
                                                          config,
                                                          clusterSpec,
                                                          origConfPath,
                                                          generatedConfDirPath);
     localResources.putAll(confResources);
 
     //now add the image if it was set
     if (HoyaUtils.maybeAddImagePath(fs, localResources, imagePath)) {
       log.debug("Registered image path {}", imagePath);
     }
 
     if (log.isDebugEnabled()) {
       for (String key : localResources.keySet()) {
         LocalResource val = localResources.get(key);
         log.debug("{}={}", key, HoyaUtils.stringify(val.getResource()));
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
 
 
     //build the environment
     Map<String, String> env =
       HoyaUtils.buildEnvMap(clusterSpec.getOrAddRole("master"));
     String classpath = buildClasspath(relativeHoyaConfDir);
     log.debug("AM classpath={}", classpath);
     env.put("CLASSPATH", classpath);
     log.debug("Environment Map:\n{}", HoyaUtils.stringifyMap(env));
     amContainer.setEnvironment(env);
 
     String rmAddr = serviceArgs.rmAddress;
     //spec out the RM address
     if (isUnset(rmAddr) && HoyaUtils.isRmSchedulerAddressDefined(config)) {
       rmAddr =
         NetUtils.getHostPortString(HoyaUtils.getRmSchedulerAddress(config));
     }
 
     //build up the args list, intially as anyting
     List<String> commands = new ArrayList<String>(20);
     commands.add(ApplicationConstants.Environment.JAVA_HOME.$() + "/bin/java");
     //insert any JVM options);
     commands.add(HoyaKeys.JAVA_FORCE_IPV4);
     commands.add(HoyaKeys.JAVA_HEADLESS);
     commands.add(String.format(HoyaKeys.FORMAT_D_CLUSTER_NAME, clustername));
     commands.add(String.format(HoyaKeys.FORMAT_D_CLUSTER_TYPE, provider.getName()));
     //add the generic sevice entry point
     commands.add(ServiceLauncher.ENTRY_POINT);
     //immeiately followed by the classname
     commands.add(HoyaMasterServiceArgs.CLASSNAME);
     //now the app specific args
     commands.add(HoyaMasterServiceArgs.ARG_DEBUG);
     commands.add(HoyaActions.ACTION_CREATE);
     commands.add(clustername);
 
     //set the cluster directory path
     commands.add(HoyaMasterServiceArgs.ARG_HOYA_CLUSTER_URI);
     commands.add(clusterDirectory.toUri().toString());
 
     if (!isUnset(rmAddr)) {
       commands.add(HoyaMasterServiceArgs.ARG_RM_ADDR);
       commands.add(rmAddr);
     }
 
     if (serviceArgs.filesystemURL != null) {
       commands.add(CommonArgs.ARG_FILESYSTEM);
       commands.add(serviceArgs.filesystemURL.toString());
     }
 
 
     //write out the path output
     commands.add(
       "1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/out.txt");
     commands.add(
       "2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/err.txt");
 
     String cmdStr = HoyaUtils.join(commands, " ");
     log.info("Completed setting up app master command {}", cmdStr);
 
     amContainer.setCommands(commands);
     // Set up resource type requirements
     Resource capability = Records.newRecord(Resource.class);
     // Amt. of memory resource to request for to run the App Master
     capability.setMemory(DEFAULT_AM_MEMORY);
     capability.setVirtualCores(1);
     provider.prepareAMResourceRequirements(clusterSpec, capability);
     appContext.setResource(capability);
     Map<String, ByteBuffer> serviceData = new HashMap<String, ByteBuffer>();
     // Service data is a binary blob that can be passed to the application
     // Not needed in this scenario
     provider.prepareAMServiceData(clusterSpec, serviceData);
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
     if (HoyaUtils.hasAppFinished(report)) {
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
         if (report != null &&
             report.getYarnApplicationState() == YarnApplicationState.RUNNING) {
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
                               clustername + ": " + E_CLUSTER_RUNNING + " :" +
                               existing.get(0));
     }
   }
 
   public String getUsername() throws IOException {
     return UserGroupInformation.getCurrentUser().getShortUserName();
   }
 
   /**
    * Get the name of any deployed cluster
    * @return the cluster name
    */
   public String getDeployedClusterName() {
     return deployedClusterName;
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
   private LocalResource submitJarWithClass(Class clazz,
                                            Path tempPath,
                                            String subdir,
                                            String jarName)
     throws IOException, HoyaException {
     File localFile = HoyaUtils.findContainingJar(clazz);
     if (null == localFile) {
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
   private LocalResource submitFile(File localFile,
                                    Path tempPath,
                                    String subdir,
                                    String destFileName) throws IOException {
     Path src = new Path(localFile.toString());
     Path destPath = new Path(tempPath, subdir + destFileName);
 
     getClusterFS().copyFromLocalFile(false, true, src, destPath);
 
     // Set the type of resource - file or archive
     // archives are untarred at destination
     // we don't need the jar file to be untarred for now
     return HoyaUtils.createAmResource(getClusterFS(),
                                       destPath,
                                       LocalResourceType.FILE);
   }
 
   /**
    * Get the filesystem of this cluster
    * @return the FS of the config
    */
   private FileSystem getClusterFS() throws IOException {
     return FileSystem.get(serviceArgs.filesystemURL, getConfig());
   }
 
   /**
    * Build up the classpath for execution 
    * -behaves very differently on a mini test cluster vs a production
    * one.
    * @param hoyaConfDir relative path to the dir containing hoya config options
    * to put on the classpath -or null
    * @return a classpath
    */
   private String buildClasspath(String hoyaConfDir) {
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
       if (hoyaConfDir != null) {
         classPathEnv.append(File.pathSeparatorChar).append(hoyaConfDir);
       }
     }
     return classPathEnv.toString();
   }
 
   /**
    * ask if the client is using a mini MR cluster
    * @return
    */
   private boolean getUsingMiniMRCluster() {
     return getConfig().getBoolean(YarnConfiguration.IS_MINI_YARN_CLUSTER,
                                   false);
   }
 
   private String getAppName() {
     return "hoya";
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
   private int buildExitCode(ApplicationId appId,
                             ApplicationReport report) throws
                                                       IOException,
                                                       YarnException {
     if (null == report) {
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
                    "YarnState = {}, DSFinalStatus = {} Breaking monitoring loop",
                    state, dsStatus);
           return EXIT_YARN_SERVICE_FINISHED_WITH_ERROR;
         }
 
       case KILLED:
         log.info("Application did not finish. YarnState={}, DSFinalStatus={}",
                  state, dsStatus);
         return EXIT_YARN_SERVICE_KILLED;
 
       case FAILED:
         log.info("Application Failed. YarnState={}, DSFinalStatus={}", state,
                  dsStatus);
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
    * Get the report of a this application
    * @return the app report or null if it could not be found.
    * @throws IOException
    * @throws YarnException
    */
   public ApplicationReport getApplicationReport() throws
                                                   IOException,
                                                   YarnException {
     return getApplicationReport(applicationId);
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
 
     if (duration.limit <= 0) {
       throw new HoyaException("Invalid monitoring duration");
     }
     log.debug("Waiting {} millis for app to reach state {} ",
               duration.limit,
               desiredState);
     duration.start();
     while (true) {
 
       // Get application report for the appId we are interested in 
 
       ApplicationReport r = getApplicationReport(appId);
 
       log.debug("queried status is\n{}",
                 new HoyaUtils.OnDemandReportStringifier(r));
 
       YarnApplicationState state = r.getYarnApplicationState();
       if (state.ordinal() >= desiredState.ordinal()) {
         log.debug("App in desired state (or higher) :{}", state);
         return r;
       }
       if (duration.getLimitExceeded()) {
         log.debug(
           "Wait limit of {} millis to get to state {}, exceeded; app status\n {}",
           duration.limit,
           desiredState,
           new HoyaUtils.OnDemandReportStringifier(r));
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
   private KillApplicationResponse killRunningApplication(ApplicationId applicationId,
                                                          String reason) throws
                                                                         YarnException,
                                                                         IOException {
     log.info("Killing application {} - {}", applicationId.getClusterTimestamp(),
              reason);
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
     Set<String> types = new HashSet<String>(1);
     types.add(HoyaKeys.APP_TYPE);
     List<ApplicationReport> allApps = getApplications(types);
     List<ApplicationReport> results = new ArrayList<ApplicationReport>();
     for (ApplicationReport report : allApps) {
       if (user == null || user.equals(report.getUser())) {
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
 
     if (clustername == null || clustername.isEmpty()) {
       log.info("Hoya instances for {}:{}",
                (user != null ? user : "all users"),
                instances.size());
       for (ApplicationReport report : instances) {
         logAppReport(report);
       }
       return EXIT_SUCCESS;
     } else {
       validateClusterName(clustername);
       log.debug("Listing cluster named {}", clustername);
       ApplicationReport report =
         findClusterInInstanceList(instances, clustername);
       if (report != null) {
         logAppReport(report);
         return EXIT_SUCCESS;
       } else {
         throw unknownClusterException(clustername);
       }
     }
   }
 
   /**
    * Log the application report at INFO
    * @param report
    */
   public void logAppReport(ApplicationReport report) {
     log.info(HoyaUtils.appReportToString(report, "\n"));
   }
 
   /**
    * Implement the islive action: probe for a cluster of the given name existing
    *
    * @return exit code
    */
   @VisibleForTesting
   public int actionFlex(String name) throws YarnException, IOException {
     verifyManagerSet();
     log.debug("actionFlex({})", name);
     Map<String, Integer> roleInstances = new HashMap<String, Integer>();
     Map<String, String> roleMap = serviceArgs.getRoleMap();
     for (Map.Entry<String, String> roleEntry : roleMap.entrySet()) {
       String key = roleEntry.getKey();
       String val = roleEntry.getValue();
       try {
         roleInstances.put(key, Integer.valueOf(val));
       } catch (NumberFormatException e) {
         throw new BadCommandArgumentsException("Requested count of role %s" +
                                                " is not a number: \"%s\"",
                                                key, val);
       }
     }
     return flex(name, roleInstances, serviceArgs.persist);
   }
   /**
    * Implement the islive action: probe for a cluster of the given name existing
    *
    * @return exit code
    */
   @VisibleForTesting
   public int actionExists(String name) throws YarnException, IOException {
     verifyManagerSet();
     log.debug("actionExists({})", name);
     ApplicationReport instance = findInstance(getUsername(), name);
     if (instance == null) {
       log.info("cluster {} not found");
       throw unknownClusterException(name);
     } else {
       //the app exists, but it may be in a terminated state
       HoyaUtils.OnDemandReportStringifier report =
         new HoyaUtils.OnDemandReportStringifier(instance);
       YarnApplicationState state =
         instance.getYarnApplicationState();
       if (state.ordinal() >= YarnApplicationState.FINISHED.ordinal()) {
         log.info("Cluster {} found but is in state {}", state);
         log.debug("State {}", report);
         throw unknownClusterException(name);
       }
       log.info("Cluster {} is running:\n{}", name, report);
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
   public List<ApplicationReport> findAllInstances(String user,
                                                   String appname) throws
                                                                   IOException,
                                                                   YarnException {
     List<ApplicationReport> instances = listHoyaInstances(user);
     List<ApplicationReport> results =
       new ArrayList<ApplicationReport>(instances.size());
     for (ApplicationReport report : instances) {
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
   public List<ApplicationReport> findAllLiveInstances(String user,
                                                       String appname) throws
                                                                       YarnException,
                                                                       IOException {
     List<ApplicationReport> instances = listHoyaInstances(user);
     List<ApplicationReport> results =
       new ArrayList<ApplicationReport>(instances.size());
     for (ApplicationReport app : instances) {
       if (app.getName().equals(appname)
           && app.getYarnApplicationState().ordinal() <=
              YarnApplicationState.RUNNING
                                  .ordinal()) {
         results.add(app);
       }
     }
     return results;
 
   }
 
   public ApplicationReport findClusterInInstanceList(List<ApplicationReport> instances,
                                                      String appname) {
     ApplicationReport found = null;
     ApplicationReport foundAndLive = null;
     for (ApplicationReport app : instances) {
       if (app.getName().equals(appname)) {
         found = app;
         if (app.getYarnApplicationState().ordinal() <=
             YarnApplicationState.RUNNING.ordinal()) {
           foundAndLive = app;
         }
       }
     }
     if (foundAndLive != null) {
       found = foundAndLive;
     }
     return found;
   }
 
   private HoyaClusterProtocol connect(ApplicationReport app) throws
                                                               YarnException,
                                                               IOException {
     String host = app.getHost();
     int port = app.getRpcPort();
     String address = host + ":" + port;
     if (host == null || 0 == port) {
       throw new HoyaException(EXIT_CONNECTIVTY_PROBLEM,
                               "Hoya YARN instance " + app.getName() + " isn't" +
                               " providing a valid address for the" +
                               " Hoya RPC protocol: " + address);
     }
     InetSocketAddress addr = NetUtils.createSocketAddrForHost(host, port);
     UserGroupInformation currentUser = UserGroupInformation.getCurrentUser();
     Configuration conf = getConfig();
     
     return RpcBinder.connectToServer(addr,currentUser,conf, 15000);
 
   }
   
   
 
   /**
    * Status operation; 'name' arg defines cluster name.
    * @return
    */
   @VisibleForTesting
   public int actionStatus(String clustername) throws
                                               YarnException,
                                               IOException {
     verifyManagerSet();
     validateClusterName(clustername);
     ClusterDescription status = getClusterStatus(clustername);
     log.info(status.toJsonString());
     return EXIT_SUCCESS;
   }
 
   /**
    * Stop the cluster
    *
    * @param clustername cluster name
    * @param text
    * @return the cluster name
    */
   public int actionFreeze(String clustername, int waittime, String text) throws
                                                             YarnException,
                                                             IOException {
     verifyManagerSet();
     validateClusterName(clustername);
     log.debug("actionFreeze({}, {})", clustername, waittime);
     ApplicationReport app = findInstance(getUsername(), clustername);
     if (app == null) {
       //exit early
       log.info("Cluster {} not running", clustername);
       //not an error to freeze a frozen cluster
       return EXIT_SUCCESS;
     }
     log.debug("App to freeze was found: {}:\n{}", clustername,
               new HoyaUtils.OnDemandReportStringifier(app));
     if (app.getYarnApplicationState().ordinal() >=
         YarnApplicationState.FINISHED.ordinal()) {
       log.info("Cluster {} is a terminated state {}", clustername,
                app.getYarnApplicationState());
       return EXIT_SUCCESS;
     }
     HoyaClusterProtocol appMaster = connect(app);
     Messages.StopClusterRequestProto r =
       Messages.StopClusterRequestProto.newBuilder().setMessage(text).build();
     appMaster.stopCluster(r);
     log.debug("Cluster stop command issued");
     if (waittime > 0) {
       monitorAppToState(app.getApplicationId(),
                         YarnApplicationState.FINISHED,
                         new Duration(waittime * 1000));
     }
     return EXIT_SUCCESS;
   }
 
   /**
    * get the cluster configuration
    * @param clustername cluster name
    * @return the cluster name
    */
   @SuppressWarnings("UseOfSystemOutOrSystemErr")
   public int actionGetConf(String clustername,
                            String format,
                            File outputfile) throws
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
       String description = "Hoya cluster " + clustername;
       if (format.equals(ClientArgs.FORMAT_XML)) {
         Configuration siteConf = new Configuration(false);
         for (String key : status.clientProperties.keySet()) {
           siteConf.set(key, status.clientProperties.get(key), description);
         }
         siteConf.writeXml(writer);
       } else if (format.equals(ClientArgs.FORMAT_PROPERTIES)) {
         Properties props = new Properties();
         props.putAll(status.clientProperties);
         props.store(writer, description);
       } else {
         throw new BadCommandArgumentsException("Unknown format: " + format);
       }
     } finally {
       //data is written.
       //close the file
       writer.close();
     }
     //then, if this is not a file write, print it
     if (toPrint) {
       //not logged
       System.out.println(writer.toString());
     }
     return EXIT_SUCCESS;
   }
 
   /**
    * Restore a cluster
    */
   public int actionThaw(String clustername) throws YarnException, IOException {
     validateClusterName(clustername);
     //see if it is actually running and bail out;
     verifyManagerSet();
     verifyNoLiveClusters(clustername);
 
     //load spec
     verifyFileSystemArgSet();
     return startCluster(clustername);
   }
 
   /**
    * Load and start a cluster specification.
    * This assumes that all validation of args and cluster state
    * have already taken place
    * @param clustername name of the cluster.
    * @return the exit code
    * @throws YarnException
    * @throws IOException
    */
   private int startCluster(String clustername) throws
                                                YarnException,
                                                IOException {
     Path clusterSpecPath = locateClusterSpecification(clustername);
 
     ClusterDescription clusterSpec =
       HoyaUtils.loadAndValidateClusterSpec(getClusterFS(), clusterSpecPath);
     Path clusterDirectory =
       HoyaUtils.buildHoyaClusterDirPath(getClusterFS(), clustername);
 
     return executeClusterStart(clusterDirectory, clusterSpec);
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
     FileSystem fs = getClusterFS();
     return HoyaUtils.locateClusterSpecification(fs, clustername);
   }
 
   /**
    * Implement flexing
    * @param clustername name of the cluster
    * @param workers number of workers
    * @param masters number of masters
    * @return EXIT_SUCCESS if the #of nodes in a live cluster changed
    */
   public int flex(String clustername,
                   Map<String, Integer> roleInstances,
                   boolean persist) throws
                                    YarnException,
                                    IOException {
     verifyManagerSet();
     validateClusterName(clustername);
     Path clusterSpecPath = locateClusterSpecification(clustername);
     FileSystem fs = getClusterFS();
     ClusterDescription clusterSpec =
       HoyaUtils.loadAndValidateClusterSpec(fs, clusterSpecPath);
 
     for (Map.Entry<String, Integer> entry : roleInstances.entrySet()) {
       String role = entry.getKey();
       int count = entry.getValue();
       if (count < 0) {
         throw new BadCommandArgumentsException(
           "Requested number of "+role +" instances is out of range");
       }
 
 
       clusterSpec.setDesiredInstanceCount(role, count);
 
       log.debug("Flexed cluster specification ( {} -> {}) : \n{}",
                 role,
                 count,
                 clusterSpec);
     }
     if (persist) {
       Path clusterDirectory =
         HoyaUtils.buildHoyaClusterDirPath(getClusterFS(), clustername);
       log.debug("Saving the cluster specification to {}", clusterSpecPath);
       //save the specification
       if (!HoyaUtils.updateClusterSpecification(getClusterFS(),
                                                 clusterDirectory,
                                                 clusterSpecPath,
                                                 clusterSpec)) {
         log.warn("Failed to save new cluster size to {}", clusterSpecPath);
       } else {
         log.debug("New cluster size: persisted");
       }
     }
     int exitCode = EXIT_FALSE;
 
     //now see if it is actually running and bail out if not
     verifyManagerSet();
     ApplicationReport instance = findInstance(getUsername(), clustername);
     if (instance != null) {
       log.info("Flexing running cluster");
       HoyaClusterProtocol appMaster = connect(instance);
       Messages.FlexClusterRequestProto request =
         Messages.FlexClusterRequestProto.newBuilder()
                 .setClusterSpec(clusterSpec.toJsonString())
                 .build();
       Messages.FlexClusterResponseProto response =
         appMaster.flexCluster(request);
       if (response.getResponse()) {
         log.info("Cluster size updated");
         exitCode = EXIT_SUCCESS;
       } else {
         log.info(
           "Requested cluster size is the same as current size: no change");
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
     HoyaClusterProtocol appMaster = bondToCluster(clustername);
     Messages.GetJSONClusterStatusRequestProto req =
       Messages.GetJSONClusterStatusRequestProto.newBuilder().build();
     Messages.GetJSONClusterStatusResponseProto resp =
       appMaster.getJSONClusterStatus(req);
     String statusJson = resp.getClusterSpec();
     try {
       return ClusterDescription.fromJson(statusJson);
     } catch (JsonParseException e) {
       log.error(
         "Exception " + e + " parsing:\n" + statusJson,
         e);
       throw e;
     }
   }
 
   /**
    * Connect to the cluster and get its current state
    * @return its description
    */
   @VisibleForTesting
   public ClusterDescription getClusterStatus() throws
                                                YarnException,
                                                IOException {
     return getClusterStatus(getDeployedClusterName());
   }
 
   /**
    * List all node UUIDs in a role
    * @param role role name or "" for all 
    * @return an array of UUID strings
    * @throws IOException
    * @throws YarnException
    */
   @VisibleForTesting
   public String[] listNodeUUIDsByRole(String role) throws
                                                IOException,
                                                YarnException {
     HoyaClusterProtocol appMaster = bondToCluster(getDeployedClusterName());
     Collection<String> uuidList = innerListNodeUUIDSByRole(appMaster, role);
     String[] uuids = new String[uuidList.size()];
     return uuidList.toArray(uuids);
   }
 
   private List<String> innerListNodeUUIDSByRole(HoyaClusterProtocol appMaster,
                                             String role) throws
                                                                            IOException,
                                                                            YarnException {
     Messages.ListNodeUUIDsByRoleRequestProto req =
       Messages.ListNodeUUIDsByRoleRequestProto
               .newBuilder()
               .setRole(role)
               .build();
     Messages.ListNodeUUIDsByRoleResponseProto resp =
       appMaster.listNodeUUIDsByRole(req);
     return resp.getUuidList();
   }
 
 
   /**
    * List all nodes in a role. This is a double round trip: once to list
    * the nodes in a role, another to get their details
    *
    * @param role
    * @return an array of ContainerNode instances
    * @throws IOException
    * @throws YarnException
    */
   @VisibleForTesting
   public List<ClusterNode> listClusterNodesInRole(String role) throws
                                                IOException,
                                                YarnException {
     HoyaClusterProtocol appMaster = bondToCluster(getDeployedClusterName());
     
     Collection<String> uuidList = innerListNodeUUIDSByRole(appMaster, role);
     Messages.GetClusterNodesRequestProto req =
       Messages.GetClusterNodesRequestProto
               .newBuilder()
               .addAllUuid(uuidList)
               .build();
     Messages.GetClusterNodesResponseProto resp = appMaster.getClusterNodes(req);
     return convertNodeWireToClusterNodes(resp.getClusterNodeList());
   }
 
   /**
    * Get the details on a list of uuids
    * @param uuids 
    * @return a possibly empty list of node details
    * @throws IOException
    * @throws YarnException
    */
   @VisibleForTesting
   public List<ClusterNode> listClusterNodes(String[] uuids) throws
                                                IOException,
                                                YarnException {
 
     if (uuids.length == 0) {
       //short cut on an empty list
       return new LinkedList<ClusterNode>();
     }
     HoyaClusterProtocol appMaster = bondToCluster(getDeployedClusterName());
     Messages.GetClusterNodesRequestProto req =
       Messages.GetClusterNodesRequestProto
               .newBuilder()
               .addAllUuid(Arrays.asList(uuids))
               .build();
     Messages.GetClusterNodesResponseProto resp = appMaster.getClusterNodes(req);
     return convertNodeWireToClusterNodes(resp.getClusterNodeList());
   }
 
   private List<ClusterNode> convertNodeJsonToClusterNodes(String[] nodes) throws
                                                                           IOException {
     List<ClusterNode> nodeList = new ArrayList<ClusterNode>(nodes.length);
     for (String node : nodes) {
       nodeList.add(ClusterNode.fromJson(node));
     }
     return nodeList;
   }
 
   private List<ClusterNode> convertNodeWireToClusterNodes(List<Messages.RoleInstanceState> nodes) throws
                                                                           IOException {
     List<ClusterNode> nodeList = new ArrayList<ClusterNode>(nodes.size());
     for (Messages.RoleInstanceState node : nodes) {
       nodeList.add(ClusterNode.fromProtobuf(node));
     }
     return nodeList;
   }
 
 
   /**
    * Get a node from the AM
    * @param uuid uuid of node
    * @return deserialized node
    * @throws IOException IO problems
    * @throws NoSuchNodeException if the node isn't found
    */
   @VisibleForTesting
   public ClusterNode getNode(String uuid) throws IOException, YarnException {
     HoyaClusterProtocol appMaster = bondToCluster(getDeployedClusterName());
     return getNode(appMaster, uuid);
   }
 
   /**
    * Get a node from the AM
    * @param appMaster AM
    * @param uuid uuid of node
    * @return deserialized node
    * @throws IOException IO problems
    * @throws NoSuchNodeException if the node isn't found
    */
   private ClusterNode getNode(HoyaClusterProtocol appMaster, String uuid)
     throws IOException, NoSuchNodeException, YarnException {
     Messages.GetNodeRequestProto req =
       Messages.GetNodeRequestProto.newBuilder().setUuid(uuid).build();
     Messages.GetNodeResponseProto node = appMaster.getNode(req);
     return ClusterNode.fromProtobuf(node.getClusterNode());
   }
 
   /**
    *   Bond to a running cluster
    *
    *
    * @param clustername cluster name
    * @return the AM RPC client
    * @throws HoyaException if the cluster is unkown
    */
   private HoyaClusterProtocol bondToCluster(String clustername) throws
                                                                   YarnException,
                                                                   IOException {
     verifyManagerSet();
     ApplicationReport instance = findInstance(getUsername(), clustername);
     if (null == instance) {
       throw unknownClusterException(clustername);
     }
     return connect(instance);
   }
 
   /**
    * Wait for an instance of a named role to be live (or past it in the lifecycle)
    *
    *
    * @param clustername cluster
    * @param role role to look for
    * @param timeout time to wait
    * @return the state. If still in CREATED, the cluster didn't come up
    * in the time period. If LIVE, all is well. If >LIVE, it has shut for a reason
    * @throws IOException IO
    * @throws HoyaException Hoya
    * @throws WaitTimeoutException if the wait timed out
    */
   @VisibleForTesting
   public int waitForRoleInstanceLive(String role, long timeout)
     throws WaitTimeoutException, IOException, YarnException {
     Duration duration = new Duration(timeout).start();
     boolean live = false;
     int state = ClusterDescription.STATE_CREATED;
     HoyaClusterProtocol appMaster = bondToCluster(getDeployedClusterName());
 
     log.info("Waiting {} millis for a live node in role {}", timeout, role);
     while (!live) {
       //see if there is a node in that role yet
       List<String> uuids = innerListNodeUUIDSByRole(appMaster, role);
       String[] containers = uuids.toArray(new String[uuids.size()]);
       int roleCount = containers.length;
       ClusterNode roleInstance = null;
       if (roleCount != 0) {
 
         //if there is, get the node
         roleInstance = getNode(appMaster, containers[0]);
         if (roleInstance != null) {
           state = roleInstance.state;
           live = state >= ClusterDescription.STATE_LIVE;
         }
       }
       if (!live) {
         if (duration.getLimitExceeded()) {
           throw new WaitTimeoutException(
             String.format("Timeout after %d millis" +
                           " waiting for a live instance of type %s; " +
                           "instances found %d %s",
                           timeout, role, roleCount,
                           (roleInstance != null
                            ? (" instance -\n" + roleInstance.toString())
                            : "")
                          ));
         } else {
           try {
             Thread.sleep(1000);
           } catch (InterruptedException ignored) {
             //ignored
           }
         }
       }
     }
     return state;
   }
 
 
   public HoyaException unknownClusterException(String clustername) {
     return new HoyaException(EXIT_UNKNOWN_HOYA_CLUSTER,
                              "Hoya cluster not found: '" + clustername + "' ");
   }
 
 /*
 
   public List<ApplicationReport> getApplicationList() throws YarnException,
                                                           IOException {
     return getApplications(null);
   }
 */
 
   @Override
   public String toString() {
     return "HoyaClient in state " + getServiceState();
   }
 
   /**
    * Implementation of set-ness, groovy definition of true/false for a string
    * @param s
    * @return
    */
   private static boolean isUnset(String s) {
     return s == null || s.isEmpty();
   }
 
   private static boolean isSet(String s) {
     return !isUnset(s);
   }
 }
