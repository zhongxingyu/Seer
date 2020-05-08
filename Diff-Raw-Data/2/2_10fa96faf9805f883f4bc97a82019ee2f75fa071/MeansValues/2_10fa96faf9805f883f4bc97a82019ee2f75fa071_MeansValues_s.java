 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.WritableComparable;
 import org.apache.hadoop.io.WritableComparator;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 
 public class MeansValues extends Configured implements Tool {
 
     public static class IntPair implements WritableComparable<IntPair> {
         private int first = 0;
         private int second = 0;
 
         public IntPair(int first, int second) {
             set(first, second);
         }
 
         public void set(int left, int right) {
             first = left;
             second = right;
         }
 
         public int getFirst() {
             return first;
         }
 
         public int getSecond() {
             return second;
         }
 
         public void readFields(DataInput in) throws IOException {
             first = in.readInt() + Integer.MIN_VALUE;
             second = in.readInt() + Integer.MIN_VALUE;
         }
 
         public void write(DataOutput out) throws IOException {
             out.writeInt(first - Integer.MIN_VALUE);
             out.writeInt(second - Integer.MIN_VALUE);
         }
 
         @Override
         public int hashCode() {
             return first * 157 + second;
         }
 
         @Override
         public boolean equals(Object right) {
             if (right instanceof IntPair) {
                 IntPair r = (IntPair) right;
                 return r.first == first && r.second == second;
             } else {
                 return false;
             }
         }
 
         public static class Comparator extends WritableComparator {
             public Comparator() {
                 super(IntPair.class);
             }
 
             public int compare(byte[] b1, int s1, int l1,
                                byte[] b2, int s2, int l2) {
                 return compareBytes(b1, s1, l1, b2, s2, l2);
             }
         }
 
         // register this comparator
         static {
             WritableComparator.define(IntPair.class, new Comparator());
         }
 
         public int compareTo(IntPair o) {
             if (first != o.first) {
                 return first < o.first ? -1 : 1;
             } else if (second != o.second) {
                 return second < o.second ? -1 : 1;
             } else {
                 return 0;
             }
         }
     }
 
     public static class Map extends Mapper<Text, IntWritable, Text, IntPair> {
         @Override
         protected void setup(Context context) throws IOException, InterruptedException {
             super.setup(context);
         }
 
         @Override
         protected void map(Text key, IntWritable value, Context context) throws IOException, InterruptedException {
             context.write(key, new IntPair(value.get(), 1));
         }
 
         @Override
         protected void cleanup(Context context) throws IOException, InterruptedException {
             super.cleanup(context);
         }
     }
 
     // Input and output of the combiner must be the same and should be equal to the output of the mapper
     public static class Combine extends Reducer<Text, IntPair, Text, IntPair> {
         @Override
         protected void setup(Context context) throws IOException, InterruptedException {
             super.setup(context);
         }
 
         @Override
         protected void reduce(Text key, Iterable<IntPair> values, Context context) throws IOException, InterruptedException {
             int sum = 0, count = 0;
             for (IntPair value : values) {
                 sum += value.getFirst();
                 count += value.getSecond();
             }
             context.write(key, new IntPair(sum, count));
         }
 
         @Override
         protected void cleanup(Context context) throws IOException, InterruptedException {
             super.cleanup(context);
         }
     }
 
     public static class Reduce extends Reducer<Text, IntPair, Text, IntWritable> {
         @Override
         protected void setup(Context context) throws IOException, InterruptedException {
             super.setup(context);
         }
 
         @Override
         protected void reduce(Text key, Iterable<IntPair> values, Context context) throws IOException, InterruptedException {
             int sum = 0, count = 0;
             for (IntPair value : values) {
                 sum += value.getFirst();
                 count += value.getSecond();
             }
             int mean = sum / count;
             context.write(key, new IntWritable(mean));
         }
 
         @Override
         protected void cleanup(Context context) throws IOException, InterruptedException {
             super.cleanup(context);
         }
     }
 
     public int run(String[] args) throws Exception {
         Job job = new Job();
 
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(IntWritable.class);
 
         job.setMapperClass(Map.class);
        job.setCombinerClass(Reduce.class);
         job.setReducerClass(Reduce.class);
 
         job.setInputFormatClass(TextInputFormat.class);
         job.setOutputFormatClass(TextOutputFormat.class);
 
         FileInputFormat.setInputPaths(job, new Path(args[0]));
         FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
         job.setJarByClass(MeansValues.class);
 
 //        job.submit();
         job.waitForCompletion(true);
         return 0;
     }
 
 }
