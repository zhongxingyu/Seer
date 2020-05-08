 package mx.itam.metodos.bf;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.BitSet;
 
 import mx.itam.metodos.common.IntArrayWritable;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.mapreduce.Reducer;
 
 public class TrainBFReducer extends Reducer<NullWritable, IntArrayWritable, NullWritable, NullWritable> {
 
   private BitSet filter = new BitSet();
   
   @Override
   public void reduce(NullWritable key, Iterable<IntArrayWritable> counts, Context context) throws IOException,
           InterruptedException {
     for (IntArrayWritable hashes : counts) {
       for (Writable h : hashes.get()) {
         IntWritable pos = (IntWritable) h;
         filter.set(pos.get());
       }
     }
    //context.write(key, new IntWritable(count));
   }
   
   @Override
   public void cleanup(Context context) throws IOException {
     Configuration conf = context.getConfiguration();
     Path modelPath = new Path(conf.get(BFLMDriver.OUT_PATH));
     FileSystem fs = modelPath.getFileSystem(conf);
     FSDataOutputStream out = fs.create(modelPath);
     ObjectOutputStream os = new ObjectOutputStream(out);
     os.writeObject(filter);
     os.close();
   }
 }
