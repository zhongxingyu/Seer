 package step1;
 
 import java.io.IOException;
 
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.mapreduce.Reducer;
 
 import structures.DisjointSet;
import constants.Constants;
 
public class Reducer1 extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable>{
 	@Override
 	/**
 	 * At the moment, this does nothing but emit what it's given
 	 */
	protected void reduce(IntWritable key, Iterable<IntWritable> values,
			Reducer<IntWritable, IntWritable, IntWritable, IntWritable>. Context context)
 					throws IOException, InterruptedException {
 	    DisjointSet set = new DisjointSet(Constants.groupLength * Constants.M);
		for (IntWritable value : values) {
			context.write(key,value);
		}
 	}
 }
