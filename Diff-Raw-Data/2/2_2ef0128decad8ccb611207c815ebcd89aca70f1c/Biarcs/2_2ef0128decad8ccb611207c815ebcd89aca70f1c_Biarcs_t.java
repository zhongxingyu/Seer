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
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 
 
 public class Biarcs {
     private static final String
         tokenDelim = " ",
         splitTokenDelim = "/",
         nounWildcard = "*";
 
     // Ngram reader.
     public static class MySequenceFileInputFormat extends SequenceFileInputFormat<LongWritable, Text> {}
 
     private static final Logger logger = Utils.setLogger(Logger.getLogger(Biarcs.class.getName()));
 
 
     // Write { (N1, N2), i1, i2 : dep-tree, total-count }
     public static class MapClass extends Mapper<LongWritable, Text, Text, Text> {
 
         private Text newKey = new Text(),
                 newValue = new Text();
 
 
         @Override
         public void map(LongWritable key, Text value, Context context)
             throws IOException, InterruptedException {
 
             String[] ngram = value.toString().split(Utils.biarcDelim),
                 tokens;
 
             String syntacticNgram = ngram[1];
             tokens = syntacticNgram.split(tokenDelim);
 
             // Noun indexes in biarc.
             String[] nouns = new String[tokens.length];
 
             // Holds the biarc with nouns showing as "NNx" instead of the original noun word.
             StringBuilder sb = new StringBuilder();
 
             String tokenWithoutWord;
             String[] token;  // Holds each token data.
             int nc = 0,  // Noun counter.
                 i, j;
 
             // Parse each token and index nouns in biarcs.
             for (i=0; i < tokens.length; i++) {
                 // Holds the token data without the first part.
                 // For example: 'cease/VB/ccomp/0' --> '/VB/ccomp/0'.
                 tokenWithoutWord = tokens[i].substring(tokens[i].indexOf(splitTokenDelim));
 
                 // Remeber word if noun, and place wildcard char instead of original word in token.
                 token = tokens[i].split(splitTokenDelim);
                 if (token[1].startsWith("NN")) {
                     nouns[i] = token[0];  // [0] is word's location in token.
                     nc++;
 
                     sb.append(nounWildcard + tokenWithoutWord);
                 } else {
                     // Else don't remember word and don't change the token.
                     nouns[i] = null;
                     sb.append(tokens[i]);
                 }
 
                 if (i != tokens.length -1) {
                     sb.append(Utils.delim);
                 }
             }
 
             // Append ngram total count.
             sb.append(Utils.biarcDelim + ngram[2]);
 
             // Do nothing if this biarc has less than 2 nouns.
             if (nc < 2) {
                 return;
             }
 
             // Set value to be 'parsed' biarc with biarc's total count..
             newValue.set(sb.toString());
 
 
             // Set key and emit.
             for (i=0; i < nouns.length; i++) {
                 if (nouns[i] != null) {
                     for (j = i+1; j < nouns.length; j++) {
                         if (nouns[j] != null) {
                             if (nouns[i].compareTo(nouns[j]) < 0) {
                                 newKey.set(nouns[i] + Utils.delim + nouns[j] + Utils.delim + i + Utils.delim + j);
                             } else {
                                 newKey.set(nouns[j] + Utils.delim + nouns[i] + Utils.delim + j + Utils.delim + i);
                             }
 
                             context.write(newKey, newValue);
                         }
                     }
                 }
             }
         }
     }
 
 
     // Partition by key hash code.
     public static class PartitionerClass extends Partitioner<Text, Text> {
         @Override
         public int getPartition(Text key, Text value, int numPartitions) {
             return (key.hashCode() & Integer.MAX_VALUE) % numPartitions;
         }
 
     }
 
 
     // Reducer just passes on data.
     public static class ReduceClass extends Reducer<Text, Text, Text, Text> {
 
         @Override
         public void reduce(Text key, Iterable<Text> values, Context context)
             throws IOException, InterruptedException {
 
             // Each (N1, N2) can have multiple dep-trees.
             for (Text value : values) {
                 context.write(key, value);
             }
         }
     }
 
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration();
 
         Job job = new Job(conf, "Biarcs");
 
         job.setInputFormatClass(MySequenceFileInputFormat.class);
 
         job.setJarByClass(Biarcs.class);
         job.setMapperClass(MapClass.class);
         job.setPartitionerClass(PartitionerClass.class);
         job.setReducerClass(ReduceClass.class);
 
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(Text.class);
 
         // Add all but last argument as input path,
         // and append biarcs file postfix.
         String biarc;
        for (int i=0; i < 99; i++) {
             biarc = i <= 9 ? "0" + i : String.valueOf(i);
             FileInputFormat.addInputPath(job, new Path(args[Utils.argInIndex] + biarc + "-of-99"));
         }
 
         FileOutputFormat.setOutputPath(job, new Path(args[Utils.argInIndex + 1]));
 
         boolean result = job.waitForCompletion(true);
 
         System.exit(result ? 0 : 1);
     }
 }
