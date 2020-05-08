 package project2;
 
 import java.io.IOException;
 import java.util.*;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 
 /**
  * Simple MapReduce Over Nodes
  *
  */
 
 public class NodeDriver {
 	
 	/*
 	 * There is a pre-processed file called Preprocess_76.txt made by a Python Script that Matthew did that rejects 
 	 * 0.998754% of the entries in edges.txt.
 	 * fromNetID = 0.76
 	 * rejectMin = 0.99 * fromNetID;
 	 * rejectLimit = rejectMin + 0.01;
 	 * 
 	 * Reject Min: 0.752400
 	 * Reject Limit: 0.762400
 	 */
 	
 	// use a hadoop counter to track the total residual error so we can compute the average at the end
 	public static enum ProjectCounters {
 	    RESIDUAL_ERROR
 	};
 	public static final int totalNodes = 685230;	// total # of nodes in the input set
	public static final int precision = 10000;	// this allows us to store the error in the counter as an int
     //TODO: change to 5 once round 1 is working
 	private static final int NUM_ITERATIONS = 1; // # of iterations to run
     
 	public static void main(String[] args) throws Exception {

 		
		String inputFile = "../Pre-Process Text Files/PreprocessFinalFile.txt";
		String outputPath = "OutputFolder/";
 
         for (int i = 0; i < (NUM_ITERATIONS-1); i++) {
             Job job = new Job();
             // Set a unique job name
             job.setJobName("pagerank_"+ (i+1));
             job.setJarByClass(project2.NodeDriver.class);
 
             // Set Mapper and Reducer class
             job.setMapperClass(project2.LeMapper.class);
             job.setReducerClass(project2.LeReducer.class);
             
             // set the classes for output key and value
             job.setOutputKeyClass(Text.class);
             job.setOutputValueClass(Text.class);
             
             // on the initial pass, use the preprocessed input file
             // note that we use the default input format which is TextInputFormat (each record is a line of input)
             if (i == 0) {
                 FileInputFormat.addInputPath(job, new Path(inputFile)); 	
             // otherwise use the output of the last pass as our input
             } else {
            	FileInputFormat.addInputPath(job, new Path(outputPath + "temp"+i)); 
             }
             // set the output file path
            FileOutputFormat.setOutputPath(job, new Path(outputPath + "temp"+(i+1)));
             
             // execute the job and wait for completion before starting the next pass
             job.waitForCompletion(true);
             
             // before starting the next pass, compute the avg residual error for this pass and print it out
            float residualErrorSum = job.getCounters().findCounter(ProjectCounters.RESIDUAL_ERROR).getValue() / precision;
            Float residualErrorAvg = new Float(residualErrorSum / totalNodes);
            System.out.println("Residual error for iteration " + i + ": " + residualErrorAvg.toString());
             
         }
         
     }
 	
 	
 }
