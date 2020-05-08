 package mapreduce;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 //import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 
 import vector.VectorDoubleWritable;
 import config.Constants;
 
 public class CanopyDriver {
 	public static void configure() {
 		// intentionally left blank?
 	}
 
 	public static void main(String[] args) {
 
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
 			job.setNumReduceTasks(Constants.CANOPY_REDUCER_NUMBER);
 			job.setJobName("Canopy clustering");
 
 			job.setJarByClass(CanopyDriver.class);
 			// general configuration
 			TextInputFormat.addInputPath(job, in);
 			SequenceFileOutputFormat.setOutputPath(job, out);
 			job.setInputFormatClass(TextInputFormat.class);
 			job.setOutputFormatClass(SequenceFileOutputFormat.class);
 			
 			// configure mapper
 			job.setMapperClass(CanopyMapper.class);
 			job.setMapOutputKeyClass(Text.class);
 			job.setMapOutputValueClass(VectorDoubleWritable.class);
 			// configure reducer
 			job.setReducerClass(CanopyReducer.class);
 			job.setOutputKeyClass(LongWritable.class);
			job.setOutputValueClass(Text.class);
 
 			job.waitForCompletion(true);
 
 		} catch (Exception e) {
 			// TODO:
 			// a better error report routine
 			e.printStackTrace();
 		}
 
 	}
 
 }
