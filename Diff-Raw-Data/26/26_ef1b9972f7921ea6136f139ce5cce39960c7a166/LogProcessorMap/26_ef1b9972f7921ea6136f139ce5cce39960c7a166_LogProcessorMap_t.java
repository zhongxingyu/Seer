 package com.hadooptraining.lab8;
 
 import java.io.IOException;
 
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 
 public class LogProcessorMap extends Mapper<Object, LogWritable, Text, IntWritable> {
 
    public static enum LOG_PROCESSOR_COUNTER {
        BAD_RECORDS
    }

     public void map(Object key, LogWritable value, Context context)
             throws IOException, InterruptedException {
 
        if (value.getResponseSize().get() < 1) {
            context.getCounter(LOG_PROCESSOR_COUNTER.BAD_RECORDS).increment(1);
        }
         context.write(value.getUserIP(),value.getResponseSize());
     }
 }
