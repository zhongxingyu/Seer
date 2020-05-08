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
 
 package org.apache.hoya.providers.agent;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.yarn.api.records.LocalResource;
 import org.apache.hadoop.yarn.api.records.Resource;
 import org.apache.hoya.HoyaKeys;
 import org.apache.hoya.HoyaXmlConfKeys;
 import org.apache.hoya.api.ClusterDescription;
 import org.apache.hoya.exceptions.BadCommandArgumentsException;
 import org.apache.hoya.exceptions.BadConfigException;
 import org.apache.hoya.exceptions.HoyaException;
 import org.apache.hoya.providers.AbstractProviderCore;
 import org.apache.hoya.providers.ClientProvider;
 import org.apache.hoya.providers.ProviderRole;
 import org.apache.hoya.providers.ProviderUtils;
 import org.apache.hoya.providers.hbase.HBaseKeys;
 import org.apache.hoya.tools.ConfigHelper;
 import org.apache.hoya.tools.HoyaFileSystem;
 import org.apache.hoya.tools.HoyaUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * This class implements  the client-side aspects
  * of the agent deployer
  */
 public class AgentClientProvider extends AbstractProviderCore implements
                                                               AgentKeys, HoyaKeys,
                                                           ClientProvider{
 
 
 
   
   protected static final Logger log =
     LoggerFactory.getLogger(AgentClientProvider.class);
   protected static final String NAME = "agent";
   
   private static final ProviderUtils providerUtils = new ProviderUtils(log);
 
 
   protected AgentClientProvider(Configuration conf) {
     super(conf);
   }
 
   @Override
   public String getName() {
     return NAME;
   }
 
 
 
   @Override
   public Configuration create(Configuration conf) {
     return conf;
   }
 
   @Override
   public List<ProviderRole> getRoles() {
     return AgentRoles.getRoles();
   }
 
 
   /**
    * Get a map of all the default options for the cluster; values
    * that can be overridden by user defaults after
    * @return a possibly empty map of default cluster options.
    */
   @Override
   public Configuration getDefaultClusterConfiguration() throws
                                                         FileNotFoundException {
     return ConfigHelper.loadMandatoryResource(
       "org/apache/hoya/providers/agent/agent.xml");
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
                                                                        HoyaException, IOException {
     Map<String, String> rolemap = new HashMap<String, String>();
       // node settings
       Configuration conf = ConfigHelper.loadMandatoryResource(
         "org/apache/hoya/providers/agent/role-node.xml");
       HoyaUtils.mergeEntries(rolemap, conf);
     return rolemap;
   }
 
   /**
    * Build time review and update of the cluster specification
    * @param clusterSpec spec
    */
   @Override // Client
   public void reviewAndUpdateClusterSpec(ClusterDescription clusterSpec) throws
                                                                          HoyaException{
 
     validateClusterSpec(clusterSpec);
   }
 
   // URL to talk back to Agent Controller
   String CONTROLLER_URL = "controller.url";
   // path to package
   String PACKAGE_PATH = "package.root";
   // path to bin directory where script of starting/stopping is located
   String BIN_DIR = "bin.directory";
   // path to agent
   String AGENT_PATH = "app.home";
   
   @Override //Client
   public void preflightValidateClusterConfiguration(HoyaFileSystem hoyaFileSystem,
                                                     String clustername,
                                                     Configuration configuration,
                                                     ClusterDescription clusterSpec,
                                                     Path clusterDirPath,
                                                     Path generatedConfDirPath,
                                                     boolean secure) throws
                                                                     HoyaException,
                                                                     IOException {
     validateClusterSpec(clusterSpec);
     Path templatePath = new Path(generatedConfDirPath, AgentKeys.CONF_FILE);
     //load the HBase site file or fail
     Configuration siteConf = ConfigHelper.loadConfiguration(hoyaFileSystem.getFileSystem(),
                                                             templatePath);

     //core customizations
     HoyaUtils.verifyOptionSet(siteConf, CONTROLLER_URL, false);
     HoyaUtils.verifyOptionSet(siteConf, PACKAGE_PATH, false);
     HoyaUtils.verifyOptionSet(siteConf, BIN_DIR, false);
     HoyaUtils.verifyOptionSet(siteConf, AGENT_PATH, false);

   }
   
   /**
    * Validate the cluster specification. This can be invoked on both
    * server and client
    * @param clusterSpec
    */
   @Override // Client and Server
   public void validateClusterSpec(ClusterDescription clusterSpec) throws
                                                                   HoyaException {
     super.validateClusterSpec(clusterSpec);
     providerUtils.validateNodeCount(AgentKeys.ROLE_NODE,
                                     clusterSpec.getDesiredInstanceCount(
                                       AgentKeys.ROLE_NODE,
                                       1), 1, -1);
 
 
   }
 
   @Override
   public Map<String, LocalResource> prepareAMAndConfigForLaunch(HoyaFileSystem hoyaFileSystem,
                                                                 Configuration serviceConf,
                                                                 ClusterDescription clusterSpec,
                                                                 Path originConfDirPath,
                                                                 Path generatedConfDirPath,
                                                                 Configuration clientConfExtras,
                                                                 String libdir,
                                                                 Path tempPath) throws
                                                                                IOException,
                                                                                HoyaException {
     //load in the template site config
     log.debug("Loading template configuration from {}", originConfDirPath);
 
 
     Map<String, LocalResource> providerResources;
     providerResources = hoyaFileSystem.submitDirectory(generatedConfDirPath,
                                                        HoyaKeys.PROPAGATED_CONF_DIR_NAME);
 
     return providerResources;
   }
 
   /**
    * Update the AM resource with any local needs
    * @param capability capability to update
    */
   @Override
   public void prepareAMResourceRequirements(ClusterDescription clusterSpec,
                                             Resource capability) {
 
   }
 
 
   /**
    * Any operations to the service data before launching the AM
    * @param clusterSpec cspec
    * @param serviceData map of service data
    */
   @Override  //Client
   public void prepareAMServiceData(ClusterDescription clusterSpec,
                                    Map<String, ByteBuffer> serviceData) {
     
   }
 
 
 
 }
