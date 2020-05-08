 package mx.itam.metodos.bf;
 
 import mx.itam.metodos.common.IntArrayWritable;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class BFLMDriver extends Configured implements Tool {
 
   public static final String N = "n";
   public static final String P = "p";
   public static final String OUT_PATH = "out-path";
 
   @Override
   public int run(String[] args) throws Exception {
     Configuration conf = getConf();
     Path data = new Path(args[0]);
     int rows = Integer.parseInt(args[2]);
     conf.setInt(N, rows);
     float err = Float.parseFloat(args[3]);
     conf.setFloat(P, err);
     conf.set(OUT_PATH, args[4]);
     String suffix = String.format("%s", rows);
     Path out = new Path(args[1] + "-" + suffix);
     out.getFileSystem(conf).delete(out, true);
     return (computeNGrams(data, out, conf)) ? 0 : 1;
   }
 
   private static boolean computeNGrams(Path data, Path out, Configuration conf)
           throws Exception {
     Job job = new Job(conf, "compute-ngrams");
     job.setJarByClass(BFLMDriver.class); 
     job.setMapperClass(TrainBFMapper.class);
     job.setMapOutputKeyClass(NullWritable.class);
     job.setMapOutputValueClass(IntArrayWritable.class);
     job.setReducerClass(TrainBFReducer.class);
     job.setNumReduceTasks(1);
     job.setOutputKeyClass(NullWritable.class);
     job.setOutputValueClass(NullWritable.class);
     job.setInputFormatClass(TextInputFormat.class);
     job.setOutputFormatClass(TextOutputFormat.class);
     FileInputFormat.setInputPaths(job, data);
     FileOutputFormat.setOutputPath(job, out);
     return job.waitForCompletion(true);
   }
 
   public static void main(String[] args) throws Exception {
     int res = ToolRunner.run(new Configuration(), new BFLMDriver(), args);
     System.exit(res);
   }
 }
