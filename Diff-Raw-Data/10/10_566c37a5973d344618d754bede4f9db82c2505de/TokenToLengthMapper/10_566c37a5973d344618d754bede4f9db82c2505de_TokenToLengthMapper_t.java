 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 
 public class TokenToLengthMapper extends
		Mapper<Object, Text, IntWritable, IntWritable> {
 	
 	private IntWritable one = new IntWritable(1);
 	
 	private IntWritable length = new IntWritable();
 	
	protected void map( Object key, Text value, Context context)
 			throws java.io.IOException, InterruptedException {
 		
		String word = value.toString().split("\t")[1];
		
		length.set(Integer.parseInt(word));
 		
 		//take key only
 		context.write(length, one);
 		
 	};
 }
