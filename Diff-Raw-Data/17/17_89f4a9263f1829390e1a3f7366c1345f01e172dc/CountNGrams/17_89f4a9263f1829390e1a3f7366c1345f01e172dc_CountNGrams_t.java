 package mx.itam.metodos.mr;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class CountNGrams extends Configured implements Tool {
 
   public static final String N = "n";
 
   @Override
   public int run(String[] args) throws Exception {
     Configuration conf = getConf();
     Path data = new Path(args[0]);
     int rows = Integer.parseInt(args[2]);
     conf.setInt(N, rows);
     boolean analyze = Boolean.parseBoolean(args[3]);
     String suffix = String.format("%s", rows);
     Path out = new Path(args[1] + "-" + suffix);
     out.getFileSystem(conf).delete(out, true);
     return (computeNGrams(data, out, analyze, conf)) ? 0 : 1;
   }
 
   private static boolean computeNGrams(Path data, Path out, boolean analyze, Configuration conf) throws Exception {
     Job job = new Job(conf, "compute-ngrams");
     job.setJarByClass(CountNGrams.class);
     if (analyze) {
       job.setMapperClass(NGramsAnalyzerMapper.class);
     } else {
       job.setMapperClass(NGramsMapper.class);
     }
     job.setMapOutputKeyClass(Text.class);
     job.setMapOutputValueClass(IntWritable.class);
     job.setCombinerClass(NGramsReducer.class);
     job.setReducerClass(NGramsReducer.class);
     job.setOutputKeyClass(Text.class);
     job.setOutputValueClass(IntWritable.class);
     job.setInputFormatClass(TextInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);
     FileInputFormat.setInputPaths(job, data);
     FileOutputFormat.setOutputPath(job, out);
     return job.waitForCompletion(true);
   }
 
   public static void main(String[] args) throws Exception {
     int res = ToolRunner.run(new Configuration(), new CountNGrams(), args);
     System.exit(res);
   }
 }
