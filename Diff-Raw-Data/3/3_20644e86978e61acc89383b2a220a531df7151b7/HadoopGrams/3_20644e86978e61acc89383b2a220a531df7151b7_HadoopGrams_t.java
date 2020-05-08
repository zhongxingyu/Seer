 package org.jovislab.tools.hadoop.hadoopgrams;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 
 public class HadoopGrams extends Configured implements Tool {
 
 	public int run(String[] args) throws Exception {
 		if (args.length < 3) {
			System.out.println("Usage: bin/hadoop jar HadoopGrams.jar org.jovislab.tools.hadoop.hadoopgrams.HadoopGrams <input_file> <output_file> <n> [<filter_class>] [<combiner_threshold>] [<reducer_threshold>]");
 			return -1;
 		}
 
 		Job job = new Job();
 		job.setJarByClass(HadoopGrams.class);
 		job.setMapperClass(Map.class);
 		job.setCombinerClass(Combine.class);
 		job.setReducerClass(Reduce.class);
 		job.setOutputKeyClass(Text.class);
 		job.setOutputValueClass(LongWritable.class);
 
 		Configuration config = job.getConfiguration();
 		config.setInt("n", Integer.parseInt(args[2]));
 		if (args.length >= 4) {
 			config.setClass("map.filter", Class.forName(args[3]), Filter.class);
 			// System.out.println("filter.class=" +
 			// Class.forName(args[3]).getName());
 		}
 		if (args.length >= 5) {
 			config.setInt("combine.threshold", Integer.parseInt(args[4]));
 		}
 		if (args.length >= 6) {
 			config.setInt("reduce.threshold", Integer.parseInt(args[5]));
 		}
 		FileInputFormat.setInputPaths(job, new Path(args[0]));
 		FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
 		job.waitForCompletion(true);
 		return 0;
 	}
 
 	public static void main(String args[]) throws Exception {
 		ToolRunner.run(new HadoopGrams(), args);
 	}
 }
