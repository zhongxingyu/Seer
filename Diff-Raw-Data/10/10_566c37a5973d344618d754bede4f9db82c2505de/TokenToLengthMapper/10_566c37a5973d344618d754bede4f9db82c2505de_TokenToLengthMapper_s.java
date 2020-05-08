 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 
 public class TokenToLengthMapper extends
		Mapper<Text, Text, IntWritable, IntWritable> {
 	
 	private IntWritable one = new IntWritable(1);
 	
 	private IntWritable length = new IntWritable();
 	
	protected void map( Text key, Text value, Context context)
 			throws java.io.IOException, InterruptedException {
 		
		length.set(Integer.parseInt(key.toString()));
 		
 		//take key only
 		context.write(length, one);
 		
 	};
 }
