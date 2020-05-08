 package step1;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.logging.Logger;
 
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.mapreduce.Reducer;
 
 import structures.DisjointSet;
 import structures.MatrixUtilities;
 import structures.Tuple;
 import base.PingingReducer;
 import constants.Constants;
 
 public class Reducer1 extends
         PingingReducer<IntWritable, IntWritable, IntWritable, Tuple> {
 
     DisjointSet set;
     HashSet<Integer> memory;
     final int height = Constants.M;
     private final static Logger LOGGER = Logger.getLogger(Reducer1.class
             .getName());
 
     @Override
     protected void reduce(
             final IntWritable key,
             final Iterable<IntWritable> values,
             final Reducer<IntWritable, IntWritable, IntWritable, Tuple>.Context context)
             throws IOException, InterruptedException {
         if (Constants.DEBUG)
             LOGGER.info("Reducer1 - reduce called");
 
         set = new DisjointSet(Constants.groupSize);
         memory = new HashSet<Integer>(Constants.groupSize);
 
         final int group = key.get();
 
         // Pass 1 - Build a memory to "sort" the values
         for (final IntWritable value : values) {
             memory.add(value.get());
             if (Constants.DEBUG)
                 LOGGER.info("Reducer1 - pass 1 looped");
         }
 
         final int minP = MatrixUtilities.minInGroup(group);
         final int maxP = MatrixUtilities.maxInGroup(group);
 
         // Pass 2 - Unions
         for (int p = minP; p <= maxP; p++) {
             if (!memory.contains(p))
                 continue;
 
             if ((p > height) && memory.contains(p - height))
                 set.union(p, p - height);
 
             if (Constants.COMPUTE_DIAGONAL) {
                // Compute lower right diagonal
                 if ((p > height) && (p % height != 1)
                         && memory.contains(p - height - 1))
                     set.union(p, p - height - 1);
 
                 // Compute upper right diagonal
                 if ((p > height) && (p % height != 0)
                         && memory.contains(p - height))
                     set.union(p, p - height + 1);
             }
 
             if (((p % height) != 1) && memory.contains(p - 1))
                 set.union(p, p - 1);
             ping(context);
             if (Constants.DEBUG)
                 LOGGER.info("Reducer1 - pass 2 looped");
         }
 
         // Pass 3 - Find and output
         for (int p = minP; p <= maxP; p++) {
             if (!memory.contains(p))
                 continue;
 
             context.write(key, new Tuple(p, set.find(p)));
             if (Constants.DEBUG)
                 LOGGER.info("Reducer1 - pass 3 looped");
         }
         memory.clear();
     }
 }
