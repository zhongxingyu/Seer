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
 public interface HoyaKeys extends HoyaXmlConfKeys {
 
   String ROLE_HOYA_AM = "hoya";
   /**
    * Hoya role is "special"
    */
   int ROLE_HOYA_AM_PRIORITY_INDEX = 0;
   
   
   /**
    * The path under which cluster and temp data are stored
    * {@value}
    */
   String HOYA_BASE_DIRECTORY = ".hoya";
 
   /**
    *  name of the relative path to expaned an image into:  {@value}.
    *  The title of this path is to help people understand it when
    *  they see it in their error messages
    */
   String LOCAL_TARBALL_INSTALL_SUBDIR = "expandedarchive";
 
 
   /**
    * Application type for YARN  {@value}
    */
   String APP_TYPE = "hoya";
 
   /**
    * JVM arg to force IPv4  {@value}
    */
   String JVM_ENABLE_ASSERTIONS = "-ea";
   
   /**
    * JVM arg enable JVM system/runtime {@value}
    */
   String JVM_ENABLE_SYSTEM_ASSERTIONS = "-esa";
 
   /**
    * JVM arg to force IPv4  {@value}
    */
   String JVM_FORCE_IPV4 = "-Djava.net.preferIPv4Stack=true";
 
   /**
    * JVM arg to go headless  {@value}
    */
 
   String JVM_JAVA_HEADLESS = "-Djava.awt.headless=true";
   String FORMAT_D_CLUSTER_NAME = "-Dhoya.cluster.name=%s";
   String FORMAT_D_CLUSTER_TYPE = "-Dhoya.app.type=%s";
 
 
   /**
    * This is the name of the dir/subdir containing
    * the hbase conf that is propagated via YARN
    *  {@value}
    */
   String PROPAGATED_CONF_DIR_NAME = "propagatedconf";
   String GENERATED_CONF_DIR_NAME = "generated";
  String ORIG_CONF_DIR_NAME = "original";
   String DATA_DIR_NAME = "database";
   String HISTORY_DIR_NAME = "history";
   String HISTORY_FILENAME_SUFFIX = "json";
   String HISTORY_FILENAME_PREFIX = "rolehistory-";
   
   /**
    * Filename pattern is required to save in strict temporal order.
    * Important: older files must sort less-than newer files when using
    * case-sensitive name sort.
    */
   String HISTORY_FILENAME_CREATION_PATTERN = HISTORY_FILENAME_PREFIX +"%016x."+
                                     HISTORY_FILENAME_SUFFIX;
   /**
    * The posix regexp used to locate this 
    */
   String HISTORY_FILENAME_MATCH_PATTERN = HISTORY_FILENAME_PREFIX +"[0-9a-f]+\\."+
                                     HISTORY_FILENAME_SUFFIX;
     /**
    * The posix regexp used to locate this 
    */
   String HISTORY_FILENAME_GLOB_PATTERN = HISTORY_FILENAME_PREFIX +"*."+
                                     HISTORY_FILENAME_SUFFIX;
   
   String CLUSTER_SPECIFICATION_FILE = "cluster.json";
 
   /**
    * XML resource listing the standard Hoya providers
    * {@value}
    */
   String HOYA_XML ="org/apache/hadoop/hoya/hoya.xml";
 
   String CLUSTER_DIRECTORY = "cluster";
 
   /**
    * JVM property to define the hoya configuration directory;
    * this is set by the hoya script: {@value}
    */
   String PROPERTY_HOYA_CONF_DIR = "hoya.confdir";
 
   /**
    * name of generated dir for this conf: {@value}
    */
   String SUBMITTED_HOYA_CONF_DIR = "hoya_confdir";
 
   /**
    * name of the Hoya client resource
    * loaded when the service is loaded.
    */
   String HOYA_CLIENT_RESOURCE = "hoya-client.xml";
 
   /**
    * The name of the resource to put on the classpath
    * This only goes up on a real cluster, not a test run.
    */
   String HOYA_SERVER_RESOURCE = "hoya-server.xml";
 
   String HOYA_TMP_LOGDIR_PREFIX = "/tmp/hoya-";
   String HOYA_JAR = "hoya.jar";
   String JCOMMANDER_JAR = "jcommander.jar";
   String SLF4J_JAR = "slf4j.jar";
   String SLF4J_LOG4J_JAR = "slf4j-log4j.jar";
   String ZOOKEEPER_JAR = "zookeeper.jar";
 
   String DEFAULT_JVM_HEAP = "256M";
   int DEFAULT_YARN_MEMORY = 256;
   String STDOUT_HOYAAM = "hoyaam.txt";
   String STDERR_HOYAAM = "hoyaam-err.txt";
   String DEFAULT_GC_OPTS = "";
 }
