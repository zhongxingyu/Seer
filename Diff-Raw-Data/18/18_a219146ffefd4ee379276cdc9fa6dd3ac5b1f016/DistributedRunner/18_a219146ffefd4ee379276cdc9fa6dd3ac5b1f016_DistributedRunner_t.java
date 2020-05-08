 package org.apache.hadoop.test.runner;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.test.runner.client.ClasspathUploader;
 import org.apache.hadoop.test.runner.client.IClasspathUploader;
 import org.apache.hadoop.test.runner.client.JobSerializer;
 import org.apache.hadoop.test.runner.common.FileUtils;
 import org.apache.hadoop.test.runner.common.RunnerConfiguration;
 import org.apache.hadoop.test.runner.server.JUnitMapper;
 import org.apache.hadoop.test.runner.server.JUnitReducer;
 
 
 public class DistributedRunner {
     
     public static final String localJunitTestsJarPath = "D:\\Filip\\Apache\\JunitHadoop\\src\\test\\resources\\junit-4.11-SNAPSHOT-tests.jar";
     public static final String localJunitJarPath = "D:\\Filip\\Apache\\JunitHadoop\\src\\test\\resources\\junit-4.10.jar";    
     
     private static final String runnerOutput = "CloudRunnerOutput/";
 
     /**
      * @param args
      * @throws Exception 
      */
     public static void main(String[] args) throws Exception { 
         new DistributedRunner().run(args);
     }
 
     private void run(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
         final RunnerConfiguration config = RunnerConfiguration.get(args);
         final Configuration configuration = new Configuration();
 
         uploadResources(configuration, config);
         
         JobSerializer serializer = new JobSerializer(1, config.getSutPath());
         File descriptions = new File("descriptions.txt");
         serializer.serialize(new File("descriptions.txt"));
         
         Job job = Job.getInstance(configuration);        
         job.setMapperClass(JUnitMapper.class);
         job.setMapOutputKeyClass(Text.class);
         job.setMapOutputValueClass(IntWritable.class);
         job.setReducerClass(JUnitReducer.class);
         
         FileInputFormat.addInputPath(job, new Path(descriptions.getAbsolutePath()));        
         FileOutputFormat.setOutputPath(job, createOutputPath());
         
         job.waitForCompletion(true);
     }
     
     private void uploadResources(final Configuration configuration, final RunnerConfiguration runnerConf) throws IOException {
         IClasspathUploader classpathUploader = ClasspathUploader.get(configuration);        
         classpathUploader.upload(new File(runnerConf.getSutPath()), configuration);
         classpathUploader.upload(new File(runnerConf.getClasspath()), configuration);                 
     }
     
     private Path createOutputPath() {
         File file = new File(runnerOutput);
         if (file.exists())
             FileUtils.fullyDelete(file);
         return new Path(runnerOutput);
     }
 
 }
