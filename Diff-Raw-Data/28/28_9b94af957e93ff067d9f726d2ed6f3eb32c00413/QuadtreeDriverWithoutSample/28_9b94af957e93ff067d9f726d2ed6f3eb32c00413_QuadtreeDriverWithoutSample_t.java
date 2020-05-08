 
 
 import mapreduce.example.quadtreeWithoutSample.QuadTreeMapper;
 import mapreduce.example.quadtreeWithoutSample.QuadTreeReducer;
 import mapreduce.io.PointWritable;
 
 import org.apache.hadoop.conf.*;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.*;
 import org.apache.hadoop.util.*;
 
 
 
 public class QuadtreeDriverWithoutSample extends Configured implements Tool{
 
 	@Override
 	public int run(String[] args) throws Exception {
 		if(args.length != 2) {
 			System.err.printf("Usage: %s [generic option] <input> <output\n", 
 					getClass().getSimpleName());
 			ToolRunner.printGenericCommandUsage(System.err);
 			return -1;
 		}
 				
 		JobConf conf = new JobConf(getConf(), getClass());
 		conf.setJobName("QuadTreeWithoutSample");
 		
		conf.setInt("capacity", 1000);
 		conf.setStrings("boundary",
 				"0-100",
 				"0-100"				
 		);
 		
 		
 		FileInputFormat.addInputPath(conf, new Path(args[0]));
 		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
 		
 		
 //		conf.setInputFormat(theClass);
 		conf.setMapOutputKeyClass(Text.class);
 		conf.setMapOutputValueClass(PointWritable.class);		
 		conf.setMapperClass(QuadTreeMapper.class);
 		
 	
 		conf.setOutputValueClass(Text.class);		
 		
 		
 //		conf.setNumReduceTasks(0);
 		
 		conf.setReducerClass(QuadTreeReducer.class);
 		
 		
 		JobClient.runJob(conf);
 		return 0;
 	}
 
 	public static void main(String[] args) throws Exception{
 		int exitCode = ToolRunner.run(new QuadtreeDriverWithoutSample(), args);
 		System.exit(exitCode);
 	}
 }
