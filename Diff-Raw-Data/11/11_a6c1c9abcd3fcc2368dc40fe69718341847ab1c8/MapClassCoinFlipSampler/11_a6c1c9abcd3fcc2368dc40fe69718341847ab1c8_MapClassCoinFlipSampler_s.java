 
 import java.util.*; 
 import java.io.*; 
 import java.lang.Math;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.conf.*;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.mapreduce.*;
 import org.apache.hadoop.mapreduce.lib.input.*;
 import org.apache.hadoop.mapreduce.lib.output.*;
 import org.apache.hadoop.util.*;
 
 import laur.dm.ar.*;
 import laur.tools.Timer;
 
 public class MapClassCoinFlipSampler extends Mapper<LongWritable, Text, IntWritable, Text>
 {
 	/**
 	 * XXX It would be great if we could set these parameters at runtime and
 	 * then read them from the Configuration. MR
 	 */
 	public static final int REDUCER_NUM = 64; 
	public static final int DATASET_SIZE = 1000000
 	
 	@Override
 	public void map(LongWritable lineNum, Text value, Context context) throws IOException, InterruptedException
 	{
 		Random rand = new Random();
 		
 		try
 		{
 			for (int i=0; i < REDUCER_NUM; i++)
 			{
 				double f = rand.nextDouble();
 				if (f <= 1.0 / DATASET_SIZE)
 				{
 					context.write(new IntWritable(i), value);
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			System.out.println(e.getMessage()); 
 		}
 	}
 }
