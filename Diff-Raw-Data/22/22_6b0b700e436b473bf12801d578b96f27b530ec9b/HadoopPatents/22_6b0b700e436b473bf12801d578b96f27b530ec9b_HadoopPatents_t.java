 package edu.cooper.ece460.patents;
 
 import java.io.*;
 import java.util.*;
 
 import org.apache.hadoop.fs.*;
 import org.apache.hadoop.conf.*;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.util.*;
 import org.apache.hadoop.mapreduce.*;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 
 public class HadoopPatents{
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration();
 
         Path inPath = new Path(args[0]);
         Path outPath = new Path(args[1]);
         Path outPath2 = new Path(args[2]);
 		Path outPath3 = new Path(args[3]);
 
         citation inversion
         Job job1 = new Job(conf, "HadoopPatents");
         job1.setJarByClass(HadoopPatents.class);
         job1.setMapperClass(CitationInversionMap.class);
         // job1.setPartitionerClass(PicasaPartition.class);
         job1.setReducerClass(CitationCatReduce.class);
         job1.setNumReduceTasks(12);
         job1.setMapOutputKeyClass(Text.class);
         job1.setMapOutputValueClass(Text.class);
         job1.setOutputKeyClass(Text.class);
         job1.setOutputValueClass(Text.class);
         job1.setInputFormatClass(TextInputFormat.class);
         job1.setOutputFormatClass(TextOutputFormat.class);
         FileInputFormat.addInputPath(job1, inPath);
         FileOutputFormat.setOutputPath(job1, outPath);
		//job1.waitForCompletion(true);
 
         // citation count
         Job job2 = new Job(conf, "HadoopPatents");
         job2.setJarByClass(HadoopPatents.class);
         job2.setMapperClass(CitationInversionMap.class);
         // job2.setPartitionerClass(PicasaPartition.class);
         job2.setReducerClass(CitationCountReduce.class);
         job2.setNumReduceTasks(12);
         job2.setMapOutputKeyClass(Text.class);
         job2.setMapOutputValueClass(Text.class);
         job2.setOutputKeyClass(Text.class);
         job2.setOutputValueClass(IntWritable.class);
         job2.setInputFormatClass(TextInputFormat.class);
         job2.setOutputFormatClass(TextOutputFormat.class);
         FileInputFormat.addInputPath(job2, inPath);
         FileOutputFormat.setOutputPath(job2, outPath2);
         job2.waitForCompletion(true);
 		
 		// citation histogram
 		Job job3 = new Job(conf, "HadoopPatents");
         job3.setJarByClass(HadoopPatents.class);
         job3.setMapperClass(CitationHistMap.class);
         // job3.setPartitionerClass(PicasaPartition.class);
         job3.setReducerClass(CitationHistReduce.class);
         job3.setNumReduceTasks(12);
         job3.setMapOutputKeyClass(IntWritable.class);
         job3.setMapOutputValueClass(IntWritable.class);
         job3.setOutputKeyClass(IntWritable.class);
         job3.setOutputValueClass(IntWritable.class);
         job3.setInputFormatClass(TextInputFormat.class);
         job3.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.addInputPath(job3, outPath2);
         FileOutputFormat.setOutputPath(job3, outPath3);
         job3.waitForCompletion(true);
 
     }
 }
