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
 
 package org.apache.hadoop.hoya.providers.hbase;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.Abortable;
 import org.apache.hadoop.hbase.ClusterStatus;
 import org.apache.hadoop.hbase.HConstants;
 import org.apache.hadoop.hbase.ServerName;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.hadoop.hbase.client.HConnection;
 import org.apache.hadoop.hbase.client.HConnectionManager;
 import org.apache.hadoop.hbase.zookeeper.MasterAddressTracker;
 import org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher;
 import org.apache.hadoop.hoya.HostAndPort;
 import org.apache.hadoop.hoya.HoyaKeys;
 import org.apache.hadoop.hoya.api.ClusterDescription;
 import org.apache.hadoop.hoya.api.RoleKeys;
 import org.apache.hadoop.hoya.api.StatusKeys;
 import org.apache.hadoop.hoya.exceptions.BadCommandArgumentsException;
 import org.apache.hadoop.hoya.exceptions.HoyaException;
 import org.apache.hadoop.hoya.exceptions.HoyaInternalStateException;
 import org.apache.hadoop.hoya.providers.AbstractProviderService;
 import org.apache.hadoop.hoya.providers.ClientProvider;
 import org.apache.hadoop.hoya.providers.ProviderCore;
 import org.apache.hadoop.hoya.providers.ProviderRole;
 import org.apache.hadoop.hoya.providers.ProviderUtils;
 import org.apache.hadoop.hoya.servicemonitor.HttpProbe;
 import org.apache.hadoop.hoya.servicemonitor.MonitorKeys;
 import org.apache.hadoop.hoya.servicemonitor.Probe;
 import org.apache.hadoop.hoya.tools.ConfigHelper;
 import org.apache.hadoop.hoya.tools.HoyaUtils;
 import org.apache.hadoop.hoya.yarn.service.EventCallback;
 import org.apache.hadoop.security.UserGroupInformation;
 import org.apache.hadoop.yarn.api.ApplicationConstants;
 import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
 import org.apache.hadoop.yarn.api.records.LocalResource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This class implements the server-side aspects
  * of an HBase Cluster
  */
 public class HBaseProviderService extends AbstractProviderService implements
                                                                   ProviderCore,
                                                                   HBaseKeys,
                                                                   HoyaKeys {
 
   private MasterAddressTracker masterTracker = null;
 
   public static final String ERROR_UNKNOWN_ROLE = "Unknown role ";
   protected static final Logger log =
     LoggerFactory.getLogger(HBaseProviderService.class);
   protected static final String NAME = "hbase";
   private static final ProviderUtils providerUtils = new ProviderUtils(log);
   private HBaseClientProvider clientProvider;
 
   public HBaseProviderService() {
     super("HBaseProviderService");
   }
 
   @Override
   public List<ProviderRole> getRoles() {
     return HBaseRoles.getRoles();
   }
 
   @Override
   protected void serviceInit(Configuration conf) throws Exception {
     super.serviceInit(conf);
     clientProvider = new HBaseClientProvider(conf);
   }
 
   @Override
   public ClientProvider getClientProvider() {
     return clientProvider;
   }
   
   @Override
   public int getDefaultMasterInfoPort() {
     return HBaseConfigFileOptions.DEFAULT_MASTER_INFO_PORT;
   }
 
   @Override
   public String getSiteXMLFilename() {
     return SITE_XML;
   }
 
 
   /**
    * Validate the cluster specification. This can be invoked on both
    * server and client
    * @param clusterSpec
    */
   @Override // Client and Server
   public void validateClusterSpec(ClusterDescription clusterSpec) throws
                                                                   HoyaException {
     clientProvider.validateClusterSpec(clusterSpec);
   }
 
 
   /**
    * Get the path to hbase home
    * @return the hbase home path
    */
   public String buildHBaseScriptBinPath(ClusterDescription cd) throws
                                                                FileNotFoundException {
     return providerUtils.buildPathToScript(cd, "bin",
                                            HBaseKeys.HBASE_SCRIPT);
   }
   
   @Override  // server
   public void buildContainerLaunchContext(ContainerLaunchContext ctx,
                                           FileSystem fs,
                                           Path generatedConfPath,
                                           String role,
                                           ClusterDescription clusterSpec,
                                           Map<String, String> roleOptions
                                          ) throws
                                            IOException,
                                            HoyaException {
     // Set the environment
     Map<String, String> env = HoyaUtils.buildEnvMap(roleOptions);
 
     env.put(HBASE_LOG_DIR, providerUtils.getLogdir());
 
     env.put("PROPAGATED_CONFDIR", ApplicationConstants.Environment.PWD.$()+"/"+
                                   HoyaKeys.PROPAGATED_CONF_DIR_NAME);
     ctx.setEnvironment(env);
 
     //local resources
     Map<String, LocalResource> localResources =
       new HashMap<String, LocalResource>();
 
     //add the configuration resources
     Map<String, LocalResource> confResources;
     confResources = HoyaUtils.submitDirectory(fs,
                                               generatedConfPath,
                                               HoyaKeys.PROPAGATED_CONF_DIR_NAME);
     localResources.putAll(confResources);
     //Add binaries
     //now add the image if it was set
     if (clusterSpec.isImagePathSet()) {
       Path imagePath = new Path(clusterSpec.getImagePath());
       log.info("using image path {}", imagePath);
       HoyaUtils.maybeAddImagePath(fs, localResources, imagePath);
     }
     ctx.setLocalResources(localResources);
     List<String> commands = new ArrayList<String>();
 
     List<String> command = new ArrayList<String>();
 
     String heap = clusterSpec.getRoleOpt(role, RoleKeys.JVM_HEAP, DEFAULT_JVM_HEAP);
     if (HoyaUtils.isSet(heap)) {
      String adjustedHeap = HoyaUtils.translateTrailingHeapUnit(heap);
      command.add("HBASE_HEAPSIZE=" + adjustedHeap);
      env.put("HBASE_HEAPSIZE", adjustedHeap);
     }
     
     //this must stay relative if it is an image
     command.add(buildHBaseScriptBinPath(clusterSpec));
     //config dir is relative to the generated file
     command.add(ARG_CONFIG);
     command.add("$PROPAGATED_CONFDIR");
     
     //now look at the role
     if (ROLE_WORKER.equals(role)) {
       //role is region server
       command.add(REGION_SERVER);
       command.add(ACTION_START);
       //log details
       command.add(
         "1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/region-server.txt");
       command.add("2>&1");
     } else if (ROLE_MASTER.equals(role)) {
       command.add(MASTER);
       command.add(ACTION_START);
       //log details
       command.add(
         "1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/master.txt");
       command.add("2>&1");
     } else {
       throw new HoyaInternalStateException("Cannot start role %s", role);
     }
 /*    command.add("-D httpfs.log.dir = "+
                 ApplicationConstants.LOG_DIR_EXPANSION_VAR);*/
 
     String cmdStr = HoyaUtils.join(command, " ");
 
 
     commands.add(cmdStr);
     ctx.setCommands(commands);
 
   }
 
   /**
    * Run this service
    *
    *
    * @param cd component description
    * @param confDir local dir with the config
    * @param env environment variables above those generated by
    * @param execInProgress callback for the event notification
    * @throws IOException IO problems
    * @throws HoyaException anything internal
    */
   @Override
   public boolean exec(ClusterDescription cd,
                       File confDir,
                       Map<String, String> env,
                       EventCallback execInProgress) throws
                                                  IOException,
                                                  HoyaException {
 
     return false;
   }
 
 
   /**
    * This is a validation of the application configuration on the AM.
    * Here is where things like the existence of keytabs and other
    * not-seen-client-side properties can be tested, before
    * the actual process is spawned. 
    * @param clusterSpec clusterSpecification
    * @param confDir configuration directory
    * @param secure flag to indicate that secure mode checks must exist
    * @throws IOException IO problemsn
    * @throws HoyaException any failure
    */
   @Override
   public void validateApplicationConfiguration(ClusterDescription clusterSpec,
                                                File confDir,
                                                boolean secure
                                               ) throws IOException, HoyaException {
     String siteXMLFilename = getSiteXMLFilename();
     File siteXML = new File(confDir, siteXMLFilename);
     if (!siteXML.exists()) {
       throw new BadCommandArgumentsException(
         "Configuration directory %s doesn't contain %s - listing is %s",
         confDir, siteXMLFilename, HoyaUtils.listDir(confDir));
     }
 
     //now read it in
     Configuration siteConf = ConfigHelper.loadConfFromFile(siteXML);
     //look in the site spec to see that it is OK
     clientProvider.validateHBaseSiteXML(siteConf, secure, siteXMLFilename);
     
     if (secure) {
       //secure mode: take a look at the keytab of master and RS
       HoyaUtils.verifyKeytabExists(siteConf,
                                    HBaseConfigFileOptions.KEY_MASTER_KERBEROS_KEYTAB);
       HoyaUtils.verifyKeytabExists(siteConf,
                                    HBaseConfigFileOptions.KEY_REGIONSERVER_KERBEROS_KEYTAB);
 
     }
   }
 
   @Override
   public boolean initMonitoring() {
     startZKWatcher();
     return true;
   }
 
   private void startZKWatcher() {
     try {
       Abortable abortable = new ProviderAbortable();
       ZooKeeperWatcher zkw =
         new ZooKeeperWatcher(getConf(), "HBaseClient", abortable);
       masterTracker = new MasterAddressTracker(zkw, abortable);
     } catch (IOException ioe) {
       log.error("Couldn't instantiate ZooKeeperWatcher", ioe);
     }
   }
 
 
   @Override
   public List<Probe> createProbes(ClusterDescription clusterSpec, String urlStr,
                                   Configuration config,
                                   int timeout)
     throws IOException {
     List<Probe> probes = new ArrayList<Probe>();
     if (urlStr != null) {
       // set up HTTP probe if a path is provided
       String prefix = "";
       URL url = null;
       if (!urlStr.startsWith("http") && urlStr.contains("/proxy/")) {
         if (!UserGroupInformation.isSecurityEnabled()) {
           prefix = "http://proxy/relay/";
         } else {
           prefix = "https://proxy/relay/";
         }
       }
       try {
         url = new URL(prefix + urlStr);
       } catch (MalformedURLException mue) {
         log.error("tracking url: " + prefix + urlStr + " is malformed");
       }
       if (url != null) {
         log.info("tracking url: " + url);
         HttpURLConnection connection = null;
         try {
           connection = HttpProbe.getConnection(url, timeout);
           // see if the host is reachable
           connection.getResponseCode();
 
           HttpProbe probe = new HttpProbe(url, timeout,
                                           MonitorKeys.WEB_PROBE_DEFAULT_CODE,
                                           MonitorKeys.WEB_PROBE_DEFAULT_CODE,
                                           config);
           probes.add(probe);
         } catch (UnknownHostException uhe) {
           log.error("host unknown: " + url);
         } finally {
           if (connection != null) {
             connection.disconnect();
             connection = null;
           }
         }
       }
     }
     return probes;
   }
 
   /**
    * Build the provider status, can be empty
    * @return the provider status - map of entries to add to the info section
    */
   public Map<String, String> buildProviderStatus() {
     Map<String, String> stats = new HashMap<String, String>();
     if (masterTracker != null) {
       ServerName sn = masterTracker.getMasterAddress();
       log.debug("getMasterAddress " + sn + ", quorum="
                 + getConf().get(HConstants.ZOOKEEPER_QUORUM));
       if (sn == null) {
         return null;
       }
       HostAndPort hostAndPort = new HostAndPort(sn.getHostname(), sn.getPort());
       stats.put(StatusKeys.INFO_MASTER_ADDRESS, hostAndPort.toString());
     }
     return stats;
   }
 
   public Collection<HostAndPort> listDeadServers(Configuration conf) throws
                                                                      IOException {
     HConnection hbaseConnection = HConnectionManager.createConnection(conf);
     HBaseAdmin hBaseAdmin = new HBaseAdmin(hbaseConnection);
     try {
       ClusterStatus cs = hBaseAdmin.getClusterStatus();
       return serverNameToHostAndPort(cs.getDeadServerNames());
     } finally {
       hBaseAdmin.close();
       hbaseConnection.close();
     }
   }
 
 
   private Collection<HostAndPort> serverNameToHostAndPort(Collection<ServerName> servers) {
     Collection<HostAndPort> col = new ArrayList<HostAndPort>();
     if (servers == null || servers.isEmpty()) return col;
     for (ServerName sn : servers) {
       col.add(new HostAndPort(sn.getHostname(), sn.getPort()));
     }
     return col;
   }
 }
