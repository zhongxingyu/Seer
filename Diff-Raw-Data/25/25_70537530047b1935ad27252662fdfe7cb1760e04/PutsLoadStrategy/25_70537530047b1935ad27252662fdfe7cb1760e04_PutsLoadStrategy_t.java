 package com.nearinfinity.bulkloader;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 
 import java.io.IOException;
 
 public class PutsLoadStrategy implements LoadStrategy {
    private static final Log LOG = LogFactory.getLog(PutsLoadStrategy.class);
     private final Configuration conf;
     private Path inputDir;
     private Path outputDir;
     private String hb_table;
 
     public PutsLoadStrategy(Configuration conf) throws IOException {
         this.conf = conf;
         outputDir = new Path(conf.get("output_path"));
         inputDir = new Path(conf.get("input_path"));
         hb_table = conf.get("hb_table");
     }
 
     @Override
     public void load() throws Exception {
         createBulkLoadJob();
     }
 
     private Job createBulkLoadJob() throws Exception {
         Job job = new Job(conf, "Import with puts from file " + inputDir.toString() + " into table " + hb_table);
 
         int columnCount = conf.getInt(SamplingPartitioner.COLUMN_COUNT, 3);
         job.setJarByClass(BulkLoader.class);
         FileInputFormat.setInputPaths(job, inputDir);
         job.setInputFormatClass(TextInputFormat.class);
 
         job.setMapperClass(SmallLoaderMapper.class);
         job.setMapOutputKeyClass(ImmutableBytesWritable.class);
         job.setMapOutputValueClass(Put.class);
         job.setNumReduceTasks(2 * columnCount + 1);
 
         FileOutputFormat.setOutputPath(job, outputDir);
 
         TableMapReduceUtil.initTableReducerJob(hb_table, SmallLoaderReducer.class, job, SamplingPartitioner.class);
 
        LOG.info(String.format("Strategy Class: %s", PutsLoadStrategy.class.getName()));
         if (!job.waitForCompletion(true)) {
             System.exit(-1);
         }
 
         return job;
     }
 }
