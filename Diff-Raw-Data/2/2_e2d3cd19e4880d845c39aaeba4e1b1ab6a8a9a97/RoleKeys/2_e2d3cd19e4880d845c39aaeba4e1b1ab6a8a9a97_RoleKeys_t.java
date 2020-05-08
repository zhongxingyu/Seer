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
 
 package org.apache.hadoop.hoya.api;
 
 /**
  * Standard options for roles
  */
 public interface RoleKeys {
 
 
   /**
    * The name of a role: {@value}
    */
   String ROLE_NAME = "role.name";
   
   /**
    * Number of instances of a role needed: {@value} 
    */
   String ROLE_INSTANCES = "role.instances";
 
   /**
    * Status report: number actually granted : {@value} 
    */
   String ROLE_ACTUAL_INSTANCES = "role.actual.instances";
 
   /**
    * Status report: number currently requested: {@value} 
    */
   String ROLE_REQUESTED_INSTANCES = "role.requested.instances";
 
   /**
    * Status report: number currently being released: {@value} 
    */
   String ROLE_RELEASING_INSTANCES = "role.releasing.instances";
 
   /**
    * Status report: number currently being released: {@value} 
    */
   String ROLE_FAILED_INSTANCES = "role.failed.instances";
 
   /**
    * Status report: number currently being released: {@value} 
    */
   String ROLE_FAILED_STARTING_INSTANCES = "role.failed.starting.instances";
 
   /**
    * Extra arguments (non-JVM) to use when starting this role
    */
  String ROLE_ADDITIONAL_ARGS = "role.additional.args";
   
   /**
    *  Amount of memory to ask YARN for in MB.
    *  <i>Important:</i> this may be a hard limit on the
    *  amount of RAM that the service can use
    *  {@value}
    */
   String YARN_MEMORY = "yarn.memory";
 
   /** {@value} */
   int DEF_YARN_MEMORY = 256;
 
   
   /**
    * Number of cores/virtual cores to ask YARN for
    *  {@value}
    */
   String YARN_CORES = "yarn.vcores";
 
   /** {@value} */
   int DEF_YARN_CORES = 1;
 
   /**
    * Constant to indicate that the requirements of a YARN resource limit
    * (cores, memory, ...) should be set to the maximum allowed by
    * the queue into which the YARN container requests are placed.
    */
   String YARN_RESOURCE_MAX = "max";
   /**
    * For applications that support a web port that can be externally configured,
    * this is the value
    *  {@value}
    */
   String APP_INFOPORT = "app.infoport";
 
   /**
    *  JVM heap size for Java applications in MB.  Only relevant for Java applications.
    *  This MUST be less than or equal to the {@link #YARN_MEMORY} option
    *  {@value}
    */
   String JVM_HEAP = "jvm.heapsize";
 
   /**
    * JVM options other than heap size. Only relevant for Java applications.
    *  {@value}
    */
   String JVM_OPTS = "jvm.opts";
 
 
   /**
    * All keys w/ env. are converted into env variables and passed down
    */
   String ENV_PREFIX = "env.";
 
 
   /**
    * Default no. of cores in the AM {@value}
    */
   int DEFAULT_AM_V_CORES = 1;
   
   /**
    * The default memory of the AM:  {@value}
    */
   int DEFAULT_AM_MEMORY = 1024;
 
   /**
    * The default heap of the AM:  {@value}
    */
   String DEFAULT_AM_HEAP = "512M";
 }
