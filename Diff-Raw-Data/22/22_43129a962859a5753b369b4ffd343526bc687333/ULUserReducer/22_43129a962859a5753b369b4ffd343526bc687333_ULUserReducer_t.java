 package us.yuxin.demo.hadoop.zero.userlogin;
 
 import java.io.IOException;
 
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Reducer;
 
 public class ULUserReducer extends
 		Reducer<Text, IntWritable, IntWritable, ULDistribution> {
 	private IntWritable first = new IntWritable();
 
 	@Override
 	protected void reduce(Text key, Iterable<IntWritable> values, Context context)
 			throws IOException, InterruptedException {
 		
 		ULDistribution uld = new ULDistribution();
 		for (IntWritable month : values) {
 			uld.add(month.get());
 		}
		//first.set(uld.firstLogin());
		first = new IntWritable(uld.firstLogin());
 		context.write(first, uld);
 	}
 }
