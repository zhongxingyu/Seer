 package step2;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.mapreduce.Reducer;
 
 import structures.DisjointSet;
 import structures.Tuple;
 import constants.Constants;
 
 public class Reducer2 extends
         Reducer<IntWritable, Tuple, IntWritable, IntWritable> {
 
    DisjointSet set = new DisjointSet((2 * Constants.g - 2) * Constants.M);
     ArrayList<Tuple> inputList = new ArrayList<Tuple>();
     
     @Override
     protected void reduce(
             final IntWritable key,
             final Iterable<Tuple> tuples,
             final Reducer<IntWritable, Tuple, IntWritable, IntWritable>.Context context)
             throws IOException, InterruptedException {
 
         Iterator<Tuple> iter = tuples.iterator();
         while(iter.hasNext()) {
             Tuple t1 = new Tuple(iter.next());
             set.union(t1.getFirst(), t1.getSecond());
             if(iter.hasNext()) {
                 Tuple t2 = new Tuple(iter.next());
                 set.union(t2.getFirst(), t2.getSecond());
             }
             inputList.add(t1);
         }
 
        int group = key.get();
        if(group == Constants.numGroups - 2) { //FIXME!
            for(Tuple tup : inputList)
                context.write(new IntWritable(tup.getFirst()), new IntWritable(set.find(tup.getFirst())));
        }
     }
 }
