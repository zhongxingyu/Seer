 package invertedIndex;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Reducer;
 
 import dos.*;
 
 public class ReduceInvert extends Reducer<IntWritable, WeightWritable, IntWritable, WeightArrayWritable> {
 	
 	@Override
 	public void reduce(IntWritable key, Iterable<WeightWritable> values, Context context) throws IOException, InterruptedException {
		WeightWritable[] weights = new WeightWritable[50];
		int size = 50;
		int i = 0;
 		
 		for (WeightWritable weight : values) {
 			
			if(weights.length < size){
				size += 50;
				WeightWritable[] tmpWeight = new WeightWritable[size];
				System.arraycopy(tmpWeight, 0, weights, 0, weights.length);
			}
			weights[i++] = new WeightWritable(weight);
 		}
		
 		WeightArrayWritable output = new WeightArrayWritable(); 
		output.set(weights);
 		
 		context.write(key, output);
 	}
 }
