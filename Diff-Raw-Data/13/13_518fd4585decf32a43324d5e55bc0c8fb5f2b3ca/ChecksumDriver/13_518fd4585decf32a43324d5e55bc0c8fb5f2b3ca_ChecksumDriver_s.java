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
        job.setMapperClass(ChecksumMapper.class);
        job.setReducerClass(ChecksumReducer.class);
         job.setInputFormat(RawFileInputFormat.class);
        job.setOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);

        // CSV format. Close enough.
         job.setOutputFormat(TextOutputFormat.class);
        conf.set("mapred.textoutputformat.separatorText", ",");
 
         FileOutputFormat.setOutputPath(job, new Path("/checksum-" + System.currentTimeMillis()));
         JobClient.runJob(job);
 
         return 0;
     }
 
     public static void main(String[] args) throws Exception {
         int result = ToolRunner.run(new ChecksumDriver(), args);
         System.exit(result);
     }
 }
