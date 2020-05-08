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
 
 package org.apache.hoya.yarn.params;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.ParametersDelegate;
 import org.apache.hadoop.fs.Path;
 import org.apache.hoya.exceptions.BadCommandArgumentsException;
 import org.apache.hoya.providers.HoyaProviderFactory;
 import org.apache.hoya.providers.hbase.HBaseConfigFileOptions;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Abstract Action to build things; shares args across build and
  * list
  */
 public class AbstractClusterBuildingActionArgs extends AbstractActionArgs {
 
   /**
    *    Declare the image configuration directory to use when creating or reconfiguring a hoya cluster. The path must be on a filesystem visible to all nodes in the YARN cluster.
    Only one configuration directory can be specified.
    */
   @Parameter(names = ARG_CONFDIR,
              description = "Path to cluster configuration directory in HDFS",
              converter = PathArgumentConverter.class)
   public Path confdir;
 
 
   @Parameter(names = ARG_APP_ZKPATH,
              description = "Zookeeper path for the application")
   public String appZKPath;
 
   @Parameter(names = ARG_ZKHOSTS,
              description = "comma separated list of the Zookeeper hosts")
   public String zkhosts;
 
   @Parameter(names = ARG_ZKPORT,
              description = "Zookeeper port")
   public int zkport = HBaseConfigFileOptions.HBASE_ZK_PORT;
 
 
   /**
    * --image path
    the full path to a .tar or .tar.gz path containing an HBase image.
    */
   @Parameter(names = ARG_IMAGE,
              description = "The full path to a .tar or .tar.gz path containing the application",
              converter = PathArgumentConverter.class)
   public Path image;
 
   @Parameter(names = ARG_APP_HOME,
              description = "Home directory of a pre-installed application")
   public String appHomeDir;
 
   @Parameter(names = ARG_PROVIDER,
              description = "Provider of the specific cluster application")
   public String provider = HoyaProviderFactory.DEFAULT_CLUSTER_TYPE;
 
   /**
   * This is a listing of the roles to create
    */
   @Parameter(names = {ARG_OPTION, ARG_OPTION_SHORT}, arity = 2,
              description = "option <name> <value>")
   public List<String> optionTuples = new ArrayList<String>(0);
 
 
   @ParametersDelegate
   public RoleArgsDelegate roleDelegate = new RoleArgsDelegate();
 
   /**
    * All the role option triples
    */
   @Parameter(names = {ARG_ROLEOPT}, arity = 3,
              description = "Role option " + ARG_ROLEOPT +
                            " <role> <name> <option>")
   public List<String> roleOptTriples = new ArrayList<String>(0);
 
 
   public Map<String, String> getOptionsMap() throws
                                              BadCommandArgumentsException {
     return convertTupleListToMap("options", optionTuples);
   }
 
 
   /**
    * Get the role heap mapping (may be empty, but never null)
    * @return role heap mapping
    * @throws BadCommandArgumentsException parse problem
    */
   public Map<String, Map<String, String>> getRoleOptionMap() throws
                                                              BadCommandArgumentsException {
     return convertTripleListToMaps(ARG_ROLEOPT, roleOptTriples);
   }
 
   public List<String> getRoleTuples() {
     return roleDelegate.getRoleTuples();
   }
 
   /**
    * Get the role mapping (may be empty, but never null)
    * @return role mapping
    * @throws BadCommandArgumentsException parse problem
    */
   public Map<String, String> getRoleMap() throws BadCommandArgumentsException {
     return roleDelegate.getRoleMap();
   }
 
   public Path getConfdir() {
     return confdir;
   }
 
   public String getAppZKPath() {
     return appZKPath;
   }
 
   public String getZKhosts() {
     return zkhosts;
   }
 
   public int getZKport() {
     return zkport;
   }
 
   public Path getImage() {
     return image;
   }
 
   public String getAppHomeDir() {
     return appHomeDir;
   }
 
   public String getProvider() {
     return provider;
   }
 }
