 package com.sandreckoning.hadoop.checksum;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class ChecksumDriver extends Configured implements Tool {
 
     @Override
     public int run(String[] strings) throws Exception {
         Configuration conf = getConf();
 
         JobConf job = new JobConf(conf, ChecksumDriver.class);
         job.setInputFormat(RawFileInputFormat.class);
        job.setMapperClass(ChecksumMapper.class);
         job.setOutputFormat(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setReducerClass(ChecksumReducer.class);
 
         FileOutputFormat.setOutputPath(job, new Path("/checksum-" + System.currentTimeMillis()));
         JobClient.runJob(job);
 
         return 0;
     }
 
     public static void main(String[] args) throws Exception {
         int result = ToolRunner.run(new ChecksumDriver(), args);
         System.exit(result);
     }
 }
