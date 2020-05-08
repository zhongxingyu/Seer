 package com.gsihadoop;
 
 import org.apache.hadoop.io.DoubleWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 import java.io.IOException;
 
 /**
  * This is the main Mapper class template. 
  * 
  * @author ameyb
  */
 public class FinanceMapper extends 
         Mapper<LongWritable, Text, Text, DoubleWritable> 
 {
 //	 public enum Counters { DataRowsWritten, DataInputErrors };
     /**
      * The `Mapper` method.
      * @param key - Input key - The line offset in the file - ignored.
      * @param value - Input Value - This is the line itself.
      * @param context - Provides access to the OutputCollector and Reporter.
      * @throws IOException
      * @throws InterruptedException 
      */
     @Override
     public void map(LongWritable key, Text value, Context context) throws 
             IOException, InterruptedException {
 
         String line = value.toString();
         
         StockData record = StockData.parse(line);
 
         if(record != null && !record.getExchange().equals("exchange")){
         	
         	String year = record.getDate().substring(0, 4);
         	String outputKey = record.getExchange() + " " + record.getStock_symbol() + " " + year;
        	double outputValue = Double.parseDouble(record.getStock_price_close());
         	
             // Record the output in the Context object
         	context.write(new Text(outputKey), new DoubleWritable(outputValue));
         } else {
 //        	context.getCounter(Counters.DataInputErrors).increment(1);
         }
         
 //        context.getCounter(Counters.DataRowsWritten).increment(1);
     }
 }
 
