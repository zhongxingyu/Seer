 package pagerank;
 
 import java.io.IOException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.DoubleWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 
 @SuppressWarnings("deprecation")
 public class PageRankDriver {
 
 	private static FileSystem fs;
 
 	/**
 	 * PageRank steps
 	 * 		
 	 * 		1 - Update PR values
 	 * 		2 - Sort documents by PR value 
 	 * 
 	 */
 	
 	public static void main(String[] args) {
 		
 		stepOne();
 		try {
 			stepTwo();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void stepOne()
 	{
 		JobClient client = new JobClient();
 		JobConf conf = new JobConf(pagerank.PageRankDriver.class);
 	 
 		
 		//specify mapper and reducer
 		conf.setMapperClass(PageRankMapper.class);
 		conf.setReducerClass(PageRankReducer.class);
 		
 		
 		// specify input and output types
 		conf.setOutputKeyClass(Text.class);
 		conf.setOutputValueClass(Text.class);
 		
 		//set specific mapper output - webpage
 		conf.setMapOutputValueClass(WebPage.class);
 
 		Path in_path = new Path("input/");
 		Path out_path = new Path("output-pg/");
 		
 		FileInputFormat.setInputPaths(conf,in_path);
 		FileOutputFormat.setOutputPath(conf,out_path);
 
 		client.setConf(conf);
 		try {
 			JobClient.runJob(conf);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void stepTwo() throws IOException
 	{
		
		//Hdfs setup
		Configuration fs_conf = new Configuration();
		fs = FileSystem.get(fs_conf);
		
 		if(fs.exists(new Path("output-pg/_logs")))
 		{
 			fs.delete(new Path("output-pg/_logs"));
 		}
 
 		Path in_path = new Path("output-pg/");
 		Path out_path = new Path("finaloutput/");
 		
 		JobClient client = new JobClient();
 		JobConf conf = new JobConf(pagerank.PageRankDriver.class);
 		
 		//set mapper
 		conf.setMapperClass(SortByPageRankMapper.class);
 	
 		// specify input and output types
 		conf.setOutputKeyClass(DoubleWritable.class);
 		conf.setOutputValueClass(Text.class);
 		
 		FileInputFormat.setInputPaths(conf,in_path);
 		FileOutputFormat.setOutputPath(conf,out_path);
 		
 		client.setConf(conf);
 		try {
 			JobClient.runJob(conf);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return;
 	}
 
 }
