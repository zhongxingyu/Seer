 package us.yuxin.demo.hadoop.zero.userlogin;
 
 import java.io.IOException;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 
 public class ULMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
 	private Text username = new Text();
 	private IntWritable month = new IntWritable();
 
 	protected static int strToMonth(String str) {
 		if (str.length() <= 7)
 			return 0;
 		
 		return Integer.parseInt(str.substring(0, 4)) * 100 +
 			Integer.parseInt(str.substring(5, 7));
 	}
 	
 	@Override
 	protected void map(LongWritable key, Text value, Context context)
 			throws IOException, InterruptedException {
 		StringTokenizer tokenizer = new StringTokenizer(value.toString(), ",");
 		
		month.set(strToMonth(tokenizer.nextToken()));
 		tokenizer.nextToken();
 		tokenizer.nextToken();
		username.set(tokenizer.nextToken());
 		
 		context.write(username, month);
 	}
 }
