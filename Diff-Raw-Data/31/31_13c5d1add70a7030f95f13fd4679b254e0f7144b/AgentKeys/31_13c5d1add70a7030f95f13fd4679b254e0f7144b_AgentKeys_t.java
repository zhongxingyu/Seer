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
 
 package org.apache.hoya.providers.agent;
 
 /*
 
  */
 public interface AgentKeys {
 
   String PROVIDER_AGENT = "agent";
 
   String ROLE_NODE = "node";
   
 
  /** {@value} */
  String CONF_FILE = "agent.conf";
  /** {@value} */
   String REGION_SERVER = "regionserver";
 
   /**
    * What is the command for hbase to print a version: {@value}
    */
   String COMMAND_VERSION = "version";
 
   String ACTION_START = "start";
   String ACTION_STOP = "stop";
 
   /**
    * Config directory : {@value}
    */
   String ARG_CONFIG = "--config";
   
   /**
    * Template stored in the hoya classpath -to use if there is
    * no site-specific template
    *  {@value}
    */
   String CONF_RESOURCE = "org/apache/hoya/providers/agent/conf/";
   
 
 
 }
 
 
