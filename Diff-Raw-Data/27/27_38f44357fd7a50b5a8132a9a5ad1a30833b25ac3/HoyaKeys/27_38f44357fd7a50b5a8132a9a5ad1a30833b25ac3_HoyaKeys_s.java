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
 
 package org.apache.hadoop.hoya;
 
 
 /**
  * Keys and various constants for Hoya
  */
 public interface HoyaKeys {
 
   String ROLE_MASTER = "master";
   
   /**
    * The path under which cluster and temp data are stored
    * {@value}
    */
   String HOYA_BASE_DIRECTORY = ".hoya";
 
   /**
    *  name of the relative path to install hBase into:  {@value}
    */
   String LOCAL_TARBALL_INSTALL_SUBDIR = "local";
 
 
   /**
    * Application type for YARN  {@value}
    */
   String APP_TYPE = "hoya";
 
   /**
    * JVM arg to force IPv4  {@value}
    */
   String JAVA_FORCE_IPV4 = "-Djava.net.preferIPv4Stack=true";
 
   /**
    * JVM arg to go headless  {@value}
    */
 
   String JAVA_HEADLESS = "-Djava.awt.headless=true";
   String FORMAT_D_CLUSTER_NAME = "-Dhoya.cluster.name=%s";
   String FORMAT_D_CLUSTER_TYPE = "-Dhoya.app.type=%s";
 
 
   /**
    * This is the name of the dir/subdir containing
    * the hbase conf that is propagated via YARN
    *  {@value}
    */
   String PROPAGATED_CONF_DIR_NAME = "conf";
   String GENERATED_CONF_DIR_NAME = "generated";
   String ORIG_CONF_DIR_NAME = "original";
   String DATA_DIR_NAME = "database";
   String CLUSTER_SPECIFICATION_FILE = "cluster.json";
 
   int MIN_HEAP_SIZE = 0;
 
   /**
    * XML resource listing the standard Hoya providers
    * {@value}
    */
   String HOYA_XML ="org/apache/hadoop/hoya/hoya.xml";
 
   /**
    * pattern to identify a hoya provider
    * {@value}
    */
   String HOYA_PROVIDER_KEY = "hoya.provider.%s";
 
   /**
    * conf option set to point to where the config came from
    * {@value}
    */
   String KEY_HOYA_TEMPLATE_ORIGIN = "hoya.template.origin";
 
   String CLUSTER_DIRECTORY = "cluster";
   /**
    * Command to issue to override any specific role in the in-AM master
    * script. Used for things like issuing a version command in testing
    */
   String OPTION_HOYA_MASTER_COMMAND =
     "hoya.master.command";
 
   String OPTION_SITE_PREFIX = "site.";
 
 
   /**
    * Time in milliseconds to wait after forking the in-AM master
    * process before attempting to start up the containers. 
    * A shorter value brings the cluster up faster, but means that if the
    * master process fails (due to a bad configuration), then time
    * is wasted starting containers on a cluster that isn't going to come
    * up
    */
   String OPTION_CONTAINER_STARTUP_DELAY = "hoya.container.startup.delay";
   int CONTAINER_STARTUP_DELAY = 5000;
  String FS_DEFAULT_NAME = "fs.default.name";
 }
