 /**************************************************************************************************************************
 
  * File: MRDriver.java
  * Authors: Justin A. DeBrabant (debrabant@cs.brown.edu)
 			Matteo Riondato (matteo@cs.brown.edu)
  * Last Modified: 12/27/2011
  
  * Description:
 	Driver for Hadoop implementation of parallel association rule mining.
  
  * Usage: java MRDriver <mapper id> <path to input database> <path to output local FIs> <path to output global FIs>
 	* mapper id - specifies which Map method should be used
 		1 for partition mapper, 2 for binomial mapper, 3 for weighted coin flip sampler 
 	* path to input database - path to file containing transactions in .dat format (1 transaction per line)
 	* local FI output - path to directory to write local (per-reducer) FIs
 	* global FI output - path to directory to write global FIs (combined from all local FIs)
  
 ***************************************************************************************************************************/
 
 import java.net.URI;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Random;
 
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.filecache.DistributedCache;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.DoubleWritable;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.MapWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.lib.IdentityMapper;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.SequenceFileInputFormat;
 import org.apache.hadoop.mapred.SequenceFileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class MRDriver extends Configured implements Tool
 {
 	public final int MR_TIMEOUT_MILLI = 60000000;
 	
 	public static void main(String args[]) throws Exception
 	{
 		if (args.length != 10)
 		{
 			System.out.println("usage: java MRDriver <epsilon> <delta> <minFreqPercent> <d> <datasetSize> <nodes> <mapper id> <path to input database> " + 
 							   "<path to output local FIs> <path to output global FIs>");
 			System.exit(1); 
 		}
 
 		int res = ToolRunner.run(new MRDriver(), args);
 
 		System.exit(res);
 	}
 
 	public int run(String args[]) throws Exception
 	{
 		long job_start_time, job_end_time; 
 		long job_runtime; 
 		float epsilon = Float.parseFloat(args[0]);
 		double delta = Double.parseDouble(args[1]);
 		int minFreqPercent = Integer.parseInt(args[2]);
 		int d = Integer.parseInt(args[3]);
 		int datasetSize = Integer.parseInt(args[4]);
 		int nodes = Integer.parseInt(args[5]);
 
 
 		/************************ Job 1 (local FIM) Configuration ************************/
 		
 		JobConf conf = new JobConf(getConf()); 
 
 		int numSamples = (int) Math.floor(0.95 * nodes * conf.getInt("mapred.tasktracker.reduce.tasks.maximum", nodes * 2));
 		int reqApproxNum = 0;
 		double phi = 0.25;
 		double low = 0.0;
 		double top = 0.50;
 		while (low <= top)
 		{
 		  	phi = (low + top) / 2;
 			reqApproxNum = (int) Math.ceil(Math.sqrt(numSamples*(1-phi)*2*Math.log(1/delta)) + 1);
 			reqApproxNum = Math.max(numSamples/2 +1, reqApproxNum);
 			if (reqApproxNum == Math.floor(numSamples*(1-phi)))
 			{
 			  	break;
 			}
 			if (reqApproxNum > numSamples*(1-phi))
 			{
 			  	top = phi;
 			}
 			if (reqApproxNum < numSamples*(1-phi))
 			{
 			  	low = phi;
 			}
 		}
 
 		int sampleSize = (int) Math.ceil((2 / Math.pow(epsilon, 2))*(d + Math.log(1/ phi)));
 
 		conf.setInt("PARMM.reducersNum", numSamples);
 		conf.setInt("PARMM.datasetSize", datasetSize);
 		conf.setInt("PARMM.minFreqPercent", minFreqPercent);
 		conf.setFloat("PARMM.epsilon", epsilon);
 			
 		conf.setNumReduceTasks(numSamples);
 
 		conf.setBoolean("mapred.reduce.tasks.speculative.execution", false); 
 		conf.setInt("mapred.task.timeout", MR_TIMEOUT_MILLI); 
 
 		conf.setJarByClass(MRDriver.class);
 			
 		conf.setMapOutputKeyClass(IntWritable.class); 
 		conf.setMapOutputValueClass(Text.class); 
 			
 		conf.setOutputKeyClass(Text.class); 
 		conf.setOutputValueClass(DoubleWritable.class); 
 
 		conf.setInputFormat(SequenceFileInputFormat.class);
 		SequenceFileInputFormat.addInputPath(conf, new Path(args[7]));
 		conf.setOutputFormat(SequenceFileOutputFormat.class);
 		SequenceFileOutputFormat.setOutputPath(conf, new Path(args[8]));
 
 		
 		// set the mapper class based on command line option
 		if(args[6].equals("1"))
 		{
 			System.out.println("running partition mapper..."); 
 			conf.setMapperClass(PartitionMapper.class);
 		}
 		else if(args[6].equals("2"))
 		{
 			System.out.println("running binomial mapper..."); 
 			conf.setMapperClass(BinomialSamplerMapper.class);
 		}
 		else if(args[6].equals("3"))
 		{
 			System.out.println("running coin mapper..."); 
 			conf.setMapperClass(CoinFlipSamplerMapper.class);
 		}
 		else if(args[6].equals("4"))
 		{
 			System.out.println("running sampler mapper..."); 
 			conf.setMapperClass(InputSamplerMapper.class);
 			
 			// create a random sample of size T*m
 			Random rand = new Random();
 			int[] samples = new int[numSamples * sampleSize];
 			for (int i = 0; i < numSamples * sampleSize; i++)
 			{
 				samples[i] = rand.nextInt(datasetSize);
 			}
 
 			// for each key in the sample, create a list of all T samples to which this key belongs
 			// XXX I wonder whether we could more efficiently create
 			// the MapWritable directly. MR
 			Hashtable<LongWritable, ArrayList<IntWritable>> hashTable = new Hashtable<LongWritable, ArrayList<IntWritable>>();
 			for (int i=0; i < numSamples * sampleSize; i++) 
 			{
 				ArrayList<IntWritable> sampleIDs = null;
 				LongWritable key = new LongWritable(samples[i]);
 				if (hashTable.contains(key))  
 					sampleIDs = hashTable.get(key);
 				else
 					sampleIDs = new ArrayList<IntWritable>();
 				sampleIDs.add(new IntWritable(i / sampleSize));
 				hashTable.put(key, sampleIDs);
 			}
 
 			MapWritable map = new MapWritable();
 			for (LongWritable key : hashTable.keySet())
 			{
 			  	ArrayList<IntWritable> sampleIDs = hashTable.get(key);
 				IntArrayWritable sampleIDsIAW = new IntArrayWritable();
 				sampleIDsIAW.set(sampleIDs.toArray(new IntWritable[1]));
 				map.put(key, sampleIDsIAW);
 			}
 
 			FileSystem fs = FileSystem.get(URI.create("samplesMap.ser"), conf);
 			FSDataOutputStream out = fs.create(new Path("samplesMap.ser"), true);
 			map.write(out);
 			out.sync();
 			out.close();
 			DistributedCache.addCacheFile(new URI(fs.getWorkingDirectory() + "/samplesMap.ser#samplesMap.ser"), conf);
 		}
 		else
 		{
 			// NOT REACHED
 		}
 		
 		// We don't use the default hash partitioner because we want to
 		// maximize the parallelism. That's why we also fix the number
 		// of reducers.
 		conf.setPartitionerClass(FIMPartitioner.class);
 
 		conf.setReducerClass(FIMReducer.class);
 			
 		job_start_time = System.currentTimeMillis(); 
 		JobClient.runJob(conf);
 		job_end_time = System.currentTimeMillis(); 
 			
 		job_runtime = (job_end_time-job_start_time) / 1000; 
 			
 		System.out.println("local FIM runtime (seconds): " + job_runtime);	
 	
 		/************************ Job 2 (aggregation) Configuration ************************/
 		
 		JobConf confAggr = new JobConf(getConf());
 
 		confAggr.setInt("PARMM.reducersNum", numSamples);
 		confAggr.setInt("PARMM.reqApproxNum", reqApproxNum);
		confAggr.setInt("PARMM.sampleSize", sampleSize);
 		confAggr.setFloat("PARMM.epsilon", epsilon);
 
 		confAggr.setBoolean("mapred.reduce.tasks.speculative.execution", false); 
 		confAggr.setInt("mapred.task.timeout", MR_TIMEOUT_MILLI); 
 
 		confAggr.setJarByClass(MRDriver.class);
 			
 		confAggr.setMapOutputKeyClass(Text.class); 
 		confAggr.setMapOutputValueClass(DoubleWritable.class); 
 			
 		confAggr.setOutputKeyClass(Text.class); 
 		confAggr.setOutputValueClass(Text.class); 
 			
 		confAggr.setMapperClass(IdentityMapper.class);
 		confAggr.setReducerClass(AggregateReducer.class);
 			
 		confAggr.setInputFormat(SequenceFileInputFormat.class);
 		SequenceFileInputFormat.addInputPath(confAggr, new Path(args[8]));
 
 		FileOutputFormat.setOutputPath(confAggr, new Path(args[9]));
 
 		job_start_time = System.currentTimeMillis(); 
 		JobClient.runJob(confAggr);
 		job_end_time = System.currentTimeMillis(); 
 			
 		job_runtime = (job_end_time-job_start_time) / 1000; 
 			
 		System.out.println("aggregation runtime (seconds): " +
 				job_runtime); 
 		 
 		return 0;
 	}
 }
 
