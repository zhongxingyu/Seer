 package step1;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 
 import test.Util;
 import constants.Constants;
 
 public class Step1 {
 
     public void run(final String inputPath) throws Exception {
         final Configuration conf = new Configuration();
 
         final Job job = new Job(conf, "Step1");
         job.setJarByClass(Step1.class);
 
         job.setOutputKeyClass(IntWritable.class);
         job.setOutputValueClass(IntWritable.class);
 
         job.setMapperClass(Map1.class);
         job.setReducerClass(Reducer1.class);
 
         job.setPartitionerClass(GroupPartitioner.class);
 
         job.setInputFormatClass(TextInputFormat.class);
         job.setOutputFormatClass(TextOutputFormat.class);
 
         FileInputFormat.addInputPath(job, new Path(inputPath));
 
         Util.deleteDir(Constants.reducer1OutputDir);
         FileOutputFormat.setOutputPath(job, new Path(
                 Constants.reducer1OutputDir));
 
         job.waitForCompletion(true);
     }
		step1.run("data/appendix_test_files/data6-36.txt");
 }
