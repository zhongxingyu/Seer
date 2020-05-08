 package com.hadooptraining.lab9;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.FloatWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 /**
  * An example MapReduce program that uses the combiner.  It uses the NYSE_daily data-set, which has a scheme as follows:
  * exchange,stock_symbol,date,stock_price_open,stock_price_high,stock_price_low,stock_price_close,stock_volume,stock_price_adj_close
  * It finds the maximum high price for each stock over the course of all the data.
  */
 public class StockPriceAnalyzer extends Configured implements Tool {
 
     public static class StockMapper extends Mapper<LongWritable, Text, Text, FloatWritable> {
         private Text stock = new Text();
 
         public void map(LongWritable key, Text value, Context context)
                 throws IOException, InterruptedException {
 
             String line = value.toString(); // Convert the value from Text to a String so we can use the StringTokenizer on it.
 
             StringTokenizer tokenizer = new StringTokenizer(line, ","); // Split the line into fields, using comma as the delimiter
 
             String stock_symbol = null; // We only care about the 2nd and 5th fields (stock_symbol and stock_price_high)
             String stock_price_high = null;
             for (int i = 0; i < 5 && tokenizer.hasMoreTokens(); i++) {
                 switch (i) {
                     case 1:
                         stock_symbol = tokenizer.nextToken();
                         break;
                     case 4:
                         stock_price_high = tokenizer.nextToken();
                         break;
                     default:
                         tokenizer.nextToken();
                         break;
                 }
             }
 
             if (stock_symbol == null || stock_price_high == null) {
                 System.err.println("Warning, bad record!"); // This is a bad record, throw it out
                 return;
             }
 
             if (stock_symbol.equals("stock_symbol")) {
                 // Throw out the schema line at the head of each file
             } else {
                 stock.set(stock_symbol);
                 FloatWritable high = new FloatWritable(Float.valueOf(stock_price_high));
                 context.write(stock, high);
             }
         }
     }
 
     public static class StockReducer
             extends Reducer<Text, FloatWritable, Text, FloatWritable> {
 
        public void reduce(Text key, Iterator<FloatWritable> values, Context context)
                 throws IOException, InterruptedException {
             float max = 0.0f; // assume prices are never negative
            while (values.hasNext()) {
                float current = values.next().get();
                if (max < current) max = current;
             }
             context.write(key, new FloatWritable(max));
         }
     }
 
     @Override
     public int run(String[] args) throws Exception {
         if (args.length < 2) {
             System.err.println("Usage: <input_path> <output_path>");
             System.exit(-1);
         }
 
 		/* input parameters */
         String inputPath = args[0];
         String outputPath = args[1];
 
         Job job = new Job(getConf(), "stock-analysis");
 
         job.setJarByClass(StockPriceAnalyzer.class);
 
         job.setMapperClass(StockMapper.class);
         job.setReducerClass(StockReducer.class);
 
         job.setCombinerClass(StockReducer.class);  // Set a combiner for the task
 
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(FloatWritable.class);
 
         job.setMapOutputKeyClass(Text.class);
         job.setMapOutputValueClass(FloatWritable.class);
 
         FileInputFormat.setInputPaths(job, new Path(inputPath));
         FileOutputFormat.setOutputPath(job, new Path(outputPath));
 
         return job.waitForCompletion(true) ? 0 : 1;
     }
 
     public static void main(String[] args) throws Exception {
         int res = ToolRunner.run(new Configuration(), new StockPriceAnalyzer(), args);
         System.exit(res);
     }
 }
