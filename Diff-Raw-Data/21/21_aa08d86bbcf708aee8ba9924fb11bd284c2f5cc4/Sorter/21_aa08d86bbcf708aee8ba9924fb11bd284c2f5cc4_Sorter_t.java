 package edu.nyu.cloud;
 
 import java.io.IOException;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.DoubleWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.util.GenericOptionsParser;
 
 public class Sorter
 {
     public static class SorterMapper extends Mapper<LongWritable, Text, DoubleWritable, Text>
     {
         @Override
         public void map(LongWritable key, Text value, Context context)
                 throws IOException, InterruptedException
         {
            StringTokenizer st = new StringTokenizer(value.toString(), PageRank.DELIMITER+"");
             String url = st.nextToken();
             double rank = Double.parseDouble(st.nextToken());
             context.write(new DoubleWritable(rank), new Text(url));
         }
     }
 
     public static void main(String[] args) throws IOException,
             ClassNotFoundException, InterruptedException
     {
         PageRank.printArray(args);
         Configuration conf = new Configuration();
         String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
         if (otherArgs.length != 3)
         {
             System.err.println("Usage: sort <in> <out>");
             System.exit(2);
         }
         Job job = new Job(conf, "sort");
         job.setJarByClass(Sorter.class);
         job.setMapperClass(SorterMapper.class);
        job.setOutputKeyClass(DoubleWritable.class);
         job.setOutputValueClass(Text.class);
         FileInputFormat.addInputPath(job, new Path(otherArgs[1]));
         FileOutputFormat.setOutputPath(job, new Path(otherArgs[2]));
         System.exit(job.waitForCompletion(true) ? 0 : 1);
     }
 
 }
