 package step1;
 
 import java.io.IOException;
 
 import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.mapreduce.Reducer;
 
import constants.Constants;

 import structures.DisjointSet;
 
public class Reducer1 extends Reducer<IntWritable, LongWritable, IntWritable, LongWritable>{
 	@Override
 	/**
 	 * At the moment, this does nothing but emit what it's given
 	 */
	protected void reduce(IntWritable key, java.lang.Iterable<LongWritable> values,
			org.apache.hadoop.mapreduce.Reducer<IntWritable, LongWritable, IntWritable, 
				LongWritable>.Context context)
 					throws IOException, InterruptedException {
 	    DisjointSet set = new DisjointSet(Constants.groupLength * Constants.M);
//		for (LongWritable value : values) {
//			context.write(key,value);	
//		}
 	}
 }
