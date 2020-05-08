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
 
 package org.apache.hcatalog.hcatmix.load;
 
 import org.apache.hadoop.mapred.JobConf;
 
 /**
  * This interface represents the task that would be benchmarked, by the benchmarking framework
  * It would execute in parallel threads by multiple maps which are launched parallelly. The number
  * of threads per map would keep at a fixed interval of time, to see the effect of time taken to
  * execute the task against increase number of parallel clients
  *
  * The same task is executed by different threads simultaneously, so any local state must be stored in
  * ThreadLocal variables.
  */
 public interface Task {
     /**
      * Return the name of the task, that shows up in statistics graphs etc
      * @return
      */
     public String getName();
 
    public void configure(JobConf jobConf) throws Exception;
 
     /**
      * Do the actual task, would be called repeatedly by different threads
      * @throws Exception
      */
     public void doTask() throws Exception;
 
     /**
      * Cleanup code goes here
      */
     public void close();
 
     /**
      * Return the number of times errors occurred which doing the task since inception
      * @return
      */
     public int getNumErrors();
 }
