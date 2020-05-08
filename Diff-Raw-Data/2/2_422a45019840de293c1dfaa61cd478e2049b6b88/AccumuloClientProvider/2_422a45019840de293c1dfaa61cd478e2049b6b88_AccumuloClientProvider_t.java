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
 
 package org.apache.hadoop.hoya.providers.accumulo;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.CommonConfigurationKeys;
 import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hoya.HoyaKeys;
 import org.apache.hadoop.hoya.api.ClusterDescription;
 import org.apache.hadoop.hoya.api.OptionKeys;
 import org.apache.hadoop.hoya.api.RoleKeys;
 import org.apache.hadoop.hoya.exceptions.BadConfigException;
 import org.apache.hadoop.hoya.exceptions.HoyaException;
 import org.apache.hadoop.hoya.providers.ClientProvider;
 import org.apache.hadoop.hoya.providers.ProviderCore;
 import org.apache.hadoop.hoya.providers.ProviderRole;
 import org.apache.hadoop.hoya.providers.ProviderUtils;
 import org.apache.hadoop.hoya.tools.ConfigHelper;
 import org.apache.hadoop.hoya.tools.HoyaUtils;
 import org.apache.hadoop.yarn.api.records.LocalResource;
 import org.apache.hadoop.yarn.api.records.Resource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import static org.apache.hadoop.hoya.providers.accumulo.AccumuloConfigFileOptions.*;
 import static org.apache.hadoop.hoya.api.RoleKeys.*;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 /**
  * This class implements both the client-side and server-side aspects
  * of an HBase Cluster
  */
 public class AccumuloClientProvider extends Configured implements
                                                        ProviderCore,
                                                        AccumuloKeys,
                                                        ClientProvider {
 
   protected static final Logger log =
     LoggerFactory.getLogger(AccumuloClientProvider.class);
   private static final ProviderUtils providerUtils = new ProviderUtils(log);
 
   protected AccumuloClientProvider(Configuration conf) {
     super(conf);
   }
 
 
 
   public static List<ProviderRole> getProviderRoles() {
     return AccumuloRoles.ROLES;
 
   }
 
   @Override
   public String getName() {
     return PROVIDER_ACCUMULO;
   }
 
   @Override
   public List<ProviderRole> getRoles() {
     return AccumuloRoles.ROLES;
   }
 
   private void putSiteOpt(Map<String, String> options, String key, String val) {
     options.put(
       OptionKeys.OPTION_SITE_PREFIX + key, val);
   }
 
   /**
    * Get a map of all the default options for the cluster; values
    * that can be overridden by user defaults after
    * @return a possibly emtpy map of default cluster options.
    */
   @Override
   public Map<String, String> getDefaultClusterOptions() {
     Map<String, String> options = new HashMap<String, String>();
     //create an instance ID
     putSiteOpt(options, AccumuloConfigFileOptions.INSTANCE_SECRET,
       UUID.randomUUID().toString());
     //make up a password
     options.put(OPTION_ACCUMULO_PASSWORD, UUID.randomUUID().toString());
 
 
     putSiteOpt(options, MASTER_PORT_CLIENT , "0");
     putSiteOpt(options, MONITOR_PORT_CLIENT , "0");
     putSiteOpt(options, TRACE_PORT_CLIENT , "0");
     putSiteOpt(options, TSERV_PORT_CLIENT , "0");
 
     return options;
   }
 
   /**
    * Create the default cluster role instance for a named
    * cluster role; 
    *
    * @param rolename role name
    * @return a node that can be added to the JSON
    */
   @Override
   public Map<String, String> createDefaultClusterRole(String rolename) throws
                                                                        HoyaException {
     Map<String, String> rolemap = new HashMap<String, String>();
     rolemap.put(RoleKeys.ROLE_NAME, rolename);
 
     rolemap.put(RoleKeys.JVM_HEAP, DEFAULT_ROLE_HEAP);
     rolemap.put(RoleKeys.YARN_CORES, DEFAULT_ROLE_YARN_VCORES);
     rolemap.put(RoleKeys.YARN_MEMORY, DEFAULT_ROLE_YARN_RAM);
 
     if (rolename.equals(ROLE_MASTER)) {
       rolemap.put(RoleKeys.ROLE_INSTANCES, "1");
       rolemap.put(RoleKeys.JVM_HEAP, DEFAULT_MASTER_HEAP);
       rolemap.put(RoleKeys.YARN_CORES, DEFAULT_MASTER_YARN_VCORES);
       rolemap.put(RoleKeys.YARN_MEMORY, DEFAULT_MASTER_YARN_RAM);
 
     } else if (rolename.equals(ROLE_TABLET)) {
       rolemap.put(RoleKeys.ROLE_INSTANCES, "1");
     } else if (rolename.equals(ROLE_TRACER)) {
     } else if (rolename.equals(ROLE_GARBAGE_COLLECTOR)) {
     } else if (rolename.equals(ROLE_MONITOR)) {
     }
     return rolemap;
   }
 
   void propagateKeys(Map<String, String> sitexml,
                      Configuration conf,
                      String... keys) {
     for (String key : keys) {
       propagate(sitexml, conf, key, key);
     }
   }
 
   /**
    * Propagate a key's value from the conf to the site, ca
    * @param sitexml
    * @param conf
    * @param srckey
    * @param destkey
    */
   private void propagate(Map<String, String> sitexml,
                          Configuration conf,
                          String srckey, String destkey) {
     String val = conf.get(srckey);
     if (val != null) {
       sitexml.put(destkey, val);
     }
   }
 
   private void assignIfSet(Map<String, String> sitexml,
                            String prop,
                              ClusterDescription cd,
                            String role,
                            String key) throws BadConfigException {
     Map<String, String> map = cd.getMandatoryRole(role);
 
     String value = map.get(key);
     if (value!=null) {
       sitexml.put(prop, value);
     }
   }
 
   /**
    * Build the conf dir from the service arguments, adding the hbase root
    * to the FS root dir.
    * This the configuration used by HBase directly
    * @param clusterSpec this is the cluster specification used to define this
    * @return a map of the dynamic bindings for this Hoya instance
    */
   @Override // ProviderCore
   public Map<String, String> buildSiteConfFromSpec(ClusterDescription clusterSpec)
     throws BadConfigException {
 
 
     Map<String, String> master = clusterSpec.getMandatoryRole(
       AccumuloKeys.ROLE_MASTER);
 
     Map<String, String> tserver = clusterSpec.getMandatoryRole(
       AccumuloKeys.ROLE_TABLET);
     Map<String, String> monitor = clusterSpec.getMandatoryRole(
       AccumuloKeys.ROLE_MONITOR);
 
     Map<String, String> sitexml = new HashMap<String, String>();
 
 
     providerUtils.propagateSiteOptions(clusterSpec, sitexml);
 
     String fsDefaultName =
       getConf().get(CommonConfigurationKeys.FS_DEFAULT_NAME_KEY);
     if (fsDefaultName == null) {
       throw new BadConfigException("Key not found in conf: {}",
                                    CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
     }
     sitexml.put(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY, fsDefaultName);
     sitexml.put(HoyaKeys.FS_DEFAULT_NAME_CLASSIC, fsDefaultName);
 
     String dataPath = clusterSpec.dataPath;
     Path path = new Path(dataPath);
     URI parentUri = path.toUri();
     String authority = parentUri.getAuthority();
     String fspath =
      parentUri.getScheme() + "://" + (authority == null ? "" : authority) + "/";
     sitexml.put(AccumuloConfigFileOptions.INSTANCE_DFS_URI, fspath);
     sitexml.put(AccumuloConfigFileOptions.INSTANCE_DFS_DIR,
                 parentUri.getPath());
 
     assignIfSet(sitexml, MASTER_PORT_CLIENT, clusterSpec, ROLE_MASTER,
                 APP_INFOPORT);
     assignIfSet(sitexml, MONITOR_PORT_CLIENT, clusterSpec, ROLE_MONITOR,
                 APP_INFOPORT);
     assignIfSet(sitexml, TSERV_PORT_CLIENT, clusterSpec, ROLE_TABLET,
                 APP_INFOPORT);
     assignIfSet(sitexml, MASTER_PORT_CLIENT, clusterSpec, ROLE_MASTER,
                 APP_INFOPORT);
 
     //fix up ZK
     int zkPort = clusterSpec.zkPort;
     String zkHosts = clusterSpec.zkHosts;
 
     //parse the hosts
     String[] hostlist = zkHosts.split(",", 0);
     String quorum = HoyaUtils.join(hostlist, ":" + zkPort + ",");
     //this quorum has a trailing comma
     quorum = quorum.substring(0, quorum.length() - 1);
     sitexml.put(AccumuloConfigFileOptions.ZOOKEEPER_HOST, quorum);
 
     return sitexml;
   }
 
 
   /**
    * Build time review and update of the cluster specification
    * @param clusterSpec spec
    */
   @Override // Client
   public void reviewAndUpdateClusterSpec(ClusterDescription clusterSpec) throws
                                                                          HoyaException {
 
     validateClusterSpec(clusterSpec);
   }
 
   @Override //Client
   public void preflightValidateClusterConfiguration(ClusterDescription clusterSpec,
                                                     FileSystem clusterFS,
                                                     Path generatedConfDirPath,
                                                     boolean secure) throws
                                                                     HoyaException,
                                                                     IOException {
     validateClusterSpec(clusterSpec);
   }
 
   /**
    * This builds up the site configuration for the AM and downstream services;
    * the path is added to the cluster spec so that launchers in the 
    * AM can pick it up themselves. 
    *
    *
    * @param clusterFS filesystem
    * @param serviceConf conf used by the service
    * @param clusterSpec cluster specification
    * @param originConfDirPath the original config dir -treat as read only
    * @param generatedConfDirPath path to place generated artifacts
    * @param clientConfExtras
    * @return a map of name to local resource to add to the AM launcher
    */
   @Override //client
   public Map<String, LocalResource> prepareAMAndConfigForLaunch(FileSystem clusterFS,
                                                                 Configuration serviceConf,
                                                                 ClusterDescription clusterSpec,
                                                                 Path originConfDirPath,
                                                                 Path generatedConfDirPath,
                                                                 Configuration clientConfExtras) throws
                                                                                            IOException,
                                                                                            BadConfigException {
     Configuration siteConf = ConfigHelper.loadTemplateConfiguration(
       serviceConf,
       originConfDirPath,
       AccumuloKeys.SITE_XML,
       AccumuloKeys.SITE_XML_RESOURCE);
 
     //construct the cluster configuration values
     Map<String, String> clusterConfMap = buildSiteConfFromSpec(clusterSpec);
     //merge them
     ConfigHelper.addConfigMap(siteConf, clusterConfMap, "Accumulo Provider");
 
     if (log.isDebugEnabled()) {
       ConfigHelper.dumpConf(siteConf);
     }
 
     Path sitePath = ConfigHelper.saveConfig(serviceConf,
                                             siteConf,
                                             generatedConfDirPath,
                                             AccumuloKeys.SITE_XML);
 
     log.debug("Saving the config to {}", sitePath);
     Map<String, LocalResource> confResources;
     confResources = HoyaUtils.submitDirectory(clusterFS,
                                               generatedConfDirPath,
                                               HoyaKeys.PROPAGATED_CONF_DIR_NAME);
     return confResources;
   }
 
   /**
    * Update the AM resource with any local needs
    * @param capability capability to update
    */
   @Override //client
   public void prepareAMResourceRequirements(ClusterDescription clusterSpec,
                                             Resource capability) {
     //no-op unless you want to add more memory
     capability.setMemory(clusterSpec.getRoleOptInt(ROLE_MASTER,
                                                    RoleKeys.YARN_MEMORY,
                                                    capability.getMemory()));
     capability.setVirtualCores(1);
   }
 
 
   /**
    * Any operations to the service data before launching the AM
    * @param clusterSpec cspec
    * @param serviceData map of service data
    */
   @Override //client
   public void prepareAMServiceData(ClusterDescription clusterSpec,
                                    Map<String, ByteBuffer> serviceData) {
 
   }
 
 
   /**
    * Validate the cluster specification. This can be invoked on both
    * server and client
    * @param clusterSpec
    */
   @Override // Client and Server
   public void validateClusterSpec(ClusterDescription clusterSpec) throws
                                                                   HoyaException {
     providerUtils.validateNodeCount(AccumuloKeys.ROLE_TABLET,
                                     clusterSpec.getDesiredInstanceCount(
                                       AccumuloKeys.ROLE_TABLET,
                                       1), 1, -1);
 
 
     providerUtils.validateNodeCount(AccumuloKeys.ROLE_MASTER,
                                     clusterSpec.getDesiredInstanceCount(
                                       AccumuloKeys.ROLE_MASTER,
                                       1), 1, 1);
 
     providerUtils.validateNodeCount(AccumuloKeys.ROLE_GARBAGE_COLLECTOR,
                                     clusterSpec.getDesiredInstanceCount(
                                       AccumuloKeys.ROLE_GARBAGE_COLLECTOR,
                                       0), 0, 1);
 
     providerUtils.validateNodeCount(AccumuloKeys.ROLE_MONITOR,
                                     clusterSpec.getDesiredInstanceCount(
                                       AccumuloKeys.ROLE_MONITOR,
                                       0), 0, 1);
 
     providerUtils.validateNodeCount(AccumuloKeys.ROLE_TRACER,
                                     clusterSpec.getDesiredInstanceCount(
                                       AccumuloKeys.ROLE_TRACER,
                                       0), 0, 1);
 
     clusterSpec.verifyOptionSet(AccumuloKeys.OPTION_ZK_HOME);
     clusterSpec.verifyOptionSet(AccumuloKeys.OPTION_HADOOP_HOME);
   }
 
 
   /**
    * Get the path to the script
    * @return the script
    */
   public static File buildScriptBinPath(ClusterDescription cd) {
     String startScript = AccumuloKeys.START_SCRIPT;
     return new File(buildImageDir(cd), startScript);
   }
 
 
   /**
    * Build the image dir. This path is relative and only valid at the far end
    * @param cd cluster spec
    * @return a relative path to accumulp home
    */
   public static File buildImageDir(ClusterDescription cd) {
     return providerUtils.buildImageDir(cd, AccumuloKeys.ARCHIVE_SUBDIR);
   }
 
 }
