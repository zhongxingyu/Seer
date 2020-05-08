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
 
 package org.apache.hoya;
 
 /**
  * Keys shared across tests
  */
 public interface HoyaXMLConfKeysForTesting {
 
   String KEY_HOYA_TEST_HBASE_HOME = "hoya.test.hbase.home";
   String KEY_HOYA_TEST_HBASE_TAR = "hoya.test.hbase.tar";
   String KEY_HOYA_TEST_HBASE_APPCONF = "hoya.test.hbase.appconf";
   String KEY_HOYA_TEST_ACCUMULO_HOME = "hoya.test.accumulo.home";
   String KEY_HOYA_TEST_ACCUMULO_TAR = "hoya.test.accumulo.tar";
   String KEY_HOYA_TEST_ACCUMULO_APPCONF = "hoya.test.accumulo.appconf";
 
   String KEY_HOYA_THAW_WAIT_TIME = "hoya.test.thaw.wait.seconds";
 
   int DEFAULT_HOYA_THAW_WAIT_TIME = 60000;
 
 
   String KEY_HOYA_FREEZE_WAIT_TIME = "hoya.test.freeze.wait.seconds";
 
   int DEFAULT_HOYA_FREEZE_WAIT_TIME = 60000;
 
   String KEY_HOYA_TEST_TIMEOUT = "hoya.test.timeout.millisec";
 
   int DEFAULT_HOYA_TEST_TIMEOUT = 10 * 60 * 1000;
 
 
   String KEY_HOYA_HBASE_LAUNCH_TIME = "hoya.test.hbase.launch.wait.seconds";
 
   String KEY_HOYA_TEST_HBASE_ENABLED = "hoya.test.hbase.enabled";
 
   int DEFAULT_HOYA_HBASE_LAUNCH_TIME = 60 * 3 * 1000;
 
   String KEY_HOYA_TEST_ACCUMULO_ENABLED = "hoya.test.accumulo.enabled";
 
   String KEY_HOYA_ACCUMULO_LAUNCH_TIME =
     "hoya.test.accumulo.launch.wait.seconds";
 
   int DEFAULT_HOYA_ACCUMULO_LAUNCH_TIME = 60 * 3 * 1000;
 
 
   /**
    * AM-RESTART-SUPPORT
    * Flag to indicate whether or not YARN supports container rebind
    * on restart. It is for testing
    */
  boolean YARN_AM_SUPPORTS_RESTART = true;
 }
