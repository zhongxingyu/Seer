 package com.hadooptraining.lab8;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Counters;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class LogProcessorWithCustomInputFormat extends Configured implements Tool {
 
     @Override
     public int run(String[] args) throws Exception {
         if (args.length < 3) {
             System.err.println("Usage:  <input_path> <output_path> <num_reduce_tasks>");
             System.exit(-1);
         }
 
 		/* input parameters */
         String inputPath = args[0];
         String outputPath = args[1];
         int numReduce = Integer.parseInt(args[2]);
 
         Job job = Job.getInstance(getConf(), "log-analysis");
 
         job.setJarByClass(LogProcessorWithCustomInputFormat.class);
 
         job.setMapperClass(LogProcessorMap.class);
         job.setReducerClass(LogProcessorReduce.class);
 
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(IntWritable.class);
 
         job.setInputFormatClass(LogFileInputFormat.class);
 
         FileInputFormat.setInputPaths(job, new Path(inputPath));
         FileOutputFormat.setOutputPath(job, new Path(outputPath));
 
         job.setNumReduceTasks(numReduce);
 
        boolean jobResult = job.waitForCompletion(true);

        Counters counters = job.getCounters();
        Counter badRecords = counters.findCounter(LogProcessorMap.LOG_PROCESSOR_COUNTER.BAD_RECORDS);
        System.out.println("Number of Bad Records: " + badRecords.getValue());

        return jobResult ? 0 : 1;
     }
 
     public static void main(String[] args) throws Exception {
         int res = ToolRunner.run(new Configuration(), new LogProcessorWithCustomInputFormat(), args);
         System.exit(res);
     }
 }
