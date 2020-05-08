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
 
 import org.apache.hadoop.security.UserGroupInformation;
 import org.apache.hadoop.yarn.api.ApplicationConstants;
 import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
 import org.apache.hadoop.yarn.api.records.LocalResource;
 import org.apache.hoya.HoyaKeys;
 import org.apache.hoya.api.ClusterDescription;
 import org.apache.hoya.api.RoleKeys;
 import org.apache.hoya.exceptions.BadCommandArgumentsException;
 import org.apache.hoya.exceptions.HoyaException;
 import org.apache.hoya.exceptions.HoyaInternalStateException;
 import org.apache.hoya.providers.AbstractProviderService;
 import org.apache.hoya.providers.ProviderCore;
 import org.apache.hoya.providers.ProviderRole;
 import org.apache.hoya.providers.ProviderUtils;
 import org.apache.hoya.servicemonitor.HttpProbe;
 import org.apache.hoya.servicemonitor.MonitorKeys;
 import org.apache.hoya.servicemonitor.Probe;
 import org.apache.hoya.tools.HoyaFileSystem;
 import org.apache.hoya.tools.HoyaUtils;
 import org.apache.hoya.yarn.service.EventCallback;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This class implements the server-side aspects
  * of an agent deployment
  */
 public class AgentProviderService extends AbstractProviderService implements
                                                                   ProviderCore,
                                                                   AgentKeys,
                                                                   HoyaKeys {
 
 
   protected static final Logger log =
     LoggerFactory.getLogger(AgentProviderService.class);
  protected static final String NAME = "hbase";
   private static final ProviderUtils providerUtils = new ProviderUtils(log);
   private AgentClientProvider clientProvider;
 
   public AgentProviderService() {
     super("AgentProviderService");
   }
 
   @Override
   public List<ProviderRole> getRoles() {
     return AgentRoles.getRoles();
   }
 
   @Override
   protected void serviceInit(Configuration conf) throws Exception {
     super.serviceInit(conf);
     clientProvider = new AgentClientProvider(conf);
   }
   
   @Override
   public int getDefaultMasterInfoPort() {
     return 0;
   }
 
 
   @Override
   public Configuration loadProviderConfigurationInformation(File confDir) throws
                                                                           BadCommandArgumentsException,
                                                                           IOException {
     return new Configuration(false);
   }
 
 
   @Override 
   public void validateClusterSpec(ClusterDescription clusterSpec) throws
                                                                   HoyaException {
     clientProvider.validateClusterSpec(clusterSpec);
   }
 
   
   @Override  // server
   public void buildContainerLaunchContext(ContainerLaunchContext ctx,
                                           HoyaFileSystem hoyaFileSystem,
                                           Path generatedConfPath,
                                           String role,
                                           ClusterDescription clusterSpec,
                                           Map<String, String> roleOptions
                                          ) throws
                                            IOException,
                                            HoyaException {
     // Set the environment
     Map<String, String> env = HoyaUtils.buildEnvMap(roleOptions);
 
     env.put("PROPAGATED_CONFDIR", ApplicationConstants.Environment.PWD.$()+"/"+
                                   HoyaKeys.PROPAGATED_CONF_DIR_NAME);
     ctx.setEnvironment(env);
 
     //local resources
     Map<String, LocalResource> localResources =
       new HashMap<String, LocalResource>();
 
     //add the configuration resources
     Map<String, LocalResource> confResources;
     confResources = hoyaFileSystem.submitDirectory(
             generatedConfPath,
             HoyaKeys.PROPAGATED_CONF_DIR_NAME);
     localResources.putAll(confResources);
     //Add binaries
     //now add the image if it was set
     if (clusterSpec.isImagePathSet()) {
       Path imagePath = new Path(clusterSpec.getImagePath());
       log.info("using image path {}", imagePath);
       hoyaFileSystem.maybeAddImagePath(localResources, imagePath);
     }
     ctx.setLocalResources(localResources);
     List<String> commands = new ArrayList<String>();
 
     List<String> command = new ArrayList<String>();
 
     //this must stay relative if it is an image
     command.add("bin/ambari");
     //config dir is relative to the generated file
     command.add(ARG_CONFIG);
     command.add("$PROPAGATED_CONFDIR");
     
     //now look at the role
     if (ROLE_NODE.equals(role)) {
       //role is region server
       command.add(REGION_SERVER);
       command.add(ACTION_START);
       //log details
       command.add(
         "1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/agent-server.txt");
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
 
   }
 
   @Override
   public boolean initMonitoring() {
     return true;
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
     return stats;
   }
 
 }
