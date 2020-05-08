 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.mapreduce.Reducer;
 
 public class IntIntSumReducer extends
 		Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
 
 	
 	private IntWritable count = new IntWritable();
 	
 	protected void reduce(IntWritable key, Iterable<IntWritable> values,
 			Context ctx) throws java.io.IOException, InterruptedException {
 		
 		int i = 0;
 		for(IntWritable value: values){
			i+=value.get();
 		}
 		count.set(i);
 		ctx.write(key,count);
 		
 	};
 }
