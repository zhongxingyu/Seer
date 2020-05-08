 package com.directsupply.MisspelledSearchTermWordCount;
 
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Reducer;
 
 import java.io.IOException;
 
 public class Reduce extends Reducer<Text, Text, Text, Text> {
     @Override
     public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
         int count = 0;
 
         for (Text ignored : values) {
             count++;
         }
 
         Text reduceOutput = new Text();
        reduceOutput.set(String.format("%d", count));
 
         context.write(key, reduceOutput);
     }
 }
 
