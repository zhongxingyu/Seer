 package com.akuroda.mapreduce.montecarlopi;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.chain.ChainReducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 
 public class MonteCarloPI {
 
 	static final String IN_THE_CIRCLE = "1";
 	static final String OUT_OF_THE_CIRCLE = "0";
 
 	static class MonteCarloPIMapper extends
 			Mapper<LongWritable, Text, Text, LongWritable> {
 
 		@Override
 		protected void map(LongWritable key, Text value, Context context)
 				throws IOException, InterruptedException {
 
 			double x = Math.random() * 2 - 1;
 			double y = Math.random() * 2 - 1;
 
 			if (Math.hypot(x, y) <= 1.0) {
 				context.write(new Text(IN_THE_CIRCLE), key);
 			} else {
 				context.write(new Text(OUT_OF_THE_CIRCLE), key);
 			}
 		}
 	}
 
 	static class MonteCarloPIReducer extends
 			Reducer<Text, LongWritable, Text, LongWritable> {
 
 		@Override
 		protected void reduce(Text key, Iterable<LongWritable> values,
 				Context context) throws IOException, InterruptedException {
 
 			long counter = 0;
 
 			for (LongWritable value : values) {
 				counter++;
 			}
 			context.write(key, new LongWritable(counter));
 		}
 	}
 
 	static class FinalMapper extends
 			Mapper<Text, LongWritable, Text, LongWritable> {
 
 		static Logger logger = Logger.getLogger(FinalMapper.class.getName());
 		
 		long in_points = 0;
 		long out_points = 0;
 
 		@Override
 		protected void map(Text key, LongWritable val, Context context)
 				throws IOException, InterruptedException {
 			long longval = val.get();
 			if (key.toString().equals(IN_THE_CIRCLE))
 				in_points = longval;
 			else
 				out_points = longval;
 		}
 
 		@Override
 		protected void cleanup(Context context) throws IOException,
 				InterruptedException {
 			double result = (double) in_points / (in_points + out_points) * 4.0;
 			logger.info("IN: " + in_points + ", OUT: " + out_points);
 			logger.info("result = " + result);
 			context.write(new Text("" + result), new LongWritable(in_points + out_points));
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		if (args.length != 2) {
 			System.err.println("Usage: MonteCarloPI <input path> <output path>");
 			System.exit(-1);
 		}
 
		// deprecated as of 0.21
		// Job job = new Job();
 		Job job = Job.getInstance();
 		job.setJarByClass(MonteCarloPI.class);
 
 		FileInputFormat.addInputPath(job, new Path(args[0]));
 		FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
 		job.setMapperClass(MonteCarloPIMapper.class);
 
 		ChainReducer.setReducer(job, MonteCarloPIReducer.class,
 				Text.class, LongWritable.class,
 				Text.class, LongWritable.class,
 				null);
 		ChainReducer.addMapper(job, FinalMapper.class,
 				Text.class, LongWritable.class,
 				Text.class, LongWritable.class,
 				null);
 
 		job.setOutputKeyClass(Text.class);
 		job.setOutputValueClass(LongWritable.class);
 
 		System.exit(job.waitForCompletion(true) ? 0 : 1);
 	}
 }
