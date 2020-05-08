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
 
 package org.apache.hcatalog.hcatmix.load.tasks;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hive.metastore.Warehouse;
 import org.apache.hadoop.hive.metastore.api.*;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.thrift.TException;
 import org.apache.thrift.transport.TTransportException;
 import org.perf4j.StopWatch;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.text.MessageFormat;
 import java.util.*;
 
 /**
 * Author: malakar
 */
 public class HCatAddPartitionTask extends HCatLoadTask {
     Partition partition;
     Table hiveTable;
     StorageDescriptor sd;
     Map<String,String> parameters;
     List<FieldSchema> partitionKeys;
     String location;
     private final String hostname;
     private final Random rand = new Random();
 
     public HCatAddPartitionTask() throws IOException, NoSuchObjectException, TException, MetaException {
         super();
         hostname = InetAddress.getLocalHost().getHostName();
     }
 
     @Override
     public void configure(JobConf jobConf) throws Exception {
         super.configure(jobConf);
         hiveTable = hiveClient.get().getTable(dbName, tableName);
         sd = new StorageDescriptor(hiveTable.getSd());
         parameters = hiveTable.getParameters();
         partitionKeys = hiveTable.getPartitionKeys();
         location = hiveTable.getSd().getLocation();
     }
 
     @Override
     public String getName() {
         return "addPartition";
     }
 
     @Override
     public StopWatch doTask() throws Exception {
         StopWatch stopWatch = null;
         try {
             partition = new Partition();
             partition.setDbName(dbName);
             partition.setTableName(tableName);
 
             List<String> pvals = new ArrayList<String>();
 
             // Make the partition name as unique as possible, so that two tasks or wont end up adding the same partition
             // The rand is to introduce more randomness to reduce chances of the same thread adding an already added partition
            final String uniquePartition = MessageFormat.format("{0}_{1}_{2}_{3}", hostname, Thread.currentThread().getId(),
                           System.nanoTime(), UUID.randomUUID().toString());
            pvals.add(uniquePartition);
 
             partition.setValues(pvals);
             partition.setSd(sd);
             partition.setParameters(parameters);
             String partName;
             try {
                 partName = Warehouse.makePartName(partitionKeys, pvals);
             } catch (MetaException e) {
                 throw new RuntimeException("Exception while creating partition name.", e);
             }
             Path partPath = new Path(location, partName);
             partition.getSd().setLocation(partPath.toString());
             partition.setCreateTime((int) (System.currentTimeMillis() / 1000));
             partition.setLastAccessTimeIsSet(false);
 
             stopWatch = new StopWatch(getName());
             hiveClient.get().add_partition(partition);
             stopWatch.stop();
         } catch (TTransportException e) {
             recycleHiveClient();
             throw e;
         } catch(AlreadyExistsException e) {
             // If the partition already exists it is not an error on hcatalog server side, ignore it
         } catch (Exception e) {
             LOG.info("Error adding partitions", e);
             throw e;
         }
         return stopWatch;
     }
 }
