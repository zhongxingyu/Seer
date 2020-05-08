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
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hive.conf.HiveConf;
 import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
 import org.apache.hadoop.hive.metastore.Warehouse;
 import org.apache.hadoop.hive.metastore.api.*;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.security.UserGroupInformation;
 import org.apache.hadoop.security.token.Token;
 import org.apache.hcatalog.hcatmix.conf.HiveTableSchema;
 import org.apache.thrift.TException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 /**
  * Author: malakar
  */
 public abstract class HCatLoadTask implements Task {
     // Needs to be ThreadLocal, hiveMetaStoreClient fails if used in multiple threads
     private static ThreadLocal<HiveMetaStoreClient> hiveClient;
     private static ThreadLocal<Integer> numErrors;
     private static final Logger LOG = LoggerFactory.getLogger(Task.class);
     public static final String HIVE_CONF_TOKEN_KEY = "hive.metastore.token.signature";
 
     private static final String DB_NAME = "default";
     private static final String TABLE_NAME = "load_test_table_0_1";
 
     HCatLoadTask() throws IOException {
         numErrors = new ThreadLocal<Integer>(){
             @Override
             protected Integer initialValue() {
                 return 0;
             }
         };
     }
 
     @Override
    public void configure(JobConf jobConf) {
         Token token = jobConf.getCredentials().getToken(new Text(HadoopLoadGenerator.METASTORE_TOKEN_KEY));
 
         try {
             UserGroupInformation.getCurrentUser().addToken(token);
         } catch (IOException e) {
             LOG.info("Error adding token to user", e);
         }
         if (token == null) {
             throw new IllegalArgumentException("Delegation token needs to be set");
         }
 
         final HiveConf hiveConf = new HiveConf(Task.class);
         hiveConf.set(HIVE_CONF_TOKEN_KEY, HadoopLoadGenerator.METASTORE_TOKEN_SIGNATURE);
         hiveClient = new ThreadLocal<HiveMetaStoreClient>() {
             @Override
             protected HiveMetaStoreClient initialValue() {
                 try {
                     return new HiveMetaStoreClient(hiveConf);
                 } catch (MetaException e) {
                     throw new RuntimeException("Couldn't create HiveMetaStoreClient", e);
                 }
             }
         };
     }
 
     public void close() {
         try {
             hiveClient.get().close();
         } catch (Exception e) {
             LOG.error("Couldn't close hiveClient, ignored error", e);
         }
     }
 
     public int getNumErrors() {
         return numErrors.get();
     }
 
     public static class HCatListPartitionTask extends HCatLoadTask {
         HCatListPartitionTask() throws IOException {
             super();
         }
 
         @Override
         public String getName() {
             return "listPartitions";
         }
 
         @Override
         public void doTask() throws Exception {
             try {
                 hiveClient.get().listPartitions(DB_NAME, TABLE_NAME, (short) -1);
             } catch (Exception e) {
                 LOG.info("Error listing partitions", e);
                 numErrors.set(numErrors.get() + 1);
                 throw e;
             }
         }
     }
 
     public static class HCatAddPartitionTask extends HCatLoadTask {
        final Partition partition;
 
         HCatAddPartitionTask() throws IOException, NoSuchObjectException, TException, MetaException {
             super();
 
             Table hiveTable = hiveClient.get().getTable(DB_NAME, TABLE_NAME);
             partition = new Partition();
             partition.setDbName(DB_NAME);
             partition.setTableName(TABLE_NAME);
 
             List<String> pvals = new ArrayList<String>();
             pvals.add(UUID.randomUUID().toString());
 
             partition.setValues(pvals);
             StorageDescriptor sd = new StorageDescriptor(hiveTable.getSd());
             partition.setSd(sd);
             partition.setParameters(hiveTable.getParameters());
             String partName;
             try {
                 partName = Warehouse.makePartName(
                         hiveTable.getPartitionKeys(), pvals);
                 LOG.info("Setting partition location to :" + partName);
             } catch (MetaException e) {
                 throw new RuntimeException("Exception while creating partition name.", e);
             }
             Path partPath = new Path(hiveTable.getSd().getLocation(), partName);
             partition.getSd().setLocation(partPath.toString());
             partition.setCreateTime((int) (System.currentTimeMillis() / 1000));
             partition.setLastAccessTimeIsSet(false);
         }
 
         @Override
         public String getName() {
             return "addPartition";
         }
 
         @Override
         public void doTask() throws Exception {
             try {
                 hiveClient.get().add_partition(partition);
                 LOG.info("Successfully created partition");
             } catch (Exception e) {
                 LOG.info("Error listing partitions", e);
                 numErrors.set(numErrors.get() + 1);
                 throw e;
             }
         }
     }
 }
