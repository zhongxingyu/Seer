 package nl.tudelft.in4325.a1.normalization;
 
 import java.io.IOException;
 
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Reducer;
 
 /**
  * Reducer for Wikipedia XML corpus normalization
  */
public class NormalizationReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
	public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
     {
         int sum = 0;
        for(@SuppressWarnings("unused") IntWritable iw : values)
         {
            sum++;
         }
         context.write(key, new IntWritable(sum));
     }
 }
