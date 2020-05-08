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
 
 package org.apache.hadoop.hoya.yarn;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.ParameterException;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hoya.api.RoleKeys;
 import org.apache.hadoop.hoya.exceptions.BadCommandArgumentsException;
 import org.apache.hadoop.hoya.providers.hbase.HBaseConfigFileOptions;
 import org.apache.hadoop.hoya.tools.HoyaUtils;
 import org.apache.hadoop.hoya.tools.PathArgumentConverter;
 import org.apache.hadoop.hoya.tools.URIArgumentConverter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This class contains the common argument set for all tne entry points,
  * and the core parsing logic to verify that the action is on the list
  * of allowed actions -and that the remaining number of arguments is
  * in the range allowed
  */
 
 public class CommonArgs implements HoyaActions {
 
   public static final String ARG_ACTION = "--action";
   public static final String ARG_APP_HOME = "--apphome";
   public static final String ARG_APP_ZKPATH = "--zkpath";
  public static final String ARG_CONFDIR = "--appconf";
   public static final String ARG_DEBUG = "--debug";
   public static final String ARG_FILESYSTEM = "--fs";
   public static final String ARG_FILESYSTEM_LONG = "--filesystem";
   public static final String ARG_GENERATED_CONFDIR = "--generated_confdir";
   public static final String ARG_HELP = "--help";
   public static final String ARG_IMAGE = "--image";
   public static final String ARG_MANAGER = "--manager";
   public static final String ARG_NAME = "--name";
   public static final String ARG_OUTPUT = "--output";
   public static final String ARG_OPTION = "--option";
   public static final String ARG_OPTION_SHORT = "-O";
   public static final String ARG_PROVIDER = "--provider";
   public static final String ARG_ROLE = "--role";
 
 
   public static final String ARG_ROLEOPT = "--roleopt";
   public static final String ARG_USER = "--user";
 
   public static final String ARG_ZKPORT = "--zkport";
   public static final String ARG_ZKHOSTS = "--zkhosts";
 
   /** for testing only: {@value} */
 
   @Deprecated
   public static final String ARG_X_HBASE_MASTER_COMMAND =
     "--Xhbase-master-command";
 
 
   /**
    * ERROR Strings
    */
   public static final String ERROR_NO_ACTION = "No action specified";
   public static final String ERROR_UNKNOWN_ACTION = "Unknown command: ";
   public static final String ERROR_NOT_ENOUGH_ARGUMENTS =
     "Not enough arguments for action: ";
   
   public static final String ERROR_PARSE_FAILURE =
     "Failed to parse ";
   
   /**
    * All the remaining values after argument processing
    */
   public static final String ERROR_TOO_MANY_ARGUMENTS =
     "Too many arguments for action:";
 
   public static final String ARG_RESOURCE_MANAGER = "--rm";
 
   protected static final Logger LOG = LoggerFactory.getLogger(CommonArgs.class);
   public static final String ERROR_DUPLICATE_ENTRY = "Duplicate entry for ";
 
   @Parameter
   public List<String> parameters = new ArrayList<String>();
 
   @Parameter(names = ARG_DEBUG, description = "Debug mode")
   public boolean debug = false;
 
   /**
    *    Declare the image configuration directory to use when creating or reconfiguring a hoya cluster. The path must be on a filesystem visible to all nodes in the YARN cluster.
    Only one configuration directory can be specified.
    */
   @Parameter(names = ARG_CONFDIR,
              description = "Path to cluster configuration directory in HDFS",
              converter = PathArgumentConverter.class)
   public Path confdir;
 
   @Parameter(names = {ARG_FILESYSTEM, ARG_FILESYSTEM_LONG}, description = "Filesystem URI",
              converter = URIArgumentConverter.class)
   public URI filesystemURL;
 
   @Parameter(names = ARG_APP_ZKPATH,
              description = "Zookeeper path for the application")
   public String appZKPath;
 
   @Parameter(names = ARG_HELP, help = true)
   public boolean help;
 
   //TODO: do we need this?
   @Parameter(names = ARG_RESOURCE_MANAGER,
              description = "Resource manager hostname:port ",
              required = false)
   public String rmAddress;
 
   @Parameter(names = ARG_USER,
              description = "Username if not the current user")
   public String user = System.getProperty("user.name");
 
   @Parameter(names = ARG_ZKHOSTS,
              description = "comma separated list of the Zookeeper hosts")
   public String zkhosts;
 
   @Parameter(names = ARG_ZKPORT,
              description = "Zookeeper port")
   public int zkport = HBaseConfigFileOptions.HBASE_ZK_PORT;
 
   /*
    -D name=value
 
    Define an HBase configuration option which overrides any options in
     the configuration XML files of the image or in the image configuration
      directory. The values will be persisted.
       Configuration options are only passed to the cluster when creating or reconfiguring a cluster.
 
    */
 
   @Parameter(names = "-D", arity = 1, description = "Definitions")
   public List<String> definitions = new ArrayList<String>();
   public Map<String, String> definitionMap = new HashMap<String, String>();
 
   @Parameter(names = {"--m", ARG_MANAGER},
              description = "hostname:port of the YARN resource manager")
   public String manager;
 
   @Parameter(names = {ARG_OUTPUT, "-o"},
              description = "Output file for the configuration data")
   public String output;
 
   @Parameter(names = ARG_X_HBASE_MASTER_COMMAND,
              description = "Testing only: hbase command to exec on the master")
   public String xHBaseMasterCommand = null;
 
   /**
    * fields
    */
   public JCommander commander;
   public String action;
   //action arguments; 
   public List<String> actionArgs;
   public final String[] args;
 
   /**
    * create a 3-tuple
    * @param msg
    * @param min
    * @param max
    * @return
    */
   protected static List<Object> t(String msg, int min, int max) {
     List<Object> l = new ArrayList<Object>(3);
     l.add(msg);
     l.add(min);
     l.add(max);
     return l;
   }
 
   /**
    * Create a tuple
    * @param msg
    * @param min
    * @return
    */
   protected static List<Object> t(String msg, int min) {
     return t(msg, min, min);
   }
 
   /**
    * get the name: relies on arg 1 being the cluster name in all operations 
    * @return the name argument, null if there is none
    */
   public String getClusterName() {
     return (actionArgs == null || actionArgs.isEmpty() || args.length < 2) ?
            null : args[1];
   }
 
   public CommonArgs(String[] args) {
     this.args = args;
     commander = new JCommander(this);
   }
 
   public CommonArgs(Collection args) {
     List<String> argsAsStrings = HoyaUtils.collectionToStringList(args);
     this.args = argsAsStrings.toArray(new String[argsAsStrings.size()]);
     commander = new JCommander(this);
   }
 
 
   public String usage() {
     StringBuilder builder = new StringBuilder("\n");
     commander.usage(builder, "  ");
     builder.append("\nactions: ");
     Map<String, List<Object>> actions = getActions();
     for (String key : actions.keySet()) {
       builder.append(key).append(" ");
     }
     return builder.toString();
   }
 
   public void parse() throws BadCommandArgumentsException {
     try {
       commander.parse(args);
     } catch (ParameterException e) {
       throw new BadCommandArgumentsException(e, "%s in %s", 
         e.toString(),
         (args != null ? ( HoyaUtils.join(args, " ")) : "[]"));
     }
   }
 
   /**
    * Map of supported actions to (description, #of args following)
    * format is of style:
    * <pre>
    *   (ACTION_CREATE): ["create cluster", 1],
    * </pre>
    * @return
    */
   public Map<String, List<Object>> getActions() {
     return Collections.emptyMap();
   }
 
   /**
    * validate args via {@link #validate()}
    * then postprocess the arguments
    */
   public void postProcess() throws BadCommandArgumentsException {
     validate();
     for (String prop : definitions) {
       String[] keyval = prop.split("=", 2);
       if (keyval.length == 2) {
         definitionMap.put(keyval[0], keyval[1]);
       }
     }
   }
 
   /**
    * Validate the arguments against the action requested
    */
   public void validate() throws BadCommandArgumentsException {
     if (parameters.isEmpty()) {
       throw new BadCommandArgumentsException(ERROR_NO_ACTION
                                              + (args != null
                                                 ? (" in " +
                                                    HoyaUtils.join(args, " "))
                                                 : "")
                                              + usage());
     }
     action = parameters.get(0);
     LOG.debug("action={}", action);
     Map<String, List<Object>> actionMap = getActions();
     List<Object> actionOpts = actionMap.get(action);
     if (null == actionOpts) {
       throw new BadCommandArgumentsException(ERROR_UNKNOWN_ACTION
                                              + action
                                              + (args != null
                                                 ? (" in " +
                                                    HoyaUtils.join(args, " "))
                                                 : "")
                                              + usage());
     }
     actionArgs = parameters.subList(1, parameters.size());
 
     int minArgs = (Integer) actionOpts.get(1);
     int actionArgSize = actionArgs.size();
 /*
     LOG.debug("Action {} expected #args={} actual #args={}", action, minArgs,
               actionArgSize);
 */
     if (minArgs > actionArgSize) {
       throw new BadCommandArgumentsException(
         ERROR_NOT_ENOUGH_ARGUMENTS + action + " in \"" +
         HoyaUtils.join(actionArgs, " ") + "\"");
     }
     int maxArgs =
       (actionOpts.size() == 3) ? ((Integer) actionOpts.get(2)) : minArgs;
     if (actionArgSize > maxArgs) {
       throw new BadCommandArgumentsException(
         ERROR_TOO_MANY_ARGUMENTS + action
         + " in \"" + HoyaUtils.join(actionArgs, " ") + "\"");
     }
   }
 
   /**
    * Apply all the definitions on the command line to the configuration
    * @param conf config
    */
   public void applyDefinitions(Configuration conf) {
     for (String key : definitionMap.keySet()) {
       String val = definitionMap.get(key);
       conf.set(key, val, "command line");
     }
   }
 
   /**
    * If the Filesystem URL was provided, it overrides anything in
    * the configuration
    * @param conf configuration
    */
   public void applyFileSystemURL(Configuration conf) {
     if (filesystemURL != null) {
       //filesystem argument was set -this overwrites any defaults in the
       //configuration
       FileSystem.setDefaultUri(conf, filesystemURL);
     }
   }
 
   /**
    * Create a map from a tuple list like ['worker','2','master','1] into a map
    * ['worker':'2',"master":'1'];
    * Duplicate entries also trigger errors
    * @param description description for errors
    * @param list list to conver to tuples
    * @return the map of key value pairs -unordered.
    * @throws BadCommandArgumentsException odd #of arguments received
    */
   public Map<String, String> convertTupleListToMap(String description,
                                                    List<String> list) throws
                                                                       BadCommandArgumentsException {
     Map<String, String> results = new HashMap<String, String>();
     if (list != null && !list.isEmpty()) {
       int size = list.size();
       if (size % 2 != 0) {
         //odd number of elements, not permitted
         throw new BadCommandArgumentsException(
           ERROR_PARSE_FAILURE + description);
       }
       for (int count = 0; count < size; count += 2) {
         String key = list.get(count);
         String val = list.get(count + 1);
         if (results.get(key) != null) {
           throw new BadCommandArgumentsException(
             ERROR_DUPLICATE_ENTRY + description
             + ": " + key);
         }
         results.put(key, val);
       }
     }
     return results;
   }
 }
