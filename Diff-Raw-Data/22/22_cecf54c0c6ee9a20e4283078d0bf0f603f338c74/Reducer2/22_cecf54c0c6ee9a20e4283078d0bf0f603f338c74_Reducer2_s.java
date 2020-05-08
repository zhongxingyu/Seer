 package step2;
 
 import java.io.IOException;
 
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.mapreduce.Reducer;
 
 import structures.DisjointSet;
 import constants.Constants;
 
 public class Reducer2 extends
         Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
 
     @Override
     protected void reduce(
             final IntWritable key,
             final Iterable<IntWritable> qs,
             final Reducer<IntWritable, IntWritable, IntWritable, IntWritable>.Context context)
             throws IOException, InterruptedException {
 
         DisjointSet set = new DisjointSet((2 * Constants.g - 2) * Constants.M);
 
        int lastQ = 0;

        for (final IntWritable q : qs) {
             set.union(key.get(), q.get());
            lastQ = q.get();
        }
 
        context.write(key, new IntWritable(set.find(lastQ)));
     }
 }
