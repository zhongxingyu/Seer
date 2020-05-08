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
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.huahinframework.emanager.queue.QueueUtils;
 import org.huahinframework.emanager.util.JobUtils;
 import org.huahinframework.emanager.util.S3Utils;
 
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
 import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
 import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsRequest;
 import com.amazonaws.services.elasticmapreduce.model.BootstrapActionConfig;
 import com.amazonaws.services.elasticmapreduce.model.DescribeJobFlowsRequest;
 import com.amazonaws.services.elasticmapreduce.model.DescribeJobFlowsResult;
 import com.amazonaws.services.elasticmapreduce.model.JobFlowDetail;
 import com.amazonaws.services.elasticmapreduce.model.JobFlowInstancesConfig;
 import com.amazonaws.services.elasticmapreduce.model.JobFlowInstancesDetail;
 import com.amazonaws.services.elasticmapreduce.model.PlacementType;
 import com.amazonaws.services.elasticmapreduce.model.RunJobFlowRequest;
 import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;
 import com.amazonaws.services.elasticmapreduce.model.ScriptBootstrapActionConfig;
 import com.amazonaws.services.elasticmapreduce.model.StepConfig;
 import com.amazonaws.services.elasticmapreduce.model.StepDetail;
 import com.amazonaws.services.elasticmapreduce.model.TerminateJobFlowsRequest;
 import com.amazonaws.services.elasticmapreduce.util.StepFactory;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 
 /**
  *
  */
 public class ElasticMapReduceManager extends Thread {
     private static final Log log = LogFactory.getLog(ElasticMapReduceManager.class);
 
     private static final String MAP_REDUCE_NAME = "Job Flow";
     private static final String MEMORY_BOOTSTRAP_NAME = "memory bootstrap";
     private static final String HADOOP_BOOTSTRAP_NAME = "Configure MapReduce";
     private static final String HUAHIN_BOOTSTRAP_NAME = "Huahin Manager";
 
     private static final String MEMORY_BOOTSTRAP_URI =
             "s3://elasticmapreduce/bootstrap-actions/configurations/latest/memory-intensive";
     private static final String HADOOP_BOOTSTRAP_URI =
             "s3://elasticmapreduce/bootstrap-actions/configure-hadoop";
     private static final String HUAHIN_MANAGER_URI =
             "s3://huahin/manager/configure";
 
     private static final int POLLING_SECOND = 1 * (1000 * 60); // 1 minute
     private static final int TIME_LIMIT_MINUTES = 50;
     private static final int CHARGE_MINUTES = 60;
 
     private static final String EMR_DEBUGGIN_NAME = "Enable Debugging";
     private static final String ACTION_ON_TERMINATE = "TERMINATE_JOB_FLOW";
 
     private AmazonElasticMapReduce emr;
     private AmazonS3 s3;
     private EMRProperties emrProperties;
 
     private String jobFlowId;
     private String masterPublicDnsName;
     private Date checkDate;
     private boolean running;
     private boolean terminated;
 
     private HiveStepConfig hsc;
     private PigStepConfig psc;
     private StreamingStepConfig ssc;
     private CustomJarStepConfig jsc;
 
     /**
      * @param emrProperties
      */
     public ElasticMapReduceManager(EMRProperties emrProperties) {
         this.emrProperties = emrProperties;
         emr = new AmazonElasticMapReduceClient(
                 new BasicAWSCredentials(emrProperties.getAccessKey(),
                                         emrProperties.getSecretKey()));
         s3 = new AmazonS3Client(
                 new BasicAWSCredentials(emrProperties.getAccessKey(),
                                         emrProperties.getSecretKey()));
         if (!isEmpty(emrProperties.getEndpoint())) {
             emr.setEndpoint(emrProperties.getEndpoint());
            s3.setEndpoint(emrProperties.getEndpoint());
         }
     }
 
     /**
      * @param config
      * @throws URISyntaxException
      */
     public void runJob(Config config) throws URISyntaxException {
         RunJobFlowRequest runJobFlowRequest = null;
 
         CreateStepConfigger csc = getCreateStepConfigger(config);
         if (csc == null) {
             log.error("Step config create error");
             return;
         }
 
         if (jobFlowId == null) {
             runJobFlowRequest =
                     new RunJobFlowRequest()
                             .withName(MAP_REDUCE_NAME)
                             .withBootstrapActions(
                                 new BootstrapActionConfig().withName(MEMORY_BOOTSTRAP_NAME)
                                                            .withScriptBootstrapAction(
                                                                    new ScriptBootstrapActionConfig()
                                                                        .withPath(MEMORY_BOOTSTRAP_URI)),
                                 new BootstrapActionConfig().withName(HADOOP_BOOTSTRAP_NAME)
                                                            .withScriptBootstrapAction(
                                                                    new ScriptBootstrapActionConfig()
                                                                        .withPath(HADOOP_BOOTSTRAP_URI)
                                                                        .withArgs("--mapred-key-value",
                                                                                  "mapred.task.timeout=3600000")),
                                 new BootstrapActionConfig().withName(HUAHIN_BOOTSTRAP_NAME)
                                                            .withScriptBootstrapAction(
                                                                    new ScriptBootstrapActionConfig()
                                                                        .withPath(HUAHIN_MANAGER_URI)))
                             .withInstances(setupJobFlowInstancesConfig());
             if (!isEmpty(emrProperties.getLogUri())) {
                 runJobFlowRequest.setLogUri(emrProperties.getLogUri());
             }
 
             List<StepConfig> stepConfigs = new ArrayList<StepConfig>();
             if (emrProperties.isDebug()) {
                 StepConfig enableDebugging =
                         new StepConfig()
                                 .withName(EMR_DEBUGGIN_NAME)
                                 .withActionOnFailure(ACTION_ON_TERMINATE)
                                 .withHadoopJarStep(new StepFactory().newEnableDebuggingStep());
                 stepConfigs.add(enableDebugging);
             }
 
             for (StepConfig sc : csc.createStepConfig(config)) {
                 stepConfigs.add(sc);
             }
             runJobFlowRequest.setSteps(stepConfigs);
 
             try {
                 RunJobFlowResult result = emr.runJobFlow(runJobFlowRequest);
                 jobFlowId = result.getJobFlowId();
                 checkDate = new Date();
             } catch (Exception e) {
                 e.printStackTrace();
                 log.error(e);
             }
         } else {
             AddJobFlowStepsRequest addJobFlowStepsRequest =
                     new AddJobFlowStepsRequest().withJobFlowId(jobFlowId)
                                                 .withSteps(csc.createStepConfig(config));
             emr.addJobFlowSteps(addJobFlowStepsRequest);
         }
 
         running = true;
         try {
             config.setJobFlowId(jobFlowId);
             QueueUtils.updateQueue(config);
         } catch (IOException e) {
             e.printStackTrace();
             log.error(e);
         }
 
         int stepSize = 0;
         String stepStatus = JobUtils.STEP_STATUS_PENDING;
         while (stepStatus.equals(JobUtils.STEP_STATUS_PENDING) ||
                stepStatus.equals(JobUtils.STEP_STATUS_RUNNING)) {
             if (sleep()) {
                 break;
             }
 
             DescribeJobFlowsRequest describeJobFlowsRequest =
                     new DescribeJobFlowsRequest().withJobFlowIds(jobFlowId);
             DescribeJobFlowsResult describeJobFlowsResult =
                     emr.describeJobFlows(describeJobFlowsRequest);
             if (describeJobFlowsResult.getJobFlows().size() != 1) {
                 break;
             }
 
             JobFlowDetail jobFlowDetail = describeJobFlowsResult.getJobFlows().get(0);
             JobFlowInstancesDetail instancesDetail = jobFlowDetail.getInstances();
             masterPublicDnsName = instancesDetail.getMasterPublicDnsName();
             if (isEmpty(config.getMasterPublicDnsName())) {
                 try {
                     config.setMasterPublicDnsName(masterPublicDnsName);
                     QueueUtils.updateQueue(config);
                 } catch (IOException e) {
                     e.printStackTrace();
                     log.error(e);
                 }
             }
 
             stepSize = jobFlowDetail.getSteps().size();
             for (StepDetail stepDetail : jobFlowDetail.getSteps()) {
                 if (stepDetail.getStepConfig().getName().equals(config.getName())) {
                     stepStatus = stepDetail.getExecutionStatusDetail().getState();
                     break;
                 }
             }
         }
 
         if (config.isDeleteOnExit()) {
             if (config.getJobType() == Config.JOB_TYPE_STREAMING) {
                 S3Utils.delete(s3, config.getArgMap().get("mapper"));
                 S3Utils.delete(s3, config.getArgMap().get("reducer"));
             } else {
                 S3Utils.delete(s3, config.getRun());
             }
         }
 
         // Add More than 256 Steps to a Job Flow(http://goo.gl/JDtsV)
         if (stepSize >= 255) {
             instanceTerminate();
         }
 
         running = false;
 
         if (stepStatus.equals(JobUtils.STEP_STATUS_COMPLETED)) {
             config.setStatus(Config.JOB_STATUS_COMPLETE);
         } else if (stepStatus.equals(JobUtils.STEP_STATUS_FAILED)) {
             config.setStatus(Config.JOB_STATUS_ERROR);
         } else if (terminated) {
             config.setStatus(Config.JOB_STATUS_CANCEL);
         }
 
         try {
             QueueUtils.updateQueue(config);
         } catch (IOException e) {
             e.printStackTrace();
             log.error(e);
         }
     }
 
     /**
      *
      */
     public void instanceTerminate() {
         if (jobFlowId != null) {
             log.info("terminate MapReduce instance");
             TerminateJobFlowsRequest request = new TerminateJobFlowsRequest();
             request.setJobFlowIds(Arrays.asList(jobFlowId));
             emr.terminateJobFlows(request);
             jobFlowId = null;
             checkDate = null;
         }
     }
 
     /**
      * @return terminated
      */
     private boolean sleep() {
         try {
             super.sleep(POLLING_SECOND);
         } catch (InterruptedException e) {
             return true;
         }
 
         return false;
     }
 
     /**
      *
      */
     public void terminate() {
         terminated = true;
         super.interrupt();
     }
 
     /**
      * @return If true, returns time afeter
      */
     public boolean isTimeAfter() {
         if (checkDate == null) {
             return false;
         }
 
         Calendar check = Calendar.getInstance();
         Calendar now = Calendar.getInstance();
 
         check.setTime(checkDate);
         check.set(Calendar.MINUTE, check.get(Calendar.MINUTE) + CHARGE_MINUTES);
         if (now.after(check)) {
             checkDate = check.getTime();
             return false;
         }
 
         check.setTime(checkDate);
         check.set(Calendar.MINUTE, check.get(Calendar.MINUTE) + TIME_LIMIT_MINUTES);
 
         return now.after(check);
     }
 
     /**
      * @return the running
      */
     public boolean isRunning() {
         return running;
     }
 
     /**
      * @return JobFlowInstancesConfig
      */
     private JobFlowInstancesConfig setupJobFlowInstancesConfig() {
         JobFlowInstancesConfig config =
                 new JobFlowInstancesConfig()
                         .withKeepJobFlowAliveWhenNoSteps(true)
                         .withInstanceCount(emrProperties.getInstanceCount())
                         .withMasterInstanceType(emrProperties.getMasterInstanceType());
 
         if (!isEmpty(emrProperties.getKeyPairName())) {
             config.setEc2KeyName(emrProperties.getKeyPairName());
         }
 
         if (!isEmpty(emrProperties.getHadoopVersion())) {
             config.setHadoopVersion(emrProperties.getHadoopVersion());
         }
 
         if (!isEmpty(emrProperties.getAvailabilityZone())) {
             config.setPlacement(new PlacementType()
                                         .withAvailabilityZone(emrProperties.getAvailabilityZone()));
         }
 
         if (!isEmpty(emrProperties.getSlaveInstanceType())) {
             config.setSlaveInstanceType(emrProperties.getSlaveInstanceType());
         } else {
             config.setSlaveInstanceType(emrProperties.getMasterInstanceType());
         }
 
         return config;
     }
 
     /**
      * @param config
      * @return CreateStepConfigger
      */
     private CreateStepConfigger getCreateStepConfigger(Config config) {
         switch (config.getJobType()) {
         case Config.JOB_TYPE_HIVE:
             if (hsc == null) {
                 hsc = new HiveStepConfig();
             }
             return hsc;
         case Config.JOB_TYPE_PIG:
             if (psc == null) {
                 psc = new PigStepConfig();
             }
             return psc;
         case Config.JOB_TYPE_STREAMING:
             if (ssc == null) {
                 ssc = new StreamingStepConfig();
             }
             return ssc;
         case Config.JOB_TYPE_CUSTOM_JAR:
             if (jsc == null) {
                 jsc = new CustomJarStepConfig();
             }
             return jsc;
         default:
             return null;
         }
     }
 
     /**
      *
      */
     private boolean isEmpty(String s) {
         return s == null || s.isEmpty();
     }
 }
