 package org.apache.hadoop.test.runner.server;
 
 import java.io.IOException;
 
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.junit.runner.Result;
 

/**
 * Maps the input test name into testName/failureCount pair. 
 *
 */
 public class JUnitMapper extends Mapper<Object, Text, Text, IntWritable> {      
     
     @Override
     protected void map(Object key, Text canonicalMethodName, Context context)
             throws IOException, InterruptedException {
         try {
             RunTestCaseJob job = RunTestCaseJob.get(
                     DistributedClassLoader.get(context.getConfiguration()),
                     canonicalMethodName);
             job.start();
             job.join();
             Result result = job.getTestResult();
             context.write(canonicalMethodName, new IntWritable(result.getFailureCount()));
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
