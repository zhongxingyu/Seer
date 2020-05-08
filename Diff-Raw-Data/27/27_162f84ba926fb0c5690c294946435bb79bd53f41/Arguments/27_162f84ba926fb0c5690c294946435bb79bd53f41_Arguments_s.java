 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *   
  *    http://www.apache.org/licenses/LICENSE-2.0
  *   
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License. See accompanying LICENSE file.
  */
 
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
 
 package org.apache.hadoop.hoya.yarn;
 
 /**
  * Here are all the arguments that may be parsed by the client or server
  * command lines. Both entry 
  */
 public interface Arguments {
 //  String ARG_ACTION = "--action";
   String ARG_AMQUEUE = "--amqueue";
   String ARG_AMPRIORITY = "--ampriority";
   String ARG_APP_HOME = "--apphome";
   String ARG_APP_ZKPATH = "--zkpath";
   String ARG_CONFDIR = "--appconf";
   String ARG_DEBUG = "--debug";
   String ARG_DEFINE = "-D";
   /**
    filesystem-uri: {@value}
    */
   String ARG_FILESYSTEM = "--fs";
   String ARG_FILESYSTEM_LONG = "--filesystem";
   String ARG_FORMAT = "--format";
   String ARG_HELP = "--help";
   String ARG_IMAGE = "--image";
   String ARG_MANAGER = "--manager";
 //  String ARG_NAME = "--name";
   String ARG_OUTPUT = "--output";
   String ARG_OPTION = "--option";
   String ARG_OPTION_SHORT = "-O";
   String ARG_PERSIST = "--persist";
   String ARG_PROVIDER = "--provider";
   String ARG_RESOURCE_MANAGER = "--rm";
   String ARG_ROLE = "--role";
   String ARG_SYSPROP = "-S";
   String ARG_ROLEOPT = "--roleopt";
  String ARG_USER = "--user";
   String ARG_WAIT = "--wait";
   String ARG_ZKPORT = "--zkport";
   String ARG_ZKHOSTS = "--zkhosts";
 
   /**
    * server: URI for the cluster
    */
   String ARG_HOYA_CLUSTER_URI = "--hoya-cluster-uri";
 
 
   /**
    * server: Path for the resource manager instance (required)
    */
   String ARG_RM_ADDR = "--rm";
 
 }
