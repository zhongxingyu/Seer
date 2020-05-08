 
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
 
 import colt.jet.random.Binomial;
 
 import laur.dm.ar.*;
 import laur.tools.Timer;
 
 public class MapClassBinomialSampler extends Mapper<LongWritable, Text, IntWritable, Text>
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
 		try
 		{
 			for (int i=0; i < REDUCER_NUM; i++)
 			{
 				int sampledTimes = Binomial.staticNextInt(DATASET_SIZE / REDUCER_NUM, 1.0 / DATASET_SIZE);
 				/**
 				 * XXX I assume there is a better way of doing
 				 * this, by only having one "message" sent to
 				 * reducer i, for example by making "value" an
 				 * object containing the fields "sampleTimes"
 				 * and "value". MR
 				 */
				f
 				for (int j=0; j < sampledTimes; j++)
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
