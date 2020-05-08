 package twitter;
 
 import java.io.IOException;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class FollowersCount extends Configured implements Tool {
 
     public static class CountFollowersMapper extends
            Mapper<Text, Text, Text, IntWritable> {
 
         private final static IntWritable one = new IntWritable(1);
 
         public void map(Text key, Text value, Context context)
                 throws IOException, InterruptedException {
            context.write(key, one);
         }
     }
 
     public static class IntSumReducer extends
            Reducer<Text, IntWritable, Text, IntWritable> {
        public void reduce(Text key, Iterable<IntWritable> values, Context context) 
                 throws IOException, InterruptedException {
             int sum = 0;
             for (IntWritable val : values) {
                 sum += val.get();
             }
             context.write(key, new IntWritable(sum));
         }
     }
 
     public int run(String[] args) throws Exception {
         if (args.length != 2) {
             System.err
                     .println("Usage: twitter.FollowersCount <input_path> <output_path>");
             return -1;
         }
 
         Job job = new Job(getConf());
         job.setJarByClass(FollowersCount.class);
        job.setJobName("amazing_followers_count");
 
         job.setMapperClass(CountFollowersMapper.class);
         job.setCombinerClass(IntSumReducer.class);
         job.setReducerClass(IntSumReducer.class);
 
        job.setMapOutputKeyClass(Text.class);
         job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(IntWritable.class);
 
         job.setInputFormatClass(KeyValueTextInputFormat.class);
         job.setOutputFormatClass(TextOutputFormat.class);
 
         FileInputFormat.addInputPath(job, new Path(args[0]));
         FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
         boolean success = job.waitForCompletion(true);
         return success ? 0 : 1;
     }
 
     public static void main(String[] args) throws Exception {
         int ret = ToolRunner.run(new FollowersCount(), args);
         System.exit(ret);
     }
 
 }
