 package nl.tudelft.in4325.a1.normalization;
 
 import java.io.IOException;
 
import nl.tudelft.in4325.a1.indexing.TextArrayWritable;

 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Reducer;
 
 /**
  * Reducer for Wikipedia XML corpus normalization
  */
public class NormalizationReducer extends Reducer<Text, TextArrayWritable, Text, IntWritable> {
	public void reduce(Text key, Iterable<TextArrayWritable> values, Context context) throws IOException, InterruptedException
     {
         int sum = 0;
        for(TextArrayWritable tw : values)
         {
            sum += tw.toStrings().length;
         }
         context.write(key, new IntWritable(sum));
     }
 }
