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
 
 
public class CitationHistReduce extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable>
 {
 		//@Override
    public void reduce(IntWritable key, Iterable<IntWritable> values,
                        Context context) throws IOException, InterruptedException {
 			int count = 0;
 			for(IntWritable value : values) {
 				count += value.get();
 			}
 			context.write(key, new IntWritable(count));
 		}
 }
