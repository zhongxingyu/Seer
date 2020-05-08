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
 
 import org.apache.hadoop.hive.conf.HiveConf;
 import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
 import org.apache.hadoop.hive.metastore.api.Database;
 import org.apache.hadoop.hive.metastore.api.MetaException;
 import org.apache.hadoop.security.token.Token;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.Random;
 
 /**
  * Author: malakar
  */
 public abstract class HCatLoadTask implements Task {
     // Needs to be ThreadLocal, hiveMetaStoreClient fails if used in multiple threads
     private static ThreadLocal<HiveMetaStoreClient> hiveClient;
     private static ThreadLocal<Integer> numErrors;
     private static final Logger LOG = LoggerFactory.getLogger(Task.class);
     public static final String HIVE_CONF_TOKEN_KEY = "hive.metastore.token.signature";
 
     HCatLoadTask(Token token) throws IOException {
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
 
        numErrors = new ThreadLocal<Integer>();
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
 
     public static class HCatReadLoadTask extends HCatLoadTask {
         Random rand = new Random();
 
         HCatReadLoadTask(Token token) throws IOException {
             super(token);
         }
 
         @Override
         public String getName() {
             return "getDatabase";
         }
 
         @Override
         public void doTask() throws Exception {
             try {
                 LOG.info("Doing work in Task");
                 final Database db = hiveClient.get().getDatabase("default");
                 LOG.info("Got database successfully!");
             } catch (Exception e) {
                LOG.info("Error reading database: default", e);
                 numErrors.set(numErrors.get() + 1);
                throw e;
             }
         }
     }
 }
