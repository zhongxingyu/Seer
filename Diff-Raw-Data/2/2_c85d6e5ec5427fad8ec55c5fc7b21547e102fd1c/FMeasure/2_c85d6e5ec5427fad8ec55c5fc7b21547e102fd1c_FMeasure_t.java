 package com.dsp.ass2;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Counter;
 import org.apache.hadoop.mapreduce.Counters;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Partitioner;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 
 
 public class FMeasure {
 
     private static final Logger logger = Utils.setLogger(Logger.getLogger(FMeasure.class.getName()));
 
     // NOTE we don't need to count TN, but we keep for ease of understanding the calculations in hand.
     private static final Text tp = new Text("tp"),
             fp = new Text("fp"),
             fn = new Text("fn"),
             tn = new Text("tn");
 
 
     // Recieves { decade, pmi : w1, w2 } and emits { tp/fp/tn/fn : 1 }
     public static class MapClass extends Mapper<LongWritable, Text, Text, LongWritable> {
 
         private static final String unrelatedLink = Utils.s3Uri + "steps/FMeasure/input/wordsim-neg.txt",
                 relatedLink = Utils.s3Uri + "steps/FMeasure/input/wordsim-pos.txt";
 
         private static final LongWritable one = new LongWritable(1);
 
         private static double threshold;
 
         // This structure will hold tagged word pairs: { w1, w2 : true/false }
         private static Map<String, Boolean> taggedPairs = new HashMap <String, Boolean>();
 
 
         // Initialize tagged pairs structure, and orders words in pairs lexicographically.
         private void initData(Boolean value, String pairs) {
             String[] splitPairs = pairs.split("\n"),
                 words;
 
             for (int i=0; i < splitPairs.length; i++) {
                 words = splitPairs[i].split(Utils.delim);
 
                 if (words.length >= 2) {
                     // Arrange each pair lexicographically.
                     if (words[0].compareTo(words[1]) < 0) {
                         taggedPairs.put(words[0] + Utils.delim + words[1], value);
                     } else {
                         taggedPairs.put(words[1] + Utils.delim + words[0], value);
                     }
                 }
             }
         }
 
 
         // Set threshold given as argument, and init tagged pairs.
         @Override
         public void setup(Context context) {
             String t = context.getConfiguration().get("threshold", null);
 
             if (t == null) {
                 logger.severe("Can't get threshold variable.");
                 return;
             }
 
             threshold = Double.parseDouble(t);
 
             // Initialize tagged word pairs hashmap.
             initData(new Boolean(true), Utils.LinkToString(relatedLink));
             initData(new Boolean(false), Utils.LinkToString(unrelatedLink));
         }
 
 
         @Override
         public void map(LongWritable key, Text value, Context context)
             throws IOException, InterruptedException {
 
             // Fetch words from value.
             String[] words = value.toString().split(Utils.delim);
             String decade = words[0],
                     w1 = words[2],
                     w2 = words[3];
 
             // Filter pairs not from last decade.
             if (!decade.equals("200")) {
                 logger.severe("Unsupported decade: " + decade);
                 return;
             }
 
             // we assume that w1 < w2
             Boolean testResult = taggedPairs.get(w1 + Utils.delim +  w2);
 
             // Do nothing if pair isn't a learning sample.
             // That is, not in tagged pairs hashmap.
             if (testResult == null) {
                 return;
             }
 
             // Fetch pmi from key.
             double PMI = Double.parseDouble(words[1]);
 
             // Write pair test result (tp/fp/tn/fn).
             if (testResult.booleanValue()) {
                 if (PMI >= threshold) {
                     context.write(tp, one);  // TP
                 } else {
                     context.write(fn, one);  // FN
                 }
             } else {
                 if (PMI >= threshold) {
                     context.write(fp, one);  // FP
                 } else {
                     context.write(tn, one);  // TN
                 }
             }
         }
     }
 
 
     // Sum every identical count values before sending to reducer.
     public static class CombineClass extends Reducer<Text, LongWritable, Text, LongWritable> {
         @Override
         public void reduce(Text key, Iterable<LongWritable> values, Context context)
             throws IOException, InterruptedException {
 
             context.write(key, new LongWritable(Utils.sumValues(values)));
         }
     }
 
 
     // Partition by test result (tp/fp/tn/fn).
     public static class PartitionerClass extends Partitioner<Text, LongWritable> {
         @Override
         public int getPartition(Text key, LongWritable value, int numPartitions) {
             return (getPartitionNumber(key) % numPartitions);
         }
 
 
         private int getPartitionNumber(Text key) {
             if (key.equals(tp)) {
                 return 0;
             } else if(key.equals(fp)) {
                 return 1;
             } else {  // this means key.equals(fn)
                 return 2;
             }
         }
     }
 
 
     public static class ReduceClass extends Reducer<Text, LongWritable, Text, LongWritable> {
 
         public static enum COUNTER {
             tp, fp , fn , tn
         };
 
 
         // Update global test result counters.
         private void updateCounter(Text key, long sum, Context context) {
             Counter counter = null;
 
             if (key.equals(tp)) {
                 counter = context.getCounter(COUNTER.tp);
             } else if(key.equals(fp)) {
                 counter = context.getCounter(COUNTER.fp);
             } else if (key.equals(tn)) {
                 counter = context.getCounter(COUNTER.tn);
             } else if (key.equals(fn)) {
                 counter = context.getCounter(COUNTER.fn);
             }
 
             if (counter != null) {
                 counter.increment(sum);
             }
         }
 
 
         // Update tp/fp/fn global variables (will be uploaded to S3 in main()):
         // Write { tp/fp/tn/fn : num of tp/fp/tn/fn accoridngly. }
         public void reduce(Text key, Iterable<LongWritable> values, Context context)
             throws IOException, InterruptedException {
 
             long sum = Utils.sumValues(values);
             // Writing is unnecessary since we only need 4 constant global variables.
             // context.write(key, new LongWritable(sum));
             updateCounter(key, sum, context);
         }
     }
 
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration();
         // We need maximum of 4 reducers, since we calculate only 4 different keys: tp/fp/tn/fn.
         conf.set("mapred.reduce.tasks", "4");
         conf.set("mapred.reduce.slowstart.completed.maps", "1");
         conf.set("threshold", args[Utils.argInIndex +2]);
 
         Job job = new Job(conf, "FMeasure");
 
         job.setJarByClass(FMeasure.class);
         job.setMapperClass(MapClass.class);
         job.setCombinerClass(CombineClass.class);
         job.setPartitionerClass(PartitionerClass.class);
         job.setReducerClass(ReduceClass.class);
 
         job.setMapOutputKeyClass(Text.class);
         job.setMapOutputValueClass(LongWritable.class);
 
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(LongWritable.class);
 
         FileInputFormat.addInputPath(job, new Path(args[Utils.argInIndex]));
         FileOutputFormat.setOutputPath(job, new Path(args[Utils.argInIndex +1]));
 
         boolean result = job.waitForCompletion(true);
 
         if (result) {
             long fpNum, fnNum, tpNum, tnNum;
 
             Counters counters = job.getCounters();
 
             fpNum = counters.findCounter(ReduceClass.COUNTER.fp).getValue();
             fnNum = counters.findCounter(ReduceClass.COUNTER.fn).getValue();
             tpNum = counters.findCounter(ReduceClass.COUNTER.tp).getValue();
             tnNum = counters.findCounter(ReduceClass.COUNTER.tn).getValue();
 
             // Calculate precision, recall, and f-measure.
             double precision = tpNum + fpNum == 0 ? 0 : ((double) tpNum) / (tpNum + fpNum),
                    recall = tpNum + fnNum == 0 ? 0 : ((double) tpNum) / (tpNum + fnNum),
                    fmeasure = precision + recall == 0 ? 0 : (2 * precision * recall) / (precision + recall);
 
             // Upload test results, precision, recall, and f-measure to S3.
             StringBuilder sb = new StringBuilder();
            sb.append("threshold" + Utils.delim).append(args[Utils.argInIndex +2]).append("\n");
             sb.append("fp" + Utils.delim).append(Long.toString(fpNum)).append("\n");
             sb.append("fn" + Utils.delim).append(Long.toString(fnNum)).append("\n");
             sb.append("tp" + Utils.delim).append(Long.toString(tpNum)).append("\n");
             sb.append("tn" + Utils.delim).append(Long.toString(tnNum)).append("\n");
             sb.append("precision" + Utils.delim).append(Double.toString(precision)).append("\n");
             sb.append("recall" + Utils.delim).append(Double.toString(recall)).append("\n");
             sb.append("f-measure" + Utils.delim).append(Double.toString(fmeasure)).append("\n");
 
             Utils.uploadToS3(sb.toString(), Utils.fmeasureOutput + Utils.countersFileName);
         }
 
         System.exit(result ? 0 : 1);
     }
 }
