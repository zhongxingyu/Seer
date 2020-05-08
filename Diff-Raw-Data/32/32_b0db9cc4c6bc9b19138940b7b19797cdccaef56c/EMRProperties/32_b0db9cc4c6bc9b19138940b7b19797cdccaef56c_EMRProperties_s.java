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
 package org.huahinframework.emanager.amazonaws.elasticmapreduce;
 
 /**
  *
  */
 public class EMRProperties {
     private boolean jobForWait;
     private String accessKey;
     private String secretKey;
     private String endpoint;
     private String keyPairName;
     private String hadoopVersion;
     private String availabilityZone;
     private int instanceCount;
     private String masterInstanceType;
     private String slaveInstanceType;
     private boolean debug;
     private String logUri;
 
     /**
      * @return the jobForWait
      */
     public boolean isJobForWait() {
         return jobForWait;
     }
 
     /**
      * @param jobForWait the jobForWait to set
      */
     public void setJobForWait(boolean jobForWait) {
         this.jobForWait = jobForWait;
     }
 
     /**
      * @return the accessKey
      */
     public String getAccessKey() {
         return accessKey;
     }
 
     /**
      * @param accessKey the accessKey to set
      */
     public void setAccessKey(String accessKey) {
         this.accessKey = accessKey;
     }
 
     /**
      * @return the secretKey
      */
     public String getSecretKey() {
         return secretKey;
     }
 
     /**
      * @param secretKey the secretKey to set
      */
     public void setSecretKey(String secretKey) {
         this.secretKey = secretKey;
     }
 
     /**
      * @return the endpoint
      */
     public String getEndpoint() {
         return endpoint;
     }
 
     /**
      * @param endpoint the endpoint to set
      */
     public void setEndpoint(String endpoint) {
         this.endpoint = endpoint;
     }
 
     /**
      * @return the keyPairName
      */
     public String getKeyPairName() {
         return keyPairName;
     }
 
     /**
      * @param keyPairName the keyPairName to set
      */
     public void setKeyPairName(String keyPairName) {
         this.keyPairName = keyPairName;
     }
 
     /**
      * @return the hadoopVersion
      */
     public String getHadoopVersion() {
         return hadoopVersion;
     }
 
     /**
      * @param hadoopVersion the hadoopVersion to set
      */
     public void setHadoopVersion(String hadoopVersion) {
         this.hadoopVersion = hadoopVersion;
     }
 
     /**
      * @return the availabilityZone
      */
     public String getAvailabilityZone() {
         return availabilityZone;
     }
 
     /**
      * @param availabilityZone the availabilityZone to set
      */
     public void setAvailabilityZone(String availabilityZone) {
         this.availabilityZone = availabilityZone;
     }
 
     /**
      * @return the instanceCount
      */
     public int getInstanceCount() {
         return instanceCount;
     }
 
     /**
      * @param instanceCount the instanceCount to set
      */
     public void setInstanceCount(int instanceCount) {
         this.instanceCount = instanceCount;
     }
 
     /**
      * @return the masterInstanceType
      */
     public String getMasterInstanceType() {
         return masterInstanceType;
     }
 
     /**
      * @param masterInstanceType the masterInstanceType to set
      */
     public void setMasterInstanceType(String masterInstanceType) {
         this.masterInstanceType = masterInstanceType;
     }
 
     /**
      * @return the slaveInstanceType
      */
     public String getSlaveInstanceType() {
         return slaveInstanceType;
     }
 
     /**
      * @param slaveInstanceType the slaveInstanceType to set
      */
     public void setSlaveInstanceType(String slaveInstanceType) {
         this.slaveInstanceType = slaveInstanceType;
     }
 
     /**
      * @return the debug
      */
     public boolean isDebug() {
         return debug;
     }
 
     /**
      * @param debug the debug to set
      */
     public void setDebug(boolean debug) {
         this.debug = debug;
     }
 
     /**
      * @return the logUri
      */
     public String getLogUri() {
         return logUri;
     }
 
     /**
      * @param logUri the logUri to set
      */
     public void setLogUri(String logUri) {
         this.logUri = logUri;
     }
 }
