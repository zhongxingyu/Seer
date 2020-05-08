 package com.dsp.ass3;
 
 import java.util.logging.Logger;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.services.ec2.model.InstanceType;
 import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
 import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
 import com.amazonaws.services.elasticmapreduce.model.BootstrapActionConfig;
 import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
 import com.amazonaws.services.elasticmapreduce.model.JobFlowInstancesConfig;
 import com.amazonaws.services.elasticmapreduce.model.PlacementType;
 import com.amazonaws.services.elasticmapreduce.model.RunJobFlowRequest;
 import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;
 import com.amazonaws.services.elasticmapreduce.model.ScriptBootstrapActionConfig;
 import com.amazonaws.services.elasticmapreduce.model.StepConfig;
 
 
 public class JobFlow {
 
     private static final Logger logger = Utils.setLogger(Logger.getLogger(JobFlow.class.getName()));
 
     private static String actionOnFailure = "TERMINATE_JOB_FLOW",
             jobName = "jobname",
 
             ec2KeyName = "ec2",
             placementType = "us-east-1a",
             amiVersion = "2.4.2",
             // amiVersion = "3.1.0",
             hadoopVersion = "1.0.3",
             // hadoopVersion = "2.4.0",
             // instanceType = InstanceType.M1Small.toString(),
             instanceType = InstanceType.M1Xlarge.toString(),
 
             s3BaseUri = "s3n://" + Utils.bucket + "/",
 
             logUri = s3BaseUri + "logs/",
 
             updateLuceneUri = s3BaseUri + "lucene/update-lucene.sh",
 
             hadoopOutputFileName = "part-r-*",
 
             countClass = "Count",
 
             countJarUrl = s3BaseUri + "jars/Count.jar",
 
             countOutput = s3BaseUri + Utils.countOutput,
 
             // countInput = s3BaseUri + "steps/Count/input/eng.corp.10k",  // For Testing.
            countInput = "s3://datasets.elasticmapreduce/ngrams/books/20090715/eng-gb-all/5gram/data",
 
     private static int instanceCount = 1;
 
 
     public static void main(String[] args) throws Exception {
         // Load credentials.
         AWSCredentials credentials = Utils.loadCredentials();
         AmazonElasticMapReduce mapReduce = new AmazonElasticMapReduceClient(credentials);
 
         // Set Count job flow step.
         HadoopJarStepConfig countJarConfig = new HadoopJarStepConfig()
             .withJar(countJarUrl)
             .withMainClass(countClass)
             .withArgs(countInput, countOutput);
 
         StepConfig countConfig = new StepConfig()
             .withName(countClass)
             .withHadoopJarStep(countJarConfig)
             .withActionOnFailure(actionOnFailure);
 
         // Set instances.
         JobFlowInstancesConfig instances = new JobFlowInstancesConfig()
             .withInstanceCount(instanceCount)
             .withMasterInstanceType(instanceType)
             .withSlaveInstanceType(instanceType)
             .withHadoopVersion(hadoopVersion)
             .withEc2KeyName(ec2KeyName)
             .withKeepJobFlowAliveWhenNoSteps(false)
             .withPlacement(new PlacementType(placementType));
 
         // Set bootstrap action to update lucene version to the one stated in pom.xml.
         BootstrapActionConfig bootstrapConfig = new BootstrapActionConfig()
             .withName("Update Lucene")
             .withScriptBootstrapAction(new ScriptBootstrapActionConfig().withPath(updateLuceneUri));
 
         // Set job flow request.
         RunJobFlowRequest runFlowRequest = new RunJobFlowRequest()
             .withName(jobName)
             .withAmiVersion(amiVersion)
             .withInstances(instances)
             .withBootstrapActions(bootstrapConfig)
             .withLogUri(logUri)
             // Both parts (A+B), all steps.
             .withSteps(countConfig);
             // Custom steps.
             // .withSteps(fmeasureConfig);
 
         // Execute job flow.
         RunJobFlowResult runJobFlowResult = mapReduce.runJobFlow(runFlowRequest);
         String jobFlowId = runJobFlowResult.getJobFlowId();
         System.out.println("ID: " + jobFlowId);
     }
 }
