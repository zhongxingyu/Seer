 package com.hadooptraining.lab12;
 
 import java.net.URI;
 
 import com.hadooptraining.lab10.IPBasedPartitioner;
 import com.hadooptraining.lab8.LogFileInputFormat;
 import com.hadooptraining.lab8.LogProcessorReduce;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.filecache.DistributedCache;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 /**
  * We can use the Hadoop DistributedCache to distribute read-only file based resources
  * to the Map and Reduce tasks. These resources can be simple data files, archives or
  * JAR files that are needed for the computations performed by the mappers or the reducers.
  *
  * In this exercise we will solve the same log processing application as in Lab 10, but instead
  * of using the IP address as the key, we'll use the country. This requires us to lookup the
  * country for every IP address. We'll be using the free GeoIP database for this purpose. The
  * GeoIP database may be downloaded from http://dev.maxmind.com/geoip. The database itself
  * is a data file that needs to be referenced by the mapper. We'll be using the distributed
  * cache to copy the data file over to every machine so that it is available locally for
  * reference.
  */
 public class LogProcessorDistributed extends Configured implements Tool {
 
     /**
      * The run method for constructing your job. This is required according to the Tools interface.
      * The Tools interface helps constructing a Hadoop job that needs reading, parsing, and
      * processing command-line arguments.
      * @param args
      * @return
      * @throws Exception
      */
     @Override
     public int run(String[] args) throws Exception {
         // If the number of arguments is insufficient, print an error message and exit
         if (args.length < 3) {
             System.err.println("Usage: <input_path> <output_path> <num_reduce_tasks>");
             System.exit(-1);
         }
 
         // Your job is handled by the Job object - managed by the JobTracker
         Job job = Job.getInstance(getConf(), "log-analysis");
 
         // Add the GeoID database in job's cache. The datafile must be available on HDFS.
        // job.addCacheFile(new URI("/user/shrek/GeoIP.dat")); // modern way, but broken in CDH4
        DistributedCache.addCacheFile(new URI("/user/shrek/GeoIP.dat"), job.getConfiguration());
 
         // This locates the jar file that needs to be run by using a class name
         job.setJarByClass(LogProcessorDistributed.class);
 
         // Set the mapper and reducer classes
         job.setMapperClass(LogProcessorMapDistributed.class);
         job.setReducerClass(LogProcessorReduce.class);
 
         // Set reducer output key and value classes
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(IntWritable.class);
 
         // Set the InputFormat class
         job.setInputFormatClass(LogFileInputFormat.class);
 
         // Configure the partitioner
         job.setPartitionerClass(IPBasedPartitioner.class);
 
         // Add the input and output paths from program arguments
         FileInputFormat.setInputPaths(job, new Path(args[0]));
         FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
         // Extract the number of reduce tasks from one of the arguments
         int numReduce = Integer.parseInt(args[2]);
 
         // Set the number of reduce tasks
         job.setNumReduceTasks(numReduce);
         // job.setOutputFormatClass(SequenceFileOutputFormat.class);
 
         // Fire the job and return job status based on success of job
         return job.waitForCompletion(true) ? 0 : 1;
     }
 
     /**
      * This is the main program, which just calls the ToolRunner's run method.
      * @param args arguments to the program
      * @throws Exception
      */
     public static void main(String[] args) throws Exception {
         // Invoke the ToolRunner's run method with required arguments
         int res = ToolRunner.run(new Configuration(), new LogProcessorDistributed(), args);
 
         // Return the same exit code that was returned by ToolRunner.run()
         System.exit(res);
     }
 
 }
