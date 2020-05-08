 package mx.itam.metodos.invertedindex;
 
 import java.io.IOException;
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.List;
 
 import org.apache.hadoop.io.ArrayWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Reducer;
 
 public class IndexReducer extends Reducer<Text, PositionalPosting, Text, ArrayWritable> {
 
   public void reduce(Text key, Iterable<PositionalPosting> values, Context context) throws IOException,
           InterruptedException {
     List<PositionalPosting> postingList = new ArrayList<PositionalPosting>();
     for (PositionalPosting val : values) {
       postingList.add(new PositionalPosting(val));
     }
    Collections.sort(postingList);
     ArrayWritable result = new PositionalPostingArrayWritable(postingList.toArray(new PositionalPosting[0]));
     context.write(key, result);
   }
 }
