 import java.io.IOException;
 import java.util.*;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.conf.*;
 import org.apache.hadoop.util.*;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.mapred.*;
 public class Hog {
     public static class Functions {
         public static int fib(int n) {
             if (n == 0) {
                 int x = 5;
                 return x;
             }
              else if (n == 1) {
                 return 1;
             }
              else {
                 return Functions.fib(n - 1) + Functions.fib(n - 2);
             }
          }
         public static int factorial(int n) {
             if (n == 0 || n == 1) {
                 return 1;
             }
              else {
                 return n * Functions.factorial(n - 1);
             }
          }
         public static int max(int x, int y) {
             if (x >= y) {
                 return x;
             }
              else {
                 return y;
             }
          }
         public static int tooManyArgs(int x, List<Integer> y, Set<List<Integer>> foo, double percentage, boolean doNothing) {
             if (!doNothing) {
                 return 1;
             }
             return 1;
          }
         public static List<Integer> reverseList(List<Integer> oldList) {
             List<Integer> newList = new ArrayList<Integer>();
             for (int i = oldList.size() - 1; i >= 0; i--) {
                 newList.add(oldList.get(i));
              }
             return newList;
          }
     }
     public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
         public void map(LongWritable lineNum, Text value,  OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
             String line = value.toString();
            int a = 5;
             for (String word : line.split(" ")) {
                 output.collect(new Text(word), new IntWritable(1));
              }
         }
     }
     public static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
         public void reduce(Text word, Iterator<IntWritable> values,  OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
             int count = 0;
             while (values.hasNext()) {
                 count = count + values.next().get();
              }
             output.collect(new Text(word), new IntWritable(count));
         }
     }
     public static void main(String[] args) throws Exception {
         JobConf conf = new JobConf(Hog.class);
         conf.setJobName("hog");
         conf.setOutputKeyClass(Text.class);
         conf.setOutputValueClass(IntWritable.class);
         conf.setMapperClass(Map.class);
         conf.setCombinerClass(Reduce.class);
         conf.setReducerClass(Reduce.class);
         conf.setInputFormat(TextInputFormat.class);
         conf.setOutputFormat(TextOutputFormat.class);
         FileInputFormat.setInputPaths(conf, new Path(args[0]));
         FileOutputFormat.setOutputPath(conf, new Path(args[1]));
         JobClient.runJob(conf);
     }
 }
