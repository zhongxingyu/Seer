 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.util.ToolRunner;
 
 import java.io.IOException;
 import java.util.*;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.conf.*;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.mapred.*;
 import org.apache.hadoop.util.*;	
 
 
 public class MainRunner extends Configured implements Tool{
 
	public static int MAX_DISTINCT = 5000;
 	
 	/*
 	 * Workhorse of the project.  Will actually run things.  Returns a success code as an int.
 	 * @see org.apache.hadoop.util.Tool#run(java.lang.String[])
 	 */
 	public int run(String[] args) throws IOException
 	{
 	    JobConf conf = new JobConf(MainRunner.class);  
 		
 		conf.setJobName("Splicer");
 		conf.setMapperClass(LineMapper.class);
 		conf.setMapOutputKeyClass(Text.class);
 		conf.setReducerClass(LineReducer.class);
 		conf.setOutputKeyClass(NullWritable.class);
 		conf.setOutputFormat(MultiOut.class);
 
 	     FileInputFormat.setInputPaths(conf, new Path(args[0]));  
 	     FileOutputFormat.setOutputPath(conf, new Path(args[1]));  
 		
 		JobClient.runJob(conf);
 		return 0;
 		
 	}
 	
 	/*
 	 * Prints out how the application should be invoked with an explanation.
 	 */
 	public static void printUsage()
 	{
 		System.err.println("Usage:");
		System.err.println("Splicer.java [[usage | help | -h] | <input> <output folder> [-k <numOfDistinctKeysxxxc| test]");
 		System.err.println("If no output folder is found, one will be created.");
 		System.err.println("Use usage or help  to print this message."); 
 	}
 	
 	
 	/*
 	 * Main method.  Just chooses what to do and what to run.
 	 */
 	public static void main(String[] args) throws Exception{
 		// TODO Auto-generated method stub
 
 		if(args[0].equals("test"))
 			org.junit.runner.JUnitCore.runClasses(UTests.class);
 		else if(args.length < 2)
 		{
 			printUsage();
 			System.exit(1);
 		}
 		else if (args[0].toLowerCase().equals("-h") || args[0].toLowerCase().equals("Usage") 
 				|| args[0].toLowerCase().equals("help") )
 		{
 			printUsage();
 			System.exit(0);
 		}
 		else if (args.length == 2)
 		{
 			int exitCode = ToolRunner.run(new MainRunner(), args);
 			System.exit(exitCode);
 		}
		else if (args.length == 3)
			System.out.println("Hey man, you're like totally missing a key value.  Please specify the key man.");
		else if (args.length == 4  && args[2].equals("-k"))
		{
			MAX_DISTINCT = Integer.parseInt(args[3]);
			int exitCode = ToolRunner.run(new MainRunner(), args);
			System.exit(exitCode);
		}
			
 		else
 		{
 			System.err.println("Unknown options!!");
 			printUsage();
 			System.exit(2);
 		}
 		
 		
 	}
 
 }
