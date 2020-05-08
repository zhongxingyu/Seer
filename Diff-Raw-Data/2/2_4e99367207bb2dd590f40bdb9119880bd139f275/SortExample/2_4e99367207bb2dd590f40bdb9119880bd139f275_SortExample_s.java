 package sort;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 
 import java.io.IOException;
 
 /**
  * User: ChenLong
  * Created Date: 9/18/13 6:49 下午
  * Description:
  */
 public class SortExample {
     public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
         Job job = new Job();
 

         job.setMapperClass(Map.class);
         job.setReducerClass(Reduce.class);
 
         job.setInputFormatClass(TextInputFormat.class);
         job.setOutputKeyClass(IntWritable.class);
         job.setOutputValueClass(Text.class);
 
         FileInputFormat.setInputPaths(job, new Path(args[0]));
         FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
         System.exit(job.waitForCompletion(true) ? 0 : 1);
 
     }
 }
