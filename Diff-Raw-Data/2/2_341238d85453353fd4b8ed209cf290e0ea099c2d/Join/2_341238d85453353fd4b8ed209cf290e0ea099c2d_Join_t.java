 package com.dsp.ass2;
 
 import java.io.IOException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.Partitioner;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.io.LongWritable;
 
 
 public class Join {
 
     public static class MapClass extends Mapper<LongWritable, Text, Text, Text> {
 
         private Text word = new Text();
 
         // Write { <w1,w2> : w1, c(w1), c(w1,w2) }
         @Override
         public void map(LongWritable key, Text value, Context context)
             throws IOException, InterruptedException {
 
             // Fetch words from value.
             String[] words = value.toString().split(Utils.delim);
             String w1 = words[0],
                    w2 = words[1],
                    cW1 = words[2],
                    cW1W2 = words[3];
 
             Text newValue = new Text(w1 + Utils.delim + cW1 + Utils.delim + cW1W2);
 
             word.set(w1 + Utils.delim + w2);
             context.write(word, newValue);
 
             word.set(w2 + Utils.delim + w1);
             context.write(word, newValue);
         }
     }
 
 
     public static class ReduceClass extends Reducer<Text,Text,Text,Text> {
 
         // For every <w1,w2> - Write { <w1,w2> : c(w1), c(w2), c(w1,w2) }
         @Override
         public void reduce(Text key, Iterable<Text> values, Context context)
             throws IOException, InterruptedException {
             String cW1 = "0", cW2 = "0", cW1W2 = "0";
 
             // Fetch w1, w2, c(w1), c(w2), c(w1,w2).
             String[] w1w2 = key.toString().split(Utils.delim);
             String w1 = w1w2[0],
                    w2 = w1w2[1];
 
             String[] counters;
             for (Text value : values) {
                counters = value.toString().split(Utils.delim);
                 cW1W2 = counters[2];
 
                 if (counters[0].equals(w1)) {
                     cW1 = counters[1];
                 } else {
                     cW2 = counters[1];
                 }
             }
 
             Text newKey = new Text(w1 + Utils.delim + w2),
                  newValue = new Text(cW1 + Utils.delim + cW2 + Utils.delim + cW1W2);
             context.write(newKey, newValue);
         }
     }
 
 
     public static class PartitionerClass extends Partitioner<Text, Text> {
         // TODO make this smarter.
         @Override
         public int getPartition(Text key, Text value, int numPartitions) {
             return getLanguage(key) % numPartitions;
         }
 
         private int getLanguage(Text key) {
             if (key.getLength() > 0) {
                 int c = key.charAt(0);
                 if (c >= Long.decode("0x05D0").longValue() && c <= Long.decode("0x05EA").longValue())
                     return 1;
             }
             return 0;
         }
     }
 
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration();
         conf.set("mapred.reduce.slowstart.completed.maps", "1");
         //conf.set("mapred.map.tasks","10");
         //conf.set("mapred.reduce.tasks","2");
         Job job = new Job(conf, "Join");
         job.setJarByClass(Join.class);
         job.setMapperClass(MapClass.class);
         job.setPartitionerClass(PartitionerClass.class);
        // job.setCombinerClass(CombineClass.class);
         job.setReducerClass(ReduceClass.class);
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(Text.class);
         FileInputFormat.addInputPath(job, new Path(args[0]));
         FileOutputFormat.setOutputPath(job, new Path(args[1]));
         System.exit(job.waitForCompletion(true) ? 0 : 1);
     }
 }
