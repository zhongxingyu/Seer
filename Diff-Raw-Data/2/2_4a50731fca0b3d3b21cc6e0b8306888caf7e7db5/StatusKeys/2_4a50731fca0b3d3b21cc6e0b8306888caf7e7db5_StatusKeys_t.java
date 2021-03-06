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
 
 package org.apache.hoya.api;
 
 /**
  * Contains status and statistics keys
  */
 public interface StatusKeys {
 
   String STATISTICS_CONTAINERS_ACTIVE_REQUESTS = "containers.active.requests";
   String STATISTICS_CONTAINERS_COMPLETED = "containers.completed";
   String STATISTICS_CONTAINERS_DESIRED = "containers.desired";
   String STATISTICS_CONTAINERS_FAILED = "containers.failed";
   String STATISTICS_CONTAINERS_LIVE = "containers.live";
   String STATISTICS_CONTAINERS_REQUESTED = "containers.requested";
   String STATISTICS_CONTAINERS_STARTED = "containers.start.started";
   String STATISTICS_CONTAINERS_START_FAILED =
       "containers.start.failed";
   String STATISTICS_CONTAINERS_SURPLUS =
       "containers.surplus";
   String STATISTICS_CONTAINERS_UNKNOWN_COMPLETED =
       "containers.unknown.completed";
   /**
    * No of containers provided on AM restart
    */
   String INFO_CONTAINERS_AM_RESTART = "containers.am-restart";
 
  String INFO_CREATE_TIME_MILLIS = "create.time.millis";
   String INFO_CREATE_TIME_HUMAN = "create.time";
   String INFO_LIVE_TIME_MILLIS = "live.time.millis";
   String INFO_LIVE_TIME_HUMAN = "live.time";
   String INFO_FLEX_TIME_MILLIS = "flex.time.millis";
   String INFO_FLEX_TIME_HUMAN = "flex.time";
 
   String INFO_MASTER_ADDRESS = "master.address";
 
   /**
    * System time in millis when the status report was generated
    */
   String INFO_STATUS_TIME_MILLIS = "status.time.millis";
 
   /**
    * System time in human form when the status report was generated
    */
   String INFO_STATUS_TIME_HUMAN = "status.time";
 
   
 }
