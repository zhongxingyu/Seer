 package com.imaginea.qctree.hadoop;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 import com.imaginea.qctree.measures.Aggregates;
 
 public class QueryDriver implements Tool {
 
   private Configuration conf;
 
   @Override
   public Configuration getConf() {
     return conf;
   }
 
   @Override
   public void setConf(Configuration conf) {
     this.conf = conf;
   }
 
   @Override
   public int run(String[] args) throws Exception {
     Job query = Job.getInstance(getConf(), "Query");
     query.setJarByClass(QueryDriver.class);
     query.setMapperClass(QueryMapper.class);
     query.setMapOutputKeyClass(NullWritable.class);
     query.setMapOutputValueClass(Aggregates.class);
     query.setInputFormatClass(SequenceFileInputFormat.class);
 
    Path input = new Path(args[0]);
    Path output = new Path(args[1]);

    FileInputFormat.setInputPaths(query, input);
    FileOutputFormat.setOutputPath(query, output);
 
     return query.waitForCompletion(true) ? 0 : -1;
   }
 
   public static void main(String[] args) throws Exception {
     Configuration conf = new Configuration();
     conf.set("query", args[2]);
    ToolRunner.run(conf, new QueryDriver(), args);
   }
 
 }
