 package mapreduce;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.mapreduce.Counter;
 import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 
 
 import canopy.*;
 import config.Constants;
 
 
 public class CanopyDriver {
 	public static void configure() {
 
 	}
 	
 	public static void main(String[] args){
 	
 		if (args.length < 4) {
 			System.out.println("Usage: program <t1> <t2> <input> <output>");
 			System.exit(0);
 		}
 	
 		Configuration conf = new Configuration();
 		
 		conf.set(Constants.T1_KEY, args[0]);
 		conf.set(Constants.T2_KEY, args[1]);
 		
 		Path in = new Path(args[2]);
 		Path out = new Path(args[3]);
 		
 		
 		try {
 
 				Job job = new Job(conf);
 				job.setNumReduceTasks(2);
 				job.setJobName("Canopy clustering");
 				
 				job.setMapperClass(CanopyMapper.class);
 				job.setReducerClass(CanopyReducer.class);
 				job.setJarByClass(CanopyDriver.class);
 
				FileInputFormat.addInputPath(job, in);
				FileOutputFormat.setOutputPath(job, out);
				job.setInputFormatClass(FileInputFormat.class);
				job.setOutputFormatClass(FileOutputFormat.class);
 
 				job.setOutputKeyClass(IntWritable.class);
 				job.setOutputValueClass(Canopy.class);
 
 				job.waitForCompletion(true);
 
 
 		} catch (Exception e) {
 			// TODO:
 			// a better error report routine
 			e.printStackTrace();
 		}
 		
 	}
 	
 }
 
