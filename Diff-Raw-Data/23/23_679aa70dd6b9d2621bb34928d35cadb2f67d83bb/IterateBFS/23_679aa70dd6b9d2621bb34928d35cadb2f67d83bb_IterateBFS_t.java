 package bfs;
 
 import java.util.Arrays;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;

 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 
 public class IterateBFS {
 
 	static enum Counters {
 		ReachableNodesAtMap, ReachableNodesAtReduce
 	};
 
 	public static void runJob(String[] input, String output) throws Exception {
 
 		Configuration conf = new Configuration();
 		Job job = new Job(conf);
 		
 		conf.set("mapreduce.child.java.opts", "-Xmx2048m");
 		
 		job.setJarByClass(IterateBFS.class);
 
 		job.setMapperClass(IterateBFSMapper.class);
 		job.setReducerClass(IterateBFSReducer.class);
 
 		
 		//You might want to have programmatic access to the number of reducers
 		int numReducers = 3;
 		job.setNumReduceTasks(numReducers);
 
 
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
 
 		job.setOutputKeyClass(Text.class);
 		job.setOutputValueClass(BFSNode.class);
 
 		Path outputPath = new Path(output);
 
 		FileInputFormat.setInputPaths(job, StringUtils.join(input, ","));
 		FileOutputFormat.setOutputPath(job, outputPath);
 
 		outputPath.getFileSystem(conf).delete(outputPath, true);
 		job.waitForCompletion(true);
 	}
 
 	public static void main(String[] args) throws Exception {
 
 		runJob(Arrays.copyOfRange(args, 0, args.length - 1),
 				args[args.length - 1]);
 
 		
 
 
 	}
 
 }
