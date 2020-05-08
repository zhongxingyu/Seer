 package com.dsp.ass3;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Partitioner;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 
 
 public class SingleLine {
 
     private static final Logger logger = Utils.setLogger(Logger.getLogger(SingleLine.class.getName()));
 
     // Mapper just passes on data.
     public static class MapClass extends Mapper<LongWritable, Text, Text, Text> {
 
         private Text newKey = new Text(),
                 newValue = new Text();
 
         @Override
         public void map(LongWritable key, Text value, Context context)
             throws IOException, InterruptedException {
 
             // Fetch key/value and emit them.
             String v = value.toString();
             int i = v.indexOf(Utils.keyValueDelim, v.indexOf(Utils.keyValueDelim) + 1);
             newKey.set(v.substring(0, i));
             newValue.set(v.substring(i+1));
 
             context.write(newKey, newValue);
         }
     }
 
 
     // Partition by key hash code.
     public static class PartitionerClass extends Partitioner<Text, Text> {
         @Override
         public int getPartition(Text key, Text value, int numPartitions) {
             return (key.hashCode() & Integer.MAX_VALUE) % numPartitions;
         }
     }
 
 
     // For every noun-pair, write: { N1, N2, hypernym-index : un/related, [dep-tree, i1, i2, hits] ... }
     public static class ReduceClass extends Reducer<Text, Text, Text, Text> {
 
         private Text newValue = new Text();
 
 
         @Override
         public void reduce(Text key, Iterable<Text> values, Context context)
             throws IOException, InterruptedException {
 
             // Append coordinates to string.
             StringBuilder sb = new StringBuilder();
             boolean firstItem = true;
             for (Text value : values) {
                 if (firstItem) {
                     sb.append(value.toString());
                     firstItem = false;
                 } else {
                     sb.append(Utils.coordinateDelim).append(value.toString());
                 }
             }
 
             newValue.set(sb.toString());
             context.write(key, newValue);
         }
     }
 
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration();
 
         Job job = new Job(conf, "SingleLine");
 
         job.setJarByClass(SingleLine.class);
         job.setMapperClass(MapClass.class);
         job.setPartitionerClass(PartitionerClass.class);
         job.setReducerClass(ReduceClass.class);
 
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(Text.class);
 
         // Use this for local testing.
         // Add all but last argument as input path.
        for (int i=0; i < args.length -2; i++) {
             FileInputFormat.addInputPath(job, new Path(args[Utils.argInIndex +i]));
         }
         FileOutputFormat.setOutputPath(job, new Path(args[args.length -1]));
 
         // Use this for AWS.
         // NOTE we use two different input paths in here.
         // FileInputFormat.addInputPath(job, new Path(args[Utils.argInIndex]));
         // FileOutputFormat.setOutputPath(job, new Path(args[Utils.argInIndex +1]));
 
         boolean result = job.waitForCompletion(true);
 
         System.exit(result ? 0 : 1);
     }
 }
